package ca.pfv.spmf.algorithms.episodes.minepi;

import java.util.HashSet;
import java.util.Set;
/*
 * This file is part of the SPMF DATA MINING SOFTWARE *
 * (http://www.philippe-fournier-viger.com/spmf).
 *
 * SPMF is free software: you can redistribute it and/or modify it under the *
 * terms of the GNU General Public License as published by the Free Software *
 * Foundation, either version 3 of the License, or (at your option) any later *
 * version. SPMF is distributed in the hope that it will be useful, but WITHOUT
 * ANY * WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright Peng Yang  2019
 */

/**
 * This class represents a simultaneous event set. It is used by the MINEPI
 * algorithm.
 *
 * @see AlgoMINEPI
 * @author Peng yang
 */
public class Event {
	/** a set of events */
	Set<Integer> events = new HashSet<>();
	/** the time */
	int time;

	/** constructor */
	Event() {

	}

	/**
	 * Constructor
	 * 
	 * @param events a set of events
	 * @param time   the time
	 */
	Event(String[] events, int time) {
		for (String e : events) {
			this.events.add(Integer.parseInt(e));
		}
	}

	/**
	 * Constructor
	 * 
	 * @param event a single event
	 * @param time  the time
	 */
	Event(int event, int time) {
		events.add(event);
		this.time = time;
	}

	/**
	 * Set the time
	 * @param time the time
	 */
	public void setTime(int time) {
		this.time = time;
	}

	/**
	 * Add an event
	 * @param event the event
	 */
	public void addEvent(Integer event) {
		this.events.add(event);
	}

	/** 
	 * Check if this event set contains an event
	 * @param event the event
	 * @return true if the event appears. Otherwise, false
	 */
	public boolean contains(Integer event) {
		return this.events.contains(event);
	}

	/**
	 * Get the time
	 * @return the time
	 */
	public int getTime() {
		return this.time;
	}
}
