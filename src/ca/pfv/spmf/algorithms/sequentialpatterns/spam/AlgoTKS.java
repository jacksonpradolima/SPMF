package ca.pfv.spmf.algorithms.sequentialpatterns.spam;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;

import ca.pfv.spmf.patterns.itemset_list_integers_without_support.Itemset;
import ca.pfv.spmf.tools.MemoryLogger;

/*** 
 * This is the original implementation of the TKS algorithm
 * for top-k sequential patterns. 
 * <br/><br/>
 * 
 * Copyright (c) 2013 Philippe Fournier-Viger, Antonio Gomariz
 *  <br/><br/>
 *  
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 * 
 *  <br/><br/>
 * SPMF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <br/><br/>
 * 
 * SPMF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br/><br/>
 * 
 * You should have received a copy of the GNU General Public License
 * along with SPMF. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @see Bitmap
*  @see Prefix
*  @see PatternTKS
*  @see Candidate
*  @author Philippe Fournier-Viger  & Antonio Gomariz
 */

public class AlgoTKS{
		
	// for statistics
	/** start time of last algorithm execution */
	private long startTime;  
	/** end time of preprocessing of last execution */
	private long startMiningTime; 
	/** end time of last algorithm execution */
	private long endTime;   
	
	/**  minsup, which is dynamically raised during execution */
	private int minsup = 0; 
	
	/** for stats, the minsup just after preprocessing */
	private int minsupAfterPreProcessing = 0;
	
	/**  the k parameter given by the user */
	private int k = 0;  
	
	/** Vertical database (bitsets) */
	Map<Integer, Bitmap> verticalDB = new HashMap<Integer, Bitmap>();
	
	/** List indicating the number of bits per sequence */
	List<Integer> sequencesSize = null;
	
	/**  the last bit position that is used in bitmaps */
	int lastBitIndex = 0;  
	
	/**  the top k patterns found until now  */
	PriorityQueue<PatternTKS> kPatterns;  
	
	/** the candidates for expansion */
	PriorityQueue<Candidate> candidates;  
	
	/** the max number of candidates at the same time during the last execution (for stats) */
	int maxCandidateCount = 0;
	
	/** the number of candidates considered (for stats) */
	int candidateExplored = 0;
	
	/** the set of items that have been discarded because their support has become
	* lower than minsup and should not be considered anymore */
	Set<Integer> discardedItems;
	
	// THE STRATEGIES FOR OPTIMIZING THE ALGORITHMS
	// EACH ONE CAN BE ACTIVATED OR DESACTIVATED.
	// #0 : choose most promising branch 
	// #1 : raise minsup during preprocessing for single items and discard infrequent items
	// before DFS exploration
	// #2 keep note of discarded single items and ignore them during searching
	final boolean useDiscardedItemsPruningStrategy = true; 
	// #3 very small improvement (rarely match the condition so that it can be applied)
	final boolean usePruneBranchesInsideDFSPruning = true; 
	
	// #4 rebuild tree  when it is too large
	final boolean rebuildCandidateTreeWhenTooLarge = false;  
	int addedCandidatesSinceLastRebuilt = 0;
	final int MIN_CANDIDATES_COUNT_BEFORE_REBUILD = 1500;
	final int MIN_ADDED_CANDIDATE_COUNT_SINCE_LAST_REBUILD_BEFORE_REBUILD = 400;
	
	// #5  cooccurrence  map  (useful for sparse datasets such as BMS...)
	final boolean useCooccurrenceInformation = true;
	
	// the cooccurrence map
    // Map: key: item   value:  another item that followed the first item + support
    Map<Integer, Map<Integer, Integer>> coocMapAfter = null;
    Map<Integer, Map<Integer, Integer>> coocMapEquals = null;
    
	/** maximum pattern length in terms of item count */
	private int minimumPatternLength = 0;

	/** minimum pattern length in terms of item count */
	private int maximumPatternLength = 1000;
	
	/** items that need to appear in patterns found by TKS 
	* (or any items if the array is empty) */
	int[] mustAppearItems;
	
	/** the max gap between two itemsets of a pattern.
	* It is an optional parameter that the user can set. */
	private int maxGap = Integer.MAX_VALUE;

	
	/** Optional parameter to decide whether sequence identifiers should be shown in the output for each pattern found */
	private boolean outputSequenceIdentifiers; 

