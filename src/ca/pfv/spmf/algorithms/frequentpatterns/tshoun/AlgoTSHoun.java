package ca.pfv.spmf.algorithms.frequentpatterns.tshoun;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ca.pfv.spmf.algorithms.frequentpatterns.two_phase.ItemsetsTP;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This is an implementation of the "TSHOUN" algorithm for High on-shelf utility itemsets Mining
 * while considering negative profit value.
 * TSHOUN is described in this paper: <br/><br/>
 * 
 *  Guo-Cheng Lan, Tzung-Pei Hong, Jen-Peng Huang, Vincent S. Tseng: 
 *  On-shelf utility mining with negative item values. 
 *  Expert Syst. Appl. 41(7): 3450-3459 (2014)<br/><br/>
 *  
 *  
 *  WARNING : THIS CODE PROBABLY ASSUME THAT TRANSACTIONS ARE SORTED IN LEXICOGRAPHICAL ORDER
 *
 * @see ItemsetsTP
 * @see ItemsetTP
 * @see TransactionWithPeriod
 * @see UtilityTransactionDatabase
 * @author Philippe Fournier-Viger 2014
 */
public class AlgoTSHoun {

	// the set of high utility itemsets found by the algorithm
//	private HashTable highUtilityItemsets = null;
	// the database
	protected DatabaseWithPeriods database;
	
	// the min utility threshold
	double minUtilityRatio;

	// for statistics
	long startTimestamp = 0;  // start time
	long endTimestamp = 0; // end time
	private int candidatesCount; // the number of candidates generated
	
	
	//  Information common to all periods
	Map<Integer, BitSet> mapItemPeriod = null; 
	List<Integer> periodUtilities = null;
	
	Map<Integer, Pair> mapItemExactEstUtility = null; 
	Set<Integer> negativeItems = null;
	HashTable hashtable = null;
	List<Integer> candidate1 = null;
	
	int resultCount = 0;
	
	// writer to write the output file 
	BufferedWriter writer = null;  
	
	boolean DEBUG = false ;
	
	/**
	 * Default constructor
	 */
	public AlgoTSHoun() {
	}

