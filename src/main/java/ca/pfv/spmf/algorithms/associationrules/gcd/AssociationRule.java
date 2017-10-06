/**
 * Warranty disclaimer: This software is provided 'as-is', without any express or implied warranty.    
 * In no event will the author(s) be held liable for any damages arising from the use of this software.  
 * 
 * Copyrights (c) 2015 to Ahmed El-Serafy (a.elserafy@ieee.org) and Hazem El-Raffiee (hazem.farouk.elraffiee@gmail.com)  
 * 
 * All of the files that are part of the GCDs Association Rules algorithm are licensed under either GPL v.3 or dual-licensed under the following terms.  
 * 
 * 1- Any use of the provided source code must be preceded by a written authorization from one of the author(s).  
 * 2- The license text must be kept in source files headers.   
 * 3- The use of the provided source code must be acknowledged in the project documentation and any consequent presentations or documents.   
 * This is achieved by referring to the original repository (https://bitbucket.org/aelserafy/gcd-association-rules)  
 * 4- Any enhancements introduced to the provided algorithm must be shared with the original author(s) along with its source code and changes log.   
 * This is if you are building directly or indirectly upon the algorithm provided by the original author(s).  
 * 5- The public availability of the new source code is provided upon agreement with the original author(s).  
 * 6- For commercial distribution and use, a license agreement must be obtained from one of the author(s).  
*/

package ca.pfv.spmf.algorithms.associationrules.gcd;

public class AssociationRule {
	private MyBigInteger antecedent;
	private MyBigInteger consequent;
	private double support;
	private double confidence;
	private static int totalTransactionsCount;

	public AssociationRule(MyBigInteger antecedent, MyBigInteger consequent, double support, double confidence) {
		this.antecedent = antecedent;
		this.consequent = consequent;
		this.support = support;
		this.confidence = confidence;
	}

	public static void setTotalTransactionsCount(int transactionsCount) {
		AssociationRule.totalTransactionsCount = transactionsCount;
	}

	public static int getTotalTransactionsCount() {
		return totalTransactionsCount;
	}

	public MyBigInteger getAntecedent() {
		return antecedent;
	}

	public MyBigInteger getConsequent() {
		return consequent;
	}

	public Double getSupport() {
		return support;
	}

	public double getConfidence() {
		return confidence;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AssociationRule) {
			AssociationRule associationRule = (AssociationRule) obj;
			if (!associationRule.antecedent.equals(antecedent))
				return false;
			if (!associationRule.consequent.equals(consequent))
				return false;
			if (!(associationRule.support == support))
				return false;
			if (!(associationRule.confidence == confidence))
				return false;
			return true;
		}
		return false;
	}
}
