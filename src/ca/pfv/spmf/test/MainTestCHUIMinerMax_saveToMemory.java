package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;

import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoCHUIMiner;
import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoCHUIMinerMax;
import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.Itemset;


/**
 * Example of how to use the CHUI-Miner(MAX) algorithm 
 * from the source code and save the result to memory.
 * @author Philippe Fournier-Viger, 2014
 */
public class MainTestCHUIMinerMax_saveToMemory {

	public static void main(String [] arg) throws IOException{
		
		String input = fileToPath("random_1.txt");
		int min_utility = 50;
		
		// (1) Applying the algorithm to find 
		// maximal high utility itemsets (CHUIs)
		
		AlgoCHUIMinerMax algorithm = new AlgoCHUIMinerMax(true);
		List<Itemset> maximalItemsets = algorithm.runAlgorithm(input, min_utility, null);
		algorithm.printStats();
		 
		//  (2) PRINTING THE ITEMSETS FOUND TO THE CONSOLE
		for(Itemset itemset : maximalItemsets) {
			System.out.println(itemset.toString());
		}
	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestCHUIMinerMax_saveToMemory.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
