package ca.pfv.spmf.algorithms.frequentpatterns.ulbminer;

/* This file is copyright (c) 2008-2018 Philippe Fournier-Viger
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

import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.UtilityList;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This is an implementation of the "ULB-Miner" algorithm for High-Utility Itemsets Mining
 * as described in the conference paper : <br/><br/>
 * 
 * Duong, Q.H., Fournier-Viger, P., Ramampiaro, H., Norvag, K. Dam, T.-L. (2017). 
 * Effcient High Utility Itemset Mining using Buffered Utility-Lists. Applied Intelligence, 
 * Springer (to appear)
 * 
 * @see UtilityList
 * @see Element
 * @author Philippe Fournier-Viger & Q. H. Duong
 */
public class AlgoULBMiner {
	
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
	
	/** Map to remember the Support of each item */
	Map<Integer, Integer> mapItemToSupport;
	
	/** writer to write the output file  */
	BufferedWriter writer = null;  
	
	/** The eucs structure:  key: item   key: another item   value: twu */
	Map<Integer, Map<Integer, Long>> mapFMAP;  
	
	/** enable LA-prune strategy  */
	boolean ENABLE_LA_PRUNE = true;
	
	/** variable for debug mode */
	boolean DEBUG = false;
	
	/** buffer for storing the current itemset that is mined when performing mining
	* the idea is to always reuse the same buffer to reduce memory usage. */
	final int BUFFERS_SIZE = 200;
	private int[] itemsetBuffer = null;
	
	/**  Buffer to store utility-lists and reuse the memory */
	private UtilityListBuffer utilityListBuffer = null;
	
	/** this class represent an item and its utility in a transaction */
	class Pair{
		int item = 0;
		int utility = 0;
	}
	
	/**
	 * Default constructor
	 */
	public AlgoULBMiner() {
		
	}