	/**
	 * Default constructor
	 */
	public AlgoTKS(){
	}

	/**
	 * Method to run the algorithm
	 * @param input  path to an input file
	 * @param outputFilePath path for writing the output file
	 * @param k the number of sequential pattern to be found
	 */
	public PriorityQueue<PatternTKS> runAlgorithm(String input, String outputFilePath, int k) throws IOException {
 
		// to log the memory used
		MemoryLogger.getInstance().reset(); 
		
		// RUN THE ALGORITHM
		tks(input, k); 
		// record end time
		endTime = System.currentTimeMillis(); 
		
		// return the top-patterns
		return kPatterns;
	}
	
	/**
	 * This is the main method for the TKS algorithm
	 * @param an input file
	 * @param k the number of patterns to be found
	 * @throws IOException 
	 */
	private PriorityQueue<PatternTKS> tks(String input, int k) throws IOException{
		this.k = k;

		// set minsup = 1 (will be increased by the algorithm progressively)
		minsup =1;
		
		candidateExplored = 0;
		
		// the sets that will contain the top-k patterns and the candidates
		kPatterns = new PriorityQueue<PatternTKS>();
		candidates = new PriorityQueue<Candidate>();
		discardedItems = new HashSet<Integer>();
		
		// the structure to store the vertical database
		// key: an item    value : bitmap
		verticalDB = new HashMap<Integer, Bitmap>();
		
		// structure to store the horizontal database
		List<int[]> inMemoryDB = new ArrayList<int[]>();
		
		// STEP 0: SCAN THE DATABASE TO STORE THE FIRST BIT POSITION OF EACH SEQUENCE 
		// AND CALCULATE THE TOTAL NUMBER OF BIT FOR EACH BITMAP
		sequencesSize = new ArrayList<Integer>();
		
		lastBitIndex =0; // variable to record the last bit position that we will use in bitmaps
		try {
			// read the file
			FileInputStream fin = new FileInputStream(new File(input));
			BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
			String thisLine;
			int bitIndex =0;
			// for each line (sequence) in the file until the end
			
			while ((thisLine = reader.readLine()) != null) {
				// if the line is  a comment, is  empty or is a
				// kind of metadata
				if (thisLine.isEmpty() == true ||  thisLine.startsWith("#") || thisLine.charAt(0) == '%' 	|| thisLine.charAt(0) == '@') {
					continue;
				}
				
				String tokens[] = thisLine.split(" ");
				
				int [] transactionArray = new int[tokens.length];
				
				boolean containsAMustAppearItem = false;
				
				// record the length of the current sequence (for optimizations)
				sequencesSize.add(bitIndex);
				// split the sequence according to spaces into tokens
				for(int i=0; i< tokens.length; i++){
					int item = Integer.parseInt(tokens[i]);
					transactionArray[i] = item;
					// if it is not an itemset separator
					if(item == -1){ // indicate the end of an itemset
						// increase the number of bits that we will need for each bitmap
						bitIndex++;
					}
					// check if this item must appear in patterns (optional)
					if(itemMustAppearInPatterns(item)) {
						containsAMustAppearItem = true;
					}
				}
				
				// if this transaction contains at least an item that must appear
				// in the pattern (this feature is optional), we keep the sequence
				// otherwise we don't add it
				if(containsAMustAppearItem) {
					inMemoryDB.add(transactionArray);
				}
			}
			// record the last bit position for the bitmaps
			lastBitIndex = bitIndex -1;
			reader.close(); // close the input file
		} catch (Exception e) {
			e.printStackTrace();
		}

		// record start time
		startTime = System.currentTimeMillis();   // FOR COMPARISON WITH PPWSR WE HAVE TO PUT HERE
		
		// STEP1: SCAN THE DATABASE TO CREATE THE BITMAP VERTICAL DATABASE REPRESENTATION
		int sid =0; // to know which sequence we are scanning
		int tid =0;  // to know which itemset we are scanning
		
		// for each line (sequence) from the input file
		for(int[] transaction : inMemoryDB){
			// split the sequence according to spaces into tokens
			for(Integer item : transaction){
				if(item == -1){ // indicate the end of an itemset
					tid++;
				}else if(item == -2){ // indicate the end of a sequence
//						determineSection(bitindex - previousBitIndex);  // register the sequence length for the bitmap
					sid++;
					tid =0;
				}else{  // indicate an item
					// Get the bitmap for this item. If none, create a new one.
					Bitmap bitmapItem = verticalDB.get(item);
					if(bitmapItem == null){
						bitmapItem = new Bitmap(lastBitIndex);
						verticalDB.put(item, bitmapItem);
					}
					// Register the bit in the bitmap for this item
					bitmapItem.registerBit(sid, tid, sequencesSize);
				}
			}
		}
		
//		// STEP2: REMOVE INFREQUENT ITEMS FROM THE DATABASE BECAUSE THEY WILL NOT APPEAR IN ANY FREQUENT SEQUENTIAL PATTERNS
		List<Integer> frequentItems = new LinkedList<Integer>();
		Iterator<Entry<Integer, Bitmap>> iter = verticalDB.entrySet().iterator();
		// we iterate over items from the vertical database that we have in memory
		while (iter.hasNext()) {
			//  we get the bitmap for this item
			Map.Entry<Integer, Bitmap> entry = (Map.Entry<Integer, Bitmap>) iter.next();
			Integer item = entry.getKey();
			Integer support = entry.getValue().getSupport();

//			// if the cardinality of this bitmap is lower than minsup
			if(support < minsup){
				// we remove this item from the database.
				iter.remove(); 
			}else{
				// otherwise, we save this item as a frequent
				// sequential pattern of size 1
				Prefix prefix = new Prefix();
				prefix.addItemset(new Itemset(item));
				PatternTKS pattern = new PatternTKS(prefix, support);
				if(outputSequenceIdentifiers) {
					pattern.bitmap = entry.getValue();
				}
				
				if(1 >= minimumPatternLength  && 1 <= maximumPatternLength) {
					save(pattern);
				}
			}
		}

		if(maximumPatternLength > 1) {
			// STEP4:CREATE COOCURRENCE STRUCTURE
			if(useCooccurrenceInformation){
		        coocMapEquals = new HashMap<Integer, Map<Integer, Integer>>(frequentItems.size());
		        coocMapAfter = new HashMap<Integer, Map<Integer, Integer>>(frequentItems.size());
				
		        for (int[] transaction : inMemoryDB) {
	
		            Set<Integer> alreadyProcessed = new HashSet<Integer>();
		            Map<Integer, Set<Integer>> equalProcessed = new HashMap<>();
		            loopI:
		            for (int i = 0; i < transaction.length; i++) {
		                Integer itemI = transaction[i];
	
		                Set<Integer> equalSet = equalProcessed.get(itemI);
		                if (equalSet == null) {
		                    equalSet = new HashSet<Integer>();
		                    equalProcessed.put(itemI, equalSet);
		                }
	
		                if (itemI < 0) {
		                    continue;
		                }
	//					System.out.println(itemsetCount);
	
	//	                // update lastItemMap
	//	                if (useLastPositionPruning) {
	//	                    Short last = lastItemPositionMap.get(itemI);
	//	                    if (last == null || last < itemsetCount) {
	//	                        lastItemPositionMap.put(itemI, itemsetCount);
	//	                    }
	//	                }
	
		                Bitmap bitmapOfItem = verticalDB.get(itemI);
		                if (bitmapOfItem == null || bitmapOfItem.getSupport() < minsup) {
		                    continue;
		                }
	
		                Set<Integer> alreadyProcessedB = new HashSet<Integer>(); // NEW
	
		                boolean sameItemset = true;
		                for (int j = i + 1; j < transaction.length; j++) {
		                    Integer itemJ = transaction[j];
	
		                    if (itemJ < 0) {
		                        sameItemset = false;
		                        continue;
		                    }
	
		                    Bitmap bitmapOfitemJ = verticalDB.get(itemJ);
		                    if (bitmapOfitemJ == null || bitmapOfitemJ.getSupport() < minsup) {
		                        continue;
		                    }
	//										if (itemI != itemJ){
		                    Map<Integer, Integer> map = null;
		                    if (sameItemset) {
		                        if (!equalSet.contains(itemJ)) {
		                            map = coocMapEquals.get(itemI);
		                            if (map == null) {
		                                map = new HashMap<Integer, Integer>();
		                                coocMapEquals.put(itemI, map);
		                            }
		                            Integer support = map.get(itemJ);
		                            if (support == null) {
		                                map.put(itemJ, 1);
		                            } else {
		                                map.put(itemJ, ++support);
		                            }
		                            equalSet.add(itemJ);
		                        }
		                    } else if (!alreadyProcessedB.contains(itemJ)) {
		                        if (alreadyProcessed.contains(itemI)) {
		                            continue loopI;
		                        }
		                        map = coocMapAfter.get(itemI);
		                        if (map == null) {
		                            map = new HashMap<Integer, Integer>();
		                            coocMapAfter.put(itemI, map);
		                        }
		                        Integer support = map.get(itemJ);
		                        if (support == null) {
		                            map.put(itemJ, 1);
		                        } else {
		                            map.put(itemJ, ++support);
		                        }
		                        alreadyProcessedB.add(itemJ); // NEW
		                    }
		                }
		                alreadyProcessed.add(itemI);
		            }
		        }
			}
			
			// PRUNE INFREQUENT ITEMS FROM VERTICAL DB AND REMOVE ITEM  FROM LIST OF FREQUENT ITEMS
			// PLUS GENERATE CANDIDATE
			if(useCooccurrenceInformation){
				Iterator<Entry<Integer, Bitmap>> iter2 = verticalDB.entrySet().iterator();
				while (iter2.hasNext()) {
					Entry<Integer, Bitmap> entry = (Entry<Integer, Bitmap>) iter2.next();
					Bitmap bitmap = entry.getValue();
					if(bitmap.getSupport() >= minsup){
						candidateExplored++;
						Integer item = entry.getKey();
						// We create a prefix with that item
						Prefix prefix = new Prefix();
						prefix.addItemset(new Itemset(item));
						// We register this prefix as a path for future exploration
						if(coocMapAfter.get(item) != null){
							Collection<Integer> afterItems = coocMapAfter.get(item).keySet();
							registerAsCandidate(new Candidate(prefix, bitmap, afterItems,  afterItems, item, 1));
						}
					}
				}
			}else{
				Iterator<Integer> iter2 = frequentItems.iterator();
				while (iter2.hasNext()) {
					Integer item = (Integer) iter2.next();
					if(verticalDB.get(item).getSupport() < minsup){
					// we remove this item from the database.
						iter2.remove(); 
						verticalDB.remove(item);
					}else{
						candidateExplored++;
						
						// We create a prefix with that item
						Prefix prefix = new Prefix();
						prefix.addItemset(new Itemset(item));
						// We register this prefix as a path for future exploration
						registerAsCandidate(new Candidate(prefix, verticalDB.get(item), frequentItems, frequentItems, item, 1));
					}
				}
			}
			minsupAfterPreProcessing = minsup; // for stats
	
			// STEP3: WE PERFORM THE RECURSIVE DEPTH FIRST SEARCH
			// to find longer sequential patterns recursively
	
			startMiningTime = System.currentTimeMillis();
			
			 while(!candidates.isEmpty()){
				 // we take the rule with the highest support first
				Candidate cand = candidates.poll();
				
				// if there is no more candidates with enough support, then we stop
				if(cand.bitmap.getSupport() < minsup){
					break;
				}
	
				candidateExplored++;
				
				// we try to expand the candidate pattern
				dfsPruning(cand.prefix, cand.bitmap, cand.sn, cand.in, cand.hasToBeGreaterThanForIStep, cand.candidateLength);
				
				if(rebuildCandidateTreeWhenTooLarge && 
						candidates.size() > MIN_CANDIDATES_COUNT_BEFORE_REBUILD 
						&& addedCandidatesSinceLastRebuilt > MIN_ADDED_CANDIDATE_COUNT_SINCE_LAST_REBUILD_BEFORE_REBUILD ){	
					PriorityQueue<Candidate> temp = new PriorityQueue<Candidate>();
					for(Candidate candidate : candidates){
						if(candidate.bitmap.getSupport() >= minsup){
							temp.add(candidate);
						}
					}
					candidates = temp;
	//				System.out.println("REBUILD =======================");
				}
	//			System.out.println(candidates.size());
			}
		}

		// check the memory usage
		MemoryLogger.getInstance().checkMemory();
		// return the top-rules
		return kPatterns;
	}
	

