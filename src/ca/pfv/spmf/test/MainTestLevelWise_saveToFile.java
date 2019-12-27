package ca.pfv.spmf.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.levelwise.AlgoLevelWise;


/**
 * Example of how to use LevelWise from the source code and save
 * the resutls to a file.
 * @author Wu Cheng-Wei, Huang Jian-Tao, Lai Yi-Pei (Copyright 2018)
 */

public class MainTestLevelWise_saveToFile {

	public static void main(String [] arg) throws FileNotFoundException, IOException{
		// the file paths
		String input = fileToPath("contextMushroom_FCI90.txt");  // the database
		String output = ".//output.txt";  // the path for saving the frequent itemsets found

		// Applying the LevelWise algorithm AlgoDFIGrowth.java
		AlgoLevelWise algo = new AlgoLevelWise();
		algo.runAlgorithm(input,output);
		algo.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		 URL url = MainTestLevelWise_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
	
}
