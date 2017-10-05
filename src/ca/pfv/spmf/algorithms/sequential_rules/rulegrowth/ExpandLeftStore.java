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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is the LeftStore structure used by the ERMiner algorithm
 * 
 * @see AlgoERMiner
 * @author Philippe Fournier-Viger
 */
public class ExpandLeftStore {
	
	// SIZE,  HASH on LEFT ITEMSET,  LIST OF RULES
	Map<Integer, Map<Integer, List<LeftEquivalenceClass>>> store = new HashMap<Integer, Map<Integer, List<LeftEquivalenceClass>>>();

	public ExpandLeftStore() {
		
	}
	public void register(LeftRule leftRule, int[] itemsetJ,
			Set<Integer> tidsJ, Map<Integer, Occurence> occurencesI,
			Map<Integer, Occurence> occurencesJ) {
		int size = itemsetJ.length;
		int hash = Arrays.hashCode(itemsetJ);
		
		Map<Integer, List<LeftEquivalenceClass>> map = store.get(size);
		if(map == null) {
			map = new HashMap<Integer, List<LeftEquivalenceClass>>();
			store.put(size, map);
		}	
		List<LeftEquivalenceClass> list = map.get(hash);	
		if(list == null) {
			LeftEquivalenceClass eclass = new LeftEquivalenceClass(itemsetJ, tidsJ, occurencesJ);
			list = new ArrayList<LeftEquivalenceClass>();
			map.put(hash, list);
			list.add(eclass);
			eclass.rules.add(leftRule);
		}else {
			for(LeftEquivalenceClass eclass : list) {
				if(Arrays.equals(eclass.itemsetJ, itemsetJ)) {
					eclass.rules.add(leftRule);
					return;
				}
			}
			// if not found
			LeftEquivalenceClass eclass = new LeftEquivalenceClass(itemsetJ, tidsJ, occurencesJ);
			list.add(eclass);
			eclass.rules.add(leftRule);
		}
	}


	public Map<Integer, Map<Integer, List<LeftEquivalenceClass>>> getStore() {
		return store;
	}
}
