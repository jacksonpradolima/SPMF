package ca.pfv.spmf.algorithms.episodes.minepiplus;

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
 * This is a implementation of the MINEPI+ algorithm. MINEPI+ was proposed by
 * Huang et al. 2008 in this paper <br/>
 * <br/>
 * 
 * Kuo-Yu Huang,Chia-Hui Chang (2008):Efficient mining of frequent episodes from
 * complex sequences. Inf. Syst.33(1):96-114
 * 
 * <br/>
 * <br/>
 * This algorithm is implemented for mining episodes in a complex sequence The
 * calculation of frequency base on the head frequency the head frequency
 * statisfy anti-monotone
 * 
 * @author Peng Yang
 */
public class AlgoMINEPIPlus {

	/** start time of the latest execution */
	private long startTimestamp;

	/** end time of the latest execution */
	private long endTimestamp;

	/** candidate count */
	private int candidateCount = 0;

	/**
	 * Set this to true if the dataset don't have timestamps. Then timestamps will
	 * be given automatically to transactions by self-increment, that is 1, 2, 3 ...
	 * If false, the timestamps from the dataset will be used instead.
	 */
	private boolean selfIncrement;

	/**
	 * The patterns that are found (if the user want to keep them into memory)
	 */
	protected FrequentEpisodes freEpisodes = null;

	/** save the frequent episodes of size 1 */
	private List<Episode> f1;

	/** save the boundlist of frequent episodes of size 1 */
	private List<List<int[]>> f1BoundList;

	/** the minimum support threshold */
	private int minSupport;

	/** the maximum window threshold */
	private int maxWindow;

	/**
	 * Constructor
	 */
	public AlgoMINEPIPlus() {

	}

	/**
	 * Method to run MINEPI+ algorithm
	 * 
	 * @param input         a sequence
	 * @param output        the path of the output file to save the result or null
	 *                      if you want the result to be saved into memory
	 * @param minSupport    the minimum support threshold
	 * @param maxWindow     the maximum window size
	 * @param selfIncrement Set this to true if the dataset don't have timestamps.
	 *                      Then timestamps will be given automatically to
	 *                      transactions by self-increment, that is 1, 2, 3 ... If
	 *                      false, the timestamps from the dataset will be used
	 *                      instead.
	 * @return the set of frequent episodes
	 * @throws IOException if error while reading or writing files
	 */
	public FrequentEpisodes runAlgorithm(String input, String output, int minSupport, int maxWindow,
			boolean selfIncrement) throws IOException {
		// reset maximum
		MemoryLogger.getInstance().reset();

		this.minSupport = minSupport;
		this.maxWindow = maxWindow;
		this.selfIncrement = selfIncrement;

		startTimestamp = System.currentTimeMillis();

		this.freEpisodes = new FrequentEpisodes();

		// scan the file to the menory (sequence) and determine the frequent 1-episodes
		// in the level1
		scanDatabaseToDetermineFrequentSingleEpisode(input);

		for (int i = 0; i < f1.size(); i++) {
			// the episode contains the boundlist.
			serialJoins(f1.get(i), f1BoundList.get(i), f1.get(i).getLastItem(), 1);
		}

		// record end time
		endTimestamp = System.currentTimeMillis();
		// check the memory usage
		MemoryLogger.getInstance().checkMemory();

		if (output != null) {
			this.freEpisodes.saveToFile(output);
		}
		return freEpisodes;
	}

	public void serialJoins(Episode alpha, List<int[]> alphaBoundlist, int lastItem, int levelNum) {

		for (int j = 0; j < f1.size(); j++) {
			Episode fj = f1.get(j);

			if (fj.getLastItem() > lastItem) {
				List<int[]> tempBoundlist = equalJoin(alphaBoundlist, f1BoundList.get(j));
				int support = getEntityCount(tempBoundlist);
				if (support >= minSupport) {
					Episode beta = alpha.iExtension(fj.getLastItem(), support);
					this.freEpisodes.addFrequentEpisode(beta, levelNum);
					serialJoins(beta, tempBoundlist, fj.getLastItem(), levelNum);
				}
			}
			List<int[]> tempBoundlist = temporalJoin(alphaBoundlist, f1BoundList.get(j));
			int support = getEntityCount(tempBoundlist);
			if (support >= minSupport) {
				Episode beta = alpha.sExtension(fj.getLastItem(), support);
				this.freEpisodes.addFrequentEpisode(beta, levelNum + 1);
				serialJoins(beta, tempBoundlist, fj.getLastItem(), levelNum + 1);
			}
		}
	}

	/**
	 * Get the entity count of a bound list
	 * 
	 * @param tempBoundlist the bound list
	 * @return the entity count (support)
	 */
	private int getEntityCount(List<int[]> tempBoundlist) {
		if (tempBoundlist.size() <= 0) {
			return 0;
		}
		int support = 1;
		int lastStarTime = tempBoundlist.get(0)[0];
		for (int i = 0; i < tempBoundlist.size(); i++) {
			if (lastStarTime != tempBoundlist.get(i)[0]) {
				support++;
				lastStarTime = tempBoundlist.get(i)[0];
			}
		}
		return support;
	}

