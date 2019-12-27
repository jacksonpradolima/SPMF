package ca.pfv.spmf.algorithms.frequentpatterns.fhmds.naive;

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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * This is an implementation of the "FHM-DS-Naive" algorithm for High-Utility Itemsets
 * Mining in a stream as described in the conference paper : <br/>
 * <br/>
 * 
 * Siddharth Dawar, Veronica Sharma, Vikram Goyal: Mining top-k high-utility itemsets
 *  from a data stream under sliding window model. Appl. Intell. 47(4): 1240-1255 (2017)
 *
 * @see UtilityList
 * @see Element
 * @author Siddharth Dawar et al.
 */
public class AlgoFHMDS_Naive {

	/**  the maximum memory usage */
	public double maxMemory = 0; 
	
	/** the time the algorithm started */
	public long startTimestamp = 0, startTimestamp2 = 0, endTimestamp2 = 0; 
	
	/** the time the algorithm terminated */
	public long endTimestamp = 0, construct_time = 0, fhm_time = 0; 
	
	/** the number of HUI generated */
	public int huiCount = 0; 
	
	public long min_supp = 0, max_supp = 0, avg_supp = 0;
	public int candidateCount = 0, construct_calls = 0;
	
	/** number of batches that have been processed */
	int processedBatchCount;

	/** Map to remember the TWU of each item for first window only
	// The ordering of items decided in the first window is fixed for rest
	// of the windows also, unlike FHM_Naive where the ordering is
	// different for each window as FHM sorts items in ascending
	// order of TWU values. */
	static Map<Integer, Float> mapItemToTWU = new HashMap<Integer, Float>();


	/** writer to write the output file */
	BufferedWriter writer = null;

	// NEW OPTIMIZATION - FMAP (FAST)
	Map<Integer, Map<Integer, Float>> mapFMAP; // PAIR OF ITEMS , item --> item,
												// twu
	// END NEW OPTIMIZATION
	/** Extra variables to store information for top-k computation */
	public int k, win_size, number_of_transactions_batch, win_number;
	static float min_top_k_utility_current_window = 0F;
	
	static ArrayList<ArrayList<String>> window = new ArrayList<ArrayList<String>>();

	/** Structure to store the top k HUIs */
	static private List<Itemset> top_k_hui = new ArrayList<Itemset>();

	/**  CREATE A LIST TO STORE THE UTILITY LIST OF ITEMS WITH TWU >= MIN_UTILITY. */
	static List<UtilityList> listOfUtilityLists = new ArrayList<UtilityList>();
	
	/** A MAP TO STORE THE UTILITY LIST FOR EACH ITEM.
	// Key : item Value : utility list associated to that item */
	static Map<Integer, UtilityList> mapItemToUtilityList = new HashMap<Integer, UtilityList>();

	
	/** variable for activating the debug mode */
	boolean debug = false;

	// this class represent an item and its utility in a transaction
	class Pair {
		int item = 0;
		float utility = 0;

		public String toString() {
			return "[" + item + "," + utility + "]";
		}
	}

	/**
	 * Default constructor
	 */
	public AlgoFHMDS_Naive() {

	}

	long total = 0, hui = 0;

	/**
	 * Run the algorithm
	 * 
	 * @param input
	 *            the input file path
	 * @param output
	 *            the output file path
	 * @param minUtility
	 *            the minimum utility threshold
	 * @throws IOException
	 *             exception if error while writing the file
	 */
	public void runAlgorithm(String transactionFile, int k, int win_size,
			int number_of_transactions_batch, String resultFile)
			throws IOException {

		processedBatchCount = 0;
		this.k = k;
		this.win_size = win_size;
		this.number_of_transactions_batch = number_of_transactions_batch;
		// PQ=new PriorityQueue<Float>(k);
		// this.min_top_k_utility_current_window=229763;

		startTimestamp = System.currentTimeMillis();

		// Scan the complete database to create windows and call FHM.
		BufferedReader myInput = null;
		String thisLine;
		int iterateBatch = 0, iterateWindow = 0, windowCount = 0, batchNumber = 0;
		ArrayList<String> batchTransaction = new ArrayList<String>();
		// this.min_top_k_utility_current_window=146871.66F;
		try {
			// prepare the object for reading the file
			myInput = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(transactionFile))));
