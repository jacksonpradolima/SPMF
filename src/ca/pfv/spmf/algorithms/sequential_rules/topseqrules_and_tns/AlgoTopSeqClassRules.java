package ca.pfv.spmf.algorithms.sequential_rules.topseqrules_and_tns;
/* This file is copyright (c) 2008-2013 Philippe Fournier-Viger
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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ca.pfv.spmf.algorithms.ArraysAlgos;
import ca.pfv.spmf.datastructures.redblacktree.RedBlackTree;
import ca.pfv.spmf.input.sequence_database_array_integers.Sequence;
import ca.pfv.spmf.input.sequence_database_array_integers.SequenceDatabase;
import ca.pfv.spmf.tools.MemoryLogger;

/** 
 * This is the original implementation of the TopSeqRule algorithm for mining Top-K sequential rules.
 * It is described in this paper:
 * <br/><br/>
 * Fournier-Viger, P. & Tseng, V. S. (2011). Mining Top-K Sequential Rules.
 * Proceedings of the 7th Intern. Conf. on Advanced Data Mining and Applications (ADMA 2011). 
 * LNAI 7121, Springer, pp.180-194. 
 * <br/><br/>
 * The implementation use the red-black tree data structure for storing the current top-k rules during
 * the mining process and the top-k candidate for exploration (see details in the paper).
 * 
 * @see RedBlackTree
 * @see Sequence
 * @see SequenceDatabase
 * @author Philippe Fournier-Viger
 */
public class AlgoTopSeqClassRules {

	
	/** start time of latest execution */
	long timeStart = 0;  
	
	/**  end time of latest execution */
	long timeEnd = 0;  
	
	// parameters
	/** minimum confidence */
	double minConfidence;  
	
	/** the k parameter */
	int k=0;  
	
	/** the sequence database */
	SequenceDatabase database; 
	
	// internal variables
	/** minimum support which will be raised dynamically */
	int minsuppRelative;  
	
	/** the top k rules found until now  */
	RedBlackTree<ClassRule> kRules;  
	
	/**  the candidates for expansion */
	RedBlackTree<ClassRule> candidates;  

	/** the max number of candidates at the same time during the last execution */
	int maxCandidateCount = 0;
	
	/**Arrays where the ith position contains
	// the map of last or first occurrences for the item i
	// The key of the maps is a sequence ID and the value is an occurence. */
	Map<Integer, Short>  arrayMapItemCountFirst[];  // item, <tid, occurence>
	Map<Integer, Short>  arrayMapItemCountLast[];  // item, <tid, occurence>
	
	/**  the maximum size of the antecedent of rules (optional) */
	int maxAntecedentSize = Integer.MAX_VALUE;
	
	/** the list of items that can be used as consequent for all rules */
	 int[] itemToBeUsedAsConsequent;
	 
	/**
	 * Default constructor
	 */
	public AlgoTopSeqClassRules() {
	}

	
	/**
	 * The main method to run the algorithm
	 * @param k : the chosen value of k
	 * @param database : a sequence database
	 * @param minConfidence : the minimum confidence threshold
	 * @param itemToBeUsedAsConsequent 
	 * @return a redblacktree containing the  top-k  sequential rules
	 */
	public RedBlackTree<ClassRule> runAlgorithm(int k, SequenceDatabase database, double minConfidence, int[] itemToBeUsedAsConsequent) {
		// save the parameters
		this.database = database;
		this.minConfidence = minConfidence;
		this.k = k;
		this.itemToBeUsedAsConsequent = itemToBeUsedAsConsequent;
		
		// reset the utility for checking the memory usage
		MemoryLogger.getInstance().reset();
		
		// for statistics
		this.maxCandidateCount = 0;
		
		// set minsup = 1 (will be increased by the algorithm progressively)
		this.minsuppRelative = 1;
		
		// create the structure for storing the first/last occurences
		arrayMapItemCountFirst = new HashMap[database.maxItem+1];
		arrayMapItemCountLast = new HashMap[database.maxItem+1];
		
		// the sets that will contain the top-k rules and the candidates
		kRules = new RedBlackTree<ClassRule>();
		candidates = new RedBlackTree<ClassRule>();

		// record start time
		timeStart = System.currentTimeMillis();
		
		if(maxAntecedentSize >=1){
			// scan the database to count the occurence of each item
			scanDatabase(database);	
			// start the algorithm
			start();
		}
		
		// record end time
		timeEnd = System.currentTimeMillis(); 
		
		// return the top-rules
		return kRules;
	}

