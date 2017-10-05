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
*/


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
/**
 * This is an implementation of the "HUG-Miner" algorithm for High-Utility Generator Mining
 * as described in the conference paper : <br/><br/>
 * 
 * "Novel Concise Representations of High Utility Itemsets using Generator Patterns"
 * 
 * @see UtilityListWithCriticalObjects
 * @see Element
 * @see PairItemUtility
 * @author Philippe Fournier-Viger
 */
public class AlgoHUGMiner {

	/** maximum memory usage */
	public double maxMemory = 0;    
	/** start time of the algorithm */
	public long startTimestamp = 0; 
	/** end time of the algorithm */
	public long endTimestamp = 0;   
	/** number of HUGs found */
	public int hugsCount =0;  
	
	/** number of candidates considered by the algorithm */
	public long candidateCount =0;
	/** number of candidates avoided by FHM (EUCP) pruning */
	public long candidateAvoidedbyFHMPruning =0;
	
	/** number of times that an itemset was checked if it was a generator */
	public long generatorChecks = 0;
	/** number of times that the generator check was stoped early due to optimizations */
	public long partiallyAvoidedOrAvoidedGeneratorChecks = 0;
	
	/** Map to remember the TWU of each item */
	Map<Integer, Integer> mapItemToTWU;

	/** writer to write the output file  */
	BufferedWriter writer = null;  
	
	/** NEW OPTIMIZATION - Structure used by the EUCP strategy (as in the FHM paper)
	 Key: for an item x, 
	 Value: a map of item where key is an item y and value is the TWU of {x,y} */
	Map<Integer, Map<Integer, Integer>> mapFMAP;  
	
	/** number of transaction in the database */
	private int transactionCount = 0;
	
	/** set this variable to true for showing extra debugging information in the
	 console  */
	boolean debug = false;
	
	/** A Map that stores the utility list and critical object of each single item */
	private Map<Integer, UtilityListWithCriticalObjects> mapItemToUtilityList;

	/** buffer for storing the current itemset that is mined when performing mining
	/* the idea is to always reuse the same buffer to reduce memory usage. */
	private int[] itemsetBuffer = null;
	/** The default buffer size */
	final int BUFFERS_SIZE = 200;
	
	/** enable LA-prune pruning strategy (as in the HUP-Miner paper)*/
	boolean enableLAPrune = true;
	
	/**
	 * Default constructor
	 */
	public AlgoHUGMiner() {
		 
	}

	/**
	 * Run the HUG-MINER algorithm
	 * @param input the input file path
	 * @param output the output file path
	 * @param minUtility the minimum utility threshold
	 * @throws IOException exception if error while writing the file
	 */
	public void runAlgorithm(String input, String output, int minUtility) throws IOException {
		// reset maximum
		maxMemory =0;
		
		// initialize the buffer for storing the current itemset
		itemsetBuffer = new int[BUFFERS_SIZE];
		
		//initialise the map used by the EUCP strategy
		mapFMAP =  new HashMap<Integer, Map<Integer, Integer>>();
		
		// record start timestamp
		startTimestamp = System.currentTimeMillis();
		
		// initialize object to write output file
		writer = new BufferedWriter(new FileWriter(output));
	

		//  We create a  map to store the TWU of each item
		mapItemToTWU = new HashMap<Integer, Integer>();
	
		// This variable will be used to record the total utility stored in a database
		long totalUtility = 0;
		
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
				if (thisLine.isEmpty() == true || 	thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%' 		|| thisLine.charAt(0) == '@') {
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
					twu = (twu == null)?  transactionUtility : twu + transactionUtility;
					mapItemToTWU.put(item, twu);
				}
				// update the transaction count
				transactionCount++;
				// add the utility of this transaction to the total utility of the database
				totalUtility += transactionUtility;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
	    }
		
