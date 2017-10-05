package ca.pfv.spmf.test;

/* This is an implementation of the HAUI-MMAU algorithm. 
* 
* Copyright (c) 2016 HAUI-MMAU
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

import ca.pfv.spmf.algorithms.frequentpatterns.haui_mmau.AlgoHAUIMMAU;
import ca.pfv.spmf.algorithms.frequentpatterns.haui_mmau.ItemsetsTP;
import ca.pfv.spmf.algorithms.frequentpatterns.haui_mmau.UtilityTransactionDatabaseTP;

/**
 * Example of how to use the HAUIMMAU Algorithm in source code.
 * @author Ting Li, 2016
 */
public class MainTestHAUIMMAU_saveToFile {
	
	public static void main(String [] args) throws IOException{
		String input = fileToPath("contextHAUIMMAU.txt");
		String minutilityPath=fileToPath("MAU_Utility.txt");
		String output = ".//output.txt";
		int GLMAU=0;
		// Loading the database into memory
		UtilityTransactionDatabaseTP database = new UtilityTransactionDatabaseTP();
		database.loadFile(input,minutilityPath);
		//database.printDatabase();
		
		//Applying the HAUIMMAU algorithm
		AlgoHAUIMMAU HAUIMMAU = new AlgoHAUIMMAU();
		ItemsetsTP highAUtilityItemsets = HAUIMMAU.runAlgorithm(database, database.mutipleMinUtilities, GLMAU);
		 highAUtilityItemsets.saveResultsToFile(output, database.size(),  GLMAU);
		 HAUIMMAU.printStats();
	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestHAUIMMAU_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
	
}
