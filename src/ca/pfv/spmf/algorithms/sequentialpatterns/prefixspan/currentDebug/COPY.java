package ca.pfv.spmf.algorithms.sequentialpatterns.prefixspan.currentDebug;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ca.pfv.spmf.patterns.itemset_list_integers_without_support.Itemset;
import ca.pfv.spmf.tools.MemoryLogger;


/*** 
 * This is my 2016 implementation of the BIDE+ algorithm. I have re-implemented this algorithm in SPMF 0.99
 * with better design decisions and optimizations than in the previous implementation offered in SPMF, which
 * had been implemented a few years ago. This new implementation can be up to 10 times faster than the previous 
 * implementation. 
 * 
 * If you want to understand the BIDE+ algorithm, I highly recommend to first read the paper very carefully as it
 * is a quite complex algorithm, and it would thus be quite difficult to understand the code without reading the paper
 * to first understand the key ideas of BIDE+.
 * 
 * The BIDE+ algorithm was proposed by Pei et al. 2001.
 * 
 * NOTE: This implementation saves the pattern  to a file as soon 
 * as they are found or can keep the pattern into memory, depending
 * on what the user choose.
 *
 * Copyright (c) 2016 Philippe Fournier-Viger
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 *
 * SPMF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SPMF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SPMF.  If not, see <http://www.gnu.org/licenses/>.
 */

public class COPY{
		
	/** for statistics **/
	long startTime;
	long endTime;
	
	/** the number of pattern found */
	public int patternCount;
	
	/** absolute minimum support */
	private int minsuppAbsolute;

	/** writer to write output file */
	BufferedWriter writer = null;
	
	/** The sequential patterns that are found  (if the user want to keep them into memory) */
	private SequentialPatterns patterns = null;
	
	/** maximum pattern length in terms of item count */
	private int maximumPatternLength = Integer.MAX_VALUE;
	
	/** if true, sequence identifiers of each pattern will be shown*/
	boolean showSequenceIdentifiers = false;
	
	/** buffer for storing the current pattern that is mined when performing mining
	* the idea is to always reuse the same buffer to reduce memory usage. **/
	final int BUFFERS_SIZE = 2000;
	private int[] patternBuffer = new int[BUFFERS_SIZE];
	
	/** original sequence count **/
	int sequenceCount = 0;
	
	/** the sequence database **/
	SequenceDatabase sequenceDatabase;
	
	/** boolean indicating whether this database contains itemsets with multiple items or not */
	boolean containsItemsetsWithMultipleItems = false;
	
	/**
	 * A set that is used to remember which items we have already seen in a sequence. It is
	 * a variable reused by various methods, to save memory 
	 */
	Set<Integer> alreadySeen = new HashSet<Integer>();
	/**
	 * A set that is used to remember which items we have already seen in a sequence. It is
	 * a variable reused by various methods, to save memory 
	 */
	Set<Integer> alreadySeenPostfix = new HashSet<Integer>();
	/**
	 * A set that is used to remember which items we have already seen in a sequence. It is
	 * a variable reused by various methods, to save memory 
	 */
	Set<Integer> alreadySeenSuffix = new HashSet<Integer>();
	
	/**
	 * A variable reused by various method for counting the support of items that can extend a sequential pattern
	 * by s-extension in a projected database
	 */
	Map<Integer,Integer> mapItemSupport = new HashMap<Integer,Integer>();
	/**
	 * A variable reused by various method for counting the support of items that can extend a sequential pattern
	 * by i-extension (as part of a postfix) in a projected database
	 */
	Map<Integer,Integer> mapsItemSupportPostfix = new HashMap<Integer,Integer>();
	/**
	 * A variable reused by various method for counting the support of items that can extend a sequential pattern
	 * by i-extension (as part of a suffix) in a projected database
	 */
	Map<Integer,Integer> mapsItemSupportSuffix = new HashMap<Integer,Integer>();
	
	/**
	 * Default constructor
	 */
	public COPY(){
	}
	
	/**
	 * Run the algorithm
	 * @param inputFile : a sequence database
	 * @param minsupRelative  :  the minimum support as a percentage (e.g. 50%) as a value in [0,1]
	 * @param outputFilePath : the path of the output file to save the result
	 *                         or null if you want the result to be saved into memory
	 * @return return the result, if saved into memory, otherwise null
	 * @throws IOException  exception if error while writing the file
	 */
	public SequentialPatterns runAlgorithm(String inputFile, double minsupRelative, String outputFilePath) throws IOException {
		// record start time
		startTime = System.currentTimeMillis();
		
		// Load the sequence database
		sequenceDatabase = new SequenceDatabase(); 
		sequenceDatabase.loadFile(inputFile);
		sequenceCount = sequenceDatabase.size();
		
		// convert to a absolute minimum support
		this.minsuppAbsolute = (int) Math.ceil(minsupRelative * sequenceCount);
		if(this.minsuppAbsolute == 0){ // protection
			this.minsuppAbsolute = 1;
		}
		
		// run the algorithm
		bide(sequenceDatabase, outputFilePath);
		
		// release memory used by the sequence database
		sequenceDatabase = null;
		
		// record end time
		endTime = System.currentTimeMillis();
		// close the output file if the result was saved to a file
		if(writer != null){
			writer.close();
		}
		return patterns;
	}
	
	/**
	 * Run the algorithm
	 * @param inputFile : the file path to the input file
	 * @param minsup  :  the minimum support as an integer
	 * @param outputFilePath : the path of the output file to save the result
	 *                         or null if you want the result to be saved into memory
	 * @return return the result, if saved into memory, otherwise null if the result is saved to an output file
	 * @throws IOException  exception if error while writing the file
	 */
	public SequentialPatterns runAlgorithm(String inputFile, String outputFilePath, int minsup) throws IOException {
		// initialize variables for statistics
		patternCount =0;
		MemoryLogger.getInstance().reset();
		
		// save the minsup chosen  by the user
		this.minsuppAbsolute = minsup;
		
		// save the start time
		startTime = System.currentTimeMillis();
		
		// Load the sequence database
		sequenceDatabase = new SequenceDatabase(); 
		sequenceDatabase.loadFile(inputFile);
//		sequenceDatabase.print();
		
		// run the algorithm
		bide(sequenceDatabase, outputFilePath);
		
		// release memory
		sequenceDatabase = null;
		
		// save the end time
		endTime = System.currentTimeMillis();
		
		// close the output file if the result was saved to a file
		if(writer != null){
			writer.close();
		}
		return patterns;
	}
	
	/**
	 * This is the main method for the BIDE+ algorithm that is called
	 * to start the algorithm.
	 * @param sequenceDatabase a sequence database
	 * @param outputFilePath  an output file path if the result should be saved to a file
	 *                        or null if the result should be saved to memory.
	 * @throws IOException exception if an error while writing the output file
	 */
	private void bide(SequenceDatabase sequenceDatabase, String outputFilePath) throws IOException{
		// if the user want to keep the result into memory
		if(outputFilePath == null){
			writer = null;
			patterns = new SequentialPatterns("FREQUENT SEQUENTIAL PATTERNS");
		}else{ // if the user want to save the result to a file
			patterns = null;
			writer = new BufferedWriter(new FileWriter(outputFilePath)); 
		}
		// Remember the size of the sequence database
		sequenceCount = sequenceDatabase.size();
//		System.out.println(sequenceDatabase.toString());
		
		//============== CALCULATE FREQUENCY OF EACH SINGLE ITEMS BY SCANNING THE DATABASE =============
		// We have to scan the database to find all frequent sequential patterns containing 1 item.
		// We note the sequences where each item appears.
		Map<Integer, List<Integer>> mapSequenceID = findSequencesContainingItems();

		//====== Remove infrequent items from the database, and then explore each projected database ================
		// This step is performed differently depending on whether the database contains multiple items
		// per itemset, or not. This allows to do additional optimizations.
		// CASE 1: the database has multiple items per itemset
		if(containsItemsetsWithMultipleItems){
			bideWithMultipleItems(mapSequenceID);	
		}else{
			// CASE 2: the database does not have multiple items per itemset.
			bideWithSingleItems(mapSequenceID);	
		}
	}
	
	/**
	 * Remove infrequent items from the sequence database, then save each pattern of length 1 having a frequency no less than 
	 * minsup, then recursively explore larger patterns.
	 *  Important: this method is optimized for the case where all sequences in the database contains a single item per itemset.
	 * @param mapSequenceID the set of items (Integer) with the corresponding IDs of sequence where they appears (List<Integer>)
	 * @throws IOException if error writing to file
	 */
	private void bideWithSingleItems(Map<Integer, List<Integer>> mapSequenceID) throws IOException {
		//=============== REMOVE INFREQUENT ITEMS ========================
		// We scan the database to remove infrequent items  and resize sequences after removal.
		// For each sequence in the current database
		for(int i=0; i < sequenceDatabase.size(); i++){
			// Get the sequence
			int[] sequence  = sequenceDatabase.getSequences().get(i);
			
			// We will copy the frequent items one by one but not those items that are infrequent
			// The following variable will be used to remember the position where to copy (we start at 0).
			int currentPosition = 0;
			
			// for each token in this sequence (item, separator between itemsets (-1) or end of sequence (-2)
			for(int j = 0; j < sequence.length; j++){
				int token = sequence[j];
				
				// if it is an item
				if(token > 0){
					// check if it is frequent
					boolean isFrequent = mapSequenceID.get(token).size() >= minsuppAbsolute;
					
					// if the item is frequent
					if(isFrequent){
						// copy the item to the current position
						sequence[currentPosition] = token;
						// increment the current position
						currentPosition++;
					}	
				}else if(token == -2){
					// If it is the end of the sequence,
					// If the sequence is not empty after having removed the infrequent items
					if(currentPosition >0){
						// copy the item to the end of sequence -2 at the current position
						sequence[currentPosition] = -2;
						
						// now replace the previous sequence in the database with the new sequence where items have been removed
						int[] newSequence = new int[currentPosition+1];
						System.arraycopy(sequence, 0, newSequence, 0, currentPosition+1);
						sequenceDatabase.getSequences().set(i, newSequence);
						// continue to next sequence
						continue; 
					}else{
						// if the sequence is  empty, delete this sequence by replacing it with null
						sequenceDatabase.getSequences().set(i, null);
					}
				}
			}
		}
		
		//============= WE EXPLORE EACH PROJECTED DATABASE  ================================
		// For each  item
		for(Entry<Integer, List<Integer>> entry : mapSequenceID.entrySet()){
			// Get its support
			int support = entry.getValue().size();
			// if the item is frequent  (has a support >= minsup)
			if(support >= minsuppAbsolute){ 
				int item = entry.getKey();

				// check if the current item pass the backscan pruning method of the BIDE+ algorithm
				boolean passBackscanPruning = checkBackscanPruningSingleItemsFirstTime(item, entry.getValue());
				
				// if it passes the backscan pruning condition
				if(passBackscanPruning){
					
					// Create the sequential pattern with this item by copying the item in the buffer for the current patern
					patternBuffer[0] = item;

					// build the projected database for that item
					List<PseudoSequence> projectedDatabase  = buildProjectedDatabaseSingleItems(item, entry.getValue());
					
					// Make a recursive exploration of patterns by trying to extend the current pattern
					// This method returns the highest support of extensions found by this depth-first search
					int maxSupportExtensions = 0;
					 
					// If the maximum pattern length set by the user (optional) is greater than 1
					if(maximumPatternLength >1){
						maxSupportExtensions = recursionSingleItems(projectedDatabase, 2, 0); 
					}
					
					// If the current pattern passes the forward extension pruning condition of the BIDE+ algorithm
					// (that no extension of the current pattern has the same support)
					if(support != maxSupportExtensions){
					
						// Check if the pattern has a backward extension.
						boolean passBackwardExtensionChecking = checkBackwardExtensionSingleItemsFirstTime(item, entry.getValue());
			
						// If the pattern has no backward extension, then it is closed and it can be saved.
						if(passBackwardExtensionChecking){
							// save the pattern to the output file.
							savePattern(item, support, entry.getValue()); 
						}
					}
				}
			}
		}
	}

