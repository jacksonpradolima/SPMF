package ca.pfv.spmf.algorithms.sequentialpatterns.prefixspan;

/**
 * This represents a sequence from a projected database (as based in PrefixSpan).
 * Since it is a projected sequence, it makes reference to the original sequence.
 * 
 * This class is used by the PrefixSpan and BIDE+ algorithms.
 *
 * Copyright (c) 2008-2012 Philippe Fournier-Viger
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 *
 * SPMF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SPMF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SPMF.  If not, see <http://www.gnu.org/licenses/>.
 */
public class PseudoSequence {

	// the corresponding sequence in the original database
	protected int sequenceID;

	// the first itemset of this pseudo-sequence in the original sequence
	protected int indexFirstItem;
	
	/**
	 * Get the original sequence corresponding to this projected sequence.
	 * @return the original sequence
	 */
	public int getOriginalSequenceID() {
		return sequenceID;
	}
	
	/**
	 * Create a pseudo-sequence from a sequence that is an original sequence.
	 * @param sequence the original sequence.
	 * @param indexFirstItem the item where the pseudo-sequence should start in terms of the original sequence.
	 */
	protected  PseudoSequence(int sequenceID, int indexFirstItem){
		// remember the original sequence
		this.sequenceID = sequenceID;
		// remember the starting position of this pseudo-sequence in terms
		// of the original sequence.
		this.indexFirstItem = indexFirstItem;
	}
}