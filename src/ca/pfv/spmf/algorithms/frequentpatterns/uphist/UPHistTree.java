package ca.pfv.spmf.algorithms.frequentpatterns.uphist;
/* This file is copyright (c) 2018+  by Siddharth Dawar et al.
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A UP-Hist tree as used by the UPHist algorithm.
 * @author S. Dawar et al.
 *@see AlgoUPHist
 */
public class UPHistTree {

	List<Integer> headerList = null;
	public UPHistNode histogram;
		// flag that indicate if the tree has more than one path
	boolean hasMoreThanOnePath = false;

	// List of pairs (item, Utility) of the header table
	Map<Integer, UPHistNode> mapItemNodes = new HashMap<Integer, UPHistNode>();
	

	// root of the tree
	UPHistNode root = new UPHistNode(); // null node

	// Map that indicates the last node for each item using the node links
	// key: item value: an fp tree node (added by Philippe)
	Map<Integer, UPHistNode> mapItemLastNode = new HashMap<Integer, UPHistNode>();

	public UPHistTree() {

	}

	/**
	 * Method for adding a transaction to the up-tree (for the initial
	 * construction of the UP-Tree).
	 * 
	 * @param transaction    reorganised transaction
	 * @param RTU   reorganised transaction utility
	 */
	public void addTransaction(List<Item> transaction, int RTU) {
		UPHistNode currentNode = root;
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
			int quantity=transaction.get(i).getUtility();
	
		//	LocalItemSet temp = new LocalItemSet(item, quantity);
			// int itm=Integer.parseInt(item);
			// look if there is a node already in the FP-Tree
			UPHistNode child = currentNode.getChildWithID(item);
             //mm1.put(item,child.count);
			if (child == null) {
				int nodeUtility = (RTU - RemainingUtility);
				// Nodeutility=  previous + (RTU - utility of
				// descendent items)
				RemainingUtility = 0; // reset RemainingUtility for next item
				
				// there is no node, we create a new one
				currentNode = insertNewNode(currentNode, item, nodeUtility,-1,true,quantity,null,null);
			} else {
				// there is a node already, we update it
				int currentNU = child.nodeUtility; // current node utility
				// Nodeutility=  previous + (RTU - utility of
				// descendent items)
				int nodeUtility = currentNU + (RTU - RemainingUtility); 
				RemainingUtility = 0; // reset RemainingUtility for next item
				child.count++;
				child.nodeUtility = nodeUtility;
				child.histogram.updateHist(quantity,child.count);
				currentNode = child;
				
			}
		}
	}

	/**
	 * Add a transaction to the UP-Tree (for a local UP-Tree)
	 * @param localPath the path to be inserted
	 * @param pathUtility the path utility
	 * @param pathCount the path count
	 * @param mapMinimumItemUtility the map storing minimum item utility
	 */
	public void addLocalTransaction(List<UPHistNode> localPath, int pathUtility,
			Map<Integer, Integer> mapMinimumItemUtility, int pathCount) {
		UPHistNode currentlocalNode = root;
		int i = 0;
		int RemainingUtility = 0;
		int size = localPath.size();
		// For each item in the transaction
		for (i = 0; i < size; i++) {
			for (int k = i + 1; k < localPath.size(); k++) {
				UPHistNode search = localPath.get(k);
				RemainingUtility+=search.histogram.getMinSupportInterU(pathCount);
				//RemainingUtility=0;
			}
			int item = localPath.get(i).itemID;
			UPHistNode child = currentlocalNode.getChildWithID(item);
        	if (child == null) {
				int nodeUtility = (pathUtility - RemainingUtility); 
				RemainingUtility = 0; // reset RU for next item
				currentlocalNode = insertNewNode(currentlocalNode, item, nodeUtility,pathCount,false,(short)0,localPath.get(i).histogram,localPath.get(i).uid);
			} else {
				// there is a node already, we update it
				int currentNU = child.nodeUtility; // current node utility
				int nodeUtility = currentNU + (pathUtility - RemainingUtility);
				RemainingUtility = 0;
				child.count=child.count+pathCount;
				child.nodeUtility = nodeUtility;
				if(child.uid!=localPath.get(i).uid)
					child.histogram.updateHist(localPath.get(i).histogram);
				currentlocalNode = child;
				
				
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
	private UPHistNode insertNewNode(UPHistNode currentlocalNode, int item,	int nodeUtility,int pathCount,boolean global,int quantity,Hist hist_previous,UUID uid) {
//		UPHistNode child=currentlocalNode.getChildWithID(item);
		UPHistNode newNode = new UPHistNode();
		newNode.itemID = item;
		newNode.nodeUtility = nodeUtility;
		if(global)
			newNode.uid=UUID.randomUUID();
		else
			newNode.uid=uid;
		
		if(global)
			newNode.count = 1;
		else
			newNode.count=pathCount;	
		if(global)
		{
			newNode.histogram=new Hist();
			newNode.histogram.H.put(quantity,1); 
		}
		else {
			newNode.histogram=new Hist();
			newNode.histogram.updateHist(hist_previous);
		}
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
		UPHistNode localheadernode = mapItemNodes.get(item);
		if (localheadernode == null) { // there is not
			mapItemNodes.put(item, newNode);
			mapItemLastNode.put(item, newNode);
		} else { // there is
					// we find the last node with this id.
					// get the latest node in the tree with this item
			UPHistNode lastNode = mapItemLastNode.get(item);
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
	void createHeaderList(final Map<Integer, Integer> mapItemToEstimatedUtility) {
		// create an array to store the header list with
		// all the items stored in the map received as parameter
		headerList = new ArrayList<Integer>(mapItemNodes.keySet());

		// sort the header table by decreasing order of utility
		Collections.sort(headerList, new Comparator<Integer>() {
			public int compare(Integer id1, Integer id2) {
				// compare the Utility
				int compare = (int)(mapItemToEstimatedUtility.get(id2)
						- mapItemToEstimatedUtility.get(id1));
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

	public String toString(String indent, UPHistNode node) {
		String output = indent + node.toString() + "\n";
		String childsOutput = "";
		for (UPHistNode child : node.childs) {
			childsOutput += toString(indent + " ", child);
		}
		return output + childsOutput;
	}

}