package ca.pfv.spmf.algorithms.frequentpatterns.hmine;

/* This file is copyright (c) 2008-2015 Philippe Fournier-Viger
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ca.pfv.spmf.algorithms.ItemNameConverter;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * An implementation of the HMine algorithm for mining frequent itemsets from a
 * transaction database.<br/><br/>
 * 
 * It is based on the description in:<br/><br/>
 * 
 * Pei et al. (2007) H-Mine: Fast and space-preserving frequent pattern mining
 * in large databases. IIE Transactions, 39, 593-605.<br/><br/>
 * 
 * @see supportList
 * @see Element
 * @author Philippe Fournier-Viger, 2015
 */
public class AlgoHMine {
 
	/** the time the algorithm started */
	long startTimestamp = 0; 
	/** the time the algorithm terminated */
	long endTimestamp = 0;  
	/** the number of patterns generated */
	int patternCount =0;  
	
	/** writer to write the output file **/
	BufferedWriter writer = null;  
	
	/** buffer for storing the current itemset that is mined when performing mining
	* the idea is to always reuse the same buffer to reduce memory usage. **/
	final int BUFFERS_SIZE = 200;
	private int[] itemsetBuffer = null;
	
	/** if true, debugging information will be shown in the console */
	boolean DEBUG = false;
	
	/** This represent the cells in transactions in the projected databasee 
	 * It is an array, where each  transaction appears followed by "-1". 
	 * This is the most efficient way that I have found to implement projected databases.**/
	int cells[];
	
	/** the minSupport threshold **/
	int minSupport = 0;
	
	/** temporary structure used to match an item to its row in a header table structure.
	 * It is used for building each projected H structure. 
	 * Key= item Value= the corresponding row */
	private Map<Integer, Row> mapItemRow;
	
	/** a class to convert old item name to new item names according to
	 * the total oder for optimization **/
	ItemNameConverter nameConverter;
	
	/**
	 * Default constructor
	 */
	public AlgoHMine() {
	}

	/**
	 * Run the algorithm
	 * @param input the input file path
	 * @param output the output file path
	 * @param minSupport the minimum support threshold
	 * @throws IOException exception if error while writing the file
	 */
	public void runAlgorithm(String input, String output, double minSupport) throws IOException {

		// initialize the buffer for storing the current itemset
		itemsetBuffer = new int[BUFFERS_SIZE];
		
		// reset memory logger
		MemoryLogger.getInstance().reset();
		
		// record the start time of the algorithm
		startTimestamp = System.currentTimeMillis();
		
		// create a writer object to write results to file
		writer = new BufferedWriter(new FileWriter(output));

		// create a map to store the support of each item
		final Map<Integer, Integer> mapItemToSupport = new HashMap<Integer, Integer>();

		// We scan the database a first time to calculate the support of each item.
		BufferedReader myInput = null;
		String thisLine;
		// this variable will count the number of item occurence in the database
		int itemOccurrencesCount = 0;
		// this variable will count the number of transactions
		int transactionCount = 0;
		try {
			// prepare the object for reading the file
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
				
				// the first part is the list of items
				String items[] = thisLine.split(" "); 
				// for each item, we update its support
				for(int i=0; i <items.length; i++){
					// convert item to integer
					Integer item = Integer.parseInt(items[i]);
					// get the current support of that item
					Integer support = mapItemToSupport.get(item);
					// add 1 to the support of this item
					support = (support == null)? 1 : support + 1;
					mapItemToSupport.put(item, support);
					itemOccurrencesCount++;
				}
				transactionCount++;
			}
		} catch (Exception e) {
			// catches exception if error while reading the input file
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
	    }
		
		// convert from an absolute minsup to a relative minsup by multiplying
		// by the database size
		this.minSupport = (int) Math.ceil(minSupport * transactionCount);
		
		// Create a list of table rows for the initial HStructure
		List<Row> rowList = new ArrayList<Row>();
		// Create a map to map each item to its corresponding row
		// key: item  value: the corresponding row in the initial H-Struct
		mapItemRow = new HashMap<Integer, Row>();
		
		// This is to store the transactions used by the HStruct 
		// (we only create it once and it will be used for projections)
		// All transactions are stored in a single common array where
		//  each transaction appears followed by -1.
		cells = new int[transactionCount + itemOccurrencesCount];
		
