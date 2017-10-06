package ca.pfv.spmf.algorithms.frequentpatterns.efim_closed;
import java.util.List;


/* This file is copyright (c) 2008-2012 Philippe Fournier-Viger
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
 * This class represents an itemset (a set of items) implemented as an array of integers with
 * a variable to store the utility count of the itemset.
* 
 * @author Philippe Fournier-Viger
 */
public class Itemset {
	/** the array of items **/
	public int[] itemset; 

	/**  the utility of this itemset */
	public double utility = 0; 
	
	/**
	 * Get the items as array
	 * @return the items
	 */
	public int[] getItems() {
		return itemset;
	}
	
	/**
	 * Constructor
	 */
	public Itemset(){
		itemset = new int[]{};
	}
	
	/**
	 * Constructor 
	 * @param item an item that should be added to the new itemset
	 */
	public Itemset(int item){
		itemset = new int[]{item};
	}

	/**
	 * Constructor 
	 * @param items an array of items that should be added to the new itemset
	 */
	public Itemset(int [] items){
		this.itemset = items;
	}
	
	/**
	 * Constructor 
	 * @param items a list of Integer representing items in the itemset
	 * @param utility the utility of the itemset
	 */
	public Itemset(List<Integer> itemset, double utility){
		this.itemset = new int[itemset.size()];
	    int i = 0;
	    for (Integer item : itemset) { 
	    	this.itemset[i++] = item.intValue();
	    }
	    this.utility = utility;
	}
	
	/**
	 * Constructor
	 * @param itemset
	 * @param utility
	 */
	public Itemset(int[] itemset, double utility){
		this.itemset = itemset;
	    this.utility = utility;
	}
	
	/**
	 * Get the utility of this itemset
	 */
	public double getUtility(){
		return utility;
	}
	
	/**
	 * Get the size of this itemset 
	 */
	public int size() {
		return itemset.length;
	}

	/**
	 * Get the item at a given position in this itemset
	 */
	public Integer get(int position) {
		return itemset[position];
	}

	/**
	 * Set the utility of this itemset
	 * @param utility the utility
	 */
	public void setUtility(double utility) {
		this.utility = utility;
	}

	/**
	 * Make a copy of this itemset but exclude a given item
	 * @param itemToRemove the given item
	 * @return the copy
	 */
	public Itemset cloneItemSetMinusOneItem(Integer itemToRemove) {
		// create the new itemset
		int[] newItemset = new int[itemset.length -1];
		int i=0;
		// for each item in this itemset
		for(int j =0; j < itemset.length; j++){
			// copy the item except if it is the item that should be excluded
			if(itemset[j] != itemToRemove){
				newItemset[i++] = itemset[j];
			}
		}
		return new Itemset(newItemset); // return the copy
	}
	
	/**
	 * Get a string representatino of this transaction
	 * @return a string
	 */
	public String toString(){
		// use a string buffer for more efficiency
		StringBuffer r = new StringBuffer ();
		// for each item, append it to the stringbuffer
		for(int i=0; i< size(); i++){
			r.append(get(i));
			r.append(' ');
		}
		return r.toString(); // return the tring
	}
}
