package ca.pfv.spmf.algorithms.frequentpatterns.upgrowth_ihup;


/**
 * This class represent an itemset and its exact utility as used by the UPGrowth algorithm.
 * 
 * Copyright (c) 2014 Prashant Barhate
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
 * @see AlgoUPGrowth
 */
public class Itemset {

	private int[] itemset;
	private int utility =0;
	
	/**
	 * Constructor
	 * @param itemset the itemset
	 */
	public Itemset(int[] itemset) {
		this.itemset = itemset;
	}
	
	/**
	 * Get the exact utility of this itemset
	 * @return the exat utility
	 */
	public int getExactUtility() {
		return utility;
	}

	/**
	 * Increase the utility of this itemset.
	 * @param utility the amount of utility to be added (int).
	 */
	public void increaseUtility(int utility) {
		this.utility += utility;
	}


	public Integer get(int pos) {
		return itemset[pos];
	}
	
	public int size() {
		return itemset.length;
	}

}
