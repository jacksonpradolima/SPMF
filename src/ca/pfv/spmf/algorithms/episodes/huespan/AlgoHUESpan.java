package ca.pfv.spmf.algorithms.episodes.huespan;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
/**
 * Copyright (c) 2019 Peng Yang, Philippe Fournier-Viger et al.

 * This file is part of the SPMF DATA MINING SOFTWARE *
 * (http://www.philippe-fournier-viger.com/spmf).
 *
 * SPMF is free software: you can redistribute it and/or modify it under the *
 * terms of the GNU General Public License as published by the Free Software *
 * Foundation, either version 3 of the License, or (at your option) any later *
 * version. SPMF is distributed in the hope that it will be useful, but WITHOUT
 * ANY * WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.pfv.spmf.tools.MemoryLogger;

/*
 * This file is part of the SPMF DATA MINING SOFTWARE *
 * (http://www.philippe-fournier-viger.com/spmf).
 *
 * SPMF is free software: you can redistribute it and/or modify it under the *
 * terms of the GNU General Public License as published by the Free Software *
 * Foundation, either version 3 of the License, or (at your option) any later *
 * version. SPMF is distributed in the hope that it will be useful, but WITHOUT
 * ANY * WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright Peng Yang, Philippe Fournier-Viger, 2019
 */
/**
 * This algorithm is implemented for mining high utility episode in a complex
 * sequence The UP_Span cannot find the truly high utility episode, because
 * there are many utilities of a minimal occurrence of an episode. The UP_Span
 * only choose the first one, however, it may be a none maximum utility in the
 * minimal occurrence. Hence, The HUE_Span is proposed to minimg completely and
 * truly high utility episodes by adding a 'checkMaximumUtility' function to
 * UP_Span. Meanwhile, a tighter upper-bound was proposed to mine the HUEs
 * efficiently And a coocMatrix structure was proposed to efficiently mine the
 * HUEs
 *
 * HUE-Span is presented in this paper: <br/>
 * <br/>
 * 
 * Fournier-Viger, P., Yang, P., Lin, J. C.-W., Yun, U. (2019). HUE-SPAN: Fast
 * High Utility Episode Mining. Proc. 14th Intern. Conference on Advanced Data
 * Mining and Applications (ADMA 2019) Springer LNAI, pp. 169-184.
 * 
 * @author Peng yang
 */
public class AlgoHUESpan {

	/** start time of the latest execution */
	private long startTimestamp;

	/** end time of the latest execution */
	private long endTimestamp;

	/** the candidate count */
	private long candidateCount = 0;

	/** the combinated episode count */
	private long combinatedEpisodeCount = 0;

	/** the HUE count */
	private long hueCount = 0;

	/** the cooc matrix pruning count */
	private long matrixPruningCount = 0;

	/** the upperBound Pruning count */
	private long upperBoundPruningCount = 0;

	/**
	 * whether use 'checkMaximumUtility' function if not use, the algorithm will not
	 * find the complete HUEs, (UP_Span)
	 */
	private boolean checkMaximumUtility;

	/**
	 * If true, the tighter upper-bound (ERU) will be used. If false , EWU will be
	 * used
	 **/
	private boolean useTighterUpperBound;

	/** If true, The CoocMatrix pruning will be used */
	private boolean useCoocMatrix;

	/**
	 * If true; The prefix of episode will tried to do pruning in coocMatrix pruning
	 * operation
	 */
	private boolean pruningPrefix;

	/** Only for testing, if true, the matrixs will be outputed */
	private boolean showMatrix = false;

	/** the path to output the matrix if needed for debugging */
	private String outputMatrixPath = "HUE_showMatrix.txt";

	/** If true the single events will be output */
	private boolean outputSingleEvents;

	/** record the non-maximal utility episode count */
	private int episodeWithNonMaxUtilityCount = 0;

	/** the minimum utility threshold : a ratio */
	private double minUtilityRatio;

	/** absolute minimum utility threshold */
	private double minUtilityAbsolute;

	/** The maximum time duration threshold **/
	private int maxDuration;

	/** Total utility in the database */
	private long sequenceUtility = 0;

	/**
	 * Map: key: item value: another item that followed the first item + support
	 * (could be replaced with a triangular matrix...)
	 */
	private Map<Integer, Map<Integer, Integer>> coocMapAfter = null;
	private Map<Integer, Map<Integer, Integer>> coocMapEquals = null;

	/** writer to write output file */
	private BufferedWriter writer = null;

	/** The patterns that are found **/
	private HighUtilityEpisodes huepisodes = null;

	/** The complex sequence that contains simultaneous items */
	private ComplexSequence complexSequence;

