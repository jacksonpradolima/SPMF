package ca.pfv.spmf.algorithms.sequentialpatterns.prosecco;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.pfv.spmf.algorithms.sequentialpatterns.prefixspan.SequentialPattern;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixspan.SequentialPatterns;
import ca.pfv.spmf.patterns.itemset_list_integers_without_support.Itemset;
import ca.pfv.spmf.tools.MemoryLogger;

/** * * * This is an implementation of the ProSecCo algorithm.
 *
 * Copyright (c) 2019 Sacha Servan-Schreiber
 *
 * This file is part of the SPMF DATA MINING SOFTWARE * (http://www.philippe-fournier-viger.com/spmf).
 *
 *
 * SPMF is free software: you can redistribute it and/or modify it under the * terms of the GNU General Public License as published by the Free Software * Foundation, either version 3 of the License, or (at your option) any later * version. *

 * SPMF is distributed in the hope that it will be useful, but WITHOUT ANY * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR * A PARTICULAR PURPOSE. See the GNU General Public License for more details. *
 *
 * You should have received a copy of the GNU General Public License along with * SPMF. If not, see .
 *
 * @author Sacha Servan-Schreiber
 */ 

public class AlgoProsecco implements  ProgressiveSequenceDatabaseCallbacks{

	/** for statistics **/
	protected long startTime;
	protected long noCountTime;
	protected long endTime;
	long prevRuntime;
	long noCountTimeBlock;

	/** absolute minimum support */
	protected int minsuppAbsolute;

	/** The sequential patterns that are found  (if the user want to keep them into memory) */
	protected SequentialPatterns patterns = null;

	/** set of items from the first set of blocks mined */
	protected Map<Integer, List<Integer>> mapSequenceID;
	
	protected String outputFilepath;

	private AlgoGetFS alg = new AlgoGetFS();

	/** original sequence count **/
	protected int sequenceCount = 0;

	/** boolean indicating whether this database contains itemsets with multiple items or not */
	boolean containsItemsetsWithMultipleItems = false;

	ProgressiveSequenceDatabase sequenceDatabase;
	ProseccoCallbacks callback;

	double minsupRelative;
	/** the number of pattern found */
	protected int progressivePatternCount;

	/** The sequential patterns that are found  (if the user want to keep them into memory) */
	protected SequentialPatterns progressivePatterns = null;

	public AlgoProsecco(ProseccoCallbacks callback){
		this.callback = callback;
	}

	public AlgoProsecco() throws IOException{
		
	}

	/**
	 * Run the algorithm
	 * @param inputFile : a sequence database
	 * @param minsupRelative  :  the minimum support as a percentage (e.g. 50%) as a value in [0,1]
	 * @param outputFilePath : the path of the output file to save the result
	 *                         or null if you want the result to be saved into memory
	 * @return return the result, if saved into memory, otherwise null
	 * @throws IOException  exception if error while writing the file
	 */
	public SequentialPatterns runAlgorithm(
			String inputFilePath, 
			String outputFilePath, 
			int blockSize, 
			int dbSize,
			double errorTolerance,
			double minsupRelative) throws IOException {
		// record start time
		startTime = System.currentTimeMillis();
		MemoryLogger.getInstance().reset();
		prevRuntime = startTime;
		this.outputFilepath = outputFilePath;

		// Load the sequence database
		this.minsupRelative = minsupRelative;
		sequenceDatabase = new ProgressiveSequenceDatabase();
		sequenceDatabase.loadFile(
				inputFilePath, 
				outputFilePath, 
				blockSize, 
				dbSize, 
				errorTolerance, 
				minsupRelative/2,
				this);

		return null;
	}

