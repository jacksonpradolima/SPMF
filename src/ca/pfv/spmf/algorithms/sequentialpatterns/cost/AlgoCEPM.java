/*
O* This is an implementation of the CEPB, corCEPB, CEPN algorithms.
*
* This file is part of the SPMF DATA MINING SOFTWARE * (http://www.philippe-fournier-viger.com/spmf).
*
* SPMF is free software: you can redistribute it and/or modify it under the * terms of the GNU General Public License as published by the Free Software * Foundation, either version 3 of the License, or (at your option) any later * version. *

* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR * A PARTICULAR PURPOSE. See the GNU General Public License for more details. *
*
* You should have received a copy of the GNU General Public License along with * SPMF. If not, see .
*
*  Copyright (c) 2019 Jiaxuan Li
*/

package ca.pfv.spmf.algorithms.sequentialpatterns.cost;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This is a implementation of the CEPB, corCEPB, and CEPN algorithms. <br/>
 * <br/>
 * 
 * The three algorithms are implemented for mining cost-effective patterns in a
 * sequential event logs in terms of different type of utility information.
 * <br/>
 * <br/>
 * 
 * In the first case, the utility information is presented by a binary class,
 * but only positive sequence utility is considered. <br/>
 * <br/>
 * 
 * In the second case, the utility information is presented by a binary class
 * and all sequences are considered. A statistical measure is proposed to
 * measure the correlation between the pattern and utility. The corCEPB
 * algorithm is designed for this case. <br/>
 * <br/>
 * 
 * In the third case, the utility information is presented by a numeric value.
 * Another statistical measure is proposed to measure the patterns' efficiency
 * in terms of their spent cost and obtained utility. <br/>
 * <br/>
 * 
 * Meanwhile, a lower-bound is proposed to mine the cost-effective patterns
 * efficiently.<br/>
 * <br/>
 * 
 * This work is described in this paper: <br/>
 * <br/>
 * 
 * Fournier-Viger, P., Li, J., Lin, J. C., Chi, T. T., Kiran, R. U. (2019).
 * Mining Cost-Effective Patterns in Event Logs. Knowledge-Based Systems (KBS),
 * Elsevier,
 * 
 * @author Jiaxuan Li
 */

public class AlgoCEPM {
	/** the abreviation of the  average cost */
	private static final String AVGCOST = " #AVGCOST: ";

	/** the abreviation of the  trade-off */
	private static final String TRADE = " #TRADE: ";

	/** the abreviation of the support */
	private static final String SUP = " #SUP: ";

	/** the abreviation of the utility */
	private static final String UTIL = " #UTIL: ";
	
	/** the abreviation of the occupancy */
	private static final String OCCUP = " #OCCUP: ";

	/** start time of the latest execution */
	private long startTime;

	/** end time of the latest execution */
	private long endTime;

	/** input sequential event logs database */
	private SequenceDatabase sequenceDatabase;
	
	/** the three algorithm types implemented by this class*/
	enum AlgorithmType {
		CEPB, CEPN, CORCEPB
	}

	/** algorithm name */
	private AlgorithmType algorithmName = null;

	/** minimum support threshold */
	private int minimumSupport;

	/** maximum cost threshold */
	private double maximumCost;
	
	/** minimum occupancy threshold */
	private double minimumOccpuancy;
	
	/** the number of found cost-effective patterns */
	private int patternCount;

	/** the number of projected database */
	private int projectedDatabaseCount;

	/** the number of candidates patterns */
	private int consideredPatternCount;

	/** the size limitation of the cost-effective patterns */
	private int maximumPatternLength = 999;

	/** a list of sequential cost-effective patterns */
	private SequentialPatterns patterns = null;

	/** the size limitation of the buffer */
	private static final int BUFFERSSIZE = 2000;

	/** the array of the pattern buffer */
	private final int[] patternBuffer = new int[BUFFERSSIZE];
	
	/** DEBUG MODE if set to true */
	private static final boolean DEBUGMODE = false;
	

	/**
	 * the map of the sequence id and its utility key: sequence id in the database
	 * <br>
	 * value: utility information in each sequence
	 */
	private Map<Integer, Double> sequenceIdUtility = new HashMap<Integer, Double>();

	/** the list of patterns' cost and utility information */
	private ArrayList<CostUtilityPair> costUtilityPairs = new ArrayList<>();

	/** whether use the pruning strategy lower-bound */
	private boolean useLowerBound;
	
	/** if true, patterns in the output file are sorted by utility  */
	private boolean sortByUtilityForCEPN = false;
	/** if true, only patterns with lowest trade-off are output for each utility value */
	private boolean outputLowestTradeOffForCEPN = false;
	
	/** if true, patterns in the output file are sorted by correlation */
	private boolean sortByCorrelationCORCEPB = false;
	
	
	/**
	 * Run the CEPB algorithm
	 * setting parameters
	 * 
	 * @param inputFile     the sequential event logs database
	 * @param outputFile    the path of the file for writing results (if not null)
	 * @param minsup        the minimum support threshold
	 * @param maxcost       the maximum cost threshold
	 * 
	 * @return satisfied cost-effective patterns
	 * @throws IOException
	 */
	public SequentialPatterns runAlgorithmCEPB(String inputFile, String outputFile, int minsup, double maxcost, double minoccupancy)
			throws IOException {

		this.algorithmName = AlgorithmType.CEPB;
		runAlgorithm(inputFile, outputFile, minsup, maxcost, minoccupancy);
		return patterns;
	}
	
	/**
	 * Run the CEPB algorithm
	 * setting parameters
	 * 
	 * @param inputFile     the sequential event logs database
	 * @param outputFile    the path of the file for writing results (if not null)
	 * @param minsup        the minimum support threshold
	 * @param maxcost       the maximum cost threshold
	 * @param outputLowestTradeOffForCEPN  if true, patterns in the output file are sorted by utility 
	 * @param sortByUtilityForCEPN if true, only patterns with lowest trade-off are output for each utility value
	 * 
	 * @return satisfied cost-effective patterns
	 * @throws IOException
	 */
	public SequentialPatterns runAlgorithmCEPN(String inputFile, String outputFile, int minsup, double maxcost, double minoccupancy, 
			boolean sortByUtilityForCEPN, boolean outputLowestTradeOffForCEPN) throws IOException {
		
		this.outputLowestTradeOffForCEPN = outputLowestTradeOffForCEPN;
		this.sortByUtilityForCEPN = sortByUtilityForCEPN; 
		
		this.algorithmName = AlgorithmType.CEPN;
		runAlgorithm(inputFile, outputFile, minsup, maxcost, minoccupancy);
		return patterns;
	}
	
	/**
	 * Run the corCEPB algorithm setting parameters
	 * 
	 * @param inputFile         the sequential event logs database
	 * @param outputFile        the path of the file for writing results (if not
	 *                          null)
	 * @param minsup            the minimum support threshold
	 * @param maxcost           the maximum cost threshold
	 * @param sortByCorrelation if true, patterns in the output file are sorted by correlation
	 * 
	 * @return satisfied cost-effective patterns
	 * @throws IOException
	 */
	public SequentialPatterns runAlgorithmCorCEPB(String inputFile, String outputFile, int minsup, double maxcost, double minoccupancy, 
			boolean sortByCorrelationCORCEPB) throws IOException {

		this.sortByCorrelationCORCEPB = sortByCorrelationCORCEPB;
		this.algorithmName = AlgorithmType.CORCEPB;
		runAlgorithm(inputFile, outputFile, minsup, maxcost, minoccupancy);
		return patterns;
	}
	

