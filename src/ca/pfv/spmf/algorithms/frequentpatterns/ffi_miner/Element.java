package ca.pfv.spmf.algorithms.frequentpatterns.ffi_miner;

/* This is an implementation of the FFI-Miner algorithm. 
* 
* Copyright (c) 2016 FFI-Miner
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
 * This class represents an Element of a fuzzy list as used by the FFI-Miner algorithm.
 * 
 * @see AlgoFFIMiner
 * @see FFIList
 * @author Ting Li
 */
public class Element {
	// The three variables as described in the paper:
	/** transaction id */
	final int tid ;   
	/** itemset utility */
	final float iutils; 
	/** remaining utility */
	final float rutils; 
	
	/**
	 * Constructor.
	 * @param tid  the transaction id
	 * @param iutils  the itemset utility
	 * @param rutils  the remaining utility
	 */
	public Element(int tid, float iutils, float rutils){
		this.tid = tid;
		this.iutils = iutils;
		this.rutils = rutils;
	}
}
