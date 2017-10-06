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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ca.pfv.spmf.algorithms.ArraysAlgos;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This is an implementation of the "MinFHM" algorithm for Minimal High-Utility Itemsets Mining
 * as described in the conference paper : <br/><br/>
 * 
 *  Fournier-Viger, P., Lin, C.W., Wu, C.-W., Tseng, V. S., Faghihi, U. (2016). Mining Minimal High-Utility Itemsets. 
 *  Proc. 27th Intern. Conf. on Database and Expert Systems Applications (DEXA 2016). Springer, LNCS, 13 pages, to appear
 *
 * @see UtilityList
 * @see Element
 * @author Philippe Fournier-Viger
 */
public class AlgoMinFHM {

	// variable for statistics 
	/** the time the algorithm started */
	public long startTimestamp = 0;   
	/** the time the algorithm terminated*/ 
	public long endTimestamp = 0;    
	/** the number of HUI generated */
	public int huiCount =0;  
	
	/** the number of candidates **/
	public int candidateCount =0;
	
	/** Map to remember the TWU of each item **/
	Map<Integer, Integer> mapItemToTWU;
	
	/** writer to write the output file  */
	BufferedWriter writer = null;  
	
	/** the EUCP structure */
	Map<Integer, Map<Integer, Integer>> mapFMAP;  // PAIR OF ITEMS , item --> item, twu
	
	/** variable to activate the debug mode */
	boolean debug = false;
	
	/** if true, enable the LA-PRUNE optimization*/
	boolean ENABLE_LA_PRUNE = true;
	
	/** The structure called the "itemset store" in the paper */
	List<List<Itemset>> listItemsetsBySize = null;

	/**
	 * Check if there exists an itemset smaller than a given itemset in the MinHUI-Store
	 * @param itemset the given itemset
	 * @return true if there is a smaller itemset
	 */
	public boolean isSubsumingAFoundItemset(int [] itemset) {
		// THIS IS AN OPTIMIZATION.... SINCE ITEMSETS OF SIZE 1 ARE NOT IN THE MINHUI-STORE,
		// WE DON'T NEED TO CHECK FOR ITEMSETS OF SIZE 2
		if(itemset.length == 2) {
			return false;
		}
		/////  END OF OPTIMIZATION
//
//		System.out.println("IS SUBSUMED? " + Arrays.toString(itemset));
//		System.out.println();
		
		// WE CHECK STARTING FROM SMALLEST ITEMS
		for(int i=0; i < itemset.length	&& i < listItemsetsBySize.size(); i++) {    // IMPORTANT -1
			List<Itemset> list = listItemsetsBySize.get(i);
			if(list.size() > 0) {
				for(Itemset itemsetInList : list) {
					if(ArraysAlgos.includedIn(itemsetInList.itemset, itemset)){
						return true;
					}
				}
			}
		}
		return false;
	}

	
	// store a k-itemset  (k > 1)
	/**
	 * This method insert an itemset of size k > 1 in the store  and remove its supersets
	 * @param itemset the itemset
	 * @param utility its utility
	 * @param support its support
	 */
	public void registerItemsetAndRemoveLarger(int [] itemset, long utility, int support) {
//		// OPTIMIZATION: if it is an itemset of size 2, we set the pair to ZERO in the EUCS ===========
		if(itemset.length == 2) {
			mapFMAP.get(itemset[0]).put(itemset[1], 0);
		}
//		/// END OF OPTIMIZATION =======================
		
		// ADD THE ITEMSET
//		System.out.println("Register " + Arrays.toString(itemset));
//		System.out.println();
		
		// create the lists for storing itemsets in the HUI-store structure if they
		// don't exist already
		if(itemset.length >= listItemsetsBySize.size()) {
			int i= listItemsetsBySize.size();
			while(i <= itemset.length) {
				listItemsetsBySize.add(new ArrayList<Itemset>());
				i++;
			}
		}
		
		// add the itemset in the list of itemsets having the same size
		List<Itemset> listToAdd = listItemsetsBySize.get(itemset.length);
		listToAdd.add(new Itemset(itemset, utility, support));
		
		// REMOVE ALL SUPERSETS of the inserted itemset
		for(int i=itemset.length+1; i < listItemsetsBySize.size(); i++) {    // IMPORTANT -1
			List<Itemset> list = listItemsetsBySize.get(i);
			if(list.size() > 0) {
				Iterator<Itemset> iter = list.iterator();
				while (iter.hasNext()) {
					Itemset itemset2 = (Itemset) iter.next();
					if(ArraysAlgos.includedIn(itemset, itemset2.itemset)){
						iter.remove();
					}
				}
			}
		}
	}

	// ======================================================
	// ======================================================
	
	/** 
	 * This class represents an item and its utility in a transaction
	 * @author Philippe Fournier-Viger 2016
	 */
	class Pair{
		/** the item */
		int item = 0;
		/** its utility */
		int utility = 0;
		
		/**
		 * Get a string representation of this  item with utility
		 * @return a string
		 */
		public String toString() {
			return "[" + item + "," + utility + "]";
		}
	}
	
