package ca.pfv.spmf.test;

/* This is an implementation of the HAUI-Miner algorithm. 
* 
* Copyright (c) 2016 HAUI-Miner
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

import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.memu.AlgoMEMU;

/**
 * Example of how to use the MEMU algorithm 
 * from the source code.
 */
public class MainTestMEMU {

	public static void main(String [] arg) throws Exception{
		
		// Input/output file paths
		String inputProfit = fileToPath("UtilityDB_profit.txt");
		String inputDB = fileToPath("UtilityDB.txt");
		String output = "output.txt";

		// parameters
		int beta = 2;
		int GLMAU = 25;
		
		// Applying the HAUIMiner algorithm
		AlgoMEMU algorithm = new AlgoMEMU();
		algorithm.runAlgorithm(inputProfit, inputDB, output, beta, GLMAU);
		algorithm.printStats();
 
	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestMEMU.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
