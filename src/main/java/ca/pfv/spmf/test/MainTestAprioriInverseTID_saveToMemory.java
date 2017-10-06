package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.aprioriTID_inverse.AlgoAprioriTIDInverse;
import ca.pfv.spmf.algorithms.frequentpatterns.apriori_inverse.AlgoAprioriInverse;
import ca.pfv.spmf.patterns.itemset_array_integers_with_tids.Itemsets;

/**
 * Example of how to use the APRIORI-INVERSE algorithm from the source
 * code (this is for the version of AprioriInverse based on AprioriTID).
 * 
 * @author Philippe Fournier-Viger (Copyright 2017)
 */
public class MainTestAprioriInverseTID_saveToMemory {

	public static void main(String [] arg) throws IOException{
		// Loading a binary context
		String inputFilePath = fileToPath("contextInverse.txt");
		String outputFilePath = null;  
		// Note that we set the output file path to null because
		// we want to keep the result in memory instead of saving them
		// to an output file in this example.
		
		// the thresholds that we will use:
		double minsup = 0.001;
		double maxsup = 0.6;
		
		// Applying the APRIORI-Inverse algorithm to find sporadic itemsets
		AlgoAprioriTIDInverse apriori2 = new AlgoAprioriTIDInverse();
		// apply the algorithm
		Itemsets patterns = apriori2.runAlgorithm(inputFilePath, outputFilePath, minsup, maxsup);
		int databaseSize = apriori2.getDatabaseSize();
		patterns.printItemsets(databaseSize); // print the result
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestAprioriInverseTID_saveToMemory.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