	public void nextSequenceBlock(List<int[]> block, String outputFilePath, boolean isLast) {

		try {
			double epsilon = sequenceDatabase.getError();
			sequenceCount = sequenceDatabase.size();

			// convert to a absolute minimum support
			this.minsuppAbsolute = (int) Math.ceil((minsupRelative - epsilon/2) * sequenceCount);
			if (this.minsuppAbsolute <= 0)
				this.minsuppAbsolute = 1;

			alg.reset();

			if (progressivePatterns != null) 
				alg.setMapSequenceID(mapSequenceID);

			this.patterns = alg.getFS(sequenceDatabase, minsuppAbsolute);

			this.minsuppAbsolute  = (int) Math.ceil((minsupRelative - epsilon) * sequenceDatabase.numSequencesProcessed());
			if (this.minsuppAbsolute <= 0)
				this.minsuppAbsolute = 1;

			if (progressivePatterns == null) {
				mapSequenceID = new HashMap<Integer, List<Integer>>(alg.getMapSequenceID()); 
				progressivePatterns = patterns.copy();
				progressivePatternCount = patterns.getSequenceCount();
				containsItemsetsWithMultipleItems = alg.isContainsItemsetsWithMultipleItems();
				MemoryLogger.getInstance().checkMemory();

			} else {   
				merge();
				countInfrequent();
				MemoryLogger.getInstance().checkMemory();
				prune();
			}


			if (callback != null) {
				long startTime = System.currentTimeMillis();
				callback.blockUpdate(
						progressivePatterns, 
						sequenceDatabase.numSequencesProcessed(), 
						getCurrentBatchRuntime(), 
						getCurrentError()
						);
				long endTime = System.currentTimeMillis();

				// don't count what happens during block update
				noCountTime += endTime - startTime;
				noCountTimeBlock += endTime - startTime;

			} else if (outputFilepath != null) {
				long startTime = System.currentTimeMillis();
				savePatternsToFile(progressivePatterns);
				long endTime = System.currentTimeMillis();

				// don't count what happens during block update
				noCountTime += endTime - startTime;
				noCountTimeBlock += endTime - startTime;
			}

			if (isLast) {
				endTime = System.currentTimeMillis();
			}


		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void merge() {

		int k = 0;
		for(List<SequentialPattern> level : progressivePatterns.getLevels()) {
			for(SequentialPattern pattern : level) {

				pattern.setIsFound(false);

				if (k >= patterns.getLevelCount())
					continue;			

				for (SequentialPattern newPattern : patterns.getLevel(k)) {

					if (newPattern.equals(pattern)) {
						pattern.setIsFound(true);
						pattern.addAdditionalSupport(newPattern.getAbsoluteSupport());

						break;
					}
				}

			}

			k++;    	
		}

	}

	private void countInfrequent() {

		for(List<SequentialPattern> level : progressivePatterns.getLevels()) {
			for(SequentialPattern pattern : level) {

				if (!pattern.isFound()) {

					for (int [] sequence : sequenceDatabase.getSequences()) {

						if (sequence != null && Utils.isSubsequenceOf(pattern, sequence, containsItemsetsWithMultipleItems)) {
							pattern.addAdditionalSupport(1);
						} 

					}
				} 

			}
		}
	}

	private void prune() {

		for(List<SequentialPattern> level : progressivePatterns.getLevels()) {
			for(int i = level.size()-1; i >= 0; i--) {
				SequentialPattern pattern = level.get(i);


				if (pattern.getAbsoluteSupport() < this.minsuppAbsolute) {
					if (pattern.size() == 1) {
						mapSequenceID.remove(pattern.get(0).get(0));
					}

					level.remove(i);
					progressivePatternCount--;
				} 
			}
		}
	}

	public long getCurrentBatchRuntime() {
		long batchRuntime = System.currentTimeMillis() - noCountTimeBlock - prevRuntime;
		prevRuntime = System.currentTimeMillis();
		noCountTimeBlock = 0;
		return batchRuntime;
	}

	public long getTotalRuntime() {
		return endTime - startTime - noCountTime;
	}

	public double getCurrentError() {
		return sequenceDatabase.getError();
	}


	/**
	 * Save a pattern containing two or more items to the output file (or in memory, depending on what the user prefer)
	 * @param lastBufferPosition the last position in the buffer for this pattern
	 * @param pseudoSequences the list of pseudosequences where this pattern appears.
	 * @param length the pattern length in terms of number of items.
	 * @throws IOException if error when writing to file
	 */
	private void savePatternsToFile(SequentialPatterns patterns) throws IOException {
		
		File file = new File(outputFilepath); 
		file.delete();

		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilepath)); 

		// create a StringBuilder
		StringBuilder r = new StringBuilder();

		// For each element in this pattern
		for (List<SequentialPattern> patternsAtLevel : patterns.getLevels()){
			for (SequentialPattern pattern : patternsAtLevel) {

				for (Itemset it : pattern.getItemsets()) {
					for (Integer item : it.getItems()) {
						// append the element
						r.append(item);
						r.append(" ");
					}

					r.append("-1");
				}

				r.append(" ");

				// append the support
				r.append("#SUP: ");
				r.append(pattern.getAbsoluteSupport());
				r.append("\n");
			}
		}
		
		// write the string to the file
		writer.write(r.toString());
		// start a new line
		writer.newLine();
		
		// close
		writer.close();
	}


	/**
	 * Print statistics about the algorithm execution to System.out.
	 * @param size  the size of the database
	 */
	public void printStatistics() {
		StringBuilder r = new StringBuilder(200);
		r.append("=============  PROSECCO - STATISTICS =============\n Total time ~ ");
		r.append(endTime - startTime - noCountTime);
		r.append(" ms\n");
		r.append(" Frequent sequences count : " + progressivePatternCount);
		r.append('\n');
		r.append(" Max memory (mb) : ");
		r.append(MemoryLogger.getInstance().getMaxMemory());
		r.append('\n');
		r.append(" minsup = " + minsuppAbsolute + " sequences.");
		r.append('\n');
		r.append(" Pattern count : ");
		r.append(progressivePatternCount);
		r.append('\n');
		r.append("===================================================\n");
		// if the result was save into memory, print it
		if(progressivePatterns !=null){
			progressivePatterns.printFrequentPatterns(sequenceDatabase.numSequencesProcessed(), false);
		}
		System.out.println(r.toString());
	}
}
