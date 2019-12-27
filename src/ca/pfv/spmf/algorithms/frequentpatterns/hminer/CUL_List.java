package ca.pfv.spmf.algorithms.frequentpatterns.hminer;

/* This file is copyright (c) 2018+  by Siddharth Dawar et al.
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
 * @see Element_CUL_List
 * @author Siddharth Dawar et al.
 */
class CUL_List {
	/** the item */
	int item; 
	
	/** the sum of item utilities */
	long sumNu = 0;  
	
	/** the sum of remaining utilities */
	long sumNru = 0; 
	
	long sumCu = 0;
	long sumCru = 0;
	long sumCpu = 0;
	
	/** the elements */
	List<Element_CUL_List> elements = new ArrayList<Element_CUL_List>(); 
	
	/**
	 * Constructor.
	 * @param item the item that is used for this utility list
	 */
	public CUL_List(int item){
		this.item = item;
	}
	
	/**
	 * Method to add an element to this utility list and update the sums at the same time.
	 */
	public void addElement(Element_CUL_List element){
		sumNu += element.Nu;
		sumNru += element.Nru;
		//Some conditions to update CU, CRU, and CPU
		elements.add(element);
	}
}
