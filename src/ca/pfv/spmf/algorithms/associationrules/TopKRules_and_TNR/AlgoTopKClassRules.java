package ca.pfv.spmf.algorithms.associationrules.TopKRules_and_TNR;

/* This file is copyright (c) 2008-2012 Philippe Fournier-Viger
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
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;

import ca.pfv.spmf.tools.MemoryLogger;

/**
 * TopKRules is an algorithm for mining the TOP-K  association rules from a 
 * transaction database using
 * a pattern growth approach and several optimizations. This is a modification of the original
 * implementation called TopKClassRules that is designed for mining rules with fixed items
 * as consequent (what is called class association rules). The TopKRule algorithm was 
 * proposed in this paper:
 * <br/><br/>
 * 
 * Fournier-Viger, P., Wu, C.-W., Tseng, V. S. (2012). Mining Top-K Association Rules. Proceedings of the 25th Canadian Conf. on Artificial Intelligence (AI 2012), Springer, LNAI 7310, pp. 61-73.
 * 
 * @author Philippe Fournier-Viger, 2012
 * @see AlgoTopKClassRules
 */
public class AlgoTopKClassRules {
	
	/** start time of latest execution */
	long timeStart = 0;  
	
	/**  end time of latest execution */
	long timeEnd = 0;  
	
	/** minimum confidence */
	double minConfidence;  
	
	/** parameter k */
	int k = 0;          
	
	/**  a transaction database */
	Database database;   

	/** minimum support that will be reased during the search */
	int minsuppRelative;
	
	/** a vertical representation of the database 
	 * [item], IDs of transaction containing the item */
	BitSet[] tableItemTids;  
	/** a table indicating the support of each item
	 * [item], support
	 */
	int[] tableItemCount; 
	
	/**  the top k rules found until now */
	PriorityQueue<ClassRuleG> kRules; 
	
	/** the candidates for expansion */
	PriorityQueue<ClassRuleG> candidates; 

	/** the maximum number of candidates at the same time during the last execution */
	int maxCandidateCount = 0;
	
	/**  the maximum size of the antecedent of rules (optional) */
	int maxAntecedentSize = Integer.MAX_VALUE;
	
	/** the list of items that can be used as consequent for all rules */
	 int[] itemToBeUsedAsConsequent;
	/**
	 * Default constructor
	 */
	public AlgoTopKClassRules() {
	}

	/**
	 * Run the algorithm.
	 * @param k the value of k.
	 * @param minConfidence the minimum confidence threshold.
	 * @param database the database.
	 * @param fixedItemAsConsequent an item that must be the consequent of all rules
	 */
	public void runAlgorithm(int k, double minConfidence, Database database,  int[] itemToBeUsedAsConsequent) {
		// reset statistics
		MemoryLogger.getInstance().reset(); // reset utility to check memory usage
		maxCandidateCount = 0;
		
		// save parameters
		this.minConfidence = minConfidence;
		this.database = database;
		this.k = k;
		this.itemToBeUsedAsConsequent = itemToBeUsedAsConsequent;

		// prepare internal variables and structures
		this.minsuppRelative = 1;
		tableItemTids = new BitSet[database.maxItem + 1]; // id item, count
		tableItemCount = new int[database.maxItem + 1];
		kRules = new PriorityQueue<ClassRuleG>();
		candidates = new PriorityQueue<ClassRuleG>(new Comparator<ClassRuleG>(){
			// BUG FIX 2017
			@Override
			public int compare(ClassRuleG o1, ClassRuleG o2) {
				return - (o1.compareTo(o2));
			}});

		// record the start time
		timeStart = System.currentTimeMillis(); 
		
		if(maxAntecedentSize >=1){
			// perform the first database scan to generate vertical database representation
			scanDatabase(database);
			 
			// start the generation of rules
			start();
		}
		
		// record the end time
		timeEnd = System.currentTimeMillis(); 
	}

