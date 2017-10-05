package ca.pfv.spmf.algorithms.frequentpatterns.skymine;

/**
 * This is an implementation of an Item as used by Skymine
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

public class Item {

	/** the item */
	int itemName = 0; 
	/** the utility of the item */
	long utility = 0; 
	/** the quantity of the item */
	short quantity = 0;

	/**
	 * Constructor with item name as parameter
	 * @param itemName the name of the item
	 */
	public Item(int itemName) {
		this.itemName = itemName;
	}

	/**
	 * Constructor with item name as parameter
	 * @param itemName the name of the item
	 * @param utility the utility of the item
	 * @param quantity the quantity of the item
	 */
	public Item(int itemName, long utility, short quantity) {
		this.itemName = itemName;
		this.utility = utility;
		this.quantity = quantity;
	}

	/**
	 * Constructor with item name as parameter
	 * @param itemName the name of the item
	 * @param utility the utility of the item
	 */
	public Item(int name, long utility) {
		this.itemName = name;
		this.utility = utility;

	}

	/**
	 * method to get node utility
	 */
	public long getUtility() {
		return utility;
	}

	/**
	 * method to set node utility
	 */
	public void setUtility(long utility) {
		this.utility = utility;
	}

	/**
	 * method to get perticular item
	 */
	public int getName() {
		return itemName;
	}

	/**
	 * Get the quantity of this item
	 * @return the quantity as a short
	 */
	public short getQuantity() {
		return quantity;
	}

	/**
	 * Set the quantity of this item
	 * @param quantity the quantity as a short
	 */
	public void setQuantity(short quantity) {
		this.quantity = quantity;
	}
}