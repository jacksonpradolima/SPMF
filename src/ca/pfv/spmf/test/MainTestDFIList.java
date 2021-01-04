package ca.pfv.spmf.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.DFIList.AlgoDFIList;
/* This is an implementation of the DFI-Growth algorithm. 
* 
* Copyright (c) 2018  Wu Cheng-Wei, Huang Jian-Tao
* 
* This file is part of the SPMF DATA MINING SOFTWARE 
* (http://www.philippe-fournier-viger.com/spmf). 
* 
* SPMF is free software: you can redistribute it and/or modify it under the 
* terms of the GNU General Public License as published by the Free Software 
* Foundation, either version 3 of the License, or (at your option) any later version. 
*
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY 
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR 
* A PARTICULAR PURPOSE. See the GNU General Public License for more details. 
* 
* You should have received a copy of the GNU General Public License along with 
* SPMF. If not, see <http://www.gnu.org/licenses/>. 
* 
* @author Wu Cheng-Wei et al 2020
*/
/**
 * Example of how to use DFI-List algorithm from the source code and save
 * the results to a file.
 */

public class MainTestDFIList {
	
	public static void main(String [] arg) throws FileNotFoundException, IOException{
		// the file paths
		String input = fileToPath("contextMushroom_FCI90.txt");  // the database
		String output = ".//output.txt";  // the path for saving the frequent itemsets found

		// Applying the algorithm
		AlgoDFIList algo = new AlgoDFIList();
		algo.runAlgorithm(input,output);
		algo.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		 URL url = MainTestDFIList.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
	
}
