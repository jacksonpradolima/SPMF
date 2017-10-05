package ca.pfv.spmf.algorithms.frequentpatterns.haui_miner;

/* This is an implementation of the HAUI-Miner algorithm. 
* 
* Copyright (c) 2016 HAUI-Miner
* 
* This file is part of the SPMF DATA MINING SOFTWARE * (http://www.philippe-fournier-viger.com/spmf). 
* 
* 
* SPMF is free software: you can redistribute it and/or modify it under the * terms of the GNU General Public License as published by the Free Software * Foundation, either version 3 of the License, or (at your option) any later * version. * 

* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR * A PARTICULAR PURPOSE. See the GNU General Public License for more details. * 
* 
* You should have received a copy of the GNU General Public License along with * SPMF. If not, see . 
* 
* @author Ting Li
*/

/**
 * This class represents an Element of a average-utility list as used by the HAUI-Miner algorithm.
 * 
 * @see AlgoHAUIMiner
 * @see UtilityList
 * @author Ting Li
 */
class Element {
	// The three variables as described in the paper:
	/** transaction id */
	final int tid ;   
	/** itemset utility */
	final int iutils; 
	/** remaining utility */
	final int mutils; 
	
	/**
	 * Constructor.
	 * @param tid  the transaction id
	 * @param iutils  the itemset utility
	 * @param rutils  the remaining utility
	 */
	public Element(int tid, int iutils, int mutils){
		this.tid = tid;
		this.iutils = iutils;
		this.mutils = mutils;
	}
}