	/**
	 * This method checks if the current pattern respects the backscan pruning condition of the BIDE+ algorithm (if it
	 * should not be pruned).
	 * It is optimized for patterns containing a single item.
	 * @param the item the pattern consisting of a single item
	 * @param sequenceIDs the identifiers of the sequences containing this pattern
	 * @return true if there is no backward extension. Otherwise, false.
	 */
	private boolean checkBackscanPruningSingleItemsFirstTime(int item, List<Integer> sequenceIDs) {
		// A map is used to store the support of items that can form a backward extension
		// with the current pattern. Key: item  Value: support
		Map<Integer,Integer> mapItemSupport = new HashMap<Integer,Integer>();
		
		// As an optimization, we will use a variable to remember the highest support until now.
		int highestSupportUntilNow = 0;

		// for each sequence where the pattern appears
		for(int k =0; k < sequenceIDs.size(); k++){
			int sid = sequenceIDs.get(k);
			// Get the sequence
			int[] sequence = sequenceDatabase.getSequences().get(sid);
			
			// Clear the set to remember items that we have already seen in this sequence.
			// This set is used to avoid updating the support of the same item twice or more if it appears
			// several times in the same sequence.
			alreadySeen.clear();
			
			// for each item when reading the sequence forward
			for(int j=0; sequence[j] != -2; j++){
				// get the item
				int token = sequence[j];
				
				// if it is an item
				if(token > 0){
					// if we have found the item, we stop because we are looking for what
					// appear before the first occurrence of that item
					if(token == item){
						break;
					}else{
						// if it is an item that appear before the first occurrence of the item
						// and it is not the item used for projection, we increase its support by 1.
						// We first make sure that we have not updated the support of this item
						// already for the current sequence.
						if(alreadySeen.contains(token) == false){
							// Get the support of that item
							Integer itemSupport = mapItemSupport.get(token);
							// increase the support
							if(itemSupport == null){
								itemSupport = 1;
							}else{
								itemSupport++;
							}
							// update highest support until now
							if(itemSupport > highestSupportUntilNow){
								highestSupportUntilNow = itemSupport;
							}
							// update the support in the map
							mapItemSupport.put(token, itemSupport);
							
							// if this extension has the same support as the sequential pattern that
							// we want to check for a backward extension
							if(itemSupport == sequenceIDs.size()){
								// return false because it means that the current sequential patterns has a backward extension
								return false;
							}

							
							alreadySeen.add(token);
						}
					}
				}
			}
			
			// Optimization: if there is not enough sequences left for finding a backward extension given
			// the highest support found until now, we can know that there will not be any backward extension for that pattern.
			if(highestSupportUntilNow + (sequenceIDs.size() - k - 1) < sequenceIDs.size()) {
				return true;
			}
		}
		// There is no backward extension. Return true.
		return true;
	}

	
	/**
	 * This method checks if the current pattern has a backward extension.
	 * This method is optimized for patterns containing a single item.
	 * @param the item a pattern consisting of a single item
	 * @param sequenceIDs the identifiers of the sequences containing this pattern
	 * @return true if there is no backward extension. Otherwise false.
	 */
	private boolean checkBackwardExtensionSingleItemsFirstTime(int item, List<Integer> sequenceIDs) {
		
		// a map that will be used to count the support of items that can form a backward extension
		// with the current pattern
		// key: item  value: support
		Map<Integer,Integer> mapItemSupport = new HashMap<Integer,Integer>();
		
		// As an optimization, we will use a variable to remember the highest support until now.
		int highestSupportUntilNow = 0;

		// for each sequence where the pattern appears
		for(int k =0; k < sequenceIDs.size(); k++){
			int sid = sequenceIDs.get(k);
			int[] sequence = sequenceDatabase.getSequences().get(sid);
			
			// clear the set to remember item that we have already updated their support for the
			// current sequence
			alreadySeen.clear();
			
			// for each item when reading the sequence backward
			boolean foundTheItem = false;
			for(int j = sequence.length-1; j >= 0; j--){
				int token = sequence[j];
				// if it is an item
				if(token > 0){
					// if we have found the item, we remember that
					// because everything before can be a backward extension
					if(token == item){
						foundTheItem = true;
						continue;
					}
					if(foundTheItem){
						// if it is an item that appear before the last occurrence of the item
						// and it is not the item used for projection, we update its support
						// We first make sure that we have not updated the support of this item
						// already for the current sequence.
						if(alreadySeen.contains(token) == false){
							Integer itemSupport = mapItemSupport.get(token);
							if(itemSupport == null){
								itemSupport = 1;
							}else{
								itemSupport++;
							}

							// update highest support until now
							if(itemSupport > highestSupportUntilNow){
								highestSupportUntilNow = itemSupport;
							}

							// update the support in the map
							mapItemSupport.put(token, itemSupport);
							
							// if this extension has the same support as the item used
							// for database projection
							if(itemSupport == sequenceIDs.size()){
								return false;
							}
							
							alreadySeen.add(token);
						}
					}
				}
			}
			
			// Optimization: if there is not enough sequences left for finding a backward extension given
			// the highest support found until now, we can know that there will not be any backward extension for that pattern.
			if(highestSupportUntilNow + (sequenceIDs.size() - k - 1) < sequenceIDs.size()) {
				return true;
			}
		}
		
		return true;
	}

	/**
	 * Remove infrequent items from the sequence database. Then save each frequent items as a sequential pattern of length 1.
	 * Then, recursively explore extensions of patterns of length 1 using a depth first search.
	 * This method is designed for the case where the sequence database contains sequences having multiple items per itemsets.
	 * @param mapSequenceID a map where the key is an item and the value is the list of identifiers of sequences containing that item
	 * @throws IOException if there is an error while writing the result to the output file.
	 */
	private void bideWithMultipleItems(Map<Integer, List<Integer>> mapSequenceID) throws IOException {

		//=============== REMOVE INFREQUENT ITEMS ========================
		// We scan the database to remove infrequent items from sequences.
		// For each sequence in the current database
		for(int i=0; i < sequenceDatabase.size(); i++){
			int[] sequence  = sequenceDatabase.getSequences().get(i);
			
			// We will copy the frequent items one by one but not those items that are infrequent 
			// to obtain a sequence without the infrequent items.
			// The following variable will be used to remember the position were will copy next (we start at 0).
			int currentPosition = 0;
			// This variable is used to count the number of items in the current itemset (after removing infrequent items)
			int currentItemsetItemCount = 0;
			
			// for each token in this sequence (item, separator between itemsets (-1) or end of sequence (-2)
			for(int j =0; j < sequence.length; j++){
				int token = sequence[j];
				
				// if it is an item
				if(token > 0){
					// Check if that item is frequent
					boolean isFrequent = mapSequenceID.get(token).size() >= minsuppAbsolute;
					
					// if the item is frequent
					if(isFrequent){
						// copy the item to the current position
						sequence[currentPosition] = token;
						// increment the current position
						currentPosition++;
						// increment the number of items in the current itemset
						currentItemsetItemCount++;
					}	
				}else if(token == -1){
					// If we have reached the end of an itemset (-1 is the itemset separator)
					// If this itemset is not empty after having removed the infrequent items.
					if(currentItemsetItemCount >0){
						// copy the itemset separator (-1) to the current position
						sequence[currentPosition] = -1;
						// increment the current position
						currentPosition++;
						// reset the number of items in the current itemset for the next itemset
						currentItemsetItemCount = 0;
					}
				}else if(token == -2){
					// If we have reached the end of the current sequence (-2 is the symbol indicating the end of a sequence),
					// and if the sequence is not empty after having removed the infrequent items
					if(currentPosition >0){
						// copy the item to the current position
						sequence[currentPosition] = -2;
						
						// now replace the previous array representing the current sequence
						// with the new array where infrequent items have been removed.
						int[] newSequence = new int[currentPosition+1];
						System.arraycopy(sequence, 0, newSequence, 0, currentPosition+1);
						sequenceDatabase.getSequences().set(i, newSequence);
						// continue to next sequence
						continue; 
					}else{
						// If the sequence is  empty after having removed the infrequent items, 
						// we delete this sequence by replacing it with null.
						sequenceDatabase.getSequences().set(i, null);
					}
				}
			}
		}
		
		//============= WE EXPLORE EACH PROJECTED DATABASE  ================================
		// For each  item
		for(Entry<Integer, List<Integer>> entry : mapSequenceID.entrySet()){
			// Get its support
			int support = entry.getValue().size();
			// if the item is frequent  (has a support >= minsup)
			if(support >= minsuppAbsolute){ 
				int item = entry.getKey();
				
				// Check the backscan pruning condition for this item to determine if we should try to explore
				// larger pattern by extending this item
				boolean passBackscanPruning = checkBackscanPruningMultipleItemsFirstTime(item, entry.getValue());
				
				// If the item has passed the pruning condition (we should explore its supersets
				if(passBackscanPruning){
					System.out.println("PASSED BACKSCAN");

					// Create the current pattern by copying the item in the buffer
					patternBuffer[0] = item;

					// build the projected database for that item
					List<PseudoSequence> projectedDatabase  = buildProjectedDatabaseFirstTimeMultipleItems(item, entry.getValue());
					
					// Recursively try to extend this pattern to find larger patterns if the 
					// maximum pattern length set by the user is greater than 1 (an optional parameter).
					int maxSupportExtensions =0;
					if(maximumPatternLength >1){
						// Recursive call
						maxSupportExtensions = recursionMultipleItems(projectedDatabase, 2, 0); 
					}
					// If the current pattern has no forward extensions (no extension having the same support)
					// that means that it might be a closed pattern.
					if(support != maxSupportExtensions){
						// We check if the pattern passes the backward extension checking of the BIDE algorithm
						boolean passBackwardExtensionChecking = checkBackwardExtensionMultipleItemsFirstTime(item, entry.getValue());
						
						// If yes, the pattern is closed, and we save it to file or memory.
						if(passBackwardExtensionChecking){
//							System.out.println("PASSED");
							savePattern(item, support, entry.getValue()); 
						}
					}
				}else{
					System.out.println("FAILED BACKSCAN");
				}
			}
		}
	} 
	
