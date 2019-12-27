package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.chud.AlgoCHUD;

/* This file is copyright (c) 2018 Philippe Fournier-Viger
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
 * This example shows how to run the original source code of the CHUD
 * algorithm from the source code.
 * 
 * It is to be noted that the original CHUD algorithm has different implementations
 * assumptions than other algorithms offered in SPMF. In particular, this algorithm
 * first convert a database to a vertical database before performing data mining.
 * 
 * @author Philippe Fournier-Viger, 2015 
 */
public class MainTestCHUD {


	public static void main(String [] arg) throws NumberFormatException, IOException{
		
		// input file path
		String inputFilePath = fileToPath("DB_Utility.txt");
		
		// output file path
		String outputFilePath = "output.txt";

		// minimum utility threshold
		int minUtility = 1; //
		
		// run the algorithm
		AlgoCHUD algo = new AlgoCHUD();
		algo.runAlgorithm(inputFilePath, outputFilePath,  minUtility);
		
		// print statistics about the execution of the algorithm
		algo.printStats();
	}

	

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestCHUD.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
