package ca.pfv.spmf.algorithms.frequentpatterns.mffi_miner;

/* This is an implementation of the MFFI-Miner algorithm. 
* 
* Copyright (c) 2016 MFFI-Miner
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

import ca.pfv.spmf.tools.MemoryLogger;


/**
 * This is an implementation of the "MFFI-MINER Algorithm" for Fuzzy Frequent Mining
 * as described in the conference paper : <br/><br/>
 * 
 * CW Lin��T Li��P Fournier-Viger��TP Hong (2015). Efficient Mining of Multiple Fuzzy Frequent Itemsets. 
 *  IJFS, 2016:1-9
 *
 * @see MFFIList
 * @see Element
 * @author Ting Li
 */
public class AlgoMFFIMiner {

	/** the time at which the algorithm started */
	public long startTimestamp = 0;  
	
	/** the time at which the algorithm ended */
	public long endTimestamp = 0; 
	
	/** the number of high-fuzzy itemsets generated */
	public int MFFICount =0; 
	
	/** Map to remember the TWU of each item */
	Map<Integer, Float> mapItemLowSUM;
	Map<Integer, Float> mapItemMiddleSUM;
	Map<Integer, Float> mapItemHighSUM;
	Map<Integer, Float> mapItemSUM;
	
	/** writer to write the output file  */
	BufferedWriter writer = null;  
	
	/** the number of MFFI-list that was constructed */
	private int joinCount;
	
	/** buffer for storing the current itemset that is mined when performing mining
	* the idea is to always reuse the same buffer to reduce memory usage. */
	final int BUFFERS_SIZE = 200;
	private int[] itemsetBuffer = null;
	
	/** this class represent an item and its fuzzy in a transaction */
	class Pair{
		int item = 0;
		float quanaity = 0;
	}
	
	/**
	 * Default constructor
	 */
	public AlgoMFFIMiner() {
	}

