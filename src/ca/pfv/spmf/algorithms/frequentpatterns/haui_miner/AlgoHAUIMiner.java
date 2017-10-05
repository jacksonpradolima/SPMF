package ca.pfv.spmf.algorithms.frequentpatterns.haui_miner;

/* This is an implementation of the HAUI-Miner algorithm. 
* 
* Copyright (c) 2016 HAUI-Miner
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is an implementation of the "HAUI-Miner Algorithm" for High Average-Utility Itemsets Mining
 * as described in the paper : Jerry Chun-Wei Lin, Ting Li, Philippe Fournier-Viger, Tzung-Pei Hong, Justin Zhan, and Miroslav Voznak. 
 *  An Efficient Algorithm to Mine High Average-Utility Itemsets[J]. 
 *  Advanced Engineering Informatics, 2016, 30(2):233-243
 *
 * @see UtilityList
 * @see Element
 * @author Ting Li
 */
public class AlgoHAUIMiner {

	// variable for statistics
	double maxMemory = 0;     // the maximum memory usage
	long startTimestamp = 0;  // the time the algorithm started
	long endTimestamp = 0;   // the time the algorithm terminated
	int huiCount =0;  // the number of HUI generated
	
	Map<Integer, Integer> mapItemToAUUB;
	
	BufferedWriter writer = null;  // writer to write the output file
	
	// this class represent an item and its utility in a transaction
	class Pair{
		int item = 0;
		int utility = 0;
	}
	
	/**
	 * Default constructor
	 */
	public AlgoHAUIMiner() {
	}

