/*
* This is an implementation of the CEPB, corCEPB, CEPN algorithm.
*
* 
* This file is part of the SPMF DATA MINING SOFTWARE * (http://www.philippe-fournier-viger.com/spmf).
*
* SPMF is free software: you can redistribute it and/or modify it under the * terms of the GNU General Public License as published by the Free Software * Foundation, either version 3 of the License, or (at your option) any later * version. *

* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR * A PARTICULAR PURPOSE. See the GNU General Public License for more details. *
*
* You should have received a copy of the GNU General Public License along with SPMF. If not, see <http://www.gnu.org/licenses/>.
*
* Copyright (c) 2019 Jiaxuan Li
*/

package ca.pfv.spmf.test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ca.pfv.spmf.algorithms.sequentialpatterns.cost.AlgoCEPM;
import ca.pfv.spmf.algorithms.sequentialpatterns.cost.SequentialPatterns;

/**
 * Example of how to use the CEPN algorithm to extract cost-effective patterns,
 * in source code, and then save the result to file.
 * 
 * @see AlgoCEPM
 * @author Jiaxuan Li
 *
 */
class MainTestCEPN {

	public static void main(String[] args) throws IOException {
		// the input file
		String inputFile = fileToPath("example_CEPN.txt");

		// the output file path (can be set to null if you dont want to save results to
		// a file)
		String outputFile = "output.txt";

		// minimum support and maximum cost threshold
		int minsup = 2;
		double maxcost = 50;
		double minoccupancy = 0.1;

		// if true, patterns in the output file are sorted by utility
		boolean sortByUtility = true;

		// if true, only patterns with lowest trade-off are output for each utility
		// value
		boolean outputLowestTradeOff = false;

		// run the algorithm
		AlgoCEPM algo = new AlgoCEPM();

		// The following line allows to set a constraint on the maximum length of a pattern.
		algo.setMaximumPatternLength(100);
		
		/* activate the lowerBound pruning strategy */
		algo.setUseLowerBound(true);

		// found cost-effective patterns
		SequentialPatterns patterns = algo.runAlgorithmCEPN(inputFile, outputFile, minsup, maxcost, minoccupancy, sortByUtility,
				outputLowestTradeOff);

		// output the cost-effective patterns in Console and save the cost-effective
		// pattern into the .txt file

		DecimalFormat df = new DecimalFormat("0.00");

		// ============ group patterns in terms of their utility ============
//		System.out.println("== THE PATTERNS GROUPED BY UTILITY VALUES: == ");
//		Map<Integer, List<SequentialPattern>> chooseMapTuiTrade = algo.chooseMapUtilTrade(patterns);
//		for (Entry<Integer, List<SequentialPattern>> entry : chooseMapTuiTrade.entrySet()) {
//			System.out.println("#UTIL: " + entry.getKey() + ":");
//			for (SequentialPattern pattern : entry.getValue()) {
//				System.out.println(pattern.eventSetstoString() + " #SUP: " + pattern.getAbsoluteSupport() + " #TRADE: "
//						+ df.format(pattern.getTradeOff()) + " #AVGCOST: " + df.format(pattern.getAverageCost()));
//			}
//		}

		// ============ output the lowest trade-off for each utility ============
//		System.out.println("== THE PATTERN HAVING THE LOWEST TRADE-OFF FOR EACH UTILITY VALUE: == ");
//		Map<Integer, SequentialPattern> smallestTraPattern = algo.chooseSmallMapUtiTrade(patterns);
//		for (Entry<Integer, SequentialPattern> entry : smallestTraPattern.entrySet()) {
//
//			SequentialPattern pattern = entry.getValue();
//			System.out.println(pattern.eventSetstoString() + " #SUP: " + pattern.getAbsoluteSupport() + " #TRADE: "
//					+ df.format(pattern.getTradeOff()) + " #AVGCOST: " + df.format(pattern.getAverageCost()));
//		}
		
		// Print the algorithm's statistics
		algo.printStatistics();

	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException {
		URL url = MainTestCEPN.class.getResource(filename);
		return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
	}
}