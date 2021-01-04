package ca.pfv.spmf.algorithms.episodes.emma;

import java.util.List;
/*
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
 * Copyright Peng Yang, Philippe Fournier-Viger, 2019
 */
/**
 * An itemset with its location list
 * 
 * @author Peng Yang
 * @see AlgoEMMA
 */
public class Itemset implements Comparable<Itemset> {
	/** the items */
	private int[] name;
	
	/** the list of locations */
	private List<Integer> locationList = null;

	/** constructor */
	Itemset() {

	}

	/** constructor with some list of item names
	 * @param name an array of item names
	 **/
	Itemset(int[] name) {
		this.name = name;
	}

	/** constructor with some list of item names and location list
	 * @param name an array of item names
	 * @param locationList a list of locations
	 **/
	Itemset(int[] name, List<Integer> locationList) {
		this.name = name;
		this.locationList = locationList;
	}

	/**
	 * Get the item names
	 * @return the item names
	 */
	public int[] getName() {
		return this.name;
	}

	/** 
	 * Get the location list
	 * @return the location list
	 */
	public List<Integer> getLocationList() {
		return this.locationList;
	}

	/** Set the location list
	 * 
	 * @param locationList a list of location
	 */
	public void setLocationList(List<Integer> locationList) {
		this.locationList = locationList;
	}

	/**
	 * Get the support of this itemset
	 * @return the support (an integer)
	 */
	public int getSupport() {
		return this.locationList.size();
	}

	  /**
     * Compare this pattern with another pattern
     * @param o another pattern
     * @return 0 if equal, -1 if smaller, 1 if larger (in terms of support).
     */
    public int compareTo(Itemset o) {
		if(o == this){
			return 0;
		}
		if(this.locationList == null && o.locationList == null) {
			return 0;
		}
		long compare =  this.locationList.size() - o.locationList.size();
		if(compare > 0){
			return 1;
		}
		if(compare < 0){
			return -1;
		}
		return 0;
	}
}