	/**
	 * Run the algorithm
	 * setting parameters
	 * 
	 * @param inputFile     the sequential event logs database
	 * @param outputFile    the path of the file for writing results (if not null)
	 * @param minsup        the minimum support threshold
	 * @param maxcost       the maximum cost threshold
	 * 
	 * @return satisfied cost-effective patterns
	 * @throws IOException if error while reading the input file or writing the output file
	 */

	public SequentialPatterns runAlgorithm(String inputFile, String outputFile, int minsup, double maxcost, double minoccupancy)
			throws IOException {
		
		// Save the parameters
		this.minimumSupport = minsup;
		this.maximumCost = maxcost;
		this.minimumOccpuancy = minoccupancy;

		// Reset the tool to record memory usage
		MemoryLogger.getInstance().reset();
		
		// Record the start time
		startTime = System.currentTimeMillis();
		
		// Read the database from text file
		sequenceDatabase = new SequenceDatabase();
		sequenceDatabase.loadFile(inputFile);
		
		// Get sequence utility information
		sequenceIdUtility = sequenceDatabase.sequenceIdUtility;
		
		// Run the algorithm
		patterns = new SequentialPatterns("SEQUENTIAL LOWER BOUND PATTERN MINING");
		Map<Integer, Map<Integer, Pair>> mapSequenceID = findSequencesContainingItems();
		prefixSpanWithSingleItem(mapSequenceID);
		sequenceDatabase = null;
		
		// Write results to file
		if(outputFile != null) {
			if (AlgorithmType.CEPB.equals(algorithmName)) {
				writeResultsToFileCEPB(outputFile);
			}else if (AlgorithmType.CEPN.equals(algorithmName)) {
				writeResultsToFileCEPN(outputFile);
			}else { // CorCEPB
				writeResultsToFileCORCEPB(outputFile);
			}
		}
		
		// Record the end time
		endTime = System.currentTimeMillis();
		
		// Returns the patterns found
		return patterns;
	}

	/**
	 * Write results to an output file (CEPB algorithm)
	 * @param outputFile the path to save the results
	 * @throws IOException if error while writing to file
	 */
	private void writeResultsToFileCEPB(String outputFile) throws IOException {

		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

		try {
			DecimalFormat df = new DecimalFormat("0.000");
			// For each level
			for (List<SequentialPattern> level : patterns.levels) {
				// For each pattern
				for (SequentialPattern pattern : level) {
					// Write the pattern
					writer.write(pattern.eventSetstoString());
					writer.write(SUP + pattern.getAbsoluteSupport());
					writer.write(AVGCOST + df.format(pattern.getAverageCost()));
					writer.write(OCCUP + df.format(pattern.getOccupancy()));
					writer.newLine();
				}
			}
			writer.close();
		} finally {
			writer.close();
		}
	}
	
	/**
	 * Write results to an output file (CEPN algorithm)
	 * @param outputFile the path to save the results
	 * @throws IOException if error while writing to file
	 */
	private void writeResultsToFileCEPN(String outputFile) throws IOException {

		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

		try {
			DecimalFormat df = new DecimalFormat("0.000");

			if (sortByUtilityForCEPN) {
				// OUTPUT PATTERNS BY SORTED BY UTILITY
				Map<Double, List<SequentialPattern>> list = sortByUtility(patterns);
				for (Map.Entry<Double, List<SequentialPattern>> entry : list.entrySet()) {
					for (SequentialPattern pattern : entry.getValue()) {
						// Write the pattern
						writer.write(pattern.eventSetstoString());
						writer.write(UTIL + df.format(pattern.getUtility()));
						writer.write(SUP + pattern.getAbsoluteSupport());
						writer.write(TRADE + df.format(pattern.getTradeOff()));
						writer.write(OCCUP + df.format(pattern.getOccupancy()));				
						writer.write(AVGCOST + df.format(pattern.getAverageCost()));
						writer.newLine();
					}
				}

			} else if (outputLowestTradeOffForCEPN) {
				// OUTPUT ONLY THE LOWEST TRADE-OFF FOR EACH UTILITY
				Map<Integer, SequentialPattern> smallestTraPattern = chooseSmallMapUtiTrade(patterns);
				for (Entry<Integer, SequentialPattern> entry : smallestTraPattern.entrySet()) {

					// Write the pattern
					SequentialPattern pattern = entry.getValue();
					writer.write(pattern.eventSetstoString());
					writer.write(UTIL + df.format(pattern.getUtility()));
					writer.write(SUP + pattern.getAbsoluteSupport());
					writer.write(TRADE + df.format(pattern.getTradeOff()));
					writer.write(AVGCOST + df.format(pattern.getAverageCost()));
					writer.write(OCCUP + df.format(pattern.getOccupancy()));
					writer.newLine();
				}
			} else {
				// OTHERWISE, DEFAULT IS TO OUTPUT ALL PATTERNS SORTED BY SIZE
				// For each level
				for (List<SequentialPattern> level : patterns.levels) {
					// For each pattern
					for (SequentialPattern pattern : level) {

						// Write the pattern
						writer.write(pattern.eventSetstoString());
						writer.write(UTIL + df.format(pattern.getUtility()));
						writer.write(SUP + pattern.getAbsoluteSupport());
						writer.write(TRADE + df.format(pattern.getTradeOff()));
						writer.write(AVGCOST + df.format(pattern.getAverageCost()));
						writer.write(OCCUP + df.format(pattern.getOccupancy()));
						writer.newLine();
					}
				}
			}

			writer.close();
		} finally {
			writer.close();
		}
	}
	
	/**
	 * Write results to an output file (CORCEPB algorithm)
	 * @param outputFile the path to save the results
	 * @throws IOException if error while writing to file
	 */
	private void writeResultsToFileCORCEPB(String outputFile) throws IOException {
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

		try {
			DecimalFormat df = new DecimalFormat("0.000");
			if (sortByCorrelationCORCEPB) {
				// IF THE USER CHOOSE TO WRITE THE PATTERNS, SORTED BY CORRELATION
				Map<Double, List<SequentialPattern>> list = sortByCorrelation(patterns);
				for (Map.Entry<Double, List<SequentialPattern>> entry : list.entrySet()) {
					for (SequentialPattern pattern : entry.getValue()) {
						// Write the pattern
						writer.write(pattern.eventSetstoString());
						writer.write(" #CORR: " + df.format(pattern.getCorrelation()));
						writer.write(SUP + pattern.getAbsoluteSupport());
						writer.write(AVGCOST + df.format(pattern.getAverageCost()));
						writer.write(OCCUP + df.format(pattern.getOccupancy()));
						writer.newLine();
					}
				}
			} else {
				// OTHERWISE, DEFAULT IS TO OUTPUT ALL PATTERNS SORTED BY SIZE
				// For each level
				for (List<SequentialPattern> level : patterns.levels) {
					// For each pattern
					for (SequentialPattern pattern : level) {

						// Write the pattern
						writer.write(pattern.eventSetstoString());
						writer.write(" #CORR: " + df.format(pattern.getCorrelation()));
						writer.write(SUP + pattern.getAbsoluteSupport());
						writer.write(AVGCOST + df.format(pattern.getAverageCost()));
						writer.write(OCCUP + df.format(pattern.getOccupancy()));
						writer.newLine();
					}
				}

			}
			writer.close();
		} finally {
			writer.close();
		}
	}
	
