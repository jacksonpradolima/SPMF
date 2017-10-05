package   ca.pfv.spmf.algorithms.frequentpatterns.efim;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ca.pfv.spmf.tools.MemoryLogger;


/* This file is copyright (c) 2012-2015 Souleymane Zida & Philippe Fournier-Viger
* 
* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
* 
* SPMF is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/

/**
 * This is an implementation of the EFIM algorithm for
 * mining high-utility itemsets from a transaction database.
 * More information on the EFIM algorithm can be found in that paper: <br\>
 *
 * @author Souleymane Zida, Philippe Fournier-Viger using some code by Alan Souza
 */
public class AlgoEFIM {

	/** the set of high-utility itemsets */
    private Itemsets highUtilityItemsets;
    
	/** object to write the output file */
	BufferedWriter writer = null;
	
	/** the number of high-utility itemsets found (for statistics) */
	private int patternCount; 

	/** the start time and end time of the last algorithm execution */
	long startTimestamp;
	long endTimestamp;
	
	/** the minutil threshold */
	int minUtil;
	
	/** if this variable is set to true, some debugging information will be shown */
    final boolean  DEBUG = false;
	
    /** The following variables are the utility-bins array 
	// Recall that each bucket correspond to an item */
    /** utility bin array for sub-tree utility */
	private int[] utilityBinArraySU;   
	/** utility bin array for local utility */
	private int[] utilityBinArrayLU; 
	
	/** a temporary buffer */
	private int [] temp= new int [500];
	
	/** The total time spent for performing intersections */
	long timeIntersections;
	/** The total time spent for performing database reduction */
	long timeDatabaseReduction;
	/** The total time spent for identifying promising items */
	long timeIdentifyPromisingItems;
	/** The total time spent for sorting */
	long timeSort;
	/** The total time spent for binary search */
	long timeBinarySearch;
	

	/** an array that map an old item name to its new name */
    int[] oldNameToNewNames;
    /** an array that map a new item name to its old name */
    int[] newNamesToOldNames;
    /** the number of new items */
    int newItemCount;

    /** if true, transaction merging will be performed by the algorithm */
    boolean activateTransactionMerging;

    /** A parameter for transaction merging*/
    final int MAXIMUM_SIZE_MERGING = 1000;
    
    /** number of times a transaction was read */
    long transactionReadingCount;
    /** number of merges */
    long mergeCount;

    /** number of itemsets from the search tree that were considered */
	private long candidateCount;

	/** If true, sub-tree utility pruning will be performed */
	private boolean activateSubtreeUtilityPruning;
    
	/** 
	 * Constructor
	 */
    public AlgoEFIM() {
         
    }

