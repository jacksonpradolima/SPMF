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
import java.util.Set;

import ca.pfv.spmf.algorithms.ArraysAlgos;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This is an implementation of the "GHUI-MINER" algorithm for High-Utility Itemsets Mining
 * as described in the conference paper : <br/><br/>
 * 
 * Fournier-Viger, P., Wu, C.W., Tseng, V.S. (2014). Novel Concise Representations of High Utility 
 * Itemsets using Generator Patterns. Proc. 10th Intern. Conference on Advanced Data Mining and 
 * Applications (ADMA 2014), Springer LNCS 8933, pp. 30-43.
 * 
 * @see UtilityList
 * @see Element
 * @author Philippe Fournier-Viger 2014
 */
public class AlgoGHUIMINER {
  
	/** start time of the algorithm */
	public long startTimestamp = 0; 
	/** end time of the algorithm */
	public long endTimestamp = 0;   
	/** number of GHUIs found */
	public long ghuiCount =0;  
	/** number of candidates considered by the algorithm */
	public long candidateCount =0;
	/** number of candidates avoided by FHM (EUCP) pruning */
	public long candidateAvoidedbyFHM =0;
	/** number of closure retrieval */
	public long closureRetrievals = 0;
	
	/** number of times that an itemset was checked if it was a generator */
	public long generatorChecks = 0;
	/** number of times that the generator check was stoped early due to optimizations */
	public long partiallyAvoidedOrAvoidedGeneratorChecks = 0;
	
	/** Map to remember the TWU of each item */
	Map<Integer, Integer> mapItemToTWU;

	/** writer to write the output file  */
	BufferedWriter writer = null;  
	
	/** NEW OPTIMIZATION - Structure used by the EUCP strategy
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
	
	/** A list of utilitylists */
	private List<UtilityListWithCriticalObjects> listOfUtilityLists;
	
	/** a boolean indicating if the empty set is a GHUI */
	boolean emptySetIsGHUIs = false;
	
	/** the minutil threshold */
	int minUtility = 0;
	
	// buffer for storing the current itemset that is mined when performing mining
	// the idea is to always reuse the same buffer to reduce memory usage.
	final int BUFFERS_SIZE = 200;
	private int[] itemsetBuffer = null;
	
	// enable LA-prune pruning strategy
	boolean enableLAPrune = true;
	
	// ======================================================
	// ================ STRUCTURE TO STORE CLOSED ITEMSETS (CHUIs) ======================
	
	/** The set of closed high utility itemsets (CHUIs) ordered by their support
	 * and then by their size.  It is implemented as a list of list.
	 * In the first list, the i-th position contains the list of itemsets
	 * containing i items ordered by their support. */
	List<List<Itemset>> closedItemsetsBySize = null;

	/**
	 * Retrieve the closure of an itemset
	 * @param itemset  the itemset
	 * @param support the itemset support
	 * @param prefixLength  the length of the itemset
	 * @return the closure of the itemset or null if none was found
	 */
	public Itemset getClosure(int [] itemsetX, int prefixLength, int support) {
		// increase the number of closure retrieval for statistics
		closureRetrievals++;
		
		// Perform a loop starting from itemsets with the same size 
		// as the itemset and until the maximum size
		for(int i=prefixLength; i < closedItemsetsBySize.size(); i++) {   
			// get the list of CHUIs of size i
			List<Itemset> list = closedItemsetsBySize.get(i);
			if(list != null) {
				// for each closed itemset of size i
				for(Itemset itemsetInList : list) {
					// if the support of the closed itemset is larger
					// than the support of the itemset the closed itemset cannot be a superset
					// and also any succeeding itemset in the list will not be superset 
					// (since the list is ordered by increasing support), so we break
					if (support < itemsetInList.support) {
						break;
					}
					
					// if the support is the same, and the itemset is included in the
					// closed itemset, then it is the closure and we return it.
					if(support == itemsetInList.support && ArraysAlgos.includedIn(itemsetX, prefixLength, itemsetInList.itemset)){
						return itemsetInList;
					}
				}
			}
		}
		// If the closure was not found, it means that the closure is not a CHUI,
		// Thus this method returns null.
		return null;
	}
	
