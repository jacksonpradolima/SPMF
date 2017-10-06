package ca.pfv.spmf.algorithms.frequentpatterns.d2hup;

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

import ca.pfv.spmf.algorithms.frequentpatterns.hmine.AlgoHMine;

/**
 * This class represents a row of the table in the CAUL structure.
 * A row represent an item and contains (1) its support, (2) the utility of
 * the current prefix ending with this item, (3) the local twu (ubitem) and
 * (4) the remaining utility (UbPFE).  Note that UbPFE is defined slightly differently
 * than in the paper. In the implementation, UbPFE excludes the utility of the item
 * represented by the row, while in the paper it includes the utility of the item.
 * This is not a problem since the utility of the item is added elsewhere in the 
 * d2hup algorithm instead.
 *
 * @see AlgoHMine
 * @author Philippe Fournier-Viger
 */
class Row {
	/** the item **/
	int item;  
	/** its support **/
	int support; 
	/** its utility **/
	int utility; 
	/** local twu (ubitem) **/
	int ltwu;
	/** the sum of the utility and remaining utility(UbPFE) **/
	int rutil = 0; 
	/** the list of pointers to items in transactions */
	List<Pointer> pointers = new ArrayList<Pointer>();

	/**
	 * Constructor.
	 * @param item the item that is used for this utility list
	 */
	public Row(int item){
		this.item = item;
	}
	
	/**
	 * Get a string representation of this cell.
	 * @return a string representation
	 */
	public String toString() {
		String temp = item + " s:" + support + " u:" + utility
				+ " ubItem:" + ltwu + " ubPFE:" + rutil + " pointers: " + pointers;
		return temp;
	}
	
}
