package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.aprioriTID_rare.AlgoAprioriTIDrare;
import ca.pfv.spmf.algorithms.frequentpatterns.apriori_rare.AlgoAprioriRare;


/**
 * Example of how to use APRIORI-RARE (TID version) and save the output to a file,
 * from the source code. This version keeps the transaction identifiers of patterns in memory and
 * is based on AprioriTID instead of Apriori.
 * @author Philippe Fournier-Viger (Copyright 2017)
 */
public class MainTestAprioriRareTID_saveToFile {

	public static void main(String [] arg) throws IOException{
		//Input and output file paths
		String inputFilePath = fileToPath("contextZart.txt");
		String outputFilePath = ".//output.txt"; 
		
		// the threshold that we will use:
		double minsup = 0.6;
		
		// Applying the APRIORI-Inverse algorithm to find sporadic itemsets
		AlgoAprioriTIDrare algo = new AlgoAprioriTIDrare();
		
        // if you set the following parameter to true, the sequence ids of the sequences where
        // each pattern appears will be shown in the result
        algo.setShowTransactionIdentifiers(true);
		
		// apply the algorithm
		algo.runAlgorithm(inputFilePath, outputFilePath, minsup);
		algo.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestAprioriRareTID_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