	/**
	 * Start the rule generation.
	 */
	private void start() {	
		// We will now try to generate rules with one item in the
		// antecedent and one item in the consequent using
		// frequent items.
		
		// For each pair of frequent items i  and j such that i != j
main1:	for(int itemI=database.minItem; itemI<= database.maxItem; itemI++){
			// Get the map of occurences of item I
			Map<Integer, Short> occurencesIfirst = arrayMapItemCountFirst[itemI];
			
			// if none continue
			if(occurencesIfirst == null){
				continue main1;
			}
			// get  the set of sequence IDs containing I
			Set<Integer> tidsI = occurencesIfirst.keySet();
			// if the support of I (cardinality of the tids) is lower
			// than minsup, than it is not frequent, so we skip this item
			if(tidsI.size() < minsuppRelative){
				continue main1;
			}
			
main2:		for(int itemJ : itemToBeUsedAsConsequent){
				if(itemJ == itemI){
					continue;
				}
				
				// Get the map of occurences of item J
				Map<Integer, Short> occurencesJfirst = (Map<Integer, Short>) arrayMapItemCountFirst[itemJ];
				
				// if none continue
				if(occurencesJfirst == null){
					continue main2;
				}
				// get  the set of sequence IDs containing J
				Set<Integer> tidsJ = occurencesJfirst.keySet();
				// if the support of J (cardinality of the tids) is lower
				// than minsup, than it is not frequent, so we skip this item
				if(tidsJ.size() < minsuppRelative){
					continue main2;
				}
				
				// (1) Build list of common  tids  and count occurences 
				// of i ==> j  and  j ==> i.
				
				// These two hashsets will store the tids of: 
				Set<Integer> tidsIJ = new HashSet<Integer>();  // i ==> j
				Set<Integer> tidsJI = new HashSet<Integer>(); // j ==> i.

				// These maps will store the last occurence of I 
				// and last occurence of J for each sequence ID (a.k.a. tid)
				//  key: tid     value:  itemset position 
				Map<Integer, Short> occurencesJlast = (Map<Integer, Short>) arrayMapItemCountLast[itemJ];
				Map<Integer, Short> occurencesIlast = arrayMapItemCountLast[itemI];

				// if there is less tids in J, then
				// we will loop over J instead of I to calculate the tidsets
				if(tidsI.size() > tidsJ.size()){ 
					
					// this repsents the number of itemsets left to be scanned
					int left = tidsJ.size();
					
					// for each tid where J eappears
					for(Entry<Integer, Short> entry : occurencesJfirst.entrySet()){
						Integer tid = entry.getKey();
						
						// get the first occurence of I
						Short occIFirst = occurencesIfirst.get(tid);
						// if there is one
						if(occIFirst !=  null){
							// get the first and last occurences of J
							Short occJFirst = occurencesJfirst.get(tid);
							Short occJLast = occurencesJlast.get(tid);
							// If the first of I appears before the last of J
							if(occIFirst < occJLast){
								// current tid to the tidset of  i ==> j 
								tidsIJ.add(tid);
							}
							Short occILast = occurencesIlast.get(tid);
							// If the first of J appears before the last of I
							if(occJFirst < occILast){
								// current tid to the tidset of  j ==> i 
								tidsJI.add(tid);
							}
						}
						left--;// go to next itemset (in backward direction)
						
						// if there is not enough itemset left so that i--> j
						// or j==> i could be frequent, then we can stop
						if(((left + tidsIJ.size()) < minsuppRelative) && 
								((left + tidsJI.size()) < minsuppRelative)){
							continue main2;
						}
					}
				}else{
					// otherwise
					// we will loop over I instead of J to calculate the tidsets
					
					// this repsents the number of itemsets left to be scanned
					int left = tidsI.size();
					
					for(Entry<Integer, Short> entry : occurencesIfirst.entrySet()){
						Integer tid = entry.getKey();
						
						// get the first occurence of J
						Short occJFirst = occurencesJfirst.get(tid);
						
						// if there is one
						if(occJFirst !=  null){
							// get the first and last occurences of I
							Short occIFirst = occurencesIfirst.get(tid);
							Short occILast = occurencesIlast.get(tid);
							// If the first of J appears before the last of I
							if(occJFirst < occILast){
								// current tid to the tidset of  j ==> i
								tidsJI.add(tid);
							}
							Short occJLast = occurencesJlast.get(tid);
							// If the first of I appears before the last of J
							if(occIFirst < occJLast){
								// current tid to the tidset of  i ==> j 
								tidsIJ.add(tid);
							}
						}
						left--; // go to next itemset (in backward direction)
						
						// if there is not enough itemset left so that i--> j
						// or j==> i could be frequent, then we can stop
						if(((left + tidsIJ.size()) < minsuppRelative) && 
								((left + tidsJI.size()) < minsuppRelative)){
							continue main2;
						}
					}
				}
				
				// (2) check if the two itemsets have enough common tids
				// if not, we don't need to generate a rule for them.
				// create rule IJ
				int supIJ = tidsIJ.size();

				// if the rule I ==> J  is frequent
				if(supIJ >= minsuppRelative){
					// create the rule
					double confIJ = ((double)tidsIJ.size()) / occurencesIfirst.size();
					int[] itemsetI = new int[1];
					itemsetI[0]= itemI;
					
					ClassRule ruleIJ = new ClassRule(itemsetI, itemJ, confIJ, supIJ, tidsI, tidsJ, tidsIJ, occurencesJlast);
					
					// if the rule is valid
					if(confIJ >= minConfidence){
						// save the rule to current top-k list
						save(ruleIJ, supIJ); 
					}
					registerAsCandidate(ruleIJ);
				}
			}
		}
		
	// Now we have finished checking all the rules containing 1 item
	// in the left side and 1 in the right side,
	// the next step is to recursively expand rules in the set 
	// "candidates" to find more rules.
	while(!candidates.isEmpty()){
		// we take the rule with the highest support first
			ClassRule rule = candidates.popMaximum();
			// if there is no more candidates with enough support, then we stop
			if(rule.getAbsoluteSupport() < minsuppRelative){
				break;
			}
			expandL(rule);
		}
	}

