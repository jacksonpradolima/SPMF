package ca.pfv.spmf.algorithms.episodes.upspan;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
/* This file is copyright (c) Wu et al. 2013
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
 * Implementation of an algorithm to calculate information about a transaction database with utility information by Wu et al.
 * Obtained from the UP-Miner software under the GPL 3 license.
 * 
 * @author Wu et al. ౮p  V01.2013.01.29
 *
 */
public class CalculateDatabaseInfo {
	private String inputPath;
	private long totalUtility = 0;
	private int databaseSize = 0;
	private int maxID = 0;
	private int sumAllLength = 0;
	private double avgLength = 0.0;
	private int maxLength = 0;
	private Set<Integer> allItem = new HashSet<Integer>();
	
	public CalculateDatabaseInfo(String inputPath) {
		this.inputPath = inputPath;
	}
	
	public boolean runCalculate(){
		BufferedReader br;
		String line = ""; // get the String by reading from BufferedReader
		String[] tokens1, tokens2; // get the result of split String 
		
		try {
			br = new java.io.BufferedReader(new java.io.FileReader(inputPath));
			
			while(true)
		    {
				line = br.readLine();
				if(line == null)
				{
					break;
				}
				databaseSize++;
				tokens1 = line.split(":"); // divide into 3 parts : 1.itemsets 2.transaction utility 3.utility of each item 
				tokens2 = tokens1[0].split(" "); // divide itemsets into items
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
			avgLength = (int)((double)sumAllLength/databaseSize*100)/100.0; // accurate to the second decimal place
			br.close();

		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false; // failure
		}
		return true;
	}
	
	// write the result to specified file
	public void OutputResult(String outputPath){	
		PrintWriter output = null;
		try {
			output = new PrintWriter(outputPath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		System.out.println("----------Database Information----------");
		System.out.println("Input file path : " + inputPath);
		System.out.println("Output file path : " + outputPath);
		System.out.println("Number of transations : " + String.valueOf(databaseSize));
		System.out.println("Total utility : " + String.valueOf(totalUtility));
		System.out.println("Number of distinct items : " + String.valueOf(allItem.size()));
		System.out.println("Maximum Id of item : " + String.valueOf(maxID));
		System.out.println("Average length of transaction : " + String.valueOf(avgLength));
		System.out.println("Maximum length of transaction : " + String.valueOf(maxLength));
		
		output.println("----------Database Information----------");
		output.println("Input file path : " + inputPath);
		output.println("Output file path : " + outputPath);
		output.println("Number of transations : " + String.valueOf(databaseSize));
		output.println("Total utility : " + String.valueOf(totalUtility));
		output.println("Number of distinct items : " + String.valueOf(allItem.size()));
		output.println("Maximum Id of item : " + String.valueOf(maxID));
		output.println("Average length of transaction : " + String.valueOf(avgLength));
		output.println("Maximum length of transaction : " + String.valueOf(maxLength));
		
		output.close();
	}
	
	public int getMaxID(){
		return maxID;
	}
	
	public int getMaxLength(){
		return maxLength;
	}
	
	public int getDBSize(){
		return databaseSize;
	}

}
