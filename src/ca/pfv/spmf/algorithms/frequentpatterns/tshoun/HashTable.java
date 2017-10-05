package ca.pfv.spmf.algorithms.frequentpatterns.tshoun;


import java.util.ArrayList;
import java.util.List;

import ca.pfv.spmf.algorithms.frequentpatterns.charm.AlgoCharm_Bitset;
import ca.pfv.spmf.datastructures.triangularmatrix.TriangularMatrix;
import ca.pfv.spmf.input.transaction_database_list_integers.TransactionDatabase;

/**
 * This class represents an HashTable for storing itemsets found by the Charm
 * algorithm to perform the closeness check.
 * 
 * @see AlgoCharm_Bitset
 * @see TriangularMatrix
 * @see TransactionDatabase
 * @see Itemset
 * @see Itemsets
 * @author Philippe Fournier-Viger 2014
 */
class HashTable {

	// the internal array for the hash table
	public List<ItemsetTP>[] table;

	/**
	 * Construtor.
	 * @param size size of the internal array for the hash table.
	 */
	public HashTable(int size) {
		table = new ArrayList[size];
	}

	
	/**
	 * Retrieve a given itemset from the hash table if it appears in it
	 * @param itemset the given itemset
	 * @param hashcode the hashcode of the itemset (need to be calculated before by using the
	 *  provided hashcode() method.
	 * @return true if the hash table contains at least one superset, otherwise false.
	 */
	public ItemsetTP retrieveItemset(int[] itemset, int hashcode) {
		// If the position in the array that is given by the hashcode is empty,
		// then return false.
		if (table[hashcode] == null) {
			return null;
		}
		// For each itemset X at that hashcode position
		for (Object object : table[hashcode]) {
			ItemsetTP itemsetX = (ItemsetTP) object;
			// if the support of X is the same as the given itemset and X contains
			// the given itemset
			if (same(itemsetX.getItems(), itemset)) {
				// then return true
				return itemsetX;
			}
		}
		// Otherwise we did not find the itemset in the hashtable
		return null;
	}


	public static boolean same(int[]  itemset1, int[]  itemset2) {
		if(itemset1.length != itemset2.length) {
			return false;
		}
		// Otherwise, we have to compare item by item
		for (int i = 0; i < itemset1.length - 1; i++) {
			// if they are not the last items, they should be the same
			// otherwise return false
			if (itemset1[i] != itemset2[i]) {
				return false;
			}
		}
		// All items are the same. We return true.
		return true;
	}

	/**
	 * Add an itemset to the hash table.
	 * @param itemset the itemset to be added to the hashtable
	 * @param hashcode the hashcode of the itemset (need to be calculated before by using the
	 *  provided hashcode() method.
	 */
	public void put(ItemsetTP itemset, int hashcode) {
		// if the position in the array is empty create a new array list
		// for that position
		if (table[hashcode] == null) {
			table[hashcode] = new ArrayList<ItemsetTP>();
		}
		// store the itemset in the arraylist of that position
		table[hashcode].add(itemset);
	}

	/**
	 * Calculate the hashcode of an itemset as the sum of the tids of its tidset,
	 * modulo the internal array length.
	 * @param tidset the tidset of the itemset
	 * @return the hashcode (an integer)
	 */
	public int hashCode(int[] itemset) {
		int hashcode = 0;
		// for each tid in the tidset
		for (int i=0; i< itemset.length; i++) {
			// make the sum
			hashcode += itemset[i]*3;
		}
		// If an integer overflow occurs and the hashcode is negative,
		// then we make it positive.
		if(hashcode < 0){
			hashcode = 0 - hashcode;
		}
		// Finally the hashcode is obtained by performing the modulo 
		// operation using the size of the internal array.
		return (hashcode % table.length);
	}
}
