package ca.pfv.spmf.algorithms.episodes.minepi;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
 * This class represents a set of frequent episodes. It is used by the MINEPI
 * algorithm.
 *
 * @see AlgoMINEPI
 * @author Peng yang
 */
public class FrequentEpisodes {

	/** Position i in "levels" contains the list of Episodes of size i */
	private final List<Level> levels = new ArrayList<Level>();

	/** the total number of episode **/
	private int episodeCount = 0;

	/**
	 * Constructor
	 */
	public FrequentEpisodes() {
		// we create an empty level 0 by default
		levels.add(new Level());

	}

	/**
	 * Add an episode of length k.
	 * 
	 * @param episode the episode
	 * @param k       the length k
	 */
	public void addFrequentEpisode(Episode episode, int k) {
		while (levels.size() <= k) {
			levels.add(new Level());
		}
		levels.get(k).addFreEpisode(episode);
		this.episodeCount++;
	}

	/**
	 * Add frequent episode and its block_start.
	 * 
	 * @param episode     an episode
	 * @param k           the length k
	 * @param block_start its block start
	 */
	public void addFrequentFpisodeAndBlockStart(Episode episode, int k, int block_start) {
		while (levels.size() <= k) {
			levels.add(new Level());
		}
		levels.get(k).addFreEpisodeAndBlockStart(episode, block_start);
		this.episodeCount++;
	}

	/**
	 * Initialize the first level.
	 */
	public void initFirstLevelBlockStart() {
		this.levels.get(1).init_firstLevel_block_start();
	}

	/**
	 * Save the frequent episodes to a file.
	 * 
	 * @param output the output file path
	 * @throws IOException if error while writing to file
	 */
	public void out2file(String output) throws IOException {
		// Create a string buffer
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));
		StringBuilder buffer = new StringBuilder();
//		int numLevel = 0;
		for (Level l : levels) {
//            buffer.append("  L" + numLevel + " \r\n");
			for (Episode episode : l.getK_freEpisodes()) {
				buffer.append(episode.toString());
				buffer.append("\r\n");
			}
//            buffer.append("\r\n");
//			numLevel++;
		}

		// write to file and create a new line
		writer.write(buffer.toString());
		writer.close();
	}

	/**
	 * Generate candidates by level.
	 * 
	 * @param k the size k
	 * @return the candidates
	 */
	public Candidates genCandidateByLevel(int k) {
		if (levels.size() > k) {
			return this.levels.get(k).genCandidateEpisode(k);
		}
		return null;
	}

	/**
	 * Get the number of frequent episodes.
	 * 
	 * @return the number of episodes
	 */
	public int getFrequentEpisodesCount() {
		return this.episodeCount;
	}

	/**
	 * Print the frequent episodes to the console.
	 */
	public void printFrequentEpisodes() {
		int numLevel = 0;
		for (Level l : levels) {
			System.out.println("  L" + numLevel + " +\r\n");
			for (Episode episode : l.getK_freEpisodes()) {
				System.out.println(episode.toString() + "\r\n");
			}
			System.out.println("\r\n");
			numLevel++;
		}
	}

}
