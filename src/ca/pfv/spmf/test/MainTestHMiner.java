package ca.pfv.spmf.test;

import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.hminer.AlgoHMiner;

/* This file is copyright (c) 2018+  by Siddharth Dawar et al.
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
 * Example of how to run the HMiner algorithm from the source code of SPMF
 *
 */
public class MainTestHMiner {

	public static void main(String[] args) {
		try {

			String input = fileToPath("DB_Utility.txt");
			String output = ".//output.txt";

			long min_utility = 30; //

			AlgoHMiner algorithm = new AlgoHMiner();

			boolean applyTransactionMergingOptimization = true;
			boolean applyEUCSOptimization = true;

			algorithm.runAlgorithm(input, output, min_utility,
					applyTransactionMergingOptimization, applyEUCSOptimization);
			algorithm.printStats();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String fileToPath(String filename)
			throws UnsupportedEncodingException {
		URL url = MainTestFHM.class.getResource(filename);
		return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
	}
}
