package ca.pfv.spmf.algorithms.frequentpatterns.hui_miner;

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


import java.util.BitSet;

/**
 * This class represents a UtilityList as used by the HUG-Miner algorithm.
 *
 * @see AlgoHUGMiner
 * @see Element
 * @author Philippe Fournier-Viger
 */
class UtilityListWithCriticalObjects extends UtilityList {
	/** This bit vectors stores the list of transaction containing the itemset represented
	 * by this utility-list
	 */
	BitSet tidset = new BitSet(); 
	
	/** This array of bitsets represents the critical objects of the itemsets represented by
	 *  this utility-list (see the DefMe paper for more details about critical objects 
	 */
	BitSet crit[] = null;
	
	/**
	 * Constructor.
	 * @param item the item that is used for this utility list
	 */
	public UtilityListWithCriticalObjects(Integer item){
		super(item);
//		this;
	}
	
	/**
	 * Method to add an element to this utility list and update the sums at the same time.
	 */
	public void addElement(Element element){
		sumIutils += element.iutils;
		sumRutils += element.rutils;
		elements.add(element);
		tidset.set(element.tid);
	}
	
}
