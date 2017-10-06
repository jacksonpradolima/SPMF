package ca.pfv.spmf.algorithms.frequentpatterns.hui_miner;


/* This file is copyright (c) 2008-2015 Philippe Fournier-Viger
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
 * This is an implementation of the "FHM+" algorithm for High-Utility Itemsets Mining
 * as described in the conference paper : <br/><br/>
 * 
 *  	Fournier-Viger, P., Lin, C.W., Duong, Q.-H., Dam, T.-L. (2016). 
 *      FHM+: Faster High-Utility Itemset Mining using Length Upper-Bound Reduction. 
 *      Proc. 29th Intern. Conf. on Industrial, Engineering and Other Applications of 
 *      Applied Intelligent Systems (IEA AIE 2016), Springer LNAI, to appear
 *
 * @see UtilityListFHMPlus
 * @see ElementFHMPlus
 * @author Philippe Fournier-Viger, 2016
 */
public class AlgoFHMPlus {
	
	/** the time at which the algorithm started */
	public long startTimestamp = 0;  
	
	/** the time at which the algorithm ended */
	public long endTimestamp = 0; 
	
	/** the number of high-utility itemsets generated */
	public int huiCount =0; 
	
	/** the number of candidate high-utility itemsets */
	public int candidateCount =0;
	
	/** Map to remember the TWU of each item */
	Map<Integer, Long> mapItemToTWU;
	
	/** writer to write the output file  */
	BufferedWriter writer = null;  
	
	/** The eucs structure:  key: item   key: another item   value: twu */
	Map<Integer, Map<Integer, Long>> mapFMAP;  
	
	/** enable LA-prune strategy  */
	boolean ENABLE_LA_PRUNE = true;
	
	/** variable for debug mode */
	boolean DEBUG = false;
	
	/** the total execution time **/
	public double totalExecutionTime = 0;
	
	/** the maximumMemoryUsage **/
	public double maximumMemoryUsage = 0;
	
	
	/** buffer for storing the current itemset that is mined when performing mining
	* the idea is to always reuse the same buffer to reduce memory usage. */
	final int BUFFERS_SIZE = 200;
	private int[] itemsetBuffer = null;
	

	/** enable LA-prune strategy  */
	boolean ENABLE_NEW_CONSTRUCT = true;
	
	/** this variable indicate to use the alphabetical order instead of the ascending order of RTWU */
	boolean USE_ALPHABETICAL_ORDER = true;


	/// =================== BEGIN FHM-PLUS ===================
	/** the maximum pattern length in terms of number of items */
	private int maximumLength = Integer.MAX_VALUE;
	
	/** the maximum pattern length in terms of number of items */
	private int minimumLength = 1;
	
	/** the size of a buffer for storing a transaction */
	final int TRANSACTION_BUFFER = 3000;
	/** a buffer for storing utility values */
	private int[] utilitiesBuffer = new int[TRANSACTION_BUFFER];
	/** a buffer for storing pairs of the form (item, utility), in a transaction */
	private Pair[] sortedPairsBuffer = new Pair[TRANSACTION_BUFFER];
	/// =================== END FHM-PLUS ===================

	/** this class represent an item and its utility in a transaction */
	class Pair{
		int item = 0;
		int utility = 0;
	}
	
