package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoEIHI;

/**
 * Example of how to use the EIHI algorithm for incremental high-utility itemset mining,
 * from the source code.
 * 
 * @author Philippe Fournier-Viger, 2015
 */
public class MainTestEIHI {

	public static void main(String[] arg) throws IOException {

		// Initialize the algorithm
		AlgoEIHI algo = new AlgoEIHI();

		// Set the minimum utility threshold
		int  min_utility = 30;

		// 1) Apply the algorithm on a first file containing transactions
		System.out.println("1) Run the algorithm on the first file");

		String input1 = fileToPath("DB_UtilityIncremental1.txt");
		algo.runAlgorithm(input1, min_utility, 0, Integer.MAX_VALUE);
		algo.printStats();
		
		// Print the number of HUIs found until now to the console
		int realHUICount =  algo.getRealHUICount();
		System.out.println("NUMBER OF HUI FOUND: " + realHUICount);
		
		// PRINT THE HUIs FOUND
		algo.printHUIs();

		// 2) Apply the algorithm on a second file containing transactions
		System.out.println("\n 2) Run the algorithm on the second file");
		
		// Applying the algorithm
		String input2 = fileToPath("DB_UtilityIncremental2.txt");
		algo.runAlgorithm(input2, min_utility, 0, Integer.MAX_VALUE);
		algo.printStats();
		
		// Print the number of HUIs found until now to the console
		realHUICount =  algo.getRealHUICount();
		System.out.println("NUMBER OF HUI FOUND: " + realHUICount);
		
		// PRINT THE HUIs FOUND
		algo.printHUIs();
		
		// PRINT THE TRIE FOR DEBUGGING
//		algo.printTrie();
		
		// WE CAN ALSO WRITE ALL THE HUIs found until now to a file at any time with
		// the following code
		String output = ".//output.txt";
		algo.writeHUIsToFile(output);
	}

	public static String fileToPath(String filename)
			throws UnsupportedEncodingException {
		URL url = MainTestEIHI.class.getResource(filename);
		return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
	}
}
