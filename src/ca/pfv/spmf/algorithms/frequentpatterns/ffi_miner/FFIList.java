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

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a FFIList as used by the FFI-Miner algorithm.
 *
 * @see AlgoFFIMiner
 * @see Element
 * @author Ting Li
 */
public class FFIList {
	Integer item;  // the item
	float sumIutils = 0;  // the sum of fuzzy utilities
	float sumRutils = 0;  // the sum of remaining fuzzy utilities
	List<Element> elements = new ArrayList<Element>();  // the elements
	
	/**
	 * Constructor.
	 * @param item the item that is used for this utility list
	 */
	public FFIList(Integer item){
		this.item = item;
	}
	
	/**
	 * Method to add an element to this fuzzy list and update the sums at the same time.
	 */
	public void addElement(Element element){
		sumIutils += element.iutils;
		sumRutils += element.rutils;
		elements.add(element);
	}
}
