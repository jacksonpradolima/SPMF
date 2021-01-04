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
 * This class is an eventSet implementation. A eventSet stores a series of
 * events.
 * 
 * @see AlgoCEPM
 * @author Jiaxuan Li
 */
public class EventSet {

	/** a list of the events in the eventSetF */
	private final List<Integer> events = new ArrayList<Integer>();

	public EventSet() {
	}

	/**
	 * Constructor
	 * 
	 * @param event event
	 */
	public EventSet(int event) {
		addEvent(event);
	}

	/**
	 * Add an event into the eventSet
	 * 
	 * @param event
	 */
	public void addEvent(int event) {
		events.add(event);
	}

	/**
	 * Get the eventSet
	 * 
	 * @return eventSet
	 */
	public List<Integer> getEvents() {
		return events;
	}
}