package ca.pfv.spmf.algorithms.frequentpatterns.vhuqi;

/* This file is copyright (c) Cheng-Wei Wu et al. and obtained under GPL license from the UP-Miner software.
 *  The modification made for integration in SPMF are (c) Philippe Fournier-Viger
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 * 
 * SPMF is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * This is an implementation of a QItem
 * 
 * @see AlgoVHUQI
 * @author Cheng Wei wu et al.
 */

class QItemTrans {

	/** transaction id */
	private int tid;
	/** estimated utility */
	private int eu;
	/** remaining utility */
	private int ru;

	/** 
	 * Constructor
	 * @param tid transaction id
	 * @param eu estimated utility
	 * @param ru remaining utility
	 */
	QItemTrans(int tid, int eu, int ru) {
		this.tid = tid;
		this.eu = eu;
		this.ru = ru;
	}

	/**
	 * Get the transaction id
	 * @return the transaction id
	 */
	int getTid() {
		return tid;
	}

	/**
	 * Get the estimated utility
	 * @return the estimated utility
	 */
	int getEu() {
		return eu;
	}

	/**
	 * Get the remaining utility
	 * @return the remaining utility
	 */
	int getRu() {
		return ru;
	}

	/**
	 * Get the sum of remaining and estimated utility
	 * @return the sum
	 */
	int sum() {
		return eu + ru;

	}

	/** 
	 * Get a string representation of this object
	 * @return a string
	 */
	public String toString() {
		return tid + " " + eu + "	" + ru;
	}
}