package ca.pfv.spmf.test;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.tshoun.AlgoTSHoun;
import ca.pfv.spmf.algorithms.frequentpatterns.tshoun.DatabaseWithPeriods;

/**
 * Example of how to use the TS-Houn in source code, and save the output to a file.
 * @author Philippe Fournier-Viger, 2014
 */
public class MainTestTSHOUN_saveToFile {

	public static void main(String [] arg) throws IOException{

		
		String input = fileToPath("DB_FOSHU.txt");
		double min_utility_ratio = 0.8d;
		int periodCount = 3;

		String output = "./output.txt";

		// Loading the database into memory
		DatabaseWithPeriods database = new DatabaseWithPeriods(periodCount);
		database.loadFile(input);
		
		// Applying the algorithm
		AlgoTSHoun algo = new AlgoTSHoun();
		algo.runAlgorithm(database, min_utility_ratio, output, periodCount);
		algo.printStats();

	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestTSHOUN_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