	/**
	 * Run the algorithm
	 * @param input the input file path
	 * @param output the output file path
	 * @param minAUtility the minimum utility threshold
	 * @throws IOException exception if error while writing the file
	 */
	public void runAlgorithm(String input, String output, int minAUtility) throws IOException {
		// reset maximum
		maxMemory =0;
		
		startTimestamp = System.currentTimeMillis();
		
		writer = new BufferedWriter(new FileWriter(output));

		//  We create a  map to store the TWU of each item
		mapItemToAUUB = new HashMap<Integer, Integer>();

		// We scan the database a first time to calculate the TWU of each item.
		BufferedReader myInput = null;
		String thisLine;
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
				String utilityValues[] = split[2].split(" "); 
				// find the transaction max utility
				Integer transactionMUtility = Integer.MIN_VALUE;
				
				for(int i = 0; i < utilityValues.length; i++){
					if(transactionMUtility <  Integer.parseInt(utilityValues[i])){
						transactionMUtility = Integer.parseInt(utilityValues[i]);
					}
				}
				
				// for each item, we add the transaction utility to its AUUB
				for(int i=0; i <items.length; i++){
					// convert item to integer
					Integer item = Integer.parseInt(items[i]);
					// get the current AUUB of that item
					Integer auub = mapItemToAUUB.get(item);
					// add the utility of the item in the current transaction to its AUUB
					auub = (auub == null)? 
							transactionMUtility : auub + transactionMUtility;
					mapItemToAUUB.put(item, auub);
				}
			}
		} catch (Exception e) {
			// catches exception if error while reading the input file
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
	    }
		
		// CREATE A LIST TO STORE THE UTILITY LIST OF ITEMS WITH TWU  >= MIN_UTILITY.
		List<Integer> ltemLists = new ArrayList<Integer>();
		// CREATE A MAP TO STORE THE UTILITY LIST FOR EACH ITEM.
		
		// For each item
		for(Integer item: mapItemToAUUB.keySet()){
			// if the item is promising  (AUUB >= minAutility)
			if(mapItemToAUUB.get(item) >= minAUtility){
				// create an empty Utility List that we will fill later.
				ltemLists.add(item);
			}
		}
		// SORT THE LIST OF HIGH TWU ITEMS IN ASCENDING ORDER
		Collections.sort(ltemLists, new Comparator<Integer>(){
			public int compare(Integer o1, Integer o2) {
				// compare the TWU of the items
				return compareItems(o1, o2);
			}
			} );
		
		try {
			// prepare object for reading the file
			myInput = new BufferedReader(new InputStreamReader(new FileInputStream(new File(input))));
			//
			List<List<Pair>> reviesdDatabase = new ArrayList<List<Pair>>();
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
				
				// Copy the transaction into lists but 
				// without items with TWU < minutility
				
				// Create a list to store items
				List<Pair> revisedTransaction = new ArrayList<Pair>();
				// for each item
				for(int i=0; i <items.length; i++){
					/// convert values to integers
					Pair pair = new Pair();
					pair.item = Integer.parseInt(items[i]);
					pair.utility = Integer.parseInt(utilityValues[i]);
					// if the item has enough utility
					if(mapItemToAUUB.get(pair.item) >= minAUtility){
						// add it
						revisedTransaction.add(pair);
					}
				}
				
				Collections.sort(revisedTransaction, new Comparator<Pair>(){
					public int compare(Pair o1, Pair o2) {
						return compareItems(o1.item, o2.item);
					}});
				
				reviesdDatabase.add(revisedTransaction);
			}

			
			for(Integer item: ltemLists)
				initialUtilityList(minAUtility, reviesdDatabase, item);
			
		} catch (Exception e) {
			// to catch error while reading the input file
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
	    }
		
		
		// check the memory usage
		checkMemory();
		
		// check the memory usage again and close the file.
		checkMemory();
		// close output file
		writer.close();
		// record end time
		endTimestamp = System.currentTimeMillis();
	}
	
	/**
	 * 
	 * @param input
	 * @param minAUtility
	 * @throws IOException 
	 */
	private void initialUtilityList(int minAUtility, List<List<Pair>> revisedDatabase, int item) throws IOException{

		// SECOND DATABASE PASS TO CONSTRUCT THE UTILITY LISTS 
		// OF 1-ITEMSETS  HAVING TWU  >= minutil (promising items)

		Map<Integer, UtilityList> mapItemToUtilityList = new HashMap<Integer, UtilityList>();
		Map<Integer, Integer> mapItemToAuubList = new HashMap<Integer, Integer>();
		for(int tid = 0; tid < revisedDatabase.size(); tid++){
			// for each item left in the transaction
			for(int i =0; i < revisedDatabase.get(tid).size(); i++){
				
				if(revisedDatabase.get(tid).get(i).item==item){
					
					int transactionMaxUtility = Integer.MIN_VALUE;
					
					// re-calculate the max utility of the transaction 
					for(int j = i; j < revisedDatabase.get(tid).size(); j++){
						Pair pair = revisedDatabase.get(tid).get(j);
						if(pair.utility>transactionMaxUtility){
							transactionMaxUtility = pair.utility;
						}
					}
					for(int j = i; j < revisedDatabase.get(tid).size(); j++){
						Pair pair = revisedDatabase.get(tid).get(j);
						Integer auub = mapItemToAuubList.get(pair.item);
						auub = (auub==null) ? transactionMaxUtility
								: auub+transactionMaxUtility;
						mapItemToAuubList.put(pair.item, auub);}
					}
			}
		}
		
		for(int tid = 0; tid < revisedDatabase.size(); tid++){
			// for each item left in the transaction
			for(int i =0; i < revisedDatabase.get(tid).size(); i++){
				
				if(revisedDatabase.get(tid).get(i).item==item){
					
					int maxUtility = Integer.MIN_VALUE;
					
					// re-calculate the max utility of the transaction 
					for(int j = i; j < revisedDatabase.get(tid).size(); j++){
						Pair pair = revisedDatabase.get(tid).get(j);
						if(mapItemToAuubList.get(pair.item) >= minAUtility && pair.utility>maxUtility){
							maxUtility = pair.utility;
						}
					}
					
					for(int j = i; j < revisedDatabase.get(tid).size(); j++){
						Pair pair = revisedDatabase.get(tid).get(j);
						
						//create a new Element to the utility list of this item corresponding to this transaction
						Element element = new Element(tid, pair.utility, maxUtility);
						if(mapItemToAuubList.get(pair.item) >= minAUtility){
							if(mapItemToUtilityList.containsKey(pair.item)){
								// get the average-utility list of this item
								UtilityList utilityListOfItem = mapItemToUtilityList.get(pair.item);
								// Add a new Element to the utility list of this item corresponding to this transaction
								utilityListOfItem.addElement(element);
							}else{
								UtilityList utilityListOfItem = new UtilityList(pair.item);
								// Add a new Element to the utility list of this item corresponding to this transaction
								utilityListOfItem.addElement(element);
								mapItemToUtilityList.put(pair.item, utilityListOfItem);
							}
						}
					}
					break;
				}
			}
		}
		
		// CREATE A LIST TO STORE THE UTILITY LIST OF ITEMS WITH TWU  >= MIN_UTILITY.
		List<UtilityList> listOfUtilityLists = new ArrayList<UtilityList>();
		// For each item
				for(Integer mapItem: mapItemToUtilityList.keySet()){
					UtilityList auList = mapItemToUtilityList.get(mapItem);
					// if the item is promising  (AUUB >= minAutility)
					if(auList.sumMutils >= minAUtility){
						// add the item to the list of high auub items
						listOfUtilityLists.add(auList);
					}
				}
		// SORT THE LIST OF HIGH TWU ITEMS IN ASCENDING ORDER
				Collections.sort(listOfUtilityLists, new Comparator<UtilityList>(){
					public int compare(UtilityList o1, UtilityList o2) {
						// compare the TWU of the items
						return compareItems(o1.item, o2.item);
					}
					} );
		// Mine the database recursively
		huiMiner(new int[0], null, listOfUtilityLists, minAUtility, 1);

	}
	
	private int compareItems(int item1, int item2) {
		int compare = mapItemToAUUB.get(item1) - mapItemToAUUB.get(item2);
		// if the same, use the lexical order otherwise use the TWU
		return (compare == 0)? item1 - item2 :  compare;
	}

	
	/**
	 * This is the recursive method to find all high utility itemsets. It writes
	 * the itemsets to the output file.
	 * @param prefix  This is the current prefix. Initially, it is empty.
	 * @param pUL This is the Utility List of the prefix. Initially, it is empty.
	 * @param ULs The utility lists corresponding to each extension of the prefix.
	 * @param minAUtility The minAUtility threshold.
	 * @throws IOException
	 */
	private void huiMiner(int [] prefix, UtilityList pUL, List<UtilityList> ULs, int minAUtility, int length)
			throws IOException {
		
		// For each extension X of prefix P
		for(int i=0; i< ULs.size(); i++){
			UtilityList X = ULs.get(i);

			// If pX is a high utility itemset.
			// we save the itemset:  pX 
			if(X.sumIutils/length >= minAUtility){
				// save to file
				writeOut(prefix, X.item, X.sumIutils/length);
			}
			
			// If the sum of the remaining utilities for pX
			// is higher than minAUtility, we explore extensions of pX.
			// (this is the pruning condition)
			if(X.sumMutils >= minAUtility){
				// This list will contain the utility lists of pX extensions.
				List<UtilityList> exULs = new ArrayList<UtilityList>();
				// For each extension of p appearing
				// after X according to the ascending order
				for(int j=i+1; j < ULs.size(); j++){
					UtilityList Y = ULs.get(j);
					// we construct the extension pXY 
					// and add it to the list of extensions of pX
					exULs.add(construct(pUL, X, Y));
				}
				// We create new prefix pX
				int [] newPrefix = new int[prefix.length+1];
				System.arraycopy(prefix, 0, newPrefix, 0, prefix.length);
				newPrefix[prefix.length] = X.item;
				
				// We make a recursive call to discover all itemsets with the prefix pXY
				huiMiner(newPrefix, X, exULs, minAUtility, length+1); 
			}
			if(length == 1)
				break;
		}
	}
	
	/**
	 * This method constructs the utility list of pXY
	 * @param P :  the utility list of prefix P.
	 * @param px : the utility list of pX
	 * @param py : the utility list of pY
	 * @return the utility list of pXY
	 */
	private UtilityList construct(UtilityList P, UtilityList px, UtilityList py) {
		// create an empy utility list for pXY
		UtilityList pxyUL = new UtilityList(py.item);
		// for each element in the utility list of pX
		for(Element ex : px.elements){
			// do a binary search to find element ey in py with tid = ex.tid
			Element ey = findElementWithTID(py, ex.tid);
			if(ey == null){
				continue;
			}
			// if the prefix p is null
			if(P == null){
				// Create the new element
				Element eXY = new Element(ex.tid, ex.iutils + ey.iutils, ey.mutils);
				// add the new element to the utility list of pXY
				pxyUL.addElement(eXY);
				
			}else{
				// find the element in the utility list of p wih the same tid
				Element e = findElementWithTID(P, ex.tid);
				if(e != null){
					// Create new element
					Element eXY = new Element(ex.tid, ex.iutils + ey.iutils - e.iutils,
								ey.mutils);
					// add the new element to the utility list of pXY
					pxyUL.addElement(eXY);
				}
			}	
		}
		// return the utility list of pXY.
		return pxyUL;
	}
	
	/**
	 * Do a binary search to find the element with a given tid in a utility list
	 * @param ulist the utility list
	 * @param tid  the tid
	 * @return  the element or null if none has the tid.
	 */
	private Element findElementWithTID(UtilityList ulist, int tid){
		List<Element> list = ulist.elements;
		
		// perform a binary search to check if  the subset appears in  level k-1.
        int first = 0;
        int last = list.size() - 1;
       
        // the binary search
        while( first <= last )
        {
        	int middle = ( first + last ) >>> 1; // divide by 2

            if(list.get(middle).tid < tid){
            	first = middle + 1;  //  the itemset compared is larger than the subset according to the lexical order
            }
            else if(list.get(middle).tid > tid){
            	last = middle - 1; //  the itemset compared is smaller than the subset  is smaller according to the lexical order
            }
            else{
            	return list.get(middle);
            }
        }
		return null;
	}

	/**
	 * Method to write a high utility itemset to the output file.
	 * @param the prefix to be writent o the output file
	 * @param an item to be appended to the prefix
	 * @param utility the utility of the prefix concatenated with the item
	 */
	private void writeOut(int[] prefix, int item, int autility) throws IOException {
		huiCount++; // increase the number of high utility itemsets found
		
		//Create a string buffer
		StringBuffer buffer = new StringBuffer();
		// append the prefix
		for (int i = 0; i < prefix.length; i++) {
			buffer.append(prefix[i]);
			buffer.append(' ');
		}
		// append the last item
		buffer.append(item);
		// append the utility value
		buffer.append(" #AUTIL: ");
		buffer.append(autility);
//		System.out.println(buffer);
		// write to file
		writer.write(buffer.toString());
		writer.newLine();
	}
	
	/**
	 * Method to check the memory usage and keep the maximum memory usage.
	 */
	private void checkMemory() {
		// get the current memory usage
		double currentMemory = (Runtime.getRuntime().totalMemory() -  Runtime.getRuntime().freeMemory())
				/ 1024d / 1024d;
		// if higher than the maximum until now
		if (currentMemory > maxMemory) {
			// replace the maximum with the current memory usage
			maxMemory = currentMemory;
		}
	}
	
	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  HAUI-MINER ALGORITHM v.2.15 - STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Memory ~ " + maxMemory+ " MB");
		System.out.println(" High-utility itemsets count : " + huiCount); 
		System.out.println("===================================================");
	}
}