	/**
	 * FOR CEPN: grouping cost-effective patterns in terms of their obtained utility
	 * 
	 * @param found cost-effective patterns
	 * @return cost-effective patterns grouped by their utility
	 */
	public static Map<Integer, List<SequentialPattern>> chooseMapUtilTrade(SequentialPatterns patterns) {
		Map<Integer, List<SequentialPattern>> utilityPatternTrade = new HashMap<Integer, List<SequentialPattern>>();
		for (List<SequentialPattern> level : patterns.levels) {
			for (SequentialPattern pattern : level) {
				int utility = (int) pattern.getUtility();
				if (utilityPatternTrade.get(utility) == null) {
					List<SequentialPattern> patternList = new ArrayList<SequentialPattern>();
					patternList.add(pattern);
					utilityPatternTrade.put(utility, patternList);
					continue;
				}
				utilityPatternTrade.get(utility).add(pattern);
			}
		}

		return utilityPatternTrade;
	}

	/**
	 * 
	 * FOR CEPN:  grouping cost-effective patterns by their utility, and output the pattern
	 * with the smallest trade-off in each utility
	 * 
	 * @param patterns found cost-effective patterns
	 * @return the pattern with the smallest trade-off in each utility
	 */
	public static Map<Integer, SequentialPattern> chooseSmallMapUtiTrade(SequentialPatterns patterns) {
		Map<Integer, SequentialPattern> smallestTraPattern = new HashMap<Integer, SequentialPattern>();
		for (List<SequentialPattern> level : patterns.levels) {
			for (SequentialPattern pattern : level) {
				int utility = (int) pattern.getUtility();
				if (smallestTraPattern.get(utility) == null) {
					smallestTraPattern.put(utility, pattern);
				}
				if (pattern.getTradeOff() <= smallestTraPattern.get(utility).getTradeOff()) {
					smallestTraPattern.put(utility, pattern);
				}
			}
		}

		return smallestTraPattern;
	}
	
	/**
	 * FOR CorCEPB:  sorting patterns in terms of their correlation
	 * 
	 * @param patterns found cost-effective patterns
	 * @return cost-effective patterns sorted by correlation
	 */
	public static Map<Double, List<SequentialPattern>> sortByCorrelation(SequentialPatterns patterns) {
		Map<Double, List<SequentialPattern>> treeMap = new TreeMap<>(new Comparator<Double>() {
			public int compare(Double o1, Double o2) {
				return o2.compareTo(o1);
			}
		});
		for (List<SequentialPattern> level : patterns.levels) {
			for (SequentialPattern token : level) {
				if (treeMap.get(token.getCorrelation()) == null) {
					ArrayList<SequentialPattern> list = new ArrayList<>();
					treeMap.put(token.getCorrelation(), list);
				}
				treeMap.get(token.getCorrelation()).add(token);
			}
		}
		return treeMap;
	}
	
	
	/**
	 * FOR CEPN:  sorting patterns in terms of their utility
	 * 
	 * @param patterns found cost-effective patterns
	 * @return cost-effective patterns sorted by utility
	 */
	public static Map<Double, List<SequentialPattern>> sortByUtility(SequentialPatterns patterns) {
		Map<Double, List<SequentialPattern>> treeMap = new TreeMap<>(new Comparator<Double>() {
			public int compare(Double o1, Double o2) {
				return o2.compareTo(o1);
			}
		});
		for (List<SequentialPattern> level : patterns.levels) {
			for (SequentialPattern token : level) {
				if (treeMap.get(token.getUtility()) == null) {
					ArrayList<SequentialPattern> list = new ArrayList<>();
					treeMap.put(token.getUtility(), list);
				}
				treeMap.get(token.getUtility()).add(token);
			}
		}
		return treeMap;
	}


	/**
	 * This method finds single events and recording the map of their sequence id
	 * and cost, utility information Map<Integer, Map<Integer, Pair>>, key:
	 * event_id, values: key:sequence_id, value: event's cost, sequence's length
	 * 
	 * @return the map of single events and of their located sequence id and event's
	 *         cost, sequence's length <br>
	 *         key: event, value: key:sequence id, value: event cost and sequence
	 *         length
	 * 
	 */

	private Map<Integer, Map<Integer, Pair>> findSequencesContainingItems() {

		Map<Integer, Map<Integer, Pair>> mapSequenceID = new HashMap<Integer, Map<Integer, Pair>>();

		for (int i = 0; i < sequenceDatabase.size(); i++) {
			Event[] sequence = sequenceDatabase.getSequences().get(i);

			for (Event token : sequence) {
				if (token.getId() >= 0) {
					if (mapSequenceID.get(token.getId()) == null) {
						Map<Integer, Pair> seqIdCost = new HashMap<>();
						seqIdCost.put(i, new Pair(token.getCost(), sequence.length, i+1));
						mapSequenceID.put(token.getId(), seqIdCost);
					} else {
						if (!mapSequenceID.get(token.getId()).keySet().contains(i))
							mapSequenceID.get(token.getId()).put(i, new Pair(token.getCost(), sequence.length, i+1));
					}
				}
			}

		}

		return mapSequenceID;
	}

