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
 * All high utility episodes of a given size K, where the size 
 * of episode defined as the number of simultaneous event sets (itemset)
 * 
 * @author Peng Yang
 * @see AlgoHUESpan
 */
public class HighUtilityEpisodesOfSizeK {

	/** List contains all high utility episodes of the size K **/
	private List<HighUtilityEpisode> highUtilityEpisodeOfSizeK;

	/** The count of episodes in this list **/
	private int episodeCount;

	/**
	 * Default constructor
	 */
	HighUtilityEpisodesOfSizeK() {
		this.highUtilityEpisodeOfSizeK = new ArrayList<>();
		this.episodeCount = 0;
	}

	/**
	 * Add a high utility episode of size K to the list
	 * 
	 * @param hueOfSizeK high utility episode of size K
	 */
	public void addHighUtilityEpisodeOfSizeK(HighUtilityEpisode hueOfSizeK) {
		this.highUtilityEpisodeOfSizeK.add(hueOfSizeK);
		episodeCount++;
	}

	/**
	 * Get the episode count
	 * 
	 * @return
	 */
	public int getEpisodeCount() {
		return this.episodeCount;
	}

	/**
	 * get the list
	 * 
	 * @return
	 */
	public List<HighUtilityEpisode> getHighUtilityEpisodesOfSizeK() {
		return this.highUtilityEpisodeOfSizeK;
	}
}
