package ca.pfv.spmf.algorithms.frequentpatterns.pfpm;

/* This file is copyright (c) 2008-2016 Philippe Fournier-Viger
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
* 
*/


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This is an implementation of the "PFPM" algorithm for Periodic Itemsets 
 * as described in the conference paper : <br/><br/>
 * 
 * Fournier-Viger, P., Lin, C.-W., Duong, Q.-H., Dam, T.-L., Sevcic, L., Uhrin, D., Voznak, M. (2016). 
 * PFPM: Discovering Periodic Frequent Patterns with Novel Periodicity Measures. Proc. 2nd Czech-China 
 * Scientific Conference 2016, Elsevier, 10 pages.
 *
 * @see TIDList
 * @see Element
 * @author Philippe Fournier-Viger 2016
 */
public class AlgoPFPM {
	
	private static final boolean ENABLE_LA_PRUNE = false;

	/** the number of high-utility itemsets generated */
	public int phuiCount =0; 
	
	/** the number of candidate high-utility itemsets */
	public int candidateCount =0;
	
	/** Map to remember the TWU, support and largest periodicity of each item */
	static Map<Integer, ItemInfo> mapItemToItemInfo;
	
	/** writer to write the output file  */
	BufferedWriter writer = null;   
	
	/** The eucs structure:  key: item   key: another item   value: support */
	Map<Integer, Map<Integer, Long>> mapESCS = null;  
	
	/** enable ESCP strategy  */
	boolean ENABLE_ESCP = true;
	
	/** variable for debug mode */
	boolean DEBUG = false;
	
	/** buffer for storing the current itemset that is mined when performing mining
	* the idea is to always reuse the same buffer to reduce memory usage. */
	final int BUFFERS_SIZE = 200;
	private int[] itemsetBuffer = null;
	
	/** buffer for storing the current transaction. */
	final int TRANSACTION_BUFFERS_SIZE = 1000;
	private int[] transactionBuffer = null;
	
	/** the database size (number of transactions */
	protected int databaseSize = 0;
	
	/** minimum periodicity threshold**/
	int minPeriodicity;

	/** maximum periodicity threshold **/
	int maxPeriodicity;

	/** maximum average periodicity threshold **/
	int minAveragePeriodicity;
	
	/** maximum average periodicity threshold **/
	int maxAveragePeriodicity;
	
	/** minimum number of items that patterns should contain (optional parameter) **/
	int minimumLength = 0;
	
	/** maximum number of items that patterns should contain (optional parameter)**/
	int maximumLength = Integer.MAX_VALUE;
	
	/** the gamma parameter **/
	protected double supportPruningThreshold = 0;
	
	/** the total execution time **/
	public double totalExecutionTime = 0;
	
	/** the maximumMemoryUsage **/
	public double maximumMemoryUsage = 0;

	/** this class represent a single item and its support and periodicity */
	class ItemInfo{
		int support = 0;
		int largestPeriodicity = 0;
		int smallestPeriodicity = Integer.MAX_VALUE;
		int lastSeenTransaction = 0;
	}
	
	/**
	 * Default constructor
	 */
	public AlgoPFPM() {
		
	}

