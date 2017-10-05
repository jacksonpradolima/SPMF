package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.aprioriTID_inverse.AlgoAprioriTIDInverse;


/**
 * Example of how to use the AprioriInverse  algorithm and save the output
 * to a file, from the source code (this is for the version of AprioriInverse based on AprioriTID).
 * 
 * @author Philippe Fournier-Viger (Copyright 2017)
 */
public class MainTestAprioriInverseTID_saveToFile {

	public static void main(String [] arg) throws IOException{
		// Loading a binary context
		String inputFilePath = fileToPath("contextInverse.txt");
		String outputFilePath = ".//output.txt";  // the path for saving the frequent itemsets found
		 
		// Note that we set the output file path to null because
		// we want to keep the result in memory instead of saving them
		// to an output file in this example.
		
		// the thresholds that we will use:
		double minsup = 0.001;
		double maxsup = 0.6;
		
		// Applying the APRIORI-Inverse algorithm to find sporadic itemsets
		AlgoAprioriTIDInverse algo = new AlgoAprioriTIDInverse();
		
        // if you set the following parameter to true, the sequence ids of the sequences where
        // each pattern appears will be shown in the result
        algo.setShowTransactionIdentifiers(false);
        
		// apply the algorithm
		algo.runAlgorithm(inputFilePath, outputFilePath, minsup, maxsup);
		algo.getDatabaseSize();
		algo.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestAprioriInverseTID_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
