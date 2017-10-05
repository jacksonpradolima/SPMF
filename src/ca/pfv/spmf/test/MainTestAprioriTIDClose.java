package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.aprioriTIDClose.AlgoAprioriTIDClose;
import ca.pfv.spmf.input.transaction_database_list_integers.TransactionDatabase;
import ca.pfv.spmf.patterns.itemset_array_integers_with_tids.Itemsets;


/**
 * Example of how to use the AprioriTIDClose algorithm, from the
 * source code.
 * @author Philippe Fournier-Viger (Copyright 2008)
 */
public class MainTestAprioriTIDClose {

	public static void main(String [] arg) throws IOException{

		long startTime = System.currentTimeMillis();
		
		// Loading the binary context
		TransactionDatabase database = new TransactionDatabase();
		try {
			database.loadFile(fileToPath("contextPasquier99.txt"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
//		database.printDatabase();
		
		// Applying the APRIORI-CLOSE algorithm
		AlgoAprioriTIDClose algo = new AlgoAprioriTIDClose();

		// We pass null as output file path, because we want to keep
		// the result into memory instead.
		Itemsets frequents = algo.runAlgorithm(database, 0.4, null);
		
		long endTime = System.currentTimeMillis();
		
		// print the frequent itemsets found
		frequents.printItemsets(database.size());
		
		// print the frequent closed itemsets found
		algo.getFrequentClosed().printItemsets(database.size());
		algo.printStats();

		System.out.println("total Time : " + (endTime - startTime) + "ms");
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestAprioriTIDClose.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
