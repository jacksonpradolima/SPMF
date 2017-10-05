package ca.pfv.spmf.algorithms.frequentpatterns.hui_miner;


/* This file is copyright (c) 2008-2014 Philippe Fournier-Viger
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

/**
 * This is an implementation of the
 * "HUI-LIST-INS" algorithm by Lin et al. (2014) an incremental version
 * of the FHM algorithm.
 * 
 * This algorithm is said to be incremental because it updates the utility-lists
 * and EUCS structure of FHM if some new transactions are added which
 * speed up the first two database scans. However, the search is redone from scratch every 
 * time that some new transactions are added. 
 * 
 * @see UtilityList
 * @see Element
 * @author Philippe Fournier-Viger
 */
public class AlgoHUI_LIST_INS {

	// variable for statistics
	public double maxMemory = 0;     // the maximum memory usage
	public long startTimestamp = 0;  // the time the algorithm started
	public long endTimestamp = 0;   // the time the algorithm terminated
	public int huiCount =0;  // the number of HUI generated
	
	/** Store the total times for all runs of this algorithm */
	public long totalTimeForAllRuns = 0;
	public int totalCandidateCountForAllRuns = 0;
	
	public int candidateCount =0;
	
	// Map to remember the TWU of each item
	Map<Integer, Integer> mapItemToTWU;
	
	//  During first database, the item are sorted by TWU.... Then we keep this ordering
	// in the following map because if the ordering change in an updated database,
	// then the result may be incorrect.
	Map<Integer, Integer> mapItemToRank;
	
	// writer to write the output file 
	BufferedWriter writer = null;  
	
	// NEW OPTIMIZATION - EUCS  (FAST)
	Map<Integer, Map<Integer, Integer>> mapEUCS;  // PAIR OF ITEMS , item --> item, twu
	// END NEW OPTIMIZATION
	
	// variable for debug mode
	boolean debug = false;
	private Map<Integer, UtilityList> mapItemToUtilityList;
	
	List<UtilityList> listOfUtilityLists;
	
	int totalDBUtility = 0;
	
	int minUtility;
	
	// buffer for storing the current itemset that is mined when performing mining
	// the idea is to always reuse the same buffer to reduce memory usage.
	final int BUFFERS_SIZE = 200;
	private int[] itemsetBuffer = null;
	
	
	// This variable stores the current TID for using
	int currentTidForUtilityList = 0;

	// this class represent an item and its utility in a transaction
	class Pair{
		int item = 0;
		int utility = 0;
		
		public String toString() {
			return "[" + item + "," + utility + "]";
		}
	}
	
	/**
	 * Default constructor
	 */
	public AlgoHUI_LIST_INS() {
		
	}

