package ca.pfv.spmf.tools.dataset_stats;

/* This file is copyright (c) 2008-2014 Philippe Fournier-Viger
* 
* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
* 
* SPMF is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import ca.pfv.spmf.input.transaction_database_list_integers.TransactionDatabase;

/**
 * This class read a transaction database and calculates statistics
 * about this transactions database, then it prints the statistics to the console.
 * <br/><br/>
 * In this version this class reads the database into memory before calculating the
 * statistics. It could be optimized to calculate statistics without
 * reading the database in memory because a single pass is required. It
 * was done like that because the code is simpler and easier to understand and performance
 * is not an issue for this kind of tasks.

* @author Philippe Fournier-Viger
 */
public class TransactionStatsGenerator {


	/**
	 * This method generates statistics for a transaction database (a file)
	 * @param path the path to the file
	 * @throws IOException  exception if there is a problem while reading the file.
	 */
	public void getStats(String path) throws IOException {

		/////////////////////////////////////
		//  (1) First we will read the transaction database into memory.
		// (actually, we don't really need to read it into memory because it
		//  just require a single pass, but the code is more simple like that
		//  - it could be optimized, if necessary).
		///////////////////////////////////	
		
		TransactionDatabase database = new TransactionDatabase();
		database.loadFile(path);

		/////////////////////////////////////
		//  We finished reading the database into memory.
		//  We will calculate statistics on this transaction database.
		///////////////////////////////////

		System.out.println("============  TRANSACTION DATABASE STATS ==========");
		System.out.println("Number of transactions : " + database.size());
		
		// we initialize some variables that we will use to generate the statistics
		int minItem = Integer.MAX_VALUE; // the largest id for items in the database
		int maxItem = 0; // the largest id for items in the database
		Set<Integer> items = new java.util.HashSet<Integer>();  // the set of all items
		List<Integer> sizes = new ArrayList<Integer>(); // the lengths of each transactions
		
		// this map is used to store the number of times that each item
		// appear in the database.
		// the key is an item
		// the value is the number of items that the item appears
		HashMap<Integer, Integer> mapItemSupport = new HashMap<Integer, Integer>();
		
		
		// Loop on transactions from the database
		for (List<Integer> transaction : database.getTransactions()) {
			// we add the size of this transaction to the list of sizes
			sizes.add(transaction.size());
			

			// Loop on items from this transaction
			for (int item : transaction) {
				if(item > maxItem) {
					maxItem = item; 
				}
				if(item < minItem) {
					minItem = item; 
				}
				// If the item is not in the map already, we set count to 0
				Integer count = mapItemSupport.get(item);
				if (count == null) {
					count = 0;
				}
				mapItemSupport.put(item, count+1);
				// finally, we add the item to the set of items
				items.add(item);
			}
		}
		
		// put support of items into a list
		List<Integer> listSupportOfItems = new ArrayList(mapItemSupport.values());
		
		// we print the statistics
		System.out.println("File " + path);
		System.out.println("Number of distinct items: " + items.size());
		System.out.println("Smallest item id: " + minItem);
		System.out.println("Largest item id: " + maxItem);
		System.out.println("Average number of items per transaction: "
				+ calculateMean(sizes) + " standard deviation: "
				+ calculateStdDeviation(sizes) + " variance: "
				+ calculateVariance(sizes));
		System.out.println("Average item support in the database: "
				+ calculateMean(listSupportOfItems) + " standard deviation: "
				+ calculateStdDeviation(listSupportOfItems) + " variance: "
				+ calculateVariance(listSupportOfItems)
				+ " min value: " + calculateMinValue(listSupportOfItems)
				+ " max value: " + calculateMaxValue(listSupportOfItems)
				);
	}


	/**
	 * This method calculate the mean of a list of integers
	 * @param list the list of integers
	 * @return the mean 
	 */
	private static double calculateMean(List<Integer> list) {
		double sum = 0;
		for (Integer val : list) {
			sum += val;
		}
		return sum / list.size();
	}

	/**
	 * This method calculate the standard deviation of a list of integers
	 * @param list the list of integers
	 * @return the standard deviation
	 */
	private static double calculateStdDeviation(List<Integer> list) {
		double deviation = 0;
		double mean = calculateMean(list);
		for (Integer val : list) {
			deviation += Math.pow(mean - val, 2);
		}
		return Math.sqrt(deviation / list.size());
	}

	/**
	 * This method calculate the mean of a list of doubles
	 * @param list the list of doubles
	 * @return the mean
	 */
	private static double calculateMeanD(List<Double> list) {
		double sum = 0;
		for (Double val : list) {
			sum += val;
		}
		return sum / list.size();
	}

	/**
	 * This method calculate the standard deviation of a list of doubles
	 * @param list the list of doubles
	 * @return the standard deviation
	 */
	private static double calculateStdDeviationD(List<Double> list) {
		double deviation = 0;
		double mean = calculateMeanD(list);
		for (Double val : list) {
			deviation += Math.pow(mean - val, 2);
		}
		return Math.sqrt(deviation / list.size());
	}

	/**
	 * This method calculate the variance of a list of integers
	 * @param list the list of integers
	 * @return the variance 
	 */
	private static double calculateVariance(List<Integer> list) {
		double deviation = 0;
		double mean = calculateMean(list);
		for (Integer val : list) {
			deviation += Math.pow(mean - val, 2);
		}
		return Math.pow(Math.sqrt(deviation / list.size()), 2);
	}

	/**
	 * This method return the smallest integer from a list of integers
	 * @param list the list of integers
	 * @return the smallest integer 
	 */
	private static int calculateMinValue(List<Integer> list) {
		int min = Integer.MAX_VALUE;
		for (Integer val : list) {
			if (val < min) {
				min = val;
			}
		}
		return min;
	}

	/**
	 * This method return the largest integer from a list of integers
	 * @param list the list of integers
	 * @return the largest integer 
	 */
	private static int calculateMaxValue(List<Integer> list) {
		int max = 0;
		for (Integer val : list) {
			if (val >= max) {
				max = val;
			}
		}
		return max;
	}
}