	/**
	 * Run the algorithm
	 * @param input the input file path
	 * @param output the output file path
	 * @param minUtility the minimum utility threshold
	 * @throws IOException exception if error while writing the file
	 */
	public void runAlgorithm(String input, String output, int minUtility) throws IOException {
		// reset maximum
		MemoryLogger.getInstance().reset();
		
		// initialize the buffer for storing the current itemset
		itemsetBuffer = new int[BUFFERS_SIZE];
		
		
		mapFMAP =  new HashMap<Integer, Map<Integer, Long>>();
		
		startTimestamp = System.currentTimeMillis();
		
		writer = new BufferedWriter(new FileWriter(output));

		//  We create a  map to store the TWU of each item
		mapItemToTWU = new HashMap<Integer, Long>();

		//  We create a  map to store the support of each item
		mapItemToSupport = new HashMap<Integer, Integer>();

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
				// the first part is the list of items
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
					
					// add the support of the item in the current transaction to its twu
					Integer support = mapItemToSupport.get(item);
					if(support == null){
						support = 1;
					}else{
						support++;
					}
					mapItemToSupport.put(item, support);
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
		
		int sumSupport = 0;
		
		// CREATE A LIST OF PROMISING ITEMS
		List<Integer> promisingItems = new ArrayList<Integer>();
		// For each item
		for(Integer item: mapItemToTWU.keySet()){
			// if the item is promising  (TWU >= minutility)
			if(mapItemToTWU.get(item) >= minUtility){
				promisingItems.add(item);
				sumSupport += mapItemToSupport.get(item);
			}
		}

		// initialize the utility-list buffer
		utilityListBuffer = new UtilityListBuffer(sumSupport, promisingItems.size());
		
		// SORT PROMISING ITEMS BY TWU ASCENDING ORDER
		Collections.sort(promisingItems, new Comparator<Integer>(){
			public int compare(Integer o1, Integer o2) {
				// compare the TWU of the items
				return compareItems(o1, o2);
			}
			});
		
		// CREATE A LIST TO STORE THE UTILITY LIST OF ITEMS WITH TWU  >= MIN_UTILITY.
		// CREATE A MAP TO STORE THE UTILITY LIST FOR EACH ITEM.
		// Key : item    Value :  start position of the utility-list
		Map<Integer, Integer> mapItemToUtilityList = new HashMap<Integer, Integer>();
		int endPosition = 0;
		
		// For each item
		for(Integer item: promisingItems){
			int support = mapItemToSupport.get(item);
			
			// CREATE THE UTILITY-LIST
			utilityListBuffer.createANewUtilityList(item, endPosition);
			// ALLOCATE THE SPACE FOR STORING THE ELEMENTS OF THAT UTILITY-LIST
			// For the current item, we know that the number of tids will be equal to its support.
			// So we allocate "support" elements for its utility list.
			utilityListBuffer.allocateSpaceForElements(support);
			
			// REMEMBER THE POSITION OF THAT UTILITY-LIST
			mapItemToUtilityList.put(item, endPosition);
			
			// INCREASE THEPOSITION FOR THE NEXT UTILITY-LIST
			endPosition++;
		}
		
		mapItemToSupport = null;

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
					// if the item has enough utility
					if(mapItemToTWU.get(pair.item) >= minUtility){
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
//						return o1.item - o2.item;
					}});

								
				// for each item left in the transaction
				for(int i = 0; i< revisedTransaction.size(); i++){
					Pair pair =  revisedTransaction.get(i);

					// subtract the utility of this item from the remaining utility
					remainingUtility = remainingUtility - pair.utility;
					
					// get the utility list of this item
					Integer utilityListPosition = mapItemToUtilityList.get(pair.item);
					utilityListBuffer.selectCurrentUtilityList(utilityListPosition);
					
					// Add a new Element to the utility list of this item corresponding to this transaction
					utilityListBuffer.addElementToCurrentUtilityList(tid, pair.utility, remainingUtility);
										
					// BEGIN NEW OPTIMIZATION for FHM
					Map<Integer, Long> mapFMAPItem = mapFMAP.get(pair.item);
					if(mapFMAPItem == null) {
						mapFMAPItem = new HashMap<Integer, Long>();
						mapFMAP.put(pair.item, mapFMAPItem);
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
	
		// check the memory usage
		MemoryLogger.getInstance().checkMemory();
		
		utilityListBuffer.finishBuildingSingleItemsUtilityLists();
		
		// FOR DEBUGGING.  TO SEE THE UTILITY-BUFFER
//		utilityListBuffer.printToString();
		mapItemToTWU = null;

		// Mine the database recursively
		fhm(itemsetBuffer, 0, -1, 0, endPosition, minUtility);
		
		// check the memory usage again and close the file.
		MemoryLogger.getInstance().checkMemory();
		// close output file
		writer.close();
		
		// free memory from the utility-list buffer
		utilityListBuffer = null;
		
		// record end time
		endTimestamp = System.currentTimeMillis();
	}
	
	/**
	 * Method to compare items by their TWU
	 * @param item1 an item
	 * @param item2 another item
	 * @return 0 if the same item, >0 if item1 is larger than item2,  <0 otherwise
	 */
	private int compareItems(int item1, int item2) {
		int compare = (int)( mapItemToTWU.get(item1) - mapItemToTWU.get(item2));
//		// if the same, use the lexical order otherwise use the TWU
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
	private void fhm(int [] prefix,
			int prefixLength, int pULPosition, int previousStartPosition, 
			int previousEndPosition, int minUtility)
			throws IOException {
		
		// For each extension X of prefix P
		for(int X = previousStartPosition; X< previousEndPosition; X++){

			// Select the X utility-list
			utilityListBuffer.selectCurrentUtilityList(X);
			int sumIutils = utilityListBuffer.getSumIUtilCurrentUtilityList();
			int sumRutils = utilityListBuffer.getSumRUtilCurrentUtilityList();
			int itemX = utilityListBuffer.getItemCurrentUtilityList();

			// If pX is a high utility itemset.
			// we save the itemset:  pX 
			if(sumIutils >= minUtility){
				// save to file
				writeOut(prefix, prefixLength, itemX, sumIutils);
			}
			
			// If the sum of the remaining utilities for pX
			// is higher than minUtility, we explore extensions of pX.
			// (this is the pruning condition)
			if(sumIutils + sumRutils >= minUtility){
				// This list will contain the utility lists of pX extensions.
				int newStartPosition = previousEndPosition;
				int newEndPosition = previousEndPosition;
				
				// For each extension of p appearing
				// after X according to the ascending order
				for(int Y=X+1; Y < previousEndPosition; Y++){
					// Select the Y utility-list
					utilityListBuffer.selectCurrentUtilityList(Y);
					int itemY = utilityListBuffer.getItemCurrentUtilityList();

					
					// ======================== NEW OPTIMIZATION USED IN FHM
					Map<Integer, Long> mapTWUF = mapFMAP.get(itemX);
					if(mapTWUF != null) {
						Long twuF = mapTWUF.get(itemY);
						if(twuF == null || twuF < minUtility) {
							continue;
						}
					}
					candidateCount++;
					// =========================== END OF NEW OPTIMIZATION
					
					// we construct the extension pXY 
					// and add it to the list of extensions of pX
					boolean succeeded = construct(pULPosition, X, Y, minUtility, newEndPosition, itemY, sumIutils+ sumRutils);
					if(succeeded == true) {
						newEndPosition++;
					}
				}
				// We create new prefix pX
				itemsetBuffer[prefixLength] = itemX;
				// We make a recursive call to discover all itemsets with the prefix pXY
				fhm(itemsetBuffer, prefixLength+1, X, newStartPosition, newEndPosition, minUtility); 
			}
		}
		MemoryLogger.getInstance().checkMemory();
	}
	
	private boolean construct(int PPosition, int pXPosition, int pYPosition, int minUtility, int endPosition, int itemY, int totalUtility) {
		// create an empty utility list for pXY
		utilityListBuffer.createANewUtilityList(itemY, endPosition);	
		
		utilityListBuffer.selectCurrentUtilityList(pXPosition);	
		int countX = utilityListBuffer.getElementCountCurrentUtilityList();
		
		utilityListBuffer.selectCurrentUtilityList(pYPosition);	
		int countY = utilityListBuffer.getElementCountCurrentUtilityList();
		
		int countP = 0;
		if(PPosition >=0){
			utilityListBuffer.selectCurrentUtilityList(PPosition);
			countP = utilityListBuffer.getElementCountCurrentUtilityList();
		}
		int posX = 0;
	    int posY = 0;
	    int posP = 0;
	    /*
	     * To specify the common of 3 sorted arrays, we use 3 index pointers. Not using binary search as original methods
	     * The Steps are follows.
	     * 1. Starting with 3 pointers point to the first index of arrays.
	     * 2. 2 pointers of 2 arrays move step by step until the values in 2 arrays are equal.
	     * 3. The 3rd pointer then move to the index having tid equal to the current tid in 2 arrays above
	     * 4. Loop will be terminated when 2 pointers point to the last position in arrays
	     */
	    while(posX < countX && posY < countY) {
	    	utilityListBuffer.selectCurrentUtilityList(pXPosition);
			Element ex = utilityListBuffer.getIthElementInCurrentUtilityList(posX);
			
			utilityListBuffer.selectCurrentUtilityList(pYPosition);			
			Element ey = utilityListBuffer.getIthElementInCurrentUtilityList(posY);
			
	    	if(ex.tid < ey.tid) {
	    		if(ENABLE_LA_PRUNE) {
					totalUtility -= (ex.iutils+ex.rutils);
					if(totalUtility < minUtility) {						
						return false;
					}
				}
	    		posX++;
	    	}else if(ex.tid > ey.tid) {	    		
	    		posY++;
	    	}else { 
	    		int epIutil = 0;
	    		if (PPosition >=0)
	    		{
		    		utilityListBuffer.selectCurrentUtilityList(PPosition);			
					Element eP = utilityListBuffer.getIthElementInCurrentUtilityList(posP);
				
		    		while(posP < countP && eP.tid < ex.tid) {
		    			posP++;
		    			eP = utilityListBuffer.getIthElementInCurrentUtilityList(posP);
		    		}
		    		epIutil = (eP != null) ? eP.iutils: 0; 
	    		}
	    		utilityListBuffer.selectCurrentUtilityList(endPosition);
				utilityListBuffer.addElementToCurrentUtilityList(ex.tid, ex.iutils + ey.iutils - epIutil,
							ey.rutils);
	    		posX++;
	    		posY++;
	    	}
	    }
	    return true;
		
	}
//	
//	/**
//	 * This method constructs the utility list of pXY
//	 * @param P :  the utility list of prefix P.
//	 * @param px : the utility list of pX
//	 * @param py : the utility list of pY
//	 * @param totalUtility : the sum of iutils and rutils
//	 * @return true if the utility list of pXY has been constructed sucessfully
//	 */
//	private boolean construct(int PPosition, int pXPosition, int pYPosition, int minUtility, int endPosition, int itemY, int totalUtility) {
//		// create an empty utility list for pXY
//		utilityListBuffer.createANewUtilityList(itemY, endPosition);
//		
//		//== new optimization - LA-prune  == /
//		// Initialize the sum of total utility
////		long totalUtility = sumIutilRutilX;
//		
//		utilityListBuffer.selectCurrentUtilityList(pXPosition);
//		int elementCountX = utilityListBuffer.getElementCountCurrentUtilityList();
//		
//		// for each element in the utility list of pX
//		for(int i =0; i < elementCountX; i++){
//			utilityListBuffer.selectCurrentUtilityList(pXPosition);
//			Element ex = utilityListBuffer.getIthElementInCurrentUtilityList(i);
//
//			// do a binary search to find element ey in py with tid = ex.tid
//			utilityListBuffer.selectCurrentUtilityList(pYPosition);
//			Element ey = utilityListBuffer.findElementWithTIDCurrenUtilityList(ex.tid);
//			if(ey == null){ 
//				//== new optimization - LA-prune == /
//				if(ENABLE_LA_PRUNE) {
//					totalUtility -= (ex.iutils+ex.rutils);
//					if(totalUtility < minUtility) {
//						return false;
//					}
//				}
//				// =============================================== /
//				continue;
//			}
//			
//			// if the prefix p is null
//			if(PPosition <0){
//				// Create the new element
//				utilityListBuffer.selectCurrentUtilityList(endPosition);
//				utilityListBuffer.addElementToCurrentUtilityList(ex.tid, ex.iutils + ey.iutils, ey.rutils);
//				
//			}else{
//				
//				// find the element in the utility list of p wih the same tid
//				utilityListBuffer.selectCurrentUtilityList(PPosition);
//				Element ep = utilityListBuffer.findElementWithTIDCurrenUtilityList(ex.tid);
//				int epIutil = (ep != null) ? ep.iutils: 0;
//				
//				// Create new element
//				utilityListBuffer.selectCurrentUtilityList(endPosition);
//				utilityListBuffer.addElementToCurrentUtilityList(ex.tid, ex.iutils + ey.iutils - epIutil,
//							ey.rutils);
//			}	
//		}
//		// return the utility list of pXY.
//		return true;
//	}


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
//		System.out.println(" OUT :" + buffer.toString());
	}

	
	/**
	 * Print statistics about the latest execution to System.out.
	 * @throws IOException 
	 */
	public void printStats() throws IOException {
		System.out.println("=============  ULB-Miner ALGORITHM - SPMF 0.2.19 - STATS =============");
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