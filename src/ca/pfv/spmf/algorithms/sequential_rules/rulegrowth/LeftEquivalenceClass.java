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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.pfv.spmf.patterns.itemset_array_integers_with_tids.Itemset;

/**
 * This class represents a left equivalence class.
 * 
 * @see AlgoERMiner
 * @see Itemset
 * @author Philippe Fournier-Viger
 */
public class LeftEquivalenceClass {
	
	int[] itemsetJ; 
	Set<Integer> tidsJ;
	Map<Integer, Occurence> occurencesJ;
	
	List<LeftRule> rules = new ArrayList<LeftRule>();

	public LeftEquivalenceClass(int[] itemsetJ, 
			Set<Integer> tidsJ, 
			Map<Integer, Occurence> occurencesJ) {
			this.itemsetJ = itemsetJ;
			this.tidsJ = tidsJ;
			this.occurencesJ = occurencesJ;
	}
	
	@Override
	public String toString() {
		return "EQ:" + Arrays.toString(itemsetJ);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}
		LeftEquivalenceClass eq = (LeftEquivalenceClass) obj;
		
		// TODO Auto-generated method stub
		return Arrays.equals(itemsetJ, eq.itemsetJ);
	}

}
