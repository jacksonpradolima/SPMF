package ca.pfv.spmf.algorithms.frequentpatterns.skymine;

/**
 * This is a list of nodes as used by the SkyMine algorithm.
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

public class NodeList {
	/** an item */
	int item;

	/** a pointer to the next node */
	NodeList next;
	
	/** 
	 * Constructor
	 * @param itemName a item
	 */
	public NodeList(int itemName) {
		this.item = itemName;
		this.next = null;
	}

	/**
	 * Get the item name
	 * @return the item
	 */
	public int getItemName() {
		return this.item;
	}

	/**
	 * Get the next node in this list
	 * @return  a NodeList object
	 */
	public NodeList getNextNode() {
		return this.next;
	}

	/**
	 * Add a node as successor of this node
	 * @param node the node to be added
	 */
	public void addNode(NodeList node) {
		this.next = node;
	}
}