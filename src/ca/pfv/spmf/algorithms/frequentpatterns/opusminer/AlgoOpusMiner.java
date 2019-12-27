package ca.pfv.spmf.algorithms.frequentpatterns.opusminer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.PriorityQueue;

import ca.pfv.spmf.tools.MemoryLogger;

/**
 * An implementation of the Opus-Miner algorithm proposed in : </br>
 * </br>
 * 
 * Webb, G.I. & Vreeken, J. (2014) Efficient Discovery of the Most Interesting
 * Associations. ACM Transactions on Knowledge Discovery from Data. 8(3), Art.
 * no. 15.
 *
 * The code was translated from C++ to Java. The modifications to the original
 * C++ code to obtain Java code and improvements are copyright by Xiang Li and
 * Philippe Fournier-Viger, while the original C++ code is copyright by Geoff
 * Web and obtained under the GPL 3 license.
 * 
 * The code is under the GNU General Public Licence v3 license.
 * 
 * ========================================= This is the original header of the
 * C++ code:
 * 
 * OPUS Miner: Filtered Top-k Association Discovery of Self-Sufficient Itemsets
 * Version 1.2.1 Copyright (C) 2012-2016 Geoffrey I WebbThis program comes with
 * ABSOLUTELY NO WARRANTY.
 * 
 * This is free software, and you are welcome to redistribute it under certain
 * conditions. See the GNU General Public Licence <http://www.gnu.org/licenses/>
 * for details.
 * ===============================================================================
 * 
 * @author code translated to Java by Xiang Li and P. Fournier-Viger, 2018
 */
public class AlgoOpusMiner {

	/** the time the algorithm started */
	long startTimestamp = 0;
	/** the time the algorithm terminated */
	long endTimestamp = 0;

	/** writer to write the output file **/
	BufferedWriter writer = null;

	static PriorityQueue<itemsetRec> itemsets = new PriorityQueue<itemsetRec>();

	/** Number of ron-redundant productive itemsets found */
	private int nonRedundantProductiveItemsetsCount;

	/** DEBUG mode */
	boolean DEBUG = false;

	/**
	 * Search for top-k non redundant productive itemsets
	 * 
	 * @param inputFileName             input file name
	 * @param outputFileName            output file name
	 * @param printClosure
	 * @param filter
	 * @param k
	 * @param searchByLift
	 * @param correctionForMultiCompare
	 * @param redundancyTests
	 * @param isCSVInputFile            true if the file is in CSV format
	 * @throws IOException if some error occurs when reading or writing to file
	 */
	public void runAlgorithm(String inputFileName, String outputFileName, boolean printClosure, boolean filter, int k,
			boolean searchByLift, boolean correctionForMultiCompare, boolean redundancyTests, boolean isCSVInputFile)
			throws IOException {

		// Save global variables
		Global.correctionForMultCompare = correctionForMultiCompare;
		Global.printClosures = printClosure;
		Global.filter = filter;
		Global.k = k;
		Global.searchByLift = searchByLift;
		Global.redundancyTests = redundancyTests;
		
		// reset the list of itemsets
		itemsets.clear();

		ArrayList<itemsetRec> is = new ArrayList<itemsetRec>();

		startTimestamp = System.currentTimeMillis();
		MemoryLogger.getInstance().checkMemory();

		System.out.printf("Loading data from %s\n", inputFileName);
		if (isCSVInputFile) {
			LoadData.loadCSVdata(inputFileName);
		} else {
			LoadData.load_data(inputFileName);
		}

		System.out.printf("%d transactions, %d items\n", Global.noOfTransactions, Global.noOfItems);

		// create a writer object to write results to file
		writer = new BufferedWriter(new FileWriter(outputFileName));

//		writer.write(inputFileName + ": " + Global.noOfItems 
//				 + " items, " + Global.noOfTransactions + " transactions");
//		writer.newLine();

		System.out.print("Finding itemsets\n");

		FindItemsets.find_itemsets();

		// extract the itemsets from the priority queue
		while (!itemsets.isEmpty()) {
			is.add(itemsets.peek());
			itemsets.poll();
		}

		if (filter) {
			System.out.print("Filtering itemsets\n");
			FilterItemsets.filter_itemsets(is);
		}

		nonRedundantProductiveItemsetsCount = is.size();

		System.out.print("Printing itemsets\n");
		PrintItemsets.print_itemsets(writer, is, isCSVInputFile, searchByLift);
		MemoryLogger.getInstance().checkMemory();

		if (DEBUG) {
			writer.newLine();
			writer.write("ITEM NAMES\n");
			for (int i = 0; i < Global.itemNames.size(); i++) {
				writer.write("" + i + " -> " + Global.itemNames.get(i) + "\n");
			}
		}

		writer.close();

		MemoryLogger.getInstance().checkMemory();
		endTimestamp = System.currentTimeMillis();
	}

	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats() {

		String nonRedundant = Global.redundancyTests ? "Non-redundant" : "";
		String independently = Global.filter ? " Independantly" : "";
		System.out.println("=============  Opus-Miner algorithm v2.40 - STATS =======");
		System.out.println(" " + nonRedundant + independently + " productive itemset count: " + nonRedundantProductiveItemsetsCount);
		for (int i = 2; i < Global.alpha.size(); i++) {
			System.out.println("  Alpha for size " + i + " " + Global.alpha.get(i));
		}
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println(" Transaction count: " + Global.noOfTransactions + " Item count: " + Global.noOfItems);

		System.out.println("===========================================================");
	}

}