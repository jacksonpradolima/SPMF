package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.aprioriTID_rare.AlgoAprioriTIDrare;
import ca.pfv.spmf.algorithms.frequentpatterns.apriori_rare.AlgoAprioriRare;
import ca.pfv.spmf.patterns.itemset_array_integers_with_tids.Itemsets;

/**
 * Example of how to use the APRIORI RARE algorithm (TID version), from the source code.
 *  This version keeps the transaction identifiers of patterns in memory and
 * is based on AprioriTID instead of Apriori.
 * @author Philippe Fournier-Viger (Copyright 2017)
 */
public class MainTestAprioriRareTID_saveToMemory {

	public static void main(String [] arg) throws IOException{
		// Loading a binary context
		String inputFilePath = fileToPath("contextZart.txt");
		String outputFilePath = null;  
		// Note that we set the output file path to null because
		// we want to keep the result in memory instead of saving them
		// to an output file in this example.
		
		// the threshold that we will use:
		double minsup = 0.6;
		
		// Applying the APRIORI-Inverse algorithm to find sporadic itemsets
		AlgoAprioriTIDrare algo = new AlgoAprioriTIDrare();
		// apply the algorithm
		Itemsets patterns = algo.runAlgorithm(inputFilePath, outputFilePath, minsup);
		int databaseSize = algo.getDatabaseSize();
		patterns.printItemsets(databaseSize); // print the result
		algo.printStats();

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestAprioriRareTID_saveToMemory.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