	/**
	 * Default constructor
	 */
	public  AlgoFHMPlus() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Run the algorithm
	 * @param input the input file path
	 * @param output the output file path
	 * @param minUtility the minimum utility threshold
	 * @param minimumLength the minimum pattern length (a positive integer)
	 * @param maximumLength the maximum pattern length (a positive integer)
	 * @throws IOException exception if error while writing the file
	 */
	public void runAlgorithm(String input, String output, int minUtility, int minimumLength, int maximumLength) throws IOException {
		// reset maximum
		MemoryLogger.getInstance().reset();
		
		// initialize the buffer for storing the current itemset
		itemsetBuffer = new int[BUFFERS_SIZE];

		this.minimumLength = minimumLength;
		this.maximumLength = maximumLength;
		
		mapFMAP =  new HashMap<Integer, Map<Integer, Long>>();
		
		startTimestamp = System.currentTimeMillis();
		
		writer = new BufferedWriter(new FileWriter(output));

		//  We create a  map to store the TWU of each item
		mapItemToTWU = new HashMap<Integer, Long>();

		// We scan the database a first time to calculate the TWU of each item.
		BufferedReader myInput = null;
		String thisLine;
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
				
				// split the transaction according to the : separator
				String split[] = thisLine.split(":"); 
//				// the first part is the list of items
				String items[] = split[0].split(" "); 
				
				/// =================== BEGIN FHM-PLUS ===================
				//  COPY THE UTILITIES OF ITEMS IN A TEMPORARY BUFFER
				int z =0;
				// for each utility value
				for(String utilityValue : split[2].split(" ")){
					// convert it to int
					utilitiesBuffer[z++] = Integer.parseInt(utilityValue);
				}
				// SORT THE UTILITIES BY ASCENDING ORDER
				Arrays.sort(utilitiesBuffer,0, items.length);

				// Calculate the revised TWU by making the sum of the first items in  the sorted
				// transaction
				int reducedTWU = 0;
				int startIndex = (items.length - 1) - maximumLength;
				if(startIndex < 0){
					startIndex = 0;
				}
				// calculate the sum
				for(int i = items.length - 1; i >= startIndex; i--){
					reducedTWU += utilitiesBuffer[i];
				}
				/// =================== END FHM-PLUS ==================)
				
				// for each item, we add the transaction utility to its TWU
				for(int i=0; i <items.length; i++){
					// convert item to integer
					Integer item = Integer.parseInt(items[i]);
					
					// get the current TWU of that item
					Long twu = mapItemToTWU.get(item);
					// add the utility of the item in the current transaction to its twu
					twu = (twu == null)? reducedTWU : twu + reducedTWU;
					mapItemToTWU.put(item, twu);
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
		
		// CREATE A LIST TO STORE THE UTILITY LIST OF ITEMS WITH TWU  >= MIN_UTILITY.
		List<UtilityListFHMPlus> listOfUtilityLists = new ArrayList<UtilityListFHMPlus>();
		// CREATE A MAP TO STORE THE UTILITY LIST FOR EACH ITEM.
		// Key : item    Value :  utility list associated to that item
		Map<Integer, UtilityListFHMPlus> mapItemToUtilityList = new HashMap<Integer, UtilityListFHMPlus>();
		
		// For each item
		for(Integer item: mapItemToTWU.keySet()){
			// if the item is promising  (TWU >= minutility)
			if(mapItemToTWU.get(item) >= minUtility){
				// create an empty Utility List that we will fill later.
				UtilityListFHMPlus uList = new UtilityListFHMPlus(item);
				
				// store the utility-list
				mapItemToUtilityList.put(item, uList);
				// add the item to the list of high TWU items
				listOfUtilityLists.add(uList); 
				
			}
		}
		// SORT THE LIST OF HIGH TWU ITEMS IN ASCENDING ORDER
		Collections.sort(listOfUtilityLists, new Comparator<UtilityListFHMPlus>(){
			public int compare(UtilityListFHMPlus o1, UtilityListFHMPlus o2) {
				// compare the TWU of the items
				return compareItems(o1.item, o2.item);
			}
			} );
		
//		System.out.println(mapItemToTWU);
		
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
				
				/// =================== BEGIN FHM-PLUS ===================
				//  COPY THE UTILITIES OF ITEMS IN A TEMPORARY BUFFER
				// for each utility value
				int newItemCount = 0;
				for(int i=0; i< items.length; i++){
					int item = Integer.parseInt(items[i]);
					// if the item is promising, we add it to the buffer for storing elements in the transactions
					// otherwise, we do not.
					if(mapItemToTWU.get(item) >= minUtility){
						// convert it to int
						Pair pair = new Pair();
						pair.utility = Integer.parseInt(utilityValues[i]);
						pair.item =  item;
						sortedPairsBuffer[newItemCount] = pair;
						newItemCount++;
					}
				}
				// SORT THE UTILITIES BY DESCENDING ORDER  (INSTEAD OF ASCENDING ORDER) 
				Arrays.sort(sortedPairsBuffer, 0, newItemCount, new Comparator<Pair>() {
					@Override
					public int compare(Pair o1, Pair o2) {
						int compare  = o2.utility - o1.utility;
						if(compare == 0){
							compare = o2.item - o1.item;
						}
						return compare;
					}
				});
				
				// RECOMPUTE THE TWU BECAUSE WE HAVE REMOVED ITEMS
				long newTWU = 0;
				for(int z =0; z < maximumLength && z < newItemCount; z++){
					newTWU += sortedPairsBuffer[z].utility;
				}

				// FOR EACH ITEM IN THE TRANSACTION
				for(int i =0; i < newItemCount; i++){
					// GET THAT ITEM
					Pair pair =  sortedPairsBuffer[i];
		

					// UPDATE THE UTILITY LIST OF THIS ITEM	
					// get the utility list of this item
					UtilityListFHMPlus utilityListOfItem = mapItemToUtilityList.get(pair.item);
					
					// Add a new Element to the utility list of this item corresponding to this transaction
					ElementFHMPlus element = new ElementFHMPlus(tid, pair.utility);
					int sizeRemainingArray = maximumLength - 1;
					// Initialize its remaining array
					element.remainingArray = new int[sizeRemainingArray];

					
					// Get the EUCS ENTRY for that item
					Map<Integer, Long> mapFMAPItem = mapFMAP.get(pair.item);
					if(mapFMAPItem == null) {
						mapFMAPItem = new HashMap<Integer, Long>();
						mapFMAP.put(pair.item, mapFMAPItem);
					}

					// update the remaining utility and EUCS at the same time
					int numberOfItemsCanExtendWhithinMaxLimit = 0;
					// Calculate the remaining utility
					for(int j  = 0; j < newItemCount; j++){
						Pair otherPair =  sortedPairsBuffer[j];
						// if the second item can extend the first item 
						// (is greater according to the total order of ascending TWU)
						if(compareItems(pair.item, otherPair.item) < 0){
							// if  we did not reach the maximum number of items, update the remaining utility
							if(numberOfItemsCanExtendWhithinMaxLimit != sizeRemainingArray){
								// add the utility of the second item to the remaining utility of the first item
								element.rutils += otherPair.utility;
//								utilityListOfItem.sumRutils += otherPair.utility;
								
								// update the remaining array
								element.remainingArray[numberOfItemsCanExtendWhithinMaxLimit] = otherPair.utility;
								numberOfItemsCanExtendWhithinMaxLimit++;
							}

							
							// UPDATE THE EUCS 
							Long twuSum = mapFMAPItem.get(otherPair.item);
							if(twuSum == null) {
								mapFMAPItem.put(otherPair.item, newTWU);
							}else {
								mapFMAPItem.put(otherPair.item, twuSum + newTWU);
							}
							// END OPTIMIZATION of FHM
						}
					}
					
					// add the element to the utility list
					utilityListOfItem.addElement(element);
				}
				
				/// =================== END FHM-PLUS ==================
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
		MemoryLogger.getInstance().checkMemory();
		
		/// =================== BEGIN FHM-PLUS ==================
		// save high-utility itemsets of size 1
		for(UtilityListFHMPlus X : listOfUtilityLists){
//			System.out.println(X.item );
//			for(ElementFHMPlus e:  X.elements){
//				System.out.println(" TID: " + e.tid + " IUTIL: " + e.iutils);
//				System.out.println("    :   RUTIL: " + e.rutils);
//				System.out.println("    :   RARRAY: " + Arrays.toString(e.remainingArray));
//			}
			// If pX is a high utility itemset.
			// we save the itemset:  pX 
			if(X.sumIutils >= minUtility){
				// save to file
				writeOut(itemsetBuffer, 0, X.item, X.sumIutils);
			}
		}
		/// =================== END FHM-PLUS ==================
	

		// Mine the database recursively
		/// =================== BEGIN FHM-PLUS ==================
		if(maximumLength > 1){
			fhm(itemsetBuffer, 0, null, listOfUtilityLists, minUtility);
		}
		/// =================== END FHM-PLUS ==================
		
		// check the memory usage again and close the file.
		MemoryLogger.getInstance().checkMemory();
		
		// close output file
		writer.close();
		// record end time
		endTimestamp = System.currentTimeMillis();
		maximumMemoryUsage = MemoryLogger.getInstance().getMaxMemory();
		totalExecutionTime = endTimestamp - startTimestamp;
	}
	
	/**
	 * Method to compare items by their TWU
	 * @param item1 an item
	 * @param item2 another item
	 * @return 0 if the same item, >0 if item1 is larger than item2,  <0 otherwise
	 */
	private int compareItems(int item1, int item2) {
		// if we use the ascending order of RTWU as total order on items
		if(USE_ALPHABETICAL_ORDER == false){
			int compare = (int)( mapItemToTWU.get(item1) - mapItemToTWU.get(item2));
			// if the same, use the lexical order otherwise use the TWU
			return (compare == 0)? item1 - item2 :  compare;
		}else{
			// if we use the alphabetical order as total order on itemsU
			return item1 - item2;
		}
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
	private void fhm(int [] prefix, int prefixLength, UtilityListFHMPlus pUL, List<UtilityListFHMPlus> ULs, int minUtility)
			throws IOException {
		
		// For each extension X of prefix P
		for(int i=0; i< ULs.size(); i++){
			UtilityListFHMPlus X = ULs.get(i);

			// If the sum of the remaining utilities for pX
			// is higher than minUtility, we explore extensions of pX.
			// (this is the pruning condition)
			if(X.sumIutils + X.sumRutils >= minUtility){

				// We create new prefix pX
				itemsetBuffer[prefixLength] = X.item;
				
				// This list will contain the utility lists of pX extensions.
				List<UtilityListFHMPlus> exULs = new ArrayList<UtilityListFHMPlus>();
				
				// For each extension of p appearing
				// after X according to the ascending order
				for(int j=i+1; j < ULs.size(); j++){
					UtilityListFHMPlus Y = ULs.get(j);
					
					// ======================== NEW OPTIMIZATION USED IN FHM
					Map<Integer, Long> mapTWUF = mapFMAP.get(X.item);
					if(mapTWUF != null) {
						Long twuF = mapTWUF.get(Y.item);
						if(twuF == null || twuF < minUtility) {
							continue;
						}
					}
					candidateCount++;
					// =========================== END OF NEW OPTIMIZATION
					
					// we construct the extension pXY 
					// and add it to the list of extensions of pX

					UtilityListFHMPlus temp;
					if(ENABLE_NEW_CONSTRUCT){
						temp = construct(pUL, X, Y, minUtility, prefixLength+2);
					}else{
						temp = oldConstruct(pUL, X, Y, minUtility, prefixLength+2);
					}
					
					if(temp != null) {

						/// =================== BEGIN FHM-PLUS ==================
						// If pX is a high utility itemset.
						// we save the itemset:  pX 
						if(temp.sumIutils >= minUtility){
							// save to file
							writeOut(itemsetBuffer, prefixLength+1, temp.item, temp.sumIutils);
						}
						if(prefixLength+2 != maximumLength){
							exULs.add(temp);
						}
						/// =================== END FHM-PLUS ==================
					}
				}
				// We make a recursive call to discover all itemsets with the prefix pXY
				/// =================== BEGIN FHM-PLUS ==================
				if(prefixLength+2 != maximumLength){
					fhm(itemsetBuffer, prefixLength+1, X, exULs, minUtility); 
				}
				/// =================== END FHM-PLUS ==================
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
	private UtilityListFHMPlus construct(UtilityListFHMPlus P, UtilityListFHMPlus px, UtilityListFHMPlus py, int minUtility, int pxyLength) {
		// create an empy utility list for pXY
		UtilityListFHMPlus pxyUL = new UtilityListFHMPlus(py.item);
		
		//== new optimization - LA-prune  == /
		// Initialize the sum of total utility
		long totalUtility = px.sumIutils + px.sumRutils;
		// ================================================
		
		// for each element in the utility list of pX
		for(ElementFHMPlus ex : px.elements){
			// do a binary search to find element ey in py with tid = ex.tid
			ElementFHMPlus ey = findElementWithTID(py, ex.tid);
			if(ey == null){
				//== new optimization - LA-prune == /
				if(ENABLE_LA_PRUNE) {
					totalUtility -= (ex.iutils+ex.rutils);
					if(totalUtility < minUtility) {
						return null;
					}
				}
				continue;
			}
			
			// ======================= BEGIN FHMPlus ==================
			ElementFHMPlus eXY = null;
			if(P == null){
				eXY = new ElementFHMPlus(ex.tid, ex.iutils + ey.iutils);
				// calculate remaining size 
				int newRemainingArraySize = 0;
				for(int z =0; z < ey.remainingArray.length; z++){
					if(ey.remainingArray[z] != 0){
						newRemainingArraySize++;
					}else{
						break;
					}
				}
				if((newRemainingArraySize + pxyLength) > (maximumLength)){
					newRemainingArraySize--;
					if(newRemainingArraySize == -1){
						newRemainingArraySize = 0;
					}
				}
				
				eXY.remainingArray = new int[newRemainingArraySize];
				for(int i=0; i< newRemainingArraySize; i++){
					if(ey.remainingArray[i] == 0){
						break;
					}
					eXY.remainingArray[i] = ey.remainingArray[i];
					eXY.rutils += ey.remainingArray[i];
				}
				
			}else{

				// find the element in the utility list of p wih the same tid
				ElementFHMPlus e = findElementWithTID(P, ex.tid);
				if(e != null){
					// Create new element
					eXY = new ElementFHMPlus(ex.tid, ex.iutils + ey.iutils - e.iutils);
					
					int newRemainingArraySize = ey.remainingArray.length;
					if((newRemainingArraySize + pxyLength) > (maximumLength)){
						newRemainingArraySize--;
						if(newRemainingArraySize == -1){
							newRemainingArraySize = 0;
						}
					}
					
					eXY.remainingArray = new int[newRemainingArraySize];
					for(int i=0; i< newRemainingArraySize; i++){
						if(ey.remainingArray[i] == 0){
							break;
						}
						eXY.remainingArray [i] = ey.remainingArray[i];
						eXY.rutils += ey.remainingArray[i];
					}
				}
				
			}
			// add the new element to the utility list of pXY
			pxyUL.addElement(eXY);
			// ======================= END FHMPlus ==================
		}
		// return the utility list of pXY.
		return pxyUL;
	}
	
	
	/**
	 * This method constructs the utility list of pXY
	 * @param P :  the utility list of prefix P.
	 * @param px : the utility list of pX
	 * @param py : the utility list of pY
	 * @return the utility list of pXY
	 */
	private UtilityListFHMPlus oldConstruct(UtilityListFHMPlus P, UtilityListFHMPlus px, UtilityListFHMPlus py, int minUtility, int pxyLength) {
		// create an empy utility list for pXY
		UtilityListFHMPlus pxyUL = new UtilityListFHMPlus(py.item);
		
		//== new optimization - LA-prune  == /
		// Initialize the sum of total utility
		long totalUtility = px.sumIutils + px.sumRutils;
		// ================================================
		
		// for each element in the utility list of pX
		for(ElementFHMPlus ex : px.elements){
			// do a binary search to find element ey in py with tid = ex.tid
			ElementFHMPlus ey = findElementWithTID(py, ex.tid);
			if(ey == null){
				//== new optimization - LA-prune == /
				if(ENABLE_LA_PRUNE) {
					totalUtility -= (ex.iutils+ex.rutils);
					if(totalUtility < minUtility) {
						return null;
					}
				}
				// =============================================== /
				continue;
			}
			// if the prefix p is null
			if(P == null){
				// Create the new element
				ElementFHMPlus eXY = new ElementFHMPlus(ex.tid, ex.iutils + ey.iutils, ey.rutils);
				// add the new element to the utility list of pXY
				pxyUL.addElement(eXY);
				
			}else{
				// find the element in the utility list of p wih the same tid
				ElementFHMPlus e = findElementWithTID(P, ex.tid);
				if(e != null){
					// Create new element
					ElementFHMPlus eXY = new ElementFHMPlus(ex.tid, ex.iutils + ey.iutils - e.iutils,
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
	private ElementFHMPlus findElementWithTID(UtilityListFHMPlus ulist, int tid){
		List<ElementFHMPlus> list = ulist.elements;
		
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
	 * @param utility the utility of the prefix concatenated with the item
	 * @param prefixLength the prefix length
	 */
	private void writeOut(int[] prefix, int prefixLength, int item, long utility) throws IOException {
		// if this pattern does not respect the minimum length constraint, then we don't output it.
		if(prefixLength +1 < minimumLength){
			return;
		}
		
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
	 * Print statistics about the latest execution to System.out.
	 * @throws IOException 
	 */
	public void printStats() throws IOException {
		System.out.println("=============  FHM+ ALGORITHM - SPMF 0.99c - STATS =============");
		System.out.println(" Total time ~ "                  + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Memory ~ "                      + MemoryLogger.getInstance().getMaxMemory()  + " MB");
		System.out.println(" High-utility itemsets count : " + huiCount); 
		System.out.println(" Candidate count : "             + candidateCount);
		
		if(DEBUG) {
			int pairCount = 0;
			double maxMemory = getObjectSize(mapFMAP);
			for(Entry<Integer, Map<Integer, Long>> entry : mapFMAP.entrySet()) {
				maxMemory += getObjectSize(entry.getKey());
				for(Entry<Integer, Long> entry2 :entry.getValue().entrySet()) {
					pairCount++;
					maxMemory += getObjectSize(entry2.getKey()) + getObjectSize(entry2.getValue());
				}
			}
			System.out.println("CMAP size " + maxMemory + " MB");
			System.out.println("PAIR COUNT " + pairCount);
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
}