	/**
	 * Save a rule in the current top-k set
	 * @param integer the rule
	 * @param bitmap the support of the rule
	 */
	private void save(PatternTKS pattern) {
		// First, we check if the pattern contains the desired items (optional)
		// We only do that if the user has specified some items that must appear in
		// patterns.
		if(mustAppearItems != null) {
			Set<Integer> itemsFound = new HashSet<Integer>();
			// for each item in the pattern
loop:		for(Itemset itemset : pattern.prefix.getItemsets()){
				for(Integer item : itemset.getItems()) {
					// if the user required that this item must appear in all patterns 
					if(itemMustAppearInPatterns(item)) {
						// we note it
						itemsFound.add(item);
						if(itemsFound.size() == mustAppearItems.length) {
							break loop;
						}
					}
				}
			}
			// if the pattern does not contains all required items, then return
			if(itemsFound.size() != mustAppearItems.length) {
				return;
			}
		}
		
		// We add the rule to the set of top-k rules
		kPatterns.add(pattern);
		// if the size becomes larger than k
		if (kPatterns.size() > k) {
			// if the support of the rule that we haved added is higher than
			// the minimum support, we will need to take out at least one rule
			if (pattern.support > this.minsup) {
				// we recursively remove the rule having the lowest support, until only k rules are left
				do {
					PatternTKS pat = kPatterns.poll();
					
					// STRATEGY TO RECORD DISCARDED SINGLE ITEMS SO THAT WE DON'T CONSIDER THEM FOR EXPANSION
					if(useDiscardedItemsPruningStrategy && pat.prefix.size() ==1 && pat.prefix.get(0).size() ==1){
						discardedItems.add(pat.prefix.get(0).get(0));
					}
				} while (kPatterns.size() > k);
			}
			// we raise the minimum support to the lowest support in the 
			// set of top-k rules
			this.minsup = kPatterns.peek().support;
		}
	}
	
