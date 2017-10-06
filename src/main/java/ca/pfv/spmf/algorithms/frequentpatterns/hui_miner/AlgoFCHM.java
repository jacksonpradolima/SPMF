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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This is an implementation of the "FCHM" algorithm for High-Utility Itemsets Mining
 * as described in the conference paper : <br/><br/>
 * 
 * Fournier-Viger, P., Lin, C. W., Dinh, T., Le, H. B. (2016). Mining Correlated High-Utility
 *  Itemsets Using the Bond Measure. Proc. 11 th International Conference on Hybrid Artificial
 *   Intelligence Systems (HAIS 2016), Springer LNAI, 14 pages, to appear.
 * 
 * @see UtilityListFCHM
 * @see Element
 * @author Philippe Fournier-Viger
 */
public class AlgoFCHM  implements Serializable{
	
	/**
	 * Generated serial UID
	 */
	private static final long serialVersionUID = 1513172536452900775L;

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
	
	/** The eucs structure:  key: item   key: another item   value: twu&support */
	Map<Integer, Map<Integer, TwuSupportPair>> mapSMAP;  
	
	/** variable for debug mode */
	boolean DEBUG = false;
	
	/** buffer for storing the current itemset that is mined when performing mining
	* the idea is to always reuse the same buffer to reduce memory usage. */
	final int BUFFERS_SIZE = 200;
	private int[] itemsetBuffer = null;

	/** minimum bond threshold**/
	private double minBond;  
	
	/** enable LA-prune strategy  */
	boolean ENABLE_LA_PRUNE = true;
	
	/** enable SLA-prune strategy  */
	boolean ENABLE_SLA_PRUNE = true;
	
	/** number of candidates eliminated by la prune */
	private long candidateEliminatedByLAPrune =0;
	
	/** number of candidates eliminated by sla prune */
	private long candidateEliminatedBySLAPrune =0;
	
	/** bond pruning optimization **/
	private boolean ENABLE_FHM_PRUNING = true;
	
	/** bond pruning optimization **/
	private boolean ENABLE_BOND_PAIR_PRUNING = true;
	
	/** number of candidates eliminated by bond pruning */
	private long candidateEliminatedByBondPruning =0;
	
	/** number of candidates eliminated by FHM pruning */
	private long candidateEliminatedByFHMPruning =0;
	
	
	/** this class represent an item and its utility in a transaction */
	class Pair{
		int item = 0;
		int utility = 0;
	}
	
	/**
	 * Default constructor
	 */
	public AlgoFCHM() {
		
	}

