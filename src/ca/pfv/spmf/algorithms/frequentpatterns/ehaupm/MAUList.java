package ca.pfv.spmf.algorithms.frequentpatterns.ehaupm;

/** * * * This is an implementation of the EHAUPM algorithm.
*
* Copyright (c) 2018 Shi-Feng Ren
*
* This file is part of the SPMF DATA MINING SOFTWARE  (http://www.philippe-fournier-viger.com/spmf).
*
*
* SPMF is free software: you can redistribute it and/or modify it under the * terms of the GNU General Public License as published by the Free Software * Foundation, either version 3 of the License, or (at your option) any later * version. *

* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR * A PARTICULAR PURPOSE. See the GNU General Public License for more details. *
*
* You should have received a copy of the GNU General Public License along with * SPMF. If not, see .
*
* @author Shi-Feng Ren
*/

import java.util.ArrayList;
import java.util.List;


/**
 * This is an implementation of the "EHAUPM" algorithm for High-Average-Utility Itemsets Mining
 * as described in the jouranl paper : <br/><br/>
 *
 *  Lin C W, Ren S, Fournier-Viger P, et al. EHAUPM: Efficient High Average-Utility Pattern Mining with Tighter Upper-Bounds[J]. IEEE Access, 2017, PP(99):1-1.
 *
 * @see MAUEntry
 * @see AlgoEHAUPM
 * @author Shi-Feng Ren
 */

public class MAUList {
    // the item
	int item;
	// sum of utilities
	long sumutils = 0;
	// sum of remaining utilities
	long sumOfRemu = 0;
    // sum of revised utilities
	long sumOfRmu = 0;
	// container of MAUEntry,
	List<MAUEntry> CAUEntries = new ArrayList<MAUEntry>();

	public MAUList(int item){
		this.item = item;
	}

	public void addElement(MAUEntry MAUEntry) {
		sumutils += MAUEntry.utility;
		sumOfRmu += MAUEntry.rmu;
		sumOfRemu += MAUEntry.remu;
		CAUEntries.add(MAUEntry);
	}

}