		// CREATE A LIST TO STORE THE UTILITY LIST OF ITEMS WITH TWU  >= MIN_UTILITY.
		List<UtilityListWithCriticalObjects> listOfUtilityLists = new ArrayList<UtilityListWithCriticalObjects>();
		// Initialize a map to store the utility list and critical objects of single items
		mapItemToUtilityList = new HashMap<Integer, UtilityListWithCriticalObjects>();
		
		// For each item
		for(Integer item: mapItemToTWU.keySet()){
			// if the item is promising  (TWU >= minutility)
			if(mapItemToTWU.get(item) >= minUtility){
				// create an empty Utility List that we will fill later.
				UtilityListWithCriticalObjects uList = new UtilityListWithCriticalObjects(item);
				// put the created utility list in the map
				mapItemToUtilityList.put(item, uList);
				// add the item to the list of high TWU items
				listOfUtilityLists.add(uList); 
				
			}
		}
		
		// SORT THE LIST OF HIGH TWU ITEMS IN ASCENDING ORDER OF TWU
		Collections.sort(listOfUtilityLists, new Comparator<UtilityListWithCriticalObjects>(){
			public int compare(UtilityListWithCriticalObjects o1, UtilityListWithCriticalObjects o2) {
				// compare the TWU of the items
				return compareItems(o1.item, o2.item);
			}});
		
		// PERFORM A SECOND DATABASE PASS TO CONSTRUCT THE UTILITY LISTS 
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
				if (thisLine.isEmpty() == true || 	thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%' || thisLine.charAt(0) == '@') {
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
				
				// This will store the new TU (transaction utility) after
				// unpromising items have been removed.
				int newTU = 0;  
				
				// Create a list to store items
				List<PairItemUtility> revisedTransaction = new ArrayList<PairItemUtility>();
				// for each item
				for(int i=0; i <items.length; i++){
					/// convert values to integers
					PairItemUtility pair = new PairItemUtility();
					pair.item = Integer.parseInt(items[i]);
					pair.utility = Integer.parseInt(utilityValues[i]);
					// if the item has enough utility
					if(mapItemToTWU.get(pair.item) >= minUtility){
						// add the item to the revised transaction
						revisedTransaction.add(pair);
						// update the revised transaction utility
						newTU += pair.utility; 
					}
				}
				
				
				// sort the transaction
				Collections.sort(revisedTransaction, new Comparator<PairItemUtility>(){
					public int compare(PairItemUtility o1, PairItemUtility o2) {
						return compareItems(o1.item, o2.item);
					}});
	
				// This variable will be used to calculate the remaining utility 
				// in a transaction after the some items have been processed
				int remainingUtility = newTU;
				
				// for each item left in the transaction
				for(int i = 0; i< revisedTransaction.size(); i++){
					PairItemUtility pair =  revisedTransaction.get(i);
	
					// subtract the utility of this item from the remaining utility
					remainingUtility = remainingUtility - pair.utility;
					
					// get the utility list of this item
					UtilityListWithCriticalObjects utilityListOfItem = mapItemToUtilityList.get(pair.item);
					
					// Add a new Element to the utility list of this item corresponding to this transaction
					Element element = new Element(tid, pair.utility, remainingUtility);
					utilityListOfItem.addElement(element);
										
					// BEGIN NEW OPTIMIZATION for updating the map used
					// BY THE FHM EUCP STRATEGY
					Map<Integer, Integer> mapFMAPItem = mapFMAP.get(pair.item);
					if(mapFMAPItem == null) {
						mapFMAPItem = new HashMap<Integer, Integer>();
						mapFMAP.put(pair.item, mapFMAPItem);
					}
	
					for(int j = i+1; j< revisedTransaction.size(); j++){
						PairItemUtility pairAfter = revisedTransaction.get(j);
						Integer twuSum = mapFMAPItem.get(pairAfter.item);
						if(twuSum == null) {
							mapFMAPItem.put(pairAfter.item, newTU);
						}else {
							mapFMAPItem.put(pairAfter.item, twuSum + newTU);
						}
					}
						
					// END OPTIMIZATION of FHM	EUCP STRATEGY
				}
				tid++; // increase tid number for next transaction
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(myInput != null){ myInput.close();
			}
	    }
		
