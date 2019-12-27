package ca.pfv.spmf.test;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Queue;

import ca.pfv.spmf.algorithms.episodes.tup.tup_combined.AlgoTUP_Combined;
import ca.pfv.spmf.algorithms.episodes.tup.tup_combined.Episode_preinsertion_EWU;

/**
 * This is an example of how to run the TUP(Combined) algorithm and save the result to memory
 * 
 * @author Rathore et al. 2018
 *
 */
public class MainTestTUPCombined_saveToMemory {

	public static void main(String[] args) throws UnsupportedEncodingException {
		
		// Maximum time duration
		int maximumTimeDuration = 2;
		// k 
		int k = 3;
		
		// input file
		String inputFile = fileToPath("exampleTUP.txt");
		
		AlgoTUP_Combined algorithm = new AlgoTUP_Combined();
		Queue<Episode_preinsertion_EWU> topKBuffer = algorithm.runAlgorithm(inputFile, maximumTimeDuration, k);
		
		algorithm.printStats();

		// Print the top-k episodes
		int i = 0;
		for (Episode_preinsertion_EWU episode : topKBuffer) {
			System.out.println("episode " + i + ":  " + episode.getFormattedName() + " #UTIL: " + episode.getUtility() + " #EWU: " + episode.ewu);
			i++;
		}
	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException {
		URL url = MainTestTUPCombined_saveToMemory.class.getResource(filename);
		return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
	}

}