	/**
	 * Run the algorithm
	 * @param input the input file path
	 * @param output the output file path
	 * @param minUtility the minimum utility threshold
	 * @param minBond th minbond threshold
	 * @throws IOException exception if error while writing the file
	 */
	public void runAlgorithm(String input, String output, int minUtility, double minBond) throws IOException {
		// reset maximum
		MemoryLogger.getInstance().reset();
		
		// reset variables for statistics
		candidateEliminatedByBondPruning =0;
		candidateEliminatedByFHMPruning =0;
		
		// initialize the buffer for storing the current itemset
		itemsetBuffer = new int[BUFFERS_SIZE];
		
		mapSMAP =  new HashMap<Integer, Map<Integer, TwuSupportPair>>();
		
		
		startTimestamp = System.currentTimeMillis();
		
		writer = new BufferedWriter(new FileWriter(output));

		//  We create a  map to store the TWU of each item
		mapItemToTWU = new HashMap<Integer, Long>();
		
		// save the minbond threshold
		this.minBond = minBond;

		// Structure to calculate tidsets of each item.
		// This map will contain the tidset of each item
		// Key: item   Value :  tidset

		// We scan the database a first time to calculate the TWU of each item.
		BufferedReader myInput = null;
		String thisLine;
		int tid = 0; // the current tid;
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
		List<UtilityListFCHM> listOfUtilityListFCHMs = new ArrayList<UtilityListFCHM>();
		// CREATE A MAP TO STORE THE UTILITY LIST FOR EACH ITEM.
		// Key : item    Value :  utility list associated to that item
		Map<Integer, UtilityListFCHM> mapItemToUtilityListFCHM = new HashMap<Integer, UtilityListFCHM>();
		
		// For each item
		for(Integer item: mapItemToTWU.keySet()){
			
			// if the item is promising  (TWU >= minutility)
			if(mapItemToTWU.get(item) >= minUtility){
				
				// create an empty Utility List that we will fill later.
				UtilityListFCHM uList = new UtilityListFCHM(item,new BitSetSupport());
				mapItemToUtilityListFCHM.put(item, uList);
				// add the item to the list of high TWU items
				listOfUtilityListFCHMs.add(uList); 
			}
		}
		// SORT THE LIST OF HIGH TWU ITEMS IN ASCENDING ORDER
		Collections.sort(listOfUtilityListFCHMs, new Comparator<UtilityListFCHM>(){
			public int compare(UtilityListFCHM o1, UtilityListFCHM o2) {
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
			 tid =0;
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
					if(mapItemToUtilityListFCHM.get(pair.item) != null){
						
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
					UtilityListFCHM UtilityListFCHMOfItem = mapItemToUtilityListFCHM.get(pair.item);
					
					// update the tid set of that item by adding the id of the current
					// transaction
					UtilityListFCHMOfItem.bitsetDisjunctiveTIDs.bitset.set(tid);
					UtilityListFCHMOfItem.bitsetDisjunctiveTIDs.support++;
					
					// Add a new Element to the utility list of this item corresponding to this transaction
					Element element = new Element(tid, pair.utility, remainingUtility);
					
					UtilityListFCHMOfItem.addElement(element);
										
					// BEGIN NEW OPTIMIZATION for FHM
					Map<Integer, TwuSupportPair> mapFMAPItem = mapSMAP.get(pair.item);
					if(mapFMAPItem == null) {
						mapFMAPItem = new HashMap<Integer, TwuSupportPair>();
						mapSMAP.put(pair.item, mapFMAPItem);
					}

					for(int j = i+1; j< revisedTransaction.size(); j++){
						Pair pairAfter = revisedTransaction.get(j);
						TwuSupportPair twuSupportPair = mapFMAPItem.get(pairAfter.item);
						if(twuSupportPair == null) {
							twuSupportPair = new TwuSupportPair();
							mapFMAPItem.put(pairAfter.item, twuSupportPair);
						}
						
						twuSupportPair.twu += newTWU;
						twuSupportPair.support++;
//						else {
//							mapFMAPItem.put(pairAfter.item, twuSum + newTWU);
//						}
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

		// Mine the database recursively
		fchm(itemsetBuffer, 0, null, listOfUtilityListFCHMs, minUtility);
		
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
		int compare = (int)( mapItemToTWU.get(item1) - mapItemToTWU.get(item2));
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
	 * @param prefixLength The current prefix length
	 * @throws IOException
	 */
	private void fchm(int [] prefix,
			int prefixLength, UtilityListFCHM pUL, List<UtilityListFCHM> ULs, int minUtility)
			throws IOException {
		
		// For each extension X of prefix P
		for(int i=0; i< ULs.size(); i++){
			UtilityListFCHM X = ULs.get(i);

			// If pX is a high utility itemset.
			// we save the itemset:  pX 
			if(X.sumIutils >= minUtility){
				// save to file
				writeOut(prefix, prefixLength, X.item, X.sumIutils, X.getBond());
			}
			
			// If the sum of the remaining utilities for pX
			// is higher than minUtility, we explore extensions of pX.
			// (this is the pruning condition)
			if(X.sumIutils + X.sumRutils >= minUtility){
				// This list will contain the utility lists of pX extensions.
				List<UtilityListFCHM> exULs = new ArrayList<UtilityListFCHM>();
				// For each extension of p appearing
				// after X according to the ascending order
				for(int j=i+1; j < ULs.size(); j++){
					UtilityListFCHM Y = ULs.get(j);
					
					// ======================== NEW OPTIMIZATION USED IN FHM
					Map<Integer, TwuSupportPair> mapTWUF = mapSMAP.get(X.item);
					if(mapTWUF != null) {
						TwuSupportPair twuF = mapTWUF.get(Y.item);
						
						
						if(twuF != null) {
							
							if(ENABLE_FHM_PRUNING && twuF.twu < minUtility) {
								candidateEliminatedByFHMPruning++;
								continue;
							}
							if(ENABLE_BOND_PAIR_PRUNING) {
								boolean conditionY = twuF.support /  (double) Y.bitsetDisjunctiveTIDs.support < minBond;
								boolean conditionX = twuF.support /  (double) X.bitsetDisjunctiveTIDs.support  < minBond;
								
								if(conditionX || conditionY ) {
									candidateEliminatedByBondPruning++;
									continue;
								}
							}
						}
							
					}
					candidateCount++;
					// =========================== END OF NEW OPTIMIZATION
					
					// we construct the extension pXY 
					// and add it to the list of extensions of pX
					UtilityListFCHM temp = construct(pUL, X, Y, minUtility);
					if(temp != null && temp.getBond() >= minBond) {
						exULs.add(temp);
					}
				}
				// We create new prefix pX
				itemsetBuffer[prefixLength] = X.item;
				// We make a recursive call to discover all itemsets with the prefix pXY
				fchm(itemsetBuffer, prefixLength+1, X, exULs, minUtility); 
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
	private UtilityListFCHM construct(UtilityListFCHM P, UtilityListFCHM px, UtilityListFCHM py, int minUtility) {
		
		BitSetSupport bitsetPX = px.bitsetDisjunctiveTIDs;
		BitSetSupport bitsetPY = py.bitsetDisjunctiveTIDs;
		BitSetSupport bitsetPXY = performOR(bitsetPX, bitsetPY);
		
		//== new optimization - SLA-prune  == /
		// Initialize the sum of total utility
		double maxdisjunctivesupport  = bitsetPXY.support;
		double pxsupport = px.elements.size();
		int minSup = (int) Math.ceil(maxdisjunctivesupport * minBond);
		// ================================================
		
		// create an empty utility list for pXY
		UtilityListFCHM pxyUL = new UtilityListFCHM(py.item, bitsetPXY);
		
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
						candidateEliminatedByLAPrune++;
						return null;
					}
				}
				if(ENABLE_SLA_PRUNE) {
					pxsupport--;
					if(pxsupport < minSup){
						candidateEliminatedBySLAPrune++;
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
	private Element findElementWithTID(UtilityListFCHM ulist, int tid){
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
	 * @param bond the bond of this itemset
	 * @param prefixLength the prefix length
	 * @param bond 
	 */
	private void writeOut(int[] prefix, int prefixLength, int item, long utility, double bond) throws IOException {
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
		// append the bond value
		buffer.append(" #BOND: ");
		buffer.append(bond);
		// write to file
		writer.write(buffer.toString());
		writer.newLine();
	}
	
	/**
	 * Perform the OR of two tidsets for itemsets containing more than one item to
	 * calculate the conjunctive support.
	 * @param tidsetI the first tidset
	 * @param tidsetJ the second tidset
	 * @return the resulting tidset and its support
	 */
	 BitSetSupport performOR(BitSetSupport tidsetI,
			BitSetSupport tidsetJ) {
		// Create the new tidset and perform the logical AND to intersect the tidset
		BitSetSupport bitsetSupportIJ = new BitSetSupport();
		bitsetSupportIJ.bitset = (BitSet)tidsetI.bitset.clone();
		bitsetSupportIJ.bitset.or(tidsetJ.bitset);
		// set the support as the cardinality of the new tidset
		bitsetSupportIJ.support = bitsetSupportIJ.bitset.cardinality();
		// return the new tidset
		return bitsetSupportIJ;
	}

	
	/**
	 * Print statistics about the latest execution to System.out.
	 * @throws IOException 
	 */
	public void printStats() throws IOException {
		System.out.println("=============  FCHM ALGORITHM v0.96r18 - STATS =============");
		System.out.println(" Total time ~ "                  + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Memory ~ "                      + MemoryLogger.getInstance().getMaxMemory()  + " MB");
		System.out.println(" Correlated High-utility itemset count : " + huiCount); 
		System.out.println("   Candidate count : "             + candidateCount);
		System.out.println("   Candidate eliminated by bond pruning: " + candidateEliminatedByBondPruning);
		System.out.println("   Candidate eliminated by FHM pruning: " + candidateEliminatedByFHMPruning);
		System.out.println("   List constructions stopped by SLAPrune: " + candidateEliminatedBySLAPrune);
		System.out.println("   List constructions stopped by LAPrune: " + candidateEliminatedByLAPrune);	
		//
		
		// Print the SMAP
		if(DEBUG) {
			for(Entry<Integer, Map<Integer, TwuSupportPair>> entry : mapSMAP.entrySet()) {
				System.out.println("Item: " + entry.getKey());
				for(Entry<Integer, TwuSupportPair> entry2 :entry.getValue().entrySet()) {
					System.out.println(" (Item: " + entry2.getKey() + " twu: " + entry2.getValue().twu + " sup: " + entry2.getValue().support);
				}
			}
		}
		
		
		if(DEBUG) {
			int pairCount = 0;
			double maxMemory = getObjectSize(mapSMAP);
			for(Entry<Integer, Map<Integer, TwuSupportPair>> entry : mapSMAP.entrySet()) {
				maxMemory += getObjectSize(entry.getKey());
				for(Entry<Integer, TwuSupportPair> entry2 :entry.getValue().entrySet()) {
					pairCount++;
					maxMemory += getObjectSize(entry2.getKey()) + getObjectSize(entry2.getValue());
				}
			}
			System.out.println("CMAP size " + maxMemory + " MB");
			System.out.println("PAIR COUNT " + pairCount);
		}
		System.out.println("===================================================");
	}
	
	public long getTotalTime() {
		return (endTimestamp - startTimestamp);
	}
	
	public int getPatternCount() {
		return huiCount;
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
     * Class for storing the TWU and support of a pair of items
     * @author Philippe Fournier-Viger
     */
    class TwuSupportPair  implements Serializable{
		private static final long serialVersionUID = -131200309501702633L;
		int support = 0 ;
    	long twu = 0;
    }
    


}