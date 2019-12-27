package ca.pfv.spmf.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.rpgrowth.AlgoRPGrowth;

/**
 * Example of how to use RPGrowth from source code and save to file.
 * @author Ryan Benton and Blake Johns
 */

public class MainTestRPGrowth_saveToFile {	
	public static void main(String [] arg) throws FileNotFoundException, IOException{
		//the file paths
		String input = fileToPath("contextRP.txt");	//the database
		String output = ".//output.txt";			//the path for saving rare itemsets
		
		double minsup = 0.6;		//using relative support: minsup = 3 counts
		double minraresup = 0.1;	//using relative support: minraresup = 1 count
		AlgoRPGrowth algo = new AlgoRPGrowth();		//instance of the algorithm
		
		// Uncomment the following line to set the maximum pattern length (number of items per itemset, e.g. 3 )
//		algo.setMaximumPatternLength(3);
		
		algo.runAlgorithm(input, output, minsup, minraresup); //running the algorithm
		algo.printStats(); //print the execution time and other stats
		
	}
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestFPGrowth_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