	/**
	 * Check if a sequential pattern has has a backward extension. 
	 * This method is optimized for patterns containing a single item and sequence databases containing multiple
	 * items per itemsets.
	 * @param item the item
	 * @param sequenceIDs the list of sequence ids where this item appears
	 * @return true if this pattern has no backward extension. Otherwise false.
	 */
	private boolean checkBackwardExtensionMultipleItemsFirstTime(int item, List<Integer> sequenceIDs) {
		// We use three maps the store the support of possible item that could be used for the backscan pruning
		mapItemSupport.clear();
		mapsItemSupportPostfix.clear();

		// As an optimization, we will use a variable to remember the highest support until now.
		int highestSupportUntilNow = 0;
	
		// for each sequence where the pattern appears
		for(int k =0; k < sequenceIDs.size(); k++){
			// get the sequence
			int sid = sequenceIDs.get(k);
			int[] sequence = sequenceDatabase.getSequences().get(sid);

			// FIRST FIND THE LAST POSITION OF ei BY SCANNING THE SEQUENCE BACKWARD
			int posItem = 0;
			for(int j = sequence.length-1; ; j--){
				int token = sequence[j];
				// if we found the item
				if(token == item){
					// we remember that position
					posItem = j;
					// we stop scanning the sequence
					break;
				}
				
			}
			// reset the set to remember which items we have seen
			alreadySeen.clear();
			alreadySeenPostfix.clear();
			
			// variable to remember if the current itemset contains the item
			boolean itemsetContainsItem = true;
			// variable to remember if  the item before ei is contained in the same itemset  as ei
			boolean firstTimeContainsItem = (posItem >0) && sequence[posItem-1] != -1;
			
			// THEN, FOR EACH ITEM BEFORE THAT POSITION' WE WILL UPDATE ITS SUPPORT
			// for each token in this sequence (item, separator between itemsets (-1) or end of sequence (-2)
			for(int i = posItem-1; i >=0; i--){
				int token = sequence[i];
				
				// if it is another itemset
				if(token == -1){
					// we reset the variable to know if ei appears in the current itemset
					itemsetContainsItem = false;
					firstTimeContainsItem = false;
				}
				
				// if the token is an item
				if(token > 0){

					// check the different cases
					boolean couldBeExtension = false;
					boolean couldBePostfixExtension = false;
					
					// if the item is the pattern
					if(item == token){
						// we remember it
						itemsetContainsItem = true;
						couldBeExtension = true;
					}else{
						// otherwise it can be an s-extension if it is not appearing in the same itemset as ei
						couldBeExtension = !firstTimeContainsItem;
						// and it can definitely be a postfix extension (an i-extension)
						couldBePostfixExtension = itemsetContainsItem;
					}
					
					// if it can be a postfix extension
					if(couldBePostfixExtension){
						// and if we have not seen that item yet as a postfix extension in that sequence
						if(alreadySeenPostfix.contains(token) == false){
							// update the support of that item as a postfix of the current pattern 
							Integer itemSupport = mapsItemSupportPostfix.get(token);
							if(itemSupport == null){
								itemSupport = 1;
							}else{
								itemSupport++;
							}
							// update highest support until now
							if(itemSupport > highestSupportUntilNow){
								highestSupportUntilNow = itemSupport;
							}
							// update the support in the map
							mapsItemSupportPostfix.put(token, itemSupport);
	
							// if this extension has the same support as the pattern
							if(itemSupport == sequenceIDs.size()){
								return false;
							}
							// remember that we have seen that item in that sequence
							alreadySeenPostfix.add(token);
						}

					}
					// If the item can be an s-extension of the current pattern
					if(couldBeExtension){
						// and if we have not seen that item yet as an s-extension in that sequence
						if(alreadySeen.contains(token) == false){
							// Get the support of that item in the current projected 
							Integer itemSupport = mapItemSupport.get(token);
							// increase the support
							if(itemSupport == null){
								itemSupport = 1;
							}else{
								itemSupport++;
							}
							// update highest support until now
							if(itemSupport > highestSupportUntilNow){
								highestSupportUntilNow = itemSupport;
							}
							// update the support in the map
							mapItemSupport.put(token, itemSupport);
							
							// if this extension has the same support as the pattern
							if(itemSupport == sequenceIDs.size()){
								return false;
							}
							// Remember that we have seen that item in that sequence
							alreadySeen.add(token);		
						}
					}	
				}
			}
			
			// Optimization: if there is not enough sequences left for finding a backward extension given
			// the highest support found until now, we can know that there will not be any backward extension for that pattern.
			if(highestSupportUntilNow + (sequenceIDs.size() - k - 1) < sequenceIDs.size()) {
				return true;
			}
		}	
		// There is no backward extension so we return true
		return true;
	}

	/**
	 * Check if the current item and its extensions should be pruned using the backscan pruning
	 * This method is optimized for patterns containing a single item, and is designed for sequences 
	 *  where multiple items may appear in itemsets.
	 * @param item the current pattern, consisting of a single item
	 * @param sequenceIDs the list of sequence ids where this pattern appears
	 * @return  true if it should not be pruned, false if it should be pruned.
	 */
	private boolean checkBackscanPruningMultipleItemsFirstTime(int item, List<Integer> sequenceIDs) {		
		// We use two maps the store the support of possible item that could be used for the backscan pruning
		// We reset these maps
		mapItemSupport.clear();
		mapsItemSupportPostfix.clear();
		
		// As an optimization, we will use a variable to remember the highest support until now.
		int highestSupportUntilNow = 0;

		// for each sequence where the pattern appears
		for(int k =0; k < sequenceIDs.size(); k++){
			// Get the sequence
			int sid = sequenceIDs.get(k);
			int[] sequence = sequenceDatabase.getSequences().get(sid);


			// FIRST FIND THE POSITION OF THE ITEM ei BY SCANNING FORWARD
			// Variable to store the position of the item ei
			int posItem = 0;
			// Variable to store the position of the first item in the itemset containing ei
			int posItemset = 0;
			// For each item, itemset separator (-1) or end of sequence symbol (-2)
			for(int j = 0; ; j++){
				int token = sequence[j];
				// if we have found the item, than remember the position
				if(token == item){
					posItem = j;
					break;
				}
				if(token == -1){
					// if we have moved to another itemset, remember the position of its first item
					posItemset = j+1;
				}
			}

			// Reset the sets for remembering which items have been already seen in the current sequence.
			alreadySeen.clear();
			alreadySeenPostfix.clear();
			
			// THEN, FOR EACH ITEM BEFORE The positin of item ei WE WILL UPDATE ITS SUPPORT
			// for each token in this sequence (item, separator between itemsets (-1) or end of sequence (-2)
			for(int i = 0; i < posItem ; i++){
				int token = sequence[i];
				
				// if the token is an item
				if(token > 0){
					// if the current item would NOT be in the same itemset as the pattern
					if(i < posItemset){
						// If we did not see this item yet in this sequence
						if(alreadySeen.contains(token) == false){
							// Get its support
							Integer itemSupport = mapItemSupport.get(token);
							// Increase its support by 1
							if(itemSupport == null){
								itemSupport = 1;
							}else{
								itemSupport++;
							}
							// update highest support until now
							if(itemSupport > highestSupportUntilNow){
								highestSupportUntilNow = itemSupport;
							}
							// update the support in the map
							mapItemSupport.put(token, itemSupport);
							
							// if this extension has the same support as the pattern
							if(itemSupport == sequenceIDs.size()){
								return false;
							}
							// Remember that we have seen that item in that sequence
							alreadySeen.add(token);
						}
						
					}else{
						// If we did not see this item yet in this sequence
						if(alreadySeenPostfix.contains(token) == false){
							// Get its support
							Integer itemSupport = mapsItemSupportPostfix.get(token);
							// Increase its support by 1
							if(itemSupport == null){
								itemSupport = 1;
							}else{
								itemSupport++;
							}
							// update highest support until now
							if(itemSupport > highestSupportUntilNow){
								highestSupportUntilNow = itemSupport;
							}
							// update the support in the map
							mapsItemSupportPostfix.put(token, itemSupport);
	
							// if this extension has the same support as the pattern
							if(itemSupport == sequenceIDs.size()){
								return false;
							}
							// Remember that we have seen that item in that sequence
							alreadySeenPostfix.add(token);
						}
					}	
				}
			}
			
			// Optimization: if there is not enough sequences left for finding a backward extension given
			// the highest support found until now, we can know that there will not be any backward extension for that pattern.
			if(highestSupportUntilNow + (sequenceIDs.size() - k - 1) < sequenceIDs.size()) {
				return true;
			}
		}		
		// The backscan pruning condition has been successfully passed by the sequential pattern.
		return true;
	}

	/**
	 * This method saves a sequential pattern containing a single item to the output file or
	 * in memory, depending on if the user provided an output file path or not
	 * when he launched the algorithm
	 * @param item the pattern to be saved.
	 * @param support the support of this item
	 * @param sequenceIDs the list of sequences containing this item
	 * @throws IOException exception if error while writing the output file.
	 */
	private void savePattern(int item, int support, List<Integer> sequenceIDs) throws IOException {
		// increase the number of pattern found for statistics purposes
		patternCount++; 
	
		// if the result should be saved to a file
		if(writer != null){
			// create a StringBuilder
			StringBuilder r = new StringBuilder();
			r.append(item);
			r.append(" -1 #SUP: ");
			r.append(support);
			if(showSequenceIdentifiers) {
	        	r.append(" #SID: ");
	        	for (Integer sid: sequenceIDs) {
	        		r.append(sid);
	        		r.append(" ");
	        	}
			}
			// write the string to the file
			writer.write(r.toString());
			// start a new line
			writer.newLine();
		}
		// otherwise the result is kept into memory
		else{
			// Create an object to store the pattern
			SequentialPattern pattern = new SequentialPattern();
			pattern.addItemset(new Itemset(item));
			pattern.setSequenceIDs(sequenceIDs);
			
			// Store in a structure to store patterns
			patterns.addSequence(pattern, 1);
		}
	}
	
	/**
	 * Save a pattern containing two or more items to the output file (or in memory, depending on what the user prefer)
	 * @param lastBufferPosition the last position in the buffer for this pattern
	 * @param pseudoSequences the list of pseudosequences where this pattern appears.
	 * @param length the pattern length in terms of number of items.
	 * @throws IOException if error when writing to file
	 */
	private void savePattern(int lastBufferPosition, List<PseudoSequence> pseudoSequences) throws IOException {
		// increase the number of pattern found for statistics purposes
		patternCount++;  
	
		// if the result should be saved to a file
		if(writer != null){
			// create a StringBuilder
			StringBuilder r = new StringBuilder();
			// For each element in this pattern
			for(int i=0; i <= lastBufferPosition; i++){
				// append the element
				r.append(patternBuffer[i]);
				// if this database does not have multiple items per itemset in sequence
				if(containsItemsetsWithMultipleItems == false){
					// append the -1 separator
					r.append(" -1");
				}
				r.append(" ");
			}
			
			// if this database does not have multiple items per itemset in sequence
			if(containsItemsetsWithMultipleItems == false){
				r.append("-1 ");
			}
			
			// append the support
			r.append("#SUP: ");
			r.append(pseudoSequences.size());
			
			// if the user wants to see the sequence identifiers
			if(showSequenceIdentifiers) {
	        	r.append(" #SID: ");
	        	for (PseudoSequence sequence: pseudoSequences) {
	        		r.append(sequence.sequenceID);
	        		r.append(" ");
	        	}
			}
			// write the string to the file
			writer.write(r.toString());
			// start a new line
			writer.newLine();
		}
		// otherwise the result is kept into memory
		else{
			// Create the pattern
			SequentialPattern pattern = new SequentialPattern();
			int itemsetCount  =0;
			Itemset currentItemset = new Itemset();
			// For each element in that pattern
			for(int i=0; i <= lastBufferPosition; i++){
				int token = patternBuffer[i];
				// if it is an item
				if(token>0){
					currentItemset.addItem(token);
				}else if(token == -1){
					// if it is an itemset separator
					pattern.addItemset(currentItemset);
					currentItemset = new Itemset();
					itemsetCount++;
				}
			}
			// add the last itemset to the pattern
			pattern.addItemset(currentItemset);
			// increase the count of the number of itemsets in that pattern
			itemsetCount++;
			
			// create a list to store the identifiers of sequences containing that pattern
			List<Integer> sequencesIDs = new ArrayList<Integer>(pseudoSequences.size());
			for (int i = 0; i< pseudoSequences.size(); i++) {
				sequencesIDs.add(pseudoSequences.get(i).sequenceID);
        	}
			pattern.setSequenceIDs(sequencesIDs);
			
			// add the pattern to the set of patterns found until now, stored in memory
			patterns.addSequence(pattern, itemsetCount);
		}
	}
	
	
	/**
	 * Scan the database to obtain the list of identifiers of sequences where each item appears, and also
	 * determine if this database contains multiple items per itemset.
	 * @return Map where the key is an item and the value is the list of identifiers of sequence where this item appears.
	 */
	private Map<Integer, List<Integer>> findSequencesContainingItems() {
		// number of items in the current itemset 
		int itemCountInCurrentItemset;
		
		// We use a map to store the sequence IDs where an item appear
		// Key : item   Value :  a set of sequence IDs
		Map<Integer, List<Integer>> mapSequenceID = new HashMap<Integer, List<Integer>>(); 
		
		// for each sequence in the current database
		for(int i=0; i < sequenceDatabase.size(); i++){
			int[] sequence  = sequenceDatabase.getSequences().get(i);
			
			// reset the variable to count the number of items in the current itemset
			 itemCountInCurrentItemset = 0;
			 
			// for each token in this sequence (item, separator between itemsets (-1) or end of sequence (-2)
			for(int token : sequence){
				// if it is an item
				if(token > 0){
					// get the set of sequence IDs for this item until now
					List<Integer> sequenceIDs = mapSequenceID.get(token);
					if(sequenceIDs == null){
						// if the set does not exist, create one
						sequenceIDs = new ArrayList<Integer>();
						mapSequenceID.put(token, sequenceIDs);
					}
					// add the sequence ID to the 
					// set of sequences IDs of this item
					// if it is not already there
					if(sequenceIDs.size() == 0 || sequenceIDs.get(sequenceIDs.size() - 1) != i){
						sequenceIDs.add(i);
					}
					// increase the count of the number of items in this itemset
					itemCountInCurrentItemset++;
					// if this itemset contains more than 1 item, we will remember that this database
					// contains sequence with multiple items for optimization purpose.
					if(itemCountInCurrentItemset > 1){
						containsItemsetsWithMultipleItems = true;
					}
				}else if(token == -1){
					// if it is the end of an itemset, reste the item count per itemset.
					itemCountInCurrentItemset = 0;
				}
			}
		}
		// return the map
		return mapSequenceID;
	}
	
