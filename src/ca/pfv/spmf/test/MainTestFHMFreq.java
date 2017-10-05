package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoFHM_Freq;

/**
 * Example of how to use the FHM-Freq algorithm 
 * from the source code.
 * @author Philippe Fournier-Viger, 2014
 */
public class MainTestFHMFreq {

	public static void main(String [] arg) throws IOException{
		
		String input = fileToPath("DB_Utility.txt");
		String output = ".//output.txt";

		int min_utility = 30;  //
		double minsup = 0.1; // which means 40 % of the database size.
		
		// Applying the HUIMiner algorithm
		AlgoFHM_Freq fhmfreq = new AlgoFHM_Freq();
		fhmfreq.runAlgorithm(input, output, min_utility, minsup);
		fhmfreq.printStats();

	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestFHMFreq.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
