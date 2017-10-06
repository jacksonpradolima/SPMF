package ca.pfv.spmf.algorithms.frequentpatterns.skymine;

/**
 * This is an implementation of an Itemset as used by Skymine
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

public class Itemset {

	/** the itemset */
	int[] itemset;
	/** the itemset utility */
	long utility = 0;
	/** the itemset support */
	int support = 0;

	/**
	 * Constructor
	 * 
	 * @param itemset
	 *            the itemset
	 */
	public Itemset(int[] itemset) {
		this.itemset = itemset;
	}

	/**
	 * Get the exact utility of this itemset
	 * 
	 * @return the exat utility
	 */
	public long getExactUtility() {
		return utility;
	}

	/**
	 * Get the support of this itemset
	 * @return the support
	 */
	public int getSupport() {
		return support;
	}

	/**
	 * Set the support of this itemset
	 * @param support the support
	 */
	public void setSupport(int support) {
		this.support = support;
	}

	/**
	 * Increase the utility of this itemset.
	 * 
	 * @param utility
	 *            the amount of utility to be added (int).
	 */
	public void increaseUtility(long utility) {
		this.utility += utility;
	}

	/** 
	 * Get the item at a given position in this itemset
	 * @param pos the position
	 * @return the item
	 */
	public Integer get(int pos) {
		return itemset[pos];
	}

	/**
	 * Get the number of items in this itemset
	 * @return the number of items
	 */
	public int size() {
		return itemset.length;
	}

}