	/**
	 * Save a rule in the current top-k set
	 * @param rule the rule
	 * @param support the support of the rule
	 */
	private void save(ClassRule rule, int support) {
		// We add the rule to the set of top-k rules
		kRules.add(rule);
		// if the size becomes larger than k
		if(kRules.size() > k ){
			// if the support of the rule that we haved added is higher than
			// the minimum support, we will need to take out at least one rule
			if(support > this.minsuppRelative ){
				// we recursively remove the rule having the lowest support,
				// until only k rules are left
				ClassRule lower;
				do{
					lower = kRules.lower(new ClassRule(null, null, 0, this.minsuppRelative+1, null, null, null, null));
					if(lower == null){
						break;  /// IMPORTANT
					}
					kRules.remove(lower);
				}while(kRules.size() > k);
			}
			// we raise the minimum support to the lowest support in the 
			// set of top-k rules
			this.minsuppRelative = kRules.minimum().getAbsoluteSupport();
		}
//		System.out.println(this.minsuppRelative);
	}
	
	/**
	 * Add a candidate to the set of candidate
	 * @param expandLR a boolean indicating if this candidate is 
	 *        for a left AND right expansion or just a left expansion.
	 * @param ruleLR the rule
	 */
	private void registerAsCandidate(ClassRule ruleLR) {
		
		candidates.add(ruleLR); // add the rule
		
		// remember the maximum number of candidates reacher for stats
		if(candidates.size() >= maxCandidateCount){
			maxCandidateCount = candidates.size();
		}
		// check the memory usage
		MemoryLogger.getInstance().checkMemory();
	}

