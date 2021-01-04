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
 * This class implements a simple Finite-State Automaton for the MINEPI
 * algorithm. The number -1 is used as end state.
 *
 * @see AlgoMINEPI
 * @author Peng yang
 */
public class FSA4MINEPI {
	/** the end state */
	final int END_STATE = -1;

	/** the finite state automata */
	int[] FSA;

	/** the position to match */
	int pos = 0;

	/** the start time */
	int startTime;

	/** constructor */
	FSA4MINEPI() {
	}

	/**
	 * Constructor
	 * 
	 * @param candidate a candidate
	 */
	FSA4MINEPI(int[] candidate) {
		int length = candidate.length;
		this.FSA = new int[length + 1];
		System.arraycopy(candidate, 0, this.FSA, 0, length);
		// add a end state
		this.FSA[length] = this.END_STATE;
	}

	/**
	 * Get the current event to match from the finite state automata
	 * 
	 * @return the event to match
	 */
	public int waiting4Event() {
		return this.FSA[pos];
	}

	/**
	 * Move to the next state of the finite state automata.
	 */
	public void transit() {
		this.pos++;
	}

	/**
	 * Check if the end state of the automata was reached.
	 * 
	 * @return true if yes, otherwise, false.
	 */
	public boolean isEnd() {
		return this.FSA[pos] == this.END_STATE;
	}

	/**
	 * Check if this automata is equal to another automata
	 * 
	 * @param fsa another automata
	 * @return yes, if equal, otherwise, false.
	 */
	public boolean isSame(FSA4MINEPI fsa) {
		return this.pos == fsa.pos;
	}

	/**
	 * Set the start time
	 * 
	 * @param startTime the start time
	 */
	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	/**
	 * Get the window length
	 * 
	 * @param endTime the end time
	 * @return the window length
	 */
	public int getWinLength(int endTime) {
		return endTime - startTime + 1;
	}

	/**
	 * Get a string representation of this automata
	 * 
	 * @return the string representation
	 */
	public String toString() {
		String returnString = "";
		for (int i = 0; i < pos - 1; i++) {
			returnString += this.FSA[i] + " -> ";
		}
		returnString += this.FSA[pos - 1] + "  ";
		return returnString;
	}

}
