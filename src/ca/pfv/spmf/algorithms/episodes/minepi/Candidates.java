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
 * This class is used to store candidates by the MINEPI algorithm.
 * 
 * @see AlgoMINEPI
 * @author Peng yang
 */
public class Candidates {
	/**
	 * the list of the start position (index) of the block of having same n-1 prefix
	 * in the a level
	 **/
	List<Integer> blockStart;

	/** a list of candidates */
	List<int[]> candidates;

	/** the candidate count */
	int candidateCount = 0;

	/** the number k */
	int k;

	/**
	 * Constructor
	 * 
	 * @param k the value of k
	 */
	Candidates(int k) {
		this.candidates = new ArrayList<>();
		this.blockStart = new ArrayList<>();
		this.k = k;
	}

	/**
	 * Add a candidate to this set of candidates
	 * 
	 * @param candidate  the candidate
	 * @param blockIndex the block index
	 */
	public void addCandidate(int[] candidate, int blockIndex) {
		this.candidates.add(candidate);
		this.blockStart.add(blockIndex);
		candidateCount++;
	}

	/**
	 * Check if this candidate set is empty
	 * 
	 * @return true if yes. Otherwise false
	 */
	public boolean isEmpty() {
		return candidates.size() <= 0;
	}

	/**
	 * Get the number of candidates.
	 * 
	 * @return the number of candidates
	 */
	public int getCandidateCount() {
		return this.candidateCount;
	}

	/**
	 * Add the frequent k episodes
	 * 
	 * @param sequence         a sequence
	 * @param minSupport       the minimum support
	 * @param maxWindow        the maximum window
	 * @param frequentEpisodes the frequent episodes
	 */
	public void getFrequentKepisodes(List<Event> sequence, int minSupport, int maxWindow,
			FrequentEpisodes frequentEpisodes) {

		// the last candidate's block_start
		int lastBlockStart = -1;
		// the current frequentEpisode's block_start
		int currentBlockStart = -1;
		// We use k to record the number of frequent episode
		// In the paper, k = 0 , we use k = -1.
		int k = -1;
		for (int i = 0; i < this.candidateCount; i++) {

			List<FSA4MINEPI> fsaList = new ArrayList<>();
			fsaList.add(new FSA4MINEPI(this.candidates.get(i)));

			int support = 0;

			for (Event eventSet : sequence) {
				int currentFSAsize = fsaList.size();
				for (int j = currentFSAsize - 1; j >= 0; j--) {
					if (eventSet.contains(fsaList.get(j).waiting4Event())) {
						fsaList.get(j).transit();
						if (j == currentFSAsize - 1) {
							// The j-th FSA is the first start
							fsaList.get(j).setStartTime(eventSet.getTime());
							// and we add a new FSA
							fsaList.add(new FSA4MINEPI(this.candidates.get(i)));
						}
						if (fsaList.get(j).isEnd()) {
							// if current FSA reach end state, and if it statify maxWin then support ++
							// ,finally remove it.
							if (fsaList.get(j).getWinLength(eventSet.getTime()) <= maxWindow) {
								support++;
							}
							fsaList.remove(j);
						}
						if (j >= 1 && fsaList.get(j).isSame(fsaList.get(j - 1))
								&& !eventSet.contains(fsaList.get(j - 1).waiting4Event())) {
							// if current FSA's wait equals to previous FSA's wait, then we delete previous
							// FSA in the FSAList
							fsaList.remove(j - 1);
							j--; // to index the current FSA in the FSAList
						}
					}
				}
			}
			if (support >= minSupport) {
				k++;
				if (this.blockStart.get(i) != lastBlockStart) {
					lastBlockStart = this.blockStart.get(i);
					currentBlockStart = k;
				}
				Episode episode = new Episode(candidates.get(i), support);
				frequentEpisodes.addFrequentFpisodeAndBlockStart(episode, this.k, currentBlockStart);
			}
		}
	}

}
