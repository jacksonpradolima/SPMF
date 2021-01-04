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
import java.util.List;

import ca.pfv.spmf.algorithms.sequentialpatterns.cost.AlgoCEPM;
import ca.pfv.spmf.algorithms.sequentialpatterns.cost.SequentialPatterns;

/**
 * Example of how to use the CEPB algorithm to extract cost-effective patterns,
 * in source code, and then save the result to file.
 * 
 * @see AlgoCEPM
 * @author Jiaxuan Li
 *
 */
class MainTestCEPB {

	public static void main(String[] args) throws IOException {
		// the input and output files
		String inputFile = fileToPath("example_CEPB.txt");
		String outputFile = "output.txt";
		
		// minimum support and maximum cost thresholds
		int minsup = 2;
		double maxcost = 50;
		
		double minoccupancy = 0.1;

		// run the CEPB algorithm
		AlgoCEPM algo = new AlgoCEPM();
		
		/* activate the lowerBound pruning strategy*/
		algo.setUseLowerBound(true);
		
		// The following line allows to set a constraint on the maximum length of a pattern.
//		algo.setMaximumPatternLength(2);
		
		// found cost-effective patterns
		SequentialPatterns patterns = algo.runAlgorithmCEPB(inputFile, outputFile, minsup, maxcost, minoccupancy);
		
		// Print statistics about the algorithm
		algo.printStatistics();
		
		// The output has been written to the output file. But if alternatively, you want to print patterns to
		// the console, you can use the following code:
//		System.out.println("==PATTERNS FOUND==");
//		DecimalFormat df = new DecimalFormat("0.000");
//		for (List<SequentialPattern> level : patterns.levels) {
//			for (SequentialPattern pattern : level) {
//				System.out.println(pattern.eventSetstoString() + " #SUP: " + pattern.getAbsoluteSupport()
//						+ " #AVGCOST: " + df.format(pattern.getAverageCost()));
//			}
//		}
	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException {
		URL url = MainTestCEPB.class.getResource(filename);
		return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
	}
}
