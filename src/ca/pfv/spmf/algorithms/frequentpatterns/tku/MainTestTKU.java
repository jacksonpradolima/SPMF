package ca.pfv.spmf.algorithms.frequentpatterns.tku;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
/* This file is copyright (c) Fournier-Viger, Philippe 2018
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
 * Example of how to use the TKU algorithm 
 * from the source code.
 * @author (c) Philippe Fournier-Viger, 2018
 */
public class MainTestTKU {

	public static void main(String [] arg) throws IOException{

		// Input file path
		String input = fileToPath("DB_Utility.txt");
		
		// Output file path
		String output = "output.txt";
		
		// The parameter k
		int k = 3;  

		// Applying the algorithm
		AlgoTKU algo = new AlgoTKU();
		algo.runAlgorithm(input, output, k);
		
		// Display statistics about the execution of the algorithm
		algo.printStats();
	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestTKU.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
