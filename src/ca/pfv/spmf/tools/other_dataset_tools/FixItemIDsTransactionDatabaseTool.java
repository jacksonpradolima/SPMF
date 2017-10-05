package ca.pfv.spmf.tools.other_dataset_tools;

/* This file is copyright (c) 2008-2017 Philippe Fournier-Viger
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
 * This tool increment the id of items in a transaction database by a given number (e.g. 1).
 * This is useful for example, when a database contain the item "0". 
 * The item "0" is not allowed by some algorithm. So using this tool we can fix that.
 * 
 * @author Philippe Fournier-Viger, 2017
 */
public class FixItemIDsTransactionDatabaseTool {
//	3 5 1 2 4 6:30:1 3 5 10 6 5
//	3 5 2 4:20:3 3 8 6
//	3 1 4:8:1 5 2
//	3 5 1 7:27:6 6 10 5
//	3 5 2 7:11:2 3 4 2
	/**
	 * Fix the transaction database
	 * @param input the input file path (a transaction database in SPMF format)
	 * @param output the output file path (the fixed transaction database in SPMF format)
	 * @param increment the value for increasing the id of an item
	 * @throws IOException if an error while reading/writing files.
	 * @throws NumberFormatException 
	 */
	public void convert(String input, String output, int increment) throws NumberFormatException, IOException {

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
			
			String lineAfterPoint = null;
			// If this line contains ":"
			Integer positionPoint = thisLine.indexOf(':');
			if(positionPoint >=0){
				// We split the line into two parts : before and after the ":"
				lineAfterPoint = thisLine.substring(positionPoint, thisLine.length());
				thisLine = thisLine.substring(0, positionPoint);
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

					String itemString = split[i];
	
					// convert item to integer
					Integer item = Integer.parseInt(itemString) + increment;
					
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
			if(lineAfterPoint != null){
				writer.write(lineAfterPoint);
			}
			
			// write a new line
			writer.newLine();
			
		}
		myInput.close();
		
		writer.close();
	}

}

	