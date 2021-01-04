package ca.pfv.spmf.algorithms.episodes.minepiplus;

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
 * Copyright Peng Yang  2019
 */

/**
 * This class is an Episode (serial episode) in a complex sequence (where an
 * episode can contain multiple symbols for the same time point). This is used
 * by the MINEPIPlus algorithm
 * 
 * @see AlgoMINEPIPlus
 * @author Peng Yang
 */
public class Episode {

	/** the events in the serial episode (each event is a non-empty eventset) */
	List<int[]> events;

	/** The support of the episode */
	int support = 0;

	/** Constructor */
	Episode() {
		this.events = new ArrayList<>();
		this.support = 0;
	}

	/**
	 * Constructor
	 * 
	 * @param events  the events of this episode
	 * @param support the support of this episode
	 */
	Episode(List<int[]> events, int support) {
		this.events = events;

		this.support = support;
	}

	/**
	 * Perform an i-extension of this episode with an item
	 * 
	 * @param item    the item
	 * @param support the support
	 * @return the resulting episode
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
	 * Perform an s-extension of this episode with an item
	 * 
	 * @param item    the item
	 * @param support the support
	 * @return the resulting episode
	 */
	public Episode sExtension(int item, int support) {
		List<int[]> newEvents = new ArrayList<int[]>(events);
		newEvents.add(new int[] { item });
		return new Episode(newEvents, support);
	}

	/**
	 * Get the last item of this episode (to be used only for 1-episode)
	 * 
	 * @return the last item
	 */
	public int getLastItem() {
		return events.get(0)[0];
	}

	/**
	 * Increase the support of this episode by 1.
	 */
	public void increaseSupport() {
		this.support++;
	}

	/**
	 * Get a string representation of this episode
	 * 
	 * @return a string
	 */
	public String toString() {
		String returnString = "";
		int episodeLength = events.size();
		for (int i = 0; i < episodeLength - 1; i++) {
//            returnString =returnString+ "< ";
			for (int j = 0; j < events.get(i).length - 1; j++) {
				returnString = returnString + String.valueOf(events.get(i)[j]) + " ";
			}
			returnString = returnString + String.valueOf(events.get(i)[events.get(i).length - 1]);
			returnString = returnString + " -1 ";
		}
//        returnString = returnString + "< ";
		for (int j = 0; j < events.get(episodeLength - 1).length - 1; j++) {
			returnString = returnString + String.valueOf(events.get(episodeLength - 1)[j]) + " ";
		}
		returnString = returnString
				+ String.valueOf(events.get(episodeLength - 1)[events.get(episodeLength - 1).length - 1]);
		returnString = returnString + " -1 ";
		returnString = returnString + "#SUP : " + String.valueOf(this.support);
		return returnString;
	}

}
