package ca.pfv.spmf.algorithms.clustering.optics;

/* This file is copyright (c) 2008-2013 Philippe Fournier-Viger
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

import ca.pfv.spmf.algorithms.clustering.distanceFunctions.DistanceEuclidian;
import ca.pfv.spmf.algorithms.clustering.distanceFunctions.DistanceFunction;
import ca.pfv.spmf.datastructures.kdtree.KDTree;
import ca.pfv.spmf.datastructures.kdtree.KNNPoint;
import ca.pfv.spmf.patterns.cluster.Cluster;
import ca.pfv.spmf.patterns.cluster.DoubleArray;
import ca.pfv.spmf.tools.MemoryLogger;

/* This file is copyright (c) 2008-2015 Philippe Fournier-Viger
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 * 
 * SPMF is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * An implementation of the OPTICS algorithm (Ester et al., 1996). Note that
 * original algorithm suggested using a R*-tree to index points to have a log(n) 
 * complexity instead of a O(n^2) complexity. In this implementation, we instead 
 * used a KD-Tree, which also have a log(n) complexity but may perform
 * less well than a R*-tree. The OPTICS algorithm was
 * originally published in: <br/>
 * <br/>
 * 
 * Mihael Ankerst, Markus M. Breunig, Hans-Peter Kriegel, J�rg Sander (1999).
 * OPTICS: Ordering Points To Identify the Clustering Structure. ACM SIGMOD
 * international conference on Management of data. ACM Press. pp. 49�60.
 * 
 * @author Philippe Fournier-Viger, 2015
 */

public class AlgoOPTICS {

	/** the time for extracting the cluster ordering */
	protected long timeExtractClusterOrdering;

	/* The distance function to be used for clustering */
	DistanceFunction distanceFunction = new DistanceEuclidian();

	/*
	 * This KD-Tree is used to index the data points for fast access to points
	 * in the epsilon radius
	 */
	KDTree kdtree;

	/** Variable to store the cluster-ordering found by OPTICS */
	List<DoubleArrayOPTICS> clusterOrdering = null;

	/** The clusters found by the OPTICS algorithm */
	List<Cluster> clusters = null;
	
	/** Array-list buffer for storing neighbor points to a given point.
	 * There is two buffers because the algorithm need to use two at the same time. */
	List<KNNPoint> neighboorsBuffer1 = new ArrayList<KNNPoint>();
	List<KNNPoint> neighboorsBuffer2 = new ArrayList<KNNPoint>();
	

	/** The names of the attributes **/
	private List<String> attributeNames = null;

	/**
	 * Default constructor
	 */
	public AlgoOPTICS() {

	}