	/**
	 * Run the algorithm
	 * @param input the input file path
	 * @param output the output file path
	 * @param minUtility the minimum utility threshold
	 * @param firstline the first line to be read
	 * @param lastline the last line to be read
	 * @throws IOException exception if error while writing the file
	 */
	public void runAlgorithm(String input, String output, Integer minUtil, int firstLine, int lastLine) throws IOException {
		// Reset statistics
		candidateCount = 0;
		huiCount = 0; 
		
		// initialize the buffer for storing the current itemset
		itemsetBuffer = new int[BUFFERS_SIZE];
		
		// reset maximum
		maxMemory =0;
		
		// initialize output file
		writer = new BufferedWriter(new FileWriter(output));
		
		// if first time
		if(mapEUCS == null) {
			mapEUCS =  new HashMap<Integer, Map<Integer, Integer>>();
			listOfUtilityLists = new ArrayList<UtilityList>();
			mapItemToRank =  new HashMap<Integer, Integer>();
			mapItemToUtilityList = new HashMap<Integer, UtilityList>();
		}
		startTimestamp = System.currentTimeMillis();
		
		// create a list to store the utility list of new items so that they can be
		// sorted by TWU order
		List<UtilityList> newItemsUtilityLists = new ArrayList<UtilityList>();
		
		//  We create a  map to store the TWU of each item
		if(mapItemToTWU == null) {
			mapItemToTWU = new HashMap<Integer, Integer>();
		}

		// We scan the database a first time to calculate the TWU of each item.
		BufferedReader myInput = null;
		String thisLine;
		try {
			// prepare the object for reading the file
			myInput = new BufferedReader(new InputStreamReader( new FileInputStream(new File(input))));
			// for each line (transaction) until the end of file
			int tid =0;
			while ((thisLine = myInput.readLine()) != null && tid < lastLine) {
				
				if(tid >= firstLine){
					// if the line is  a comment, is  empty or is a
					// kind of metadata
					if (thisLine.isEmpty() == true ||
							thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%' || thisLine.charAt(0) == '@') {
						continue;
					}
					
					// split the transaction according to the : separator
					String split[] = thisLine.split(":"); 
					// the first part is the list of items
					String items[] = split[0].split(" "); 
					// the second part is the transaction utility
					int transactionUtility = Integer.parseInt(split[1]);  
					// for each item, we add the transaction utility to its TWU
					for(int i=0; i <items.length; i++){
						// convert item to integer
						Integer item = Integer.parseInt(items[i]);
						// get the current TWU of that item
						Integer twu = mapItemToTWU.get(item);
						// add the utility of the item in the current transaction to its twu
						if(twu == null) {
							UtilityList uList =  new UtilityList(item);
							mapItemToUtilityList.put(item, uList);
							newItemsUtilityLists.add(uList);
							twu = transactionUtility;
						}else {
							twu = twu + transactionUtility;
						}
						mapItemToTWU.put(item, twu);
					}
					totalDBUtility += transactionUtility;
				}
				tid++;
			}
		} catch (Exception e) {
			// catches exception if error while reading the input file
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
	    }

		// save the minimum utility threshold
		minUtility =  minUtil;
		
		// Sort the new items by TWU
		Collections.sort(newItemsUtilityLists, new Comparator<UtilityList>(){
			public int compare(UtilityList o1, UtilityList o2) {
				// compare the TWU of the items
				return compareItems(o1.item, o2.item);
			}
			});
		
		// Give a rank to each item w.r.t to previous items
		for(UtilityList list : newItemsUtilityLists){
			mapItemToRank.put(list.item, mapItemToRank.size()+1);
		}
		
		// Add the utility lists of new items to the list of utility lists of all items 
		listOfUtilityLists.addAll(newItemsUtilityLists);
		
		// SECOND DATABASE PASS TO CONSTRUCT THE UTILITY LISTS 
		// OF 1-ITEMSETS  HAVING TWU  >= minutil (promising items)
		try {
			// prepare object for reading the file
			myInput = new BufferedReader(new InputStreamReader(new FileInputStream(new File(input))));
			// variable to count the number of transaction
			int tid =0;
			// for each line (transaction) until the end of file
			while ((thisLine = myInput.readLine()) != null && tid < lastLine) {
				// if the line is  a comment, is  empty or is a
				// kind of metadata
				if (thisLine.isEmpty() == true ||
						thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
								|| thisLine.charAt(0) == '@') {
					continue;
				}
				
				if(tid >= firstLine){
					// increase the tid number
					currentTidForUtilityList++;
					
					// split the line according to the separator
					String split[] = thisLine.split(":");
					// get the list of items
					String items[] = split[0].split(" ");
					// the second part is the transaction utility
//					int transactionUtility = Integer.parseInt(split[1]);  
					// get the list of utility values corresponding to each item
					// for that transaction
					String utilityValues[] = split[2].split(" ");
					
					// Copy the transaction into lists but 
					// without items with TWU < minutility
					
					int remainingUtility =0;

					int newTWU = 0;  // NEW OPTIMIZATION 
					
					// Create a list to store items
					List<Pair> revisedTransaction = new ArrayList<Pair>();
					// for each item
					for(int i=0; i <items.length; i++){
						/// convert values to integers
						Pair pair = new Pair();
						pair.item = Integer.parseInt(items[i]);
						pair.utility = Integer.parseInt(utilityValues[i]);
						// if the item has enough utility
						//if(mapItemToTWU.get(pair.item) >= minUtility){
							// add it
							revisedTransaction.add(pair);
							remainingUtility += pair.utility;
							newTWU += pair.utility; // NEW OPTIMIZATION
						//}
					}
					
					// sort the transaction
					Collections.sort(revisedTransaction, new Comparator<Pair>(){
						public int compare(Pair o1, Pair o2) {
							return compareItemsByRank(o1.item, o2.item);
						}});
	
									
					// for each item left in the transaction
					for(int i = 0; i< revisedTransaction.size(); i++){
						Pair pair =  revisedTransaction.get(i);
						
//						int remain = remainingUtility; // FOR OPTIMIZATION
						
						// subtract the utility of this item from the remaining utility
						remainingUtility = remainingUtility - pair.utility;
						
						// get the utility list of this item
						UtilityList utilityListOfItem = mapItemToUtilityList.get(pair.item);
						
						// Add a new Element to the utility list of this item corresponding to this transaction
						Element element = new Element(currentTidForUtilityList, pair.utility, remainingUtility);
						utilityListOfItem.addElement(element);
											
						// BEGIN NEW OPTIMIZATION for FHM
						Map<Integer, Integer> mapFMAPItem = mapEUCS.get(pair.item);
						if(mapFMAPItem == null) {
							mapFMAPItem = new HashMap<Integer, Integer>();
							mapEUCS.put(pair.item, mapFMAPItem);
						}
	
						for(int j = i+1; j< revisedTransaction.size(); j++){
							Pair pairAfter = revisedTransaction.get(j);
							Integer twuSum = mapFMAPItem.get(pairAfter.item);
							if(twuSum == null) {
								mapFMAPItem.put(pairAfter.item, newTWU);
							}else {
								mapFMAPItem.put(pairAfter.item, twuSum + newTWU);
							}
						}
							
						// END OPTIMIZATION of FHM
					}
				}
				tid++; // increase tid number for next transaction

			}
		} catch (Exception e) {
			// to catch error while reading the input file
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
	    }
		
		// check the memory usage
		checkMemory();

		// Mine the database recursively
		huiListIns(itemsetBuffer, 0, null, listOfUtilityLists, minUtility);
		
		// check the memory usage again and close the file.
		checkMemory();
		
		//closeFile();
		writer.close();
		
		// record end time
		endTimestamp = System.currentTimeMillis();
		
		totalTimeForAllRuns += (endTimestamp - startTimestamp);
		totalCandidateCountForAllRuns += candidateCount;
	}
	