	/**
	 * Start the rule generation.
	 */
	private void start() {
		// We will now try to generate rules with one item in the
		// antecedent and one item in the consequent using
		// frequent items.
		
		// for each item I in the database
		main: for (int itemI = 0; itemI <= database.maxItem; itemI++) {
			// if the item is not frequent according to the current
			// minsup threshold, then skip it
			if (tableItemCount[itemI] < minsuppRelative) {
				continue main;
			}
			// Get the bitset corresponding to item I
			BitSet tidsI = tableItemTids[itemI];

			// for each item J in the database
			main2: for (int itemJ : itemToBeUsedAsConsequent) {
				if(itemI == itemJ){
					continue;
				}
				// if the item is not frequent according to the current
				// minsup threshold, then skip it
				if (tableItemCount[itemJ] < minsuppRelative) {
					continue main2;
				}
				// Get the bitset corresponding to item J
				BitSet tidsJ = tableItemTids[itemJ];

				// Calculate the list of transaction IDs shared
				// by I and J.
				// To do that with a bitset, we just do a logical AND.
				BitSet commonTids = (BitSet) tidsI.clone();
				commonTids.and(tidsJ);
				// We keep the cardinality of the new bitset because in java
				// the cardinality() method is expensive, and we will need it again later.
				int support = commonTids.cardinality();
				
				// If the rules I ==> J and J ==> I have enough support
				if (support >= minsuppRelative) {
					// generate  rules I ==> J and J ==> I and remember these rules
					// for future possible expansions
					generateRuleSize11(itemI, tidsI, itemJ, tidsJ, commonTids,
							support);
				}
			}
		}
	
		// Now we have finished checking all the rules containing 1 item
		// in the left side and 1 in the right side,
		// the next step is to recursively expand rules in the set 
		// "candidates" to find more rules.
		while (candidates.size() > 0) {
			// We take the rule that has the highest support first
			ClassRuleG rule = candidates.poll();
			// if there is no more candidates with enough support, then we stop
			if (rule.getAbsoluteSupport() < minsuppRelative) {
				// candidates.remove(rule);
				break;
			}
			// Otherwise, we try to expand the rule
			expandL(rule);
		}
	}

	/**
	 * This method test the rules I ==> J and J ==> I  for their confidence
	 * and record them for future expansions.
	 * @param itemI an item I
	 * @param tidI  the set of IDs of transaction containing  item I (BitSet)
	 * @param itemJ an item J
	 * @param tidJ  the set of IDs of transaction containing  item J (BitSet)
	 * @param commonTids  the set of IDs of transaction containing I and J (BitSet)
	 * @param cardinality  the cardinality of "commonTids"
	 */
	private void generateRuleSize11(Integer item1, BitSet tid1, int item2,
			BitSet tid2, BitSet commonTids, int cardinality) {
		// Create the rule I ==> J
		Integer[] itemset1 = new Integer[1];
		itemset1[0] = item1;
		ClassRuleG ruleLR = new ClassRuleG(itemset1, item2, cardinality, tid1,
				commonTids, item1); 
		
		// calculate the confidence
		double confidenceIJ = ((double) cardinality) / (tableItemCount[item1]);
		
		// if rule i->j has minimum confidence
		if (confidenceIJ >= minConfidence) {
			// save the rule in current top-k rules
			save(ruleLR, cardinality);
		}
		// register the rule as a candidate for future expansion
		if(ruleLR.getItemset1().length < maxAntecedentSize ){
			registerAsCandidate(ruleLR);
		}
	}

	/**
	 * Register a given rule in the set of candidates for future expansions
	 * @param expandLR  if true the rule will be considered for left/right 
	 * expansions otherwise only right.
	 * @param rule the given rule
	 */
	private void registerAsCandidate(ClassRuleG rule) {
		// add the rule to candidates
		candidates.add(rule);

		// record the maximum number of candidates for statistics
		if (candidates.size() >= maxCandidateCount) {
			maxCandidateCount = candidates.size();
		}
		// check the memory usage
		MemoryLogger.getInstance().checkMemory();
	}

