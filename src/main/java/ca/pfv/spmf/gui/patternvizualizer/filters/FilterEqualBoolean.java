package ca.pfv.spmf.gui.patternvizualizer.filters;
/*
 * Copyright (c) 2008-2015 Philippe Fournier-Viger
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
 *
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * This class is a filter for selecting only patterns having a boolean value equal to a given value.
 * 
 * @author Philippe Fournier-Viger
 */
public class FilterEqualBoolean extends AbstractFilter{
	/** the required value*/
	boolean value;
	
	/**
	 * Constructor
	 * @param value the given value
	 * @param columnName the colum the filter is applied to
	 * @param columnID the index of the column that the filter is applied to
	 */
	public FilterEqualBoolean(boolean value, String columnName, int columnID){
		super(columnName, columnID);
		this.value = value;
	}

	/**
	 * Get the name of this filter
	 * @return the name
	 */
	public static String getFilterGenericName() {
		return " is equal to (true/false):";
	}

	/**
	 * Get a string representation of this filter including the given value and column name
	 * @return a string representation of the filter indicating the filter name, given value and column name.
	 */
	public String getFilterWithParameterName() {
		return "\"" + getColumnName() + "\" = " + value;
	}

	/**
	 * Abstract method to determine if an object should be kept according to the filter
	 * @param object  the object
	 * @return true if the object should be kept. Otherwise, false.
	 */
	public boolean isKept(Object object) {
		return ((Boolean) object) ==  value;
	}
	
	/**
	 * Get the Class that this filter is applicable to (e.g. Integer.class)
	 * @return a Class object
	 */
	static Class getApplicableClass() {
		return Boolean.class;
	}
}
