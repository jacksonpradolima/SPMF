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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class GCDAssociator {
	private Integer primeNumber;
	private static Map<Integer, List<Transaction>> transactionSets;
	private static Set<GCDInfo> results = new HashSet<GCDInfo>();
	private static List<AssociationRule> associationRules = new LinkedList<AssociationRule>();
	private static List<GCDInfo> sortedResultsByFreqs;
	private static double inputConfThreshold;

	public GCDAssociator(Integer primeNumber, Map<Integer, List<Transaction>> transactionSets) {
		this.primeNumber = primeNumber;
		GCDAssociator.transactionSets = transactionSets;
		MyBigInteger.setResultsSet(results);
	}

	public static Set<GCDInfo> getResults() {
		return results;
	}

	public static List<AssociationRule> getAssociationRules() {
		return associationRules;
	}

	public LinkedList<SupportCalcThread> getSupportCalculationThreads() {
		LinkedList<SupportCalcThread> threads = new LinkedList<SupportCalcThread>();

		for (GCDInfo gcd : results)
			threads.add(new SupportCalcThread(gcd));

		return threads;
	}

	private static List<GCDInfo> getGCDsSortedByFreq_DESC() {
		List<GCDInfo> sortedEntries = new ArrayList<GCDInfo>(results);
		Collections.sort(sortedEntries, new Comparator<GCDInfo>() {
			@Override
			public int compare(GCDInfo o1, GCDInfo o2) {
				return o2.getFrequency().compareTo(o1.getFrequency());
			}
		});
		return sortedEntries;
	}

	public LinkedList<GCDExtractorThread> generateGCDTables() {
		final List<Transaction> firstTransactionSet = transactionSets.get(primeNumber);
		LinkedList<GCDExtractorThread> threads = new LinkedList<GCDExtractorThread>();
		for (int i = 0; i < firstTransactionSet.size(); ++i)
			threads.add(new GCDExtractorThread(i, firstTransactionSet));
		return threads;
	}

	private synchronized static void harvestAssociationRule(AssociationRule rule) {
		associationRules.add(rule);
	}

	public List<ConfidenceCalcThread> getConfidenceCalculationThreads(double freqThreshold, double inputConfThreshold) {
		GCDAssociator.inputConfThreshold = inputConfThreshold;
		List<ConfidenceCalcThread> threads = new LinkedList<ConfidenceCalcThread>();
		for (int i = 0; i < sortedResultsByFreqs.size(); ++i)
			if (sortedResultsByFreqs.get(i).getFrequency() >= freqThreshold)
				threads.add(new ConfidenceCalcThread(i));
		return threads;
	}

	public final class ConfidenceCalcThread extends Thread {
		private int gcdIndex;

		private ConfidenceCalcThread(int gcdIndex) {
			this.gcdIndex = gcdIndex;
		}

		@Override
		public void run() {
			GCDInfo bigGCDInfo = sortedResultsByFreqs.get(gcdIndex);
			MyBigInteger bigGCD = bigGCDInfo.getGCD();
			double support = bigGCDInfo.getFrequency();
			for (int j = sortedResultsByFreqs.size() - 1; j >= 0; --j) {
				if (j == gcdIndex)
					continue;
				GCDInfo smallGCDInfo = sortedResultsByFreqs.get(j);
				MyBigInteger smallGCD = smallGCDInfo.getGCD();
				double confidence = support / smallGCDInfo.getFrequency();
				if (confidence < inputConfThreshold)
					break;

				MyBigInteger subGCD = bigGCD.divide(smallGCD);
				if (subGCD != null && results.contains(subGCD))
					harvestAssociationRule(new AssociationRule(smallGCD, subGCD, support, confidence));

			}
		}
	}

	public final class SupportCalcThread extends Thread {
		private final GCDInfo gcdInfo;

		private SupportCalcThread(GCDInfo gcdInfo) {
			this.gcdInfo = gcdInfo;
		}

		@Override
		public void run() {
			MyBigInteger gcd = gcdInfo.getGCD();
			List<Integer> gcdFactors = gcd.getFactors();
			Integer biggestGCDFactor = gcdFactors.get(gcdFactors.size() - 1);
			for (Entry<Integer, List<Transaction>> entry : transactionSets.entrySet()) {
				if (biggestGCDFactor < entry.getKey())
					continue;
				List<Transaction> setTransactions = entry.getValue();
				for (Transaction transaction : setTransactions)
					if (transaction.getMultiplication().isDivisibleBy(gcd))
						gcdInfo.incrementFrequency(transaction.getFrequency());
			}
		}
	}

	public final class GCDExtractorThread extends Thread {
		private final int i;
		private final List<Transaction> firstTransactionSet;

		private GCDExtractorThread(int i, List<Transaction> firstTransactionSet) {
			this.i = i;
			this.firstTransactionSet = firstTransactionSet;
		}

		@Override
		public void run() {
			Transaction firstTransaction = firstTransactionSet.get(i);

			// same transaction, but repeated
			if (firstTransaction.getFrequency() > 1)
				MyBigInteger.harvestGCD(firstTransaction.getMultiplication());
			// within the same set
			for (int j = i + 1; j < firstTransactionSet.size(); ++j) {
				Transaction secondTransaction = firstTransactionSet.get(j);
				secondTransaction.calcAndHarvestGCDs(firstTransaction);
			}
			// across multiple sets
			List<Integer> firstTransactionItems = firstTransaction.getItems();
			Integer lastItemInFirstTransaction = firstTransactionItems.get(firstTransactionItems.size() - 1);
			for (Entry<Integer, List<Transaction>> entry : transactionSets.entrySet()) {
				Integer key = entry.getKey();
				if (key <= i)
					continue;
				if (key > lastItemInFirstTransaction)
					break; // there's no way for this transaction to intersect
							// with any more transaction sets
				List<Transaction> secondTransactionSet = entry.getValue();
				calcGCDBetweenTransactionAndSet(firstTransaction, secondTransactionSet);
			}
		}

		private void calcGCDBetweenTransactionAndSet(Transaction firstTransaction,
				List<Transaction> secondTransactionSet) {
			for (Transaction secondTransaction : secondTransactionSet)
				secondTransaction.calcAndHarvestGCDs(firstTransaction);
		}
	}

	public void cleanUp() {
		transactionSets.clear();
		sortedResultsByFreqs = getGCDsSortedByFreq_DESC();

		System.gc();
	}
}
