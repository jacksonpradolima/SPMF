package ca.pfv.spmf.algorithms.clustering.optics;

import java.util.Collections;
import java.util.List;

import ca.pfv.spmf.datastructures.kdtree.KNNPoint;
import ca.pfv.spmf.patterns.cluster.DoubleArray;
import ca.pfv.spmf.patterns.cluster.DoubleArrayInstance;

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
 * This class represents a vector of double values used by the DBScan algorithm.
 * It has a "visited" flag to remember the node that have been already visited.
* 
 * @author Philippe Fournier-Viger
 */
public class DoubleArrayOPTICS extends DoubleArrayInstance implements Comparable<DoubleArrayOPTICS>{
	
	boolean visited = false;
	public double reachabilityDistance = Double.POSITIVE_INFINITY; // undefined
	double core_distance = Double.POSITIVE_INFINITY;  // undefined

	/**
	 * Constructor
	 * @param data an array of double values
	 */
	public DoubleArrayOPTICS(double[] data, String name) {
		super(data, name);
	}

	/**
	 * Set the core distance of this point
	 * @param neighbors the neighbors of this point
	 * @param epsilon the epsilon distance
	 * @param minPts  the minPts parameter
	 */
	public void setCoreDistance(List<KNNPoint> neighboors, double epsilon,
			int minPts) {
		// if not enough neighbors, then undefined
		if(neighboors.size() < minPts - 1) {
			core_distance  = Double.POSITIVE_INFINITY;
		}else {
			// sort neighbors by increasing distance
			Collections.sort(neighboors);
			
			// the core distance is the distance of the minPts-1-th neighbor
			core_distance = (neighboors.get(minPts-2)).distance;
		}
	}

	/**
	 * Compare this point to another point
	 * @param point2 the given point.
	 * @return an integer <0 if the distance of this point is smaller than the distance of point2
	 *         an integer >0 if the distance of this point is larger than the one of point2
	 *         0 if the distance is the same.
	 */
	public int compareTo(DoubleArrayOPTICS point2) {
		return Double.compare(this.reachabilityDistance, point2.reachabilityDistance);
	}
	
	/**
	 * Obtain a string representation of this instance
	 * @return a String representation
	 */
	public String toString() {
		return  super.toString();
	}

}
