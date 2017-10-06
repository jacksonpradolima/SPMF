package ca.pfv.spmf.test;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map.Entry;

import ca.pfv.spmf.algorithms.frequentpatterns.estDec.Algo_estDecPlus;

/**
 * Class to test the estDecPlus algorithm and save the result to memory.
 */
public class MainTest_estDecPlus_saveToMemory {

	public static void main(String [] arg) throws FileNotFoundException, IOException{
        
		// Parameters of the EstDecPlus algorithm
        double mins = 0.1;
        double minsig = 0.4 * mins;
        double minmerge = 0.001;
        
        double delta = 0.001; 
//        double d = 1;//d=Math.pow(b, -1.0/h);
        double d = 1;
		       
		Algo_estDecPlus algo = new Algo_estDecPlus(mins,d,minsig,delta,minmerge);
		
		// To set the decay rate manually:
//		algo.setDecayRate(2, 10000); 

		// Use the following line if you want to use the "b" and "h" instead of parameter "d".
		// In that case d = Math.pow(b, -1.0 / h);
//        double b = 2;
//        double h = 2000;
//		algo.setDecayRate(b, h);
		
        // To process transactions from memory:
		algo.processTransaction(new int[] {1,2,4,5});
		algo.processTransaction(new int[] {2,3,5});
		algo.processTransaction(new int[] {1,2,4,5});
		algo.processTransaction(new int[] {1,2,3,5});
		algo.processTransaction(new int[] {1,2,3,4,5});
		algo.processTransaction(new int[] {2,3,5});
//		
		// To read transactions froma a file:
//		algo.processTransactionFromFile(fileToPath("contextPasquier99.txt"), 10000);
		
		// For debugging purposes, we can show the CP-Tree
//		algo.showTreeForDebugging();
		
		// To perform mining and save the result to memory:
		Hashtable<int[], Double> result = algo.performMining_saveResultToMemory();
		
		// Print statistics
		algo.printStats();
		
		// Show patterns found in console
		System.out.println("Itemsets found: ");
		for(Entry<int[], Double> entry : result.entrySet()) {
			for(int item : entry.getKey()) {
				System.out.print(item + " ");
			}
			System.out.println("#SUP: " + entry.getValue());
		}
		

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTest_estDecPlus_saveToMemory.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
