package ca.pfv.spmf.algorithms.frequentpatterns.hminer;

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
import java.util.List;
import java.util.Map;

import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This is an implementation of the "FHM_DS" algorithm for High-Utility Itemsets
 * Mining as described in the conference paper : <br/>
 * <br/>
 * 
 * Siddharth Dawar, Veronica Sharma, Vikram Goyal:
Mining top-k high-utility itemsets from a data stream under sliding window model. Appl. Intell. 47(4): 1240-1255 (2017)
 *
 * @see CUL_List
 * @see Element_CUL_List
 * @author Siddharth Dawar et al.
 */
public class AlgoHMiner {

	// variable for statistics
	/** the maximum memory usage */
	public double maxMemory = 0;
	
	/** the time the algorithm started */
	public long startTimestamp = 0; // 
	
	/** the time the algorithm ended */
	public long endTimestamp = 0;
	
	/** the time for constructing */
	public long construct_time = 0;
	
	/** the number of HUI generated */
	public long huiCount = 0; 
	
	/* Variables used for statistics */
	public long candidateCount = 0, construct_calls = 0, numberRecursions = 0;
	public long closure_time = 0, temp_closure_time = 0, p_laprune = 0,
			p_cprune = 0;
	public long recursive_calls = 0, merging_time = 0, temp_merging_time = 0;
	
	/** Map to remember the TWU of each item */
	Map<Integer, Long> mapItemToTWU;

	/** writer to write the output file */
	BufferedWriter writer = null;
	
	/** Output file path */
	String outputFile;
	
	/** EUCS map of the FHM algorithm (item -->  item --> twu */
	Map<Integer, Map<Integer, Long>> mapFMAP; 
	
	/** indicate to activate the merging optimization */
	public static boolean merging_flag;
	
	/** indicate to activate the EUCS optimization of FHM */
	public static boolean  eucs_flag;
	
	/** activate the debug mode */
	boolean debug = false;
	
	/** start time of the algorithm */
	long stats_time = 0;

	/** this class represent an item and its utility in a transaction */
	class Pair {
		int item = 0;
		long utility = 0;

		public String toString() {
			return "[" + item + "," + utility + "]";
		}
	}

