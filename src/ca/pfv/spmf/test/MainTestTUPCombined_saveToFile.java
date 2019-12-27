package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.episodes.tup.tup_combined.AlgoTUP_Combined;

/**
 * This is an example of how to run the TUP(Combined) algorithm and save the result to a file
 * 
 * @author Rathore et al. 2018
 *
 */
public class MainTestTUPCombined_saveToFile {

	public static void main(String[] args) throws IOException {
		
		// Maximum time duration
		int maximumTimeDuration = 2;
		// k 
		int k = 3;
		
		// input file
		String inputFile = fileToPath("exampleTUP.txt");

		// output file
		String outputFile = "output.txt";
		
		AlgoTUP_Combined algorithm = new AlgoTUP_Combined();
		algorithm.runAlgorithm(inputFile, maximumTimeDuration, k);
		algorithm.writeResultTofile(outputFile);
		
		algorithm.printStats();

	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException {
		URL url = MainTestTUPCombined_saveToFile.class.getResource(filename);
		return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
	}

}