	/**
	 * Run the algorithm
	 * @param input the input file path
	 * @param output the output file path
	 * @param minPeriodicity the minimum periodicity threshold 
	 * @param maxPeriodicity the maximum periodicity threshold 
	 * @param minAveragePeriodicity 
	 * @param maxAveragePeriodicity2 
	 * @throws IOException exception if error while writing the file
	 */
	public void runAlgorithm(String input, String output, int minPeriodicity, int maxPeriodicity, int minAveragePeriodicity, int maxAveragePeriodicity) throws IOException {
		// reset maximum
		MemoryLogger.getInstance().reset();
		
		/** the time at which the algorithm started */
		long startTimestamp = 0;  
		
		// save the  periodicity thresholds
		this.maxPeriodicity = maxPeriodicity;
		this.minPeriodicity = minPeriodicity;
		this.minAveragePeriodicity = minAveragePeriodicity;
		this.maxAveragePeriodicity = maxAveragePeriodicity;
		
		// initialize the buffer for storing the current itemset
		itemsetBuffer = new int[BUFFERS_SIZE];

		if(ENABLE_ESCP){
			mapESCS =  new HashMap<Integer, Map<Integer, Long>>();
		}
		
		startTimestamp = System.currentTimeMillis();
		
		writer = new BufferedWriter(new FileWriter(output));
		
		// We create a map to store the support of each item
		mapItemToItemInfo  = new HashMap<Integer, ItemInfo>();
		
		// We scan the database a first time to calculate the TWU of each item.
		BufferedReader myInput = null;
		databaseSize = 0;
		String thisLine = null;
		
		long sumOfTransactionLength = 0;  // for debugging
		
		try {
			// prepare the object for reading the file
			myInput = new BufferedReader(new InputStreamReader( new FileInputStream(new File(input))));
			// for each line (transaction) until the end of file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is  a comment, is  empty or is a
				// kind of metadata
				if (thisLine.isEmpty() == true ||
						thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
								|| thisLine.charAt(0) == '@') {
					continue;
				}

				// increase the number of transactions
				databaseSize++;
				
				// split the transaction into items
				String items[] = thisLine.split(" "); 
				
				sumOfTransactionLength += items.length;
				
				// for each item, we add the transaction utility to its TWU
				for(int i=0; i <items.length; i++){
					
					// convert item to integer
					Integer item = Integer.parseInt(items[i]);
					
					// we also add 1 to the support of the item
					ItemInfo itemInfo = mapItemToItemInfo.get(item);
					if(itemInfo == null){
						itemInfo = new ItemInfo();
						mapItemToItemInfo.put(item, itemInfo);
					}
					// increase support
					itemInfo.support++;
					
					
					// **** PFPM ***********
					// calculate periodicity
					int periodicity = databaseSize - itemInfo.lastSeenTransaction;
					// update periodicity of this item
					if(itemInfo.largestPeriodicity < periodicity){
						itemInfo.largestPeriodicity = periodicity;
					}
					itemInfo.lastSeenTransaction = databaseSize;
					
//					if(item == 4){
//						System.out.println(periodicity);
//					}
					
					// IF IT IS not the first time that we see the item, we update
					// its minimum periodicity 
					if(itemInfo.support != 1 && periodicity < itemInfo.smallestPeriodicity){
						itemInfo.smallestPeriodicity = periodicity;
					}
					// update average periodicity
//					itemInfo.averagePeriodicity = itemInfo.averagePeriodicity +  (double)periodicity;
//					System.out.println(itemInfo.averagePeriodicity);
					// **** END PFPM ***********
				}
				
			}
		} catch (Exception e) {
			// catches exception if error while reading the input file
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
	    }
		
		supportPruningThreshold  = (((double)databaseSize) / ((double)maxAveragePeriodicity) ) - 1d ;


		
		// **** PFPM ***********
		for(Entry<Integer,ItemInfo> entry: mapItemToItemInfo.entrySet()){
			ItemInfo itemInfo = entry.getValue();
			
			// calculate the last period
			int periodicity = databaseSize - itemInfo.lastSeenTransaction;
			
//			if(entry.getKey() == 4){
//				System.out.println(periodicity);
//			}
			// update periodicity of this item
			if(itemInfo.largestPeriodicity < periodicity){
				itemInfo.largestPeriodicity = periodicity;
			}
			
			// Important: we do not update the minimum periodicity of the item using its last period
			// as explained in the paper.
//			if(periodicity < itemInfo.smallestPeriodicity){
//				itemInfo.smallestPeriodicity = periodicity;
//			}
			// update average periodicity
//			itemInfo.averagePeriodicity += periodicity;
//			itemInfo.averagePeriodicity /= itemInfo.support;
			if(DEBUG){
				System.out.println(" item : " + entry.getKey() 
//						+ " avg periodicity: " + itemInfo.averagePeriodicity
						+ "\tavgPer: " + (databaseSize / (double)(itemInfo.support+1))
						+ "\tminPer: " + itemInfo.smallestPeriodicity
						+ "\tmaxPer: " + itemInfo.largestPeriodicity
						+ "\tsup.: " + itemInfo.support
						);
			}
		}
		if(DEBUG){
			System.out.println("Number of transactions : " + databaseSize);
			System.out.println("Average transaction length : " +  sumOfTransactionLength /(double)databaseSize);
			System.out.println("Number of items : " + mapItemToItemInfo.size());
			System.out.println("Average pruning threshold  (|D| / maxAvg $) - 1): " + supportPruningThreshold);
		}
		// **** END PFPM ***********
		