    /**
     * Run the algorithm
     * @param minUtil  the minimum utility threshold (a positive integer)
     * @param inputPath  the input file path
     * @param outputPath  the output file path to save the result or null if to be kept in memory
     * @param activateTransactionMerging 
     * @param activateSubtreeUtilityPruning 
     * @param maximumTransactionCount
       * @return the itemsets or null if the user choose to save to file
     * @throws IOException if exception while reading/writing to file
     */
    public Itemsets runAlgorithm(int minUtil, String inputPath, String outputPath, boolean activateTransactionMerging, int maximumTransactionCount, boolean activateSubtreeUtilityPruning) throws IOException {
    	
    	// reset variables for statistics
    	mergeCount=0;
    	transactionReadingCount=0;
		timeIntersections = 0;
		timeDatabaseReduction = 0;
    	
    	// save parameters about activating or not the optimizations
    	this.activateTransactionMerging = activateTransactionMerging;
    	this.activateSubtreeUtilityPruning = activateSubtreeUtilityPruning;
    	
		// record the start time
		startTimestamp = System.currentTimeMillis();
		
		// read the input file
		Dataset dataset = new Dataset(inputPath, maximumTransactionCount);

		// save minUtil value selected by the user
		this.minUtil = minUtil;

		// if the user choose to save to file
		// create object for writing the output file
		if(outputPath != null) {
			writer = new BufferedWriter(new FileWriter(outputPath));
		}else {
			// if the user choose to save to memory
			writer = null;
	        this.highUtilityItemsets = new Itemsets("Itemsets");
		}
		
		// reset the number of itemset found
		patternCount = 0;
		
		// reset the memory usage checking utility
		MemoryLogger.getInstance().reset();
		
		// if in debug mode, show the initial database in the console
		if(DEBUG)
		{
			System.out.println("===== Initial database === ");
			System.out.println(dataset.toString());
		}
		
	    // Scan the database using utility-bin array to calculate the TWU
		// of each item
        useUtilityBinArrayToCalculateLocalUtilityFirstTime(dataset);
        
       // if in debug mode, show the TWU calculated using the utility-bin array
       if(DEBUG) 
       {
	    	System.out.println("===== TWU OF SINGLE ITEMS === ");
			for (int i = 1; i < utilityBinArrayLU.length; i++)
			{
				System.out.println("item : " + i + " twu: " + utilityBinArrayLU[i]);
			}
			System.out.println();
       }

	   	// Now, we keep only the promising items (those having a twu >= minutil)
	   	List<Integer> itemsToKeep = new ArrayList<Integer>();
	   	for(int j=1; j< utilityBinArrayLU.length;j++) {
	   		if(utilityBinArrayLU[j] >= minUtil) {
	   			itemsToKeep.add(j);
	   		}
	   	}
	   	
	  // Sort promising items according to the increasing order of TWU
	   insertionSort(itemsToKeep, utilityBinArrayLU);
       
	   // Rename promising items according to the increasing order of TWU.
	   // This will allow very fast comparison between items later by the algorithm
	   // This structure will store the new name corresponding to each old name
       oldNameToNewNames = new int[dataset.getMaxItem() + 1];
       // This structure will store the old name corresponding to each new name
       newNamesToOldNames = new int[dataset.getMaxItem() + 1];
       // We will now give the new names starting from the name "1"
       int currentName = 1;
       // For each item in increasing order of TWU
       for (int j=0; j< itemsToKeep.size(); j++)
	   {
    	   // get the item old name
    	   int item = itemsToKeep.get(j);
    	   // give it the new name
		   oldNameToNewNames[item] = currentName;
		   // remember its old name
		   newNamesToOldNames[currentName] = item;
		   // replace its old name by the new name in the list of promising items
		   itemsToKeep.set(j, currentName);
		   // increment by one the current name so that 
		   currentName++;
	   }
       
        // remember the number of promising item
		newItemCount = itemsToKeep.size();
		// initialize the utility-bin array for counting the subtree utility
		utilityBinArraySU = new int[newItemCount + 1]; 

		// if in debug mode, print to the old names and new names to the console
		// to check if they are correct
		if (DEBUG) {
			System.out.println(itemsToKeep);
			System.out.println(Arrays.toString(oldNameToNewNames));
			System.out.println(Arrays.toString(newNamesToOldNames));
		}

        // We now loop over each transaction from the dataset
		// to remove unpromising items
    	for(int i=0; i< dataset.getTransactions().size();i++)
    	{
    		// Get the transaction
    		Transaction transaction  = dataset.getTransactions().get(i);

    		// Remove unpromising items from the transaction and at the same time
    		// rename the items in the transaction according to their new names
    		// and sort the transaction by increasing TWU order
    		transaction.removeUnpromisingItems(oldNameToNewNames);
    	}
    	
    	// Now we will sort transactions in the database according to the proposed
    	// total order on transaction (the lexicographical order when transactions
    	// are read backward).
    	long timeStartSorting = System.currentTimeMillis();
    	// We only sort if transaction merging is activated
    	if(activateTransactionMerging){
    		// Sort the dataset using a new comparator
    		Collections.sort(dataset.getTransactions(), new Comparator<Transaction>(){
				@Override
				/**
				 * Compare two transactions
				 */
				public int compare(Transaction t1, Transaction t2) {
					// we will compare the two transaction items by items starting
					// from the last items.
					int pos1 = t1.items.length - 1;
					int pos2 = t2.items.length - 1;
					
					// if the first transaction is smaller than the second one
					if(t1.items.length < t2.items.length){
						// while the current position in the first transaction is >0
						while(pos1 >=0){
							int subtraction = t2.items[pos2]  - t1.items[pos1];
							if(subtraction !=0){
								return subtraction;
							}
							pos1--;
							pos2--;
						}
						// if they ware the same, they we compare based on length
						return -1;
						
					// else if the second transaction is smaller than the first one
					}else if (t1.items.length > t2.items.length){
						// while the current position in the second transaction is >0
						while(pos2 >=0){
							int subtraction = t2.items[pos2]  - t1.items[pos1];
							if(subtraction !=0){
								return subtraction;
							}
							pos1--;
							pos2--;
						}
						// if they are the same, they we compare based on length
						return 1;

					}else{
					// else if both transactions have the same size
						while(pos2 >=0){
							int subtraction = t2.items[pos2]  - t1.items[pos1];
							if(subtraction !=0){
								return subtraction;
							}
							pos1--;
							pos2--;
						}
						// if they ware the same, they we compare based on length
						return 0;
					}
				}

    		});
    		
        	// =======================REMOVE EMPTY TRANSACTIONS==========================
        	// After removing unpromising items, it may be possible that some transactions
        	// are empty. We will now remove these transactions from the database.
    		int emptyTransactionCount = 0;
    		// for each transaction
        	for(int i=0; i< dataset.getTransactions().size();i++)
        	{
        		// if the transaction length is 0, increase the number of empty transactions
        		Transaction transaction  = dataset.getTransactions().get(i);
        		if(transaction.items.length == 0){
        			emptyTransactionCount++;
        		}
        	}
        	// To remove empty transactions, we just ignore the first transactions from the dataset
        	// The reason is that empty transactions are always at the begining of the dataset 
        	// since transactions are sorted by size 
        	dataset.transactions = dataset.transactions.subList(emptyTransactionCount, dataset.transactions.size());
        	
    	}
    	

    	// record the total time spent for sorting
    	timeSort = System.currentTimeMillis() - timeStartSorting;
    	
    	// if in debug mode, print the database after sorting and removing promising items
		if(DEBUG)
		{
			System.out.println("===== Database without unpromising items and sorted by TWU increasing order === ");
			System.out.println(dataset.toString());
		}
		
		// Use an utility-bin array to calculate the sub-tree utility of each item
    	useUtilityBinArrayToCalculateSubtreeUtilityFirstTime(dataset);
    	
    	// Calculate the set of items that pass the sub-tree utility pruning condition
		List<Integer> itemsToExplore = new ArrayList<Integer>();
		// if subtree utility pruning is activated
		if(activateSubtreeUtilityPruning){
			// for each item
			for(Integer item : itemsToKeep){
				// if the subtree utility is higher or equal to minutil, then keep it
				if (utilityBinArraySU[item] >= minUtil) {
					itemsToExplore.add(item);
				}
			}
    	}

		// If in debug mode, show the list of promising items
    	if(DEBUG)
    	{
	       	System.out.println("===== List of promising items === ");
	       	System.out.println(itemsToKeep);
    	}

//    	//======
        // Recursive call to the algorithm
       	// If subtree utility pruning is activated
    	if(activateSubtreeUtilityPruning){
    		// We call the recursive algorithm with the database, secondary items and primary items
    		backtrackingEFIM(dataset.getTransactions(), itemsToKeep, itemsToExplore, 0);
    	}else{
    		// We call the recursive algorithm with the database and secondary items
    		backtrackingEFIM(dataset.getTransactions(), itemsToKeep, itemsToKeep, 0);
    	}

		// record the end time
		endTimestamp = System.currentTimeMillis();
		
		//close the output file
		if(writer != null) {
			writer.close();
		}
		
		// check the maximum memory usage
		MemoryLogger.getInstance().checkMemory();
        
		// return the set of high-utility itemsets
        return highUtilityItemsets;
    }

