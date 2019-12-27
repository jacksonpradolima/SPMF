package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.dFIN.AlgoDFIN;


/**
 * Example of how to use the dFIN algorithm from the source code.
 * This code is similar to MainTestFIN.java.
 * @author Nader Aryabarzan (Copyright 2018)
 * @Email aryabarzan@aut.ac.ir or aryabarzan@gmail.com
 */
public class MainTestDFIN {

	public static void main(String [] arg) throws IOException{

		String input = fileToPath("contextPasquier99.txt");
		String output = "output.txt";  // the path for saving the frequent itemsets found
		
		double minsup = 0.4; // means a minsup of 2 transaction (we used a relative support)
		
		// Applying the algorithm
		AlgoDFIN algorithm = new AlgoDFIN();
		
		algorithm.runAlgorithm(input, minsup, output);
		algorithm.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestDFIN.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
