package ca.pfv.spmf.algorithms.episodes.emma;

import java.util.ArrayList;
import java.util.List;
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
 * Copyright Peng Yang, Philippe Fournier-Viger, 2019
 */
/**
 * implement Class of Episode ( serial episode) in complex sequence it means
 * that the episode can contains multiple symbols for one time point
 *
 * @author Peng Yang
 * @see AlgoEMMA
 */
public class Episode implements Comparable<Episode>{

	/** the events in the serial episode (each event is a non-empty eventset) */
	List<int[]> events;

	/** The support of episode */
	int support = 0;

	/**
	 * Constructor
	 */
	Episode() {
		this.events = new ArrayList<>();
		this.support = 0;
	}

	/**
	 * Constructor
	 * @param events the events
	 * @param support the support of this episode
	 */
	Episode(List<int[]> events, int support) {
		this.events = events;

		this.support = support;
	}

	/**
	 * Create an i-extension of this episode
	 * @param item the item used to do the i-extension
	 * @param support the support
	 * @return a new episode that is the i-extension
	 */
	public Episode iExtension(int item, int support) {
		int[] finalEventSet = this.events.get(events.size() - 1);
		int len = finalEventSet.length;
		int[] newEventSet = new int[len + 1];
		System.arraycopy(finalEventSet, 0, newEventSet, 0, len);
		newEventSet[len] = item;
		List<int[]> newEvents = new ArrayList<int[]>(events);
		// set the last eventSet to the new eventSet.
		newEvents.set(events.size() - 1, newEventSet);
		return new Episode(newEvents, support);
	}

	/**
	 * Create an s-extension of this episode
	 * @param item the item used to do the s-extension
	 * @param support the support
	 * @return a new episode that is the s-extension of this episode
	 */
	public Episode sExtension(int item, int support) {
		List<int[]> newEvents = new ArrayList<int[]>(events);
		newEvents.add(new int[] { item });
		return new Episode(newEvents, support);
	}

	/**
	 * Create an s-extension of this episode
	 * @param fllowingEpisodeName the following episode name (set of items)
	 * @param support the support
	 * @return a new episode that is the s-extension of this episode
	 */
	public Episode sExtension(int[] fllowingEpisodeName, int support) {
		List<int[]> newEvents = new ArrayList<int[]>(events);
		newEvents.add(fllowingEpisodeName);
		return new Episode(newEvents, support);
	}

	/**
	 * Get the last item (only for 1-episode to call)
	 * 
	 * @return the last item
	 */
	public int getLastItem() {
		return events.get(0)[0];
	}

	/**
	 * Increase the support of this episode by 1 
	 */
	public void increaseSupport() {
		this.support++;
	}

	/**
	 * Get a string representation of this episode.
	 * @return a string
	 */
	public String toString() {
		String returnString = "";
		int episodeLength = events.size();
		for (int i = 0; i < episodeLength - 1; i++) {
			for (int j = 0; j < events.get(i).length - 1; j++) {
				returnString = returnString + String.valueOf(events.get(i)[j]) + " ";
			}
			returnString = returnString + String.valueOf(events.get(i)[events.get(i).length - 1]);
			returnString = returnString + " -1 ";
		}
		for (int j = 0; j < events.get(episodeLength - 1).length - 1; j++) {
			returnString = returnString + String.valueOf(events.get(episodeLength - 1)[j]) + " ";
		}
		returnString = returnString
				+ String.valueOf(events.get(episodeLength - 1)[events.get(episodeLength - 1).length - 1]);
		returnString = returnString + " -1 #SUP: " + String.valueOf(this.support);
		return returnString;
	}
	
    /**
     * Compare this pattern with another pattern
     * @param o another pattern
     * @return 0 if equal, -1 if smaller, 1 if larger (in terms of support).
     */
    public int compareTo(Episode o) {
		if(o == this){
			return 0;
		}
		long compare =  this.support - o.support;
		if(compare > 0){
			return 1;
		}
		if(compare < 0){
			return -1;
		}
		return 0;
	}

}
