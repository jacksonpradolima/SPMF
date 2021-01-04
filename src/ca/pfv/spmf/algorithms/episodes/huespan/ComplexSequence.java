package ca.pfv.spmf.algorithms.episodes.huespan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * A complex sequence that contains information about items and their utility
 * @author  Peng Yang
 * @see AlgoHUESpan
 */
public class ComplexSequence {

	/**
	 * a map with key: TID(timepoint) value: (events, eventsUtility, totalUtility)
	 */
	private Map<Integer, EventsEventsUtilityTotalUtility> mapItemEET;

	/** the largest time point in the sequence */
	private int largestTID;

	/** constructor */
	public ComplexSequence() {
		this.mapItemEET = new HashMap<>();
		this.largestTID = 0;
	}

	/**
	 * Get pairs for an i-extension
	 * 
	 * @param timePoint a time point
	 * @param lastItem  the last item
	 * @return the list of pairs for the i-extension.
	 */
	public List<int[]> getPairsForIextension(int timePoint, int lastItem) {

		List<int[]> returnPairsList = new ArrayList<>();
		List<int[]> pairs = this.mapItemEET.get(timePoint).getPairs();

		// the eventset has been sorted by the TWU(EWU for single candidate) or
		// alphabetical order
		for (int i = 1; i < pairs.size(); i++) {
			if (pairs.get(i - 1)[0] == lastItem) {
				// if the previous item equals to lastItem, then we can get from current index
				// to get the returnPairsList
				returnPairsList = pairs.subList(i, pairs.size());
				break;
			}
		}

		return returnPairsList;
	}

	/**
	 * get the sum utility of the items that the order is larger than the order of
	 * the item at the timePoint
	 * 
	 * @param timePoint
	 * @param item
	 * @return utility
	 */
	public int getIrutil(int timePoint, int item) {
		int iutil = 0;
		List<int[]> pairs = this.mapItemEET.get(timePoint).getPairs();
		for (int i = pairs.size() - 1; i > 0; i--) {
			// from backward to forward
			if (pairs.get(i)[0] != item) {
				// if the current item is not the 'item';, it means the order is larger than
				// 'item'
				iutil += pairs.get(i)[1];
			} else {
				break;
			}
		}
		return iutil;
	}

	/**
	 * This methods do four things: 1. According to maxDuration and
	 * minUtilityAbsolute measure, calculate the TWU of each event, 2. get the
	 * singleCandidates that thier TWU larger than minUtilityAbsolute (remove the
	 * non 1-candidate from mapSingleCandidatesWithMOs_Utility_Pair) 3. prune the
	 * non singleCandidates from the complex sequence 4. sort each event at each
	 * timepoint by the TWU order
	 * 
	 * @param maxDuration                             a maximum duration
	 * @param minUtilityAbsolute                      a minimum utility value
	 * @param mapSingleCandidatesWithMOsUtilityPair a map of
	 */
	public void pruneSingleEventsByUpperBound(int maxDuration, double minUtilityAbsolute,
			Map<Integer, AlgoHUESpan.MoListUtilityList> mapSingleCandidatesWithMOsUtilityPair,
			boolean useTigherUpperBound) {

		// 1. calculate active utility
		/**
		 * key: event, value: active utility = utility of active occurrence
		 */
		Map<Integer, Integer> mapEventWithActUtility = new HashMap<>();

		for (int TID = 1; TID <= this.largestTID; TID++) {
			EventsEventsUtilityTotalUtility eet = this.mapItemEET.get(TID);
			if (eet == null) {
				continue;
			}
			// active utility: the utility from TID-maxDuration+1 to TID+maxDuration-1
			int actUtility = this.getTotalUtilityOfDuration(TID - maxDuration + 1, TID + maxDuration - 1);
			for (int[] pair : eet.getPairs()) {
				// pair[0] : item , pair[1] : utility
				mapEventWithActUtility.put(pair[0], mapEventWithActUtility.getOrDefault(pair[0], 0) + actUtility);
			}
		}

		// 2. get the singleCandidates and their minimal occurrences

		// active utility remove
		for (int item : mapEventWithActUtility.keySet()) {
			int actUtility = mapEventWithActUtility.get(item);
//            System.out.println(item+": "+actUtility);
			if (actUtility < minUtilityAbsolute) {
				mapSingleCandidatesWithMOsUtilityPair.remove(item);
			}
		}

		// 3. remove the non 1-candidates from the complex sequence
		for (int TID = 1; TID <= this.largestTID; TID++) {
			EventsEventsUtilityTotalUtility eet = this.mapItemEET.get(TID);
			if (eet == null) {
				continue;
			}

			// pairs[pos][0] represent a event with the order pos
			// pairs[pos][1] represent the utility of the event with the order pos
			List<int[]> pairs = eet.getPairs();

			// record the sum of utility by removing
			int removedUtilitySum = 0;

			// remove event
			for (int i = pairs.size() - 1; i >= 0; i--) {
				int event = pairs.get(i)[0];
				if (!mapSingleCandidatesWithMOsUtilityPair.containsKey(event)) {
					// record the removed utility
					removedUtilitySum += pairs.get(i)[1];
					pairs.remove(i);
				}
			}

			if (pairs.size() > 0) {

				// 4. sort the pairs by the order of the upper-bound utility or alphabet order
				if (useTigherUpperBound) {
					// sort the pairs by the order of the active utility
					Collections.sort(pairs, new Comparator<int[]>() {
						@Override
						public int compare(int[] o1, int o2[]) {
							return mapEventWithActUtility.get(o1[0]) - mapEventWithActUtility.get(o2[0]);
						}
					});
				} else {
					// sort the pairs by the order of the alphabet order
					Collections.sort(pairs, new Comparator<int[]>() {
						@Override
						public int compare(int[] o1, int[] o2) {
							return o1[0] - o2[0];
						}
					});
				}

				eet.setPairs(pairs);
				eet.setTotalUtility(eet.getTotalUtility() - removedUtilitySum);
			} else {
				this.mapItemEET.remove(TID);
			}
		}

		mapEventWithActUtility.clear();

	}