		// CREATE A LIST TO STORE THE UTILITY LIST OF ITEMS WITH TWU  >= MIN_UTILITY.
		List<TIDList> listOfUtilityLists = new ArrayList<TIDList>();
		// CREATE A MAP TO STORE THE UTILITY LIST FOR EACH ITEM.
		// Key : item    Value :  utility list associated to that item
		Map<Integer, TIDList> mapItemToUtilityList = new HashMap<Integer, TIDList>();
		
		// For each item
		for(Entry<Integer,ItemInfo> entry: mapItemToItemInfo.entrySet()){
			// if the item is promising  (TWU >= minutility)  and is frequent
			ItemInfo itemInfo = entry.getValue();
			if(itemInfo.support >= supportPruningThreshold &&
					itemInfo.largestPeriodicity <= maxPeriodicity ){
				int item = entry.getKey();
				
				// create an empty Utility List that we will fill later.
				TIDList uList = new TIDList(item);
				mapItemToUtilityList.put(item, uList);
				// add the item to the list of high TWU items
				listOfUtilityLists.add(uList); 
				///*************** PFPM ****************
				// set the periodicity
				uList.largestPeriodicity = itemInfo.largestPeriodicity;
				uList.smallestPeriodicity = itemInfo.smallestPeriodicity;
				///*************** END PFPM ****************
				
			}
		}
		// SORT THE LIST OF HIGH TWU ITEMS IN ASCENDING ORDER
		Collections.sort(listOfUtilityLists, new Comparator<TIDList>(){
			public int compare(TIDList o1, TIDList o2) {
				// compare the TWU of the items
				return compareItems(o1.item, o2.item);
			}
			});
		
