 package ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth;
 
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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemset;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;
import ca.pfv.spmf.tools.MemoryLogger;

/** 
 * This is an implementation of the FPMax algorithm (Grahne et al., 2004).
 * FPGrowth is described here:
 * <br/><br/>
 * 
 * High Performance Mining of Maximal Frequent Itemsets (with J. Zhu).
 *  6th International Workshop on High Performance Data Mining (HPDM '03)  * <br/><br/>
 * 
 * This is an optimized version that saves the result to a file
 * or keep it into memory if no output path is provided
 * by the user to the runAlgorithm method().
 *
 * @see FPTree
 * @see Itemset
 * @see Itemsets
 * @author Philippe Fournier-Viger, 2015
 */
public class AlgoFPMax {

	// for statistics
	private long startTimestamp; // start time of the latest execution
	private long endTime; // end time of the latest execution
	private int transactionCount = 0; // transaction count in the database
	private int itemsetCount; // number of freq. itemsets found
	
	// parameter
	public int minSupportRelative;// the relative minimum support
	
	BufferedWriter writer = null; // object to write the output file
	
	// The  patterns that are found 
	// (if the user want to keep them into memory)
	protected Itemsets patterns = null;
		
	// This variable is used to determine the size of buffers to store itemsets.
	// A value of 2000 should be enough 
	final int BUFFERS_SIZE = 2000;
	
	// buffer for storing the current itemset that is mined when performing mining
	// the idea is to always reuse the same buffer to reduce memory usage.
	private int[] itemsetBuffer = null;
	
	// This is the MFI tree for storing maximal itemsets
	public MFITree mfiTree = null;
	
	// Map to store the support of single items in the original databse
	private Map<Integer, Integer> originalMapSupport = null;
	
	// If set to true, the algorithm will show  information for debugging in the console
	private final boolean DEBUG = false;
	
	// Comparator to compare the items based on the order of decreasing support in the original DB.
	Comparator<Integer> comparatorOriginalOrder = new Comparator<Integer>(){
		public int compare(Integer item1, Integer item2){
			// compare the frequency
			int compare = originalMapSupport.get(item2) - originalMapSupport.get(item1);
			// if the same frequency, we check the lexical ordering!
			if(compare == 0){ 
				compare = (item1 - item2);
				return compare;
			}
			return compare;
		}
	};

	/**
	 * Constructor
	 */
	public AlgoFPMax() {
		
	}

