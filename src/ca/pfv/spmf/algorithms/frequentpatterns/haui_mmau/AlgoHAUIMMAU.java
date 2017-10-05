package ca.pfv.spmf.algorithms.frequentpatterns.haui_mmau;

/* This is an implementation of the HAUI-MMAU algorithm. 
* 
* Copyright (c) 2016 HAUI-MMAU
* 
* This file is part of the SPMF DATA MINING SOFTWARE * (http://www.philippe-fournier-viger.com/spmf). 
* 
* 
* SPMF is free software: you can redistribute it and/or modify it under the * terms of the GNU General Public License as published by the Free Software * Foundation, either version 3 of the License, or (at your option) any later * version. * 

* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR * A PARTICULAR PURPOSE. See the GNU General Public License for more details. * 
* 
* You should have received a copy of the GNU General Public License along with * SPMF. If not, see . 
* 
* @author Ting Li
*/

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.pfv.spmf.tools.MemoryLogger;



/**
 * This is an implementation of the "HAUI-MMAU" for High Average-Utility Itemsets Mining.
 * HAUI-MMAU is described in the conference paper : <br/><br/>
 * 
 *  Jerry Chun-Wei Lin, Ting Li, Philippe Fournier-Viger, Tzung-Pei Hong, and Ja-Hwung Su. 
 *  Efficient Mining of High Average-Utility Itemsets with Multiple Minimum Thresholds
 * Proceedings of Industrial Conference on Data Mining, 2016:14-28.
 *  
 *  This implementation uses the Apriori algorithm as it seems to be suggested by the article, even if
 *  the Apriori algorithm is not mentionned explicitly in the article.
 *
 * @see ItemsetsTP
 * @see ItemsetTP
 * @see TransactionTP
 * @see UtilityTransactionDatabase
 * @author Ting Li et al.
 */
public class AlgoHAUIMMAU {

	// the set of high average utility itemsets found by the algorithm
	private ItemsetsTP highAUtilityItemsets = null;
	// the database
	protected UtilityTransactionDatabaseTP database;
	
	// the min utility threshold
	 Map<Integer, Integer> minAUtility=new HashMap<Integer, Integer>();

	// for statistics
	long startTimestamp = 0;  // start time
	long endTimestamp = 0; // end time
	private int candidatesCount; // the number of candidates generated
	
	/**
	 * Default constructor
	 */
	public AlgoHAUIMMAU() {
	}

