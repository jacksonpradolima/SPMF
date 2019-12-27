package ca.pfv.spmf.algorithms.frequentpatterns.hminer;

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


/**
 * This class represents an Element of a utility list as used by the HUI-Miner algorithm.
 * 
 * @see AlgoHUIMiner
 * @see CUL_List
 * @author Siddharth Dawar et al.
 */
class Element_CUL_List {
	// The three variables as described in the paper:
	/** transaction id */
	final int tid ;   
	/** non closed itemset utility */
	long Nu; 
	/** non closed remaining utility */
	 long Nru;
	/** prefix utility */
	 long Pu;
	/** ppos */
	int Ppos;
	
	
	/**
	 * Constructor
	 * @param tid transaction id
	 * @param nu itemset utility
	 * @param nru remaining utility
	 * @param pu  prefix utility
	 * @param ppos
	 */
	public Element_CUL_List(int tid,long nu, long nru, long pu,int ppos ){
		this.tid = tid;
		this.Nu = nu;
		this.Nru = nru;
		this.Pu=pu;
		this.Ppos=ppos;
	}
}
