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
 * This class implements the Jaccard distance function. This distance
 * function is suitable for vectors of binary values (0 or 1). It should
 * not be used with vectors containing non binary numbers.
 * It is a subclass of the
 * DistanceFunction class which represents any distance function.
 * <br/><br/>
 * 
 * @see DistanceFunction
 * @author Philippe Fournier-Viger
 */

public class DistanceJaccard extends DistanceFunction {
	/** the name of this distance function */
	static String NAME = "jaccard";

	/**
	 * Calculate the Jaccard distance between two vectors of doubles, which are
	 * assumed to be either 0s or 1s.
	 * @param vector1 the first vector
	 * @param vector2 the second vector
	 * @return the distance
	 */
	public double calculateDistance(DoubleArray vector1, DoubleArray vector2) {
		double count11 = 0;	  // count of M11
		double count10or01or11 = 0; // count of M01, M10 and M11
		
		// for each position in the vector
		for(int i=0; i< vector1.data.length; i++){
			// if it is not  two 0s
			if(vector1.data[i] != 0  || vector2.data[i] != 0) {
				// if it is two 1s
				if(vector1.data[i] == 1  && vector2.data[i] == 1) {
					count11++;
				}
				// increase the count of not two 0s
				count10or01or11++;
			}
			
		}
		return count11 / count10or01or11;
	}
	
	@Override
	public String getName() {
		return NAME;
	}
	
	public static void main(String[] args) {
		DoubleArray array1 = new DoubleArray(new double[] {0,1,0,1});
		DoubleArray array2 = new DoubleArray(new double[] {1,0,0,1});
		System.out.println(new DistanceJaccard().calculateDistance(array1,array2));
		// result should be 0.33
		
		DoubleArray array4 = new DoubleArray(new double[] {1, 0});
		DoubleArray array3 = new DoubleArray(new double[] {1, 0});
		System.out.println(new DistanceCosine().calculateDistance(array3,array4));
	}
	

}