	/**
	 * This method starts to mine cost-effective patterns
	 * 
	 * @param mapSequenceID the map of single events (key) and their corresponding
	 *                      located sequence_id and event's cost, sequence's length
	 *                      (value). key: event, value: key:sequence id, value:
	 *                      event cost and sequence length
	 * 
	 * @return
	 * @throws IOException
	 */
	private void prefixSpanWithSingleItem(Map<Integer, Map<Integer, Pair>> mapSequenceID) throws IOException {

		// =============== REMOVE INFREQUENT EVENTS ========================
		// We scan the database to remove infrequent events and resize sequences after
		// removal
		// for each sequence in the current database
		for (int i = 0; i < sequenceDatabase.size(); i++) {
			Event[] sequence = sequenceDatabase.getSequences().get(i);
			int currentPosition = 0;
			for (Event token : sequence) {
				if (token.getId() >= 0) {
					boolean isFrequent = mapSequenceID.get(token.getId()).size() >= minimumSupport;
					if (isFrequent) {
						sequence[currentPosition] = token;
						currentPosition++;
					}
				} else if (token.getId() == -2) {
					if (currentPosition > 0) {
						sequence[currentPosition] = new Event(-2, -99);
					}
					Event[] newSequence = new Event[currentPosition + 1];
					System.arraycopy(sequence, 0, newSequence, 0, currentPosition + 1);
					sequenceDatabase.getSequences().set(i, newSequence);
				}

			}

		}
		// CEPB and CEPN case
		if (AlgorithmType.CEPB.equals(algorithmName) || AlgorithmType.CEPN.equals(algorithmName)) {
			// ============= WE EXPLORE EACH PROJECTED DATABASE
			// ================================
			// for each event
			// its sup should > minSup and Lower_bound should < MaxCost
			for (Entry<Integer, Map<Integer, Pair>> entry : mapSequenceID.entrySet()) {
				consideredPatternCount++;
				int support = entry.getValue().size();
				List<Integer> sequenceID = new ArrayList<Integer>(entry.getValue().keySet());

				if (support >= minimumSupport) {
					// calculate lower_bound
					int event = entry.getKey();
					double averageCost = getAverageCostWithSingleEvent(entry.getValue());
					double occupancy = getOccupancyWithSingleEvent(entry.getValue());
					if (averageCost <= maximumCost && occupancy >= minimumOccpuancy) {
						this.costUtilityPairs = getListOfCostUtility(entry.getValue());
						// save pattern which has one event
						savePattern(event, averageCost, occupancy, entry.getValue(), costUtilityPairs);
					}
					double lowerSupportCost = getLowerBound(minimumSupport, entry.getValue());
					// AMSC lowerbound
					double lowerBoundOfCost = lowerSupportCost / minimumSupport;
					// ASC lowerbound
					// double lowerBound = lowerSupportCost / support;
					double upperBoundOfOccupancy = getUpperBoundOccupancyWithSingleEvnet(entry.getValue());
					if ((lowerBoundOfCost <= maximumCost && upperBoundOfOccupancy >= minimumOccpuancy) || (useLowerBound == false)) {
						// Create the prefix for this projected database by copying the item in the
						// buffer
						patternBuffer[0] = event;
						if (maximumPatternLength > 1) {
							// build the projected database for that item
							List<PseudoSequence> projectedDatabase = buildProjectedDatabaseSingleItems(event,
									sequenceID);
							projectedDatabaseCount++;
							recursionSingleEvents(projectedDatabase, 2, 0);
						}
					} 
				}
			}
		} // end Case1, case3

		else if (AlgorithmType.CORCEPB.equals(algorithmName)) {
			// if a pattern just occur in the negative class then there is no need to save
			// this pattern to generate next subset pattern
			for (Entry<Integer, Map<Integer, Pair>> entry : mapSequenceID.entrySet()) {
				consideredPatternCount++;

				boolean isEventAllInClass1 = false;
				int numberOfClass0 = 0;
				int numberOfClass1 = 0;
				Map<Integer, Pair> token = entry.getValue();
				// identify if a pattern just appear in class 0(positive)
				for (Integer sequenceId : token.keySet()) {
					if (sequenceIdUtility.get(sequenceId) == 0) {
						++numberOfClass0;
					} else {
						++numberOfClass1;
					}
				} // end identify
				if (numberOfClass1 == token.size()) {
					isEventAllInClass1 = true;
				}
				int support = entry.getValue().size();
				List<Integer> sequenceID = new ArrayList<Integer>(entry.getValue().keySet());
				if (support >= minimumSupport && numberOfClass0 != token.size()) {
					// calculate lower_bound
					double lowerSupportCost = getLowerBound(minimumSupport, entry.getValue());
					// double lowerBound = lowerSupportCost / support;
					double lowerBoundOfCost = lowerSupportCost / minimumSupport;
					double upperBoundOfOccupancy = getUpperBoundOccupancyWithSingleEvnet(entry.getValue());
					int event = entry.getKey();
					double averageCost = getAverageCostWithSingleEvent(entry.getValue());
					double occupancy = getOccupancyWithSingleEvent(entry.getValue());
					if (averageCost <= maximumCost && occupancy >= minimumOccpuancy) {
						// save pattern which has one event
						this.costUtilityPairs = getListOfCostUtility(entry.getValue());
						savePattern(event, averageCost,occupancy, entry.getValue(), isEventAllInClass1, costUtilityPairs);
					}
					if ((lowerBoundOfCost <= maximumCost && upperBoundOfOccupancy >= minimumOccpuancy) || (useLowerBound == false) ) {
						// Create the prefix for this projected database by copying the item in the
						// buffer
						patternBuffer[0] = event;
						// Store the lowerbound cost of this pattern
						if (maximumPatternLength > 1) {
							// build the projected database for that item
							List<PseudoSequence> projectedDatabase = buildProjectedDatabaseSingleItems(event,
									sequenceID);
							projectedDatabaseCount++;
							recursionSingleEvents(projectedDatabase, 2, 0);
						}
					}
				}

			}

		}
	}
	

	/**
	 * Set the maximum pattern length
	 * @param maximumPatternLength maximum pattern length
	 */
	public void setMaximumPatternLength(int maximumPatternLength) {
		this.maximumPatternLength = maximumPatternLength;
	}

	/**
	 * This method records events'cost and sequence's utility at different
	 * sequences.
	 * 
	 * @param seqIdPair the map of the event's sequence id and event's cost, located
	 *                  sequence's length<br>
	 *                  key: sequence id, value: event's cost and sequence length
	 * @return a list of the event's cost and utility information in the sequences
	 *         where the event appears
	 */
	private ArrayList<CostUtilityPair> getListOfCostUtility(Map<Integer, Pair> seqIdPair) {
		ArrayList<CostUtilityPair> newCostUtilityPairs = new ArrayList<>();
		for (Entry<Integer, Pair> entry : seqIdPair.entrySet()) {
			newCostUtilityPairs
					.add(new CostUtilityPair(entry.getValue().getCost(), sequenceIdUtility.get(entry.getKey())));
		}
		return newCostUtilityPairs;

	}

	/**
	 * This methods save the cost-effective patterns found by the corCEPB algorithm
	 * 
	 * @param event                event
	 * @param averageCost          event's average cost
	 * @param sequcenIdCost        the map of event's located sequence id and
	 *                             corresponding event's cost and sequence's length
	 * @param isEventAllInPositive whether events only appear in the sequences with
	 *                             the positive utility
	 * @param costUtilityPairs     a list of event's cost and utility information at
	 *                             the sequences where it appears
	 * @return
	 */
	private void savePattern(int event, double averageCost, double occupancy, Map<Integer, Pair> sequcenIdCost,
			boolean isEventAllInPositive, ArrayList<CostUtilityPair> costUtilityPairs) {
		patternCount++;
		SequentialPattern pattern = new SequentialPattern();
		pattern.addEventset(new EventSet(event));
		ArrayList<Integer> sequenceIDS = new ArrayList<Integer>(sequcenIdCost.keySet());

		// if a pattern just appears in sequences with the positive utility, then
		// correlation would be 1
		if (isEventAllInPositive) {
			pattern.setCorrelation(1);
		} else {

			// create pattern(with single item) each binary class list cost
			List<Double> patternCostListOfNegativeSeq = new ArrayList<Double>();
			List<Double> patternCostListOfPostitiveSeq = new ArrayList<Double>();
			List<Double> patternCostOfBothClass = new ArrayList<Double>();
			// record the list of event's cost of the sequences with negative utility and
			// positive utility, respectively
			for (Entry<Integer, Pair> entry : sequcenIdCost.entrySet()) {
				if (sequenceIdUtility.get(entry.getKey()) == 0) {
					patternCostListOfNegativeSeq.add(entry.getValue().getCost());
				} else {
					patternCostListOfPostitiveSeq.add(entry.getValue().getCost());
				}
				patternCostOfBothClass.add(entry.getValue().getCost());
			}
			// calculate the correlation
			double correlation = getCorrelationOfPattern(patternCostListOfNegativeSeq, patternCostListOfPostitiveSeq,
					patternCostOfBothClass, averageCost, pattern);
			pattern.setCorrelation(correlation);
			pattern.setOccupancy(occupancy);
			pattern.setNumInNegative(patternCostListOfNegativeSeq.size());
			pattern.setNumInPositive(patternCostListOfPostitiveSeq.size());
		}

		pattern.setCostUtilityPairs(costUtilityPairs);
		pattern.setAverageCost(averageCost);
		pattern.setSequencesIDs(sequenceIDS);
		patterns.addSequence(pattern, 1);

	}