	/**
	 * Perform a temporal join In the temporalJoin, we only need record the all
	 * position that meeet the conditions within the maxWindow
	 * 
	 * @param alphaBoundlist the bound list of an episode alpha
	 * @param fjBoundlist    the fj bound list
	 * @return a list of bound list
	 * 
	 */
	private List<int[]> temporalJoin(List<int[]> alphaBoundlist, List<int[]> fjBoundlist) {
		this.candidateCount++;

		List<int[]> tempBoundlist = new ArrayList<>();

		for (int i = 0; i < alphaBoundlist.size(); i++) {

			for (int j = 0; j < fjBoundlist.size(); j++) {
				if (fjBoundlist.get(j)[1] > alphaBoundlist.get(i)[1]) {
					if (fjBoundlist.get(j)[1] - alphaBoundlist.get(i)[0] >= maxWindow) {
						break;
					} else {
						int[] boundlist = new int[] { alphaBoundlist.get(i)[0], fjBoundlist.get(j)[1] };

						tempBoundlist.add(boundlist);

					}
				}
			}
		}
		return tempBoundlist;
	}

	/**
	 * Perform an equal join
	 * 
	 * @param alphaBoundlist an alpha bound list
	 * @param fjBoundlist    an fj bound list
	 * @return a bound list resulting from the join
	 */
	private List<int[]> equalJoin(List<int[]> alphaBoundlist, List<int[]> fjBoundlist) {
		this.candidateCount++;

		List<int[]> tempBoundlist = new ArrayList<>();

		for (int i = 0; i < alphaBoundlist.size(); i++) {

			for (int j = 0; j < fjBoundlist.size(); j++) {
				if (alphaBoundlist.get(i)[1] < fjBoundlist.get(j)[1]) {
					// if current alphaBound less than current singleBound, then i++
					// for singleBouldlist [1] and [0] are equal
					break;
				} else if (alphaBoundlist.get(i)[1] == fjBoundlist.get(j)[1]) {
					// if current alphaBound equal to the current singleBound, we add this bound
					tempBoundlist.add(alphaBoundlist.get(i));
					break;
				}
			}
		}
		return tempBoundlist;
	}

	/**
	 * Scan the database to determine the frequent single episodes
	 * 
	 * @param input a path to an input file
	 * @throws IOException if error while reading or writing to file
	 */
	private void scanDatabaseToDetermineFrequentSingleEpisode(String input) throws IOException {
		// read file
		@SuppressWarnings("resource")
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;

		// key: 1-Episode, value: bould list
		Map<Integer, List<int[]>> mapSingleEventCount = new HashMap<>();

		if (selfIncrement) {
			int currentTID = 0;
			while (((line = reader.readLine()) != null)) {

				currentTID++;
				// if the line is a comment, is empty or is a
				// kind of metadata
				if (line.isEmpty() || line.charAt(0) == '#' || line.charAt(0) == '%' || line.charAt(0) == '@') {
					continue;
				}

				String[] lineSplited = line.split(" ");

				for (String itemString : lineSplited) {
					Integer itemName = Integer.parseInt(itemString);

					List<int[]> bouldList = mapSingleEventCount.get(itemName);
					if (bouldList == null) {
						bouldList = new ArrayList<>();
						bouldList.add(new int[] { currentTID, currentTID });
						mapSingleEventCount.put(itemName, bouldList);
						candidateCount++;

					} else {
						bouldList.add(new int[] { currentTID, currentTID });
						mapSingleEventCount.put(itemName, bouldList);
					}

				}

			}
		} else {
			//// the timestamp exist in file
			int currentTID = 1;

			while (((line = reader.readLine()) != null)) {
				if (line.isEmpty() || line.charAt(0) == '#' || line.charAt(0) == '%' || line.charAt(0) == '@') {
					continue;
				}

				String[] lineSplited = line.split("\\|");

				String[] lineItems = lineSplited[0].split(" ");
				currentTID = Integer.parseInt(lineSplited[1]);

				for (String itemString : lineItems) {
					Integer itemName = Integer.parseInt(itemString);

					List<int[]> bouldList = mapSingleEventCount.get(itemName);
					if (bouldList == null) {
						bouldList = new ArrayList<>();
						bouldList.add(new int[] { currentTID, currentTID });
						mapSingleEventCount.put(itemName, bouldList);
						candidateCount++;
					} else {
						bouldList.add(new int[] { currentTID, currentTID });
						mapSingleEventCount.put(itemName, bouldList);
					}
				}
			}
		}

		this.freEpisodes = new FrequentEpisodes();
		this.f1 = new ArrayList<>();
		this.f1BoundList = new ArrayList<>();

		for (Map.Entry<Integer, List<int[]>> entry : mapSingleEventCount.entrySet()) {
			List<int[]> bouldList = entry.getValue();
			if (bouldList.size() >= minSupport) {
				// save frequent 1-episodes
				int[] symbol = new int[] { entry.getKey() };
				@SuppressWarnings("serial")
				List<int[]> event = new ArrayList<int[]>() {
					{
						add(symbol);
					}
				};
				Episode episode = new Episode(event, bouldList.size());

				this.freEpisodes.addFrequentEpisode(episode, 1);
				this.f1.add(episode);
				this.f1BoundList.add(bouldList);
			}
		}
	}

	/**
	 * Print statistics about the algorithm execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  MINEPI+_S (head episode) - STATS =============");
		System.out.println(" Candidates count : " + candidateCount);
		System.out.println(" The algorithm stopped at size : " + freEpisodes.getTotalLevelNum());
		System.out.println(" Frequent episodes count : " + this.freEpisodes.getFrequentEpisodesCount());
		System.out.println(" Maximum memory usage : " + MemoryLogger.getInstance().getMaxMemory() + " mb");
		System.out.println(" Total time ~ : " + (endTimestamp - startTimestamp) + " ms");
		System.out.println("===================================================");
	}

}
