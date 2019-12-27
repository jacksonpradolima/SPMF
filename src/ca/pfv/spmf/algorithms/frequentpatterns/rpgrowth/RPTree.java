package ca.pfv.spmf.algorithms.frequentpatterns.rpgrowth;

/* This file is copyright (c) 2018 Ryan Benton and Blake Johns
* 
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

import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemset;

/**
 * This is an implementation of a RPTree  as used by the RPGrowth algorithm.
 * 
 * The original "FP-Tree" was created by Philippe Fournier-Viger and modified by 
 * Blake Johns and Ryan Benton
 *
 * @see FPTree
 * @see RPNode
 * @see Itemset
 * @see AlgoRPGrowth
 * @author Ryan Benton and Blake Johns
 */

public class RPTree {
	// List of items in the header table
		List<Integer> headerList = null;
		
		// List of pairs (item, frequency) of the header table
		Map<Integer, RPNode> mapItemNodes = new HashMap<Integer, RPNode>();
		
		// Map that indicates the last node for each item using the node links
		// key: item   value: an rp tree node
		Map<Integer, RPNode> mapItemLastNode = new HashMap<Integer, RPNode>();
		
		// root of the tree
		RPNode root = new RPNode(); // null node

		/**
		 * Constructor
		 */
		public RPTree(){	
			
		}

		/**
		 * Method for adding a transaction to the RP-tree (for the initial construction
		 * of the RP-Tree).
		 * @param transaction
		 */
		public void addTransaction(List<Integer> transaction) {
			RPNode currentNode = root;
			// For each item in the transaction
			for(Integer item : transaction){
				// look if there is a node already in the RP-Tree
				RPNode child = currentNode.getChildWithID(item);
				if(child == null){ 
					// there is no node, we create a new one
					RPNode newNode = new RPNode();
					newNode.itemID = item;
					newNode.parent = currentNode;
					// we link the new node to its parent
					currentNode.childs.add(newNode);
					
					// we take this node as the current node for the next for loop iteration 
					currentNode = newNode;
					
					// We update the header table.
					// We check if there is already a node with this id in the header table
					fixNodeLinks(item, newNode);	
				}else{ 
					// there is a node already, we update it
					child.counter++;
					currentNode = child;
				}
			}
		}

		/**
		 * Method to fix the node link for an item after inserting a new node.
		 * @param item  the item of the new node
		 * @param newNode the new node that has been inserted.
		 */
		private void fixNodeLinks(Integer item, RPNode newNode) {
			// get the latest node in the tree with this item
			RPNode lastNode = mapItemLastNode.get(item);
			if(lastNode != null) {
				// if not null, then we add the new node to the node link of the last node
				lastNode.nodeLink = newNode;
			}
			// Finally, we set the new node as the last node 
			mapItemLastNode.put(item, newNode); 
			
			RPNode headernode = mapItemNodes.get(item);
			if(headernode == null){  // there is not
				mapItemNodes.put(item, newNode);
			}
		}
		
		/**
		 * Method for adding a prefixpath to a rp-tree.
		 * @param prefixPath  The prefix path
		 * @param mapSupportBeta  The frequencies of items in the prefixpaths
		 * @param relativeMinsupp
		 * @param relativeMinRareSupp
		 */
		void addPrefixPath(List<RPNode> prefixPath, Map<Integer, Integer> mapSupportBeta, int relativeMinsupp, int relativeMinRareSupp) {
			// the first element of the prefix path contains the path support
			int pathCount = prefixPath.get(0).counter;  
			
			RPNode currentNode = root;
			// For each item in the transaction  (in backward order)
			// (and we ignore the first element of the prefix path)
			for(int i = prefixPath.size() -1; i >=1; i--){ 
				RPNode pathItem = prefixPath.get(i);
				// if the item is frequent(if the item is below the relative min rare support and
				// above the relative min support threshold) we skip it.
				if(mapSupportBeta.get(pathItem.itemID) < relativeMinsupp && mapSupportBeta.get(pathItem.itemID) >= relativeMinRareSupp){
		
					// look if there is a node already in the RP-Tree
					RPNode child = currentNode.getChildWithID(pathItem.itemID);
					if(child == null){ 
						// there is no node, we create a new one
						RPNode newNode = new RPNode();
						newNode.itemID = pathItem.itemID;
						newNode.parent = currentNode;
						newNode.counter = pathCount;  // set its support
						currentNode.childs.add(newNode);
						currentNode = newNode;
						// We update the header table.
						// and the node links
						fixNodeLinks(pathItem.itemID, newNode);		
					}else{ 
						// there is a node already, we update it
						child.counter += pathCount;
						currentNode = child;
					}
				}
			}
		}

		/**
		 * Method for creating the list of items in the header table, 
		 *  in descending order of support.
		 * @param mapSupport the frequencies of each item (key: item  value: support)
		 */
		void createHeaderList(final Map<Integer, Integer> mapSupport) {
			// create an array to store the header list with
			// all the items stored in the map received as parameter
			headerList =  new ArrayList<Integer>(mapItemNodes.keySet());
			
			// sort the header table by decreasing order of support
			Collections.sort(headerList, new Comparator<Integer>(){
				public int compare(Integer id1, Integer id2){
					// compare the support
					int compare = mapSupport.get(id2) - mapSupport.get(id1);
					// if the same frequency, we check the lexical ordering!
					// otherwise we use the support
					return (compare == 0) ? (id1 - id2) : compare;
				}
			});
		}
		
		@Override
		/**
		 * Method for getting a string representation of the CP-tree 
		 * (to be used for debugging purposes).
		 * @return a string
		 */
		public String toString() {
			String temp = "F";
			// append header list
			temp += " HeaderList: "+ headerList + "\n";
			// append child nodes
			temp += root.toString("");
			return temp;
		}

		
}
