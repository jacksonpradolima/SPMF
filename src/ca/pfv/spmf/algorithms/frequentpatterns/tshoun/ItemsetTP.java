package ca.pfv.spmf.algorithms.frequentpatterns.tshoun;


import java.util.ArrayList;
import java.util.List;

import ca.pfv.spmf.algorithms.frequentpatterns.two_phase.AlgoTwoPhase;

/**
 * This class represents an itemset (a set of items) with utility information found
 * by the TWO-PHASE algorithm.
 *
 * @see AlgoTwoPhase
 * @author Philippe Fournier-Viger
 */
public class ItemsetTP{
	/** an itemset is an ordered list of items */
	final int[] items; 
	/** we also indicate the utility of the itemset */
	int utility = 0;
	//	/** this is the set of tids (ids of transactions) containing this itemset */
	List<PeriodUtility> listPeriodUtility = null;

	public void setPeriodUtility(short period, int exactUtility) {
		if(listPeriodUtility == null) {
			listPeriodUtility = new ArrayList<PeriodUtility>();
		}
		listPeriodUtility.add(new PeriodUtility(period, exactUtility));
	}
	
	class PeriodUtility{
		short period;
		int utility;
		public PeriodUtility(short period, int utility) {
			this.period = period;
			this.utility = utility;
		}
	}
	
	
	/**
	 * Default constructor
	 */
	public ItemsetTP(int[] items){
		this.items = items;
	}	

	/**
	 * Get items from that itemset.
	 * @return a list of integers (items).
	 */
	public int[] getItems(){
		return items;
	}
	
	/**
	 * Get the item at at a given position in that itemset
	 * @param index the position
	 * @return the item (Integer)
	 */
	public Integer get(int index){
		return items[index];
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

//	/**
//	 * Set the tidset of this itemset.
//	 * @param listTransactionIds  a set of tids as a Set<Integer>
//	 */
//	public void setTIDset(Set<Integer> tidset) {
//		this.tidset = tidset;
//	}
	
	/**
	 * Get the number of items in this itemset
	 * @return the item count (int)
	 */
	public int size(){
		return items.length;
	}

//	/**
//	 * Get the set of transactions ids containing this itemset
//	 * @return  a tidset as a Set<Integer>
//	 */
//	public Set<Integer> getTIDset() {
//		return tidset;
//	}

	/**
	 * Get the utility of this itemset.
	 * @return utility as an int
	 */
	public int getUtility() {
		return utility;
	}
	
	/**
	 * Increase the utility of this itemset by a given amount.
	 * @param increment  the amount.
	 */
	public void incrementUtility(int increment){
		utility += increment;
	}

}
