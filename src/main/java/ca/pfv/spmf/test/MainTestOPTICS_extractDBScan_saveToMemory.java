package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;

import ca.pfv.spmf.algorithms.clustering.optics.AlgoOPTICS;
import ca.pfv.spmf.algorithms.clustering.optics.DoubleArrayOPTICS;
import ca.pfv.spmf.patterns.cluster.Cluster;
import ca.pfv.spmf.patterns.cluster.DoubleArray;

/**
 *  Example of how to use the OPTICS algorithm to extract 
 *  DBScan-style clusters, in the source code and keep the
 *  result in memory.
 */
public class MainTestOPTICS_extractDBScan_saveToMemory {
	
	public static void main(String []args) throws NumberFormatException, IOException{
		
		String input = fileToPath("inputDBScan2.txt");
		
		// we set the parameters of DBScan:
		int minPts=2;
		double epsilon = 2d;
		double epsilonPrime = 5d;
		
		// We specify that in the input file, double values on each line are separated by spaces
		String separator = " ";
		
		// Apply the algorithm to compute a cluster ordering
		AlgoOPTICS algo = new AlgoOPTICS();  
		List<DoubleArrayOPTICS> clusterOrdering = algo.computerClusterOrdering(input, minPts, epsilon, separator);
		
		// Print the cluster-ordering of points to the console (for debugging)
		System.out.println("THE CLUSTER ORDERING:");
		System.out.println(" [data point] - reachability distance");
		for(DoubleArrayOPTICS arrayOP : clusterOrdering) {
			System.out.println(" " + arrayOP.toString());
		}
		
		//  generate dbscan clusters from the cluster ordering:
		List<Cluster> dbScanClusters = algo.extractDBScan(minPts,epsilonPrime);
		
		// Print the clusters found by the algorithm
		// For each cluster:
		System.out.println();
		System.out.println("CLUSTER(S) FOUND:");
		int i=0;
		for(Cluster cluster : dbScanClusters) {
			System.out.println("Cluster " + i++);
			// For each data point:
			for(DoubleArray dataPoint : cluster.getVectors()) {
				System.out.println("   " + dataPoint);
			}
		}
		
		algo.printStatistics();
//		algo.saveToFile(output);
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestOPTICS_extractDBScan_saveToMemory.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
	
	
}
