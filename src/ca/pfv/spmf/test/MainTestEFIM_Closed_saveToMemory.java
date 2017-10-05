package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.efim_closed.AlgoEFIMClosed;
import ca.pfv.spmf.algorithms.frequentpatterns.efim_closed.Itemsets;




/**
 * Example of how to run the EFIM-Closed algorithm from the source code, and keep the result in memory
 * @author Philippe Fournier-Viger, 2016
 */
public class MainTestEFIM_Closed_saveToMemory {

	public static void main(String [] arg) throws IOException{

		// the input and output file paths
		String input = fileToPath("DB_Utility.txt");
		
		// the minutil threshold
		int minutil = 30; 

		// Run the EFIM algorithm
		AlgoEFIMClosed algo = new AlgoEFIMClosed();
		Itemsets itemsets = algo.runAlgorithm(minutil,  input, null, true, Integer.MAX_VALUE, true, true);
		// Print statistics
		algo.printStats();

		// Print the itemsets
		itemsets.printItemsets();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestEFIM_Closed_saveToMemory.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
