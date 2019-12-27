package ca.pfv.spmf.algorithms.frequentpatterns.chud;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/* This file is copyright (c) 2011 Cheng-Wei-Wu, Philippe Fournier-Viger
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

/**
 * This class implements the Phase 1 of the CHUD algorithm for closed high utility itemset mining.
 * The CHUD algorithm was published in the following paper: <br/><br/>
 * 
 * Wu, C.-W., Fournier-Viger, P., Yu., P. S., Tseng, V. S. (2011). Efficient Mining of a 
 * Concise and Lossless Representation of High Utility Itemsets. Proceedings of the 11th 
 * IEEE Intern. Conference on Data Mining (ICDM 2011). IEEE CS Press, pp.824-833.
 * 
 * @see AlgoCHUD
 */
class AlgoCHUD_Phase1 {
	/** the number of transactions */
	protected int tidCount =0;
	
	/** the largest item in this database */
	protected int maxItem =0;
	
	/** the number of items */
	protected int itemCount =0;
	
	/** the vertical database in memory */
	Map<Integer, Set<Integer>> database = null; 
	
	/** object to write result to file */
	BufferedWriter writer = null; 
	
	/** the minimum utility threshold */
	private int minUtility;
	
	/** the number of closed huis */
	protected int closedCount =0;
	
	/** the total execution time */
	protected long totaltime =0;   
	
	/** the maximum memory usage */
	protected double maxMemory = 0; 
	   
	/** if true, the strategy of removing unpromising items is activated*/
	boolean useStrategy1 = true;  
	/** if true, the strategy "removing items that has been processed and thier utilities from golbal utiltiy table" is activated */
	boolean useStrategy2 = true; 
	/** if true, the strategy " removing MIU (Minimum item utility) from the local table" is activated */
	boolean useStrategy3 = true; 
	/** if true, the strategy " Discard candidates whose MAU is smaller than min_utility " is activated */
	boolean useStrategy4 = true;
	
	/** input file path */
	String filePathInput1;
	
	/** constructor */
	public AlgoCHUD_Phase1() {
	}

