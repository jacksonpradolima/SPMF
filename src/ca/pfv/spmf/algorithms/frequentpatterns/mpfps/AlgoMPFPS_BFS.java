package ca.pfv.spmf.algorithms.frequentpatterns.mpfps;

/* This file is copyright (c) 2019 Zhitian Li, Philippe Fournier-Viger
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
 * 
 */
/**
 * This is an implementation of the "MPFPS_BFS" algorithm for mining periodic patterns common
 * to multiple sequences using a breadth-first search. It is described in this paper:
 * <br/><br/>
 * 
 * Li, Z., Fournier-Viger, P., et al. (2019) Efficient Algorithms to Identify Periodic Patterns in Multiple Sequences.
 * (to appear).
 * 
 * 
 *  @author Zhitian Li, Philippe Fournier-Viger
 **/
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
//import java.text.DecimalFormat;
import java.util.Map;

import ca.pfv.spmf.tools.MemoryLogger;

/**
 * @see SeqTidList
 * @author Zhitian Li
 */
public class AlgoMPFPS_BFS {

	/**
	 * This is the standard deviation threshold of periodicities for each
	 * sequence. //This parameter is to ensure that mined patterns outcome as
	 * periodically.
	 */
	double maxStandardDeviation = 10;

	/**
	 * minRa is the threshold of indicating how many sequences in a database a
	 * frequent pattern // appears in.
	 */
	double minRA = 0;

	/**
	 * This parameter indicates the maximum periodicity interval of a frequent
	 * pattern in a //sequence, to avoid the situation that a pattern appears
	 * multiple times at a single time while //the other time don't have the
	 * pattern.
	 */
	int maxPeriodicity = 10;

	/**
	 * In each sequence, if an itemset is frequent, it's support should be
	 * larger than a threshold.
	 */
	int minimumSupport = 2;

	/**
	 * Calculate the number of sequences in each database, so that we can
	 * calculate the ratio of a frequent //pattern.
	 */
	int numOfSequences = 0;

	/**
	 * Store the lengths of sequences, so that we can compute the last period of
	 * a frequent pattern in these //sequences.
	 */
	List<Integer> sequenceLengths = new ArrayList<Integer>();

	/**
	 * This map is used to store the contrast periodic frequent patterns and the
	 * corresponding BC parameter. Show this map to users.
	 */
	Map<int[], Double> result = new HashMap<int[], Double>();

	/** Time spent during the last execution of this algorithm */
	long totalTime;

	/** Number of patterns */
	int patternCount;

	/** Constructor */
	public AlgoMPFPS_BFS() {

	}

