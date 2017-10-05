package ca.pfv.spmf.test;

/* This is an implementation of the FFI-Miner algorithm. 
* 
* Copyright (c) 2016 FFI-Miner
* 
* This file is part of the SPMF DATA MINING SOFTWARE * (http://www.philippe-fournier-viger.com/spmf). 
* 
* 
* SPMF is free software: you can redistribute it and/or modify it under the * terms of the GNU General Public License as published by the Free Software * Foundation, either version 3 of the License, or (at your option) any later * version. * 

* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR * A PARTICULAR PURPOSE. See the GNU General Public License for more details. * 
* 
* You should have received a copy of the GNU General Public License along with * SPMF. If not, see . 
* 
* @author Ting Li
*/

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.ffi_miner.AlgoFFIMiner;

/**
 * Example of how to use the FFIMiner algorithm 
 * from the source code.
 * @author Ting Li, 2016
 */
public class MainTestFFIMiner_saveToFile {

	public static void main(String [] arg) throws IOException{
		
		// input file path
		String input = fileToPath("contextFFIMiner.txt");
		// output file path
		String output = ".//output.txt";

		// the minsup parameter
		float min_support = 2;  
		
		// Applying the FFIMiner algorithm
		AlgoFFIMiner FFIminer = new AlgoFFIMiner();
		FFIminer.runAlgorithm(input, output, min_support);
		FFIminer.printStats();

	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestFFIMiner_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
