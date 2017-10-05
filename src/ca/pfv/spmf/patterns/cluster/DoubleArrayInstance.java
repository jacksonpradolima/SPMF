package ca.pfv.spmf.patterns.cluster;

import java.util.Arrays;

import ca.pfv.spmf.patterns.cluster.DoubleArray;
/* This file is copyright (c) 2008-2016 Philippe Fournier-Viger
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
 * This class represent a double array that has a name.
 * It extends the class DoubleArray
 * 
 * @see DoubleArray
 * @author Philippe Fournier-Viger, 2016 */
public class DoubleArrayInstance extends DoubleArray{
	
	private String name = "";

	/** 
	 * Get the name of this time series
	 * @return a string (the name)
	 */
	public String getName() {
		return name;
	}

	/**
	 * Constructor
	 * @param values a list of double value
	 * @param name the name of this array
	 */
	public DoubleArrayInstance(double[] values, String name){
		super(values);
		this.name = name;
	}
		
	/**
	 * Obtain a string representation of this instance
	 * @return a String representation
	 */
	public String toString() {
		// We print the name of the instance followed by the data
		// that it contains (the double array)
		StringBuilder builder = new StringBuilder();
		builder.append(name);
		builder.append(" ");
		for(int i=0; i < this.data.length; i++){
			builder.append(this.data[i]);
			// if not the last one
			if(i != this.data.length-1){
				builder.append(" ");
			}
		}
		return  builder.toString();
	}
}
