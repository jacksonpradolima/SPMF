package ca.pfv.spmf.algorithms.frequentpatterns.skymine;

import java.util.ArrayList;
import java.util.List;

/**
 * This represents an UP-Node as used by the Skymine algorithm.
 * 
 * Copyright (c) 2015 Vikram Goyal, Ashish Sureka, Dhaval Patel, Siddharth Dawar
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
 * @see AlgoSkyMine
 * @see UPTree
 * 
 * @author Vikram Goyal, Ashish Sureka, Dhaval Patel, Siddharth Dawar
 */

public class UPNode {
	// String itemName;
	int itemID = -1;
	int count = 1;
	
	long nodeUtility;
	UPNode parent = null;
	
	// the child nodes of that node
	List<UPNode> childs = new ArrayList<UPNode>();

	// link to next node with the same item id (for the
	// header table).
	UPNode nodeLink = null; 
	short min_node_quantity = 0;

	/**
	 * Default constructor
	 */
	public UPNode() {
	}

	/**
	 * method to get child node Return the immediate child of this node having a
	 * given ID(item itself). If there is no such child, return null;
	 */
	UPNode getChildWithID(int name) {
		// for each child node
		for (UPNode child : childs) {
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