	/**
	 * Create a projected database by pseudo-projection with the initial database and a given item.
	 * This method is optimized for the case of a database containing a single item per itemset in sequences
	 * @param item The item to use to make the pseudo-projection
	 * @param list  The list of sequence ids containing the item
	 * @return the projected database.
	 */
	private List<PseudoSequence> buildProjectedDatabaseSingleItems(int item, List<Integer> sequenceIDs) {
		// We create a new projected database
		List<PseudoSequence> projectedDatabase = new ArrayList<PseudoSequence>();

		// for each sequence that contains the current item
loopSeq:for(int sequenceID : sequenceIDs){ 
			int[] sequence = sequenceDatabase.getSequences().get(sequenceID);
			
			// for each token in this sequence (item  or end of sequence (-2)
			for(int j=0; sequence[j] != -2; j++){
				int token = sequence[j];
				
				// if it is the item that we want to use for projection
				if(token == item){
					// if it is not the end of the sequence
					if(sequence[j+1] != -2){
						// Create a pseudo-sequence by cutting that sequence at position j, so that items from
						// position j+1 and after remains in the sequence
						PseudoSequence pseudoSequence = new PseudoSequence(sequenceID, j+1);
						// add the sequence to the projected database
						projectedDatabase.add(pseudoSequence);
					}
					
					// we have created the pseudosequence for the current sequence, so we continue to the next sequence
					continue loopSeq;
				}
			}
		}
		return projectedDatabase; // return the projected database
	}
	
	/**
	 * Create a projected database by pseudo-projection with the initial database and a given item.
	 * This method is optimized for the case of sequences that may contain multiple items per itemset.
	 * @param item The item to use to make the pseudo-projection
	 * @param list  The list of sequence ids containing the item
	 * @return the projected database.
	 */
	private List<PseudoSequence> buildProjectedDatabaseFirstTimeMultipleItems(int item, List<Integer> sequenceIDs) {
		// We create a new projected database
		List<PseudoSequence> projectedDatabase = new ArrayList<PseudoSequence>();

		// for each sequence that contains the current item
loopSeq:for(int sequenceID : sequenceIDs){ 
			int[] sequence = sequenceDatabase.getSequences().get(sequenceID);
			
			// for each token in this sequence (item, separator between itemsets (-1) or end of sequence (-2)
			for(int j=0; sequence[j] != -2; j++){
				int token = sequence[j];
				
				// if it is the item that we want to use for projection
				if(token == item){
					// if it is not the end of the sequence
					boolean isEndOfSequence = sequence[j+1] == -1 && sequence[j+2] == -2;
					if(isEndOfSequence == false){
						// Create a pseudo-sequence by cutting that sequence at position j, so that items from
						// position j+1 and after remains in the sequence
						PseudoSequence pseudoSequence = new PseudoSequence(sequenceID, j+1);
						// add the projected sequence to the projected database
						projectedDatabase.add(pseudoSequence);
					}

					// we have created the pseudosequence for the current sequence, so we continue to the next sequence
					continue loopSeq;
				}
			}
		}
		// return the projected database
		return projectedDatabase; 
	}
	

	/**
	 * Method to recursively grow a given sequential pattern.
	 * This method is optimized for sequence databases that may contain only a single item per itemset in sequences
	 * @param database the current projected sequence database
	 * @param k  the prefix length in terms of items
	 * @param lastBufferPosition the last position used in the buffer for storing the current prefix
	 * @return the largest support among all extensions explored by this method.
	 * @throws IOException exception if there is an error writing to the output file
	 */
	private int recursionSingleItems(List<PseudoSequence> database, int k, int lastBufferPosition) throws IOException {	

		// variable to store the largest support of extensions of the current pattern
		int maxSupport = 0;
		
		// Scan the projected database to calculate the list of pseudosequences containing each item.
		// We obtain a map where a key is an item and the value is the corresponding list of pseudosequences
		Map<Integer,List<PseudoSequence>> itemsPseudoSequences = findAllFrequentPairsSingleItems(database, lastBufferPosition);
		
		// release the memory used by the database
		database = null;
	
		// For each pair found (a pair is an item with a boolean indicating if it
		// appears in an itemset that is cut (a postfix) or not, and the sequence IDs
		// where it appears in the projected database).
		for(Entry<Integer, List<PseudoSequence>> entry : itemsPseudoSequences.entrySet()){
			// get the support of the item
			int support = entry.getValue().size();
			//if the item is frequent in the current projected database
			if(support >= minsuppAbsolute){
				// remember if it is the highest support found until now
				if(support > maxSupport){
					maxSupport = support;
				}
				
				//Create the new pattern by appending the item  to the current pattern that we are extending
				patternBuffer[lastBufferPosition+1] = entry.getKey();

				// check if the resulting pattern passes the backscan pruning check
				boolean passBackscanPruning = checkBackscanPruningSingleItems(lastBufferPosition+1, entry.getValue());
				
				// if we should not prune the resulting pattern according to the backscan pruning
				if(passBackscanPruning){
					// we use this variable to find the largest support of extension of the new  pattern
					
					int maxSupportExtensions = 0;
					// make a recursive call
					if( k < maximumPatternLength){
						// make a recursive call to recursively extend the new pattern
						maxSupportExtensions = recursionSingleItems(entry.getValue(), k+1, lastBufferPosition+1);
					}
					
					// if the new pattern has no forward extension (an extension having the same support)
					if(support != maxSupportExtensions){

						// we still need to check if this pattern has a backward extension to determine if 
						// it is closed
						boolean passBackwardExtensionChecking = checkBackwardExtensionSingleItems(lastBufferPosition+1, entry.getValue());
			
						// if it has no backward extension, it is closed
						if(passBackwardExtensionChecking){		
							// Thus, we save the pattern
							savePattern(lastBufferPosition+1, entry.getValue());
						}
					}
				}	
			}
		}

		// check the current memory usage
		MemoryLogger.getInstance().checkMemory();
		
		// Return the largest support among all extensions explored by this method.
		return maxSupport;
	}
	
	/**
	 * This method checks if the current pattern respects the backscan pruning condition.
	 * This method is optimized for sequence databases that may contain only a single item per itemset in sequences
	 * @param lastBufferPosition the last position used in the buffer for storing the current pattern
	 * @param projectedDatabase the projected dtabase
	 * @return true if there is not backward extension. Otherwise false.
	 */
	private boolean checkBackscanPruningSingleItems(int lastBufferPosition, List<PseudoSequence> projectedDatabase) {

		// we will check the backscan pruning condition for each item in the current pattern
loopi:	for(int i=0; i <= lastBufferPosition; i++){
			// As an optimization, we will use a variable to remember the highest support until now.
			int highestSupportUntilNow = 0;
			
			// reset the map for counting the support of items 
			mapItemSupport.clear();
			
			// for each sequence where the pattern appears
			// for each sequence where the pattern appears
loopSeq:	for(int k =0; k < projectedDatabase.size(); k++){
				PseudoSequence pseudoSequence = projectedDatabase.get(k);
				// get the original sequence
				int sid = pseudoSequence.getOriginalSequenceID();
				int[] sequence = sequenceDatabase.getSequences().get(sid);
				
				// we will try to match all items before the position of the current item
				int currentPositionToMatch = 0;
				
				// clear the set to remember item that we have already updated their support for the
				// current sequence
				alreadySeen.clear();
				
				// for each item when reading the sequence forward
				for(int j=0; sequence[j] != -2; j++){
					int token = sequence[j];
					// if it is an item
					if(token > 0){
						// if we have found the item that we are looking
						if(token == patternBuffer[currentPositionToMatch]){
							// if it was the last item that we were looking for, we stop
							if(i == currentPositionToMatch){
								continue loopSeq;
							}
							// otherwise, we will search for the next item
							currentPositionToMatch++;
						}else{
							// if it is an item that appear before the item that we are looking for
							// and we did not count its support yet for this sequence
							// and it is between the previous item that we are looking for an the one that we are looking for
							if(alreadySeen.contains(token) == false && currentPositionToMatch == i){
								Integer itemSupport = mapItemSupport.get(token);
								if(itemSupport == null){
									itemSupport = 1;
								}else{
									itemSupport++;
								}
								// update highest support until now
								if(itemSupport > highestSupportUntilNow){
									highestSupportUntilNow = itemSupport;
								}
								// update the support in the map for that item
								mapItemSupport.put(token, itemSupport);
								
								// if this extension has the same support as the item used
								// for database projection
								if(itemSupport == projectedDatabase.size()){
									// it means that there the current pattern should be bascan pruned
									return false;
								}
								// remember that we counted the support of that item in that sequence 
								alreadySeen.add(token);
							}
						}
					}
				}
				
				// Optimization: if there is not enough sequences left for finding a backward extension given
				// the highest support found until now, we can know that there will not be any backward extension for that pattern.
				if(highestSupportUntilNow + (projectedDatabase.size() - k - 1) < projectedDatabase.size()) {
					continue loopi;
				}
			}
		}
		// passed the backscan pruning.
		return true;
	}
	
