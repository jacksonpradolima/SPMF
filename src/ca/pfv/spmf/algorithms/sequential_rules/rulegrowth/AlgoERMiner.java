package ca.pfv.spmf.algorithms.sequential_rules.rulegrowth;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ca.pfv.spmf.input.sequence_database_list_integers.Sequence;
import ca.pfv.spmf.input.sequence_database_list_integers.SequenceDatabase;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This is the original implementation of the ERMiner algorithm for mining sequential rules
 * common to several sequences where antecedent and consequent are unordered itemsets. 
 * <br/><br/>
 * The main method of this algorithm is "runAlgorithm". It output the result to a file.
 *
 * @see Occurence
 * @see Sequence
 * @see SequenceDatabase
 * @author Philippe Fournier-Viger
 */
public class AlgoERMiner {
	//*** for statistics ***/
	long timeStart = 0;  // start time of latest execution
	long timeEnd = 0;  // end time of latest execution
	int ruleCount; // number of rules generated
	
	//*** parameters ***/
	// minimum confidence
	double minConfidence;
	// minimum support
	int minsuppRelative;
	// this is the sequence database
	SequenceDatabase database;
	
	//*** internal variables ***/
	// This map contains for each item (key) a map of occurences (value).
	// The map of occurences associates to sequence ID (key), an occurence of the item (value).
	Map<Integer,  Map<Integer, Occurence>> mapItemCount;  // item, <tid, occurence>

	// object to write the output file
	BufferedWriter writer = null; 
	
	ExpandLeftStore store = new ExpandLeftStore();
	SparseMatrix matrix = new SparseMatrix();

	private long totalCandidateCount;
	private long candidatePrunedCount;

	/**
	 * Default constructor
	 */
	public AlgoERMiner() {
	}


