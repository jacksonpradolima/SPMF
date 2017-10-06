package ca.pfv.spmf.algorithms.frequentpatterns.pfpm;

/* This file is copyright (c) 2008-2013 Philippe Fournier-Viger
* 
* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
* 
* SPMF is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* 
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/

import java.util.Arrays;
/**
 * This class represents an itemset with utility and support values.
 *
 * @see AlgoHUIMiner
 * @see Element
 * @author Philippe Fournier-Viger
 */
public class Itemset {

	int[] itemset;
	long utility; 
	int support;
	
	/**
	 * Default constructor
	 * @param itemset
	 * @param sumIutils
	 * @param support
	 */
	public Itemset(int[] itemset, long sumIutils, int support) {
		this.itemset = itemset;
		this.utility = sumIutils;
		this.support = support;
	}

//	@Override
	public String toString() {
		return Arrays.toString(itemset) + " utility : " + utility + " support:" + support;
	}
}
