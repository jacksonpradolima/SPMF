package ca.pfv.spmf.algorithms.frequentpatterns.haui_mmau;

/* This is an implementation of the HAUI-MMAU algorithm. 
* 
* Copyright (c) 2016 HAUI-MMAU
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class represents an itemset (a set of items) with average utility information found
 * by the HAUI-MMAU algorithm.
 *
 * @see AlgoHAUIMMAU
 * @author Ting Li, 2016
 */
public class ItemsetTP{
	/** an itemset is an ordered list of items */
	private final List<Integer> items = new ArrayList<Integer>(); 
	/** we also indicate the utility of the itemset */
	private int utility =0;
	/** this is the set of tids (ids of transactions) containing this itemset */
	private Set<Integer> transactionsIds = null;
	/**this is the minimum utiliity threshold of the itemset*/
	private int mau=0;
	/**
	 * Default constructor
	 */
	public ItemsetTP(){
	}

	/**
	 * Get the relative support of this itemset
	 * @param nbObject  the number of transactions
	 * @return the support
	 */
	public double getRelativeSupport(int nbObject) {
		return ((double)transactionsIds.size()) / ((double) nbObject);
	}
	
	/**
	 * Get the relative support of this itemset
	 * @param nbObject  the number of transactions
	 * @return the support
	 */
	public String getRelativeSupportAsString(int nbObject) {
		// calculate the support
		double frequence = ((double)transactionsIds.size()) / ((double) nbObject);
		// format it to use two decimals
		DecimalFormat format = new DecimalFormat();
		format.setMinimumFractionDigits(0); 
		format.setMaximumFractionDigits(4); 
		// return the formated support
		return format.format(frequence);
	}
	
	/**
	 * Get the absolute support of that itemset
	 * @return the absolute support (integer)
	 */
	public int getAbsoluteSupport(){
		return transactionsIds.size();
	}

	/**
	 * Add an item to that itemset
	 * @param value the item to be added
	 */
	public void addItem(Integer value){
			items.add(value);
	}
	
	/**
	 * Get items from that itemset.
	 * @return a list of integers (items).
	 */
	public List<Integer> getItems(){
		return items;
	}
	
	/**
	 * Get the item at at a given position in that itemset
	 * @param index the position
	 * @return the item (Integer)
	 */
	public Integer get(int index){
		return items.get(index);
	}
	
	/**
	 * print this itemset to System.out.
	 */
	public void print(){
		System.out.print(toString());
	}
	
	/**
	 * Get a string representation of this itemset
	 * @return a string
	 */
	public String toString(){
		// create a string buffer
		StringBuilder r = new StringBuilder ();
		// for each item
		for(Integer attribute : items){
			// append it
			r.append(attribute.toString());
			r.append(' ');
		}
		// return the string
		return r.toString();
	}

	/**
	 * Set the tidset of this itemset.
	 * @param listTransactionIds  a set of tids as a Set<Integer>
	 */
	public void setTIDset(Set<Integer> listTransactionIds) {
		this.transactionsIds = listTransactionIds;
	}
	
	/**
	 * Get the number of items in this itemset
	 * @return the item count (int)
	 */
	public int size(){
		return items.size();
	}

	public float getItemsetMau(Map<Integer, Integer> mutipleMinUtilities, int GLMAU){
		float sumMau=0;
		for(int i=0;i<items.size();i++){
			if(GLMAU>mutipleMinUtilities.get(items.get(i)))
				sumMau+=GLMAU;
			else sumMau+=mutipleMinUtilities.get(items.get(i));
		}

		return (float)sumMau/this.items.size();
	}
	/**
	 * Get the set of transactions ids containing this itemset
	 * @return  a tidset as a Set<Integer>
	 */
	public Set<Integer> getTIDset() {
		return transactionsIds;
	}

	/**
	 * Get the average utility of this itemset.
	 * @return average-utility as an int
	 */
	public int getAUtility() {
		return utility/items.size();
	}
	
	/**
	 * Increase the utility of this itemset by a given amount.
	 * @param increment  the amount.
	 */
	public void incrementUtility(int increment){
		utility += increment;
	}
}