	/**
	 * Implementation of Insertion sort for sorting a list of items by increasing order of TWU.
	 * This has an average performance of O(n log n)
	 * @param items list of integers to be sorted
	 * @param items list the utility-bin array indicating the TWU of each item.
	 */
	public static void insertionSort(List<Integer> items, int [] utilityBinArrayTWU){
		// the following lines are simply a modified an insertion sort
		
		for(int j=1; j< items.size(); j++){
			Integer itemJ = items.get(j);
			int i = j - 1;
			Integer itemI = items.get(i);
			
			// we compare the twu of items i and j
			int comparison = utilityBinArrayTWU[itemI] - utilityBinArrayTWU[itemJ];
			// if the twu is equal, we use the lexicographical order to decide whether i is greater
			// than j or not.
			if(comparison == 0){
				comparison = itemI - itemJ;
			}
			
			while(comparison > 0){
				items.set(i+1, itemI);

				i--;
				if(i<0){
					break;
				}
				
				itemI = items.get(i);
				comparison = utilityBinArrayTWU[itemI] - utilityBinArrayTWU[itemJ];
				// if the twu is equal, we use the lexicographical order to decide whether i is greater
				// than j or not.
				if(comparison == 0){
					comparison = itemI - itemJ;
				}
			}
			items.set(i+1,itemJ);
		}
	}
    
