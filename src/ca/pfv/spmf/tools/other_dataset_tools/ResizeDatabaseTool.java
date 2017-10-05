package ca.pfv.spmf.tools.other_dataset_tools;

/* This file is copyright (c) 2008-2016 Philippe Fournier-Viger
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

/**
 * This tool is designed to extract X % of a database to resize it.
 * For example, if a database contains 100,000 transactions and this tool is used
 * with a percentage of 75 %, the result will be a database containing the  75,000 first
 * transactions from the original database.
 * This class is designed to work with any database file in SPMF format (text file)
 * @author Philippe Fournier-Viger, 2016
 */
public class ResizeDatabaseTool {
	
	/**
	 * Resize a database to X % of its size
	 * @param input the input file path (a transaction database with utility values in SPMF format)
	 * @param output the output file path
	 * @param percentage the percentage of the size that the original database that the output database should have
	 * @throws IOException if an error while reading/writing files.
	 * @throws NumberFormatException if an error while reading the file
	 */
	public void convert(String input, String output, double percentage) throws NumberFormatException, IOException {


		// First count the number of lines of data in the file
		double lineOfDataCount = 0;
		BufferedWriter writer = new BufferedWriter(new FileWriter(output)); 
		BufferedReader myInput = new BufferedReader(new InputStreamReader( new FileInputStream(new File(input))));
		String thisLine;
		myInput = new BufferedReader(new InputStreamReader( new FileInputStream(new File(input))));
		// for each line (transaction) until the end of file
		while ((thisLine = myInput.readLine()) != null) {
			// if the line is  a comment, is  empty or is a
			// kind of metadata
			if (thisLine.isEmpty() == true ||
					thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
							|| thisLine.charAt(0) == '@') {
				continue;
			}
			
			lineOfDataCount++;
		}
		writer.close();
		
		int newDatabaseSize = (int) Math.ceil(percentage *  lineOfDataCount);
		System.out.println("Number of lines of data in original database: " + lineOfDataCount);
		System.out.println("Number of lines of data in resized database: " + newDatabaseSize);
		
		
		// Then read the file again, to write the output file
		int lineOfDataWritten = 0;
		writer = new BufferedWriter(new FileWriter(output)); 
		myInput = new BufferedReader(new InputStreamReader( new FileInputStream(new File(input))));
		myInput = new BufferedReader(new InputStreamReader( new FileInputStream(new File(input))));
		// for each line (transaction) until the end of file
		while ((thisLine = myInput.readLine()) != null) {
			// if the line is  a comment, is  empty or is a
			// kind of metadata
			if (thisLine.isEmpty() == true ||
					thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
							|| thisLine.charAt(0) == '@') {
				// write the line of data
				writer.write(thisLine);
				writer.newLine();
				continue;
			}else{
				lineOfDataWritten++;
				if(lineOfDataWritten > newDatabaseSize){
					break;
				}
			
				// write the line of data
				writer.write(thisLine);
				if(lineOfDataWritten != newDatabaseSize){
					writer.newLine();
				}
			}
		}
		writer.close();
		
		
	}

}

	