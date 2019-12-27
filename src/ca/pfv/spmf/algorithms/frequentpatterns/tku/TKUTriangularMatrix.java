package ca.pfv.spmf.algorithms.frequentpatterns.tku;
/* 
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
 * This class is a triangular matrix. It is based on the triangular matrix
 * code of SPMF but adds some functionalities specific to the TKU algorithm.
 * 
 * @see AlgoTKU  
 **/
class TKUTriangularMatrix {
	
	/** the matrix, stored in a two dimensional array */
	public int[][] matrix;
	
	/** the number of element stored in the matrix */
	public int elementCount;

	/**
	 * Constructor
	 * @param elementCount the number of element stored in the matrix
	 */
	public TKUTriangularMatrix(int elementCount){
		this.elementCount = elementCount;
		matrix = new int[elementCount][]; 
		for(int i=0; i< elementCount; i++){ 
		   // allocate an array for each row
			matrix[i] = new int[elementCount-i];
		}
	}
	
	/**
	 * Get the value at the position (i,j) in the matrix
	 * @param i a position i
	 * @param j a position j
	 * @return the value
	 */
	public int get(int i, int j){
		return matrix[i][j];
	}
	
	/**
	 * for testing!
	 */
	public static void main(String[] args) {
		
		TKUTriangularMatrix a = new TKUTriangularMatrix(5);

		System.out.println(a.toString());
		// AB, AD, AE, BD, BE, DE
		a.incrementCount(1, 2,1);
		System.out.println("add {1 2}");
		System.out.println(a.toString());
		
		System.out.println("add {1 2}");
		a.incrementCount(1, 2,1);
		System.out.println(a.toString());
		
		System.out.println("add {1 3}");
		a.incrementCount(1, 3,1);
		System.out.println(a.toString());
		a.incrementCount(1, 4,1);
		System.out.println(a.toString());
		
		a.incrementCount(1, 3,1);
		a.incrementCount(2, 4,1);
		a.incrementCount(2, 4,1);
		a.incrementCount(4, 3,1);
		System.out.println(a.toString());
		a.incrementCount(0, 2,1);
		a.incrementCount(0, 3,1);
		a.incrementCount(0, 4,1);
		System.out.println(a.toString());
		
	}
	
	/**
	 * get the string representation of this matrix
	 * @return a string
	 */
	public String toString() {
		System.out.println("Element count = " + elementCount);
		StringBuffer temp = new StringBuffer();
		for (int i = 0; i < matrix.length; i++) {
			temp.append(i);
			temp.append(": ");
			for (int j = 0; j < matrix[i].length; j++) {
				temp.append(matrix[i][j]);
				temp.append(" ");
			}
			temp.append("\n");
		}
		return temp.toString();
	}

	/**
	 * Increment count in the matrix by some value
	 * @param id1 a position in the matrix
	 * @param id2 a position in the matrix
	 * @param sum the value
	 */
	public void incrementCount(int id1, int id2, int sum) {
		if(id2 < id1){
			
			matrix[id2][elementCount - id1 - 1]+=sum;
		}else{
			
			matrix[id1][elementCount - id2 - 1]+=sum;
			
		}

	}
	
	/**
	 * Get the support of some elements in the matrix
	 * @param id1 an element
	 * @param id2 another element
	 * @return the support
	 */
	public int getSupportForItems(int id1, int id2){
		if(id2 < id1){
			return matrix[id2][elementCount - id1 - 1];
		}else{
			return matrix[id1][elementCount - id2 - 1];
		}
	}
}
