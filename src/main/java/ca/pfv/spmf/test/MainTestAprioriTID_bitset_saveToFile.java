package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.aprioriTID.AlgoAprioriTID_Bitset;

/**
 * Example of how to use the AprioriTID algorithm (bitset version), and
 * save the result to a file, from the source code.
 * 
 * @author Philippe Fournier-Viger 
 */
public class MainTestAprioriTID_bitset_saveToFile {

	public static void main(String [] arg) throws NumberFormatException, IOException{
		// Loading the binary context
		String input = fileToPath("contextPasquier99.txt");  // the database
		String output = ".//output.txt";  // the path for saving the frequent itemsets found
		
		double minsup = 0.4; // means a minsup of 2 transaction (we used a relative support)
		
		// Create an instance of the AprioriTID algorithm
		AlgoAprioriTID_Bitset algo = new AlgoAprioriTID_Bitset();
		
		// Set this variable to true to show the transaction identifiers where each pattern appear in the output file
//		algo.setShowTransactionIdentifiers(true);
		
		/// Run the algorithm 
		algo.runAlgorithm(input, output, minsup);
		
		// Print statistics
		algo.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestAprioriTID_bitset_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