	/**
	 * This method search for items for expanding left side of a rule I --> J 
	 * with any item c. This results in rules of the form I U�{c} --> J. The method makes sure that:
	 *   - c  is not already included in I or J
	 *   - c appear at least minsup time in tidsIJ before last occurence of J
	 *   - c is lexically bigger than all items in I
	 */
    private void expandL(ClassRule rule) {  
    	if(rule.getItemset1().length == maxAntecedentSize){
    		return;
    	}
    	
    	// The following map will be used to count the support of each item
    	// c that could potentially extend the rule.
    	// The map associated a set of tids (value) to an item (key).
    	Map<Integer, Set<Integer>> frequentItemsC  = new HashMap<Integer, Set<Integer>>();  
    	
    	// we scan the sequence where I-->J appear to search for items c that we could add.
    	// for each sequence containing I-->J
    	int left = rule.tidsIJ.size();
    	for(Integer tid : rule.tidsIJ){
    		// get the sequence and occurences of J in that sequence
    		Sequence sequence = database.getSequences().get(tid);
			Short end = rule.occurencesJlast.get(tid);
			
			// for each itemset before the last occurence of J
itemLoop:	for(int k=0; k < end; k++){
				Integer[] itemset = sequence.get(k);
				// for each item
				for(int m=0; m< itemset.length; m++){
					Integer itemC = itemset[m];
					
					// We will consider if we could create a rule IU{c} --> J
					// If lexical order is not respected or c is included in the rule already,
					// then we cannot so return.		
					if(ArraysAlgos.containsLEXPlus(rule.getItemset1(), itemC) 
							||  rule.getItemset2() == itemC){
						continue;
					}

					// Otherwise, we get the tidset of "c" 
					Set<Integer> tidsItemC = frequentItemsC.get(itemC);
					
					// if this set is not null, which means that "c" was not seen yet
					// when scanning the sequences from I==>J
					if(tidsItemC == null){ 
						// if there is less tids left in the tidset of I-->J to be scanned than
						// the minsup, we don't consider c anymore because  IU{c} --> J
						// could not be frequent
						if(left < minsuppRelative){
							continue itemLoop;
						}	
						// if "c" was seen before but there is not enough sequences left to be scanned
						// to allow IU{c} --> J to reach the minimum support threshold
					}else if(tidsItemC.size() + left < minsuppRelative){
						// remove c and continue the loop of items
						tidsItemC.remove(itemC);
						continue itemLoop;
					}
					// otherwise, if we did not see "c" yet, create a new tidset for "c"
					if(tidsItemC == null){
						tidsItemC = new HashSet<Integer>(rule.tidsIJ.size());
						frequentItemsC.put(itemC, tidsItemC);
					}
					// add the current tid to the tidset of "c"
					tidsItemC.add(tid);			
				}
			}
    		left--;// decrease the number of sequences left to be scanned
		}
    	
    	// For each item c found, we create a rule	IU{c} ==> J
    	for(Entry<Integer, Set<Integer>> entry : frequentItemsC.entrySet()){
    		// get the tidset IU{c} ==> J
    		Set<Integer> tidsIC_J = entry.getValue();
    		
    		// if the support of IU{c} ==> J is enough 
    		if(tidsIC_J.size() >= minsuppRelative){ 
        		Integer itemC = entry.getKey();
        		
        		// Calculate tids containing IU{c} which is necessary
    			// to calculate the confidence
    			Set<Integer> tidsIC = new HashSet<Integer>(rule.tidsI.size());
    	    	for(Integer tid: rule.tidsI){
    	    		if(arrayMapItemCountFirst[itemC].containsKey(tid)){
    	    			tidsIC.add(tid);
    	    		}
    	    	}

    			// Create rule and calculate its confidence of IU{c} ==> J 
    	    	// defined as:  sup(IU{c} -->J) /  sup(IU{c})					
				double confIC_J = ((double)tidsIC_J.size()) / tidsIC.size();
				int [] itemsetIC = new int[rule.getItemset1().length+1];
				System.arraycopy(rule.getItemset1(), 0, itemsetIC, 0, rule.getItemset1().length);
				itemsetIC[rule.getItemset1().length] = itemC;

				// if the confidence is high enough, then it is a valid rule
				ClassRule candidate = new ClassRule(itemsetIC,rule.getItemset2(), confIC_J, tidsIC_J.size(), tidsIC, null, tidsIC_J, rule.occurencesJlast);
				if(confIC_J >= minConfidence){
					// save the rule
					save(candidate, tidsIC_J.size());
				}
				registerAsCandidate(candidate); 
    		}
    	}
    	// check the memory usage
    	MemoryLogger.getInstance().checkMemory();
	}
    
	

    
	/**
	 * This method calculate the frequency of each item in one database pass.
	 * @param database : a sequence database 
	 * @return A map such that key = item
	 *                         value = a map  where a key = tid  and a value = Occurence
	 * This map allows knowing the frequency of each item and their first and last occurence in each sequence.
	 */
	private void scanDatabase(SequenceDatabase database) {
		// (1) Count the support of each item in the database in one database pass

		// for each sequence in the database
		for(int tid=0; tid< database.size(); tid++){
			Sequence sequence = database.getSequences().get(tid);
			// for each itemset in that sequence
			for(short j=0; j< sequence.getItemsets().size(); j++){
				Integer[] itemset = sequence.get(j);
				// for each item in that sequence
				for(int i=0; i<itemset.length; i++ ){
					Integer itemI = itemset[i];
					// if the map of occurences of that item is null, create a new one
					if(arrayMapItemCountFirst[itemI] == null){
						arrayMapItemCountFirst[itemI] =  new HashMap<Integer, Short>();
						arrayMapItemCountLast[itemI] = new HashMap<Integer, Short>();
					}
					// then update the occurences by adding j as the 
					// first and/or last occurence(s) in sequence k
					Short oldPosition = arrayMapItemCountFirst[itemI].get(tid);
					if(oldPosition == null){
						arrayMapItemCountFirst[itemI].put(tid, j);
						arrayMapItemCountLast[itemI].put(tid, j);
					}else{
						arrayMapItemCountLast[itemI].put(tid, j);
					}
				}
			}
		}
	}
	
