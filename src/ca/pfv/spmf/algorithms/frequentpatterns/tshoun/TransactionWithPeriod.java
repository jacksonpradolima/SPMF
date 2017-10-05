package ca.pfv.spmf.algorithms.frequentpatterns.tshoun;


import java.util.List;

import ca.pfv.spmf.algorithms.frequentpatterns.two_phase.AlgoTwoPhase;
import ca.pfv.spmf.algorithms.frequentpatterns.two_phase.UtilityTransactionDatabaseTP;


/**
 * This class represents a transaction (a set of items) from a transaction
 * database with utility values, as used by the Two-Phase algorithm for high 
 * utility itemset mining.
 *
 * @see AlgoTwoPhase
 * @see UtilityTransactionDatabaseTP
 * @see ItemUtility
 * 
 * @author Philippe Fournier-Viger
 */
public class TransactionWithPeriod{
	// a transaction is an ordered list of items
	protected final List<ItemUtility> itemsUtilities; 
	// the total transaction utility (TU)
	protected int transactionUtility;
	// the period in which the transaction occurred
	private int period;
	
	/**
	 * Get the period of this transaction
	 * @return the period (an integer >= 0)
	 */
	public int getPeriod() {
		return period;
	}

	/**
	 * Constructor
	 * @param itemsUtilities list of items
	 * @param itemsUtilities list of corresponding utility values
	 * @param transactionUtility  the transaction utility
	 * @param the transaction period (an integer >= 0 )
	 */
	public TransactionWithPeriod(List<ItemUtility> itemsUtilities, int transactionUtility, int period){
		this.itemsUtilities =  itemsUtilities;
		this.transactionUtility = transactionUtility;
		this.period = period;
	}
	
	/**
	 * Get the list of items.
	 * @return a list of items (Integer)
	 */
	public List<ItemUtility> getItems(){
		return itemsUtilities;
	}
	
	/**
	 * Get the item at a given position.
	 * @param index  the position 
	 * @return the item
	 */
	public ItemUtility get(int index){
		return itemsUtilities.get(index);
	}
	
	/**
	 * Print the transaction to System.out
	 */
	public void print(){
		System.out.print(toString());
	}
	
	/**
	 * Return a string representation of this transaction.
	 */
	public String toString(){
		// create a string buffer
		StringBuilder r = new StringBuilder ();
		// append all items
		for(int i=0; i< itemsUtilities.size(); i++){
			r.append(itemsUtilities.get(i) + " ");
			if(i == itemsUtilities.size() -1){
				r.append(":");
			}
		}
		// append the transaction utility
		r.append(transactionUtility + ": ");
		// append the item utility values
		for(int i=0; i< itemsUtilities.size(); i++){
			r.append(itemsUtilities.get(i) + " ");
		}
		// return the buffer as a string
		return r.toString();
	}

	/**
	 * Check if this transaction contains an item.
	 * @param item the given item
	 * @return true if yes, otherwise false.
	 */
	public boolean contains(Integer item) {
		// for each item in the transaction
		for(ItemUtility itemI : itemsUtilities){
			// if found, return true
			if(itemI.item == item){
				return true;
			}else if(itemI.item > item){
				// if the current item is larger, then the item will not be found
				// because of lexical order so return false
				return false;
			}
		}
		// if not found, return false
		return false;
	}
	
	/**
	 * Check if this transaction contains an item.
	 * @param item the given item
	 * @return true if yes, otherwise false.
	 */
	public boolean contains(int item) {
		// for each item in the transaction
		for(int i=0; i<itemsUtilities.size(); i++){
			// if found, return true
			if(itemsUtilities.get(i).item == item){
				return true;
			}else if(itemsUtilities.get(i).item > item){
				// if the current item is larger, then the item will not be found
				// because of lexical order so return false
				return false;
			}
		}
		// if not found, return false
		return false;
	}


	/**
	 * Get the number of items in this transaction.
	 * @return the item count
	 */
	public int size(){
		return itemsUtilities.size();
	}

	/**
	 * Get the item utilities for this transaction.
	 * @return a list containing the item utilities
	 */
	public List<ItemUtility> getItemsUtilities() {
		return itemsUtilities;
	}

	/**
	 * Get the transaction utility of this transaction.
	 * @return  an integer
	 */
	public int getTransactionUtility() {
		return transactionUtility;
	}

}
