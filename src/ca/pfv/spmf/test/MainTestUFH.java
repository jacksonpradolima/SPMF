package ca.pfv.spmf.test;

import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.UFH.AlgoUFH;


/**
 * Example of how to run the UFH algorithm from the source code, and save the result to an output file.
 */
public class MainTestUFH {

	public static void main(String args[]) {
		//
		try {
			// the input and output files
			String input = fileToPath("DB_Utility.txt");
			String output = ".//output.txt";

			// the minimum utility threshold
			int min_utility = 30;

			// run the algorithm
			AlgoUFH algorithm = new AlgoUFH();
			algorithm.runAlgorithm(input, output, min_utility);
			algorithm.printStats();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String fileToPath(String filename)
			throws UnsupportedEncodingException {
		URL url = MainTestUPHist.class.getResource(filename);
		return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
	}
}