		// We will create the row in the initial HStruct for
		// items having enough support (a.k.a support >= minsup).
		// For each item
		for(Integer item: mapItemToSupport.keySet()){
			// if the item is promising  (support >= minsup)
			int support = mapItemToSupport.get(item);
			if(support >= this.minSupport){
				// create a row for this item and add it to the HStruct table
				Row rowItem = new Row(item);
				rowItem.support = support;  // set its support (a.k.a ubItem value)
				rowList.add(rowItem); // add the row to the list of row
				mapItemRow.put(item, rowItem);
			}
		}
		
		// SORT THE LIST OF ROWS IN ASCENDING ORDER of support
		Collections.sort(rowList, new Comparator<Row>(){
			public int compare(Row o1, Row o2) {
				// compare the support of the items
				int compare = mapItemToSupport.get(o1.item) - mapItemToSupport.get(o2.item);
				// if the same, use the lexical order otherwise use the support
				return (compare == 0)? o1.item - o2.item :  compare;
			}});
		
		// We rename the items according to the lexicographical order.
		// This is an optimization that will allow us very fast comparison
		// of items according to the total order.
		nameConverter = new ItemNameConverter(rowList.size());
		// for each item
		for(Row row : rowList) {
			// we rename the item with a new name
			row.item = nameConverter.assignNewName(row.item);
		}
		