	/**
	 * Run the algorithm
	 * @param input the input file path
	 * @param output the output file path
	 * @param minSupport the minimum support threshold
	 * @throws IOException exception if error while writing the file
	 */
	public void runAlgorithm(String input, String output, float minSupport) throws IOException {
		// reset maximum
		MemoryLogger.getInstance().reset();
		
		// initialize the buffer for storing the current itemset
		itemsetBuffer = new int[BUFFERS_SIZE];
		
		startTimestamp = System.currentTimeMillis();
		
		writer = new BufferedWriter(new FileWriter(output));

		//  We create three  map to store the low, middle, high summation of each item
		mapItemLowSUM = new HashMap<Integer, Float>();
		mapItemMiddleSUM = new HashMap<Integer, Float>();
		mapItemHighSUM = new HashMap<Integer, Float>();

		// We scan the database a first time to calculate the low, middle, high value of 
		//each item through membership fuction
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
				String quanaities[] = split[2].split(" "); 

				for(int i=0; i <items.length; i++){
					// get low,middle,high value of item
					MFFIRegions regions = new MFFIRegions(Integer.parseInt(quanaities[i]));
					Integer item = Integer.parseInt(items[i]);
					// add low value
					if(mapItemLowSUM.containsKey(item)){
						float low = mapItemLowSUM.get(item);
						low += regions.low;
						mapItemLowSUM.put(item, low);
					}else{
						mapItemLowSUM.put(item, regions.low);
					}
					// add middle value
					if(mapItemMiddleSUM.containsKey(item)){
						float middle = mapItemMiddleSUM.get(item);
						middle += regions.middle;
						mapItemMiddleSUM.put(item, middle);
					}else{
						mapItemMiddleSUM.put(item, regions.middle);
					}
					// add high value
					if(mapItemHighSUM.containsKey(item)){
						float high = mapItemHighSUM.get(item);
						high += regions.high;
						mapItemHighSUM.put(item, high);
					}else{
						mapItemHighSUM.put(item, regions.high);
					}
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
		
		// CREATE A LIST TO STORE THE FUZZY LIST OF ITEMS WITH TWU  >= MIN_SUPPORT.
		List<MFFIList> listOfFFILists = new ArrayList<MFFIList>();
		// CREATE A MAP TO STORE THE FUZZY LIST FOR EACH ITEM.
		// Key : item    Value :  fuzzy list associated to that item
		Map<Integer, MFFIList> mapItemToFFIList = new HashMap<Integer, MFFIList>();
		mapItemSUM = new HashMap<Integer, Float>();
		
		// For each item
		for(Integer item: mapItemLowSUM.keySet()){
			float low  = mapItemLowSUM.get(item);
			float middle  = mapItemMiddleSUM.get(item);
			float high  = mapItemHighSUM.get(item);
			
			// if the item is promising  (support >= minSpport)
			if( low >= minSupport){
				mapItemSUM.put(item*100+12, low);
				// create an empty Fuzzy List that we will fill later.
				MFFIList fuList = new MFFIList(item*100+12);
				mapItemToFFIList.put(item*100+12, fuList);
				// add the item to the list of Fuzzy list items
				listOfFFILists.add(fuList); 
			}
			
			if( middle >= minSupport){
				mapItemSUM.put(item*1000+123, middle);
				// create an empty Fuzzy List that we will fill later.
				MFFIList fuList = new MFFIList(item*1000+123);
				mapItemToFFIList.put(item*1000+123, fuList);
				// add the item to the list of Fuzzy list items
				listOfFFILists.add(fuList); 
			}
			
			if( high >= minSupport){
				mapItemSUM.put(item*10000+1234, high);
				// create an empty Fuzzy List that we will fill later.
				MFFIList fuList = new MFFIList(item*10000+1234);
				mapItemToFFIList.put(item*10000+1234, fuList);
				// add the item to the list of Fuzzy list items
				listOfFFILists.add(fuList); 
			}
		}
		// SORT THE LIST OF FUZZY ITEMS IN ASCENDING ORDER
		Collections.sort(listOfFFILists, new Comparator<MFFIList>(){
			public int compare(MFFIList o1, MFFIList o2) {
				// compare the TWU of the items
				return (int) compareItems(o1.item, o2.item);
			}
			} );
		
		// SECOND DATABASE PASS TO CONSTRUCT THE FUZZY LISTS 
		// OF 1-ITEMSETS  HAVING TWU  >= minutil (promising items)
		try {
			// prepare object for reading the file
			myInput = new BufferedReader(new InputStreamReader(new FileInputStream(new File(input))));
			// variable to count the number of transaction
			int tid =0;
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
				// get the list of fuzzy values corresponding to each item
				// for that transaction
				String quanaities[] = split[2].split(" ");

				// Create a list to store items
				List<Pair> revisedTransaction = new ArrayList<Pair>();
				// for each item
				for(int i = 0; i < items.length; i++){
					MFFIRegions regions = new MFFIRegions(Integer.parseInt(quanaities[i]));
					
					// if the item has enough fuzzy value
					if(mapItemSUM.containsKey(Integer.parseInt(items[i])*100+12)){
						if(regions.low > 0){
							Pair pair = new Pair();
							pair.item =  Integer.parseInt(items[i])*100+12;
							pair.quanaity = regions.low;
							// add it
							revisedTransaction.add(pair);
						}
					}
					
					if(mapItemSUM.containsKey(Integer.parseInt(items[i])*1000+123)){
						if(regions.middle > 0){
							Pair pair = new Pair();
							pair.item =  Integer.parseInt(items[i])*1000+123;
							pair.quanaity = regions.middle;
							// add it
							revisedTransaction.add(pair);
						}
					}
					
					if(mapItemSUM.containsKey(Integer.parseInt(items[i])*10000+1234)){
						if(regions.high > 0){
							Pair pair = new Pair();
							pair.item =  Integer.parseInt(items[i])*10000+1234;
							pair.quanaity = regions.high;
							// add it
							revisedTransaction.add(pair);
						}
					}

				}
				// SORT THE  TRANSACTION IN ASCENDING ORDER OF PROMISING ITEMS
				Collections.sort(revisedTransaction, new Comparator<Pair>(){
					public int compare(Pair o1, Pair o2) {
						return (int) compareItems(o1.item, o2.item);
					}});

				float remainingUtility = Integer.MIN_VALUE;
				// for each item left in the transaction
				for(int i = revisedTransaction.size()-1; i  >= 0; i--){
					Pair pair = revisedTransaction.get(i);
					// subtract the fuzzy of this item from the remaining fuzzy value
					remainingUtility = (pair.quanaity > remainingUtility)? pair.quanaity: remainingUtility;
					
					// get the fuzzy list of this item
					MFFIList FFIListOfItem = mapItemToFFIList.get(pair.item);
					
					// Add a new Element to the fuzzy list of this item corresponding to this transaction
					Element element = new Element(tid, pair.quanaity, remainingUtility);
					
					FFIListOfItem.addElement(element);
				}
				tid++; // increase tid number for next transaction

			}
		} catch (Exception e) {
			// to catch error while reading the input file
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
	    }
		
		// check the memory usage
		MemoryLogger.getInstance().checkMemory();

		// Mine the database recursively
		MFFIMiner(itemsetBuffer, 0, listOfFFILists, minSupport);
		
		// check the memory usage again and close the file.
		MemoryLogger.getInstance().checkMemory();
		// close output file
		writer.close();
		// record end time
		endTimestamp = System.currentTimeMillis();
	}
	
	//used for sort
	private float compareItems(int item1, int item2) {
		float compare = mapItemSUM.get(item1) - mapItemSUM.get(item2);
		// if the same, use the lexical order otherwise use the TWU
		return (compare == 0)? item1 - item2 :  compare;
	}
	
