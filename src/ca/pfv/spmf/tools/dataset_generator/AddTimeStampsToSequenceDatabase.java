package ca.pfv.spmf.tools.dataset_generator;

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
import java.io.FileWriter;
import java.io.IOException;

import ca.pfv.spmf.input.sequence_database_array_integers.Sequence;
import ca.pfv.spmf.input.sequence_database_array_integers.SequenceDatabase;

/**
* This class is to read a sequence database in SPMF format, add consecutive timestamps
* to itemsets in each sequence, and then write the sequence database to a file.
* 
* @author Philippe Fournier-Viger
*/
public class AddTimeStampsToSequenceDatabase {

	
	/**
	 * Read a sequence database in SPMF format, add timestamps and then 
	 * write the sequence database to a file.
	 * Note that this code could be further optimized if performance is an issue 
	 * (instead of loading the file into memory, it could be loaded line by line).
	 * @param inputFile the path of a sequence database in SPMF format
	 * @param outputFile the output path for writing the database with timestamps
	 * @throws IOException  if error while reading/writing file
	 */
	public void convert(String inputFile, String outputFile) throws IOException {
		// we create an object fro writing the output file
		BufferedWriter	writer = new BufferedWriter(new FileWriter(outputFile)); 
		
		SequenceDatabase database = new SequenceDatabase();
		database.loadFile(inputFile);
		
		BufferedReader myInput = null;
		try {
			for(int i=0; i < database.getSequences().size(); i++) {
				Sequence sequence = database.getSequences().get(i);

				// for each itemset in this sequence
				for(int j = 0; j< sequence.getItemsets().size(); j++) {
					// write timestamp
					writer.write("<" + j + "> ");
					
					Integer[] itemset = sequence.get(j);
					for(Integer item : itemset) {
						writer.write(item + " ");
					}
					writer.write("-1 ");
				}
				writer.write(" -2");
				// start a new line
				writer.newLine();
			}
			// close the output file
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}
	}
}