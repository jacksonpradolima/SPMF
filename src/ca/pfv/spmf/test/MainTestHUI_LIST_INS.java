package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoHUI_LIST_INS;

/**
 * Example of how to use the HUI-LIST-INS algorithm for incremental high-utility itemset mining,
 * from the source code.
 * 
 * @author Philippe Fournier-Viger, 2015
 */
public class MainTestHUI_LIST_INS {

	public static void main(String[] arg) throws IOException {
		
		// Set the output file path
		String output = ".//output.txt";

		// Initialize the algorithm
		AlgoHUI_LIST_INS algo = new AlgoHUI_LIST_INS(); 

		// Set the minimum utility threshold
		int  min_utility = 30;

		// 1) Apply the algorithm on a first file containing transactions
		System.out.println("1) Run the algorithm on the first file");

		String input1 = fileToPath("DB_UtilityIncremental1.txt");
		algo.runAlgorithm(input1, output, min_utility, 0, Integer.MAX_VALUE);
		algo.printStats();
		
		// The result has been save to the file output.txt
		
		// Print the number of HUIs found until now to the console
		System.out.println("NUMBER OF HUI FOUND: " +  algo.huiCount);
		
		// 2) Apply the algorithm on a second file containing transactions
		System.out.println("\n 2) Run the algorithm on the second file");
		
		// Applying the algorithm
		String input2 = fileToPath("DB_UtilityIncremental2.txt");
//		algo.runAlgorithm(input2, output, min_utility, 0, Integer.MAX_VALUE);
		algo.printStats();
		
		// The result has been save to the file output.txt, and has overwritten the previous result.
		
		// Print the number of HUIs found until now to the console
		System.out.println("NUMBER OF HUI FOUND: " +  algo.huiCount);
		
		// close output file
		algo.closeFile();

		
	}

	public static String fileToPath(String filename)
			throws UnsupportedEncodingException {
		URL url = MainTestHUI_LIST_INS.class.getResource(filename);
		return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
	}
}
