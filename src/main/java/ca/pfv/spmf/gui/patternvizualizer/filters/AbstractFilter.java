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
 * This class represents a filter for sorting patterns in the pattern vizualizer window.
 * It stores the column that the filter is applied to, and also the index of this column.
 * 
 * @author Philippe Fournier-Viger
 */
public abstract class AbstractFilter{
	/** The column that the filter is applied to */
	String columnName;
	/** The index of the column that the filter is applied to */
	int columnID;
	
	/**
	 * Constructor
	 * @param columnName the column that the filter is applied to 
	 * @param columnID the index of the column that the filter is applied to
	 */
	AbstractFilter(String columnName, int columnID){
		this.columnName = columnName;
		this.columnID = columnID;
	}
	
	/**
	 * Abstract method to determine if an object should be kept according to the filter
	 * @param object  the object
	 * @return true if the object should be kept. Otherwise, false.
	 */
	public abstract boolean isKept(Object object);
	
	/**
	 * Get the name of the column that this filter is applied to
	 * @return the name of the column
	 */
	public String getColumnName(){
		return columnName;
	}
	
	/**
	 * Get the index of the column that this filter is applied to
	 * @return the index of the column (an integer >=0)
	 */
	public int getColumnID(){
		return columnID;
	}
	
	/**
	 * Get a string representation of this filter including the given value and column name
	 * @return a string representation of the filter indicating the filter name, given value and column name.
	 */
	public abstract String getFilterWithParameterName();

}