	/**
	 * Save the cost-effective patterns found by the CEPB and CEPN algorithms
	 * 
	 * @param event            event
	 * @param averageCost      event's average cost
	 * @param sequcenIdCost    the map of event's located sequence id and the
	 *                         event's cost and located sequence's length
	 * @param costUtilityPairs the list of event's cost and utility information
	 * @return
	 */
	private void savePattern(int event, Double averageCost, double occupancy, Map<Integer, Pair> sequcenIdCost,
			ArrayList<CostUtilityPair> costUtilityPairs) {
		patternCount++;
		SequentialPattern pattern = new SequentialPattern();
		pattern.addEventset(new EventSet(event));
		// get event's located sequence id
		ArrayList<Integer> sequenceIDS = new ArrayList<Integer>(sequcenIdCost.keySet());
		// if the CEPN algorithm is running, calculating the event's utility
		if (algorithmName.equals(AlgorithmType.CEPN)) {
			double patternUtility = 0;
			for (Entry<Integer, Pair> entry : sequcenIdCost.entrySet()) {
				patternUtility += sequenceIdUtility.get(entry.getKey());
			}
			double averageUtility = patternUtility / sequcenIdCost.size() == 0 ? 1
					: patternUtility / sequcenIdCost.size();

			pattern.setUtility(averageUtility);
			pattern.setTradeOff(averageCost / averageUtility);

		}
		pattern.setCostUtilityPairs(costUtilityPairs);
		pattern.setAverageCost(averageCost);
		pattern.setSequencesIDs(sequenceIDS);
		pattern.setOccupancy(occupancy);
		patterns.addSequence(pattern, 1);

	}

	/**
	 * This method calculates single event's average cost.
	 * 
	 * @param sequenceIdCost the map of event's located sequence_id and event's cost
	 *                       and its located sequence's length
	 * @return event's average cost
	 */
	private double getAverageCostWithSingleEvent(Map<Integer, Pair> sequenceIdCost) {
		double costOfPattern = 0;
		for (Entry<Integer, Pair> token : sequenceIdCost.entrySet()) {
			costOfPattern += token.getValue().getCost();
		}
		return costOfPattern / sequenceIdCost.size();
	}

	/**
	 * This method calculates the single event's lower-bound
	 * 
	 * @param minimumSupport minimum support threshold
	 * @param sequenceIdCost key:sequenceId, value: event's cost and sequence's
	 *                       length in the sequences where the event appears
	 * @return event's lower-bound
	 */
	private double getLowerBound(int minimumSupport, Map<Integer, Pair> sequenceIdCost) {
		List<Double> eventCostInEachSequence = new ArrayList<Double>();
		Collection<Pair> pairs = sequenceIdCost.values();
		for (Pair token : pairs) {
			eventCostInEachSequence.add(token.getCost());
		}
		Collections.sort(eventCostInEachSequence);
		double lowerBound = 0;
		for (int i = 0; i < minimumSupport; i++) {
			lowerBound += eventCostInEachSequence.get(i);
		}

		return lowerBound;
	}
	
	/**
	 * This method calculates the single event's occupancy
	 * 
	 * @param sequenceIDLength key:sequenceID, value: event's cost and sequence's 
	 * 						   length in the sequences where the event appears
	 * @return event's occupancy
	 */
	private double getOccupancyWithSingleEvent(Map<Integer, Pair> sequenceIDLength) {
		double occupancy = 0.0;
		for (Map.Entry<Integer, Pair> entry : sequenceIDLength.entrySet()) {
			// because of end symbol -2
			int lenthOfSeq = entry.getValue().getTotalLengthOfSeq() - 1;
			occupancy += (1.0 / lenthOfSeq);
		}
		return occupancy / sequenceIDLength.size();
	}
	
	/**
	 * This method calculates the occupancy of the pattern containing more than two events
	 * 
	 * @param pseudoSequenceList list of pseudo sequences
	 * @param patternLength the pattern's length
	 * 
	 * @return the pattern's occupancy
	 */
	private double getOccupancyWithMultipleEvents(List<PseudoSequence>pseudoSequenceList,float patternLength) {
		double occupancy = 0.0;
		for (PseudoSequence token : pseudoSequenceList) {
			int lengthOfSeq = token.getSequenceLength() - 1;
			occupancy += patternLength / lengthOfSeq;
		}
		return occupancy / pseudoSequenceList.size();
	}
	
	private double getUpperBoundOccupancyWithSingleEvnet(Map<Integer, Pair> sequenceIDLength) {
		double upperOccupancy = 0;
		ArrayList<Double> upperOccupList = new ArrayList<>();
		for (Map.Entry<Integer, Pair> entry : sequenceIDLength.entrySet()) {
			int lenOfSeq = entry.getValue().getTotalLengthOfSeq() - 1;
			upperOccupList.add((1.0 + (lenOfSeq - entry.getValue().getIndexOfNextEvent())) / lenOfSeq);
		}
		Collections.sort(upperOccupList, new Comparator<Double>() {
			public int compare(Double a, Double b) {
				return b.compareTo(a);
			}
		});
		for (int i = 0; i < minimumSupport; i++) {
			upperOccupancy += upperOccupList.get(i);
		}
		return upperOccupancy / minimumSupport;
	}

	private double getUpperBoundOccupancyWithMultipeEvents(List<PseudoSequence> list, float patternLength) {
		double upperOccupancy = 0.0;
		ArrayList<Double> upperOccupList = new ArrayList<>();
		for (PseudoSequence token : list) {
			int len = token.getSequenceLength() - 1;
			upperOccupList.add((double) ((patternLength + len - token.indexFirstItem) / len));
		}
		Collections.sort(upperOccupList, new Comparator<Double>() {
			public int compare(Double a, Double b) {
				return b.compareTo(a);
			}
		});
		for (int i = 0; i < minimumSupport; i++) {
			upperOccupancy += upperOccupList.get(i);
		}
		return upperOccupancy / minimumSupport;
	}
	
	/**
	 * This method creates a new projected database
	 * 
	 * @param event       event
	 * @param sequenceIDs a list the sequence id where the event appears
	 * @return the event's projected database
	 */
	private List<PseudoSequence> buildProjectedDatabaseSingleItems(int event, List<Integer> sequenceIDs) {
		List<PseudoSequence> projectedDatabase = new ArrayList<PseudoSequence>();

		// for each sequence that contains the current item
		loopSeq: for (int sequenceID : sequenceIDs) {
			Event[] sequence = sequenceDatabase.getSequences().get(sequenceID);

			// for each token in this sequence (item or end of sequence (-2)
			// find item by niki
			for (int j = 0; sequence[j].getId() != -2; j++) {
				int token = sequence[j].getId();

				// if it is the item that we want to use for projection
				if (token == event) {
					// if it is not the end of the sequence
					if (sequence[j + 1].getId() != -2) {
						PseudoSequence pseudoSequence = new PseudoSequence(sequenceID, j + 1, sequence.length);
						projectedDatabase.add(pseudoSequence);
					}

					// break because we have found what we have created the pseudosequence for the
					// current sequence
					continue loopSeq;
				}
			}
			MemoryLogger.getInstance().checkMemory();
		}
		return projectedDatabase; // return the projected database
	}

