package ca.pfv.spmf.algorithms.frequentpatterns.MRCPPS;

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
 * An itemset as implemented for the MRCPPS algorithm.
 * 
 * @see AlgoMRCPPS
 * @author Peng Yang
 */
public class Itemset {
	/** the list of items */
	private int[] name;

	/** the ra value */
	private double ra;

	/**
	 * Constructor
	 * 
	 * @param name the items as an array of integers
	 * @param ra   the ra value
	 */
	Itemset(int[] name, double ra) {
		this.name = name;
		this.ra = ra;
	}

	/**
	 * Constructor
	 * 
	 * @param name a single item
	 * @param ra   the ra value
	 */
	Itemset(int name, double ra) {
		this.name = new int[] { name };
		this.ra = ra;
	}

	/**
	 * get the items
	 * 
	 * @return an array of integers representing the items
	 */
	public int[] getName() {
		return name;
	}

	/**
	 * set the items
	 * 
	 * @param name an array of integers representing the items
	 */
	public void setName(int[] name) {
		this.name = name;
	}

	/**
	 * get the Ra value of this itemset
	 * 
	 * @return the ra value as a double
	 */
	public double getRa() {
		return ra;
	}

	/**
	 * Set the Ra value of this itemset
	 * 
	 * @param ra the ra value as a double
	 */
	public void setRa(double ra) {
		this.ra = ra;
	}

	/**
	 * get the number of items in this itemset
	 * 
	 * @return the number of items
	 */
	public int size() {
		return name.length;
	}

	@Override
	/**
	 * Get a string representation of this itemset and its ra value
	 * 
	 * @return a string representation of the itemset and its ra value
	 */
	public String toString() {
		String s = "";
		for (int n : name) {
			s = s + n + " ,";
		}
		s = s.substring(0, s.length() - 1);
		s += "   #ra:" + this.ra;
		return s;
	}
}
