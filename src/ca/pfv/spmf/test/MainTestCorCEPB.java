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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ca.pfv.spmf.algorithms.sequentialpatterns.cost.AlgoCEPM;
import ca.pfv.spmf.algorithms.sequentialpatterns.cost.SequentialPatterns;

/**
 * Example of how to use the corCEPB algorithm to extract cost-effective
 * patterns, in source code, and then save the result to file.
 * 
 * @see AlgoCEPM
 * @author Jiaxuan Li
 *
 */
class MainTestCorCEPB {

	public static void main(String[] args) throws IOException {
		// the input and output files
		String inputFile = fileToPath("example_CorCEPB.txt");
		String outputFile =  "output.txt";

		// minimum support and maximum cost threshold
		int minsup = 2;
		double maxcost = 50;
		double minoccupancy = 0.1;
		
		// if true, patterns in the output file are sorted by correlation
		boolean sortByCorrelation = false;

		// run the corCEPB algorithm
		AlgoCEPM algo = new AlgoCEPM();
		
		// The following line allows to set a constraint on the maximum length of a pattern.
		algo.setMaximumPatternLength(2);
		
		/* activate the lowerBound pruning strategy*/
		algo.setUseLowerBound(true);
		
		// found cost-effective patterns
		SequentialPatterns patterns = algo.runAlgorithmCorCEPB(inputFile, outputFile, minsup, maxcost, minoccupancy, sortByCorrelation);
		// output the cost-effective patterns in Console and save the cost-effective
		// pattern into the .txt file
//		System.out.println("==PATTERNS FOUND==");
//		DecimalFormat df = new DecimalFormat("0.000");
//		Map<Double, List<SequentialPattern>> list = algo.sortByCorrelation(patterns);
//		for (Map.Entry<Double, List<SequentialPattern>> entry : list.entrySet()) {
//			for (SequentialPattern token : entry.getValue()) {
//				System.out.println(token.eventSetstoString() + " #SUP  : " + token.getAbsoluteSupport()
//						+ " #CORR: " + df.format(token.getCorrelation()) + " #AVGCOST: "
//						+ df.format(token.getAverageCost()));
//			}
//		}
		
		// Print the algorithm's statistics
		algo.printStatistics();
	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException {
		URL url = MainTestCorCEPB.class.getResource(filename);
		return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
	}
}
