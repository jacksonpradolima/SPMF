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

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * This class is an implementation for storing the list of sequential patterns.
 * 
 * @see AlgoCEPM
 * @author Jiaxuan Li
 */
public class SequentialPatterns {

	/**
	 * A list of list is used to stored the sequential patterns. At position i, a
	 * list of sequential patterns contains all sequential patterns of size i.
	 */
	public final List<List<SequentialPattern>> levels = new ArrayList<List<SequentialPattern>>();

	/** the number of sequential patterns */
	public int sequenceCount = 0;

	/** the name of sequential pattern */
	private final String name;

	/**
	 * Constructor
	 * 
	 * @param name the name of sequential pattern
	 */
	public SequentialPatterns(String name) {
		this.name = name;
		levels.add(new ArrayList<SequentialPattern>());
	}

	/** add the sequential pattern to the corresponding index list */
	public void addSequence(SequentialPattern sequence, int k) {
		while (levels.size() <= k) {
			levels.add(new ArrayList<SequentialPattern>());
		}
		levels.get(k).add(sequence);
		sequenceCount++;
	}

	/**
	 * Get a string representation of this set of sequential patterns.
	 * 
	 * @param nbObject                the number of sequences in the database where
	 *                                these patterns were found.
	 * @param showSequenceIdentifiers if true, sequence identifiers will be output
	 *                                for each pattern
	 * @return a string
	 */
	public String toString(int nbObject, boolean showSequenceIdentifiers) {
		StringBuilder r = new StringBuilder(200);
		r.append(" ----------");
		r.append(name);
		r.append(" -------\n");
		int levelCount = 0;
		int patternCount = 0;
		for (List<SequentialPattern> level : levels) {
			r.append("  L");
			r.append(levelCount);
			r.append(" \n");
			for (SequentialPattern sequence : level) {
				patternCount++;
				r.append("  pattern ");
				r.append(patternCount);
				r.append(":  ");
				r.append(sequence.toString());
				r.append("support :  ");
				r.append(sequence.getRelativeSupportFormated(nbObject));
				r.append(" (");
				r.append(sequence.getAbsoluteSupport());
				r.append('/');
				r.append(nbObject);
				r.append(")");
				if (showSequenceIdentifiers) {
					r.append(" sequence ids: ");
					for (Integer sid : sequence.getSequenceIdCost().keySet()) {
						r.append(sid);
						r.append(" ");
					}
				}

				r.append("\n");
			}
			levelCount++;
		}
		r.append(" -------------------------------- Patterns count : ");
		r.append(sequenceCount);
		return r.toString();
	}

	public List<SequentialPattern> getLevel(int index) {
		return levels.get(index);
	}

	public int getLevelCount() {
		return levels.size();
	}

}