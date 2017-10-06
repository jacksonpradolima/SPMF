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
 * This class implements the cosine distance function. It is a subclass of the
 * DistanceFunction class which represent any distance function.
 * <br/><br/>
 * 
 * @see DistanceFunction
 * @author Philippe Fournier-Viger
 */

public class DistanceCosine extends DistanceFunction {
	/** the name of this distance function */
	static String NAME = "cosine";
	
	/**
	 * Calculate the Cosine distance between two vectors of doubles.
	 * @param vector1 the first vector
	 * @param vector2 the second vector
	 * @return the distance
	 */
	public double calculateDistance(DoubleArray vector1, DoubleArray vector2) {
		double dotproduct = 0;	
		double norm1 = 0;
		double norm2 = 0;
		for(int i=0; i< vector1.data.length; i++){
			dotproduct += vector1.data[i] * vector2.data[i];
			norm1 += Math.pow(vector1.data[i],2);
			norm2 += Math.pow(vector2.data[i],2);
		}
		if(norm1 == 0 || norm2 == 0) {
			return 0;
		}
		return 1d - (dotproduct / (Math.sqrt(norm1) * Math.sqrt(norm2)));
	}
	
	public static void main(String[] args) {
		DoubleArray array1 = new DoubleArray(new double[] {3, 2, 0,5, 0, 0, 0, 2, 0, 0});
		DoubleArray array2 = new DoubleArray(new double[] {1, 0, 0, 0, 0, 0, 0, 1, 0, 2});
		System.out.println(new DistanceCosine().calculateDistance(array1,array2));
		// The result should be 1 - 0.3150
		
		DoubleArray array4 = new DoubleArray(new double[] {0, 0});
		DoubleArray array3 = new DoubleArray(new double[] {0, 0});
		System.out.println(new DistanceCosine().calculateDistance(array3,array4));
	}
	
	@Override
	public String getName() {
		return NAME;
	}
	

}
