package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.apriori.AlgoApriori;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;

/**
 * Example of how to use APRIORI
 *  algorithm from the source code.
 * @author Philippe Fournier-Viger (Copyright 2008)
 */
public class MainTestApriori_saveToMemory {

	public static void main(String [] arg) throws IOException{

		String input = fileToPath("contextPasquier99.txt");
		String output = null;
		// Note : we here set the output file path to null
		// because we want that the algorithm save the 
		// result in memory for this example.
		
		double minsup = 0.4; // means a minsup of 2 transaction (we used a relative support)
		
		// Applying the Apriori algorithm
		AlgoApriori algorithm = new AlgoApriori();
		
		// Uncomment the following line to set the maximum pattern length (number of items per itemset, e.g. 3 )
//		apriori.setMaximumPatternLength(3);
		
		Itemsets result = algorithm.runAlgorithm(minsup, input, output);
		algorithm.printStats();
		result.printItemsets(algorithm.getDatabaseSize());
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		System.out.println("filename : " + filename);
		URL url = MainTestApriori_saveToMemory.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
