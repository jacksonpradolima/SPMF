package ca.pfv.spmf.test;




import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.fin_prepost.FIN;


/**
 * Example of how to use FIN algorithm from the source code.
 * @author Philippe Fournier-Viger (Copyright 2014)
 */
public class MainTestFIN {

	public static void main(String [] arg) throws IOException{

		String input = fileToPath("contextPasquier99.txt");
		String output = ".//output.txt";  // the path for saving the frequent itemsets found
		
		double minsup = 0.3; // means a minsup of 2 transaction (we used a relative support)
		
		// Applying the algorithm
		FIN algorithm = new FIN();
		algorithm.runAlgorithm(input, minsup, output);
		algorithm.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestFIN.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