	/**
	 * The 1-candidates that their TWU larger than minUtility key: 1-candidates
	 * value: 1. the minimal occurrrences list of 1-candidates we only use one
	 * figure to represent the occurrences for the start timepoint is same as end
	 * timepoint 2. the utility vaue in the sequence
	 */
	private Map<Integer, MoListUtilityList> mapSingleCandidatesWithMoListAndUtilityList;

	/** The largest TID in the comoplex sequence */
	private int largestTID;

	/**
	 * Constructor
	 */
	public AlgoHUESpan() {
		// empty
	}

	/**
	 * The method to run the algorithm
	 * 
	 * @param inputFile            the path of input file
	 * @param outputFile           the path of output file
	 * @param minUtilityRatio      the minimum utility ratio
	 * @param maxDuration          the maximum duration
	 * @param checkMaximumUtility  if true, uses maximal high utility
	 * @param useTighterUpperBound if true, uses tighter upper-bound
	 * @param outputSingleEvents   if true, output single event
	 * @param useCoocMatrix        if true, use EEUCS sturcture
	 * @param pruningPrefix        if true, pruning episode by checking their prefix
	 * @return the high utility episodes
	 * @throws IOException if error reading or writing to a file
	 */
	public HighUtilityEpisodes runAlgorithm(String inputFile, String outputFile, double minUtilityRatio,
			int maxDuration, boolean checkMaximumUtility, boolean useTighterUpperBound, boolean outputSingleEvents,
			boolean useCoocMatrix, boolean pruningPrefix) throws IOException {

		// reset maximum memory usage
		MemoryLogger.getInstance().reset();

		// initialize variables
		this.minUtilityRatio = minUtilityRatio;
		this.maxDuration = maxDuration;
		this.checkMaximumUtility = checkMaximumUtility;
		this.useTighterUpperBound = useTighterUpperBound;
		this.outputSingleEvents = outputSingleEvents;
		this.useCoocMatrix = useCoocMatrix;
		this.pruningPrefix = pruningPrefix;

		// record start time
		this.startTimestamp = System.currentTimeMillis();

		// if the user want to keep the result into memory
		if (outputFile == null) {
			writer = null;
			this.huepisodes = new HighUtilityEpisodes();
		} else { // if the user want to save the result to a file
			this.huepisodes = null;
			this.writer = new BufferedWriter(new FileWriter(outputFile));

		}

		this.complexSequence = new ComplexSequence();
		// init the singleCandidates and its minimal occurrences and total utilities
		this.mapSingleCandidatesWithMoListAndUtilityList = new HashMap<>();

		/* determine the high utility single episode ,and get singleCandidates */
		scanDatabaseToFindHighUtilitySingleEpisodes(inputFile);

		/*
		 * remove the non candidates from complexSequence and mapSingleCandidates.., and
		 * sort events at each point in the complexSequence
		 */
		this.complexSequence.pruneSingleEventsByUpperBound(this.maxDuration, this.minUtilityAbsolute,
				this.mapSingleCandidatesWithMoListAndUtilityList, this.useTighterUpperBound);

		if (this.useCoocMatrix) {
			// build coocMatrixs
			buildCoocUtilityMatrix();
		}
		

		System.out.println("checkMaximumUtility : " + checkMaximumUtility);
		System.out.println(" min utility absolute : " + minUtilityAbsolute);
		

		for (Map.Entry<Integer, MoListUtilityList> entry : this.mapSingleCandidatesWithMoListAndUtilityList.entrySet()) {
			combinatedEpisodeCount++;
			int candidate = entry.getKey();
			List<int[]> alphaEpisode = new ArrayList<>();
			alphaEpisode.add(new int[] { candidate });

			List<Integer> alphaMOs = entry.getValue().getMoList();
			List<Integer> alphaUtilityList = entry.getValue().getUtilityList();

			// previousUtility records the utility of k-episode from 1 ~ k-1. Only the
			// utility of last (k) eventset do not take into account
			// for 1-episode, the previousUtility is zero
			List<Integer> alphaPreviousUtility = new ArrayList<>();
			for (int i = 0; i < alphaMOs.size(); i++) {
				alphaPreviousUtility.add(0);
			}
			int[] totalUtilityUpperBoundUtilityPairOfMOs = calculateUtilityAndUpperBoundOfMOs(candidate, alphaMOs,
					alphaMOs, alphaPreviousUtility, alphaUtilityList);
			int totalUtility = totalUtilityUpperBoundUtilityPairOfMOs[0];
			int upperBoundUtility = totalUtilityUpperBoundUtilityPairOfMOs[1];

			if (upperBoundUtility >= this.minUtilityAbsolute) {

				if (this.outputSingleEvents && totalUtility >= this.minUtilityAbsolute) {
					// if the item is a HUE, then we save them
					HighUtilityEpisode hue = new HighUtilityEpisode(alphaEpisode, totalUtility);
					save(hue, hue.getSize());
				}

				mineHUE(alphaEpisode, alphaMOs, // represent the start point (minimal occurrence and possible minimal
												// occurrence)
						alphaMOs, // represent the end point (minimal occurrence and possible minimal occurrnece)
						alphaPreviousUtility, alphaUtilityList);

				// check the memory usage
				MemoryLogger.getInstance().checkMemory();

			} else {
				upperBoundPruningCount++;
			}

		}
		// close the output file if the result was saved to a file
		if (writer != null) {
			writer.close();
		}
		// record the execution end time
		endTimestamp = System.currentTimeMillis();

		// check the memory usage
		MemoryLogger.getInstance().checkMemory();

		// return the result (if saved to memory)
		return huepisodes;
	}

