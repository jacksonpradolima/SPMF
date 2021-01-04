package ca.pfv.spmf.algorithms.episodes.minepi;

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
 * This class implements a level for the MINEPI algorithm.
 *
 * @see AlgoMINEPI
 * @author Peng yang
 */
public class Level {
	/**
	 * the list of the start position (index) of the block having the same n-1
	 * prefix in a level
	 */
	List<Integer> block_start;

	/** the k frequent episodes */
	List<Episode> k_frequentEpisodes;

	/** the episode count */
	int episodeCount = 0;

	/**
	 * Constructor
	 */
	Level() {
		this.block_start = new ArrayList<>();
		this.k_frequentEpisodes = new ArrayList<>();
	}

	/**
	 * Add a frequent episode
	 * 
	 * @param episode the episode
	 */
	public void addFreEpisode(Episode episode) {
		this.k_frequentEpisodes.add(episode);
		episodeCount++;
	}

	/**
	 * Add a frequent episode and block start
	 * 
	 * @param episode the episode
	 * @param index   the block start
	 */
	public void addFreEpisodeAndBlockStart(Episode episode, int index) {
		this.k_frequentEpisodes.add(episode);
		this.block_start.add(index);
		episodeCount++;
	}

	/**
	 * Initialize the block start of first level ( for 1-episodes)
	 */
	public void init_firstLevel_block_start() {
		for (int i = 0; i < this.k_frequentEpisodes.size(); i++) {
			this.block_start.add(0);
		}
	}

	/**
	 * Get the episode count
	 * 
	 * @return the episode count
	 */
	public int getEpisodeCount() {
		return this.episodeCount;
	}

	/**
	 * Get the k frequent episodes
	 * 
	 * @return a list of episodes
	 */
	public List<Episode> getK_freEpisodes() {
		return this.k_frequentEpisodes;
	}

	/**
	 * generate next K+1 candidate episodes from this K-level The candidateLength =
	 * K
	 * 
	 * @param numLevel the number of levels
	 * @return a set of candidates
	 */

	public Candidates genCandidateEpisode(int numLevel) {

		Candidates candidates = new Candidates(numLevel + 1);
		// This k only for record candidates.
		int k = -1;
		for (int i = 0; i < k_frequentEpisodes.size(); i++) {
			// this i th will become the suffix(include the last n-1 event)

			for (int j = 0; j < k_frequentEpisodes.size(); j++) {
				// to compare every episode's prefix
				if (k_frequentEpisodes.get(i).compare2prefix(k_frequentEpisodes.get(j))) {
					// If the match is successful
					// use every episode in the this block with the i th episode to generate
					// candidates
					// i th episode provides : all events, z th episode provides : the last events
					int current_block_start = k + 1;
					int[] prefixEvents = new int[numLevel + 1];
					System.arraycopy(this.k_frequentEpisodes.get(i).events, 0, prefixEvents, 0, numLevel);
					for (int z = this.block_start.get(j); z < this.block_start.size()
							&& this.block_start.get(z) == this.block_start.get(j); z++) {
						k++;
						int[] candidate = prefixEvents.clone();
						candidate[numLevel] = this.k_frequentEpisodes.get(z).events[numLevel - 1];
						candidates.addCandidate(candidate, current_block_start);
					}

					// when we complete this block, then we need not to try others blocks
					// but only for the first level, because others's suffix are same

					break;

				}
			}
		}
		return candidates;
	}
}
