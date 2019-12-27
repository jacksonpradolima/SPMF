package   ca.pfv.spmf.algorithms.frequentpatterns.lcm;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemset;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;
import ca.pfv.spmf.tools.MemoryLogger;

/* This file is copyright (c) 2012-2014 Alan Souza
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
 * This is an implementation of the LCMFreq algorithm for
 * mining frequent itemsets from a transaction database.
 * More information on the LCMFreq algorithm can be found in papers by
 * T. Uno, such as: <br/><br/>
 * 
 * T. Uno, M. Kiyomi, and H. Arimura. Lcm ver. 2:
  * Efficient mining algorithms for
 * frequent/closed/maximal itemsets. In FIMI, 2004
 * 
 * This implementation of LCM was made by Alan Souza and was
 * modified by Philippe Fournier-Viger to add optimizations. <br/>
 * 
 * The implementation is similar to LCM version 2 with some differences.
 * For example, transaction merging is not performed yet and
 * items in transactions are not sorted in descending order of frequency.
 *
 * @author Alan Souza <apsouza@inf.ufrgs.br>
 */
public class AlgoLCMFreq {

    private Itemsets frequentItemsets;
    
	// object to write the output file
	BufferedWriter writer = null;
	
	// the number of frequent itemsets found (for
	// statistics)
	private int frequentCount; 

	// the start time and end time of the last algorithm execution
	long startTimestamp;
	long endTimestamp;
	int minsupRelative;
	
	/** buffer for storing the current itemset that is mined when performing mining
	* the idea is to always reuse the same buffer to reduce memory usage. **/
	final int BUFFERS_SIZE = 1000;
	private int[] itemsetBuffer = null;
	
	/** This buffer is used to store an itemset that will be written to file
	* so that the algorithm can sort the itemset before it is output to file
	* (when the user choose to output result to file). */
	private int[] itemsetOutputBuffer = null;
	
	/** This buffer is used to store the locally frequent items in a projected database*/
//	private int[] locallyFrequentItemsBuffer = null;
	
	/** Buckets for occurence delivery 
	* Recall that each bucket correspond to an item
    * and contains the transactions where the items appears. */
	private List<Transaction>[] buckets;
	
	
//	/** a class to convert old item name to new item names according to
//	 * the total oder for optimization **/
//	ItemNameConverter nameConverter;
	
	/** Special parameter to set the maximum size of itemsets to be discovered */
	int maxItemsetSize = Integer.MAX_VALUE;

    public AlgoLCMFreq() {
         
    }

