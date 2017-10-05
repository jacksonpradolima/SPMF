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
 * This class represents a QMatrix as described in the paper describing USPan
 * @author Philippe Fournier-Viger, 2015
 * @see AlgoUSpan
 */
class QMatrix {

	/** the qmatrix for items  [item][itemset] -> utility  */
	int matrixItemUtility[][];
	/** the qmatrix for remaining utility [item][itemset] -> remaining utility*/
	int matrixItemRemainingUtility[][];
	/** the item names */
	int[] itemNames;
	/** the swu of this sequence **/
	int swu;
	
	/**
	 * Constructor
	 * @param nbItem the number of item in the sequence
	 * @param nbItemset the number of itemsets in that sequence
	 */
	public QMatrix(int nbItem, int nbItemset, int[] itemNames, int itemNamesLength, int swu){
		matrixItemUtility = new int[nbItem][nbItemset];
		matrixItemRemainingUtility = new int[nbItem][nbItemset];
		this.swu = swu;
		
		this.itemNames = new int[itemNamesLength];
		System.arraycopy(itemNames, 0, this.itemNames, 0, itemNamesLength);
	}
	
	/**
	 * Register item in the matrix
	 * @param itemPos an item position in "itemNames"
	 * @param itemset the itemset number
	 * @param utility the utility of the item in that itemset
	 * @param remainingUtility the reamining utility of that item at that itemset
	 */
	public void registerItem(int itemPos, int itemset, int utility, int remainingUtility) {
		// we store the utility in the cell for this item/itemset
		matrixItemUtility[itemPos][itemset] = utility;
		// we store the remaining utility in the cell for this item/itemset
		matrixItemRemainingUtility[itemPos][itemset] = remainingUtility;
	}

	
	/**
	 * Get a string representation of this matrix (for debugging purposes)
	 * @return the string representation
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(" MATRIX \n");
		for(int i=0; i< itemNames.length; i++) {
			buffer.append( "\n  item: " + itemNames[i] + "  ");
			for(int j=0; j< matrixItemUtility[i].length; j++) {
				buffer.append("  " + matrixItemUtility[i][j] + "[" + 
						+ matrixItemRemainingUtility[i][j] + "]");
			}
		}
		buffer.append("   swu: " + swu);
		buffer.append("\n");
		return buffer.toString();
	}
}
