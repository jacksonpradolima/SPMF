package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoCHUIMiner;
import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoGHUIMINER;
import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.Itemset;


/**
 * Example of how to use the GHUI-Miner algorithm 
 * from the source code.
 * This example first applies CHUI-Miner to discover 
 * closed high utility itemsets.
 * Then, it applies GHUI-Miner to find GHUIs.
 * @author Philippe Fournier-Viger, 2014
 */
public class MainTestGHUIMiner {

	public static void main(String [] arg) throws IOException{

		String input = fileToPath("DB_Utility.txt");
		int min_utility =  30; 
		
		String output = ".//GHUIs.txt";
		
		// (1) Applying the CHUI-Miner algorithm to find 
		// closed high utility itemsets (CHUIs)
		System.out.println("Step 1: Mining CHUIs...");
		AlgoCHUIMiner chuiMineClosed = new AlgoCHUIMiner(true);
		List<List<Itemset>> closedItemsets = chuiMineClosed.runAlgorithm(input, min_utility, null);
		chuiMineClosed.printStats();
		Set<Integer> itemsInClosedItemsets = chuiMineClosed.setOfItemsInClosedItemsets;

		System.out.println("Step 2: Mining GHUIs...");
		// (2) Apply the GHUI-Miner algorithm to find
		// the set of generators of high utility itemsets (GHUIs)
		AlgoGHUIMINER ghuiMiner = new AlgoGHUIMINER();
		ghuiMiner.runAlgorithm(input, output, min_utility, closedItemsets, itemsInClosedItemsets);
		ghuiMiner.printStats();
		

	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestGHUIMiner.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
