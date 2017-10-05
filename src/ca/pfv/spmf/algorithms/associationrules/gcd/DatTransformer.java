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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class DatTransformer {

	private Integer[] transRules;
	private List<Integer[]> inputFile = new ArrayList<Integer[]>();
	private Map<Integer, List<Transaction>> transactionSets = new TreeMap<Integer, List<Transaction>>();
	private double freqThreshold;
	private Integer freqThresholdPrime;
	private int rows = 0;
	
	public void cleanUP(){
		transRules= null;
		inputFile.clear();
		for(List<Transaction> set:transactionSets.values())
			set.clear();
		transactionSets.clear();
	}

	public DatTransformer(List<String> inputLines, double freqThreshold) throws IOException, ClassNotFoundException {
		this.freqThreshold = freqThreshold;
		read(inputLines);
		transform();
	}

	public int getRowsCount() {
		return rows;
	}

	public double getFreqThreshold() {
		return freqThreshold;
	}

	/**
	 * This function is used to revert the transformation from primes to the
	 * original items
	 */
	public Integer[] primes2originals(List<Integer> primeFactors) {
		Integer[] translated = new Integer[primeFactors.size()];
		List<Integer> transRulesList = Arrays.asList(transRules);
		for (int i = 0; i < primeFactors.size(); ++i)
			translated[i] = transRulesList.indexOf(primeFactors.get(i));
		return translated;
	}

	private void transform() throws IOException, ClassNotFoundException {
		for (Integer[] primeNumbers : inputFile) {
			for (int i = 0; i < primeNumbers.length; ++i)
				primeNumbers[i] = transRules[primeNumbers[i]];

			Arrays.sort(primeNumbers);
			Integer primeSet_key = primeNumbers[0];

			if (primeSet_key <= freqThresholdPrime) {
				List<Transaction> list = transactionSets.get(primeSet_key);
				if (list == null) {
					list = new ArrayList<Transaction>();
					transactionSets.put(primeSet_key, list);
				}
				list.add(new Transaction(primeNumbers));
			}
		}
		removeRepeatedTransactions();
		System.gc();
	}

	/**
	 * This function removes repeated entries and assigns their count to the
	 * kept entry
	 */
	private void removeRepeatedTransactions() {
		Map<Integer, List<Transaction>> nonObsoleteTransactionSets = new HashMap<Integer, List<Transaction>>();
		for (Entry<Integer, List<Transaction>> entry : transactionSets.entrySet()) {
			Integer prime = entry.getKey();
			List<Transaction> set = entry.getValue();
			Collections.sort(set);
			int setSize = set.size();
			for (int i = setSize - 1; i > 0; --i) {
				// (i > 0) because i = 0 will never be obsolete
				Transaction transaction = set.get(i);
				Transaction lastTransaction = set.get(i - 1);
				if (transaction.equals(lastTransaction)) {
					int newFrequency = transaction.getFrequency() + 1;
					lastTransaction.setFrequency(newFrequency);
					transaction.makeObsolete();
				}
			}
			List<Transaction> nonObsoleteTransactionSet = new ArrayList<Transaction>();
			for (int i = 0; i < setSize; ++i) {
				Transaction transaction = set.get(i);
				if (!transaction.isObsolete())
					nonObsoleteTransactionSet.add(transaction);
			}
			if (!nonObsoleteTransactionSet.isEmpty())
				nonObsoleteTransactionSets.put(prime, nonObsoleteTransactionSet);
		}
		transactionSets = nonObsoleteTransactionSets;
	}

	/**
	 * Performs the first pass on the original file to get the frequency of
	 * occurrence of each item
	 */
	private void read(List<String> inputLines) throws IOException {
		Map<Integer, Integer> freqs = new HashMap<Integer, Integer>();
		int cols = 0;
		for (String line : inputLines) {
			++rows;
			String[] tokens = line.split("\\s+");
			Integer[] numbers = new Integer[tokens.length];
			for (int i = 0; i < tokens.length; ++i) {
				String token = tokens[i];
				int number = Integer.parseInt(token);
				numbers[i] = number;
				Integer freq = freqs.get(number);
				if (freq != null) {
					int newValue = freq + 1;
					freqs.put(number, newValue);
				} else
					freqs.put(number, 1);
				if (number > cols)
					cols = number;
			}
			inputFile.add(numbers);
		}

		freqThreshold *= rows;
		System.out.println("Minimum item frequency is: " + (int) freqThreshold);
		Set<Entry<Integer, Integer>> entries = freqs.entrySet();

		List<Entry<Integer, Integer>> sortedEntries = new ArrayList<Entry<Integer, Integer>>(entries);

		Collections.sort(sortedEntries, new Comparator<Entry<Integer, Integer>>() {
			@Override
			public int compare(Entry<Integer, Integer> o1, Entry<Integer, Integer> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		});
		transRules = new Integer[cols + 1];
		Primes primes = Primes.getInstance();
		for (Entry<Integer, Integer> entry : sortedEntries) {
			transRules[entry.getKey()] = primes.getNextPrime();
			if (freqThresholdPrime == null && entry.getValue() < freqThreshold)
				freqThresholdPrime = transRules[entry.getKey()];
		}
		if (freqThresholdPrime == null)
			freqThresholdPrime = Integer.MAX_VALUE;
		Transaction.setMaxPrimeThreshold(freqThresholdPrime);
	}

	public Map<Integer, List<Transaction>> getTransactionSets() {
		return transactionSets;
	}
}