    /**
     * Recursive method to find all high-utility itemsets
     * @param the list of transactions containing the current prefix P
	 * @param itemsToKeep the list of secondary items in the p-projected database
	 * @param itemsToExplore the list of primary items in the p-projected database
	 * @param the current prefixLength
     * @throws IOException if error writing to output file
     */
    private void backtrackingEFIM( List<Transaction> transactionsOfP,
    		List<Integer> itemsToKeep, List<Integer> itemsToExplore, int prefixLength) throws IOException {

    	// update the number of candidates explored so far
		candidateCount += itemsToExplore.size();
    	
        // ========  for each frequent item  e  =============
		for (int j = 0; j < itemsToExplore.size(); j++) {
			Integer e = itemsToExplore.get(j);

			// ========== PERFORM INTERSECTION =====================
			// Calculate transactions containing P U {e} 
			// At the same time project transactions to keep what appears after "e"
	        List<Transaction> transactionsPe = new ArrayList<Transaction>();
	        
	        // variable to calculate the utility of P U {e}
			int utilityPe = 0;		

			// For merging transactions, we will keep track of the last transaction read
			// and the number of identical consecutive transactions
			Transaction previousTransaction = null;
			int consecutiveMergeCount = 0;
			
	        // this variable is to record the time for performing intersection
			long timeFirstIntersection = System.currentTimeMillis();
			
			// For each transaction
	        for(Transaction transaction : transactionsOfP) {
	        	// Increase the number of transaction read
	        	transactionReadingCount++;
	        	
	        	// To record the time for performing binary searh
	        	long timeBinaryLocal = System.currentTimeMillis();
	        	
	        	// we remember the position where e appears.
	        	// we will call this position an "offset"
	        	int positionE = -1;
	        	// Variables low and high for binary search
	    		int low = transaction.offset;
	    		int high = transaction.items.length - 1;

	    		// perform binary search to find e in the transaction
	    		while (high >= low ) {
	    			int middle = (low + high) >>> 1; // divide by 2
	    			if (transaction.items[middle] < e) {
	    				low = middle + 1;
	    			}else if (transaction.items[middle] == e) {
	    				positionE =  middle;
	    				break;
	    			}  else{
	    				high = middle - 1;
	    			}
	    		}
	    		// record the time spent for performing the binary search
	        	timeBinarySearch +=  System.currentTimeMillis() - timeBinaryLocal;
	        	
//	        	if(prefixLength == 0 && newNamesToOldNames[e] == 385) {
//		        	for(int i=0; i < transaction.getItems().length; i++) {
//		        		if(transaction.getItems()[i] == e) {
//		        			innerSum += transaction.getUtilities()[i];
//		        		}
//		        	}
//		        }
	        	
	        	// if 'e' was found in the transaction
	            if (positionE > -1  ) { 
	 
	            	// optimization: if the 'e' is the last one in this transaction,
	            	// we don't keep the transaction
					if(transaction.getLastPosition() == positionE){
						// but we still update the sum of the utility of P U {e}
						utilityPe  += transaction.utilities[positionE] + transaction.prefixUtility;
					}else{
						// otherwise
		            	if(activateTransactionMerging && MAXIMUM_SIZE_MERGING >= (transaction.items.length - positionE)){
			            	// we cut the transaction starting from position 'e'
							Transaction projectedTransaction = new Transaction(transaction, positionE);
							utilityPe  += projectedTransaction.prefixUtility;
							
							// if it is the first transaction that we read
							if(previousTransaction == null){
								// we keep the transaction in memory 
								previousTransaction = projectedTransaction;
							}else if (isEqualTo(projectedTransaction, previousTransaction)){
								// If it is not the first transaction of the database and 
								// if the transaction is equal to the previously read transaction,
								// we will merge the transaction with the previous one
								
								// increase the number of consecutive transactions merged
								mergeCount++;
								
								// if the first consecutive merge
								if(consecutiveMergeCount == 0){
									// copy items and their profit from the previous transaction
									int itemsCount = previousTransaction.items.length - previousTransaction.offset;
									int[] items = new int[itemsCount];
									System.arraycopy(previousTransaction.items, previousTransaction.offset, items, 0, itemsCount);
									int[] utilities = new int[itemsCount];
									System.arraycopy(previousTransaction.utilities, previousTransaction.offset, utilities, 0, itemsCount);
									
									// make the sum of utilities from the previous transaction
							    	int positionPrevious = 0;
									int positionProjection = projectedTransaction.offset;
									while(positionPrevious < itemsCount){
										utilities[positionPrevious] += projectedTransaction.utilities[positionProjection];
										positionPrevious++;
										positionProjection++;
									}
									
									// make the sum of prefix utilities
									int sumUtilities = previousTransaction.prefixUtility += projectedTransaction.prefixUtility;
									
									// create the new transaction replacing the two merged transactions
									previousTransaction = new Transaction(items, utilities, previousTransaction.transactionUtility + projectedTransaction.transactionUtility);
									previousTransaction.prefixUtility = sumUtilities;	
	
								}else{
									// if not the first consecutive merge
	
									// add the utilities in the projected transaction to the previously
									// merged transaction
							    	int positionPrevious = 0;
									int positionProjected = projectedTransaction.offset;
									int itemsCount = previousTransaction.items.length;
									while(positionPrevious < itemsCount){
										previousTransaction.utilities[positionPrevious] += projectedTransaction.utilities[positionProjected];
										positionPrevious++;
										positionProjected++;
									}
									
									// make also the sum of transaction utility and prefix utility
									previousTransaction.transactionUtility += projectedTransaction.transactionUtility;
									previousTransaction.prefixUtility += projectedTransaction.prefixUtility;	
								}
								// increment the number of consecutive transaction merged
								consecutiveMergeCount++;
							}else{
								// if the transaction is not equal to the preceding transaction
								// we cannot merge it so we just add it to the database
								transactionsPe.add(previousTransaction);
								// the transaction becomes the previous transaction
								previousTransaction = projectedTransaction;
								// and we reset the number of consecutive transactions merged
								consecutiveMergeCount = 0;
							}
						}else{
			            	// Otherwise, if merging has been deactivated
							// then we just create the projected transaction
							Transaction projectedTransaction = new Transaction(transaction, positionE);
							// we add the utility of Pe in that transaction to the total utility of Pe
							utilityPe  += projectedTransaction.prefixUtility;
							// we put the projected transaction in the projected database of Pe
							transactionsPe.add(projectedTransaction);
						}
					}
					// This is an optimization for binary search:
					// we remember the position of E so that for the next item, we will not search
					// before "e" in the transaction since items are visited in lexicographical order
		            transaction.offset = positionE;   
	            }else{
					// This is an optimization for binary search:
					// we remember the position of E so that for the next item, we will not search
					// before "e" in the transaction since items are visited in lexicographical order
	            	transaction.offset = low;
	            }
	        }
	        // remember the total time for peforming the database projection
	        timeIntersections += (System.currentTimeMillis() - timeFirstIntersection);

	        // Add the last read transaction to the database if there is one
	        if(previousTransaction != null){
	        	transactionsPe.add(previousTransaction);
	        }
	        
	        // Append item "e" to P to obtain P U {e}
	        // but at the same time translate from new name of "e"  to its old name
	        temp[prefixLength] = newNamesToOldNames[e];
     
	        // if the utility of PU{e} is enough to be a high utility itemset
	        if(utilityPe  >= minUtil)
	        {
	        	// output PU{e}
	        	output(prefixLength, utilityPe );
	        }

			//==== Next, we will calculate the Local Utility and Sub-tree utility of
	        // all items that could be appended to PU{e} ====
	        useUtilityBinArraysToCalculateUpperBounds(transactionsPe, j, itemsToKeep);  
			
	        // we now record time for identifying promising items
			long initialTime = System.currentTimeMillis();
			
			// We will create the new list of secondary items
			List<Integer> newItemsToKeep = new ArrayList<Integer>();
			// We will create the new list of primary items
			List<Integer> newItemsToExplore = new ArrayList<Integer>();
			
			// for each item
	    	for (int k = j+1; k < itemsToKeep.size(); k++) {
	        	Integer itemk =  itemsToKeep.get(k);
	        	
	        	// if the sub-tree utility is no less than min util
	            if(utilityBinArraySU[itemk] >= minUtil) {
	            	// and if sub-tree utility pruning is activated
	            	if(activateSubtreeUtilityPruning){
	            		// consider that item as a primary item
	            		newItemsToExplore.add(itemk);
	            	}
	            	// consider that item as a secondary item
	            	newItemsToKeep.add(itemk);
	            }else if(utilityBinArrayLU[itemk] >= minUtil)
	            {
	            	// otherwise, if local utility is no less than minutil,
	            	// consider this itemt to be a secondary item
	            	newItemsToKeep.add(itemk);
	            }
	        }
	    	// update the total time  for identifying promising items
	    	timeIdentifyPromisingItems +=  (System.currentTimeMillis() -  initialTime);
			
			// === recursive call to explore larger itemsets
	    	if(activateSubtreeUtilityPruning){
	    		// if sub-tree utility pruning is activated, we consider primary and secondary items
	    		backtrackingEFIM(transactionsPe, newItemsToKeep, newItemsToExplore,prefixLength+1);
	    	}else{
	    		// if sub-tree utility pruning is deactivated, we consider secondary items also
	    		// as primary items
	    		backtrackingEFIM(transactionsPe, newItemsToKeep, newItemsToKeep,prefixLength+1);
	    	}
		}

		// check the maximum memory usage for statistics purpose
		MemoryLogger.getInstance().checkMemory();
    }