	/**
	 * This method checks if a given itemset is a subset or a strict subset of a CHUI.
	 * @param itemset the itemset
	 * @param support the itemset support
	 * @param prefixLength the prefix length
	 * @param strictSubsetCheck  if true, this method check for "strict subset of". Otherwise, it checks for "subset of"
	 * @return true if the itemset is a subset (strict subset) of a CHUI. Otherwise false.
	 */
	public boolean isSubsetOfACHUI(int [] itemsetX, int prefixLength, int support, boolean strictSubsetCheck) {
		// if strict subset, we will start searching for itemset having a greater
		// length than that of the itemset.
		int minSize = strictSubsetCheck ? prefixLength+1 : prefixLength; 
		
		// Perform a loop on CHUIs by size
		for(int i=closedItemsetsBySize.size()-1; i >= minSize; i--) {    // IMPORTANT -1
			List<Itemset> list = closedItemsetsBySize.get(i);
			if(list != null) {
				// for each CHUI of size i
				for(Itemset itemsetInList : list) {
					// if the support of the closed itemset is larger
					// than the support of the itemset the closed itemset cannot be a superset
					// and also any succeeding itemset in the list will not be superset 
					// (since the list is ordered by increasing support), so we break
					if (support < itemsetInList.support) {
						break;
					}
					
					// if the itemset is included in the
					// closed itemset, then it is the closure and we return it.
					if(ArraysAlgos.includedIn(itemsetX, prefixLength, itemsetInList.itemset)){
						return true;
					}
				}
			}
		}
		// otherwise return false
		return false;
	}
	
	/**
	 * Sort the closed itemsets having the same size by increasing support.
	 */
	private void sortClosedItemsets() {
		// for itemsets of same size
		for(List<Itemset> itemsetsBySize : closedItemsetsBySize) {
			// sort them by increasing support order
			Collections.sort(itemsetsBySize, new Comparator<Itemset>(){
				public int compare(Itemset o1, Itemset o2) {
//					return o2.support - o1.support;  // DECREASING SUPPORT ORDER
					return o1.support - o2.support;   // INCREASING SUPPORT ORDER
				}
			});
		}
	}
	
	/**
	 * Sort the items in closed itemsets by TWU order.
	 */
	private void sortItemsInAllCHUIsByTWU() {
		// for CHUIs having the same size
		for(List<Itemset> itemsetsBySize : closedItemsetsBySize) {
			// for each CHUI
			for(Itemset itemset : itemsetsBySize) {
				// if the support equal to the number of transactions, 
				// we take note that the empty set is a CHUI
				if(itemset.support == transactionCount) {
					emptySetIsGHUIs = true;
				}
				// sort the items in the CHUI by TWU order
				insertionSort(itemset.itemset);
			}
		}
	}
	
	/**
	 * Implementation of Insertion sort for sorting an itemset by TWU order
	 * This has an average performance of O(n log n)
	 * @param a array of integers
	 */
	public void insertionSort(int [] a){
		for(int j=1; j< a.length; j++){
			int key = a[j];
			int i = j - 1;
			for(; i>=0 && compareItems(a[i], key)>0; i--){
				a[i+1] = a[i];
			}
			a[i+1] = key;
		}
	}
	
	// ======================================================
	// ======================================================
	
	
	
	/**
	 * Default constructor
	 */
	public AlgoGHUIMINER() {
		
	}

