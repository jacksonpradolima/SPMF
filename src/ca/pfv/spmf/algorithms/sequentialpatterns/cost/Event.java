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
 * This class is an event implementation. A event stores its unique number label
 * as well as its cost.
 * 
 * @author Jiaxuan Li
 * @see AlgoCEPM
 */
public class Event {
	/** unique number label */
	int id;

	/** event's cost */
	double cost;

	/**
	 * Constructor
	 * 
	 * @param id   event's id
	 * @param cost event's cost
	 */
	public Event(int id, double cost) {
		this.id = id;
		this.cost = cost;
	}

	/**
	 * Setting
	 * 
	 * @param id   event's id
	 * @param cost event's cost
	 */
	public void setItem(int id, double cost) {
		this.id = id;
		this.cost = cost;
	}

	/**
	 * Get event's id
	 * 
	 * @return event's id
	 */
	public int getId() {
		return id;
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
	public void setCost(double cost) {
		this.cost = cost;
	}

}