	/**
	 * scan the database once to find the high utility 1-episosdes and calculate
	 * their EWUs (or tighter upper-bound named ERUs 'Episode Remaining Utility')
	 * and their minimal occurrences
	 */
	private void scanDatabaseToFindHighUtilitySingleEpisodes(String inputFile) throws IOException {
		// read file
		@SuppressWarnings("resource")
		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		String line;

		int lineNumber = 0;
		while ((line = reader.readLine()) != null) {

			lineNumber++;

			// if the line is a comment, is empty or is a
			// kind of metadata
			if (line.isEmpty() || line.charAt(0) == '#' || line.charAt(0) == '%' || line.charAt(0) == '@') {
				continue;
			}

			String[] lineSplited = line.split(":"); // events:totalUtility:eventsUtility
			String[] events = lineSplited[0].split(" ");
			int totalUtility = Integer.parseInt(lineSplited[1]);
			String[] eventsUtility = lineSplited[2].split(" ");

			for (int i = 0; i < events.length; i++) {
				// convert the event to Integer
				Integer event = Integer.parseInt(events[i]);

				// convert the utility of event to Integer
				Integer eventUtility = Integer.parseInt(eventsUtility[i]);

				this.complexSequence.add(lineNumber, event, eventUtility);

				if (!this.mapSingleCandidatesWithMoListAndUtilityList.containsKey(event)) {
					this.mapSingleCandidatesWithMoListAndUtilityList.put(event, new MoListUtilityList());
				}
				// save current timepoint and utility of the event to the map
				this.mapSingleCandidatesWithMoListAndUtilityList.get(event).add(lineNumber, eventUtility);

			}
			this.sequenceUtility += totalUtility;
			this.complexSequence.setTotalUtility(lineNumber, totalUtility);
		}
		// set the largest TID to sequence
		this.largestTID = lineNumber;

		this.complexSequence.setLargestTID(this.largestTID);

		/* get the absolute minimal utility */
		this.minUtilityAbsolute = this.sequenceUtility * this.minUtilityRatio;

	}

	/**
	 * Build the coocurrence utility matrix
	 * 
	 * @throws IOException if error reading/writing to a file
	 */
	private void buildCoocUtilityMatrix() throws IOException {

		this.coocMapAfter = new HashMap<>();
		this.coocMapEquals = new HashMap<>();

		for (int TID = 1; TID <= this.largestTID; TID++) {
			List<int[]> pairs = this.complexSequence.getEventSetAndItsUtilityByTID(TID);

			Set<Integer> alreadyProcessedEquals = new HashSet<Integer>();

			for (int[] pair : pairs) {
				int itemI = pair[0];

				// I- process:
				int utilityEquals = this.complexSequence.getTotalUtilityOfDuration(TID - maxDuration + 1,
						TID + maxDuration - 1);

				// ( itemJ, itemI ) is a 2-tuple, and the order of itemJ smaller than the order
				// of itemI
				for (int itemJ : alreadyProcessedEquals) {
					Map<Integer, Integer> map = coocMapEquals.get(itemJ);
					if (map == null) {
						map = new HashMap<>();
						coocMapEquals.put(itemJ, map);
					}
					Integer utility = map.get(itemI);
					if (utility == null) {
						map.put(itemI, utilityEquals);
					} else {
						map.put(itemI, utility + utilityEquals);
					}
				}
				alreadyProcessedEquals.add(itemI);

				// S- process:
				Set<Integer> alreadyProcessedAfter = new HashSet<>();
				for (int TIDAfter = TID + 1; TIDAfter <= TID + maxDuration - 1; TIDAfter++) {
					List<int[]> pairsAfter = this.complexSequence.getEventSetAndItsUtilityByTID(TIDAfter);
					for (int[] pairAfter : pairsAfter) {
						int itemJ = pairAfter[0];

						if (alreadyProcessedAfter.contains(itemJ)) {
							// if itemJ has been processed, pass it
							continue;
						}

						// if itemJ has not been processed, itemI -> itemJ is a minimal occurrence
						int utilityAfter = this.complexSequence.getTotalUtilityOfDuration(TIDAfter - maxDuration + 1,
								TID + maxDuration - 1);

						Map<Integer, Integer> map = coocMapAfter.get(itemI);
						if (map == null) {
							map = new HashMap<>();
							coocMapAfter.put(itemI, map);
						}
						Integer utility = map.get(itemJ);
						if (utility == null) {
							map.put(itemJ, utilityAfter);
						} else {
							map.put(itemJ, utility + utilityAfter);
						}
						alreadyProcessedAfter.add(itemJ);
					}
				}
			}
		}
		// ############# show the matrix ############
		// only for testing
		if (showMatrix) {
			BufferedWriter out = new BufferedWriter(new FileWriter(outputMatrixPath));
			for (int itemI : coocMapEquals.keySet()) {
				Map<Integer, Integer> map = coocMapEquals.get(itemI);
				for (int itemJ : map.keySet()) {
					int awu = map.get(itemJ);
					out.write(itemI + " , " + itemJ + "  #AWU: " + awu);
					out.newLine();
				}
			}

			for (int itemI : coocMapAfter.keySet()) {
				Map<Integer, Integer> map = coocMapAfter.get(itemI);
				for (int itemJ : map.keySet()) {
					int awu = map.get(itemJ);
					out.write(itemI + " -> " + itemJ + "  #AWU: " + awu);
					out.newLine();
				}
			}
			out.close();
		}

	}

