package ca.pfv.spmf.algorithms.frequentpatterns.ssfim;
import java.util.Arrays;

/* This file is copyright (c) 2008-2018 Philippe Fournier-Viger
* 
* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
* 
* SPMF is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/

/**
 * This class represents an itemset (a set of items) implemented as an array of integers.
 * We use this class instead of int[] to redefine the hashcode() function.
* 
 * @author Philippe Fournier-Viger
 */
public class Itemset  {
	/** the array of items **/
	public int[] itemset; 
	
	/**
	 * Constructor
	 * @param itemset itemset
	 */
	public Itemset(int[] itemset){
		this.itemset = itemset;
	}

	/**
	 * Get the items as array
	 * @return the items
	 */
	public int[] getItems() {
		return itemset;
	}
	

	/**
	 * Get the size of this itemset 
	 */
	public int size() {
		return itemset.length;
	}

	
	@Override
	public int hashCode() {
		
		return Arrays.hashCode(itemset);
	}
	
	@Override
	public boolean equals(Object obj) {
		Itemset itemset2 = (Itemset) obj;
		return Arrays.equals(itemset, itemset2.itemset);
	}
}
