package ca.pfv.spmf.algorithms.frequentpatterns.estDec;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This is an implementation of the estDecPlus algorithm 
 * <br/>
 * 
 * Se Jung Shin , Dae Su Lee , Won Suk Lee, “CP-tree: An adaptive synopsis structure 
 * for compressing frequent itemsets over online data streams”, 
 * Information Sciences,Volume 278, 10 September 2014, Pages 559–576<br/>
 * 
 * This implementation was made by Azadeh Soltani <br/>
 * <br/>
 * 
 * Copyright (c) 2008-2012 Azadeh Soltani, Philippe Fournier-Viger <br/>
 * <br/>
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf). <br/>
 * <br/>
 * 
 * SPMF is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version. <br/>
 * <br/>
 * 
 * SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details. <br/>
 * <br/>
 * 
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @see CPTreeNode
 * @see CPTree
 * @author Azadeh Soltani
 */
public class Algo_estDecPlus {

	// the Compressible Prefix tree
	CPTree tree; 
	
	// the number of transactions
	int transactionCount = 0; 

	// the total time for mining (for stats)
	private long miningTime = 0;
	
	// the total time for transaction insertion (for stats)
	double sumTransactionInsertionTime = 0; 
	
	/**
	 * Constructor
	 * 
	 * @param mins the minimum support threshold as a value in [0,1] representing a percentage
	 * @param d the decay parameter
	 * @param minSigValue the minSig parameter
	 * @param deltaValue the delta value
	 * @param minMergeValue the minMerge parameter
	 */
	public Algo_estDecPlus(double mins, double d, double minSigValue,
			double deltaValue, double minMergeValue) {
		// Reset memory logger
		MemoryLogger.getInstance().reset();

		// create the "Monitoring Lattice" tree
		tree = new CPTree(d, mins, minSigValue, deltaValue, minMergeValue);
		System.out.println(" Parameters  : minsup: " + mins + " minSig:  "+minSigValue+ " minmerge:  "+minMergeValue+ " delta:  "+deltaValue);
		
	}
	
	/**
	 * A method to set the decay rate "d" using the "b" and "h" parameter (see the
	 * EstDec article)
	 * @param b  decay base 
	 * @param h decay-base life
	 */
	public void setDecayRate(double b, double h) {
		tree.setDecayRate(b,h);
	}

	/**
	 * Run the algorithm by loading the transactions from an input file.
	 * 
	 * @param input   the input file path
	 * @param output  the output file path for saving the result
	 * @param mins    the minsup threshold as a double value in [0, 1]
	 * @throws FileNotFoundException  if error opening the input file
	 * @throws IOException if error reading/writing files
	 */
	public void processTransactionFromFile(String input)
			throws FileNotFoundException, IOException {

		// read the input file
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
//		int n=0;

		// for each line (transaction)
		while (((line = reader.readLine()) != null)) {
			// split the line into an array of strings (items)
			String[] lineSplited = line.split(" ");
			
			// convert the array of strings to an array of integers (items)
			int[] transaction = new int[lineSplited.length];
			for (int i = 0; i < lineSplited.length; i++) {
				transaction[i] = Integer.parseInt(lineSplited[i]);
			} 
			//sort the transaction
//			Arrays.sort(transaction);
			// process the transaction
//			if (transaction.length<40)
			  processTransaction(transaction);
//			else
//				n++;
		

		}// while
//		System.out.println("Transaction read from file:" + n);
		reader.close();
	}

