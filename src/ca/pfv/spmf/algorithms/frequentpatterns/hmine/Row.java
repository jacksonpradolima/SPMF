package ca.pfv.spmf.algorithms.frequentpatterns.hmine;

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
 * This class represents a row of the header table.
 * A row represent an item and contains (1) its support, (3) a list of pointers
 * to items in the projected database
 *
 * @see AlgoHMine
 * @author Philippe Fournier-Viger, 2015
 */
class Row {
	/** the item **/
	int item;  
	/** its support **/
	int support;
	/** the list of pointers to items in transactions */
	List<Integer> pointers = new ArrayList<Integer>();

	/**
	 * Constructor.
	 * @param item the item that is used for this row
	 */
	public Row(int item){
		this.item = item;
	}
	
	/**
	 * Get a string representation of this cell.
	 * @return a string representation
	 */
	public String toString() {
		String temp = item + " s:" + support + " pointers: " + pointers;
		return temp;
	}
	
}