	public void runAlgorithm(int minUtility, String filePathInput1, String filePathInput2,	
			String filePathInput3, String output1) throws IOException{
		
		this.filePathInput1 = filePathInput1;
		
		this.minUtility = minUtility;
		totaltime = System.currentTimeMillis();
		maxMemory = 0;

		database = new HashMap<Integer, Set<Integer>>();
		//------------------------------------------------
		// (A) FIRST WE READ THE INFORMATION OF FILE 3
		BufferedReader readerFile3 = new BufferedReader(new FileReader(filePathInput3));
		String line3 = readerFile3.readLine();
		tidCount = Integer.parseInt(line3);  // number of transaction in database
		line3 = readerFile3.readLine();
		maxItem = Integer.parseInt(line3);  // the maximum item id in the database
		line3 = readerFile3.readLine();
		itemCount = Integer.parseInt(line3);  // the number of different items in the database
		readerFile3.close();
		
		// ---------------------------------------------------
		// (B) WE SCAN FILE 2 CONTAINING THE TID OF EACH TRANSACTION AND ITS TRANSACTION UTILITY.
		// table: key= tid   value = transaction utility
		int[] tableTU = new int[tidCount];
		BufferedReader readerFile2 = new BufferedReader(new FileReader(filePathInput2));
		String line;
		while( ((line = readerFile2.readLine())!= null)){
			String[] lineSplited = line.split(":");
			tableTU[Integer.parseInt(lineSplited[0])] = Integer.parseInt(lineSplited[1]);
		}
		readerFile2.close();
		
		// items appearing in Level1
		Set<Integer> allItems = new HashSet<Integer>(itemCount);  ////////  <----
		Set<Integer> promisingItems = new HashSet<Integer>(itemCount);  ////////  <----
		
		int [] tableMax = new int[maxItem+1];
		int [] tableMin = new int[maxItem+1];
		 
		// (C) WE SCAN THE VERTICAL DATABASE (FILE 1) to calculate the exact utility of each item
		// and create each candidate of size 1 (with its tidset).
		String line2;
		BufferedReader readerFile1 = new BufferedReader(new FileReader(filePathInput1));
		while( ((line2 = readerFile1.readLine())!= null)){
			String[] lineSplited = line2.split(":"); // item: tids: list of utilities
			int item = Integer.parseInt(lineSplited[0]);

			allItems.add(item);  ////////  <----

			String[] tidsList = lineSplited[1].split(" ");
			String[] itemTidsUtilities = lineSplited[2].split(" ");
			int itemExactUtility = 0;
			int max=0;
			int min=Integer.MAX_VALUE;
			for(String utilityString : itemTidsUtilities){
				int utility = Integer.parseInt(utilityString);
				itemExactUtility += utility;
				if(utility > max){
					max = utility;
				}
				if(utility < min){
					min = utility;
				}
			}
			tableMax[item] = max;
			tableMin[item] = min;
			
			// calculate estimated utility
			int itemEstimatedUtility = 0;
			for(String tidString : tidsList){
				int tid = Integer.parseInt(tidString);
				itemEstimatedUtility += tableTU[tid];
			}
			
			// if ESTIMATED utility is enough, note that this item is promising
			if(useStrategy1 == false || itemEstimatedUtility >= minUtility){
				Set<Integer> tidset = new HashSet<Integer>();
				for(String tid : tidsList){
					tidset.add(Integer.parseInt(tid));
				}
				database.put(item, tidset);
				promisingItems.add(item);
			}
		}
		readerFile1.close();
		
		// (D) We recalculate TWU with only items of size 1 that have minimum estimated utility
		// For each items that has disapeared from the level K2 but was in level K1, 
		// we remove its utility from the TWU of each transaction.
		recalculateTU(allItems, promisingItems, filePathInput1, tableTU);
		
		// --------------------------
//		System.out.println("Running PHASE 1 of CHUD");
		
		writer = new BufferedWriter(new FileWriter(output1)); 
 
		// (E) INITIALIZE VARIABLES FOR THE FIRST CALL TO THE "DCI_CLOSED" PROCEDURE
		List<Integer> closedset = new ArrayList<Integer>();
		Set<Integer> closedsetTIDs = new HashSet<Integer>();
		List<Integer> preset = new ArrayList<Integer>();
		
		// create postset and sort it by descending order or support.
	 	List<Integer> postset = new ArrayList<Integer>(promisingItems.size());
		for(Integer item : promisingItems){
			postset.add(item);
		}
		
		// sort items by support ascending order.  // TODO: ORDER BY UTILITY?
		Collections.sort(postset, new Comparator<Integer>(){
			public int compare(Integer item1, Integer item2) {
				int size1 = database.get(item1).size();
				int size2 = database.get(item2).size();
				if(size1 == size2){
					return (item1 < item2) ? -1 : 1; // use lexicographical order if support is the same
				}
				return size1 - size2;
			}
		});
		
		// (3) CALL THE "DCI_CLOSED" RECURSIVE PROCEDURE
		chud_phase1(true, closedset, closedsetTIDs, postset, preset, tableTU, tableMin, tableMax, 0);
		
//		printStatistics();
		// close the file
		writer.close();
		
	}

	private void printStatistics() {
		// print statistics
		totaltime = (System.currentTimeMillis() - totaltime);
		System.out.println("========== PHASE 1 - STATS ============");
		System.out.println(" Number of transactions: " + tidCount );
		System.out.println(" Number of frequent closed itemsets: " + closedCount );
		System.out.println(" Total time ~: " + totaltime + " ms");
	}
	
	private void recalculateTU(Set<Integer> itemsK1, Set<Integer> itemsK2, String filePathInput1, int[] tableTU) throws NumberFormatException, IOException {		
		BufferedReader reader = new BufferedReader(new FileReader(filePathInput1));
		String line;
		while( ((line = reader.readLine())!= null)){
			String[] lineSplited = line.split(":"); // item: tids: list of utilities
			int item = Integer.parseInt(lineSplited[0]);
			
			// if the item was in level K1 but is not in K2 : 
			if(itemsK1.contains(item) && !itemsK2.contains(item)){
				String[] tidsList = lineSplited[1].split(" ");
				String[] itemTidsUtilities = lineSplited[2].split(" ");
				for(int i=0; i< tidsList.length; i++){
					int tid = Integer.parseInt(tidsList[i]);
					tableTU[tid] = tableTU[tid] - Integer.parseInt(itemTidsUtilities[i]);
				}
			}
		}
		reader.close();
	}
	
