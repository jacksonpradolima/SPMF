package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.HUIM_BPSO_tree.AlgoHUIM_BPSO_tree;

/**
 * Example of how to use the HUIM-BPSO-tree algorithm 
 * from the source code.
 * @author Jerry Chun-Wei Lin, Lu Yang, Philippe Fournier-Viger, 2016
 */
public class MainTestHUIM_BPSO_tree {

	public static void main(String [] arg) throws IOException{
		
		String input = fileToPath("contextHUIM.txt");
		
		String output = ".//output.txt";

		int min_utility = 40;  // 
		
		// Applying the huim_bpso_tree algorithm
		AlgoHUIM_BPSO_tree huim_bpso_tree = new AlgoHUIM_BPSO_tree();
		huim_bpso_tree.runAlgorithm(input, output, min_utility);
		huim_bpso_tree.printStats();

	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestHUIM_BPSO_tree.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
