package ca.pfv.spmf.algorithms.episodes.tup.tup_preinsertion;

import java.util.PriorityQueue;
import java.util.Queue;

/**
 * A set of global variables used by the TUP(Preinsertion) algorithm.
 *
 */
public class TUPPGlobalVariables {

	protected static int k = 30;
	protected static CustomComparator_preinsertion idComparator = new CustomComparator_preinsertion();
	protected static Queue<Episode_preinsertion> topKBuffer = new PriorityQueue<Episode_preinsertion>(k, idComparator);

}
