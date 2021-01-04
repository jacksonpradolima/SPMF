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
 * This is an implementation for storing the event's cost and its located
 * sequence length.
 * 
 * @author Jiaxuan Li
 * @see AlgoCEPM
 */
public class Pair {
	
	/** event's cost */
	private double cost;
	
	/** sequence length where event located */
	private int totalLengthOfSeq;
	
	/** the index of the next event where the current event located */
	private int indexOfNextEvent;

	/**
	 * Constructor
	 * 
	 * @param cost             event's cost
	 * @param totalLengthOfSeq sequence length
	 * @param indexOfNextEvent the index of the next event where the current event located
	 * 
	 */
	public Pair(double cost, int totalLengthOfSeq,int indexOfNextEvent) {
		this.cost = cost;
		this.totalLengthOfSeq = totalLengthOfSeq;
		this.indexOfNextEvent = indexOfNextEvent;
	}

	/**
	 * Get sequence length where the event locates
	 * 
	 * @return sequence length
	 */
	public int getTotalLengthOfSeq() {
		return totalLengthOfSeq;
	}

	/**
	 * Setting
	 * 
	 * @param totalLengthOfSeq, sequence length
	 */
	public void setTotalLengthOfSeq(int totalLengthOfSeq) {
		this.totalLengthOfSeq = totalLengthOfSeq;
	}

	public Pair() {

	}
	
	/** 
	 * Get the index of the next event where the current event located 
	 * 
	 * @return indexOfNextEvent
	 */
	public int getIndexOfNextEvent() {
		return indexOfNextEvent;
	}

	/**
	 * Get event's cost
	 * 
	 * @return event's cost
	 */
	public double getCost() {
		return cost;
	}

	/**
	 * Setting
	 * 
	 * @param cost event's cost
	 */
	public void setCost(int cost) {
		this.cost = cost;
	}

}