	/**
	 * Method to run the FPGRowth algorithm.
	 * @param input the path to an input file containing a transaction database.
	 * @param output the output file path for saving the result (if null, the result 
	 *        will be returned by the method instead of being saved).
	 * @param minsupp the minimum support threshold.
	 * @return the result if no output file path is provided.
	 * @throws IOException exception if error reading or writing files
	 */
	public Itemsets runAlgorithm(String input, String output, double minsupp) throws FileNotFoundException, IOException {
		// record start time
		startTimestamp = System.currentTimeMillis();
		// number of itemsets found
		itemsetCount = 0;
		
		//initialize tool to record memory usage
		MemoryLogger.getInstance().reset();
		MemoryLogger.getInstance().checkMemory();
		
		// if the user want to keep the result into memory
		if(output == null){
			writer = null;
			patterns =  new Itemsets("FREQUENT ITEMSETS");
	    }else{ // if the user want to save the result to a file
			patterns = null;
			writer = new BufferedWriter(new FileWriter(output)); 
		}
		
		// (1) PREPROCESSING: Initial database scan to determine the frequency of each item
		// The frequency is stored in a map:
		//    key: item   value: support
		originalMapSupport = scanDatabaseToDetermineFrequencyOfSingleItems(input); 

		// convert the minimum support as percentage to a
		// relative minimum support
		this.minSupportRelative = (int) Math.ceil(minsupp * transactionCount);
		

		// Create the MFI Tree
		mfiTree = new MFITree();	
		
		// (2) Scan the database again to build the initial FP-Tree
		// Before inserting a transaction in the FPTree, we sort the items
		// by descending order of support.  We ignore items that
		// do not have the minimum support.
		FPTree tree = new FPTree();
		
		// read the file
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		// for each line (transaction) until the end of the file
		while( ((line = reader.readLine())!= null)){ 
			// if the line is  a comment, is  empty or is a
			// kind of metadata
			if (line.isEmpty() == true ||	line.charAt(0) == '#' || line.charAt(0) == '%'
				|| line.charAt(0) == '@') {
				continue;
			}
			
			String[] lineSplited = line.split(" ");
			List<Integer> transaction = new ArrayList<Integer>();
			
			// for each item in the transaction
			for(String itemString : lineSplited){  
				Integer item = Integer.parseInt(itemString);
				// only add items that have the minimum support
				if(originalMapSupport.get(item) >= minSupportRelative){
					transaction.add(item);	
				}
			}
			// sort item in the transaction by descending order of support
			Collections.sort(transaction, comparatorOriginalOrder);
			// add the sorted transaction to the fptree.
			tree.addTransaction(transaction);
		}
		// close the input file
		reader.close();
		
		// We create the header table for the tree using the calculated support of single items
		tree.createHeaderList(originalMapSupport);
		

//		System.out.println(tree);
		
		// (5) We start to mine the FP-Tree by calling the recursive method.
		// Initially, the prefix alpha is empty.
		// if at least an item is frequent
		if(tree.headerList.size() > 0) {
			// initialize the buffer for storing the current itemset
			itemsetBuffer = new int[BUFFERS_SIZE];
			// Next we will recursively generate frequent itemsets using the fp-tree
			fpMax(tree, itemsetBuffer, 0, transactionCount, originalMapSupport);
		}
		
		// close the output file if the result was saved to a file
		if(writer != null){
			writer.close();
		}
		// record the execution end time
		endTime= System.currentTimeMillis();
		
		// check the memory usage
		MemoryLogger.getInstance().checkMemory();
		
		// return the result (if saved to memory)
		return patterns;
	}

	
	/**
	 * Mine an FP-Tree having more than one path.
	 * @param tree  the FP-tree
	 * @param prefix  the current prefix, named "alpha"
	 * @param mapSupport the frequency of items in the FP-Tree
	 * @throws IOException  exception if error writing the output file
	 */
	private void fpMax(FPTree tree, int [] prefix, int prefixLength, int prefixSupport, Map<Integer, Integer> mapSupport) throws IOException {
//		======= DEBUG ========
		if(DEBUG) {
			System.out.print("###### Prefix: ");
			for(int k=0; k< prefixLength; k++) {
				System.out.print(prefix[k] + "  ");
			}
			System.out.println("\n");
			System.out.println(tree);
		}	//========== END DEBUG =======
		
		// We first check if the FPtree contains a single path
		// We will check if the FPtree contains a single path
		boolean singlePath = true;
		// We will use a variable to keep the support of the single path if there is one
		int singlePathSupport = 0;
		// This variable is used to count the number of items in the single path  (if there
		// is one + the prefix length
		int position = prefixLength;
		if(tree.root.childs.size() > 1) {
			// if the root has more than one child, than it is not a single path
			singlePath = false;
		}else {
			// if the root has exactly one child, we need to recursively check childs
			// of the child to see if they also have one child
			FPNode currentNode = tree.root.childs.get(0);
			while(true){
				// if the current child has more than one child, it is not a single path!
				if(currentNode.childs.size() > 1) {
					singlePath = false;
					break;
				}
				// otherwise, we copy the current item in the buffer and move to the child
				itemsetBuffer[position] = currentNode.itemID;
				singlePathSupport = currentNode.counter;
				position++;
				// if this node has no child, that means that this is the end of this path
				// and it is a single path, so we break
				if(currentNode.childs.size() == 0) {
					break;
				}
				currentNode = currentNode.childs.get(0);
			}
		}
		
		// Case 1: the FPtree contains a single path
		if(singlePath && singlePathSupport >= minSupportRelative){	
			// We save the path, because it is a maximal itemset
			saveItemset(itemsetBuffer, position, singlePathSupport);
		}else {
			// Case 2: There are multiple paths.
			
			// For each frequent item in the header table list of the tree in reverse order. (in decreasing order of support...)
			for(int i = tree.headerList.size()-1; i>=0; i--){
				// get the item
				Integer item = tree.headerList.get(i);
				
				// get the item support
				int support = mapSupport.get(item);

				// Create Beta by concatening item to the current prefix  alpha
				prefix[prefixLength] = item;
				
				// calculate the support of the new prefix beta
				int betaSupport = (prefixSupport < support) ? prefixSupport: support;

				
				// === (A) Construct beta's conditional pattern base ===
				// It is a subdatabase which consists of the set of prefix paths
				// in the FP-tree co-occuring with the prefix pattern.
				List<List<FPNode>> prefixPaths = new ArrayList<List<FPNode>>();
				FPNode path = tree.mapItemNodes.get(item);
				
				// Map to count the support of items in the conditional prefix tree
				// Key: item   Value: support
				Map<Integer, Integer> mapSupportBeta = new HashMap<Integer, Integer>();
				
				while(path != null){
					// if the path is not just the root node
					if(path.parent.itemID != -1){
						// create the prefixpath
						List<FPNode> prefixPath = new ArrayList<FPNode>();
						// add this node.
						prefixPath.add(path);   // NOTE: we add it just to keep its support,
						// actually it should not be part of the prefixPath
						
						// ####
						int pathCount = path.counter;
						
						//Recursively add all the parents of this node.
						FPNode parent = path.parent;
						while(parent.itemID != -1){
							prefixPath.add(parent);
							
							// FOR EACH PATTERN WE ALSO UPDATE THE ITEM SUPPORT AT THE SAME TIME
							// if the first time we see that node id
							if(mapSupportBeta.get(parent.itemID) == null){
								// just add the path count
								mapSupportBeta.put(parent.itemID, pathCount);
							}else{
								// otherwise, make the sum with the value already stored
								mapSupportBeta.put(parent.itemID, mapSupportBeta.get(parent.itemID) + pathCount);
							}
							parent = parent.parent;
						}
						// add the path to the list of prefixpaths
						prefixPaths.add(prefixPath);
					}
					// We will look for the next prefixpath
					path = path.nodeLink;
				}

				
				// ===== FPMAX ======
				// concatenate Beta with all the frequent itemsets in the pattern base
				// to get head U P
				List<Integer> headWithP = new ArrayList<Integer>(mapSupportBeta.size() + prefixLength+1);
				// concatenate the prefix
				for(int z=0; z < prefixLength+1; z++) {
					headWithP.add(prefix[z]);
				}
				// concatenate the other FREQUENT items in the pattern base
				// for each item
				for(Entry<Integer,Integer> entry: mapSupportBeta.entrySet()) {
					// if the item is frequent
					if(entry.getValue() >= minSupportRelative) {
						headWithP.add(entry.getKey());
					}
				}
				
				// Sort Head U P according to the original header list total order on items
				// sort item in the transaction by descending order of support
				Collections.sort(headWithP, comparatorOriginalOrder);

				//======= DEBUG ========
				if(DEBUG) {
					System.out.println(" CHECK2 : " + headWithP);
				}
				//========== END DEBUG =======
				
				// CHECK IF HEAD U P IS A SUBSET OF A MFI ACCORDING TO THE MFI-TREE
				if(mfiTree.passSubsetChecking(headWithP)) {
					
					if(DEBUG) {
						System.out.println("    passed!");
					}
					// (B) Construct beta's conditional FP-Tree using its prefix path
					// Create the tree.
					FPTree treeBeta = new FPTree();
					// Add each prefixpath in the FP-tree.
					for(List<FPNode> prefixPath : prefixPaths){
						treeBeta.addPrefixPath(prefixPath, mapSupportBeta, minSupportRelative); 
					}  
					// Mine recursively the Beta tree if the root has child(s)
					if(treeBeta.root.childs.size() > 0){

						// Create the header list.
						treeBeta.createHeaderList(originalMapSupport); 
						
						// recursive call
						fpMax(treeBeta, prefix, prefixLength+1, betaSupport, mapSupportBeta);
					}
					
					// ======= After that, we still need to check if beta is a maximal itemset ====
					List<Integer> temp = new ArrayList<Integer>(mapSupportBeta.size() + prefixLength+1);
					for(int z=0; z < prefixLength+1; z++) {
						temp.add(prefix[z]);
					}
					Collections.sort(temp, comparatorOriginalOrder);
					// if beta pass the test, we save it
					if(mfiTree.passSubsetChecking(temp)) {
						saveItemset(prefix, prefixLength+1, betaSupport);
					}
					//===========================================================
				}
				else if (DEBUG){
					System.out.println("     failed!");
				}
			}
		}
	}

