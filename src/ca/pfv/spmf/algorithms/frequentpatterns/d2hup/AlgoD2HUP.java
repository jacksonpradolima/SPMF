package ca.pfv.spmf.algorithms.frequentpatterns.d2hup;

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
 * This is an implementation of the "D2HUP Algorithm" for High-Utility Itemsets Mining
 * as described in the conference paper : <br/><br/>
 * 
 *  Liu, J.,  Wang, K., Fung, B. (2012). Direct discovery of high utility 
 *  itemsets without candidate generation. Proceedings of the 2012 IEEE 12th 
 *  International Conference on Data Mining. IEEE Computer Society, 2012.
 *
 * @author Philippe Fournier-Viger, 2015
 */
public class AlgoD2HUP {

	/** the time the algorithm started */
	long startTimestamp = 0; 
	/** the time the algorithm terminated */
	long endTimestamp = 0;  
	/** the number of HUI generated */
	int huiCount =0;  
	/** the number of times case 1 is applied (for statistics)*/
	int case1count = 0;
	/** the number of times case 2 is applied (for statistics)*/
	int case2count = 0;
	
	/** writer to write the output file **/
	BufferedWriter writer = null;  
	
	/** buffer for storing the current itemset that is mined when performing mining
	* the idea is to always reuse the same buffer to reduce memory usage. **/
	final int BUFFERS_SIZE = 200;
	private int[] itemsetBuffer = null;
	
	/** if true, debugging information will be shown in the console */
	boolean DEBUG = false;
	
	/** This represent the cells in transactions in the CAUL structure 
	 * It is an array, where each  transaction appears followed by "null". 
	 * This is the most efficient way that I have found to implement CAUL.**/
	Cell cells[];
	
	/** the minUtility threshold **/
	int minUtility = 0;
	
	/** temporary structure used to match an item to its row in a CAUL structure.
	 * It is used for building each CAUL. Key= item Value= the corresponding row */
	private Map<Integer, Row> mapItemRow;
	
	/** a class to convert old item name to new item names according to
	 * the total oder for optimization **/
	ItemNameConverter nameConverter;
	
	/**
	 * Default constructor
	 */
	public AlgoD2HUP() {
	}

