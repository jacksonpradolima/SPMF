package ca.pfv.spmf.gui.patternvizualizer;

import java.util.ArrayList;
import java.util.List;

import javax.swing.RowFilter;

import ca.pfv.spmf.gui.patternvizualizer.filters.AbstractFilter;
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
 * This is a RowFilter subclass to filter patterns in the PatternTableModel used by the
 * pattern vizualizer window.
 * @author Philippe Fournier-Viger
 *
 */
public class PatternTableRowFilters<PatternTableModel, Object> extends RowFilter {
	
	/** the current list of filters */
	public List<AbstractFilter> filters = new  ArrayList<AbstractFilter>();
	
	/**
	 * Default constructor */
	PatternTableRowFilters(){
	}
 
	@Override
	/** This method returns true if a given TableModel entry respects the filters. Otherwise,
	 * it returns false.
	 * @return true if a given TableModel entry respects the filters. Otherwise,
	 * it returns false.
	 */
	public boolean include(Entry entry) {

		// we apply each filter
		for(AbstractFilter filter : filters){
			// if the entry does not respect one of the filter, we reject it
			int columnIndex = filter.getColumnID();
			if(filter.isKept(entry.getValue(columnIndex))== false){
				return false;
			}
			
		}
		// otherwise we will keep that entry
		return true;
	}
}
