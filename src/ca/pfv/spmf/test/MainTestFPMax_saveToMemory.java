package ca.pfv.spmf.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth.AlgoFPMax;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;


/**
 * Example of how to use FPMax from the source code.
 * @author Philippe Fournier-Viger (Copyright 2008)
 */
public class MainTestFPMax_saveToMemory {

	public static void main(String [] arg) throws FileNotFoundException, IOException{
		// Loading the transaction database
		String input = fileToPath("contextPasquier99.txt");  // the database

		// the minimum support threshold
		double minsup = 0.4; // means a minsup of 2 transaction (we used a relative support)

		// Applying the FPMax algorithm
		AlgoFPMax algo = new AlgoFPMax();
		// Run the algorithm
		// Note that here we use "null" as output file path because we want to keep the results into memory instead of saving to a file
		Itemsets patterns = algo.runAlgorithm(input, null, minsup);  
		
		// Show the MFI-Tree for debugging!
//		System.out.println(algo.mfiTree);
		
		// show the execution time and other statistics
		algo.printStats();
		// print the patterns to System.out
		patterns.printItemsets(algo.getDatabaseSize());
//		System.out.println("$$$$$$$$$$$");

//		//===============================================================
//       THE FOLLOWING CODE IS FOR DEBUGGING.  IT IS FOR ALSO RUNNING CHARM-MFI
//       SO THAT THE OUTPUT CAN BE COMPARED WITH FPMax
//		//===============================================================
//		// Loading the binary context
//		TransactionDatabase database = new TransactionDatabase();
//		database.loadFile(input);
//
//		// Applying the Charm algorithm
//		AlgoCharm_Bitset algo3 = new AlgoCharm_Bitset();
//		algo3.runAlgorithm(null, database,  minsup, true,100000 );
//		// if you change use "true" in the line above, CHARM will use
//		// a triangular matrix  for counting support of itemsets of size 2.
//		// For some datasets it should make the algorithm faster.
//		
//		// Run CHARM MFI
//		AlgoCharmMFI algo2 = new AlgoCharmMFI();
//		algo2.runAlgorithm(null, algo3.getClosedItemsets());
//		
//		// Code to browse the itemsets in memory
//		System.out.println(" ===== MAXIMAL ITEMSETS FOUND ====");
//		ca.pfv.spmf.patterns.itemset_array_integers_with_tids_bitset.Itemsets itemsets = algo2.getItemsets();
//		for(List<Itemset> level : itemsets.getLevels()) {
//			 for(Itemset itemset : level) {
//				 for(Integer item : itemset.itemset) {
//					 System.out.print(item + " ");
//				 }
//				 System.out.println( "  support " + itemset.getAbsoluteSupport());
//			 }
//		}
//		
//		// Print statistics about the algorithm execution
//		algo2.printStats(database.size());
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestFPMax_saveToMemory.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
