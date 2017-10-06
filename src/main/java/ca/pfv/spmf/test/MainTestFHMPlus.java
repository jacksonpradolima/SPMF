package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoFHMPlus;

/**
 * Example of how to use the FHM+ algorithm 
 * from the source code.
 * @author Philippe Fournier-Viger, 2016
 */
public class MainTestFHMPlus {

	public static void main(String [] arg) throws IOException{
		
		String input = fileToPath("DB_Utility.txt");
		String output = ".//output.txt";

		// minimum utility threshold
		int min_utility = 30;  
		
		// minimum and maximum length
		int minimumLength = 2; 
		int maximumLength = 3; 
		
		// Applying the algorithm
		AlgoFHMPlus algo = new AlgoFHMPlus();
		algo.runAlgorithm(input, output, min_utility, minimumLength, maximumLength);
		algo.printStats();
	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestFHMPlus.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