	/**
	 * The method "DCI_CLOSED" as described in the paper.
	 * @param tableMax 
	 * @param tableMin 
	 * @param level 
	 * @param mapTidTU:  LOCAL TABLE OF TRANSACTION UTILITIES
	 */
	private void chud_phase1(boolean firstTime, List<Integer> closedset, Set<Integer> closedsetTIDs, 
			List<Integer> postset, List<Integer> preset, int[] tableTU, int[] tableMin, int[] tableMax, int level) throws IOException {
		
		//L2: for all i in postset
		for(Integer i : postset){
			// L4 check the tidset of newgen
			Set<Integer> newgenTIDs;
			if(firstTime){
				newgenTIDs = database.get(i);
			}else{
				newgenTIDs = intersectTIDset(closedsetTIDs, database.get(i));
			}
			
			// ###### Compute estimated utility of the new itemset by using the local table. #####
			int twu =0;
			for(Integer tid : newgenTIDs){
				twu += tableTU[tid];
			}
			
			if(twu >= minUtility){  // #####  if the estimated utility is enough #####
				// L3: newgen = closedset U {i}
				List<Integer> newgen = new ArrayList<Integer>(closedset.size()+1);
				newgen.addAll(closedset);
				newgen.add(i);
				
				// L5:
				if(is_dup(newgenTIDs, preset) == false){
					// L6: ClosedsetNew = newGen
					List<Integer> closedsetNew = new ArrayList<Integer>();
					closedsetNew.addAll(newgen);
					// calculate tidset
					Set<Integer> closedsetNewTIDs = new HashSet<Integer>();
					if(firstTime){
						closedsetNewTIDs = database.get(i);
					}else{
						closedsetNewTIDs.addAll(newgenTIDs);
					}
					
					// L7 : PostsetNew = emptyset
					List<Integer> postsetNew = new ArrayList<Integer>();
					// L8 for each j in Postset such that i _ j : 
					for(Integer j : postset){
						if(smallerAccordingToTotalOrder(i, j)){
							// L9
							if(database.get(j).containsAll(newgenTIDs)){
								closedsetNew.add(j);
								// recalculate TIDS of closedsetNEW by intersection
								Set<Integer> jTIDs = database.get(j);
								Iterator<Integer> iter = closedsetNewTIDs.iterator();
								while(iter.hasNext()){
									Integer tid = iter.next();
									if(jTIDs.contains(tid) == false){
										iter.remove();
									}
								}
							}else{
								postsetNew.add(j);
							}
						}
					}
					
					
					
					// L16: recursive call
					// we make a copy of preset before the recursive call
					List<Integer> presetNew = new ArrayList<Integer>(preset);
					// ###################### MAKE A COPY OF tableTU #####################
					int[] tableTUnew = new int[tidCount];
					System.arraycopy(tableTU, 0, tableTUnew, 0, tableTU.length);
					
					// ###  RECALCULATE MIN AND MAX TABLES
					// for each tid containing the item, update the min max
//					for(int tid: database.get(i)){
//						
//					}
					
					chud_phase1(false, closedsetNew, closedsetNewTIDs, postsetNew, presetNew, tableTUnew, tableMin, tableMax, level+1);
					
					// L15 : write out Closed_setNew and its support
					// ###### Compute estimated utility of the new itemset by using the local table. #####
					twu =0;
					for(Integer tid : newgenTIDs){
						twu += tableTU[tid];
					}
					
					// ###############   WRITE TO FILE ONLY IF MAXIMUM UTIL. IS ENOUGH...
					if(twu >= minUtility){
						int maxUtility = 0;
						for(Integer item : closedsetNew){
							maxUtility += tableMax[item];
						}
						if(useStrategy4 == false || ((maxUtility * closedsetNewTIDs.size()) >= minUtility)){
							writeOut(closedsetNew, closedsetNewTIDs);
						}
					}
					
					
					// L17 : Preset = Preset U {i}
					preset.add(i);
				}
			}	
			
			
			if(firstTime && useStrategy3){
				// read the file
				String line2;
				BufferedReader readerFile1 = new BufferedReader(new FileReader(filePathInput1));
				while( ((line2 = readerFile1.readLine())!= null)){
					
					String[] lineSplited = line2.split(":"); // item: tids: list of utilities
					int item = Integer.parseInt(lineSplited[0]);
					if(item == i){
						String [] tids = lineSplited[1].split(" ");
						String [] utilities = lineSplited[2].split(" ");
						
						for(int k=0; k< tids.length; k++){		
							Integer tidInt = Integer.valueOf(tids[k]);
							Integer utility = Integer.valueOf(utilities[k]);
							tableTU[tidInt] = tableTU[tidInt] - utility;
						}
						break;
						
					}					
				}
				readerFile1.close();
			}
			// STRATEGY RML: removing MIU (Minimum item utility) from the local table 
			// ### REMOVE MIN ITEM UTILITY OF THE CURRENT ELEMENT OF POSTSET
			else if(useStrategy4){
				for(Integer tid : newgenTIDs ){ //  $$$$$$ database.get(i)
					tableTU[tid] = tableTU[tid] - tableMin[i];
				}
			}
		}
	}