	/**
	 * Mine simultaneous and paralllel high utility episodes
	 * 
	 * @param alphaEpisode             an episode alpha
	 * @param alphaStartPoints         the start points of episode alpha
	 * @param alphaEndPoints           the end points of episode alpha
	 * @param alphaPreviousUtilityList the previous utility of episode alpha
	 * @param alphaUtilityList         the utility list of alpha
	 * @throws IOException if error reading/writing to a file
	 */
	private void mineHUE(List<int[]> alphaEpisode, List<Integer> alphaStartPoints, List<Integer> alphaEndPoints,
			List<Integer> alphaPreviousUtilityList, List<Integer> alphaUtilityList) throws IOException {
		this.candidateCount++;

		mineSimultaneousHUE(alphaEpisode, alphaStartPoints, alphaEndPoints, alphaPreviousUtilityList, alphaUtilityList);
		mineSerialHUE(alphaEpisode, alphaStartPoints, alphaEndPoints, alphaPreviousUtilityList, alphaUtilityList);
	}

	/**
	 * Mine simultaneous high utility episodes
	 * 
	 * @param alphaEpisode             an episode alpha
	 * @param alphaStartPoints         the start points of episode alpha
	 * @param alphaEndPoints           the end points of episode alpha
	 * @param alphaPreviousUtilityList the previous utility of episode alpha
	 * @param alphaUtilityList         the utility list of alpha
	 * @throws IOException if error reading/writing to a file
	 */
	@SuppressWarnings("serial")
	private void mineSimultaneousHUE(List<int[]> alphaEpisode, List<Integer> alphaStartPoints,
			List<Integer> alphaEndPoints, List<Integer> alphaPreviousUtilityList, List<Integer> alphaUtilityList)
			throws IOException {

		// key: event name
		// value : List<List<>>, the size of the outside List (List<List>) is 4, it
		// means |List<List<>>| = 4
		// And first inside List<> represent betaStartPoints
		// Second inside List<> represent betaEndPoints
		// Third inside List<> represent betaPreviousUtilityList
		// Forth inside List<> represent betaUtilityList
		Map<Integer, List<List<Integer>>> mapBetaWithInfoList = new HashMap<>();

		int[] lastItemset = alphaEpisode.get(alphaEpisode.size() - 1);
		int lastItem = lastItemset[lastItemset.length - 1];

		Set<Integer> pruningSet = new HashSet<>();

		// for each occurrence of alpha episode
		for (int i = 0; i < alphaEndPoints.size(); i++) {
			int startPoint = alphaStartPoints.get(i);
			int endPoint = alphaEndPoints.get(i);
			int alphaPreviousUtility = alphaPreviousUtilityList.get(i);
			int alphaUtility = alphaUtilityList.get(i);

			// the int[] is int[2] ,that means int[0] represent the event, int[1] represent
			// the utility of the adding Item
			List<int[]> pairsForIextention = this.complexSequence.getPairsForIextension(endPoint, lastItem);

			loop1: for (int j = 0; j < pairsForIextention.size(); j++) {
				int beta = pairsForIextention.get(j)[0];
				int betaUtility = alphaUtility + pairsForIextention.get(j)[1];

				if (useCoocMatrix) {
					if (pruningSet.contains(beta)) {
						continue loop1;
					}
					// pruning I-extention by using coocMatrix
					loop2: for (int itemI : lastItemset) {
						Map<Integer, Integer> mapUtilityItemsEquals = coocMapEquals.get(itemI);
						if (mapUtilityItemsEquals == null) {
							continue loop2;
						} else {
							Integer utilityEquals = mapUtilityItemsEquals.get(beta);
							if (utilityEquals == null || utilityEquals < this.minUtilityAbsolute) {
								pruningSet.add(beta);
								continue loop1;
							}
						}
					}
					// pruing prefix of the episode
					if (pruningPrefix) {
						for (int size = 0; size < alphaEpisode.size() - 1; size++) {
							int[] itemset = alphaEpisode.get(size);
							loop3: for (int itemI : itemset) {
								Map<Integer, Integer> mapUtilityItemsAfter = coocMapAfter.get(itemI);
								if (mapUtilityItemsAfter == null) {
									continue loop3;
								} else {
									Integer utilityAfter = mapUtilityItemsAfter.get(beta);
									if (utilityAfter == null || utilityAfter < this.minUtilityAbsolute) {
										pruningSet.add(beta);
										continue loop1;
									}
								}
							}
						}
					}
				}

				if (!mapBetaWithInfoList.containsKey(beta)) {

					mapBetaWithInfoList.put(beta, new ArrayList<List<Integer>>() {
						{
							add(new ArrayList<>());
							add(new ArrayList<>());
							add(new ArrayList<>());
							add(new ArrayList<>());
						}
					});
				}

				mapBetaWithInfoList.get(beta).get(0).add(startPoint); // betaStartPoints
				mapBetaWithInfoList.get(beta).get(1).add(endPoint); // betaEndPoints, different from MiningSimultHUE
				mapBetaWithInfoList.get(beta).get(2).add(alphaPreviousUtility); // betaPreviousUtilityList, different
																				// from MiningSimultHUE
				mapBetaWithInfoList.get(beta).get(3).add(betaUtility); // betaUtilityList

			}
		}

		this.matrixPruningCount += pruningSet.size();
		this.combinatedEpisodeCount += pruningSet.size();

		for (int beta : mapBetaWithInfoList.keySet()) {
			combinatedEpisodeCount++;

			List<Integer> betaStartPoints = mapBetaWithInfoList.get(beta).get(0);
			List<Integer> betaEndPoints = mapBetaWithInfoList.get(beta).get(1);
			List<Integer> betaPreviousUtilityList = mapBetaWithInfoList.get(beta).get(2);
			List<Integer> betaUtilityList = mapBetaWithInfoList.get(beta).get(3);

			int[] totalUtilityUpperBoundUtilityPairOfMOs = calculateUtilityAndUpperBoundOfMOs(beta, betaStartPoints,
					betaEndPoints, betaPreviousUtilityList, betaUtilityList);
			int totalUtility = totalUtilityUpperBoundUtilityPairOfMOs[0];
			int upperBoundUtility = totalUtilityUpperBoundUtilityPairOfMOs[1];

			if (upperBoundUtility >= this.minUtilityAbsolute) {
				int[] newLastItemset = new int[lastItemset.length + 1];
				System.arraycopy(lastItemset, 0, newLastItemset, 0, lastItemset.length);
				newLastItemset[lastItemset.length] = beta;
				List<int[]> betaEpisode = new ArrayList<int[]>(alphaEpisode.subList(0, alphaEpisode.size() - 1));
				betaEpisode.add(newLastItemset);

				if (totalUtility >= this.minUtilityAbsolute) {
					HighUtilityEpisode hue = new HighUtilityEpisode(betaEpisode, totalUtility);
					save(hue, hue.getSize());
				}

				mineHUE(betaEpisode, betaStartPoints, betaEndPoints, betaPreviousUtilityList, betaUtilityList);

			} else {
				upperBoundPruningCount++;
			}
		}
		// check the memory usage
		MemoryLogger.getInstance().checkMemory();
	}