	/**
	 * This method finds the longer cost-effective patterns
	 * 
	 * @param database                    the pattern's projected database
	 * @param k                           pattern's length (number of events with
	 *                                    the pattern)
	 * @param lastBufferPositionOfPattern the last event' position in the buffer
	 * @throws IOException
	 */
	private void recursionSingleEvents(List<PseudoSequence> database, int k, int lastBufferPositionOfPattern)
			throws IOException {

		Map<Integer, List<PseudoSequence>> eventsPseudoSequence = findAllFrequentPairsSingleEvents(database);

		database = null;
		// for multiple events
		if (algorithmName.equals(AlgorithmType.CEPB) || algorithmName.equals(AlgorithmType.CEPN)) {
			for (Map.Entry<Integer, List<PseudoSequence>> entry : eventsPseudoSequence.entrySet()) {
				consideredPatternCount++;
				int support = entry.getValue().size();
				if (support >= minimumSupport) {
					patternBuffer[lastBufferPositionOfPattern + 1] = -1;
					patternBuffer[lastBufferPositionOfPattern + 2] = entry.getKey();
					// get lowerBound of patterns which have more than two events
					double lowerSupportCost = getLowerBound(lastBufferPositionOfPattern, entry.getKey(),
							entry.getValue());
					double lowerboundOfCost = lowerSupportCost / minimumSupport;
					double averageCost = getAverageCostWithMulEvents(lastBufferPositionOfPattern, entry.getValue(),
							entry.getKey());
					double occupancy = getOccupancyWithMultipleEvents(entry.getValue(),k);
					double upperBoundOfOccupancy = getOccupancyWithMultipleEvents(entry.getValue(), k);
					if (averageCost <= maximumCost && occupancy >= minimumOccpuancy) {
						this.costUtilityPairs = setListOfCostUtility(lastBufferPositionOfPattern + 2, entry.getValue());
						savePattern(lastBufferPositionOfPattern + 2, entry.getValue(), averageCost, occupancy, costUtilityPairs);
					}
					if ((lowerboundOfCost <= maximumCost && upperBoundOfOccupancy >= minimumOccpuancy) || (!useLowerBound)) {
						// make a recursive call
						if (k < maximumPatternLength) {
							projectedDatabaseCount++;
							recursionSingleEvents(entry.getValue(), k + 1, lastBufferPositionOfPattern + 2);
						}
					} 
				}
				MemoryLogger.getInstance().checkMemory();
			}
		} // end case1

		if (algorithmName.equals(AlgorithmType.CORCEPB)) {
			for (Map.Entry<Integer, List<PseudoSequence>> entry : eventsPseudoSequence.entrySet()) {
				consideredPatternCount++;
				int numberOfNegSeq = 0;
				int numberOfPosSeq = 0;
				for (PseudoSequence sequence : entry.getValue()) {
					if (sequenceIdUtility.get(sequence.sequenceID) == 0) {
						++numberOfNegSeq;
					} else {
						++numberOfPosSeq;
					}
				}
				boolean isPatternAllInPosSeq = (numberOfPosSeq == entry.getValue().size());

				int support = entry.getValue().size();
				if (support >= minimumSupport && numberOfNegSeq != entry.getValue().size()) {
					double lowerSupportCost = getLowerBound(lastBufferPositionOfPattern, entry.getKey(),
							entry.getValue());
					double lowerboundOfCost = lowerSupportCost / minimumSupport;
					double averageCost = getAverageCostWithMulEvents(lastBufferPositionOfPattern, entry.getValue(),
							entry.getKey());
					double occupancy = getOccupancyWithMultipleEvents(entry.getValue(), k);
					double upperBoundOfOccupancy = getUpperBoundOccupancyWithMultipeEvents(entry.getValue(), k);			
					patternBuffer[lastBufferPositionOfPattern + 1] = -1;
					patternBuffer[lastBufferPositionOfPattern + 2] = entry.getKey();
					if (averageCost <= maximumCost && occupancy >= minimumOccpuancy) {
						this.costUtilityPairs = setListOfCostUtility(lastBufferPositionOfPattern + 2, entry.getValue());
						// save the pattern which has more than two events
						savePattern(lastBufferPositionOfPattern + 2, eventsPseudoSequence.get(entry.getKey()),
								isPatternAllInPosSeq, averageCost, occupancy, costUtilityPairs);
					}
					if ((lowerboundOfCost <= maximumCost && upperBoundOfOccupancy >= minimumOccpuancy) || (useLowerBound == false)) {
						// make a recursive call
						if (k < maximumPatternLength) {
							projectedDatabaseCount++;
							recursionSingleEvents(entry.getValue(), k + 1, lastBufferPositionOfPattern + 2);
						}
					}
				}
				MemoryLogger.getInstance().checkMemory();
			}

		}
	}

	/**
	 * This method first obtains the patterns containing more than two events and
	 * then records patterns' list of the cost and utility information
	 * 
	 * @param lastBufferPosition the last event position in the buffer
	 * @param pseudoSequences    the list of last event's sequence id and index in
	 *                           the database
	 * @return the list of pattern's cost and utility information
	 */
	private ArrayList<CostUtilityPair> setListOfCostUtility(int lastBufferPosition,
			List<PseudoSequence> pseudoSequences) {

		ArrayList<CostUtilityPair> costUtilityPairs = new ArrayList<>();
		List<Integer> eventsIdBeforeCurrentEvent = new ArrayList<Integer>();
		SequentialPattern pattern = new SequentialPattern();
		EventSet currentEventset = new EventSet();
		// get current pattern
		for (int i = 0; i <= lastBufferPosition; i++) {
			int token = patternBuffer[i];
			if (token >= 0) {
				currentEventset.addEvent(token);
				eventsIdBeforeCurrentEvent.add(token);
			} else if (token == -1) {
				pattern.addEventset(currentEventset);
				currentEventset = new EventSet();
			}
		}
		pattern.addEventset(currentEventset);

		for (PseudoSequence token : pseudoSequences) {
			int currentEventPosition = (token.indexFirstItem) - 1;
			int sequenceId = token.sequenceID;
			double costOfPattern = 0;
			Event[] events = sequenceDatabase.getSequences().get(sequenceId);
			// the pointer of eventsBeforeCurrentEvent
			int j = 0;
			// calculate pattern's cost at the sequences where it appears
			for (int i = 0; i <= currentEventPosition; i++) {
				if (events[i].getId() == eventsIdBeforeCurrentEvent.get(j)) {
					costOfPattern += events[i].getCost();
					j++;
				}
			}
			costUtilityPairs.add(new CostUtilityPair(costOfPattern, sequenceIdUtility.get(sequenceId)));
		}
		return costUtilityPairs;

	}

	/**
	 * This method gets the projected database of the event
	 * 
	 * @param sequences          the list of last event's sequence id and its index
	 *                           in each sequence
	 * @return event's projected database, key:event, value: list of sequence_id and
	 *         the event's starting index in the sequence
	 */
	private Map<Integer, List<PseudoSequence>> findAllFrequentPairsSingleEvents(List<PseudoSequence> sequences) {
		Map<Integer, List<PseudoSequence>> mapEventsPseudoSequences = new HashMap<Integer, List<PseudoSequence>>();
		for (PseudoSequence pseudoSequence : sequences) {
			int sequenceID = pseudoSequence.getOriginalSequenceID();
			Event[] sequence = sequenceDatabase.getSequences().get(sequenceID);

			// for each token in this sequence
			for (int i = pseudoSequence.indexFirstItem; sequence[i].getId() != -2; i++) {
				int token = sequence[i].getId();

				// if it is an item
				if (token >= 0) {
					// get the pair object stored in the map if there is one already
					List<PseudoSequence> listSequences = mapEventsPseudoSequences.get(token);
					// if there is no pair object yet
					if (listSequences == null) {
						listSequences = new ArrayList<PseudoSequence>();
						// store the pair object that we created
						mapEventsPseudoSequences.put(token, listSequences);
					}

					// Check if that sequence as already been added to the projected database of
					// this item
					boolean ok = true;
					if (listSequences.size() > 0) {
						ok = listSequences.get(listSequences.size() - 1).sequenceID != sequenceID;
					}
					// if not we add it
					if (ok) {
						listSequences.add(new PseudoSequence(sequenceID, i + 1, sequence.length));
					}
				}
				MemoryLogger.getInstance().checkMemory();
			}

		}
		return mapEventsPseudoSequences;
	}

