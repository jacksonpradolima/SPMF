package ca.pfv.spmf.algorithms.frequentpatterns.UFH;

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


import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a UtilityList as used by the HUI-Miner algorithm.
 *
 * @see AlgoHUIMiner
 * @see Element_SPMF
 * @author Philippe Fournier-Viger
 */
public class UtilityList_SPMF {
	public int item;  // the item
	public int sumIutils = 0;  // the sum of item utilities
	public int sumRutils = 0;  // the sum of remaining utilities
	public List<Element_SPMF> elements = new ArrayList<Element_SPMF>();  // the elements
	
	public UtilityList_SPMF()
	{
		
	}
	/**
	 * Constructor.
	 * @param item the item that is used for this utility list
	 */
	public UtilityList_SPMF(int item){
		this.item = item;
	}
	
	/**
	 * Method to add an element to this utility list and update the sums at the same time.
	 */
	public void addElement(Element_SPMF element){
		sumIutils += element.iutils;
		sumRutils += element.rutils;
		elements.add(element);
	}
}