		// check the memory usage
		checkMemory();
		
		// NEW CODE FOR HUG-MINER
		// Create the tidset of the empty set 
		BitSet tidsetEmptySet = new BitSet(transactionCount);
		tidsetEmptySet.set(1, transactionCount);
		UtilityListWithCriticalObjects emptyUL = new UtilityListWithCriticalObjects(null);
		emptyUL.tidset = tidsetEmptySet;
		emptyUL.crit = new BitSet[0];
		int emptySet[] = new int[0];
		// END NEW CODE FOR HUG-MINER
		
		// PERFORM A LOOP FOR EACH SINGLE ITEM
		Iterator<UtilityListWithCriticalObjects> iter = listOfUtilityLists.iterator();
		while (iter.hasNext()) {
			// GET THE UTILITY LIST OF THE ITEM
			UtilityListWithCriticalObjects ul = (UtilityListWithCriticalObjects) iter.next();
			// CHECK IF THE SINGLE ITEM IS A GENERATOR
			checkIfGeneratorSingleItem(emptyUL, ul);
			// If it is not a generator or has no high utility supersets.
			if(ul.crit == null || (ul.sumIutils + ul.sumRutils < minUtility)) {
				// then we remove it because no superset can be a HUG
				iter.remove();
			}else {
				// if the item is high-utility according to its utility list
				if(ul.sumIutils >= minUtility){
					// Then, it is a HUG, so we output it
					// and we also keep it in the list of item for generating 
					// larger itemsets
					writeOut(emptySet, 0, ul.item, ul.sumIutils, transactionCount);
				}
			}
		}

		//	If the total utility of the database is larger than the minUtil threshold
		if(totalUtility >= minUtility) {
			// Recursively search for larger patterns
			hugminer(itemsetBuffer, 0, emptyUL, listOfUtilityLists, minUtility);
		}

