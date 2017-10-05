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
 * This class represents an Element of a utility list as used by the FHN algorithm.
 * 
 * @see AlgoHUIMiner
 * @see UtilityListFHN
 * @see Element
 * @author Philippe Fournier-Viger
 */
public class ElementFHN extends Element{
	/** itemset utility */
	public final int inutils;  
	
	/**
	 * Constructor. 
	 * @param tid  the transaction id
	 * @param iutils  the itemset utility (for positive items)
	 * @param inutils  the itemset utility (for negative items)
	 * @param rutils  the remaining utility
	 */
	public ElementFHN(int tid, int iutils, int inutils, int rutils){
		super(tid, iutils, rutils);
		this.inutils = inutils;
	}
}
