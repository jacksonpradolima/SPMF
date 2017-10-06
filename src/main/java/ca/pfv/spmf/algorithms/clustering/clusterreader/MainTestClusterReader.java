package ca.pfv.spmf.algorithms.clustering.clusterreader;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;

import ca.pfv.spmf.algorithms.timeseries.TimeSeries;
import ca.pfv.spmf.patterns.cluster.Cluster;

/**
 * Example of how to read clusters from a file
 * @author Philippe Fournier-Viger, 2016.
 */
public class MainTestClusterReader {

	public static void main(String [] arg) throws IOException{
		
		// the input file
		String input = fileToPath("clustersDBScan.txt");  
		
		// Applying the  algorithm
		AlgoClusterReader algorithm = new AlgoClusterReader();
		List<Cluster> clusters = algorithm.runAlgorithm(input);
		algorithm.printStats();
		
		// print the attribute names
		List<String> attributeNames = algorithm.getAttributeNames();
		System.out.println("ATTRIBUTES");
		for(String attributeName : attributeNames){
			System.out.println(" "  + attributeName);
		}
		
		// print the clusters
		System.out.println("Clusters");
		for(Cluster cluster : clusters){
			System.out.println(" "  + cluster);
		}
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestClusterReader.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
