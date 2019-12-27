package ca.pfv.spmf.test;

import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.sequentialpatterns.skopus.AlgoSkopus;

/**
 * Example of how to use the SKOPUS algorithm from the source code.
 * @author Philippe Fournier-Viger, 2017.
 */
public class MainTestSkopus {

	public static void main(String [] arg) throws Exception{
		
		//------------ Parameters ----------------------//
		String input = fileToPath("contextPrefixSpan.txt");  
	 
	 	// output file path (for saving the patterns found 
		String output = ".//output.txt"; 
		
		//if true, the patterns will be found until the leverage interestingness measure instead of the support
		boolean useLeverageMeasureInsteadOfSupport = false; 
		
		// if true, show debug information
		boolean showDebugInformation = false;
		
		// if true, smoothed values will be used
		boolean useSmoothedValues = false;
		
		// if smoothing is used, this is the smoothing coefficient (e.g. 0.5)
		double smoothingCoefficient = 0.5;

		//  this is the maximum sequential pattern length
		int maximumSequentialPatternLength = Integer.MAX_VALUE;
		
		// this is the maximum sequential pattern length
		int k = 10;

		//--------------- Applying the  algorithm  ---------//
		AlgoSkopus algorithm = new AlgoSkopus();
		algorithm.runAlgorithm(input, output, useLeverageMeasureInsteadOfSupport, 
				showDebugInformation,
				useSmoothedValues, smoothingCoefficient, 
				maximumSequentialPatternLength, k);
		
		// Print statistics
		algorithm.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestSkopus.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
