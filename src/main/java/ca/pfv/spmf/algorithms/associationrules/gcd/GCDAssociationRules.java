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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import ca.pfv.spmf.tools.MemoryLogger;

public class GCDAssociationRules {
	private static final DateFormat FORMATTER = new SimpleDateFormat("[hh:mm:ss a]");
	private DatTransformer datTransformer;
	private double inputConfThreshold;
	private double inputFreqThreshold;
	private String outputFilePath;
	private String inputFilePath;
	private int itemsetCount;
	private long startTimestamp;
	private long endTimestamp;

	public GCDAssociationRules(String inputFilePath, String outputFilePath, double inputFreqThreshold,
			double inputConfThreshold, int combinationsElementsLimit) {
		this.inputFilePath = inputFilePath;
		this.outputFilePath = outputFilePath;
		this.inputFreqThreshold = inputFreqThreshold;
		this.inputConfThreshold = inputConfThreshold;
		MyBigInteger.setCombinationsElementsLimit(combinationsElementsLimit);
	}

	public void runAlgorithm() throws Exception {
		MemoryLogger.getInstance().reset();
		
		startTimestamp = Calendar.getInstance().getTimeInMillis();
		
		List<String> inputLines = extractInputFileLines();

		// Read the file and assign primes based on the frequency of items in
		// the input file
		// Constructs the transactions set, removes duplicate transactions and
		// calculates the multiplications
		datTransformer = new DatTransformer(inputLines, inputFreqThreshold);
		Map<Integer, List<Transaction>> transactionSets = datTransformer.getTransactionSets();
		LinkedList<Thread> allThreads = new LinkedList<Thread>();

		// Extract GCDs between entries' multiplications
		System.out.println("Extracting GCDs");
		for (Integer primeNumber : new TreeSet<Integer>(transactionSets.keySet())) {
			allThreads.addAll(new GCDAssociator(primeNumber, transactionSets).generateGCDTables());
		}
		executeThreads(allThreads);

		long gcdsExtractionTime = Calendar.getInstance().getTimeInMillis() - startTimestamp;
		System.out.println("GCDs extraction time (ms): " + gcdsExtractionTime);

		// Extracts the support values of GCDs from the original transactions
		System.out.println("Extracting support values");
		allThreads.addAll(new GCDAssociator(0, transactionSets).getSupportCalculationThreads());
		executeThreads(allThreads);

		long supportCalculationTime = Calendar.getInstance().getTimeInMillis() - startTimestamp - gcdsExtractionTime;
		System.out.println("Support values time (ms): " + supportCalculationTime);

		// Cleaning up some data and signaling the GC
		new GCDAssociator(0, transactionSets).cleanUp();

		// Calculating confidence values and constructing the association rules
		// objects
		System.out.println("Extracting confidence values");
		allThreads.addAll(new GCDAssociator(0, null).getConfidenceCalculationThreads(datTransformer.getFreqThreshold(),
				inputConfThreshold));
		executeThreads(allThreads);

		System.out.println("Confidence values time (ms): "
				+ (Calendar.getInstance().getTimeInMillis() - startTimestamp - gcdsExtractionTime - supportCalculationTime));

		// Sorting association rules according to their support values
		List<AssociationRule> associationRules = GCDAssociator.getAssociationRules();
		AssociationRule.setTotalTransactionsCount(datTransformer.getRowsCount());
		Collections.sort(associationRules, new Comparator<AssociationRule>() {
			@Override
			public int compare(AssociationRule o1, AssociationRule o2) {
				return o2.getSupport().compareTo(o1.getSupport());
			}
		});

		List<String> lines = constructCSVLines(associationRules);

		writeCSV(lines);

		GCDAssociator.getResults().clear();
		associationRules.clear();
		lines.clear();		
		datTransformer.cleanUP();
		
		endTimestamp = Calendar.getInstance().getTimeInMillis();
		System.gc();
		MemoryLogger.getInstance().checkMemory();
	}

