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


import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a UtilityList as used by the FHN algorithm.
 *
 * @see AlgoHUIMiner
 * @see ElementFHN
 * @author Philippe Fournier-Viger
 */
public class UtilityListFHN extends UtilityList{

	// the sum of iutil values of negative items
	long  sumINutils = 0;
	
	// the list of elements in this utility list
	public List<ElementFHN> elements = new ArrayList<ElementFHN>();
 
	/**
	 * Constructor
	 * @param item the last item of the itemset represented by this utility list
	 */
	public UtilityListFHN(Integer item) {
		super(item);
	}

	/**
	 * Method to add an element to this utility list and update the sums at the same time.
	 */
	public void addElement(ElementFHN element){
		sumIutils += element.iutils;
		sumRutils += element.rutils;
		sumINutils += element.inutils;
		elements.add(element);
	}
}