	/**
	 * Run the OPTICS algorithm
	 * 
	 * @param inputFile
	 *            an input file path containing a list of vectors of double
	 *            values
	 * @param minPts
	 *            the minimum number of points (see DBScan article)
	 * @param epsilon
	 *            the epsilon distance (see DBScan article)
	 * @param seaparator
	 *            the string that is used to separate double values on each line
	 *            of the input file (default: single space)
	 * @return a list of clusters (some of them may be empty)
	 * @throws IOException
	 *             exception if an error while writing the file occurs
	 */
	public List<DoubleArrayOPTICS> computerClusterOrdering(String inputFile,
			int minPts, double epsilon, String separator)
			throws NumberFormatException, IOException {

		// record the start time
		timeExtractClusterOrdering = 0;
		long startTimestampClusterOrdering = System.currentTimeMillis();

		// Structure to store the vectors from the file
		List<DoubleArray> points = new ArrayList<DoubleArray>();
		
		// The list of attribute names
		attributeNames = new ArrayList<String>();

		// read the vectors from the input file
		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		String line;
		String currentInstanceName = null;
		// for each line until the end of the file
		while (((line = reader.readLine()) != null)) {
			// if the line is a comment, is empty or is a
			// kind of metadata
			if (line.isEmpty() == true ||
					line.charAt(0) == '#' || line.charAt(0) == '%') {
				continue;
			}
			
			// Read the name of the instance from the file
			if(line.charAt(0) == '@'){
				// if it is the name of the instance
				if(line.startsWith("@NAME=")){
					currentInstanceName = line.substring(6, line.length());
				}
				// if it is the name of an attribute   // @ATTRIBUTEDEF=Y
				if(line.startsWith("@ATTRIBUTEDEF=")){
					String attributeName = line.substring(14, line.length());
					attributeNames.add(attributeName);
				}
				continue;
			}
			// if no name in the file, then we generate one
			String nameToUse = currentInstanceName == null ?  "Instance" + points.size() : currentInstanceName;			
			currentInstanceName = null;

			line = line.trim();
			// split the line by spaces
			String[] lineSplited = line.split(separator);
			// create a vector of double
			double[] vector = new double[lineSplited.length];
			// for each value of the current line
			for (int i = 0; i < lineSplited.length; i++) {
				// convert to double
				double value = Double.parseDouble(lineSplited[i]);
				// add the value to the current vector
				vector[i] = value;
			}
			// add the vector to the list of vectors
			points.add(new DoubleArrayOPTICS(vector, nameToUse));
		}
		// close the file
		reader.close();
		
		// If the file did not contain attribute names, we will generate some
		if(attributeNames.size() == 0 && points.size() > 0){
			int dimensionCount = points.get(0).data.length;
			for(int i = 0; i < dimensionCount; i++){
				attributeNames.add("Attribute"+i);
			}
		}

		// build kd-tree
		kdtree = new KDTree();
		kdtree.buildtree(points);

		// For debugging, you can print the KD-Tree by uncommenting the
		// following line:
		// System.out.println(kdtree.toString());

		// Variable to store the order of points generated by OPTICS
		clusterOrdering = new ArrayList<DoubleArrayOPTICS>();

		// For each point in the dataset
		for (DoubleArray point : points) {
			// if the node is already visited, we skip it
			DoubleArrayOPTICS pointDBS = (DoubleArrayOPTICS) point;
			if (pointDBS.visited == false) {
				
				// process this point
				expandClusterOrder(pointDBS, clusterOrdering, epsilon, minPts);
			}
		}

		// check memory usage
		MemoryLogger.getInstance().checkMemory();

		// record end time
		timeExtractClusterOrdering = System.currentTimeMillis() - startTimestampClusterOrdering;

		// free some memory
		neighboorsBuffer1.clear();
		neighboorsBuffer2.clear();
		kdtree = null;

		// return the clusters
		return clusterOrdering;
	}



	/**
	 * The DBScan expandCluster() method
	 * 
	 * @param object
	 *            the current point
	 * @param orderedFile
	 *            the current order of points generated by OPTICS
	 * @param epsilon
	 *            the epsilon parameter
	 * @param minPts
	 *            the minPts parameter
	 * @param neighbors  an array list that should used for returning the result
	 */
	private void expandClusterOrder(DoubleArrayOPTICS pointDBS,	List<DoubleArrayOPTICS> orderedFile, double epsilon, int minPts) {

		// find the neighboors of this point with their distance
		neighboorsBuffer1.clear();
		kdtree.pointsWithinRadiusOfWithDistance(pointDBS, epsilon, neighboorsBuffer1);

		// mark the point as visited
		pointDBS.visited = true;

		// ********** NEXT LINE, WE USE EPSILON AS MAX DISTANCE******* ///
		pointDBS.reachabilityDistance = Double.POSITIVE_INFINITY; // / /// &&*&*&*
																	
		pointDBS.setCoreDistance(neighboorsBuffer1, epsilon, minPts); /// &&*&*&*

		// add the current point to the order
		orderedFile.add(pointDBS);

		if (pointDBS.core_distance != Double.POSITIVE_INFINITY) {
			// Create the orderSeeds structure to store points ordered by
			// increasing reachability-distances
			PriorityQueue<DoubleArrayOPTICS> orderSeeds = new PriorityQueue<DoubleArrayOPTICS>();
			update(neighboorsBuffer1, pointDBS, orderSeeds, minPts, epsilon);
			while (orderSeeds.isEmpty() == false) {
				DoubleArrayOPTICS currentObject = (DoubleArrayOPTICS) orderSeeds.poll();

				// Find the neighboors of the current object using a buffer 
				neighboorsBuffer2.clear();
				kdtree.pointsWithinRadiusOfWithDistance(currentObject, epsilon, neighboorsBuffer2);  /// &$&$ CurrentObject

				// mark the point as visited
				currentObject.visited = true;

				currentObject.setCoreDistance(neighboorsBuffer2, epsilon, minPts);

				// add the current point to the order
				orderedFile.add(currentObject);

				if (currentObject.core_distance != Double.POSITIVE_INFINITY) {
					update(neighboorsBuffer2, currentObject, orderSeeds, minPts, epsilon);
				}
			}
		}

		// check memory usage
		MemoryLogger.getInstance().checkMemory();
	}

