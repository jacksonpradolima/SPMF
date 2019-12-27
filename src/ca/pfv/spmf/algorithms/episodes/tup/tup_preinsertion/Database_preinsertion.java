package ca.pfv.spmf.algorithms.episodes.tup.tup_preinsertion;

import java.util.HashMap;
import java.util.Set;
/* This file is copyright (c) Rathore et al. 2018
* 
* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
* 
* SPMF is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* 
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/

/**
 * Sequence database
 * 
 */
public class Database_preinsertion {

	private static HashMap<Integer, Sequence_preinsertion> sequences;
	private static double CS = 0;

	/**
	 * get all sequences
	 * 
	 */
	public static HashMap<Integer, Sequence_preinsertion> getSequences() {
		if (null == sequences) {
			sequences = new HashMap<Integer, Sequence_preinsertion>();
		}
		return sequences;
	}

	/**
	 * get a particular sequence
	 * 
	 * @param i
	 *            sequence no.
	 */
	public static Sequence_preinsertion getSequence(Integer i) {
		return sequences.get(i);
	}

	/**
	 * add a particular sequence
	 * 
	 * @param i
	 *            sequence no.
	 */

	public void addSequence(Integer i, Sequence_preinsertion seq) {
		this.getSequences().put(i, seq);
	}

	/**
	 * Number of sequences
	 * 
	 * @return
	 */
	public static int size() {
		return getSequences().size();
	}

	/**
	 * get total utility of database complex event sequence
	 * 
	 */
	public static double getCS() {
		if (CS == 0) {
			HashMap<Integer, Sequence_preinsertion> sequences = getSequences();
			Set<Integer> keys = sequences.keySet();

			for (Integer key : keys) {
				Sequence_preinsertion seq = sequences.get(key);
				CS += seq.getSeqUtility();
			}
		}
		return CS;
	}
}
