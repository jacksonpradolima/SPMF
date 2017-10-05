package ca.pfv.spmf.algorithmmanager;

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
/**
 * This class is used to describe an algorithm's parameter.
 * 
 * @see DescriptionOfAlgorithm
 * @author Philippe Fournier-Viger, 2016
 */
public class DescriptionOfParameter{
	
	/** name of this parameter */
	public final String name;
	/** example value for this parameter */
	public final String example;
	/** type of parameter value */
	public final Class parameterType;
	/** this parameter is optional or not? */
	public final boolean isOptional;
	
	/**
	 * Constructor for this parameter
	 * @param name  the name of the parameter (a string)
	 * @param example a string providing an example value that this parameter could take
	 * @param parameterType the type of this parameter (e.g. Integer.class, Double.class, String.class...)
	 */
	public DescriptionOfParameter(String name, String example, Class parameterType, boolean isOptional){
		this.name = name;
		this.example = example;
		this.parameterType = parameterType;
		this.isOptional = isOptional;
	}
	
	@Override
	/**
	 * Obtain a String representation of this parameter description
	 * @return a String
	 */
	public String toString() {
		return "[" + name + ", " + example + ", " + parameterType + ", isOptional = " + isOptional + " ]";
	}

}
