package ca.pfv.spmf.algorithms.frequentpatterns.ehaupm;

/** * * * This is an implementation of the EHAUPM algorithm.
*
* Copyright (c) 2018 Shi-Feng Ren
*
* This file is part of the SPMF DATA MINING SOFTWARE  (http://www.philippe-fournier-viger.com/spmf).
*
*
* SPMF is free software: you can redistribute it and/or modify it under the * terms of the GNU General Public License as published by the Free Software * Foundation, either version 3 of the License, or (at your option) any later * version. *

* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR * A PARTICULAR PURPOSE. See the GNU General Public License for more details. *
*
* You should have received a copy of the GNU General Public License along with * SPMF. If not, see .
*
* @author Shi-Feng Ren
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
import java.util.Map.Entry;


/**
 * This is an implementation of the "EHAUPM" algorithm for High-Average-Utility Itemsets Mining
 * as described in the journal paper : <br/><br/>
 *
 * Lin C W, Ren S, Fournier-Viger P, et al. EHAUPM: Efficient High Average-Utility Pattern Mining with Tighter Upper-Bounds[J]. IEEE Access, 2017, PP(99):1-1.
 *
 * @see MAUList
 * @see MAUEntry
 * @author Shi-Feng Ren
 */
public class AlgoEHAUPM {

	/** The time at which the algorithm started */
	public long startTimestamp = 0;

	/**  The time at which the algorithm ended */
	public long endTimestamp = 0;

	/** The number of high average utility itemset */
	public int nhauis =0;

	/** The number of candidate high-utility itemsets */
	public long joinCount =0;

	/** Map to remember the AUUB of each item */
	Map<Integer, Long> items2auub;

	/** The eaucs structure */
	Map<Integer, Map<Integer, Integer>> EAUCM;

	/** Buffer for itemsets, initial buffer size is 200. */
	int BUFFERS_SIZE = 200;
	private int[] itemsetBuffer = null;

	/** Write HAUIs in a specified file */
    BufferedWriter writer = null;

	// Memory recoder: an thread is used to dynamically record peak-memory
    // And the peak-memory is used as the final memory usage.
	MemoryUpdateRunnable memRecorder = new MemoryUpdateRunnable();

	// This class represent an item and its utility in a transaction
	class Pair{
		int item = 0;
		int utility = 0;
	}


	/**
	 * Default constructor
	 */
	public AlgoEHAUPM() {

	}


