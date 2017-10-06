package ca.pfv.spmf.algorithms.frequentpatterns.tshoun;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.pfv.spmf.algorithms.frequentpatterns.two_phase.AlgoTwoPhase;


/**
 * This class represents a transaction database with utility values, 
 * as used by the Two-Phase algorithm for high 
 * utility itemset mining.
 *
 * @see AlgoTwoPhase
 * @see TransactionWithPeriod
 * @author Philippe Fournier-Viger
 */
public class DatabaseWithPeriods {
	
	int periodCount;

	// this is the set of items in the database
	private final Set<Integer> allItems = new HashSet<Integer>();

	// this is the set of items in the database
	private final Set<Integer> allNegativeItems = new HashSet<Integer>();

	// this is the list of transactions in the database
	private final List<TransactionWithPeriod> transactions = new ArrayList<TransactionWithPeriod>();

	// the total utility of each period starting from 0
	private List<Integer> periodsTotalUtilities = new ArrayList<Integer>();
	
	// the list of periods for each item, key: item  value: periods where the item occurs.
	private Map<Integer, BitSet> mapItemPeriods =  new HashMap<Integer, BitSet>(); 
	
	// map indicating the utility of each item
	Map<Integer, Pair> mapItemUtility = new HashMap<Integer, Pair>();
	
	public int smallestID = Integer.MAX_VALUE;
	public  int largestID = 0;

	// ==== FOR THE SCALABILITY EXPERIMENT
	public int maxSEQUENCECOUNT = Integer.MAX_VALUE;
	// ===================================
	
	public DatabaseWithPeriods(int periodCount) {
		this.periodCount = periodCount;
		this.periodsTotalUtilities = new ArrayList<Integer>(periodCount);
	}
	


	/** 
	 * Get the map indicating the utility of each item
	 * @return the map
	 */
	public Map<Integer, Pair> getMapItemExactEstUtility() {
		return mapItemUtility;
	}
	
	
	/**
	 * Get  the total utility of each period starting from 0
	 * @return  the list of period utilities
	 */
	public List<Integer> getPeriodUtilities() {
		return periodsTotalUtilities;
	}
	
	/**
	 * Get the set of periods where an item occurs
	 * @param item the item
	 * @return the set of periods as a bitset
	 */
	BitSet getPeriodsOfItem(int item) {
		return mapItemPeriods.get(item);
	}
	

	/** Add a period to the list of periods of a given item.
	 * @param period the period
	 * @param item the item
	 */
	private void addPeriodToListOfPeriodsOfItem(int period, int item) {
		BitSet bitset = mapItemPeriods.get(item);
		if(bitset == null) {
			bitset = new BitSet();
			mapItemPeriods.put(item, bitset);
		}
		bitset.set(period);
	}
	


	/**
	 * Get the set of periods for each item.
	 * @return a map where key: item  value:  a bitset indicating the periods of the item
	 */
	public Map<Integer, BitSet> getMapItemPeriod() {
		return mapItemPeriods;
	}
	
	/**
	 * Get the set of negative items in this database
	 * @return a Set of items
	 */
	public Set<Integer> getNegativeItems() {
		return allNegativeItems;
	}

	
	/**
	 * Get the number of periods
	 * @return the number of periods
	 */
	public int getPeriodCount() {
		return periodsTotalUtilities.size();
	}
	
	/**
	 * Get the total utility of a given period
	 * @param period the period
	 * @return the total utility
	 */
	public int getPeriodUtility(int period) {
		return periodsTotalUtilities.get(period);
	}
	

	/**
	 * Add the utility of a transaction to the total utility of that period
	 * @param period  the period (an integer)
	 * @param transactionUtility the utility of the transaction to be added
	 */
	private void incrementPeriodUtility(int period, int transactionUtility) {
		// check if the period was seen before
		boolean notSeenBefore = periodsTotalUtilities.size() < (period +1);
		if(notSeenBefore) {
			// add the utility 0 for all the period before this one that were not seen
			while(periodsTotalUtilities.size() < period) {
				periodsTotalUtilities.add(0);
			}
			// add the utility of the transaction for the current period
			periodsTotalUtilities.add(transactionUtility);
		}else {
			// sum the period utility with the transaction utility and save it 
			// as the period utility
			int sumUtility =  periodsTotalUtilities.get(period) + transactionUtility;
			periodsTotalUtilities.set(period, sumUtility);
		}
		
	}
	

