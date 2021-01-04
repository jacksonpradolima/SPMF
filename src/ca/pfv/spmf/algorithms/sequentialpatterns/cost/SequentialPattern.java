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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class represents a sequential pattern as used by CEPB, CEPN and CorCEPB
 * algorithms
 *
 * @see AlgoCEPM
 * @author phil Jiaxuan Li
 */
public class SequentialPattern implements Comparable<SequentialPattern> {
	/** eventSets */
	private final List<EventSet> eventsets;

	/** */
	private Map<Integer, Integer> sequencesIdCost;

	/** a list of sequence_id where the event located */
	private List<Integer> sequenceIDS;

	/** pattern's averageCost */
	private double averageCost;
	
	/** pattern's occupancy */
	private double occupancy;

	/** pattern's correlation with the binary utility for corCEPB algorithm */
	private double correlation;

	/**
	 * pattern's trade-off in terms of its cost and numeric utility for CEPN
	 * algorithm
	 */
	private double tradeOff;

	/** pattern's numeric utility for CEPN algorithm */
	private double utility;

	/** number of positive sequences where the pattern locates */
	private int numInPositive;

	/** number of negative sequences where the pattern locates */
	private int numInNegative;

	/** average cost of the pattern located in the positive sequences */
	private double averageCostInPos;

	/** average cost of the pattern located in the negative sequences */
	private double averageCostInNeg;

	/**
	 * Get average cost of the pattern located in the positive sequences
	 * 
	 * @return average cost of the pattern located in the positive sequences
	 */
	public double getAverageCostInPos() {
		return averageCostInPos;
	}

	/**
	 * Setting
	 * 
	 * @param averageCostInPos average cost of the pattern located in the positive
	 *                         sequences
	 */
	public void setAverageCostInPos(double averageCostInPos) {
		this.averageCostInPos = averageCostInPos;
	}

	/**
	 * Get average cost of the pattern located in the negative sequences
	 * 
	 * @return average cost of the pattern located in the negative sequences
	 */
	public double getAverageCostInNeg() {
		return averageCostInNeg;
	}

	/**
	 * Setting
	 * 
	 * @param averageCostInNeg average cost of the pattern located in the negative
	 *                         sequences
	 */
	public void setAverageCostInNeg(double averageCostInNeg) {
		this.averageCostInNeg = averageCostInNeg;
	}

	/**
	 * Get the number of positive sequences where the pattern locates
	 * 
	 * @return the number of positive sequences where the pattern locates
	 */
	public int getNumInPositive() {
		return numInPositive;
	}

	/**
	 * Setting
	 * 
	 * @param the number of positive sequences where the pattern locates
	 */
	public void setNumInPositive(int numInPositive) {
		this.numInPositive = numInPositive;
	}

	/**
	 * Get the number of negative sequences where the pattern locates
	 * 
	 * @return the number of negative sequences where the pattern locates
	 */
	public int getNumInNegative() {
		return numInNegative;
	}

	/**
	 * Setting
	 * 
	 * @param the number of negative sequences where the pattern locates
	 */
	public void setNumInNegative(int numInNegative) {
		this.numInNegative = numInNegative;
	}

	/** the list of pattern's cost and numeric utility information */
	private ArrayList<CostUtilityPair> costUtilityPairs;

	/**
	 * Get the pattern's utility
	 * 
	 * @return pattern's utility
	 */
	public double getUtility() {
		return utility;
	}

	/**
	 * Setting
	 * 
	 * @param utility pattern's utility
	 */
	public void setUtility(double utility) {
		this.utility = utility;
	}

	/**
	 * Get the pattern's trade-off
	 * 
	 * @return pattern's trade-off
	 */
	public double getTradeOff() {
		return tradeOff;
	}

	/**
	 * Setting
	 * 
	 * @param tradeOff pattern's trade-off
	 */
	public void setTradeOff(double tradeOff) {
		this.tradeOff = tradeOff;
	}

	/**
	 * Get pattern's correlation
	 * 
	 * @return pattern's correlation
	 */
	public double getCorrelation() {
		return correlation;
	}

