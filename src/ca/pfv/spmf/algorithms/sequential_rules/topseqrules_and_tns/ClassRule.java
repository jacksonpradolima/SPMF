package ca.pfv.spmf.algorithms.sequential_rules.topseqrules_and_tns;
/* This file is copyright (c) 2008-2013 Philippe Fournier-Viger
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

import java.util.Map;
import java.util.Set;


/**
 * This class represent a class sequential rule as output by the TopSeqClassRules algorithm.
 * <br/><br/>
 * It is optimized for these algorithms by storing several additional fields that are necessary 
 * for such top-k algorithms. 
 * 
 * @see AlgoTopSeqClassRules
 * @author Philippe Fournier-Viger
 */
public class ClassRule implements Comparable<ClassRule>{
	
	/** antecedent */
	private int[] itemset1; 
	/** consequent */
	private Integer itemset2; 
	/** absolute support */
	int transactioncount; 
	/** the transaction IDs of the antecedent */
	Set<Integer> tidsI;  
	/** the transaction IDs of the consequent */
	Set<Integer> tidsJ; 
	/** transaction IDs of the sequences where the antecedent appears before the consequent */
	Set<Integer> tidsIJ; 	
	/**  maps of last occurences of the antecedent */
	Map<Integer, Short> occurencesJlast; 
	 /** confidence of the rule */
	private double confidence; 
	
	/**
	 * Constructor
	 * @param itemset1 antecedent
	 * @param itemset2 consequent
	 * @param confidence confidence of the rule
	 * @param transactioncount absolute support
	 * @param tidsI the transaction IDs of the antecedent
	 * @param tidsJ the transaction IDs of the consequent
	 * @param tidsIJ transaction IDs of the sequences where the antecedent appears before the consequent
	 * @param occurencesIfirst maps of first occurences of the antecedent
	 * @param occurencesJlast maps of last occurences of the antecedent
	 */
	public ClassRule(int[] itemset1, Integer itemset2, double confidence, int transactioncount, 
			Set<Integer> tidsI, Set<Integer> tidsJ, Set<Integer> tidsIJ, 
			Map<Integer, Short> occurencesJlast){
		this.itemset1 = itemset1;
		this.itemset2 = itemset2;
		this.confidence = confidence;
		this.transactioncount = transactioncount;
		this.tidsI = tidsI;
		this.tidsJ = tidsJ;
		this.tidsIJ = tidsIJ;
		this.occurencesJlast = occurencesJlast;
	}

	/**
	 * Get the antecedent itemset.
	 * @return the antecedent.
	 */
	public int[] getItemset1() {
		return itemset1;
	}
	
	/**
	 * Get the consequent itemset.
	 * @return the consequent.
	 */
	public int getItemset2() {
		return itemset2;
	}
	
	/**
	 * Get the absolute support (a number of sequences)
	 * @return the absolute support
	 */
	public int getAbsoluteSupport(){
		return transactioncount;
	}
	
	/**
	 * Get the relative support (a percentage)
	 * @param sequencecount  the number of sequence in the original sequence database
	 * @return the relative support
	 */
	public double getRelativeSupport(int sequencecount) {
		return ((double)transactioncount) / ((double) sequencecount);
	}

	/**
	 * Print this rule to the console
	 */
	public void print(){
		System.out.println(toString());
	}
	
	/**
	 * Get a string representation of this rule.
	 * @return the string representation
	 */
	public String toString(){
		StringBuilder buffer = new StringBuilder();
		for(int i=0; i< itemset1.length; i++){
			buffer.append(itemset1[i]);
			if(i != itemset1.length-1){
				buffer.append(",");
			}
		}
		buffer.append(" ==> ");
		buffer.append(itemset2);
		return buffer.toString();
	}

	/**
	 * Get the confidence of this rule
	 * @return the confidence
	 */
	public double getConfidence() {
		return confidence;
	}
	
	/**
	 * Compare this rule to another rule
	 * @return 0 if equal, 0< if smaller or >0 if larger
	 */
	public int compareTo(ClassRule o) {
		if(o == this){
			return 0;
		}
		int compare = this.getAbsoluteSupport() - o.getAbsoluteSupport();
		if(compare !=0){
			return compare;
		}
		
		int itemset1sizeA = this.itemset1 == null ? 0 : this.itemset1.length;
		int itemset1sizeB = o.itemset1 == null ? 0 : o.itemset1.length;
		int compare2 = itemset1sizeA - itemset1sizeB;
		if(compare2 !=0){
			return compare2;
		}
		
		int compare4 = Double.compare(this.confidence, o.confidence);
		if(compare4 !=0){
			return compare4;
		}

		return this.hashCode() - o.hashCode();
	}
	
	/**
	 * Check if this rule is equal to another (if they have the same items in their antecedent and consequent).
	 * @parameter o another rule
	 * @return true if equal
	 */
	public boolean equals(Object o){
		ClassRule ruleX = (ClassRule)o;
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
}