	/**
	 * This method checks if the current pattern respect the backscan pruning condition.
	 * This method is optimized for sequence databases that may contain only a single item per itemset in sequences
	 * @param lastBufferPosition the last position used in the buffer for storing the current prefix
	 * @param sequenceIDs the ids of sequences containing this pattern
	 * @return true if there is not backward extension. Otherwise false.
	 */
	private boolean checkBackwardExtensionSingleItems(int lastBufferPosition, List<PseudoSequence> projectedDatabase) {

		
		// we will check if there is a backward extension for each item in the current pattern
		// (an extension between i and i-1 in the following)
loopi:	for(int i=0; i <= lastBufferPosition; i++){
			// As an optimization, we will use a variable to remember the highest support until now.
			int highestSupportUntilNow = 0;
			
			// reset the map for counting the support of items
			mapItemSupport.clear();
			
			// for each sequence where the pattern appears
			// for each sequence where the pattern appears
			for(int k =0; k < projectedDatabase.size(); k++){
				PseudoSequence pseudoSequence = projectedDatabase.get(k);
				// get the original sequence
				int sid = pseudoSequence.getOriginalSequenceID();
				int[] sequence = sequenceDatabase.getSequences().get(sid);
				
				// FIRST FIND THE POSITION OF THE FIRST INSTANCE OF e1, e2... ei-1
				int currentPositionToMatch1 = 0;
				
				// this variable will record the position after the first instance of e1... ei-1
				int posAfterFirstInstance = 0;
				if(i!=0){
					// for each item when reading the sequence forward, we try to match the first i items
					for(int j=0; j < sequence.length; j++){
						int token = sequence[j];
						// if it is an item
						if(token > 0){
							// if we have found the item that we are looking for
							if(token == patternBuffer[currentPositionToMatch1]){
								// if it was the last item that we were looking for, we stop
								if(currentPositionToMatch1 == i-1){
									// we found the end of the first instance, so we stop
									posAfterFirstInstance = j+1;
									break;
								}
								// otherwise, we will search for the next item
								currentPositionToMatch1++;
							}
						}
					}
				}
				
				// SECOND FIND THE POSITION OF THE OTHER PART  ei ei+1... by going backward
				// we will try to match all items after the position of the current item
				int currentPositionToMatch = lastBufferPosition;
				
				// clear the set to remember item that we have already updated their support for the
				// current sequence
				alreadySeen.clear();
				
				// for each item when reading the sequence backward
				for(int j=sequence.length-1; j >= posAfterFirstInstance; j--){
					int token = sequence[j];
					// if it is an item
					if(token > 0){
						// if we have found the item that we are looking for
						if(currentPositionToMatch >= i && token == patternBuffer[currentPositionToMatch]){
							// found an item, we will search for the next item
							currentPositionToMatch--;
						}else{
							// if it is an item that appear before the item  ei and after ei-1
							// and we did not count its support yet for this sequence
							// and it is between the item that we are looking for and the one after
							if(currentPositionToMatch == i-1 && alreadySeen.contains(token) == false){
								Integer itemSupport = mapItemSupport.get(token);
								if(itemSupport == null){
									itemSupport = 1;
								}else{
									itemSupport++;
								}
								// update highest support until now
								if(itemSupport > highestSupportUntilNow){
									highestSupportUntilNow = itemSupport;
								}
								// update the support in the map for that item
								mapItemSupport.put(token, itemSupport);
								
								// if this extension has the same support as the item used
								// for database projection
								if(itemSupport == projectedDatabase.size()){
									// it means that there the current pattern has a backward extension
									return false;
								}
								// remember that we counted the support of that item in that sequence 
								alreadySeen.add(token);
							}
						}
					}
				}
				// end
				
				
				// Optimization: if there is not enough sequences left for finding a backward extension given
				// the highest support found until now, we can know that there will not be any backward extension for that pattern.
				if(highestSupportUntilNow + (projectedDatabase.size() - k - 1) < projectedDatabase.size()) {
					continue loopi;
				}
			}
		}
		// passed the backward extension checking.
		return true;
	}
	
	/**
	 * Method to recursively grow a given sequential pattern.
	 * This method is optimized for sequence databases that may contain multiple items per itemset in sequences.
	 * @param database the current projected sequence database
	 * @param k  the prefix length in terms of items
	 * @param lastBufferPosition the last position used in the buffer for storing the current prefix
	 * @return the support of the most frequent extension of the current pattern
	 * @throws IOException exception if there is an error writing to the output file
	 */
	private int recursionMultipleItems(List<PseudoSequence> database, int k, int lastBufferPosition) throws IOException {

		// variable to store  the support of the most frequent extension of the current pattern
		int maxSupport = 0;
		
		// FIND FREQUENT PAIRS
		// find frequent items of size 1 in the current projected database, and at the same
		// time create their respective projected databases
		// We create some maps of pairs for storing the frequent items. The following object
		// contains two maps. The first one is for item extending the current pattern as an i-extension,
		// while the second is for item extending the current pattern as an s-extension.
		MapFrequentPairs mapsPairs = findAllFrequentPairs(database, lastBufferPosition);
		
		// release the memory used by the database
		database = null;
	
		// For each pair found that is in a postfix itemset(a pair is an item with a boolean indicating if it
		// appears in an itemset that is cut (a postfix) or not, and the sequence IDs
		// where it appears in the projected database).
		for(Entry<Pair,Pair> entry : mapsPairs.mapPairsInPostfix.entrySet()){
			Pair pair = entry.getKey();
			// Get the support of that pair
			int support = pair.getCount();
			
			// if the item is frequent in the current projected database
			if(pair.getCount() >= minsuppAbsolute){
				
				// If it is the item with the highest support until, we will remember that support value
				if(support > maxSupport){
					maxSupport = support;
				}

				// we append the item to the current pattern to create a new pattern
				int newBuferPosition = lastBufferPosition;
				newBuferPosition++;
				patternBuffer[newBuferPosition] = pair.item;
								
				// We check if the new pattern passes the backscan pruning condition
				boolean passBackscanPruning = checkBackscanPruningMultipleItems(newBuferPosition, entry.getValue().getPseudoSequences());
				
				// If the new pattern should  not be pruned according to the backscan pruning condition
				if(passBackscanPruning){
					System.out.println("PASSED");
					// make a recursive call to explore extensions of the new pattern
					int maxSupportExtensions = 0;
					if( k < maximumPatternLength){
						// the recursive call return the largest support value among extension of the new pattern
						 maxSupportExtensions = recursionMultipleItems(pair.getPseudoSequences(), k+1, newBuferPosition);
					}

					// if the new pattern has no forward extension (an extension having the same support, it may be closed
					if(support != maxSupportExtensions){
						// but we still need to check if it has a backward extension
						boolean passBackwardExtensionChecking = checkBackwardExtensionMultipleItems(newBuferPosition, entry.getValue().getPseudoSequences());
						// If the new pattern has no backward extension
						if(passBackwardExtensionChecking){	
							System.out.println("PASSED");
							// Then it is closed, so we save the pattern
							savePattern(newBuferPosition, pair.getPseudoSequences());	
						}
					}				
				}		
			}
		}
		
		// For each pair found representing an item that is not in a postfix
		for(Entry<Pair,Pair> entry : mapsPairs.mapPairs.entrySet()){
			Pair pair = entry.getKey();
			// get the support of that item
			int support = pair.getCount();

			
			// if the item is frequent in the current projected database
			if(support >= minsuppAbsolute){

				// If it is the item with the highest support until, we will remember that support value
				if(support > maxSupport){
					maxSupport = support;
				}

				// we append the item to the current pattern to create a new pattern
				int newBuferPosition = lastBufferPosition;
				newBuferPosition++;
				patternBuffer[newBuferPosition] = -1;
				newBuferPosition++;
				patternBuffer[newBuferPosition] = pair.item;
				
				
//				if(lastBufferPosition == 0 && patternBuffer[0] == 5 && pair.item == 2){
//					System.out.println("test");
//				}

				// We check if the new pattern passes the backscan pruning condition
				boolean passBackscanPruning = checkBackscanPruningMultipleItems(newBuferPosition, entry.getValue().getPseudoSequences());

				// If the new pattern should  not be pruned according to the backscan pruning condition
				if(passBackscanPruning){

					// make a recursive call to explore extensions of the new pattern
					int maxSupportExtensions = 0;
					// make a recursive call
					if( k < maximumPatternLength){
						// the recursive call return the largest support value among extension of the new pattern
						maxSupportExtensions = recursionMultipleItems(pair.getPseudoSequences(), k+1, newBuferPosition);
					}

					// if the new pattern has no forward extension (an extension having the same support, it may be closed
					if(support != maxSupportExtensions){

						// but we still need to check if it has a backward extension
						boolean passBackwardExtensionChecking = checkBackwardExtensionMultipleItems(newBuferPosition, entry.getValue().getPseudoSequences());
						// If the new pattern has no backward extension
						if(passBackwardExtensionChecking){	
							// Then it is closed, so we save the pattern
							savePattern(newBuferPosition, pair.getPseudoSequences());
						}
					}
				
				}
			}
		}
		// check the current memory usage
		MemoryLogger.getInstance().checkMemory();
		
		// return the support of the largest extension of the current pattern
		return maxSupport;
		
	}
	
