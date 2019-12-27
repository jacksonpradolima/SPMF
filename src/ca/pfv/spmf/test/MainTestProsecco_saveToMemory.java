package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.sequentialpatterns.prefixspan.SequentialPatterns;
import ca.pfv.spmf.algorithms.sequentialpatterns.prosecco.AlgoProsecco;
import ca.pfv.spmf.algorithms.sequentialpatterns.prosecco.ProseccoCallbacks;

/**
 * Example of how to use the ProSecCo algorithm in source code.
 * @author Sacha Servan-Schreiber
 */
public class MainTestProsecco_saveToMemory {

	public static void main(String [] arg) throws IOException{   
		// input file
		String inputFile = fileToPath("contextPrefixSpan.txt");
		
		// output file path
		String outputPath = ".//output.txt";

		// called on each progressive output
		ProseccoCallbacks callback = new ProseccoCallbacks() {
			
			@Override
			public void blockUpdate(SequentialPatterns patterns, int numTransactionsProcessed, long blockRuntime,
					double blockErrorBound) {
				
				patterns.printFrequentPatterns(numTransactionsProcessed, false);
			}	
		};
		
		// create an instance of the algorithm with minsup = 50 %
		AlgoProsecco algo = new AlgoProsecco(callback); 
		
		int blockSize = 1; // number of transactions to process in each block
		int dbSize = 4; // number of transactions in the dataset
		double errorTolerance = 0.05; // failure probability
		double minsupRelative = 0.5; // 50%
		
		// execute the algorithm
		algo.runAlgorithm(inputFile, outputPath, blockSize, dbSize, errorTolerance, minsupRelative);    
		algo.printStatistics();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestProsecco_saveToMemory.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}