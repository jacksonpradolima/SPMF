package ca.pfv.spmf.algorithms.frequentpatterns.UFH;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an implementation of the UP-Node structure used by Up Tree in
 * UPGrowth algorithm.
 * 
 * Copyright (c) 2014 Prashant Barhate
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE *
 * (http://www.philippe-fournier-viger.com/spmf).
 * 
 * SPMF is free software: you can redistribute it and/or modify it under the *
 * terms of the GNU General Public License as published by the Free Software *
 * Foundation, either version 3 of the License, or (at your option) any later *
 * version. SPMF is distributed in the hope that it will be useful, but WITHOUT
 * ANY * WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @see AlgoUFH
 * @see UPTree_SPMF
 * 
 * @author Prashant Barhate
 * 
 */

public class UPNode_SPMF {
	// String itemName;
	int itemID = -1;
	int count = 1;
	//short quantity=0;
	int nodeUtility;
	UPNode_SPMF parent = null;
	// the child nodes of that node
	List<UPNode_SPMF> childs = new ArrayList<UPNode_SPMF>();

	UPNode_SPMF nodeLink = null; // link to next node with the same item id (for the
							// header table).
	int min_node_utility=0;
	
	
	//short max_quantity=0;
	/**
	 * Default constructor
	 */
	public UPNode_SPMF() {
	}

	/**
	 * method to get child node Return the immediate child of this node having a
	 * given ID(item itself). If there is no such child, return null;
	 */
	UPNode_SPMF getChildWithID(int name) {
		// for each child node
		for (UPNode_SPMF child : childs) {
			// if the ID(item itself) is the one that we are looking for
			if (child.itemID == name) {
				// return that node
				return child;
			}
		}
		// if not found, return null
		return null;
	}

	@Override
	public String toString() {
		return "(i=" + itemID + " count=" + count + " nu=" + nodeUtility + ")";
	}

}