	/**
	 * Default constructor
	 */
	public AlgoMinFHM() {
		
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
		
		mapFMAP =  new HashMap<Integer, Map<Integer, Integer>>();
		
		startTimestamp = System.currentTimeMillis();
		
		writer = new BufferedWriter(new FileWriter(output));

		//  We create a  map to store the TWU of each item
		mapItemToTWU = new HashMap<Integer, Integer>();

		// Map to remember the utility of each item
		Map<Integer, Integer> mapItemToUtility = new HashMap<Integer, Integer>();

		listItemsetsBySize = new ArrayList<List<Itemset>>();
		
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
				// get the list of utility values corresponding to each item
				// for that transaction
				String utilityValues[] = split[2].split(" ");
				
				
				// for each item, we add the transaction utility to its TWU
				for(int i=0; i <items.length; i++){
					// convert item to integer
					Integer item = Integer.parseInt(items[i]);
					
					// UPDATE THE TWU ================
					// get the current TWU of that item
					Integer twu = mapItemToTWU.get(item);
					// add the utility of the item in the current transaction to its twu
					twu = (twu == null)? 
							transactionUtility : twu + transactionUtility;
					mapItemToTWU.put(item, twu);
					
					// UPDATE THE UTILITY ========================
					Integer utility = Integer.parseInt(utilityValues[i]);
					// get the current TWU of that item
					Integer totalUtilityOfItem = mapItemToUtility.get(item);
					
					// add the utility of the item in the current transaction to its twu
					totalUtilityOfItem = (totalUtilityOfItem == null)? 
							utility : utility + totalUtilityOfItem;
					mapItemToUtility.put(item, totalUtilityOfItem);
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
		List<UtilityList> listOfUtilityLists = new ArrayList<UtilityList>();
		// CREATE A MAP TO STORE THE UTILITY LIST FOR EACH ITEM.
		// Key : item    Value :  utility list associated to that item
		Map<Integer, UtilityList> mapItemToUtilityList = new HashMap<Integer, UtilityList>();
		
		// For each item
		for(Integer item: mapItemToTWU.keySet()){
			// if the item is promising  (TWU >= minutility)
			// and its utility is lower than minutil, we create the utility list of that item
			if(mapItemToTWU.get(item) >= minUtility && mapItemToUtility.get(item) < minUtility){
				// create an empty Utility List that we will fill later.
				UtilityList uList = new UtilityList(item);
				mapItemToUtilityList.put(item, uList);
				// add the item to the list of high TWU items
				listOfUtilityLists.add(uList); 
				
			}
		}
		// SORT THE LIST OF HIGH TWU ITEMS IN ASCENDING ORDER
		Collections.sort(listOfUtilityLists, new Comparator<UtilityList>(){
			public int compare(UtilityList o1, UtilityList o2) {
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
					if(mapItemToTWU.get(pair.item) >= minUtility && mapItemToUtility.get(pair.item) < minUtility){
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
										
					// subtract the utility of this item from the remaining utility
					remainingUtility = remainingUtility - pair.utility;
					
					// get the utility list of this item
					UtilityList utilityListOfItem = mapItemToUtilityList.get(pair.item);
					
					// Add a new Element to the utility list of this item corresponding to this transaction
					Element element = new Element(tid, pair.utility, remainingUtility);
					
					utilityListOfItem.addElement(element);
										
					// BEGIN NEW OPTIMIZATION for FHM
					Map<Integer, Integer> mapFMAPItem = mapFMAP.get(pair.item);
					if(mapFMAPItem == null) {
						mapFMAPItem = new HashMap<Integer, Integer>();
						mapFMAP.put(pair.item, mapFMAPItem);
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

		// ========================= SPECIFIC TO MMMINER ============
//		Iterator<UtilityList> iter = listOfUtilityLists.iterator();
//		while (iter.hasNext()) {
//			UtilityList ul = (UtilityList) iter.next();
//			// If pX is a high utility itemset.
//
//			if(ul.sumIutils >= minUtility){
//				int[] itemset = new int[] {ul.item};
//				registerSize1Itemset(itemset, ul.sumIutils, ul.elements.size());
//				iter.remove();
//			}
//		}
		// Write each single item high utility itemset to the file
		for(Entry<Integer,Integer> entry : mapItemToUtility.entrySet()) {
			// if it is a high utility item
			if(entry.getValue() >= minUtility) {
				writeOutItemsetSize1(entry.getKey(), entry.getValue());
			}
		}
		// ========================= END SPECIFIC TO MMMINER ============
		
		// Mine the database recursively
		minfhm(new int[0], null, listOfUtilityLists, minUtility);
		
		// SAVE ALL ITEMSETS TO THE FILE
		for(List<Itemset> listItemsets : listItemsetsBySize) {
			for(Itemset itemset : listItemsets) {
				writeOut(itemset);
			}
		}
		
		// check the memory usage again and close the file.
		MemoryLogger.getInstance().checkMemory();
		// close output file
		writer.close();
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
		int compare = mapItemToTWU.get(item1) - mapItemToTWU.get(item2);
		// if the same, use the lexical order otherwise use the TWU
		return (compare == 0)? item1 - item2 :  compare;
	}
	
	/**
	 * This is the recursive method to find all high utility itemsets. It writes
	 * the itemsets to the output file.
	 * @param prefix  This is the current prefix. Initially, it is empty.
	 * @param pUL This is the Utility List of the prefix. Initially, it is empty.
	 * @param ULs The utility lists corresponding to each extension of the prefix.
	 * @param minUtility The minUtility threshold.
	 * @throws IOException
	 */
	private void minfhm(int [] prefix, UtilityList pUL, List<UtilityList> ULs, int minUtility)
			throws IOException {
		
		// For each extension X of prefix P
		for(int i=0; i< ULs.size(); i++){
			UtilityList X = ULs.get(i);

			// If the sum of the remaining utilities for pX
			// is higher than minUtility, we explore extensions of pX.
			// (this is the pruning condition)
			if(X.sumIutils + X.sumRutils >= minUtility){
				
				int[] newPrefix = ArraysAlgos.appendIntegerToArray(prefix, X.item);
				
				// This list will contain the utility lists of pX extensions.
				List<UtilityList> exULs = new ArrayList<UtilityList>();
				// For each extension of p appearing
				// after X according to the ascending order
				for(int j=i+1; j < ULs.size(); j++){
					UtilityList Y = ULs.get(j);
					
					// ======================== NEW OPTIMIZATION USED IN FHM
					Map<Integer, Integer> mapTWUF = mapFMAP.get(X.item);
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
					UtilityList pXY = construct(pUL, X, Y, minUtility);
					
					//If the itemset pXY passes the LA-Prune strategy.
					if(pXY != null) {
						// If pX is a high utility itemset.
						// we save the itemset:  pX 
						int[] itemset = ArraysAlgos.appendIntegerToArray(newPrefix, Y.item);
						
						if(pXY.sumIutils >= minUtility && isSubsumingAFoundItemset(itemset) == false){
							registerItemsetAndRemoveLarger(itemset, pXY.sumIutils,  pXY.elements.size());
						}else if (isSubsumingAFoundItemset(itemset)){
							exULs.add(pXY);
						}
					}
				}
				
				// We make a recursive call to discover all itemsets with the prefix pXY
				if(exULs.size() > 1) {
					minfhm(newPrefix, X, exULs, minUtility); 
				}
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
	private UtilityList construct(UtilityList P, UtilityList px, UtilityList py, int minUtility) {
		// create an empy utility list for pXY
		UtilityList pxyUL = new UtilityList(py.item);
		
		//== new optimization - LA-prune  == /
		// Initialize the sum of total utility
		long totalUtility = px.sumIutils + px.sumRutils;
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
				}
				// =============================================== /
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
	 * @param utility the utility of the prefix concatenated with the item
	 */
	private void writeOut(Itemset itemset) throws IOException {
		huiCount++; // increase the number of high utility itemsets found
		
		//Create a string buffer
		StringBuilder buffer = new StringBuilder();
		// append the prefix
		for (int i = 0; i < itemset.itemset.length; i++) {
			buffer.append(itemset.itemset[i]);
			if(i != itemset.itemset.length -1) {
				buffer.append(' ');
			}
		}
//		// append the utility value
//		buffer.append(" #SUP: ");
//		buffer.append(itemset.support);
		// append the utility value
		buffer.append(" #UTIL: ");
		buffer.append(itemset.utility);
		// write to file
		writer.write(buffer.toString());
		writer.newLine();
		
	}
	
	/**
	 * Method to write a high utility itemset to the output file, that contains a single item.
	 * @param the prefix to be writent o the output file
	 * @param an item to be appended to the prefix
	 * @param utility the utility of the prefix concatenated with the item
	 */
	private void writeOutItemsetSize1(int item, int utility) throws IOException {
		huiCount++; // increase the number of high utility itemsets found
		
		//Create a string buffer
		StringBuilder buffer = new StringBuilder();
		// append the prefix
		buffer.append(item);
//		// append the utility value
//		buffer.append(" #SUP: ");
//		buffer.append(itemset.support);
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
		System.out.println("=============  MinFHM ALGORITHM - STATS =============");
		System.out.println(" Total time ~ "                  + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Memory ~ "                      + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println(" MinHUIs count : " + huiCount); 
		System.out.println(" Candidate count : "             + candidateCount);
		
		if(debug) {
			int pairCount = 0;
			double maxMemory = getObjectSize(mapFMAP);
			for(Entry<Integer, Map<Integer, Integer>> entry : mapFMAP.entrySet()) {
				maxMemory += getObjectSize(entry.getKey());
				for(Entry<Integer, Integer> entry2 :entry.getValue().entrySet()) {
					pairCount++;
					maxMemory += getObjectSize(entry2.getKey()) + getObjectSize(entry2.getValue());
				}
			}
			System.out.println("CMAP size " + maxMemory + " MB");
			System.out.println("PAIR COUNT " + pairCount);
		}
		System.out.println("===================================================");
	}
	
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