package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoHUGMiner;

/**
 * Example of how to use the HUG-Miner algorithm 
 * from the source code.
 * @author Philippe Fournier-Viger, 2014
 */
public class MainTest_HUGMINER_saveToFile {

	public static void main(String [] arg) throws IOException{
		
		String input = fileToPath("DB_Utility.txt");

		int min_utility =  20; 
		
		String output = ".//output.txt";
		
		// Applying the HUIMiner algorithm
		AlgoHUGMiner hugMiner = new AlgoHUGMiner();
		hugMiner.runAlgorithm(input, output, min_utility);
		hugMiner.printStats();
	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTest_HUGMINER_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