	/**
	 * Add a candidate to the set of candidate
	 * @param candidate the candidate
	 */
	private void registerAsCandidate(Candidate candidate) {
		
		candidates.add(candidate); // add the pattern
		
		addedCandidatesSinceLastRebuilt++;
		
		// remember the maximum number of candidates reacher for stats
		if(candidates.size() >= maxCandidateCount){
			maxCandidateCount = candidates.size();
		}
	}
	
	/**
	 * This is the dfsPruning method as described in the SPAM paper.
	 * @param prefix the current prefix
	 * @param prefixBitmap  the bitmap corresponding to the current prefix
	 * @param sn  a list of items to be considered for i-steps
	 * @param in  a list of items to be considered for s-steps
	 * @param hasToBeGreaterThanForIStep
	 * @param prefixLength the number of items in the prefix (the prefix length)
	 * @param m size of the current prefix in terms of items
	 * @throws IOException  if there is an error writing a pattern to the output file
	 */
	private void dfsPruning(Prefix prefix, Bitmap prefixBitmap, Collection<Integer> sn, Collection<Integer> in, int hasToBeGreaterThanForIStep, int prefixLength) throws IOException {
//		System.out.println(prefix.toString());
//		Itemset lastItemsetOfPrefix = prefix.get(prefix.getItemsets().size()-1));
//		Integer lastAppendedItem = lastItemsetOfPrefix.get(lastItemsetOfPrefix.size()-1);
		
		int newCandidatesLength = prefixLength+1;

		//  ======  S-STEPS ======
		// Temporary variables (as described in the paper)
		List<Integer> sTemp = new ArrayList<Integer>();
		List<Bitmap> sTempBitmaps = new ArrayList<Bitmap>();
		
        // for CMAP pruning, we will only check against the last appended item
//        Map<Integer, Integer> mapSupportItemsAfter = coocMapAfter.get(lastAppendedItem);

        
		// for each item in sn
loopi:	for(Integer i : sn){
			// IMPORTANT DON'T CONSIDER DISCARDED ITEMS...   *************
			if(useDiscardedItemsPruningStrategy && discardedItems.contains(i)){
				continue;
			}
			
			if(useCooccurrenceInformation){
				for(Itemset itemset : prefix.getItemsets()){
					for(Integer itemX : itemset.getItems()){
						Map<Integer, Integer> mapSupportItemsAfter = coocMapAfter.get(itemX);
						if(mapSupportItemsAfter == null){
//							System.out.println("PRUNE");
							continue loopi;
						}
						Integer support = mapSupportItemsAfter.get(i);
						if(support == null || support < minsup){
//							System.out.println("PRUNE");
							continue loopi;
						}
					}
				}
			}
			
			// perform the S-STEP with that item to get a new bitmap
			Bitmap newBitmap = prefixBitmap.createNewBitmapSStep(verticalDB.get(i), sequencesSize,  lastBitIndex, maxGap);
			// if the support is higher than minsup
			if(newBitmap.getSupportWithoutGapTotal() >= minsup){
				// record that item and pattern in temporary variables
				sTemp.add(i); 
				sTempBitmaps.add(newBitmap);
			}
		}
		
		// for each pattern recorded for the s-step
		for(int k=0; k < sTemp.size(); k++){

			// create the new bitmap
			Bitmap newBitmap = sTempBitmaps.get(k);
			
			// IMPORTANT ---- PRUNING *******************************
			if(usePruneBranchesInsideDFSPruning && newBitmap.getSupport() < minsup){
				continue;
			}
			
			int item = sTemp.get(k);
			// create the new prefix
			Prefix prefixSStep = prefix.cloneSequence();
			prefixSStep.addItemset(new Itemset(item));

			 if(newBitmap.getSupport() >= minsup) {
				// save the pattern to the file
				if(newCandidatesLength >= minimumPatternLength 
						&& newCandidatesLength <= maximumPatternLength) {
					PatternTKS pattern = new PatternTKS(prefixSStep, newBitmap.getSupport());
					if(outputSequenceIdentifiers) {
						pattern.bitmap = newBitmap;
					}
					save(pattern);
				}
				// recursively try to extend that pattern
	
				// IMPORTANT  *************
				if(newCandidatesLength+1 <= maximumPatternLength) {
					registerAsCandidate(new Candidate(prefixSStep, newBitmap, sTemp, sTemp, item, newCandidatesLength));
				}
			}
		}
		
		// ========  I STEPS =======
		// Temporary variables
		List<Integer> iTemp = new ArrayList<Integer>();
		List<Bitmap> iTempBitmaps = new ArrayList<Bitmap>();
		
		// for each item in in
loop2:	for(Integer i : in){
	
			if(i <= hasToBeGreaterThanForIStep){
				continue;
			}
			
			// IMPORTANT DON'T CONSIDER DISCARDED ITEMS...   *************
			if(useDiscardedItemsPruningStrategy &&  discardedItems.contains(i)){
				continue;
			}
			
			if(useCooccurrenceInformation){
				for(Itemset itemset : prefix.getItemsets()){
					for(Integer itemX : itemset.getItems()){
						Map<Integer, Integer> mapSupportItemsAfter = coocMapEquals.get(itemX);
						if(mapSupportItemsAfter == null){
							continue loop2;
						}
						Integer support = mapSupportItemsAfter.get(i);
						if(support == null || support < minsup){
							continue loop2;
						}
					}
				}
			}
			
			// the item has to be greater than the largest item
			// already in the last itemset of prefix.
			
			// Perform an i-step with this item and the current prefix.
			// This creates a new bitmap
			Bitmap newBitmap = prefixBitmap.createNewBitmapIStep(verticalDB.get(i), sequencesSize,  lastBitIndex);
			// If the support is no less than minsup
			if(newBitmap.getSupport() >= minsup){
				// record that item and pattern in temporary variables
				iTemp.add(i);
				iTempBitmaps.add(newBitmap);
			}
		}
		// for each pattern recorded for the i-step
		for(int k=0; k < iTemp.size(); k++){

			// create the new bitmap
			Bitmap newBitmap = iTempBitmaps.get(k);
			
			// IMPORTANT ---- PRUNING *******************************
			if(usePruneBranchesInsideDFSPruning && newBitmap.getSupport() < minsup){
				continue;
			}
			
			int item = iTemp.get(k);
			// create the new prefix
			Prefix prefixIStep = prefix.cloneSequence();
			prefixIStep.getItemsets().get(prefixIStep.size()-1).addItem(item);
			
			// save the pattern
			if(newCandidatesLength >= minimumPatternLength && newCandidatesLength <= maximumPatternLength) {
				save(new PatternTKS(prefixIStep, newBitmap.getSupport()));
			}
			// recursively try to extend that pattern
			if(newCandidatesLength+1 <= maximumPatternLength) {
				registerAsCandidate(new Candidate(prefixIStep, newBitmap, sTemp, iTemp, item, newCandidatesLength));
			}
		}	
		// check the memory usage
		MemoryLogger.getInstance().checkMemory();
	}