	/**
	 * Run the algorithm
	 * 
	 * @param maxStandardDeviation
	 *            maximum standard deviation
	 * @param minRA
	 *            minimum RA
	 * @param maxPeriodicity
	 *            maximum periodicity
	 * @param minimumSupport
	 *            minimum support
	 * @param outputFile
	 *            output file path
	 * @param inputFile
	 *            input file path
	 * @throws IOException
	 *             if error reading/writting to file
	 */
	public void runAlgorithm(double maxStandardDeviation, double minRA,
			int maxPeriodicity, int minimumSupport, String inputFile,
			String outputFile) throws Exception {
		MemoryLogger.getInstance().reset();
		long startTime = System.currentTimeMillis();

		// save parameters
		this.maxStandardDeviation = maxStandardDeviation;
		this.maxPeriodicity = maxPeriodicity;
		this.minRA = minRA;
		this.minimumSupport = minimumSupport;

		// initialize data structures
		result = new HashMap<int[], Double>();
		sequenceLengths = new ArrayList<Integer>();
		numOfSequences = 0;

		// Run the algorithm
		List<SeqTidList> periodicFrequentPatterns = getFreqPeriodicPattern(inputFile);

		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
				outputFile)));

		// --------------------------- OUTPUT -------------------------------

		for (SeqTidList periodicPat : periodicFrequentPatterns) {
			out.println(periodicPat);
		}
		out.println();
		out.close();

		patternCount = periodicFrequentPatterns.size();

		MemoryLogger.getInstance().checkMemory();
		totalTime = System.currentTimeMillis() - startTime;
	}

	/**
	 * Print statistics about the last algorithm execution.
	 */
	public void printStats() {
		System.out
				.println("=============  MPFPS_BFS v.2.40 - STATS =============");
		System.out.println("Pattern count: " + patternCount);
		System.out.println("Memory : "
				+ MemoryLogger.getInstance().getMaxMemory() + " mb");
		System.out.println("Total time : " + totalTime + " ms");
		System.out
				.println("===================================================");
	}

	/**
	 * Reads the file and returns a list of SeqTidList objects which itemSet[]
	 * is single, and are frequent and periodic in the database. Moreover, the
	 * frequent patterns must be satisfied with the MIN_RA threshold.
	 * 
	 * @param fileName
	 *            the name of the file to be read.
	 * @return singleItemTidList: an arrayList of SeqTidList objects
	 * @throws IOException
	 *             IO error.
	 */
	public List<List<SeqTidList>> getSingleItemTidList(String fileName)
			throws IOException {

		File file = new File(fileName);
		BufferedReader reader = null;
		String tempReader = null;
		String[] tempSplitted;
		List<SeqTidList> singleItemTidList = new ArrayList<SeqTidList>();
		int currentLine = 0;
		reader = new BufferedReader(new FileReader(file));

		while ((tempReader = reader.readLine()) != null) {
			// if the line is  a comment, is  empty or is a
			// kind of metadata
			if (tempReader.isEmpty() == true ||
					tempReader.charAt(0) == '#' || tempReader.charAt(0) == '%'
							|| tempReader.charAt(0) == '@') {
				continue;
			}
			
			tempSplitted = tempReader.split(" ");
			int currentTran = 0;
			// The number of the transaction currently processing.

			// While reading a new line, reset the flag of each TidList of
			// single item to true.
			for (SeqTidList tempSeqTid : singleItemTidList)
				tempSeqTid.newLine = true;

			// When the read number is a positive integer, judge if this is the
			// first appearance of a new line. If
			// true, then create a new list of Integer to store the tidSet. If
			// false, add this tid to current list.
			for (int i = 0; i < tempSplitted.length - 1; i++) {
				boolean found = false;
				int temp = Integer.parseInt(tempSplitted[i]);
				// temp is the item currently processing.
				int tempOfNext = Integer.parseInt(tempSplitted[i + 1]);

				if (temp != -1 && temp != -2) {
					for (SeqTidList current : singleItemTidList) {
						int lengthOfSequences = current.sequenceNum.size();

						if (temp == current.itemSet[0]) {
							if (current.sequenceNum.get(lengthOfSequences - 1) != currentLine) {
								List<Integer> newTidSet = new ArrayList<Integer>();
								newTidSet.add(currentTran);
								current.seqTidSet.add(newTidSet);
								current.sequenceNum.add(currentLine);
								current.newLine = false;
							}

							else {
								int length = current.seqTidSet.size();
								current.seqTidSet.get(length - 1).add(
										currentTran);
							}
							found = true;
							break;
						}
					}

					if (!found) {
						SeqTidList current = new SeqTidList(1);
						current.itemSet[0] = temp;
						current.seqTidSet.add(new ArrayList<Integer>());
						current.seqTidSet.get(0).add(currentTran);
						current.sequenceNum.add(currentLine);
						singleItemTidList.add(current);
					}
				} else if (temp == -1 && tempOfNext != -2) {
					currentTran++;
				} else if (temp == -1 && tempOfNext == -2) {
					sequenceLengths.add(currentTran);
					break;
				}
			}
			currentLine++;
		}
		numOfSequences = currentLine;
		reader.close();
		List<SeqTidList> freMaxPrSingleTidList = new ArrayList<SeqTidList>();
		List<SeqTidList> periodicSingleTidList = new ArrayList<SeqTidList>();
		// Check if the SeqTidList objects are periodic or not.
		for (SeqTidList singleItemSeqTid : singleItemTidList) {
			int num = 0; // The number of the tid list in sequenceNum.
			SeqTidList freSingleItemSeqTid = new SeqTidList();
			freSingleItemSeqTid.itemSet = singleItemSeqTid.itemSet;
			// Store the tid lists which satisfy the thresholds MIN_SUP and
			// MAX_PR.
			SeqTidList periodicSingleItemSeqTid = new SeqTidList();
			periodicSingleItemSeqTid.itemSet = singleItemSeqTid.itemSet;
			// Store the tid lists which satisfy the thresholds MIN_SUP, MAX_PR
			// and MAX_STAN_DEV .
			for (List<Integer> currentTidList : singleItemSeqTid.seqTidSet) {
				boolean[] periodic = checkPeriodicity(currentTidList,
						sequenceLengths.get(singleItemSeqTid.sequenceNum.get(num)));
				if (periodic[0]) {
					freSingleItemSeqTid.seqTidSet.add(currentTidList);
					freSingleItemSeqTid.sequenceNum
							.add(singleItemSeqTid.sequenceNum.get(num));
				}
				if (periodic[1]) {
					periodicSingleItemSeqTid.seqTidSet.add(currentTidList);
					periodicSingleItemSeqTid.sequenceNum
							.add(singleItemSeqTid.sequenceNum.get(num));
				}
				num++;
			}

			double freSingleBoundRa = (double) freSingleItemSeqTid.sequenceNum
					.size() / numOfSequences;
			double periodicSingleRa = (double) periodicSingleItemSeqTid.sequenceNum
					.size() / numOfSequences;

			if (!freSingleItemSeqTid.seqTidSet.isEmpty()
					&& freSingleBoundRa >= minRA)
				freMaxPrSingleTidList.add(freSingleItemSeqTid);
			if (!periodicSingleItemSeqTid.seqTidSet.isEmpty()
					&& periodicSingleRa >= minRA){

				// start - added by Philippe
				periodicSingleItemSeqTid.ra = periodicSingleRa;
				// end - added by Philippe
				periodicSingleTidList.add(periodicSingleItemSeqTid);
			}
		}
		List<List<SeqTidList>> freMaxPrAndPeriodicSingle = new ArrayList<List<SeqTidList>>();
		freMaxPrAndPeriodicSingle.add(freMaxPrSingleTidList);
		freMaxPrAndPeriodicSingle.add(periodicSingleTidList);
		return freMaxPrAndPeriodicSingle;
	}

	private boolean[] checkPeriodicity(List<Integer> currentTidSet,
			int lengthOfSequence) {
		boolean periodic[] = { true, true };
		if (currentTidSet.size() == 0 || currentTidSet.size() < minimumSupport) {
			periodic[0] = false;
			periodic[1] = false;
			return periodic;
		}
		int length = currentTidSet.size();
		int periods[] = new int[length + 1];
		int temp = 0;
		double avgPr = 0;
		int sum = 0;
		double sumDevi = 0;
		double stanDevi = 0;
		Collections.sort(currentTidSet);

		int firstPeriod = currentTidSet.get(0);
		if (firstPeriod < 0 || firstPeriod > maxPeriodicity)
			periodic[0] = false;
		else
			periods[0] = firstPeriod;
		// The first period should be first transaction number minus 0.

		for (int i = 0; i < length - 1; i++) {
			temp = currentTidSet.get(i + 1) - currentTidSet.get(i);
			if (temp < 0 || temp > maxPeriodicity) {
				periodic[0] = false;
				break;
			} else {
				periods[i + 1] = temp;
				sum += temp;
			}
		}

		int lastPeriod = lengthOfSequence - currentTidSet.get(length - 1);
		if (lastPeriod < 0 || lastPeriod > maxPeriodicity)
			periodic[0] = false;
		else
			periods[length] = lastPeriod;
		// The last period should be the number of transactions of this sequence
		// minus the last position this
		// frequent pattern appears.

		if (periodic[0]) {
			avgPr = sum / length;
			for (int j = 0; j < periods.length; j++) {
				sumDevi += ((periods[j] - avgPr) * (periods[j] - avgPr));
			}
			stanDevi = Math.sqrt(sumDevi / length);
			if (periodic[0] && stanDevi > maxStandardDeviation)
				periodic[1] = false;
		}
		return periodic;
	}

	/**
	 * Get all the frequent patterns that are periodic in this database.
	 * 
	 * @param fileName
	 *            the file to be read.
	 * @return periodicFrequent: all the frequent patterns which are periodic in
	 *         this database.
	 * @throws IOException
	 *             if IO error
	 */
	public List<SeqTidList> getFreqPeriodicPattern(String fileName)
			throws IOException {
		List<List<SeqTidList>> frePeriodicSingleSeq = getSingleItemTidList(fileName);
		List<SeqTidList> freSingleItemTidlist = frePeriodicSingleSeq.get(0);
		List<SeqTidList> periodicSingleTidList = frePeriodicSingleSeq.get(1);
		List<SeqTidList> periodicPatterns = new ArrayList<SeqTidList>();
		for (SeqTidList periodicPattern : periodicSingleTidList) {
			periodicPatterns.add(periodicPattern);
		}
		List<List<SeqTidList>> freSeqTid = periodicFrequent(
				freSingleItemTidlist, minimumSupport);
		if (freSeqTid != null) {
			if (freSeqTid.size() == 2) {
				for (SeqTidList periodicPattern : freSeqTid.get(1)) {
					periodicPatterns.add(periodicPattern);
				}
			}
			while (freSeqTid.get(0).size() >= 2) {
				freSeqTid = periodicFrequent(freSeqTid.get(0), minimumSupport);
				for (SeqTidList periodicPattern : freSeqTid.get(1)) {
					periodicPatterns.add(periodicPattern);
				}
			}
		}
		return periodicPatterns;
	}

	/**
	 * calculate the intersect of tidLists of two different TidLists
	 * 
	 * @param list1
	 * @param list2
	 * @return
	 */
	public List<Integer> intersectTids(List<Integer> list1, List<Integer> list2) {
		List<Integer> common = new ArrayList<Integer>();
		for (int i = 0; i < list1.size(); i++)
			if (Collections.binarySearch(list2, list1.get(i)) >= 0)
				common.add(list1.get(i));
		return common;
	}

	/**
	 * get the union itemsets of two integer arrays.
	 * 
	 * @param list1
	 * @param list2
	 * @return unionItemSet: an integer array.
	 */
	public int[] unionItemsets(int list1[], int list2[]) {
		int len = list1.length;
		int unionItemSet[] = Arrays.copyOf(list1, len + 1);
		unionItemSet[len] = list2[len - 1];
		return unionItemSet;
	}

	/**
	 * Inspired by Apriori algorithm. At the same time, check if the patterns
	 * are periodic or not. If yes, output this periodic pattern.
	 * 
	 * @param tidlistOfTemp
	 * @param minSup
	 * @param result
	 *            : result is a Map object to store all frequent patterns and
	 *            their corresponding supports
	 * @return
	 */
	public List<List<SeqTidList>> periodicFrequent(
			List<SeqTidList> tidlistOfTemp, int minSup) {
		List<SeqTidList> tempFreMaxPrTidList = new ArrayList<SeqTidList>();
		List<SeqTidList> tempPeriodicTidList = new ArrayList<SeqTidList>();
		List<List<SeqTidList>> freMaxPrAndPeriodic = new ArrayList<List<SeqTidList>>();

		for (int i = 0; i < tidlistOfTemp.size(); i++) {

			SeqTidList currentTidListA = tidlistOfTemp.get(i);

			loopJ: for (int j = i + 1; j < tidlistOfTemp.size(); j++) {
				SeqTidList currentTidListB = tidlistOfTemp.get(j);
				if (haveSamePrefix(currentTidListA, currentTidListB)) {

					SeqTidList freMaxPrTidListAB = new SeqTidList();
					SeqTidList periodicTidListAB = new SeqTidList();
					freMaxPrTidListAB.itemSet = unionItemsets(
							currentTidListA.itemSet, currentTidListB.itemSet);
					Arrays.sort(freMaxPrTidListAB.itemSet);
					for (int item1 : freMaxPrTidListAB.itemSet) {
						int[] itemset = new int[freMaxPrTidListAB.itemSet.length - 1];
						int index = 0;
						boolean found = false;
						for (int item2 : freMaxPrTidListAB.itemSet) {
							if (item2 != item1) {
								itemset[index++] = item2;
							}
						}
						for (SeqTidList temp : tidlistOfTemp) {
							if (Arrays.equals(itemset, temp.itemSet)) {
								found = true;
								break;
							}
						}
						if (!found) {
							continue loopJ;
						}
					}
					List<Integer> interSequenceNum = intersectTids(
							currentTidListA.sequenceNum,
							currentTidListB.sequenceNum);

					for (int serial : interSequenceNum) {
						int serialNumA = Collections.binarySearch(
								currentTidListA.sequenceNum, serial);
						int serialNumB = Collections.binarySearch(
								currentTidListB.sequenceNum, serial);

						List<Integer> tidSetA = currentTidListA.seqTidSet
								.get(serialNumA);
						List<Integer> tidSetB = currentTidListB.seqTidSet
								.get(serialNumB);
						List<Integer> interTidsAB = intersectTids(tidSetA,
								tidSetB);
						boolean[] periodic = checkPeriodicity(interTidsAB,
								sequenceLengths.get(serial));

						if (interTidsAB.isEmpty())
							continue;

						if (periodic[0]) {
							freMaxPrTidListAB.seqTidSet.add(interTidsAB);
							freMaxPrTidListAB.sequenceNum.add(serial);
						}
						if (periodic[1]) {
							periodicTidListAB.itemSet = freMaxPrTidListAB.itemSet;
							periodicTidListAB.seqTidSet.add(interTidsAB);
							periodicTidListAB.sequenceNum.add(serial);
						}
					}
					double freMaxPrRa = (double) freMaxPrTidListAB.sequenceNum
							.size() / numOfSequences;
					double periodicRa = (double) periodicTidListAB.sequenceNum
							.size() / numOfSequences;
					

					// System.out.println(
					// Arrays.toString(freMaxPrTidListAB.itemSet) + "\n ra: " +
					// periodicRa);

					if (!freMaxPrTidListAB.seqTidSet.isEmpty()
							&& freMaxPrRa >= minRA) {
						tempFreMaxPrTidList.add(freMaxPrTidListAB);
					}
					if (!periodicTidListAB.seqTidSet.isEmpty()
							&& periodicRa >= minRA) {
						// start - added by Philippe
						periodicTidListAB.ra = periodicRa;
						// end - added by Philippe
						tempPeriodicTidList.add(periodicTidListAB);
					}
				}
			}
		}
		freMaxPrAndPeriodic.add(tempFreMaxPrTidList);
		freMaxPrAndPeriodic.add(tempPeriodicTidList);
		if (freMaxPrAndPeriodic.isEmpty())
			return null;
		else
			return freMaxPrAndPeriodic;
	}

	/**
	 * Judge if the two TidList objects have the same prefix.
	 * 
	 * @param list1
	 * @param list2
	 * @return
	 */
	public boolean haveSamePrefix(SeqTidList list1, SeqTidList list2) {

		int length = list1.itemSet.length;
		boolean havaSamePrefix = false;
		boolean temp = true;
		if (list1.itemSet.length == 1 && list2.itemSet.length == 1)
			havaSamePrefix = true;

		else {
			for (int i = 0; i < length - 1; i++)
				if (list1.itemSet[i] != list2.itemSet[i])
					temp = false;
			if (temp)
				if (list1.itemSet[length - 1] != list2.itemSet[length - 1])
					havaSamePrefix = true;
		}
		return havaSamePrefix;
	}
}