	/**
	 * Run the Two-phase algorithm
	 * @param database  a transaction database containing utility information.
	 * @param minUtilityRatio the min utility threshold
	 * @param periodCount 
	 * @throws IOException 
	 */
	public void  runAlgorithm(DatabaseWithPeriods database, double minUtilityRatio, String output, int periodCount) throws IOException {
		// save the parameters
		this.database = database;
		this.minUtilityRatio = minUtilityRatio;
		
		writer = new BufferedWriter(new FileWriter(output));
		
		// reset the utility to check the memory usage
		MemoryLogger.getInstance().reset();
		// record start time
		startTimestamp = System.currentTimeMillis();
		// reset HUI count
		candidatesCount = 0;

		hashtable = new HashTable(10000);
		mapItemPeriod = database.getMapItemPeriod();
		negativeItems = database.getNegativeItems();
		periodUtilities = database.getPeriodUtilities();	
		mapItemExactEstUtility = database.getMapItemExactEstUtility();
		
		// If in debugging mode, we will print the period utilities
		if(DEBUG){
			System.out.println("===== PERIOD UTILITIES =====");
			for (int i = 0; i < database.periodCount; i++) {
				int periodUtility = database.getPeriodUtility(i);
				System.out.println(" period " + i + "  utility: " + periodUtility);
				
			}
		}
		
		
		// ======== FIND  1-ITEMSET THAT ARE HIGH ON-SHELF UTILITY  AND 1-CANDIDATES
		for(Entry<Integer, Pair> entry: database.getMapItemExactEstUtility().entrySet()) {
			int item = entry.getKey();
			Pair pair = entry.getValue();
		
			BitSet periodsOfItem = mapItemPeriod.get(item);
			boolean isPromisingInAtLeastOnePeriod = false;
			int sumPeriodUtility = 0;
			// for each period containing the item
			for (int i = periodsOfItem.nextSetBit(0); i >= 0; 
					i = periodsOfItem.nextSetBit(i+1)) {
			    int periodUtility = periodUtilities.get(i);
				if(isPromisingInAtLeastOnePeriod == false) {
					if(calculateRelativeUtility(periodUtility, pair.estimatedUtility[i]) >= minUtilityRatio) {
						isPromisingInAtLeastOnePeriod = true;
					}
				}
				sumPeriodUtility += periodUtility;
			}
			// IF THE ITEM IS A HIGH ON-SHELF UTILITY ITEM

//			// IF IT IS NOT A CANDIDATE THEN WE WILL NOT CONSIDER THIS ITEM ANYMORE
			if(isPromisingInAtLeastOnePeriod == false) {
				database.getAllItems().remove(item);
			}else{
				double relativeUtility = calculateRelativeUtility(sumPeriodUtility, pair.exactUtility);
				if(relativeUtility>= minUtilityRatio) {
					writeOutItem(item, pair.exactUtility,relativeUtility);
				}
			}
		}
		
		candidate1 = new ArrayList<Integer>();
		candidate1.addAll(database.getMapItemExactEstUtility().keySet());
		Collections.sort(candidate1);
		
		if(candidate1.size() == 0) {
			MemoryLogger.getInstance().checkMemory();
			endTimestamp = System.currentTimeMillis();		
			writer.close();
			return;
		}
		
		// =====  OPTIMIZATION: Scan the database once more to remove unpromising items  AND
		// === delete transactions containing only unpromising items AND
		// ==== adjust the transaction utilities by removing utility of unpromising items ===/
		if(DEBUG) {
			System.out.println("REMOVE UNPROMISING ITEMS");
		}
		Iterator<TransactionWithPeriod> iterTrans = database.getTransactions().iterator();
		while (iterTrans.hasNext()) {
			TransactionWithPeriod trans =  iterTrans.next();
			Iterator<ItemUtility> iter = trans.getItems().iterator();
			while (iter.hasNext()) {
				ItemUtility itemUtility = iter.next();
				if(database.getAllItems().contains(itemUtility.item) == false) {
					int utility = itemUtility.utility;
					if(utility >0) {
						trans.transactionUtility -= utility;
					}
					iter.remove();
				}
			}
			if(trans.size() == 0) {
				iterTrans.remove();
			}
		}
		if(DEBUG) {
			System.out.println("END REMOVING UNPROMISING ITEMS");
		}
		
		
		// sort the transaction by periods in the database so that they are contiguous
		Collections.sort(database.getTransactions(), new Comparator<TransactionWithPeriod>() {
			public int compare(TransactionWithPeriod t1, TransactionWithPeriod t2) {
				if(t1 == t2) { // maybe not necessary
					return 0;
				}
				int compare = t1.getPeriod() - t2.getPeriod();
				if(compare !=0) {
					return compare;
				}
				
				return  t1.getItems().get(0).item - t2.getItems().get(0).item;
				// else determine the smallest according to lexical order
//				boolean t1IsShorter = t1.getItems().size() < t2.getItems().size();
//				int size = t1IsShorter ? t1.getItems().size(): t2.getItems().size();
//				for(int i=0; i< size; i++) {
//					if(t1.getItems().get(i).item < t2.getItems().get(i).item) {
//						return -1;
//					}
//					if(t1.getItems().get(i).item > t2.getItems().get(i).item) {
//						return 1;
//					}
//				}
//				if(t1.getItems().size() == t2.getItems().size()) {
//					return 0;
//				}
//				return t1IsShorter ? -1 : 1;
			}});
		
//		database.printDatabase();
		System.out.println();

		// ===  WE WILL CALCULATE WHERE EACH PERIOD STARTS AND ENDS ==========
		int [] periodsStart = new int[database.getPeriodCount()];
		int [] periodsEnd = new int[database.getPeriodCount()];
		
		int startIndex = 0;
		int endIndex = -1;
		for(short currentPeriod =0; currentPeriod < database.getPeriodCount(); currentPeriod++) {
			// determine the end index for that period
			// If the last period, then the period end with the last transaction
			if(currentPeriod == database.getPeriodCount() -1) {
				endIndex = database.getTransactions().size()-1;
			}else {
				endIndex = binarySearch(currentPeriod, database.getTransactions(), startIndex) -1;
			}
			
			// save where the period starts and end
			periodsStart[currentPeriod] = startIndex;
			periodsEnd[currentPeriod] = endIndex;
//
//			if(debug) {
//			System.out.println("start : " + startIndex + " end : " + endIndex 
//					+ "   periodStart : " + database.getTransactions().get(startIndex).getPeriod()
//					+ "   periodEnd : " + database.getTransactions().get(endIndex-1).getPeriod());
//			}
			//			
			// set the start index to the begining of the next period
			startIndex = endIndex + 1;
		}
		// ==========================================================

		if(DEBUG) {
			System.out.println("START CALCULATING TU OF 2-candidates");
		}
		
		// NOW WE WILL PROCESS EACH PERIOD TO FIND 2-ITEMS CANDIDATES
		// for each transaction
		Map<Integer, Map<Integer, Pair>> mapItemItemUtility = new HashMap<Integer, Map<Integer, Pair>>();
		for(TransactionWithPeriod transact: database.getTransactions()){

			int period = transact.getPeriod();
			
			// for each item I
			for(int i=0; i< transact.size(); i++) {
				int itemI = transact.get(i).item;
				
				// if it is not a candidate of size 1, skip it
				if(candidate1.contains(itemI) == false){
					continue;
				}
				
				Map<Integer, Pair> mapItemUtility = mapItemItemUtility.get(itemI);
				if(mapItemUtility == null) {
					mapItemUtility = new HashMap<Integer, Pair>();
					mapItemItemUtility.put(itemI, mapItemUtility);
				}
				
				// for each item J
				for(int j=i+1; j< transact.size(); j++) {
					
					// update the transaction utility of the pair
					int itemJ = transact.get(j).item;
					
					// if it is not a candidate of size 1, skip it
					if(candidate1.contains(itemJ) == false){
						continue;
					}
					
					Pair pairIJ = mapItemUtility.get(itemJ);
					if(pairIJ == null) {
						pairIJ = new Pair(periodCount);
						mapItemUtility.put(itemJ, pairIJ);
					}
					pairIJ.exactUtility += (transact.get(i).utility + transact.get(j).utility);
					if(pairIJ.estimatedUtility[period] == null) {
						pairIJ.estimatedUtility[period] = transact.getTransactionUtility();
					}else{
						pairIJ.estimatedUtility[period] += transact.getTransactionUtility();
					}
				}
			}
		}
		
		if(DEBUG) {
			System.out.println(" Removing unpromising 2-itemsets ");
			System.out.println(" and output HOU 2-itemsets ");
		}
		
//		System.out.println("exact util of 39 175 : " + mapItemItemUtility.get(39).get(175).exactUtility);
//		System.out.println("est. util of 39 175 : " + mapItemItemUtility.get(39).get(175).estimatedUtility);
//		System.out.println("est. util of 39 48 : " + mapItemItemUtility.get(39).get(48).exactUtility);
//		System.out.println("est. util of 39 48 : " + mapItemItemUtility.get(39).get(48).estimatedUtility);
		
		
		int candidate2count = 0;
		List<ItemsetTP> candidates2 = new ArrayList<ItemsetTP>();
		// remove unpromising TWU of size 2
		for(Entry<Integer, Map<Integer, Pair>> entryMap : mapItemItemUtility.entrySet()) {
			int itemI = entryMap.getKey();

			Iterator<Entry<Integer, Pair>> iter = entryMap.getValue().entrySet().iterator();
			BitSet bitsetI = mapItemPeriod.get(itemI);
			while (iter.hasNext()) {
				Entry<Integer, Pair> entryJ = iter.next();
				int itemJ = entryJ.getKey();
				
//				if(itemI == 4 && itemJ == 5) {
//					System.out.println("TEST");
//				}
				
				boolean isPromisingInAtLeastOnePeriod = false;
				Integer estimatedUtilityIJ[] = entryJ.getValue().estimatedUtility;
				int sumOfPeriodUtility = 0;
				for(int i=0; i<estimatedUtilityIJ.length; i++) {
					// if {i,j} appears in the i-th period
					if(estimatedUtilityIJ[i] != null) {
						int twuIJ =0;
						if(isPromisingInAtLeastOnePeriod == false) {
							if(calculateRelativeUtility(periodUtilities.get(i), estimatedUtilityIJ[i]) >= minUtilityRatio) {
								isPromisingInAtLeastOnePeriod = true;
							}
						}
						sumOfPeriodUtility += periodUtilities.get(i);
					}
				}
				
				// if {I,J} is not a candidate remove it
				if(isPromisingInAtLeastOnePeriod == false) {
					iter.remove();
				}else {
					int[] array = new int[]{itemI, itemJ};
					ItemsetTP itemsetTP = new ItemsetTP(array);
					candidates2.add(itemsetTP);
					candidate2count++;
					
					int exactUtilityIJ = entryJ.getValue().exactUtility;
					// if HIGH ON SHELF 2-ITEMSET, THEN OUTPUT IT
					double relativeUtility = calculateRelativeUtility(sumOfPeriodUtility, exactUtilityIJ);
					if(relativeUtility >= minUtilityRatio) {
						writeOut(array, exactUtilityIJ, relativeUtility);
					}
				}
			}
		}
		
		if(candidates2.size() == 0) {
			MemoryLogger.getInstance().checkMemory();
			endTimestamp = System.currentTimeMillis();		
			writer.close();
			return;
		}
		
		// Sort candidates of size 2 by lexicographical order.
		// Important because they were not ordered in the map used previously. 
		// If we avoid this step, we will miss some patterns.
		Collections.sort(candidates2, new Comparator<ItemsetTP>(){
			public int compare(ItemsetTP o1, ItemsetTP o2) {
				if(o1.items[0] < o2.items[0]) {
					return -1;
				}else if(o1.items[0] > o2.items[0]) {
					return 1;
				}
				
				if(o1.items[1] < o2.items[1]) {
					return -1;
				}else if(o1.items[1] > o2.items[1]) {
					return 1;
				}
				return 0;
			}
			
		});
		
		MemoryLogger.getInstance().checkMemory();
		if(DEBUG) {
			System.out.println("FINISHED CALCULATING TU of 2-candidates  (" + candidate2count + ")");
			System.out.println("START MINING PERIODS FOR ALL CANDIDATES");
		}
		
		//  ================ FOR EACH PERIOD, PERFORM THE MINING PROCESS ============
		for(short currentPeriod =0; currentPeriod < database.getPeriodCount(); currentPeriod++) {
			startIndex = periodsStart[currentPeriod];
			endIndex = periodsEnd[currentPeriod];
			
			if(DEBUG) {
				System.out.println("PERIOD " + currentPeriod);
			}
			
			List<TransactionWithPeriod> periodDB = database.getTransactions().subList(startIndex, endIndex+1);
			int periodUtility = periodUtilities.get(currentPeriod);
			
			if(DEBUG){
				System.out.println("TRANSACTIONS IN THIS PERIOD");
				for(TransactionWithPeriod trans : periodDB){
					System.out.println(trans.toString());
				}
				System.out.println();
			}

			performMiningOnPeriod(periodDB, periodUtility, candidates2, currentPeriod);
			 	
			 	
//			System.out.println("start : " + startIndex + " end : " + endIndex 
//					+ "   periodStart : " + database.getTransactions().get(startIndex).getPeriod()
//					+ "   periodEnd : " + database.getTransactions().get(endIndex-1).getPeriod());
			
			// set the start index to the begining of the next period
			startIndex = endIndex+1;
		}
		
		if(DEBUG) {
			System.out.println("ENDED MINING PERIODS FOR ALL CANDIDATES");
		}
		MemoryLogger.getInstance().checkMemory();
		
		// ======= SCAN THE DATABASE TO CALCULATE THE EXACT UTILITY OF CANDIDATES IN HASHTABLE ========
		for (List<ItemsetTP> entryHash: hashtable.table) {
			if(entryHash == null) {
				continue;
			}
			for(ItemsetTP itemset : entryHash) {
				// Calculate the periods of this itemset
				BitSet periods = (BitSet)mapItemPeriod.get(itemset.items[0]).clone();
				for(int i=1; i< itemset.items.length; i++) {
					periods.and(mapItemPeriod.get(itemset.items[i]));
				}
				
				if(DEBUG){
					// To show the itemsets for debugging
					System.out.println(Arrays.toString(itemset.items));
					System.out.println();
				}
				
				int exactUtility = 0;
				int sumPeriodUtility = 0;
//				// for each period containing the itemset
	loop:		for (int period = periods.nextSetBit(0); period >= 0; 
						period = periods.nextSetBit(period+1)) {
					// sum the period utility
					sumPeriodUtility += periodUtilities.get(period);
		
					// check if the utility of the itemset in that period has been already been calculated
					for(ItemsetTP.PeriodUtility pair : itemset.listPeriodUtility) {
						if(pair.period == period) {
							exactUtility += pair.utility;
							continue loop;
						}
					}
					// Otherwise, we will calculate it
					
				    // get the transactions of that period
					startIndex = periodsStart[period];
					endIndex = periodsEnd[period];
					List<TransactionWithPeriod> periodDB = database.getTransactions().subList(startIndex, endIndex+1);
					// check each transaction
					for(int z = 0; z < periodDB.size(); z++) {
						TransactionWithPeriod transaction = periodDB.get(z);
						
						// get the utility of the itemset in that transaction
						exactUtility +=  containsOrEquals(transaction.getItems(), itemset.items);
					}
				}
				
				// if the itemset IS A HIGH ON SHELF UTILITY ITEMSET
				double relativeUtility = calculateRelativeUtility(sumPeriodUtility, exactUtility);
				if(relativeUtility >= minUtilityRatio) {
//					System.out.println(Arrays.toString(itemset.items) + " " + exactUtility);
					writeOut(itemset.items, exactUtility, relativeUtility);
				}
			}
			
		}
		// ===========================================
		
		// check memory usage
		MemoryLogger.getInstance().checkMemory();
		endTimestamp = System.currentTimeMillis();		
		writer.close();
	}