	/**
	 *  run HAUI-MMAU algorithm 
	 * @param database: used database
	 * @param mutipleMinUtilities: the minimum average-utility threshold of each item.
	 * @param GLMAU: user-specified and represents the global least average-utility value
	 * @return final result
	 */
	public ItemsetsTP runAlgorithm(UtilityTransactionDatabaseTP database, Map<Integer, Integer> mutipleMinUtilities, int GLMAU) {
		// save the parameters
		this.database = database;
		this.minAUtility = mutipleMinUtilities;
	
		// reset the utility to check the memory usage
		MemoryLogger.getInstance().reset();
		// record start time
		startTimestamp = System.currentTimeMillis();

		// initialize the set of HAUIs (high average utility itemsets)
		highAUtilityItemsets = new ItemsetsTP("HIGH AVERAGE UTILITY ITEMSETS WITH MULTIPLE MINIMUM THRESHOLDS");
		
		// keep HAUIs
		 Map<List<Integer>, Integer> HAUIMap= new HashMap<List<Integer>, Integer>();
		 
		// keep high AUUB candidate 
		 Map<List<Integer>, Integer> candidateHAUIMap= new HashMap<List<Integer>, Integer>();
		 
		// number of caniddate count
		candidatesCount =0;
		
		// ===================  PHASE 1: GENERATE CANDIDATES  =================== 
		
		// First, we create the level of candidate itemsets of size 1
		List<ItemsetTP> candidatesSize1 = new ArrayList<ItemsetTP>();
		
		// Scan database one time to get the tidset of each item and its utility for the whole database
		// Map to store the tidset of each item
		// key: item   value: tidset as a set of integers
		Map<Integer, Set<Integer>> mapItemTidsets = new HashMap<Integer, Set<Integer>>();
		// Map to store the AUUB of each item  (key: item , value: AUUB)
		Map<Integer, Integer> mapItemAUUB = new HashMap<Integer, Integer>();
		// variable to remember the maximum item ID
		int maxItem = Integer.MIN_VALUE;
		
		// for each line (transaction) in the database
		for(int i=0; i< database.size(); i++){
			// get the transaction
			TransactionTP transaction = database.getTransactions().get(i);
			
			// for each item in the current transactions
			for(int j=0; j< transaction.getItems().size(); j++) {
				ItemUtility itemUtilityObj = transaction.getItems().get(j);
				int item = itemUtilityObj.item;
				
				// if this is the largest item until now, remember it
				if(item > maxItem){
					maxItem = item;
				}
				// Add the tid of this transaction to the tidset of the item
				Set<Integer> tidset = mapItemTidsets.get(item);
				if(tidset == null){
					tidset = new HashSet<Integer>();
					mapItemTidsets.put(item, tidset);
				}
				tidset.add(i);
				
				// Add transaction utility for this item to its AUUB
				Integer sumUtility = mapItemAUUB.get(item);
				if(sumUtility == null){  // if no utility yet
					sumUtility = 0;
				}
				sumUtility += transaction.getTransactionUtility(); // add the utility
				mapItemAUUB.put(item, sumUtility);
			}
		}
		
		// Create a candidate itemset for each item having a AUUB  >= LMAU
		// For each item
		for(int item=0; item<= maxItem; item++){
			// Get the AUUB of the item
			Integer estimatedUtility = mapItemAUUB.get(item);
			// if it is a HAUUBI itemset (see formal definition in paper)
			if(estimatedUtility != null && estimatedUtility >= database.getLMAU(GLMAU)){//Lee
				// Create the itemset with this item and set its tidset
				ItemsetTP itemset = new ItemsetTP();
				itemset.addItem(item);
				itemset.setTIDset(mapItemTidsets.get(item));
				//add to candidate  map
				candidateHAUIMap.put(itemset.getItems(), estimatedUtility);
				// add it to candidates
				candidatesSize1.add(itemset);
				// add it to the set of HAUIs
				highAUtilityItemsets.addItemset(itemset, itemset.size());
			}
		}

		//sort 1-itemset
		for(int i=0;i<candidatesSize1.size();i++){
			for(int j=candidatesSize1.size()-1;j>0;j--){
				if(mutipleMinUtilities.get(candidatesSize1.get(j-1).getItems().get(0))>mutipleMinUtilities.get(candidatesSize1.get(j).getItems().get(0))){
					Collections.swap(candidatesSize1, j, j-1);
				}
			}
		}
		// From candidate of size 1, we recursively create candidates of greater size
		// until no candidates can be generated
		List<ItemsetTP> currentLevel = candidatesSize1;
		while (true) {
			// Generate candidates of size K+1
			int candidateCount = highAUtilityItemsets.getItemsetsCount();
			currentLevel = generateCandidateSizeK(currentLevel, highAUtilityItemsets, candidateHAUIMap, mutipleMinUtilities, GLMAU);
			// if no new candidates are found, then we stop because no more candidates will be found.
			if(candidateCount == highAUtilityItemsets.getItemsetsCount()){
				break;
			}
		}
		// the Phase 1 of the algorithm is now completed!

		// check memory usage
		MemoryLogger.getInstance().checkMemory();

		// ========================  PHASE 2: Calculate exact average utility of each candidate =============
		// for each level of HAUUBIs found in phase 1
		for(List<ItemsetTP> level : highAUtilityItemsets.getLevels()){
			// update the number of candidates generated until now
			candidatesCount += level.size();
			// for each HAUUBIs in that level
			Iterator<ItemsetTP> iterItemset = level.iterator();
			while(iterItemset.hasNext()){
				// this is the current HAUUBI
				ItemsetTP candidate = iterItemset.next();
				
				if(judge(candidate.getItems(), HAUIMap, true)==false){
					iterItemset.remove(); // delete it
					highAUtilityItemsets.decreaseCount();  // decrease number of itemsets found
					continue;
				}

				// Calculate exact average utility of that HAUUBI by scanning transactions of its tidset
				// For each transaction
				for(TransactionTP transaction : database.getTransactions()){
					// variable to store the transaction utility of "candidate" for the current transaction
					int transactionUtility =0;
					// the number of items from "candidate" appearing in this transaction
					int matchesCount =0; 
					// for each item of the transaction
					for(int i=0; i< transaction.size(); i++){
						// if it appears in "candidate"
						if(candidate.getItems().contains(transaction.get(i).item)){
							// add the transaction utility
							transactionUtility += transaction.getItemsUtilities().get(i).utility;
							matchesCount++; // increase the number of matches
						}
					}
					// if the number of matches is the size of "candidate", it means
					// that it appears completely in the transaction,
					// so we add the transaction utility of "candidate" to its utility.
					if(matchesCount == candidate.size()){
						candidate.incrementUtility(transactionUtility);
					}
				}
				
				// finally, after scanning all transactions for "candidate", we have its
				// real utility value.
				
				// if lager than mau, put into HAUIMap
				if(candidate.getAUtility() >= candidate.getItemsetMau(mutipleMinUtilities, GLMAU)){
					HAUIMap.put(candidate.getItems(), candidate.getAUtility());
				}
				
				// if lower than mau, it is not a HAUI so:
				if(candidate.getAUtility() < candidate.getItemsetMau(mutipleMinUtilities, GLMAU)){
					iterItemset.remove(); // delete it
					highAUtilityItemsets.decreaseCount();  // decrease number of itemsets found
				}
				
			}
		}
		
		// check memory usage
		MemoryLogger.getInstance().checkMemory();
		
		// record end time
		endTimestamp = System.currentTimeMillis();
		
		// Return all frequent itemsets found!
		return highAUtilityItemsets; 
	}

