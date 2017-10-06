package ca.pfv.spmf.test;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.foshu.AlgoFOSHU;

/**
 * Example of how to use the FOSHU algorithm  from the source code, and save the output to a file.
 * @author Philippe Fournier-Viger, 2014
 */
public class MainTestFOSHU_saveToFile {

	public static void main(String [] arg) throws IOException{

		// This shows how to run the FOSHU algorithm on the database described in the paper.
		String input = fileToPath("DB_FOSHU.txt");
		
		// Set a min-util ratio of 0.8
		double min_utility_ratio = 0.8d;
		
		// The result will be saved to a file "output.txt"
		String output = "output.txt";
		
		// Apply the algorithm
		AlgoFOSHU algo = new AlgoFOSHU();
		algo.runAlgorithm(input, output, min_utility_ratio);
		
		// The following line allows to specify how many lines from the input file should
		// be read. By default this parameter is set to Integer.MAX_VALUE (read the whole file).
//		algo.maxSEQUENCECOUNT =  9196;
	
		// Prin statistics about the algorithm execution
		algo.printStats();

	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestFOSHU_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