	public void closeFile() throws IOException {
		// close output file
		writer.close();
	}
	
	/**
	 * Method to compare items by their TWU
	 * @param item1 an item
	 * @param item2 another item
	 * @return 0 if the same item, >0 if item1 is larger than item2,  <0 otherwise
	 */
	private int compareItems(int item1, int item2) {
		int compare = mapItemToTWU.get(item1) - mapItemToTWU.get(item2);  
		// if the same, use the lexical order otherwise use the TWU
		//*************** BEGIN NEW ****************************
		//  DESACTIVATE THE TWU SORTING ORDER HERE
		// ***************************************************
		return (compare == 0)? item1 - item2 :  compare;
	}
	
	private int compareItemsByRank(int item1, int item2) {
		int compare = mapItemToRank.get(item1) - mapItemToRank.get(item2);   
		// if the same, use the lexical order otherwise use the TWU
		//*************** BEGIN NEW ****************************
		//  DESACTIVATE THE TWU SORTING ORDER HERE
		// ***************************************************
		return (compare == 0)? item1 - item2 :  compare;
	}
	
	/**
	 * This is the recursive method to find all high utility itemsets. It writes
	 * the itemsets to the output file.
	 * @param prefix  This is the current prefix. Initially, it is empty.
	 * @param pUL This is the Utility List of the prefix. Initially, it is empty.
	 * @param ULs The utility lists corresponding to each extension of the prefix.
	 * @param minUtility The minUtility threshold.
	 * @param prefixLength The current prefix length
	 * @throws IOException
	 */
	private void huiListIns(int [] prefix, int prefixLength, UtilityList pUL, List<UtilityList> ULs, int minUtility)
			throws IOException {
		
		// For each extension X of prefix P
		for(int i=0; i< ULs.size(); i++){
			UtilityList X = ULs.get(i);

			// If pX is a high utility itemset.
			// we save the itemset:  pX 
			if(X.sumIutils >= minUtility){
				// save to file
				writeOut(prefix, prefixLength, X.item, X.sumIutils, X.elements.size());
			}
			
			// If the sum of the remaining utilities for pX
			// is higher than minUtility, we explore extensions of pX.
			// (this is the pruning condition)
			if(X.sumIutils + X.sumRutils >= minUtility){
				// This list will contain the utility lists of pX extensions.
				List<UtilityList> exULs = new ArrayList<UtilityList>();
				// For each extension of p appearing
				// after X according to the ascending order
				for(int j=i+1; j < ULs.size(); j++){
					UtilityList Y = ULs.get(j);
	
					// ======================== NEW OPTIMIZATION USED IN FHM
					Map<Integer, Integer> mapTWUF = mapEUCS.get(X.item);
					if(mapTWUF != null) {
						Integer twuF = mapTWUF.get(Y.item);
						if(twuF == null || twuF < minUtility) {
							continue;
						}
					}
					candidateCount++;
					// =========================== END OF NEW OPTIMIZATION
					
					// we construct the extension pXY 
					// and add it to the list of extensions of pX
					UtilityList temp = construct(pUL, X, Y);
						
					exULs.add(temp);
				}
				// We create new prefix pX
				itemsetBuffer[prefixLength] = X.item;
				
				// We make a recursive call to discover all itemsets with the prefix pXY
				huiListIns(itemsetBuffer, prefixLength+1, X, exULs, minUtility); 
			}
		}
	}
	