	/**
	 * This method calculates the sum of the first minimum support number of the
	 * pattern's cost containing more than two events, this return value is used to
	 * calculate the lower-bound of the pattern
	 * 
	 * @param lastBufferPosition the last event position in the buffer
	 * @param currentEvent       the current event
	 * @param pseudoSequences    the list of last event's sequence id and index in
	 *                           the database
	 * @return lower bound of patterns that have more than two events
	 */
	private double getLowerBound(int lastBufferPosition, int currentEvent, List<PseudoSequence> pseudoSequences) {
		double lowerSupportCost = 0;
		List<Integer> eventsBeforeCurrentEvent = new ArrayList<Integer>();
		// get the eventSet before the current event
		for (int i = 0; i <= lastBufferPosition; i++) {
			int token = patternBuffer[i];
			if (token >= 0) {
				eventsBeforeCurrentEvent.add(token);
			}
		}
		// get the pattern containing all events
		eventsBeforeCurrentEvent.add(currentEvent);
		// calculate pattern's cost
		ArrayList<Double> listOfCost = new ArrayList<Double>();
		for (PseudoSequence token : pseudoSequences) {
			double costOfPattern = 0;
			int currentEventPosition = (token.indexFirstItem) - 1;
			int sequenceId = token.sequenceID;
			Event[] events = sequenceDatabase.getSequences().get(sequenceId);
			// the pointer of eventsBeforeCurrentEvent
			int j = 0;
			for (int i = 0; i <= currentEventPosition; i++) {
				if (events[i].getId() == eventsBeforeCurrentEvent.get(j)) {
					costOfPattern += events[i].getCost();
					j++;
				}
			}
			listOfCost.add(costOfPattern);
		}
		// calculate the sum of the first minimum support number of the pattern's cost
		Collections.sort(listOfCost);
		for (int i = 0; i < minimumSupport; i++) {
			lowerSupportCost += listOfCost.get(i);
		}

		return lowerSupportCost;
	}

	/**
	 * This method save the patterns containing more than two events found by CEPN
	 * 
	 * @param lastBufferPosition the last event position in the buffer
	 * @param pseudoSequences    the list of last event's sequence id and its index
	 *                           in each sequence
	 * @param averageCost        pattern's average cost
	 * @param costUtilityPairs   the list of pattern's cost and utility information
	 * @throws IOException
	 */
	private void savePattern(int lastBufferPosition, List<PseudoSequence> pseudoSequences, double averageCost, double occupancy, 
			ArrayList<CostUtilityPair> costUtilityPairs) {
		// increase the number of pattern found for statistics purposes
		patternCount++;
		// create a set to record events before currentEvent
		List<Integer> eventsIdBeforeCurrentEvent = new ArrayList<Integer>();

		SequentialPattern pattern = new SequentialPattern();
		int eventsetCount = 0;
		EventSet currentEventset = new EventSet();
		for (int i = 0; i <= lastBufferPosition; i++) {
			int token = patternBuffer[i];
			if (token >= 0) {
				currentEventset.addEvent(token);
				eventsIdBeforeCurrentEvent.add(token);
			} else if (token == -1) {
				pattern.addEventset(currentEventset);
				currentEventset = new EventSet();
				eventsetCount++;
			}
		}
		pattern.addEventset(currentEventset);
		eventsetCount++;

		List<Integer> sequencesIDs = new ArrayList<Integer>(pseudoSequences.size());
		for (int i = 0; i < pseudoSequences.size(); i++) {
			sequencesIDs.add(pseudoSequences.get(i).sequenceID);
		}
		// calculate pattern's trade-off of case3
		if (algorithmName.equals(AlgorithmType.CEPN)) {
			double tradeOff = getPatternWithMultiEvenTradeoff(pseudoSequences, averageCost, pattern);

			pattern.setTradeOff(tradeOff);
		}
		pattern.setCostUtilityPairs(costUtilityPairs);
		// add sequenceIDs
		pattern.setSequencesIDs(sequencesIDs);
		pattern.setAverageCost(averageCost);
		pattern.setOccupancy(occupancy);
		patterns.addSequence(pattern, eventsetCount);

	}

	// save pattern which has more than two events case 2
	/**
	 * This method save the patterns containing more than two events found by
	 * corCEPB
	 * 
	 * @param lastBufferPosition        the last event position in the buffer
	 * @param pseudoSequences           the list of last event's sequence id and its
	 *                                  index in each sequence
	 * @param isPatternAllInPositiveSeq whether patterns only appears in the
	 *                                  sequences with the positive utility
	 * @param averageCost               pattern's average cost
	 * @param costUtilityPairs          the list of pattern's cost and utility
	 *                                  information
	 * @throws IOException
	 */
	private void savePattern(int lastBufferPosition, List<PseudoSequence> pseudoSequences,
			boolean isPatternAllInPositiveSeq, double averageCost, double occupancy, ArrayList<CostUtilityPair> costUtilityPairs)
 {
		// increase the number of pattern found for statistics purposes
		patternCount++;
		// create a set to record eventId before currentEvent
		List<Integer> eventsIdBeforeCurrentEvent = new ArrayList<Integer>();
		SequentialPattern pattern = new SequentialPattern();
		int eventsetCount = 0;
		EventSet currentEventset = new EventSet();
		for (int i = 0; i <= lastBufferPosition; i++) {
			int token = patternBuffer[i];
			if (token >= 0) {
				currentEventset.addEvent(token);
				eventsIdBeforeCurrentEvent.add(token);
			} else if (token == -1) {
				pattern.addEventset(currentEventset);
				currentEventset = new EventSet();
				eventsetCount++;
			}
		}
		pattern.addEventset(currentEventset);
		eventsetCount++;

		List<Integer> sequencesIDs = new ArrayList<Integer>(pseudoSequences.size());
		for (int i = 0; i < pseudoSequences.size(); i++) {
			sequencesIDs.add(pseudoSequences.get(i).sequenceID);
		}
		// calculate pattern's corrrealtion
		double correlation = getPatternWithMultiEventCorrelation(eventsIdBeforeCurrentEvent, pseudoSequences, pattern,
				isPatternAllInPositiveSeq);
		// add sequenceIDs
		pattern.setCostUtilityPairs(costUtilityPairs);
		pattern.setSequencesIDs(sequencesIDs);
		pattern.setAverageCost(averageCost);
		pattern.setOccupancy(occupancy);
		pattern.setCorrelation(correlation);
		patterns.addSequence(pattern, eventsetCount);

	}

	/**
	 * This method calculates the pattern's average cost containing more than two
	 * events found by CEPB
	 * 
	 * @param lastBufferPosition the last event position in the buffer
	 * @param pseudoSequences    the list of last event's sequence id and index in
	 *                           the database
	 * @param currentEvent       the current event
	 * @return pattern's average cost
	 */
	private double getAverageCostWithMulEvents(int lastBufferPosition, List<PseudoSequence> pseudoSequences,
			int currentEvent) {
		List<Integer> eventsIdBeforeCurrentEvent = new ArrayList<Integer>();
		double costOfPattern = 0;
		for (int i = 0; i <= lastBufferPosition; i++) {
			int token = patternBuffer[i];
			if (token >= 0) {
				eventsIdBeforeCurrentEvent.add(token);
			}
		}
		eventsIdBeforeCurrentEvent.add(currentEvent);
		for (PseudoSequence token : pseudoSequences) {
			int currentEventPosition = (token.indexFirstItem) - 1;
			int sequenceId = token.sequenceID;
			Event[] events = sequenceDatabase.getSequences().get(sequenceId);
			// the pointer of eventsBeforeCurrentEvent
			int j = 0;
			for (int i = 0; i <= currentEventPosition; i++) {
				if (events[i].getId() == eventsIdBeforeCurrentEvent.get(j)) {
					costOfPattern += events[i].getCost();
					j++;
				}
			}
		}

		return costOfPattern / pseudoSequences.size();

	}