		// SECOND DATABASE PASS TO CONSTRUCT THE TID LISTS 
		// OF 1-ITEMSETS  HAVING TWU  >= minutil (promising items)
		try {
			transactionBuffer = new int[TRANSACTION_BUFFERS_SIZE];
					
			// prepare object for reading the file
			myInput = new BufferedReader(new InputStreamReader(new FileInputStream(new File(input))));
			// variable to count the number of transaction
			int tid =0;
			// for each line (transaction) until the end of file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is  a comment, is  empty or is a
				// kind of metadata
				if (thisLine.isEmpty() == true ||
						thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
								|| thisLine.charAt(0) == '@') {
					continue;
				}
				
				// get the list of items
				String items[] = thisLine.split(" ");
				
				int sizeNewTransaction = 0;
				
				// Convert the transaction to integers and keep only promising items
				for(int i=0; i <items.length; i++){
					int item = Integer.parseInt(items[i]);
					ItemInfo itemInfo = mapItemToItemInfo.get(item);
					/// convert values to integers
					// if the item has enough utility
					if(itemInfo.support >= supportPruningThreshold &&
							itemInfo.largestPeriodicity <= maxPeriodicity ){
						transactionBuffer[sizeNewTransaction++] = Integer.parseInt(items[i]);
					}
				}
				
				// If the optimization is enabled, we need to sort each transaction to ensure
				// that the optimization will work as expected
				if(ENABLE_ESCP){
					insertionSort(transactionBuffer,sizeNewTransaction);
				}
				
//				Arrays.sort(listOfUtilityLists, new Comparator<TIDList>(){
//					public int compare(TIDList o1, TIDList o2) {
//						// compare the TWU of the items
//						return compareItems(o1.item, o2.item);
//					}
//					});
				
				// for each item
				for(int i=0; i <sizeNewTransaction; i++){
					/// convert values to integers
					int item = transactionBuffer[i];
					// get the utility list of this item
					TIDList utilityListOfItem = mapItemToUtilityList.get(item);
					
					// Add a new Element to the utility list of this item corresponding to this transaction
					utilityListOfItem.addElement(tid);


					if(ENABLE_ESCP){
						Map<Integer, Long> mapESItem = mapESCS.get(item);
						if(mapESItem == null) {
							mapESItem = new HashMap<Integer, Long>();
							mapESCS.put(item, mapESItem);
						}
						for(int j = i+1; j< sizeNewTransaction; j++){
							int item2 = transactionBuffer[j];
							Long support = mapESItem.get(item2);
							if(support == null) {
								mapESItem.put(item2, 1l);
							}else {
								mapESItem.put(item2, support + 1);
							}
						}
					}
				}

				tid++; // increase tid number for next transaction

			}
			transactionBuffer = null;
			
		} catch (Exception e) {
			// to catch error while reading the input file
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
			transactionBuffer = null;
	    }
		
		// **** Release the memory for the maps ****
		mapItemToItemInfo = null;
		mapItemToUtilityList = null;
		
		// check the memory usage
		MemoryLogger.getInstance().checkMemory();

		// Mine the database recursively
		fpp(itemsetBuffer, 0, null, listOfUtilityLists);
		
		// check the memory usage again and close the file.
		MemoryLogger.getInstance().checkMemory();
		// close output file
		writer.close();
		// record end time/** the time at which the algorithm ended */
		totalExecutionTime  = System.currentTimeMillis() - startTimestamp;
		// save the maximum memory usage
		maximumMemoryUsage = MemoryLogger.getInstance().getMaxMemory();
	}
	
	/**
	 * Implementation of Insertion sort for integers.
	 * This has an average performance of O(n log n)
	 * @param a array of integers
	 * @param sizeNewTransaction 
	 */
	public static void insertionSort(int [] a, int sizeNewTransaction){
		for(int j=1; j< sizeNewTransaction; j++){
			int key = a[j];
			int i = j - 1;
			for(; i>=0 && (compareItems(a[i],key) > 0); i--){
				a[i+1] = a[i];
			}
			a[i+1] = key;
		}
	}
	
	/**
	 * Method to compare items by their TWU
	 * @param item1 an item
	 * @param item2 another item
	 * @return 0 if the same item, >0 if item1 is larger than item2,  <0 otherwise
	 */
	private static int compareItems(int item1, int item2) {
//		int compare = (int)( mapItemToItemInfo.get(item1).support - mapItemToItemInfo.get(item2).support);
		
		int compare = (int)( mapItemToItemInfo.get(item1).support - mapItemToItemInfo.get(item2).support);
		// if the same, use the lexical order otherwise use the TWU
		return (compare == 0)? item1 - item2 :  compare;
	}
	
	/**
	 * This is the recursive method to find all periodic high utility itemsets. It writes
	 * the itemsets to the output file.
	 * @param prefix  This is the current prefix. Initially, it is empty.
	 * @param pUL This is the Utility List of the prefix. Initially, it is empty.
	 * @param ULs The utility lists corresponding to each extension of the prefix.
	 * @param minUtility The minUtility threshold.
	 * @param prefixLength The current prefix length
	 * @throws IOException
	 */
	private void fpp(int [] prefix,	int prefixLength, TIDList pUL, List<TIDList> ULs)
			throws IOException {
		
		// The size of patterns that will be produced 
		int patternSize = prefixLength + 1;
		
		// For each extension X of prefix P
		for(int i=0; i< ULs.size(); i++){
			TIDList X = ULs.get(i);
	
			double averagePeriodicity = (double) databaseSize / ((double) X.getSupport()+1) ;
			
			// If pX is a high utility itemset.
			// we save the itemset:  pX 
			if(averagePeriodicity <= maxAveragePeriodicity
					&& averagePeriodicity >= minAveragePeriodicity
					&& X.smallestPeriodicity >= minPeriodicity
					&& X.largestPeriodicity <= maxPeriodicity){
				
				// save to file if the pattern meet the minimum and maximum length constraints
				if(patternSize >= minimumLength && patternSize <= maximumLength){
					writeOut(prefix, prefixLength, X, averagePeriodicity);
				}
			}
			
			// If we still want to find larger patterns... (based on the optional maximum length constraint)
			if(patternSize < maximumLength){
			
				// This list will contain the utility lists of pX extensions.
				List<TIDList> exULs = new ArrayList<TIDList>();
				// For each extension of p appearing
				// after X according to the ascending order
				for(int j=i+1; j < ULs.size(); j++){
					TIDList Y = ULs.get(j);
		
					// =========================== END OF NEW OPTIMIZATION
					// ======================== NEW OPTIMIZATION USED IN PFPM
					if(ENABLE_ESCP){
						Map<Integer, Long> mapSUPF = mapESCS.get(X.item);
						if(mapSUPF != null) {
							Long supportF = mapSUPF.get(Y.item);
							if(supportF == null || supportF < supportPruningThreshold) {
								continue;
							}
						}
					}
					// =========================== END OF NEW OPTIMIZATION
					candidateCount++;
					
					// we construct the extension pXY 
					// and add it to the list of extensions of pX
					TIDList temp = construct(prefixLength == 0, X, Y);
					if(temp != null) {
						exULs.add(temp);
					}
				}
				// We create new prefix pX
				itemsetBuffer[prefixLength] = X.item;
				// We make a recursive call to discover all itemsets with the prefix pXY
				fpp(itemsetBuffer, prefixLength+1, X, exULs); 
			
			} // end if
		}
		MemoryLogger.getInstance().checkMemory();
	}
	
	/**
	 * This method constructs the utility list of pXY
	 * @param firstTime : if the first time
	 * @param px : the utility list of pX
	 * @param py : the utility list of pY
	 * @return the utility list of pXY
	 */
	private TIDList construct(boolean firstTime, TIDList px, TIDList py) {
		// create an empy utility list for pXY
		TIDList pxyUL = new TIDList(py.item);

		// ********** PFPM *************
		int lastTid = -1;  // IMPORTANT BECAUSE TIDS STARTS AT ZERO...!!
//		if(P==null && px.item == 4 && py.item == 2){
//			System.out.println("DEBUG");
//		}
		// ********** END PFPM *************
		
		// A similar strategy to LA-prune will be applied for the support
		// Initialize the sum of support
		long totalSupport = px.getSupport();
		// ================================================
		
		// for each element in the utility list of pX
		for(Integer ex : px.elements){
			// do a binary search to find element ey in py with tid = ex.tid
			Integer ey = findElementWithTID(py, ex);
			if(ey == null){
				//== new optimization - LA-prune == /
				if(ENABLE_LA_PRUNE) {
					// decrease the support by one transaction
					totalSupport -= 1;
					if(totalSupport < supportPruningThreshold) {
						return null;
					}
				}
				continue;
			}
			// if the prefix p is null
			if(firstTime){
				// ********** PFPM *************
				// check the periodicity
				int periodicity = ex - lastTid;
//				if(P==null && px.item == 4 && py.item == 2){
//					System.out.println("period : " + periodicity);
//				}
//				
				if(periodicity > maxPeriodicity){
					return null;
				}
				if(periodicity >= pxyUL.largestPeriodicity){
					pxyUL.largestPeriodicity = periodicity;
				}
				lastTid = ex;
				
				// IMPORTANT DO NOT COUNT THE FIRST PERIOD FOR MINIMUM UTILITY
				if(pxyUL.elements.size() > 0 && periodicity < pxyUL.smallestPeriodicity){
					pxyUL.smallestPeriodicity = periodicity;
				}
				
				// ********** END PFPM *************
				
				// add the new element to the utility list of pXY
				pxyUL.addElement(ex);
				
			}else{
				// find the element in the utility list of p wih the same tid

				// ********** PFPM *************
				// check the periodicity
				int periodicity = ex - lastTid;
				if(periodicity > maxPeriodicity){
					return null;
				}
				if(periodicity >= pxyUL.largestPeriodicity){
					pxyUL.largestPeriodicity = periodicity;
				}
				lastTid = ex;
				
				// IMPORTANT DO NOT COUNT THE FIRST PERIOD FOR MINIMUM UTILITY
				if(pxyUL.elements.size() > 0 && periodicity < pxyUL.smallestPeriodicity){
					pxyUL.smallestPeriodicity = periodicity;
				}
				// ********** END PFPM *************
				
				// add the new element to the utility list of pXY
				pxyUL.addElement(ex);
			}	
		}
		
		// ********** PFPM *************
		// check the periodicity
		int periodicity = (databaseSize-1) - lastTid;  // Need -1 because tids starts at zero
//		if(P==null && px.item == 4 && py.item == 2){
//			System.out.println("period : " + periodicity);
//		}
		
		if(periodicity > maxPeriodicity){
			return null;
		}
		if(periodicity >= pxyUL.largestPeriodicity){
			pxyUL.largestPeriodicity = periodicity;
		}
		
		if(pxyUL.getSupport() < supportPruningThreshold){
			return null;
		}
		
		// WE DO NOT UPDATE THE MINIMUM PERIOD
//		if(pxyUL.smallestPeriodicity > maxAveragePeriodicity){
//			return null;
//		}

		// return the utility list of pXY.
		return pxyUL;
	}
	
	/**
	 * Do a binary search to find the element with a given tid in a utility list
	 * @param ulist the utility list
	 * @param tid  the tid
	 * @return  the element or null if none has the tid.
	 */
	private Integer findElementWithTID(TIDList ulist, int tid){
		List<Integer> list = ulist.elements;
		
		// perform a binary search to check if  the subset appears in  level k-1.
        int first = 0;
        int last = list.size() - 1;
       
        // the binary search
        while( first <= last )
        {
        	int middle = ( first + last ) >>> 1; // divide by 2

            if(list.get(middle)< tid){
            	first = middle + 1;  //  the itemset compared is larger than the subset according to the lexical order
            }
            else if(list.get(middle) > tid){
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
	 * @param prefixLength the prefix length
	 * @param utilityList the utility list
	 * @param averagePeriodicity the average periodicity
	 */
	private void writeOut(int[] prefix, int prefixLength, TIDList utilityList, double averagePeriodicity) throws IOException {
		phuiCount++; // increase the number of high utility itemsets found
		
		//Create a string buffer
		StringBuilder buffer = new StringBuilder();
		// append the prefix
		for (int i = 0; i < prefixLength; i++) {
			buffer.append(prefix[i]);
			buffer.append(' ');
		}
		// append the last item
		buffer.append(utilityList.item);
		// append the utility value
		buffer.append(" #SUP: ");
		buffer.append(utilityList.getSupport());
		
		// append the smallest periodicity 
		buffer.append(" #MINPER: ");
		buffer.append(utilityList.smallestPeriodicity);
		
		// append the largest periodicity 
		buffer.append(" #MAXPER: ");
		buffer.append(utilityList.largestPeriodicity);
		
		// append the average periodicity
		buffer.append(" #AVGPER: ");
		buffer.append(averagePeriodicity);
		
		// write to file
		writer.write(buffer.toString());
		writer.newLine();
	}

	
	/**
	 * Print statistics about the latest execution to System.out.
	 * @throws IOException 
	 */
	public void printStats() throws IOException {
		// print the eucs
		
		// print the escs
		if(DEBUG && ENABLE_ESCP) {
			System.out.println("===== CONTENT OF ESCS =====");
			for(Entry<Integer, Map<Integer, Long>> entry : mapESCS.entrySet()) {
				System.out.print("Item:" + entry.getKey()  + " -- ");
				for(Entry<Integer, Long> entry2 :entry.getValue().entrySet()) {
					System.out.print(entry2.getKey()  + " (" + entry2.getValue() + ")  ");
				}
				System.out.println();
			}
		}
	
		
		String optimizationESCP = ENABLE_ESCP ? " ESCP: true " : " ESCP: false ";
		System.out.println("=============  PFPM ALGORITHM v2.17" 
		+ optimizationESCP + "=====");
		System.out.println(" Database size: "                      + (databaseSize)  + " transactions");
		System.out.println(" Time : " + totalExecutionTime + " ms");
		System.out.println(" Memory ~ "                      + maximumMemoryUsage + " MB");
		System.out.println(" Periodic Itemsets count : " + phuiCount); 
		System.out.println(" Candidate count : "             + candidateCount);
		System.out.println(" Gamma (support prunning threshold):" + supportPruningThreshold);

		if(DEBUG && ENABLE_ESCP) {
			int pairCount = 0;
			double maxMemory = getObjectSize(mapESCS);
			for(Entry<Integer, Map<Integer, Long>> entry : mapESCS.entrySet()) {
				maxMemory += getObjectSize(entry.getKey());
				for(Entry<Integer, Long> entry2 :entry.getValue().entrySet()) {
					pairCount++;
					maxMemory += getObjectSize(entry2.getKey()) + getObjectSize(entry2.getValue());
				}
			}
			System.out.println("ESCS size " + maxMemory + " MB    PAIR COUNT " + pairCount);
		}
		System.out.println("===================================================");
		

		
	}

	/**
	 * Get the size of a Java object (for debugging purposes)
	 * @param object the object
	 * @return the size in MB
	 * @throws IOException
	 */
    private double getObjectSize(
            Object object)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        oos.close();
        double maxMemory = baos.size() / 1024d / 1024d;
        return maxMemory;
    }

    /**
     * This method let the user specify if the ESCP should be enabled.
     * @param enable if this parameter is true, ESCP will be enabled. Otherwise, not.
     */
	public void setEnableESCP(boolean enable) {
		ENABLE_ESCP = enable;
	}
	
    /**
     * This method let the user specify a minimum length for patterns to be found.
     * By the default, the minimum length is 1.
     * @param minimumLength  an integer indicating a minimum number of items
     */
	public void setMinimumLength(int minimumLength) {
		this.minimumLength = minimumLength;
	}
	
    /**
     * This method let the user specify a maximum length for patterns to be found.
     * By the default, the maximum length is 1.
     * @param maximumLength  an integer indicating a maximum number of items
     */
	public void setMaximumLength(int maximumLength) {
		this.maximumLength = maximumLength;
	}

}