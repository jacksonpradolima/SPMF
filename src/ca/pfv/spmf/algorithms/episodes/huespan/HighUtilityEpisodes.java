package ca.pfv.spmf.algorithms.episodes.huespan;

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
 * Copyright Peng Yang, Philippe Fournier-Viger, 2019
 */
/**
 * Some high utility episodes found in a complex sequence.
 * 
 * @author Peng Yang
 * @see AlgoHUESpan
 */
public class HighUtilityEpisodes {

	/**
	 * Position k in "HighUtilityEpisodesOfSizeK" contains the list of Episodes of
	 * size K
	 */
	private final List<HighUtilityEpisodesOfSizeK> hues = new ArrayList<HighUtilityEpisodesOfSizeK>();

	/** the total number of high utility episode **/
	private int episodeCount = 0;

	/**
	 * Constructor
	 */
	HighUtilityEpisodes() {
		// we create an empty list 0 by default
		hues.add(new HighUtilityEpisodesOfSizeK());
	}

	/**
	 * Add a high utility episode of size K to HUEs
	 * 
	 * @param HUE high utility episode
	 * @param k   the size of HUE
	 */
	public void addHighUtilityEpisode(HighUtilityEpisode HUE, int k) {
		while (hues.size() <= k) {
			hues.add(new HighUtilityEpisodesOfSizeK());
		}
		hues.get(k).addHighUtilityEpisodeOfSizeK(HUE);
		this.episodeCount++;
	}

	/**
	 * Get the number of levels
	 * @return the number of levels
	 */
	public int getTotalSize() {
		return this.hues.size();
	}

	/** 
	 * Get the number of high utility peisodes
	 * @return the number
	 */
	public int getHighUtilityEpisodeCount() {
		return this.episodeCount;
	}

	/**
	 * print the high utility episodes
	 */
	public void printHighUtilityEpisodes() {
		System.out.println(" ------- High Utility Episodes -------");
		int patternCount = 0;
		int numLevel = 0;
		// for each level (a level is a set of itemsets having the same number of items)
		for (HighUtilityEpisodesOfSizeK HUEofSizeK : hues) {
			// print how many HighUtilityEpisodes of size K are contained in this level
			System.out.println("  The size of episode is : " + numLevel + " \r\n");
			// for each episode
			for (HighUtilityEpisode episode : HUEofSizeK.getHighUtilityEpisodesOfSizeK()) {
				// print the episodes
				System.out.print("  pattern " + patternCount + ":  ");
				System.out.println(episode.toString());
				patternCount++;

			}
			numLevel++;
		}
		System.out.println(" --------------------------------");
	}

	/**
	 * output the information to a file
	 * 
	 * @param output file name
	 * @throws IOException
	 */
	public void saveToFile(String output) throws IOException {
		// Create a string buffer
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));
		StringBuilder buffer = new StringBuilder();
		int numLevel = 0;
		for (HighUtilityEpisodesOfSizeK HUEofSizeK : hues) {
			buffer.append("  The size of episode is : " + numLevel + " \r\n");
			for (HighUtilityEpisode episode : HUEofSizeK.getHighUtilityEpisodesOfSizeK()) {
				buffer.append(episode.toString());
				buffer.append("\r\n");
			}
			buffer.append("\r\n");
			numLevel++;
		}
		// write to file and create a new line
		writer.write(buffer.toString());
		writer.close();
	}
}