	/**
	 * Mining serial high utility episodes
	 * 
	 * @param alphaEpisode             an episode alpha
	 * @param alphaStartPoints         the start points of episode alpha
	 * @param alphaEndPoints           the end points of episode alpha
	 * @param alphaPreviousUtilityList the previous utility of episode alpha
	 * @param alphaUtilityList         the utility list of alpha
	 * @throws IOException if error while reading or writing a file
	 */
	@SuppressWarnings("serial")
	private void mineSerialHUE(List<int[]> alphaEpisode, List<Integer> alphaStartPoints, List<Integer> alphaEndPoints,
			List<Integer> alphaPreviousUtilityList, List<Integer> alphaUtilityList) throws IOException {
		// key: event name
		// value : List<List<>>, the size of the outside List (List<List>) is 4, it
		// means |List<List<>>| = 4
		// And first inside List<> represent betaStartPoints
		// Second inside List<> represent betaEndPoints
		// Third inside List<> represent betaPreviousUtilityList
		// Forth inside List<> represent betaUtilityList
		Map<Integer, List<List<Integer>>> mapBetaWithInfoList = new HashMap<>();

		int[] lastItemset = alphaEpisode.get(alphaEpisode.size() - 1);
		Set<Integer> pruningSet = new HashSet<>();

		// record the previous start point
		int previousStartPoint = -1;

		// for each occurrence of alpha episode of having same start point
		for (int i = 0; i < alphaEndPoints.size(); i++) {

			int startPoint = alphaStartPoints.get(i);

			if (startPoint != previousStartPoint) {
				// if having different start point, it meets the minimal occurrence
				previousStartPoint = startPoint;
				int endPoint = alphaEndPoints.get(i);
//				int alphaPreviousUtility = alphaPreviousUtilityList.get(i);
				int alphaUtility = alphaUtilityList.get(i);

				// cannot exceed the endPoint of the next minimal occurrence , also cannot
				// exceed the maxDuration
				int j = i + 1;
				for (; j < alphaEndPoints.size(); j++) {
					if (alphaStartPoints.get(j) != startPoint) {
						break;
					}
				}
				int endPointOfnextMO = this.largestTID;
				if (j < alphaEndPoints.size()) {
					// get the end point of next minimal occurrence
					endPointOfnextMO = alphaEndPoints.get(j);
				}

				// get the maximal point that can do the s-extentions from this start point
				int extentionBound = endPointOfnextMO < startPoint + this.maxDuration - 1 ? endPointOfnextMO
						: startPoint + this.maxDuration - 1;

				if (!useTighterUpperBound && !useCoocMatrix) {
					// in the UP_Span, they will only consider [startPoint, this.maxDuration-1]
					extentionBound = startPoint + this.maxDuration - 1;
				}

				int maximalAlphaUtility = alphaUtility;
				for (int TID = endPoint + 1; TID <= extentionBound; TID++) {
					loop1: for (int[] pair : this.complexSequence.getEventSetAndItsUtilityByTID(TID)) {
						int beta = pair[0];

						if (checkMaximumUtility && alphaEpisode.size() >= 2) {
							// the alphaUtility (betaPreviousUtility) can have different utility, we need
							// keep the maximal
							maximalAlphaUtility = this.complexSequence.getMaximalUtility(alphaEpisode, startPoint,
									TID - 1);
						}
						int betaUtility = maximalAlphaUtility + pair[1];

						if (useCoocMatrix) {
							if (pruningSet.contains(beta)) {
								continue loop1;
							}
							// pruning S-extention by using coocMatrix
							loop2: for (int itemI : lastItemset) {
								Map<Integer, Integer> mapUtilityItemsAfter = coocMapAfter.get(itemI);
								if (mapUtilityItemsAfter == null) {
									continue loop2;
								} else {
									Integer utilityAfter = mapUtilityItemsAfter.get(beta);
									if (utilityAfter == null || utilityAfter < this.minUtilityAbsolute) {
										pruningSet.add(beta);
										continue loop1;
									}
								}
							}
							// pruing prefix of the episode
							if (pruningPrefix) {

								for (int size = 0; size < alphaEpisode.size() - 1; size++) {
									int[] itemset = alphaEpisode.get(size);
									loop3: for (int itemI : itemset) {
										Map<Integer, Integer> mapUtilityItemsAfter = coocMapAfter.get(itemI);
										if (mapUtilityItemsAfter == null) {
											continue loop3;
										} else {
											Integer utilityAfter = mapUtilityItemsAfter.get(beta);
											if (utilityAfter == null || utilityAfter < this.minUtilityAbsolute) {
												pruningSet.add(beta);
												continue loop1;
											}
										}
									}
								}
							}
						}

						if (!mapBetaWithInfoList.containsKey(beta)) {

							if (maximalAlphaUtility > alphaUtility) {
								// if the maximal utility is larger than alphaUtility
								// it means this episode is not maximal utility episode
								// the super episode of this type episode, we donot take into account
								this.episodeWithNonMaxUtilityCount++;
							}

							mapBetaWithInfoList.put(beta, new ArrayList<List<Integer>>() {
								{
									add(new ArrayList<>());
									add(new ArrayList<>());
									add(new ArrayList<>());
									add(new ArrayList<>());
								}
							});
						}

						mapBetaWithInfoList.get(beta).get(0).add(startPoint); // betaStartPoints
						mapBetaWithInfoList.get(beta).get(1).add(TID); // betaEndPoints, different from MiningSimultHUE
						mapBetaWithInfoList.get(beta).get(2).add(alphaUtility); // betaPreviousUtilityList, different
																				// from MiningSimultHUE
						mapBetaWithInfoList.get(beta).get(3).add(betaUtility); // betaUtilityList

					}
				}
			}
		}

		this.matrixPruningCount += pruningSet.size();
		this.combinatedEpisodeCount += pruningSet.size();

		for (int beta : mapBetaWithInfoList.keySet()) {
			combinatedEpisodeCount++;

			List<Integer> betaStartPoints = mapBetaWithInfoList.get(beta).get(0);
			List<Integer> betaEndPoints = mapBetaWithInfoList.get(beta).get(1);
			List<Integer> betaPreviousUtilityList = mapBetaWithInfoList.get(beta).get(2);
			List<Integer> betaUtilityList = mapBetaWithInfoList.get(beta).get(3);

			int[] totalUtilityUpperBoundUtilityPairOfMOs = calculateUtilityAndUpperBoundOfMOs(beta, betaStartPoints,
					betaEndPoints, betaPreviousUtilityList, betaUtilityList);
			int totalUtility = totalUtilityUpperBoundUtilityPairOfMOs[0];
			int upperBoundUtility = totalUtilityUpperBoundUtilityPairOfMOs[1];

			if (upperBoundUtility >= this.minUtilityAbsolute) {

				List<int[]> betaEpisode = new ArrayList<int[]>(alphaEpisode.subList(0, alphaEpisode.size()));
				betaEpisode.add(new int[] { beta });

				if (totalUtility >= this.minUtilityAbsolute) {
					HighUtilityEpisode hue = new HighUtilityEpisode(betaEpisode, totalUtility);
					save(hue, hue.getSize());
				}

				mineHUE(betaEpisode, betaStartPoints, betaEndPoints, betaPreviousUtilityList, betaUtilityList);

			} else {
				upperBoundPruningCount++;
			}
		}
		// check the memory usage
		MemoryLogger.getInstance().checkMemory();
	}

