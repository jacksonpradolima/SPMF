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



/**
 * This class represents a Partitioned Utility List as used by the HUP-Miner algorithm.
 *
 * @see AlgoHUPMiner
 * @see UtilityList
 * @see Element
 * @author Philippe Fournier-Viger
 */
class UtilityListHUP extends UtilityList {
	/** this variable stores the sum of SU+RU for each partition */
	int[] partitionSURU;
	 
	/**
	 * Constructor
	 * @param item the item that is used for this utility list
	 * @param k the number of partitions
	 */
	public UtilityListHUP(int item, int k){
		super(item);
		this.partitionSURU = new int[k];
	}
	
	/**
	 * Method to add an element to this utility list and update the sums at the same time.
	 * @param element the element to be added
	 * @param partitionSize the size of the partition
	 */
	public void addElement(Element element, int partitionSize){
		
		sumIutils += element.iutils;
		sumRutils += element.rutils;
		elements.add(element);
		
		// update the sum of utility and remaining utility for the partition
		// corresponding to this element
		int partition = element.tid / partitionSize;
		partitionSURU[partition] += element.iutils + element.rutils;
	}
}
