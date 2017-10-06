package ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan;

/**
 * Class representing a position in a pseudo-sequence.
 * (position of an itemset + position of an item).
 */
 class Position{
	final int itemset;
	final int item;
	public Position(int itemset, int item){
		this.itemset = itemset;
		this.item = item;
	}
	
	public String toString() {
		return "(" + itemset + "," + item + ")";
	}
}