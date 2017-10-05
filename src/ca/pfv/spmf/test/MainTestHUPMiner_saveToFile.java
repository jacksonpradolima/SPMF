package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoHUPMiner;

/**
 * Example of how to use the HUP-Miner algorithm 
 * from the source code, and save the output to a file.
 * @author Philippe Fournier-Viger, 2015
 */
public class MainTestHUPMiner_saveToFile {

	public static void main(String [] arg) throws IOException{
		
		// Set the input and output file path
		String input = fileToPath("DB_Utility.txt");
		String output = ".//output.txt";

		// Set the minimum utility threshold
		int min_utility = 30;  
		
		//Set  the number of partitions to be used
		// (note that this parameter influences the performance but do not influence the
		// output of the algorithm). What is a good value of K should be found
		// empirically for each dataset, to obtain optimal performance.
		int k = 2;  
		
		// Applying the HUIMiner algorithm
		AlgoHUPMiner algo = new AlgoHUPMiner();
		algo.runAlgorithm(input, output, min_utility, k);
		
		// Print statistics about the algorithm execution (time, memory used, etc.)
		algo.printStats();

	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestHUPMiner_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
