package ca.pfv.spmf.algorithms.clustering.kmeans;
/* This file is copyright (c) 2008-2015 Philippe Fournier-Viger
* 
* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
* 
* SPMF is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* 
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ca.pfv.spmf.algorithms.clustering.distanceFunctions.DistanceFunction;
import ca.pfv.spmf.patterns.cluster.ClusterWithMean;
import ca.pfv.spmf.patterns.cluster.ClustersEvaluation;
import ca.pfv.spmf.patterns.cluster.DoubleArray;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * An implementation of the Bisecting K-means algorithm (Steinbach et al, 2000). 
 * <br/><br/>
 * 
 * "A comparison of document clustering techniques", M. Steinbach, G. Karypis 
 * and V. Kumar. Workshop on Text Mining, KDD, 2000.<br/><br/>
 * 
 * The Bisecting K-Means algorithm is a variation of the regular K-Means algorithms.
 * It consists of the following steps:
 * (1) pick a cluster, (2) find 2-subclusters using the basic K-Means algorithm 
 * (bisecting step), (3) repeat step 2, the bisecting step, for ITER times and take
 * the split that produces the clustering, (4) repeat steps 1,2,3 until the desired
 * number of clusters is reached.
 * <br/><br/>
 * 
 * In this implementation, we use the Squared Sum of Errors (SSE) to determine if
 * a split is good. Moreover, we always choose to split the largest cluster as
 * suggested by Steinbach et al. However, note that an alternative way
 * would be to always choose the cluster with the highest SSE. But we have not done that.
 * <br/><br/>
 * 
 * @author Philippe Fournier-Viger
 * @see AlgoKMeans
 */

public class AlgoBisectingKMeans extends AlgoKMeans{
	
	/** the number times a split should be repeated to choose the best one */
	int iter = -1;

	/**
	 * Default constructor
	 */
	public AlgoBisectingKMeans() { 
		
	}
	
	/**
	 * Run the K-Means algorithm
	 * @param inputFile an input file path containing a list of vectors of double values
	 * @param k the parameter k
	 * @param distanceFunction  a distance function
	 * @param iter the number times a split should be repeated to choose the best one
	 * @param separator the character used to separate double values in the input file
	 * @return a list of clusters (some of them may be empty)
	 * @throws IOException exception if an error while writing the file occurs
	 */
	public List<ClusterWithMean> runAlgorithm(String inputFile, int k, 
			DistanceFunction distanceFunction, int iter, String separator) throws NumberFormatException, IOException {
		this.iter = iter;
		
		return runAlgorithm(inputFile, k, distanceFunction, separator);
	}
	
	/**
	 * Apply the K-means algorithm
	 * @param k the parameter k
	 * @param distanceFunction a distance function
	 * @param vectors the list of initial vectors
	 * @param minValue the min value
	 * @param maxValue the max value
	 * @param vectorsSize  the vector size
	 */
	void applyAlgorithm(int k, DistanceFunction distanceFunction,
			List<DoubleArray> vectors, double minValue, double maxValue,
			int vectorsSize) {
		
		clusters = new ArrayList<ClusterWithMean>();
		
		List<DoubleArray> currentVectors = vectors;
		
		while(true) {
			// apply kmeans iter times and keep the best clusters
			List<ClusterWithMean> bestClustersUntilNow = null;
			double smallestSSE = Double.MAX_VALUE;
			
			// Apply KMEANS with K = 2  "iter" times
			// and select the partition with the best SSE (Sum of Squared errors)
			for(int i = 0; i < iter; i++) {
				List<ClusterWithMean> newClusters = applyKMeans(2, distanceFunction, currentVectors, minValue, maxValue, vectorsSize);
				double sse = ClustersEvaluation.calculateSSE(newClusters, distanceFunction);
				if(sse < smallestSSE) {
					bestClustersUntilNow = newClusters;
					smallestSSE = sse;
				}
			}
			
			// add the best 2 clusters to the list of all clusters until now
			clusters.addAll(bestClustersUntilNow);
			
			// if we have enough clusters, we stop
			if(clusters.size() == k){
				break;
			}
			
			// otherwise, we choose the next cluster to be bisected.
			int biggestClusterSize = -1;
			int biggestClusterIndex = -1;
			for(int i =0; i < clusters.size(); i++) {
				ClusterWithMean cluster = clusters.get(i);
				// if the biggest cluster until now, we remember it
				if(cluster.getVectors().size() > biggestClusterSize) {
					biggestClusterIndex = i;
					biggestClusterSize = cluster.getVectors().size();
					currentVectors = cluster.getVectors();
				}
			}
			// remove the cluster from the list of clusters because we will split it
			clusters.remove(biggestClusterIndex);
		}
	}
	
	/**
	 * Print statistics of the latest execution to System.out.
	 */
	public void printStatistics() {
		System.out.println("========== BISECTING KMEANS - SPMF 2.09 - STATS ============");
		System.out.println(" Distance function: " + distanceFunction.getName());
		System.out.println(" Total time ~: " + (endTimestamp - startTimestamp)
				+ " ms");
		System.out.println(" SSE (Sum of Squared Errors) (lower is better) : " + ClustersEvaluation.calculateSSE(clusters, distanceFunction));
		System.out.println(" Max memory:" + MemoryLogger.getInstance().getMaxMemory() + " mb ");
		System.out.println("=====================================");
	}

}
