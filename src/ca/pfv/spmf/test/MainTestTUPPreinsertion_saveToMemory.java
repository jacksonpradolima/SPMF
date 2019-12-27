package ca.pfv.spmf.test;
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

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Queue;

import ca.pfv.spmf.algorithms.episodes.tup.tup_preinsertion.AlgoTUP_preinsertion;
import ca.pfv.spmf.algorithms.episodes.tup.tup_preinsertion.Episode_preinsertion;
/**
 * This is an example of how to run the TUP(Preinsertion) algorithm and save the result to memory
 * 
 * @author Rathore et al. 2018
 *
 */
public class MainTestTUPPreinsertion_saveToMemory {

	public static void main(String[] args) throws UnsupportedEncodingException {

		// Maximum time duration
		int maximumTimeDuration = 2;
		// k 
		int k = 3;
		
		// input file
		String inputFile = fileToPath("exampleTUP.txt");

		AlgoTUP_preinsertion algorithm = new AlgoTUP_preinsertion();
		Queue<Episode_preinsertion> topKBuffer = algorithm.runAlgorithm(inputFile, maximumTimeDuration, k);
		
		algorithm.printStats();

		// Print the top-k episodes
		int i = 0;
		for (Episode_preinsertion episode : topKBuffer) {
			System.out.println("episode " + i + ":  " + episode.getFormattedName() + " #UTIL: " + episode.getUtility() + " #EWU: " + episode.ewu);
			i++;
		}
	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException {
		URL url = MainTestTUPPreinsertion_saveToMemory.class.getResource(filename);
		return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
	}
}
