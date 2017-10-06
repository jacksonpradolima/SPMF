package ca.pfv.spmf.test;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.sequential_rules.husrm.AlgoHUSRM;
/* This file is copyright (c) 2008-2015 Philippe Fournier-Viger
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
 *  This example files shows how to run the HUSRM algorithm
 * @author Souleymane Zida and Philippe Fournier-Viger, 2015.
 */
public class MainTestHUSRM_saveToFile {
	
	public static void main(String [] arg) throws IOException{
		// THIS IS THE INPUT FILE:
		String input = fileToPath("DataBase_HUSRM.txt"); 
		// THIS IS THE OUTPUT FILE PATH FOR SAVING HIGH UTILITY SEQUENTIAL RULES
		String output = "./output.txt";  
     

		// THIS IS THE MINIMUM CONFIDENCE PARAMETER  (e.g. 70 %)
		double minconf = 0.70;

		// THIS IS THE MINIMUM UTILITY PARAMETER  (e.g. 30 $ ) 
		double minutil = 40;
		
		//  THESE ARE ADDITIONAL PARAMETERS
		//   THE FIRST PARAMETER IS A CONSTRAINT ON THE MAXIMUM NUMBER OF ITEMS IN THE LEFT SIDE OF RULES
		// For example, we don't want to find rules with more than 4 items in their left side
		int maxAntecedentSize = 4;
		//   THE SECOND PARAMETER IS A CONSTRAINT ON THE MAXIMUM NUMBER OF ITEMS IN THE RIGHT SIDE OF RULES
		// For example, we don't want to find rules with more than 4 items in their right side
		int maxConsequentSize = 4;
		
		// This parameter let the user specify how many sequences from the input file should be used.
		// For example, it could be used to read only the first 1000 sequences of an input file
		int maximumSequenceCount = Integer.MAX_VALUE;
		
		// This create the algorithm and run it
		// Results will be output to the file
		AlgoHUSRM algo = new AlgoHUSRM();
		algo.runAlgorithm(input, output, minconf, minutil, maxAntecedentSize, maxConsequentSize, maximumSequenceCount);
		
		// print statistics
		algo.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestHUSRM_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
