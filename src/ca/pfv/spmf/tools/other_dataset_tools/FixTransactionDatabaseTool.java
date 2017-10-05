package ca.pfv.spmf.tools.other_dataset_tools;

/* This file is copyright (c) 2008-2012 Philippe Fournier-Viger
* 
* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
* 
* SPMF is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This tool allows to fix some common problems in a transaction database file in SPMF format.
 * In particular: (1) the tool removes items that appears more than once in a transaction.
 * (2) it sort transactions according to the lexicographical ordering.
 * The reason for performing this is that many itemset and association rule mining 
 * algorithms assumes that items cannot appear more than once in a transaction 
 * and that transactions are sorted.
 * 
 * @author Philippe Fournier-Viger, 2014
 */
public class FixTransactionDatabaseTool {
	
	/**
	 * Fix the transaction database
	 * @param input the input file path (a transaction database in SPMF format)
	 * @param output the output file path (the fixed trnasactoin database in SPMF format)
	 * @throws IOException if an error while reading/writting files.
	 * @throws NumberFormatException 
	 */
	public void convert(String input, String output) throws NumberFormatException, IOException {

		// for stats
		BufferedWriter writer = new BufferedWriter(new FileWriter(output)); 
		BufferedReader myInput = new BufferedReader(new InputStreamReader( new FileInputStream(new File(input))));
		// for each line (transaction) until the end of file
		String thisLine;
		while ((thisLine = myInput.readLine()) != null) {
			// if the line is empty we skip it
			if (thisLine.isEmpty() == true) {
				continue;
			// if the line is some kind of metadata we just write the line as it is
			}else if(thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
							|| thisLine.charAt(0) == '@') {
				writer.write(thisLine + " ");
				writer.newLine();
				continue;
			}
			
			// Otherwise
			// split the transaction according to the white space separator
			String [] split = thisLine.split(" ");
			
			// This will store the current transaction in memory
			// so that we can sort it
			List<Integer> transaction = new ArrayList<Integer>();

			// This is to remember items that we have already seen in the current transaction.
			Set<Integer> alreadySeen = new HashSet<Integer>();
			for(int i=0; i <split.length; i++){
				// if that position is not empty (an extra space) or the value NaN
				if(split[i].isEmpty() == false && "NaN".equals(split[i]) == false){

					// convert item to integer
					Integer item = Integer.parseInt(split[i]);
					
					// if the item is appearing for the first time in the transaction
					// we add the item to the transaction
					if(alreadySeen.contains(item) == false) {
						// we add the item
						transaction.add(item);
						// we remember that we have seen this item
						alreadySeen.add(item);
					}
				}
			}
			
			// Sort the transaction
			Collections.sort(transaction);
			
			// Then write the transaction to the file
			for(int i = 0; i < transaction.size(); i++) {
				Integer item = transaction.get(i);
				writer.write(String.valueOf(item));
				if(i != transaction.size()-1) {
					writer.write(" ");
				}
			}
			// write a new line
			writer.newLine();
			
		}
		myInput.close();
		
		writer.close();
	}

}

	