	private List<String> constructCSVLines(List<AssociationRule> associationRules) {
		List<String> lines = new LinkedList<String>();
		for (AssociationRule rule : associationRules) {
			StringBuilder line = new StringBuilder();
			line.append(getOriginalFactorsForGCD(rule.getAntecedent())).append(" ==> ");
			line.append(getOriginalFactorsForGCD(rule.getConsequent())).append(" #SUP: ");
			line.append(rule.getSupport()).append(" #CONF: ");
			line.append(rule.getConfidence());
			lines.add(line.toString());
		}
		itemsetCount = lines.size();
		return lines;
	}

	private List<String> extractInputFileLines() throws FileNotFoundException, IOException {
		List<String> inputLines = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new FileReader(new File(inputFilePath)));
		String line;
		while ((line = reader.readLine()) != null)
			inputLines.add(line);
		reader.close();
		return inputLines;
	}

	private String getOriginalFactorsForGCD(MyBigInteger gcd) {
		Integer[] originalFactors = datTransformer.primes2originals(gcd.getFactors());
		Arrays.sort(originalFactors);
		return Arrays.toString(originalFactors).replaceAll("[\\[\\]]", "").replaceAll(",", "");
	}

	private void writeCSV(List<String> lines) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outputFilePath)));
		for (String line : lines) {
			writer.append(line).append("\n");
		}
		writer.close();
	}

	/**
	 * This algorithm can be parallelized in many of its phases. So this
	 * function is made to utilize the processing power of multiple cores by
	 * maintaining a constant number of threads that equals the number of cores
	 */
	private void executeThreads(LinkedList<Thread> threads) throws InterruptedException {
		int threadsCount = threads.size();
		if (threadsCount == 0)
			return;
		int max_running_threads = (Runtime.getRuntime().availableProcessors());
		Thread[] activeThreads = new Thread[max_running_threads];
		for (int i = 0; i < activeThreads.length; ++i) {
			activeThreads[i] = threads.poll();
			if (activeThreads[i] == null)
				break;
			activeThreads[i].start();
		}
		int progress = calculateProgress(threads, threadsCount, max_running_threads);

		printProgress(progress);
		Thread nextThread = threads.poll();
		long time = Calendar.getInstance().getTimeInMillis();
		while (nextThread != null) {
			int i;
			for (i = 0; activeThreads[i].isAlive(); i = (i + 1) % activeThreads.length)
				;
			activeThreads[i] = nextThread;
			activeThreads[i].start();
			progress = calculateProgress(threads, threadsCount, max_running_threads);

			nextThread = threads.poll();
			if (Calendar.getInstance().getTimeInMillis() - time > 5000) {
				printProgress(progress);
				time = Calendar.getInstance().getTimeInMillis();
				// System.gc();
			}
		}
		for (int i = 0; i < activeThreads.length; i = (i + 1) % activeThreads.length) {
			if (activeThreads[i] == null) {
				continue;
			}
			if (activeThreads[i].isAlive()) {
				continue;
			}
			int doneCounter = 0;
			for (int j = 0; j < activeThreads.length; ++j) {
				if (activeThreads[j] != null && !activeThreads[j].isAlive()) {
					activeThreads[j] = null;
				}
				if (activeThreads[j] == null) {
					++doneCounter;
				}
			}
			progress = Math.round(100 * ((threadsCount - max_running_threads + doneCounter) / (float) threadsCount));

			printProgress(progress);
			if (doneCounter == max_running_threads) {
				break;
			}
		}
		printProgress(100);
		MemoryLogger.getInstance().checkMemory();
	}

	private int calculateProgress(LinkedList<Thread> threads, int threadsCount, int max_running_threads) {
		return Math.round(100 * ((threadsCount - threads.size() - max_running_threads) / (float) threadsCount));
	}

	private void printProgress(int progress) {
		String time = FORMATTER.format(Calendar.getInstance().getTime());
		System.out.println(time + " " + progress + "%");
	}
	
	/**
	 * Print statistics about the algorithm execution to System.out.
	 */
	public void printStats() {
		System.out.println("=========  GCD Association Rules - STATS =========");
		System.out.println(" Pattern count : " + itemsetCount);
		System.out.println(" Maximum memory usage : " + MemoryLogger.getInstance().getMaxMemory() + " mb");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println("===================================================");
	}

	/**
	 * Get the number of pattern found
	 * @return the number of patterns
	 */
	public int getPatternCount() {
		return itemsetCount;
	}
}
