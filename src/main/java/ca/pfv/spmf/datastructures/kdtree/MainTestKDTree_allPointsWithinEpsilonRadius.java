package ca.pfv.spmf.datastructures.kdtree;
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

import java.util.ArrayList;
import java.util.List;

import ca.pfv.spmf.patterns.cluster.DoubleArray;

/**
 * This test show how to use the KDTree structure to find 
 * the K nearest neighbors to a given point and is intended for testing the KDtree structure
 * by developers.
* 
* @see KDTree
 * @author Philippe Fournier-Viger
 */
class MainTestKDTree_allPointsWithinEpsilonRadius {

	public static void main(String[] args) {
		// create kd tree with two dimensions  and of type double
		KDTree tree = new KDTree();
		
		// Use a list of point to create the kd-tree
		List<DoubleArray> points = new ArrayList<DoubleArray>();
		points.add(new DoubleArray(new double[]{1d,1d}));
		points.add(new DoubleArray(new double[]{0d,1d}));
		points.add(new DoubleArray(new double[]{1d,0d}));
		points.add(new DoubleArray(new double[]{10d,10d}));
		points.add(new DoubleArray(new double[]{10d,13d}));
		points.add(new DoubleArray(new double[]{13d,13d}));
		points.add(new DoubleArray(new double[]{54d,54d}));
		points.add(new DoubleArray(new double[]{55d,55d}));
		points.add(new DoubleArray(new double[]{89d,89d}));
		points.add(new DoubleArray(new double[]{57d,55d}));
		
		// Create a KD Tree with the points
		tree.buildtree(points);
		
		// Print the tree for debugging
		System.out.println("\nTREE: \n" + tree.toString() + "  \n\n Number of elements in tree: " + tree.size());
	
		// Find the nearest neighboor to the point 4,4
		DoubleArray querypoint =  new DoubleArray(new double[]{1d,0d});
		double radius = 5;
		List<DoubleArray> result = tree.pointsWithinRadiusOf(querypoint, radius);
		
		System.out.println("THE POINTS WITHIN THE RADIUS ARE : ");	
		for(DoubleArray point : result) {
			System.out.println(" " + point);
		}
	}
	
	public static String toString(double [] values){
		StringBuilder buffer = new StringBuilder();
		for(Double element : values ){
			buffer.append("   " + element);
		}
		return buffer.toString();
	}
}