    /**
     * Check if two transaction are identical
     * @param t1  the first transaction
     * @param t2  the second transaction
     * @return true if they are equal
     */
    private boolean isEqualTo(Transaction t1, Transaction t2) {
    	// we first compare the transaction lenghts
		int length1 = t1.items.length - t1.offset;
		int length2 = t2.items.length - t2.offset;
		// if not same length, then transactions are not identical
    	if(length1 != length2){
    		return false;
    	}
    	// if same length, we need to compare each element position by position,
    	// to see if they are the same
    	int position1 = t1.offset;
		int position2 = t2.offset;
		
		// for each position in the first transaction
		while(position1 < t1.items.length){
			// if different from corresponding position in transaction 2
			// return false because they are not identical
			if(t1.items[position1]  != t2.items[position2]){
				return false;
			}
			// if the same, then move to next position
			position1++;
			position2++;
		}
		// if all items are identical, then return to true
		return true;
	}

	/**
	 * Scan the initial database to calculate the local utility of each item
	 * using a utility-bin array
	 * @param dataset the transaction database
	 */
	public void useUtilityBinArrayToCalculateLocalUtilityFirstTime(Dataset dataset) {

		// Initialize utility bins for all items
		utilityBinArrayLU = new int[dataset.getMaxItem() + 1];

		// Scan the database to fill the utility bins
		// For each transaction
		for (Transaction transaction : dataset.getTransactions()) {
			// for each item
			for(Integer item: transaction.getItems()) {
				// we add the transaction utility to the utility bin of the item
				utilityBinArrayLU[item] += transaction.transactionUtility;
			}
		}
	}
	