		// SECOND DATABASE PASS TO FILL THE INITIAL HStruct
		// OF 1-ITEMSETS  HAVING support  >= minsup (promising items)
		// The HStruct is composed of two parts:
		// - An array of cells representing the transactions
		// - The table (a list of rows) of the HStruct, which also includes the pointers
		// between the transactions
		cells[0] = -1;
		// This variable is the current insertion position in the cell array
		// We start at 0
		int currentCellIndex = 0;
		try {
			// prepare object for reading the file
			myInput = new BufferedReader(new InputStreamReader(new FileInputStream(new File(input))));

			// for each line (transaction) until the end of file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is  a comment, is  empty or is a
				// kind of metadata
				if (thisLine.isEmpty() == true ||
						thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
								|| thisLine.charAt(0) == '@') {
					continue;
				}
				
				// get the list of items
				String items[] = thisLine.split(" ");
				
				
				// record the position of the first item of the current transaction
				// in the cell array
				int transactionBegin = currentCellIndex;
				// For each item, create its cell in the cell array
				for(int i=0; i <items.length; i++){
					int item = Integer.parseInt(items[i]);
					// if the item has enough support
					if(mapItemToSupport.get(item) >= this.minSupport){
						// add it to the current transaction in the list
						// of transactions, where each item is represented by a cell
						cells[currentCellIndex++] = nameConverter.toNewName(item);
					}
				}
				// record the position of the last item of the current transaction
				// in the cell array
				int transactionEnd = currentCellIndex-1;
				
				// sort the transaction by ascending order of support
				Arrays.sort(cells, transactionBegin, transactionEnd+1);
				
				// insert a -1 after the transaction in the cell array to
				// separate it from the next transaction
				cells[currentCellIndex++] = -1;
				
				// for each item left in the transaction
				// we will update its row in the HStruct table
				for(int i = transactionBegin; i <= transactionEnd; i++) {
					int item = cells[i];

					// get the row of this item in the current HStruct table
					Row row = mapItemRow.get(nameConverter.toOldName(item));

					// add the pointer to the list of pointers in the HStruct for this item
					row.pointers.add(i);
				}
			}
		} catch (Exception e) {
			// to catch error while reading the input file
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
	    }
		
		// If in debug mode, we will print the initial HStruct cells
		// and the initial HStruct table.
		if(DEBUG) {
			System.out.println("------ INITIAL HStruct -----");
			// Print the cell list
			System.out.println("The cell list:");
			for(int i=1; i< cells.length; i++) {
				int item = cells[i];
				if(item == -1) {
					System.out.println("|");
				}else {
					System.out.print(item);
				}
			}
			// Print the table
			System.out.println("The table:");
			for(Row row : rowList) {
				// print the row
				System.out.print(row);
				// print the item that are pointed from that row
				System.out.print( "[the items:");
				for(int pointer : row.pointers) {
					System.out.print(cells[pointer] + " ");
				}
				System.out.println();
			}
		}

		// check the memory usage
		MemoryLogger.getInstance().checkMemory();

		// Mine the database recursively
		hmine(itemsetBuffer, 0, rowList);
		
		// check the memory usage again and close the file.
		MemoryLogger.getInstance().checkMemory();
		// close output file
		writer.close();
		// record end time
		endTimestamp = System.currentTimeMillis();
	}
	
	/**
	 * This is the recursive method to find all patterns. It writes
	 * the itemsets to the output file.
	 * @param prefix  This is the current prefix. Initially, it is empty.
	 * @param prefixLength The current prefix length
	 * @param rowList the list of row in the current HStruct (containing only promising items)
	 * @throws IOException
	 */
	private void hmine(int[] prefix, int prefixLength, List<Row> rowList) throws IOException {
		
		// Note: we assume that only the frequent items are in the HStruct.
		// ======== Otherwise
		// For each item that can extend the current prefix and is frequent
		for(Row row : rowList) {

			// create the new projected row list 
			List<Row> newRowList = new ArrayList<Row>();
			mapItemRow.clear();
			
			// for each transaction containing the item
			for(int pointer : row.pointers) {
				int transactionBegin = pointer;
				
				// if there is nothing after the item, we don't need
				// to create a new row
				transactionBegin++;
				if(cells[transactionBegin] == -1) {
					continue;
				}
				
				// find the end of the transaction
				// and calculate the reamining support
				int transactionEnd = -1;
				for(int pos = transactionBegin; ; pos++) {
					if(cells[pos] == -1) {
						transactionEnd = pos-1;
						break;
					}
				}
				
				// otherwise, we create the projected row
				// For each item in the transaction
				for(int pos = transactionBegin; pos <= transactionEnd; pos++) {
					int item = cells[pos];
					Row rowItem = mapItemRow.get(item);
					if(rowItem == null) {
						rowItem = new Row(item);
						mapItemRow.put(item, rowItem);
					}
					rowItem.support++;
					
					// add new pointer
					rowItem.pointers.add(pos);
				}
			}
			
			// add all the promising row and sort them
			for(Entry<Integer,Row> entry : mapItemRow.entrySet()) {
				Row currentRow = entry.getValue();
				if(currentRow.support >= minSupport) {
					newRowList.add(currentRow);
				}
			}

			// output this itemset
			writeOut(itemsetBuffer, prefixLength, row.item, row.support);
			
			if(newRowList.size() != 0) {
				// SORT THE LIST OF ROWS IN ASCENDING ORDER of support
				// (in the paper they used descending order, but it is equivalent here 
				//  because we traverse items in the row table in reverse order 
				// compared to the paper).
				Collections.sort(newRowList, new Comparator<Row>(){
					public int compare(Row o1, Row o2) {
						// compare the support of the items (because they have been renamed
						return o1.item - o2.item;
					}
					} );
				
				// recursive call using the projected row list
				itemsetBuffer[prefixLength] = row.item;
				
				// Recursive call to mine larger itemsets using the new prefix
				hmine(itemsetBuffer, prefixLength+1, newRowList);
			}
		}
		MemoryLogger.getInstance().checkMemory();
	}


	/**
	 * Method to write an itemset to the output file.
	 * @param the prefix to be writent o the output file
	 * @param an item to be appended to the prefix
	 * @param support the support of the prefix concatenated with the item
	 * @param prefixLength the prefix length
	 */
	private void writeOut(int[] prefix, int prefixLength, int item, int support) throws IOException {
		patternCount++; // increase the number of high support itemsets found
		
		//Create a string buffer
		StringBuilder buffer = new StringBuilder();
		// append the prefix
		for (int i = 0; i < prefixLength; i++) {
			buffer.append(nameConverter.toOldName(prefix[i]));
			buffer.append(' ');
		}
		// append the last item
		buffer.append(nameConverter.toOldName(item));
		// append the support value
		buffer.append(" #SUP: ");
		buffer.append(support);
		// write to file
		writer.write(buffer.toString());
		writer.newLine();
	}

	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  HMine ALGORITHM v96r18- STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + 
				MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println(" Frequent itemsets count : " + patternCount); 
		System.out.println("===================================================");
	}
}