	/**
	 * This method constructs the utility list of pXY
	 * @param P :  the utility list of prefix P.
	 * @param px : the utility list of pX
	 * @param py : the utility list of pY
	 * @return the utility list of pXY
	 */
	private UtilityList construct(UtilityList P, UtilityList px, UtilityList py) {
		// create an empy utility list for pXY
		UtilityList pxyUL = new UtilityList(py.item);
		// for each element in the utility list of pX
		for(Element ex : px.elements){
			// do a binary search to find element ey in py with tid = ex.tid
			Element ey = findElementWithTID(py, ex.tid);
			if(ey == null){
				continue;
			}
			// if the prefix p is null
			if(P == null){
				// Create the new element
				Element eXY = new Element(ex.tid, ex.iutils + ey.iutils, ey.rutils);
				// add the new element to the utility list of pXY
				pxyUL.addElement(eXY);
				
			}else{
				// find the element in the utility list of p wih the same tid
				Element e = findElementWithTID(P, ex.tid);
				if(e != null){
					// Create new element
					Element eXY = new Element(ex.tid, ex.iutils + ey.iutils - e.iutils,
								ey.rutils);
					// add the new element to the utility list of pXY
					pxyUL.addElement(eXY);
				}
			}	
		}
		// return the utility list of pXY.
		return pxyUL;
	}
	
	/**
	 * Do a binary search to find the element with a given tid in a utility list
	 * @param ulist the utility list
	 * @param tid  the tid
	 * @return  the element or null if none has the tid.
	 */
	private Element findElementWithTID(UtilityList ulist, int tid){
		List<Element> list = ulist.elements;
		
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
	 * Method to write a high utility itemset to the output file.
	 * @param the prefix to be writent o the output file
	 * @param an item to be appended to the prefix
	 * @param sumIutils the utility of the prefix concatenated with the item
	 * @param prefixLength The current prefix length
	 */
	private void writeOut(int[] prefix, int prefixLength, int item, long sumIutils, int support) throws IOException {
		huiCount++; // increase the number of high utility itemsets found
		
		//Create a string buffer
		StringBuilder buffer = new StringBuilder();
		List<Integer> itemset = new ArrayList<Integer>(prefix.length);
		// append the prefix
		for (int i = 0; i < prefixLength; i++) {
			buffer.append(prefix[i]);
			buffer.append(' ');
			itemset.add(prefix[i]);
		}
		// append the last item
		buffer.append(item);
		itemset.add(item);
		// append the utility value
		buffer.append(" #UTIL: ");
		buffer.append(sumIutils);
		// write to file
		writer.write(buffer.toString());
		writer.newLine();
	}
	
	/**
	 * Method to check the memory usage and keep the maximum memory usage.
	 */
	private void checkMemory() {
		// get the current memory usage
		double currentMemory = (Runtime.getRuntime().totalMemory() -  Runtime.getRuntime().freeMemory())
				/ 1024d / 1024d;
		// if higher than the maximum until now
		if (currentMemory > maxMemory) {
			// replace the maximum with the current memory usage
			maxMemory = currentMemory;
		}
	}
	
	/**
	 * Print statistics about the latest execution to System.out.
	 * @throws IOException 
	 */
	public void printStats() throws IOException {
		System.out.println("=============  HUI-LIST_INS ALGORITHM - STATS =============");
		System.out.println(" Transaction processed count : " + currentTidForUtilityList);
		System.out.println(" Execution time ~ "                  + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Memory ~ "                      + maxMemory+ " MB");
		System.out.println(" High-utility itemsets count : " + huiCount); 
		System.out.println(" Candidate count : "             + candidateCount);
		System.out.println(" minutil : " + minUtility);
		System.out.println("===================================================");
		System.out.println("TOTAL CANDIDATE COUNT FOR ALL RUNS:" + totalCandidateCountForAllRuns + " candidates");
		System.out.println("TOTAL TIME FOR ALL RUNS:" + totalTimeForAllRuns + " ms");
		System.out.println("===================================================");
	}

}