	/**
	 * This method does three tasks: 1. get the minimal occurrence of an episode 2.
	 * calculate the total utility of the episode at these minimal occurreces 3.
	 * calculate the upper bound on the utility of the episode at these minimal
	 * occurrences.
	 * 
	 * @param betaStartPoints         the start points of an episode beta
	 * @param betaEndPoints           the end points of an episode beta
	 * @param betaPreviousUtilityList the previous utility list of beta
	 * @param betaUtilityList         the utility list of beta
	 * @return an array containing the total utility and an upper-bound value
	 */
	private int[] calculateUtilityAndUpperBoundOfMOs(int beta, List<Integer> betaStartPoints,
			List<Integer> betaEndPoints, List<Integer> betaPreviousUtilityList, List<Integer> betaUtilityList) {
		// The betaStartPoints and betaEndPoints contains the minimal occurrences and
		// the possible minimal occurrences
		// The possible minimal occurrence defined as follow:
		// -- it contains a minimal occurrence and it's start point = the start point of
		// minimal occurrence
		// So, we need calculate the total utility and upper bound utility of the
		// episode
		// at these minimal occurrences
		// And we can choose the first different value in the betaStartPoint, because
		// with same start point,
		// the corresponding endPoints will be increasing sequence in the
		// 'betaEndPoints', because we keep the order by S-extensions

//        System.out.print(betaStartPoints.size()+" "+betaEndPoints.size());
		if (!useTighterUpperBound && !useCoocMatrix) {
			// In UP_Span, it needs to repair the minimal occurrence (Repair_moSet), to get
			// the total utility and upper bound
			return repaiMOSet(betaStartPoints, betaEndPoints, betaPreviousUtilityList, betaUtilityList);
		}
//        System.out.println(" : "+betaStartPoints.size()+" "+betaEndPoints.size());

		int totalUtility = 0;
		int upperBound = 0;

		int previousStartPoint = -1;
		for (int pos = 0; pos < betaStartPoints.size(); pos++) {
			int startPoint = betaStartPoints.get(pos);
			if (startPoint != previousStartPoint) {
				previousStartPoint = startPoint;
				// it means, this startPoint is the first different point in the 'betaStartPoint
				// and [startPoints[pos], endPoints[pos]] is a minimal occurrence
				totalUtility += betaUtilityList.get(pos);

				int endPoint = betaEndPoints.get(pos);

				if (this.useTighterUpperBound) {
					upperBound += betaUtilityList.get(pos) + this.complexSequence.getIrutil(endPoint, beta)
							+ this.complexSequence.getTotalUtilityOfDuration(endPoint + 1,
									startPoint + this.maxDuration - 1);
				} else {
					// previous utility + the remaning utility form endPoint to
					// startPoint+maxDuration-1 (contains)
					upperBound += betaPreviousUtilityList.get(pos) + this.complexSequence
							.getTotalUtilityOfDuration(endPoint, startPoint + this.maxDuration - 1);
				}
			}
		}
		return new int[] { totalUtility, upperBound };
	}

