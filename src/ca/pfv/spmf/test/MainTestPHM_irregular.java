package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoPHM;

/**
 * Example of how to use the PHM_irregular algorithm from the source code.
 * @author Philippe Fournier-Viger, 2016
 */
public class MainTestPHM_irregular {

	public static void main(String [] arg) throws IOException{

		String output = ".//output.txt";

		// =======================
		// EXAMPLE FROM THE ARTICLE : 
		String input = fileToPath("DB_UtilityPerHUIs.txt");
		int min_utility = 20;   
		// This is the regularity threshold (i.e. minimum periodicity (a number of transactions)
		int regularityThreshold = 2;  
		// =======================

		// Applying the PHM algorithm
		AlgoPHM algorithm = new AlgoPHM();
		
		// To disable some optimizations:
		//algorithm.setEnableEUCP(false); 
		
		// to set constraints on the length of patterns to be found
//		algorithm.setMinimumLength(1);
//		algorithm.setMaximumLength(5);
		
		// Run the algorithm
		algorithm.runAlgorithmIrregular(input, output, min_utility, regularityThreshold);
		
		// Print statistics about the execution of the algorithm
		algorithm.printStats();

	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestPHM_irregular.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