	/**
	 * X1+X2=X, if X1 and X2 are both not ,
	 * then X will not be a 
	 * @param items: waited for being judged whether can be a  
	 * @param HAUIMap: the set of 
	 * @return
	 */
	protected static boolean judge(List<Integer> items, Map<List<Integer>, Integer> HAUIMap, boolean mark){
		boolean temp=true;
		// if items is 1-itemsets, reture ture, 
		// which means scanning database is needed for getting actually average utility value for items
		if(items.size()==1)
			return true;
		
		List<List<Integer>> subset= new ArrayList<List<Integer>>();
		List<Integer> set= new ArrayList<Integer>();
		set.add(items.get(0));
		subset.add(set);
		//X1+X2=X, generate half subset X1
		for(int i=1;i<items.size()-1;i++){
			int j=0;
			int k=subset.size();
			while(j<k){
				List<Integer> setadd= new ArrayList<Integer>();
				setadd.addAll(subset.get(j));
				setadd.add(items.get(i));
				subset.add(setadd);
				j++;
			}
			
			List<Integer> setadd= new ArrayList<Integer>();
			setadd.add(items.get(i));
			subset.add(setadd);
		}
		// the process of judging,X1+X2=X, if X1 and X2 are both not HAUIs,
		// then X will not be a 
		for(int i=0;i<subset.size();i++){
			if(mark==true){
				if(!HAUIMap.containsKey(subset.get(i))&&!HAUIMap.containsKey(subtraction(items, subset.get(i)))){
					temp=false;
					break;
				}
			}else if(!HAUIMap.containsKey(subset.get(i))||!HAUIMap.containsKey(subtraction(items, subset.get(i)))){
				temp=false;
				break;
			}
		}
		
		return temp;
	}
	
/**
 * 
 * @param items
 * @param subitems
 * @return
 */
	protected static List<Integer> subtraction(List<Integer> items, List<Integer> subitems){
		//used for keeping items-subitems
		List<Integer> remainitems= new ArrayList<Integer>();
		//for each item in items
		for(int i=0;i<items.size();i++){
			boolean temp=true;
			//compare with each item in subitems
			for(int j=0;j<subitems.size();j++){
				//items which have appeared in subitems will not copy to remainitems. 
				if(items.get(i)==subitems.get(j)){
					temp=false;
					break;
				}
			}
			if(temp)
				remainitems.add(items.get(i));
		}
		return remainitems;
	}
	
