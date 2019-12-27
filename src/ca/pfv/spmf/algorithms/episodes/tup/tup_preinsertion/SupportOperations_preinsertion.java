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

public class SupportOperations_preinsertion {

	public static void callSimultHUE(Episode_preinsertion epi) {

		List<Episode_preinsertion.Occurrence> minOcc = epi.getMinOcc();
		List<Episode_preinsertion> betaEpisodeList = new ArrayList<Episode_preinsertion>();
		List<Episode_preinsertion> simultEpiList = MiningSimultHUE_preinsertion.getEvents(epi, minOcc);
		double minimumThreshold = AlgoTUP_preinsertion.getUtility();
		// System.out.println("list size "+simultEpiList.size());

		if (simultEpiList.size() != 0) {
			for (int i = 0; i < simultEpiList.size(); i++) {
				Episode_preinsertion e = simultEpiList.get(i);
				Episode_preinsertion beta = MiningSimultHUE_preinsertion.newEpisode(epi, minOcc, e);

				boolean epiPresent = false;
				if (beta.getName().size() == 1) {
					epiPresent = beta.simultEpiPresent();
					if (!epiPresent)
						Episode_preinsertion.singleLengthEpi.add(beta.getName().get(0));
				}

				if (!epiPresent) {

					betaEpisodeList.add(beta);
				}

			}

		}

		for (Episode_preinsertion betaEpi : betaEpisodeList) {

			double epiUtility = betaEpi.getUtility();
			if (epiUtility > AlgoTUP_preinsertion.getUtility())
				addToTopK(betaEpi, epiUtility);

			if (betaEpi.getEwu() >= minimumThreshold) {

				Episode_preinsertion.addEpisode(betaEpi);
				callSimultHUE(betaEpi);
				callSerialHUE(betaEpi);
			}

		}

	}

	public static void callSerialHUE(Episode_preinsertion epi) {

		List<Episode_preinsertion.Occurrence> minOcc = epi.getMinOcc();
		List<Episode_preinsertion> betaEpisodeList = new ArrayList<Episode_preinsertion>();
		List<Episode_preinsertion> serialEpiList = MiningSerialHUE_preinsertion.getEvents(epi, minOcc);
		double minimumThreshold = AlgoTUP_preinsertion.getUtility();

		if (serialEpiList.size() != 0) {

			for (int i = 0; i < serialEpiList.size(); i++) {

				Episode_preinsertion e = serialEpiList.get(i);
				Episode_preinsertion beta = MiningSerialHUE_preinsertion.newEpisode(epi, minOcc, e);

				betaEpisodeList.add(beta);

			}
		}

		for (Episode_preinsertion betaEpi : betaEpisodeList) {

			double epiUtility = betaEpi.getUtility();
			if (epiUtility > AlgoTUP_preinsertion.getUtility())
				addToTopK(betaEpi, epiUtility);

			if (betaEpi.getEwu() >= minimumThreshold) {
				Episode_preinsertion.addEpisode(betaEpi);
				callSimultHUE(betaEpi);

				callSerialHUE(betaEpi);
			}
		}

	}

	public static void addToTopK(Episode_preinsertion beta, double epiUtility) {

		if (TUPPGlobalVariables.topKBuffer.size() < TUPPGlobalVariables.k)
			TUPPGlobalVariables.topKBuffer.add(beta);
		else {
			Episode_preinsertion e = TUPPGlobalVariables.topKBuffer.peek();
			AlgoTUP_preinsertion.setUtility(e.getUtility());

			if (beta.utility > AlgoTUP_preinsertion.getUtility()) {
				TUPPGlobalVariables.topKBuffer.poll();
				TUPPGlobalVariables.topKBuffer.add(beta);
				e = TUPPGlobalVariables.topKBuffer.peek();
				AlgoTUP_preinsertion.setUtility(e.getUtility());
				/*
				 * System.out.println("top k list" + test.topK.size() +
				 * "utility "+ e.getUtility());
				 */
			}
		}

	}
}