	/**
	 * Try to expand a rule by left and right expansions.
	 * @param ClassRuleG the rule
	 */
	private void expandL(ClassRuleG ClassRuleG) {
    	if(ClassRuleG.getItemset1().length == maxAntecedentSize){
    		return;
    	}
    	
		// Maps to record the potential item to expand the left/right sides of the rule
		// Key: item   Value: bitset indicating the IDs of the transaction containing the item
		// from the transactions containing the rule.
		Map<Integer, BitSet> mapCountLeft = new HashMap<Integer, BitSet>();

		
		for (int tid = ClassRuleG.common.nextSetBit(0); tid >= 0; tid = ClassRuleG.common
				.nextSetBit(tid + 1)) {
			Iterator<Integer> iter = database.getTransactions().get(tid)
					.getItems().iterator();
			while (iter.hasNext()) {
				Integer item = iter.next();
				// CAN DO THIS BECAUSE TRANSACTIONS ARE SORTED BY DESCENDING
				// ITEM IDS (see Database.Java)
				if (item < ClassRuleG.maxLeft && item < ClassRuleG.getItemset2()) { //
					break;
				}
				if (tableItemCount[item] < minsuppRelative) {
					iter.remove();
					continue;
				}
				if (item > ClassRuleG.maxLeft &&  item != ClassRuleG.getItemset2()) {
					BitSet tidsItem = mapCountLeft.get(item);
					if (tidsItem == null) {
						tidsItem = new BitSet();
						mapCountLeft.put(item, tidsItem);
					}
					tidsItem.set(tid);
				}
			}
		}

		// for each item c found in the previous step, we create a rule	
		// I  U {c} ==> J if the support is enough
		if(ClassRuleG.getItemset1().length < maxAntecedentSize){
			for (Entry<Integer, BitSet> entry : mapCountLeft.entrySet()) {
				BitSet tidsRule = entry.getValue();
				int ruleSupport = tidsRule.cardinality();
	
				// if the support is enough
				if (ruleSupport >= minsuppRelative) {
					Integer itemC = entry.getKey();
	
					// The tidset of the left itemset is calculated
					BitSet tidsLeft = (BitSet) ClassRuleG.tids1.clone();
					tidsLeft.and(tableItemTids[itemC]);
	
					// create new left part of rule
					Integer[] newLeftItemset = new Integer[ClassRuleG.getItemset1().length + 1];
					System.arraycopy(ClassRuleG.getItemset1(), 0, newLeftItemset, 0,
							ClassRuleG.getItemset1().length);
					newLeftItemset[ClassRuleG.getItemset1().length] = itemC;
	
					// recompute maxLeft
					int maxLeft = itemC >= ClassRuleG.maxLeft ? itemC : ClassRuleG.maxLeft;
	
					// calculate the confidence of the rule
					double confidence = ((double) ruleSupport)
							/ tidsLeft.cardinality();
					
					// create the rule
					ClassRuleG candidate = new ClassRuleG(newLeftItemset,
							ClassRuleG.getItemset2(), ruleSupport, tidsLeft, tidsRule,
							maxLeft);
	
					// if the confidence is high enough
					if (confidence >= minConfidence) {
						// save the rule to the top-k rules
						save(candidate, ruleSupport);
					}
					// register the rule as a candidate for further expansions
					registerAsCandidate(candidate);
				}
			}
		}
	}
	
	
	
	/**
	 * Save a rule to the current set of top-k rules.
	 * @param rule the rule to be saved
	 * @param support the support of the rule
	 */
	private void save(ClassRuleG rule, int support) {
		// We add the rule to the set of top-k rules
		kRules.add(rule);
		// if the size becomes larger than k
		if (kRules.size() > k) {
			// if the support of the rule that we haved added is higher than
			// the minimum support, we will need to take out at least one rule
			if (support > this.minsuppRelative) {
				// we recursively remove the rule having the lowest support,
				// until only k rules are left
				do {
					kRules.poll();
				} while (kRules.size() > k);
			}
			// we raise the minimum support to the lowest support in the 
			// set of top-k rules
			this.minsuppRelative = kRules.peek().getAbsoluteSupport();
		}
	}

	/**
	 * Method to scan the database to create the vertical database.
	 * @param database a database of type Database.
	 */
	private void scanDatabase(Database database) {
		// for each transaction
		for (int j = 0; j < database.getTransactions().size(); j++) {
			Transaction transaction = database.getTransactions().get(j);
			// for each item in the current transaction
			for (Integer item : transaction.getItems()) {
				// update the tidset of this item (represented by a bitset.
				BitSet ids = tableItemTids[item];
				if (ids == null) {
					tableItemTids[item] = new BitSet(database.tidsCount);
				}
				tableItemTids[item].set(j);
				// update the support of this item
				tableItemCount[item] = tableItemCount[item] + 1;
			}
		}
	}
	
	/**
	 * Print statistics about the last algorithm execution.
	 */
	public void printStats() {
		System.out.println("=============  TOP-K CLASS RULES SPMF v.2.28 - STATS =============");
		System.out.println("Minsup : " + minsuppRelative);
		System.out.println("Rules count: " + kRules.size());
		System.out.println("Memory : " + MemoryLogger.getInstance().getMaxMemory() + " mb");
		System.out.println("Total time : " + (timeEnd - timeStart) + " ms");
		System.out.println("===================================================");
	}
	
	/**
	 * Write the rules found to an output file.
	 * @param path the path to the output file
	 * @throws IOException exception if an error while writing the file
	 */
	public void writeResultTofile(String path) throws IOException {
		// Prepare the file
		BufferedWriter writer = new BufferedWriter(new FileWriter(path));
		
		if(kRules.size() > 0){
			// sort the rules in sorted order before printing them
			// because the Iterator from Java on a priority queue do not
			// show the rules in priority order unfortunately (even though
			// they are sorted in the priority queue. 
			Object[] rules = kRules.toArray();
			Arrays.sort(rules);  
			
			// for each rule
			for(Object ruleObj : rules){
				ClassRuleG rule = (ClassRuleG) ruleObj;
				
				// Write the rule
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
		// close the file
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
