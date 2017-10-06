package ca.pfv.spmf.algorithms.sequentialpatterns.uspan;

/* Copyright (c) 2008-2015 Philippe Fournier-Viger
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
 * This class represents a position in a projected q-matrix.
 * It stores the row and column of a q-matrix cell as well as the utility of the
 * current prefix explored by USpan that ends at this cell.
 * 
 * @author Philippe Fournier-Viger, 2015
 * 
 * @see AlgoUSpan
 * @see QMatrix
 * @see QMatrixProjection
 */
class MatrixPosition {
	
	/** a row **/
	public int row;
	
	/** a column  **/
	public int column;
	
	/** the utility of the prefix ending at this cell (row/column) **/
	public int utility;
	
	/**
	 * Constructor
	 * @param row the row
	 * @param column the column
	 * @param utility the utility
	 */
	public MatrixPosition(int row, int column, int utility) {
		this.row = row;
		this.column = column;
		this.utility = utility;
	}

}
