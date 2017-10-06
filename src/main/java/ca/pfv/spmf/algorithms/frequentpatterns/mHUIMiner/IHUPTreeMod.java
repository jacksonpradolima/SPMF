package ca.pfv.spmf.algorithms.frequentpatterns.mHUIMiner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is an implementation of the IHUP-Tree used by mHUIMiner. 
 * The tree is ordered based on TWU in descending order
 * 
 * @see AlgoMHUIMiner
 * @author Prashant Barhate, modified by Alex Peng
 */

public class IHUPTreeMod {

	// List of items in the header table
	List<Integer> headerList = null;

	// flag that indicate if the tree has more than one path
	boolean hasMoreThanOnePath = false;

	// List of pairs (item, Node)
	Map<Integer, Node> mapItemNodes = new HashMap<Integer, Node>();

	// root of the tree
	Node root = new Node(); 

	// Map that indicates the last node for each item using the node links
	// Used to help build links between nodes that have the same itemID
	// Should not be used elsewhere 
	// key: itemID, value: Node 
	Map<Integer, Node> mapItemLastNode = new HashMap<Integer, Node>();

	public IHUPTreeMod() {

	}

	/**
	 * Method for adding a transaction to the IHUP-tree (for the initial
	 * construction of the IHUP-Tree).
	 * 
	 * @param transaction
	 *            reorganized transaction that contains Item objects
	 * @param tid
	 *            transaction ID
	 */

	public void addTransaction(List<Item> transaction, int tid) {
		Node currentNode = root;

		// Because transaction is ordered based on TWU in ascending order,
		// we add items from the tail of the transaction
		for (int i = transaction.size() - 1; i >= 0; i--) {
			int itemID = transaction.get(i).getItemID();

			// check if there is a node already in the IHUP-Tree
			Node child = currentNode.getChildWithID(itemID);
			if (child == null) {
				// there is no node, we create a new one
				currentNode = insertNewNode(currentNode, itemID);
			} else {
				// there is a node already, we update it
				child.count++;
				currentNode = child;
			}
		}
	}


	/**
	 * Add a transaction to the local IHUP-Tree (for a local tree!!!)
	 * 
	 * @param localPath
	 *            the path to be inserted
	 */
	public void addLocalTransaction(List<Integer> localPath) {

		Node currentlocalNode = root;

		// For each item in the transaction
		for (int i = localPath.size() - 1; i >= 0; i--) {
			// new item to be inserted
			int itemID = localPath.get(i);
			// check if there is a node already in the tree
			Node child = currentlocalNode.getChildWithID(itemID);

			if (child == null) {
				// there is no node, we create a new one
				currentlocalNode = insertNewNode(currentlocalNode, itemID);
			} else {
				// there is a node already, we update it
				child.count++;
				currentlocalNode = child;
			}
		} // end for
	}


	/**
	 * Insert a new node in the IHUP-Tree as child of a parent node
	 * 
	 * @param currentlocalNode
	 *            the parent node
	 * @param itemID
	 *            the item in the new node
	 * @return the new node
	 */
	private Node insertNewNode(Node currentlocalNode, int itemID) {
		// create the new node
		Node newNode = new Node();
		newNode.itemID = itemID;
		newNode.count = 1;
		newNode.parent = currentlocalNode;

		// we link the new node to its parent
		currentlocalNode.childs.add(newNode);

		// check if more than one path
		if (!hasMoreThanOnePath && currentlocalNode.childs.size() > 1) {
			hasMoreThanOnePath = true;
		}

		// check if there is already a node with this id in the header table
		Node localheadernode = mapItemNodes.get(itemID);
		if (localheadernode == null) {
			mapItemNodes.put(itemID, newNode);
			mapItemLastNode.put(itemID, newNode);
		} else { // If there is a node with this id already
					// we find the last node with this id.
					// get the latest node in the tree with this item
			Node lastNode = mapItemLastNode.get(itemID);
			// we add the new node to the node link of the last node
			lastNode.nodeLink = newNode;

			// Finally, we set the new node as the last node
			mapItemLastNode.put(itemID, newNode);
		}

		// we return this node as the current node for the next loop
		return newNode;
	}

	/**
	 * Method for creating the header table for IHUP tree, in descending
	 * order of TWU.
	 * 
	 * @param mapItemToTWU
	 *            the TWU of each item (key: item, value: TWU)
	 */
	void createHeaderList(final Map<Integer, Integer> mapItemToTWU) {
		// create an array to store the header list with
		// all the items stored in the map received as parameter
		headerList = new ArrayList<Integer>(mapItemNodes.keySet());

		// sort the header table by decreasing order of utility
		Collections.sort(headerList, new Comparator<Integer>() {
			public int compare(Integer id1, Integer id2) {
				// compare the Utility
				int compare = mapItemToTWU.get(id2) - mapItemToTWU.get(id1);
				// if the same utility, we check the lexical ordering!
				if (compare == 0) {
					return (id1 - id2);
				}
				// otherwise we use the utility
				return compare;
			}
		});
	}

	@Override
	public String toString() {
		String output = "";
		output += "HEADER TABLE: " + mapItemNodes + " \n";
		output += "hasMoreThanOnePath: " + hasMoreThanOnePath + " \n";
		return output + toString("", root);
	}

	public String toString(String indent, Node node) {
		String output = indent + node.toString() + "\n";
		String childsOutput = "";
		for (Node child : node.childs) {
			childsOutput += toString(indent + " ", child);
		}
		return output + childsOutput;
	}

}