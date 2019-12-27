package ca.pfv.spmf.algorithms.frequentpatterns.rpgrowth;

/* This file is copyright (c) 2018 Ryan Benton and Blake Johns
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
import java.util.List;

import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemset;

/**
 * This is an implementation of a RPTree node as used by the RPGrowth algorithm.
 *
 * The original "FPNode" was created by Philippe Fournier-Viger and modified by 
 * Blake Johns and Ryan Benton
 * 
 * @see FPTree
 * @see RPTree
 * @see Itemset
 * @see AlgoRPGrowth
 * @author Ryan Benton, Blake Johns
 */
public class RPNode {
	int itemID = -1;  // item id
	int counter = 1;  // frequency counter  (a.k.a. support)
	
	// the parent node of that node or null if it is the root
	RPNode parent = null; 
	// the child nodes of that node
	List<RPNode> childs = new ArrayList<RPNode>();
	
	RPNode nodeLink = null; // link to next node with the same item id (for the header table).
	
	/**
	 * constructor
	 */
	RPNode(){
		
	}

	/**
	 * Return the immediate child of this node having a given ID.
	 * If there is no such child, return null;
	 */
	RPNode getChildWithID(int id) {
		// for each child node
		for(RPNode child : childs){
			// if the id is the one that we are looking for
			if(child.itemID == id){
				// return that node
				return child;
			}
		}
		// if not found, return null
		return null;
	}

	/**
	 * Method for getting a string representation of this tree 
	 * (to be used for debugging purposes).
	 * @param an indentation
	 * @return a string
	 */
	public String toString(String indent) {
		StringBuilder output = new StringBuilder();
		output.append(""+ itemID);
		output.append(" (count="+ counter);
		output.append(")\n");
		String newIndent = indent + "   ";
		for (RPNode child : childs) {
			output.append(newIndent+ child.toString(newIndent));
		}
		return output.toString();
	}
	
	public String toString() {
		return ""+itemID;
	}
}