    /**
     * Run the algorithm
     * @param minimumSupport  the minimum support threshold as percentage value between 0 and 1
     * @param dataset  the dataset
     * @param outputPath  the output file path to save the result or null if to be kept in memory
     * @throws IOException if exception while reading/writing to file
     */
    public Itemsets runAlgorithm(double minimumSupport, Dataset dataset, String outputPath) throws IOException {
		// record the start time
		startTimestamp = System.currentTimeMillis();

		// if the user choose to save to file
		// create object for writing the output file
		if(outputPath != null) {
			writer = new BufferedWriter(new FileWriter(outputPath));
			itemsetOutputBuffer = new int[BUFFERS_SIZE];
		}else {
			// if the user choose to save to memory
			writer = null;
	        this.frequentItemsets = new Itemsets("Itemsets");
		}
		
		// initialize the buffer for storing the current itemset
		itemsetBuffer = new int[BUFFERS_SIZE];
		
		// reset the number of itemset found
		frequentCount = 0;
		
		// reset the memory usage checking utility
		MemoryLogger.getInstance().reset();
		
		// convert from an absolute minsup to a relative minsup by multiplying
		// by the database size
		this.minsupRelative = (int) Math.ceil(minimumSupport * dataset.getTransactions().size());
    	
		if(maxItemsetSize >=1){
	        // Create the initial occurrence array for the dataset
	        performFirstOccurenceDelivery(dataset);
	        
	        //======
	        // Remove infrequent items from transactions by using support calculated using
	        // the buckets. Recall that each bucket correspond to an item
	        // and contains the transactions where the items appears.
	    	for(Transaction transaction : dataset.getTransactions()) {
	    		transaction.removeInfrequentItems(buckets, minsupRelative);
	    	}
	    	
	    	//======
	    	// Create the array of all frequent items.
	    	List<Integer> allItems = new ArrayList<Integer>();
	    	for(Integer item : dataset.getUniqueItems()) {
	    		if(buckets[item].size() >= minsupRelative && maxItemsetSize >=1) {
	    			allItems.add(item);
	    		}
	    	}
	    	// Sort all items
	    	Collections.sort(allItems);
	    	
			// We rename the items according to the lexicographical order.
			// This is an optimization that will allow us very fast comparison
			// of items according to the total order.
//			nameConverter = new ItemNameConverter(allItems.size());
//			// for each item
//			for(int i = 0; i < allItems.size(); i++) {
//				int item = allItems.get(i);
//				int newName = nameConverter.assignNewName(item);
//				// we rename the item with a new name
//				allItems.set(i, newName);
//			}
	    	
	    	//======
	        // Call the recursive method witht the empty set as prefix.
	        // Since it is the empty set, we will have all transactions and no frequency count
	        backtrackingLCMFreq(itemsetBuffer, 0, dataset.getTransactions(), allItems);
		}

		// record the end time
		endTimestamp = System.currentTimeMillis();
		//close the output file
		if(writer != null) {
			writer.close();
		}
		
		MemoryLogger.getInstance().checkMemory();
        
        return frequentItemsets;
    }

    
    /**
     * Recursive method to find all frequent itemsets
     * @param p  a prefix itemset P
     * @param pLength the prefix length
     * @param transactionsOfP the transations containing P
	 * @param frequentItems the list of frequent items in the p-projected database
     * @param tailPosInP the tail item position in itemset P
     * @throws IOException if error writing to output file
     */
    private void backtrackingLCMFreq(int[] p, int pLength, List<Transaction> transactionsOfP,
    		List<Integer> frequentItems) throws IOException {
    	
        // ========  for each frequent item  e  =============
		for (int j = 0; j < frequentItems.size(); j++) {
			Integer e = frequentItems.get(j);

			// Calculate transactions containing P U e 
			// At the same time truncate the transactions to keep what appears after "e"
			List<Transaction> transactionsPe = intersectTransactions(transactionsOfP, e); //ok

			//  ======= Create a closed itemset using PU{e...}  =====
			// First append item "e" to p
			p[pLength] = e;

	        // ===== save the frequent itemset
	    	int supportPe = transactionsPe.size();
			output(p, pLength+1, supportPe);
			
			// === recursive call
	    	if(pLength+2 <= maxItemsetSize){
	
				//==== perform database reduction ====
				anyTimeDatabaseReductionFreq(transactionsPe, j, frequentItems, p, pLength, e); 
				
		    	// ================ Find frequent items in transactions containing P ============
		        // Get all frequent items e such that e > tailOfP  
		    	// (i.e. "e" appears after the position of the tail item in the list of all items)
				List<Integer> newFrequentItems = new ArrayList<Integer>();
		    	for (int k = j+1; k < frequentItems.size(); k++) {
		        	Integer itemK =  frequentItems.get(k);
		        	int supportK = buckets[itemK].size();
		            if(supportK >= minsupRelative) {
		            	newFrequentItems.add(itemK);
		            }
		        }
				
				backtrackingLCMFreq(p, pLength+1, transactionsPe, newFrequentItems);
	    	}
		}

		MemoryLogger.getInstance().checkMemory();
    }
    

    /**
	 * Perform the initial occurence delivery with the original dataset
	 * containing all items
	 * @param dataset
	 */
	public void performFirstOccurenceDelivery(Dataset dataset) {

		buckets = new List[dataset.getMaxItem() + 1]; 

		for (Integer item : dataset.uniqueItems) {
			buckets[item] = new ArrayList<Transaction>();
		}

		for (Transaction transaction : dataset.getTransactions()) {
			for (Integer item : transaction.getItems()) {
				// for each item get its bucket and add the current transaction
				buckets[item].add(transaction);
			}

		}
	}

    /**
     * Perform the anytime database reduction for an itemset P U {e}
     * @param transactions the transactions
     * @param j the position of j in the list of frequent items
     * @param frequentItems 
     * @param itemset 
     * @param e 
     */
    private void anyTimeDatabaseReductionFreq(List<Transaction> transactionsPe, int j, List<Integer> frequentItems, int[] itemset, int itemsetLength, Integer e) {

		// We just reset the buckets for item  > e
		// instead of all buckets
		for (int i = j+1; i < frequentItems.size(); i++) {
			Integer item = frequentItems.get(i);
			buckets[item] = new ArrayList<Transaction>();
		}
		
       // for each transaction
       for(Transaction transaction : transactionsPe) {
    	   // we consider each item I  of the transaction such that  itemI > e 
    	   for(int i = transaction.getItems().length-1; i >transaction.offset; i--) {
    		   Integer item = transaction.getItems()[i];
    		   if(frequentItems.contains(item)) {
	    		   // we add the transaction to the bucket of the itemI
	    		   buckets[item].add(transaction);
    		   }
        	}
        }
    }
    