	/**
	 * Write an itemset to the output file
	 * @param prefix the itemset
	 * @param exactUtility the utility
	 * @param relativeUtility the relative utility
	 * @throws IOException exception if error writing to file
	 */
	private void writeOut(int[] prefix, int exactUtility, double relativeUtility) throws IOException {
		//Create a string buffer
		StringBuilder buffer = new StringBuilder();
		// append the prefix
		for (int i = 0; i < prefix.length; i++) {
			buffer.append(prefix[i]);
			buffer.append(' ');
		}
		buffer.append("#UTIL: ");
		buffer.append(exactUtility);
		// append the relative utility value
		buffer.append(" #RUTIL: ");
		buffer.append(relativeUtility);
		writer.write(buffer.toString());
		writer.newLine();
		resultCount++;
	}

	/**
	 * Write an itemset containing a single item to the output file
	 * @param prefix the itemset
	 * @param exactUtility the utility
	 * @param relativeUtility the relative utility
	 * @throws IOException exception if error writing to file
	 */
	private void writeOutItem(int item, int exactUtility, double relativeUtility) throws IOException {
		writer.write(item + " #UTIL: " + exactUtility);
		writer.newLine();

		// append the relative utility value
		writer.append(" #RUTIL: ");
		writer.append("" + relativeUtility);
		resultCount++;
	}
	
