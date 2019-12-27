package ca.pfv.spmf.algorithms.frequentpatterns.UFH;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is an implementation of the UP-Tree used by UPGrowth algorithm.
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
 * 
 * @author Prashant Barhate
 */

public class UPTree_SPMF {

	// List of items in the header table
	List<Integer> headerList = null;

	// flag that indicate if the tree has more than one path
	boolean hasMoreThanOnePath = false;

	// List of pairs (item, Utility) of the header table
	Map<Integer, UPNode_SPMF> mapItemNodes = new HashMap<Integer, UPNode_SPMF>();

	// root of the tree
	UPNode_SPMF root = new UPNode_SPMF(); // null node

	// Map that indicates the last node for each item using the node links
	// key: item value: an fp tree node (added by Philippe)
	Map<Integer, UPNode_SPMF> mapItemLastNode = new HashMap<Integer, UPNode_SPMF>();

	public UPTree_SPMF() {

	}

	/**
	 * Method for adding a transaction to the up-tree (for the initial
	 * construction of the UP-Tree).
	 * 
	 * @param transaction    reorganised transaction
	 * @param RTU   reorganised transaction utility
	 */
	public void addTransaction(List<Item_SPMF> transaction, int RTU) {
		UPNode_SPMF currentNode = root;
		int i = 0;
		int RemainingUtility = 0;
		int size = transaction.size();

		// For each item in the transaction
		for (i = 0; i < size; i++) {
			for (int k = i + 1; k < transaction.size(); k++) {
				// remaining utility is calculated as sum of utilities of all
				// itms behind currnt one
				RemainingUtility += transaction.get(k).getUtility();
			}

			int item = transaction.get(i).getName();
			int utility=transaction.get(i).getUtility();
			// int itm=Integer.parseInt(item);
			// look if there is a node already in the FP-Tree
			UPNode_SPMF child = currentNode.getChildWithID(item);

			if (child == null) {
				int nodeUtility = (RTU - RemainingUtility);
				// Nodeutility=  previous + (RTU - utility of
				// descendent items)
				RemainingUtility = 0; // reset RemainingUtility for next item
				
				// there is no node, we create a new one
				currentNode = insertNewNode(currentNode, item, nodeUtility,-1,true,utility);
			} else {
				// there is a node already, we update it
				int currentNU = child.nodeUtility; // current node utility
				// Nodeutility=  previous + (RTU - utility of
				// descendent items)
				int nodeUtility = currentNU + (RTU - RemainingUtility); 
				RemainingUtility = 0; // reset RemainingUtility for next item
				child.count++;
				child.nodeUtility = nodeUtility;
				currentNode = child;
				
				
				if(child.min_node_utility>utility)
				{
					child.min_node_utility=utility;
				}/*
				if(child.max_quantity<quantity)
				{
					child.max_quantity=quantity;
				}*/
			}
		}
	}
	public void addTransaction(EFIM_UP_Tree_Transaction_SPMF transaction, int RTU) {
		UPNode_SPMF currentNode = root;
		int i = 0;
		int RemainingUtility = 0;
		//int size = transaction.size();

		// For each item in the transaction
		for (i = transaction.getItems().length-1; i >=0; i--) {
			for (int k = i - 1; k >=0; k--) {
				// remaining utility is calculated as sum of utilities of all
				// itms behind currnt one
				RemainingUtility += transaction.getUtilities()[k];
			}

			int item = transaction.getItems()[i];
			//short quantity=transaction.get(i).getQuantity();
			int utility =transaction.getUtilities()[i]; 
			// int itm=Integer.parseInt(item);
			// look if there is a node already in the FP-Tree
			UPNode_SPMF child = currentNode.getChildWithID(item);

			if (child == null) {
				int nodeUtility = (RTU - RemainingUtility);
				
				//float nodeUtility = (RTU - RemainingUtility);
				// Nodeutility=  previous + (RTU - utility of
				// descendent items)
				RemainingUtility = 0; // reset RemainingUtility for next item
				
				// there is no node, we create a new one
				currentNode = insertNewNode(currentNode, item, nodeUtility,-1,true,utility);
			} else {
				// there is a node already, we update it
				int currentNU = child.nodeUtility; // current node utility
				// Nodeutility=  previous + (RTU - utility of
				// descendent items)
				int nodeUtility = currentNU + (RTU - RemainingUtility); 
				RemainingUtility = 0; // reset RemainingUtility for next item
				child.count++;
				child.nodeUtility = nodeUtility;
				currentNode = child;
				
				
				if(child.min_node_utility>utility)
				{
					child.min_node_utility=utility;
				}			}
		}
	}