	/**
	 * Update the orderSeeds w.r.t to the current object
	 * 
	 * @param neighbors
	 *            the neighbors of the current object
	 * @param centerObject
	 *            the current object
	 * @param orderSeeds
	 *            the orderSeeds structure
	 * @param epsilon the epsilon parameter
	 * @param minPts  the minPts parameter
	 */
	private void update(List<KNNPoint> neighbors,
			DoubleArrayOPTICS centerObject, PriorityQueue<DoubleArrayOPTICS> orderSeeds, int minPts, double epsilon) {
		double cDist = centerObject.core_distance;

		// FOR all object from neighbors DO:
		for (KNNPoint object : neighbors) {
			// if the object has not been visited yet
			DoubleArrayOPTICS objectOP = (DoubleArrayOPTICS) object.values;
			if (objectOP.visited == false) {
				double newRDistance = Math.max(cDist, distanceFunction
						.calculateDistance(objectOP, centerObject));

				// if not already in orderSeeds
				if (objectOP.reachabilityDistance == Double.POSITIVE_INFINITY) {
					objectOP.reachabilityDistance = newRDistance;
					orderSeeds.add(objectOP);
				} else {
					// the object was already in orderSeeds
					if (newRDistance < objectOP.reachabilityDistance) {
						objectOP.reachabilityDistance = newRDistance;
						// ******** THE FOLLOWING CODE MIGHT BE OPTIMIZED IN A
						// BETTER WAY.... *****\\\\\
						// Currently, we just remove and insert again...
						orderSeeds.remove(objectOP);
						orderSeeds.add(objectOP);
					}
				}
			}
		}
		// check memory usage
		MemoryLogger.getInstance().checkMemory();
	}
	

	/**
	 * This method extract cluster from the cluster-ordering using a DBScan
	 * based approach, as proposed in the OPTICS paper. However, as explained in
	 * the OPTICS paper, it is better to use the extractClusters() method to
	 * find the clusters than this method. But I have implemented it.
	 * 
	 * @return a list of clusters (some of them may be empty)
	 */
	public List<Cluster> extractDBScan(int minPts, double epsilonPrime)
			throws IOException {
		clusters = new ArrayList<Cluster>();

		Cluster currentCluster = new Cluster();
		// for each object (point) of the cluster ordering
		for (DoubleArrayOPTICS objectOP : clusterOrdering) {

			// if object is not density-reachable with respect to e' and minPts
			// from a preceding object in the order, it means that the current
			// object is not part of the current cluster
			if (objectOP.reachabilityDistance > epsilonPrime) {
				// we look a the core distance of object and start a new cluster
				// if object is a core object w.r.t e' and minPTs
				if (objectOP.core_distance <= epsilonPrime) {
					// add previous cluster to the list of clusters if not empty
					if (currentCluster.getVectors().size() > 0) {
						clusters.add(currentCluster);
					}
					// create a new cluster for the current object
					currentCluster = new Cluster();
					currentCluster.addVector(objectOP);
				}// else, it is noise
			} else {
				// the object belongs to the current cluster, so we add it
				currentCluster.addVector(objectOP);
			}
		}

		// add the current cluster to the list of clusters if not empty
		if (currentCluster.getVectors().size() > 0) {
			clusters.add(currentCluster);
		}
		return clusters;
	}

