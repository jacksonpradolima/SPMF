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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This is an adapted version of BigInteger that speeds up the execution time
 * for this application
 */
public class MyBigInteger implements Comparable<MyBigInteger> {
	private static final int INT_MAX = Integer.MAX_VALUE;
	private BigInteger bigInteger;
	private boolean previouslyOverflowed;
	private List<Integer> factors;
	private static int combinationsElementsLimit;
	private static Set<GCDInfo> results = new HashSet<GCDInfo>();

	public static void setResultsSet(Set<GCDInfo> results) {
		MyBigInteger.results = results;
	}

	public static void setCombinationsElementsLimit(int combinationsElementsLimit) {
		MyBigInteger.combinationsElementsLimit = combinationsElementsLimit;
	}

	public static MyBigInteger constructMyBigInteger_One() {
		return new MyBigInteger(1);
	}

	private MyBigInteger(Integer integer) {
		this(BigInteger.valueOf(integer), new ArrayList<Integer>(), false);
	}

	private MyBigInteger(BigInteger bigInteger, List<Integer> factors, boolean overflowed) {
		this.bigInteger = bigInteger;
		this.previouslyOverflowed = overflowed;
		this.factors = factors;
	}

	/**
	 * Multiplication of integers is fast and utilized here till it overflows
	 * The moment it overflows, all further multiplication operations are
	 * carried out using the BigInteger functions The factors list is always
	 * updated in order to speed up the rest of the operations
	 */
	public void multiply(int input) {
		factors.add(input);
		Collections.sort(factors);
		if (!previouslyOverflowed) {
			long longMultiplication = bigInteger.longValue() * input;
			if (longMultiplication > 0) {
				bigInteger = BigInteger.valueOf(longMultiplication);
				return;
			}
		}
		bigInteger = bigInteger.multiply(BigInteger.valueOf(input));
		previouslyOverflowed = true;
	}

	/**
	 * This check utilizes the factors list provided instead of performing a
	 * division
	 */
	public boolean isDivisibleBy(MyBigInteger other) {
		if (factors.size() < other.factors.size())
			return false;

		for (Integer factorInOther : other.factors)
			if (factors.indexOf(factorInOther) == -1)
				return false;
		
		return true;
	}

	public MyBigInteger divide(MyBigInteger other) {
		if (factors.size() < other.factors.size())
			return null;

		MyBigInteger result = new MyBigInteger(1);

		List<Integer> factorsCopy = new ArrayList<Integer>(factors);
		for (Integer factorInOther : other.factors) {
			int index = factorsCopy.indexOf(factorInOther);
			if (index == -1)
				return null;
			else
				factorsCopy.set(index, null);
		}

		for (Integer factor : factorsCopy)
			if (factor != null)
				result.multiply(factor);

		return result;
	}

	private static void harvestGCDs(Set<MyBigInteger> GCDs) {
		for (MyBigInteger gcd : GCDs)
			harvestGCD(gcd);
	}

	public static void harvestGCD(MyBigInteger gcd) {
		GCDInfo gcdInfo = new GCDInfo(gcd);
		if (!results.contains(gcdInfo))
			harvestGCD(gcdInfo);
	}

	private synchronized static void harvestGCD(GCDInfo gcd) {
		results.add(gcd);
	}

