package ca.pfv.spmf.algorithms.frequentpatterns.ssfim;

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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import ca.pfv.spmf.tools.MemoryLogger;

/**
 * An implementation of the SSFIM algorithm for mining frequent itemsets from a
 * transaction database.<br/><br/>
 * 
 * It is based on the description in:<br/><br/>
 * 
 * Djenouri, Y., Comuzzi, M. and Djenouri, D., 2017, May. SS-FIM: Single Scan for Frequent Itemsets Mining in Transactional Databases. 
 * In Pacific-Asia Conference on Knowledge Discovery and Data Mining (pp. 644-654). Springer.
 * <br/><br/>
 * 
 * @see Itemset
 * @author Philippe Fournier-Viger, 2017
 */
public class AlgoSSFIM {
 
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
	private int[] itemsetBuffer = null;
	
	/** buffer for storing the current transaction that is read from the file to reduce memory usage. **/
	private int[] transactionBuffer = null;
	
	/** size of the buffers */
	final int BUFFERS_SIZE = 200;

	/** the minSupport threshold **/
	int minSupportAbsolute = 0;

	/**
	 * Default constructor
	 */
	public AlgoSSFIM() {
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
		
		// initialize the buffer for storing the current transaction
		transactionBuffer = new int[BUFFERS_SIZE];
		
		// reset memory logger
		MemoryLogger.getInstance().reset();
		
		// record the start time of the algorithm
		startTimestamp = System.currentTimeMillis();
		
		// create a writer object to write results to file
		writer = new BufferedWriter(new FileWriter(output));

		// create a map to store the support of each itemset
		// (the hash table)
		final Map<Itemset, Integer> mapItemsetToSupport = new HashMap<Itemset, Integer>();

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
				
				int count = items.length;
				
				// Copy each item from the transaction to a buffer
				for(int i=0; i <items.length; i++){
					transactionBuffer[i] = Integer.valueOf(items[i]);
				}
				
				// Generate all subsets of a transaction except the empty set
				// and output them. We use bits to generate all subsets.
				for (long i = 1, max = 1 << count; i < max; i++) {
					
					// 
					int itemCount = 0;
							
					// for each bit
					for (int j = 0; j < count; j++) {
						// check if the j bit is set to 1
						int isSet = (int) i & (1 << j);
						// if yes, add the bit position as an item to the new subset
						if (isSet > 0) {
							itemsetBuffer[itemCount] = transactionBuffer[j];
							itemCount++;
						}
					}
					
					// copy the itemset to a new object
					int[] newItemset = new int[itemCount];
					System.arraycopy(itemsetBuffer, 0, newItemset, 0, itemCount);
					
					Itemset itemsetObject = new Itemset(newItemset);
					
					// Update the support of that itemset
					Integer support = mapItemsetToSupport.get(itemsetObject);
					if(support == null){
						mapItemsetToSupport.put(itemsetObject, 1);
					}else{
						mapItemsetToSupport.put(itemsetObject, support + 1);
					}
				}
				
//				Arrays.deepHashCode(a)
				
				// increase the number of transactions
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
		this.minSupportAbsolute = (int) Math.ceil(minSupport * transactionCount);
		
		// Save the frequent itemsets
		for(Entry<Itemset, Integer> entry : mapItemsetToSupport.entrySet()){
			int support = entry.getValue();
			if(support >= minSupportAbsolute){
				int[] itemset = entry.getKey().itemset;
				writeOut(itemset, itemset.length, support);
			}
		}

		// check the memory usage
		MemoryLogger.getInstance().checkMemory();

		// check the memory usage again and close the file.
		MemoryLogger.getInstance().checkMemory();
		// close output file
		writer.close();
		// record end time
		endTimestamp = System.currentTimeMillis();
	}


	/**
	 * Method to write an itemset to the output file.
	 * @param itemset an itemset
	 * @param support the support of the itemset
	 * @param length the ote,set
	 */
	private void writeOut(int[] itemset, int length,  int support) throws IOException {
		patternCount++; // increase the number of high support itemsets found
		
		//Create a string buffer
		StringBuilder buffer = new StringBuilder();
		// append the prefix
		for (int i = 0; i < length; i++) {
			buffer.append(itemset[i]);
			buffer.append(' ');
		}
		// append the support value
		buffer.append("#SUP: ");
		buffer.append(support);
		// write to file
		writer.write(buffer.toString());
		writer.newLine();
	}

	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  SSFIM ALGORITHM v2.19 - STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + 
				MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println(" Frequent itemsets count : " + patternCount); 
		System.out.println("===================================================");
	}
}