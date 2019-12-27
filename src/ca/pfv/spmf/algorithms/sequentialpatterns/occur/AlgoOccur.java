package ca.pfv.spmf.algorithms.sequentialpatterns.occur;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ca.pfv.spmf.algorithms.sequentialpatterns.prefixspan.SequenceDatabase;
import ca.pfv.spmf.patterns.itemset_list_integers_without_support.Itemset;
import ca.pfv.spmf.tools.MemoryLogger;


/*** 
 * This is a 2016 implementation of the PrefixSpan algorithm.
 * PrefixSpan was proposed by Pei et al. 2001.
 * 
 * NOTE: This implementation saves the pattern  to a file as soon 
 * as they are found or can keep the pattern into memory, depending
 * on what the user choose.
 * 
 * This implementation was done in 2016. It is different than the previous implementation
 * in SPMF which was implemented in 2008, and the AGP implementation. I have re-implemented the code to make
 * it more efficient. This new implementation can be 10 times faster than the 2008 implementation, since
 * I have added more optimizations
 *
 * Copyright (c) 2008-2012 Philippe Fournier-Viger
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

public class AlgoOccur{
		
	/** for statistics **/
	long startTime;
	long endTime;

	/** writer to write output file */
	BufferedWriter writer = null;

	/** the sequence database **/
	SequenceDatabase sequenceDatabase;
	
	/**
	 * Default constructor
	 */
	public AlgoOccur(){
	}
	
	/**
	 * Run the algorithm
	 * @param databaseFile : a sequence database
	 * @param patternFile : a file of sequential patterns
	 * @param outputFilePath : the path of the output file to save the result
	 *                         or null if you want the result to be saved into memory
	 * @throws IOException  exception if error while writing the file
	 */
	public void runAlgorithm(String inputFile, String patternFile, String outputFilePath) throws IOException {
		// record start time
		startTime = System.currentTimeMillis();
		
		// Load the sequence database
		sequenceDatabase = new SequenceDatabase(); 
		sequenceDatabase.loadFile(inputFile);
		
		// if the user want to keep the result into memory
		if(outputFilePath == null){
			writer = null;
		}else{ // if the user want to save the result to a file
			writer = new BufferedWriter(new FileWriter(outputFilePath)); 
		}
		
		processPatterns(patternFile);

		// record end time
		endTime = System.currentTimeMillis();
		// close the output file if the result was saved to a file
		if(writer != null){
			writer.close();
		}
	}
	

	/**
	 * Process all patterns of a file
	 * @param patternFile input file
	 * @throws IOException 
	 */
	private void processPatterns(String patternFile) throws IOException {
		String thisLine; // variable to read each line.
		BufferedReader myInput = null;

		FileInputStream fin = new FileInputStream(new File(patternFile));
		myInput = new BufferedReader(new InputStreamReader(fin));
		while ((thisLine = myInput.readLine()) != null) {
			// if the line is not a comment, is not empty or is not other
			// kind of metadata
			if (thisLine.isEmpty() == false &&
					thisLine.charAt(0) != '#' && thisLine.charAt(0) != '%'
					&& thisLine.charAt(0) != '@') {
				
				int posFirstCharacterSUP = thisLine.indexOf('#');
				String sequenceText = thisLine.substring(0, posFirstCharacterSUP-1);

//				// THE SIDS AS TEXT
				int posFirstCharacterSIDLine = thisLine.indexOf("#SID");
				String sidListString = thisLine.substring(posFirstCharacterSIDLine+6);


				String supText = thisLine.substring(posFirstCharacterSUP, posFirstCharacterSIDLine-1);
				
				// THE SIDS as an Integer array
				String []sidsSplit = sidListString.split(" ");
				int[] sids = new int[sidsSplit.length];
				for(int i = 0; i < sidsSplit.length; i++){
					sids[i] = Integer.parseInt(sidsSplit[i]);
				}
				
				// The sequential pattern as integers
				String []itemSplit = sequenceText.split(" ");
				int[] pattern = new int[itemSplit.length];
				for(int i = 0; i < itemSplit.length; i++){
					pattern[i] = Integer.parseInt(itemSplit[i]);
				}
				
				/// Write the pattern
				writer.append(sequenceText);
				writer.append(' ');
				writer.append(supText);
				writer.append(" #SIDOCC:");
				
				findOccurrences(sids, pattern);

				writer.newLine();
			}
		}
		myInput.close();
	}

	/**
	 * Recursive function to find occurrences
	 * @param sequenceDatabase2 
	 * @param sids the list of sids
	 * @param pattern the pattern
	 * @param occurrences the occurrences that have been found
	 * @param posPattern
	 * @param posSequence
	 * @throws IOException 
	 */
	private void findOccurrences(int[] sids, int[] pattern) throws IOException {
		// for each sequence
		for(int sid : sids){

			List<String> occurrences = new ArrayList<String>();
			int[] sequence = sequenceDatabase.getSequences().get(sid);
			
			// try to match the pattern with that sequence
			findOccurrencesHelper(pattern, sequence, 0, 0, "", 0, occurrences);		
			
//			writer.append(" sid:");
			writer.append(" "+sid);
			for(int i = 0; i < occurrences.size(); i++){
				writer.append('[');
				writer.append(occurrences.get(i));
				writer.append(']');
				if(i != occurrences.size() -1){
					writer.append(' ');
				}
			}
		}
	}

	/**
	 * Try to match an itemset of a sequence with a pattern
	 * @param pattern
	 * @param sequence
	 * @param posPattern
	 * @param posSequence
	 * @param occurrence
	 * @param posItemsetSequence
	 * @param listOccurrences 
	 */
	private void findOccurrencesHelper(int[] pattern, int[] sequence,
			int posPattern, int posSequence, String occurrence, 
			int posItemsetSequence, List<String> listOccurrences) {
		
		int patternResetPosition = posPattern;
		
		do{
			if(pattern[posPattern] == sequence[posSequence]){
				// if we matched the whole itemset
				if(pattern[posPattern] == -1){
					String newOccurrence;
					if(occurrence.length() == 0){
						newOccurrence = occurrence + posItemsetSequence;
					}else{
						newOccurrence = occurrence + " " + posItemsetSequence;
					}
					
					// if it is the last item of the pattern
					if(posPattern == pattern.length-1){
						listOccurrences.add(newOccurrence);
						
					}else{
						// if it is not the last item of the pattern, we need to check the 
						// next itemset
						findOccurrencesHelper(pattern, sequence, posPattern+1, posSequence+1, 
								newOccurrence, posItemsetSequence+1, listOccurrences);
					}
					
					posItemsetSequence++;
					posPattern = patternResetPosition;
					
				}else{
					//if not
					posPattern++;
				}
			}else if(sequence[posSequence] == -1){
				// end of sequence itemset but did not match
				// so we need to start again to match that pattern from the previous itemset.
				posPattern = patternResetPosition;
				posItemsetSequence++;
			}
			posSequence++;

		}while(posSequence < sequence.length);
	}

	/**
	 * Print statistics about the algorithm execution to System.out.
	 * @param size  the size of the database
	 */
	public void printStatistics() {
		StringBuilder r = new StringBuilder(200);
		r.append("=============  Occur 2.37 - STATISTICS =============\n Total time ~ ");
		r.append(endTime - startTime);
		r.append(" ms");
		r.append(System.lineSeparator());
		r.append(" Max memory (mb) : ");
		r.append(MemoryLogger.getInstance().getMaxMemory());
		r.append(System.lineSeparator());
		r.append("===================================================");
		r.append(System.lineSeparator());
		System.out.println(r.toString());
	}


}
