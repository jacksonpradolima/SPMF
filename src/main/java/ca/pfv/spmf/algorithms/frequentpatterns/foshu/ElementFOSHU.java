package ca.pfv.spmf.algorithms.frequentpatterns.foshu;


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
 * This class represents an Element of a utility list as used by the FOSHU algorithm.
 * 
 * @see AlgoFOSHU
 * @see UtilityListFOSHU
 * @author Philippe Fournier-Viger
 */
public class ElementFOSHU {
	/** tid  (transaction id)**/
	public final int tid;
	/** itemset utility */
	public final int iputils; 
	/** itemset utility */
	public final int inutils; 
	/** remaining utility */
	public final int rutils; 
	
	/**
	 * Constructor.
	 * @param tid  the transaction id
	 * @param iutils  the itemset utility
	 * @param rutils  the remaining utility
	 */
	public ElementFOSHU(int tid, int iputils, int inutils, int rutils){
		this.tid = tid;
		this.iputils = iputils;
		this.inutils = inutils;
		this.rutils = rutils;
	}
}