    /**
     * Check if an item appears in this itemset
     * @param item  the item
     * @return true if it appears. Otherwise, false.
     */
	public boolean containsByBinarySearch(List<Integer> items, Integer item, int searchAfterPosition) {
		if(items.size() == 0 || item > items.get(items.size() -1)) {
			return false;
		}
		int low = searchAfterPosition +1;
		int high = items.size() - 1;

		while (high >= low) {
			int middle = ( low + high ) >>> 1; // divide by 2
			if (items.get(middle).equals(item)) {
				return true;
			}
			if (items.get(middle) < item) {
				low = middle + 1;
			}
			if (items.get(middle) > item) {
				high = middle - 1;
			}
		}
		return false;
	}
	
	public boolean containsByBinarySearch(List<Integer> items, Integer item) {
		if(items.size() == 0 || item > items.get(items.size() -1)) {
			return false;
		}
		int low = 0;
		int high = items.size() - 1;

		while (high >= low) {
			int middle = ( low + high ) >>> 1; // divide by 2
			if (items.get(middle).equals(item)) {
				return true;
			}
			if (items.get(middle) < item) {
				low = middle + 1;
			}
			if (items.get(middle) > item) {
				high = middle - 1;
			}
		}
		return false;
	}
	
	public boolean containsByBinarySearch(int[] items, int item, int start, int end) {

		int low = start;
		int high = end;

		while (high >= low) {
			int middle = ( low + high ) >>> 1; // divide by 2
			if (items[middle] ==item ) {
				return true;
			}
			if (items[middle] < item) {
				low = middle + 1;
			}
			if (items[middle] > item) {
				high = middle - 1;
			}
		}
		return false;
	}
	
    /**
     * Calculate the transactions of the union of an itemset "P" with an item "e".
     * @param transactionsOfP the transactions containing P
     * @param e  the item "e"
     * @return the transactions containing P U "e"
     */
    public List<Transaction> intersectTransactions(List<Transaction> transactionsOfP, Integer e) {
        List<Transaction> transactionsPe = new ArrayList<Transaction>();

        // transactions of P U e
        for(Transaction transaction : transactionsOfP) {
        	// we remember the position where e appears.
        	// we will call this position an "offset"
        	int posE = transaction.containsByBinarySearch(e);
            if (posE != -1) { // T(P U e)
                transactionsPe.add(new Transaction(transaction, posE));
            }
        }
        return transactionsPe;
    }

	/**
	 * Write a frequent itemset that is found to the output file or
	 * keep into memory if the user prefer that the result be saved into memory.
	 */
	private void output(int [] itemset, int itemsetLength, int support) throws IOException {
		
		// increase the number of itemsets found for statistics purpose
		frequentCount++;
		
		// if the result should be saved to a file
		if(writer != null){
			// copy the itemset in the output buffer and sort items
			System.arraycopy(itemset, 0, itemsetOutputBuffer, 0, itemsetLength);
			Arrays.sort(itemsetOutputBuffer, 0, itemsetLength);
			
			// Create a string buffer
			StringBuilder buffer = new StringBuilder();
			// write the items of the itemset
			for(int i=0; i< itemsetLength; i++){
				buffer.append(itemsetOutputBuffer[i]);
				if(i != itemsetLength-1){
					buffer.append(' ');
				}
			}
			// Then, write the support
			buffer.append(" #SUP: ");
			buffer.append(support);
			// write to file and create a new line
			writer.write(buffer.toString());
			writer.newLine();
		}// otherwise the result is kept into memory
		else{
			// create an object Itemset and add it to the set of patterns 
			// found.
			int[] itemsetArray = new int[itemsetLength];
			System.arraycopy(itemset, 0, itemsetArray, 0, itemsetLength);
			
			// sort the itemset so that it is sorted according to lexical ordering before we show it to the user
			Arrays.sort(itemsetArray);
			
			Itemset itemsetObj = new Itemset(itemsetArray);
			itemsetObj.setAbsoluteSupport(support);
			frequentItemsets.addItemset(itemsetObj, itemsetLength);
		}
	}

    /**
     * Print statistics about the latest execution of the algorithm.
     */
	public void printStats() {
		System.out.println("========== LCMFreq v0.96r18 - STATS ============");
		System.out.println(" Freq. itemsets count: " + frequentCount);
		System.out.println(" Total time ~: " + (endTimestamp - startTimestamp)
				+ " ms");
		System.out.println(" Max memory:" + MemoryLogger.getInstance().getMaxMemory());
		System.out.println("=====================================");
	}

	/** 
	 * Set the maximum pattern length
	 * @param length the maximum length
	 */
	public void setMaximumPatternLength(int length) {
		this.maxItemsetSize = length;
	}

}
