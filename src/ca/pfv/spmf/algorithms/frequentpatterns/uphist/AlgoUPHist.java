package ca.pfv.spmf.algorithms.frequentpatterns.uphist;

/* This file is copyright (c) 2018+  by Siddharth Dawar et al.
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 * 
 * SPMF is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
//import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ca.pfv.spmf.algorithms.frequentpatterns.upgrowth_ihup.Itemset;
import ca.pfv.spmf.tools.MemoryLogger;

//import java.util.Iterator;

/**
 * This is an implementation of the UPHist algorithm for mining high utility itemsets.<\br><\br>
 * 
 * The UPHist algorithm was proposed in this paper: <\br><\br>
 * 
 * 	Siddharth Dawar, Vikram Goyal: UP-Hist Tree: An Efficient Data Structure for Mining High Utility Patterns from Transaction Databases. IDEAS 2015: 56-61 <\br><\br>
 * 
 * @author  Siddhart Dawar et al.
 */

public class AlgoUPHist {
	float previousItem;
	/** the time the algorithm started */
	private long startTimestamp = 0; 
	/** the time the algorithm terminated */
	private long endTimestamp = 0; 
	
	/** writer to write the output file */
	BufferedWriter writer = null;
//	
//	/** the number of HUIs generated */
	private int huiCount = 0; 
	
	// We create a map to store the TWU of each item
	final Map<Integer, Integer> mapItemToTWU = new HashMap<Integer, Integer>();

	// key and profit as value
	// map for minimum node utility during DLU(Decreasing Local Unpromizing
	// items) strategy
	private Map<Integer, Integer> mapMinimumItemUtility;
	private Map<Integer, Integer> mapMaximumItemUtility;
	static ArrayList<Integer> headerlist = new ArrayList<Integer>();
	static private HashMap<Integer, ItemSummary> itemDetail = new HashMap<Integer, ItemSummary>();
	// ****************************************************************

	// Structure to store the potential HUIs
	private List<Itemset> phuis = new ArrayList<Itemset>();
	private int phuisCount = 0; // the number of PHUIs generated

	public long getTotalTime() {
		return ((this.endTimestamp - this.startTimestamp));
	}


	public long getHUICount() {
		return this.huiCount;
	}

	public long getCandidatePatterns() {
		return this.phuisCount;
	}