	/**
	 * Save the clusters to an output file
	 * 
	 * @param output
	 *            the output file path
	 * @throws IOException
	 *             exception if there is some writing error.
	 */
	public void saveToFile(String output) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));
		
		// First, we will print the attribute names
		for(String attributeName : attributeNames){
			writer.write("@ATTRIBUTEDEF=" + attributeName);
			writer.newLine();
		}
		
		// for each cluster
		for (int i = 0; i < clusters.size(); i++) {
			// if the cluster is not empty
			if (clusters.get(i).getVectors().size() >= 1) {
				// write the cluster
				writer.write(clusters.get(i).toString());
				// if not the last cluster, add a line return
				if (i < clusters.size() - 1) {
					writer.newLine();
				}
			}
		}
		// close the file
		writer.close();
	}
	

	/**
	 * Save the cluster ordering to a file
	 * @param output  the output file path
	 * @throws IOException if error while writting to file
	 */
	public void saveClusterOrderingToFile(String output) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));
		// for each cluster
		// Print the cluster-ordering of points to the console (for debugging)
		for(DoubleArrayOPTICS arrayOP : clusterOrdering) {
			writer.write(arrayOP.toString() + " " + arrayOP.reachabilityDistance);
			writer.newLine();
		}
		// close the file
		writer.close();
	}

	/**
	 * Print statistics of the latest execution to System.out.
	 */
	public void printStatistics() {
		System.out.println("========== OPTICS  SPMF 2.09 - STATS ============");
		System.out.println(" Time ExtractClusterOrdering() ~: "
				+ timeExtractClusterOrdering + " ms");
		System.out.println(" Max memory:"
				+ MemoryLogger.getInstance().getMaxMemory() + " mb ");
		
		// System.out.println(" Number of noise points: " +
		// numberOfNoisePoints);
		if(clusters != null){
			System.out.println(" Number of clusters: " + clusters.size());
		}
		System.out.println("=====================================");
	}


