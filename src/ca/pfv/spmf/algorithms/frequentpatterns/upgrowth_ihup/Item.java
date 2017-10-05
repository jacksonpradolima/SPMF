package ca.pfv.spmf.algorithms.frequentpatterns.upgrowth_ihup;

/**
 * This is an implementation of an Item as used by the UPGrowth algorithm.
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
 * 
 * @author Prashant Barhate
 * 
 */

public class Item {

	public int name = 0; // item
	public int utility = 0; // utility of item

	// constructor that takes item name
	public Item(int name) {
		this.name = name;
	}

	// constructor that takes item name and utility
	public Item(int name, int utility) {
		this.name = name;
		this.utility = utility;

	}

	/**
	 * method to get node utility
	 */
	public int getUtility() {
		return utility;
	}

	/**
	 * method to set node utility
	 */
	public void setUtility(int utility) {
		this.utility = utility;
	}

	/**
	 * method to get perticular item
	 */
	public int getName() {
		return name;
	}
}