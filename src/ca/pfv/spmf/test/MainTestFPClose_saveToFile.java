package ca.pfv.spmf.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth.AlgoFPClose;

/**
 * Example of how to use FPClose from the source code and
 * the result to a file.
 * @author Philippe Fournier-Viger (Copyright 2015)
 */
public class MainTestFPClose_saveToFile {

	public static void main(String [] arg) throws FileNotFoundException, IOException{
		// the file paths
		String input = fileToPath("contextPasquier99.txt");  // the database
		String output = ".//output.txt";  // the path for saving the frequent itemsets found
		
		double minsup = 0.4; // means a minsup of 2 transaction (we used a relative support)

		// Applying the algorithm
		AlgoFPClose algo = new AlgoFPClose();
		algo.runAlgorithm(input, output, minsup);
		algo.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestFPClose_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
