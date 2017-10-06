package ca.pfv.spmf.algorithms.frequentpatterns.estDec;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an implementation of a CPTree node. <br\>
 * <br\>
 * 
 * This implementation was made by Azadeh Soltani <br\>
 * <br\>
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).<br\>
 * <br\>
 * 
 * SPMF is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.<br\>
 * <br\>
 * 
 * SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details. <br\>
 * <br\>
 * 
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @see Algo_estDecPlus
 * @see CPTree
 * @author Azadeh Soltani
 */
class ParentNode{
	CPTreeNode pNode;
	short pInd=0;
	
	ParentNode(CPTreeNode mp, short q){
		pNode=mp;
		pInd=q;
	}
	
	boolean equal(ParentNode m){
		return (m.pNode == pNode && m.pInd == pInd);
	}
	
	int isMember(List<ParentNode> parents){
		for (int i=0;i<parents.size();++i)
			if (this.equal(parents.get(i))) return i;
		return -1;
	}
}

public class CPTreeNode {
	List<Integer> itemIDList;// item id list
	List<ParentNode>parents;// parent list 
    double counter1; //counter
	double counter2; // counterII
	List<CPTreeNode> children; // children nodes

	/**
	 * constructor
	 * @param item  the item
	 * @param count the count 
	 * @param k  the last transaction id
	 */
	CPTreeNode(Integer item, CPTreeNode parent, short parentInd, double count) {
		itemIDList = new ArrayList<Integer>();
		itemIDList.add(item);
		parents = new ArrayList<ParentNode>();
		parents.add(new ParentNode(parent,parentInd));
		counter1 = count;
		counter2 = count;
		children = new ArrayList<CPTreeNode>();
	}

	/**
	 * Default constructor
	 */
	CPTreeNode() {
		itemIDList = new ArrayList<Integer>();
		parents = new ArrayList<ParentNode>();
		counter1 = 1;
		counter2 = 1;
		children = new ArrayList<CPTreeNode>();
	}

	/**
	 * Return the immediate index of itemIDList of this node having a given ID. If there is no
	 * such index, return -1;
	 * 
	 * @param id the id
	 * @return the node or null
	 */
	public short getInnerIndexWithID(int id,CPTreeNode parentNode,short parentInd ) {
		
		for (short i = (short) (parentInd + 1); i < itemIDList.size(); ++i)
			if (itemIDList.get(i) == id && parents.get(i).pNode == parentNode
					&& parents.get(i).pInd == parentInd)
				return i;
		return (short) -1;
	}

	/**
	 * Return the immediate child of this node having a given ID. If there is no
	 * such child, return null;
	 * 
	 * @param id the id
	 * @return the node or null
	 */
	public CPTreeNode getChildWithID(int id,int q) {
		if (children == null)
			return null;
		for (CPTreeNode child : children) {
			if (child.itemIDList.get(0)==id && child.parents.get(0).pInd==q) {
			       return child;
			}
		}
		return null;
	}
	
	public int getLevel(int j) {
		int level = 0;
		while (true) {
			level++;
			if (j != 0)
				j = parents.get(j).pInd;
			else
				return level;
		}
	}

	public int getLongestLevel() {
		int level = 1;
		ArrayList<Integer> p = new ArrayList<Integer>();
		p.add(0);
		
		while (true) {
			if (p.size() == 0)
				break;
			level++;
			ArrayList<Integer> p2 = new ArrayList<Integer>();
			for (int j = 1; j < itemIDList.size(); ++j){
				int x=parents.get(j).pInd;
				if (p.contains(x)) {
					p2.add(j);
				}
			}
			p = p2;

		}
		return level-1;
	}

	/**
	 * if the itemID is the leaf level node return true 
	 * else return false;
	 * 
	 * @param id
	 */
	public Boolean isLeafLevel(int id) {

		for (int i = id + 1; i < itemIDList.size(); ++i) {
			if (parents.get(i).pNode == this && parents.get(i).pInd == id)
				return false;
		}

		return true;
	}
	/**
	 * Update the count of a node
	 * 
	 * @param k the current transaction id
	 * @param value the value to be added to the count
	 * @param d  the decay rate
	 */
	
	public void update(double d) {
		counter1 = counter1 * d + 1;
	}
	
	/**
	 * Compute the support of this node as a percentage.
	 * 
	 * @param N an integer representing a transaction count.
	 */
	public double computeSupport(double N,int level) {
		int maxL = getLongestLevel();
		return estimateMergeCount(level, maxL) / N;
	}
	
	public double estimateMergeCount(int level,int longestL){
		if (level == 1)
			return counter1;
		else if (level == longestL)
			return counter2;
		else{
			//function 1 :
		//	double x=counter1	- ((counter1 - counter2) * (level - 1) / (longestL - 1));
			//return counter1	- ((counter1 - counter2) * (level - 1) / (longestL - 1));
			
			//function 2:
			double  x=0;
			for(int l=1;l<=longestL-1;++l)
				x+=1.0/l;
			double s=0;
			for(int l=1;l<=level-1;++l)
				s+=(1.0/l);
//			double f=counter1    -((counter1-counter2)   *   s/x);   
			return   counter1    -((counter1-counter2)   *   s/x);   
		
		}
	}
	
	/**
	 * Method for getting a string representation of this tree 
	 * (to be used for debugging purposes).
	 * @param an indentation
	 * @return a string
	 */
	public String toString(String indent) {
		StringBuilder output = new StringBuilder();
		output.append("ID="+ super.toString());
		output.append(" Counter1="+ counter1);
		output.append(" Counter2="+ counter2);
		output.append(" ItemIDList=");
		for(Integer item: itemIDList) {
			output.append(item + ",");
		}
		output.append(" Parents=");
		for(ParentNode parent: parents) {
			output.append("(pind=" + parent.pInd + ",node=" + parent.pNode.toString() + ")");
		}
		output.append("\n");
		String newIndent = indent + "   ";
		for (CPTreeNode child : children) {
			output.append(newIndent+ child.toString(newIndent));
		}
		return output.toString();
	}
	
}