	/**
	 * Add a transaction to the UP-Tree (for a local UP-Tree)
	 * @param localPath the path to be inserted
	 * @param pathUtility the path utility
	 * @param pathCount the path count
	 * @param mapMinimumItemUtility the map storing minimum item utility
	 */
	public void addLocalTransaction(List<UPNode_SPMF> localPath, int pathUtility,
			Map<Integer, Integer> mapMinimumItemUtility, int pathCount) {

		
		UPNode_SPMF currentlocalNode = root;
		int i = 0;
		int RemainingUtility = 0;
		int size = localPath.size();
		// For each item in the transaction
		for (i = size-1; i >=0; i--) {
		//for (i = 0; i < size; i++) {
			for (int k = i - 1; k >=0; k--) {
			//for (int k = i + 1; k < localPath.size(); k++) {
				UPNode_SPMF search = localPath.get(k);
				//RemainingUtility += mapMinimumItemUtility.get(search.itemID) * pathCount;
				//RemainingUtility+=search.min_node_utility*AlgoUPGrowth_FHM_Hybrid_Opt.utilityMap.get(search.itemID)*pathCount;
				RemainingUtility+=search.min_node_utility*pathCount;
			}
			int item = localPath.get(i).itemID;

			// look if there is a node already in the UP-Tree
			UPNode_SPMF child = currentlocalNode.getChildWithID(item);

			if (child == null) {
				//float nodeUtility = (pathUtility - temp_nu.get(item));
				int nodeUtility = (pathUtility - RemainingUtility); 
				// Nodeutility=  previous + (RTU - utility of
				// descendent items)
				RemainingUtility = 0; // reset RU for next item
				
				// there is no node, we create a new one
				currentlocalNode = insertNewNode(currentlocalNode, item, nodeUtility,pathCount,false,localPath.get(i).min_node_utility);
			} else {
				// there is a node already, we update it
				int currentNU = child.nodeUtility; // current node utility
				// Nodeutility=  previous + (RTU - utility of
				// descendent items)
				int nodeUtility = currentNU + (pathUtility - RemainingUtility);
				//float nodeUtility = currentNU + (pathUtility - temp_nu.get(item));
				RemainingUtility = 0;
				//child.count++;
				
				child.count=child.count+pathCount;
				child.nodeUtility = nodeUtility;
				currentlocalNode = child;
				if(child.min_node_utility>localPath.get(i).min_node_utility||child.min_node_utility==0)
				{
					child.min_node_utility=localPath.get(i).min_node_utility;
				}
					
			}
		}
		
	}

	/**
	 * Insert a new node in the UP-Tree as child of a parent node
	 * @param currentlocalNode the parent node
	 * @param item the item in the new node
	 * @param nodeUtility the node utility of the new node
	 * @return the new node
	 */
	private UPNode_SPMF insertNewNode(UPNode_SPMF currentlocalNode, int item,	int nodeUtility,int pathCount,boolean global,int min_utility) {
		// create the new node
		UPNode_SPMF newNode = new UPNode_SPMF();
		newNode.itemID = item;
		newNode.nodeUtility = nodeUtility;
		newNode.min_node_utility=min_utility;
		if(global)
			newNode.count = 1;
		else
			newNode.count=pathCount;	
		newNode.parent = currentlocalNode;
		
		// we link the new node to its parrent
		currentlocalNode.childs.add(newNode);

		// check if more than one path
		if (!hasMoreThanOnePath && currentlocalNode.childs.size() > 1) {
			hasMoreThanOnePath = true;
		}

		// We update the header table.
		// We check if there is already a node with this id in the
		// header table
		UPNode_SPMF localheadernode = mapItemNodes.get(item);
		if (localheadernode == null) { // there is not
			mapItemNodes.put(item, newNode);
			mapItemLastNode.put(item, newNode);
		} else { // there is
					// we find the last node with this id.
					// get the latest node in the tree with this item
			UPNode_SPMF lastNode = mapItemLastNode.get(item);
			// we add the new node to the node link of the last node
			lastNode.nodeLink = newNode;

			// Finally, we set the new node as the last node
			mapItemLastNode.put(item, newNode);
		}
		
		// we return this node as the current node for the next loop
		// iteration
		return newNode;
	}

	/**
	 * Method for creating the list of items in the header table, in descending
	 * order of TWU or path utility.
	 * 
	 * @param mapItemToEstimatedUtility
	 *            the Utilities of each item (key: item value: TWU or path
	 *            utility)
	 */
	void createHeaderList(final Map<Integer, Integer> mapItemToEstimatedUtility, final int[] n) {
		// create an array to store the header list with
		// all the items stored in the map received as parameter
		headerList = new ArrayList<Integer>(mapItemNodes.keySet());

		// sort the header table by decreasing order of utility
		Collections.sort(headerList, new Comparator<Integer>() {
			public int compare(Integer id1, Integer id2) {
				// compare the Utility
				if(mapItemToEstimatedUtility.get(n[id1]) > mapItemToEstimatedUtility.get(n[id2]))
					return -1;
				else if(mapItemToEstimatedUtility.get(n[id1]) < mapItemToEstimatedUtility.get(n[id2]))
					return 1;
				else
				{
					if(n[id1]>n[id2])
						return -1;
					else
						return 1;
				}
				
				
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

	public String toString(String indent, UPNode_SPMF node) {
		String output = indent + node.toString() + "\n";
		String childsOutput = "";
		for (UPNode_SPMF child : node.childs) {
			childsOutput += toString(indent + " ", child);
		}
		return output + childsOutput;
	}

}