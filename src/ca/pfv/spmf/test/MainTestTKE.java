package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.episodes.emma.AlgoTKE;

/**
 * This file shows how to run the TKE algorithm on an input file.
 * 
 * @author Philippe Fournier-Viger et al.
 */
public class MainTestTKE {
	public static void main(String[] args) throws IOException {

		// the Input and output files
		String inputFile = fileToPath("contextEmma.txt");
		String outputFile = "output.txt";

		// The algorithm parameters:
		int k = 6;
		int maxWindow = 2;

		// If the input file does not contain timestamps, then set this variable to true
		// to automatically assign timestamps as 1,2,3...
		boolean selfIncrement = false;
		
		// Activate the dynamic search optimization  (it improves performance)
		boolean useDynamicSearch = true;

		AlgoTKE algo = new AlgoTKE();
		algo.setUseDynamicSearch(useDynamicSearch);
				
		algo.runAlgorithm(inputFile, outputFile, k, maxWindow, selfIncrement);
		algo.printStats();
		
		System.out.println(45 % 10);
	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException {
		URL url = MainTestTKE.class.getResource(filename);
		return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
	}
}
