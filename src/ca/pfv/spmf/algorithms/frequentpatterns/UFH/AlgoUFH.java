package ca.pfv.spmf.algorithms.frequentpatterns.UFH;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This is an implementation of the UPGrowth-FHM (UFH) hybrid
 * algorithm.<\br><\br> The UFH algorithm was proposed in this paper: <\br><\br>
 * 
 * Siddharth Dawar, Vikram Goyal, Debajyoti Bera: A hybrid framework for mining
 * high-utility itemsets in a sparse transaction database. Appl. Intell. 47(3):
 * 809-827 (2017)
 * 
 * Implementation of the UFH algorithm is copyright (c) 2018 Siddharth Dawar et
 * al<\br><\br>
 * 
 * It extends on code of UPGrowth, EFIM and FHM algorithms from the SPMF library.
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE *
 * (http://www.philippe-fournier-viger.com/spmf). <\br><\br>
 * 
 * SPMF is free software: you can redistribute it and/or modify it under the *
 * terms of the GNU General Public License as published by the Free Software *
 * Foundation, either version 3 of the License, or (at your option) any later *
 * version. SPMF is distributed in the hope that it will be useful, but WITHOUT
 * ANY * WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. <\br><\br>
 * 
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>. <\br><\br>
 * 
 * @author Siddharth Dawar 
 */

public class AlgoUFH {

	/**  the time the algorithm started */
	private long startTimestamp = 0; 
	
	/**  the time the algorithm terminated */
	private long endTimestamp = 0; 
	
	private long before_upg = 0;
	
	/** the number of HUIs generated */
	private int HUICount = 0; 
	
	/** writer to write the output file */
	BufferedWriter writer = null;
	
	/** debug mode */
	private boolean DEBUG = false;
	
	/** the number of PHUIs generated */
	private int candidate_count_upg = 0; 
	private int candidate_count_fhm = 0, min_length = Integer.MAX_VALUE,
			max_length = 0;
	public int utilitythreshold = 0;
	static int counter1 = 0, number_construct_our = 0,
			number_construct_fhm = 0, number_create_UL = 0,
			time_onlylocaltree = 0, counter_only_local_tree = 0,
			candidateCount = 0;
	long construct_time_our = 0, construct_time_fhm = 0, merge_time = 0,
			create_UL_time = 0, counternull = 0, number_of_recursions = 0,
			time_localtree = 0, contains_all_time = 0,
			contains_all_counter = 0, time_highcode = 0,
			time_fhm_ancestor_codes = 0, counter_fhm_ancestor_codes = 0,
			time_hmap = 0, time_opti = 0, time_prefix_creation = 0;
	static long counter_total_itemsets = 0, counter_optimization = 0;
	long time_createul = 0, time_writefile = 0, time_listul = 0, time_fhm = 0,
			time_gc;
	long time_phase1 = 0, time_phase2 = 0, time_phase3 = 0, time_phase4 = 0,
			number_of_loop_calls = 0;
	long count_prefix = 0, length_prefix = 0;
	long upgrowth_p1 = 0, upgrowth_p2 = 0, upgrowth_p3 = 0,
			upgrowth_prefix_twu_nodelist = 0;
	public static ArrayList<UtilityList_SPMF> singleton_Utility_list = new ArrayList<UtilityList_SPMF>();
	static ArrayList<Integer> blacklisted_itemset = new ArrayList<Integer>();
	BufferedWriter tempcandidateBufferWriter = null, tempwriter = null;
	// We create a map to store the TWU of each item
	final Map<Integer, Integer> mapItemToTWU = new HashMap<Integer, Integer>();
	// map for minimum node utility during DLU(Decreasing Local Unpromizing
	// items) strategy
	private Map<Integer, Integer> mapMinimumItemUtility;
	private Map<Integer, Integer> mapMaximumItemUtility;
	static ArrayList<Integer> headerlist = new ArrayList<Integer>();
	static HashMap<String, LinkedList<String>> multi = new HashMap<String, LinkedList<String>>();
	// ****************************************************************
	long number_of_TWU = 0, multi_time = 0, singleton_items_time = 0,
			singleton_items_count = 0;
	int p_pos = 0, y_pos = 0;
	public HashMap<String, UtilityList_SPMF> Itemset_Utility_List_Map = new HashMap<String, UtilityList_SPMF>();
	// public BitMapIndex invertedIndex= null;
	// NEW OPTIMIZATION - FMAP (FAST)
	protected Map<Integer, Map<Integer, Integer>> mapFMAP = new HashMap<Integer, Map<Integer, Integer>>(); // PAIR

	// END NEW OPTIMIZATION

	// this class represent an item and its utility in a transaction
	class Pair {
		int item = 0;
		int utility = 0;

		public String toString() {
			return "[" + item + "," + utility + "]";
		}
	}

	// ****************************************************************

	/** an array that map an old item name to its new name */
	int[] oldNameToNewNames;
	/** an array that map a new item name to its old name */
	int[] newNamesToOldNames;
	/** the number of new items */
	int newItemCount;

	public long getHybridAlgoTime() {
		return ((this.before_upg));
	}

	public long getVerifiedPatterns() {
		return this.HUICount;
	}

	public long getVerifiedPatterns(BufferedWriter tempwriter) {
		return this.HUICount;
	}

	public long getCandidatePatterns() {
		return this.candidate_count_upg;
	}

	public long getTotalUPGRecursions() {
		return number_of_recursions;
	}

	public long getTotalUPGTWU() {
		return number_of_TWU;
	}

	public long getCandidatePatternsFHM() {
		return this.candidate_count_fhm;
	}

	public int getUtilityThreshold() {
		return this.utilitythreshold;
	}

	void create_singleton_items_utility_list() {
		// CREATE A LIST TO STORE THE UTILITY LIST OF ITEMS WITH TWU >=
		// MIN_UTILITY.
		ArrayList<UtilityList_SPMF> listOfUtilityLists = new ArrayList<UtilityList_SPMF>();
		// CREATE A MAP TO STORE THE UTILITY LIST FOR EACH ITEM.
		// Key : item Value : utility list associated to that item
		// Map<Short, UtilityList> mapItemToUtilityList = new HashMap<Short,
		// UtilityList>();
		// For each item
		for (Integer item : mapItemToTWU.keySet()) {
			// if the item is promising (TWU >= minutility)
			if (mapItemToTWU.get(item) >= utilitythreshold) {
				// create an empty Utility List that we will fill later.
				UtilityList_SPMF uList = new UtilityList_SPMF(item);
				/*
				 * StringBuilder temp_string=new StringBuilder();
				 * temp_string.append(item);
				 */
				ArrayList<Integer> temp = new ArrayList<Integer>();
				temp.add(item);
				Itemset_Utility_List_Map.put(temp.toString(), uList);

				// mapItemToUtilityList.put(item, uList);
				// add the item to the list of high TWU items
				listOfUtilityLists.add(uList);

			}
		}

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

		MemoryLogger.getInstance().reset();
		
		writer = new BufferedWriter(new FileWriter(outputFile));

		startTimestamp = System.currentTimeMillis();

		this.utilitythreshold = minUtility;

		// ******************************************
		// First database scan to calculate the TWU of each item.

		EFIM_UP_Tree_Dataset_SPMF dataset = new EFIM_UP_Tree_Dataset_SPMF(input);

		for (EFIM_UP_Tree_Transaction_SPMF transaction : dataset.getTransactions()) {
			// for each item
			for (Integer it : transaction.getItems()) {

				Integer twu = mapItemToTWU.get(it);
				twu = (twu == null) ? transaction.transactionUtility : twu
						+ transaction.transactionUtility;
				mapItemToTWU.put(it, twu);
			}
		}

		// Now, we keep only the promising items (those having a twu >= minutil)
		List<Integer> itemsToKeep = new ArrayList<Integer>();

		for (int i : mapItemToTWU.keySet()) {
			if (mapItemToTWU.get(i) >= minUtility) {
				itemsToKeep.add(i);

			}
		}

		// Sort promising items according to the increasing order of TWU
		for (int j = 1; j < itemsToKeep.size(); j++) {
			Integer itemJ = itemsToKeep.get(j);
			int i = j - 1;
			Integer itemI = itemsToKeep.get(i);

			// we compare the twu of items i and j
			int comparison = (int) (mapItemToTWU.get(itemI) - mapItemToTWU
					.get(itemJ));
			// if the twu is equal, we use the lexicographical order to decide
			// whether i is greater
			// than j or not.
			if (comparison == 0) {
				comparison = itemI - itemJ;
			}

			while (comparison > 0) {
				itemsToKeep.set(i + 1, itemI);

				i--;
				if (i < 0) {
					break;
				}

				itemI = itemsToKeep.get(i);
				comparison = (int) (mapItemToTWU.get(itemI) - mapItemToTWU
						.get(itemJ));
				// if the twu is equal, we use the lexicographical order to
				// decide whether i is greater
				// than j or not.
				if (comparison == 0) {
					comparison = itemI - itemJ;
				}
			}
			itemsToKeep.set(i + 1, itemJ);
		}

		// Rename promising items according to the increasing order of TWU.
		// This will allow very fast comparison between items later by the
		// algorithm
		// This structure will store the new name corresponding to each old name

		oldNameToNewNames = new int[dataset.getMaxItem() + 1];

		// This structure will store the old name corresponding to each new name

		newNamesToOldNames = new int[dataset.getMaxItem() + 1];

		// We will now give the new names starting from the name "1"
		int currentName = 1;
		// For each item in increasing order of TWU
		for (int j = 0; j < itemsToKeep.size(); j++) {
			// get the item old name
			int it = itemsToKeep.get(j);
			// give it the new name
			oldNameToNewNames[it] = currentName;
			// remember its old name
			newNamesToOldNames[currentName] = it;
			// replace its old name by the new name in the list of promising
			// items
			// itemsToKeep.set(j, currentName);
			// t = new HashMap<Integer,ArrayList<Float>>();
			// UL_Map.put(currentName,t);
			// increment by one the current name so that
			currentName++;
		}

		// ********************************
		// create_singleton_items_utility_list();
		// CREATE A LIST TO STORE THE UTILITY LIST OF ITEMS WITH TWU >=
		// MIN_UTILITY.
		ArrayList<UtilityList_SPMF> listOfUtilityLists = new ArrayList<UtilityList_SPMF>();
		// CREATE A MAP TO STORE THE UTILITY LIST FOR EACH ITEM.
		// Key : item Value : utility list associated to that item
		Map<Integer, UtilityList_SPMF> mapItemToUtilityList = new HashMap<Integer, UtilityList_SPMF>();

		// For each item in increasing order of TWU
		for (int j = 0; j < itemsToKeep.size(); j++) {
			// create an empty Utility List that we will fill later.
			UtilityList_SPMF uList = new UtilityList_SPMF(
					oldNameToNewNames[itemsToKeep.get(j)]);
			mapItemToUtilityList.put(oldNameToNewNames[itemsToKeep.get(j)],
					uList);

			listOfUtilityLists.add(uList);
		}

		// We now loop over each transaction from the dataset
		// to remove unpromising items
		for (int i = 0; i < dataset.getTransactions().size(); i++) {
			// Get the transaction
			EFIM_UP_Tree_Transaction_SPMF transaction = dataset
					.getTransactions().get(i);

			// Remove unpromising items from the transaction and at the same
			// time
			// rename the items in the transaction according to their new names
			// and sort the transaction by increasing TWU order

			transaction.removeUnpromisingItems(oldNameToNewNames);
			// transaction.removeUnpromisingItems(mapItemToTWU,this.utilitythreshold);
		}

		// Now we will sort transactions in the database according to the
		// proposed
		// total order on transaction (the lexicographical order when
		// transactions
		// are read backward).
		long timeStartSorting = System.currentTimeMillis();
		// We only sort if transaction merging is activated
		if (true) {
			// Sort the dataset using a new comparator
			Collections.sort(dataset.getTransactions(),
					new Comparator<EFIM_UP_Tree_Transaction_SPMF>() {
						@Override
						public int compare(EFIM_UP_Tree_Transaction_SPMF t1,
								EFIM_UP_Tree_Transaction_SPMF t2) {
							// we will compare the two transaction items by
							// items starting
							// from the last items.
							int pos1 = t1.items.length - 1;
							int pos2 = t2.items.length - 1;

							// if the first transaction is smaller than the
							// second one
							if (t1.items.length < t2.items.length) {
								// while the current position in the first
								// transaction is >0
								while (pos1 >= 0) {
									int subtraction = t2.items[pos2]
											- t1.items[pos1];
									if (subtraction != 0) {
										return subtraction;
									}
									pos1--;
									pos2--;
								}
								// if they ware the same, they we compare based
								// on length
								return -1;

								// else if the second transaction is smaller
								// than the first one
							} else if (t1.items.length > t2.items.length) {
								// while the current position in the second
								// transaction is >0
								while (pos2 >= 0) {
									int subtraction = t2.items[pos2]
											- t1.items[pos1];
									if (subtraction != 0) {
										return subtraction;
									}
									pos1--;
									pos2--;
								}
								// if they ware the same, they we compare based
								// on length
								return 1;

							} else {
								// else if both transactions have the same size
								while (pos2 >= 0) {
									int subtraction = t2.items[pos2]
											- t1.items[pos1];
									if (subtraction != 0) {
										return subtraction;
									}
									pos1--;
									pos2--;
								}
								// if they ware the same, they we compare based
								// on length
								return 0;
							}
						}

					});

			// =======================REMOVE EMPTY
			// TRANSACTIONS==========================
			// After removing unpromising items, it may be possible that some
			// transactions
			// are empty. We will now remove these transactions from the
			// database.
			int emptyTransactionCount = 0;
			// for each transaction
			for (int i = 0; i < dataset.getTransactions().size(); i++) {
				// if the transaction length is 0, increase the number of empty
				// transactions
				EFIM_UP_Tree_Transaction_SPMF transaction = dataset
						.getTransactions().get(i);
				if (transaction.items.length == 0) {
					emptyTransactionCount++;
				}
			}
			// To remove empty transactions, we just ignore the first
			// transactions from the dataset
			// The reason is that empty transactions are always at the begining
			// of the dataset
			// since transactions are sorted by size
			dataset.transactions = dataset.transactions.subList(
					emptyTransactionCount, dataset.transactions.size());
			if(DEBUG){
				System.out.println("Empty transaction count: "
					+ emptyTransactionCount);
			}
		}

		if(DEBUG){
			// record the total time spent for sorting
			long timeSort = System.currentTimeMillis() - timeStartSorting;
	
			System.out.println("TimeSort + remove empty tx: " + timeSort);
		}

		long temp_3 = System.currentTimeMillis();
		List<EFIM_UP_Tree_Transaction_SPMF> merged_dataset = txmerging(dataset
				.getTransactions());
		
		if(DEBUG){
			long temp2 = System.currentTimeMillis() - temp_3;
			System.out.println("Time taken for 1st level merging: " + temp2);
		}

		MemoryLogger.getInstance().checkMemory();

		// Scan the database to compute remainingutility and insert tx in tree

		int remainingUtilityFHM;
		int tid = 0;
		UPTree_SPMF tree = new UPTree_SPMF();
		// Create a list to store items
		// List<Item> revisedTransaction = null;
		mapMinimumItemUtility = new HashMap<Integer, Integer>();
		mapMaximumItemUtility = new HashMap<Integer, Integer>();

		for (EFIM_UP_Tree_Transaction_SPMF transaction : merged_dataset) {

			// for (EFIM_UP_Tree_Transaction transaction :
			// dataset.getTransactions()) {
			// We will scan the transaction backward. Thus,
			// the current sub-tree utility in that transaction is zero
			// for the last item of the transaction.
			// sumSU = 0;
			remainingUtilityFHM = transaction.transactionUtility;
			// float newTWU = 0; // NEW OPTIMIZATION

			int[] rutilities = new int[transaction.getItems().length];
			// Item element=null;
			// For each item when reading the transaction backward
			for (int i = 0; i <= transaction.getItems().length - 1; i++) {
				// get the item
				Integer it = transaction.getItems()[i];

				// we add the utility of the current item to its sub-tree
				// utility

				// sumSU += transaction.getUtilities()[i];
				remainingUtilityFHM = remainingUtilityFHM
						- transaction.getUtilities()[i];

				UtilityList_SPMF utilityListOfItem = mapItemToUtilityList
						.get(it);

				// Add a new Element to the utility list of this item
				// corresponding to this transaction
				Element_SPMF element = new Element_SPMF(tid,
						transaction.getUtilities()[i], remainingUtilityFHM);

				utilityListOfItem.addElement(element);

				// BEGIN NEW OPTIMIZATION for FHM
				Map<Integer, Integer> mapFMAPItem = mapFMAP.get(it);
				if (mapFMAPItem == null) {
					mapFMAPItem = new HashMap<Integer, Integer>();
					mapFMAP.put(it, mapFMAPItem);
				}

				for (int j = i + 1; j < transaction.getItems().length; j++) {
					// get the item
					Integer pairAfter = transaction.getItems()[j];

					Integer twuSum = mapFMAPItem.get(pairAfter);
					if (twuSum == null) {
						mapFMAPItem.put(pairAfter,
								transaction.transactionUtility);
					} else {
						mapFMAPItem.put(pairAfter, twuSum
								+ transaction.transactionUtility);
					}
				}
				// END OPTIMIZATION of FHM

				rutilities[i] = remainingUtilityFHM;

				// get the current Minimum Item Utility of that item
				Integer minItemUtil = mapMinimumItemUtility.get(it);
				Integer maxItemUtil = mapMaximumItemUtility.get(it);
				// Minimum Item Utility is utility of Transaction T if there
				// does not exist Transaction T' such that utility(T')<
				// utility(T)
				if ((minItemUtil == null)
						|| (minItemUtil > transaction.getUtilities()[i])) {
					mapMinimumItemUtility
							.put(it, transaction.getUtilities()[i]);
				}
				if ((maxItemUtil == null)
						|| (maxItemUtil < transaction.getUtilities()[i])) {
					mapMaximumItemUtility
							.put(it, transaction.getUtilities()[i]);
				}

			}
			tree.addTransaction(transaction, transaction.transactionUtility);
			tid++;

		}

		// We create the header table for the global UP-Tree
		// tree.createHeaderList(mapItemToTWU);
		tree.createHeaderList(mapItemToTWU, this.newNamesToOldNames);
		long temp_time = System.currentTimeMillis();

		for (UtilityList_SPMF temp : listOfUtilityLists) {

			ArrayList<Integer> t = new ArrayList<Integer>();
			t.add(temp.item);
			Itemset_Utility_List_Map.put(t.toString(), temp);

		}
		
		if(DEBUG){
			System.out.println("Time to add single UL to map: "
				+ (System.currentTimeMillis() - temp_time));
			System.out.println("Till from DB scan to tree: "
				+ (temp_time - startTimestamp));
		}

		// Mine tree with UPGrowth with 2 strategies DLU and DLN
		long t = System.currentTimeMillis();
		upgrowth_fhm_hybrid_outer(tree, minUtility, new int[0], null);
		before_upg += (System.currentTimeMillis() - temp_time);
		

		endTimestamp = System.currentTimeMillis();

		if(DEBUG){
			System.out.println("Time taken by algo: "
					+ (System.currentTimeMillis() - t));
	
			System.out.println("Time to add prefix to local AL before checking: "
					+ upgrowth_p1);
			System.out.println("Time to perform check subset: " + upgrowth_p2);
			System.out.println("Time to run inner alg: " + upgrowth_p3);
			System.out.println("Time spent in local tree creation:"
					+ time_localtree + " counter: " + counter_only_local_tree);
			System.out.println("Time to create UL of prefix: " + time_createul);
			System.out.println("Time to create UL of extensions for FHM: "
					+ time_listul);
			System.out.println("Time taken by pure FHM: " + time_fhm);
			System.out.println("Time taken by bit GC: " + time_gc);
			System.out.println("Time taken by multi GC: " + multi_time);
			System.out.println("Time for constructing list from singleton items: "
					+ singleton_items_time + " count: " + singleton_items_count);
			System.out
					.println("Number of pure FHM calls (listUL): " + count_prefix);
		}
		// endTimestamp_before_veri = System.currentTimeMillis();

		// check the memory usage again and close the file.
		// checkMemory();
		MemoryLogger.getInstance().checkMemory();
		
		writer.close();

		// this.tempcandidateBufferWriter.close();
		// this.tempwriter.close();
		mapMinimumItemUtility = null;
		mapMaximumItemUtility = null;

	}

	// ************************************************************************************

	boolean check_equals_or_subset(ArrayList<Integer> itemset) {
		long temp = System.currentTimeMillis();
		if (itemset.size() >= blacklisted_itemset.size()
				&& blacklisted_itemset.size() != 0) {

			if (itemset.containsAll(blacklisted_itemset)) {
				contains_all_time = contains_all_time
						+ (System.currentTimeMillis() - temp);
				contains_all_counter++;
				return true;
			}
			contains_all_counter++;
			contains_all_time = contains_all_time
					+ (System.currentTimeMillis() - temp);
		}

		return false;
	}

	public List<EFIM_UP_Tree_Transaction_SPMF> txmerging(
			List<EFIM_UP_Tree_Transaction_SPMF> dataset) {
		// For merging transactions, we will keep track of the last transaction
		// read
		// and the number of identical consecutive transactions
		EFIM_UP_Tree_Transaction_SPMF previousTransaction = null;
		int consecutiveMergeCount = 0;
		List<EFIM_UP_Tree_Transaction_SPMF> merged_dataset = new ArrayList<EFIM_UP_Tree_Transaction_SPMF>();
		// For each transaction
		for (EFIM_UP_Tree_Transaction_SPMF transaction : dataset) {

			if (true) {
				// we cut the transaction starting from position 'e'
				EFIM_UP_Tree_Transaction_SPMF projectedTransaction = new EFIM_UP_Tree_Transaction_SPMF(
						transaction);

				// if it is the first transaction that we read
				if (previousTransaction == null) {
					// we keep the transaction in memory
					previousTransaction = projectedTransaction;
				} else if (isEqualTo(projectedTransaction, previousTransaction)) {
					// If it is not the first transaction of the database and
					// if the transaction is equal to the previously read
					// transaction,
					// we will merge the transaction with the previous one


					// if the first consecutive merge
					if (consecutiveMergeCount == 0) {
						// copy items and their profit from the previous
						// transaction
						int itemsCount = previousTransaction.items.length
								- previousTransaction.offset;
						int[] items = new int[itemsCount];
						System.arraycopy(previousTransaction.items,
								previousTransaction.offset, items, 0,
								itemsCount);
						int[] utilities = new int[itemsCount];
						System.arraycopy(previousTransaction.utilities,
								previousTransaction.offset, utilities, 0,
								itemsCount);

						// make the sum of utilities from the previous
						// transaction
						int positionPrevious = 0;
						int positionProjection = projectedTransaction.offset;
						while (positionPrevious < itemsCount) {
							utilities[positionPrevious] += projectedTransaction.utilities[positionProjection];
							positionPrevious++;
							positionProjection++;
						}

						// make the sum of prefix utilities
						int sumUtilities = previousTransaction.prefixUtility += projectedTransaction.prefixUtility;

						// create the new transaction replacing the two merged
						// transactions
						previousTransaction = new EFIM_UP_Tree_Transaction_SPMF(
								items,
								utilities,
								previousTransaction.transactionUtility
										+ projectedTransaction.transactionUtility);
						previousTransaction.prefixUtility = sumUtilities;

					} else {
						// if not the first consecutive merge

						// add the utilities in the projected transaction to the
						// previously
						// merged transaction
						int positionPrevious = 0;
						int positionProjected = projectedTransaction.offset;
						int itemsCount = previousTransaction.items.length;
						while (positionPrevious < itemsCount) {
							previousTransaction.utilities[positionPrevious] += projectedTransaction.utilities[positionProjected];
							positionPrevious++;
							positionProjected++;
						}

						// make also the sum of transaction utility and prefix
						// utility
						previousTransaction.transactionUtility += projectedTransaction.transactionUtility;
						previousTransaction.prefixUtility += projectedTransaction.prefixUtility;
					}
					// increment the number of consecutive transaction merged
					consecutiveMergeCount++;
				} else {
					// if the transaction is not equal to the preceding
					// transaction
					// we cannot merge it so we just add it to the database
					merged_dataset.add(previousTransaction);

					// the transaction becomes the previous transaction
					previousTransaction = projectedTransaction;
					// and we reset the number of consecutive transactions
					// merged
					consecutiveMergeCount = 0;
				}
			} 

		}
		if (previousTransaction != null) {
			merged_dataset.add(previousTransaction);

		}
		if(DEBUG){
			System.out.println("In try merging method");
			System.out.println("Original: " + dataset.size() + " merged: "
					+ merged_dataset.size());
		}
		return merged_dataset;
	}

	/**
	 * Check if two transaction are identical
	 * 
	 * @param t1
	 *            the first transaction
	 * @param t2
	 *            the second transaction
	 * @return true if they are equal
	 */
	private boolean isEqualTo(EFIM_UP_Tree_Transaction_SPMF t1,
			EFIM_UP_Tree_Transaction_SPMF t2) {
		// we first compare the transaction lenghts
		int length1 = t1.items.length - t1.offset;
		int length2 = t2.items.length - t2.offset;
		// if not same length, then transactions are not identical
		if (length1 != length2) {
			return false;
		}
		// if same length, we need to compare each element position by position,
		// to see if they are the same
		int position1 = t1.offset;
		int position2 = t2.offset;

		// for each position in the first transaction
		while (position1 < t1.items.length) {
			// if different from corresponding position in transaction 2
			// return false because they are not identical
			if (t1.items[position1] != t2.items[position2]) {
				return false;
			}
			// if the same, then move to next position
			position1++;
			position2++;
		}
		// if all items are identical, then return to true
		return true;
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
	private int upgrowth_fhm_hybrid_inner(UPTree_SPMF pass_tree, int threshold,
			int[] pass_prefix, int pass_item, NodeList_SPMF nList)
			throws IOException {
		number_of_recursions++;
		long temp1 = System.currentTimeMillis();

		int[] newPrefix = new int[pass_prefix.length + 1];
		System.arraycopy(pass_prefix, 0, newPrefix, 0, pass_prefix.length);
		newPrefix[pass_prefix.length] = pass_item;

		UPNode_SPMF pathCPB = pass_tree.mapItemNodes.get(pass_item);
		int supp = 0;
		// take item
		int pathCPBUtility = 0;
		// short max_quantity=pathCPB.max_quantity;

		while (pathCPB != null) {
			// sum of items node utility
			// if(pathCPB.max_quantity>max_quantity)
			// max_quantity=pathCPB.max_quantity;

			pathCPBUtility += pathCPB.nodeUtility;
			supp += pathCPB.count;
			pathCPB = pathCPB.nodeLink;

		}

		// *******************************************
		NodeList_SPMF node = new NodeList_SPMF(pass_item);
		node.addNode(nList);
		// *******************************************
		upgrowth_prefix_twu_nodelist += (System.currentTimeMillis() - temp1);
		if (pathCPBUtility >= threshold) {

			number_of_TWU++;

			int highCodeUtility = getNodeHighUtilityValue(node, supp);

			// clear the temp arraylist
			headerlist.clear();
			long temp = System.currentTimeMillis();

			// Create Local Tree
			UPTree_SPMF localTree = createLocalTree(threshold, pass_tree,
					pass_item);
			time_localtree += System.currentTimeMillis() - temp;
			counter_only_local_tree++;

			if (highCodeUtility >= threshold) {
				candidate_count_upg++;
				temp1 = System.currentTimeMillis();
				UtilityList_SPMF newCodeSofar_UL = create_UL(newPrefix,
						pass_item, 1);
				time_createul = time_createul
						+ (System.currentTimeMillis() - temp1);

				if (newCodeSofar_UL == null) {
					counternull++;
					return -1;
				}

				if (newCodeSofar_UL.sumIutils >= threshold) {
					// flag=1;
					writeOut(pass_prefix, newCodeSofar_UL.item, 1000 + newCodeSofar_UL.sumIutils);
					HUICount++;

				}

				if (newCodeSofar_UL.sumIutils + newCodeSofar_UL.sumRutils >= threshold) {
					count_prefix++;
					if (min_length > newPrefix.length)
						min_length = newPrefix.length;
					if (max_length < newPrefix.length)
						max_length = newPrefix.length;

					length_prefix += newPrefix.length;
					// writeTempCandidateFile(newPrefix,highCodeUtility);
					// Create the utility list of the supersets of
					// "newCodeSofar" and call fhm
					temp1 = System.currentTimeMillis();
					List<UtilityList_SPMF> ULs = list_UL(pass_prefix,
							newPrefix, headerlist, newCodeSofar_UL);
					time_listul = time_listul
							+ (System.currentTimeMillis() - temp1);
					temp1 = System.currentTimeMillis();

					fhm(newPrefix, newCodeSofar_UL, ULs, threshold);
					time_fhm = time_fhm + (System.currentTimeMillis() - temp1);

					temp1 = System.currentTimeMillis();
					if (Itemset_Utility_List_Map.containsKey(newPrefix))
						Itemset_Utility_List_Map.remove(newPrefix);
					newCodeSofar_UL.elements.clear();
					newCodeSofar_UL = null;
					pathCPB = null;
					newPrefix = null;
					for (UtilityList_SPMF temp_util_list : ULs)
						temp_util_list.elements.clear();
					ULs = null;
					node = null;
					localTree = null;
					time_gc = time_gc + (System.currentTimeMillis() - temp1);

				}

			}

			else {

				if (localTree.headerList.size() > 0) {

					upgrowth_fhm_hybrid_outer(localTree, threshold, newPrefix,
							node);
				}

			}
		}// If TWU >=threshold
		return 1;
	}

	// *********************************************************************************************

	public int getNodeHighUtilityValue(NodeList_SPMF nList, int support) {
		int utility = 0;
		NodeList_SPMF tempHead = nList;
		while (tempHead != null) {
			utility = utility
					+ this.getHighUtilityValue(tempHead.getItemName(), support);
			tempHead = tempHead.getNextNode();
		}
		return utility;
	}

	public int getHighUtilityValue(int itemName, int support) {
		int utility = 0;
		// short item=(shortitemName;
		utility = support * mapMaximumItemUtility.get(itemName);

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
	private void upgrowth_fhm_hybrid_outer(UPTree_SPMF tree, int minUtility,
			int[] prefix, NodeList_SPMF node) throws IOException {

		MemoryLogger.getInstance().checkMemory();

		counter_total_itemsets++;
		long temp_time = System.currentTimeMillis();
		ArrayList<Integer> key = new ArrayList<Integer>();
		if (prefix != null && prefix.length > 0) {

			for (int i = 0; i < prefix.length; i++) {
				key.add(prefix[i]);
			}

		}
		upgrowth_p1 += (System.currentTimeMillis() - temp_time);
		for (int i = tree.headerList.size() - 1; i >= 0; i--) {

			if (prefix != null && prefix.length > 0) {
				temp_time = System.currentTimeMillis();

				if (check_equals_or_subset(key)) {
					upgrowth_p2 += (System.currentTimeMillis() - temp_time);
					counter_optimization++;
					return;
				}
				upgrowth_p2 += (System.currentTimeMillis() - temp_time);
			}
			// get the item
			long new_temp_time = System.currentTimeMillis();
			Integer item = tree.headerList.get(i);
			upgrowth_fhm_hybrid_inner(tree, minUtility, prefix, item, node);
			upgrowth_p3 += (System.currentTimeMillis() - new_temp_time);

		}
		long t = System.currentTimeMillis();
		if (multi.containsKey(key.toString())) {
			LinkedList<String> prefix_superset_itemsets = multi.get(key
					.toString());

			for (String itemset : prefix_superset_itemsets) {

				if (Itemset_Utility_List_Map.containsKey(itemset)) {
					Itemset_Utility_List_Map.get(itemset).elements.clear();
					Itemset_Utility_List_Map.remove(itemset);
				}

			}
			prefix_superset_itemsets.clear();
			prefix_superset_itemsets = null;
			multi.remove(key.toString());
		}
		if (Itemset_Utility_List_Map.containsKey(key.toString())) {
			Itemset_Utility_List_Map.get(key.toString()).elements.clear();
			Itemset_Utility_List_Map.remove(key.toString());
		}

		multi_time += System.currentTimeMillis() - t;

	}

	private UPTree_SPMF createLocalTree(int minUtility, UPTree_SPMF tree,
			Integer item) {

		// === Construct conditional pattern base ===
		// It is a subdatabase which consists of the set of prefix paths
		List<List<UPNode_SPMF>> prefixPaths = new ArrayList<List<UPNode_SPMF>>();
		UPNode_SPMF path = tree.mapItemNodes.get(item);

		// map to store path utility of local items in CPB
		final Map<Integer, Integer> itemPathUtility = new HashMap<Integer, Integer>();
		while (path != null) {

			// get the Node Utiliy of the item
			int nodeutility = path.nodeUtility;
			// if the path is not just the root node
			if (path.parent.itemID != -1) {
				// create the prefixpath
				List<UPNode_SPMF> prefixPath = new ArrayList<UPNode_SPMF>();
				// add this node.
				prefixPath.add(path); // NOTE: we add it just to keep its
				// actually it should not be part of the prefixPath

				// Recursively add all the parents of this node.
				UPNode_SPMF parentnode = path.parent;
				Integer pu;
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
		UPTree_SPMF localTree = new UPTree_SPMF();

		// for each prefixpath
		int pathCount, supp;
		int pathUtility;
		List<UPNode_SPMF> localPath;
		for (List<UPNode_SPMF> prefixPath : prefixPaths) {
			// the Utility of the prefixpath is the node utility of its
			// first node.
			pathCount = prefixPath.get(0).count;
			pathUtility = prefixPath.get(0).nodeUtility;

			localPath = new ArrayList<UPNode_SPMF>();
			// for each node in the prefixpath,
			// except the first one, we count the frequency
			for (int j = 1; j < prefixPath.size(); j++) {

				int itemValue = 0; // It store multiplication of minimum
									// item utility and pathcount
				// for each node in prefixpath
				UPNode_SPMF node = prefixPath.get(j);

				// Here is DLU Strategy #################
				// we check whether local item is promising or not
				if (itemPathUtility.get(node.itemID) >= minUtility) {
					localPath.add(node);
					// localPath_nodes.put(node.itemID,node);

				} else { // If item is unpromising then we recalculate path
							// utility
					Integer minItemUtility = 0;
					minItemUtility = node.min_node_utility;
					itemValue = minItemUtility * pathCount;
				}
				pathUtility = pathUtility - itemValue;

			}

			// we reorganize local path in decending order of path utility
			/*
			 * Collections.sort(localPath, new Comparator<UPNode>() {
			 * 
			 * public int compare(UPNode o1, UPNode o2) { // compare the TWU of
			 * the items //return compareItemsDesc(o1.itemID, o2.itemID,
			 * itemPathUtility); return
			 * compareItemsDesc(o1.itemID,o2.itemID,mapItemToTWU); } });
			 */
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
				// throw e;
			}
		}

		// We create the local header table for the tree item - CPB
		// localTree.createHeaderList(itemPathUtility);
		localTree.createHeaderList(mapItemToTWU, this.newNamesToOldNames);
		int temp_item;
		for (int i = 0; i < localTree.headerList.size(); i++) {

			temp_item = localTree.headerList.get(i);

			headerlist.add(temp_item);

		}

		return localTree;
	}

	/* ---------------------------------------------------------- */
	/*                                                            */
	/* FHM */
	/*                                                            */
	/* ---------------------------------------------------------- */
	private void fhm(int[] prefix, UtilityList_SPMF pUL,
			List<UtilityList_SPMF> ULs, int minUtility) throws IOException {

		long temp_time = 0;
		UtilityList_SPMF X, Y, temp;
		List<UtilityList_SPMF> exULs;
		Integer twuF;
		for (int i = 0; i < ULs.size(); i++) {

			temp_time = System.currentTimeMillis();
			X = ULs.get(i);
			if (X.sumIutils >= minUtility) {
				writeOut(prefix, X.item, X.sumIutils);
				HUICount++;

			}
			time_phase1 = time_phase1
					+ (System.currentTimeMillis() - temp_time);

			// If the sum of the remaining utilities for pX
			// is higher than minUtility, we explore extensions of pX.
			// (this is the pruning condition)
			if (X.sumIutils + X.sumRutils >= minUtility) {

				// This list will contain the utility lists of pX extensions.
				exULs = new ArrayList<UtilityList_SPMF>();
				// For each extension of p appearing
				// after X according to the ascending order
				temp_time = System.currentTimeMillis();
				for (int j = i + 1; j < ULs.size(); j++) {

					Y = ULs.get(j);
					long temp_time1 = System.currentTimeMillis();
					// ======================== NEW OPTIMIZATION USED IN FHM
					Map<Integer, Integer> mapTWUF = mapFMAP.get(X.item);
					if (mapTWUF != null) {
						twuF = mapTWUF.get(Y.item);
						if (twuF != null && twuF < minUtility) {
							time_opti = time_opti
									+ (System.currentTimeMillis() - temp_time1);
							continue;
						}
					}
					time_opti = time_opti + System.currentTimeMillis()
							- temp_time1;
					candidateCount++;
					candidate_count_fhm++;
					// =========================== END OF NEW OPTIMIZATION

					// we construct the extension pXY
					// and add it to the list of extensions of pX
					temp_time1 = System.currentTimeMillis();
					temp = null;

					temp = construct(pUL, X, Y);
					construct_time_fhm = construct_time_fhm
							+ (System.currentTimeMillis() - temp_time1);
					number_construct_fhm++;
					number_of_loop_calls++;
					exULs.add(temp);
				}
				time_phase2 = time_phase2
						+ (System.currentTimeMillis() - temp_time);
				// We create new prefix pX
				temp_time = System.currentTimeMillis();
				long temp_time1 = System.currentTimeMillis();

				int[] newPrefix = new int[prefix.length + 1];
				System.arraycopy(prefix, 0, newPrefix, 0, prefix.length);
				newPrefix[prefix.length] = X.item;

				time_prefix_creation = time_prefix_creation
						+ (System.currentTimeMillis() - temp_time1);
				time_phase3 = time_phase3
						+ (System.currentTimeMillis() - temp_time);
				temp_time = System.currentTimeMillis();
				fhm(newPrefix, X, exULs, minUtility);
				time_phase4 = time_phase4
						+ (System.currentTimeMillis() - temp_time);
			}

		}
		ULs.clear();
		ULs = null;
	}

	/**
	 * This method constructs the utility list of pXY
	 * 
	 * @param P
	 *            : the utility list of prefix P.
	 * @param px
	 *            : the utility list of pX
	 * @param py
	 *            : the utility list of pY
	 * @return the utility list of pXY
	 */
	private UtilityList_SPMF construct(UtilityList_SPMF P, UtilityList_SPMF px,
			UtilityList_SPMF py) {
		// create an empty utility list for pXY
		UtilityList_SPMF pxyUL = new UtilityList_SPMF(py.item);
		// for each element in the utility list of pX
		int min_rutil;
		y_pos = 0;
		p_pos = 0;

		if (px.elements.size() <= py.elements.size()) {
			for (Element_SPMF ex : px.elements) {
				// do a binary search to find element ey in py with tid = ex.tid
				Element_SPMF ey = findElementWithTID2(py, ex.tid, y_pos);
				if (ey == null) {
					continue;
				}
				// min_rutil=0;

				// if(ex.rutils>=ey.rutils)
				min_rutil = ey.rutils;
				// else
				// min_rutil=ex.rutils;
				// if the prefix p is null
				if (P == null) {
					// Create the new element
					Element_SPMF eXY = null;
					eXY = new Element_SPMF(ex.tid, ex.iutils + ey.iutils,
							min_rutil);
					pxyUL.addElement(eXY);

				} else {
					// find the element in the utility list of p wih the same
					// tid
					Element_SPMF e = findElementWithTID2(P, ex.tid, p_pos);
					if (e != null) {
						// Create new element
						Element_SPMF eXY = null;
						eXY = new Element_SPMF(ex.tid, ex.iutils + ey.iutils
								- e.iutils, min_rutil);
						pxyUL.addElement(eXY);
					}
				}
			}
		} else {
			for (Element_SPMF ey : py.elements) {
				// do a binary search to find element ey in py with tid = ex.tid
				Element_SPMF ex = findElementWithTID2(px, ey.tid, y_pos);
				if (ex == null) {
					continue;
				}
				// min_rutil=0;

				// if(ex.rutils>=ey.rutils)
				min_rutil = ey.rutils;
				// else
				// min_rutil=ex.rutils;
				// if the prefix p is null
				if (P == null) {
					// Create the new element
					Element_SPMF eXY = null;
					eXY = new Element_SPMF(ex.tid, ex.iutils + ey.iutils,
							min_rutil);
					pxyUL.addElement(eXY);

				} else {
					// find the element in the utility list of p wih the same
					// tid
					Element_SPMF e = findElementWithTID2(P, ex.tid, p_pos);
					if (e != null) {
						// Create new element
						Element_SPMF eXY = null;
						eXY = new Element_SPMF(ex.tid, ex.iutils + ey.iutils
								- e.iutils, min_rutil);
						pxyUL.addElement(eXY);
					}
				}
			}

		}
		// return the utility list of pXY.
		return pxyUL;
	}

	/**
	 * Do a binary search to find the element with a given tid in a utility list
	 * 
	 * @param ulist
	 *            the utility list
	 * @param tid
	 *            the tid
	 * @return the element or null if none has the tid.
	 */

	private Element_SPMF findElementWithTID2(UtilityList_SPMF ulist, int tid,
			int x) {
		List<Element_SPMF> list = ulist.elements;

		// perform a binary search to check if the subset appears in level k-1.
		int first = x;
		int last = list.size() - 1;

		// the binary search
		while (first <= last) {
			int middle = (first + last) >>> 1; // divide by 2

			if (list.get(middle).tid < tid) {
				first = middle + 1; // the itemset compared is larger than the
									// subset according to the lexical order
			} else if (list.get(middle).tid > tid) {
				last = middle - 1; // the itemset compared is smaller than the
									// subset is smaller according to the
									// lexical order
			} else {
				x = middle;
				return list.get(middle);
			}
		}
		return null;
	}

	UtilityList_SPMF construct_list_from_singleton_items(int[] itemset) {
		UtilityList_SPMF last_two_items = null, merge_temp = null;
		ArrayList<Integer> index_last = new ArrayList<Integer>();

		index_last.add(itemset[0]);
		index_last.add(itemset[1]);

		LinkedList<String> temp_linked_list = new LinkedList<String>();
		String tempString = null;

		long temp_time1 = System.currentTimeMillis();
		last_two_items = Itemset_Utility_List_Map.get(index_last.toString());
		time_hmap = time_hmap + (System.currentTimeMillis() - temp_time1);
		if (last_two_items != null) {

			if (last_two_items.sumIutils + last_two_items.sumRutils < utilitythreshold) {
				/*
				 * for(int i=2;i<itemset.length-1;i++) {
				 * index_last.add(itemset[i]);
				 * Itemset_Utility_List_Map.put(index_last.toString(),
				 * last_two_items);
				 * 
				 * }
				 */
				long temp_time = System.currentTimeMillis();
				if (blacklisted_itemset.size() == 0)
					blacklisted_itemset.addAll(index_last);
				else if (index_last.containsAll(blacklisted_itemset)) {

					// do nothing since subset is already there
				} else {
					blacklisted_itemset.clear();
					blacklisted_itemset.addAll(index_last);
				}
				upgrowth_p2 += (System.currentTimeMillis() - temp_time);

				index_last.clear();
				index_last = null;
				last_two_items = null;

				return null;
			}
			// if(!Itemset_Utility_List_Map.containsKey(index_last))
			{
				// Itemset_Utility_List_Map.put(index_last.toString(),
				// last_two_items);
				// tempString=index_last.toString();
				// temp_linked_list.add(index_last.toString());
			}

			merge_temp = last_two_items;
		} else {
			// construct utility list of first two items of the itemset
			index_last.remove(1);
			temp_time1 = System.currentTimeMillis();
			UtilityList_SPMF indexlast_list = Itemset_Utility_List_Map
					.get(index_last.toString());
			time_hmap = time_hmap + (System.currentTimeMillis() - temp_time1);
			ArrayList<Integer> indexsecond_last = new ArrayList<Integer>();

			indexsecond_last.add(itemset[1]);
			// indexsecond_last.add(itemset[itemset.length-2]);

			temp_time1 = System.currentTimeMillis();
			UtilityList_SPMF indexsecondlast_list = Itemset_Utility_List_Map
					.get(indexsecond_last.toString());
			time_hmap = time_hmap + (System.currentTimeMillis() - temp_time1);
			temp_time1 = System.currentTimeMillis();

			try {
				merge_temp = construct(null, indexlast_list,
						indexsecondlast_list);
			} catch (Exception e) {
				System.out.println("Caught");
			}

			construct_time_our = construct_time_our
					+ (System.currentTimeMillis() - temp_time1);

			number_construct_our++;

			index_last.add(itemset[1]);
			temp_time1 = System.currentTimeMillis();
			/*
			 * if(!Itemset_Utility_List_Map.containsKey(index_last)) {
			 * Itemset_Utility_List_Map.put(index_last.toString(), merge_temp);
			 * }
			 */
			time_hmap = time_hmap + (System.currentTimeMillis() - temp_time1);

			if (merge_temp.sumIutils + merge_temp.sumRutils < utilitythreshold) {
				/*
				 * if(!Itemset_Utility_List_Map.containsKey(index_last)) {
				 * Itemset_Utility_List_Map.put(index_last.toString(),
				 * merge_temp); } for(int i=2;i<itemset.length-1;i++) {
				 * index_last.add(itemset[i]);
				 * Itemset_Utility_List_Map.put(index_last.toString(),
				 * last_two_items); }
				 */
				long temp_time = System.currentTimeMillis();
				if (blacklisted_itemset.size() == 0)
					blacklisted_itemset.addAll(index_last);
				else if (index_last.containsAll(blacklisted_itemset)) {
					// do nothing since subset is already there
				} else {
					blacklisted_itemset.clear();
					blacklisted_itemset.addAll(index_last);
				}
				upgrowth_p2 += (System.currentTimeMillis() - temp_time);

				index_last.clear();
				index_last = null;
				merge_temp = null;
				last_two_items = null;
				return null;
			}
			// if(!Itemset_Utility_List_Map.containsKey(index_last))
			{
				Itemset_Utility_List_Map.put(index_last.toString(), merge_temp);
				tempString = index_last.toString();
				temp_linked_list.add(index_last.toString());
			}

		}
		ArrayList<Integer> temp;
		for (int i = 2; i <= itemset.length - 1; i++)

		{
			temp = new ArrayList<Integer>();

			temp.add(itemset[i]);
			index_last.add(itemset[i]);

			temp_time1 = System.currentTimeMillis();
			UtilityList_SPMF temp_list = Itemset_Utility_List_Map
					.get(index_last.toString());
			time_hmap = time_hmap + (System.currentTimeMillis() - temp_time1);
			if (temp_list == null) {
				temp_time1 = System.currentTimeMillis();

				merge_temp = construct(null, merge_temp,
						Itemset_Utility_List_Map.get(temp.toString()));
				construct_time_our = construct_time_our
						+ (System.currentTimeMillis() - temp_time1);
				number_construct_our++;

				temp_time1 = System.currentTimeMillis();
				/*
				 * if(!Itemset_Utility_List_Map.containsKey(index_last)) {
				 * Itemset_Utility_List_Map.put(index_last.toString(),
				 * merge_temp); }
				 */

				Itemset_Utility_List_Map.put(index_last.toString(), merge_temp);
				if (tempString == null)
					tempString = index_last.toString();
				temp_linked_list.add(index_last.toString());
				time_hmap = time_hmap
						+ (System.currentTimeMillis() - temp_time1);
			} else
				merge_temp = temp_list;

			if (merge_temp.sumIutils + merge_temp.sumRutils < utilitythreshold
					&& i != 0) {
				/*
				 * if(!Itemset_Utility_List_Map.containsKey(index_last)) {
				 * Itemset_Utility_List_Map.put(index_last.toString(),
				 * merge_temp); }
				 * 
				 * for(int j=i+1;j<itemset.length-1;j++) {
				 * index_last.add(itemset[j]);
				 * Itemset_Utility_List_Map.put(index_last.toString(),
				 * merge_temp); }
				 */
				long temp_time = System.currentTimeMillis();
				if (blacklisted_itemset.size() == 0)
					blacklisted_itemset.addAll(index_last);
				else if (index_last.containsAll(blacklisted_itemset)) {
					// do nothing since subset is already there
				} else {
					blacklisted_itemset.clear();
					blacklisted_itemset.addAll(index_last);
				}
				upgrowth_p2 += (System.currentTimeMillis() - temp_time);

				index_last.clear();
				index_last = null;
				merge_temp = null;
				return null;
			}

		}
		multi.put(tempString, temp_linked_list);
		index_last.clear();
		index_last = null;
		last_two_items = null;
		return merge_temp;
	}

	// Creates the utility list of itemset from singleton items.
	UtilityList_SPMF create_UL(int[] itemset, int X, int flag) {
		number_create_UL++;
		long temp_time = System.currentTimeMillis();
		UtilityList_SPMF temp = null;
		long temp_time1 = 0;
		ArrayList<Integer> key_temp = new ArrayList<Integer>();
		// when the length of the itemset is 1
		if (itemset.length == 1) {
			temp_time1 = System.currentTimeMillis();
			key_temp.add(itemset[0]);
			temp = Itemset_Utility_List_Map.get(key_temp.toString());
			time_hmap = time_hmap + (System.currentTimeMillis() - temp_time1);
			key_temp.clear();
			key_temp = null;
			return temp;
		}

		// check if complete itemset is in HashMap or not
		temp_time1 = System.currentTimeMillis();
		for (int i = 0; i < itemset.length - 1; i++) {
			key_temp.add(itemset[i]);
		}
		/*
		 * temp=Itemset_Utility_List_Map.get(key_temp.toString());
		 * time_hmap=time_hmap + (System.currentTimeMillis()-temp_time1);
		 * if(temp!=null) { key_temp.clear(); key_temp=null; return temp; }
		 */

		// Approach in which we just check for one itemset existence of length
		// size-1.
		// int last_index=key_temp.size();
		// temp_time1=System.currentTimeMillis();
		// System.out.println(itemset.length+" "+X);
		// key_temp.remove(last_index-1);

		temp = Itemset_Utility_List_Map.get(key_temp.toString());
		time_hmap = time_hmap + (System.currentTimeMillis() - temp_time1);
		if (temp != null) {

			// Merge the utility List of itemset[1...................length-2]
			// with the utility list of prefix(itemset[length-1)
			ArrayList<Integer> X_temp = new ArrayList<Integer>();
			temp_time1 = System.currentTimeMillis();
			X_temp.add(itemset[itemset.length - 1]);
			UtilityList_SPMF uX = Itemset_Utility_List_Map.get(X_temp
					.toString());
			time_hmap = time_hmap + (System.currentTimeMillis() - temp_time1);
			temp_time1 = System.currentTimeMillis();
			UtilityList_SPMF new_utility_list = construct(null, temp, uX);
			construct_time_our = construct_time_our
					+ (System.currentTimeMillis() - temp_time1);
			number_construct_our++;
			create_UL_time = create_UL_time
					+ (System.currentTimeMillis() - temp_time);
			X_temp.clear();
			X_temp = null;

			temp = null;
			uX = null;
			key_temp.clear();
			key_temp = null;
			return new_utility_list;
		} else {
			key_temp.add(itemset[itemset.length - 1]);
			temp = new UtilityList_SPMF(X);
			long t = System.currentTimeMillis();
			temp = construct_list_from_singleton_items(itemset);
			// this.Itemset_Utility_List_Map.put(key_temp.toString(), temp);

			singleton_items_time += System.currentTimeMillis() - t;
			singleton_items_count++;
			if (flag == 1) {
				if (temp == null) {
					key_temp.clear();
					key_temp = null;
					return null;
				}
			}
			temp_time1 = System.currentTimeMillis();
			// Itemset_Utility_List_Map.put(key_temp.toString(),temp);
			time_hmap = time_hmap + (System.currentTimeMillis() - temp_time1);
			create_UL_time = create_UL_time
					+ (System.currentTimeMillis() - temp_time);
			key_temp.clear();
			key_temp = null;
			return temp;
		}
		// ************************************************************************
	}

	// Creates the utility list of every superset of the itemset newCodeSofar
	List<UtilityList_SPMF> list_UL(int[] pass_prefix, int[] new_prefix,
			List<Integer> HeaderList, UtilityList_SPMF Px) {
		ArrayList<UtilityList_SPMF> temp_list = new ArrayList<UtilityList_SPMF>();
		long temp = System.currentTimeMillis();
		long temp_time1;
		if (HeaderList.size() > 0) {

			int[] Xarray = new int[1];
			Xarray[0] = new_prefix[new_prefix.length - 1];
			// UtilityList
			// X=create_UL(Xarray,new_prefix[new_prefix.length-1],0);
			ArrayList<Integer> key_temp = new ArrayList<Integer>();
			key_temp.add(Xarray[0]);

			UtilityList_SPMF X = this.Itemset_Utility_List_Map.get(key_temp
					.toString());
			key_temp.clear();
			int item;
			int[] Yarray = new int[1];
			if (pass_prefix != null && pass_prefix.length > 0
					&& HeaderList.size() - 1 >= 0) {
				// PuL=create_UL(pass_prefix,(short)-1,0);
				for (int index = 0; index < pass_prefix.length; index++) {
					key_temp.add(pass_prefix[index]);
				}
				key_temp.clear();

				/*
				 * for(int index=0;index<new_prefix.length;index++) {
				 * key_temp.add(new_prefix[index]); }
				 * //Px=construct(null,PuL,X);
				 * //Px=this.Itemset_Utility_List_Map.get(key_temp.toString());
				 * key_temp.clear();
				 */

			}
			for (int i = HeaderList.size() - 1; i >= 0; i--) {

				item = HeaderList.get(i);

				Yarray[0] = item;
				key_temp.add(item);
				// UtilityList Y=create_UL(Yarray,item,0);
				UtilityList_SPMF Y = this.Itemset_Utility_List_Map.get(key_temp
						.toString());
				key_temp.clear();
				UtilityList_SPMF tempnewCodeSofar_UL = null;
				if (pass_prefix != null && pass_prefix.length > 0) {

					temp_time1 = System.currentTimeMillis();

					// tempnewCodeSofar_UL=construct(PuL,Px,Py);
					tempnewCodeSofar_UL = construct(null, Px, Y);

					construct_time_our = construct_time_our
							+ (System.currentTimeMillis() - temp_time1);
					number_construct_our = number_construct_our + 3;
				} else {
					temp_time1 = System.currentTimeMillis();
					tempnewCodeSofar_UL = construct(null, X, Y);
					construct_time_our = construct_time_our
							+ (System.currentTimeMillis() - temp_time1);
					number_construct_our++;
				}
				Y = null;
				temp_list.add(tempnewCodeSofar_UL);

			}
			Px = null;
			X = null;

		}

		time_fhm_ancestor_codes = time_fhm_ancestor_codes
				+ (System.currentTimeMillis() - temp);
		return temp_list;
	}
	
	/**
	 * Write a HUI to the output file
	 * @param HUI
	 * @param utility
	 * @param sumIutils 
	 * @throws IOException
	 */
	private void writeOut(int[] itemset,  int item, int utility) throws IOException {
//		huiCount++; // increase the number of high utility itemsets found

		//Create a string buffer
		StringBuilder buffer = new StringBuilder();
		//Append each item
		for (int  i = 0; i < itemset.length; i++) {
			buffer.append(newNamesToOldNames[itemset[i]]);
			buffer.append(' ');
		}
		buffer.append(newNamesToOldNames[item]);
		buffer.append(' ');
		
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
		System.out.println("=============  UFH ALGORITHM - SPMF 0.2.35 - STATS =============");
		System.out.println(" Total time: "                  + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Memory:  "                      + MemoryLogger.getInstance().getMaxMemory()  + " MB");
		System.out.println(" HUI count: "        + HUICount);
		System.out.println("===================================================");
	}
}