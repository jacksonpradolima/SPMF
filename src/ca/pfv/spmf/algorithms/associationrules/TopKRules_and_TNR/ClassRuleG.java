package ca.pfv.spmf.algorithms.associationrules.TopKRules_and_TNR;

/* This file is copyright (c) 2008-2012 Philippe Fournier-Viger
* 
* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
* 
* SPMF is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* 
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/

import java.util.BitSet;

/**
 * This class represents an association rule found by the 
 *  TopKClassRules algorithm for top-k class association rule mining.
 * <br/><br/>
 * 
 * This implementation is optimized for these algorithms. In particular,
 * it stores the tidset of the rule and the tidset of the 
 * rule antecedent as bitsets. Furthermore, the antecedent and 
 * consequent are stored as array of integers.  Lastly,
 * for optimization, the maximum item id of the antecedent
 * is precalculated and kept (as explained in the paper).
 * 
 * @see AlgoTopKClassRules
 * @author Philippe Fournier-Viger
 */
public class ClassRuleG implements Comparable<ClassRuleG>{

	/** antecedent of the rule */
	private Integer[] itemset1; 
	
	/** consequent of the rule */
	private int itemset2; 
	
	/** tidset of the antecedent */
	public BitSet    tids1;  
	
	/**  tidset of the rule */
	public BitSet    common;
	
	/** maximum item id in the antecedent */
	public int maxLeft; 
	
	/** the support of this rule */
	private int count; 
		
	/**
	 * Constructor
	 * @param itemset1  the left itemset
	 * @param itemset2  the right itemset
	 * @param count   support of the rule
	 * @param tids1   tidset of the antecedent
	 * @param common  tidset of the rule
	 * @param maxLeft  maximum item id in the antecedent
	 * @param maxRight maximum item id in the consequent
	 */
	public ClassRuleG(Integer[] itemset1, int itemset2, int count, BitSet tids1, BitSet common, int maxLeft){
		this.count = count;
		this.itemset1 = itemset1;
		this.itemset2 = itemset2;
		this.common =  common;
		this.tids1 = tids1;
		this.maxLeft= maxLeft;
	}

	/**
	 * Get the antecedent.
	 * @return an itemset
	 */
	public Integer[] getItemset1() {
		return itemset1;
	}

	/**
	 * Get the consequent.
	 * @return an itemset
	 */
	public int getItemset2() {
		return itemset2;
	}
	
	/**
	 * Get the support of this rule.
	 * @return the support (integer)
	 */
	public int getAbsoluteSupport(){
		return count;
	}

	/**
	 * Get the confidence of this rule.
	 * @return the confidence (double)
	 */
	public double getConfidence() {
		return ((double)count) / tids1.cardinality();
	}

	
	/**
	 * Compare this rule with another rule "o".
	 * @return 1 if this rule is larger, 0 if equal, or -1 if smaller.
	 *       The comparison is done based on the support,
	 *       then on the size of the antecedent,
	 *       then on the size of the consequent,
	 *       then on the confidence,
	 *       then on the hashCodes.
	 */
	public int compareTo(ClassRuleG o) {
		// if the same object, return 0.
		if(o == this){
			return 0;
		}
		// compare the supports
		int compare = this.getAbsoluteSupport() - o.getAbsoluteSupport();
		if(compare !=0){
			return compare;
		}
		
		//compare antecedent sizes
		int itemset1sizeA = this.itemset1 == null ? 0 : this.itemset1.length;
		int itemset1sizeB = o.itemset1 == null ? 0 : o.itemset1.length;
		int compare2 = itemset1sizeA - itemset1sizeB;
		if(compare2 !=0){
			return compare2;
		}
		
		// compare confidence
		int compare4 = (int)(this.getConfidence()  - o.getConfidence());
		if(compare !=0){
			return compare4;
		}

		// compare hashcodes
		return this.hashCode() - o.hashCode();
	}
	
	public boolean equals(Object o){
		ClassRuleG ruleX = (ClassRuleG)o;
		if(ruleX.itemset1.length != this.itemset1.length){
			return false;
		}
		for(int i=0; i< itemset1.length; i++){
			if(this.itemset1[i] != ruleX.itemset1[i]){
				return false;
			}
		}
		if(this.itemset2 != ruleX.itemset2){
			return false;
		}
		return true;
	}
		
	/**
	 * Get a string representation of this rule.
	 * @return a string
	 */
	public String toString(){
		return toString(itemset1) +  " ==> " + itemset2;
	}

	/**
	 * Return a string representation of an itemset
	 * @param itemset the itemset
	 * @return a string
	 */
	private String toString(Integer[] itemset) {
		StringBuilder temp = new StringBuilder();
		// for each item, add it to the string, separated by a space
		for(int item : itemset){
			temp.append(item + " ");
		}
		return temp.toString();
	}
}