	/**
	 * Run the algorithm
     * @param dbPath local address of the file records the quantities of each item.
     * @param HAUIsFile the file which is used to store the discovered HAUIs.
     * @param delta minimum high average utility threshold(a positive integer value).
	 * @throws Exception
	 */
	public void runAlgorithm(String dbPath, String HAUIsFile, int delta) throws IOException {
		
		startTimestamp = System.currentTimeMillis();

	    // Reset
		Thread timeThread = new Thread(memRecorder);
		memRecorder.isTestMem = true;
		timeThread.start();

		// Initialize the buffer for storing the current itemset
		itemsetBuffer = new int[BUFFERS_SIZE];

		// Initialize the structure of EAUCM, which is used to store the auub value of all 2-itemsets whose auub >= delta*total_utility
		EAUCM =  new HashMap<Integer, Map<Integer, Integer>>();

		// Initialize writer
		if(HAUIsFile!=null && !HAUIsFile.equalsIgnoreCase("null"))
        	writer = new BufferedWriter(new FileWriter(HAUIsFile));


		// Create a map to store the auub value of each item
		items2auub = new HashMap<Integer, Long>();


		// At first, scan the database to calculate the auub value of each item.
		BufferedReader dbReader = null;
		String curTran; // current transaction
        // TU: total utility of database; minUtility: minimum high average utility
		long minUtility=0;
		try {
			dbReader = new BufferedReader(new InputStreamReader( new FileInputStream(new File(dbPath))));
			// for each transaction until the end of file
			while ((curTran = dbReader.readLine()) != null) {

                // if the line is  a comment, is  empty or is a
                // kind of metadata
                if (curTran.isEmpty() == true ||
						curTran.charAt(0) == '#' || curTran.charAt(0) == '%'
                        || curTran.charAt(0) == '@') {
                    continue;
                }

                // split the transaction according to the : separator
                String split[] = curTran.split(":");
                // the first part is the list of items
                String items[] = split[0].split(" ");
                String utilityValues[] = split[2].split(" ");
                // find the transaction max utility
                Integer transactionMUtility = Integer.MIN_VALUE;

                for(int i = 0; i < utilityValues.length; i++){
//                    TU += Integer.parseInt(utilityValues[i]);
                    if(transactionMUtility <  Integer.parseInt(utilityValues[i])){
                        transactionMUtility = Integer.parseInt(utilityValues[i]);
                    }
                }

                // for each item, we add the transaction utility to its AUUB
                for(int i=0; i <items.length; i++){
                    // convert item to integer
                    Integer item = Integer.parseInt(items[i]);
                    // get the current AUUB of that item
                    Long auub = items2auub.get(item);
                    // add the utility of the item in the current transaction to its AUUB
                    auub = (auub == null)?
                            transactionMUtility : auub + transactionMUtility;
                    items2auub.put(item, auub);
                }
			}
//			minUtility =(long)(TU*delta);
			minUtility = delta;
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(dbReader != null){
				dbReader.close();
			}
	    }

		// Container of MAUList of each item whose auub value >= minUtility.
		List<MAUList> listOfMAULists = new ArrayList<MAUList>();
		// Key: item, Value:MAUList
		Map<Integer, MAUList> mapItemToUtilityList = new HashMap<Integer, MAUList>();


		for(Entry<Integer,Long> entry: items2auub.entrySet()) {
			Integer item = entry.getKey();
			if(items2auub.get(item) >= minUtility) {
				MAUList uList = new MAUList(item);
				mapItemToUtilityList.put(item, uList);
				listOfMAULists.add(uList);
			}
		}
		// Sort MAUList according to its auub-ascending order
		Collections.sort(listOfMAULists, new Comparator<MAUList>(){
			public int compare(MAUList o1, MAUList o2) {
				return compareItems(o1.item, o2.item);
			}
		} );

		// Scan DB again to construct MAUList of each item whose auub value >= minUtility.
		try {
			dbReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(dbPath))));

			int tid =0;
			while ((curTran = dbReader.readLine()) != null) {
                // if the line is  a comment, is  empty or is a
                // kind of metadata
                if (curTran.isEmpty() == true ||
						curTran.charAt(0) == '#' || curTran.charAt(0) == '%'
                        || curTran.charAt(0) == '@') {
                    continue;
                }

                // split the line according to the separator
                String split[] = curTran.split(":");
                // get the list of items
                String items[] = split[0].split(" ");
                // get the list of utility values corresponding to each item for that transaction
                String utilityValues[] = split[2].split(" ");

                // Copy the transaction into lists but
                // without items with aiuub < minutility

                // Create a list to store items
                List<Pair> revisedTransaction = new ArrayList<Pair>();


				int maxUtilityOfCurTrans =0;
                // for each item
                for(int i=0; i <items.length; i++){
                    /// convert values to integers
                    Pair pair = new Pair();
                    pair.item = Integer.parseInt(items[i]);
                    pair.utility = Integer.parseInt(utilityValues[i]);
                    // if the item has enough utility
                    if(items2auub.get(pair.item) >= minUtility){
                        // add it
                        revisedTransaction.add(pair);
                        if(maxUtilityOfCurTrans < pair.utility){
                            maxUtilityOfCurTrans = pair.utility;
                        }
                    }
                }

				// sort the transaction according to auub-ascending order
				Collections.sort(revisedTransaction, new Comparator<Pair>(){
					public int compare(Pair o1, Pair o2) {
					    return compareItems(o1.item, o2.item);
					}
				});

                // Get remu value and rmu value without extra computation
				int remu=0, rmu=0;
				for(int i = revisedTransaction.size()-1; i>=0; --i){

					Pair pair =  revisedTransaction.get(i);

					rmu = rmu < pair.utility ? pair.utility : rmu;

					// get the utility list of this item
					MAUList MAUListOfItem = mapItemToUtilityList.get(pair.item);

					// Add a new MAUEntry to the MAUList of this item corresponding to this transaction
					MAUEntry MAUEntry = new MAUEntry(tid, pair.utility, rmu, remu);
					MAUListOfItem.addElement(MAUEntry);

					remu = (remu<pair.utility) ? pair.utility : remu;
				}

				// Construct EAUCM structure for store auub value of 2-itemset
				for(int i = 0; i< revisedTransaction.size(); i++){
					Pair pair =  revisedTransaction.get(i);
					Map<Integer, Integer> subEAUCS = EAUCM.get(pair.item);
					if(subEAUCS == null) {
						subEAUCS = new HashMap<Integer, Integer>();
						EAUCM.put(pair.item, subEAUCS);
					}
					for(int j = i+1; j< revisedTransaction.size(); j++){
						Pair pairAfter = revisedTransaction.get(j);
						Integer auubSum = subEAUCS.get(pairAfter.item);
						if(auubSum == null) {
							subEAUCS.put(pairAfter.item, maxUtilityOfCurTrans);
						}else {
							subEAUCS.put(pairAfter.item, auubSum + maxUtilityOfCurTrans);
						}
					}

				}
				++tid;

			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(dbReader != null){
				dbReader.close();
			}
	    }

		// Enumerate the enumeration-tree
		search(itemsetBuffer, 0, null, listOfMAULists, minUtility);

		memRecorder.isTestMem =false;
		if(writer!=null)
            writer.close();
		
		endTimestamp = System.currentTimeMillis();

	}

    /**
     * Compare the auub value of two items, if equals, compare their Lexicographic order
     * @param item1
     * @param item2
     * @return return 0 if equals, -1 if item1>items or auub(item1)>auub(item2), 1 if item1<items or auub(item1)<auub(item2)
     */
	private int compareItems(int item1, int item2) {
		int compare = (int)( items2auub.get(item1) - items2auub.get(item2));
		// if the same, use the lexical order otherwise use the auub value
		return (compare == 0)? item1 - item2 :  compare;
	}

	/**
	 * @param prefix  This is the current prefix. Initially, it is empty.
	 * @param ULOfPxy This is the Utility List of the prefix. Initially, it is empty.
	 * @param ULs The utility lists corresponding to each extension of the prefix.
	 * @param minUtility The minUtility threshold.
	 * @param prefixLength The current prefix length
	 * @throws IOException
	 */
	private void search(int [] prefix,
						int prefixLength, MAUList ULOfPxy, List<MAUList> ULs, long minUtility)
			throws IOException {


		for(int i=0; i< ULs.size(); i++) {
			MAUList X = ULs.get(i);

			if( (X.sumutils / (double)(prefixLength+1)) >= minUtility) {
                nhauis++;
                if(writer!=null)
				    writeOut(prefix, prefixLength, X.item, X.sumutils / (double)(prefixLength+1));
			}

			// Proposed loose upper bound
			if((X.sumutils / (double)(prefixLength+1) + X.sumOfRemu) < minUtility){
				continue;
			}

			// Proposed revised uppper bound
			if(X.sumOfRmu >= minUtility){
				// This list will contain the MAUList of Px 1-extensions.
				List<MAUList> extensionOfPx = new ArrayList<>();
				// For each extension of P appearing
				// after x according to the ascending order
				for(int j=i+1; j < ULs.size(); j++){

				    MAUList Y = ULs.get(j);
					Map<Integer, Integer> auub1 = EAUCM.get(X.item);

					if(auub1 != null) {
						Integer auub2 = auub1.get(Y.item);
						if(auub2 == null || auub2 < minUtility) {
							continue;
						}
					}
					joinCount++;

					// we construct MAUList of Pxy and add it to the list of extensions of pX
					// For some datasets, `construct_opt` and `construct` have different running status.
					// Not all of them are always better than the other
					MAUList pxy = construct_opt(prefixLength+1, ULOfPxy, X, Y, minUtility);

					if(pxy != null) {
						extensionOfPx.add(pxy);
					}
				}
				// Create new prefix Px
                if(prefixLength==BUFFERS_SIZE) {
				    BUFFERS_SIZE = BUFFERS_SIZE + (int)(BUFFERS_SIZE/2);
                    int[] tmp = new int[BUFFERS_SIZE];
                    System.arraycopy(tmp,0, itemsetBuffer, 0, prefixLength);
                    itemsetBuffer = tmp;
                }
				itemsetBuffer[prefixLength] = X.item;
				// Recursive call to discover all itemsets with the prefix Px
				search(itemsetBuffer, prefixLength+1, X, extensionOfPx, minUtility);
			}
		}
	}