	/**
	 * Get the total utility of all timepoints in the [start, end] contains start
	 * and end
	 * 
	 * @param start start timepoint
	 * @param end   end timepoint
	 * @return
	 */
	public int getTotalUtilityOfDuration(int start, int end) {

		if (start > this.largestTID) {
			return 0;
		}
		if (end > this.largestTID) {
			end = this.largestTID;
		}
		int totalUtility = 0;
		for (int TID = start; TID <= end; TID++) {
			if (this.mapItemEET.containsKey(TID)) {
				totalUtility += this.mapItemEET.get(TID).getTotalUtility();
			}
		}
		return totalUtility;
	}

	/**
	 * Get the maximal utility
	 * 
	 * @param episode an episode
	 * @param start   a start index
	 * @param end     an end index
	 * @return the maximal utility
	 */
	public int getMaximalUtility(List<int[]> episode, int start, int end) {
		int maximalUtility = 0;
		List<FiniteStateMachine> fsaList = new ArrayList<>();
		int utilityOfFirstEventSet = getEventsUtilityByTID(episode.get(0), start);
		fsaList.add(new FiniteStateMachine(episode, utilityOfFirstEventSet));

		for (int TID = start + 1; TID <= end; TID++) {
			if (!mapItemEET.containsKey(TID)) {
				continue;
			}
			List<int[]> pairs = this.mapItemEET.get(TID).getPairs();
			for (int j = fsaList.size() - 1; j >= 0; j--) {
				if (fsaList.get(j).scan(pairs)) {
					// current pairs contains current itemset
					fsaList.get(j).transit();
					if (j == fsaList.size() - 1) {
						// if it is last FSA, we need create a new FSA,
						fsaList.add(new FiniteStateMachine(episode, utilityOfFirstEventSet));
					}
					if (fsaList.get(j).isEnd()) {
						// if current FSA reach end state, and compare to the maximalUtility, and save
						// maximal
						maximalUtility = Math.max(maximalUtility, fsaList.get(j).getUtility());
						fsaList.remove(j);
					}
					if (j >= 1 && fsaList.get(j).isSame(fsaList.get(j - 1)) && !fsaList.get(j - 1).scan(pairs)) {
						// if previous FSA cannot do transit, and current FSA equals to previous FSA
						// then we save the FSA that it's utility is larger
						if (fsaList.get(j).getUtility() - fsaList.get(j - 1).getUtility() >= 0) {
							// if current FSA's utility is larger, then remove previous
							fsaList.remove(j - 1);
							j--; // to index the current FSA in the FSAlist
						} else {
							// if previous FSA's utility is larger, then remove current FSA
							fsaList.remove(j);
						}
					}
				}
			}
		}
		return maximalUtility;
	}

