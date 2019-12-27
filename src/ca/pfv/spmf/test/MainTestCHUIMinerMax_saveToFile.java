package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoCHUIMinerMax;


/**
 * Example of how to use the CHUI-Miner(MAX) algorithm 
 * from the source code and save the result to file.
 * @author Philippe Fournier-Viger, 2014
 */
public class MainTestCHUIMinerMax_saveToFile {

	public static void main(String [] arg) throws IOException{
		// input file path
		String input = fileToPath("DB_Utility.txt");
		
		// the minutility threshold
		int min_utility = 25;   
		
		// output file path
		String output = ".//output.txt";
		
		// (1) Applying the algorithm to find 
		// maximal high utility itemsets (CHUIs)
		AlgoCHUIMinerMax algorithm = new AlgoCHUIMinerMax(true);
		algorithm.runAlgorithm(input, min_utility, output);
		algorithm.printStats();
	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestCHUIMinerMax_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
