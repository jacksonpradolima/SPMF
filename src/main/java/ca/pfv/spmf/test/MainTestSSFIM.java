package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.ssfim.AlgoSSFIM;

/**
 * Example of how to use SSFIM from the source code.
 * @author Philippe Fournier-Viger, 2017.
 */
public class MainTestSSFIM {

	public static void main(String [] arg) throws IOException{
		
		// the database
		String input = fileToPath("contextPasquier99.txt");  
		
		// the path for saving the frequent itemsets found
		String output = ".//output.txt";  
		
		// 40% means a minsup of 2 transaction (we used a relative support)
		double minsup = 0.4; 
		
		// Applying the  algorithm
		AlgoSSFIM algorithm = new AlgoSSFIM();
		algorithm.runAlgorithm(input, output, minsup);
		algorithm.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestSSFIM.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
