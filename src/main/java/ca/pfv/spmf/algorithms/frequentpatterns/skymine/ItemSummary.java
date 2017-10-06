package ca.pfv.spmf.algorithms.frequentpatterns.skymine;

/**
 * This is an implementation of an Item summary as used by Skymine
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

public class ItemSummary {
	/** the name of the item */
	int itemName;
	
	short minFrequency;
	short maxFrequency;
	long TWU;
	int totalFrequency;
	int support;

	ItemSummary() {

	}

	/**
	 * Constructor
	 * @param item the item
	 */
	public ItemSummary(int item) {
		itemName = item;
		minFrequency = 0;
		maxFrequency = 0;
		TWU = 0;
		totalFrequency = 0;
		this.support = 0;
	}

	public void updateMinFrequency(short minF) {
		if (this.minFrequency == 0)
			this.minFrequency = minF;
		else {
			if (this.minFrequency > minF)
				this.minFrequency = minF;

		}

	}

	public void updateMaxFrequency(short maxF) {
		if (this.maxFrequency < maxF)
			this.maxFrequency = maxF;
	}

	public void updateTWU(long twu) {
		this.TWU = this.TWU + twu;
	}

	public void updateTotalFrequency(int freq) {
		// long key=((HashMap<Short,Long>)utilityMap.get(key1)).get(freq);
		this.totalFrequency = this.totalFrequency + freq;
	}

	public void incrementSupp() {
		this.support++;
	}

	public long getTotalFreq() {
		return totalFrequency;
	}

	public int getSupport() {
		return support;
	}


	/**
	 * Get a string representation of this object
	 * @return a string
	 */
	public String toString() {
		String line = "";
		line = line + this.itemName + " " + this.minFrequency + " "
				+ this.maxFrequency + " " + this.TWU + " "
				+ this.totalFrequency + " " + this.support;
		return line;
	}

}