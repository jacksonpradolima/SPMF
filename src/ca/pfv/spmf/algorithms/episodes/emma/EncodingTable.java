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
 * Encoding table of the EMMA algorithm
 * @author Peng Yang
 * @see AlgoEMMA
 */
public class EncodingTable {
	
	/** list of episodes F1*/
	private List<Episode> f1;

	/** the bound list of F1 */
	private List<List<int[]>> f1Boundlist;

	/** the table length */
	private int tableLength;

	/** constructor **/
	EncodingTable() {
		this.f1 = new ArrayList<>();
		this.f1Boundlist = new ArrayList<>();
		this.tableLength = 0;
	}

	/** 
	 * Add an episode and its bound list
	 * @param episode the episode
	 * @param boundlist its bound list
	 */
	public void addEpisodeAndBoundlist(Episode episode, List<int[]> boundlist) {
		f1.add(episode);
		f1Boundlist.add(boundlist);
		tableLength++;
	}

	/**
	 * Obtain an episode having a given ID
	 * @param id the ID
	 * @return the episode
	 */
	public Episode getEpisodebyID(int id) {
		return this.f1.get(id);
	}

	/**
	 * Get a bound list having a given ID
	 * @param id the ID
	 * @return the bound list
	 */
	public List<int[]> getBoundlistByID(int id) {
		return this.f1Boundlist.get(id);
	}

	/**
	 * Get an episode name corresponding to a given ID
	 * @param id the ID
	 * @return the episode name
	 */
	public int[] getEpisodeNameByID(int id) {
		return this.f1.get(id).events.get(0);
	}

	/**
	 * Get F1
	 * @return a list of episodes
	 */
	public List<Episode> getF1() {
		return f1;
	}

	/**
	 * Get the bound lists of all episodes in F1
	 * @return a list of bound list
	 */
	public List<List<int[]>> getF1boundlist() {
		return f1Boundlist;
	}

	/** Get the length of this table
	 * 
	 * @return the length
	 */
	public int getTableLength() {
		return this.tableLength;
	}
}
