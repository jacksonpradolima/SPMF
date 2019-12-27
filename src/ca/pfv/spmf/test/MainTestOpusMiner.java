package ca.pfv.spmf.test;



import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.opusminer.AlgoOpusMiner;

/**
 * Example of how to use OpusMiner from the source code.
 * @author Philippe Fournier-Viger, 2018.
 */
public class MainTestOpusMiner {

	public static void main(String [] arg) throws IOException{
		
		//------------ Parameters ----------------------//
		String input = fileToPath("contextOpusMiner.txt");  // the database
		String output = ".//output.txt";  // the path for saving the frequent itemsets found


		// Set k to the integer value <i>.  By default it is 100.
		int k = 3;
		
		// Supress filtering out itemsets that are not independently productive.
		boolean filter = true;
		
		// Set the measure of interest to lift.  By default it is leverage.
		boolean searchByLift = true;
		
		// Allow redundant itemsets.
		boolean redundancyTests = true;
		
		boolean correctionForMultiCompare = true;
		
		//Each output itemset is followed by its closure.
		boolean printClosure = false;
		
		boolean loadCSVFormat = false;
		//------------ End of parameters ----------------------//

		// Applying the  algorithm
		AlgoOpusMiner algorithm = new AlgoOpusMiner();
		algorithm.runAlgorithm(input, output, printClosure, filter, k ,searchByLift, 
				correctionForMultiCompare, redundancyTests, loadCSVFormat);
		algorithm.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestOpusMiner.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
