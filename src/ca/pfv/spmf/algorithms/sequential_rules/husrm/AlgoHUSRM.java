package ca.pfv.spmf.algorithms.sequential_rules.husrm;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This is the implementation of the HUSRM algorithm that we have submited at MLDM 2015.
 * <br/><br/>
 * Zida, S., Fournier-Viger, P., Wu, C.-W., Lin, J. C. W., Tseng, V.S., (2015). Efficient 
 * Mining of High Utility Sequential Rules. Proc. 11th International Conference on Machine
 *  Learning and Data Mining (MLDM 2015). Springer, LNAI, 15 pages (to appear).
 * <br/>
 * 
 * @see SequenceWithUtility
 * @see SequenceDatabaseWithUtility
 * @author Souleymane Zida and Philippe Fournier-Viger, 2015
 */
public class AlgoHUSRM {
	// for statistics //
	/** start time of latest execution */
	long timeStart = 0; 
	/** end time of latest execution */
	long timeEnd = 0; 
	/**  number of rules generated */
	int ruleCount; 

	// parameters ***/
	/** minimum confidence **/
	double minConfidence;
	
	/** minimum support */
	double minutil;

	/** this is the sequence database */
	SequenceDatabaseWithUtility database;

	/** this buffered writer is used to write the output file */
	BufferedWriter writer = null;
	
	/** this is a map where the KEY is an item and the VALUE is the list of sequences
	/* containing the item. */
	private Map<Integer, ListSequenceIDs> mapItemSequences;
	
	/** this variable is used to activate the debug mode.  When this mode is activated
	/* some additional information about the algorithm will be shown in the console for
	/* debugging **/
	final boolean DEBUG = false;
	
	/** this is a contrainst on the maximum number of item that the left side of a rule should
	/* contain */
	private int maxSizeAntecedent;
	
	/** this is a contrainst on the maximum number of item that the right side of a rule should
	/* contain */
	private int maxSizeConsequent;
	
	////// ================ STRATEGIES ===============================
	// Various strategies have been used to improve the performance of HUSRM.
	// The following boolean values are used to deactivate these strategies.
	
	/** Strategy 1: remove items with a sequence estimated utility < minutil */
	private boolean deactivateStrategy1 = false;  
	
	/** Strategy 2: remove rules contains two items a--> b with a sequence estimated utility < minutil */
	private boolean deactivateStrategy2 = false; 
	
	/** Strategy 3 use bitvectors instead of array list for quickly calculating the support of
	/*  rule antecedent */
	private boolean deactivateStrategy3 = false;  
	
	/** Strategy 4 :  utilize the sum of the utility of lutil, lrutil and rutil
	/* If deactivated, we use the same utility tables, but the upper bound will be calculated as
	/*  lutil + lrutil + rutil instead of the better upper bounds described in the paper */
	private boolean deactivateStrategy4 = false;  
	
	
	
	/**
	 * Default constructor
	 */
	public AlgoHUSRM() {
	}
	
    /**
	 * This is a structure to store some estimated utility and a list of sequence ids.
	 * It will be use in the code for storing the estimated utility of a rule and the list
	 * of sequence ids where the rule appears.
	 */
	public class EstimatedUtilityAndSequences{
		// an estimated profit value
		Double utility = 0d;
		// a list of sequence ids
		List<Integer> sequenceIds = new ArrayList<Integer>();
	}
	