	/**
	 * Check if there is a backward extension, for a pattern of length greater than 1.
	 * This method is optimized for sequence databases that may contain multiple items per itemset in sequences
	 * @param lastBufferPosition the last item of the current pattern in the buffer 
	 * @param sequences a projected database
	 * @return true if the pattern has no backward extension. Otherwise, false.
	 */
	private boolean checkBackwardExtensionMultipleItems(int lastBufferPosition, List<PseudoSequence> sequences) {
		
		// DEBUG ===== PRINT THE CURRENT PATTERN ================
		for(int i=0; i<= lastBufferPosition; i++){
			if(patternBuffer[i] == -1)
				System.out.print("|");
			else
				System.out.print(patternBuffer[i]);
		}
		System.out.println();
		// END DEBUG ============================================

		
		// we will check if there is a backward extension for each item in the current pattern
loopi:	for(int i=0; i <= lastBufferPosition; i++){
			// As an optimization, we will use a variable to remember the highest support until now.
			int highestSupportUntilNow = 0;
			
			if(lastBufferPosition == 5  
					&& patternBuffer[0] == 6 
					&& patternBuffer[1] == -1
					&& patternBuffer[2] == 4 
					&& patternBuffer[3] == 5
					&& patternBuffer[4] == -1 
					&& patternBuffer[5] == 7
					){
				System.out.println("POS: " + i);
				System.out.println();
			}
			
			// =======  IMPORTANT ==============
			// skip if there is a -1 (an itemset separator)
			if(patternBuffer[i] == -1){
				continue;
			}
			//===================================
			
			//===========================================
			// ====== REALLY IMPORTANT ==================
			// if the position before i is a -1 in the pattern buffer,
			// we backward one more position to get the real position of
			// ei-1 in the buffer.
			int posIminus1 = i-1;
			if(i>0 && patternBuffer[i-1] == -1){
				posIminus1--;
			}
			// ==========================================
			//===========================================
			
			// We use three maps the store the support of possible item that could be used for the backscan pruning
			mapItemSupport.clear();
			mapsItemSupportPostfix.clear();
			mapsItemSupportSuffix.clear();
			
			// for each sequence
			// for each sequence where the pattern appears
			for(int k =0; k < sequences.size(); k++){
				PseudoSequence pseudoSequence = sequences.get(k);
				int sid = pseudoSequence.getOriginalSequenceID();
				int[] sequence = sequenceDatabase.getSequences().get(sid);
				
				
				if(lastBufferPosition == 5  
						&& patternBuffer[0] == 6 
						&& patternBuffer[1] == -1
						&& patternBuffer[2] == 4 
						&& patternBuffer[3] == 5
						&& patternBuffer[4] == -1 
						&& patternBuffer[5] == 7
						){
					System.out.println("SEQ ID" + sequences.get(k).sequenceID);
					System.out.print("");
				}
				
				// clear the variables to remember which items we have already seen in the current sequence
				// that can extend the current pattern as a:
				// s-extension
				alreadySeen.clear();
				// i-extension that is a postfix
				alreadySeenPostfix.clear();
				// i-extension that is a suffix
				alreadySeenSuffix.clear();
			
				//======================================================
				// FIRST we will try to match  e1, e2, ei-1...
				// (all items before the position of the current item)
				// Note that it is possible that ei, ei+1... appears in the same itemset as ei-1.
				// In that case, we also need to match these itemsets
	
				// This variable is used to remember the begining of the current itemset in the pattern
				int resetPosition = 0;
				// This variable is used to remember the position of the current item to match in the pattern
				int currentPositionToMatch = 0;
				// This is the position of the item ei-1
				int posItemFirst = 0;
				// This is the position of the first item of the itemset in the sequence containing ei-1
				int posItemsetFirst = 0;
				
				
				// We only do that if i >0  because otherwise, we want to scan from the begining of the sequence
				if(i > 0){
					// For each item in the sequence...
					for(int j = 0; ; j++){
						// This is the current item in the sequence.
						int token = sequence[j];
						
						// IF it is the end of an itemset in the sequence
						// it means that we have not matched the whole itemset of the pattern
						if(token == -1){
							// thus we need to restart trying to match the current itemset of the pattern
							currentPositionToMatch = resetPosition; 
							posItemsetFirst = j+1;
							posItemFirst = 0;
						}
						
						if(token > 0){
							// If it is an item that we want to match
							if(token == patternBuffer[currentPositionToMatch]){
								// if it is ei-1 we will save its position
								if(currentPositionToMatch == posIminus1){
									posItemFirst = j;
								}
		
								// increase the position to look for
								currentPositionToMatch++;
							
								// if we have matched the full itemset in the pattern and 
								// and that itemset contained ei-1, then we can stop here
								if(currentPositionToMatch > lastBufferPosition ||
										patternBuffer[currentPositionToMatch] == -1
									){
									
									if(currentPositionToMatch >= posIminus1 ){
										break;
									}
									// Since we have matched the full itemset of the pattern, we can move to the next one
									// in the sequence.
									while(sequence[j] != -1){
										j++;
									}
									j++;
									
									// ======= IMPORTANT ======
									// We move to the next position in the buffer because
									// the current position is a -1
									currentPositionToMatch++;
									//==========================
									
									// Since we have matched a full itemset of the pattern, 
									// next time, we will start to match from the next position
									resetPosition = currentPositionToMatch;
								}
							}
						}
					}
				}

			//======================================================
			// Second, we will look for  ei ei+1 etc.  by scanning backward to get the last occurrence of ei.
			// Two possibitilies:
			//  -  ei-1 is in the same itemset as ei, ei-1, ei-2...
			//  -  ei-1 is in an itemset appearing before another itemset where ei appears
			// If ei and ei-1 are within the same itemset (there is no -1 separator between them).
				
			// This variable is used to remember the begining of the current itemset in the pattern
			resetPosition = lastBufferPosition;
			
			// This variable is used to remember the position of the current item to match in the pattern
			currentPositionToMatch = lastBufferPosition;
			
			// This is the position of the item ei-1
			int posItemLast = 99999;
			
			// This is the position of the first item of the itemset in the sequence containing ei-1
			int posLastItemset = 99999;
			
			// For each item in the sequence starting from the last one 
			// (before the "-1 -2" representing the end of sequence)
			for(int j = sequence.length-3; ; j--){
				// This is the current item in the sequence.
				int token = sequence[j];
				
				// IF it is the end of an itemset in the sequence
				// it means that we have not matched the whole itemset of the pattern
				if(token == -1){
					// thus we need to restart trying to match the current itemset of the pattern
					currentPositionToMatch = resetPosition; 
					posLastItemset = j-1;
					posItemLast = 99999;
				}
				
				if(token > 0){
					// If it is an item that we want to match
					if(token == patternBuffer[currentPositionToMatch]){
						// if it is ei we will save its position
						if(currentPositionToMatch == i){
							posItemLast = j;
						}

						// decreasethe position to look for
						currentPositionToMatch--;
					
						// if we have matched the full itemset in the pattern and 
						// and that itemset contained ei, then we can stop here
						if(currentPositionToMatch < 0 || patternBuffer[currentPositionToMatch] == -1){
							if(currentPositionToMatch <= i ){
								break;
							}
							// Since we have matched the full itemset of the pattern, we can move to the next one
							// in the sequence.
							while(sequence[j] != -1){
								j--;
							}
//							j--;  Don't need it because the for loop will decrement j
							
							// ======= IMPORTANT ======
							// We move to the next position in the buffer because
							// the current position is a -1
							currentPositionToMatch--;
							//==========================
							
							// Since we have matched a full itemset of the pattern, 
							// next time, we will start to match from the next position
							resetPosition = currentPositionToMatch;
						}
	
					}
				}
			}			

			///=================================================================================
			// NEXT WE WILL UPDATE THE SUPPORT OF ITEMS in the  [posItemFirst, posItemLast] interval
//			for(int j = posItemFirst; j <= posItemLast ; j++){
//				int token = sequence[j];
//				
//			}
			
			// variable to remember that we start in the itemset for ei-1
			boolean firstItemstIsCut = i!=0 && sequence[posItemFirst] != -1;
			boolean lastItemsetIsCut = posItemLast >= 0 && sequence[posItemLast] != -1;  // NEW
			boolean inFirstPostfix = firstItemstIsCut;
			boolean firstItemsetIsMiddleCut = i!=0 && patternBuffer[i-1] != -1;

			if(firstItemsetIsMiddleCut){
				// we check if all the items appear
				int posToMatch = posItemLast+1;
				inFirstPostfix = false; 
				for(int j = posItemFirst; sequence[j] != -1 ; j++){
					if(sequence[j] == sequence[posToMatch]){
						posToMatch++;
						if(sequence[posToMatch] == -1 ){
							inFirstPostfix = true;
							break;
						}
					}
				}
			}
			
			boolean inAnotherPostfix = false;
			// we will try to match the last itemset containing ei-1 to determine if the current itemset is a postfix
			
			// ==== EXPERIMENTAL ====
			 resetPosition = i-1;
			if(resetPosition >=0 && patternBuffer[resetPosition] == -1){
				resetPosition--;
			}
			///==== END EXPERIMENTAL ====

			//===== EXPERIMENTAL : I CHANGED TO RESET POSITION INSTEAD OF ITEMSETFIRST
			int postfixItemToMatch = resetPosition;
			//====================================
			
			for(int j = posItemFirst; j <= posItemLast ; j++){
				int token = sequence[j];
				
				if(token == -1){
					inFirstPostfix = false;
					inAnotherPostfix = false;
					
					// we will try to match the last itemset containing ei-1 to determine if the current itemset is a postfix
					//===== EXPERIMENTAL : I CHANGED TO RESET POSITION INSTEAD OF ITEMSETFIRST
					postfixItemToMatch = resetPosition;
					// ===== END EXPERIMENTAL
					
					//===== EXPERIMENTAL : I PUT THE FOLLOWING LINES INTO COMMENTS : 
					// INSERTED TO FIX BUG: 2016-03-13
//					if(patternBuffer[postfixItemToMatch] == -1){
//						postfixItemToMatch++;
//					}
					// ===== END EXPERIMENTAL
				} 
				if(token > 0){
					boolean justMatched = false;
					// if we matched the full itemset containing ei-1
					if(!inAnotherPostfix && i != 0 &&  patternBuffer[postfixItemToMatch] == token){
						postfixItemToMatch--;
						// if we matched the full itemset containing ei-1, the next items in the same itemset may be appended as postfix
						if(postfixItemToMatch <0 || patternBuffer[postfixItemToMatch] == -1){
							// IMPORTANT
							inAnotherPostfix = true;
							if(lastItemsetIsCut){
								// ok if we are in an itemset that is cut on the left but the last itemset is cut on the right, 
								// we still need to check if the postfix of the right is matching. 
								// For example, if the itemset is (1,6) in the pattern, and we cut after 1, we need to 
								// make sure that 6 also appears in the same itemset. This is what we will do next
								int posToMatch = posItemLast+1;
								inFirstPostfix = false;
								for(int w = j; w <= posItemLast ; w++){
									if(sequence[w] == sequence[posToMatch]){
										posToMatch++;
										if(sequence[posToMatch] == -1 ){
											// YES !!!!!!!!
											inAnotherPostfix = true;
											break;
										}
									}
								}
							}
							// END IMPORTANT
							justMatched = true;
						}
					}
					
					
					if(inFirstPostfix || (inAnotherPostfix && !justMatched)){
						highestSupportUntilNow = updateMapItemSupportPostfix(sequences, highestSupportUntilNow, token);
						if(highestSupportUntilNow == -1){
							return false;
						}
					}
					
					// if the current item is in the last itemset 
					if(j >= posLastItemset){
						highestSupportUntilNow = updateMapItemSupportSuffix(sequences, highestSupportUntilNow, token);
						if(highestSupportUntilNow == -1){
							return false;
						}
					}
					if(!inFirstPostfix && j < posLastItemset){
						highestSupportUntilNow = updateMapItemSupport(sequences, highestSupportUntilNow,
								token);
						if(highestSupportUntilNow == -1){
							return false;
						}
					}
					/////////////////
				}
				// END IF TOKEN
			}
			// END FOR TOKEN
			
			
			// Optimization: if there is not enough sequences left for finding a backward extension given
			// the highest support found until now, we can know that there will not be any backward extension for that pattern.
//			if(highestSupportUntilNow + (sequences.size() - k - 1) < sequences.size()) {
//				continue loopi;
//			}
			
			
			} // END FOR EACH SEQUENCE
		} // END FOR EACH i

		return true;
	}

	private int updateMapItemSupport(List<PseudoSequence> sequences,
			int highestSupportUntilNow, int token) {
		if(alreadySeen.contains(token) == false){
			Integer itemSupport = mapItemSupport.get(token);
			if(itemSupport == null){
				itemSupport = 1;
			}else{
				itemSupport++;
			}
			if(itemSupport ==  sequences.size()){
				return -1;
			}
			// update highest support until now
			if(itemSupport > highestSupportUntilNow){
				highestSupportUntilNow = itemSupport;
			}
//						// update the support in the map
			mapItemSupport.put(token, itemSupport);
			alreadySeen.add(token);
		}
		return highestSupportUntilNow;
	}

	private int updateMapItemSupportPostfix(List<PseudoSequence> sequences,
			int highestSupportUntilNow, int token) {
		if(alreadySeenPostfix.contains(token) == false){
			Integer itemSupport = mapsItemSupportPostfix.get(token);
			if(itemSupport == null){
				itemSupport = 1;
			}else{
				itemSupport++;
			}
			if(itemSupport ==  sequences.size()){
				return -1;
			}
			// update highest support until now
			if(itemSupport > highestSupportUntilNow){
				highestSupportUntilNow = itemSupport;
			}
//						// update the support in the map
			mapsItemSupportPostfix.put(token, itemSupport);
			alreadySeenPostfix.add(token);
		}
		return highestSupportUntilNow;
	}

	private int updateMapItemSupportSuffix(List<PseudoSequence> sequences,
			int highestSupportUntilNow, int token) {
		if(alreadySeenSuffix.contains(token) == false){
			Integer itemSupport = mapsItemSupportSuffix.get(token);
			if(itemSupport == null){
				itemSupport = 1;
			}else{
				itemSupport++;
			}
			if(itemSupport ==  sequences.size()){
				return -1;
			}
			// update highest support until now
			if(itemSupport > highestSupportUntilNow){
				highestSupportUntilNow = itemSupport;
			}
//						// update the support in the map
			mapsItemSupportSuffix.put(token, itemSupport);
			alreadySeenSuffix.add(token);
		}
		return highestSupportUntilNow;
	}

