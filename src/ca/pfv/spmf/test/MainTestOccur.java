package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.sequentialpatterns.occur.AlgoOccur;


/**
 * Example of how to use the PrefixSpan algorithm in source code.
 * @author Philippe Fournier-Viger
 */
public class MainTestOccur {

	public static void main(String [] arg) throws IOException{   
		// input file - database
		String databaseFile = fileToPath("contextPrefixSpan.txt");

		// input file - patterns
		String patternFile = fileToPath("spmPatterns.txt");
		
		// output file path
		String outputPath = ".//output.txt";

		// Create an instance of the algorithm with minsup = 50 %
		AlgoOccur algo = new AlgoOccur(); 
		
		// execute the algorithm
		algo.runAlgorithm(databaseFile, patternFile, outputPath);    
		algo.printStatistics();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestOccur.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}