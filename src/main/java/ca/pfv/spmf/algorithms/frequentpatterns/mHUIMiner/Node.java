package ca.pfv.spmf.algorithms.frequentpatterns.mHUIMiner;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an implementation of the Node structure used by the IHUP tree structure in
 * AlgoSimba algorithm.
 * 
 * @see AlgoMHUIMiner
 * @see IHUPTreeMod
 * @author Prashant Barhate, modified by Alex Peng
 * 
 */

public class Node {
	/** the id of an item */
	int itemID = -1;
	
	/** count information for that item in the tree*/
	int count = 1;

	/** a pointer to a parent node */
	Node parent = null;
	
	/** the child nodes of that node **/
	List<Node> childs = new ArrayList<Node>();

	/** link to the next node with the same item id (for the header table) */
	Node nodeLink = null; 

	/**
	 * Default constructor
	 */
	public Node() {
	}

	/**
	 * method to get child node 
	 * Return the immediate child of this node having a given ID(item itself). 
	 * If there is no such child, return null;
	 */
	Node getChildWithID(int name) {
		// for each child node
		for (Node child : childs) {
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
		return "(i=" + itemID + " count=" + count + ")";
	}

}