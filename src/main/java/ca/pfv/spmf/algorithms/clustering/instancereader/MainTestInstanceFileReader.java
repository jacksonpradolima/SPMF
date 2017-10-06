package ca.pfv.spmf.algorithms.clustering.instancereader;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;

import ca.pfv.spmf.patterns.cluster.DoubleArray;

/**
 * Example of how to read instances from a file
 * @author Philippe Fournier-Viger, 2016.
 */
public class MainTestInstanceFileReader {

	public static void main(String [] arg) throws IOException{
		
		// the input file
		String input = fileToPath("inputDBScan.txt");  

		// Parameters of the algorithm
		String separator = " ";
		
		// Applying the  algorithm
		AlgoInstanceFileReader algorithm = new AlgoInstanceFileReader();
		List<DoubleArray> instances = algorithm.runAlgorithm(input, separator);
		algorithm.printStats();

		// print the attribute names
		List<String> attributeNames = algorithm.getAttributeNames();
		System.out.println("ATTRIBUTES");
		for(String attributeName : attributeNames){
			System.out.println(" "  + attributeName);
		}
		
		// print the time series
		System.out.println("INSTANCES");
		for(DoubleArray instance : instances){
			System.out.println(" "  + instance);
		}
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestInstanceFileReader.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
