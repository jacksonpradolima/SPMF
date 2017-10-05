package ca.pfv.spmf.algorithms.frequentpatterns.haui_mmau;

/* This is an implementation of the HAUI-MMAU algorithm. 
* 
* Copyright (c) 2016 HAUI-MMAU
* 
* This file is part of the SPMF DATA MINING SOFTWARE * (http://www.philippe-fournier-viger.com/spmf). 
* 
* 
* SPMF is free software: you can redistribute it and/or modify it under the * terms of the GNU General Public License as published by the Free Software * Foundation, either version 3 of the License, or (at your option) any later * version. * 

* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR * A PARTICULAR PURPOSE. See the GNU General Public License for more details. * 
* 
* You should have received a copy of the GNU General Public License along with * SPMF. If not, see . 
* 
* @author Ting Li
*/


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;


/**
 * This class represents a transaction database with average utility values, 
 * as used by the HAUI-MMAU algorithm for high average
 * utility itemset mining.
 *
 * @see AlgoHAUIMMAU
 * @see TransactionTP
 * @author Ting Li
 */
public class UtilityTransactionDatabaseTP {

	// this is the set of items in the database
	private final Set<Integer> allItems = new HashSet<Integer>();
	// this is the list of transactions in the database
	private final List<TransactionTP> transactions = new ArrayList<TransactionTP>();
	// this is the list of mutiple minimum utility
	public Map<Integer, Integer> mutipleMinUtilities = new HashMap<Integer, Integer>();
	

	/**
	 * Load a transaction database from a file.
	 * @param path the path of the file
	 * @throws IOException exception if error while reading the file.
	 */
	public void loadFile(String path, String minutilityPath) throws IOException {
		String thisLine;
		BufferedReader myInput = null;
		try {
			FileInputStream fin = new FileInputStream(new File(path));
			myInput = new BufferedReader(new InputStreamReader(fin));
			// for each transaction (line) in the input file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is  a comment, is  empty or is a
				// kind of metadata
				if (thisLine.isEmpty() == true ||
						thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
								|| thisLine.charAt(0) == '@') {
					continue;
				}
				
				// process the transaction
				processTransaction(thisLine.split(":"));
			}
		} catch (Exception e) {
			// catch exceptions
			e.printStackTrace();
		}finally {
			if(myInput != null){
				// close the file
				myInput.close();
			}
	    }
		
		try {
			FileInputStream fin = new FileInputStream(new File(minutilityPath));
			myInput = new BufferedReader(new InputStreamReader(fin));
			// for each transaction (line) in the input file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is  a comment, is  empty or is a
				// kind of metadata
				if (thisLine.isEmpty() == true ||
						thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
								|| thisLine.charAt(0) == '@') {
					continue;
				}
				String[] items=thisLine.split(" ");
				mutipleMinUtilities.put(Integer.parseInt(items[0]), Integer.parseInt(items[1]));
			}
		} catch (Exception e) {
			// catch exceptions
			e.printStackTrace();
		}finally {
			if(myInput != null){
				// close the file
				myInput.close();
			}
	    }
	}
	
	/**
	 * Process a line (transaction) from the input file
	 * @param line  a line
	 */
	private void processTransaction(String line[]){
		
		String[] items = line[0].split(" ");
		String[] utilities = line[2].split(" ");
		
		int transactionUtility = 0;
		
		// Create a list for storing items
		List<ItemUtility> itemUtilityObjects = new ArrayList<ItemUtility>();
		// for each item
		for(int i=0; i< items.length; i++) {
			itemUtilityObjects.add(new ItemUtility(
					Integer.parseInt(items[i]),
					Integer.parseInt(utilities[i])));
			
			// get the  max utility of transaction
			if(Integer.parseInt(utilities[i])>transactionUtility)
				transactionUtility=Integer.parseInt(utilities[i]);
		}

		// add the transaction to the list of transactions
		transactions.add(new TransactionTP(itemUtilityObjects, transactionUtility));
	}

	/**
	 * Print this database to System.out.
	 */
	public void printDatabase(){
		System.out.println("===================  Database ===================");
		int count = 0;
		// for each transaction
		for(TransactionTP itemset : transactions){
			// print the transaction
			System.out.print("0" + count + ":  ");
			itemset.print();
			System.out.println("");
			count++;
		}
	}
	
	/**
	 * Get the number of transactions.
	 * @return a int
	 */
	public int size(){
		return transactions.size();
	}
	
	/**
	 * Get LMAU value
	 * @return a int
	 */
	public int getLMAU(int GLMAU){
		int LMAU=Integer.MAX_VALUE;
		java.util.Iterator<Entry<Integer, Integer>> iter1 =mutipleMinUtilities.entrySet().iterator();  
        while (iter1.hasNext()) {
            Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) iter1.next();  
            if(entry.getValue()<LMAU)
            	LMAU=entry.getValue();
        }
		if(LMAU<GLMAU)
			LMAU=GLMAU;
		return LMAU;
	}

	/**
	 * Get the list of transactions.
	 * @return the list of Transactions.
	 */
	public List<TransactionTP> getTransactions() {
		return transactions;
	}

	/**
	 * Get the set of items in this database.
	 * @return a Set of Integers
	 */
	public Set<Integer> getAllItems() {
		return allItems;
	}

}