	/**
	 * Repair a minimal occurrence set
	 * 
	 * @param betaStartPoints         the start points of an episode beta
	 * @param betaEndPoints           the end points of an episode beta
	 * @param betaPreviousUtilityList the previous utility list of beta
	 * @param betaUtilityList         the utility list of beta
	 * @return an array containing the total utility and an upper-bound value
	 */
	public int[] repaiMOSet(List<Integer> betaStartPoints, List<Integer> betaEndPoints,
			List<Integer> betaPreviousUtilityList, List<Integer> betaUtilityList) {

		Map<Integer, Integer> mapStartEnd = new HashMap<>();
		Map<Integer, Integer> mapEndStart = new HashMap<>();

		for (int pos = 0; pos < betaStartPoints.size(); pos++) {
			int startPoint = betaStartPoints.get(pos);
			int endPoint = betaEndPoints.get(pos);

			if (mapStartEnd.containsKey(startPoint)) {
				int existEndPoint = mapStartEnd.get(startPoint);
				if (endPoint < existEndPoint) {
					mapStartEnd.put(startPoint, endPoint);

					mapEndStart.remove(existEndPoint);
					mapEndStart.put(endPoint, startPoint);
				}
			} else {
				mapStartEnd.put(startPoint, endPoint);
				if (mapEndStart.containsKey(endPoint)) {
					int existStartPoint = mapEndStart.get(endPoint);
					if (startPoint > existStartPoint) {
						mapEndStart.put(endPoint, startPoint);

						mapStartEnd.remove(existStartPoint);
						mapStartEnd.put(startPoint, endPoint);
					}
				} else {
					mapEndStart.put(endPoint, startPoint);
				}
			}
		}

		int totalUtility = 0;
		int upperBound = 0;
		for (int pos = 0; pos < betaStartPoints.size(); pos++) {
			int startPoint = betaStartPoints.get(pos);
			int endPoint = betaEndPoints.get(pos);

			if (mapStartEnd.containsKey(startPoint) && mapStartEnd.get(startPoint) == endPoint) {
				totalUtility += betaUtilityList.get(pos);
				upperBound += betaPreviousUtilityList.get(pos)
						+ this.complexSequence.getTotalUtilityOfDuration(endPoint, startPoint + this.maxDuration - 1);
			}
		}
		return new int[] { totalUtility, upperBound };

	}

