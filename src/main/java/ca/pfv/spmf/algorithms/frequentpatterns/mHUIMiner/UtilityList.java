package ca.pfv.spmf.algorithms.frequentpatterns.mHUIMiner;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a UtilityList as used by the AlgoSimba algorithm.
 *
 * @see UtilityTuple
 * @see AlgoMHUIMiner
 * @author Philippe Fournier-Viger, modified by Alex Peng
 */
public class UtilityList {
	/** the id of an item */
	final Integer itemID;
	
	/** the sum of the item's utilities */
	long sumIutils = 0; 
	
	/** the sum of the item's remaining utilities */
	long sumRutils = 0; 
	
	/** all the utilityTuples of the item */
	List<UtilityTuple> uLists = new ArrayList<UtilityTuple>();

	/**
	 * Constructor.
	 * 
	 * @param item
	 *            the item that is used for this utility list
	 */
	public UtilityList(Integer itemID) {
		this.itemID = itemID;
	}
	
	/**
	 * Constructor.
	 * 			used when the itemID is not important
	 * 
	 */
	public UtilityList() {
		this.itemID = null;
	}
	

	/**
	 * Method to add a utility tuple to this utility list and update the sums at the
	 * same time.
	 */
	public void addTuple(UtilityTuple uTuple) {
		sumIutils += uTuple.getIutils();
		sumRutils += uTuple.getRutils();
		uLists.add(uTuple);
	}

	/**
	 * Get the support of the itemset represented by this utility-list
	 * 
	 * @return the support as a number of trnsactions
	 */
	public int getSupport() {
		return uLists.size();
	}
}