	/**
	 * Run the algorithm
	 * @param input the input file path
	 * @param output the output file path
	 * @param minUtility the minimum utility threshold
	 * @param closedItemsets 
	 * @param itemsInClosedItemsets 
	 * @throws IOException exception if error while writing the file
	 */
	public void runAlgorithm(String input, String output, int minUtility, List<List<Itemset>> closedItemsets, Set<Integer> itemsInClosedItemsets) throws IOException {
		// initialize variable to count the number of closure retrievals
		closureRetrievals = 0;
		
		// initialize the buffer for storing the current itemset
		itemsetBuffer = new int[BUFFERS_SIZE];
		
		// initialize the structured for EUCP strategy introduced in FHM algorithm (Fournier-Viger et al., 2014)
		// It will store the TWU of all pairs of items
		mapFMAP =  new HashMap<Integer, Map<Integer, Integer>>();
		
		// save the minutil threshold
		this.minUtility = minUtility;
		
		// record the start timestamp
		startTimestamp = System.currentTimeMillis();
		
		// reset memory usage
		MemoryLogger.getInstance().reset();

		// prepare object for writting result to a file
		writer = new BufferedWriter(new FileWriter(output));
		
		//  We create a  map to store the TWU of each item
		mapItemToTWU = new HashMap<Integer, Integer>();

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
					
					// NEW
					if(itemsInClosedItemsets.contains(item) == false) {
						continue;
					}
					// END NEW
					
					// get the current TWU of that item
					Integer twu = mapItemToTWU.get(item);
					// add the utility of the item in the current transaction to its twu
					twu = (twu == null)?  transactionUtility : twu + transactionUtility;
					mapItemToTWU.put(item, twu);
				}
				transactionCount++;
				totalUtility += transactionUtility;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
	    }
		
		listOfUtilityLists = new ArrayList<UtilityListWithCriticalObjects>();
		mapItemToUtilityList = new HashMap<Integer, UtilityListWithCriticalObjects>();
		
		// For each item
		for(Integer item: mapItemToTWU.keySet()){
			// if the item is promising  (TWU >= minutility)
			if(mapItemToTWU.get(item) >= minUtility){
				// create an empty Utility List that we will fill later.
				UtilityListWithCriticalObjects uList = new UtilityListWithCriticalObjects(item);
				mapItemToUtilityList.put(item, uList);
				// add the item to the list of high TWU items
				listOfUtilityLists.add(uList); 
				
			}
		}
		// SORT THE LIST OF HIGH TWU ITEMS IN ASCENDING ORDER
		Collections.sort(listOfUtilityLists, new Comparator<UtilityListWithCriticalObjects>(){
			public int compare(UtilityListWithCriticalObjects o1, UtilityListWithCriticalObjects o2) {
				// compare the TWU of the items
				return compareItems(o1.item, o2.item);
			}});
		
		
		// NEW
		this.closedItemsetsBySize = closedItemsets;
		sortClosedItemsets();
		sortItemsInAllCHUIsByTWU();
		// END NEW
		
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
				// the second part is the transaction utility
				int transactionUtility = Integer.parseInt(split[1]);  
				
				// Copy the transaction into lists but 
				// without items with TWU < minutility
				
				// This variable will store the transaction utility of the revised transaction
				int newTU = 0;  
				
				// Create a list to store items
				List<PairItemUtility> revisedTransaction = new ArrayList<PairItemUtility>();
				// for each item
				for(int i=0; i <items.length; i++){
					/// convert values to integers
					PairItemUtility pair = new PairItemUtility();
					pair.item = Integer.parseInt(items[i]);
					pair.utility = Integer.parseInt(utilityValues[i]);
					// if the item has enough utility we will keep it in the revised transaction
					Integer utility = mapItemToTWU.get(pair.item);
					if(utility != null && mapItemToTWU.get(pair.item) >= minUtility){
						// add the item to the revised transaction
						revisedTransaction.add(pair);
						newTU += pair.utility; // update transaction utility
					}else {
						transactionUtility = transactionUtility - pair.utility;
					}
				}
				
				// sort the transaction by TWU order
				Collections.sort(revisedTransaction, new Comparator<PairItemUtility>(){
					public int compare(PairItemUtility o1, PairItemUtility o2) {
						return compareItems(o1.item, o2.item);
					}});

								
				// for each item left in the revised transaction
				for(int i = 0; i< revisedTransaction.size(); i++){
					// get the item
					PairItemUtility pair =  revisedTransaction.get(i);

					// subtract the utility of this item from the remaining utility
					int remainingUtility = transactionUtility - pair.utility;  // MODIFICATINO ######$@#$@#$
					
					// get the utility list of this item
					UtilityListWithCriticalObjects utilityListOfItem = mapItemToUtilityList.get(pair.item);
					
					// Add a new Element to the utility list of this item corresponding to this transaction
					Element element = new Element(tid, pair.utility, remainingUtility);
					utilityListOfItem.addElement(element);
										
					// BEGIN CODE FOR UPDATING THE STRUCTURE USED BY THE EUCP STRATEGY
					// TO STORE TWU OF ALL PAIRS OF TWO ITEMS CO-OCCURRING
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
					// END OF CODE FOR EUCP STRATEGY
				}
				// increase tid number for next transaction
				tid++; 
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(myInput != null){ myInput.close();}
	    }
		
		// check the memory usage
		MemoryLogger.getInstance().checkMemory();
		
		// CREATE THE EMPTY SET AND ITS  BITSET
		BitSet tidsetEmptySet = new BitSet(transactionCount);
		tidsetEmptySet.set(1, transactionCount);
		UtilityListWithCriticalObjects emptyUL = new UtilityListWithCriticalObjects(null);
		emptyUL.tidset = tidsetEmptySet;
		emptyUL.crit = new BitSet[0];
		int emptySet[] = new int[0];
		
		// FOR EACH SINGLE ITEM
		Iterator<UtilityListWithCriticalObjects> iter = listOfUtilityLists.iterator();
		while (iter.hasNext()) {
			// Get its utility list and support
			UtilityListWithCriticalObjects ul = (UtilityListWithCriticalObjects) iter.next();
			int [] itemset = new int[] {ul.item};
			int support = ul.elements.size();
			
			// Check if the item is a generator by using its critical object
			checkIfGeneratorSingleItem(emptyUL, ul);
			
			// If the item is not a generator or has no high utility supersets.
			if(ul.crit == null || (ul.sumIutils + ul.sumRutils < minUtility)) {
				// then we remove it because no superset can be a GHUI
				iter.remove();
			}else if(ul.sumIutils >= minUtility){
				// If the item is a high utility itemset, then it is a GHUI so we save it
				writeOut(emptySet, 0, ul.item, ul.sumIutils, ul.elements.size());
				// If the item is not a subset of a CHUI, then its supersets are not GHUIs and
				// we can remove it
				if(isSubsetOfACHUI(itemset, 1, support, true) == false) {
					iter.remove();
				}
			}else if(getClosure(itemset, 1, support) != null) {
				// if the closure of the itemset is a CHUI, then the itemset is a LUG
				// and we output it
				writeOut(emptySet, 0, ul.item, ul.sumIutils, ul.elements.size());	
			}
		}

		// if the number of CHUIs is not 0, then
		if(closedItemsetsBySize.size()>0) {
			// if the empty is a GHUIs, we output it
			if(emptySetIsGHUIs) {
				writeOutEmptySet(totalUtility);
			}
			// Mine the database recursively to find GHUIs containing more than 1 item
			ghuiMinerE(itemsetBuffer, 0, emptyUL, listOfUtilityLists);
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
	 * This is the recursive method to find all GHUIs.
	 * @param prefixP  This is the current prefix. Initially, it is empty.
	 * @param p_UL This is the Utility List of the prefix. Initially, it is empty.
	 * @param extensionsULs The utility lists corresponding to each extension of the prefix.
	 * @param prefixLength The current prefix length
	 * @throws IOException
	 */
		
	private void ghuiMinerE(int [] prefixP, int prefixLength, UtilityListWithCriticalObjects p_UL, 
			List<UtilityListWithCriticalObjects> extensionsULs) throws IOException {

		MemoryLogger.getInstance().checkMemory();

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
				
				// This list will contain the utility lists of pX extensions.
				List<UtilityListWithCriticalObjects> extensionsOfPX = new ArrayList<UtilityListWithCriticalObjects>();
				
				// For each extension pY such that Y appear
				// after X according to the ascending order
				// we will consider pXY
				for(int j=i+1; j < extensionsULs.size(); j++){
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
						pXYUL = (p_UL.item == null) ? construct(pX_UL, pY_UL, minUtility) : construct(pX_UL, pY_UL, minUtility);
					
					// if empty utility list, skip it
					if(pXYUL == null || pXYUL.elements.isEmpty()) {
						continue;
					}
					
					// ======== OPTIMIZATION NOT IN DEFME  ===========
					// if the pattern has the same support as PX or PY, it is not a generator, WE SKIP IT
					if(  pXYUL.elements.size() == pX_UL.elements.size() 
					  || pXYUL.elements.size() == pY_UL.elements.size()) {
						partiallyAvoidedOrAvoidedGeneratorChecks++;
						continue;
					}

					// if pXY is not a subset of a CHUI, then it is not a GHUI
					// as well as all its supersets

					// We create new prefix pXY
					itemsetBuffer[prefixLength+1] = pY_UL.item;
//					int[] append = ArraysAlgos.appendIntegerToArray(newPrefix, pY_UL.item);
					if(isSubsetOfACHUI(itemsetBuffer, prefixLength+2, pXYUL.elements.size(), false) == false) {
						continue; 
					}
					
					// ===== PRUNING: IF THE PATTERN IS NOT HUI 
					// AND HAS NO HUI SUPERSETS WE SKIP IT  ====
					if(pXYUL.sumIutils + pXYUL.sumRutils < minUtility) {
						continue;
					}

					// CHECK IF THE PATTERN pXY IS A GENERATOR
					boolean isGenerator = checkIfGenerator(pX_UL, pXYUL, prefixLength+1);
					
					// IF pXY IS A GENERATOR
					if(isGenerator) {
						// If it is high utility
						if(pXYUL.sumIutils >= minUtility){
							// Then pXY is a HUG and we save it
							writeOut(itemsetBuffer, prefixLength+1, pXYUL.item, pXYUL.sumIutils, pXYUL.elements.size());
						}else if(getClosure(itemsetBuffer, prefixLength+2, pXYUL.elements.size()) != null) {
							// Otherwise, if its closure is a CHUI, then pXY is a LUG
							// and we save it
							writeOut(itemsetBuffer, prefixLength+1, pXYUL.item, pXYUL.sumIutils, pXYUL.elements.size());
						}
						// Since pXY is a generator, its supersets may be GHUIs,
						// so we keep pXY for further exploration
						extensionsOfPX.add(pXYUL);
					}
				}
				
				// We make a recursive call to discover all itemsets with the prefix pXab by combining 
				// pairs of 1-extensions of pX such as pXa and pXb
				if(extensionsOfPX.size() > 1) {
					ghuiMinerE(itemsetBuffer, prefixLength+1, pX_UL, extensionsOfPX); 
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
				candidateAvoidedbyFHM++;
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
			int cardinality = pX_UL.crit[j].cardinality();
//			System.out.println(cardinality);
			if(cardinality == 0){
				if(j < prefixSize -1) {
					partiallyAvoidedOrAvoidedGeneratorChecks++;
				}
				return false;
			}
			// =========== END OPTIMIZATION ===========
		}

// ===========  WITHOUT THE OPTIMIZATION WE WOULD DO THIS:  ========
//		for(BitSet covStarP : pX_UL.crit) {
//			if(covStarP.cardinality() == 0){
//				return false;
//			}
//		}
// =================================================================
		return true;
	}
	

	/**
	 * This method checks if an itemset X is a generator (optimized for the 
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
		generatorChecks++;

		// CALCULATE the critical objects of {X}
		BitSet tidsetE = mapItemToUtilityList.get(X_UL.item).tidset;
		BitSet crit = (BitSet) emptySet_UL.tidset.clone();
		crit.andNot(tidsetE);
		X_UL.crit = new BitSet[]{crit};
	}
	
	
	/**
	 * This method constructs the utility list of itemset {x,y} using the utility lists of
	 * itemsets {x} and {y} (done as in HUI-Miner).
	 * @param x_UL the utility list of {x}
	 * @param y_UL the utility list of {y}
	 * @param minUtility the minimum utility threshold
	 * @return the utility list of {x,y}
	 */
	private UtilityListWithCriticalObjects construct(UtilityListWithCriticalObjects px, UtilityListWithCriticalObjects py,
			int minUtility) {
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
			if(ey != null){
				// Create the new element
				Element eXY = new Element(ex.tid, ex.iutils + ey.iutils, ex.rutils - ey.iutils);
				// add the new element to the utility list of pXY
				pxyUL.addElement(eXY);
			}else {
				//== new optimization - LA-prune == /
				if(enableLAPrune) {
					totalUtility -= (ex.iutils+ex.rutils);
					if(totalUtility < minUtility) {
						return null;
					}
				}
				//====================
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

	
	
	private void writeOutEmptySet(long totalUtility) throws IOException {
		ghuiCount++; // increase the number of high utility itemsets found

		// write to file
		writer.write("#SUP: " + transactionCount + " #UTIL: " +totalUtility);
		writer.newLine();
	}
	
	/**
	 * Write a GHUI to the output file
	 * @param prefix the prefix of the HUG
	 * @param item  an item to be appended to the prefix
	 * @param sumIutils the utility of the itemset composed of prefix + item
	 * @param support the support of the itemset composed of prefix + item
	 * @param prefixLength The current prefix length
	 * @throws IOException if an error occurs while writing the itemset to file.
	 */
	private void writeOut(int[] prefix, int prefixLength, int item, long sumIutils, int support) throws IOException {
		ghuiCount++; // increase the number of high utility itemsets found
		
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
	 * Print statistics about the latest execution to System.out.
	 * @throws IOException 
	 */
	public void printStats() throws IOException {
		System.out.println("=============  GHUI-MINER - SPMF 0.97e - STATS =============");
		System.out.println("   Candidate count : "             + candidateCount  + "     (avoided by FHM : " + candidateAvoidedbyFHM 
				+ ")\n"
				+ "   Closure retrievals : " + closureRetrievals
				+ " \n" 
				+ "   Genenerator checks: " + generatorChecks
				+ "   (partially avoided : " + partiallyAvoidedOrAvoidedGeneratorChecks + ")");
		System.out.println(" Total time ~ "                  + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Memory ~ "                      + MemoryLogger.getInstance().getMaxMemory()+ " MB");
		System.out.println(" GHUI count : " + ghuiCount); 
		System.out.println("===================================================");
	}
}