package ca.pfv.spmf.algorithms.sequentialpatterns.prefixspan;
/* Copyright (c) 2008-2013 Philippe Fournier-Viger
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
* 
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;



/**
 * Implementation of a sequence database, where each sequence is implemented
 * as a list of integers and should have a unique id.
*
* @see Sequence
 * @author Philipe-Fournier-Viger
 */
public class SequenceDatabase {

	/** a matrix to store the sequences in this database */
	List<int[]> sequences = new ArrayList<int[]>();

	/** the total number of item occurrences in this database
	 * (variable to be used for statistics) */
	long itemOccurrenceCount = 0;
	
	/**
	 * Method to load a sequence database from a text file in SPMF format.
	 * @param path  the input file path.
	 * @throws IOException exception if error while reading the file.
	 */
	public void loadFile(String path) throws IOException {
		// initialize the variable to calculate the total number of item occurrence
		itemOccurrenceCount = 0;
		// initalize the list of arrays for storing sequences
		sequences = new ArrayList<int[]>();
		
		String thisLine; // variable to read each line.
		BufferedReader myInput = null;
		try {
			FileInputStream fin = new FileInputStream(new File(path));
			myInput = new BufferedReader(new InputStreamReader(fin));
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is not a comment, is not empty or is not other
				// kind of metadata
				if (thisLine.isEmpty() == false &&
						thisLine.charAt(0) != '#' && thisLine.charAt(0) != '%'
						&& thisLine.charAt(0) != '@') {
					
					// split this line according to spaces and process the line
					String[] tokens = thisLine.split(" ");
					
					// we will store the sequence as a list of integers in memory
					int[] sequence = new int[tokens.length];
					// we convert each token from the line to an integer and add it
					// to the array representing the current sequence.
					for(int j=0; j < tokens.length; j++){
						sequence[j] = Integer.parseInt(tokens[j]);
					}
					
					// add the sequence to the list of sequences
					sequences.add(sequence);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}
	}
		
	/**
	 * Print this sequence database to System.out.
	 */
	public void print() {
		System.out.println("============  SEQUENCE DATABASE ==========");
		System.out.println(toString());
	}
	
	/**
	 * Print statistics about this database.
	 */
	public void printDatabaseStats() {
		System.out.println("============  STATS ==========");
		System.out.println("Number of sequences : " + sequences.size());
		
		// Calculate the average size of sequences in this database
		double meansize = ((float)itemOccurrenceCount) / ((float)sequences.size());
		System.out.println("mean size" + meansize);
	}

	/**
	 * Return a string representation of this sequence database.
	 */
	public String toString() {
		StringBuilder buffer = new StringBuilder();

		// for each sequence
		for (int i=0; i < sequences.size(); i++) { 
			buffer.append(i + ":  ");
			
			// get that sequence
			int[] sequence = sequences.get(i);
			
			// for each token in that sequence (items, or separators between items)
			// we will print it in a human-readable way
			
			boolean startingANewItemset = true;
			for(int token : sequence){
				// if it is an item
				if(token >=0){
					// if this is a new itemset, we start with a parenthesis
					if(startingANewItemset == true){
						startingANewItemset = false;
						buffer.append("(");
					}else{
						// otherwise we print a space
						buffer.append(" ");
					}
					// then we print the item
					buffer.append(token);
					
					// increase the number of item occurrences for statistics
					itemOccurrenceCount++;
				}else if(token == -1){
					// if it is an itemset separator
					buffer.append(")");
					// remember that we have just finished reading a full itemset
					startingANewItemset = true;
				}else if(token == -2){
					// if it is the end of the sequence we break, in case there
					// would be something stored after in the array.
					break;
				}
			}
		
			// print each item print eac
			buffer.append(System.lineSeparator());
		}
		return buffer.toString();
	}
	
	/**
	 * Get the sequence count in this database.
	 * @return the sequence count.
	 */
	public int size() {
		return sequences.size();
	}
	
	/**
	 * Get the sequences from this sequence database.
	 * @return A list of sequences (int[]) in SPMF format.
	 */
	public List<int[]> getSequences() {
		return sequences;
	}

}
