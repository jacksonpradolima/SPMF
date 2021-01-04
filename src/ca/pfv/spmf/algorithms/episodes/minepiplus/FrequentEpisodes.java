package ca.pfv.spmf.algorithms.episodes.minepiplus;

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
 * A set of frequent episodes found by the MINEPIPlus algorithm
 * 
 * @see AlgoMINEPIPlus
 * @author Peng Yang
 */
public class FrequentEpisodes {
	/**
	 * Episodes organized by levels. This is a list where the position i of the list
	 * contains the list of Episodes of size i
	 */
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
	 * Add a frequent episode
	 * 
	 * @param episode the episode
	 * @param k       the size of the episode
	 */
	public void addFrequentEpisode(Episode episode, int k) {
		while (levels.size() <= k) {
			levels.add(new Level());
		}
		levels.get(k).addFreEpisode(episode);
		this.episodeCount++;

//        System.out.println(episode.toString());
	}

	/**
	 * Save the frequent episode to an output file
	 * 
	 * @param output the path of the output file
	 * @throws IOException if an error occur while writing to file
	 */
	public void saveToFile(String output) throws IOException {
		// Create a string buffer
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));
		StringBuilder buffer = new StringBuilder();
		for (Level l : levels) {
			for (Episode episode : l.getKFrequentEpisodes()) {
				buffer.append(episode.toString());
				buffer.append("\r\n");
			}
		}
		// write to file and create a new line
		writer.write(buffer.toString());
		writer.close();
	}

	/**
	 * Get the total number of levels
	 * 
	 * @return the number of levels
	 */
	public int getTotalLevelNum() {
		return this.levels.size();
	}

	/**
	 * Get the number of frequent episodes.
	 * 
	 * @return the number of frequent episodes.
	 */
	public int getFrequentEpisodesCount() {
		return this.episodeCount;
	}

	/**
	 * Print the frequent episodes to the console
	 */
	public void printFrequentEpisodes() {
		int numLevel = 0;
		for (Level l : levels) {
			System.out.println("  L" + numLevel + " +\r\n");
			for (Episode episode : l.getKFrequentEpisodes()) {
				System.out.println(episode.toString() + "\r\n");
			}
			System.out.println("\r\n");
			numLevel++;
		}
	}

}
