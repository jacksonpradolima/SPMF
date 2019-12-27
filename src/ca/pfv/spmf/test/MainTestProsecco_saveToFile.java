package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.sequentialpatterns.prosecco.AlgoProsecco;

/**
 * Example of how to use the ProSecCo algorithm in source code.
 * @author Sacha Servan-Schreiber
 */
public class MainTestProsecco_saveToFile {

	public static void main(String [] arg) throws IOException{   
		// input file
		String inputFile = fileToPath("contextPrefixSpan.txt");
		
		// output file path
		String outputPath = ".//output.txt";
		
		// create an instance of the algorithm with minsup = 50 %
		AlgoProsecco algo = new AlgoProsecco(); 
		
		int blockSize = 1; // number of transactions to process in each block
		int dbSize = 4; // number of transactions in the dataset
		double errorTolerance = 0.05; // failure probability
		double minsupRelative = 0.5; // 50%
		
		// execute the algorithm
		algo.runAlgorithm(inputFile, outputPath, blockSize, dbSize, errorTolerance, minsupRelative);    
		algo.printStatistics();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestProsecco_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}