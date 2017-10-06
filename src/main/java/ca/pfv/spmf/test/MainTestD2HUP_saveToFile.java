package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.d2hup.AlgoD2HUP;

/**
 * Example of how to use the D2HUP algorithm 
 * from the source code and output the result to a file.
 * @author Philippe Fournier-Viger, 2015
 */
public class MainTestD2HUP_saveToFile {

	public static void main(String [] arg) throws IOException{

		// set the input and output file path
		String input = fileToPath("DB_Utility.txt");
		String output = ".//output.txt";

		// set the minimum utility threshold
		int min_utility = 30; 
		
		// Apply the algorithm
		AlgoD2HUP algo = new AlgoD2HUP();
		algo.runAlgorithm(input, output, min_utility);
		
		// Print statistics about the algorithm execution
		algo.printStats();
	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestD2HUP_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
