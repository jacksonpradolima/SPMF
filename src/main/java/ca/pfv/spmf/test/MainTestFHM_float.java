package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoFHM;
import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner_float.AlgoFHM_Float;

/**
 * Example of how to use the FHM algorithm with float values 
 * from the source code.
 * @author Philippe Fournier-Viger, 2014
 */
public class MainTestFHM_float {

	public static void main(String [] arg) throws IOException{
		
		String input = fileToPath("DB_UtilityFloat.txt");
		String output = ".//output.txt";

		int min_utility = 30;  // 
		
		// Applying the HUIMiner algorithm
		AlgoFHM_Float fhm = new AlgoFHM_Float();
		fhm.runAlgorithm(input, output, min_utility);
		fhm.printStats();

	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestFHM_float.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
