package ca.pfv.spmf.algorithms.frequentpatterns.foshu;



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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This is an implementation of the "FHN" algorithm for High-Utility Itemsets 
 * Mining with negative profit values
 * as described in the conference paper : <br/><br/>
 * 
 * Fournier-Viger, P., Zida, S. (2015). FOSHU: Faster On-Shelf High Utility Itemset 
 * Mining– with or without negative unit profit. Proc. 30th Symposium on Applied 
 * Computing (ACM SAC 2015). ACM Press, 8 pages.
 *  
 * @see UtilityListFOSHU
 * @see ElementFOSHU
 * @author Philippe Fournier-Viger 2014
 */
public class AlgoFOSHU {

	// variable for statistics
	public double maxMemory = 0;     // the maximum memory usage
	public long startTimestamp = 0;  // the time the algorithm started
	public long endTimestamp = 0;   // the time the algorithm terminated
	public int huiCount =0;  // the number of HUI generated
	
	// the number of join operations performed by the algorithm
	public int joinCount =0;
	
	// the input file path
	public String input;
	
	// Map to remember the TWU of each item
	Map<Integer, Integer> mapItemToTWU;
	
	// Map to remember the TWU of each item
	int[] transactionsTU;
	
	// writer to write the output file 
	BufferedWriter writer = null;  
	
	// variable for debug mode
	boolean debug = false;
	
	//======================== FOR SCALABILITY EXPERIMENTS =====
	// The following line allows to specify how many lines from the input file should
	// be read. By default this parameter is set to Integer.MAX_VALUE (read the whole file).
	public int maxSEQUENCECOUNT = Integer.MAX_VALUE;
	
	//==================================================
	

	//===================== FOSHU ===========================
	// The set of negative items
	Set<Integer> negativeItems = null;
	// The minimum utility ratio
	double minUtilityRatio = 0;	
	
	//  The total utility of each period
	List<Integer> periodUtilities = null;
	//====================================================
	
	/**
	 * This class represents an item and its utility in a transaction
	 * @author Philippe Fournier-Viger
	 */
	class Pair{
		// the item
		int item = 0;
		// its utility in a transaction
		int utility = 0;
		
		/**
		 * Get a string representation of this object
		 * @return a string
		 */
		public String toString() {
			return "[" + item + "," + utility + "]";
		}
	}
	
	/**
	 * Default constructor
	 */
	public AlgoFOSHU() {
		
	}

