package ca.pfv.spmf.algorithms.frequentpatterns.mpfps;

import java.util.*;
/* This file is copyright (c) 2019 Zhitian Li, Philippe Fournier-Viger
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
* 
*/
/**
 * This is an implementation of a sequence of transaction ID list. This structure
 * is used by the MPFPS_BFS and MPFPS_DFS algorithms. It stores an itemset and its sequence of transaction ids and some measures.
 * 
 * @see AlgoMPFPS_BFS
 * @see AlgoMPFPS_DFS
 * @author Zhitian Li, Philippe Fournier-Viger
 **/
public class SeqTidList {
	
	/** the itemset (set of items)*/
	int itemSet[] ;
	
	/** While reading the file, for each single item, we need to know if the read number is
	//a normal transaction, or appears in a new line. If it's in the position of first appearance
	//of a new line, we need to create a new List<Integer>. */
	boolean newLine = false;
	
	/** A sequence of tid set. The outer List means different sequences, inner List denotes the tidSet of a sequence. */
	List<List<Integer>> seqTidSet = new ArrayList<List<Integer>>();
	
	/** The support of itemSet in each sequence.*/ 
	List<Integer> seqSupport = new ArrayList<Integer>();
	
	/** The sequences' ID in which this itemSet appears.*/
	List<Integer> sequenceNum= new ArrayList<Integer>();
	
	/** The periodicity distribution of this periodic frequent pattern in this database.*/
	double[] conf;

	/** the RA value of this pattern */
	public double ra;
	
	/** 
	 * Constructor
	 */
	public SeqTidList(){}
	
	/**
	 * Constructor. The number of items is 1.
	 * @param val a value
	 */
	public SeqTidList(int val){
		itemSet = new int [val];
	}
	
	/**
	 * Get a string representation of an itemset
	 * @param items
	 * @return
	 */
	public String printItemSet(int [] items){
		return Arrays.toString(items);
	}
	
	/**
	 * Get a string representation of this itemset with its measures
	 */
	@Override
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		// print items
		for(int item : itemSet){
			buffer.append(item);
			buffer.append(' ');
		}
		// add the RA value
		buffer.append("#RA:");
		buffer.append(ra);
		// print sids
		buffer.append(" #SIDOCC: ");
		for(int i =0; i< sequenceNum.size(); i++){
			Integer sid = sequenceNum.get(i);
			buffer.append(sid);
			// print tids
			for(Integer tid : seqTidSet.get(i)){
				buffer.append('[');
				buffer.append(tid);
				buffer.append("] ");
			}
		}
		return buffer.toString();
	}

	
}