	/**
	 * Scan the initial database to calculate the sub-tree utility of each item
	 * using a utility-bin array
	 * @param dataset the transaction database
	 */
	public void useUtilityBinArrayToCalculateSubtreeUtilityFirstTime(Dataset dataset) {

		int sumSU;
		// Scan the database to fill the utility-bins of each item
		// For each transaction
		for (Transaction transaction : dataset.getTransactions()) {
			// We will scan the transaction backward. Thus,
			// the current sub-tree utility in that transaction is zero 
			// for the last item of the transaction.
			sumSU = 0;

			// For each item when reading the transaction backward
    	   for(int i = transaction.getItems().length-1; i >=0; i--) {
    		   // get the item
    		   Integer item = transaction.getItems()[i];

    		   // we add the utility of the current item to its sub-tree utility
    		   sumSU += transaction.getUtilities()[i];
    		   // we add the current sub-tree utility to the utility-bin of the item
    		   utilityBinArraySU[item] += sumSU;
        	}
		}
	}

    /**
     * Utilize the utility-bin arrays to calculate the sub-tree utility and local utility of all
     * items that can extend itemset P U {e}
     * @param transactions the projected database for P U {e}
     * @param j the position of j in the list of promising items
     * @param itemsToKeep the list of promising items
     */
    private void useUtilityBinArraysToCalculateUpperBounds(List<Transaction> transactionsPe, 
    		int j, List<Integer> itemsToKeep) {

    	// we will record the time used by this method for statistics purpose
		long initialTime = System.currentTimeMillis();
		
		// For each promising item > e according to the total order
		for (int i = j + 1; i < itemsToKeep.size(); i++) {
			Integer item = itemsToKeep.get(i);
			// We reset the utility bins of that item for computing the sub-tree utility and
			// local utility
			utilityBinArraySU[item] = 0;
			utilityBinArrayLU[item] = 0;
		}

		int sumRemainingUtility;
		// for each transaction
		for (Transaction transaction : transactionsPe) {
			// count the number of transactions read
			transactionReadingCount++;
			
			// We reset the sum of reamining utility to 0;
			sumRemainingUtility = 0;
			// we set high to the last promising item for doing the binary search
			int high = itemsToKeep.size() - 1;

			// for each item in the transaction that is greater than i when reading the transaction backward
			// Note: >= is correct here. It should not be >.
			for (int i = transaction.getItems().length - 1; i >= transaction.offset; i--) {
				// get the item
				int item = transaction.getItems()[i];
				
				// We will check if this item is promising using a binary search over promising items.

				// This variable will be used as a flag to indicate that we found the item or not using the binary search
				boolean contains = false;
				// we set "low" for the binary search to the first promising item position
				int low = 0;

				// do the binary search
				while (high >= low) {
					int middle = (low + high) >>> 1; // divide by 2
					int itemMiddle = itemsToKeep.get(middle);
					if (itemMiddle == item) {
						// if we found the item, then we stop
						contains = true;
						break;
					} else if (itemMiddle < item) {
						low = middle + 1;
					} else {
						high = middle - 1;
					}  
				}
				// if the item is promising
				if (contains) {
					// We add the utility of this item to the sum of remaining utility
					sumRemainingUtility += transaction.getUtilities()[i];
					// We update the sub-tree utility of that item in its utility-bin
					utilityBinArraySU[item] += sumRemainingUtility + transaction.prefixUtility;
					// We update the local utility of that item in its utility-bin
					utilityBinArrayLU[item] += transaction.transactionUtility + transaction.prefixUtility;
				}
			}
		}
		// we update the time for database reduction for statistics purpose
		timeDatabaseReduction += (System.currentTimeMillis() - initialTime);
    }



