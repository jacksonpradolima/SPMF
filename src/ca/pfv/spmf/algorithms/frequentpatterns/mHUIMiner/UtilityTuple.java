package ca.pfv.spmf.algorithms.frequentpatterns.mHUIMiner;

/**
 * This class represents a UtilityTuple used in the AlgoSimba class
 * 
 * @see AlgoMHUIMiner
 * @see UtilityList
 * @author Philippe Fournier-Viger, modified by Alex Peng
 */
public class UtilityTuple {
	// these properties should never be changed once instantiated
	/** transaction id */
	private final int tid;
	/** itemset utility */
	private final int iutils;
	/** remaining utility */
	private final int rutils;

	/**
	 * Constructor.
	 * 
	 * @param tid
	 *            the transaction id
	 * @param iutils
	 *            the itemset utility
	 * @param rutils
	 *            the remaining utility
	 */
	public UtilityTuple(int tid, int iutils, int rutils) {
		this.tid = tid;
		this.iutils = iutils;
		this.rutils = rutils;
	}

	/**
	 * Get the transaction id
	 * @return the transaction id (an integer)
	 */
	public int getTid() {
		return tid;
	}

	/**
	 * Get the sum of utilities (iutils)
	 * @return the sum
	 */
	public int getIutils() {
		return iutils;
	}

	/**
	 * Get the sum of remaining utilities (rutil)
	 * @return the sum
	 */
	public int getRutils() {
		return rutils;
	}
}