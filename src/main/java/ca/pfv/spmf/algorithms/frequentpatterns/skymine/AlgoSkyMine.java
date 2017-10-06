package ca.pfv.spmf.algorithms.frequentpatterns.skymine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This is an implementation of the SKYMine algorithm, which was proposed in the following
 * conference paper: <br/><br/>
 * 
 * Goyal, V., Sureka, A., & Patel, D. (2015). Efficient Skyline Itemsets Mining. 
 * In Proceedings of the Eighth International C* Conference on Computer Science & Software 
 * Engineering (pp. 119-124). ACM.
 * <br/><br/>
 * 
 * Copyright (c) 2015 Vikram Goyal, Ashish Sureka, Dhaval Patel, Siddharth Dawar

 * This file is part of the SPMF DATA MINING SOFTWARE *
 * (http://www.philippe-fournier-viger.com/spmf).
 * 
 * SPMF is free software: you can redistribute it and/or modify it under the *
 * terms of the GNU General Public License as published by the Free Software *
 * Foundation, either version 3 of the License, or (at your option) any later *
 * version. SPMF is distributed in the hope that it will be useful, but WITHOUT
 * ANY * WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @see AlgoSkyMine
 * @see UPTree
 * 
 * @author Vikram Goyal, Ashish Sureka, Dhaval Patel, Siddharth Dawar
 */

public class AlgoSkyMine {

	// variable for statistics
	/** the time when the algorithm started **/
	private long startTimestamp = 0; 
	/** the time when the algorithm terminated **/
	private long endTimestamp = 0; 
	
	/** map to store the TWU of each item  Key: item  Value : TWU */
	private Map<Integer, Long> mapItemToTWU;
	
	/** map to store the utility of each item  Key: item  Value : internal utility value */
	public static  Map<Integer, Long> mapItemUtility;
	
	/** map to store the minimum utility of each item  Key: item  Value : minimum utility */
	private Map<Integer, Long> mapMinimumItemUtility;
	
	/** map to store the maximum utility of each item  Key: item  Value : maximum utility */
	private Map<Integer, Long> mapMaximumItemUtility;
	
	/** variable to store the header list */
	static ArrayList<Integer> headerlist;
	
	/** Map to store the Item summary of each item (utility, support, min/max frequencies, etc.) */
	static private HashMap<Integer, ItemSummary> itemDetail;

	// =========  Data structure for skyline mining ===========================//
	
	/** Array of utility frequencies (utility /support pairs) **/
	UtilitySupport[][] itempairsUtilityMatrix = null;
	
	/** Number of items */
	int number_items = 0;
	
	/** count array */
	int[][] countArray = null;
	long numberInsertedPatterns = 0, numberVerifiedPatterns = 0, numberOfSkylineItemsets = 0;

	/** Candidate set */
	ParetoSet candidateSet;
	
	/** Result set */
	ParetoSet resultSet;
	
	/** Structure to store potential High utility itemsets */
	private List<Itemset> phuis;
	
	/** writer to write the output file **/
	private BufferedWriter writer = null;

	/**
	 * Method to run the algorithm
	 * 
	 * @param transactionFile
	 *            path to an input file containing transactions with quantities
	 * @param utilityTableFile
	 *            path to an input file containing a utility table indicating the internal utility of each item
	 * @param outputFilePath
	 *            path for writing the output file (if null the result is kept in memory and not saved to file)
	 * @param minUtility
	 *            the minimum utility threshold
	 * @param usePreInsertingSingleAndPairs if true, use the strategy of pre-inserting single and pairs of items
	 * @param useRaisingUMinByNodeUtilities if true, use the strategy of raising UMin by node utility
	 * @throws IOException exception if error while reading or writing the file
	 */
	public void runAlgorithm(String transactionFile, String utilityTableFile, String outputFilePath, boolean usePreInsertingSingleAndPairs,
			boolean useRaisingUMinByNodeUtilities)
			throws IOException {
		
		// Start the Memory Logger
		MemoryLogger.getInstance().reset();

		// Record the start time
		startTimestamp = System.currentTimeMillis();
		
		// initialize candidate set
		candidateSet = new ParetoSet();
		
		// initialize result set
		resultSet = new ParetoSet();
		
		// Initialize map to store the minimum utility of each item
		mapMinimumItemUtility = new HashMap<Integer, Long>();
		
		// Initialize map to store the maximum utility of each item
		mapMaximumItemUtility = new HashMap<Integer, Long>();
		
		// Initialize map to store the TWU of each item  Key: item  Value : TWU 
		mapItemToTWU = new HashMap<Integer, Long>();
		
		// Initialize map to store the utility of each item  Key: item  Value : internal utility value 
		mapItemUtility = new HashMap<Integer, Long>();
		
		// Initialize variable to store the header list 
		headerlist = new ArrayList<Integer>();
		
		// Initialize map to store the Item summary of each item (utility, support, min/max frequencies, etc.) 
		 itemDetail = new HashMap<Integer, ItemSummary>();
		 
		// Initialize structure to store potential High utility itemsets */
		phuis = new ArrayList<Itemset>();
		
		if(outputFilePath != null){
			writer = new BufferedWriter(new FileWriter(outputFilePath));
		}

		// read the utility table in the first file
		BufferedReader myInput = null;
		String thisLine;
		// reading the utility table
		try {
			myInput = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(utilityTableFile))));
			// For each line in the file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is a comment, is empty or is a kind of metadata
				if (thisLine.isEmpty() == true || thisLine.charAt(0) == '#'
						|| thisLine.charAt(0) == '%'
						|| thisLine.charAt(0) == '@') {
					continue;
				}
				// Split the like into two tokens (item and utility)
				StringTokenizer dataLine = new StringTokenizer(thisLine);
				int item = Integer.parseInt(dataLine.nextToken());
				long internalUtility = Long.parseLong(dataLine.nextToken());
				
				// increase the number of distinct items  by 1
				number_items++;
				
				// utility = 1.0f/utility;
				mapItemUtility.put(item, internalUtility);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// =============================================================================
		// Create a two-dimensional array for counting
		countArray = new int[number_items][2];
		// Create a matrix to count the utility and frequencies of pairs of items
		// This matrix is initialize with zero and will be filled when scaning the database
		itempairsUtilityMatrix = new UtilitySupport[number_items + 1][number_items + 1];
		for (int i = 0; i <= number_items; i++) {
			for (int j = i + 1; j <= number_items; j++) {
				itempairsUtilityMatrix[i][j] = new UtilitySupport(0, 0);
			}
		}
		// =============================================================================

		// Prepare to read the transaction file
		try {
			// prepare the object for reading the file
			myInput = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(transactionFile))));
			// for each line (transaction) until the end of file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is a comment, is empty or is a kind of metadata
				if (thisLine.isEmpty() == true || thisLine.charAt(0) == '#'
						|| thisLine.charAt(0) == '%'
						|| thisLine.charAt(0) == '@') {
					continue;
				}
				StringTokenizer dataLine = new StringTokenizer(thisLine);
				int numberOfTokens = dataLine.countTokens();

				// Empty line or end of file found, break
				if (numberOfTokens == 0)
					break;

				long transactionUtility = 0;
				ArrayList<String> items = new ArrayList<String>();
				ArrayList<Short> quantityValues = new ArrayList<Short>();

				for (int tokenCounter = 0; tokenCounter < numberOfTokens; tokenCounter++) {
					String temp[] = dataLine.nextToken().split(":");
					items.add(temp[0]);

					Integer item = new Integer(temp[0]).intValue();
					Short quantity = new Short(temp[1]).shortValue();

					// ===========================================
					quantityValues.add(quantity);
					// ===========================================

					// get the current Minimum Item Utility of that item
					Long minItemUtil = mapMinimumItemUtility.get(item);
					Long maxItemUtil = mapMaximumItemUtility.get(item);

					// Calculate the utility of the item in that transaction
					long utility = quantity	* mapItemUtility.get(item);

					// add the utility of the item to the transaction utility
					transactionUtility += utility;
					
					// update the minimum utility of that item
					if ((minItemUtil == null)
							|| (minItemUtil >= utility)) {
						mapMinimumItemUtility.put(item,	utility);
					}
					
					// update the maximum utility of that item
					if ((maxItemUtil == null)
							|| (maxItemUtil < utility)) {
						mapMaximumItemUtility.put(item, utility);
					}

					// Adding ItemSummary Code
					if (!itemDetail.containsKey(item)) {

						ItemSummary summary = new ItemSummary(item);
						itemDetail.put(item, summary);
					}
					itemDetail.get(item).incrementSupp();
					itemDetail.get(item).updateTotalFrequency(quantity);
					itemDetail.get(item).updateMinFrequency(quantity);
					itemDetail.get(item).updateMaxFrequency(quantity);

				}
				for (String itlocal : items) {
					Integer it = Integer.parseInt(itlocal);
					Long twu = mapItemToTWU.get(it);
					twu = (twu == null) ? transactionUtility : twu
							+ transactionUtility;
					mapItemToTWU.put(it, twu);
				}
				// ================================================
				UpdatePairsUtility(items, quantityValues);
				// ================================================

			}

			// ===========================================
			orderCountArray(countArray);
			// ===========================================
		} catch (Exception e) {
			// catches exception if error while reading the input file
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}

		// =================================================================

		if (usePreInsertingSingleAndPairs){
			updateParetoSingleDoubleItems();
		}


		// ******************************************
		// second database scan generate revised transaction and global UP-Tree
		// and calculate the minimum utility of each item
		// (required by the DLU(Decreasing Local Unpromizing items) strategy)
		// long before_tree_creation=System.currentTimeMillis();
		long temp_util;
		ArrayList<Long> utilityValues;
		try {
			UPTree tree = new UPTree();
			// System.out.println("Creating UP-Tree");
			// prepare the object for reading the file
			myInput = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(transactionFile))));

			// Transaction ID to track transactions
			// for each line (transaction) until the end of file
			// startTimestamp = System.currentTimeMillis();
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is a comment, is empty or is a kind of metadata
				if (thisLine.isEmpty() == true || thisLine.charAt(0) == '#'
						|| thisLine.charAt(0) == '%'
						|| thisLine.charAt(0) == '@') {
					continue;
				}

				// ************************************************
				StringTokenizer dataLine = new StringTokenizer(thisLine);
				int numberOfTokens = dataLine.countTokens();

				// Empty line or end of file found, break
				if (numberOfTokens == 0)
					break;

				ArrayList<String> items = new ArrayList<String>();
				ArrayList<Short> quantityValues = new ArrayList<Short>();
				utilityValues = new ArrayList<Long>();
				for (int tokenCounter = 0; tokenCounter < numberOfTokens; tokenCounter++) {
					// System.out.println(dataLine.nextToken());
					String temp[] = dataLine.nextToken().split(":");
					// System.out.println(new Integer(temp[0]).intValue());
					items.add(temp[0]);
					Integer number = Integer.parseInt(temp[0]);
					Short quantity = Short.parseShort(temp[1]);
					temp_util = quantity * mapItemUtility.get(number);
					utilityValues.add(temp_util);
					quantityValues.add(quantity);
				}

				long remainingUtility = 0;
				int itm;
				Item element;
				// Create a list to store items
				List<Item> revisedTransaction = new ArrayList<Item>();

				for (int i = 0; i < items.size(); i++) {
					// convert values to integers

					itm = Integer.parseInt(items.get(i));
					long utility = utilityValues.get(i);
					short quan = quantityValues.get(i);

					if (mapItemToTWU.get(itm) >= candidateSet.getUtility(itemDetail.get(itm).support)){
						element = new Item(itm, utility, quan);
						// add it
						revisedTransaction.add(element);
						remainingUtility += utility;
					}
				}

				// revised transaction in desceding order of TWU
				Collections.sort(revisedTransaction, new Comparator<Item>() {
					public int compare(Item o1, Item o2) {
						return compareItemsDesc(o1.itemName, o2.itemName, mapItemToTWU);
					}
				});

				// add transaction to the global UP-Tree
				tree.addTransaction(revisedTransaction, remainingUtility);
			}

			// We create the header table for the global UP-Tree
			tree.createHeaderList(mapItemToTWU);
			
			// System.out.println("Mining UP-tree");
			if (useRaisingUMinByNodeUtilities)
				generateTreeItemSets(tree.root, new int[0]);

			// Mine tree with UPGrowth with 2 strategies DLU and DLN
			upgrowth_plus(tree, new int[0], null);

			endTimestamp = System.currentTimeMillis();

		} catch (Exception e) {
			// catches exception if error while reading the input file
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}

		// Verification process starts
		// Adding candidate patterns to PHUI Itemset list and will compute its
		// exact utility by scanning the database again
		for (int j = candidateSet.utilities.size() - 1; j >= 1; j--) {
			ArrayList<int[]> itemsets = candidateSet.getUtilities().get(j).getItemSets();
			for (int i = 0; i < itemsets.size(); i++) {
				int[] items_i = itemsets.get(i);
				if (items_i.length > 0) {
					this.numberVerifiedPatterns++;

					int[] sorted_itemset = new int[items_i.length];
					System.arraycopy(items_i, 0, sorted_itemset, 0,
							items_i.length);
					Arrays.sort(sorted_itemset);
					Itemset new_itemset = new Itemset(sorted_itemset);
					this.phuis.add(new_itemset);

				}
			}
		}
		
		// check the memory usage (for statistics)
		MemoryLogger.getInstance().checkMemory();

		try {
			// prepare the object for reading the file
			myInput = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(transactionFile))));

			// for each line (transaction) until the end of file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is a comment, is empty or is a kind of metadata
				if (thisLine.isEmpty() == true || thisLine.charAt(0) == '#'
						|| thisLine.charAt(0) == '%'
						|| thisLine.charAt(0) == '@') {
					continue;
				}

				StringTokenizer dataLine = new StringTokenizer(thisLine);
				int numberOfTokens = dataLine.countTokens();

				// Empty line or end of file found, break
				if (numberOfTokens == 0)
					break;

				ArrayList<String> items = new ArrayList<String>();
				utilityValues = new ArrayList<Long>();
				for (int tokenCounter = 0; tokenCounter < numberOfTokens; tokenCounter++) {
					// System.out.println(dataLine.nextToken());
					String temp[] = dataLine.nextToken().split(":");
					// System.out.println(new Integer(temp[0]).intValue());
					items.add(temp[0]);
					Integer number = Integer.parseInt(temp[0]);
					Short quantity = Short.parseShort(temp[1]);
					temp_util = quantity * mapItemUtility.get(number);
					utilityValues.add(temp_util);
					// transactionUtility+=quantity*utilityMap.get(number);
				}

				// Create a list to store items
				List<Item> revisedTransaction = new ArrayList<Item>();
				for (int i = 0; i < items.size(); i++) {
					// / convert values to integers
					int item = Integer.parseInt(items.get(i));
					long utility = utilityValues.get(i);

					Item element = new Item(item, utility);
					revisedTransaction.add(element);

				}

				// sort the transaction by lexical order
				// for faster comparison since PHUIs have been sorted
				// by lexical order and this will make faster
				// comparison
				Collections.sort(revisedTransaction, new Comparator<Item>() {
					public int compare(Item o1, Item o2) {
						return o1.itemName - o2.itemName;
					}
				});

				// Compare each itemset with the transaction
				for (Itemset itemset : phuis) {
					// OPTIMIZATION:
					// if this itemset is larger than the current transaction
					// it cannot be included in the transaction, so we stop
					// and we don't need to consider the folowing itemsets
					// either since they are ordered by increasing size.
					if (itemset.size() > revisedTransaction.size()) {
						break;
					}

					// Now check if itemset is included in the transaction
					// and if yes, update its utility
					updateExactUtility(revisedTransaction, itemset);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// OUTPUT ALL HUIs
		for (Itemset itemset : phuis) {
			// System.out.println(Arrays.toString(itemset.itemset));
			resultSet.insert(itemset.itemset, itemset.getExactUtility(),
					itemset.getExactUtility(), itemset.support);
		}


		

		// check the memory usage (for statistics)
		MemoryLogger.getInstance().checkMemory();

		// if the user chooose to save the results to a file
		// save the result
		saveResultToFile();
		// close the file
		writer.close();

		// record end time
		endTimestamp = System.currentTimeMillis();

		// Release some memory
		phuis.clear();
		mapMinimumItemUtility = null;

		// this.tempcandidateBufferWriter.close();
		// this.tempwriter.close();
		mapMinimumItemUtility = null;
		mapMaximumItemUtility = null;

	}


	/** Generates itemsets by scanning original FP tree */
	/**
	 * 
	 * @param root
	 *            of the FP-Tree
	 */
	public void generateTreeItemSets(UPNode root, int[] prefix) {

		if (root != null) {
			List<UPNode> children = root.childs;
			for (int i = 0; i < children.size(); i++) {
				getBFSItemsets(children.get(i), prefix, prefix);
			}
		}
	}

	/**
	 * 
	 * @param root
	 *            root of subtree
	 * @param items
	 *            prefix of tree till root node
	 */

	public void getBFSItemsets(UPNode root, int[] items, int[] prefix) {
		long utilityL = 0;
		if (root != null) {
			int[] itemset = realloc1(items, root.itemID);
			if (prefix.length > 0) {
				// utilityH = getHighUtilityValue(itemset, root.node.itemCount);
				utilityL = getLowUtilityValue(itemset, root.count);
			}
			this.numberInsertedPatterns++;
			candidateSet
					.insert(itemset, utilityL, root.nodeUtility, root.count);
			if (root.childs != null) {
				List<UPNode> children = root.childs;
				for (int i = 0; i < children.size(); i++) {
					getBFSItemsets(children.get(i), itemset, prefix);
				}
			}
		}

	}

	public void updateParetoSingleDoubleItems() {
		// i and j denotes original item names
		for (int i = 0; i < number_items; i++) {
			for (int j = i + 1; j < number_items; j++) {
				int[] items = new int[2];
				items[0] = i;
				items[1] = j;
				this.numberInsertedPatterns++;
				candidateSet.insert(items,
						itempairsUtilityMatrix[i][j].utility,
						itempairsUtilityMatrix[i][j].utility,
						itempairsUtilityMatrix[i][j].support);
			}
		}
		Iterator<Integer> itemIter = itemDetail.keySet().iterator();
		while (itemIter.hasNext()) {
			int item = itemIter.next();
			int[] itemsArray = { item };
			long utility = itemDetail.get(item).getTotalFreq()
					* mapItemUtility.get(item);
			this.numberInsertedPatterns++;
			candidateSet.insert(itemsArray, utility, utility,
					itemDetail.get(item).getSupport());
		}

	}

	/* REALLOC 1 */

	/**
	 * Resizes given item set so that its length is increased by one and appends
	 * new element (identical to append method)
	 * 
	 * @param oldItemSet
	 *            the original item set
	 * @param newElement
	 *            the new element/attribute to be appended
	 * @return the combined item set
	 */

	protected int[] realloc1(int[] oldItemSet, int newElement) {

		// No old item set

		if (oldItemSet == null) {
			int[] newItemSet = { newElement };
			return (newItemSet);
		}

		// Otherwise create new item set with length one greater than old
		// item set

		int oldItemSetLength = oldItemSet.length;
		int[] newItemSet = new int[oldItemSetLength + 1];

		// Loop

		int index;
		for (index = 0; index < oldItemSetLength; index++)
			newItemSet[index] = oldItemSet[index];
		newItemSet[index] = newElement;

		// Return new item set

		return (newItemSet);
	}

	public int[] getPruneItems(HashMap<Integer, ItemSummary> items, ParetoSet ps) {
		ArrayList<Integer> pruneItems = new ArrayList<>();
		Iterator<Integer> itemVar1 = items.keySet().iterator();
		Integer item1;
		while (itemVar1.hasNext()) {
			item1 = itemVar1.next();
			if (this.mapItemToTWU.get(item1) <= ps
					.getUtility(items.get(item1).support))
				pruneItems.add(item1);
		}
		int[] temp = new int[pruneItems.size()];
		for (int i = 0; i < pruneItems.size(); i++)
			temp[i] = pruneItems.get(i);
		return temp;
	}

	/* ORDER COUNT ARRAY */

	/**
	 * Bubble sorts count array produced by <TT>countSingles</TT> method so that
	 * array is ordered according to frequency of single items.
	 * 
	 * @param countArray
	 *            The 2-D array returned by the <TT>countSingles</TT> method.
	 */

	private void orderCountArray(int[][] countArray) {
		int attribute, quantity;
		boolean isOrdered;
		int index;

		do {
			isOrdered = true;
			index = 1;
			while (index < (countArray.length - 1)) {
				if (countArray[index][1] >= countArray[index + 1][1])
					index++;
				else {
					isOrdered = false;
					// Swap
					attribute = countArray[index][0];
					quantity = countArray[index][1];
					countArray[index][0] = countArray[index + 1][0];
					countArray[index][1] = countArray[index + 1][1];
					countArray[index + 1][0] = attribute;
					countArray[index + 1][1] = quantity;
					// Increment index
					index++;
				}
			}
		} while (isOrdered == false);
	}

	public void UpdatePairsUtility(ArrayList<String> items,
			ArrayList<Short> quantity) {
		for (int i = 0; i < items.size(); i++) {
			for (int j = i + 1; j < items.size(); j++) {
				try {

					itempairsUtilityMatrix[Integer.parseInt(items.get(i))][Integer
							.parseInt(items.get(j))].utility += quantity.get(i)
							* mapItemUtility.get(Integer.parseInt(items.get(i)))
							+ quantity.get(j)
							* mapItemUtility.get(Integer.parseInt(items.get(j)));

				} catch (Exception e) {
					System.out.println("caught");
					throw e;
				}
				itempairsUtilityMatrix[Integer.parseInt(items.get(i))][Integer
						.parseInt(items.get(j))].support++;
			}
		}

	}

	/**
	 * Update the exact utility of an itemset given a transaction It assumes
	 * that itemsets are sorted according to the lexical order.
	 * 
	 * @param itemset1
	 *            the first itemset
	 * @param itemset2
	 *            the second itemset
	 * @return true if the first itemset contains the second itemset
	 */
	public void updateExactUtility(List<Item> transaction, Itemset itemset) {
		long utility = 0;
		// for each item in the itemset
		loop1: for (int i = 0; i < itemset.size(); i++) {
			Integer itemI = itemset.get(i);
			// for each item in the transaction
			for (int j = 0; j < transaction.size(); j++) {
				Item itemJ = transaction.get(j);
				// if the current item in transaction is equal to the one in
				// itemset
				// search for the next one in itemset1
				if (itemJ.itemName == itemI) {
					utility += transaction.get(j).utility;
					continue loop1;
				}
				// if the current item in itemset1 is larger
				// than the current item in itemset2, then
				// stop because of the lexical order.
				else if (itemJ.itemName > itemI) {
					return;
				}
			}
			// means that an item was not found
			return;
		}
		// if all items were found, increase utility.
		itemset.increaseUtility(utility);
		itemset.support++;
	}



	private int compareItemsDesc(int item1, int item2,
			Map<Integer, Long> mapItemEstimatedUtility) {
		int compare = (int) (mapItemEstimatedUtility.get(item2) - mapItemEstimatedUtility
				.get(item1));
		// if the same, use the lexical order otherwise use the TWU

		return (compare == 0) ? item1 - item2 : compare;

	}

	protected int[] realloc2(int[] oldItemSet, int newElement) {

		// No old array

		if (oldItemSet == null) {
			int[] newItemSet = { newElement };
			return (newItemSet);
		}

		// Otherwise create new array with length one greater than old array

		int oldItemSetLength = oldItemSet.length;
		int[] newItemSet = new int[oldItemSetLength + 1];

		// Loop

		newItemSet[0] = newElement;
		for (int index = 0; index < oldItemSetLength; index++)
			newItemSet[index + 1] = oldItemSet[index];

		// Return new array

		return (newItemSet);
	}

	// ##################################Code
	// Added#################################################################
	private void upgrowth_plus_inner(UPTree pass_tree, int[] pass_prefix,
			int pass_item, NodeList nList) throws IOException {
		
		int[] newPrefix = new int[pass_prefix.length + 1];
		System.arraycopy(pass_prefix, 0, newPrefix, 0, pass_prefix.length);
		newPrefix[pass_prefix.length] = pass_item;

		// if(newPrefix.length==4 && newPrefix[0]==2 && newPrefix[1]==4 &&
		// newPrefix[2]==5 && newPrefix[3]==3)
		// System.out.println("Caught");

		UPNode pathCPB = pass_tree.mapItemNodes.get(pass_item);
		int supp = 0;
		// take item
		int pathCPBUtility = 0;
		// short min_quantity=pathCPB.min_node_quantity;
		// short max_quantity=pathCPB.max_quantity;

		while (pathCPB != null) {
			// sum of items node utility
			// if(pathCPB.max_quantity>max_quantity)
			// max_quantity=pathCPB.max_quantity;

			pathCPBUtility += pathCPB.nodeUtility;
			supp += pathCPB.count;
			pathCPB = pathCPB.nodeLink;

		}

		NodeList node = new NodeList(pass_item);
		node.addNode(nList);
		if (pathCPBUtility > candidateSet.getUtility(supp)) {

			// long highCodeUtility = getHighUtilityValue(newPrefix,supp);
			// long lowCodeUtility = getLowUtilityValue(newPrefix,supp);
			long highCodeUtility = getNodeHighUtilityValue(node, supp);
			long lowCodeUtility = getNodeLowUtilityValue(node, supp);

			if (highCodeUtility > pathCPBUtility)
				highCodeUtility = pathCPBUtility;

			if (highCodeUtility > candidateSet.getUtility(supp)) {
				this.numberInsertedPatterns++;
				candidateSet.insert(newPrefix, lowCodeUtility, highCodeUtility,
						supp);

			}
			// Create Local Tree
			UPTree localTree = createLocalTree(pass_tree, pass_item);

			if (localTree.headerList.size() > 0) {


				upgrowth_plus(localTree, newPrefix, node);
			}

		}// If TWU >=threshold

		// check the memory usage (for statistics)
		MemoryLogger.getInstance().checkMemory();
	}

	public long getHighUtilityValue(int[] itemset, int support) {
		long utility = 0;
		long itemUtility;
		int i = 0;
		int item;
		ItemSummary iDetail;
		for (i = 0; i < itemset.length; i++) {
			item = itemset[i];
			iDetail = itemDetail.get(item);
			itemUtility = mapItemUtility.get(item);
			utility = utility
					+ Math.min((iDetail.totalFrequency * itemUtility)
							- ((iDetail.support - support)
									* iDetail.minFrequency * itemUtility),
							support * iDetail.maxFrequency * itemUtility);

		}
		return utility;
	}

	public long getNodeHighUtilityValue(NodeList nList, int support) {
		long utility = 0;
		NodeList tempHead = nList;
		while (tempHead != null) {
			utility = utility
					+ this.getHighUtilityValue(tempHead.getItemName(), support);
			tempHead = tempHead.getNextNode();
		}
		return utility;
	}

	public long getNodeLowUtilityValue(NodeList nList, int support) {
		long utility = 0;
		NodeList tempHead = nList;
		while (tempHead != null) {
			utility = utility
					+ this.getLowUtilityValue(tempHead.getItemName(), support);
			tempHead = tempHead.getNextNode();
		}
		return utility;
	}

	public long getHighUtilityValue(int itemName, int support) {
		long utility = 0;
		long itemUtility;
		// short item=(shortitemName;
		itemUtility = mapItemUtility.get(itemName);
		ItemSummary iDetail = itemDetail.get(itemName);

		utility = Math
				.min(iDetail.totalFrequency
						* itemUtility
						- ((iDetail.support - support) * iDetail.minFrequency * itemUtility),
						support * mapMaximumItemUtility.get(itemName));

		return utility;
	}

	public long getLowUtilityValue(int itemName, int support) {
		long utility = 0;
		long itemUtility;
		// short item=(shortitemName;
		itemUtility = mapItemUtility.get(itemName);
		ItemSummary iDetail = itemDetail.get(itemName);

		utility = Math
				.max(iDetail.totalFrequency
						* itemUtility
						- ((iDetail.support - support) * iDetail.maxFrequency * itemUtility),
						support * mapMinimumItemUtility.get(itemName));

		return utility;
	}

	public long getLowUtilityValue(int[] itemset, int support) {
		long utility = 0;
		long itemUtility;
		int i = 0;
		int item;
		ItemSummary iDetail;
		for (i = 0; i < itemset.length; i++) {
			item = itemset[i];
			iDetail = itemDetail.get(item);
			itemUtility = mapItemUtility.get(item);

			utility = utility
					+ Math.max((iDetail.totalFrequency * itemUtility)
							- ((iDetail.support - support)
									* iDetail.maxFrequency * itemUtility),
							support * iDetail.minFrequency * itemUtility);
		}
		return utility;
	}

	// *************************************************************************************88

	/**
	 * Mine UP Tree recursively
	 * 
	 * @param tree
	 *            UPTree to mine
	 * @param minUtility
	 *            minimum utility threshold
	 * @param prefix
	 *            the prefix itemset
	 */
	private void upgrowth_plus(UPTree tree, int[] prefix, NodeList node)
			throws IOException {

		for (int i = tree.headerList.size() - 1; i >= 0; i--) {
			Integer item = tree.headerList.get(i);
			upgrowth_plus_inner(tree, prefix, item, node);
		}
	}

	private UPTree createLocalTree(UPTree tree, Integer item) {

		// === Construct conditional pattern base ===
		// It is a subdatabase which consists of the set of prefix paths
		List<List<UPNode>> prefixPaths = new ArrayList<List<UPNode>>();
		UPNode path = tree.mapItemNodes.get(item);

		// map to store path utility of local items in CPB
		final Map<Integer, Long> itemPathUtility = new HashMap<Integer, Long>();
		while (path != null) {

			// get the Node Utiliy of the item
			long nodeutility = path.nodeUtility;
			// if the path is not just the root node
			if (path.parent.itemID != -1) {
				// create the prefixpath
				List<UPNode> prefixPath = new ArrayList<UPNode>();
				// add this node.
				prefixPath.add(path); // NOTE: we add it just to keep its
				// actually it should not be part of the prefixPath

				// Recursively add all the parents of this node.
				UPNode parentnode = path.parent;
				Long pu;
				while (parentnode.itemID != -1) {
					prefixPath.add(parentnode);

					// pu - path utility
					pu = itemPathUtility.get(parentnode.itemID);
					pu = (pu == null) ? nodeutility : pu + nodeutility;

					itemPathUtility.put(parentnode.itemID, pu);
					parentnode = parentnode.parent;
				}
				// add the path to the list of prefixpaths
				prefixPaths.add(prefixPath);
			}
			// We will look for the next prefixpath
			path = path.nodeLink;
		}

		// Calculate the Utility of each item in the prefixpath
		UPTree localTree = new UPTree();

		// for each prefixpath
		int pathCount, supp;
		long pathUtility;
		List<UPNode> localPath;
		for (List<UPNode> prefixPath : prefixPaths) {
			// the Utility of the prefixpath is the node utility of its
			// first node.
			pathCount = prefixPath.get(0).count;
			pathUtility = prefixPath.get(0).nodeUtility;

			localPath = new ArrayList<UPNode>();
			// for each node in the prefixpath,
			// except the first one, we count the frequency
			for (int j = 1; j < prefixPath.size(); j++) {

				long itemValue = 0; // It store multiplication of minimum
										// item utility and pathcount
				// for each node in prefixpath
				UPNode node = prefixPath.get(j);

				// Here is DLU Strategy #################
				// we check whether local item is promising or not
				if (itemPathUtility.get(node.itemID) >= candidateSet
						.getUtility(itemDetail.get(node.itemID).support)) {
					localPath.add(node);
					// localPath_nodes.put(node.itemID,node);

				} else { // If item is unpromising then we recalculate path
							// utility
					// Long minItemUtility =
					// mapMinimumItemUtility.get(node.itemID);
					Long minItemUtility = 0l;
					minItemUtility = node.min_node_quantity
							* mapItemUtility.get(node.itemID);
					// minItemUtility = mapMinimumItemUtility.get(node.itemID);
					// itemValue=0;
					itemValue = minItemUtility * pathCount;
				}
				pathUtility = pathUtility - itemValue;

			}

			// we reorganize local path in decending order of path utility
			Collections.sort(localPath, new Comparator<UPNode>() {

				public int compare(UPNode o1, UPNode o2) {
					// compare the TWU of the items
					// return compareItemsDesc(o1.itemID, o2.itemID,
					// itemPathUtility);
					return compareItemsDesc(o1.itemID, o2.itemID,
							itemPathUtility);
				}
			});
			// Collections.reverse(localPath);
			supp = pathCount;
			// create tree for conditional pattern base
			try {
				localTree.addLocalTransaction(localPath, pathUtility,
						mapMinimumItemUtility, supp);
			} catch (Exception e) {
				System.out.println("Exception in adding path to local tree");
				System.out.println("pathUtility: " + pathUtility + " supp: "
						+ supp + " mapMinimumItemUtility: "
						+ mapMinimumItemUtility.get((short) 1));
				e.printStackTrace();
				System.out.println(localPath.toString());
				throw e;
			}
		}

		// We create the local header table for the tree item - CPB
		// localTree.createHeaderList(itemPathUtility);
		localTree.createHeaderList(mapItemToTWU);
		int temp_item;
		for (int i = 0; i < localTree.headerList.size(); i++) {

			temp_item = localTree.headerList.get(i);

			headerlist.add(temp_item);

		}
		return localTree;
	}

	/**
	 * Print statistics about the execution of the algorithm
	 */
	public void printStats() {
		StringBuilder buffer = new StringBuilder();


		buffer.append("=============  SkyMine ALGORITHM - STATS =============\n");
		buffer.append("Total time: " + (this.endTimestamp - this.startTimestamp)+ " ms \n");
		buffer.append("Memory: " + MemoryLogger.getInstance().getMaxMemory() + " MB \n");
		buffer.append("Number of inserted patterns in candidate set: "
				+ this.numberInsertedPatterns+ "\n");
		buffer.append("Number of patterns to be verified: "
				+ this.numberVerifiedPatterns+ "\n");
		buffer.append("Number of skyline patterns: " + this.numberOfSkylineItemsets+ "\n");
		buffer.append("===================================================\n\n \n");
		System.out.println(buffer.toString());
	}
	
	/**
	 * Save the result to a file
	 * @throws IOException throw exception if there is an error when writing to the output file
	 */
	private void saveResultToFile() throws IOException {
		//Create a string buffer
		StringBuilder buffer = new StringBuilder();
		
		// variable to remember if an itemset was written to know if we should insert a new line before the 
		// next itemset when writing the next itemset to file
		boolean shouldInsertNewLineBeforeNextItemset = false;
		
		for (int j = resultSet.utilities.size() - 1; j >= 1; j--) {
			ArrayList<ItemsetUtility> itemsets = resultSet.getUtilities().get(j).getItemSetsWithUtilities();

			// For each skyline itemset
			for (int i = 0; i < itemsets.size(); i++) {
				
				// if we should insert a new line
				if(shouldInsertNewLineBeforeNextItemset){
					// start a new line
					shouldInsertNewLineBeforeNextItemset = false;
					buffer.append(System.lineSeparator());
				}
				
				ItemsetUtility itemsetUtility = itemsets.get(i);
				int[] itemsItemsetI = itemsetUtility.itemset;
				long utility = itemsetUtility.utility;
				
				// for each item in that itemset
				for(int k = 0; k < itemsItemsetI.length; k++){
					buffer.append(itemsItemsetI[k]);
					buffer.append(" ");
				}
				buffer.append("#UTIL: " + utility);
				
				// remember that we should insert a new line
				shouldInsertNewLineBeforeNextItemset = true;
				// increase the number of results
				numberOfSkylineItemsets++;
			}
			
		}
	
		// write to file
		writer.write(buffer.toString());
	}
}