//	/**
//	 * This method used the ordered points (objects) generated by OPTICS to
//	 * produce cluster. It is the ExtractClusters() method as based on the
//	 * OPTICS article.
//	 * 
//	 * @param orderedFile
//	 *            the ordered points generated by OPTICS
//	 * @return a list of clusters
//	 */
//	public List<Cluster> extractClusters(double xi, int minPts) {
//
//		// Initialize set of clusters
//		clusters = new ArrayList<Cluster>();
//
//		// Initialize set of steep down areas
//		List<SteepDownArea> steepDownAreas = new ArrayList<SteepDownArea>();
//
//		// Initialize index and mib variables to 0
//		int index = 0;
//		double mib = 0;
//
//		// For each point in the ordering
//		while (index < clusterOrdering.size()) {
//			double rIndex = clusterOrdering.get(index).reachabilityDistance;
//			// Update Mib value
//			mib = Math.max(mib, rIndex);
//			
//			// Try to get a steep down area starting from this point
//			SteepDownArea downArea = getSteepDownArea(xi,index, rIndex, minPts);
//
//			// IF(start of a steep down area D at index)
//			if (downArea != null) {
//				// update mib-values and filter SetOfSteepDownAreas(*)
//				
//				// set D.mib = 0
//				downArea.mib = 0;
//				
//				// add D to the SetOfSteepDownAreas
//				steepDownAreas.add(downArea);
//				
//				// index = end of D + 1;
//				index = downArea.endIndex+1;
//
//				// mib = r(index)
//				mib = clusterOrdering.get(index).reachabilityDistance;
//			} else {
//
//				// Try to get a steep down area starting from this point
//				SteepUpArea upArea = getSteepUpArea(xi,index, rIndex, minPts);
//
//
//				// ELSE IF(start of steep up area U at index)
//				if (upArea != null) {
//					// update mib-values and filter SetOfSteepDownAreas
//					
//					// index = end of U + 1; 
//					index = upArea.endIndex +1;
//
//					mib = rIndex;
//
//					// FOR EACH D in SetOfSteepDownAreas DO
//					for (SteepDownArea D : steepDownAreas) {
//						// IF(combination of D and U is valid AND(**)
//						// satisfies cluster conditions 1, 2, 3a)
//						// compute [s, e] add cluster to SetOfClusters
//						
//					}
//				} else {
//					index++;
//				}
//			}
//
//		}
//		System.out.println("Nb of down areas :" + steepDownAreas.size());
//		return clusters;
//	}
//
//
//	/**
//	 * Get the steep up area starting at a given point
//	 * @param xi the xi parameter
//	 * @param index the current index
//	 * @param rIndex the reachability distance at position index
//	 * @param minPts  the minPts parameter
//	 * @return a steep up area if there is one, otherwise null
//	 */
//	private SteepUpArea getSteepUpArea(double xi, int index, double rIndex, int minPts) {
//
//		// First, check if it is the current index is a steep up point
//		if(isSteepUpPoint(xi, index, rIndex) == false) {
//			return null;
//		}
//		
//		// we will start from the next point
//		int lastlySeenSteepPoint =  index;
//		int currentIndex = index+1;
//		int nbOfConsecutiveNonSteepPoints = 0;
//		double rPredecessor = rIndex;
//		
//		
//		// for each point until the end
//		while(currentIndex < clusterOrdering.size()) {
//
//			// if the current point is larger than its predecessor,
//			// then it is not part of a down area
//			double rCurrent = clusterOrdering.get(currentIndex).reachabilityDistance; 
//			if(rCurrent < rPredecessor) {
//				break;
//			}
//	
//			// now we check if the current point is a steep down point
//			boolean isSteepUpPoint = isSteepUpPoint(xi, currentIndex, rCurrent);
//			// if not
//			if(isSteepUpPoint == false) {
//				// then we increment the number of non Steep up point
//				nbOfConsecutiveNonSteepPoints++;
//				// if we reach enough, we stop and set 
//				// the end index to the last steep up point position
//				if(nbOfConsecutiveNonSteepPoints >  minPts) {
//					break;
//				}
//			}else {
//				// if it is a steep up point, we remember it
//				// and reset the number of consecutive steep up point
//				lastlySeenSteepPoint = currentIndex;
//				nbOfConsecutiveNonSteepPoints = 0;
//			}
//			// we move to the next point
//			currentIndex++;
//			rPredecessor = rCurrent;
//		}
//		
//		// this area is not big enough
//		if((lastlySeenSteepPoint - index)+1 < minPts) {
//			return null;
//		}
//		
//		return new SteepUpArea(index, lastlySeenSteepPoint);
//	}
//	
//	/**
//	 * Get the steep down area starting at a given point
//	 * @param xi the xi parameter
//	 * @param index the current index
//	 * @param rIndex the reachability distance at position index
//	 * @param minPts  the minPts parameter
//	 * @return a steep down area if there is one, otherwise null
//	 */
//	private SteepDownArea getSteepDownArea(double xi, int index, double rIndex, int minPts) {
//
//		// First, check if it is the current index is a steep down point
//		if( isSteepDownPoint(xi, index, rIndex) == false) {
//			return null;
//		}
//		
//		// we will start from the next point
//		int lastlySeenSteepDownPoint =  index;
//		int currentIndex = index+1;
//		int nbOfConsecutiveNonSteepDownPoints = 0;
//		double rPredecessor = rIndex;
//		
//		
//		// for each point until the end
//		while(currentIndex < clusterOrdering.size()) {
//
//			// if the current point is larger than its predecessor,
//			// then it is not part of a down area
//			double rCurrent = clusterOrdering.get(currentIndex).reachabilityDistance; 
//			if(rCurrent > rPredecessor) {
//				break;
//			}
//	
//			// now we check if the current point is a steep down point
//			boolean isSteepDownPoint = isSteepDownPoint(xi, currentIndex, rCurrent);
//			// if not
//			if(isSteepDownPoint == false) {
//				// then we increment the number of non Steep down point
//				nbOfConsecutiveNonSteepDownPoints++;
//				// if we reach enough, we stop and set 
//				// the end index to the last steep down point position
//				if(nbOfConsecutiveNonSteepDownPoints >  minPts) {
//					break;
//				}
//			}else {
//				// if it is a steep down point, we remember it
//				// and reset the number of consecutive steep down point
//				lastlySeenSteepDownPoint = currentIndex;
//				nbOfConsecutiveNonSteepDownPoints = 0;
//			}
//			// we move to the next point
//			currentIndex++;
//			rPredecessor = rCurrent;
//		}
//		
//		// this area is not big enough
//		if((lastlySeenSteepDownPoint - index)+1 < minPts) {
//			return null;
//		}
//		
//		return new SteepDownArea(index, lastlySeenSteepDownPoint, 0);
//	}
//
//	/**
//	 * This method checks if a point at position "index" is a steep up point
//	 * @param xi the xi parameter
//	 * @param index the index position
//	 * @param rIndex the reachability distance of point at position "index"
//	 * @return true if it is a steep up point
//	 */
//	private boolean isSteepUpPoint(double xi, int index, double rIndex) {
//		// if not the last point
//		if (index != clusterOrdering.size()-1) {
//			double rSuccessor = clusterOrdering.get(index + 1).reachabilityDistance;
//			// we check
//			return rIndex <= (1 - xi) * rSuccessor;
//		}
//		return false;
//	}
//
//	/**
//	 * This method checks if a point at position "index" is a steep down point
//	 * @param xi the xi parameter
//	 * @param index the index position
//	 * @param rIndex the reachability distance of point at position "index"
//	 * @return true if it is a steep down point
//	 */
//	private boolean isSteepDownPoint(double xi, int index, double rIndex) {
//		// if not the last point
//		if (index != clusterOrdering.size()-1) {
//			double rSuccessor = clusterOrdering.get(index + 1).reachabilityDistance;
//			return rIndex * (1 - xi) >= rSuccessor;
//		}
//		return false;
//	}
//
//	/**
//	 * A steep down area as used by OPTICS
//	 * 
//	 * @author Philippe Fournier-Viger
//	 */
//	public class SteepDownArea {
//		/** start index */
//		public int startIndex;
//		/** end index */
//		public int endIndex;
//		/** mib value */
//		public double mib;
//
//		/**
//		 * Constructor of a steep down area
//		 * 
//		 * @param startIndex
//		 *            start index
//		 * @param endIndex
//		 *            end index
//		 * @param mibValue
//		 *            mib value
//		 */
//		public SteepDownArea(int startIndex, int endIndex, double mibValue) {
//			this.startIndex = startIndex;
//			this.endIndex = endIndex;
//			this.mib = mibValue;
//		}
//	}
//	
//	/**
//	 * A steep down area as used by OPTICS
//	 * 
//	 * @author Philippe Fournier-Viger
//	 */
//	public class SteepUpArea {
//		/** start index */
//		public int startIndex;
//		/** end index */
//		public int endIndex;
//
//		/**
//		 * Constructor of a steep down area
//		 * 
//		 * @param startIndex
//		 *            start index
//		 * @param endIndex
//		 *            end index
//		 * @param mibValue
//		 *            mib value
//		 */
//		public SteepUpArea(int startIndex, int endIndex) {
//			this.startIndex = startIndex;
//			this.endIndex = endIndex;
//		}
//	}
}
