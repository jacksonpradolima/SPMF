package ca.pfv.spmf.algorithms;

import java.util.HashMap;
import java.util.Map;

/* This file is copyright (c) 2008-2015 Philippe Fournier-Viger
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
 * This class is used to rename items in a datasets.
 * It is used by several algorithms that use a total order, which
 * is different from the alphabetical or lexicographical order. 
 * By renaming items with consecutive names, it allows several optimization
 * such as faster comparison between items by using the > < == operators.
 *
 * @author Philippe Fournier-Viger, 2015
 */
public class ItemNameConverter {

	/** This structure is used for converting new names to old names.
	 *  The i-th position contains the old item name corresponding to the new name "i" **/
	int[] newNamesToOldNames;
	/** This structure is used for converting old names to new names.
	 *  The key is an old name. The value is a new name **/
	Map<Integer, Integer> oldNamesToNewNames;
	
	/** this variable is the next new name that will be given*/
	int currentIndex;
	
	/**
	 * Constructor
	 * @param itemCount we have to specify the number of items in the dataset.
	 */
	public ItemNameConverter(int itemCount) {
		// initialize the internal data structures
		newNamesToOldNames = new int[itemCount+1];
		oldNamesToNewNames = new HashMap<Integer, Integer>(itemCount);
		currentIndex = 1;
	}
	
	/**
	 * This method takes an old name as parameter and create a new name.
	 * @param oldName the old name
	 * @return the new name
	 */
	public int assignNewName(int oldName) {
		// we give the new name "currentIndex"
		int newName = currentIndex;
		oldNamesToNewNames.put(oldName, newName);
		// we store the old name so that we may convert back to old name if needed
		newNamesToOldNames[newName] = oldName;
		// we increase this variable so that the value + 1 will be the next new name
		// to be given
		currentIndex++;
		// we return the new name
		return newName;
	}
	
	/**
	 * Convert an old name to the corresponding new name.
	 * @param oldName an old name
	 * @return the corresponding new name or null, if no new name exists for that old name.
	 */
	public int toNewName(int oldName) {
		return oldNamesToNewNames.get(oldName);
	}
	
	/**
	 * Convert an old name to the corresponding new name.
	 * @param oldName an old name
	 * @return the corresponding new name or null, if no new name exists for that old name.
	 */
	public int toOldName(int newName) {
		return newNamesToOldNames[newName];
	}

	
}
