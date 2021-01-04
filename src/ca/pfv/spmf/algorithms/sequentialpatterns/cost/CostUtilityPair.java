/*
* This is an implementation of the CEPB, corCEPB, CEPN algorithm.
*
* 
* This file is part of the SPMF DATA MINING SOFTWARE * (http://www.philippe-fournier-viger.com/spmf).
*
* SPMF is free software: you can redistribute it and/or modify it under the * terms of the GNU General Public License as published by the Free Software * Foundation, either version 3 of the License, or (at your option) any later * version. *

* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR * A PARTICULAR PURPOSE. See the GNU General Public License for more details. *
*
* You should have received a copy of the GNU General Public License along with SPMF. If not, see <http://www.gnu.org/licenses/>.
*
* Copyright (c) 2019 Jiaxuan Li
*/

package ca.pfv.spmf.algorithms.sequentialpatterns.cost;

/**
 * This class is an implementation for storing the patterns' cost and utility
 * value
 * 
 * @author Jiaxuan Li
 * @see AlgoCEPM
 */
public class CostUtilityPair {
	/** pattern's cost */
	private double cost;

	/** pattern's utility */
	private double utility;

	/**
	 * Constructor
	 * 
	 * @param cost    pattern's cost
	 * @param utility pattern's utility
	 */
	public CostUtilityPair(double cost, double utility) {
		this.cost = cost;
		this.utility = utility;
	}

	/**
	 * Get the exact cost of this pattern
	 * 
	 * @return pattern's cost
	 */
	public double getCost() {
		return cost;
	}

	/**
	 * Setting
	 * 
	 * @param cost pattern's cost
	 */
	public void setCost(double cost) {
		this.cost = cost;
	}

	/**
	 * Get the exact utility of this pattern
	 * 
	 * @return pattern's utility
	 */
	public double getUtility() {
		return utility;
	}

	/**
	 * Setting
	 * 
	 * @param utility pattern's utility
	 */
	public void setUtility(int utility) {
		this.utility = utility;
	}

}