package ca.pfv.spmf.algorithms.clustering.distanceFunctions;

import ca.pfv.spmf.patterns.cluster.DoubleArray;
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

/**
 * This class represent a distance function to calculate the distance between two 
 * arrays of double values. It is an abstract class having several subclasses 
 * such as the Euclidian distance and the Manathan distance. 
 * The distance classes are used by the clustering algorithm. It let the user
 * choose which distance measure should be used.
 * <br/><br/>
 * 
 * @author Philippe Fournier-Viger
 */
public abstract class DistanceFunction {
	
	/**
	 * Calculate the  distance between two vectors of doubles.
	 * @param vector1 the first vector
	 * @param vector2 the second vector
	 * @return the distance
	 */
	public abstract double calculateDistance(DoubleArray vector1, DoubleArray vector2);
	
	/**
	 * Get the nam of this distance function
	 * @return a string
	 */
	public  abstract String getName();
	
	
	/**
	 * This method returns the distance function having a given name
	 * @param name the name  (euclidian, manathan, cosine, correlation,...)
	 * @return the distance function
	 */
	public static DistanceFunction getDistanceFunctionByName(String name){
		if(DistanceCorrelation.NAME.equals(name)) {
			return new DistanceCorrelation();
		}else if(DistanceCosine.NAME.equals(name)) {
			return new DistanceCosine();
		}else if(DistanceEuclidian.NAME.equals(name)) {
			return new DistanceEuclidian();
		}else if(DistanceManathan.NAME.equals(name)) {
			return new DistanceManathan();
		}else if(DistanceJaccard.NAME.equals(name)) {
			return new DistanceJaccard();
		}
		return null;
	}
}
