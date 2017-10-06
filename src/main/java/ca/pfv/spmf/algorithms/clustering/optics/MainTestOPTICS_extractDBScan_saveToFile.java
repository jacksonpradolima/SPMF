package ca.pfv.spmf.algorithms.clustering.optics;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

/**
 *  Example of how to use the OPTICS algorithm to extract DBSCAN-style clusters, in source code,
 *  and then save the result to file.
 */
public class MainTestOPTICS_extractDBScan_saveToFile {
	
	public static void main(String []args) throws NumberFormatException, IOException{
		
		String input = fileToPath("inputDBScan.txt");
		String output = ".//output.txt";
		
		// we set the parameters of DBScan:
		int minPts=2;
		double epsilon = 5d;
		double epsilonPrime = 5d;
		
		// We specify that in the input file, double values on each line are separated by spaces
		String separator = " ";
		
		// Apply the algorithm to compute a cluster ordering
		AlgoOPTICS algo = new AlgoOPTICS();  
		algo.computerClusterOrdering(input, minPts, epsilon, separator);

		//  generate dbscan clusters from the cluster ordering:
		algo.extractDBScan(minPts,epsilonPrime);

		algo.printStatistics();
		algo.saveToFile(output);
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestOPTICS_extractDBScan_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
	
	
}
