package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.huim_abc.AlgoHUIM_ABC;

/**
 * Discovering High Utility Itemsets Based on the Artificial Bee Colony
 * Algorithm
 * 
 * @author Wei Song,Chaomin Huang
 */

public class MainTestHUIM_ABC {
	public static void main(String[] arg) throws IOException {

		String input = fileToPath("contextHUIM.txt");

		String output = ".//output.txt";

		int min_utility = 40;

		AlgoHUIM_ABC huim_abc = new AlgoHUIM_ABC();
		
		// Set the bucket number to 2.  The bucket number is important
		// It should be adjusted with regards to the number of items in the input database
		huim_abc.setBucketNum(2); 
		
		// Run the algorithm
		huim_abc.runAlgorithm(input, output, min_utility);
		huim_abc.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestHUIM_BPSO.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
