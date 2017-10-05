package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.efim.AlgoEFIM;


/**
 * Example of how to run the EFIM algorithm from the source code, and save the result to an output file.
 * @author Philippe Fournier-Viger, 2015
 */
public class MainTestEFIM_saveToFile {

	public static void main(String [] arg) throws IOException{

		// the input and output file paths
		String input = fileToPath("DB_Utility.txt");
		String output = ".//output.txt";
		
		// the minutil threshold
		int minutil = 30; 

		// Run the EFIM algorithm
		AlgoEFIM algo = new AlgoEFIM();
		algo.runAlgorithm(minutil,  input, output, true, Integer.MAX_VALUE, true);
		// Print statistics
		algo.printStats();

		
		// if true in next line it will find only closed itemsets, otherwise, all frequent itemsets
		//===================
//		Itemsets itemsets = algo.runAlgorithm(minutil,  input, null, activateMerging, maximumNumberOfTransactions, activateSubTreeUtilityPruning);
//		//==========================
//		itemsets.printItemsets();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestEFIM_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
