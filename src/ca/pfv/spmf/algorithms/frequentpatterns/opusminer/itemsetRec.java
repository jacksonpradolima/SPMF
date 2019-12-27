package ca.pfv.spmf.algorithms.frequentpatterns.opusminer;

/**
 * This is the ItemsetRec structure used by the Opus-Miner algorithm proposed in : </br></br>
 * 
 * Webb, G.I. & Vreeken, J. (2014) Efficient Discovery of the Most Interesting Associations.
  ACM Transactions on Knowledge Discovery from Data. 8(3), Art. no. 15.
 *
 *  The code was translated from C++ to Java.  The modifications to the original C++ code to obtain Java
 *  code and improvements are copyright by Xiang Li and Philippe Fournier-Viger, 
 *  while the original C++ code is copyright by Geoff Web.
 *  
 *  The code is under the GPL license.
 */
public class itemsetRec extends itemset implements Comparable<itemsetRec> {
	
	public int compareTo(itemsetRec otherInstance) {
		if (lessThan(otherInstance)) {
			return -1;
		} else if (otherInstance.lessThan(this)) {
			return 1;
		}
		return 0;
	}
	
	// used for sorting itemsets
	public boolean lessThan(itemsetRec pI) {
		return (value < pI.value);
	}

	public itemsetRec() {
		this.count = 0;
		this.value = 0.0F;
		this.p = 1.0;
		this.self_sufficient = true;
	}



	public int count;
	public float value;
	public double p;
	public boolean self_sufficient;
}