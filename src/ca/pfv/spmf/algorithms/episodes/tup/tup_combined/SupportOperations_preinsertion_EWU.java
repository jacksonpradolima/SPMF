package ca.pfv.spmf.algorithms.episodes.tup.tup_combined;
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
import java.util.PriorityQueue;
import java.util.Queue;

public class SupportOperations_preinsertion_EWU {

	public static void callSimultHUE(Episode_preinsertion_EWU epi) {

		List<Episode_preinsertion_EWU.Occurrence> minOcc = epi.getMinOcc();
		List<Episode_preinsertion_EWU> betaEpisodeList = new ArrayList<Episode_preinsertion_EWU>();
		List<Episode_preinsertion_EWU> simultEpiList = MiningSimultHUE_preinsertion_EWU.getEvents(epi, minOcc);
		double minimumThreshold = AlgoTUP_Combined.getUtility();
		// System.out.println("list size "+simultEpiList.size());
		EWUComparator_preinsertion_EWU serialComparator = new EWUComparator_preinsertion_EWU();

		if (simultEpiList.size() != 0) {
			Queue<Episode_preinsertion_EWU> ewuQueue = new PriorityQueue<Episode_preinsertion_EWU>(simultEpiList.size(),
					serialComparator);

			for (Episode_preinsertion_EWU e : simultEpiList) {
				ewuQueue.add(e);
			}
			for (int i = 0; i < simultEpiList.size(); i++) {
				Episode_preinsertion_EWU e = ewuQueue.poll();
				// Episode e=simultEpiList.get(i);
				Episode_preinsertion_EWU beta = MiningSimultHUE_preinsertion_EWU.newEpisode(epi, minOcc, e);

				boolean epiPresent = false;
				if (beta.getName().size() == 1) {
					epiPresent = beta.simultEpiPresent();
					if (!epiPresent)
						Episode_preinsertion_EWU.singleLengthEpi.add(beta.getName().get(0));
				}

				if (!epiPresent) {

					betaEpisodeList.add(beta);
				}

			}

		}

		for (Episode_preinsertion_EWU betaEpi : betaEpisodeList) {

			double epiUtility = betaEpi.getUtility();
			if (epiUtility > AlgoTUP_Combined.getUtility())
				addToTopK(betaEpi, epiUtility);

			if (betaEpi.getEwu() >= minimumThreshold) {

				Episode_preinsertion_EWU.addEpisode(betaEpi);
				callSimultHUE(betaEpi);
				callSerialHUE(betaEpi);
			}

		}

	}

	public static void callSerialHUE(Episode_preinsertion_EWU epi) {

		List<Episode_preinsertion_EWU.Occurrence> minOcc = epi.getMinOcc();
		List<Episode_preinsertion_EWU> betaEpisodeList = new ArrayList<Episode_preinsertion_EWU>();
		List<Episode_preinsertion_EWU> serialEpiList = MiningSerialHUE_preinsertion_EWU.getEvents(epi, minOcc);
		double minimumThreshold = AlgoTUP_Combined.getUtility();

		if (serialEpiList.size() != 0) {
			EWUComparator_preinsertion_EWU serialComparator = new EWUComparator_preinsertion_EWU();
			Queue<Episode_preinsertion_EWU> ewuQueue = new PriorityQueue<Episode_preinsertion_EWU>(serialEpiList.size(),
					serialComparator);

			for (Episode_preinsertion_EWU e : serialEpiList) {
				ewuQueue.add(e);
			}

			for (int i = 0; i < serialEpiList.size(); i++) {

				// Episode e=serialEpiList.get(i);
				Episode_preinsertion_EWU e = ewuQueue.poll();
				Episode_preinsertion_EWU beta = MiningSerialHUE_preinsertion_EWU.newEpisode(epi, minOcc, e);

				betaEpisodeList.add(beta);

			}
		}

		for (Episode_preinsertion_EWU betaEpi : betaEpisodeList) {

			double epiUtility = betaEpi.getUtility();
			if (epiUtility > AlgoTUP_Combined.getUtility())
				addToTopK(betaEpi, epiUtility);

			if (betaEpi.getEwu() >= minimumThreshold) {
				Episode_preinsertion_EWU.addEpisode(betaEpi);
				callSimultHUE(betaEpi);

				callSerialHUE(betaEpi);
			}
		}

	}

	public static void addToTopK(Episode_preinsertion_EWU beta, double epiUtility) {

		if (TUPCGlobalVariables.topKBuffer.size() < TUPCGlobalVariables.k)
			TUPCGlobalVariables.topKBuffer.add(beta);
		else {
			Episode_preinsertion_EWU e = TUPCGlobalVariables.topKBuffer.peek();
			AlgoTUP_Combined.setUtility(e.getUtility());

			if (beta.utility > AlgoTUP_Combined.getUtility()) {
				TUPCGlobalVariables.topKBuffer.poll();
				TUPCGlobalVariables.topKBuffer.add(beta);
				e = TUPCGlobalVariables.topKBuffer.peek();
				AlgoTUP_Combined.setUtility(e.getUtility());
				/*
				 * System.out.println("top k list" + test.topK.size() +
				 * "utility "+ e.getUtility());
				 */
			}
		}

	}
}
