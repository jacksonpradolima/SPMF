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
 * This class is a filter for selecting only patterns NOT containing a given string value for a given column.
 * 
 * @author Philippe Fournier-Viger
 */
public class FilterStringNotContains extends AbstractFilter{
	/** the given value*/
	String value;
	
	/**
	 * Constructor
	 * @param value the given value
	 * @param columnName the colum that the filter is applied to
	 * @param columnID the index of the column that the filter is applied to
	 */
	public FilterStringNotContains(String value, String columnName, int columnID){
		super(columnName, columnID);
		this.value = value;
	}

	/**
	 * Get the name of this filter
	 * @return the name
	 */
	public static String getFilterGenericName() {
		return "does not contains the string:";
	}

	/**
	 * Get a string representation of this filter including the given value and column name
	 * @return a string representation of the filter indicating the filter name, given value and column name.
	 */
	public String getFilterWithParameterName() {
		return "\"" + getColumnName() + "\" not contains " + value;
	}
	
	/**
	 * Abstract method to determine if an object should be kept according to the filter
	 * @param object  the object
	 * @return true if the object should be kept. Otherwise, false.
	 */
	public boolean isKept(Object object) {
		return ((String) object).contains(value) == false;
	}

	/**
	 * Get the Class that this filter is applicable to (e.g. Integer.class)
	 * @return a Class object
	 */
	static Class getApplicableClass() {
		return String.class;
	}

}