	/**
	 * 
	 * @param levelK_1
	 * @param candidatesHAUUBI
	 * @param candidateHAUIMap
	 * @param mutipleMinUtilities
	 * @param GLMAU
	 * @return
	 */
	protected List<ItemsetTP> generateCandidateSizeK(List<ItemsetTP> levelK_1, ItemsetsTP candidatesHAUUBI, 
				Map<List<Integer>,Integer> candidateHAUIMap, Map<Integer, Integer> mutipleMinUtilities, int GLMAU) {
		
	// For each itemset I1 and I2 of level k-1
	loop1:	for(int i=0; i< levelK_1.size(); i++){
				ItemsetTP itemset1 = levelK_1.get(i);
	loop2:		for(int j=i+1; j< levelK_1.size(); j++){
					ItemsetTP itemset2 = levelK_1.get(j);
			
				// we compare items of itemset1  and itemset2.
				// If they have all the same k-1 items and the last item of itemset1 is smaller than
				// the last item of itemset2, we will combine them to generate a candidate
				for(int k=0; k< itemset1.size()-1; k++){
					if(itemset1.getItems().get(k) != itemset2.get(k))
						continue loop2;
				}
				
				// NOW COMBINE ITEMSET 1 AND ITEMSET 2
				Integer missing = itemset2.get(itemset2.size()-1);
				// Create a new candidate by combining itemset1 and itemset2
				ItemsetTP candidate = new ItemsetTP();
				for(int k=0; k < itemset1.size(); k++){
					candidate.addItem(itemset1.get(k));
				}
				candidate.addItem(missing);
				
				if(!judge(candidate.getItems(), candidateHAUIMap, false)){
					continue loop2;
				}
				
				// create list of common tids
				Set<Integer> tidset = new HashSet<Integer>();
				for(Integer val1 : itemset1.getTIDset()){
					if(itemset2.getTIDset().contains(val1)){
						tidset.add(val1);
					}
				}
				
				// Calculate AUUB of itemset
				// it is defined as the sum of the transaction utility (TU) for the
				// tidset of the itemset
				int AUUB =0;
				for(Integer tid : tidset){
					AUUB += database.getTransactions().get(tid).getTransactionUtility();
				}
		
				// if the average-utility upper-bound (AUUB) is high enough
				if(AUUB >= candidate.getItemsetMau(mutipleMinUtilities, GLMAU)){
					// set its tidset
					candidate.setTIDset(tidset);
					// add it to the set of HAUUBI of size K
					candidatesHAUUBI.addItemset(candidate, candidate.size());
					//add candidate into candidate  map
					candidateHAUIMap.put(candidate.getItems(), candidate.getAUtility());
				}
			}
		}
		// return candidates HAUUBIs of size
		return candidatesHAUUBI.getLevels().get(candidatesHAUUBI.getLevels().size()-1);
}

	/**
	 * Print statistics about the latest algorithm execution to System out.
	 */
	public void printStats() throws IOException {
		System.out.println("=============  HAUIMMAU  ALGORITHM v. 2.15 - STATS =============");
		
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");

		System.out.println(" Max memory: "+MemoryLogger.getInstance().getMaxMemory()+"MB");
		
		System.out.println(" High avergae-utility itemsets count : " + highAUtilityItemsets.getItemsetsCount()); 
		
		System.out.println(" Candidates count : " + candidatesCount); 

		System.out.println("===================================================");
	}
}