package ca.pfv.spmf.algorithms.frequentpatterns.opusminer;

public class FindClosure {
	/**
	 * The FindClosure class from the Opus-Miner algorithm proposed in : </br></br>
	 * 
	 * Webb, G.I. & Vreeken, J. (2014) Efficient Discovery of the Most Interesting Associations.
	  ACM Transactions on Knowledge Discovery from Data. 8(3), Art. no. 15.
	 *
	 *  The code was translated from C++ to Java.  The modifications to the original C++ code to obtain Java
	 *  code and improvements are copyright by Xiang Li and Philippe Fournier-Viger, 
	 *  while the original C++ code is copyright by Geoff Web.
	 *  
	 *  The code is under the GPL license.
	 */
	//=========================================
	// This is the header of the original C++ code:
	//  
	 /* find_closure.cpp - a module of OPUS Miner providing find_closure, a
	 * function to find the closure of an itemset. Copyright (C) 2012 Geoffrey I
	 * Webb** This program is free software: you can redistribute it and/or
	 * modify it under the terms of the GNU General Public License as published
	 * by the Free Software Foundation, either version 3 of the License, or (at
	 * your option) any later version.** This program is distributed in the hope
	 * that it will be useful, but WITHOUT ANY WARRANTY; without even the
	 * implied warranty of* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
	 * See the GNU General Public License for more details.** You should have
	 * received a copy of the GNU General Public License along with this
	 * program. If not, see <http://www.gnu.org/licenses/>.
	 */

	public static void find_closure(itemset is, itemset closure) {
		tidset thistids = new tidset();

		closure.addAll(is);

		thistids = Utils.gettids(is, thistids);

		Integer item = null;
	
		for (item = 1; item <= Global.noOfItems; item++) {
			if (Global.tids.get(item).size() >= thistids.size()
					&& is.contains(item) == false
					&& tidset
							.countIntersection(thistids, Global.tids.get(item)) == thistids
							.size()) {
				closure.add(item);
			}
		}
	}
}