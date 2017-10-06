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
 * This class implements the Manathan distance function. It is a subclass of the
 * DistanceFunction class which represents any distance function.
 * <br/><br/>
 * 
 * @see DistanceFunction
 * @author Philippe Fournier-Viger
 */

public class DistanceManathan extends DistanceFunction {
	/** the name of this distance function */
	static String NAME = "manathan";

	/**
	 * Calculate the Manathan distance between two vectors of doubles.
	 * @param vector1 the first vector
	 * @param vector2 the second vector
	 * @return the distance
	 */
	public double calculateDistance(DoubleArray vector1, DoubleArray vector2) {
		double sum =0;	
		for(int i=0; i< vector1.data.length; i++){
			sum += Math.abs(vector1.data[i] - vector2.data[i]);
		}
		return sum;
	}
	
	@Override
	public String getName() {
		return NAME;
	}
//	
//	public static void main(String[] args) {
//		DoubleArray array1 = new DoubleArray(new double[] {0,2});
//		DoubleArray array2 = new DoubleArray(new double[] {2,0});
//		System.out.println(new DistanceManathan().calculateDistance(array1,array2));
//	}
	

}