	/**
	 * Get the utility of an event for a given tid
	 * 
	 * @param eventset the event set
	 * @param tid      the tid
	 * @return the utility
	 */
	public int getEventsUtilityByTID(int[] eventset, int tid) {
		int utility = 0;
		int index = 0;
		List<int[]> pairs = this.mapItemEET.get(tid).getPairs();
		for (int j = 0; j < pairs.size(); j++) {
			if (index == eventset.length) {
				break;
			}
			int item = pairs.get(j)[0];
			if (item == eventset[index]) {
				index++;
				utility += pairs.get(j)[1];
			}
		}
		return index == eventset.length ? utility : 0;
	}

	/**
	 * Add a event with its utility to the sequence
	 * 
	 * @param tid     a transaction id
	 * @param event   an event
	 * @param utility the utility
	 */
	public void add(int tid, int event, int utility) {
		EventsEventsUtilityTotalUtility eet = this.mapItemEET.get(tid);
		if (eet == null) {
			eet = new EventsEventsUtilityTotalUtility();
			this.mapItemEET.put(tid, eet);
		}
		eet.add(event, utility);
	}

	/**
	 * Get an event set and its utility for a given TID
	 * 
	 * @param tid the transaction identifier
	 * @return the event set
	 */
	public List<int[]> getEventSetAndItsUtilityByTID(int tid) {
		return this.mapItemEET.containsKey(tid) ? this.mapItemEET.get(tid).getPairs() : new ArrayList<>();
	}

	/**
	 * Add the total utility (transaction utility) of TID to the sequence
	 * 
	 * @param tid          the transaction id
	 * @param totalUtility the total utility
	 */
	public void setTotalUtility(int tid, int totalUtility) {
		this.mapItemEET.get(tid).setTotalUtility(totalUtility);
	}

	/**
	 * set the largest TID
	 * 
	 * @param tid the transaction id
	 */
	public void setLargestTID(int tid) {
		this.largestTID = tid;
	}

	/**
	 * implements a class contains events, their utilities, and the totalUtility for
	 * one timepoint
	 */
	public class EventsEventsUtilityTotalUtility {
		/**
		 * An array of pairs where: pairs[pos][0] represent a event with the order pos
		 * pairs[pos][1] represent the utility of the event with the order pos
		 */
		List<int[]> pairs;

		/** the total utility */
		int totalUtility;

		/**
		 * Constructor
		 */
		public EventsEventsUtilityTotalUtility() {
			this.pairs = new ArrayList<>();
			this.totalUtility = 0;
		}

		/**
		 * Constructor with a list of pairs and a total utility
		 */
		public EventsEventsUtilityTotalUtility(List<int[]> pairs, int totalUtility) {
			this.pairs = pairs;
			this.totalUtility = totalUtility;
		}

		/**
		 * Add ane event and its utility to the list of pairs
		 * 
		 * @param event   an event
		 * @param utility its utility
		 */
		public void add(int event, int utility) {
			this.pairs.add(new int[] { event, utility });
		}

		/**
		 * Set the total utility
		 * 
		 * @param totalUtility the total utility
		 */
		public void setTotalUtility(int totalUtility) {
			this.totalUtility = totalUtility;
		}

		/**
		 * Get the total utility
		 * 
		 * @return the total utility
		 */
		public int getTotalUtility() {
			return totalUtility;
		}

		/**
		 * Get the list of pairs
		 * 
		 * @return the list of pairs
		 */
		public List<int[]> getPairs() {
			return pairs;
		}

		/**
		 * Set the list of pairs
		 * 
		 * @param pairs the list of pairs
		 */
		public void setPairs(List<int[]> pairs) {
			this.pairs = pairs;
		}

		/**
		 * Get the i-remaining utility by Event
		 * 
		 * @param event
		 * @return
		 */
		public int getIremainingUtilityByevent(int event) {
			int iRemainingUtility = 0;
			for (int i = this.pairs.size() - 1; i > 0; i--) {
				int[] pair = this.pairs.get(i);
				if (pair[0] > event) {
					iRemainingUtility += pair[1];
				} else {
					break;
				}
			}
			return iRemainingUtility;
		}

	}
}
