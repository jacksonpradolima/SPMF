package ca.pfv.spmf.algorithms.sequentialpatterns.uhuspm;

import java.util.ArrayList;
import java.util.List;


/* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
*
* SPMF is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* SPMF is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with SPMF.  If not, see <http://www.gnu.org/licenses/>.
* 
* @Copyright Ting Li et al., 2018
*/
/**
 * This is an implementation of a sequence or patterns, as used by the UHUSPM algorithm.
 * 
 * @see AlgoUHUSPM
 * @author Ting Li
 */
public class SequenceList {
	
	/** the itemsets in this sequence */
	List<List<Integer>> itemsets = new ArrayList<List<Integer>>();
	
	/** the list of elements */
	List<Element> elements = new ArrayList<Element>();
	
	/** the sum of the utility */
	int sumUtility = 0;
	
	/** the sum of probability */
	float sumProbability = 0;
	
	/** the sum of SWU */
	int sumSWU = 0;
	
	/**
	 * Get the elements
	 * @return the elements
	 */
	public List<Element> getElements(){
		return this.elements;
	}
	
	/** 
	 * Add an element
	 * @param SID a sequence id
	 * @param location the location (itemset number)
	 * @param utility the utility
	 * @param probability the probability
	 * @param SWU the SWU
	 */
	void addElement(int SID, int location, int utility, float probability, int SWU){
		Element element = new Element(SID, location, utility, probability, SWU);
		this.elements.add(element);
	}
	
	/**
	 * Add an itemset
	 * @param itemset the itemset
	 */
	void addItemset(List<Integer> itemset){
		this.itemsets.add(itemset);
	}
	
	/**
	 * Extend using an item
	 * @param pattern
	 * @param item
	 * @param sequnceDatabase
	 */
	void itemBasedExtend( SequenceList pattern, int item, List<List<Itemset>> sequnceDatabase){
		for(Element element : pattern.elements){
			itemBasedAddElement(element, item, sequnceDatabase);
		}
	}
	
	/**
	 * Extend using an item
	 * @param pattern
	 * @param item
	 * @param sequnceDatabase
	 */
	void itemsetBasedExtend( SequenceList pattern, int item, List<List<Itemset>> sequnceDatabase){
		for(Element element : pattern.elements){
			itemsetBasedAddElement(element, item, sequnceDatabase);
		}
	}
	
	/**
	 * Extend using an item
	 * @param element
	 * @param item
	 * @param sequnceDatabase
	 */
	private void itemBasedAddElement(Element element, int item, List<List<Itemset>> sequnceDatabase){
		//item-based add element
		int SID = element.SID;
		int location = element.location;
		int utility = element.utility;
		float probability = element.probability;
		int SWU = element.SWU;
		
		int size = this.elements.size()-1;
		
		for(Item Item: sequnceDatabase.get(element.SID).get(element.location).Itemset){
			if(item == Item.item){
				utility += Item.utility;
				
				if(size >= 0 && SID == this.elements.get(size).SID && location == this.elements.get(size).location){
					this.elements.get(size).utility = Integer.max(this.elements.get(size).utility, utility);
					}else {
						Element newElement = new Element(SID, location, utility, probability, SWU);
						this.elements.add(newElement);
					}
				break;
			}
		}
	}
	
	/**
	 * ...
	 * @param element an element
	 * @param item an item
	 * @param sequnceDatabase a sequene database
	 */
	private void itemsetBasedAddElement(Element element, int item, List<List<Itemset>> sequnceDatabase){
		//itemset-based add element
		int SID = element.SID;
		int size = this.elements.size()-1;
		int SWU = element.SWU;
		
		for(int i = element.location + 1; i<sequnceDatabase.get(SID).size(); i++){
			int utility = element.utility;
			float probability = element.probability;
			int location = i;

			for(Item Item: sequnceDatabase.get(element.SID).get(i).Itemset){
				if(item == Item.item){
					utility += Item.utility;
					
					if(size >= 0 && SID == this.elements.get(size).SID && location == this.elements.get(size).location ){
						this.elements.get(size).utility = Integer.max(this.elements.get(size).utility, utility);

						}else {
							Element newElement = new Element(SID, location, utility, probability, SWU);
							this.elements.add(newElement);
						}
					break;
				}
			}
		}
	}

	/**
	 * Calculate various values (SWU, sum of utility...)
	 */
	void calculate(){

		int order = 0;
		int orderUtility = 0;
		float orderProbability = 0;
		int SWU = 0;
		
		if( !this.elements.isEmpty()){
			order = this.elements.get(0).SID;
			orderUtility = this.elements.get(0).utility;
			orderProbability = this.elements.get(0).probability;
			SWU = this.elements.get(0).SWU;
		}
		for( Element element : this.elements){
			
			if(element.SID ==order){
				
				if(element.utility >= orderUtility){
					orderUtility = element.utility;
				}
				if(element.probability >= orderProbability){
					orderProbability = element.probability;
				}
			}else {
				this.sumUtility +=orderUtility;
				this.sumProbability += orderProbability;
				this.sumSWU += SWU;
				
				order = element.SID;
				orderUtility = element.utility;
				orderProbability = element.probability;
				SWU = element.SWU;
			}
		}
		
		this.sumUtility +=orderUtility;
		this.sumProbability += orderProbability;
		this.sumSWU += SWU;
	}

}
