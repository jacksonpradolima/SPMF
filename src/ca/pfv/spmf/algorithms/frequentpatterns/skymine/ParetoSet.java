package ca.pfv.spmf.algorithms.frequentpatterns.skymine;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This is a Pareto Set as used by the SkyMine algorithm.
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
public class ParetoSet {
	
	/** A list of UtilityInterval objects */
	ArrayList<UtilityInterval> utilities = null;

	/**
	 * Constructor
	 */
	public ParetoSet() {
		// array of utilities for each index frequency
		utilities = new ArrayList<UtilityInterval>(); 
	}

	/**
	 * Insert an itemset in this Pareto set 
	 * @param itemSet an itemset to be inserted
	 * @param utility the utility value of the itemSet
	 * @param support the support of the itemset
	 */
	public void insert(int[] itemSet, long utilityL, long utilityH,
			int support) {
		/*
		 * Case when utilities array size is smaller than supp increase size of
		 * array and initialize update utility and add itemset clears previous
		 * itemsets
		 */
		if (support == 0)
			return;
		if (utilities.size() <= support) {
			int extendSize = support - utilities.size();
			for (int j = 0; j <= extendSize; j++) {
				utilities.add(new UtilityInterval());
				// System.out.println(utilities.size());
			}
		}
		// Add new element in utility array
		utilities.get(support).insertUtilityInt(utilityL, utilityH, itemSet);
		updateLowSupportUtilities(support, utilityL, utilityH);

		return ;
	}

	private void updateLowSupportUtilities(int supp, long uL, long uH) {
		for (int i = supp - 1; i > 0; i--) {
			if (utilities.get(i).getUtilityValue() > uH)
				break;
			else
				utilities.get(i).insertUtilityInt(uL, uH, null);
		}
	}

	public long getUtility(int support) {
		if (utilities.size() > support)
			return utilities.get(support).getUtilityValue();
		else
			return 0;
	}

	public ArrayList<UtilityInterval> getUtilities() {
		return utilities;
	}


}