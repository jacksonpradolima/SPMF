package ca.pfv.spmf.algorithms.episodes.minepi;
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
 * This class represents an episode (serial episode) in a simple sequence, that
 * is an episode containing no more than a single symbol for each time point. It
 * is used by the MINEPI algorithm.
 *
 * @see AlgoMINEPI
 * @author Peng yang
 */
public class Episode {

	/**
	 * the events in the serial episode each event is a single event(symbol)
	 */
	int[] events;

	/** The support of episode */
	int support = 0;

	/**
	 * Constructor
	 */
	Episode() {

	}

	/**
	 * Constructor of an episode
	 * 
	 * @param events  the events
	 * @param support the support
	 */
	Episode(int[] events, int support) {
		this.events = events;
		this.support = support;
	}

	/**
	 * Increase the support by 1.
	 */
	public void increaseSupport() {
		this.support++;
	}

	/**
	 * Compare two prefix. This episode will use last n-1 event as suffix to compare
	 * with the first n-1 event of the episode of having the same size
	 * 
	 * @param prefix the other prefix
	 * @return true if the same prefix. Otherwise, false.
	 */
	public boolean compare2prefix(Episode prefix) {
		// we only compare with others in the condition that the size is large 1
		for (int i = 0; i < this.events.length - 1; i++) {
			if (this.events[i + 1] != prefix.events[i])
				return false;
		}
		return true;
	}

	/**
	 * Get a string representation of this object
	 * 
	 * @return a string
	 */
	public String toString() {
		String returnString = "";
		int episodeLength = events.length;
		for (int i = 0; i < episodeLength - 1; i++) {
			returnString = returnString + String.valueOf(events[i]) + " -1 ";
		}
		returnString = returnString + String.valueOf(events[episodeLength - 1]) + " -1 #SUP : "
				+ String.valueOf(this.support);
		return returnString;
	}

}
