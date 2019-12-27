package ca.pfv.spmf.algorithms.frequentpatterns.opusminer;

import java.util.ArrayList;

/**
 * The FisherTest class from the Opus-Miner algorithm proposed in : </br></br>
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
/* globals.cpp - a module of OPUS Miner providing global variable
 * declarations. Copyright (C) 2012 Geoffrey I Webb
 **
 ** This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 ** 
 ** This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 ** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 ** 
 ** You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

public class Global {


	public static void expandAlpha(int depth) {
		if (alpha.isEmpty()) {
			// alpha[0[ and [1] are not used.
			alpha.add(1.0);
			alpha.add(1.0);
			if (depth <= 1) {
				return;
			}
		}

		if (depth > noOfItems) {
			alpha.add(0.0);
		} else if (depth == noOfItems) {
			alpha.add(alpha.get(depth - 1)); // at deepest level so might as
												// well use as much of the rest
												// of the probability mass as
												// possible
		} else {
			// C++ TO JAVA CONVERTER WARNING: Unsigned integer types have no
			// direct equivalent in Java:
			// ORIGINAL LINE: unsigned int i;
			int i;
			for (i = alpha.size(); i <= depth; i++) {
				alpha.add(Math.min((Math.pow(0.5, (int) (depth - 1)) / Math.exp(FisherTest.log_combin(noOfItems, depth))) * 0.05,
						alpha.get(depth - 1)));
			}
		}
	}
	
	static double getAlpha(int depth) {
	  if (!correctionForMultCompare) {
	    return 0.05;
	  }
	  
	  if (depth >= alpha.size()) {
	    expandAlpha(depth);
	  }

	  return alpha.get(depth);
	}
	

	/////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static int k = 100; // the number of associations to return
	public static boolean filter = true; // if true perform a filter for
											// self-sufficiency
	public static boolean correctionForMultCompare = true; // if true we should
															// correct alpha for
															// the size of the
															// search space
	public static int noOfTransactions = 0;
	public static int noOfItems = 0;
	public static ArrayList<tidset> tids = new ArrayList<tidset>();
	public static ArrayList<Double> alpha = new ArrayList<Double>();
	
	// PHILIPPE: store the name of each item
	// The ith-position is the name of item "i"
	public static ArrayList<String> itemNames = new ArrayList<String>();

	public static boolean searchByLift = false;
	public static boolean redundancyTests = true;
	public static boolean printClosures = false;

}