	/**
	 * This is the core operator for this implementation of the algorithm It
	 * utilizes the merge routine without using heavy computational division
	 * operations.
	 * 
	 * This function is an approximation that extracts the most frequent
	 * combinations
	 * 
	 * Note: The exact implementation would be to return all combinations of all
	 * items, but this would grow exponentially in time and memory
	 */
	public void calcAndHarvestGCDs(MyBigInteger other) {
		Set<MyBigInteger> combinationsOfK = new HashSet<MyBigInteger>();
		int i = 0, j = 0;
		Integer intFrom1stList = factors.get(i);
		Integer intFrom2ndList = other.factors.get(j);
		int firstListSize = factors.size();
		int secondListSize = other.factors.size();
		List<Integer> commonFactors = new ArrayList<Integer>();
		while (true) {
			if (intFrom1stList > intFrom2ndList) {
				if (++j < secondListSize)
					intFrom2ndList = other.factors.get(j);
				else
					break;
			} else if (intFrom1stList < intFrom2ndList) {
				if (++i < firstListSize)
					intFrom1stList = factors.get(i);
				else
					break;
			} else {
				commonFactors.add(intFrom1stList);
				if (++i < firstListSize && ++j < secondListSize) {
					intFrom2ndList = other.factors.get(j);
					intFrom1stList = factors.get(i);
				} else
					break;
			}
		}

		MyBigInteger gcd1 = new MyBigInteger(1);
		MyBigInteger gcd2 = new MyBigInteger(1);
		int commonFactorsSize = commonFactors.size();
		int lastElementLocation = commonFactorsSize - 1;
		for (i = 0; i < commonFactorsSize; ++i) {
			Integer firstFactor = commonFactors.get(i);
			gcd1.multiply(firstFactor);
			harvestGCD(gcd1.copy());

			if (i != lastElementLocation) {
				gcd2.multiply(commonFactors.get(lastElementLocation - i));
				harvestGCD(gcd2.copy());
			}

			if (i != 0 && i != lastElementLocation) {
				ArrayList<Integer> combinationFactors = new ArrayList<Integer>();
				combinationFactors.add(firstFactor);
				harvestGCD(new MyBigInteger(BigInteger.valueOf(firstFactor), combinationFactors, false));
			}

			if (combinationsElementsLimit > 1) {
				// Generate combinations of 2
				for (j = i + 1; j < commonFactorsSize; ++j) {
					MyBigInteger combinationOfTwo = new MyBigInteger(1);
					combinationOfTwo.multiply(firstFactor);
					combinationOfTwo.multiply(commonFactors.get(j));
					combinationsOfK.add(combinationOfTwo);
				}
			}
		}

		// Generate combinations of K, starting from 3 and above
		int combinationSlots = 3;
		while (combinationsElementsLimit > 2 && combinationSlots <= MyBigInteger.combinationsElementsLimit) {
			harvestGCDs(combinationsOfK);
			List<MyBigInteger> previousCombinationsOfK = new ArrayList<MyBigInteger>(combinationsOfK);
			combinationsOfK.clear();

			for (MyBigInteger previousCombination : previousCombinationsOfK) {
				Integer biggestFactor = previousCombination.getFactors()
						.get(previousCombination.getFactors().size() - 1);
				for (Integer commonFactor : commonFactors) {
					if (commonFactor <= biggestFactor)
						continue;
					MyBigInteger newCombination = previousCombination.copy();
					newCombination.multiply(commonFactor);
					combinationsOfK.add(newCombination);
				}
			}

			++combinationSlots;
		}

		harvestGCDs(combinationsOfK);
	}

	private MyBigInteger copy() {
		return new MyBigInteger(bigInteger.add(BigInteger.ZERO), new ArrayList<Integer>(factors), previouslyOverflowed);
	}

	public List<Integer> getFactors() {
		return factors;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MyBigInteger)
			return bigInteger.equals(((MyBigInteger) obj).bigInteger);
		else if (obj instanceof BigInteger)
			return bigInteger.equals(obj);
		else if (obj instanceof Long)
			return ((Long) obj).equals(bigInteger.longValue());
		else if (obj instanceof Integer)
			return ((Integer) obj).equals(bigInteger.intValue());
		else if (obj instanceof GCDInfo)
			return this.equals(((GCDInfo) obj).getGCD());
		else
			return false;
	}

	@Override
	public int hashCode() {
		// This is based on the probability of collisions between primes
		// multiplications is low
		return bigInteger.mod(BigInteger.valueOf(INT_MAX)).intValue();
	}

	@Override
	public String toString() {
		return factors.toString();
	}

	@Override
	public int compareTo(MyBigInteger other) {
		return this.bigInteger.compareTo(other.bigInteger);
	}
}
