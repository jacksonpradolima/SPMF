package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoMinFHM;

/**
 * Example of how to use the MinFHM algorithm from the source code.
 * @author Philippe Fournier-Viger, 2016
 */
public class MainTestMinFHM {

	public static void main(String [] arg) throws IOException{

		String input = fileToPath("DB_Utility.txt");
		String output = ".//output.txt";

		int min_utility = 30;  // 
		
		// Applying the  algorithm
		AlgoMinFHM algorithm = new AlgoMinFHM();
		algorithm.runAlgorithm(input, output, min_utility);
		algorithm.printStats();

	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestMinFHM.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