		// check the memory usage again and close the file.
		checkMemory();
		// close output file
		writer.close();
		// record end time
		endTimestamp = System.currentTimeMillis();
	}


	/**
	 * This method compare two items by their TWU and if they are equal, they are
	 * compared according to lexographical order.
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
	 * This is the recursive method to find all HUGs. 
	 * It writes the HUGs to the output file.
	 * @param prefixP  This is the current prefix. Initially, it is empty.
	 * @param p_UL This is the Utility List of the prefix. Initially, it is empty.
	 * @param extensionsULs The utility lists corresponding to each extension of the prefix.
	 * @param minUtility The minUtility threshold.
	 * @param prefixLength The current prefix length
	 * @throws IOException if an error occurs when writing to file
	 */
	private void hugminer(int [] prefixP, int prefixLength, UtilityListWithCriticalObjects p_UL, 
			List<UtilityListWithCriticalObjects> extensionsULs, int minUtility) throws IOException {

		// For each extension pX of prefix p
		for(int i=0; i< extensionsULs.size(); i++){
			// Obtain the utility list and critical objects of pX
			UtilityListWithCriticalObjects pX_UL = extensionsULs.get(i);
			
			// If the sum of the iutils + rutils in the utility list of pX
			// is higher than minUtility, we explore extensions of pX.
			// (this is the pruning condition of HUI-Miner)
			if(pX_UL.sumIutils + pX_UL.sumRutils >= minUtility){

				// We create new prefix pX
				itemsetBuffer[prefixLength] = pX_UL.item;
				
				// This list will contain the extensions of pX and their utility lists.
				List<UtilityListWithCriticalObjects> extensionsOfPX = new ArrayList<UtilityListWithCriticalObjects>();
				
				// For each extension pY such that Y appear
				// after X according to the ascending order
				// we will consider pXY
				for(int j=i+1; j < extensionsULs.size(); j++){
					// Get the utility list and critical object of pY
					UtilityListWithCriticalObjects pY_UL = extensionsULs.get(j);
	
					// ======================== EUCP pruning strategy from FHM
					// Check if the TWU of {X,Y} < minutil.
					// If yes, then pXY and its supersets are not high utility itemsets
					boolean shouldPrune = checkEUCPStrategy(minUtility, pX_UL.item, pY_UL.item);
					if(shouldPrune) {
						continue;
					}
					// =========================== END OF NEW OPTIMIZATION
					
					// update this variable to count the nb of candidates for statistics
					candidateCount++; 
					
					// we construct the utility list of pXY
					UtilityListWithCriticalObjects 
						pXYUL = (p_UL.item == null) ? construct(pX_UL, pY_UL, minUtility) : construct(p_UL, pX_UL, pY_UL, minUtility);
					
					// if the utility list of pXY is empty, skip it
					if(pXYUL == null || pXYUL.elements.isEmpty()) {
						continue;
					}
					
					// ======== OPTIMIZATION NOT IN DEFME  ===========
					// if the pattern has the same support as PX or PY, it is not a generator, WE SKIP IT
					if(  pXYUL.elements.size() == pX_UL.elements.size() 
					  || pXYUL.elements.size() == pY_UL.elements.size()) {
						continue;
					}
					// -======================================
					
					// ===== PRUNING: IF THE PATTERN IS NOT HUI 
					// AND HAS NO HUI SUPERSETS WE SKIP IT  ====
					if(pXYUL.sumIutils + pXYUL.sumRutils < minUtility) {
						continue;
					}
	
					// CHECK IF THE PATTERN IS A GENERATOR USING THE DEFME METHOD (but optimized)
					boolean isGenerator = checkIfGenerator(pX_UL, pXYUL, prefixLength+1);
					
					// IF IT IS A GENERATOR WE KEEP IT
					if(isGenerator) {
						// IF pXY is high utility
						if(pXYUL.sumIutils >= minUtility){
							// Then pXY is a HUG and is output to the output file
							writeOut(itemsetBuffer, prefixLength+1, pXYUL.item, pXYUL.sumIutils, pXYUL.elements.size());
						}
						// If pXY is a generator, we add it to the set of extensions
						extensionsOfPX.add(pXYUL);
					}
				}
				
				// We make a recursive call to discover all itemsets with the prefix pXab by combining 
				// pairs of 1-extensions of pX such as pXa and pXb
				if(extensionsOfPX.size() > 1) {
					// recursive call to generate more itemsets using extensions of pX
					hugminer(itemsetBuffer, prefixLength+1, pX_UL, extensionsOfPX, minUtility); 
				}
			}
		}
	}

	/**
	 * This method performs the EUCP pruning from the FHM algorithm (see FHM paper at ISMIS 2014)
	 * @param minUtility the min utility threshold
	 * @param itemX  an item X
	 * @param itemY an item Y
	 * @return true if  TWU({x,y} < minutil.  Otherwise return false
	 */
	private boolean checkEUCPStrategy(int minUtility, int itemX, int itemY) {
		Map<Integer, Integer> mapTWUF = mapFMAP.get(itemX);
		if(mapTWUF != null) {
			Integer twuF = mapTWUF.get(itemY);
			if(twuF == null || twuF < minUtility) {
				candidateAvoidedbyFHMPruning++;
				return true;
			}
		}
		return false;
	}


	/**
	 * Check if an integer appear in an array of integers
	 * @param list the array of integers
	 * @param integer the integer 
	 * @return true if the integer appear in the array. Otherwise false.
	 */
	public boolean contains(int[] list, int integer) {
		// for each item in th
		for(int item : list) {
			if(item == integer) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * This method checks if an itemset pX is a generator (general case)
	 * @param p_UL  the itemset p (and its critical objects)
	 * @param pX_UL  the itemset pX (and its critical objects) 
	 * @param prefixSize the size of prefix "p"
	 * @return true if pX is a generator. Otherwise false.
	 */
	private boolean checkIfGenerator(UtilityListWithCriticalObjects p_UL,
			UtilityListWithCriticalObjects pX_UL, int prefixSize) {
		
		// increase the number of generator check for statistics
		generatorChecks++;

		// obtain the tidset of the item X
		BitSet tidsetE = mapItemToUtilityList.get(pX_UL.item).tidset;
		
		// Initialize the array for storing critical objects of pX
		pX_UL.crit = new BitSet[prefixSize+1];
		
		// Create the critical object of pX w.r.t item X
		BitSet critE = (BitSet) p_UL.tidset.clone();
		critE.andNot(tidsetE);
		pX_UL.crit[pX_UL.crit.length-1] = critE;
		
		// ======= OPTIMIZATION NOT IN DEFME =========== :
		// If this critical object is empty, pX is not a generator and
		// we immediately stop computing the critical objects of pX
		if(critE.cardinality() == 0){
			partiallyAvoidedOrAvoidedGeneratorChecks++;
			// return false to indicate that pX is not a generator
			return false;		
		}
		// =========== END OPTIMIZATION ===========
		
		// Now we will compute the cirtical objects of pX w.r.t 
		// to every item e' in X that is different from X
		for(int j=0; j < prefixSize; j++) {
			// calculate the critical objects of the j-th item in P
			pX_UL.crit[j] = (BitSet)p_UL.crit[j].clone();
			pX_UL.crit[j].and(tidsetE);
			// ======= OPTIMIZATION NOT IN DEFME  - PART 2 FOR ALL OTHER ITEMS =========== :
			// IF The CRITICAL OBJECT W.R.T the j-th item HAS SIZE 0,
			// We return false to indicate that pX is not a generator
			// and stop computing critical objects
			if(pX_UL.crit[j].cardinality() == 0){
				if(j < prefixSize -1) {
					partiallyAvoidedOrAvoidedGeneratorChecks++;
				}
				return false;
			}
			// =========== END OPTIMIZATION ===========
		}
		return true;
	}
	
	/**
	 * This method checks if an itemset pX is a generator (optimized for the 
	 * case of an itemset containing a single item)
	 * @param emptySet_UL  the empty set (and its critical objects)
	 * @param X_UL  the itemset {X} (and its critical objects)
	 * @return true if {X} is a generator. Otherwise false.
	 */
	private void checkIfGeneratorSingleItem(UtilityListWithCriticalObjects emptySet_UL,
			UtilityListWithCriticalObjects X_UL) {
		// OPTIMIZATION NOT IN DEFME: 
		// Given that the itemset {X} contains a single item,
		// WE JUST NEED TO CHECK IF THE SUPPORT OF {X} IS EQUAL TO THE SUPPORT OF 
		// THE EMPTY SET TO DETERMINE IF {X} is a generator.
		// If it is not a generator, then we don't need to compute the critical objects.
		// (because X will not be used to generate other itemsets).
		if(transactionCount == X_UL.elements.size()) {
			partiallyAvoidedOrAvoidedGeneratorChecks++;
			return;
		}
		// increase the number of generator check
		generatorChecks++;
		
		// CALCULATE the critical objects of {X}
		BitSet tidsetE = mapItemToUtilityList.get(X_UL.item).tidset;
		BitSet crit = (BitSet) emptySet_UL.tidset.clone();
		crit.andNot(tidsetE);
		X_UL.crit = new BitSet[]{crit};
	}
	
	/**
	 * This method constructs the utility list of pXY using the utility lists of
	 * p, pX and pY (done as in HUI-Miner).
	 * @param P :  the utility list of prefix P.
	 * @param px : the utility list of pX
	 * @param py : the utility list of pY
	 * @param minUtility : the minimum utility threshold
	 * @return the utility list of pXY
	 */
	private UtilityListWithCriticalObjects construct(UtilityListWithCriticalObjects P, UtilityListWithCriticalObjects px, UtilityListWithCriticalObjects py, int minUtility) {
		
		// create an empy utility list for pXY
		UtilityListWithCriticalObjects pxyUL = new UtilityListWithCriticalObjects(py.item);
		
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
				if(enableLAPrune) {
					totalUtility -= (ex.iutils+ex.rutils);
					if(totalUtility < minUtility) {
						return null;
					}
				}
				//====================
				continue;
			}
			// find the element in the utility list of p wih the same tid
			Element e = findElementWithTID(P, ex.tid);
			if(e != null){
				// Create new element
				int diff = ey.iutils - e.iutils;
				Element eXY = new Element(ex.tid, ex.iutils + diff,	ey.rutils);
				// add the new element to the utility list of pXY
				pxyUL.addElement(eXY);
			}
		}
		// return the utility list of pXY.
		return pxyUL;
	}
	

	/**
	 * This method constructs the utility list of itemset {x,y} using the utility lists of
	 * itemsets {x} and {y} (done as in HUI-Miner).
	 * @param x_UL the utility list of {x}
	 * @param y_UL the utility list of {y}
	 * @param minUtility minimum utility
	 * @return the utility list of {x,y}
	 */
	private UtilityListWithCriticalObjects construct(UtilityListWithCriticalObjects x_UL, UtilityListWithCriticalObjects y_UL, int minUtility) {
		// create an empy utility list for {x,y}
		UtilityListWithCriticalObjects pxyUL = new UtilityListWithCriticalObjects(y_UL.item);
		
		//== new optimization - LA-prune  == /
		// Initialize the sum of total utility
		long totalUtility = x_UL.sumIutils + x_UL.sumRutils;
		// ================================================
		
		// for each element in the utility list of {x}
		for(Element ex : x_UL.elements){
			// do a binary search to find element ey in py with tid = ex.tid
			Element ey = findElementWithTID(y_UL, ex.tid);
			if(ey != null){
				// Create the new element
				Element eXY = new Element(ex.tid, ex.iutils + ey.iutils, ey.rutils);
				// add the new element to the utility list of {x,y}
				pxyUL.addElement(eXY);
			}else {
				//== new optimization - LA-prune == /
				if(enableLAPrune) {
					totalUtility -= (ex.iutils+ex.rutils);
					if(totalUtility < minUtility) {
						return null;
					}
				}
			}
		}
		// return the utility list of {x,y}.
		return pxyUL;
	}

	/**
	 * Do a binary search to find the element with a given tid in a utility list
	 * @param ulist the utility list
	 * @param tid  the tid
	 * @return  the element or null if none has the tid.
	 */
	private Element findElementWithTID(UtilityListWithCriticalObjects ulist, int tid){
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
	 * Write a HUG to the output file
	 * @param prefix the prefix of the HUG
	 * @param item  an item to be appended to the prefix
	 * @param sumIutils the utility of the itemset composed of prefix + item
	 * @param support the support of the itemset composed of prefix + item
	 * @param prefixLength The current prefix length
	 * @throws IOException if an error occurs while writing the itemset to file.
	 */
	private void writeOut(int[] prefix, int prefixLength, int item, long sumIutils, int support) throws IOException {
		hugsCount++; // increase the number of high utility generators found
		
		//Create a string buffer
		StringBuilder buffer = new StringBuilder();
		// append the prefix
		for (int i = 0; i < prefixLength; i++) {
			buffer.append(prefix[i]);
			buffer.append(' ');
		}
		// append the prefix
		buffer.append(item);
		// append the support value
		buffer.append(" #SUP: ");
		buffer.append(support);
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
		System.out.println("=============  HUG-Miner ALGORITHM - SPMF 0.97e - STATS =============");
		System.out.println("   Candidate count : "             + candidateCount  + "     (avoided by EUCP strategy : " + candidateAvoidedbyFHMPruning 
				+ ")\n"
				+ "   Genenerator checks: " + generatorChecks
				+ "   (partially avoided : " + partiallyAvoidedOrAvoidedGeneratorChecks + ")");
		System.out.println(" Total time ~ "                  + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max. Memory ~ "                      + maxMemory+ " MB");
		System.out.println(" HUGs count : " + hugsCount); 
		System.out.println("==============================================================");
	}


}