	/**
	 * Setting
	 * 
	 * @param correlation pattern's correlation
	 */
	public void setCorrelation(double correlation) {
		this.correlation = correlation;
	}

	/**
	 * Get pattern's average cost
	 * 
	 * @return pattern's average cost
	 */
	public double getAverageCost() {
		return averageCost;
	}

	/**
	 * Setting
	 * 
	 * @param averageCost pattern's average cost
	 */
	public void setAverageCost(double averageCost) {
		this.averageCost = averageCost;
	}

	/**
	 * Constructor
	 * 
	 */
	public SequentialPattern() {
		eventsets = new ArrayList<EventSet>();
	}

	/**
	 * Get pattern's occupancy
	 * 
	 * @return pattern's occupancy
	 */
	public double getOccupancy() {
		return occupancy;
	}

	/**
	 * Setting
	 * 
	 * @param occupancy, pattern's occupancy
	 */
	public void setOccupancy(double occupancy) {
		this.occupancy = occupancy;
	}

	/**
	 * Setting
	 * 
	 * @param sequencesIdCost key: sequence id where the pattern locates, value:
	 *                        pattern's cost
	 */
	public void setSequencesIdCost(Map<Integer, Integer> sequencesIdCost) {
		this.sequencesIdCost = sequencesIdCost;
	}

	/**
	 * Setting
	 * 
	 * @param sequenceIDS a list of sequence id where the pattern locates
	 */
	public void setSequencesIDs(List<Integer> sequenceIDS) {
		this.sequenceIDS = sequenceIDS;
	}

	/**
	 * Get the list of sequence id where the pattern locates
	 * 
	 * @return the list of sequence id where the pattern locates
	 */
	public List<Integer> getSequencesIDs() {
		return sequenceIDS;
	}

	public void addEventset(EventSet eventSet) {
		eventsets.add(eventSet);
	}

	/**
	 * Get the list of cost and utility information of the pattern
	 * 
	 * @return the list of cost and utility information of the pattern
	 */
	public ArrayList<CostUtilityPair> getCostUtilityPairs() {
		return costUtilityPairs;
	}

	/**
	 * Setting
	 * 
	 * @param costUtilityPairs the list of cost and utility information of the
	 *                         pattern
	 */
	public void setCostUtilityPairs(ArrayList<CostUtilityPair> costUtilityPairs) {
		this.costUtilityPairs = costUtilityPairs;
	}

	public void print() {
		System.out.println(toString());
	}

	/**
	 * Transforming the pattern to text
	 * 
	 * @return pattern in text
	 */
	public String eventSetstoString() {
		StringBuilder r = new StringBuilder();
		// For each eventset in this sequential pattern
		for (EventSet eventset : eventsets) {
			// For each event in the current eventset
			for (Integer event : eventset.getEvents()) {

				// Transform event number to string
				String string = DataMapper.getKey(event);

				// String string = event.toString();
				r.append(string); // append the event
			}
			r.append(" -1 ");// end of an eventset
		}
		return r.append("-2").toString();
	}

	/**
	 * Get the relative support of this pattern (a percentage)
	 * 
	 * @param sequencecount the number of sequences in the original database
	 * @return the support as a string
	 */
	public String getRelativeSupportFormated(int sequencecount) {
		double relSupport = ((double) sequencesIdCost.size()) / ((double) sequencecount);
		// pretty formating :
		DecimalFormat format = new DecimalFormat();
		format.setMinimumFractionDigits(0);
		format.setMaximumFractionDigits(5);
		return format.format(relSupport);
	}

	/**
	 * Get the absolute support of this pattern.
	 * 
	 * @return the support (an integer >= 1)
	 */
	public int getAbsoluteSupport() {
		return sequenceIDS.size();
	}

	public Map<Integer, Integer> getSequenceIdCost() {
		return sequencesIdCost;
	}

	@Override
	public int compareTo(SequentialPattern arg0) {
		return 0;
	}

}