package ca.pfv.spmf.algorithms.episodes.huespan;

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
 * A HighUtilityEpisode in a complex sequence. A complex sequence means that the
 * episode can contains multiple symbols for the same time point. An episode is
 * a high utility episode if it satisfies two conditions: 1. The occurrence must
 * be a minimal occurrence 2. The utility must be no less than the minimum
 * utility threshold.
 * 
 * @author Peng Yang
 * @see AlgoHUESpan
 */
public class HighUtilityEpisode {

	/** episode contains simultaneous event sets (itemset) **/
	private List<int[]> events;

	/** the utility of the episode **/
	private int utility;

	/**
	 * Default constructor
	 */
	HighUtilityEpisode() {
		this.events = new ArrayList<>();
		this.utility = 0;
	}

	/**
	 * Constructor
	 * 
	 * @param events  the size of episode >=2
	 * @param utility the utility of the episode
	 */
	HighUtilityEpisode(List<int[]> events, int utility) {
		this.events = events;
		this.utility = utility;
	}

	/**
	 * Get the utility of this episode
	 * @return the utility
	 */
	public int getUtility() {
		return this.utility;
	}

	/**
	 * Get the list of events of this episode
	 * @return the list of events
	 */
	public List<int[]> getEvents() {
		return this.events;
	}

	/**
	 * get the size of this episode
	 * @return the size
	 */
	public int getSize() {
		return this.events.size();
	}

	/**
	 * Get a string representation of this episode
	 * @return a string
	 */
	public String toString() {
		String returnString = "";
		int episodeSize = this.events.size();
		for (int i = 0; i < episodeSize; i++) {
			int[] itemset = this.events.get(i);
			int itemsetLength = itemset.length;

			for (int j = 0; j < itemsetLength; j++) {
				returnString += String.valueOf(itemset[j]) + " ";
			}
			returnString += "-1 ";
		}
		returnString = returnString + "#UTIL: " + String.valueOf(this.utility);
		return returnString;
	}
}
