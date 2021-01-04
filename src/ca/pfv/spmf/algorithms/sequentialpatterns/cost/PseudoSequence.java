/*
* This is an implementation of the CEPB, corCEPB, CEPN algorithm.
*
* 
* This file is part of the SPMF DATA MINING SOFTWARE * (http://www.philippe-fournier-viger.com/spmf).
*
* SPMF is free software: you can redistribute it and/or modify it under the * terms of the GNU General Public License as published by the Free Software * Foundation, either version 3 of the License, or (at your option) any later * version. *

* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR * A PARTICULAR PURPOSE. See the GNU General Public License for more details. *
*
* You should have received a copy of the GNU General Public License along with SPMF. If not, see <http://www.gnu.org/licenses/>.
*
* Copyright (c) 2019 Jiaxuan Li
*/

package ca.pfv.spmf.algorithms.sequentialpatterns.cost;

/**
 * This class is a projected sequences implementation. A pseudoSequence stores
 * the event's located sequence id and its following event's index in the
 * sequence. This class is used to construct the projected database of the
 * event.
 * 
 * @see AlgoCEPM
 * @author Jiaxuan Li
 */

public class PseudoSequence {

	/** the sequence id where the event locates */
	public int sequenceID;

	/** the first itemset of this pseudo-sequence in the original sequence */
	public int indexFirstItem;

	/** the sequence's length where the evnet locates */
	public int sequenceLength;
	
	/**
	 * Get sequence Id where the event locates
	 * 
	 * @return sequence id
	 */
	public int getOriginalSequenceID() {
		return sequenceID;
	}
	
	/**
	 * Get sequence's length where the event locates
	 * 
	 * @return sequence's length
	 */
	public int getSequenceLength() {
		return sequenceLength;
	}

	/**
	 * Create a pseudo-sequence from a sequence that is an original sequence.
	 * 
	 * @param sequence       the original sequence.
	 * @param indexFirstItem the item where the pseudo-sequence should start in
	 *                       terms of the original sequence.
	 * @param sequenceLength the length of the sequence where the event locates
	 */
	public PseudoSequence(int sequenceID, int indexFirstItem, int sequenceLength) {
		// remember the original sequence
		this.sequenceID = sequenceID;
		// remember the starting position of this pseudo-sequence in terms
		// of the original sequence.
		this.indexFirstItem = indexFirstItem;
		this.sequenceLength = sequenceLength;
	}
}