	/**
	 * This is the main sub-proceedings
	 * @param prefix This is the current prefix. Initially, it is empty.
	 * @param prefixLength Current length of promising itemset
	 * @param FFILs A FUZZY-LIST
	 * @param minSupport The minimum support threshold count
	 * @throws IOException
	 */
	private void MFFIMiner(int [] prefix, 
			int prefixLength, List<MFFIList> FFILs, float minSupport)
			throws IOException {
		
		// For each extension X of prefix P
		for(int i=0; i< FFILs.size(); i++){
			MFFIList X = FFILs.get(i);

			// If pX is a fuzzy frequent itemset.
			// we save the itemset:  pX 
			if(X.sumIutils >= minSupport){
				// save to file
				writeOut(prefix, prefixLength, X.item, X.sumIutils);
			}
			
			int xitem=0;
			if(X.item %10000 == 1234){
				xitem = X.item/10000;
			}else if(X.item %1000 == 123){
				xitem = X.item/1000;
			} else if(X.item %100 == 12){
				xitem = X.item/100;
			}
			
			// If the sum of the remaining fuzzy utilities for X
			// is higher than minSupport, we explore extensions of X.
			// (this is the pruning condition)
			if(X.sumRutils >= minSupport){
				// This list will contain the fuzzy lists of X extensions.
				List<MFFIList> exULs = new ArrayList<MFFIList>();
				// For each extension of p appearing
				// after X according to the ascending order
				for(int j=i+1; j < FFILs.size(); j++){
					MFFIList Y = FFILs.get(j);
					int yitem=0;
					if(Y.item %10000 == 1234){
						yitem = Y.item/10000;
					}else if(Y.item %1000 == 123){
						yitem = Y.item/1000;
					} else if(Y.item %100 == 12){
						yitem = Y.item/100;
					}
					if(xitem == yitem)
						continue;
					// we construct the extension pXY 
					// and add it to the list of extensions of pX
					exULs.add(construct(X, Y));
					joinCount++;
				}
				// We create new prefix pX
				itemsetBuffer[prefixLength] = X.item;
				
				// We make a recursive call to discover all FFIs with the prefix X
				MFFIMiner(itemsetBuffer, prefixLength+1, exULs, minSupport); 
			}
		}
	}

	/**
	 * This method constructs the fuzzy list of pXY
	 * @param px: the fuzzy list of px
	 * @param py: the fuzzy list of py
	 * @return the fuzzy list of pxy
	 */
	private MFFIList construct(MFFIList px, MFFIList py) {
		// create an empy fuzzy list for pxy
		MFFIList pxyUL = new MFFIList(py.item);
		// for each element in the fuzzy list of px
		for(Element ex : px.elements){
			// do a binary search to find element ey in py with tid = ex.tid
			Element ey = findElementWithTID(py, ex.tid);
			if(ey == null){
				continue;
			}

			// Create the new element
			Element eXY = new Element(ex.tid, Float.min(ex.iutils,  ey.iutils), ey.rutils);
			// add the new element to the fuzzy list of pXY
			pxyUL.addElement(eXY);
					
		}
		// return the fuzzy list of pXY.
		return pxyUL;
	}
	
	/**
	 * Do a binary search to find the element with a given tid in a fuzzy list
	 * @param ulist the fuzzy list
	 * @param tid  the tid
	 * @return  the element or null if none has the tid.
	 */
	private Element findElementWithTID(MFFIList ulist, int tid){
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
	 * Method to write a  fuzzy frequent itemset to the output file.
	 * @param the prefix to be writent o the output file
	 * @param an item to be appended to the prefix
	 * @param sumIutils the fuzzy of the prefix concatenated with the item
	 * @param prefixLength the prefix length
	 */
	private void writeOut(int[] prefix, int prefixLength, int item, float sumIutils) throws IOException {
		MFFICount++; // increase the number of high fuzzy itemsets found
		
		//Create a string buffer
		StringBuilder buffer = new StringBuilder();
		// append the prefix
		for (int i = 0; i < prefixLength; i++) {
			if(prefix[i] % 10000 == 1234){
				buffer.append(prefix[i]/10000+".H");
			}else if(prefix[i] % 1000 == 123){
				buffer.append(prefix[i]/1000+".M");
			}if(prefix[i] %100 == 12){
				buffer.append(prefix[i]/100+".L");
			}
			buffer.append(' ');
		}
		// append the last item
		if(item % 10000 == 1234){
			buffer.append(item/10000+".H");
		}else if(item % 1000 == 123){
			buffer.append(item/1000+".M");
		}if(item %100 == 12){
			buffer.append(item/100+".L");
		}
		// append the fuzzy value
		buffer.append(" #SUP: ");
		buffer.append(sumIutils);
//		System.out.println(buffer);
		// write to file
		writer.write(buffer.toString());
		writer.newLine();
	}
	
	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  MFFI-MINER ALGORITHM - STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println(" MFFI count : " + MFFICount); 
		System.out.println(" Join count : " + joinCount); 
		System.out.println("===================================================");
	}
}