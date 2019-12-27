package ca.pfv.spmf.algorithms.episodes.tup.tup_combined;

import java.util.PriorityQueue;
import java.util.Queue;

/**
 * A set of global variables used by the TUP(Combined) algorithm.
 *
 */
 class TUPCGlobalVariables {
	
	protected static int k = 30;
	protected static CustomComparator_preinsertion_EWU idComparator = new CustomComparator_preinsertion_EWU();
	public static Queue<Episode_preinsertion_EWU> topKBuffer = new PriorityQueue<Episode_preinsertion_EWU>(k, idComparator);

}
