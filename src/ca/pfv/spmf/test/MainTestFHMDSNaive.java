package ca.pfv.spmf.test;


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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.fhmds.naive.AlgoFHMDS_Naive;

/**
 * This example shows how to use the FHM-DS-NAIVE algorithm using the source code of SPMF.
 * @author Siddharth Dawar et al.
 *
 */
public class MainTestFHMDSNaive {

	public static void main(String[] args) throws IOException {
		
		String input = fileToPath("DB_Utility.txt");
		String output = ".//output.txt";
		
		int k =  5;
		
		// Win size is the number of batches in a window
		int win_size = 2;
		
		// number_of_transactions_batch is the number of transactions in a batch
		int number_of_transactions_batch = 2;

		// Run the algorithm
		AlgoFHMDS_Naive algorithm = new AlgoFHMDS_Naive();
		algorithm.runAlgorithm(
				input,
				k,
				win_size, 
				number_of_transactions_batch, output);
		
		algorithm.printStats();

	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestFHM.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}

}
