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

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import ca.pfv.spmf.algorithms.sequential_rules.cmdeogun.AlgoCMDeogun;
import ca.pfv.spmf.patterns.itemset_array_integers_with_tids.Itemset;

/**
 * This class represents a sequential rule found by the CMDeo algorithm.
 * 
 * @see AlgoCMDeogun
 * @see Itemset
 * @author Philippe Fournier-Viger
 */
public class Rule {
	
	int[] itemsetI;
	int[] itemsetJ; 
	Set<Integer> tidsI;
	Set<Integer> tidsJ;
	Set<Integer> tidsIJ;
	Map<Integer, Occurence> occurencesI;
	Map<Integer, Occurence> occurencesJ;

	public Rule(int[] itemsetI, int[] itemsetJ, Set<Integer> tidsI,
			Set<Integer> tidsJ, Set<Integer> tidsIJ,
			Map<Integer, Occurence> occurencesI,
			Map<Integer, Occurence> occurencesJ) {
			this.itemsetI = itemsetI;
			this.itemsetJ = itemsetJ;
			this.tidsI = tidsI;
			this.tidsJ = tidsJ;
			this.tidsIJ = tidsIJ;
			this.occurencesI = occurencesI;
			this.occurencesJ = occurencesJ;
	}
	
	@Override
	public String toString() {
		return Arrays.toString(itemsetI) + " ==> " + Arrays.toString(itemsetJ);
	}

}