//			int counter = 0;
//			int flag = 1;
			// for each line (transaction) until the end of file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is a comment, is empty or is a
				// kind of metadata
				if (thisLine.isEmpty() == true || thisLine.charAt(0) == '#'
						|| thisLine.charAt(0) == '%'
						|| thisLine.charAt(0) == '@') {
					continue;
				}
				
				iterateBatch++;
				if (iterateBatch <= this.number_of_transactions_batch) {
					batchTransaction.add(thisLine);
				}

				if ((iterateBatch == this.number_of_transactions_batch)) {
					iterateBatch = 0;
					batchNumber++;
					window.add(new ArrayList<String>(batchTransaction));
					batchTransaction.clear();
					iterateWindow++;
					// if iterateWindow equals to window size
					if (iterateWindow >= this.win_size) {
						windowCount++;
						// invoke FHM on this Window

						initial_call_FHM(window, windowCount, resultFile);
						writeResultTofile(resultFile, batchNumber == 1);
						// this.min_top_k_utility_current_window=0;
						window.remove(0);
//						flag = 0;
					}
				}

			}

			// if it is last batch with elements less than user specified batch
			// elements
			if ((iterateBatch > 0)
					&& (iterateBatch < this.number_of_transactions_batch)) {
				windowCount++;
				batchNumber++;
				// window.remove(0);
				window.add(batchTransaction);
				initial_call_FHM(window, windowCount, resultFile);
				writeResultTofile(resultFile, true);
				batchTransaction.clear();
			}
		} catch (Exception e) {
			// catches exception if error while reading the input file
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
			batchTransaction.clear();
			window.clear();
			thisLine = "";
		}
		endTimestamp = System.currentTimeMillis();
		// this.printStats(resultFile, transactionFileLoc);

	}

	void initial_call_FHM(ArrayList<ArrayList<String>> window,
			int windowCount, String resultFile) {
		
		top_k_hui.clear();

		// System.out.println("Computation on Window: "+windowNumber);
		// We create a map to store the TWU of each item
		mapItemToTWU = new HashMap<Integer, Float>();
		mapFMAP = new HashMap<Integer, Map<Integer, Float>>();
		listOfUtilityLists = new ArrayList<UtilityList>();
		mapItemToUtilityList = new HashMap<Integer, UtilityList>();

		// mapItemToTWU = new HashMap<Integer, Float>();
		startTimestamp2 = System.currentTimeMillis();
		win_number = windowCount;

		/*
		 * try { writer = new BufferedWriter(new
		 * FileWriter("./src/result_fhm_top_100_new_DS_preinsertion_lexi.txt"));
		 * } catch (IOException e1) { 
		 * e1.printStackTrace(); }
		 */
		// We scan the database first time to calculate the TWU of each item.
		for (ArrayList<String> batch_transactions : window) {
			for (String thisLine : batch_transactions) {


				// ======= PHIL ====
				// split the line according to the separator
				String split[] = thisLine.split(":");
				// get the list of items
				String items[] = split[0].split(" ");
				// the second part is the transaction utility
				float transactionUtility = Integer.parseInt(split[1]);  
				// get the list of utility values corresponding to each item
				// for that transaction
//				String utilityValues[] = split[2].split(" ");

				// ======= END PHIL ====
				
				// for each item
				for(int i=0; i <items.length; i++){

					Integer item = Integer.parseInt(items[i]);
					
					Float twu = mapItemToTWU.get(item);
					twu = (twu == null) ? transactionUtility : twu
							+ transactionUtility;
					mapItemToTWU.put(item, twu);

				}
			}
		}

		// System.out.println("Number of transactions in Window: "+counter);

		for (Integer item : mapItemToTWU.keySet()) {
			// if the item is promising (TWU >= minutility)

			if (mapItemToTWU.get(item) >= min_top_k_utility_current_window) {
				// create an empty Utility List that we will fill later.
				UtilityList uList = new UtilityList(item, this.win_size,
						windowCount);
				mapItemToUtilityList.put(item, uList);
				// add the item to the list of high TWU items
				listOfUtilityLists.add(uList);
				// counter++;

			}

		}
		// System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		// SORT THE LIST OF HIGH TWU ITEMS IN ASCENDING ORDER
		Collections.sort(listOfUtilityLists, new Comparator<UtilityList>() {
			public int compare(UtilityList o1, UtilityList o2) {
				// compare the TWU of the items
				return compareItems(o1.item, o2.item);
			}
		});
		if (debug) {
			System.out.println("Window: " + windowCount);
		}

		// SECOND DATABASE PASS TO CONSTRUCT THE UTILITY LISTS
		// OF 1-ITEMSETS HAVING TWU >= minutil (promising items)
		int tid = (windowCount - 1) * this.number_of_transactions_batch;
		for (ArrayList<String> batch_transactions : window) {
			for (String thisLine : batch_transactions) {
				tid++;
				float remainingUtility = 0;
//				float newTWU = 0;
				
				// ======= PHIL ====
				// split the line according to the separator
				String split[] = thisLine.split(":");
				// get the list of items
				String items[] = split[0].split(" ");
				// get the list of utility values corresponding to each item
				// for that transaction
				String utilityValues[] = split[2].split(" ");
		
				// ======= END PHIL ====
				
//				String line = thisLine.trim();
//				String[] lineSplited = line.split(" ");
				List<Pair> revisedTransaction = new ArrayList<Pair>();
				
				// for each item
				for(int i=0; i <items.length; i++){
					/// convert values to integers
					Pair pair = new Pair();
					pair.item = Integer.parseInt(items[i]);
					pair.utility = Integer.parseInt(utilityValues[i]);
					// if the item has enough utility
					if (mapItemToTWU.get(pair.item) >= min_top_k_utility_current_window) {
						// add it
						revisedTransaction.add(pair);
						remainingUtility += pair.utility;
//						newTWU += pair.utility; // NEW OPTIMIZATION
					}
				}

				// sort the transaction
				// System.setProperty("java.util.Arrays.useLegacyMergeSort",
				// "true");
				Collections.sort(revisedTransaction, new Comparator<Pair>() {
					public int compare(Pair o1, Pair o2) {
						return compareItems(o1.item, o2.item);
					}
				});

				// for each item left in the transaction
				for (int i = 0; i < revisedTransaction.size(); i++) {
					Pair pair = revisedTransaction.get(i);

//					float remain = remainingUtility; // FOR OPTIMIZATION

					// subtract the utility of this item from the remaining
					// utility
					remainingUtility = remainingUtility - pair.utility;

					// get the utility list of this item
					UtilityList utilityListOfItem = mapItemToUtilityList
							.get(pair.item);

					// Add a new Element to the utility list of this item
					// corresponding to this transaction
					Element element = new Element(tid, pair.utility,
							remainingUtility);

					utilityListOfItem.addElement(element, win_size,
							number_of_transactions_batch);

				}

			}

		}

		// Add exact utility of single items to top-k buffer
		for (UtilityList temp : listOfUtilityLists) {

			// if(temp.sumIutils>=this.min_top_k_utility_current_window)

			int[] itemset = new int[1];
			itemset[0] = temp.item;
			Itemset i = new Itemset(itemset, temp.sumIutils);
			if (temp.batches.containsKey(this.win_number))
				i.last_batch_utility = temp.sumIutils
						- temp.batches.get(Collections.min(temp.batches
								.keySet())).sum_batch_iutils;
			else
				i.last_batch_utility = temp.sumIutils;
			top_k_hui.add(i);
			// sort tophui list in descending utility order
			System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
			Collections.sort(top_k_hui, new Comparator<Itemset>() {
				public int compare(Itemset o1, Itemset o2) {
					return (int) (o2.getExactUtility() - o1.getExactUtility());
				}
			});
			if (top_k_hui.size() > k) {
				int lastindex = top_k_hui.size() - 1;

				Itemset set = top_k_hui.get(lastindex);
				top_k_hui.remove(lastindex);
				if (set.getExactUtility() > min_top_k_utility_current_window) {
					min_top_k_utility_current_window = set.getExactUtility();
				}
			}

		}

		if (top_k_hui.size() >= k)
			min_top_k_utility_current_window = top_k_hui.get(
					top_k_hui.size() - 1).getExactUtility();



		if (debug) {
			System.out.println("Single item threshold: "
					+ min_top_k_utility_current_window + " size: "
					+ top_k_hui.size());
		}
		long temp = System.currentTimeMillis();
		try {
			fhm(new int[0], null, listOfUtilityLists);
		} catch (IOException e) {
			e.printStackTrace();
		}
		fhm_time = System.currentTimeMillis() - temp;
//		float curr_top_k_util = min_top_k_utility_current_window;
		if (debug) {
			System.out.println("Top k utility: "
					+ top_k_hui.get(top_k_hui.size() - 1).getExactUtility());
		}

		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		Collections.sort(top_k_hui, new Comparator<Itemset>() {
			public int compare(Itemset o1, Itemset o2) {
				return (int) ((o2.last_batch_utility - o1.last_batch_utility));
			}
		});

		if (top_k_hui.size() >= k)
			min_top_k_utility_current_window = top_k_hui
					.get(top_k_hui.size() - 1).last_batch_utility;
		else
			min_top_k_utility_current_window = 0;
		// this.min_top_k_utility_current_window=p.peek();

		// PQ.clear();
		System.out.println("Threshold for next window: "
				+ min_top_k_utility_current_window + " size: "
				+ top_k_hui.size());
		System.out.println("Total: " + total);
		mapItemToTWU.clear();
		mapItemToUtilityList.clear();
		listOfUtilityLists.clear();
		mapFMAP.clear();

		endTimestamp2 = System.currentTimeMillis();

	}

	/**
	 * Lexicographic ordering (asscending order)
	 */
	private int compareItems(int item1, int item2) {
		// int compare = (int)(mapItemToTWU.get(item1) -
		// mapItemToTWU.get(item2));
		// return (compare == 0)? item1 - item2 : compare;
		if (mapItemToTWU.get(item1) > mapItemToTWU.get(item2))
			return 1;
		else if (mapItemToTWU.get(item1) < mapItemToTWU.get(item2))
			return -1;
		else {
			if (item1 > item2)
				return 1;
			else
				return -1;
		}
		// return item1-item2;
	}

	/**
	 * This is the recursive method to find all high utility itemsets. It writes
	 * the itemsets to the output file.
	 * 
	 * @param prefix
	 *            This is the current prefix. Initially, it is empty.
	 * @param pUL
	 *            This is the Utility List of the prefix. Initially, it is
	 *            empty.
	 * @param ULs
	 *            The utility lists corresponding to each extension of the
	 *            prefix.
	 * @param minUtility
	 *            The minUtility threshold.
	 * @throws IOException
	 */
	private void fhm(int[] prefix, UtilityList pUL, List<UtilityList> ULs)
			throws IOException {

		// For each extension X of prefix P
		for (int i = 0; i < ULs.size(); i++) {
			UtilityList X = ULs.get(i);

			// If pX is a high utility itemset.
			// we save the itemset: pX
			if (X.sumIutils >= min_top_k_utility_current_window
					&& prefix.length > 0 && X.sumIutils != 0f) {
				huiCount++;
				hui++;
				int[] itemset = new int[prefix.length + 1];
				System.arraycopy(prefix, 0, itemset, 0, prefix.length);
				itemset[prefix.length] = X.item;

				Itemset item_set = new Itemset(itemset, X.sumIutils);
				if (X.batches.containsKey(win_number))
					item_set.last_batch_utility = X.sumIutils
							- X.batches
									.get(Collections.min(X.batches.keySet())).sum_batch_iutils;
				else
					item_set.last_batch_utility = X.sumIutils;
				top_k_hui.add(item_set);

				// sort tophui list in descending utility order
				System.setProperty("java.util.Arrays.useLegacyMergeSort",
						"true");
				Collections.sort(top_k_hui, new Comparator<Itemset>() {
					public int compare(Itemset o1, Itemset o2) {
						return (int) (o2.getExactUtility() - o1
								.getExactUtility());
					}
				});
				if (top_k_hui.size() > k) {
					int lastindex = top_k_hui.size() - 1;

					Itemset set = top_k_hui.get(lastindex);
					top_k_hui.remove(lastindex);

					// check if utility of removed itemset is greater than
					// minimum
					// utility, if greater than increase the minimum utility of
					// itemset with the utility of removed itemset
					if (set.getExactUtility() > min_top_k_utility_current_window) {
						min_top_k_utility_current_window = set
								.getExactUtility();
					}
				}
				if (top_k_hui.size() >= k)
					min_top_k_utility_current_window = top_k_hui.get(
							top_k_hui.size() - 1).getExactUtility();
			}

			// If the sum of the remaining utilities for pX
			// is higher than minUtility, we explore extensions of pX.
			// (this is the pruning condition)
			if (X.sumIutils + X.sumRutils >= min_top_k_utility_current_window
					&& X.sumIutils + X.sumRutils != 0f) {

				// This list will contain the utility lists of pX extensions.
				List<UtilityList> exULs = new ArrayList<UtilityList>();
				// For each extension of p appearing
				// after X according to the ascending order
				for (int j = i + 1; j < ULs.size(); j++) {
					UtilityList Y = ULs.get(j);

					// ======================== NEW OPTIMIZATION USED IN FHM
					/*
					 * Map<Integer, Float> mapTWUF = mapFMAP.get(X.item);
					 * if(mapTWUF != null) { Float twuF = mapTWUF.get(Y.item);
					 * if(twuF != null && twuF <
					 * min_top_k_utility_current_window) { continue; } }
					 */
					candidateCount++;
					total++;
					/*
					 * if(candidateCount%5000000==0) {
					 * System.out.println("candidateCount : "+candidateCount); }
					 */
					// =========================== END OF NEW OPTIMIZATION

					// we construct the extension pXY
					// and add it to the list of extensions of pX
					UtilityList temp = construct(pUL, X, Y);

					exULs.add(temp);
				}
				// We create new prefix pX
				int[] newPrefix = new int[prefix.length + 1];
				System.arraycopy(prefix, 0, newPrefix, 0, prefix.length);
				newPrefix[prefix.length] = X.item;

				// We make a recursive call to discover all itemsets with the
				// prefix pXY
				fhm(newPrefix, X, exULs);
			}
		}
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
	private UtilityList construct(UtilityList P, UtilityList px, UtilityList py) {
		construct_calls++;
		long temp = System.currentTimeMillis();
		// create an empty utility list for pXY
		UtilityList pxyUL = new UtilityList(py.item, win_size, win_number);

		// Find common batches in Utility Lists of Px and Py

		Set<Integer> common_batches = new HashSet<Integer>();
		common_batches.addAll(px.batches.keySet());
		common_batches.retainAll(py.batches.keySet());

		for (int batch : common_batches) {
			for (Element ex : px.batches.get(batch).elements) {
				Element ey = findElementWithTID(py.batches.get(batch).elements,
						ex.tid);
				if (ey == null) {
					continue;
				}
				// if the prefix p is null
				if (P == null) {
					// Create the new element
					Element eXY = new Element(ex.tid, ex.iutils + ey.iutils,
							ey.rutils);
					// add the new element to the utility list of pXY
					pxyUL.addElement(eXY, win_size,
							number_of_transactions_batch);

				} else {
					// find the element in the utility list of p wih the same
					// tid
					Element e = findElementWithTID(
							P.batches.get(batch).elements, ex.tid);
					if (e != null) {
						// Create new element
						Element eXY = new Element(ex.tid, ex.iutils + ey.iutils
								- e.iutils, ey.rutils);
						// add the new element to the utility list of pXY
						pxyUL.addElement(eXY, win_size,
								number_of_transactions_batch);
					}
				}
			}

		}
		construct_time = construct_time + (System.currentTimeMillis() - temp);
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
	private Element findElementWithTID(List<Element> elements, int tid) {
		List<Element> list = elements;

		// perform a binary search to check if the subset appears in level k-1.
		int first = 0;
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
				return list.get(middle);
			}
		}
		return null;
	}
	
	/**
	 * Write the result to a file
	 * @param path the output file path
	 * @param appendToFile whether to append or overwrite to the result file
	 * @throws IOException if an exception for reading/writing to file
	 */
	public void writeResultTofile(String path, boolean appendToFile) throws IOException {
		processedBatchCount++;
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(path, appendToFile));
		
		if(appendToFile){
			writer.newLine();
			writer.write("@NEXT_BATCH");
			writer.newLine();
		}

		Iterator<Itemset> iter = top_k_hui.iterator();
		while (iter.hasNext()) {
			StringBuffer buffer = new StringBuffer();
			Itemset itemset = (Itemset) iter.next();
			
			// append the prefix
			for (int i = 0; i < itemset.itemset.length; i++) {
				buffer.append(itemset.itemset[i]);
				buffer.append(' ');
			}
			
			// append the utility value
			buffer.append(" #UTIL: ");
			buffer.append(itemset.getExactUtility());
			
			// write to file
			writer.write(buffer.toString());
			if(iter.hasNext()){
				writer.newLine();
			}
		}
		writer.close();
	}

	/**
	 * Print statistics about the latest execution to System.out.
	 * 
	 * @throws IOException
	 */
	public void printStats() throws IOException {

		System.out
				.println("=============  FHMDS-Naive ALGORITHM v.2.34 Stats =============");
		if (debug) {
			System.out.println("k " + k + " Transaction count per batch:"
					+ number_of_transactions_batch + " win size: " + win_size);
		}
		System.out.println(" Processed batch count: " + processedBatchCount);
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp)
				+ " ms");
		System.out.println(" Candidate count: " + total);
		System.out
				.println("======================================================");
	}

}