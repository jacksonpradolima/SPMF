package ca.pfv.spmf.algorithms.sequential_rules.rulegrowth;
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
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the SparseMatrix structure used by ERMiner.
 * 
 * @see AlgoERMiner
 * @author Philippe Fournier-Viger
 */
public class SparseMatrix {
	

	// item i, item j ,  count of i,j appearing together
	Map<Integer, Map<Integer, Integer>> matrix = new HashMap<Integer, Map<Integer, Integer>>();
	
	public SparseMatrix() {
		
	}
	
	public void increaseCountOfPair(int i, int j) {
		// IMPORTANT
		if(i < j) {  
			return;
		}
		
		Map<Integer, Integer> map = matrix.get(i);
		if(map == null) {
			map = new HashMap<Integer, Integer>();
			map.put(j, 1);
			matrix.put(i, map);
		}else {
			Integer count = map.get(j);
			if(count == null) {
				map.put(j, 1);	
			}else {
				map.put(j, count+1);
			}
		}
	}
	
	public int getCount(int i, int j) {
		Map<Integer, Integer> map = matrix.get(i);
		if(map == null) {
			return 0;
		}
		Integer count = map.get(j);
		if(count == null) {
			return 0;
		}
		return count;
	}

	public Map<Integer, Map<Integer, Integer>> getMatrix() {
		return matrix;
	}
	
	
	
	 

}
