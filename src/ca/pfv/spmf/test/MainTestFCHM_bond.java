package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoFCHM_bond;

/**
 * Example of how to use the FCHM_bond algorithm 
 * from the source code.
 * @author Philippe Fournier-Viger and Yimin Zhang, 2018
 */
public class MainTestFCHM_bond {

	public static void main(String [] arg) throws IOException{
		// input file
		String input = fileToPath("DB_utility.txt");
		// output file path
		String output = ".//output.txt";

		// minimum utility treshold
		int min_utility = 30;  
		// minimum bond
		double minbond = 0.5; // the minimum bond threhsold

		// Applying the HUIMiner algorithm
		AlgoFCHM_bond algo = new AlgoFCHM_bond();
		algo.runAlgorithm(input, output, min_utility, minbond);
		algo.printStats();
	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestFCHM_bond.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
