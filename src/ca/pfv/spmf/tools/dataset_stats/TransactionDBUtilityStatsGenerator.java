package ca.pfv.spmf.tools.dataset_stats;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import ca.pfv.spmf.algorithms.frequentpatterns.chud.AlgoCHUD;

/* This file is copyright (c) Cheng-Wei Wu et al.
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
 * This class is used to calculate statistics about a transaction database with
 * utility information. It was obtained from UP-Miner under the GPL license.
 * 
 * @author Cheng-Wei Wu et al.    // Adapted by Philippe Fournier-Viger for SPMF.
 */

public class TransactionDBUtilityStatsGenerator {

	
	/**
	 * Constructor
	 */
	public TransactionDBUtilityStatsGenerator() {
		
	}
	
	/**
	 * Calculate information about a database with utility information
	 * @param inputPath an input file path
	 * @throws IOException 
	 */
	public void runAlgorithm(String inputPath) throws IOException{
		
		/** The total utility in this database */
		long totalUtility = 0;
		
		/** the number of transactions in this database */
		int databaseSize = 0;
		
		/** the maximum ID of items in this database */
		int maxID = 0;
		
		/** the sum of all transaction length for this database */
		int sumAllLength = 0;
		
		/** the average transaction length in this database */
		double avgLength = 0.0;
		
		/** the maximum length of transactions in this database */
		int maxLength = 0;
		
		/** the set of all items in this database */
		Set<Integer> allItem = new HashSet<Integer>();

		BufferedReader br;
		String line = ""; // get the String by reading from BufferedReader
		String[] tokens1, tokens2; // get the result of split String 
		
		br = new java.io.BufferedReader(new java.io.FileReader(inputPath));
		
		while(true)
	    {
			line = br.readLine();
			if(line == null)
			{
				break;
			}
			databaseSize++;
			 // divide into 3 parts : 1.itemsets 2.transaction utility 3.utility of each item 
			tokens1 = line.split(":");			
			// divide itemsets into items
			tokens2 = tokens1[0].split(" "); 
			totalUtility += Long.parseLong(tokens1[1]);
			sumAllLength += tokens2.length;
			if(maxLength < tokens2.length){
				maxLength = tokens2.length;
			}
			
			for(int i=0;i<tokens2.length;i++){
				int num = Integer.parseInt(tokens2[i]);
				if(num > maxID){
					maxID = num;
				}
				allItem.add(num);
			}
	    }
		br.close();
		
		avgLength = (int)((double)sumAllLength/databaseSize*100)/100.0; // accurate to the second decimal place

		// Print the calculated information to the console
		System.out.println("----------Database Information----------");
		System.out.println("Number of transations : " + String.valueOf(databaseSize));
		System.out.println("Total utility : " + String.valueOf(totalUtility));
		System.out.println("Number of distinct items : " + String.valueOf(allItem.size()));
		System.out.println("Maximum Id of item : " + String.valueOf(maxID));
		System.out.println("Average length of transaction : " + String.valueOf(avgLength));
		System.out.println("Maximum length of transaction : " + String.valueOf(maxLength));

	}

}