    /**
     * Save a high-utility itemset to file or memory depending on what the user chose.
     * @param itemset the itemset
     * @throws IOException if error while writting to output file
     */
    private void output(int tempPosition, int utility) throws IOException {
        patternCount++;
            
        	// if user wants to save the results to memory
		if (writer == null) {
			// we copy the temporary buffer into a new int array
			int[] copy = new int[tempPosition+1];
			System.arraycopy(temp, 0, copy, 0, tempPosition+1);
			// we create the itemset using this array and add it to the list of itemsets
			// found until now
			highUtilityItemsets.addItemset(new Itemset(copy, utility),copy.length); 
		} else {
			// if user wants to save the results to file
			// create a stringuffer
			StringBuffer buffer = new StringBuffer();
			// append each item from the itemset to the stringbuffer, separated by spaces
			for (int i = 0; i <= tempPosition; i++) {
				buffer.append(temp[i]);
				if (i != tempPosition) {
					buffer.append(' ');
				}
			}
			// append the utility of the itemset
			buffer.append(" #UTIL: ");
			buffer.append(utility);
			
			// write the stringbuffer to file and create a new line
			// so that we are ready for writing the next itemset.
			writer.write(buffer.toString());
			writer.newLine();
		}
    }


 
 
    /**
     * Print statistics about the latest execution of the EFIM algorithm.
     */
	public void printStats() {

		System.out.println("========== EFIM v97 - STATS ============");
		System.out.println(" minUtil = " + minUtil);
		System.out.println(" High utility itemsets count: " + patternCount);	
		System.out.println(" Total time ~: " + (endTimestamp - startTimestamp)
				+ " ms");
		// if in debug mode, we show more information
		if(DEBUG) {
			System.out.println(" Transaction merge count ~: " + mergeCount);	
			System.out.println(" Transaction read count ~: " + transactionReadingCount);	
			
			System.out.println(" Time intersections ~: " + timeIntersections
					+ " ms");	
			System.out.println(" Time database reduction ~: " + timeDatabaseReduction
					+ " ms");
			System.out.println(" Time promising items ~: " + timeIdentifyPromisingItems
					+ " ms");
			System.out.println(" Time binary search ~: " + timeBinarySearch
					+ " ms");
			System.out.println(" Time sort ~: " + timeSort	+ " ms");
		}
		System.out.println(" Max memory:" + MemoryLogger.getInstance().getMaxMemory());
		System.out.println(" Candidate count : "             + candidateCount);
		System.out.println("=====================================");
	}
}