	/**
	 * The main method to run the algorithm
	 * @param minSupport : the minimum support (percentage as a double value)
	 * @param minConfidence : the minimum confidence threshold
	 * @param input : an input file path of a seq   uence database
	 * @param output : a file path for writing the output file containing the seq. rules.
	 * @exception IOException if error reading/writing files
	 */
	public void runAlgorithm(double minSupport, double minConfidence, String input, String output) throws IOException {
		try {
			// read the input database
			database = new SequenceDatabase(); 
			database.loadFile(input);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// convert minimum support to an absolute minimum support (integer)
		this.minsuppRelative = (int) Math.ceil(minSupport * database.size());
		
		// run the algorithm  with the just calculated absolute minimum support
		runAlgorithm(input, output, minsuppRelative, minConfidence);
	}
	
	/**
	 * The main method to run the algorithm
	 * @param relativeMinsup : the minimum support as an integer value (a relative minimum support)
	 * @param minConfidence : the minimum confidence threshold
	 * @param input : an input file path of a sequence database
	 * @param output : a file path for writing the output file containing the seq. rules.
	 * @exception IOException if error reading/writing files
	 */
	public void runAlgorithm(String input, String output, int relativeMinsup, double minConfidence) throws IOException {
		// save the minimum confidence parameter
		this.minConfidence = minConfidence;
		// reinitialize the number of rules found
		ruleCount = 0;
//		countPruning = 0;
		
		// if the database was not loaded, then load it.
		if(database == null){
			try {
				database = new SequenceDatabase(); 
				database.loadFile(input);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// reset the stats for memory usage
		MemoryLogger.getInstance().reset();

		// prepare the object for writing the output file
		writer = new BufferedWriter(new FileWriter(output)); 
		
		// if minsup is 0, set it to 1 to avoid generating
		// rules not in the database
		this.minsuppRelative =  relativeMinsup;
		if(this.minsuppRelative == 0){ // protection
			this.minsuppRelative = 1;
		}

		// save the start time
		timeStart = System.currentTimeMillis(); // for stats

		// Remove infrequent items from the database in one database scan.
		// Then perform another database scan to count the
		// the support of each item in the same database scan 
		// and their occurrences.
		calculateFrequencyOfEachItem(database);	
		
		// =================== CALCULATE MATRIX ==============
		// for each sequence
		generateMatrix(database);
		// =================== 
		
		// We will now generate the equivalence classes of rules of size 1*1
		// We create two maps to store the equivalence classes.
		// The map for left-equivalence classes has a key : item on the right side, value: list of rules
		Map<Integer, LeftEquivalenceClass> mapEclassLeft = new HashMap<Integer, LeftEquivalenceClass>();
		// The map for right-equivalence classes has a key : item on the left side, value: list of rules
		Map<Integer, RightEquivalenceClass> mapEclassRight = new HashMap<Integer, RightEquivalenceClass>();
		
		// for each entry in the matrix
		// entry  =  key: item I    value: a map where key: item J and value: the support of {i, j} 
		for(Entry<Integer, Map<Integer, Integer>> entry :  matrix.getMatrix().entrySet()) {
			Integer intI = entry.getKey();
			Map<Integer, Occurence> occurencesI = mapItemCount.get(intI);
			// get the tidset of item I
			Set<Integer> tidsI = occurencesI.keySet();
			
			// for each item J that co-occurs with I
			for(Entry<Integer, Integer> entryJ : entry.getValue().entrySet()) {
				// if I and J are infrequent, we don't continue
				if(entryJ.getValue() < minsuppRelative) {
					continue;
				}
				Integer intJ = entryJ.getKey();
				Map<Integer, Occurence> occurencesJ = mapItemCount.get(intJ);

				// (1) We will now calculate the tids of I -->J  and J-->I.
				
				// initialize the tids sets
				Set<Integer> tidsIJ = new HashSet<Integer>();  // tidset of  I -->J  
				Set<Integer> tidsJI = new HashSet<Integer>(); // tidset of J-->I
				
				// for each occurence of I
				if(occurencesI.size() < occurencesJ.size()) {
					calculateTidsetsIJandJI(occurencesI, occurencesJ, tidsIJ, tidsJI);
				}else {
					calculateTidsetsIJandJI(occurencesJ, occurencesI, tidsJI, tidsIJ);
				}
				
				// (2) check if the two itemsets have enough common tids
				// if not, we don't need to generate a rule for them.
				
				// create rule IJ
				if(tidsIJ.size() >= minsuppRelative){
					// calculate the confidence of I ==> J
					double confIJ = ((double)tidsIJ.size()) / occurencesI.size();

					// create itemset of the rule I ==> J
					int[] itemsetI = new int[]{intI};
					int[] itemsetJ = new int[]{intJ};

					Set<Integer> tidsJ = occurencesJ.keySet();

					// if the confidence is high enough, save the rule
					if(confIJ >= minConfidence){
						saveRule(tidsIJ, confIJ, itemsetI, itemsetJ);
					}
					// register the rule in the appropriate equivalence classes
					registerRule11(intI, intJ, tidsI, tidsJ, tidsIJ, occurencesI, occurencesJ, mapEclassLeft, mapEclassRight);
				}
					
				// check if J ==> I has enough common tids
				// If yes, we create the rule J ==> I
				if(tidsJI.size() >= minsuppRelative){
					// create itemset of the rule J ==> I
					int[] itemsetI = new int[]{intI};
					int[] itemsetJ = new int[]{intJ};
					
					// calculate the confidence
					double confJI = ((double)tidsJI.size()) / occurencesJ.size();

					Set<Integer> tidsJ = occurencesJ.keySet();

					// if the confidence is high enough, save the rule
					if(confJI >= minConfidence){
						saveRule(tidsJI, confJI, itemsetJ, itemsetI);
					}
					// register the rule in the appropriate equivalence classes
					registerRule11(intJ, intI, tidsJ,  tidsI, tidsJI, occurencesJ, occurencesI, mapEclassLeft, mapEclassRight);
				}
			}
		}

		// PERFORM EXPAND LEFT FOR EACH LEFT-EQUIVALENCE CLASS OF SIZE 1-1
		for(LeftEquivalenceClass eclassLeft : mapEclassLeft.values()) {
			if(eclassLeft.rules.size() != 1) {
				Collections.sort(eclassLeft.rules, new Comparator<LeftRule>() {
					public int compare(LeftRule arg0, LeftRule arg1) {
						return arg0.itemsetI[0] - arg1.itemsetI[0];
					}});

				expandLeft(eclassLeft);
			}
		}
		
		mapEclassLeft = null;
		
		// PERFORM EXPAND RIGHT FOR EACH RIGHT-EQUIVALENCE CLASS OF SIZE 1-1
		for(RightEquivalenceClass eclassRight : mapEclassRight.values()) {
			if(eclassRight.rules.size() != 1) {
				Collections.sort(eclassRight.rules, new Comparator<RightRule>() {
					public int compare(RightRule arg0, RightRule arg1) {
						return arg0.itemsetJ[0] - arg1.itemsetJ[0];
					}});
					
				expandRight(eclassRight, true);
			}
		}
		
		mapEclassRight = null;
		
		// PROCESS ALL EQUIVALENCE CLASSES WITH MORE THAN ONE ITEM IN LEF PART FOR EXPAND LEFT AFTER RIGHT...
		for(Map<Integer, List<LeftEquivalenceClass>> map : store.getStore().values()) {
			for(List<LeftEquivalenceClass> eclassList :  map.values()) {
				for(LeftEquivalenceClass eclass : eclassList) {
					if(eclass.rules.size() != 1) {
						Collections.sort(eclass.rules, new Comparator<LeftRule>() {
							public int compare(LeftRule arg0, LeftRule arg1) {
								return arg0.itemsetI[arg0.itemsetI.length-1] - arg1.itemsetI[arg1.itemsetI.length-1];
							}});
					
						expandLeft(eclass);
					}

				}
			}
		}
		
		// save end time
		timeEnd = System.currentTimeMillis(); 
		
		// close the file
		writer.close();
		
		// after the algorithm ends, we don't need a reference to the database anymore.
		database = null;
	}

	private void registerRule11(Integer intI, Integer intJ, Set<Integer> tidsI,
			Set<Integer> tidsJ, Set<Integer> tidsIJ,
			Map<Integer, Occurence> occurencesI,
			Map<Integer, Occurence> occurencesJ,
			Map<Integer, LeftEquivalenceClass> mapEclassLeft,
			Map<Integer, RightEquivalenceClass> mapEclassRight) {
		
		// add the rule to the left equivalence class
		LeftEquivalenceClass leftClass = mapEclassLeft.get(intJ);
		if(leftClass == null) {
			leftClass = new LeftEquivalenceClass(new int[] {intJ}, tidsJ, occurencesJ);
			mapEclassLeft.put(intJ, leftClass);
		}
		LeftRule ruleL = new LeftRule(new int[] {intI}, tidsI, tidsIJ);
		leftClass.rules.add(ruleL);
		 
		// add the rule to the right equivalence class
		RightEquivalenceClass rightclass = mapEclassRight.get(intI);
		if(rightclass == null) {
			rightclass = new RightEquivalenceClass(new int[] {intI}, tidsI, occurencesI);
			mapEclassRight.put(intI, rightclass);
		}
		RightRule ruleR = new RightRule(new int[] {intJ}, tidsJ, tidsIJ, occurencesJ);
		rightclass.rules.add(ruleR);
	}


	private void calculateTidsetsIJandJI(Map<Integer, Occurence> occurencesI,
			Map<Integer, Occurence> occurencesJ, Set<Integer> tidsIJ, Set<Integer> tidsJI) {
		for(Entry<Integer, Occurence> entryOccI : occurencesI.entrySet()){
			Integer tid = entryOccI.getKey();
			// get the occurence of J in the same sequence
			Occurence occJ = occurencesJ.get(tid);
			// if J appears in that sequence
			if(occJ !=  null){
				Occurence occI = entryOccI.getValue();
				// if J appeared before I in that sequence,
				// then we put this tid in the tidset of  J-->I
				if(occJ.firstItemset < occI.lastItemset){
					tidsJI.add(tid);
				}
				// if I appeared before J in that sequence,
				// then we put this tid in the tidset of  I-->J
				if(occI.firstItemset < occJ.lastItemset){
					tidsIJ.add(tid);
				}
			}
		}
	}
	
	public int[] concatenate(int [] itemset, int item) {
		int[] newItemset = new int[itemset.length+1];
		System.arraycopy(itemset, 0, newItemset, 0, itemset.length);
		newItemset[itemset.length] = item;
		return newItemset;
	}
	

	private void expandLeft(LeftEquivalenceClass eclass){
		for(int w=0; w < eclass.rules.size()-1; w++){  // IMPORTANT : SIZE -1 BECAUSE THE LAST ONE HAS NOTHING LEFT FOR COMPARISON
			LeftRule rule1 = eclass.rules.get(w);
			int d = rule1.itemsetI[rule1.itemsetI.length -1];
			
			LeftEquivalenceClass rulesForRecursion 
				= new LeftEquivalenceClass(eclass.itemsetJ, eclass.tidsJ,
						eclass.occurencesJ);

			// for each rule J != I
			for(int m=w+1; m < eclass.rules.size(); m++)	{
				LeftRule rule2 = eclass.rules.get(m);

				int c = rule2.itemsetI[rule2.itemsetI.length -1];
				
				if(matrix.getCount(c, d) < minsuppRelative){
					candidatePrunedCount++;
					totalCandidateCount++;
					continue;
				}
				totalCandidateCount++;

				
				// CALCULATE TIDS I U {C}
				Set<Integer> tidsIC = new HashSet<Integer>(); 
				
				Map<Integer, Occurence> mapC = mapItemCount.get(c);
				
				// depending of the relative size of tids of I and tids of {c} we have two ways to calculate it
				
				if(rule1.tidsI.size() < mapC.size()) {
					// EARLY SKIP OPTIMIZATION: <<<<<********
					int remains = rule1.tidsI.size();
		 			// for each sequence containing I
					for(Integer tid: rule1.tidsI){
						// Get the first and last occurences of C in that sequence
						// if there is an occurence
			    		if(mapC.get(tid) != null){
			    			// add the tid of the sequence to the tidset of JU{c}
			    			tidsIC.add(tid);
			    		}
			    		remains--;
			    		// early skip
			    		if(tidsIC.size() + remains < minsuppRelative) {
			    			break;
			    		}
			    	}
				}else {
					int remains = mapC.size();
					// for each sequence containing I
					for(Integer tid: mapC.keySet()){
						// Get the first and last occurences of C in that sequence
						// if there is an occurence
			    		if(rule1.tidsI.contains(tid)){
			    			// add the tid of the sequence to the tidset of JU{c}
			    			tidsIC.add(tid);
			    		}
			    		remains--;
			    		if(tidsIC.size() + remains < minsuppRelative) {
			    			break;
			    		}
			    	}
				}
				
				// CALCULATE TIDS IC ==> J
				Set<Integer>  tidsIC_J = new HashSet<Integer>();
		    	
				// depending of the relative size of tids of IUJ and tids of {c} we have two ways to calculate it
				if(rule1.tidsIJ.size() < mapC.size()) {
					// for each sequence containing I
					for(Integer tid: rule1.tidsIJ){
						// Get the first and last occurences of C in that sequence
						Occurence occurenceC = mapC.get(tid);
						// if there is an occurence
			    		if(occurenceC != null){
							Occurence occurenceJ = eclass.occurencesJ.get(tid);
			    			if(occurenceC.firstItemset < occurenceJ.lastItemset){
			    				// add the tid of the sequence to the tidset of JU{c}
			    				tidsIC_J.add(tid);
				    		}
			    		}
			    	}
				}else {
					// for each sequence containing I
					for(Entry<Integer, Occurence> entryC: mapC.entrySet()){
						int tid = entryC.getKey();
						if(rule1.tidsIJ.contains(tid)) {
							// Get the first and last occurences of C in that sequence
							Occurence occurenceC = entryC.getValue();
							// if there is an occurence
							Occurence occurenceJ = eclass.occurencesJ.get(tid);
			    			if(occurenceC.firstItemset < occurenceJ.lastItemset){
				    			// add the tid of the sequence to the tidset of JU{c}
				    			tidsIC_J.add(tid);
			    			}
						}
			    	}
				}
	 			
				
				if(tidsIC_J.size() >= minsuppRelative) {
					// Create rule and calculate its confidence of IU{c} ==> J 
			    	// defined as:  sup(IU{c} -->J) /  sup(IU{c})			
					double confIC_J = ((double) tidsIC_J.size()) / tidsIC.size();
		
					// try to combine the rules
					int itemsetIC[] = new int[rule1.itemsetI.length+1];
					System.arraycopy(rule1.itemsetI, 0, itemsetIC, 0, rule1.itemsetI.length);
					itemsetIC[rule1.itemsetI.length] = c;

					LeftRule newRule = new LeftRule(itemsetIC, tidsIC, tidsIC_J);
					
					// if the confidence is high enough, then it is a valid rule
					if(confIC_J >= minConfidence){
						// save the rule
						saveRule(tidsIC_J, confIC_J, itemsetIC,  eclass.itemsetJ);
					}
		
					rulesForRecursion.rules.add(newRule);
				}
			}
			
			if(rulesForRecursion.rules.size() >1) {
				expandLeft(rulesForRecursion);
			}
		}
		// check the memory usage
    	MemoryLogger.getInstance().checkMemory();
	}


	private void expandRight(RightEquivalenceClass eclass, boolean firstTime) {
		for(int w=0; w < eclass.rules.size()-1; w++){ // IMPORTANT : SIZE -1 BECAUSE THE LAST ONE HAS NOTHING LEFT FOR COMPARISON
			RightRule rule1 = eclass.rules.get(w);
			int d = rule1.itemsetJ[rule1.itemsetJ.length -1];
			RightEquivalenceClass rulesForRecursion= new RightEquivalenceClass(eclass.itemsetI, eclass.tidsI, eclass.occurencesI);

			// for each rule J != I
			for(int m=w+1; m < eclass.rules.size(); m++)	{
				RightRule rule2 = eclass.rules.get(m);
				
				int c = rule2.itemsetJ[rule2.itemsetJ.length -1];

				if(matrix.getCount(c, d) < minsuppRelative) {
					candidatePrunedCount++;
					totalCandidateCount++;
					continue;
				}
				totalCandidateCount++;
				
				// CALCULATE TIDS OF  I ==> JC
				Set<Integer> tidsI_JC = new HashSet<Integer>();
				
	 			// for each sequence containing I
				Map<Integer, Occurence> mapC = mapItemCount.get(c);

				// depending of the relative size of tids of IUJ and tids of {c} we have two ways to calculate it
				if(rule1.tidsIJ.size() < mapC.size()) {
					// EARLY SKIP OPTIMIZATION:
					int remains = rule1.tidsIJ.size();
					for(Integer tid: rule1.tidsIJ){
						// Get the first and last occurences of C in that sequence
						Occurence occurenceC = mapC.get(tid);
						// if there is an occurence
			    		if(occurenceC != null) {
							Occurence occurenceI = eclass.occurencesI.get(tid);
			    			if(occurenceC.lastItemset > occurenceI.firstItemset){
			    				// add the tid of the sequence to the tidset of JU{c}
			    				tidsI_JC.add(tid);
			    			}
			    		}
			    		remains--;
			    		if(tidsI_JC.size() + remains < minsuppRelative) {
			    			break;
			    		}
			    	}
				}else {
					// EARLY SKIP OPTIMIZATION:
					int remains = mapC.size();
					for(Entry<Integer, Occurence> entryC: mapC.entrySet()){
						int tid = entryC.getKey();
						// if there is an occurence
			    		if(rule1.tidsIJ.contains(tid)) {
							Occurence occurenceC = entryC.getValue();
							Occurence occurenceI = eclass.occurencesI.get(tid);
			    			if(occurenceC.lastItemset > occurenceI.firstItemset){
			    				// add the tid of the sequence to the tidset of JU{c}
			    				tidsI_JC.add(tid);
			    			}
			    		}
			    	}
					remains--;
		    		if(tidsI_JC.size() + remains < minsuppRelative) {
		    			break;
		    		}
				}
				
				
				// if the support of I ==> JU{c} is enough 
	    		if(tidsI_JC.size() >= minsuppRelative){  
	    			
	    			// CALCULATE THE OCCURENCES OF JU{c}
	    			Set<Integer> tidsJC = new HashSet<Integer>(rule1.tidsJ.size());
	    			Map<Integer, Occurence> occurencesJC = new HashMap<Integer, Occurence>();
	    			
	    			// for each sequence containing J
	    			if(rule1.tidsJ.size() < mapC.size()) {
		    			for(Integer tid: rule1.tidsJ){
		    				// Get the first and last occurences of C in that sequence
		    				Occurence occurrenceC = mapC.get(tid);
		    				// if there is an occurence
		    	    		if(occurrenceC != null){
		    	    			// add the tid of the sequence to the tidset of JU{c}
		    	    			tidsJC.add(tid);
		    	    			// calculate last occurence of JU{c} depending on if
		    	    			// the last occurence of J is before the last occurence
		    	    			// of c or not.
		    	    			Occurence occurenceJ = rule1.occurencesJ.get(tid);
		    	    			if(occurrenceC.lastItemset < occurenceJ.lastItemset){
		    	    				occurencesJC.put(tid, occurrenceC);
		    	    			}else{
		    	    				occurencesJC.put(tid, occurenceJ);
		    	    			}
		    	    		}
		    	    	}
	    			}else {
	    				for(Entry<Integer, Occurence> entryC: mapC.entrySet()){
							int tid = entryC.getKey();
							// if there is an occurence
				    		if(rule1.tidsJ.contains(tid)) {
								// add the tid of the sequence to the tidset of JU{c}
		    	    			tidsJC.add(tid);
		    	    			// calculate last occurence of JU{c} depending on if
		    	    			// the last occurence of J is before the last occurence
		    	    			// of c or not.
								Occurence occurrenceC = entryC.getValue();
		    	    			Occurence occurenceJ = rule1.occurencesJ.get(tid);
		    	    			if(occurrenceC.lastItemset < occurenceJ.lastItemset){
		    	    				occurencesJC.put(tid, occurrenceC);
		    	    			}else{
		    	    				occurencesJC.put(tid, occurenceJ);
		    	    			}
				    		}
	    				}
	    			}
	    			
	    			// Create rule I ==> J U{c} and calculate its confidence   
	    	    	// defined as:  sup(I -->J U{c}) /  sup(I)	
	    			double confI_JC = ((double)tidsI_JC.size()) / eclass.tidsI.size();
					int[] itemsetJC = new int[rule1.itemsetJ.length+1];
					System.arraycopy(rule1.itemsetJ, 0, itemsetJC, 0, rule1.itemsetJ.length);
					itemsetJC[rule1.itemsetJ.length]= c;
				
					// if the confidence is enough
					if(confI_JC >= minConfidence){
						// then it is a valid rule so save it
						saveRule(tidsI_JC, confI_JC, eclass.itemsetI, itemsetJC);
					}
					// recursively try to expand the left and right side
					// of the rule
					RightRule rightRule =
							new RightRule(itemsetJC, tidsJC, tidsI_JC, occurencesJC);
					rulesForRecursion.rules.add(rightRule);
					LeftRule leftRule = new LeftRule(eclass.itemsetI, eclass.tidsI, tidsI_JC);
					store.register(leftRule, itemsetJC, tidsJC, eclass.occurencesI, occurencesJC); // register for left expansion
	    		}
			}

			if(rulesForRecursion.rules.size() >1) {
				expandRight(rulesForRecursion, false);
			}
		}
    	// check the memory usage
    	MemoryLogger.getInstance().checkMemory();
    	
	}

	/**
	 * This method calculate the frequency of each item in one database pass.
	 * Then it remove all items that are not frequent.
	 * @param database : a sequence database 
	 * @return A map such that key = item
	 *                         value = a map  where a key = tid  and a value = Occurence
	 * This map allows knowing the frequency of each item and their first and last occurence in each sequence.
	 */
	private Map<Integer, Map<Integer, Occurence>> calculateFrequencyOfEachItem(SequenceDatabase database) {
		// (1) Count the support of each item in the database in one database pass
		mapItemCount = new HashMap<Integer, Map<Integer, Occurence>>(); // <item, Map<tid, occurence>>
		
		// for each sequence in the database
		for(int k=0; k< database.size(); k++){
			Sequence sequence = database.getSequences().get(k);
			// for each itemset in that sequence
			for(short j=0; j< sequence.getItemsets().size(); j++){
				List<Integer> itemset = sequence.get(j);
				// for each item in that itemset
				for(Integer itemI : itemset){
					
					// get the map of occurences of that item
					Map<Integer, Occurence> occurences = mapItemCount.get(itemI);
					// if this map is null, create a new one
					if(occurences == null){
						occurences = new HashMap<Integer, Occurence>();
						mapItemCount.put(itemI, occurences);
						occurences.put(k, new Occurence(j, j));
					}else {
						// then update the occurence by adding j as the 
						// last occurence in sequence k
						Occurence occurence = occurences.get(k);
						if(occurence == null){
							occurences.put(k, new Occurence(j, j));
						}else{
							occurence.lastItemset = j;
						}
					}
				}
			}
		}
		
		// return the map of occurences of items
		return mapItemCount;
	}


	private void generateMatrix(SequenceDatabase database) {
		// for each sequence
		for(Sequence sequence : database.getSequences()){
			
			// to remember which items have been processed
			Set<Integer> alreadyProcessed = new HashSet<Integer>();
			
			// for each itemset
			for(List<Integer> itemsetj : sequence.getItemsets()) {
				
				// for each item
				for(Integer itemk : itemsetj) {
					if(alreadyProcessed.contains(itemk) || mapItemCount.get(itemk).size() < minsuppRelative){
						continue;
					}
					
					/// for item k we should update the matrix with each item co-occurring 
					Set<Integer> alreadyProcessedWithRespectToK = new HashSet<Integer>();
					for(List<Integer> itemsetjj : sequence.getItemsets()) {
						
						for(Integer itemkk : itemsetjj) {
							if(itemkk == itemk || alreadyProcessedWithRespectToK.contains(itemkk)
									||  mapItemCount.get(itemkk).size() < minsuppRelative){
								continue;
							}
							
							matrix.increaseCountOfPair(itemk, itemkk);
							alreadyProcessedWithRespectToK.add(itemkk);
						}
					}
					// end second loop
					alreadyProcessed.add(itemk);
				}
			}
		}
	}
	


	/**
	 * Save a rule I ==> J to the output file
	 * @param tidsIJ the tids containing the rule
	 * @param confIJ the confidence
	 * @param itemsetI the left part of the rule
	 * @param itemsetJ the right part of the rule
	 * @throws IOException exception if error writing the file
	 */
	private void saveRule(Set<Integer> tidsIJ, double confIJ, int[] itemsetI, int[] itemsetJ) {
		// increase the number of rule found
		ruleCount++;
		
		// create a string buffer
		StringBuilder buffer = new StringBuilder();
		
		// write itemset 1 (antecedent)
		for(int i=0; i<itemsetI.length; i++){
			buffer.append(itemsetI[i]);
			if(i != itemsetI.length -1){
				buffer.append(",");
			}
		}
		
		// write separator
		buffer.append(" ==> ");
		
		// write itemset 2  (consequent)
		for(int i=0; i<itemsetJ.length; i++){
			buffer.append(itemsetJ[i]);
			if(i != itemsetJ.length -1){
				buffer.append(",");
			}
		}
		// write support
		buffer.append(" #SUP: ");
		buffer.append(tidsIJ.size());
		// write confidence
		buffer.append(" #CONF: ");
		buffer.append(confIJ);
		try {
			writer.write(buffer.toString());
			writer.newLine();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	

	
	/**
	 * Print statistics about the last algorithm execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  ERMiner - STATS ========");
		System.out.println("Sequential rules count: " + ruleCount);
		System.out.println("Total time: " + (timeEnd - timeStart) + " ms");
		System.out.println("Candidates pruned (%)" + candidatePrunedCount + " of " + totalCandidateCount);
		System.out.println("Max memory: " + MemoryLogger.getInstance().getMaxMemory());
		System.out.println("==========================================");
	}

}