	/**
	 * Load a transaction database from a file.
	 * @param path the path of the file
	 * @throws IOException exception if error while reading the file.
	 */
	public void loadFile(String path) throws IOException {
		String thisLine;
		BufferedReader myInput = null;
		try {
			FileInputStream fin = new FileInputStream(new File(path));
			myInput = new BufferedReader(new InputStreamReader(fin));
			// for each transaction (line) in the input file
			int tid =0;
			while ((thisLine = myInput.readLine()) != null && tid < maxSEQUENCECOUNT) {
				// if the line is  a comment, is  empty or is a
				// kind of metadata
				if (thisLine.isEmpty() == true ||
						thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
								|| thisLine.charAt(0) == '@') {
					continue;
				}
				
				// process the transaction
				processTransaction(thisLine.split(":"));
				tid++;
			}
		} catch (Exception e) {
			// catch exceptions
			e.printStackTrace();
		}finally {
			if(myInput != null){
				// close the file
				myInput.close();
			}
	    }
	}
	
	/**
	 * Process a line (transaction) from the input file
	 * @param line  a line
	 */
	private void processTransaction(String line[]){
		String[] items = line[0].split(" ");
		String[] utilities = line[2].split(" ");
		// == code for handling period ==
		int period = Integer.parseInt(line[3]);
		// ============================
		
		//****** BUG FIX 2016 *****
		// We will not trust the transaction utility value in the input file.
		// We will calculate it again.
		// First, we will convert the utility values to integer
		// and calculate the transaction utility WITH negative values
		//   (this is necessary to calculate the relative utility of itemsets)
		// and the transaction utility WITH only positive values
		//   (this is necessary to calculate the 
		//      RTWU upper-bound on the utility of itemsets
		int[] utilityValuesInt = new int[utilities.length];
		int transactionUtilityWithNegativeAndPositive = 0;
		int transactionUtilityWithPositive = 0;
		for(int j=0; j< utilities.length; j++){
			utilityValuesInt[j] = Integer.parseInt(utilities[j]);
			transactionUtilityWithNegativeAndPositive += utilityValuesInt[j];
			if(utilityValuesInt[j] > 0){
				transactionUtilityWithPositive += utilityValuesInt[j];
			}
		}
		
		//****** END BUG FIX 2016 *****
		
		
				
		// Create a list for storing items
		List<ItemUtility> itemUtilityObjects = new ArrayList<ItemUtility>();
		// for each item
		for(int i=0; i< items.length; i++) {
			int item = Integer.parseInt(items[i]);
			int utility = Integer.parseInt(utilities[i]);
			
			itemUtilityObjects.add(new ItemUtility(
					item, utility));
			
			if(utility < 0) {
				allNegativeItems.add(item);
			}
			
			if(item < smallestID) {
				smallestID = item;
			}
			
			if(item > largestID) {
				largestID = item;
			}
			
			allItems.add(item);
			
			// update exact utility of the item
			Pair pair = mapItemUtility.get(item);
			if(pair == null) {
				pair = new Pair(periodCount);
				pair.exactUtility += utility;
				mapItemUtility.put(item, pair);
			}else {
				pair.exactUtility += utility;
			}
			if(pair.estimatedUtility[period] == null){ 
				pair.estimatedUtility[period] = transactionUtilityWithPositive;
			}else {
				pair.estimatedUtility[period] += transactionUtilityWithPositive;
			}
			
			addPeriodToListOfPeriodsOfItem(period, item);
		}

		// add the transaction to the list of transactions
		transactions.add(new TransactionWithPeriod(itemUtilityObjects, transactionUtilityWithPositive, period));
		
		//====  code for periods =====
		incrementPeriodUtility(period, transactionUtilityWithNegativeAndPositive);
		
		//==== end code for periods =====
	}


	/**
	 * Print this database to System.out.
	 */
	public void printDatabase(){
		System.out
		.println("===================  Database ===================");
		int count = 0;
		// for each transaction
		for(TransactionWithPeriod itemset : transactions){
			// print the transaction
			System.out.print("0" + count + ":  ");
			itemset.print();
			System.out.println("");
			count++;
		}
	}
	
	/**
	 * Get the number of transactions.
	 * @return a int
	 */
	public int size(){
		return transactions.size();
	}

	/**
	 * Get the list of transactions.
	 * @return the list of Transactions.
	 */
	public List<TransactionWithPeriod> getTransactions() {
		return transactions;
	}

	/**
	 * Get the set of items in this database.
	 * @return a Set of Integers
	 */
	public Set<Integer> getAllItems() {
		return allItems;
	}




}