	/**
	 * The main method to run the algorithm
	 * 
	 * @param input an input file
	 * @param output an output file
	 * @param minConfidence the minimum confidence threshold
	 * @param minutil the minimum utility threshold
	 * @param maxConsequentSize a constraint on the maximum number of items that the right side of a rule should contain
	 * @param maxAntecedentSize a constraint on the maximum number of items that the left side of a rule should contain
	 * @param maximumNumberOfSequences the maximum number of sequences to be used
	 * @exception IOException if error reading/writing files
	 */
	//@SuppressWarnings("unused")
	public void runAlgorithm(String input, String output,
			double minConfidence, double minutil, int maxAntecedentSize, int maxConsequentSize, 
			int maximumNumberOfSequences) throws IOException {
		
		// save the minimum confidence parameter
		this.minConfidence = minConfidence;
		
		// save the constraints on the maximum size of left/right side of the rules
		this.maxSizeAntecedent  = maxAntecedentSize;
		this.maxSizeConsequent  = maxConsequentSize;
		
		// reinitialize the number of rules found
		ruleCount = 0;
		this.minutil = minutil;

		// if the database was not loaded, then load it.
		if (database == null) {
			try {
				database = new SequenceDatabaseWithUtility();
				database.loadFile(input, maximumNumberOfSequences);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// if in debug mode, we print the database to the console
		if(DEBUG){
			database.print();
		}
		
		// We reset the tool for calculating the maximum memory usage
		MemoryLogger.getInstance().reset();

		// we prepare the object for writing the output file
		writer = new BufferedWriter(new FileWriter(output));

		// if minutil is 0, set it to 1 to avoid generating
		// all rules 
		this.minutil = minutil;
		if (this.minutil == 0) { 
			this.minutil = 0.001;
		}

		// save the start time
		timeStart = System.currentTimeMillis(); // for stats

		// FIRST STEP: We will calculate the estimated profit of each single item

		// if this strategy has not been deactivated
		if(deactivateStrategy1 == false){
			// This map will store pairs of (key: item   value: estimated profit of the item)
			Map<Integer, Double> mapItemEstimatedUtility = new HashMap<Integer, Double>();
			 
			// We read the database.
			// For each sequence 
			for (SequenceWithUtility sequence : database.getSequences()) {

				// for each itemset in that sequence
				for (List<Integer> itemset : sequence.getItemsets()) {
					
					// for each item
					for (Integer item : itemset) {

						// get the current sequence estimated utility of that item
						Double estimatedUtility = mapItemEstimatedUtility.get(item);						
						
						// if we did not see that item yet
						if  (estimatedUtility == null)    {
							// then its estimated utility of that item until now is the 
							// utility of that sequence
							estimatedUtility = sequence.exactUtility;
							 
						} else {
							// otherwise, it is not the first time that we saw that item
							// so we add the utility of that sequence to the sequence
							// estimated utility f that item
							estimatedUtility = estimatedUtility + sequence.exactUtility;
						}
 				
						// update the estimated utility of that item in the map
						mapItemEstimatedUtility.put(item, estimatedUtility);

					}
				}
			}
	
			// if we are in debug mode, we will print the calculated estimated utility
			// of all items to the console for easy debugging
			if(DEBUG){
				System.out
						.println("==============================================================================");
				System.out
						.println("--------------------ESTIMATED UTILITY OF ITEMS -----------------------------------");
				System.out
						.println("==============================================================================");
				System.out.println(" ");
		
				// for each entry in the map
				for (Entry<Integer, Double> entreeMap : mapItemEstimatedUtility.entrySet()) {
					// we print the item and its estimated utility
					System.out.println("item : " + entreeMap.getKey()
							+ " profit estime: " + entreeMap.getValue());
				}
				
				
				 
		
				// NEXT STEP: WE WILL REMOVE THE UNPROMISING ITEMS
		
				System.out
						.println("==============================================================================");
				System.out
						.println("-------------------ESTIMATED UTILITY OF PROMISING ITEMS      ----------------");
				System.out
						.println("==============================================================================");
			}
			
			
			// we create an iterator to loop over all items
			Iterator<Entry<Integer, Double>> iterator = mapItemEstimatedUtility.entrySet().iterator();
			// for each item
			while (iterator.hasNext()) {
				
				// we obtain the entry in the map
				Map.Entry<java.lang.Integer, java.lang.Double> entryMapItemEstimatedUtility 
					= (Map.Entry<java.lang.Integer, java.lang.Double>) iterator.next();
				Double estimatedUtility = entryMapItemEstimatedUtility.getValue();
				
				// if the estimated utility of the current item is less than minutil
				if (estimatedUtility < minutil) {
					
					// we remove the item from the map
					iterator.remove();
				}
			}
			
			
			// if the debug mode is activated
			if(DEBUG){
				// we will print all the promising items
				
				// we loop over the entries of the map
				for (Entry<Integer, Double> entreeMap : mapItemEstimatedUtility.entrySet()) {
					// we print the item and its estimated utility
					System.out.println("item : " + entreeMap.getKey()
							+ " profit estime: " + entreeMap.getValue());
				}
				
				System.out
						.println("==============================================================================");
				System.out
						.println("-------------- DATABASE WITH ONLY ITEMS HAVING ESTIMATED UTILITY >= miinutil-------------");
				System.out
						.println("==============================================================================");
	
			}

			// NEXT STEP: WE REMOVE UNPROMISING ITEMS FROM THE SEQUENCES
			// (PREVIOUSLY WE HAD ONLY REMOVED THEM FROM THE MAP).
		 
			
			// So we scan the database again.
			// For each sequence
			Iterator<SequenceWithUtility> iteratorSequence = database.getSequences().iterator();
			while (iteratorSequence.hasNext()) {
				SequenceWithUtility sequence = iteratorSequence.next();
				
				//For each itemset
				Iterator<List<Integer>> iteratorItemset = sequence.getItemsets().iterator(); 
				Iterator<List<Double>> iteratorItemsetUtilities = sequence.getUtilities().iterator();
				while (iteratorItemset.hasNext()) {
					// the items in that itemset
					List<java.lang.Integer> itemset = iteratorItemset.next();
					// the utility values in that itemset
					List<java.lang.Double> itemsetUtilities = iteratorItemsetUtilities.next();
					
					// Create an iterator over each item in that itemset
					Iterator<Integer> iteratorItem = itemset.iterator();
					// Create an iterator over utility values in that itemset
					Iterator<Double> iteratorItemUtility = itemsetUtilities.iterator();

					// For each item
					while (iteratorItem.hasNext()) {
						// get the item 
						Integer item = iteratorItem.next();
						// get its utility value
						Double utility = iteratorItemUtility.next();
						 
						// if the item is unpromising
						if(mapItemEstimatedUtility.get(item) == null){
						
							// remove the item
							iteratorItem.remove();
							// remove its utility value
							iteratorItemUtility.remove();
							// subtract the item utility value from the sequence utility.
							sequence.exactUtility -= utility;
						}
					}
					
					// If the itemset has become empty, we remove it from the sequence
					if(itemset.isEmpty()){
						iteratorItemset.remove();
						iteratorItemsetUtilities.remove();
					}
				}
				
				// If the sequence has become empty, we remove the sequences from the database
				if(sequence.size() ==0){
					iteratorSequence.remove();
				}
			}
		}
	
		// if we are in debug mode
		if(DEBUG)
		{
			// print the database without the unpromising items
			database.print();
			
			System.out
					.println("==============================================================================");
			System.out
					.println("----- FOR EACH ITEM, REMEMBER THE IDS OF SEQUENCES CONTAINING THE ITEM  -------");
			System.out
					.println("==============================================================================");

		}

		// We create a map to store for each item, the list of sequences containing the item
		// Key: an item   Value:  the list of sequences containing the item
		mapItemSequences = new HashMap<Integer, ListSequenceIDs>();
		
		// For each sequence
		for (int i=0; i < database.getSequences().size(); i++){
			SequenceWithUtility sequence = database.getSequences().get(i);
			
			// For each itemset
			for (List<Integer> itemset : sequence.getItemsets()) {
				
				// For each item
				for (Integer item : itemset) {
					// Get the list of identifiers of sequence containing that item
					ListSequenceIDs numerosSequenceItem = mapItemSequences.get(item);
					
					// If the list does not exist, we will create it
					if(numerosSequenceItem == null){
						// if the user desactivated strategy 3, we will use an arraylist implementation
						if(deactivateStrategy3){
							numerosSequenceItem = new ListSequenceIDsArrayList();
						}else{
							// otherwise we use a bitvector implementation, which is more efficient
							numerosSequenceItem = new ListSequenceIDsBitVector();
						}
						// we add the list in the map for that item
						mapItemSequences.put(item, numerosSequenceItem);
					}
					// finally we add the current sequence ids to the list of sequences ids of the current
					// item
					numerosSequenceItem.addSequenceID(i);
				}
			}
		}

		// if we are in debug mode
		if(DEBUG){
			// We will print the map which will show the list of sequence identifiers
			// for each item.
			for(Entry<Integer, ListSequenceIDs> entree : mapItemSequences.entrySet()){
				System.out.println("Item : " + entree.getKey() + " Sequences : " + entree.getValue());
			}
			
			System.out
					.println("==============================================================================");
			System.out
					.println("----- CALCULATE SEQUENCE ESTIMATED UTILITY OF EACH RULE OF SIZE 2 -------------");
			System.out
					.println("==============================================================================");
		}
		
		// We create a map of map to store the estimated utility and list of sequences ids for
		// each rule of two items (e.g. a -> b  ).
		// The key of the first map: the item "a" in the left side of the rule
		// The key of the second map:  the item "b" in the right side of the rule
		// The value in the second map:  the estimated utility of the rule and sequence ids for that rule
		Map<Integer,Map<Integer, EstimatedUtilityAndSequences>> mapItemItemEstimatedUtility = new HashMap<Integer,Map<Integer, EstimatedUtilityAndSequences>>();
		
		// For each sequence
		for (int z=0; z < database.getSequences().size(); z++) {
			SequenceWithUtility sequence = database.getSequences().get(z);

			// For each itemset I 
			for (int i=0; i< sequence.getItemsets().size(); i++) {
				
				// get the itemset
				List<Integer> itemset = sequence.getItemsets().get(i);
				
				// For each item  X 
				for (int j=0; j < itemset.size(); j++) {
					Integer itemX = itemset.get(j);
					// SI X N'A PAS DEJA ETE VU

					// For each item Y occuring after X,
					// that is in the itemsets I+1, I+2 .... 
					for (int k=i+1; k < sequence.getItemsets().size(); k++) {
						//  for a given itemset K
						List<Integer> itemsetK = sequence.getItemsets().get(k);
						// for an item Y
						for(Integer itemY : itemsetK){
		
							// We will update the estimated profit of the rule X --> Y 
							// by adding the sequence utility of that sequence to the 
							// sequence estimated utility of that rule
							
							// Get the map for item X
							Map<Integer, EstimatedUtilityAndSequences> mapXItemUtility = mapItemItemEstimatedUtility.get(itemX);
							
							// If we never saw X before
							if(mapXItemUtility == null){
								// we create a map for X
								mapXItemUtility = new HashMap<Integer, EstimatedUtilityAndSequences>();
								mapItemItemEstimatedUtility.put(itemX, mapXItemUtility);
								
								// Then we create a structure for storing the estimated utility of X ->Y
								EstimatedUtilityAndSequences structure = new EstimatedUtilityAndSequences();
								structure.utility = sequence.exactUtility; // the current sequence utility
								structure.sequenceIds.add(z); // the sequence id
								// add it in the map for X -> Y
								mapXItemUtility.put(itemY, structure);
							}
							else{
								// in the case were we saw X before.
								// We get its structure for storing the estimated utility of X -> Y
								EstimatedUtilityAndSequences structure = mapXItemUtility.get(itemY);
								// if we never saw X ->Y
								if(structure == null){

									// Then we create a structure for storing the estimated utility of X ->Y
									 structure = new EstimatedUtilityAndSequences(); 
									structure.utility = sequence.exactUtility; // the current sequence utility
									structure.sequenceIds.add(z); // the sequence id

									// add it in the map for X -> Y
									mapXItemUtility.put(itemY, structure);
								}else{
									// if we saw X -> Y before
									// We add the sequence utility to the utility of that rule
									structure.utility = structure.utility + sequence.exactUtility;
									// We add the sequence ids to the list of sequence ids of that rule.
									structure.sequenceIds.add(z);
								}
							}	
							
						}	
					}
				}
			}
		}
		
		// if in debuging mode
		if(DEBUG){
			// we will print the estimated utility and list of sequences ids of all rules containing two items
			// e.g.   "a" -> "b"
			
			// for each item X
			for(Entry<Integer, Map<Integer, EstimatedUtilityAndSequences>> entreeX : mapItemItemEstimatedUtility.entrySet()){
				int itemX = entreeX.getKey();
				
				// for each item Y 
				for(Entry<Integer, EstimatedUtilityAndSequences> entreeYProfit : entreeX.getValue().entrySet()){
					int itemY = entreeYProfit.getKey();
					EstimatedUtilityAndSequences structureXY = entreeYProfit.getValue();
					
					// Print the rule X ->Y  with its estimated utility and list of sequence ids.
					System.out.println("  RULE: " + itemX + " --> " + itemY + "   estimated utility " + structureXY.utility
							+ "   sequences " + structureXY.sequenceIds);
				}
			}
			
			
			System.out
			.println("==============================================================================");
			System.out
			.println("-------------- RULES OF SIZE 2 WITH ESTIMATED UTILITY >= minutil -------------");
			System.out
			.println("==============================================================================");
		}

		// For each entry in the map
		for(Entry<Integer, Map<Integer, EstimatedUtilityAndSequences>> mapI : mapItemItemEstimatedUtility.entrySet()){
		
			// An entry represents an item "i" (the key) and some maps (value)
			// We will loop over the entries of the secondary map of "i" (value)
			// to remove all rules of the form i -> j where the estimated utility
			// is lower than minutil
			
			// Create an iterator
			Iterator<Entry<Integer, EstimatedUtilityAndSequences>> iterEntry = 	mapI.getValue().entrySet().iterator();
			
			// loop over the map
			while (iterEntry.hasNext()) {
				// We consider each entry j and the estimated utility of i-> j
				Map.Entry<java.lang.Integer, EstimatedUtilityAndSequences> entry = (Map.Entry<java.lang.Integer, EstimatedUtilityAndSequences>) iterEntry
						.next();
				// if the estimated profit of i -> j is lower than minutil
				// we remove that rule because no larger rule containing that rule
				// can have a estimated utility higher or equal to minutil.
				if(entry.getValue().utility < minutil){
					// we only do that if the user did not deactivate strategy 2
					if(deactivateStrategy2 == false){
						iterEntry.remove();
					}
				}
				
			}
		}
		
		// If in debug mode
		if(DEBUG){
			// We will print the remaining rules
			
			// we will print the estimated utility and list of sequences ids of all rules containing two items
			// e.g.   "a" -> "b"

			// for each item X
			for(Entry<Integer, Map<Integer, EstimatedUtilityAndSequences>> entreeX : mapItemItemEstimatedUtility.entrySet()){
				int itemX = entreeX.getKey();

				// for each item Y
				for(Entry<Integer, EstimatedUtilityAndSequences> entreeYProfit : entreeX.getValue().entrySet()){
					int itemY = entreeYProfit.getKey();

					// Print the rule X ->Y  with its estimated utility and list of sequence ids.
					EstimatedUtilityAndSequences structureXY = entreeYProfit.getValue();
					System.out.println("  REGLE: " + itemX + " --> " + itemY + "   profit estime " + structureXY.utility
							+ "   sequences " + structureXY.sequenceIds);
				}
			}
			
			System.out
			.println("==============================================================================");
			System.out
			.println("-------------- RULES OF SIZE 2 WITH UTILITY >= minutil -------------");
			System.out
			.println("==============================================================================");
		}

	// For each rule X --> Y
	for(Entry<Integer, Map<Integer, EstimatedUtilityAndSequences>> entryX : mapItemItemEstimatedUtility.entrySet()){
		// Get the item X
		Integer itemX = entryX.getKey();
		
		// Get the list of sequence ids containing the item X
		ListSequenceIDs sequenceIDsX =  mapItemSequences.get(itemX);
		// Get the support of item X
		double supportX = sequenceIDsX.getSize();
		
		// For each Y
		for(Entry<Integer, EstimatedUtilityAndSequences> entryYUtility : entryX.getValue().entrySet()){
			Integer itemY = entryYUtility.getKey();
			
			// Get the estimated utility and list of sequences ids for the rule X -> Y
			EstimatedUtilityAndSequences structure = entryYUtility.getValue();
			List<Integer> sequencesIDsXY = structure.sequenceIds;
			
			// Get the support of the rule X ->Y
			double supportXY = sequencesIDsXY.size();
			
			// We create the utility table of rule X -�> Y
			UtilityTable table = new UtilityTable();
			
			// We will scan each sequence to fill the utility table 
			// and update the other variable to calculate the confidence of the rule.
			
			// for each sequence containing X -> Y
			for(Integer numeroSequence : sequencesIDsXY){
				// Get the sequence
				SequenceWithUtility sequence = database.getSequences().get(numeroSequence);

				// Create a new element in the table
				ElementOfTable element = new ElementOfTable(numeroSequence);
				
				// we reset position alpha and beta
				int positionAlphaItem =-1;
				int positionBetaItem =-1;

				// (1) We will scan the sequence from left to right to find X
				// and stop at the first position ALPHA where X has been seen completely.
				// At the same time, we will add the utility of items in X.
				
				// For each itemset I 
	loop1:		for (int i=0; i< sequence.getItemsets().size(); i++) {
					// get the itemset I
					List<Integer> itemset = sequence.getItemsets().get(i);
					
					// For each item J
					for (int j=0; j < itemset.size(); j++) {
						Integer itemIJ = itemset.get(j);
						
						// if we found the item X
						if(itemX.equals(itemIJ)){
							// we get its utility
							double utilityXPositionIJ = sequence.getUtilities().get(i).get(j);
							// we add it to the exact utility in the current utility table element
							element.utility += utilityXPositionIJ;
							
							// Stop and remember that position
							element.positionAlphaItemset = i;
							// remember the position ALPHA (which in this case means where the item in 
							// the right side
							// of a rule was found)
							positionAlphaItem = j;
							
							// since we found j, we don't need to continue this loop since we assume
							// that an item do not occur more than once per sequence
							break loop1;
						}else if(itemIJ > itemX){
							// If the item is greater than the item X,
							// we add the profit of this item to the "lutil" value of its element.
							double profitXPositionIJ = sequence.getUtilities().get(i).get(j);
							element.utilityLeft += profitXPositionIJ;
						}
					}
				}
	 			
				// If X does not appear, we don't do the following steps
				if(element.positionAlphaItemset == -1){
					continue;
				}

				// (2) Now we will scan the sequence from right to left to find
				//  Y and stop if we find it. That position where we find it will be called beta.
				// At the same time as we scan the sequence, we will add the utility of items in Y
				
				// for each itemset starting from the last one until itemset alpha+1
	loop2:	for (int i = sequence.getItemsets().size()-1; 
					i >  element.positionAlphaItemset ; i--) {
					// get the current itemset
					List<Integer> itemset = sequence.getItemsets().get(i);

					// for each item J in that itemset
					for (int j = itemset.size() -1; j >= 0; j--) {
						// get the item J
						Integer itemIJ = itemset.get(j);

						// if that item is Y
						if(itemY.equals(itemIJ))
						 {		 
							// we add Y's profit to the exact utility of the current element
							double profitYPositionIJ = sequence.getUtilities().get(i).get(j);
							element.utility += profitYPositionIJ;
							
							// we stop and remember that we stopped at the i-th itemset
							// we will call this position "beta".
							element.positionBetaItemset = i;
							positionBetaItem= j;

							break loop2;
						 }else if(itemIJ > itemY){
							// If the item is greater than the item Y,
								// we add the profit of this item to the "rutil" value of its element.
							double profitXPositionIJ = sequence.getUtilities().get(i).get(j);
							element.utilityRight += profitXPositionIJ;
						}
					}
				}
				/// If Y does not appear, we don't do the following steps
				 if(element.positionBetaItemset == -1){
					 continue;
				 } 

				 // (3) THIRD STEP:  WE WILL SCAN THE SEQUENCE BETWEEN THE ALPHA
				 // AND BETA POSITIONS WHERE WE HAVE STOPPED TO CALCUlATE THE "LRUTIL" VALUE
				 // FOR X ->Y in that SEQUENCE
		
				 // (A) WE SCAN THE ALPHA ITEMSET
					List<Integer> itemsetAlpha = sequence.getItemsets().get(element.positionAlphaItemset);
					// FOR EACH ITEM J IN THE ALPHA ITEMSET
					for (int j = positionAlphaItem+1; j < itemsetAlpha.size(); j++) {

						// we add the utility of the item to the "LUTIL" value of the current element.
						double profitPositionIJ = sequence.getUtilities().get(element.positionAlphaItemset).get(j);
						element.utilityLeft += profitPositionIJ;
					}

				 
				// (B) Scan the other itemsets after the alpha itemset but before the beta itemset
				for (int i = element.positionAlphaItemset+1; i < element.positionBetaItemset; i++) {
						// get the itemset
						List<Integer> itemset = sequence.getItemsets().get(i);

						// For each item J
						for (int j=0; j < itemset.size(); j++) {
							Integer itemIJ = itemset.get(j);

							// if the item is greater than X and Y
							if( itemIJ > itemX && itemIJ > itemY ){
								// it means that this item could be used to extend the left or right side
								// of the rule
								// We add its utility to "LRUTIL"
								double utilityPositionIJ = sequence.getUtilities().get(i).get(j);
								element.utilityLeftRight += utilityPositionIJ;
							}else if( itemIJ > itemX){
								// if the item is only greater than X
								// We add its utility to "RUTIL"
								double utilityPositionIJ = sequence.getUtilities().get(i).get(j);
								element.utilityLeft += utilityPositionIJ;
							}else if( itemIJ > itemY){
								// if the item is only greater than Y
								// We add its utility to "RUTIL"
								double utilityPositionIJ = sequence.getUtilities().get(i).get(j);
								element.utilityRight += utilityPositionIJ;
							}
						}
				 }
				
				// (c) Scan item in the itemset BETA after the item beta (i.e. the item Y)
				List<Integer> itemset = sequence.getItemsets().get(element.positionBetaItemset);
				
				// For each item J after the beta item (i.e. the item Y)
				for (int j=0; j < positionBetaItem - 1; j++) {
					Integer itemIJ = itemset.get(j);

					// if the item is greater than Y
					if( itemIJ > itemY){
						// We add its utility to "RUTIL"
						double profitPositionIJ = sequence.getUtilities().get(element.positionBetaItemset).get(j);
						element.utilityRight += profitPositionIJ;
					}
				}

				// Finally, we add the element of this sequence to the utility table of X->Y
				table.addElement(element);

		}
			
			// We calculate the confidence of X -> Y
			double confidence = (supportXY / supportX);
			
			double conditionExpandLeft;
			double conditionExpandRight;

			// if strategy 4 is deactivated
			// we use a worse upper bound
			if(deactivateStrategy4){
				conditionExpandLeft = table.totalUtility + table.totalUtilityLeft + table.totalUtilityLeftRight
						+ table.totalUtilityRight;
				 conditionExpandRight = conditionExpandLeft;
			}else{
				// otherwise we use a better upper bound
				conditionExpandLeft = table.totalUtility + table.totalUtilityLeft + table.totalUtilityLeftRight;
				 conditionExpandRight = table.totalUtility + table.totalUtilityRight + table.totalUtilityLeftRight
						 + table.totalUtilityLeft;
			}
			
			
			// if in debug mode
			if(DEBUG){
				//We will print the rule and its profit and whether it is a high utility rule or not
				String isInteresting = (table.totalUtility >= minutil) ? " *** HIGH UTILITY RULE! ***" : " ";
				System.out.println("\n  RULE: " + itemX + " --> " + itemY + "   utility " + table.totalUtility
						+ " frequence : " + supportXY 
						+ " confiance : " + confidence + isInteresting);
				
				// we will print the utility table of the rule
				for(ElementOfTable element : table.elements){
					System.out.println("      SEQ:" + element.numeroSequence + " \t utility: " + element.utility
							+ " \t lutil: " + element.utilityLeft
							+ " \t lrutil: " + element.utilityLeftRight + " \t rutil: " + element.utilityRight
							+ " alpha : " + element.positionAlphaItemset 
							+ " beta : " + element.positionBetaItemset);
				}
	
				System.out.println("      TOTAL: " + " \t utility: " + table.totalUtility + " \t lutil: " + table.totalUtilityLeft
						+ " \t lrutil: " + table.totalUtilityLeftRight + " \t rutil: " + table.totalUtilityRight);
							System.out.println("      Should we explore larger rules by left expansions ? " + (conditionExpandLeft >= minutil)
						+ " (" + conditionExpandLeft + " )");
				System.out.println("       Should we explore larger rules by right expansions ? " + (conditionExpandRight >= minutil)
						+ " (" + conditionExpandRight + " )");
			}

			// create the rule antecedent and consequence
			int [] antecedent =  new int[]{itemX};
			int []  consequent =  new int[]{itemY};
			
			// if high utility with ENOUGH  confidence
			if((table.totalUtility >= minutil) && confidence >= minConfidence){
				// we output the rule
				saveRule(antecedent, consequent, table.totalUtility, supportXY, confidence);
			}
			
			// if the right side size is less than the maximum size, we will try to expand the rule
			if(conditionExpandRight >= minutil && maxConsequentSize > 1)
			{
				expandRight(table, antecedent, consequent, sequenceIDsX);
			}
			
			// if the left side size is less than the maximum size, we will try to expand the rule
			if(conditionExpandLeft >= minutil  && maxAntecedentSize > 1)
			{
				expandFirstLeft(table, antecedent, consequent, sequenceIDsX);
			}
		}
	}
		

		//We will check the current memory usage
		MemoryLogger.getInstance().checkMemory();

		// save end time
		timeEnd = System.currentTimeMillis();

		// close the file
		writer.close();

		// after the algorithm ends, we don't need a reference to the database
		// anymore.
		database = null;
	}

	/**
	 * This method save a rule to the output file
	 * @param antecedent the left side of the rule
	 * @param consequent the right side of the rule
	 * @param utility the rule utility
	 * @param support the rule support
	 * @param confidence the rule confidence
	 * @throws IOException if an error occurs when writing to file
	 */
	private void saveRule(int[] antecedent, int[] consequent,
			double utility, double support, double confidence) throws IOException {

		// increase the number of rule found
		ruleCount++;

		// create a string buffer
		StringBuilder buffer = new StringBuilder();

		// write the left side of the rule (the antecedent)
		for (int i = 0; i < antecedent.length; i++) {
			buffer.append(antecedent[i]);
			if (i != antecedent.length - 1) {
				buffer.append(",");
			}
		}

		// write separator
		buffer.append("	==> ");

		// write the right side of the rule (the consequent)
		for (int i = 0; i < consequent.length; i++) {
			buffer.append(consequent[i]);
			if (i != consequent.length - 1) {
				buffer.append(",");
			}
		}
		// write support
		buffer.append("\t#SUP: ");
		buffer.append(support);
		// write confidence
		buffer.append("\t#CONF: ");
		buffer.append(confidence);
		buffer.append("\t#UTIL: ");
		buffer.append(utility);
		writer.write(buffer.toString());
		writer.newLine();
		
		//if we are in debug mode, we will automatically check that the utility, confidence and support
		// are correct to ensure that there is no bug.
		if(DEBUG){
			//We will check if the rule utility support and confidence is ok
			checkMeasuresForARule( antecedent,consequent, utility, support,  confidence);
			
		}
	}

	/**
	 * This method is used for debugging. It scan a database to check if the measures
	 * (confidence, utility, support) of a given rule have been correctly calculated.
	 * @param antecedent the left isde
	 * @param antecedent the left side of the rule
	 * @param consequent the right side of the rule
	 * @param utility the rule utility
	 * @param support the rule support
	 * @param confidence the rule confidence
	 */
	private void checkMeasuresForARule(int[] antecedent, int[] consequent,
			double utility, double support, double confidence) {
		
		// We will calculate again the utility, support and confidence by 
		// scanning the database.
		double supportOfAntecedent = 0;
		double supportOfTheRule = 0;
		double utilityOfTheRule = 0;
		
		// for each sequence
		for(SequenceWithUtility sequence : database.getSequences()){
			
			// Count the number of items already seen from the antecedent in that sequence
			int numberOfAntecedentItemsAlreadySeen =0;
			
			double ruleUtilityInSequence = 0;

			//=========================================
			// For each itemset in that sequence
			int i = 0;
loop1:	for(; i < sequence.getItemsets().size(); i++){
				List<Integer> itemset = sequence.getItemsets().get(i);
				
				// For each item
				for(int j =0; j < itemset.size(); j++){
					Integer item = itemset.get(j);
					
					// if the item appear in the left side of a rule
					if(Arrays.binarySearch(antecedent, item) >= 0 ){
						// add the profit of that item to the rule utility
						double utilityItem = sequence.getUtilities().get(i).get(j);
						ruleUtilityInSequence += utilityItem;
						
						// increase the number of items from the antecedent that we have seen 
						numberOfAntecedentItemsAlreadySeen++;
						
						// if we have completely found the antecedent X
						if(numberOfAntecedentItemsAlreadySeen  == antecedent.length){
							// increase the support of the antecedent
							supportOfAntecedent++;
							// and stop searching for items in the antecedent
							break loop1;
						}
						
					}
				}
			}
				
			//=========================================
			// Now we will search for the consequent of the rule
			// starting from the next itemset in that sequence
			i++;
			
			// This variable will count the number of items of the consequent
			// that we have already seen
			int numberOfConsequentItemsAlreadySeen =0;

			
			// for each itemset after the antecedent
	boucle2:for(; i < sequence.getItemsets().size(); i++){
				List<Integer> itemset = sequence.getItemsets().get(i);
				
				// for each item
				for(int j =0; j < itemset.size(); j++){
					Integer item = itemset.get(j);
					
					// if the item appear in the consequent of the rule
					if(Arrays.binarySearch(consequent, item) >= 0 ){
						// add the utility of that item
						double utilityItem = sequence.getUtilities().get(i).get(j);
						ruleUtilityInSequence += utilityItem;

						// increase the number of items from the consequent that we have seen 
						numberOfConsequentItemsAlreadySeen++;
						
						// if we have completely found the consequent Y 
						if(numberOfConsequentItemsAlreadySeen  == consequent.length){
							// increase the support of the rule
							supportOfTheRule++;
							// increase the global utility of the rule in the database
							utilityOfTheRule +=  ruleUtilityInSequence;
							// and stop searching for items in the antecedent
							break boucle2;
						}
						
					}
				}
			}		
		}
			
		// We now check if the support is the same as the support calculated by HUSRM
		if(support != supportOfTheRule){
			throw new RuntimeException(" The support is incorrect for the rule : "
					+ Arrays.toString(antecedent) + " ==>" + Arrays.toString(consequent)
					+ "   support : " + support + " recalculated support: " + supportOfTheRule);
		}
		
		// We now check  if the confidence is the same as the confidence calculated by HUSRM
		double recalculatedConfidence = supportOfTheRule / supportOfAntecedent;
		
		if(confidence != recalculatedConfidence){
			throw new RuntimeException(" The confidence is incorrect for the rule :"
					+ Arrays.toString(antecedent) + " ==>" + Arrays.toString(consequent)
					+ "   confidence : " + confidence + " recalculated confidence: " + recalculatedConfidence);
		}

		// We now check  if the utility is the same as the utility calculated by HUSRM
		if(utility != utilityOfTheRule){
			throw new RuntimeException(" The utility is incorrect for the rule :"
					+ Arrays.toString(antecedent) + " ==>" + Arrays.toString(consequent)
					+ "   utility : " + utility + " recalculated utility " + utilityOfTheRule);
		}
	}

	/**
	 * This method is used to create new rule(s) by adding items to the right side of a rule
	 * @param table the utility-table of the rule
	 * @param antecedent the rule antecedent
	 * @param consequent the rule consequent
	 * @param sequenceIdsAntecedent the list of ids of sequences containing the left side of the rule
	 * @throws IOException if an error occurs while writing to file
	 */
	private void expandRight(UtilityTable table, int[] antecedent,
			int[] consequent, ListSequenceIDs sequenceIdsAntecedent) throws IOException {
		
		// We first find the largest item in the left side and right side of the rule
		int largestItemInAntecedent = antecedent[antecedent.length -1];
		int largestItemInConsequent = consequent[consequent.length -1];
		
		// We create a new map where we will build the utility table for the new rules that
		// will be created by adding an item to the current rule.
		// Key: an item appended to the rule     Value: the utility-table of the corresponding new rule
		Map<Integer, UtilityTable> mapItemsTables = new HashMap<Integer, UtilityTable>();

//		// for each sequence containing the original rule (according to its utility table)
		for(ElementOfTable element : table.elements){
			
			// Optimisation: if the "rutil" is 0 for that rule in that sequence,
			// we do not need to scan this sequence.
			if(element.utilityLeft + element.utilityRight + element.utilityLeftRight  == 0){
				continue;
			}
			
			// Get the sequence
			SequenceWithUtility sequence = database.getSequences().get(element.numeroSequence);
			
			//============================================================
			// Case 1: for each itemset in BETA or AFTER BETA.....
			
			// For each itemset after beta:
			for(int i = element.positionBetaItemset; i < sequence.size(); i++){
				// get the itemset
				List<Integer> itemsetI = sequence.getItemsets().get(i);
				
				// For each item
				for(int j=0; j < itemsetI.size(); j++){
					Integer itemJ = itemsetI.get(j);
				
					// Check if the item is greater than items in the consequent of the rule 
					// according to the lexicographical order 
					if(itemJ <= largestItemInConsequent){
						// if not, then we continue because that item cannot be added to the rule
						continue;
					}
					
					// ======= Otherwise, we need to update the utility table of the item ====================
					
					// Get the utility table of the item
					UtilityTable tableItemJ = mapItemsTables.get(itemJ);
					if(tableItemJ == null){
						// if no utility table, we create one
						tableItemJ = new UtilityTable();
						mapItemsTables.put(itemJ, tableItemJ);
					}
					
					//==========

					// We will add a new element (line) in the utility table
					ElementOfTable newElement = new ElementOfTable(element.numeroSequence);
  
					// We will update the utility by adding the utility of item J
					double profitItemJ = sequence.getUtilities().get(i).get(j);
					newElement.utility = element.utility + profitItemJ;
					
					// we will copy the "lutil" value from the original rule
					newElement.utilityLeft = element.utilityLeft;
					
					// we will copy the "lrutil" value from the original rule
					newElement.utilityLeftRight = element.utilityLeftRight;
					
					// we will copy the "rutil" value from the original rule
					// but we will subtract the utility of the item J
					newElement.utilityRight = element.utilityRight - profitItemJ;

					// we will copy the position of Alpha and Beta in that sequences because it
					// does not change
					newElement.positionBetaItemset = element.positionBetaItemset; 
					newElement.positionAlphaItemset = element.positionAlphaItemset; 
					
					// Then, we will scan itemsets after the beta position in the sequence
					// We will subtract the utility of items that are smaller than item J 
					// according to the lexicographical order from "rutil" because they
					// cannot be added anymore to the new rule.
					
					// for each such itemset
					for(int z = element.positionBetaItemset; z < sequence.size(); z++){
						List<Integer> itemsetZ = sequence.getItemsets().get(z);
						
						// for each item W
						for(int w= itemsetZ.size()-1; w >= 0 ; w--){
							// Optimisation: 
							// if the item is smaller than the larger item in the right side of the rule
							Integer itemW = itemsetZ.get(w);
							if(itemW <= largestItemInConsequent){
								// we break;
								break; 
							}
			
							// otherwise, if item W is smaller than item J
							 if(itemW < itemJ ){

								// We will subtract the utility of W from "rutil"
								double profitItemW = sequence.getUtilities().get(z).get(w);
								newElement.utilityRight -= profitItemW;
							}
						}
					}
					// end
					
			
					// Now that we have created the element for that sequence and that new rule
					// , we will add the utility table of that new rule
					tableItemJ.addElement(newElement);
				}
			}
			
			//============================================================
			// CAS 2 : For each itemset from itemset BETA - 1 to itemset ALPHA + 1
			// in the sequence
			//.....
			// For each itemset before the BETA itemset, we will scan the sequence
			
			// We will look here for the case where an item J is added to the right side of a rule
			// but it is an item found between the left side and right side of the rule in the sequence.
			// In that case, the position beta will change to a new position that we will call beta prime.
			
			// These two variable will be used to sum the utility of lrutil and lutil
			// after beta has changed
			int sumUtilityLeftRightUntilBetaPrime = 0;
			int sumUtilityLeftUntilBetaPrime = 0;
			
			// For each itemset from itemset BETA - 1 to itemset ALPHA + 1
			for(int i = element.positionBetaItemset - 1; i > element.positionAlphaItemset; i--){
				// Get the itemset
				List<Integer> itemsetI = sequence.getItemsets().get(i);
				
				// Get the item
				for(int j=0; j < itemsetI.size(); j++){
					Integer itemJ = itemsetI.get(j);
				
					//Check if the item could be added to the left side, 
					// right side, or left and right side of the rule according to the lexicographical order
					boolean isLeft = itemJ > largestItemInAntecedent && itemJ < largestItemInConsequent;
					boolean isLeftRight = itemJ > largestItemInAntecedent && itemJ > largestItemInConsequent;
					boolean isRight = itemJ > largestItemInConsequent && itemJ < largestItemInAntecedent;
					
					// if the item can only be added to left side
					if(isLeft){
						// add the utility of that item to the "lutil"
						double profitItemJ = sequence.getUtilities().get(i).get(j);
						sumUtilityLeftUntilBetaPrime += profitItemJ;
						
					}else if(isRight){
						// if the item can only be added to right side
						//===========
						// We will need to update the utility table of the new rule
						// that could be generated with that item
						// Get the utility table
						UtilityTable tableItemJ = mapItemsTables.get(itemJ);
						if(tableItemJ == null){
							// if it does not exist, create a new utility table
							tableItemJ = new UtilityTable();
							mapItemsTables.put(itemJ, tableItemJ);
						}

						// Create a new element (line) in the utility table for that sequence
						ElementOfTable newElement = new ElementOfTable(element.numeroSequence);
 
						//  Add the utility of the item to the utility of the new rule
						double profitItemJ = sequence.getUtilities().get(i).get(j);
						newElement.utility = element.utility + profitItemJ;
					
						// Set the "lutil" value for the new rule
						// which is the utility of the left side of the original rule minus
						// the utility of items that could be append to left side until the current itemset
						newElement.utilityLeft = element.utilityLeft - sumUtilityLeftUntilBetaPrime;
						
						// Set the "rutil" value similarly
						newElement.utilityLeftRight = element.utilityLeftRight - sumUtilityLeftRightUntilBetaPrime;
	
						// Now we will scan the sequence from position beta prime and after
						// to calculate:
						// 1) the utility of all items D that are smaller than item J in beta prime
						// or after and can be added to the right side of the rule
						int sumUtilityRUtilItemsSmallerThanX = 0;
						// 2) the utility of all items D that are smaller than item J  in beta prime
						// or afters and can be added to the left or right side of the rule
						int sumUtilityLRUtilItemsSmallerThanX = 0;
						
						// for each such itemset
						for(int z = i; z < element.positionBetaItemset; z++){
							List<Integer> itemsetZ = sequence.getItemsets().get(z);
							
							// for each item W
							for(int w=0; w < itemsetZ.size(); w++){
								Integer itemW = itemsetZ.get(w);
								
								// check if the item can be appended to the left or right side of the rule
								boolean wIsLeftRight = itemW > largestItemInAntecedent && itemW > largestItemInConsequent;
								// check if the item can only be appended to the right side of the rule
								boolean wIsRight = itemW > largestItemInConsequent && itemW < largestItemInAntecedent;
								
								// if the item can only be appended to the right side of the original rule
								// but is smaller than item W that is appended to the right side of the
								// new rule
								if(wIsRight && itemW < itemJ){
									// We will add its profit to the sum for RUtil
									double profitItemW = sequence.getUtilities().get(z).get(w);
									sumUtilityRUtilItemsSmallerThanX += profitItemW;
								}else if(wIsLeftRight && itemW > itemJ){
									// If it is an item that can be appended to the left or right side of
									// the original rule and is greater than the current item J
									// we will add it to the sum for LRUtil
									double profitItemW = sequence.getUtilities().get(z).get(w);
									sumUtilityLRUtilItemsSmallerThanX += profitItemW;
								}
							}
						}
						// Then we will update the RUTIL for the new rule as follows:
						newElement.utilityRight = element.utilityRight - profitItemJ 
								+ sumUtilityLRUtilItemsSmallerThanX - sumUtilityRUtilItemsSmallerThanX;
	
						// We will update the position of Beta and alpha in the sequence
						newElement.positionBetaItemset = i; 
						newElement.positionAlphaItemset = element.positionAlphaItemset; 

						// We have finished creating the element for that sequence for the new rule
						// so we will add it to the utility table
						tableItemJ.addElement(newElement);
						//===========
						
					}else if(isLeftRight){
						// If the item can be added to the left or right side of the rule
						//===========
						// ======= We will update the utility table the new rule with this item on the
						//  right side ====================
						// Get the table
						UtilityTable tableItemJ = mapItemsTables.get(itemJ);
						if(tableItemJ == null){
							// if it does not exist, create a new utility table
							tableItemJ = new UtilityTable();
							mapItemsTables.put(itemJ, tableItemJ);
						}

						// Create a new element (line) in the table
						ElementOfTable newElement = new ElementOfTable(element.numeroSequence); 
 
						// Copy the utility of the original rule and add the utility of the item
						// in the current sequence.
						double profitItemJ = sequence.getUtilities().get(i).get(j);
						newElement.utility = element.utility + profitItemJ;
					
						// Set the lutil value as the lutil of the original rule
						// minus the utility of items until the beta prime itemset that could
						// be appended only on the left side of the rule
						newElement.utilityLeft = element.utilityLeft - sumUtilityLeftUntilBetaPrime;
						
						// Set the lrutil value as the lrutil of the original rule
						// minus the utility of items until the beta prime itemset that could
						// be appended  on the right or left side of the rule
						newElement.utilityLeftRight = element.utilityLeftRight - profitItemJ - sumUtilityLeftRightUntilBetaPrime;
						
						// We will scan the beta prime itemset and the following itemsets
						// to calculate
						// 1) the profit of all items that can be added on the right side of the rule
						//  which are smaller than J in the beta prime itemset, or appear in a following
						// itemset
						
						// 
						int sumUtilityRigthItemSmallerThanX = 0;
						
						// For each itemset 
						for(int z = i; z < element.positionBetaItemset; z++){
							List<Integer> itemsetZ = sequence.getItemsets().get(z);
							
							//for each item W in that itemset
							for(int w=0; w < itemsetZ.size(); w++){
								// If w is greater than J according to the lexicographical
								// order, we skip it because we are not interested here.
								Integer itemW = itemsetZ.get(w);
								if(itemW > itemJ){
									break;  // optimisatin car itemset est trie
								}
								// Otherwise, we check if the item could be append on the right side
								// but not on the left side
								boolean wEstD = itemW > largestItemInConsequent && itemW < largestItemInAntecedent;
								
								// IF it is the case
								if(wEstD){
									// then we add the sum of the utility of item W in our
									// temporary variable
									double profitItemW = sequence.getUtilities().get(z).get(w);
									sumUtilityRigthItemSmallerThanX += profitItemW;
								}
							}
						}
						
						// After that we have the informatoin to update the "RUTIL" value.
						// It is the "rutil" of the original rule minus the content of the temporary
						// variable that we calculated above
						newElement.utilityRight = element.utilityRight - sumUtilityRigthItemSmallerThanX;
	
						// The first itemset of the right side of the rule has now changed.
						// We thus set beta to the new value "i"
						newElement.positionBetaItemset = i; 
						// The left side of the rule has not changed, so Alpha stay the same.
						newElement.positionAlphaItemset = element.positionAlphaItemset; 

						// Finally, we add the element that we just created to the utility-table
						// of the new rule.
						tableItemJ.addElement(newElement);
						//===========
					}

				}
			}
			
		}

		// For each new rule
		for(Entry<Integer, UtilityTable> entryItemTable :  mapItemsTables.entrySet()){
			// We get the item and its utility table
			Integer item = entryItemTable.getKey();
			UtilityTable utilityTable = entryItemTable.getValue();
			
			// We check if we should try to expand its left side
			boolean shouldExpandLeftSide;
			// We check if we should try to expand its right side
			boolean shouldExpandRightSide;
					
			// If the user deactivate strategy 4, we use a worst upper bound to check that
			if(deactivateStrategy4){
				shouldExpandLeftSide = utilityTable.totalUtility + utilityTable.totalUtilityLeft
						+ utilityTable.totalUtilityLeftRight + utilityTable.totalUtilityRight >= minutil
								&& antecedent.length+1 < maxSizeAntecedent;
				shouldExpandRightSide = utilityTable.totalUtility + utilityTable.totalUtilityRight
						+ utilityTable.totalUtilityLeftRight + utilityTable.totalUtilityLeft >= minutil
								&& consequent.length+1 < maxSizeConsequent;
			}else{
				// Otherwise, we use the best upper bound.
				shouldExpandLeftSide = utilityTable.totalUtility + utilityTable.totalUtilityLeft
						+ utilityTable.totalUtilityLeftRight >= minutil
								&& antecedent.length+1 < maxSizeAntecedent;
				shouldExpandRightSide = utilityTable.totalUtility + utilityTable.totalUtilityRight
						+ utilityTable.totalUtilityLeftRight + utilityTable.totalUtilityLeft>= minutil
								&& consequent.length+1 < maxSizeConsequent;
				
			}
					
			// check if the rule is high utility
			boolean isHighUtility = utilityTable.totalUtility >= minutil;
			
			// We create the consequent for the new rule by appending the new item
			int [] newConsequent= new int[consequent.length+1];
			System.arraycopy(consequent, 0, newConsequent, 0, consequent.length);
			newConsequent[consequent.length] = item;
			
			// We calculate the confidence
			double confidence =  (double) utilityTable.elements.size() / (double) sequenceIdsAntecedent.getSize();
			
			// If the rule is high utility and high confidence
			if(isHighUtility && confidence >= minConfidence){
				// We save the rule to file
				saveRule(antecedent, newConsequent, utilityTable.totalUtility, utilityTable.elements.size() , confidence);
				
				// If we are in debugging mode, we will show the rule in the console
				if(DEBUG){
					System.out.println("\n\t  HIGH UTILITY SEQ. RULE: " + Arrays.toString(antecedent) +
					 " --> " + Arrays.toString(consequent)  + "," + item + "   utility " + utilityTable.totalUtility
							+ " support : " + utilityTable.elements.size() 
							+ " confidence : " + confidence);		
					
					for(ElementOfTable element : utilityTable.elements){
						System.out.println("\t      SEQ:" + element.numeroSequence + " \t utility: " + element.utility
								+ " \t lutil: " + element.utilityLeft
								+ " \t lrutil: " + element.utilityLeftRight + " \t rutil: " + element.utilityRight
								+ " alpha : " + element.positionAlphaItemset 
								+ " beta : " + element.positionBetaItemset);
					}
				}

			}else{
				// If we are in debugging mode and the rule is not high utility and high confidence,
				// we will still show it in the console for debugging
				if(DEBUG){
					System.out.println("\n\t  LOW UTILITY RULE: " + Arrays.toString(antecedent) +
							 " --> " + Arrays.toString(consequent)  + "," + item + "   utility " + utilityTable.totalUtility
								+ " support : " + utilityTable.elements.size() 
								+ " confidence : " + confidence);	
					
					for(ElementOfTable element : utilityTable.elements){
						System.out.println("\t      SEQ:" + element.numeroSequence + " \t utility: " + element.utility
								+ " \t lutil: " + element.utilityLeft
								+ " \t lrutil: " + element.utilityLeftRight + " \t rutil: " + element.utilityRight
								+ " alpha : " + element.positionAlphaItemset 
								+ " beta : " + element.positionBetaItemset);
					}
				}
			}

			// If we should try to expand the left side of this rule
			if(shouldExpandLeftSide){
				expandFirstLeft(utilityTable, antecedent, newConsequent, sequenceIdsAntecedent);
			}
			
			// If we should try to expand the right side of this rule
			if(shouldExpandRightSide){
				expandRight(utilityTable, antecedent, newConsequent, sequenceIdsAntecedent);
			}
		}
		
		// Check the maximum memory usage
		MemoryLogger.getInstance().checkMemory();
	}
	
	/**
	 * This method will recursively try to append items to the left side of a rule to generate
	 * rules containing one more item on the left side.  This method is only called for rules
	 * of size 1*1, thus containing two items (e.g. a -> b)
	 * @param utilityTable the rule utility table
	 * @param antecedent the rule antecedent
	 * @param consequent the rule consequent
	 * @param sequenceIDsConsequent the list of sequences ids of sequences containing the rule antecedent
	 * @throws IOException if error while writting to file
	 */
	private void expandFirstLeft(UtilityTable utilityTable, int[] antecedent,
		int[] consequent, ListSequenceIDs sequenceIDsConsequent) throws IOException {

		// We first find the largest item in the left side aof the rule
		int largestItemInAntecedent = antecedent[antecedent.length -1];

		// We create a new map where we will build the utility table for the new rules that
		// will be created by adding an item to the current rule.
		// Key: an item appended to the rule     Value: the utility-table of the corresponding new rule
		Map<Integer, UtilityTableLeft> mapItemUtilityTable = new HashMap<Integer, UtilityTableLeft>();
		
		// for each sequence containing the rule (a line in the utility table of the original rule)
		for(ElementOfTable element : utilityTable.elements){
			// Optimisation: if the "lutil" is 0 for that rule in that sequence,
			// we do not need to scan this sequence.
			if(element.utilityLeft  == 0){
				continue;
			}

			// Get the sequence
			SequenceWithUtility sequence = database.getSequences().get(element.numeroSequence);

			// For each itemset before beta
			for(int i=0; i < element.positionBetaItemset; i++){
				List<Integer> itemsetI = sequence.getItemsets().get(i);
				
				// For each item
				for(int j=0; j < itemsetI.size(); j++){
					Integer itemJ = itemsetI.get(j);
				
					// Check if the item is greater than items in the antecedent of the rule 
					// according to the lexicographical order 
					if(itemJ <= largestItemInAntecedent){
						continue;
					}
					
					// ======= Otherwise, we need to update the utility table of the item ====================
					// Get the utility table of the item
					UtilityTableLeft tableItemJ = mapItemUtilityTable.get(itemJ);
					if(tableItemJ == null){
						// if no utility table, we create one
						tableItemJ = new UtilityTableLeft();
						mapItemUtilityTable.put(itemJ, tableItemJ);
					}


					// We will add a new element (line) in the utility table
					ElementTableLeft newElement = new ElementTableLeft(element.numeroSequence);

					// we will update the utility vlaue of that rule by adding the utility of the item
					// in that sequence
					double profitItemJ = sequence.getUtilities().get(i).get(j);
					newElement.utility = element.utility + profitItemJ;
					

					// If the user deactivate strategy 4, we will store the lrutil in the column
					// called lutil
					if(deactivateStrategy4){
						newElement.utilityLeft = element.utilityLeft + element.utilityLeftRight
								+ element.utilityRight - profitItemJ;
					}else{
						// otherwise we really calculate the lutil
						newElement.utilityLeft = element.utilityLeft + element.utilityLeftRight - profitItemJ;
					}
					
					
					// Then, we will scan itemsets from the first one until the beta -1  itemset 
					// in the sequence.
					// We will subtract the utility of items that are smaller than item J 
					// according to the lexicographical order from "lutil" because they
					// cannot be added anymore to the new rule.

					// For each itemset before the beta itemset
					for(int z=0; z < element.positionBetaItemset; z++){
						List<Integer> itemsetZ = sequence.getItemsets().get(z);
						
						// For each item W in that itemset
						for(int w= itemsetZ.size()-1; w >= 0 ; w--){
							Integer itemW = itemsetZ.get(w);

							// if the item is smaller than the larger item in the left side of the rule
							if(itemW <= largestItemInAntecedent){
								// we break;
								break;  
							}
							
							// otherwise, if item W is smaller than item J
							if(itemJ > itemW){
								// We will subtract the utility of W from "rutil"
								double profitItemW = sequence.getUtilities().get(z).get(w);
								newElement.utilityLeft -= profitItemW;
							}
						}
					}
					// end
					
					
					// Now that we have created the element for that sequence and that new rule
					// we will add the utility table of that new rule
					tableItemJ.addElement(newElement);
				 
				}
			}
		}
		
		// After that for each new rule, we create a table to store the beta values 
		// for each sequence where the new rule appears.
		// The reason is that the "beta" column of any new rules that will be generated
		// by recursively adding to the left, will staty the same. So we don't store it in the
		// utility tble of the rule directly but in a separated structure.
		
		// Beta is a map where the key is a sequence id
		//   and the key is the position of an itemset in the sequence.
		Map<Integer, Integer> tableBeta = null;
		
	
		// For each new rule
		for(Entry<Integer, UtilityTableLeft> entryItemTable :  mapItemUtilityTable.entrySet()){
			// We get the item that was added to create the new rule
			Integer item = entryItemTable.getKey();
			// We get the utility table of the new rule
			UtilityTableLeft tableItem = entryItemTable.getValue();
			

			// We check if we should try to expand its left side
			boolean shouldExpandLeftSide = tableItem.utility + tableItem.utilityLeft >= minutil 
					&& antecedent.length+1 < maxSizeAntecedent;
			
			// We need to calculate the list of sequences ids containing the antecedent of the new
			// rule since the antecedent has changed
			ListSequenceIDs sequenceIdentifiersNewAntecedent = null;

			// To calculate the confidence
			double confidence = 0;
			
			// If we should try to expand the left side of the rule
			// or if the rule is high utility, we recalculate the sequences ids containing
			// the antecedent
			if(shouldExpandLeftSide || tableItem.utility >= minutil ){
				// We obtain the list of sequence ids for the item
				ListSequenceIDs sequencesIdsItem = mapItemSequences.get(item);
						
				// We perform the intersection of the sequences ids of the antecedent
				// with those of the item to obtain the sequence ids of the new antecedent.
				sequenceIdentifiersNewAntecedent = sequenceIDsConsequent.intersection(sequencesIdsItem);
				 
				// we calculate the confidence
				confidence =  (double) tableItem.elements.size() / (double) sequenceIdentifiersNewAntecedent.getSize();
			}

			// if the new rule is high utility and has a high confidence
			boolean isHighUtilityAndHighConfidence = tableItem.utility >= minutil && confidence >= minConfidence;
			if(isHighUtilityAndHighConfidence ){

				// We create the antecedent for the new rule by appending the new item
				int [] nouvelAntecedent = new int[antecedent.length+1];
				System.arraycopy(antecedent, 0, nouvelAntecedent, 0, antecedent.length);
				nouvelAntecedent[antecedent.length] = item;

				// We save the rule to file
				saveRule(nouvelAntecedent, consequent, tableItem.utility, tableItem.elements.size(), confidence);

				// If we are in debugging mode, we will show the rule in the console
				if(DEBUG){
					System.out.println("\n\t  HIGH UTILITY SEQ. RULE: " + Arrays.toString(antecedent) +
							 " --> " + Arrays.toString(consequent)  + "," + item + "   utility " + utilityTable.totalUtility
									+ " support : " + utilityTable.elements.size() 
									+ " confidence : " + confidence);		
							
					for(ElementTableLeft element : tableItem.elements){
						System.out.println("\t      SEQ:" + element.sequenceID + " \t utility: " + element.utility
								+ " \t lutil: " + element.utilityLeft);
					}
				}

			}else{
				// if we are in debuging mode
				if(DEBUG){
					System.out.println("\n\t  LOW UTILITY SEQ. RULE: " + Arrays.toString(antecedent) +
							 " --> " + Arrays.toString(consequent)  + "," + item + "   utility " + utilityTable.totalUtility
									+ " support : " + utilityTable.elements.size() 
									+ " confidence : " + confidence);		
					
					for(ElementTableLeft element : tableItem.elements){
						System.out.println("\t      SEQ:" + element.sequenceID + " \t utility: " + element.utility
								+ " \t lutil: " + element.utilityLeft);
					}
				}
			}
			// If we should try to expand the left side of this rule
			if(shouldExpandLeftSide){
				// We create the antecedent for the new rule by appending the new item
				int [] newAntecedent = new int[antecedent.length+1];
				System.arraycopy(antecedent, 0, newAntecedent, 0, antecedent.length);
				newAntecedent[antecedent.length] = item;
				
				// We create the table for storing the beta position in each sequence
				if(tableBeta == null){
					tableBeta = new HashMap<Integer, Integer>();
					// We loop over each line from the original utility table and copy the 
					// beta value for each line
					
					// For each element of the utility of the original rule
					for(ElementOfTable element : utilityTable.elements){
						// copy the beta position
						tableBeta.put(element.numeroSequence, element.positionBetaItemset);
					}
				}
				
				// we recursively try to expand this rule
				expandSecondLeft(tableItem, newAntecedent, consequent, sequenceIdentifiersNewAntecedent, tableBeta);
	
			}
		}
		// We check the memory usage for statistics
		MemoryLogger.getInstance().checkMemory();
	}

	/**
	 * This method will recursively try to append items to the left side of a rule to generate
	 * rules containing one more item on the left side.  This method is called for rules
	 * containing at least 2 items on their left side already. For rules having 1 item on their left side
	 * another method is used instead.
	 * 
	 * @param utilityTable the rule utility table
	 * @param antecedent the rule antecedent
	 * @param consequent the rule consequent
	 * @param sequenceIDsConsequent the list of sequences ids of sequences containing the rule antecedent
	 * @throws IOException if error while writting to file
	 */
	private void expandSecondLeft(
			UtilityTableLeft utilityTable,
			int[] antecedent, int[] consequent,
			ListSequenceIDs sequenceIDsConsequent,
			Map<Integer, Integer> tableBeta) throws IOException {
		

		// We first find the largest item in the left side aof the rule
		int largestItemInAntecedent = antecedent[antecedent.length -1];
		
		// We create a new map where we will build the utility table for the new rules that
		// will be created by adding an item to the current rule.
		// Key: an item appended to the rule     Value: the utility-table of the corresponding new rule
		Map<Integer, UtilityTableLeft> mapItemUtilityTable = new HashMap<Integer, UtilityTableLeft>();

		// for each sequence containing the rule (a line in the utility table of the original rule)
		for(ElementTableLeft element : utilityTable.elements){
			// Optimisation: if the "lutil" is 0 for that rule in that sequence,
			// we do not need to scan this sequence.
			if(element.utilityLeft  == 0){
				continue;
			}

			// Get the sequence
			SequenceWithUtility sequence = database.getSequences().get(element.sequenceID);
			
			// Get the beta position in that sequence
			Integer positionBetaItemset = tableBeta.get(element.sequenceID);

			// For each itemset before beta
			for(int i=0; i < positionBetaItemset; i++){
				List<Integer> itemsetI = sequence.getItemsets().get(i);
				
				//for each  item
				for(int j=0; j < itemsetI.size(); j++){
					Integer itemJ = itemsetI.get(j);
				
					// Check if the item is greater than items in the antecedent of the rule 
					// according to the lexicographical order 
					if(itemJ <= largestItemInAntecedent){
						continue;
					}
					
					// ======= Otherwise, we need to update the utility table of the item ====================
					// Get the utility table of the item
					UtilityTableLeft tableItemJ = mapItemUtilityTable.get(itemJ);
					if(tableItemJ == null){
						// if no utility table, we create one
						tableItemJ = new UtilityTableLeft();
						mapItemUtilityTable.put(itemJ, tableItemJ);
					}

					// We will add a new element (line) in the utility table
					ElementTableLeft newElement = new ElementTableLeft(element.sequenceID);
	

					// we will update the utility vlaue of that rule by adding the utility of the item
					// in that sequence
					double utilityItemJ = sequence.getUtilities().get(i).get(j);
					newElement.utility = element.utility + utilityItemJ;
					
					// The lutil value is updated by subtracting the utility of the item
					newElement.utilityLeft = element.utilityLeft - utilityItemJ;
					
					// Then, we will scan itemsets from the first one until the beta -1  itemset 
					// in the sequence.
					// We will subtract the utility of items that are smaller than item J 
					// according to the lexicographical order from "lutil" because they
					// cannot be added anymore to the new rule.
					
					// for each itemset
					for(int z=0; z < positionBetaItemset; z++){
						List<Integer> itemsetZ = sequence.getItemsets().get(z);
						
						// for each item
						for(int w= itemsetZ.size()-1; w >= 0 ; w--){
							Integer itemW = itemsetZ.get(w);
							// if the item is smaller than the larger item in the left side of the rule
							if(itemW <= largestItemInAntecedent){
								break; 
							}
							// otherwise, if item W is smaller than item J
							if(itemW < itemJ){
								// We will subtract the utility of W from "rutil"
								double utilityItemW = sequence.getUtilities().get(z).get(w);
								newElement.utilityLeft -= utilityItemW;
							}
						}
					}
					// end
					
					// Now that we have created the element for that sequence and that new rule
					// we will add that element to tthe utility table of that new rule
					tableItemJ.addElement(newElement);
				
				}
			}
		}

		// For each new rule
		for(Entry<Integer, UtilityTableLeft> entryItemTable :  mapItemUtilityTable.entrySet()){
			// We get the item that was added to create the new rule
			Integer item = entryItemTable.getKey();
			// We get the utility table of the new rule
			UtilityTableLeft tableItem = entryItemTable.getValue();
			

			// We check if we should try to expand its left side
			boolean shouldExpandLeft = tableItem.utility + tableItem.utilityLeft >= minutil
					&& antecedent.length+1 < maxSizeAntecedent;

			// We check if the rule is high utility
			boolean isHighUtility = tableItem.utility >= minutil;
			
			double confidence = 0;
			
			// We need to calculate the list of sequences ids containing the antecedent of the new
			// rule since the antecedent has changed
			ListSequenceIDs sequenceIdentifiersNewAntecedent = null;
			
			// If we should try to expand the left side of the rule
			// or if the rule is high utility, we recalculate the sequences ids containing
			// the antecedent
			if(shouldExpandLeft || isHighUtility ){
				// We obtain the list of sequence ids for the item
				ListSequenceIDs numerosequencesItem = mapItemSequences.get(item);
				
				// We perform the intersection of the sequences ids of the antecedent
				// with those of the item to obtain the sequence ids of the new antecedent.
				sequenceIdentifiersNewAntecedent = sequenceIDsConsequent.intersection(numerosequencesItem);

				// we calculate the confidence
				confidence =  (double) tableItem.elements.size() / (double) sequenceIdentifiersNewAntecedent.getSize(); 
			}
			
			// if the new rule is high utility and has a high confidence
			if(isHighUtility && confidence >= minConfidence){
				
				// We create the antecedent for the new rule by appending the new item
				int [] newAntecedent = new int[antecedent.length+1];
				System.arraycopy(antecedent, 0, newAntecedent, 0, antecedent.length);
				newAntecedent[antecedent.length] = item;

				// We save the rule to file
				saveRule(newAntecedent, consequent, tableItem.utility, tableItem.elements.size() , confidence);

				// If we are in debugging mode, we will show the rule in the console
				if(DEBUG){
					// print the rule
					System.out.println("\n\t  HIGH UTILITY SEQ. RULE: " + Arrays.toString(antecedent) +
							 " --> " + Arrays.toString(consequent)  + "," + item + "   utility " + utilityTable.utility
									+ " support : " + utilityTable.elements.size() 
									+ " confidence : " + confidence);		
							
					for(ElementTableLeft element : tableItem.elements){
						System.out.println("\t      SEQ:" + element.sequenceID + " \t utility: " + element.utility
								+ " \t lutil: " + element.utilityLeft);
					}
				}
			}else{
				// if we are in debuging mode
				if(DEBUG){
					// print the rule
					System.out.println("\n\t  LOW UTILITY SEQ. RULE: " + Arrays.toString(antecedent) +
							 " --> " + Arrays.toString(consequent)  + "," + item + "   utility " + utilityTable.utility
									+ " support : " + utilityTable.elements.size() 
									+ " confidence : " + confidence);		
							
					for(ElementTableLeft element : tableItem.elements){
						System.out.println("\t      SEQ:" + element.sequenceID + " \t utility: " + element.utility
								+ " \t lutil: " + element.utilityLeft);
					}
				}
			}
			
			// If we should try to expand the left side of this rule
			if(shouldExpandLeft){
				// We create the antecedent for the new rule by appending the new item
				int [] nouvelAntecedent = new int[antecedent.length+1];
				System.arraycopy(antecedent, 0, nouvelAntecedent, 0, antecedent.length);
				nouvelAntecedent[antecedent.length] = item;
	
				// we recursively call this method
				expandSecondLeft(tableItem, nouvelAntecedent, consequent, sequenceIdentifiersNewAntecedent, tableBeta);
			}
		}
		// We check the memory usage
		MemoryLogger.getInstance().checkMemory();
	}


	/**
	 * Print statistics about the last algorithm execution to System.out.
	 */
	public void printStats() {
		System.out.println("==============================================================================");
		System.out.println("-------------- HUSRM algorithm for high utility sequential rule mining -------------");
		System.out.println("==============================================================================");
		System.out.println("\tminutil: " + minutil);
		System.out.println("\tSequential rules count: " + ruleCount);
		System.out.println("\tTotal time : " + (timeEnd - timeStart) + " ms");
		System.out.println("\tMax memory (mb) : "
				+ MemoryLogger.getInstance().getMaxMemory());
		System.out.println("==============================================================================");
	}
	
	//============================================================================================================================
	// =========================================== CLASSES FOR STORING LISTS OF SEQUENCE IDs===================
	//============================================================================================================================
	
	/**
	 * This interface represents a list of sequences ids
	 * @author Souleymane Zida, Philippe Fournier-Viger
	 */
	public interface ListSequenceIDs {

		/**
		 * This method adds a sequence id to this list
		 * @param int the sequence id
		 */
		public abstract void addSequenceID(int noSequence);

		/**
		 * Get the number of sequence ids
		 * @return the number of sequence ids
		 */
		public abstract int getSize();

		/**
		 *  Method to intersect two lists of sequences ids
		 * @param vector another list
		 * @return the intersection of this list and the other list.
		 */
		public abstract ListSequenceIDs intersection(ListSequenceIDs vector2);
	}
	
	/**
	 * This class represents a list of sequences ids implemented by a bit vector
	 * @author Souleymane Zida, Philippe Fournier-Viger
	 */
	public class ListSequenceIDsBitVector implements ListSequenceIDs{
		// the internal bitset
		private BitSet bitset = new BitSet();
		// the number of bit set to 1 in the bitset
		private int size = -1;
		
		/**
		 * Constructor
		 */
		public ListSequenceIDsBitVector(){
		}

		@Override
		/**
		 * This method adds a sequence id to this list
		 * @param int the sequence id
		 */
		public void addSequenceID(int bit){
			bitset.set(bit);
		}
		
		/**
		 * Get the number of sequence ids
		 * @return the number of sequence ids
		 */
		public int getSize(){
			// if we don't know the size
			if(size == -1){
				// we calculate it but remember it in variable "size" for future use.
				size = bitset.cardinality();
			}
			// return the size
			return size;
		}
		
		/**
		 *  Method to intersect two lists of sequences ids
		 * @param vector another list
		 * @return the intersection of this list and the other list.
		 */
		public ListSequenceIDs intersection(ListSequenceIDs vector2){
			//  we get the first vector
			ListSequenceIDsBitVector bitVector2 = (ListSequenceIDsBitVector) vector2;
			
			// we create a new vector for the result
			ListSequenceIDsBitVector result = new ListSequenceIDsBitVector();
			// we clone the first bit vecotr
			result.bitset = (BitSet) bitset.clone();
			// we intersect both bit vector
			result.bitset.and(bitVector2.bitset);
			// Return the result
			return result;
		}
		
		/**
		 * Get a string representation of this list
		 * @return a string
		 */
		public String toString() {
			return bitset.toString();
		}
	}
	
	//==================================
	/**
	 * This class represents a list of sequences ids implemented by an array list
	 * @author Souleymane Zida, Philippe Fournier-Viger
	 */
		public class ListSequenceIDsArrayList implements ListSequenceIDs{
			// the internal array list representation
			List<Integer> list = new ArrayList<Integer>();
			
			/**
			 * Constructor
			 */
			public ListSequenceIDsArrayList(){
			}

			/**
			 * This method adds a sequence id to this list
			 * @param int the sequence id
			 */
			public void addSequenceID(int noSequence){
				list.add(noSequence);
			}
			

			/**
			 * Get the number of sequence ids
			 * @return the number of sequence ids
			 */
			public int getSize(){
				return list.size();
			}
			
			/**
			 *  Method to intersect two lists of sequences ids
			 * @param vector another list
			 * @return the intersection of this list and the other list.
			 */
			public ListSequenceIDs intersection(ListSequenceIDs list2){
				// Get the second list
				ListSequenceIDsArrayList arrayList2 = (ListSequenceIDsArrayList) list2;
				// Create a new list for the result
				ListSequenceIDs result = new ListSequenceIDsArrayList();
				
				// for each sequence id in this list
				for(Integer no : list){
					// if it appear in the second list
					boolean appearInSecondList = Collections.binarySearch(arrayList2.list, no) >= 0;
					if(appearInSecondList){
						// then we add it to the new list
						result.addSequenceID(no);
					}
				}
				// return the result
				return result;
			}
			
			/**
			 * Get a string representation of this list
			 * @return a string
			 */
			public String toString() {
				return list.toString();
			}
		}

	//============================================================================================================================
	// =========================================== CLASS FOR LEFT-UTILITY-TABLES ===========================================
	//============================================================================================================================
	
	/**
	 * This class represents a utility-table used for left expansions (what we call a left-utility table)
	 * @author Souleymane Zida, Philippe Fournier-Viger
	 */
	public class UtilityTableLeft{
		// the list of elements (lines) in that utility table
		List<ElementTableLeft> elements = new ArrayList<ElementTableLeft>();
		// the total utility in that table
		int utility = 0;
		// the toal lutil values of elements in that table
		int utilityLeft = 0;
		
		/**
		 * Constructor
		 */
		public UtilityTableLeft(){
		}
		
		/**
		 * Add a new element (line) to that table
		 * @param element the new element
		 */
		public void addElement(ElementTableLeft element){
			// add the element
			elements.add(element);
			// add the utility of this element to the total utility of that table
			utility += element.utility;
			// add the "lutil" utilit of this element to the total for that table
			utilityLeft += element.utilityLeft;
		}
	}
	
	/**
	 * This class represents a element(line) of a utility-table used for left expansions
	 * @author Souleymane Zida, Philippe Fournier-Viger
	 */
	public class ElementTableLeft{
		// the corresponding sequence id
		int sequenceID;
		// the utility
		double utility;
		// the "lutil" value
		double utilityLeft;
		
		/**
		 * Constructor
		 * @param sequenceID the sequence id
		 */
		public ElementTableLeft(int sequenceID){
			this.sequenceID = sequenceID;
			this.utility = 0;
			this.utilityLeft = 0;
		}
		
		/**
		 * Constructor
		 * @param sequenceID a sequence id
		 * @param utility the utility
		 * @param utilityLeft the lutil value
		 */
		public ElementTableLeft(int sequenceID, int utility, int utilityLeft){
			this.sequenceID = sequenceID;
			this.utility = utility;
			this.utilityLeft = utilityLeft;
		}
	}
	

	//============================================================================================================================
	// ===========================================  CLASS FOR LEFT-RIGHT UTILITY-TABLES===========================================
	//============================================================================================================================
	
	
	/**
	 * This class represents a utility-table used for left or right expansions (what we call a left-right utility table)
	 * @author Souleymane Zida, Philippe Fournier-Viger
	 */
	public class UtilityTable{
		// the list of elements (lines) in that utility table
		List<ElementOfTable> elements = new ArrayList<ElementOfTable>();
		// the total utility in that table
		double totalUtility = 0;
		// the toal lutil values of elements in that table
		double totalUtilityLeft = 0;
		// the toal lrutil values of elements in that table
		double totalUtilityLeftRight = 0;
		// the toal rutil values of elements in that table
		double totalUtilityRight = 0;
		
		/**
		 * Constructor
		 */
		public UtilityTable(){
			
		}
		
		/**
		 * Add a new element (line) to that table
		 * @param element the new element
		 */
		public void addElement(ElementOfTable element){
			// add the element
			elements.add(element);
			// make the sum of the utility, lutil, rutil and lrutil values
			totalUtility += element.utility;
			totalUtilityLeft += element.utilityLeft;
			totalUtilityLeftRight += element.utilityLeftRight;
			totalUtilityRight += element.utilityRight;
		}
	}
	
	/**
	 * This class represents a element(line) of a utility-table used for left or right expansions
	 * @author Souleymane Zida, Philippe Fournier-Viger
	 */
	public class ElementOfTable{
		// the corresponding sequence id
		int numeroSequence;
		// the utility
		double utility;
		// the lutil value
		double utilityLeft;
		// the lrutil value
		double utilityLeftRight;
		// the rutil value
		double utilityRight;
		// the alpha and beta values
		int positionAlphaItemset = -1;
		int positionBetaItemset = -1;
		
		/**
		 * Constructor
		 * @param sequenceID the sequence id
		 */
		public ElementOfTable(int sequenceID){
			this.numeroSequence = sequenceID;
			this.utility = 0;
			this.utilityLeft = 0;
			this.utilityLeftRight = 0;
			this.utilityRight = 0;
		}
		
		/**
		 * Constructor
		 * @param sequenceID a sequence id
		 * @param utility the utility
		 * @param utilityLeft the lutil value
		 * @param utilityLeftRight the lrutil value
		 * @param utilityRight the rutil value
		 */
		public ElementOfTable(int sequenceID,
				double utility,
				double utilityLeft,
				double utilityLeftRight,
				double utilityRight){
			this.numeroSequence = sequenceID;
			this.utility = utility;
			this.utilityLeft = utilityLeft;
			this.utilityLeftRight = utilityLeftRight;
			this.utilityRight = utilityRight;
		}
	}
}