	/**
	 * Method to run the algorithm
	 * 
	 * @param input
	 *            path to an input file
	 * @param output
	 *            path for writing the output file
	 * @param minUtility
	 *            the minimum utility threshold
	 * @throws IOException
	 *             exception if error while reading or writing the file
	 */
	public void runAlgorithm(String input,  String outputFile, int minUtility) throws IOException {

		startTimestamp = System.currentTimeMillis();
		
		writer = new BufferedWriter(new FileWriter(outputFile));

		MemoryLogger.getInstance().checkMemory();
		
		BufferedReader myInput = null;
		String thisLine;

		try {
			// prepare the object for reading the file
			myInput = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(input))));
			// for each line (transaction) until the end of file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is a comment, is empty or is a kind of metadata
				if (thisLine.isEmpty() == true || thisLine.charAt(0) == '#'
						|| thisLine.charAt(0) == '%'
						|| thisLine.charAt(0) == '@') {
					continue;
				}
				// ======= PHIL ====
				// split the line according to the separator
				String split[] = thisLine.split(":");
				// get the list of items
				String items[] = split[0].split(" ");
				// the second part is the transaction utility
				int transactionUtility = Integer.parseInt(split[1]);  
				
				// for each item, we add the transaction utility to its TWU
				for(int i=0; i <items.length; i++){
					// convert item to integer
					Integer item = Integer.parseInt(items[i]);
					// get the current TWU of that item
					Integer twu = mapItemToTWU.get(item);
					// System.out.println("TWU"+twu);
					twu = (twu == null) ? transactionUtility : twu + transactionUtility;
					mapItemToTWU.put(item, twu);
				}
				// System.out.println("map item twu"+mapItemToTWU);

			}
		} catch (Exception e) {
			// catches exception if error while reading the input file
			e.printStackTrace();
			// System.out.println("Hello "+number_temp+
			// " "+utilityMap.get(number_temp));
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}

		// ********************************

		// ******************************************
		// second database scan generate revised transaction and global UP-Tree
		// and calculate the minimum utility of each item
		// (required by the DLU(Decreasing Local Unpromizing items) strategy)
		mapMinimumItemUtility = new HashMap<Integer, Integer>();
		mapMaximumItemUtility = new HashMap<Integer, Integer>();
		try {
			UPHistTree tree = new UPHistTree();
			// System.out.println("Creating UP-Tree");
			// prepare the object for reading the file
			myInput = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(input))));
			// Transaction ID to track transactions
			// for each line (transaction) until the end of file
			// startTimestamp = System.currentTimeMillis();
			// ArrayList<Integer> quantityValues=new ArrayList<Integer>();

			while ((thisLine = myInput.readLine()) != null) {
				// if the line is a comment, is empty or is a kind of metadata
				if (thisLine.isEmpty() == true || thisLine.charAt(0) == '#'
						|| thisLine.charAt(0) == '%'
						|| thisLine.charAt(0) == '@') {
					continue;
				}
				
				// ======= PHIL ====
				// split the line according to the separator
				String split[] = thisLine.split(":");
				// get the list of items
				String items[] = split[0].split(" "); 
				// get the list of utility values corresponding to each item
				// for that transaction
				String utilityValues[] = split[2].split(" ");

				// ======= END PHIL ====
				
//				StringTokenizer dataLine = new StringTokenizer(thisLine);
//				numberOfTokens = dataLine.countTokens();
//				ArrayList<String> items = new ArrayList<String>();
//				ArrayList<Integer> utilityValues = new ArrayList<Integer>();
//
//				for (int tokenCounter = 0; tokenCounter < numberOfTokens; tokenCounter++) {
//					// System.out.println(dataLine.nextToken());
//					String temp[] = dataLine.nextToken().split(":");
//					// System.out.println(new Integer(temp[0]).intValue());
//					items.add(temp[0]);
//					number = new Integer(temp[0]).intValue();
//					// try{
//					quantity = new Integer(temp[1]).intValue();
//					utilityValues.add(quantity);
//
//					// quantityValues.add(quantity);
//					// ****************************************************************
//					// transactionUtility+=quantity*utilityMap.get(number);
//				}

				int remainingUtility = 0;

				// Create a list to store items
				List<Item> revisedTransaction = new ArrayList<Item>();
				int itm;
				int utility;
				Integer minItemUtil, maxItemUtil;
				// for each item
				for(int i=0; i <items.length; i++){
					/// convert values to integers
					itm = Integer.parseInt(items[i]);
					utility = Integer.parseInt(utilityValues[i]);
//					
//				for (int i = 0; i < items.size(); i++) {
//					// convert values to integers
//
//					itm = Integer.parseInt(items.get(i));
//					utility = utilityValues.get(i);
					// quan=quantityValues.get(i);

					if (mapItemToTWU.get(itm) >= minUtility)

					{
						Item element = new Item(itm, utility);
						// add it
						revisedTransaction.add(element);
						remainingUtility += utility;

						// get the current Minimum Item Utility of that item
						minItemUtil = mapMinimumItemUtility.get(itm);
						maxItemUtil = mapMaximumItemUtility.get(itm);

						// Minimum Item Utility is utility of Transaction T if
						// there
						// does not exist Transaction T' such that utility(T')<
						// utility(T)
						if ((minItemUtil == null) || (minItemUtil >= utility)) {
							mapMinimumItemUtility.put(itm, utility);
						}
						if ((maxItemUtil == null) || (maxItemUtil < utility)) {
							mapMaximumItemUtility.put(itm, utility);
						}

						// Adding ItemSummary Code
						if (!itemDetail.containsKey(itm)) {

							ItemSummary summary = new ItemSummary(itm);
							itemDetail.put(itm, summary);
						}
						itemDetail.get(itm).incrementSupp();
						itemDetail.get(itm).updateTotalFrequency(utility);
						if (itemDetail.get(itm).getMinFreq() == 0)
							itemDetail.get(itm).updateMinFrequency(utility);
						else if (utility < itemDetail.get(itm).getMinFreq())
							itemDetail.get(itm).updateMinFrequency(utility);

						if (itemDetail.get(itm).getMaxFreq() == 0)
							itemDetail.get(itm).updateMaxFrequency(utility);
						else if (utility > itemDetail.get(itm).getMaxFreq())
							itemDetail.get(itm).updateMaxFrequency(utility);
						// prepare object for garbage collection
						// element = null;
					}
				}

				// revised transaction in desceding order of TWU
				Collections.sort(revisedTransaction, new Comparator<Item>() {
					public int compare(Item o1, Item o2) {
						return compareItemsDesc(o1.name, o2.name, mapItemToTWU);
					}
				});

				// add transaction to the global UP-Tree
				tree.addTransaction(revisedTransaction, remainingUtility);
			}

			// We create the header table for the global UP-Tree
			tree.createHeaderList(mapItemToTWU);
			long temp_time = System.currentTimeMillis();
			/*
			 * for(UtilityList temp:listOfUtilityLists) {
			 * this.tempcandidateBufferWriter
			 * .append("item: "+temp.item+" sumIutils: "
			 * +temp.sumIutils+" sumRutils: "
			 * +temp.sumRutils+" support: "+temp.elements.size()+"\n"); }
			 */
			// Mine tree with UPGrowth with 2 strategies DLU and DLN
			uphistgrowth(tree, (int) minUtility, new int[0], null);
			endTimestamp = System.currentTimeMillis();
			// endTimestamp_before_veri = System.currentTimeMillis();

			// check the memory usage again and close the file.
			// checkMemory();

		} catch (Exception e) {
			// catches exception if error while reading the input file
			System.out.println(e.getMessage());
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}
		// save the number of candidate found
		phuisCount = phuis.size();
		System.out.println("Number of candidates generated is: " + phuisCount);
		System.out.println("Verification Started");
		// ******************************************
		// Third database scan to calculate the
		// exact utility of each PHUIs and output those that are HUIS.

		// First sort the PHUIs by size for optimization
		Collections.sort(phuis, new Comparator<Itemset>() {
			public int compare(Itemset arg0, Itemset arg1) {
				return arg0.size() - arg1.size();
			}
		});

		try {
			// prepare the object for reading the file
			myInput = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(input))));

			// for each line (transaction) until the end of file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is a comment, is empty or is a kind of metadata
				if (thisLine.isEmpty() == true || thisLine.charAt(0) == '#'
						|| thisLine.charAt(0) == '%'
						|| thisLine.charAt(0) == '@') {
					continue;
				}
				// ======= PHIL ====
				// split the line according to the separator
				String split[] = thisLine.split(":");
				// get the list of items
				String items[] = split[0].split(" "); 
				// get the list of utility values corresponding to each item
				// for that transaction
				String utilityValues[] = split[2].split(" ");

				// ======= END PHIL ====

				// Create a list to store items
				List<Item> revisedTransaction = new ArrayList<Item>();
				// for each item
				for(int i=0; i <items.length; i++){
					/// convert values to integers
					int item = Integer.parseInt(items[i]);
					int utility = Integer.parseInt(utilityValues[i]);

					Item element = new Item(item, utility);
					if (mapItemToTWU.get(item) >= minUtility) {
						revisedTransaction.add(element);
					}
				}

				// sort the transaction by lexical order
				// for faster comparison since PHUIs have been sorted
				// by lexical order and this will make faster
				// comparison
				Collections.sort(revisedTransaction, new Comparator<Item>() {
					public int compare(Item o1, Item o2) {
						return o1.name - o2.name;
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
			if (itemset.getExactUtility() >= minUtility) {
				writeOut(itemset);
				huiCount++;
			}
		}

		// check the memory usage again
		MemoryLogger.getInstance().checkMemory();
		
		writer.close();
		
		// record end time
		endTimestamp = System.currentTimeMillis();

		// Release some memory
		phuis.clear();

		// this.tempcandidateBufferWriter.close();
		mapMinimumItemUtility = null;
		mapMaximumItemUtility = null;

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
		int utility = 0;
		// for each item in the itemset
		loop1: for (int i = 0; i < itemset.size(); i++) {
			Integer itemI = itemset.get(i);
			// for each item in the transaction
			for (int j = 0; j < transaction.size(); j++) {
				Item itemJ = transaction.get(j);
				// if the current item in transaction is equal to the one in
				// itemset
				// search for the next one in itemset1
				if (itemJ.name == itemI) {
					utility += transaction.get(j).utility;
					continue loop1;
				}
				// if the current item in itemset1 is larger
				// than the current item in itemset2, then
				// stop because of the lexical order.
				else if (itemJ.name > itemI) {
					return;
				}
			}
			// means that an item was not found
			return;
		}
		// if all items were found, increase utility.
		itemset.increaseUtility(utility);
	}

	/**
	 * Save a PHUI in the list of PHUIs
	 * 
	 * @param itemset
	 *            the itemset
	 */
	private void savePHUI(int[] itemset) {
		// Create an itemset object and store it in the list of pHUIS
		Itemset itemsetObj = new Itemset(itemset);
		// Sort the itemset by lexical order to faster calculate its
		// exact utility later on.
		/*
		 * Arrays.sort(itemset); try{ writeOut(itemset,utility); }
		 * catch(Exception e) {
		 * 
		 * }
		 */
		// add the itemset to the list of PHUIs
		phuis.add(itemsetObj);
	}

//	/**
//	 * Method to compare items by their TWU
//	 * 
//	 * @param item1
//	 *            an item
//	 * @param item2
//	 *            another item
//	 * @return 0 if the same item, >0 if item1 is larger than item2, <0
//	 *         otherwise
//	 */
//	private int compareItems(int item1, int item2) {
//		int compare = 0;
//		try {
//			compare = (int) (mapItemToTWU.get(item1) - mapItemToTWU.get(item2));
//
//		} catch (Exception e) {
//			System.out.println("item 1: " + item1 + " item2: " + item2);
//			System.out.println("map 1: " + mapItemToTWU.get(item1) + " map 2: "
//					+ mapItemToTWU.get(item2));
//			System.out.println("map 1: " + mapItemToTWU.get(3) + " map 2: "
//					+ mapItemToTWU.get(1));
//			throw e;
//
//		}// if the same, use the lexical order otherwise use the TWU
//		return (compare == 0) ? item1 - item2 : compare;
//		// return item2-item1;
//	}
//
//	// ************************************************************************************
//	private int compareItemsDesc(int item1, int item2) {
//		int compare = (int) (mapItemToTWU.get(item1) - mapItemToTWU.get(item2));
//		// if the same, use the lexical order otherwise use the TWU
//		return (compare == 0) ? item1 - item2 : compare;
//	}

	private int compareItemsDesc(int item1, int item2,
			Map<Integer, Integer> mapItemEstimatedUtility) {
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
	private int uphistgrowth_new(UPHistTree pass_tree, int threshold,
			int[] pass_prefix, int pass_item, NodeList nList)
			throws IOException {

		MemoryLogger.getInstance().checkMemory();
//		long temp1 = System.currentTimeMillis();

		int[] newPrefix = new int[pass_prefix.length + 1];
		System.arraycopy(pass_prefix, 0, newPrefix, 0, pass_prefix.length);
		// System.out.println("pass item"+pass_item);
		newPrefix[pass_prefix.length] = pass_item;

		UPHistNode pathCPB = pass_tree.mapItemNodes.get(pass_item);
		int supp = 0;
		// take item
		int pathCPBUtility = 0;
		Hist histogram = genItemHistHeadTabItem(pathCPB);

		while (pathCPB != null) {
			// sum of items node utility
			pathCPBUtility += pathCPB.nodeUtility;
			supp += pathCPB.count;
			pathCPB = pathCPB.nodeLink;
			// System.out.println("node"+pathCPB);

		}

		NodeList node = new NodeList(pass_item, histogram);
		node.addNode(nList);
		// *******************************************
		/*
		 * System.out.println("=======================================");
		 * System.out.println("Prefix"); for(int i=0;i<pass_prefix.length;i++) {
		 * System.out.print(" "+ pass_prefix[i] ); }
		 * System.out.println("Pass item: "+pass_item);
		 * System.out.println("TWU: "+pathCPBUtility);
		 */
		// System.out.println("node utility"+pathCPBUtility);
		if (pathCPBUtility >= threshold) {

			float highCodeUtility = getNodeHighUtilityValue(node, supp);
			float lowCodeUtility = getNodeLowUtilityValue(node, supp);

			/*
			 * System.out.println("highCodeUtility: "+highCodeUtility);
			 * System.out.println("lowCodeUtility: "+lowCodeUtility);
			 * System.out.println("=======================================");
			 */
			// clear the temp arraylist
			// headerlist.clear();
//			long temp = System.currentTimeMillis();
			// Create Local Tree
			UPHistTree localTree = createLocalTree(threshold, pass_tree,
					pass_item);

			if (highCodeUtility >= threshold) {
				if (lowCodeUtility >= threshold) {
					huiCount++;
					writeOut(newPrefix, lowCodeUtility);
				} else {
					savePHUI(newPrefix);
					phuisCount++;
				}
			}

			if (localTree.headerList.size() > 0) {

				uphistgrowth(localTree, threshold, newPrefix, node);
			}

		}// If TWU >=threshold
		return 1;
	}

	public Hist genItemHistHeadTabItem(UPHistNode nodeLink) {
		Hist histogram = new Hist();
		// Loop

		while (nodeLink != null) {
			histogram.updateHist(nodeLink.histogram);
			nodeLink = nodeLink.nodeLink;

		}
		return histogram;
	}

	public int getNodeHighUtilityValue(NodeList nList, int support) {
		int utility = 0;
		NodeList tempHead = nList;

		while (tempHead != null) {
			utility = utility
					+ this.getHighUtilityValue(tempHead.getItemName(), support,
							tempHead.getHistogram());
			tempHead = tempHead.getNextNode();

		}
		return utility;
	}

	public int getNodeLowUtilityValue(NodeList nList, int support) {
		int utility = 0;
		NodeList tempHead = nList;

		while (tempHead != null) {
			utility = utility
					+ this.getLowUtilityValue(tempHead.getItemName(), support,
							tempHead.getHistogram());
			tempHead = tempHead.getNextNode();

		}
		return utility;
	}

	public int getHighUtilityValue(int itemName, int support, Hist histogram) {
		int utility = 0;
		ItemSummary iDetail = itemDetail.get(itemName);
		utility = Math.min(iDetail.totalUtility
				- ((iDetail.support - support) * iDetail.minUtility * 1),
				histogram.getMaxSupportInterU(support) * 1);

		return utility;

	}

	public int getLowUtilityValue(int itemName, int support, Hist histogram) {
		int utility = 0;
		ItemSummary iDetail = itemDetail.get(itemName);
		utility = Math.max(iDetail.totalUtility
				- ((iDetail.support - support) * iDetail.maxUtility),
				histogram.getMinSupportInterU(support));

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
	private void uphistgrowth(UPHistTree tree, int minUtility, int[] prefix,
			NodeList node) throws IOException {

//		long temp_time = System.currentTimeMillis();
		// UtilityList temp;

		for (int i = tree.headerList.size() - 1; i >= 0; i--) {

//			temp_time = System.currentTimeMillis();

			// get the item
			Integer item = tree.headerList.get(i);
			uphistgrowth_new(tree, minUtility, prefix, item, node);

		}

	}

	private UPHistTree createLocalTree(int minUtility, UPHistTree tree,
			Integer item) {
		List<List<UPHistNode>> prefixPaths = new ArrayList<List<UPHistNode>>();
		UPHistNode path = tree.mapItemNodes.get(item);

		// map to store path utility of local items in CPB
		final Map<Integer, Integer> itemPathUtility = new HashMap<Integer, Integer>();
		// System.out.println("path utility"+itemPathUtility);
		while (path != null) {

			// get the Node Utiliy of the item
			int nodeutility = path.nodeUtility;
			// if the path is not just the root node
			if (path.parent.itemID != -1) {
				// create the prefixpath
				List<UPHistNode> prefixPath = new ArrayList<UPHistNode>();
				// add this node.
				prefixPath.add(path); // NOTE: we add it just to keep its
				// actually it should not be part of the prefixPath

				// Recursively add all the parents of this node.
				UPHistNode parentnode = path.parent;
				while (parentnode.itemID != -1) {
					prefixPath.add(parentnode);

					// pu - path utility
					Integer pu = itemPathUtility.get(parentnode.itemID);
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
		UPHistTree localTree = new UPHistTree();

		// for each prefixpath
		for (List<UPHistNode> prefixPath : prefixPaths) {
			// the Utility of the prefixpath is the node utility of its
			// first node.
			int pathCount = prefixPath.get(0).count;
			// System.out.println("path count"+pathCount);
			int pathUtility = prefixPath.get(0).nodeUtility;

			List<UPHistNode> localPath = new ArrayList<UPHistNode>();
//			HashMap<Integer, UPHistNode> localPath_nodes = new HashMap<Integer, UPHistNode>();
			// for each node in the prefixpath,
			// except the first one, we count the frequency
			for (int j = 1; j < prefixPath.size(); j++) {

				int itemValue = 0; // It store multiplication of minimum
									// item utility and pathcount
				// for each node in prefixpath
				UPHistNode node = prefixPath.get(j);

				// Here is DLU Strategy #################
				// we check whether local item is promising or not
				if (itemPathUtility.get(node.itemID) >= minUtility) {
					// System.out.println("node.item"+node.itemID);
					localPath.add(node);
					// localPath_nodes.put(node.itemID,node);

				} else { // If item is unpromising then we recalculate path
							// utility
					Integer minItemUtility = 0;
					minItemUtility = node.histogram
							.getMinSupportInterU(pathCount);
					itemValue = minItemUtility;
				}
				pathUtility = pathUtility - itemValue;

			}

			// we reorganize local path in decending order of path utility
			Collections.sort(localPath, new Comparator<UPHistNode>() {

				public int compare(UPHistNode o1, UPHistNode o2) {
					// compare the TWU of the items
					// return compareItemsDesc(o1.itemID, o2.itemID,
					// itemPathUtility);
					return compareItemsDesc(o1.itemID, o2.itemID, mapItemToTWU);
				}
			});
			int supp = pathCount;
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
		localTree.createHeaderList(mapItemToTWU);
		for (int i = 0; i < localTree.headerList.size(); i++) {

			int temp_item = localTree.headerList.get(i);

			headerlist.add(temp_item);

		}

		return localTree;
	}
	
	/**
	 * Write a HUI to the output file
	 * @param HUI
	 * @param utility
	 * @throws IOException
	 */
	private void writeOut(Itemset HUI) throws IOException {
//		huiCount++; // increase the number of high utility itemsets found

		//Create a string buffer
		StringBuilder buffer = new StringBuilder();
		//Append each item
		for (int  i = 0; i < HUI.size(); i++) {
			buffer.append(HUI.get(i));
			buffer.append(' ');
		}
		buffer.append("#UTIL: ");
		buffer.append(HUI.getExactUtility());

		// write to file
		writer.write(buffer.toString());
		writer.newLine();
	}
	
	/**
	 * Write a HUI to the output file
	 * @param HUI
	 * @param utility
	 * @throws IOException
	 */
	private void writeOut(int[] itemset,  float utility) throws IOException {
//		huiCount++; // increase the number of high utility itemsets found

		//Create a string buffer
		StringBuilder buffer = new StringBuilder();
		//Append each item
		for (int  i = 0; i < itemset.length; i++) {
			buffer.append(itemset[i]);
			buffer.append(' ');
		}
		
		buffer.append("#UTIL: ");
		buffer.append(utility);

		// write to file
		writer.write(buffer.toString());
		writer.newLine();
	}
	
	/**
	 * Print statistics about the latest execution to System.out.
	 * @throws IOException 
	 */
	public void printStats() throws IOException {
		System.out.println("=============  UPHist ALGORITHM - SPMF 0.2.34 - STATS =============");
		System.out.println(" Total time: "                  + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Memory:  "                      + MemoryLogger.getInstance().getMaxMemory()  + " MB");
		System.out.println(" HUI count: "        + huiCount);
		System.out.println("===================================================");
	}

}