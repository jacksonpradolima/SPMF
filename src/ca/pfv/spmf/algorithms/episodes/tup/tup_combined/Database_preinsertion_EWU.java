package ca.pfv.spmf.algorithms.episodes.tup.tup_combined;

import java.util.HashMap;
import java.util.Set;

/**
 * Sequence database
 * 
 */
public class Database_preinsertion_EWU {

	private static HashMap<Integer, Sequence_preinsertion_EWU> sequences;
	private static double CS = 0;

	/**
	 * get all sequences
	 * 
	 */
	public static HashMap<Integer, Sequence_preinsertion_EWU> getSequences() {
		if (null == sequences) {
			sequences = new HashMap<Integer, Sequence_preinsertion_EWU>();
		}
		return sequences;
	}

	/**
	 * get a particular sequence
	 * 
	 * @param i
	 *            sequence no.
	 */
	public static Sequence_preinsertion_EWU getSequence(Integer i) {
		return sequences.get(i);
	}

	/**
	 * add a particular sequence
	 * 
	 * @param i
	 *            sequence no.
	 */

	public void addSequence(Integer i, Sequence_preinsertion_EWU seq) {
		getSequences().put(i, seq);
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
			HashMap<Integer, Sequence_preinsertion_EWU> sequences = getSequences();
			Set<Integer> keys = sequences.keySet();

			for (Integer key : keys) {
				Sequence_preinsertion_EWU seq = sequences.get(key);
				CS += seq.getSeqUtility();
			}
		}
		return CS;
	}
}
