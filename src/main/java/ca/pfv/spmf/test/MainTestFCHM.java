package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoFCHM;

/**
 * Example of how to use the FCHM algorithm 
 * from the source code.
 * @author Philippe Fournier-Viger, 2014
 */
public class MainTestFCHM {

	public static void main(String [] arg) throws IOException{
		// input file
		String input = fileToPath("mushroom_utility.txt");
		// output file path
		String output = ".//output.txt";

		// minimum utility treshold
		int min_utility = 5;  
		// minimum bond
		double minbond = 0.5; // the minimum bond threhsold


		// Applying the HUIMiner algorithm
		AlgoFCHM algo = new AlgoFCHM();
		algo.runAlgorithm(input, output, min_utility, minbond);
		algo.printStats();
	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestFCHM.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