	/**
	 * Run the algorithm
	 * @param input the input file path
	 * @param output the output file path
	 * @param minUtilityRatio the minimum utility ratio threshold
	 * @throws IOException exception if error while writing the file
	 */
	public void runAlgorithm(String input, String output, double minUtilityRatio) throws IOException {
		// reset maximum
		maxMemory =0;
		
		// keep the input file path
		this.input = input;
		
		//  keep the minimum utility ratio
		this.minUtilityRatio  = minUtilityRatio;
		
		// record the start time (to calculate the total runtime of the algorithm)
		startTimestamp = System.currentTimeMillis();
		
		//===================== FOSU ===========================
		// Create a set for storing negative items (items having a negative unit profit
		negativeItems = new HashSet<Integer>();
		
		// Create a list for storing the total utility of each period
		periodUtilities = new ArrayList<Integer>();
		//====================================================
		
		// Initialize writer object for saving results to a file
		writer = new BufferedWriter(new FileWriter(output));

		//  We create a  map to store the TWU of each item
		mapItemToTWU = new HashMap<Integer, Integer>();

		// this variable will count the number of transactions read
		int transactionCount = 0;
		
		// We scan the database a first time to calculate the TWU of each item.
		BufferedReader myInput = null;
		String thisLine;
		try {
			// prepare the object for reading the file
			myInput = new BufferedReader(new InputStreamReader( new FileInputStream(new File(input))));
			// for each line (transaction) until the end of file
			while ((thisLine = myInput.readLine()) != null
					&& transactionCount <= maxSEQUENCECOUNT) {
				// if the line is  a comment, is  empty or is a
				// kind of metadata
				if (thisLine.isEmpty() == true ||
						thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
								|| thisLine.charAt(0) == '@') {
					continue;
				}
				
				// count the number of transaction read
				transactionCount++;
				
				// split the transaction according to the : separator
				String split[] = thisLine.split(":"); 
				// the first part is the list of items
				String items[] = split[0].split(" "); 
				//===================== FHN ===========================
				// get the list of utility values corresponding to each item
				// for that transaction
				String utilityValues[] = split[2].split(" ");
				
				//****** BUG FIX 2016 *****
				// We will not trust the transaction utility value in the input file.
				// We will calculate it again.
				// First, we will convert the utility values to integer
				// and calculate the transaction utility WITH negative values
				//   (this is necessary to calculate the relative utility of itemsets)
				// and the transaction utility WITH only positive values
				//   (this is necessary to calculate the 
				//      RTWU upper-bound on the utility of itemsets
				int[] utilityValuesInt = new int[utilityValues.length];
				int transactionUtilityWithNegativeAndPositive = 0;
				int transactionUtilityWithPositive = 0;
				for(int j=0; j< utilityValues.length; j++){
					utilityValuesInt[j] = Integer.parseInt(utilityValues[j]);
					transactionUtilityWithNegativeAndPositive += utilityValuesInt[j];
					if(utilityValuesInt[j] > 0){
						transactionUtilityWithPositive += utilityValuesInt[j];
					}
				}
				//****** END BUG FIX 2016 *****
				
				// get the period id
				int period = Integer.parseInt(split[3]);
				//===============================================
				// the second part is the transaction utility
//				int transactionUtility = Integer.parseInt(split[1]);  
				int transactionUtility = transactionUtilityWithPositive;
				// for each item, we add the transaction utility to its TWU
				for(int i=0; i <items.length; i++){
					// convert item to integer
					Integer item = Integer.parseInt(items[i]);
					
					//===================== FOSHU ===========================
					// get the profit of the item in that transaction
					Integer itemUtility = utilityValuesInt[i];
					// if the profit is negative, we add the item to the set of negative items
					if(itemUtility < 0) {
						negativeItems.add(item);
					}
					//==================================
					
//					// get the current TWU of that item
					Integer twu = mapItemToTWU.get(item);
					// update the twu of that item by adding the current
					// transaction utility
					twu = (twu == null)? transactionUtility : twu + transactionUtility;
					mapItemToTWU.put(item, twu);
				}

				//===================== FOSHU ===========================
				// update period utility
				incrementPeriodUtility(period, transactionUtilityWithNegativeAndPositive);
				//==================================
			}
		} catch (Exception e) {
			// catches exception if error while reading the input file
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
	    }
		
		// CREATE A LIST TO STORE THE UTILITY LIST OF ITEMS
		List<UtilityListFOSHU> listOfUtilityLists = new ArrayList<UtilityListFOSHU>();
		
		// CREATE A MAP TO STORE THE UTILITY LIST FOR EACH ITEM.
		// Key : item    Value :  utility list associated to that item
		Map<Integer, UtilityListFOSHU> mapItemToUtilityList = new HashMap<Integer, UtilityListFOSHU>();
		
		// array to store the transaction utility of each transaction
		transactionsTU =  new int[transactionCount];
		
		// For each item
		for(Entry<Integer, Integer> entryItemEUtility: mapItemToTWU.entrySet()){
			// calculate the period utility and item estimated utility
			int item = entryItemEUtility.getKey();

			// create an empty Utility List that we will fill later.
			UtilityListFOSHU uList = new UtilityListFOSHU(item, periodUtilities.size());
			mapItemToUtilityList.put(item, uList);
			
			// add the item to the list of high TWU items
			listOfUtilityLists.add(uList); 
		}
		
		// SORT THE LIST OF HIGH TWU ITEMS IN ASCENDING ORDER
		Collections.sort(listOfUtilityLists, new Comparator<UtilityListFOSHU>(){
			public int compare(UtilityListFOSHU o1, UtilityListFOSHU o2) {
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
			while ((thisLine = myInput.readLine()) != null 
					&& tid <= maxSEQUENCECOUNT) {
				// if the line is  a comment, is  empty or is a
				// kind of metadata
				if (thisLine.isEmpty() == true ||
						thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
								|| thisLine.charAt(0) == '@') {
					continue;
				}
				
				// split the line according to the ":" separator
				String split[] = thisLine.split(":");
				// get the list of items
				String items[] = split[0].split(" ");
				// get the list of utility values corresponding to each item
				// for that transaction
				String utilityValues[] = split[2].split(" ");
				// get the period id
				int period = Integer.parseInt(split[3]);
				
				// Copy the transaction into lists but 
				// without items with TWU < minutility
				
				int remainingUtility =0;
				
				// Create a list to store items
				List<Pair> revisedTransaction = new ArrayList<Pair>();
				// for each item
				for(int i=0; i <items.length; i++){
					/// convert values to integers
					Pair pair = new Pair();
					pair.item = Integer.parseInt(items[i]);
					pair.utility = Integer.parseInt(utilityValues[i]);
					// if the item has enough utility
					
					// 
					// "IF THE ITEM IS PROMISING"
//					if(mapItemToUtilityList.get(pair.item) != null){  
						// add it
						revisedTransaction.add(pair);
						// ======= FOSHU (MODIF) ===========================
						// if the item has a positive profit value
						// we count its utility in the remaining utility
						// (but we don't do it if the item has a negative profit)
						if(!negativeItems.contains(pair.item)) {
							remainingUtility += pair.utility;
						}
						//================================================
//					}
				}
				
				// set the transaction utility
				transactionsTU[tid] = remainingUtility;
				
				// sort the transaction by TWU value
				Collections.sort(revisedTransaction, new Comparator<Pair>(){
					public int compare(Pair o1, Pair o2) {
						return compareItems(o1.item, o2.item);
					}});

								
				// for each item left in the transaction
				for(int i = 0; i< revisedTransaction.size(); i++){
					// get the item and its utility in that transaction
					Pair pair =  revisedTransaction.get(i);
					
					// subtract the utility of this item from the remaining utility
					// ======= FHN (MODIF) ===========================
					// if not a negative item
					if(remainingUtility != 0) {
						remainingUtility = remainingUtility - pair.utility;
					}
					
					// get the utility list of this item
					UtilityListFOSHU utilityListOfItem = mapItemToUtilityList.get(pair.item);

					// Add a new Element to the utility list of this item corresponding to this transaction
					if(pair.utility > 0) {
						ElementFOSHU element = new ElementFOSHU(tid, pair.utility, 0, remainingUtility);
						utilityListOfItem.addElement(element, period);
					}else {
						ElementFOSHU element = new ElementFOSHU(tid, 0, pair.utility, remainingUtility);
						utilityListOfItem.addElement(element, period);
					}
				}
				// increase tid number for next transaction
				tid++; 

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
		
		// REMOVE UNPROMISING ITEMS ACCORDING TO THE TWU IN EACH PERIOD
		Iterator<UtilityListFOSHU> iter= listOfUtilityLists.iterator();
		// For each utility-list
		while (iter.hasNext()) {
			UtilityListFOSHU X = (UtilityListFOSHU) iter.next();
			// We will check if the item X is promising in at least one period
			boolean isPromisingInAtLeastAPeriod = false;
			// for each period Z
			for(int z=0; z< periodUtilities.size(); z++) {
				// if X appears in that period
				if(X.appearsInPeriod(z)){
					// calculate the TWU of X in that period
					int twuX = 0;
					for(ElementFOSHU element: X.getElementsOfPeriod(z)) {
						twuX += transactionsTU[element.tid];
					}
					// if the TWU divided by the period utility is 
					// no less than the minutil ratio for that period
					if(calculateRelativeUtilityInPeriod(z, twuX) >= minUtilityRatio) {
						// than X is promising and we do not need to continue checking X
						isPromisingInAtLeastAPeriod = true;
						break;
					}
				}
			}
			// If itemset X is not promising
			if(isPromisingInAtLeastAPeriod == false) {
				// then we remove it because it cannot be part of any high utility itemset
				iter.remove();
			}
		}
		
		// Mine the database recursively
		foshu(new int[0], null, listOfUtilityLists);
		
		// check the memory usage again and close the file.
		checkMemory();
		// close output file
		writer.close();
		// record end time
		endTimestamp = System.currentTimeMillis();
	}

	/**
	 * Calculate the relative utility in a period
	 * @param z the period number
	 * @param utility  the utility value
	 * @return the relative utility
	 */
	private double calculateRelativeUtilityInPeriod(int z, double utility) {
		double x = Math.abs(((double) periodUtilities.get(z)));
		if(x == 0){
			return 0;
		}
		return utility / Math.abs(((double) periodUtilities.get(z)));
	}
	
	
	
	/**
	 * Calculate the relative utility 
	 * @param sumPeriodUtility the sum of the period utilities
	 * @param utility  the utility value
	 * @return the relative utility
	 */
	private double calculateRelativeUtility(int sumPeriodUtility, double utility) {
		if(sumPeriodUtility == 0){
			return 0;
		}
		return utility / Math.abs(sumPeriodUtility);
	}
	


	/**
	 * Method to compare items according to the total order of increasing TWU such that
	 * negative items are always last.
	 * @param item1 an item
	 * @param item2 another item
	 * @return 0 if the same item, >0 if item1 is larger than item2,  <0 otherwise
	 */
	private int compareItems(int item1, int item2) {
		//====================== FOSHU =======================
		// First check if the items are negative
		Boolean item1IsNegative = negativeItems.contains(item1);
		Boolean item2IsNegative = negativeItems.contains(item2);
		// if the first item is positive and the second one is negative
		if(!item1IsNegative && item2IsNegative) {
			return -1;
		}else if (item1IsNegative && !item2IsNegative)  {
			// if the first item is positive and the second one is negative
			return 1;
		}
		//=============================================
		// Otherwise, both items are positive or both are negative,
		// so we compare them by their TWU
		int compare = mapItemToTWU.get(item1) - mapItemToTWU.get(item2);
		// if the same, use the lexical order otherwise use the TWU
		return (compare == 0)? item1 - item2 :  compare;
	}
	
	/**
	 * This is the recursive method to find all high utility itemsets. It writes
	 * the itemsets to the output file.
	 * @param prefix  This is the current prefix. Initially, it is empty.
	 * @param pUL This is the Utility List of the prefix. Initially, it is empty.
	 * @parafm ULs The utility lists corresponding to each extension of the prefix.
	 * @param minUtility The minUtility ratio threshold (a double).
	 * @throws IOException
	 */
	private void foshu(int [] prefix, UtilityListFOSHU pUL, List<UtilityListFOSHU> ULs)
			throws IOException {
		
		// For each extension pU{X} of prefix P
		for(int i=0; i< ULs.size(); i++){     
			// Get the utility-list of X
			UtilityListFOSHU X = ULs.get(i);
			
			// Check if X is promising in at least one time period
			// and at the same time calculate the sum of the period utility of X
			int sumPeriodUtility = 0;
			boolean isPromisingInAtLeastOnePeriod = false;
			// for each period where X appears
			for(int z=0; z< periodUtilities.size(); z++) {
				// If X appears in that period
				if(X.appearsInPeriod(z)){
					// update the sum of the period utility
					sumPeriodUtility += periodUtilities.get(z);
					// We calculate the upper bound which is the sum
					// of utility and remaining utility and check if it is greater
					// than minutil. If yes, than X is promising.
					if(calculateRelativeUtilityInPeriod(z, X.getSumIRUtilsInPeriod(z)) >= minUtilityRatio) {
						isPromisingInAtLeastOnePeriod = true;
					}
				}
			}
			
			// Calculate the relative utility of p U {X}.
			double ru = 
					calculateRelativeUtility(sumPeriodUtility, (X.sumIutilP + X.sumIutilN));

			// if X is a on-shelf high utility itemset 
			// (if its relative utility is no less than minUtil)
			if(ru >= minUtilityRatio) {
				writeOut(prefix, X.item, X.sumIutilP + X.sumIutilN, ru);
			}
			
			// if X is not promising, then we will not explore its extensions, so we continue.
			if(isPromisingInAtLeastOnePeriod == false) {
				continue;
			}

			// This list will contain the utility lists of pX extensions.
			List<UtilityListFOSHU> exULs = new ArrayList<UtilityListFOSHU>();
			
			// Create the new itemset by concatenating the item of X.
			int [] newPrefix = new int[prefix.length+1];
			System.arraycopy(prefix, 0, newPrefix, 0, prefix.length);
			newPrefix[prefix.length] = X.item;
			
			// For each extension p U{y} of p appearing
			// after X according to the ascending order
			for(int j=i+1; j < ULs.size(); j++){
				UtilityListFOSHU Y = ULs.get(j);

				// increase the number of join operations for statistics purpose
				joinCount++;
				
				// create the utility list of p U {X,Y}
				UtilityListFOSHU pXY = new UtilityListFOSHU(periodUtilities.size(), pUL, X, Y);
				
				// We will check if  p U {X,Y} is promising in at least
				// one period
				boolean isPromisingInAtLeastAPeriod = false;
				// for each period 
				for(int z=0; z< periodUtilities.size(); z++) {
					// if p U {X,Y} appears in that period
					if(pXY.appearsInPeriod(z)){
						// Calculate the TWU in that period
						int twuXY = 0;
						// For each each period
						for(ElementFOSHU element: pXY.getElementsOfPeriod(z)) {
							// add the tWU
							twuXY += transactionsTU[element.tid];
						}
						
						// if the itemset p U {X,Y} is promising in that period
						if(calculateRelativeUtilityInPeriod(z, twuXY) >= minUtilityRatio) {
							// we take note of that and stop this loop
							isPromisingInAtLeastAPeriod = true;
							break;
						}
					}
				}
				
				// if the itemset p U {X,Y} is not promising
				// then we don't need to consider it anymore, in this depth first search.
				if(isPromisingInAtLeastAPeriod == true) {
					exULs.add(pXY);
				}
			}
			
			// We make a recursive call to discover all itemsets with the prefix p U {X,Y}
			foshu(newPrefix, X, exULs); 
		}
	}


	/**
	 * Method to write a high utility itemset to the output file.
	 * @param the prefix to be writent o the output file
	 * @param an item to be appended to the prefix
	 * @param utility the utility of the prefix concatenated with the item
	 * @param relativeUtility the relative utility of that itemset
	 */
	private void writeOut(int[] prefix, int item, int utility, double relativeUtility) throws IOException {
		huiCount++; // increase the number of high utility itemsets found
//		//Create a string buffer
		StringBuilder buffer = new StringBuilder();
		// append the prefix
		for (int i = 0; i < prefix.length; i++) {
			buffer.append(prefix[i]);
			buffer.append(' ');
		}
		// append the last item
		buffer.append(item);
		// append the utility value
		buffer.append(" #UTIL: ");
		buffer.append(utility);
		// append the relative utility value
		buffer.append(" #RUTIL: ");
		buffer.append(relativeUtility);
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
	 * Add the utility of a transaction to the total utility of that period
	 * @param period  the period (an integer)
	 * @param transactionUtility the utility of the transaction to be added
	 */
	private void incrementPeriodUtility(int period, int transactionUtility) {
		// check if the period was seen before
		boolean notSeenBefore = periodUtilities.size() < (period +1);
		if(notSeenBefore) {
			// add the utility 0 for all the period before this one that were not seen
			while(periodUtilities.size() < period) {
				periodUtilities.add(0);
			}
			// add the utility of the transaction for the current period
			periodUtilities.add(transactionUtility);
		}else {
			// sum the period utility with the transaction utility and save it 
			// as the period utility
			int sumUtility =  periodUtilities.get(period) + transactionUtility;
			periodUtilities.set(period, sumUtility);
		}
		
	}
	
	/**
	 * Print statistics about the latest execution to System.out.
	 * @throws IOException 
	 */
	public void printStats() throws IOException {
		System.out.println("=============  FOSHU ALGORITHM v2.02 - STATS =============");
		System.out.println("Dataset : " + input);
		System.out.println(" Total time ~ "                  + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Memory ~ "                      + maxMemory+ " MB");
		System.out.println(" HOU count : " + huiCount); 
		System.out.println(" Join count : "             + joinCount);
		System.out.println("===================================================");
	}

}