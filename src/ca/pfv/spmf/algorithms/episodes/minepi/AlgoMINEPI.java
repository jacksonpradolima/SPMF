package ca.pfv.spmf.algorithms.episodes.minepi;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * Copyright Peng Yang  2019
 */

/**
 * This is a implementation of the MINEPI algorithm. MINEPI was proposed by
 * MANNILA et al. 1997 in this paper: <br/>
 * <br/>
 * 
 * Mannila H, Toivonen H, Verkamo AI (1997) Discovery of frequent episodes in
 * event sequences. Data Mining Knowl Discov 1(3):259¨C289
 * 
 * <br/>
 * <br/>
 * This implementation only for mining the serial episode based on minimal
 * occurrences(windows)
 * 
 * @author Peng yang
 */
public class AlgoMINEPI {

	/** current level */
	private int k = 0;

	/** start time of the latest execution */
	private long startTimestamp;

	/** end time of the latest execution */
	private long endTimestamp;

	/** candidate count */
	private int candidateCount = 0;

	/** a sequence database to store all events */
	private List<Event> sequence;

	/**
	 * whether the timestamps need self increment as step of 1 for each transcation
	 */
	private boolean selfIncrement;

	/**
	 * The patterns that are found // (if the user want to keep them into memory)
	 */
	private FrequentEpisodes frequentEpisodes = null;

	/** minimum support threshold */
	private int minSupport;
	
	/** max window size */
	private int maxWindow;


	/**
	 * Constructor
	 */
	public AlgoMINEPI() {
		// empty
	}

	/**
	 * Method to run MINEPI algorithm
	 * 
	 * @param input          a sequence
	 * @param output         the path of the output file to save the result or null
	 *                       if you want the result to be saved into memory
	 * @param minSupport     the minimum support threshold
	 * @param maxWindow      the maximum window size
	 * @param selfIncrement if true, it means that there is no timestamps and
	 *                       timestamps will be assigned as 1,2,3... to
	 *                       transactions. Otherwise, timestamps are used.
	 * @return the frequent episodes
	 * @throws IOException if error while reading/writing to file
	 */
	public FrequentEpisodes runAlgorithm(String input, String output, int minSupport, int maxWindow,
			boolean selfIncrement) throws IOException {
		// reset maximum
		MemoryLogger.getInstance().reset();

		this.minSupport = minSupport;
		this.maxWindow = maxWindow;
		this.selfIncrement = selfIncrement;

		startTimestamp = System.currentTimeMillis();

		sequence = new ArrayList<>();

		frequentEpisodes = new FrequentEpisodes();

		// scan the file to the menory (sequence) and determine the frequent 1-episodes
		// in the level1

		scanDatabaseToDetermineFrequentSingleEpisode(input);

		// using level 1 to generate candidates of size 2
		this.k++;

		// generate candidates of having size 2
		Candidates candidates = frequentEpisodes.genCandidateByLevel(k);

		while (candidates != null && !candidates.isEmpty()) {
			candidateCount += candidates.getCandidateCount();
			k++;
			candidates.getFrequentKepisodes(this.sequence, this.minSupport, this.maxWindow, this.frequentEpisodes);
			candidates = frequentEpisodes.genCandidateByLevel(k);
		}

		// record end time
		endTimestamp = System.currentTimeMillis();
		// check the memory usage
		MemoryLogger.getInstance().checkMemory();

		if (output != null) {
			this.frequentEpisodes.out2file(output);
		}

		return frequentEpisodes;

	}

	/**
	 * Method to get the frequent episodes of size 1
	 * 
	 * @param input a path to an input sequence
	 * @throws IOException if error while reading the input file
	 */
	private void scanDatabaseToDetermineFrequentSingleEpisode(String input) throws IOException {
		// read file
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;

		// key: 1-Episode, value: support
		Map<Integer, Integer> mapSingleEventCount = new HashMap<>();

		if (selfIncrement) {
			int currentTID = 0;
			while (((line = reader.readLine()) != null)) {

				currentTID++;
				// if the line is a comment, is empty or is a
				// kind of metadata
				if (line.isEmpty() || line.charAt(0) == '#' || line.charAt(0) == '%' || line.charAt(0) == '@') {
					continue;
				}
				Event simulEvent = new Event();
				String[] lineSplited = line.split(" ");

				for (String itemString : lineSplited) {
					Integer itemName = Integer.parseInt(itemString);
					simulEvent.addEvent(itemName);

					Integer count = mapSingleEventCount.get(itemName);
					if (count == null) {
						mapSingleEventCount.put(itemName, 1);
						candidateCount++;
					} else {
						mapSingleEventCount.put(itemName, ++count);
					}

				}

				simulEvent.setTime(currentTID);

				this.sequence.add(simulEvent);
			}
		} else {
			//// the timestamp exist in file
			int currentTID = 1;

			while (((line = reader.readLine()) != null)) {
				if (line.isEmpty() || line.charAt(0) == '#' || line.charAt(0) == '%' || line.charAt(0) == '@') {
					continue;
				}
				Event simulEvent = new Event();

				String[] lineSplited = line.split("\\|");

				String[] lineItems = lineSplited[0].split(" ");
				for (String itemString : lineItems) {
					Integer itemName = Integer.parseInt(itemString);
					simulEvent.addEvent(itemName);

					Integer count = mapSingleEventCount.get(itemName);
					if (count == null) {
						mapSingleEventCount.put(itemName, 1);
						candidateCount++;
					} else {
						mapSingleEventCount.put(itemName, ++count);
					}
				}
				currentTID = Integer.parseInt(lineSplited[1]);
				simulEvent.setTime(currentTID);

				sequence.add(simulEvent);
			}
		}
		
		reader.close();

		for (Map.Entry<Integer, Integer> entry : mapSingleEventCount.entrySet()) {
			if (entry.getValue() >= minSupport) {
				// save frequent 1-episodes
				Episode episode = new Episode(new int[] { entry.getKey() }, entry.getValue());
				this.frequentEpisodes.addFrequentEpisode(episode, 1);
			}
		}

		frequentEpisodes.initFirstLevelBlockStart();
	}

	/**
	 * Print statistics about the algorithm execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  MINEPI - STATS =============");
		System.out.println(" Candidates count : " + candidateCount);
		System.out.println(" The algorithm stopped at size : " + k);
		System.out.println(" Frequent episodes count : " + this.frequentEpisodes.getFrequentEpisodesCount());
		System.out.println(" Maximum memory usage : " + MemoryLogger.getInstance().getMaxMemory() + " mb");
		System.out.println(" Total time ~ : " + (endTimestamp - startTimestamp) + " ms");
		System.out.println("===================================================");
	}

}
