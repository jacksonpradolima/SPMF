package ca.pfv.spmf.algorithms.episodes.huespan;

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
 * A finite state machine as used by HUE-SPAN
 * 
 * @author Peng Yang
 * @see AlgoHUESpan
 */
public class FiniteStateMachine {

	/** finite state automata to match an episode */
	List<int[]> fsa;

	/** a position */
	int pos = 0;

	/** the utility */
	int utility = 0;

	/**
	 * Constructor
	 */
	FiniteStateMachine() {
	}

	/**
	 * constructor
	 * 
	 * @param episode                an episode
	 * @param utilityOfFirstEventSet utility of the first event set
	 */
	FiniteStateMachine(List<int[]> episode, int utilityOfFirstEventSet) {
		this.fsa = episode;
		this.utility += utilityOfFirstEventSet;
		transit(); // pass the start point
	}

	/**
	 * The current event to be match
	 * 
	 * @return the current event
	 */
	public int[] waiting4Events() {
		return this.fsa.get(pos);
	}

	/**
	 * Go to the next event to be matched
	 */
	public void transit() {
		this.pos++;
	}

	/**
	 * Check if there are no more events to be matched
	 * 
	 * @return true, if no more, else, false
	 */
	public boolean isEnd() {
		return this.pos == this.fsa.size();
	}

	/**
	 * Check if the same as another finite state machine
	 * 
	 * @param fsa another finite state machine
	 * @return true or false
	 */
	public boolean isSame(FiniteStateMachine fsa) {
		return this.pos == fsa.pos;
	}

	/**
	 * Get the utility
	 * 
	 * @return the utility
	 */
	public int getUtility() {
		return this.utility;
	}

	/**
	 * Scan the eventset and compare with this finite state machine
	 * 
	 * @param pairs a list of pairs
	 * @return true if it matches, else false.
	 */
	public boolean scan(List<int[]> pairs) {
		int utilityOfitemset = 0;
		int index = 0;
//        System.out.println(pos+"  锛�  "+this.FSA.size());
		int length = this.fsa.get(pos).length;
		for (int j = 0; j < pairs.size(); j++) {
			int item = pairs.get(j)[0];
			if (item == this.fsa.get(pos)[index]) {
				index++;
				utilityOfitemset += pairs.get(j)[1];
			}

			if (index == length) {
				this.utility += utilityOfitemset;
				break;
			}
		}

		return index == length;
	}

}
