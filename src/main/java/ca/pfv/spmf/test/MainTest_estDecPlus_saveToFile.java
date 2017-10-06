package ca.pfv.spmf.test;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.estDec.Algo_estDecPlus;

/**
 * Class to test the estDecPlus algorithm and save the result to a file.
 */
public class MainTest_estDecPlus_saveToFile {

	public static void main(String [] arg) throws FileNotFoundException, IOException{
		
		String database = fileToPath("contextPasquier99.txt"); 
		String output = "output.txt"; 

        if (arg.length!=0)
          database=arg[0];
        
        double mins = 0.001;
        double minsig=0.3*mins;
        double minmerge=0.1;
        
        double delta=0.001;
        double d=1;
		       
		Algo_estDecPlus algo = new Algo_estDecPlus(mins,d,minsig,delta,minmerge);
		
		// Use the following line if you want to use the "b" and "h" instead of parameter "d".
		// In that case d = Math.pow(b, -1.0 / h);
//        double b = 2;
//        double h = 2000;
//		algo.setDecayRate(b, h);
		
		// process a set of transactions from a file
		algo.processTransactionFromFile(database);
		// For debugging purposes, we can show the CP-Tree
//		algo.showTreeForDebugging();
		// perform mining and save the result to a file
		algo.performMining_saveResultToFile(output);
		// print statistics
		algo.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTest_estDecPlus_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
