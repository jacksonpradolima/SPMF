package ca.pfv.spmf.algorithms.frequentpatterns.hui_miner;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This is an implementation of the "PHM" algorithm for Periodic High-Utility Itemsets Mining
 * as described in the conference paper : <br/><br/>
 * 
 *  Fournier-Viger, P., Lin, C.W., Duong, Q.-H., Dam, T.-L. (2016). PHM: Mining Periodic High-Utility Itemsets.
 *   Proc. 16th Industrial Conference on Data Mining. Springer LNAI 9728, 15 pages .
 *
 * @see UtilityListPHM
 * @see Element
 * @author Philippe Fournier-Viger 2016
 */
public class AlgoPHM {
	
	/** the number of high-utility itemsets generated */
	public int phuiCount =0; 
	
	/** the number of candidate high-utility itemsets */
	public int candidateCount =0;

	/** Map to remember the TWU of each item */
	Map<Integer, Long> mapItemToTWU;
	
	/** Map to remember the TWU, support and largest periodicity of each item */
	Map<Integer, ItemInfo> mapItemToItemInfo;
	
	/** writer to write the output file  */
	BufferedWriter writer = null;  
	
	/** The eucs structure:  key: item   key: another item   value: twu */
	Map<Integer, Map<Integer, Long>> mapEUCS = null;  
	
	/** The eucs structure:  key: item   key: another item   value: support */
	Map<Integer, Map<Integer, Long>> mapESCS = null;  
	
	/** enable LA-prune strategy  */
	boolean ENABLE_LA_PRUNE = true;
	
	/** enable EUCP strategy  */
	boolean ENABLE_EUCP = true;
	
	/** enable ESCP strategy  */
	boolean ENABLE_ESCP = true;
	
	/** variable for debug mode */
	boolean DEBUG = false;
	
	/** buffer for storing the current itemset that is mined when performing mining
	* the idea is to always reuse the same buffer to reduce memory usage. */
	final int BUFFERS_SIZE = 200;
	private int[] itemsetBuffer = null;
	
	/** the database size (number of transactions */
	int databaseSize = 0;
	
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
	double supportPruningThreshold = 0;
	
	/** the total execution time **/
	public double totalExecutionTime = 0;
	
	/** the maximumMemoryUsage **/
	public double maximumMemoryUsage = 0;
	
	
	/** this class represent an item and its utility in a transaction */
	class Pair{
		int item = 0;
		int utility = 0;
	}
	
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
	public AlgoPHM() {
		
	}

