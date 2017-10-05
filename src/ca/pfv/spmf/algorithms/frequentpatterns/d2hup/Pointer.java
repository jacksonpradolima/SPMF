package ca.pfv.spmf.algorithms.frequentpatterns.d2hup;

import ca.pfv.spmf.algorithms.frequentpatterns.hmine.AlgoHMine;

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
 * This class represents a pointer to a transaction item in the CAUL structure.
 * It indicates the position of the item in the transaction and the utility
 * of the current prefix ending with that item.
 *
 * @see AlgoHMine
 * @author Philippe Fournier-Viger
 */
class Pointer {
	/** the item **/
	int pos;  
	/** prefix utility **/
	int prefixUtility; 

	/**
	 * Constructor.
	 * @param item the item 
	 * @param pos  a position
	 */
	public Pointer(int prefixUtility, int pos){
		this.prefixUtility = prefixUtility;
		this.pos = pos;
	}
}
