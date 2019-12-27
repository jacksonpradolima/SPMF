package ca.pfv.spmf.algorithms.sequentialpatterns.prosecco;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import ca.pfv.spmf.algorithms.sequentialpatterns.prefixspan.SequenceDatabase;

/**
 * This class is a modified version of the SequenceDatabase class found in PrefixSpan. It reads a dataset in blocks
 * of user specified size and computes error bounds for use in the ProSecCo algorithm.
 * 
 * Copyright (c) 2008-2019 Philippe Fournier-Viger and Sacha Servan-Schreiber
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 *
 * SPMF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SPMF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SPMF.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Interface is used for obtaining each progessive block as the dataset is read
 * */
interface ProgressiveSequenceDatabaseCallbacks {
   
	/** called when the next block is read */
    public void nextSequenceBlock(List<int[]> block, String outputFilePath, boolean isLast);
}


public class ProgressiveSequenceDatabase extends SequenceDatabase {

	// metadata to keep track of sequences seen so far in the dataset
	private Metadata metadata;
	
	
	public ProgressiveSequenceDatabase() {
		
	}
	
	/**
	 * Method to load a sequence database from a text file in SPMF format.
	 * @param path  the input file path.
	 * @throws IOException exception if error while reading the file.
	 */
	public void loadFile(String inputPath, 
			String outputPath, 
			int blockSize, 
			int dbSize,
			double errorTolerance, 
			double startErrorThreshold,
			ProgressiveSequenceDatabaseCallbacks callback) throws IOException {
		
		// initialize metadata object with errorTolerance, blocksize, and db size
		metadata = new Metadata(errorTolerance, blockSize, dbSize);
		
		// initialize the variable to calculate the total number of item occurrence
		itemOccurrenceCount = 0;
		
		// initalize the list of arrays for storing sequences
		sequences = new ArrayList<int[]>();
		
		String thisLine; // variable to read each line.
		BufferedReader myInput = null;
		try {
			FileInputStream fin = new FileInputStream(new File(inputPath));
			myInput = new BufferedReader(new InputStreamReader(fin));
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is not a comment, is not empty or is not other
				// kind of metadata
				if (thisLine.isEmpty() == false &&
						thisLine.charAt(0) != '#' && 
						thisLine.charAt(0) != '%' && 
						thisLine.charAt(0) != '@') {
					
					// split this line according to spaces and process the line
					String[] tokens = thisLine.split(" ");
					
					int numItems = 0;
					
					// we will store the sequence as a list of integers in memory
					int[] sequence = new int[tokens.length];
					
					// we convert each token from the line to an integer and add it
					// to the array representing the current sequence.
					for(int j=0; j < tokens.length; j++){
						sequence[j] = Integer.parseInt(tokens[j]);
						if (sequence[j] >= 0) 
							numItems++;	
					}
					
					metadata.UpdateWithSequence(sequence, numItems);
					
					// add the sequence to the list of sequences
					sequences.add(sequence);
					
					if (sequences.size() % blockSize == 0) {
						//System.out.println(metadata.GetError());
						
						if (metadata.GetError() < startErrorThreshold) {
							List<int[]> block = new ArrayList<int[]>(sequences);
							callback.nextSequenceBlock(block, outputPath, false);
							sequences.clear();
						}
					}
				}
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				callback.nextSequenceBlock(sequences, outputPath, true);
				myInput.close();
			}
		}
	}
	
	/** Get the current error bound
	 * @return current error, see http://www.riondabsd.net/papers/ServanSchreiberEtAl-ProSecCo-ICDM.pdf
	 */
	public double getError() {
		return metadata.GetError();
	}
	
	/**
	 *  Get the number of sequences (transactions) processed
	 * @return number of transactions processed so far
	 */
	public int numSequencesProcessed() {
		return metadata.getNumSequencesProcessed();
	}
	
		
}