	/**
	 * Check if this pattern should be pruned according to backscan pruning.
	 * This is the general case (pattern of length greater than 1, in a database)
	 * This method is optimized for sequence databases that may contain multiple items per itemset in sequences.
	 * @param lastBufferPosition the last item of the current pattern in the buffer 
	 * @param sequences
	 * @return true if the pattern has no backward extension. Otherwise, false.
	 */
	private boolean checkBackscanPruningMultipleItems(int lastBufferPosition,  List<PseudoSequence> sequences) {
		
//		// DEBUG ===== PRINT THE CURRENT PATTERN ================
//		for(int i=0; i<= lastBufferPosition; i++){
//			if(patternBuffer[i] == -1)
//				System.out.print("|");
//			else
//				System.out.print(patternBuffer[i]);
//		}
//		System.out.println();
//		// END DEBUG ============================================
//		
//		if(lastBufferPosition == 3 && patternBuffer[0] == 6 && patternBuffer[1] == -1
//				&& patternBuffer[2] == 5 && patternBuffer[3] == 7){
//			System.out.println("test");
//		}
		
//		if(lastBufferPosition == 5 && patternBuffer[0] == 6 && patternBuffer[1] == -1
//				&& patternBuffer[2] == 5 && patternBuffer[3] == 7 && patternBuffer[4] == -1
//				&& patternBuffer[5] == 2){
//			System.out.println("test");
//		}
		
//		if(lastBufferPosition == 2 && patternBuffer[0] == 5 && patternBuffer[1] == -1 && patternBuffer[2] == 2){
//			System.out.println("backscan pruning [5][2]");
//		}
		
		// we will check the backscan pruning condition for each item in the current pattern
loopi:	for(int i=0; i <= lastBufferPosition; i++){
			// As an optimization, we will use a variable to remember the highest support until now.
			int highestSupportUntilNow = 0;
			
//			if(lastBufferPosition == 2 && patternBuffer[0] == 5 && patternBuffer[1] == -1 && patternBuffer[2] == 2){
//				System.out.println("POS: " + i);
//			}
//			if(lastBufferPosition == 3 && patternBuffer[0] == 6 && patternBuffer[1] == -1
//					&& patternBuffer[2] == 5 && patternBuffer[3] == 7){
//				System.out.println("POS: " + i);
//			}

			
			// =======  IMPORTANT ==============
			// skip if there is a -1 (an itemset separator)
			if(patternBuffer[i] == -1){
				continue;
			}
			//===================================
			
			//===========================================
			// ====== REALLY IMPORTANT ==================
			// if the position before i is a -1 in the pattern buffer,
			// we backward one more position to get the real position of
			// ei-1 in the buffer.
			int posIminus1 = i-1;
			if(i>0 && patternBuffer[i-1] == -1){
				posIminus1--;
			}
			// ==========================================
			//===========================================
			
			// We use three maps the store the support of possible item that could be used for the backscan pruning
			mapItemSupport.clear();
			mapsItemSupportPostfix.clear();
			mapsItemSupportSuffix.clear();
			
			// for each sequence where the pattern appears
			for(int k =0; k < sequences.size(); k++){
				
//				if(lastBufferPosition == 3 && patternBuffer[0] == 6 && patternBuffer[1] == -1
//						&& patternBuffer[2] == 5 && patternBuffer[3] == 7){
//					System.out.println("SEQ ID" + sequences.get(k).sequenceID);
//					System.out.print("");
//				}
				
//				if(lastBufferPosition == 2 && patternBuffer[0] == 5 && patternBuffer[1] == -1 && patternBuffer[2] == 2){
//
//				}
//				
//				if(lastBufferPosition == 2 && patternBuffer[0] == 5 && patternBuffer[1] == -1 && patternBuffer[2] == 2 && i ==2 ){
//					System.out.println("pos = 2 ");
//				}
				

				PseudoSequence pseudoSequence = sequences.get(k);
				int sid =+ pseudoSequence.getOriginalSequenceID();
				int[] sequence = sequenceDatabase.getSequences().get(sid);

				alreadySeen.clear();
				alreadySeenPostfix.clear();
				alreadySeenSuffix.clear();
			

				//======================================================
				// FIRST we will try to match  e1, e2, ei-1, Ei
				// (all items before the position of the current item ei and also ei)
				// Note that it is possible that ei, ei+1... appears in the same itemset as ei-1.
				// Also note that between ei-1 and ei, there might be a -1 between them in the pattenr buffer. 
				
				// This variable is used to remember the begining of the current itemset in the pattern
				int resetPosition = 0;
				// This variable is used to remember the position of the current item to match in the pattern
				int currentPositionToMatch = 0;
				// This is the position of the item ei-1
				int posItemFirst = 0;
				// This is the position of the first item of the itemset in the sequence containing ei-1
				int posItemsetFirst = 0;
				
				// The position of the item ei if it is found
				int posItemLast = 9999;
				// This is the position of the first item in the itemset in containing ei
				int posLastItemset = 0;
				
				// IMPORTANT =================
				int firstBufferPositioninEIm1Itemset = -1;

				// For each item in the sequence...
				for(int j = 0; ; j++){
					// This is the current item in the sequence.
					int token = sequence[j];
					
					// IF it is the end of an itemset in the sequence
					// it means that we have not matched the whole itemset of the pattern
					if(token == -1){
						// thus we need to restart trying to match the current itemset of the pattern
						currentPositionToMatch = resetPosition; 
						if(currentPositionToMatch <= posIminus1){
							posItemsetFirst = j+1;
							posItemFirst = -999;
							firstBufferPositioninEIm1Itemset = -1;
						}
						posLastItemset = j+1;
						posItemLast = 99999;
					}
					
					if(token > 0){
						// If it is an item that we want to match
						if(token == patternBuffer[currentPositionToMatch]){
							// IMPORTANT: 
							// Remember the first buffer position that match for the itemset that
							// may contain ei-1
							if(firstBufferPositioninEIm1Itemset == -1 && currentPositionToMatch <= posIminus1){
								firstBufferPositioninEIm1Itemset = currentPositionToMatch;
							}
							
							// if it is ei-1 we will save its position
							if(currentPositionToMatch == posIminus1){
								posItemFirst = j+1;   // IMPORTANT :::::: +1
							}
							// if it is ei we will save its position
							else if(currentPositionToMatch == i){
								posItemLast = (j == 0) ? j : j-1;  // IMPORTANT :::::: -1
								
							}
	
							// increase the position to look for
							currentPositionToMatch++;
						
							// if we have matched the full itemset in the pattern and 
							// and that itemset contained ei, then we can stop here
							if(currentPositionToMatch > lastBufferPosition ||
									patternBuffer[currentPositionToMatch] == -1 
									){
								if(currentPositionToMatch >= i ){
									break;
								}
								

								// ======= IMPORTANT ======
								// We move to the next position in the buffer because
								// the current position is a -1
								currentPositionToMatch++;
								//==========================
								
								// Since we have matched a full itemset of the pattern, 
								// next time, we will start to match from the next position
								resetPosition = currentPositionToMatch;
								
							}
						}
					}
				}
				
				System.out.println(" " + posItemFirst + "  " + posItemLast);
				
				// IMPORTANT:
				if(firstBufferPositioninEIm1Itemset == -1){
					firstBufferPositioninEIm1Itemset = 0;
				}
			
			///=================================================================================
			// NEXT WE WILL UPDATE THE SUPPORT OF ITEMS in the  ]posItemFirst, posItemLast[ interval,
		   // in other words in the ]first occurrence of ei-1, last occurrence of ei[ interval
				
			// First, we will update the support the support of all the item in the postfix of the itemset containing
		    // the item "ei - 1".
			// We do this, if the itemset containing ei-1 is a postfix
				
			// FIND The last item in the itemset containing ei
			int endOfLastItemset = posItemLast;
			while(sequence[endOfLastItemset+1] != -1){
				endOfLastItemset++;
			}
			
			// Recursive call
			hasFoundExtension = false;
			recursiveCheckBackScanPruning(sequence, 
					posItemsetFirst, firstBufferPositioninEIm1Itemset, posIminus1, i, 
				    endOfLastItemset, false,
				    sequences.size(), lastBufferPosition);


			// END FOR TOKEN
			
			if(hasFoundExtension == true){
				return false;
			}
			
//			// Optimization: if there is not enough sequences left for finding a backward extension given
//			// the highest support found until now, we can know that there will not be any backward extension for that pattern.
//			if(highestSupportUntilNow + (sequences.size() - k - 1) < sequences.size()) {
//				continue loopi;
//			}
			
			} // END FOR EACH SEQUENCE
		} // END FOR EACH i

		return true;
	}
	
	boolean hasFoundExtension = false;

	/**
	 * This method attemps to match an itemset and update the maps accordingly
	 * @param sequence  a sequence from the database
	 * @param min  the position of the first item to be checked in the itemset containing ei-1
	 * @param max   the position of the first item to be checked in the itemset containing ei
	 * @param highestSupportUntilNow 
	 * @param posIminus1 
	 * @param endOfLastItemset 
	 * @param lastBufferPosition 
	 * @return true if all the following itemsets in the buffer have matched. Otherwise, false.
	 */
	private boolean recursiveCheckBackScanPruning(
			int[] sequence, int posItemsetSequenceToMatch,
			int posItemsetBufferToMatch, 
			int posIminus1, 
			int i, 
			int endOfLastItemset, boolean eim1WasSeen, int supportOfPattern, int lastBufferPosition) {
		
		if(hasFoundExtension){
			return false;
		}
		
		// ============== FIRST, LETS TRY TO IGNORE THE CURRENT ITEMSET OF THE
		// SEQUENCE BY MAKING A RECURSIVE CALL TO THIS METHOD
		// Find the position of the first item in the next itemset
		int posNextItemset = posItemsetSequenceToMatch;
		while(sequence[posNextItemset] != -1){
			posNextItemset++;
		}
		posNextItemset++;
		// if the next itemset is not before the end of what should be considered in that sequence
		if(posNextItemset <= endOfLastItemset){
			// Recursive call
			recursiveCheckBackScanPruning(sequence, posNextItemset,
					posItemsetBufferToMatch, posIminus1, 
					i, endOfLastItemset, eim1WasSeen, supportOfPattern, lastBufferPosition);

			if(hasFoundExtension){
				return false;
			}
		}
		
		//============ THEN TRY TO MATCH THE CURRENT ITEMSET OF THE SEQUENCE
		

		boolean canFullyMatch = false;
		
		// pos of EI and EI-1 in current itemset
		int posEi = -1;
		int posEim1 = -1;

		// For each item in the current itemset 
		int j = posItemsetSequenceToMatch;
		for(; sequence[j] != -1 ; j++){
			// This is the current item in the sequence.
			int token = sequence[j];
			
			// If it is an item that we want to match
			if(token == patternBuffer[posItemsetBufferToMatch]){

				// ===== If we have found the full itemset from the buffer.=====
				int nextBufferPosition = posItemsetBufferToMatch+1;
				if(posItemsetBufferToMatch == lastBufferPosition || patternBuffer[nextBufferPosition] == -1){
					
					//if we have matched all the itemsets of the pattern in the buffer
					if(posItemsetBufferToMatch == lastBufferPosition){
						// we remember it
						canFullyMatch = true;
					}else{
						// If we have matched some itemset(s) but not all itemsets in the buffer
						// MAKE A RECURSIVE CALL TO MATCH THE NEXT ITEMSET
						// We move to the next itemset in the sequence
						int nextJ = j;
						while(sequence[nextJ] != -1){
							nextJ++;
						}
						nextJ++;
						// If the sequence has not been completely read
						if(nextJ < endOfLastItemset){
							// IF ei-1 was seen in the current itemset, we remember it
							if(posEim1 != -1){
								eim1WasSeen = true;
							}
							
							// ======= IMPORTANT ======
							// We move to the next position in the buffer because
							// the current position is a -1
							int nextBufferPos = posItemsetBufferToMatch+1;
							//==========================
							
							// recursive call
							boolean fullyMatched  = recursiveCheckBackScanPruning(sequence, nextJ,
									nextBufferPos, posIminus1, i, endOfLastItemset, eim1WasSeen, supportOfPattern, lastBufferPosition);
							
							if(fullyMatched){
								canFullyMatch = true;
							}
							if(hasFoundExtension){
								return false;
							}
						}else{
							// If the sequence has been completely read but we did not finish matching the full
							// itemset.
							// In that case, we return false because we failed at matching the whole pattern
							return false;
						}
					}
					
					// If this effort to match the whole pattern was successful
					// when considering the recursion
					if(canFullyMatch && eim1WasSeen){
						// Then we will need to update the support of each item in this itemset
						for(int k=posItemsetSequenceToMatch; sequence[k] != -1; k++){
							int item = sequence[k];
							// If item ei  
							// If the item is a postfix
							if(posEim1 != -1 && k > posEim1){
								if(alreadySeenPostfix.contains(token) == false){
									Integer itemSupport = mapsItemSupportPostfix.get(token);
									itemSupport = (itemSupport == null) ? 1: itemSupport+1;
									if(itemSupport ==  supportOfPattern){
										hasFoundExtension = true;
										return false;
									}
			
									// update the support in the map
									mapsItemSupportPostfix.put(token, itemSupport);
									alreadySeenPostfix.add(token);
								}
							}
							
							if(posEi != -1 && k < posEi){
								if(alreadySeenSuffix.contains(token) == false){
									Integer itemSupport = mapsItemSupportSuffix.get(token);
									itemSupport = (itemSupport == null) ? 1: itemSupport+1;
									if(itemSupport ==  supportOfPattern){
										hasFoundExtension = true;
										return false;
									}
			
									// update the support in the map
									mapsItemSupportSuffix.put(token, itemSupport);
									alreadySeenSuffix.add(token);
								}
							}
							
							if(eim1WasSeen && posEim1 == -1 && posEi == -1){
								if(alreadySeen.contains(token) == false){
									Integer itemSupport = mapItemSupport.get(token);
									itemSupport = (itemSupport == null) ? 1: itemSupport+1;
									if(itemSupport ==  supportOfPattern){
										hasFoundExtension = true;
										return false;
									}
			
									// update the support in the map
									mapItemSupport.put(token, itemSupport);
									alreadySeen.add(token);
								}
							}
						}
					}		
				}else{
					// If we did not found the full itemset from the buffer yet.
					
					posItemsetBufferToMatch++;
					// ===== If we have not found the full itemset from the buffer. =====
					if(posItemsetBufferToMatch == posIminus1){
						posEim1 = j;
					}
					
					if(posItemsetBufferToMatch == i){
						posEi = j;
					}
				}
			}
		}
		
		return canFullyMatch;
	}