	/**
	 * Default constructor
	 */
	public AlgoHMiner() {

	}

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
	public void runAlgorithm(String transactionFile, String outputFile, long minUtility,
			boolean merging, boolean EUCS) throws IOException {
		// reset maximum
		// reset maximum
		MemoryLogger.getInstance().reset();

		merging_flag = merging;
		eucs_flag = EUCS;
		mapFMAP = new HashMap<Integer, Map<Integer, Long>>();

		startTimestamp = System.currentTimeMillis();

		// We create a map to store the TWU of each item
		mapItemToTWU = new HashMap<Integer, Long>();
		

		// We scan the database a first time to calculate the TWU of each item.
		BufferedReader myInput = null;
		String thisLine;
		try {
			// prepare the object for reading the file
			myInput = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(transactionFile))));
			// readUtilityFile(utilityFile);
			// for each line (transaction) until the end of file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is a comment, is empty or is a
				// kind of metadata
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
					Long twu = mapItemToTWU.get(item);
					// add the utility of the item in the current transaction to its twu
					twu = (twu == null)? 
							transactionUtility : twu + transactionUtility;
					mapItemToTWU.put(item, twu);
				}
				// ======= END PHIL ====
			}
		} catch (Exception e) {
			// catches exception if error while reading the input file
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}

		// CREATE A LIST TO STORE THE UTILITY LIST OF ITEMS WITH TWU >=
		// MIN_UTILITY.
		ArrayList<CUL_List> listOfCULLists = new ArrayList<CUL_List>();

		HashMap<ArrayList<Integer>, Integer> HT = new HashMap<ArrayList<Integer>, Integer>();
		// CREATE A MAP TO STORE THE UTILITY LIST FOR EACH ITEM.
		// Key : item Value : utility list associated to that item
		Map<Integer, CUL_List> mapItemToCULList = new HashMap<Integer, CUL_List>();

		// For each item
		// int counter=0;
		for (Integer item : mapItemToTWU.keySet()) {
			// if the item is promising (TWU >= minutility)

			if (mapItemToTWU.get(item) >= minUtility) {
				// create an empty Utility List that we will fill later.
				CUL_List uList = new CUL_List(item);
				mapItemToCULList.put(item, uList);
				// add the item to the list of high TWU items
				listOfCULLists.add(uList);
				// counter++;

			}

		}
		// ul_count_map.put(1, counter);
		// SORT THE LIST OF HIGH TWU ITEMS IN ASCENDING ORDER
		Collections.sort(listOfCULLists, new Comparator<CUL_List>() {
			public int compare(CUL_List o1, CUL_List o2) {
				// compare the TWU of the items
				return compareItems(o1.item, o2.item);
			}
		});

		long time_EUCS = 0, temp_EUCS = 0;
		// SECOND DATABASE PASS TO CONSTRUCT THE CUL LISTS
		// OF 1-ITEMSETS HAVING TWU >= minutil (promising items)
		try {
			// prepare object for reading the file
			myInput = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(transactionFile))));
			// variable to count the number of transaction
			int tid = 1;
			// for each line (transaction) until the end of file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is a comment, is empty or is a
				// kind of metadata
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
				

				long remainingUtility = 0;

				long newTWU = 0; // NEW OPTIMIZATION
				ArrayList<Integer> tx_key = new ArrayList<Integer>();
				List<Pair> revisedTransaction = new ArrayList<Pair>();
				// for each item
				for(int i=0; i <items.length; i++){
					/// convert values to integers
					Pair pair = new Pair();
					pair.item = Integer.parseInt(items[i]);
					pair.utility = Integer.parseInt(utilityValues[i]);

					if (mapItemToTWU.get(pair.item) >= minUtility) {
						// add it
						revisedTransaction.add(pair);
						tx_key.add(pair.item);
						// remainingUtility += pair.utility;
						newTWU += pair.utility; // NEW OPTIMIZATION
					}
				}

				// sort the transaction
				Collections.sort(revisedTransaction, new Comparator<Pair>() {
					public int compare(Pair o1, Pair o2) {
						return compareItems(o1.item, o2.item);
					}
				});
				if (revisedTransaction.size() > 0) {
					if (merging_flag) {
						if (!HT.containsKey(tx_key)) {
							// System.out.println(revisedTransaction.get(revisedTransaction.size()-1));
							temp_merging_time = System.currentTimeMillis();
							HT.put(tx_key,
									mapItemToCULList.get(revisedTransaction
											.get(revisedTransaction.size() - 1).item).elements
											.size());
							merging_time += System.currentTimeMillis()
									- temp_merging_time;
							// for each item left in the transaction
							for (int i = revisedTransaction.size() - 1; i >= 0; i--) {
								Pair pair = revisedTransaction.get(i);

								// float remain = remainingUtility; // FOR
								// OPTIMIZATION

								// subtract the utility of this item from the
								// remaining utility
								// remainingUtility = remainingUtility -
								// pair.utility;

								// get the utility list of this item
								CUL_List CULListOfItem = mapItemToCULList
										.get(pair.item);

								// Add a new Element to the utility list of this
								// item corresponding to this transaction
								Element_CUL_List element = new Element_CUL_List(
										tid, pair.utility, remainingUtility, 0,
										0);

								if (i > 0)// PPOS
									element.Ppos = mapItemToCULList
											.get(revisedTransaction.get(i - 1).item).elements
											.size();
								else
									element.Ppos = -1;

								CULListOfItem.addElement(element);

								remainingUtility += pair.utility;

							}
						}// if HT contains key
						else // duplicate exists
						{
							temp_merging_time = System.currentTimeMillis();
							int pos = HT.get(tx_key);
							remainingUtility = 0;
							// for each item left in the transaction
							for (int i = revisedTransaction.size() - 1; i >= 0; i--) {

								// get the utility list of this item
								CUL_List CULListOfItem = mapItemToCULList
										.get(revisedTransaction.get(i).item);

								CULListOfItem.elements.get(pos).Nu += revisedTransaction
										.get(i).utility;
								CULListOfItem.elements.get(pos).Nru += remainingUtility;
								CULListOfItem.sumNu += revisedTransaction
										.get(i).utility;
								CULListOfItem.sumNru += remainingUtility;
								remainingUtility += revisedTransaction.get(i).utility;
								pos = CULListOfItem.elements.get(pos).Ppos;
							}
							merging_time += System.currentTimeMillis()
									- temp_merging_time;

						}// end of else
					} else// if merging is disable
					{
						// for each item left in the transaction
						for (int i = revisedTransaction.size() - 1; i >= 0; i--) {
							Pair pair = revisedTransaction.get(i);

							// float remain = remainingUtility; // FOR
							// OPTIMIZATION

							// subtract the utility of this item from the
							// remaining utility
							// remainingUtility = remainingUtility -
							// pair.utility;

							// get the utility list of this item
							CUL_List CULListOfItem = mapItemToCULList
									.get(pair.item);

							// Add a new Element to the utility list of this
							// item corresponding to this transaction
							Element_CUL_List element = new Element_CUL_List(
									tid, pair.utility, remainingUtility, 0, 0);

							if (i > 0)// PPOS
								element.Ppos = mapItemToCULList
										.get(revisedTransaction.get(i - 1).item).elements
										.size();
							else
								element.Ppos = -1;

							CULListOfItem.addElement(element);

							remainingUtility += pair.utility;

						}

					}

				}

				// Build EUCS
				if (eucs_flag) {
					temp_EUCS = System.currentTimeMillis();
					for (int i = revisedTransaction.size() - 1; i >= 0; i--) {
						Pair pair = revisedTransaction.get(i);
						// BEGIN NEW OPTIMIZATION for FHM
						Map<Integer, Long> mapFMAPItem = mapFMAP.get(pair.item);
						if (mapFMAPItem == null) {
							mapFMAPItem = new HashMap<Integer, Long>();
							mapFMAP.put(pair.item, mapFMAPItem);
						}

						for (int j = i + 1; j < revisedTransaction.size(); j++) {
							Pair pairAfter = revisedTransaction.get(j);
							Long twuSum = mapFMAPItem.get(pairAfter.item);
							if (twuSum == null) {
								mapFMAPItem.put(pairAfter.item, newTWU);
							} else {
								mapFMAPItem
										.put(pairAfter.item, twuSum + newTWU);
							}
						}

						// END OPTIMIZATION of FHM

					}
					time_EUCS += System.currentTimeMillis() - temp_EUCS;
				}

				tid++; // increase tid number for next transaction

			}
		} catch (Exception e) {
			// to catch error while reading the input file
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}

		// check the memory usage
		checkMemory();

		long initial_time = System.currentTimeMillis() - startTimestamp;
		if(debug){
			System.out.println("Initial time taken before mining: " + initial_time);
			System.out.println("EUCS time taken before mining: " + time_EUCS);
			System.out.println("Initial merging time: " + merging_time);
		}

		// check the memory usage
		MemoryLogger.getInstance().checkMemory();
		

		writer = new BufferedWriter(new FileWriter(outputFile));

		Explore_search_tree(new int[0], listOfCULLists, minUtility);
		
		writer.close();

		if(debug){
			System.out.println("Closure time: " + closure_time);
			System.out.println("Final merging time: " + merging_time);
			System.out.println("#recursive calls: " + recursive_calls);
			System.out.println("#LA prune successful: " + p_laprune);
			System.out.println("#C prune + LA prune successful: " + p_cprune);
		}
		// writer_stats.close();

		// System.out.println("HMINER Time: "+fhm_time+" Construct time: "+construct_time+" Number of calls: "+construct_calls);
		// record end time
		endTimestamp = System.currentTimeMillis();
		// check the memory usage again and close the file.
		// check the memory usage again and close the file.
		MemoryLogger.getInstance().checkMemory();

		// writer.close();
		// tempwriter.close();
		// close output file
		// writer.close();

	}

	/*
	 * public void readUtilityFile(String utilityFileName) throws
	 * FileNotFoundException, IOException { // read the file BufferedReader
	 * reader = new BufferedReader(new FileReader( utilityFileName)); String
	 * line; // for each line (transaction) until the end of the file while
	 * (((line = reader.readLine()) != null)) { // if the line is a comment, is
	 * empty or is a // kind of metadata if (line.isEmpty() == true ||
	 * line.charAt(0) == '#' || line.charAt(0) == '%' || line.charAt(0) == '@')
	 * { continue; }
	 * 
	 * String[] lineSplited = line.split(" "); String itemString =
	 * lineSplited[0]; String utilityString = lineSplited[1]; Integer item =
	 * Integer.parseInt(itemString); Long utility =
	 * Long.parseLong(utilityString); utilityMap.put(item, utility); } // close
	 * the input file reader.close(); // Read the utility file }
	 */

	/**
	 * Method to compare items by their TWU
	 * 
	 * @param item1
	 *            an item
	 * @param item2
	 *            another item
	 * @return 0 if the same item, >0 if item1 is larger than item2, <0
	 *         otherwise
	 */
	private int compareItems(int item1, int item2) {
		int compare = (int) (mapItemToTWU.get(item1) - mapItemToTWU.get(item2));
		// if the same, use the lexical order otherwise use the TWU
		return (compare == 0) ? item1 - item2 : compare;
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
	private void Explore_search_tree(int[] prefix, ArrayList<CUL_List> ULs,
			long minUtility) throws IOException {
		recursive_calls++;
		// For each extension X of prefix P
		for (int i = 0; i < ULs.size(); i++) {
			CUL_List X = ULs.get(i);

			int[] sorted_prefix = new int[prefix.length + 1];
			System.arraycopy(prefix, 0, sorted_prefix, 0, prefix.length);
			sorted_prefix[prefix.length] = X.item;

			// If pX is a high utility itemset.
			// we save the itemset: pX
			if (X.sumNu + X.sumCu >= minUtility) {

				// Arrays.sort(sorted_prefix);
				/*
				 * for(int k=0;k<sorted_prefix.length;k++) {
				 * //System.out.print(sorted_prefix[k]+" ");
				 * 
				 * System.out.print(sorted_prefix[k]+" "); }
				 */
				// System.out.print(" nu: "+X.sumNu+" cu: "+X.sumCu+"\n");
				
				// save to file
				writeOut(prefix, prefix.length, X.item, X.sumNu + X.sumCu);
				

			}
			candidateCount++;

			// If the sum of the remaining utilities for pX
			// is higher than minUtility, we explore extensions of pX.
			// (this is the pruning condition)
			if (X.sumNu + X.sumCu + X.sumNru + X.sumCru >= minUtility) {

				ArrayList<CUL_List> exULs = ConstructCUL(X, ULs, i, minUtility,
						sorted_prefix.length);
				// candidateCount+=exULs.size();
				Explore_search_tree(sorted_prefix, exULs, minUtility);
			}

		}
		// check the maximum memory usage for statistics purpose
		MemoryLogger.getInstance().checkMemory();

	}

	/***
	 * Construct a CUL
	 * @param X
	 * @param CULs
	 * @param st
	 * @param minutil
	 * @return
	 */
	private ArrayList<CUL_List> ConstructCUL(CUL_List X,
			ArrayList<CUL_List> CULs, int st, long minutil, int length) {
		// Need to initialize exCULs;
		ArrayList<CUL_List> exCULs = new ArrayList<CUL_List>();
		ArrayList<Long> LAU = new ArrayList<Long>();
		ArrayList<Long> CUTIL = new ArrayList<Long>();
		ArrayList<Integer> ey_tid = new ArrayList<Integer>();
		// int flag=0;
		// Initialization
		for (int i = 0; i <= CULs.size() - 1; i++) {
			CUL_List uList = new CUL_List(CULs.get(i).item);
			exCULs.add(uList);
			LAU.add((long) 0);
			CUTIL.add((long) 0);
			ey_tid.add(0);
		}
		int sz = CULs.size() - (st + 1);
		int extSz = sz; // number of extensions for closure
		// System.out.println("CUL size: "+CULs.size()+" st: "+st+" sz: "+sz);
		for (int j = st + 1; j <= CULs.size() - 1; j++) {
			// Check EUCS
			if (eucs_flag) {
				Map<Integer, Long> mapTWUF = mapFMAP.get(X.item);
				if (mapTWUF != null) {
					Long twuF = mapTWUF.get(CULs.get(j).item);
					if (twuF != null && twuF < minutil) {
						exCULs.set(j, null);
						extSz = sz - 1;
						// possible error that no value in exCULs and trying to
						// set in first position.
					} else // EUCS successful
					{
						CUL_List uList = new CUL_List(CULs.get(j).item);
						exCULs.set(j, uList);
						ey_tid.set(j, 0); // track tid position in CUL
						LAU.set(j, X.sumCu + X.sumCru + X.sumNu + X.sumNru);
						CUTIL.set(j, X.sumCu + X.sumCru);

					}
				}
			} else // EUCS flag disabled
			{
				CUL_List uList = new CUL_List(CULs.get(j).item);
				exCULs.set(j, uList);
				ey_tid.set(j, 0); // track tid position in CUL
				LAU.set(j, X.sumCu + X.sumCru + X.sumNu + X.sumNru);
				CUTIL.set(j, X.sumCu + X.sumCru);
			}

		}

		HashMap<ArrayList<Integer>, Integer> HT = new HashMap<ArrayList<Integer>, Integer>();
		ArrayList<Integer> newT = null;
		for (Element_CUL_List ex : X.elements) {
			newT = new ArrayList<Integer>();
			// System.out.println("ex.tid processing: "+ex.tid);
			for (int j = st + 1; j <= CULs.size() - 1; j++) {
				if (exCULs.get(j) == null)
					continue;
				// compute CULs[st+j].tidlist naive approach
				List<Element_CUL_List> eylist = CULs.get(j).elements;
				/*
				 * for(Element_CUL_List e:CULs.get(st+j).elements) {
				 * eylist.add(e.tid); }
				 */

				while (ey_tid.get(j) < eylist.size()
						&& eylist.get(ey_tid.get(j)).tid < ex.tid) {
					ey_tid.set(j, ey_tid.get(j) + 1); // increment tid
				}

				if (ey_tid.get(j) < eylist.size()
						&& eylist.get(ey_tid.get(j)).tid == ex.tid)
					newT.add(j); // adding extension position where tid found
				else // apply LA prune
				{
					LAU.set(j, LAU.get(j) - ex.Nu - ex.Nru); // LA prune
					if (LAU.get(j) < minutil) {
						exCULs.set(j, null);
						extSz = extSz - 1; // might cause an error
						p_laprune++;
					}

				}
			} // end for line 24 of Algo 1C

			if (newT.size() == extSz)// all extensions present in tx
			{

				temp_closure_time = System.currentTimeMillis();
				UpdateClosed(X, CULs, st, exCULs, newT, ex, ey_tid, length); // Algo
																				// 1D
				closure_time += System.currentTimeMillis() - temp_closure_time;

				// flag=1;
			} else
			// if(newT.size()>0)
			{
				if (newT.size() == 0)
					continue;
				long remainingUtility = 0;
				// System.out.println("newT size: "+newT.size());

				if (merging_flag) {
					if (!HT.containsKey(newT)) // new transaction
					{
						// CULs or exCULs?
						temp_merging_time = System.currentTimeMillis();
						HT.put(newT,
								exCULs.get(newT.get(newT.size() - 1)).elements
										.size());
						merging_time += System.currentTimeMillis()
								- temp_merging_time;

						// Insert new entries in exCULs for each newT
						for (int i = newT.size() - 1; i >= 0; i--) {
							CUL_List CULListOfItem = exCULs.get(newT.get(i));
							Element_CUL_List Y = CULs.get(newT.get(i)).elements
									.get(ey_tid.get(newT.get(i)));

							// Add a new Element to the utility list of this
							// item corresponding to this transaction
							Element_CUL_List element = new Element_CUL_List(
									ex.tid, ex.Nu + Y.Nu - ex.Pu,
									remainingUtility, ex.Nu, 0);

							if (i > 0)// PPOS
								element.Ppos = exCULs.get(newT.get(i - 1)).elements
										.size();
							else
								element.Ppos = -1;

							CULListOfItem.addElement(element);
							remainingUtility += Y.Nu - ex.Pu; // changed check
																// once

						}

					} else // duplicate transaction, update utilities Algo 1E
					{
						temp_merging_time = System.currentTimeMillis();
						int dupPos = HT.get(newT);
						UpdateElement(X, CULs, st, exCULs, newT, ex, dupPos,
								ey_tid);
						merging_time += System.currentTimeMillis()
								- temp_merging_time;
					} // end if
				} else // tx merging disabled
				{
					// Insert new entries in exCULs for each newT
					for (int i = newT.size() - 1; i >= 0; i--) {
						CUL_List CULListOfItem = exCULs.get(newT.get(i));
						Element_CUL_List Y = CULs.get(newT.get(i)).elements
								.get(ey_tid.get(newT.get(i)));

						// Add a new Element to the utility list of this item
						// corresponding to this transaction
						Element_CUL_List element = new Element_CUL_List(ex.tid,
								ex.Nu + Y.Nu - ex.Pu, remainingUtility, ex.Nu,
								0);

						if (i > 0)// PPOS
							element.Ppos = exCULs.get(newT.get(i - 1)).elements
									.size();
						else
							element.Ppos = -1;

						CULListOfItem.addElement(element);
						remainingUtility += Y.Nu - ex.Pu; // changed check once

					}

				}

			}// end if
			for (int j = st + 1; j <= CULs.size() - 1; j++)
				CUTIL.set(j, CUTIL.get(j) + ex.Nu + ex.Nru);
		}// end for

		// filter
		ArrayList<CUL_List> filter_CULs = new ArrayList<CUL_List>();
		for (int j = st + 1; j <= CULs.size() - 1; j++) {
			if (CUTIL.get(j) < minutil || exCULs.get(j) == null) {
				p_cprune++;
				continue;

			} else {
				if (length > 1) {
					exCULs.get(j).sumCu += CULs.get(j).sumCu + X.sumCu
							- X.sumCpu;
					exCULs.get(j).sumCru += CULs.get(j).sumCru;
					exCULs.get(j).sumCpu += X.sumCu;
				}
				filter_CULs.add(exCULs.get(j));

			}
		}

		return filter_CULs;
	}

	/***
	 * 
	 * @param X
	 * @param CULs
	 * @param st
	 * @param exCULs
	 * @param newT
	 * @param tid
	 */
	private void UpdateClosed(CUL_List X, ArrayList<CUL_List> CULs, int st,
			ArrayList<CUL_List> exCULs, ArrayList<Integer> newT,
			Element_CUL_List ex, ArrayList<Integer> ey_tid, int length) {
		long nru = 0;
		for (int j = newT.size() - 1; j >= 0; j--) {
			// matched extension at location st+j, j in newT.
			// System.out.println("st: "+st+" newT j: "+newT.get(j));
			CUL_List ey = CULs.get(newT.get(j));
			Element_CUL_List eyy = ey.elements.get(ey_tid.get(newT.get(j)));
			// System.out.println(ex.Nu+ey.elements.get(ey_tid.get(newT.get(j))).Nu
			// -ex.Pu);
			exCULs.get(newT.get(j)).sumCu += ex.Nu + eyy.Nu - ex.Pu;// how to
																	// subtract
																	// NPU
			exCULs.get(newT.get(j)).sumCru += nru;
			exCULs.get(newT.get(j)).sumCpu += ex.Nu; // ex.Pu or ex.Nu?
			nru = nru + eyy.Nu - ex.Pu;
		}

	}

	/**
	 * Update an element
	 * @param X
	 * @param CULs
	 * @param st
	 * @param exCULs
	 * @param newT
	 * @param ex
	 * @param dupPos
	 * @param ey_tid
	 */
	private void UpdateElement(CUL_List X, ArrayList<CUL_List> CULs, int st,
			ArrayList<CUL_List> exCULs, ArrayList<Integer> newT,
			Element_CUL_List ex, int dupPos, ArrayList<Integer> ey_tid) {
		long nru = 0;
		int pos = dupPos;
		for (int j = newT.size() - 1; j >= 0; j--) {
			CUL_List ey = CULs.get(newT.get(j));
			Element_CUL_List eyy = ey.elements.get(ey_tid.get(newT.get(j)));
			// System.out.println(exCULs.get(newT.get(j)).elements.size()+" "+pos);
			exCULs.get(newT.get(j)).elements.get(pos).Nu += ex.Nu + eyy.Nu
					- ex.Pu;
			exCULs.get(newT.get(j)).sumNu += ex.Nu + eyy.Nu - ex.Pu;
			exCULs.get(newT.get(j)).elements.get(pos).Nru += nru;
			exCULs.get(newT.get(j)).sumNru += nru;
			exCULs.get(newT.get(j)).elements.get(pos).Pu += ex.Nu;
			nru = nru + eyy.Nu - ex.Pu;
			// pos should come from exCULs only.
			// pos=ey.elements.get(ey_tid.get(newT.get(j))).Ppos;
			pos = exCULs.get(newT.get(j)).elements.get(pos).Ppos;

		}

	}
	
	/**
	 * Method to write a high utility itemset to the output file.
	 * @param the prefix to be writent o the output file
	 * @param an item to be appended to the prefix
	 * @param utility the utility of the prefix concatenated with the item
	 * @param prefixLength the prefix length
	 */
	private void writeOut(int[] prefix, int prefixLength, int item, long utility) throws IOException {
		huiCount++; // increase the number of high utility itemsets found
		
		//Create a string buffer
		StringBuilder buffer = new StringBuilder();
		// append the prefix
		for (int i = 0; i < prefixLength; i++) {
			buffer.append(prefix[i]);
			buffer.append(' ');
		}
		// append the last item
		buffer.append(item);
		// append the utility value
		buffer.append(" #UTIL: ");
		buffer.append(utility);
		// write to file
		writer.write(buffer.toString());
		writer.newLine();
	}

	/**
	 * Method to check the memory usage and keep the maximum memory usage.
	 */
	private void checkMemory() {
		// get the current memory usage
		double currentMemory = (Runtime.getRuntime().totalMemory() - Runtime
				.getRuntime().freeMemory()) / 1024d / 1024d;
		// if higher than the maximum until now
		if (currentMemory > maxMemory) {
			// replace the maximum with the current memory usage
			maxMemory = currentMemory;
		}
	}

	/**
	 * Print statistics about the latest execution to System.out.
	 * 
	 * @throws IOException
	 */
	public void printStats() throws IOException {

			System.out.println("=============  HMINER ALGORITHM v.2.34 - STATS =============");
			System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
			System.out.println(" Max Memory ~ "
					+ MemoryLogger.getInstance().getMaxMemory() + " MB");
			System.out.println(" High-utility itemsets count : "
					+ huiCount);
			System.out.println("================================================");
	}

}