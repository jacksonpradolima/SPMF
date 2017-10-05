package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.upgrowth_ihup.AlgoUPGrowth;

/**
 * Example of how to use the UPGrowth algorithm 
 * from the source code.
 * @author (c) Prashant Barhate, 2014
 */
public class MainTestUPGrowth {

	public static void main(String [] arg) throws IOException{
		
		String input = fileToPath("DB_Utility.txt");
		String output = ".//output.txt";

		int min_utility = 30;  //
		
		// Applying the HUIMiner algorithm
		AlgoUPGrowth algo = new AlgoUPGrowth();
		algo.runAlgorithm(input, output, min_utility);
		algo.printStats();

	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestUPGrowth.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