	/**
	 * Perform a binary search on the list of transaction to find the first transaction of a given period
	 * @param keyPeriod the period to be searched
	 * @param list a list of transactions
	 * @return
	 */
    public static int binarySearch(int keyPeriod, List<TransactionWithPeriod> list, int startIndex) {
        int lo = startIndex;
        int hi = list.size() - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            if (compareForBinarySearch(keyPeriod, list, mid) > 0) {  // period < list.get(mid).getPeriod()
            	hi = mid - 1;
            }
            else if (compareForBinarySearch(keyPeriod, list, mid) < 0) {
            	lo = mid + 1;
            }
            else {
            	return mid;
            }
        }
        return -1;
    }

    /**
     * This method search for the last transactions of a given period in a list of transactions
     * @param keyPeriod the period to be searched
     * @param list the list of transactions
     * @param mid  the current position
     * @return -1 if the current position is smaller than the position that we search, 0 if equal, or 1 otherwise
     */
	private static int compareForBinarySearch(int keyPeriod, List<TransactionWithPeriod> list, int mid) {
		if(list.get(mid).getPeriod() == keyPeriod) {
			return -1;
		}
		
		if(list.get(mid).getPeriod() == keyPeriod+1) {
			if(list.get(mid-1).getPeriod() == keyPeriod) {
				return 0;
			}
		}

		return 1;
	}
	
	/**
	 * Mine high utility periodical patterns from a given period
	 * @param database  the transactions of the period
	 * @param periodUtility  the total utility of the period
	 * @param candidates2 the candidates of size 2 that should be used for starting the mining
	 * @param period  the id of the period
	 */
	public void performMiningOnPeriod(List<TransactionWithPeriod> database, 
			int periodUtility, List<ItemsetTP> candidates2, short period) {


		MemoryLogger.getInstance().checkMemory();
		// ===================  PHASE 1: GENERATE CANDIDATES OF SIZE 3 =================== 
		// First, we create the level of candidate itemsets of size 1
		List<int[]> candidatesSize3 = new ArrayList<int[]>();
		for(int i=0; i < candidates2.size(); i++) {
			ItemsetTP itemset1 = candidates2.get(i);
			
			for(int j=i+1; j < candidates2.size(); j++) {
				ItemsetTP itemset2 = candidates2.get(j);
				
//				if(itemset1.items[0] == 3 && itemset1.items[1] ==  
//						&& itemset2.items[0] == 3 && itemset2.items[1] == 7) {
//					System.out.println("TEST2222");
//				}
				
				
				if(itemset1.items[0] > itemset2.items[0]) {
					break;
				}
				// else
				if(itemset1.items[0] == itemset2.items[0]) {
					int newItemset[] = new int[3];
					newItemset[0] = itemset1.items[0];
					newItemset[1] = itemset1.items[1];
					newItemset[2] = itemset2.items[1];
					candidatesSize3.add(newItemset);
				}
			}
		}
		
		MemoryLogger.getInstance().checkMemory();
		if(DEBUG) {
			System.out.println(" CANDIDATE size 3 count " + candidatesSize3.size());
		}
		//==================================================================================
		// CALCULATE THE EXACT AND ESTIMATED UTILITY OF EACH CANDIDATE ...
		pruneCandidatesAndCalculateExactUtility(database, periodUtility, period, candidatesSize3);
		
		// =============   RECURSIVE GENERATION OF CANDIDATES =================
		List<int[]> previousCandidates = candidatesSize3;
		while(previousCandidates.size() >0) {
			List<int[]> nextCandidates = generateCandidateSizeK(previousCandidates);
			
			pruneCandidatesAndCalculateExactUtility(database, periodUtility, period, nextCandidates);
			previousCandidates =  nextCandidates;
		}
		//=====================================================================

		MemoryLogger.getInstance().checkMemory();
	}

	/**
	 * Calculate the exact and estimated utility of each candidate in the list of candidates
	 * @param database the database
	 * @param periodUtility the utility of the period
	 * @param period the period id
	 * @param candidates a list of candidate itemsets
	 */
	private void pruneCandidatesAndCalculateExactUtility(
			List<TransactionWithPeriod> database, int periodUtility, short period, List<int[]> candidates) {

		Iterator<int[]> iter = candidates.iterator();
		while (iter.hasNext()) {
			int[] itemset = iter.next();
			
//			System.out.println(Arrays.toString(itemset));
//			System.out.println();
//			
			
			// DEBUGING
//			if(itemset.length == 3 && itemset[0] == 3 && itemset[1] == 4 && itemset[2] == 5 ) {
//				System.out.println();
//			}
			
			int estimatedUtility = 0;
			int exactUtility = 0;
			
			for(TransactionWithPeriod trans : database) {
				// MAJOR OPTIMIZATION
				if(trans.getItems().get(0).item > itemset[0] ) {
//				if(compare(itemset, trans.getItemsUtilities()) > 0) {
					break;
				}
				
//				System.out.print(" TRANS " );
//				for(int i = 0; i < trans.getItems().size(); i++){
//					System.out.print("   " + trans.getItems().get(i).item + " " + "");
//				}
//				System.out.println();
				
				
				
				// check if the itemset is contained in the transaction
				int utilityInThatTransaction = containsOrEquals(trans.getItems(), itemset);
				if(utilityInThatTransaction > 0) {
					estimatedUtility += trans.transactionUtility;
					exactUtility += utilityInThatTransaction;
				}
			}

			
			// Remove candidate if the utility is too low
			if(estimatedUtility / Math.abs((double)periodUtility) < minUtilityRatio) {
				iter.remove();
			}
//			// else we keep it
			if(exactUtility / Math.abs((double)periodUtility) >= minUtilityRatio) {
				int hashcode = hashtable.hashCode(itemset);
				ItemsetTP itemsetRetrieved = hashtable.retrieveItemset(itemset, hashcode);
				
				
				
				if(itemsetRetrieved == null) {
					itemsetRetrieved = new ItemsetTP(itemset);
					hashtable.put(itemsetRetrieved, hashcode);
				}
				

				
				itemsetRetrieved.setPeriodUtility(period, exactUtility);
			}
		}

		MemoryLogger.getInstance().checkMemory();
	}

	/**
	 * Get the utility of an itemset in a transaction
	 * It assumes that itemsets are sorted according to the lexical order.
	 * @param list the transaction
	 * @param items the itemset
	 * @return >0 if the first itemset contains the  itemset
	 */
	public static int containsOrEquals(List<ItemUtility> list, int[] items){
			int utility = 0;
			// for each item in the first itemset
loop1:		for(int i =0; i < items.length; i++){
				// for each item in the second itemset
				for(int j =0; j < list.size(); j++){
					// if the current item in itemset1 is equal to the one in itemset2
					// search for the next one in itemset1
					if(list.get(j).item == items[i]){
						utility += list.get(j).utility;
						continue loop1;
				    // if the current item in itemset1 is larger
					// than the current item in itemset2, then
					// stop because of the lexical order.
					}else if(list.get(j).item > items[i]){
						return 0;
					}
				}
				// means that an item was not found
				return 0;
			}
			// if all items were found, return true.
	 		return utility;
	}

	
	/**
	 * Generate candidate HWTUI of size K by using HWTUIs of size k-1
	 * @param levelK_1   HWTUIs of size k-1
	 * @param candidatesHTWUI  structure to store the HWTUIs
	 * @return  candidates of size K
	 */
	protected List<int[]> generateCandidateSizeK(List<int[]> levelK_1) {
		
		List<int[]> candidatesK = new ArrayList<int[]>();
		
	// For each itemset I1 and I2 of level k-1
	loop1:	for(int i=0; i< levelK_1.size(); i++){
				int[] itemset1 = levelK_1.get(i);
	loop2:		for(int j=i+1; j< levelK_1.size(); j++){
					int[] itemset2 = levelK_1.get(j);

					
				// we compare items of itemset1  and itemset2.
				// If they have all the same k-1 items and the last item of itemset1 is smaller than
				// the last item of itemset2, we will combine them to generate a candidate
				for(int k=0; k< itemset1.length; k++){
					// if they are the last items
					if(k == itemset1.length-1){ 
						// the one from itemset1 should be smaller (lexical order) 
						// and different from the one of itemset2
						if(itemset1[k] >= itemset2[k]){  
							continue loop1;
						}
					}
					// if they are not the last items, and 
					else if(itemset1[k] < itemset2[k]){ 
						continue loop2; // we continue searching
					}
					else if(itemset1[k] > itemset2[k]){ 
						continue loop1;  // we stop searching:  because of lexical order
					}
				}
				
				// NOW COMBINE ITEMSET 1 AND ITEMSET 2
				int[] newItemset = new int[itemset2.length+1];
				System.arraycopy(itemset1, 0, newItemset, 0, itemset1.length);
				newItemset[itemset2.length] = itemset2[itemset2.length-1];
				candidatesK.add(newItemset);
			}
		}
		// return candidates HTWUIs of size K
		return candidatesK;
	}
	
	/**
	 * Calculate the relative utility 
	 * @param sumPeriodUtility the sum of the period utilities
	 * @param utility  the utility value
	 * @return the relative utility
	 */
	private double calculateRelativeUtility(int sumPeriodUtility, double utility) {
		if(sumPeriodUtility == 0){
			return 0;
		}
		return utility / Math.abs(sumPeriodUtility);
	}

	/**
	 * Print statistics about the latest algorithm execution to System out.
	 */
	public void printStats() {
		System.out.println("=============  TS-HOUN ALGORITHM v2.02 - STATS =============");
		System.out.println(" Transactions count from database : "+ database.size());
		System.out.println(" Candidates count : " + candidatesCount); 
		System.out.println(" Memory : " + MemoryLogger.getInstance().getMaxMemory() + " MB"); 
		System.out.println(" HOU count : " + resultCount); 
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println("===================================================");
	}
}