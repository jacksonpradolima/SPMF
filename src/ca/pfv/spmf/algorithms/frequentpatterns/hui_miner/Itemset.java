package ca.pfv.spmf.algorithms.frequentpatterns.hui_miner;

 
public class Itemset {
	int[] itemset;
	long utility;
	int support;

	public Itemset(int[] itemset, long utility, int support) {
		this.itemset = itemset;
		this.utility = utility;
		this.support = support;
	}

//	@Override
	public String toString() {
		return itemset.toString() + " utility : " + utility;
	}

}
