package ca.pfv.spmf.test;

import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.sequentialpatterns.qcsp.AlgoQCSP;

/**
 * Example of how to use the QCSP algorithm from the source code.
 * @author Len Feremans, 2020.
 */
public class MainTestQCSP_multipleSequences {

	public static void main(String [] arg) throws Exception{
		
		//------------ Parameters ----------------------//
		//example from goKrimp for multiple sequence
		String input = fileToPath("test_goKrimp.dat");   //dat-lab format, and multiple sequences
		String labels = fileToPath("test_goKrimp.lab");  
		
		// if true, show debug information
		boolean showDebugInformation = false;
		
		//save output
		String output = "./output_jmlr.txt";
		
		// frequency threshold on single item
		int minsup = 4;
		
		// threshold on window, relative to pattern length. alpha=1 -> no gaps, 
		// alpha=2  -> |X| * 2 gaps allowed for (quantile-based cohesive) sequential pattern occurrence
		double alpha = 2;

		//  this is the maximum sequential pattern length
		int maximumSequentialPatternLength = 6;
		
		// top-k sequential patterns, ranked on quantile-based cohesion, to return
		int topK = 25;

		//--------------- Applying the  algorithm  ---------//
		AlgoQCSP algorithm = new AlgoQCSP();
		algorithm.setDebug(showDebugInformation);
		algorithm.setLabelsFile(labels);
		algorithm.runAlgorithm(input, output, minsup, alpha, maximumSequentialPatternLength, topK);

		// Print statistics
		algorithm.printStatistics();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestQCSP_multipleSequences.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
