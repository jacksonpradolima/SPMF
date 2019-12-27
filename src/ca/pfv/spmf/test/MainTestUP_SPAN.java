package ca.pfv.spmf.test;
/* This file is copyright (c) Philippe Fournier-Viger 2018
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.episodes.upspan.AlgoUP_Span;

/**
 * This is an example of how to run the UP-SPAN algorithm and save the result to
 * a file
 * 
 * @author Philippe Fournier-Viger 2018
 *
 */
public class MainTestUP_SPAN {

	public static void main(String[] args) throws IOException {

		// Input file
		String inputFile = fileToPath("exampleTUP.txt");

		// Output file
		String outputFile = "output.txt";

		// Minimum utility threshold (%)
		double minUtilityPercentage = 0.56;
		
		// If true, single events will also be output in the results
		boolean outputSingleEvents = false;

		// Maximum time duration
		int maximumTimeDuration = 2;

		AlgoUP_Span algorithm = new AlgoUP_Span();
		algorithm.runAlgorithm(inputFile, outputFile, minUtilityPercentage, maximumTimeDuration, outputSingleEvents);

		algorithm.printStats();
	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException {
		URL url = MainTestUP_SPAN.class.getResource(filename);
		return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
	}
}
