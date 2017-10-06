package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;

import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoCHUIMiner;
import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.Itemset;


/**
 * Example of how to use the CHUI-Miner algorithm 
 * from the source code and save the result to memory.
 * @author Philippe Fournier-Viger, 2014
 */
public class MainTestCHUIMiner_saveToMemory {

	public static void main(String [] arg) throws IOException{
		// input file path
//		String input = fileToPath("DB_Utility.txt");
//		// the minutility threshold
//		int min_utility = 30;   
		
		String input = fileToPath("DB_Utility.txt");
		int min_utility = 30;
		
		// (1) Applying the  CHUI-Miner algorithm to find 
		// closed high utility itemsets (CHUIs)
		AlgoCHUIMiner chuiMiner = new AlgoCHUIMiner(true);
		List<List<Itemset>> closedItemsets = chuiMiner.runAlgorithm(input, min_utility, null);
		chuiMiner.printStats();
		
		//  (2) PRINTING THE ITEMSETS FOUND TO THE CONSOLE
		for(List<Itemset> itemsetsSameSize : closedItemsets) {
			for(Itemset itemset : itemsetsSameSize) {
				System.out.println(itemset);
			}
		}

	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestCHUIMiner_saveToMemory.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