	/**
	 * Save a high utility episodes to file or in memory.
	 * 
	 * @param hue the high utility episode
	 * @param k   the length of the episode
	 * @throws IOException if an error occur while writing to a file
	 */
	public void save(HighUtilityEpisode hue, int k) throws IOException {
//        System.out.println(HUE.toString());
		this.hueCount++;
		// if the result should be saved to a file
		if (writer != null) {

			// write to file and create a new line
			writer.write(hue.toString());
			writer.newLine();
		} else {
			this.huepisodes.addHighUtilityEpisode(hue, k);
		}
	}

	/**
	 * Print statistics about the algorithm execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  HUE_Span V_1.0  - STATS ===============");
		long temps = endTimestamp - startTimestamp;
		System.out.println(" The minimum utility absolue: " + this.minUtilityAbsolute);
		System.out.print(" Max memory usage: " + MemoryLogger.getInstance().getMaxMemory() + " mb \n");
		System.out.println(" Episodes counts : " + this.hueCount);
		System.out.println(" Candidate counts : " + this.candidateCount);
		System.out.println(" Combinated episode counts : " + this.combinatedEpisodeCount);
		System.out.println(" matrix pruning counts : " + this.matrixPruningCount);
		System.out.println(" upper Bound pruning counts: " + this.upperBoundPruningCount);
		System.out.println(" non maximal combinated episode counts atleast: " + this.episodeWithNonMaxUtilityCount);
		System.out.println(" Total time ~ " + temps + " ms");
		System.out.println("===================================================");
	}

	/**
	 * implements a class contains moList and utility list of single candidates
	 * (items)
	 */
	public class MoListUtilityList {
		// minimal occurrence list
		List<Integer> moList;
		// utility list
		List<Integer> utilityList;

		/**
		 * Constructor
		 */
		public MoListUtilityList() {
			this.moList = new ArrayList<>();
			this.utilityList = new ArrayList<>();
		}

		/**
		 * Constructor
		 * 
		 * @param moList      a moList
		 * @param utilityList a utility list
		 */
		public MoListUtilityList(List<Integer> moList, List<Integer> utilityList) {
			this.moList = moList;
			this.utilityList = utilityList;
		}

		/**
		 * Add a minimal occurrence and its utility
		 * 
		 * @param mo      a minimal occurrence
		 * @param utility its utility
		 */
		public void add(int mo, int utility) {
			this.moList.add(mo);
			this.utilityList.add(utility);
		}

		/**
		 * Get the minimum occurrence list
		 * 
		 * @return the minimum occurrence list
		 */
		public List<Integer> getMoList() {
			return moList;
		}

		/**
		 * Get the utility list
		 * 
		 * @return the utility list
		 */
		public List<Integer> getUtilityList() {
			return utilityList;
		}
	}

}