	/**
	 * Run the algorithm by loading the transactions from an input file.
	 * 
	 * @param input   the input file path
	 * @param output  the output file path for saving the result
	 * @param mins    the minsup threshold as a double value in [0, 1]
	 * @param lineCount the number of lines to be read
	 * @throws FileNotFoundException  if error opening the input file
	 * @throws IOException if error reading/writing files
	 */
	public void processTransactionFromFile(String input, int numberOfLines)
			throws FileNotFoundException, IOException {

		// read the input file
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		int n=0;

		// for each line (transaction)
		while (((line = reader.readLine()) != null) && n < numberOfLines) {
			// split the line into an array of strings (items)
			String[] lineSplited = line.split(" ");
			
			// convert the array of strings to an array of integers (items)
			int[] transaction = new int[lineSplited.length];
			for (int i = 0; i < lineSplited.length; i++) {
				transaction[i] = Integer.parseInt(lineSplited[i]);
			} 
			//sort the transaction
//			Arrays.sort(transaction);
			// process the transaction
			processTransaction(transaction);
			
			n++;
		}// while
		System.out.println(n);
		reader.close();
	}

	/**
	 * Mine recent frequent itemsets from the current tree and save the result
	 * to a file.
	 * 
	 * @throws IOException if error writting to output file
	 * @param outputPath the output file path
	 */
	public void performMining_saveResultToFile(String outputPath)
			throws IOException {
		// Record start time
		long startMiningTimeStamp = System.currentTimeMillis();

		// Perform mining
		tree.patternMining_saveToFile(outputPath);

		// Record memory usage and end time
		System.gc();
		MemoryLogger.getInstance().checkMemory();
		miningTime = System.currentTimeMillis() - startMiningTimeStamp;
	}

	/**
	 * Mine recent frequent itemsets from the current tree and save the result
	 * to memory
	 * 
	 * @throws IOException if error when writting to output file
	 * @param outputPath the output file path
	 * @return
	 */
	public Hashtable<int[], Double> performMining_saveResultToMemory()
			throws IOException {
		// Check memory usage
		System.gc();
		MemoryLogger.getInstance().checkMemory();
		// Record start time
		long startMiningTimeStamp = System.currentTimeMillis();
		
		// Perform mining
		Hashtable<int[], Double> patterns = tree.patternMining_saveToMemory();
		
		// Record end time 
		miningTime = System.currentTimeMillis() - startMiningTimeStamp;
		
		// Record memory usage
		System.gc();
		MemoryLogger.getInstance().checkMemory();

		return patterns;
	}

	/**
	 * Process a transaction (add it to the tree and update itemsets
	 * 
	 * @param transaction an ArrayList of integers
	 */
	public void processTransaction(int[] transaction) {
		// record st
		double startCTimestamp = System.currentTimeMillis();

		// phase 1) Parameter updating
		tree.updateParams();

		// phase 2) Node restructuring
		for (int i = 0; i < tree.root.children.size(); ++i)
			tree.traverse(tree.root.children.get(i), tree.root, (short) -1,	transaction);

		// phase 3) Itemset Insertion
		tree.insertItemset(transaction);

		transactionCount++;
		if (transactionCount % 1000 == 0)
			tree.forcePruning(tree.root);
		
		tree.forcePruning(tree.root);
		sumTransactionInsertionTime += (System.currentTimeMillis() - startCTimestamp);
	}


	/**
	 * Method to print the CP-tree to the console for debugging purposes.
	 */
	public void showTreeForDebugging() {
		System.out.println(tree.toString());
	}
	
	
	/**
	 * Print statistics about the algorithm execution to the console.
	 */
	public void printStats() {
		System.out.println("===========  estDecPlus - STATS ===========");
		System.out.println(" Number of nodes : " + tree.nodeCount(tree.root));
		System.out.println(" Frequent itemsets count : " + tree.patternCount);
		System.out.println(" Maximum memory usage : " + MemoryLogger.getInstance().getMaxMemory() + " mb");
		System.out.println(" Number of transactions: " + transactionCount);
		System.out.println(" Total insertion time ~ " + sumTransactionInsertionTime);
		System.out.println(" Insertion time per transaction ~ " + sumTransactionInsertionTime
				/ (double)transactionCount + " ms");
		System.out.println(" Mining time ~ " + miningTime + " ms");
		System.out.println("============================================");
	}
}
