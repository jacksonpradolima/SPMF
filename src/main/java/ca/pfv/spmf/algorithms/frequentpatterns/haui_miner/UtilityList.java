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

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a AUtilityList as used by the HUI-Miner algorithm.
 *
 * @see AlgoHUIMiner
 * @see Element
 * @author Ting Li
 */
class UtilityList {
	int item;  // the item
	int sumIutils = 0;  // the sum of item utilities
	int sumMutils = 0;  // the sum of remaining utilities
	List<Element> elements = new ArrayList<Element>();  // the elements
	
	/**
	 * Constructor.
	 * @param item the item that is used for this average-utility list
	 */
	public UtilityList(int item){
		this.item = item;
	}
	
	/**
	 * Method to add an element to this average-utility list and update the sums at the same time.
	 */
	public void addElement(Element element){
		sumIutils += element.iutils;
		sumMutils += element.mutils;
		elements.add(element);
	}
}
