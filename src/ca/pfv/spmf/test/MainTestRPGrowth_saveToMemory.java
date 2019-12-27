package ca.pfv.spmf.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.rpgrowth.AlgoRPGrowth;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;

/**
 * Example of how to use the RPGrowth from the source code
 * @author Ryan Benton and Blake Johns
 */

public class MainTestRPGrowth_saveToMemory {
	public static void main(String[] arg) throws FileNotFoundException, IOException{
		//load the transaction database
		String input = fileToPath("contextRP.txt");
		
		//threshold range [minimum rare (min) and minimum support (max)]
		double minsup = 0.6;
		double minraresup = 0.1;
		
		//Apply the RPGrowth algorithm
		AlgoRPGrowth algo = new AlgoRPGrowth();
		
		// Uncomment the following line to set the maximum pattern length (number of items per itemset, e.g. 3 )
//		algo.setMaximumPatternLength(3);
		
		//Run the algo
		//NOTE that here we use "null" as the output file path because we are saving to memory
		Itemsets patterns = algo.runAlgorithm(input, null, minsup, minraresup);
		algo.printStats();
		
		patterns.printItemsets(algo.getDatabaseSize());
	}
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestFPGrowth_saveToMemory.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