	/**
	 * This method calculates pattern's trade-off
	 * 
	 * @param pseudoSequences the list of last event's sequence id and its index in
	 *                        each sequence
	 * @param averageCost     pattern's average cost
	 * @param pattern         current pattern
	 * @return
	 */
	private double getPatternWithMultiEvenTradeoff(List<PseudoSequence> pseudoSequences, double averageCost,
			SequentialPattern pattern) {

		if (algorithmName.equals(AlgorithmType.CEPN)) {
			double patternUtility = 0;
			for (PseudoSequence token : pseudoSequences) {
				patternUtility += sequenceIdUtility.get(token.sequenceID);
			}
			double averageUtility = patternUtility / pseudoSequences.size() == 0 ? 1
					: patternUtility / pseudoSequences.size();
			pattern.setUtility(averageUtility);
			return averageCost / averageUtility;
		}

		return -999;

	}

	// get average cost and correlation of case 2 where a pattern has more than two
	// events
	/**
	 * This method calculates pattern's correlation containing more than two events
	 * found by the corCEPB algorithm
	 * 
	 * @param eventsBeforeCurrentEvent events before the current event
	 * @param pseudoSequences          the list of last event's sequence id and its
	 *                                 index in each sequence
	 * @param pattern                  current pattern
	 * @param isPatternAllInPostiveSeq whether patterns only appears in the
	 *                                 sequences with positive utility
	 * @return pattern's correlation
	 */
	private double getPatternWithMultiEventCorrelation(List<Integer> eventsBeforeCurrentEvent,
			List<PseudoSequence> pseudoSequences, SequentialPattern pattern, boolean isPatternAllInPostiveSeq) {
		// each pattern's cost
		double costOfPattern = 0;
		double patternCostOfEachSeq = 0;
		if (algorithmName.equals(AlgorithmType.CORCEPB)) {
			if (isPatternAllInPostiveSeq) {
				return 1;
			} else {
				// for each binary Class store pattern's cost
				List<Double> patternCostListOfclass0 = new ArrayList<Double>();
				List<Double> patternCostListOfclass1 = new ArrayList<Double>();
				List<Double> patternCostOfBothClass = new ArrayList<Double>();
				for (PseudoSequence token : pseudoSequences) {
					int currentEventPosition = (token.indexFirstItem) - 1;
					int sequenceId = token.sequenceID;
					Event[] events = sequenceDatabase.getSequences().get(sequenceId);
					patternCostOfEachSeq = 0;
					int j = 0;
					for (int i = 0; i <= currentEventPosition; i++) {
						if (events[i].getId() == eventsBeforeCurrentEvent.get(j)) {
							costOfPattern += events[i].getCost();
							patternCostOfEachSeq += events[i].getCost();
							j++;
						}
					}
					if (sequenceIdUtility.get(token.sequenceID) == 0) {
						patternCostListOfclass0.add(patternCostOfEachSeq);
						patternCostOfBothClass.add(patternCostOfEachSeq);
					} else {
						patternCostListOfclass1.add(patternCostOfEachSeq);
						patternCostOfBothClass.add(patternCostOfEachSeq);
					}

				}
				double correlation = getCorrelationOfPattern(patternCostListOfclass0, patternCostListOfclass1,
						patternCostOfBothClass, costOfPattern / patternCostOfBothClass.size(), pattern);
				pattern.setNumInNegative(patternCostListOfclass0.size());
				pattern.setNumInPositive(patternCostListOfclass1.size());
				return correlation;

			}
		}
		return -99;

	}

	// get correlation of pattern that has one event
	/**
	 * This method calculates the correlation of the single event
	 * 
	 * @param patternCostListOfNegativeSeq    the list of pattern's cost in the
	 *                                        sequences with the negative utility
	 * @param patternCostListOfPositiveSeqthe list of pattern's cost in the
	 *                                        sequences with the positive utility
	 * @param patternCostOfBothClass          the list of pattern's cost in all
	 *                                        sequences
	 * @param averageCostOfEvent              event's average cost
	 * @param pattern                         cost-effective pattern containing one
	 *                                        event
	 * @return event's correlation
	 */
	private double getCorrelationOfPattern(List<Double> patternCostListOfNegativeSeq,
			List<Double> patternCostListOfPositiveSeq, List<Double> patternCostOfBothClass, double averageCostOfEvent,
			SequentialPattern pattern) {
		double correlation = 0;
		double averageCostOfNegSeq = 0;
		double averageCostOfPosSeq = 0;
		double sizeOfNegSeq = patternCostListOfNegativeSeq.size();
		double sizeOfPosSeq = patternCostListOfPositiveSeq.size();
		double totalSizeOfevent = patternCostOfBothClass.size();
		double standDeviation = 0;
		for (Double eventCost : patternCostListOfNegativeSeq) {
			averageCostOfNegSeq += eventCost;
		}
		averageCostOfNegSeq = averageCostOfNegSeq / sizeOfNegSeq;
		for (Double eventCost : patternCostListOfPositiveSeq) {
			averageCostOfPosSeq += eventCost;
		}
		averageCostOfPosSeq = averageCostOfPosSeq / sizeOfPosSeq;
		for (Double eventCost : patternCostOfBothClass) {
			standDeviation += Math.pow(eventCost - averageCostOfEvent, 2);
		}
		standDeviation = Math.sqrt(standDeviation / patternCostOfBothClass.size());
		// avoid standDeviation equals to zero, result in correlation equals to
		// non-limitation
		if (standDeviation == 0) {
			standDeviation += 1;
		}
		correlation = (averageCostOfPosSeq - averageCostOfNegSeq) / standDeviation
				* Math.sqrt((sizeOfNegSeq / totalSizeOfevent) * (sizeOfPosSeq / totalSizeOfevent));
		pattern.setAverageCostInNeg(averageCostOfNegSeq);
		pattern.setAverageCostInPos(averageCostOfPosSeq);
		return correlation;
	}

	/**
	 * Print statistics about the algorithm
	 */
	public void printStatistics() {
		StringBuilder r = new StringBuilder(200);
		r.append("=============  " + algorithmName + " 2.42 STATISTICS =============");
		r.append(System.lineSeparator());
		r.append(" Pattern count : ");
		r.append(patternCount);
		r.append(System.lineSeparator());
		r.append(" Total time : ");
		r.append(endTime - startTime);
		r.append(" ms");
		r.append(System.lineSeparator());
		r.append(" Max memory (mb) : ");
		r.append(MemoryLogger.getInstance().getMaxMemory());
		r.append(System.lineSeparator());
		if(DEBUGMODE){
			r.append("  Projected Database Count: ");
			r.append(projectedDatabaseCount);
			r.append('\n');
			r.append("  Considered pattern count: ");
			r.append(consideredPatternCount);
			 r.append('\n');
			 r.append(" Frequent sequences count : " + patterns.sequenceCount);
			r.append('\n');
		}
		r.append("===================================================");
		r.append(System.lineSeparator());
		// if the result was save into memory, print it
		System.out.println(r.toString());
	}


    /**
     * Set to use the lower bound or not
     * @param useLowerBound if true, will use the lower bound, otherwise not
     */
	public void setUseLowerBound(boolean useLowerBound) {
		this.useLowerBound = useLowerBound;
	}

}