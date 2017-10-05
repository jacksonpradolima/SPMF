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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Transaction implements Comparable<Transaction> {
	private ArrayList<Integer> items = new ArrayList<Integer>();
	private MyBigInteger multiplication = MyBigInteger.constructMyBigInteger_One();
	private int frequency = 1;
	private static int maxPrimeThreshold;

	public Transaction(Integer[] items) {
		Arrays.sort(items);
		for (int i = 0; i < items.length; ++i) {
			Integer item = items[i];
			if (i > 0 && items[i] == items[i - 1])
				continue; // remove repeated items in a transaction
			if (item > maxPrimeThreshold)
				break;
			multiplication.multiply(item);
			this.items.add(item);
		}
	}

	public static void setMaxPrimeThreshold(int maxPrimeThreshold) {
		Transaction.maxPrimeThreshold = maxPrimeThreshold;
	}

	public int getFrequency() {
		return frequency;
	}

	public void makeObsolete() {
		frequency = 0;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public boolean isObsolete() {
		return frequency == 0;
	}

	public void calcAndHarvestGCDs(Transaction other) {
		multiplication.calcAndHarvestGCDs(other.multiplication);
	}

	@Override
	public int compareTo(Transaction o) {
		return getMultiplication().compareTo(o.getMultiplication());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Transaction))
			return false;
		return ((Transaction) obj).getMultiplication().equals(getMultiplication());
	}

	@Override
	public String toString() {
		return items + "{" + frequency + "}";
	}

	public List<Integer> getItems() {
		return items;
	}

	public MyBigInteger getMultiplication() {
		return multiplication;
	}
}
