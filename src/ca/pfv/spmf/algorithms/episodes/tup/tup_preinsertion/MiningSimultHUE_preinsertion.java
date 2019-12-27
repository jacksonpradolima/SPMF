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
import java.util.List;

public class MiningSimultHUE_preinsertion {
	public static long newEpisodeSimult = 0;

	/**
	 * constructor of class
	 */
	public MiningSimultHUE_preinsertion(Episode_preinsertion e, String outputFile) {
	}

	/**
	 * method to get all simultaneous events of an episode
	 * 
	 * @param epi
	 *            is episode to be expanded simultaneouslly
	 * @param minOcc
	 *            is the minimal occurrence of epi
	 */
	public static List<Episode_preinsertion> getEvents(Episode_preinsertion epi,
			List<Episode_preinsertion.Occurrence> minOcc) {
		List<Episode_preinsertion> newEpiList = new ArrayList<Episode_preinsertion>();

		for (int i = 0; i < minOcc.size(); i++) {
			int end = minOcc.get(i).endTime;
			// System.out.println("end in min occ "+end);
			ArrayList<String> name = epi.getName();
			String last = name.get(name.size() - 1);
			String tokens[] = last.split(" ");
			Sequence_preinsertion seq = Database_preinsertion.getSequence(end);
			List<Episode_preinsertion> oneLengthEpisode = seq.getEpisodeSet();
			int index = seq.contains(tokens[tokens.length - 1]); // adding
																	// episodes
																	// that are
																	// succeed
																	// last
																	// event
			// System.out.println("index of last"+index);
			for (int j = index + 1; j < oneLengthEpisode.size(); j++) {
				if (!last.contains(oneLengthEpisode.get(j).getName().get(0))
						&& !newEpiList.contains(oneLengthEpisode.get(j)))
					newEpiList.add(oneLengthEpisode.get(j));
			}
		}
		return newEpiList;
	}

	/**
	 * method to concatenate two episodes simultaneously
	 * 
	 * @param epi
	 *            is episode to be expanded simultaneously
	 * @param minOcc
	 *            is the minimal occurrence of epi
	 */
	public static Episode_preinsertion newEpisode(Episode_preinsertion epi,
			List<Episode_preinsertion.Occurrence> epiMinOcc, Episode_preinsertion event) {
		long startTimestamp = System.currentTimeMillis();

		// System.out.println("old episode in simult "+epi+ " event "+e);

		// add the new event simultaneously to epi
		ArrayList<String> epiName = new ArrayList<String>(epi.getName());
		String lastEpi = new String(epiName.get(epiName.size() - 1));
		String newLastEpi = lastEpi + " " + event.getName().get(0);
		epiName.remove(epiName.size() - 1);
		epiName.add(newLastEpi);

		// creating the new episode simult-concat(epi,e)
		Episode_preinsertion newEpi = new Episode_preinsertion(epiName);

		List<Episode_preinsertion.Occurrence> minOccEvent = event.getMinOcc();
		int j = 0, k = 0;

		while (j < epiMinOcc.size()) {
			Episode_preinsertion.Occurrence minOccEpi = epiMinOcc.get(j);
			int epiStart = minOccEpi.startTime;
			int epiEnd = minOccEpi.endTime;
			k = 0;

			while (k < minOccEvent.size()) {
				Episode_preinsertion.Occurrence minimalOccEvent = minOccEvent.get(k);
				int eventEnd = minimalOccEvent.endTime;

				if (eventEnd == epiEnd) {
					Episode_preinsertion.Occurrence occ = newEpi.new Occurrence(epiStart, epiEnd);
					newEpi.addMinOcc(occ);
					// System.out.println(epi+" min occ epi "+minOccEpi+" event
					// "+e+"min occ "+minOccEvent.get(k));
					double utility = epi.getUtility(minOccEpi) + event.getUtility(minimalOccEvent);
					newEpi.addOccAndUtill(occ, utility);
					newEpi.addUtility(utility);
					break;
				}
				k++;
			}
			j++;
		}
		// System.out.println("new epi in simult " + newEpi);

		double ewu = newEpi.calculateEwu(newEpi.getMinOcc());
		newEpi.setEwu(ewu);

		long endTimestamp = System.currentTimeMillis();

		newEpisodeSimult += endTimestamp - startTimestamp;
		// System.out.println("time taken by simult" + (endTimestamp -
		// startTimestamp)+" msec ");
		return newEpi;
	}
}