	/**
	 * Run the algorithm
	 * @param input the input file path
	 * @param output the output file path
	 * @param minUtility the minimum utility threshold
	 * @param minPeriodicity the minimum periodicity threshold 
	 * @param maxPeriodicity the maximum periodicity threshold 
	 * @param minAveragePeriodicity 
	 * @param maxAveragePeriodicity2 
	 * @throws IOException exception if error while writing the file
	 */
	public void runAlgorithm(String input, String output, int minUtility, int minPeriodicity, int maxPeriodicity, int minAveragePeriodicity, int maxAveragePeriodicity) throws IOException {
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
		
		if(ENABLE_EUCP){
			mapEUCS =  new HashMap<Integer, Map<Integer, Long>>();
		}
		if(ENABLE_ESCP){
			mapESCS =  new HashMap<Integer, Map<Integer, Long>>();
		}
		
		
		startTimestamp = System.currentTimeMillis();
		
		writer = new BufferedWriter(new FileWriter(output));

		//  We create a  map to store the TWU of each item
		mapItemToTWU = new HashMap<Integer, Long>();
		
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
				
				// split the transaction according to the : separator
				String split[] = thisLine.split(":"); 
				// the first part is the list of items
				String items[] = split[0].split(" "); 
				// the second part is the transaction utility
				int transactionUtility = Integer.parseInt(split[1]);  
				
				sumOfTransactionLength += items.length;
				
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
					
					// we also add 1 to the support of the item
					ItemInfo itemInfo = mapItemToItemInfo.get(item);
					if(itemInfo == null){
						itemInfo = new ItemInfo();
						mapItemToItemInfo.put(item, itemInfo);
					}
					// increase support
					itemInfo.support++;
					
					
					// **** PHM ***********
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
					// **** END PHM ***********
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


		
		// **** PHM ***********
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
						+ "\tTWU: " + mapItemToTWU.get(entry.getKey())
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
		// **** END PHM ***********
		
		// CREATE A LIST TO STORE THE UTILITY LIST OF ITEMS WITH TWU  >= MIN_UTILITY.
		List<UtilityListPHM> listOfUtilityLists = new ArrayList<UtilityListPHM>();
		// CREATE A MAP TO STORE THE UTILITY LIST FOR EACH ITEM.
		// Key : item    Value :  utility list associated to that item
		Map<Integer, UtilityListPHM> mapItemToUtilityList = new HashMap<Integer, UtilityListPHM>();
		
		// For each item
		for(Integer item: mapItemToTWU.keySet()){
			// if the item is promising  (TWU >= minutility)  and is frequent
			ItemInfo itemInfo = mapItemToItemInfo.get(item);
			if(itemInfo.support >= supportPruningThreshold &&
					itemInfo.largestPeriodicity <= maxPeriodicity && 
					mapItemToTWU.get(item) >= minUtility ){
				
				// create an empty Utility List that we will fill later.
				UtilityListPHM uList = new UtilityListPHM(item);
				mapItemToUtilityList.put(item, uList);
				// add the item to the list of high TWU items
				listOfUtilityLists.add(uList); 
				///*************** PHM ****************
				// set the periodicity
				uList.largestPeriodicity = itemInfo.largestPeriodicity;
				uList.smallestPeriodicity = itemInfo.smallestPeriodicity;
				///*************** END PHM ****************
				
			}
		}
		// SORT THE LIST OF HIGH TWU ITEMS IN ASCENDING ORDER
		Collections.sort(listOfUtilityLists, new Comparator<UtilityListPHM>(){
			public int compare(UtilityListPHM o1, UtilityListPHM o2) {
				// compare the TWU of the items
				return compareItems(o1.item, o2.item);
			}
			} );
		
		// SECOND DATABASE PASS TO CONSTRUCT THE UTILITY LISTS 
		// OF 1-ITEMSETS  HAVING TWU  >= minutil (promising items)
		try {
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
				
				// split the line according to the separator
				String split[] = thisLine.split(":");
				// get the list of items
				String items[] = split[0].split(" ");
				// get the list of utility values corresponding to each item
				// for that transaction
				String utilityValues[] = split[2].split(" ");
				
				// Copy the transaction into lists but 
				// without items with TWU < minutility
				
				int remainingUtility =0;
				
				long newTWU = 0;  // NEW OPTIMIZATION 
				
				// Create a list to store items
				List<Pair> revisedTransaction = new ArrayList<Pair>();
				// for each item
				for(int i=0; i <items.length; i++){
					/// convert values to integers
					Pair pair = new Pair();
					pair.item = Integer.parseInt(items[i]);
					pair.utility = Integer.parseInt(utilityValues[i]);
					ItemInfo itemInfo = mapItemToItemInfo.get(pair.item);
					// if the item has enough utility
					if(itemInfo.support >= supportPruningThreshold &&
							itemInfo.largestPeriodicity <= maxPeriodicity && 
							mapItemToTWU.get(pair.item) >= minUtility){
						// add it
						revisedTransaction.add(pair);
						remainingUtility += pair.utility;
						newTWU += pair.utility; // NEW OPTIMIZATION
					}
				}
				
				// sort the transaction
				Collections.sort(revisedTransaction, new Comparator<Pair>(){
					public int compare(Pair o1, Pair o2) {
						return compareItems(o1.item, o2.item);
					}});

								
				// for each item left in the transaction
				for(int i = 0; i< revisedTransaction.size(); i++){
					Pair pair =  revisedTransaction.get(i);
					
//					int remain = remainingUtility; // FOR OPTIMIZATION
					
					// subtract the utility of this item from the remaining utility
					remainingUtility = remainingUtility - pair.utility;
					
					// get the utility list of this item
					UtilityListPHM utilityListOfItem = mapItemToUtilityList.get(pair.item);
					
					// Add a new Element to the utility list of this item corresponding to this transaction
					Element element = new Element(tid, pair.utility, remainingUtility);
					
					utilityListOfItem.addElement(element);
										
					// BEGIN NEW OPTIMIZATION for FHM
					if(ENABLE_EUCP){
						Map<Integer, Long> mapFMAPItem = mapEUCS.get(pair.item);
						if(mapFMAPItem == null) {
							mapFMAPItem = new HashMap<Integer, Long>();
							mapEUCS.put(pair.item, mapFMAPItem);
						}
						for(int j = i+1; j< revisedTransaction.size(); j++){
							Pair pairAfter = revisedTransaction.get(j);
							Long twuSum = mapFMAPItem.get(pairAfter.item);
							if(twuSum == null) {
								mapFMAPItem.put(pairAfter.item, newTWU);
							}else {
								mapFMAPItem.put(pairAfter.item, twuSum + newTWU);
							}
						}
					}

					if(ENABLE_ESCP){
						Map<Integer, Long> mapESItem = mapESCS.get(pair.item);
						if(mapESItem == null) {
							mapESItem = new HashMap<Integer, Long>();
							mapESCS.put(pair.item, mapESItem);
						}
						for(int j = i+1; j< revisedTransaction.size(); j++){
							Pair pairAfter = revisedTransaction.get(j);
							Long support = mapESItem.get(pairAfter.item);
							if(support == null) {
								mapESItem.put(pairAfter.item, 1l);
							}else {
								mapESItem.put(pairAfter.item, support + 1);
							}
						}
					}

					// END OPTIMIZATION of FHM
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
		
		// **** Release the memory for the maps ****
		mapItemToItemInfo = null;
		mapItemToTWU = null;
		mapItemToUtilityList = null;
		
		// check the memory usage
		MemoryLogger.getInstance().checkMemory();

		// Mine the database recursively
		phm(itemsetBuffer, 0, null, listOfUtilityLists, minUtility);
		
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
	 * Method to compare items by their TWU
	 * @param item1 an item
	 * @param item2 another item
	 * @return 0 if the same item, >0 if item1 is larger than item2,  <0 otherwise
	 */
	private int compareItems(int item1, int item2) {
//		int compare = (int)( mapItemToItemInfo.get(item1).support - mapItemToItemInfo.get(item2).support);
		
		int compare = (int)( mapItemToTWU.get(item1) - mapItemToTWU.get(item2));
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
	private void phm(int [] prefix,	int prefixLength, UtilityListPHM pUL, List<UtilityListPHM> ULs, int minUtility)
			throws IOException {
		
		// The size of patterns that will be produced 
		int patternSize = prefixLength + 1;
		
		// For each extension X of prefix P
		for(int i=0; i< ULs.size(); i++){
			UtilityListPHM X = ULs.get(i);
			
			// If the sum of the remaining utilities for pX
			// is higher than minUtility, we explore extensions of pX.
			// (this is the pruning condition)
			if(X.sumIutils + X.sumRutils >= minUtility){
				
				double averagePeriodicity = (double) databaseSize / ((double) X.getSupport()+1) ;
				
				// If pX is a high utility itemset.
				// we save the itemset:  pX 
				if(X.sumIutils >= minUtility 
						&& averagePeriodicity <= maxAveragePeriodicity
						&& averagePeriodicity >= minAveragePeriodicity
						&& X.smallestPeriodicity >= minPeriodicity
						&& X.largestPeriodicity <= maxPeriodicity){
					
					// save to file
					// save to file if the pattern meet the minimum and maximum length constraints
					if(patternSize >= minimumLength && patternSize <= maximumLength){
						writeOut(prefix, prefixLength, X, averagePeriodicity);
					}
				}
				
				// If we still want to find larger patterns... (based on the optional maximum length constraint)
				if(patternSize < maximumLength){
					// This list will contain the utility lists of pX extensions.
					List<UtilityListPHM> exULs = new ArrayList<UtilityListPHM>();
					// For each extension of p appearing
					// after X according to the ascending order
					for(int j=i+1; j < ULs.size(); j++){
						UtilityListPHM Y = ULs.get(j);
						
						// ======================== NEW OPTIMIZATION USED IN FHM
						if(ENABLE_EUCP){
							Map<Integer, Long> mapTWUF = mapEUCS.get(X.item);
							if(mapTWUF != null) {
								Long twuF = mapTWUF.get(Y.item);
								if(twuF == null || twuF < minUtility) {
									continue;
								}
							}
						}
						// =========================== END OF NEW OPTIMIZATION
						// ======================== NEW OPTIMIZATION USED IN PHM
						if(ENABLE_ESCP){
							Map<Integer, Long> mapSUPF = mapESCS.get(X.item);
							if(mapSUPF != null) {
								Long supportF = mapSUPF.get(Y.item);
								if(supportF != null && supportF < supportPruningThreshold) {
									continue;
								}
							}
						}
						// =========================== END OF NEW OPTIMIZATION
						candidateCount++;
						
						// we construct the extension pXY 
						// and add it to the list of extensions of pX
						UtilityListPHM temp = construct(pUL, X, Y, minUtility);
						if(temp != null) {
							exULs.add(temp);
						}
					}
					// We create new prefix pX
					itemsetBuffer[prefixLength] = X.item;
					// We make a recursive call to discover all itemsets with the prefix pXY
					phm(itemsetBuffer, prefixLength+1, X, exULs, minUtility); 
					
				} // end if
			}
		}
		MemoryLogger.getInstance().checkMemory();
	}
	
	/**
	 * This method constructs the utility list of pXY
	 * @param P :  the utility list of prefix P.
	 * @param px : the utility list of pX
	 * @param py : the utility list of pY
	 * @return the utility list of pXY
	 */
	private UtilityListPHM construct(UtilityListPHM P, UtilityListPHM px, UtilityListPHM py, int minUtility) {
		// create an empy utility list for pXY
		UtilityListPHM pxyUL = new UtilityListPHM(py.item);

		// ********** PHM *************
		int lastTid = -1;  // IMPORTANT BECAUSE TIDS STARTS AT ZERO...!!
//		if(P==null && px.item == 4 && py.item == 2){
//			System.out.println("DEBUG");
//		}
		// ********** END PHM *************
		
		//== new optimization - LA-prune  == /
		// Initialize the sum of total utility
		long totalUtility = px.sumIutils + px.sumRutils;
		// A similar strategy to LA-prune will be applied for the support
		// Initialize the sum of support
		long totalSupport = px.getSupport();
		// ================================================
		
		// for each element in the utility list of pX
		for(Element ex : px.elements){
			// do a binary search to find element ey in py with tid = ex.tid
			Element ey = findElementWithTID(py, ex.tid);
			if(ey == null){
				//== new optimization - LA-prune == /
				if(ENABLE_LA_PRUNE) {
					totalUtility -= (ex.iutils+ex.rutils);
					if(totalUtility < minUtility) {
						return null;
					}
					// decrease the support by one transaction
					totalSupport -= 1;
					if(totalSupport < supportPruningThreshold) {
						return null;
					}
				}
				// =============================================== /
				continue;
			}
			// if the prefix p is null
			if(P == null){
				// ********** PHM *************
				// check the periodicity
				int periodicity = ex.tid - lastTid;
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
				lastTid = ex.tid;
				
				// IMPORTANT DO NOT COUNT THE FIRST PERIOD FOR MINIMUM UTILITY
				if(pxyUL.elements.size() > 0 && periodicity < pxyUL.smallestPeriodicity){
					pxyUL.smallestPeriodicity = periodicity;
				}
				
				// ********** END PHM *************
				
				// Create the new element
				Element eXY = new Element(ex.tid, ex.iutils + ey.iutils, ey.rutils);
				// add the new element to the utility list of pXY
				pxyUL.addElement(eXY);
				
			}else{
				// find the element in the utility list of p wih the same tid
				Element e = findElementWithTID(P, ex.tid);
				if(e != null){
					// ********** PHM *************
					// check the periodicity
					int periodicity = ex.tid - lastTid;
					if(periodicity > maxPeriodicity){
						return null;
					}
					if(periodicity >= pxyUL.largestPeriodicity){
						pxyUL.largestPeriodicity = periodicity;
					}
					lastTid = ex.tid;
					
					// IMPORTANT DO NOT COUNT THE FIRST PERIOD FOR MINIMUM UTILITY
					if(pxyUL.elements.size() > 0 && periodicity < pxyUL.smallestPeriodicity){
						pxyUL.smallestPeriodicity = periodicity;
					}
					// ********** END PHM *************
					
					// Create new element
					Element eXY = new Element(ex.tid, ex.iutils + ey.iutils - e.iutils,
								ey.rutils);
					// add the new element to the utility list of pXY
					pxyUL.addElement(eXY);
				}
			}	
		}
		
		// ********** PHM *************
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
	private Element findElementWithTID(UtilityListPHM ulist, int tid){
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
	 * @param prefixLength the prefix length
	 * @param utilityList the utility list
	 * @param averagePeriodicity the average periodicity
	 */
	private void writeOut(int[] prefix, int prefixLength, UtilityListPHM utilityList, double averagePeriodicity) throws IOException {
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
		buffer.append(" #UTIL: ");
		buffer.append(utilityList.sumIutils);
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
		if(DEBUG && ENABLE_EUCP) {
			System.out.println("===== CONTENT OF EUCP =====");
			for(Entry<Integer, Map<Integer, Long>> entry : mapEUCS.entrySet()) {
				System.out.print("Item:" + entry.getKey()  + " -- ");
				for(Entry<Integer, Long> entry2 :entry.getValue().entrySet()) {
					System.out.print(entry2.getKey()  + " (" + entry2.getValue() + ")  ");
				}
				System.out.println();
			}
		}
		
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
	
		
		String optimizationEUCP = ENABLE_EUCP ? " EUCP: true -" : " EUCP: false -";
		String optimizationESCP = ENABLE_ESCP ? " ESCP: true " : " ESCP: false ";
		System.out.println("=============  PHM ALGORITHM v2.17" 
		+ optimizationEUCP + optimizationESCP + "=====");
		System.out.println(" Database size: "                      + (databaseSize)  + " transactions");
		System.out.println(" Time : " + totalExecutionTime + " ms");
		System.out.println(" Memory ~ "                      + maximumMemoryUsage + " MB");
		System.out.println(" Periodic High-utility itemsets count : " + phuiCount); 
		System.out.println(" Candidate count : "             + candidateCount);
		
		if(DEBUG && ENABLE_EUCP) {
			int pairCount = 0;
			double maxMemory = getObjectSize(mapEUCS);
			for(Entry<Integer, Map<Integer, Long>> entry : mapEUCS.entrySet()) {
				maxMemory += getObjectSize(entry.getKey());
				for(Entry<Integer, Long> entry2 :entry.getValue().entrySet()) {
					pairCount++;
					maxMemory += getObjectSize(entry2.getKey()) + getObjectSize(entry2.getValue());
				}
			}
			System.out.println("EUCS size " + maxMemory + " MB    PAIR COUNT " + pairCount);
		}
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
     * This method let the user specify if the EUCP should be enabled.
     * @param enable if this parameter is true, EUCP will be enabled. Otherwise, not.
     */
	public void setEnableEUCP(boolean enable) {
		ENABLE_EUCP = enable;
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