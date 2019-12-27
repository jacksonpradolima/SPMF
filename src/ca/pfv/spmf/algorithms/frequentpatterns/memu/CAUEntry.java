package ca.pfv.spmf.algorithms.frequentpatterns.memu;

/* This file is copyright (c) 2008-2019 Shi-Feng Ren
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
*
*/

/**
 * This is an implementation of the "MEMU" algorithm for High-Average-Utility Itemsets Mining
 * as described in the conference paper : <br/><br/>
 *
 * Jerry Chun-Wei Lin, Shifeng Ren, and Philippe Fournier-Viger. MEMU: More Efficient Algorithm to Mine High Average-Utility Patterns with Multiple Minimum Average-Utility Thresholds (unpublished)
 *
 * @see CAUList
 * @see AlgoMEMU
 * @author Shi-Feng Ren
 */
 class CAUEntry {

	/** Transaction identifier */
	final int tid;

	/** utility */
	final int utility;

	/** rmu value */
	int rmu;

	/** remu value */
	int remu;

	/**
	 * Constructor.
	 * @param tid  the transaction id
	 * @param utility  the itemset utility
	 * @param remu  the maximal utility of the transaction
	 */
	public CAUEntry(int tid, int utility, int rmu, int remu){
		this.tid=tid;
		this.utility = utility;
		this.rmu = rmu;
		this.remu = remu;
	}
}
