package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoFCHM_all_confidence;

/**
 * Example of how to use the FCHM_allconfidence algorithm 
 * from the source code.
 * 
 * @author Philippe Fournier-Viger and Yimin Zhang, 2018
 */
public class MainTestFCHM_allconfidence {

	public static void main(String [] arg) throws IOException{
		// input file
		String input = fileToPath("DB_Utility.txt");
		// output file path
		String output = ".//output.txt";

		// minimum utility treshold
		int min_utility = 30;  
		
		// minimum bond
		double minAllConfidence = 0.5; // the minAllConfidence threhsold

		// Applying the HUIMiner algorithm
		AlgoFCHM_all_confidence algo = new AlgoFCHM_all_confidence();
		algo.runAlgorithm(input, output, min_utility, minAllConfidence);
		algo.printStats();
	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestFCHM_allconfidence.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
