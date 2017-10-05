package ca.pfv.spmf.algorithms.clustering.dbscan;

import ca.pfv.spmf.patterns.cluster.Cluster;
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
public class DoubleArrayDBS extends DoubleArrayInstance{
	
	boolean visited = false;
	Cluster cluster = null;

	/**
	 * Constructor
	 * @param data an array of double values
	 * @param String the name of this array
	 */
	public DoubleArrayDBS(double[] data, String name) {
		super(data, name);
	}

}