	/**
	 * Method to find all frequent items in a projected sequence database
	 * This method is optimized for sequence databases that may contain no more than an item per itemset in sequences
	 * @param sequences  the set of sequences
	 * @param patternBuffer  the current sequential pattern that we want to try to grow
	 * @param lastBufferPosition the last position used in the buffer for storing the current prefix
	 * @return A list of pairs, where a pair is an item with (1) a boolean indicating if it
	 *         is in an itemset that is "cut" and (2) the sequence IDs where it occurs.
	 */
	protected Map<Integer,List<PseudoSequence>>  findAllFrequentPairsSingleItems(List<PseudoSequence> sequences, int lastBufferPosition){
		// We use a Map the store the pairs.
		Map<Integer,List<PseudoSequence>>  mapItemsPseudoSequences = new HashMap<Integer,List<PseudoSequence>>();

		// for each sequence
		for(PseudoSequence pseudoSequence : sequences){
			
			// for each sequence
			int sequenceID = pseudoSequence.getOriginalSequenceID();
			int[] sequence = sequenceDatabase.getSequences().get(sequenceID);
			
			// for each token in this sequence 
			for(int i = pseudoSequence.indexFirstItem;  sequence[i] != -2 ; i++){
				int token = sequence[i];
				
				// if it is an item
				if(token > 0){
					// get the pair object stored in the map if there is one already
					List<PseudoSequence> listSequences = mapItemsPseudoSequences.get(token);
					// if there is no pair object yet
					if(listSequences == null){
						listSequences = new ArrayList<PseudoSequence>();
						// store the pair object that we created
						mapItemsPseudoSequences.put(token, listSequences);
					}
					
					// Check if that sequence as already been added to the projected database of this item
					boolean ok = true;
					if(listSequences.size() > 0){
						ok = listSequences.get(listSequences.size() - 1).sequenceID != sequenceID;
					}
					// if not we add it
					if(ok){
						listSequences.add(new PseudoSequence(sequenceID, i+1));
					}
				}
			}
		}
		MemoryLogger.getInstance().checkMemory();  // check the memory for statistics.
		// return the map of pairs
		return mapItemsPseudoSequences;
	}
	
	
	/**
	 * This class contains two maps, which are used for counting the
	 * frequencies of items, whether in a postfix itemset or a normal itemset.
	 */
	public class MapFrequentPairs{
	    public final Map<Pair, Pair>  mapPairs = new HashMap<Pair, Pair>();
	    public final Map<Pair, Pair>  mapPairsInPostfix = new HashMap<Pair, Pair>();
	};

	/**
	 * Method to find all frequent items in a projected sequence database
	 * @param sequences  the set of sequences
	 * @param lastBufferPosition the last position used in the buffer for storing the current prefix
	 * @return A list of pairs, where a pair is an item with (1) a boolean indicating if it
	 *         is in an itemset that is "cut" and (2) the sequence IDs where it occurs.
	 */
	protected MapFrequentPairs findAllFrequentPairs(List<PseudoSequence> sequences, int lastBufferPosition){
		// We use an object containing two maps the store the pairs.
		MapFrequentPairs mapsPairs = new MapFrequentPairs();
		
		// find the position of the first item of the last itemset of the current sequential pattern that is grown
		int firstPositionOfLastItemsetInBuffer = lastBufferPosition;
		while(lastBufferPosition >0){
			firstPositionOfLastItemsetInBuffer--;
			if(firstPositionOfLastItemsetInBuffer < 0 || patternBuffer[firstPositionOfLastItemsetInBuffer] == -1){
				firstPositionOfLastItemsetInBuffer++;
				break;
			}
		};
		
		// use a variable to try to match the last itemset of the pattern in the buffer
		int positionToBeMatched = firstPositionOfLastItemsetInBuffer;
		
		// for each sequence
		for(PseudoSequence pseudoSequence : sequences){
			
			// for each sequence
			int sequenceID = pseudoSequence.getOriginalSequenceID();
			int[] sequence = sequenceDatabase.getSequences().get(sequenceID);

			
			// check if the first itemset of that sequence is a postfix
			// It is a postfix (the itemset is cut) if the previous item is a -1 indicating
			// the end of an itemset
			int previousItem = sequence[pseudoSequence.indexFirstItem - 1];
			boolean currentItemsetIsPostfix = (previousItem != - 1);
			boolean isFirstItemset = true;
			
			// for each token in this sequence (item, separator between itemsets (-1) or end of sequence (-2)
			for(int i = pseudoSequence.indexFirstItem;  sequence[i] != -2 ; i++){
				int token = sequence[i];
				
				// if it is an item
				if(token > 0){
					
					// create the pair corresponding to this item
					Pair pair = new Pair(token);   
					// get the pair object store in the map if there is one already
					Pair oldPair;
					if(currentItemsetIsPostfix){
						oldPair = mapsPairs.mapPairsInPostfix.get(pair);
					}else{
						oldPair = mapsPairs.mapPairs.get(pair);
					}
					// if there is no pair object yet
					if(oldPair == null){
						// store the pair object that we created
						if(currentItemsetIsPostfix){
							mapsPairs.mapPairsInPostfix.put(pair, pair);
						}else{
							mapsPairs.mapPairs.put(pair, pair);
						}
					}else{
						// otherwise use the old one
						pair = oldPair;
					}
					
					// Check if that sequence as already been added to the projected database of this item
					boolean ok = true;
					if(pair.getPseudoSequences().size() > 0){
						ok = pair.getPseudoSequences().get(pair.getPseudoSequences().size() - 1).sequenceID != sequenceID;
					}
					// if not we add it
					if(ok){
						pair.getPseudoSequences().add(new PseudoSequence(sequenceID, i+1));
					}
					
					///////// ====== IMPORTANT =========
					// if the current itemset is a postfix and it is not the first itemset
					// we must also consider that it may not be a postfix for extending the current prefix
					if(currentItemsetIsPostfix && isFirstItemset == false){
						// create the pair corresponding to this item
						 pair = new Pair(token);     // FALSE !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
						// get the pair object store in the map if there is one already
						 oldPair = mapsPairs.mapPairs.get(pair);
						// if there is no pair object yet
						if(oldPair == null){
							// store the pair object that we created
							mapsPairs.mapPairs.put(pair, pair);
						}else{
							// otherwise use the old one
							pair = oldPair;
						}
						
						// Check if that sequence as already been added to the projected database of this item
						ok = true;
						if(pair.getPseudoSequences().size() > 0){
							ok = pair.getPseudoSequences().get(pair.getPseudoSequences().size() - 1).sequenceID != sequenceID;
						}
						// if not we add it
						if(ok){
							pair.getPseudoSequences().add(new PseudoSequence(sequenceID, i+1));
						}
						
					}
					//////////////////////////////////////////////////////////

					//  try to match this item with the last itemset in the prefix
					if(currentItemsetIsPostfix == false && patternBuffer[positionToBeMatched] == token){
						positionToBeMatched++;
						if(positionToBeMatched > lastBufferPosition){
							currentItemsetIsPostfix = true;
						}
					}

				}else if(token == -1){
					isFirstItemset = false;
					currentItemsetIsPostfix = false;
					positionToBeMatched = firstPositionOfLastItemsetInBuffer;
				}
			}
		}
		MemoryLogger.getInstance().checkMemory();  // check the memory for statistics.
		// return the map of pairs
		return mapsPairs;
	}

	/**
	 * Print statistics about the algorithm execution to System.out.
	 */
	public void printStatistics() {
		StringBuilder r = new StringBuilder(200);
		r.append("============  BIDE+ - SPMF 0.99k - 2016 - STATISTICS =====\n Total time ~ ");
		r.append(endTime - startTime);
		r.append(" ms\n");
		r.append(" Frequent sequences count : " + patternCount);
		r.append('\n');
		r.append(" Max memory (mb) : ");
		r.append(MemoryLogger.getInstance().getMaxMemory());
		r.append('\n');
		r.append(" minsup = " + minsuppAbsolute + " sequences.");
		r.append('\n');
		r.append(" Pattern count : ");
		r.append(patternCount);
		r.append('\n');
		r.append("==========================================================\n");
		// if the result was save into memory, print it
		System.out.println(r.toString());
	}
	
	/**
	 * Get the maximum length of patterns to be found (in terms of item count)
	 * @return the maximumPatternLength
	 */
	public int getMaximumPatternLength() {
		return maximumPatternLength;
	}

	/**
	 * Set the maximum length of patterns to be found (in terms of item count)
	 * @param maximumPatternLength the maximumPatternLength to set
	 */
	public void setMaximumPatternLength(int maximumPatternLength) {
		this.maximumPatternLength = maximumPatternLength;
	}
	
	/**
	 * Set that the sequence identifiers should be shown (true) or not (false) for each
	 * pattern found
	 * @param showSequenceIdentifiers true or false
	 */
	public void setShowSequenceIdentifiers(boolean showSequenceIdentifiers) {
		this.showSequenceIdentifiers = showSequenceIdentifiers;
	}
}
