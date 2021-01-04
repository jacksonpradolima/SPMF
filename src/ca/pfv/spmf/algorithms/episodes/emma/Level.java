package ca.pfv.spmf.algorithms.episodes.emma;

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
 * A list of frequent episode of a given level k.
 * 
 * @author Peng Yang
 * @see AlgoEMMA
 */

public class Level {

	/** the frequent episodes */
	List<Episode> kFrequentEpisodes;

	/** the number of episodes */
	int episodeCount = 0;

	/**
	 * Constructor
	 */
	Level() {
		this.kFrequentEpisodes = new ArrayList<>();
	}


	/**
	 * Add a frequent episode
	 * @param episode the episode
	 */
	public void addFreEpisode(Episode episode) {
		this.kFrequentEpisodes.add(episode);
		episodeCount++;
	}

	/**
	 * Get the number of episode
	 * @return the episode count
	 */
	public int getEpisodeCount() {
		return this.episodeCount;
	}

	/**
	 * Get the episodes of size k
	 * @return the episodes
	 */
	public List<Episode> getKFrequentEpisodes() {
		return this.kFrequentEpisodes;
	}

}
