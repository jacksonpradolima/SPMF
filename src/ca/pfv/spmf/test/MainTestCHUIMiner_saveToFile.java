package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoCHUIMiner;


/**
 * Example of how to use the CHUI-Miner algorithm 
 * from the source code and save the result to file.
 * @author Philippe Fournier-Viger, 2014
 */
public class MainTestCHUIMiner_saveToFile {

	public static void main(String [] arg) throws IOException{
		// input file path
		String input = fileToPath("DB_Utility.txt");
		// the minutility threshold
		int min_utility = 25;   
		
		// output file path
		String output = ".//CHUIs.txt";
		
		// (1) Applying the CHUI-Miner algorithm to find 
		// closed high utility itemsets (CHUIs)
		AlgoCHUIMiner chuiMiner = new AlgoCHUIMiner(true);
		chuiMiner.runAlgorithm(input, min_utility, output);
		chuiMiner.printStats();
	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestCHUIMiner_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
