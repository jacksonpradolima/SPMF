package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.clustering.optics.AlgoOPTICS;

/**
 *  Example of how to use the OPTICS algorithm from the source code to obtain the OPTICS cluster
 *  ordering of points and save the result to file.
 */
public class MainTestOPTICS_extractClusterOrdering_saveToFile {
	
	public static void main(String []args) throws NumberFormatException, IOException{
		
		String input = fileToPath("inputDBScan2.txt");
		String output = ".//output.txt";
		
		// we set the parameters of DBScan:
		int minPts=2;
		double epsilon = 2d;
		
		// We specify that in the input file, double values on each line are separated by spaces
		String separator = " ";
		
		// Apply the algorithm to compute a cluster ordering
		AlgoOPTICS algo = new AlgoOPTICS();  
		algo.computerClusterOrdering(input, minPts, epsilon, separator);
		
		algo.printStatistics();
		algo.saveClusterOrderingToFile(output);
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestOPTICS_extractClusterOrdering_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
	
	
}
