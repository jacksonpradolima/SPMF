package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.HUIM_GA.AlgoHUIM_GA;


/**
 * Example of how to use the HUIM-GA algorithm 
 * from the source code.
 * @author Jerry Chun-Wei Lin, Lu Yang, Philippe Fournier-Viger, 2016
 */
public class MainTestHUIM_GA {

	public static void main(String [] arg) throws IOException{
		
		String input = fileToPath("contextHUIM.txt");
		
		String output = ".//output.txt";

		int min_utility = 40;  // 
		
		// Applying the huim_bpso algorithm
		AlgoHUIM_GA huim_ga = new AlgoHUIM_GA();
		huim_ga.runAlgorithm(input, output, min_utility);
		huim_ga.printStats();

	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestHUIM_GA.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