	/**
	 * Print the statistics of the algorithm execution to System.out.
	 */
	public void printStatistics() {
		StringBuilder r = new StringBuilder(200);
		r.append("=============  Algorithm TKS v0.97 - STATISTICS =============\n");
		r.append("Minsup after preprocessing : " + minsupAfterPreProcessing + "\n");
		
		r.append("Max candidates: " + maxCandidateCount);
		r.append(" Candidates explored  : " +candidateExplored + "\n");
		r.append("Pattern found count : " + kPatterns.size());
		r.append('\n');
		r.append("Time preprocessing: " + (startMiningTime - startTime) + " ms \n");
		r.append("Total time: " + (endTime - startTime) + " ms \n");
		r.append("Max memory (mb) : " );
		r.append(MemoryLogger.getInstance().getMaxMemory());
		r.append('\n');
		r.append("Final minsup value: " + minsup);
		r.append('\n');
		r.append("Intersection count " + Bitmap.INTERSECTION_COUNT + " \n");
		r.append("===================================================\n");
		System.out.println(r.toString());
	}

	/**
	 * Write the result to an output file
	 * @param path the output file path
	 * @throws IOException exception if an error occur when writing the file.
	 */
	public void writeResultTofile(String path) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(path)); 
		Iterator<PatternTKS> iter = kPatterns.iterator();
		while (iter.hasNext()) {
			PatternTKS pattern = (PatternTKS) iter.next();
			StringBuilder buffer = new StringBuilder();
			buffer.append(pattern.prefix.toString());
			// write separator
			buffer.append(" #SUP: ");
			// write support
			buffer.append(pattern.support);
	        // if the user wants the sequence IDs, we will show them
	        if(outputSequenceIdentifiers) {
	        	buffer.append(" #SID: ");
	        	buffer.append(pattern.bitmap.getSIDs(sequencesSize));
	        }
			writer.write(buffer.toString());
			writer.newLine();
			
//			System.out.println(buffer);   // DEBUG
		}
		
		writer.close();
	}
	
	/**
	 * Set the maximum length of patterns to be found (in terms of itemset count)
	 * @param maximumPatternLength the maximumPatternLength to set
	 */
	public void setMaximumPatternLength(int maximumPatternLength) {
		this.maximumPatternLength = maximumPatternLength;
	}
	
	/**
	 * Set the minimum length of patterns to be found (in terms of itemset count)
	 * @param minimumPatternLength the minimum pattern length to set
	 */
	public void setMinimumPatternLength(int minimumPatternLength) {
		this.minimumPatternLength = minimumPatternLength;
	}
	
	
	/**
	 * Optional method to specify the items that must appears in patterns found by TKS
	 * @param mustAppearItems an array of items
	 */
	public void setMustAppearItems(int[] mustAppearItems) {
		this.mustAppearItems = mustAppearItems;
	}
	
	/**
	 * Check if an item must appear in the pattern
	 * @param item the item
	 * @return true if the user has specified that this item must appear in the pattern
	 */
	public boolean itemMustAppearInPatterns(int item) {
		return (mustAppearItems == null) || Arrays.binarySearch(mustAppearItems, item) >=0;
	}

	/**
	 * This method allows to specify the maximum gap 
	 * between itemsets of patterns found by the algorithm. 
	 * If set to 1, only patterns of contiguous itemsets
	*  will be found (no gap).
	 * @param maxGap the maximum gap (an integer)
	 */
	public void setMaxGap(int maxGap) {
		this.maxGap = maxGap;
	}
	
	/**
	 * This method allows to specify if sequence identifiers should be shown in the output
	 * @param showSequenceIdentifiers if true, sequence identifiers will be shown (boolean)
	 */
	public void showSequenceIdentifiersInOutput(boolean showSequenceIdentifiers) {
		this.outputSequenceIdentifiers = showSequenceIdentifiers;
	}

}