	/**
	 * Write a frequent itemset that is found to the output file or
	 * keep into memory if the user prefer that the result be saved into memory.
	 */
	private void saveItemset(int [] itemset, int itemsetLength, int support) throws IOException {
		
		// copy the itemset in the output buffer and sort items according to the
		// order of decreasing support in the original database
		int[] itemsetCopy = new int[itemsetLength];
		System.arraycopy(itemset, 0, itemsetCopy, 0, itemsetLength);
		sortOriginalOrder(itemsetCopy, itemsetLength);
		
		if(DEBUG) {
	//		//======= DEBUG ========
			System.out.print(" ##### SAVING : ");
			for(int i=0; i< itemsetLength; i++) {
				System.out.print(itemsetCopy[i] + "  ");
			}
			System.out.println("\n");
	//		//========== END DEBUG =======
		}
		
		// add the itemset to the MFI-TREE
		mfiTree.addMFI(itemsetCopy, itemsetCopy.length, support);
		
		// increase the number of itemsets found for statistics purpose
		itemsetCount++;
		
		// if the result should be saved to a file
		if(writer != null){
			
			// Create a string buffer
			StringBuilder buffer = new StringBuilder();
			// write the items of the itemset
			for(int i=0; i< itemsetLength; i++){
				buffer.append(itemsetCopy[i]);
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
			
			// sort the itemset so that it is sorted according to lexical ordering before we show it to the user
			Arrays.sort(itemsetCopy);
			
			Itemset itemsetObj = new Itemset(itemsetCopy);
			itemsetObj.setAbsoluteSupport(support);
			patterns.addItemset(itemsetObj, itemsetLength);
		}
	}
	
	/**
	 * Sort an array of items according to the total order of support
	 * This has an average performance of O(n^2)
	 * @param a array of integers
	 */
	public void sortOriginalOrder(int [] a, int length){
		// Perform a bubble sort
		for(int i=0; i < length; i++){
			for(int j= length -1; j>= i+1; j--){
				boolean test = comparatorOriginalOrder.compare(a[j], a[j-1]) < 0;
				if(test){
					int temp = a[j];
					a[j] = a[j-1];
					a[j-1] = temp;
				}
			}
		}

	}


	/**
	 * This method scans the input database to calculate the support of single items
	 * @param input the path of the input file
	 * @throws IOException  exception if error while writing the file
	 * @return a map for storing the support of each item (key: item, value: support)
	 */
	private  Map<Integer, Integer> scanDatabaseToDetermineFrequencyOfSingleItems(String input)
			throws FileNotFoundException, IOException {
		// a map for storing the support of each item (key: item, value: support)
		 Map<Integer, Integer> mapSupport = new HashMap<Integer, Integer>();
		//Create object for reading the input file
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		// for each line (transaction) until the end of file
		while( ((line = reader.readLine())!= null)){ 
			// if the line is  a comment, is  empty or is a
			// kind of metadata
			if (line.isEmpty() == true ||  line.charAt(0) == '#' || line.charAt(0) == '%' 	|| line.charAt(0) == '@') {
				continue;
			}
			
			// split the line into items
			String[] lineSplited = line.split(" ");
			// for each item
			for(String itemString : lineSplited){  
				// increase the support count of the item
				Integer item = Integer.parseInt(itemString);
				// increase the support count of the item
				Integer count = mapSupport.get(item);
				if(count == null){
					mapSupport.put(item, 1);
				}else{
					mapSupport.put(item, ++count);
				}
			}
			// increase the transaction count
			transactionCount++;
		}
		// close the input file
		reader.close();
		
		return mapSupport;
	}


	/**
	 * Print statistics about the algorithm execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  FP-Max v0.96r14  - STATS =============");
		long temps = endTime - startTimestamp;
		System.out.println(" Transactions count from database : " + transactionCount);
		System.out.print(" Max memory usage: " + MemoryLogger.getInstance().getMaxMemory() + " mb \n");
		System.out.println(" Maximal frequent itemset count : " + itemsetCount); 
		System.out.println(" Total time ~ " + temps + " ms");
		System.out.println("===================================================");
	}

	/**
	 * Get the number of transactions in the last transaction database read.
	 * @return the number of transactions.
	 */
	public int getDatabaseSize() {
		return transactionCount;
	}
}