	/**
	 * Check if an item is smaller than another according to the support ascending order
	 * or if the support is the same, use the lexicographical order.
	 */
	private boolean smallerAccordingToTotalOrder(Integer i, Integer j) {
		int size1 = database.get(i).size();
		int size2 = database.get(j).size();
		if(size1 == size2){
			return (i < j) ? true : false;
		}
		return size2 - size1 >0;
	}

	/**
	 * Write a frequent closed itemset that is found to the output file.
	 */
	private void writeOut(List<Integer> closedset, Set<Integer> tids) throws IOException {
		closedCount++;
		StringBuilder buffer = new StringBuilder();
		// WRITE ITEMS
		Iterator<Integer> iterItem = closedset.iterator();
		while(iterItem.hasNext()){
			buffer.append(iterItem.next());
			if(iterItem.hasNext()){
				buffer.append(' ');
			}else{
				break;
			}
		}
		// ############### Write the tids ##############
		buffer.append(':');
		// WRITE SUPPORT
		Iterator<Integer> iterTID = tids.iterator();
		while(iterTID.hasNext()){
			buffer.append(iterTID.next());
			if(iterTID.hasNext()){
				buffer.append(' ');
			}else{
				break;
			}
		}
		writer.write(buffer.toString());
		writer.newLine();
	}

	/**
	 * The method "is_dup" as described in the paper.
	 */
	private boolean is_dup(Set<Integer> newgenTIDs, List<Integer> preset) {
		// L25
		for(Integer j : preset){
			// L26 :  check if tidset of newgen is included in tids of j
			if(database.get(j).containsAll(newgenTIDs)){
				return true; 
			}
		}
		return false; 
	}

	/**
	 * Method to perform the intersection of two tid sets
	 * @param tidset1 the first tid set
	 * @param tidset2 the second tid set
	 * @return the resulting tid set
	 */
	private Set<Integer> intersectTIDset(Set<Integer> tidset1,
			Set<Integer> tidset2) {
		Set<Integer> tidset = new HashSet<Integer>();
		if(tidset1.size() > tidset2.size()){
			for(Integer tid : tidset2){
				if(tidset1.contains(tid)){
					tidset.add(tid);
				}
			}
		}else{
			for(Integer tid : tidset1){
				if(tidset2.contains(tid)){
					tidset.add(tid);
				}
			}
		}
		return tidset;
	}
	
	/**
	 * Method to check the maximum memory usage of that algorithm
	 */
	private void checkMemory() {
		double currentMemory = (Runtime.getRuntime().totalMemory() -  Runtime.getRuntime().freeMemory())
				/ 1024d / 1024d;
		if (currentMemory > maxMemory) {
			maxMemory = currentMemory;
		}
	}

}
