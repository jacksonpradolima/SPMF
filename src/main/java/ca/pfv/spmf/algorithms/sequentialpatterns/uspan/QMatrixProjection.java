package ca.pfv.spmf.algorithms.sequentialpatterns.uspan;

import java.util.List;

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
 * This class represents a projected QMatrix.
 * It consists of an original QMatrix and a set of positions where the current prefix
 * explored by USpan appears.
 * 
 * @author Philippe Fournier-Viger, 2015
 * 
 * @see AlgoUSpan
 * @see QMatrix
 * @see MatrixPosition
 */
class QMatrixProjection{

	/** the qmatrix for items  [item, utility] */
	QMatrix originalMatrix;
	
	/** the row of the last added item */
	List<MatrixPosition> positions;
	
	/**
	 * Constructor
	 * @param nbItem the number of item in the sequence
	 * @param nbItemset the number of itemsets in that sequence
	 */
	public QMatrixProjection(QMatrix matrix, List<MatrixPosition> positions){
		this.originalMatrix = matrix; 
		this.positions = positions;
	}
	
	/**
	 * Constructor
	 * @param nbItem the number of item in the sequence
	 * @param nbItemset the number of itemsets in that sequence
	 */
	public QMatrixProjection(QMatrixProjection projection, List<MatrixPosition> positions){
		this.originalMatrix = projection.originalMatrix; 
		this.positions = positions;
	}

	/**
	 * Get the array of items stored in the original Q-matrix
	 * @return the array of items
	 */
	public int[] getItemNames() {
		return originalMatrix.itemNames;
	}

	/**
	 * Get the local sequence utility for a given cell in the projected q-matrix
	 * @param position the cell position (row,column)
	 * @return the local sequence utility
	 */
	public int getLocalSequenceUtility(MatrixPosition position) {
		return originalMatrix.matrixItemRemainingUtility[position.row][position.column];
	}

	/**
	 * Get the utility of a cell in the projected q-matrix at a given cell position (row,column)
	 * @param position the position (row, column)
	 * @return the utility
	 */
	public int getItemUtility(MatrixPosition position) {
		return originalMatrix.matrixItemUtility[position.row][position.column];
	}

	/**
	 * Get the utility of a cell in the projected q-matrix at a given cell position (row,column)
	 * @param row the row
	 * @param column the column
	 * @return the utility
	 */
	public int getItemUtility(int row, int column) {
		return originalMatrix.matrixItemUtility[row][column];
	}

	/**
	 * Get the remaining utility of a cell in the projected q-matrix at a given 
	 * cell position (row,column).
	 * @param row the row
	 * @param column the column
	 * @return the remaining utility
	 */
	public int getRemainingUtility(int row, int column) {
		return originalMatrix.matrixItemRemainingUtility[row][column];
	}

}
