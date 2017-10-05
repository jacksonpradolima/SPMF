package ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth;

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


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemset;


/**
 * This is an implementation of a MFITree as used by the FPMax algorithm.
 *
 * @see MFINode
 * @see Itemset
 * @see AlgoFPMax
 * @author Philippe Fournier-Viger
 */
public class MFITree {
	
	// List of pairs (item, frequency) of the header table
	Map<Integer, MFINode> mapItemNodes = new HashMap<Integer, MFINode>();
	
	// Map that indicates the last node for each item using the node links
	// key: item   value: an fp tree node
	Map<Integer, MFINode> mapItemLastNode = new HashMap<Integer, MFINode>();
	
	// root of the tree
	MFINode root = new MFINode(); // null node

	// last added itemset
	MFINode lastAddedItemsetNode = null;
	
	/**
	 * Constructor
	 */
	public MFITree(){	
		
	}

	/**
	 * Method to fix the node link for an item after inserting a new node.
	 * @param item  the item of the new node
	 * @param newNode the new node thas has been inserted.
	 */
	private void fixNodeLinks(Integer item, MFINode newNode) {
		// get the latest node in the tree with this item
		MFINode lastNode = mapItemLastNode.get(item);
		if(lastNode != null) {
			// if not null, then we add the new node to the node link of the last node
			lastNode.nodeLink = newNode;
		}
		// Finally, we set the new node as the last node 
		mapItemLastNode.put(item, newNode); 
		
		MFINode headernode = mapItemNodes.get(item);
		if(headernode == null){  // there is not
			mapItemNodes.put(item, newNode);
		}
	}


	/**
	 * Add an itemset to the MFI-Tree
	 * @param itemset the itemset
	 * @param itemsetLength the length of the itemset
	 * @param support the support of the itemset
	 */
	public void addMFI(int[] itemset, int itemsetLength, int support) {
		
		MFINode currentNode = root;
		// For each item in the itemset
		for(int i=0; i < itemsetLength; i++){
			int item = itemset[i];
			
			// look if there is a node already in the FP-Tree
			MFINode child = currentNode.getChildWithID(item);
			if(child == null){ 
				// there is no node, we create a new one
				MFINode newNode = new MFINode();
				newNode.itemID = item;
				newNode.parent = currentNode;
				// remember at which level in the tree that node appears
				newNode.level = i+1;  
				// we link the new node to its parrent
				currentNode.childs.add(newNode);
				
				// we take this node as the current node for the next for loop iteration 
				currentNode = newNode;
				
				// We update the header table.
				// We check if there is already a node with this id in the header table
				fixNodeLinks(item, newNode);	
			}else{ 
				// there is a node already, we update it
//				child.counter++;
				currentNode = child;
			}
		}
		
//		 SET THE SUPPORT OF THE MFI (the last item)
//		currentNode.counter = support;
		
		// remember that this is the last added itemset
		lastAddedItemsetNode = currentNode;
	}

	/**
	 * Perform the subset test to see if an itemset is a subset of an already
	 * found MFI
	 * @param headWithP the itemset to be tested
	 * @return true if the itemset is not a subset of an already found MFI.
	 */
	public boolean passSubsetChecking(List<Integer> headWithP) {

		// Find the node list for the last item of the itemset
		Integer lastItem = headWithP.get(headWithP.size()-1);
		
		// OPTIMIZATION:
		// We first check against the last added itemset
		if(lastAddedItemsetNode != null) {
			boolean isSubset = issASubsetOfPrefixPath(headWithP, lastAddedItemsetNode);
			// if the itemset is a subset of the last added itemset, we do not need to check further
			if(isSubset) {
				return false;
			}
		}
		
		// OTHERWISE, WE NEED TO COMPARE "headwithP" with all the patterns in the MFI-tree.
		MFINode node = mapItemNodes.get(lastItem);
		// if that last item is not yet in the MFI-tree, it means that "itemset" is not a subset 
		// of some itemset already in the tree
		if(node == null) {
			return true;
		}
		// we will loop over each node by following node links
		do {
			// for a node, we will check if "headwithP" is a subset of the path ending at node
			boolean isSubset = issASubsetOfPrefixPath(headWithP, node);
			// if it is a subset, then "headWithP" is in the MFI-tree, we return false
			if(isSubset) {   
				return false;
			}
			// go to the next itemset to test
			node = node.nodeLink;
		}while(node != null);

		// the itemset is not in the MFI-TREE.  Itemset passed the test!
		return true;
	}

	/**
	 * Check if the itemset headwithP is contained in the path ending at "node" in the MFI-tree
	 * @param headWithP the itemset headwithP
	 * @param node  the node
	 * @return true if "headwithP" is contained in the path ending at "node" in the MFI-Tree. Otherwise, false.
	 */
	private boolean issASubsetOfPrefixPath(List<Integer> headWithP,	MFINode node) {
		// optimization proposed in the fpmax* paper: if there is less than itemset node in that branch,
		// we don't need to check it
		if(node.level >= headWithP.size()) {
			// check if "itemset" is contained in the prefix path ending at "node"
			// We will start comparing from the parent of "node" in the prefix path since
			// the last item of itemset is "node".
			MFINode nodeToCheck = node;
			int positionInItemset = headWithP.size()-1;
			int itemToLookFor = headWithP.get(positionInItemset);
			// for each item in itemset
			do {
				if(nodeToCheck.itemID == itemToLookFor) {
					positionInItemset--;
					// we found the itemset completely, so the subset check test is failed
					if(positionInItemset <0) {
						return true;
					}
					itemToLookFor = headWithP.get(positionInItemset);
				}
				nodeToCheck = nodeToCheck.parent;
			}while(nodeToCheck != null);
		}
		return false;
	}
	
	@Override
	/**
	 * Method for getting a string representation of the CP-tree 
	 * (to be used for debugging purposes).
	 * @return a string
	 */
	public String toString() {
		return "M"+root.toString("");
	}

}
