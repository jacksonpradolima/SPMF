package ca.pfv.spmf.algorithms.frequentpatterns.hui_miner;

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



/**
 * This class represents a UtilityList as used by the FCHM algorithm.
 *
 * @see AlgoHUIMiner
 * @see Element
 * @author Philippe Fournier-Viger
 */
class UtilityListFCHM  extends UtilityList{

	
	BitSetSupport bitsetDisjunctiveTIDs = null;
	
	/**
	 * Constructor.
	 * @param item the item that is used for this utility list
	 * @param bitset representing the disjunctive set of transaction ids
	 */
	public UtilityListFCHM(int item, BitSetSupport bitsetDisjunctiveTIDs){
		super(item);
		this.bitsetDisjunctiveTIDs = bitsetDisjunctiveTIDs;
	}

	/**
	 * Get the bond of this utility list
	 * @return the bond
	 */
	public double getBond() {
		return elements.size() / ((double)bitsetDisjunctiveTIDs.support);
	}
}