//
//	/**
//	 * @param P :  the utility list of prefix P.
//	 * @param px : the utility list of pX
//	 * @param py : the utility list of pY
//	 * @return the CAU list of pXY
//	 */
//	private MAUList construct(int prefixLen, MAUList P, MAUList px, MAUList py, long minUtility) {
//		// create an empy utility list for Pxy
//		MAUList pxyUL = new MAUList(py.item);
//
//		long sumOfRmu = px.sumOfRmu;
//		long sumOfRemu = (long)(px.sumutils / (double)prefixLen + px.sumOfRemu);
//
//		// For each element in the utility list of pX
//		for(MAUEntry ex : px.CAUEntries) {
//			// Do a binary search to find element ey in py with tid = ex.tid
//			MAUEntry ey = findElementWithTID(py, ex.tid);
//			if(ey == null) {
//                sumOfRmu -= ex.rmu;
//                sumOfRemu -= (ex.utility /(double)prefixLen + ex.remu);
//                if(Math.min(sumOfRemu, sumOfRmu) < minUtility) {
//                    return null;
//                }
//				continue;
//			}
//
//			// If the prefix p is null
//			if(P == null){
//				// Create the new element
//				MAUEntry eXY = new MAUEntry(ex.tid, ex.utility + ey.utility, ex.rmu, ey.remu);
//
//				// add the new element to the utility list of pXY
//				pxyUL.addElement(eXY);
//
//			} else {
//				// find the element in the utility list of p wih the same tid
//				MAUEntry e = findElementWithTID(P, ex.tid);
//				if(e != null){
//					// Create new element
//					MAUEntry eXY = new MAUEntry(ex.tid, ex.utility + ey.utility - e.utility,
//							ex.rmu, ey.remu);
//					// add the new element to the utility list of pXY
//					pxyUL.addElement(eXY);
//				}
//			}
//		}
//		// return the utility list of pXY.
//		return pxyUL;
//	}

	/**
	 * Construct procedure
	 * @param prefixLen the length of the prefix
	 * @param P the MAUList of prefix P
	 * @param Px the MAUList of Px
	 * @param Py the MAUList of Py
	 * @param minUtility the minimum utility threshold
	 * @return the MAUList of Pxy
	 */
	private MAUList construct_opt(int prefixLen, MAUList P, MAUList Px, MAUList Py, long minUtility) {
		// create an empy utility list for pXY
		MAUList pxyUL = new MAUList(Py.item);
		long sumOfRmu = Px.sumOfRmu;
		long sumOfRemu =(long)(Px.sumutils /(double)prefixLen + Px.sumOfRemu) ;
		int idxPx=0, idxPy=0;

		while(idxPx < Px.CAUEntries.size() && idxPy < Py.CAUEntries.size()) {
            MAUEntry ex = Px.CAUEntries.get(idxPx);
            MAUEntry ey = Py.CAUEntries.get(idxPy);

            if(ex.tid==ey.tid) {
                if(P!=null) {
                    MAUEntry e = findElementWithTID(P, ex.tid);
                    if(e!=null) {
                        // Create the new element
                        MAUEntry eXY = new MAUEntry(ex.tid, ex.utility + ey.utility-e.utility, ex.rmu, ey.remu);
                        // add the new element to the utility list of pXY
                        pxyUL.addElement(eXY);
                    }
                } else {
                    // Create the new element
                    MAUEntry eXY = new MAUEntry(ex.tid, ex.utility + ey.utility, ex.rmu, ey.remu);
                    // add the new element to the utility list of pXY
                    pxyUL.addElement(eXY);
                }

                ++idxPx; ++idxPy;
            } else if (ex.tid > ey.tid) {
                ++idxPy;
            }
            else { // ex.tid < ey.tid : not find entry whose tid == ex.tid in CAU-List of py
                ++idxPx;
                sumOfRmu -= ex.rmu;
                sumOfRemu -= (ex.utility/(double)prefixLen + ex.remu);
                if(Math.min(sumOfRmu,sumOfRemu) < minUtility) { // select the bigger between them
					return null;
                }
            }
        }

		return pxyUL;
	}

	/**
	 * Do a binary search to find the element with a given tid in a utility list
	 * @param ulist the utility list
	 * @param tid  the tid
	 * @return  the element or null if none has the tid.
	 */
	private MAUEntry findElementWithTID(MAUList ulist, int tid){
		List<MAUEntry> list = ulist.CAUEntries;

		// perform a binary search to check if  the subset appears in  level k-1.
        int first = 0;
        int last = list.size() - 1;

        // the binary search
        while( first <= last )
        {
        	int middle = ( first + last ) >>> 1; // divide by 2

            if(list.get(middle).tid < tid){
            	first = middle + 1;  //  the itemset compared is larger than the subset according to the lexical order
            }
            else if(list.get(middle).tid > tid){
            	last = middle - 1; //  the itemset compared is smaller than the subset  is smaller according to the lexical order
            }
            else{
            	return list.get(middle);
            }
        }
		return null;
	}

	/**
	 * @param prefix the prefix to be writent o the output file
	 * @param item an item to be appended to the prefix
	 * @param utility the utility of the prefix concatenated with the item
	 * @param prefixLength the prefix length
	 */
	private void writeOut(int[] prefix, int prefixLength, int item, double utility) throws IOException {

		//Create a string buffer
		StringBuilder buffer = new StringBuilder();
		// append the prefix
		for (int i = 0; i < prefixLength; i++) {
			buffer.append(prefix[i]);
			buffer.append(' ');
		}
		// append the last item
		buffer.append(item);
		// append the average-utility value
		buffer.append(" #AUTIL: ");
		buffer.append(utility);
		// write to file
		writer.write(buffer.toString());

	}


	/**
	 * class for collecting the memory usage status
	 */
    class MemoryUpdateRunnable implements Runnable {
    	boolean isTestMem;
    	double maxConsumationMemory;
		@Override
		public void run() {
			while (this.isTestMem) {
				double currentMemory = (Runtime.getRuntime().totalMemory() - Runtime
						.getRuntime().freeMemory()) / 1024d / 1024d;

				if(currentMemory > maxConsumationMemory) {
					maxConsumationMemory = currentMemory;
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException ex) {
				}
			}
		}
	}

	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  EHAUPM ALGORITHM v.2.22 - STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Memory ~ " + memRecorder.maxConsumationMemory + " MB");
		System.out.println(" High-utility itemsets count : " + nhauis);
		System.out.println("===================================================");
	}
}