	/**
	 * Run the algorithm
	 * @param input the input file path
	 * @param output the output file path
	 * @param minUtility the minimum utility threshold
	 * @throws IOException exception if error while writing the file
	 */
	public void runAlgorithm(String input, String output, int minUtility) throws IOException {
		// reset maximum
		MemoryLogger.getInstance().reset();
		
		// initialize the buffer for storing the current itemset
		itemsetBuffer = new int[BUFFERS_SIZE];
		
		// record the start time of the algorithm
		startTimestamp = System.currentTimeMillis();
		
		// create a writer object to write results to file
		writer = new BufferedWriter(new FileWriter(output));
		
		// save the minimum utility threshold
		this.minUtility = minUtility;

		// create a map to store the TWU of each item
		// key = item      value= its TWU
		final Map<Integer, Integer> mapItemToTWU = new HashMap<Integer, Integer>();
		
		// reset counters
		case1count = 0;
		case2count = 0;

		// We scan the database a first time to calculate the TWU of each item.
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
				
				// split the transaction according to the : separator
				String split[] = thisLine.split(":"); 
				// the first part is the list of items
				String items[] = split[0].split(" "); 
				// the second part is the transaction utility
				int transactionUtility = Integer.parseInt(split[1]);  
				// for each item, we add the transaction utility to its TWU
				for(int i=0; i <items.length; i++){
					// convert item to integer
					Integer item = Integer.parseInt(items[i]);
					// get the current TWU of that item
					Integer twu = mapItemToTWU.get(item);
					// add the utility of the item in the current transaction to its twu
					twu = (twu == null)? 
							transactionUtility : twu + transactionUtility;
					mapItemToTWU.put(item, twu);
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
		
		// Create a list of table rows for the initial CAUL
		List<Row> rowList = new ArrayList<Row>();
		// Create a map to map each item to its corresponding row
		// key: item  value: the corresponding row in the initial CAUL
		mapItemRow = new HashMap<Integer, Row>();
		
		// This is to store the database used by the CAUL 
		// (we only create it once and it will be used for projections)
		// All transactions are represented by a single array where
		//  each transaction appears followed by null.
		cells = new Cell[transactionCount + itemOccurrencesCount];
		
		// We will create the row in the initial CAUL table for
		// items having enough TWU (a.k.a uBitem >= minutil).
		// For each item
		for(Integer item: mapItemToTWU.keySet()){
			// if the item is promising  (TWU >= minutility)
			int twu = mapItemToTWU.get(item);
			if(twu >= minUtility){
				// create a row for this item and add it to the CAUL table
				Row rowItem = new Row(item);
				rowItem.ltwu = twu;  // set its TWU (a.k.a ubItem value)
				rowList.add(rowItem); // add the row to the list of row
				mapItemRow.put(item, rowItem);
			}
		}
		
		// SORT THE LIST OF ROWS IN ASCENDING ORDER of TWU
		// (in the paper they used descending order, but it is equivalent  
		//  because we traverse items in the table in reverse order 
		// compared to the paper).
		Collections.sort(rowList, new Comparator<Row>(){
			public int compare(Row o1, Row o2) {
				// compare the TWU of the items
				int compare = mapItemToTWU.get(o1.item) - mapItemToTWU.get(o2.item);
				// if the same, use the lexical order otherwise use the TWU
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
		
		// SECOND DATABASE PASS TO FILL THE INITIAL CAUL
		// OF 1-ITEMSETS  HAVING TWU  >= minutil (promising items)
		// The CAUL is composed of two parts:
		// - An array of cells representing the transactions
		// - The table (a list of rows) of the CAUL, which also includes the pointers
		// between the transactions
		cells[0] = null;
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
				
				// split the line according to the separator
				String split[] = thisLine.split(":");
				// get the list of items
				String items[] = split[0].split(" ");
				// get the list of utility values corresponding to each item
				// for that transaction
				String utilityValues[] = split[2].split(" ");
				
				// Copy the transaction but 
				// without items with TWU (a.k.a uBitem < minutility)
				// We will calculate the reamining utility in the transaction
				int remainingUtility = 0;
				
				// record the position of the first item of the current transaction
				// in the cell array
				int transactionBegin = currentCellIndex;
				// For each item, create its cell in the cell array
				for(int i=0; i <items.length; i++){
					int item = Integer.parseInt(items[i]);
					// if the item has enough utility
					if(mapItemToTWU.get(item) >= minUtility){
						/// Create a new cell to store the item and its utility
						Cell cell = new Cell();
						cell.utility = Integer.parseInt(utilityValues[i]);
						cell.item = nameConverter.toNewName(item);
						// add it to the current transaction in the list
						// of transactions, where each item is represented by a cell
						cells[currentCellIndex++] = cell;
						// increase the reamining utility
						remainingUtility += cell.utility;
					}
				}
				// record the position of the last item of the current transaction
				// in the cell array
				int transactionEnd = currentCellIndex-1;
				
				// sort the transaction by ascending order of TWU
				Arrays.sort(cells, transactionBegin, transactionEnd+1,
						new Comparator<Cell>(){
					public int compare(Cell o1, Cell o2) {
						return o1.item - o2.item;
					}});
				
				// insert a null after the transaction in the cell array to
				// separate it from the next transaction
				cells[currentCellIndex++] = null;

				
				// for each item left in the transaction
				// we will update its row in the CAUL table
				for(int i = transactionBegin; i <= transactionEnd; i++) {
					// Get the cell
					Cell cell = cells[i];
					
					// get the row of this item in the current CAUL table
					Row row = mapItemRow.get(nameConverter.toOldName(cell.item));
					
					// increment the support (s) of that item
					row.support++;
					
					// increment the total utility (u) of that item
					row.utility += cell.utility;
				
					// subtract the utility of this item from the remaining utility
					remainingUtility -= cell.utility;
					
					// increment the remaining utility (ubPFE) of this item
					// Note that ubPFE here is defined as the remaining utility as in
					// the HUI-Miner paper, which is a slightly different definition but
					// give the same result.
					row.rutil += remainingUtility;
					
					// add the pointer to the list of pointers in the CAUL for this item
					row.pointers.add(new Pointer(0,i));
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
		
		// If in debug mode, we will print the initial CAUL cells
		// and the initial CAUL table.
		// Note that ubPFE is not exactly defined as in the paper therefore,
		// it may not look exactly the same as in the paper. But the result of the algorithm
		// is the same. 
		if(DEBUG) {
			System.out.println("------ INITIAL CAUL -----");
			// Print the cell list
			System.out.println("The cell list:");
			for(int i=1; i< cells.length; i++) {
				Cell cell = cells[i];
				if(cell == null) {
					System.out.println("|");
				}else {
					System.out.print(cell);
				}
			}
			// Print the table
			System.out.println("The table:");
			for(Row row : rowList) {
				// print the row
				System.out.print(row);
				// print the item that are pointed from that row
				System.out.print( "[the items:");
				for(Pointer pointer : row.pointers) {
					System.out.print(cells[pointer.pos].item + " ");
				}
				System.out.println();
			}
		}

		// check the memory usage
		MemoryLogger.getInstance().checkMemory();

		// Mine the database recursively
		d2hup(itemsetBuffer, 0, rowList, transactionCount, 0 );
		
		// check the memory usage again and close the file.
		MemoryLogger.getInstance().checkMemory();
		// close output file
		writer.close();
		// record end time
		endTimestamp = System.currentTimeMillis();
	}
	
	/**
	 * This is the recursive method to find all high utility itemsets. It writes
	 * the itemsets to the output file.
	 * @param prefix  This is the current prefix. Initially, it is empty.
	 * @param prefixLength The current prefix length
	 * @param rowList the list of row in the current CAUL (containing only promising items)
	 * @param prefixSupport This is the support of the prefix
	 * @param prefixUtility the utility of the prefix
	 * @throws IOException
	 */
	private void d2hup(int[] prefix, int prefixLength, List<Row> rowList, int prefixSupport, int prefixUtility) throws IOException {
		// Note: we assume that only the promising items are in the CAUL.
		
		// If debug, we print the prefix
		if(DEBUG) {
			System.out.print(" prefix : ");
			for(int i =0; i < prefixLength; i++) {
				System.out.print(" " +  prefix[i]);
			}
			System.out.println();
		}
//		
		// ======= Check case 1 as described in the paper
		boolean allPromisingItemsHaveSameSupportAsPrefix = true;
		boolean allPromisingItemAreHighUtility = true;
		// check if all rows are high utility prefix extensions
		for(Row row : rowList) {
			if(row.utility < minUtility) {
				allPromisingItemAreHighUtility = false;
				break;
			}
		}
		// check if all rows are prefix extensions having 
		// the same support as the prefix
		for(Row row : rowList) {
			if(row.support != prefixSupport) {
				allPromisingItemsHaveSameSupportAsPrefix = false;
				break;
			}
		}
		// if the two previous conditions are met
		if(allPromisingItemsHaveSameSupportAsPrefix && allPromisingItemAreHighUtility) {
			// We will output each non-empty subset of W U pat(N) as an HUP
			// Generate all subsets of the prefixPath except the empty set
			// and output them
			// We use bits to generate all subsets.
			for (long i = 1, max = 1 << rowList.size(); i < max; i++) {
				
				// we create a new subset
				int newPrefixLength = prefixLength;
				int utility = prefixUtility;
				// for each bit
				for (int j = 0; j < rowList.size(); j++) {
					// check if the j bit is set to 1
					int isSet = (int) i & (1 << j);
					// if yes, add the bit position as an item to the new subset
					if (isSet > 0) {
						itemsetBuffer[newPrefixLength++] = rowList.get(j).item;
						utility += (rowList.get(j).utility - prefixUtility);
					}
				}
				// save the itemset
				writeOut(prefix, newPrefixLength, utility);
			}
			
			// update statistics for case 1
			case1count++;
			
			// and then we return.
			 return;
		}
		
		// ======= Check case 2 as described in the paper

		// If all item have the same support as the prefix
		if(allPromisingItemsHaveSameSupportAsPrefix) {
			// L6: Calculate delta
			int delta = Integer.MAX_VALUE;
			for(Row row : rowList) {
				int subtraction = row.utility - prefixUtility;
				if(subtraction < delta) {
					delta = subtraction;
				}
			}
			
			// calculate the sum of utility of promising items
			int sum = prefixUtility;
			for(Row row : rowList) {
				sum += row.utility - prefixUtility;
			}
			if((minUtility <= sum) &&  (sum < (minUtility + delta))){
				// update statistics for case 1
				case2count++;
				
				// output eW U pat(N) as an HUP
				int itemsetLength = prefixLength;
				for(Row row : rowList) {
					prefix[itemsetLength++] = row.item;
				}
				writeOut(prefix, itemsetLength, sum);
				return;
			}
		}
		
		
		// ======== Otherwise
		// For each promising item
		for(Row row : rowList) {

			// if the concatenation of the item with the prefix is high utility,
			// then output it
			if(row.utility >= minUtility) {
				writeOut(prefix, prefixLength, row.item, row.utility);
			}
			// If enough remaining utility (ubpfe >= minutil), 
			// create the CAUL and recursively explore
			if(row.utility + row.rutil >= minUtility) {  /// xxxxxxxx
				// create the new projected row list 
				List<Row> newRowList = new ArrayList<Row>();
				mapItemRow.clear();
				
				// for each transaction containing the item
				for(Pointer pointer : row.pointers) {
					int transactionBegin = pointer.pos;
					
					// newPrefixUtility
					int newPrefixRowUtility = pointer.prefixUtility +
							cells[pointer.pos].utility;
					
					// if there is nothing after the item, we don't need
					// to create a new row
					transactionBegin++;
					if(cells[transactionBegin] == null) {
						continue;
					}
					
					// find the end of the transaction
					// and calculate the reamining utility
					int transactionEnd = -1;
					int rtwu = 0;
					for(int pos = transactionBegin; ; pos++) {
						if(cells[pos] == null) {
							transactionEnd = pos-1;
							break;
						}
						rtwu += cells[pos].utility;
					}
					
					// otherwise, we create the projected row
					// For each item in the transaction
					int remainingUtility = rtwu;
					for(int pos = transactionBegin; pos <= transactionEnd; pos++) {
						Cell cell = cells[pos];
						Row rowItem = mapItemRow.get(cell.item);
						if(rowItem == null) {
							rowItem = new Row(cell.item);
							mapItemRow.put(cell.item, rowItem);
						}
						rowItem.support++;
						rowItem.utility += newPrefixRowUtility + cell.utility;
						rowItem.ltwu+= rtwu;
						
						// add new pointer
						Pointer newPointer = new Pointer(newPrefixRowUtility, pos);
						rowItem.pointers.add(newPointer);
						
						// update remaining utility
						remainingUtility -= cell.utility;
						rowItem.rutil += remainingUtility;
					}
				}
				
				// add all the promising row and sort them
				for(Entry<Integer,Row> entry : mapItemRow.entrySet()) {
					Row currentRow = entry.getValue();
					if(row.utility + currentRow.ltwu >= minUtility) {
						newRowList.add(currentRow);
					}
				}
				// SORT THE LIST OF ROWS IN ASCENDING ORDER of TWU
				// (in the paper they used descending order, but it is equivalent here 
				//  because we traverse items in the row table in reverse order 
				// compared to the paper).
				Collections.sort(newRowList, new Comparator<Row>(){
					public int compare(Row o1, Row o2) {
						// compare the TWU of the items (because they have been renamed
						return o1.item - o2.item;
					}
					} );
				
				// recursive call using the projected row list
				itemsetBuffer[prefixLength] = row.item;
				
				// Recursive call to mine larger itemsets using the new prefix
				d2hup(itemsetBuffer, prefixLength+1, newRowList, row.support, row.utility);
			}
		}
		MemoryLogger.getInstance().checkMemory();
	}



	/**
	 * Method to write a high utility itemset to the output file.
	 * @param the prefix to be writent o the output file
	 * @param an item to be appended to the prefix
	 * @param utility the utility of the prefix concatenated with the item
	 * @param prefixLength the prefix length
	 */
	private void writeOut(int[] prefix, int prefixLength, int item, int utility) throws IOException {
		huiCount++; // increase the number of high utility itemsets found
		
		//Create a string buffer
		StringBuilder buffer = new StringBuilder();
		// append the prefix
		for (int i = 0; i < prefixLength; i++) {
			buffer.append(nameConverter.toOldName(prefix[i]));
			buffer.append(' ');
		}
		// append the last item
		buffer.append(nameConverter.toOldName(item));
		// append the utility value
		buffer.append(" #UTIL: ");
		buffer.append(utility);
		// write to file
		writer.write(buffer.toString());
		writer.newLine();
	}
	
	/**
	 * Method to write a high utility itemset to the output file.
	 * @param the prefix to be writent o the output file
	 * @param utility the utility of the prefix concatenated with the item
	 * @param prefixLength the prefix length
	 */
	private void writeOut(int[] prefix, int prefixLength,  int utility) throws IOException {
		huiCount++; // increase the number of high utility itemsets found
		
		//Create a string buffer
		StringBuilder buffer = new StringBuilder();
		// append the prefix
		for (int i = 0; i < prefixLength; i++) {
			buffer.append(nameConverter.toOldName(prefix[i]));
			buffer.append(' ');
		}
		// append the utility value
		buffer.append(" #UTIL: ");
		buffer.append(utility);
		// write to file
		writer.write(buffer.toString());
		writer.newLine();
	}
	
	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  D2HUP ALGORITHM v97- STATS =============");
		System.out.println(" Case1 count: " + case1count + " | Case2 count: " + case2count);
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + 
				MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println(" High-utility itemsets count : " + huiCount); 
		System.out.println("===================================================");
	}
}