package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.MSAprioriSrinivas.AlgoMSAprioriSrinivas;

/**
 *  Example of how to use the MSAPRIORI(Srinivas) algorithm in source code.
 */
public class MainTestMSAprioriSrinivas_saveToFile {

	public static void main(String [] arg) throws IOException, Exception{

		String input = fileToPath("contextIGB.txt");
		String output = ".//output.txt";  // the path for saving the frequent item sets found
		
		double beta=0.4;
		double LS=0.2;
		
		// Applying the MSApriori algorithm
		AlgoMSAprioriSrinivas algo = new AlgoMSAprioriSrinivas();
		
		// Uncomment the following line to set the maximum pattern length (number of items per itemset)
//		algo.setMaximumPatternLength(3);
		int hashUpto = 4;
		algo.runAlgorithm(input, output, beta, LS, hashUpto);
		algo.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestMSAprioriSrinivas_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
