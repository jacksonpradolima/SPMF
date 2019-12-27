package ca.pfv.spmf.algorithms.frequentpatterns.memu;

/* This file is copyright (c) 2008-2019 Shi-Feng Ren
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
*
*/

import java.util.ArrayList;
import java.util.List;


/**
 * This is an implementation of the "MEMU" algorithm for High-Average-Utility Itemsets Mining
 * as described in the conference paper : <br/><br/>
 *
 * Jerry Chun-Wei Lin, Shifeng Ren, and Philippe Fournier-Viger. MEMU: More Efficient Algorithm to Mine High Average-Utility Patterns with Multiple Minimum Average-Utility Thresholds (unpublished)
 *
 * @see CAUEntry
 * @see AlgoMEMU
 * @author Shi-Feng Ren
 */

 class CAUList {
    /** the item */
	int item;
	
    /** the sum of item utilities */
	long sumUtility = 0;
	
    /** the sum of revised maximal utilities */
	long sumOfRmu = 0;
	
	/** the sum of remaining maximal utilities */
    long sumOfRemu = 0;

    /** List container for each entry of item */
	List<CAUEntry> cauEntries = new ArrayList<CAUEntry>();

	/**
	 * Constructor
	 * @param item an item
	 */
	public CAUList(Integer item){
		this.item = item;
	}


	/**
	 * Add an element to the CAUList
	 * @param cauEntry and element (entry)
	 */
	public void addElement(CAUEntry cauEntry){
		sumUtility += cauEntry.utility;
		sumOfRmu += cauEntry.rmu;
		sumOfRemu += cauEntry.remu;
		cauEntries.add(cauEntry);
	}
}
