package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.two_phase.AlgoHUINIVMine;
import ca.pfv.spmf.algorithms.frequentpatterns.two_phase.ItemsetsTP;
import ca.pfv.spmf.algorithms.frequentpatterns.two_phase.UtilityTransactionDatabaseTP;

/**
 * Example of how to use the HUINIVMine Algorithm in source code.
 * @author Philippe Fournier-Viger, 2010
 */
public class MainTestHUINIVMine_saveToMemory {

	public static void main(String [] arg) throws IOException{
		
		String input = fileToPath("DB_NegativeUtility.txt");

		int min_utility = 80; 

		// Loading the database into memory
		UtilityTransactionDatabaseTP database = new UtilityTransactionDatabaseTP();
		database.loadFile(input);
		database.printDatabase();
		
		// Applying the THUINIV-Mine algorithm
		AlgoHUINIVMine algo = new AlgoHUINIVMine();
		ItemsetsTP highUtilityItemsets = algo.runAlgorithm(database, min_utility);
		
		// print the frequent itemsets found
		highUtilityItemsets.printItemsets(database.size());
		
		algo.printStats();

	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestHUINIVMine_saveToMemory.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