	/**
	 * Print statistics about the last algorithm execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  TOPSEQRULES - STATS ========");
		System.out.println("Max candidates: " + maxCandidateCount);
		System.out.println("Sequential rules count: " + kRules.size());
		System.out.println("-");
		System.out.println("Total time: " + (((double)(timeEnd - timeStart))/1000d) + " s");
		System.out.println("Max memory: " + MemoryLogger.getInstance().getMaxMemory());
		System.out.println("Minsup relative: " + minsuppRelative);
		System.out.println("==========================================");
	}

	/**
	 * Get the total runtime of the last execution.
	 * @return the time as a double.
	 */
	public double getTotalTime(){
		return timeEnd - timeStart;
	}

	/**
	 * Write the result by the last execution of the method "runAlgorithm" to an output file
	 * @param path the output file path
	 * @throws IOException exception if an error occur when writing the file.
	 */
	public void writeResultTofile(String path) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(path)); 
		if(kRules.size() > 0){
			Iterator<ClassRule> iter = kRules.iterator();
			while (iter.hasNext()) {
				ClassRule rule = (ClassRule) iter.next();
				StringBuilder buffer = new StringBuilder();
				buffer.append(rule.toString());
				// write separator
				buffer.append(" #SUP: ");
				// write support
				buffer.append(rule.getAbsoluteSupport());
				// write separator
				buffer.append(" #CONF: ");
				// write confidence
				buffer.append(rule.getConfidence());
				writer.write(buffer.toString());
				writer.newLine();
			}
		}
		writer.close();
	}
	
	/**
	 * Set the number of items that a rule antecedent should contain (optional).
	 * @param maxAntecedentSize the maximum number of items
	 */
	public void setMaxAntecedentSize(int maxAntecedentSize) {
		this.maxAntecedentSize = maxAntecedentSize;
	}



}
