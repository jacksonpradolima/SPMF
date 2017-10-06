package ca.pfv.spmf.algorithms.frequentpatterns.skymine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This represents a utility interval as used by the Skymine algorithm
 * 
 * Copyright (c) 2015 Vikram Goyal, Ashish Sureka, Dhaval Patel, Siddharth Dawar
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE *
 * (http://www.philippe-fournier-viger.com/spmf).
 * 
 * SPMF is free software: you can redistribute it and/or modify it under the *
 * terms of the GNU General Public License as published by the Free Software *
 * Foundation, either version 3 of the License, or (at your option) any later *
 * version. SPMF is distributed in the hope that it will be useful, but WITHOUT
 * ANY * WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @see AlgoSkyMine
 * @see UPTree
 * 
 * @author Vikram Goyal, Ashish Sureka, Dhaval Patel, Siddharth Dawar
 */

public class UtilityInterval {
	/**
	 * Interval class with low and high values has methods to get low and high
	 * values
	 * 
	 * @author vikram
	 */
	private class Interval implements Comparable<Interval> {
		/** the low value */
		long low;
		/** the high value */
		long high;
		/** the itemset */
		int[] itemset;

		Interval(long xL, long xH, int[] items) {
			low = xL;
			high = xH;
			itemset = items;
		}

		/** 
		 * Get the itemset
		 * @return the itemset
		 */
		public int[] getItemset() {
			return itemset;
		}

		/**
		 * Get the low utility value
		 * @return the low value
		 */
		public long getLow() {
			return this.low;
		}

		/**
		 * Get the high utility value
		 * @return the high value
		 */
		public long getHigh() {
			return this.high;
		}

		public int compareTo(Interval other) {
			return (int) (other.getLow() - this.getLow());
		}
	}

	/** List of intervals */
	ArrayList<Interval> intervalSet = null;
	private long currentMaxMin = 0;

	/**
	 * Constructor
	 * 
	 */
	public UtilityInterval() {
		intervalSet = new ArrayList<Interval>();
		currentMaxMin = 0;
	}

	/**
	 * Inserts an interval in the set if its high end is larger than current
	 * largest min value removes intervals that have high end value less than xL
	 * 
	 * @param xL the low end of interval
	 * @param yH the high end of interval
	 * @param itemset the itemset to be inserted
	 */

	
	public void insertUtilityInt(long xL, long yH, int[] itemset) {
		if (this.currentMaxMin < yH) {
			Interval inV = new Interval(xL, yH, itemset);
			if (!filterUtilityIntervals(xL, itemset))
				intervalSet.add(inV);
		}
		if (xL > this.currentMaxMin)
			this.currentMaxMin = xL;

	}

	/**
	 * Returns utility value updates max lowerbound
	 * 
	 * @return maximum lower bound
	 */
	public long getUtilityValue() {
		if (intervalSet.size() > 0) {
			// Collections.sort(intervalSet);
			// currentMaxMin =intervalSet.get(0).getLow();
			return currentMaxMin;
		} else
			return 0;
	}

	/**
	 * Prunes irrelevant intervals
	 * 
	 * @param xL
	 *            lowerbound value
	 */
	private boolean filterUtilityIntervals(long xL, int[] itemset) {
		boolean itemsubsetflag = false;
		for (int i = intervalSet.size() - 1; i >= 0; i--) {
			if (isSuperItemSet(intervalSet.get(i).itemset, itemset))
				itemsubsetflag = true;
			if (intervalSet.get(i).getHigh() < xL
					|| isSuperItemSet(itemset, intervalSet.get(i).itemset))
				intervalSet.remove(i);
		}
		return itemsubsetflag;
	}

	private boolean isSuperItemSet(int[] itemsetR, int[] itemsetOther) {
		if (itemsetR == null && itemsetOther == null)
			return false;
		if (itemsetOther == null)
			return true;
		if (itemsetR == null)
			return false;
		Set<Integer> referenceSet = new HashSet<Integer>();
		for (int i = 0; i < itemsetR.length; i++)
			referenceSet.add(itemsetR[i]);

		Set<Integer> otherSet = new HashSet<Integer>();
		for (int i = 0; i < itemsetOther.length; i++)
			otherSet.add(itemsetOther[i]);
		if (referenceSet.containsAll(otherSet)
				&& otherSet.containsAll(referenceSet))
			return false;
		return referenceSet.containsAll(otherSet);

	}

	/**
	 * Get the list of itemsets in this interval
	 * @return the list of itemset 
	 */
	public ArrayList<int[]> getItemSets() {
		ArrayList<int[]> resultSet = new ArrayList<>();
		for (int i = 0; i < intervalSet.size(); i++) {
			
			// Get the interval
			Interval intervalI = intervalSet.get(i);
			
			// get the itemset and its utility
			int[] itemsetArray = intervalI.getItemset();
			if (itemsetArray != null) {
				// sort the itemset by alphabetical order
				Arrays.sort(itemsetArray);
				// add the itemset to the set of itemsets
				resultSet.add(itemsetArray);
			}
		}
		// return the set of skyline itemsets
		return resultSet;
	}
	
	/**
	 * Get the list of itemsets in this interval
	 * @return the list of itemset with their utilities
	 */
	public ArrayList<ItemsetUtility> getItemSetsWithUtilities() {
		ArrayList<ItemsetUtility> resultSet = new ArrayList<>();
		for (int i = 0; i < intervalSet.size(); i++) {
			
			// Get the interval
			Interval intervalI = intervalSet.get(i);
			
			// get the itemset and its utility
			int[] itemsetArray = intervalI.getItemset();
			if (itemsetArray != null) {
				// sort the itemset by alphabetical order
				Arrays.sort(itemsetArray);
				
				// Create an ItemsetUtility object to store the result
				ItemsetUtility itemsetUtility = new ItemsetUtility();
				itemsetUtility.itemset = itemsetArray;
				itemsetUtility.utility = intervalI.getHigh();
				resultSet.add(itemsetUtility);
			}
		}
		// return the set of skyline itemsets
		return resultSet;
	}


	/**
	 * converts object data in string form
	 * 
	 * @return
	 */
	public String toString(int[] reconversionArray) {
		String result = "";
		for (int i = 0; i < intervalSet.size(); i++) {
			if (intervalSet.get(i).itemset != null) {
				result = result
						+ " ("
						+ intervalSet.get(i).getLow()
						+ ","
						+ intervalSet.get(i).getHigh()
						+ ":"
						+ Arrays.toString(convert(intervalSet.get(i).itemset,
								reconversionArray)) + ")";
			}
		}
		return result;
	}

	private int[] convert(int[] items, int[] reconversionArray) {
		// If no conversion return original item set
		if (reconversionArray == null)
			return (items);

		// If item set null return null
		if (items == null)
			return (null);

		// Define new item set
		int[] newItemSet = new int[items.length];

		// Copy
		for (int index = 0; index < newItemSet.length; index++) {
			newItemSet[index] = reconversionArray[items[index]];
		}

		// Return
		return (newItemSet);
	}
}