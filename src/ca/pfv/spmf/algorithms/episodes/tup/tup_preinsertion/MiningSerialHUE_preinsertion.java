package ca.pfv.spmf.algorithms.episodes.tup.tup_preinsertion;
/* This file is copyright (c) Rathore et al. 2018
* 
* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
* 
* SPMF is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* 
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MiningSerialHUE_preinsertion {
	public static long newEpisodeSerial = 0;

	/**
	 * method to get all serial events of an episode
	 * 
	 * @param epi
	 *            is episode to be expanded serially
	 * @param minOcc
	 *            is the minimal occurrence of epi
	 */
	public static List<Episode_preinsertion> getEvents(Episode_preinsertion epi,
			List<Episode_preinsertion.Occurrence> minOcc) {
		List<Episode_preinsertion> newEpiList = new ArrayList<Episode_preinsertion>();
		for (int i = 0; i < minOcc.size(); i++) {
			int start = minOcc.get(i).startTime;
			int end = minOcc.get(i).endTime;
			// System.out.println(" minocc "+minOcc.get(i));
			for (int a = end + 1; a <= (start + AlgoTUP_preinsertion.getMaxTimeDuration() - 1); a++) {
				Sequence_preinsertion seq = Database_preinsertion.getSequence(a);
				if (seq != null) {
					List<Episode_preinsertion> eve = seq.getEpisodeSet();

					for (Episode_preinsertion e : eve) {
						if (// !epi.equals(e.getName().get(0))
							// &&
						!newEpiList.contains(e))
							newEpiList.add(e);
					}
				}
			}
		}
		return newEpiList;
	}

	/**
	 * method to concatenate two episodes serially
	 * 
	 * @param epi
	 *            is episode to be expanded serially
	 * @param minOcc
	 *            is the minimal occurrence of epi
	 */
	public static Episode_preinsertion newEpisode(Episode_preinsertion epi,
			List<Episode_preinsertion.Occurrence> epiMinOcc, Episode_preinsertion event) {

		// System.out.println("new epi "+e);
		ArrayList<String> epiName = new ArrayList<String>(epi.getName());
		epiName.add(event.getName().get(0));
		Episode_preinsertion newEpi = new Episode_preinsertion(epiName);
		ArrayList<Episode_preinsertion.Occurrence> occList = new ArrayList<Episode_preinsertion.Occurrence>();
		List<Episode_preinsertion.Occurrence> minOccSingleLengthEpi = event.getMinOcc();
		int j = 0, k = 0;
		Map<Episode_preinsertion.Occurrence, Double> occUtillMap = new HashMap<Episode_preinsertion.Occurrence, Double>();

		while (j < epiMinOcc.size()) {
			k = 0;
			Episode_preinsertion.Occurrence epiOcc = epiMinOcc.get(j);
			int start = epiOcc.startTime;
			int end = epiOcc.endTime;
			// System.out.println(e+"event "+minOccEvent);
			while (k < minOccSingleLengthEpi.size()) {
				Episode_preinsertion.Occurrence minOccEvent = minOccSingleLengthEpi.get(k);
				int eventStart = minOccEvent.startTime;

				if (start != eventStart && start < eventStart && end < eventStart
						&& (eventStart - start + 1) <= AlgoTUP_preinsertion.getMaxTimeDuration()) {
					Episode_preinsertion.Occurrence occ = newEpi.new Occurrence(start, eventStart);
					occList.add(occ);
					double utility = epi.getUtility(epiOcc) + event.getUtility(minOccEvent);
					occUtillMap.put(occ, utility);
				}
				k++;
			}
			j++;
		}
		/*
		 * System.out.println(newEpi + " new epi in serial , occurrence : " +
		 * newEpi.getOcc());
		 */

		newEpi.setMinOccSerial(occList, occUtillMap);
		double ewu = newEpi.calculateEwu(newEpi.getMinOcc());

		newEpi.setEwu(ewu);

		return newEpi;

	}
}
