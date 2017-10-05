package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.skymine.AlgoSkyMine;
import ca.pfv.spmf.algorithms.frequentpatterns.skymine.UPTree;

/**
 * This is an example of how to run the Skymine algorithm
 * 
 * Copyright (c) 2015 Vikram Goyal, Ashish Sureka, Dhaval Patel, Siddharth
 * Dawar
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE *
 * (http://www.philippe-fournier-viger.com/spmf).
 * 
 * SPMF is free software: you can redistribute it and/or modify it under the
 * * terms of the GNU General Public License as published by the Free
 * Software * Foundation, either version 3 of the License, or (at your
 * option) any later * version. SPMF is distributed in the hope that it will
 * be useful, but WITHOUT ANY * WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with SPMF. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @see AlgoSkyMine
 * @see UPTree
 * 
 * @author Vikram Goyal, Ashish Sureka, Dhaval Patel, Siddharth Dawar
 * */
public class MainTestSkyMine_saveToFile {


	/**
	 * Main method
	 * @param args command line arguments
	 * @throws IOException  if error reading or writing to file)
	 */

	public static void main(String args[]) throws IOException {
		
		// Input file paths
		String transactionFile = fileToPath("SkyMineTransaction.txt");
		String utilityTableFile = fileToPath("SkyMineItemUtilities.txt");
		
		// Output file path
		String outputFilePath = "output.txt";
		
		// This is to choose whether optimizations should be used by the algorithms or not
		// If they are set to false, it is the baseline algorithm.
		boolean usePreInsertingSingleAndPairs = true;
		boolean useRaisingUMinByNodeUtilities = true;

		// Create an instance of the algorithm
		AlgoSkyMine up = new AlgoSkyMine();

		// run the algorithm
		up.runAlgorithm(transactionFile, utilityTableFile, outputFilePath, usePreInsertingSingleAndPairs, useRaisingUMinByNodeUtilities);
		
		// print statistics about the algorithm execution
		up.printStats();
	}
	
	/**
	 * Get the full path of a given file
	 * @param filename the file name
	 * @return the path as a String
	 * @throws UnsupportedEncodingException
	 */
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestSkyMine_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
