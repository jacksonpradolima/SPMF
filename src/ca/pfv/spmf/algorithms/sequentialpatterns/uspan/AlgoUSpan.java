package ca.pfv.spmf.algorithms.sequentialpatterns.uspan;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This is an implementation of the "USPAN" algorithm for High-Utility Sequential Pattern Mining
 * as described in the conference paper : <br/><br/>
 * 
 *  Yin, Junfu, Zhigang Zheng, and Longbing Cao. "USpan: an efficient algorithm for 
 *  mining high utility sequential patterns." Proceedings of the 18th ACM SIGKDD 
 *  international conference on Knowledge discovery and data mining. ACM, 2012.
 *  
 *  This implementation tries to be as faithful as possible to the article.
 *
 * @see QMatrix
 * @see QMatrixProjection
 * @see MatrixProjection
 * 
 * @author Philippe Fournier-Viger, 2015
 */
public class AlgoUSpan {

	/** the time the algorithm started */
	long startTimestamp = 0; 
	/** the time the algorithm terminated */
	long endTimestamp = 0;  
	/** the number of patterns generated */
	int patternCount = 0;  
	
	/** writer to write the output file **/
	BufferedWriter writer = null;  
	
	/** buffer for storing the current pattern that is mined when performing mining
	* the idea is to always reuse the same buffer to reduce memory usage. **/
	final int BUFFERS_SIZE = 2000;
	private int[] patternBuffer = null;
	
	/** if true, debugging information will be shown in the console */
	final boolean DEBUG = false;
	
	/** if true, save result to file in a format that is easier to read by humans **/
	final boolean SAVE_RESULT_EASIER_TO_READ_FORMAT = false;

	/** the minUtility threshold **/
	int minUtility = 0;
	
	/** max pattern length **/
	int maxPatternLength = 1000;
	
	/** the input file path **/
	String input;

	/**
	 * Default constructor
	 */
	public AlgoUSpan() {
		
	}

	/**
	 * Run the USPAN algorithm
	 * @param input the input file path
	 * @param output the output file path
	 * @param minUtility the minimum utility threshold
	 * @throws IOException exception if error while writing the file
	 */
	public void runAlgorithm(String input, String output, int minUtility) throws IOException {
		// reset maximum
		MemoryLogger.getInstance().reset();
		
		// input path
		this.input = input;
		
		// initialize the buffer for storing the current itemset
		patternBuffer = new int[BUFFERS_SIZE];
		
		// record the start time of the algorithm
		startTimestamp = System.currentTimeMillis();
		
		// create a writer object to write results to file
		writer = new BufferedWriter(new FileWriter(output));
		
		// save the minimum utility threshold
		this.minUtility = minUtility;

		// create a map to store the SWU of each item
		// key: item  value: the swu of the item
		final Map<Integer, Integer> mapItemToSWU = new HashMap<Integer, Integer>();
		
		//Tin added:		
		List<Integer> consideredItems = new ArrayList<Integer>();

		// ==========  FIRST DATABASE SCAN TO IDENTIFY PROMISING ITEMS =========
		// We scan the database a first time to calculate the SWU of each item.
		int sequenceCount = 0;
		BufferedReader myInput = null;
		String thisLine;
		try {
			// prepare the object for reading the file
			myInput = new BufferedReader(new InputStreamReader( new FileInputStream(new File(input))));
			// for each line (transaction) until the end of file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is a comment, is  empty or is a kind of metadata, skip it
				if (thisLine.isEmpty() == true || thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%' || thisLine.charAt(0) == '@') {
					continue;
				}
				
				// split the transaction according to the " " separator
				String tokens[] = thisLine.split(" "); 
				
				// get the sequence utility (the last token on the line)
				String sequenceUtilityString = tokens[tokens.length-1];
				int positionColons = sequenceUtilityString.indexOf(':');
				int sequenceUtility = Integer.parseInt(sequenceUtilityString.substring(positionColons+1));
				
				// Then read each token from this sequence (except the last three tokens
				// which are -1 -2 and the sequence utility)
				for(int i=0; i< tokens.length -3; i++) {
					String currentToken = tokens[i];
					// if the current token is not -1 
					if(currentToken.length() !=0 && currentToken.charAt(0) != '-') {
						// find the left brack
						int positionLeftBracketString = currentToken.indexOf('[');
						// get the item
						String itemString = currentToken.substring(0, positionLeftBracketString);
						Integer item = Integer.parseInt(itemString);
						
						//Tin added:		
						if (!consideredItems.contains(item)) 
						{
							consideredItems.add(item);
						
							// get the current SWU of that item
							Integer swu = mapItemToSWU.get(item);
							
							// add the utility of sequence utility to the swu of this item
							swu = (swu == null)?  sequenceUtility : swu + sequenceUtility;
							mapItemToSWU.put(item, swu);
//							System.out.println(" -> " + swu);
						}
//						else System.out.println(item + " is already considered in the same (line) input sequence");								
					}
				}
				
				// Philippe added:
				consideredItems.clear();
				
				// increase sequence count
				sequenceCount++;
			}
		} catch (Exception e) {
			// catches exception if error while reading the input file
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
	    }
		
		// If we are in debug mode, we will show the number of distinct items in the database,
		// the number of sequences and the SWU of each item
		if(DEBUG) {
			System.out.println("INITIAL ITEM COUNT " + mapItemToSWU.size());
			System.out.println("SEQUENCE COUNT = " + sequenceCount);
			System.out.println("INITIAL SWU OF ITEMS");
			for(Entry<Integer,Integer> entry : mapItemToSWU.entrySet()) {
				System.out.println("Item: " + entry.getKey() + " swu: " + entry.getValue());
			}
		}
		
		//================  SECOND DATABASE SCAN ===================
		// Read the database again to create the QMatrix for each sequence
		List<QMatrix> database  = new ArrayList<QMatrix>(sequenceCount);
		
		try {
			// prepare the object for reading the file
			myInput = new BufferedReader(new InputStreamReader( new FileInputStream(new File(input))));
			
			// We will read each sequence in buffers.
			// The first buffer will store the items of a sequence and the -1 between them)
			int[] itemBuffer = new int[BUFFERS_SIZE];
			// The second buffer will store the utility of items in a sequence and the -1 between them)
			int[] utilityBuffer = new int[BUFFERS_SIZE];
			// The following variable will contain the length of the data stored in the two previous buffer
			int itemBufferLength;
			// Finally, we create another buffer for storing the items from a sequence without
			// the -1. This is just used so that we can collect the list of items in that sequence
			// efficiently. We will use this information later to create the number of rows in the
			// QMatrix for that sequence.
			int[] itemsSequenceBuffer = new int[BUFFERS_SIZE];
			// The following variable will contain the length of the data stored in the previous buffer
			int itemsLength;
			
			// for each line (transaction) until the end of file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is  a comment, is  empty or is a kind of metadata
				if (thisLine.isEmpty() == true || thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%' || thisLine.charAt(0) == '@') {
					continue;
				}
				
				// We reset the two following buffer length to zero because
				// we are reading a new sequence.
				itemBufferLength = 0;
				itemsLength = 0;
				
				// split the sequence according to the " " separator
				String tokens[] = thisLine.split(" "); 
				
				// get the sequence utility (the last token on the line)
				String sequenceUtilityString = tokens[tokens.length-1];
				int positionColons = sequenceUtilityString.indexOf(':');
				int sequenceUtility = Integer.parseInt(sequenceUtilityString.substring(positionColons+1));

				// This variable will count the number of itemsets
				int nbItemsets = 1;
				// This variable will be used to remember if an itemset contains at least a promising item
				// (otherwise, the itemset will be empty).
				boolean currentItemsetHasAPromisingItem = false;
				
				// Copy the current sequence in the sequence buffer.
				// For each token on the line except the last three tokens
				// (the -1 -2 and sequence utility).
				for(int i=0; i< tokens.length -3; i++) {
					String currentToken = tokens[i];
					
					// if empty, continue to next token
					if(currentToken.length() == 0) {
						continue;
					}
					
					// if the current token is -1
					if(currentToken.equals("-1")) {
						// It means that it is the end of an itemset.
						// So we check if there was a promising item in that itemset
						if(currentItemsetHasAPromisingItem) {
							// If yes, then we keep the -1, because
							// that itemset will not be empty.
							
							// We store the -1 in the respective buffers 
							itemBuffer[itemBufferLength] = -1;
							utilityBuffer[itemBufferLength] = -1;
							// We increase the length of the data stored in the buffers
							itemBufferLength++;

							// we update the number of itemsets in that sequence that are not empty
							nbItemsets++;
							// we reset the following variable for the next itemset that 
							// we will read after this one (if there is one)
							currentItemsetHasAPromisingItem = false;
						}
					}else {
						// if  the current token is an item
						//  We will extract the item from the string:
						int positionLeftBracketString = currentToken.indexOf('[');
						int positionRightBracketString = currentToken.indexOf(']');
						String itemString = currentToken.substring(0, positionLeftBracketString);
						Integer item = Integer.parseInt(itemString);
						
						// We also extract the utility from the string:
						String utilityString = currentToken.substring(positionLeftBracketString+1, positionRightBracketString);
						Integer itemUtility = Integer.parseInt(utilityString);
						
						// it the item is promising (its SWU >= minutility), then
						// we keep it in the sequence
						if(mapItemToSWU.get(item) >= minUtility) {
							// We remember that this itemset contains a promising item
							currentItemsetHasAPromisingItem = true;
							
							// We store the item and its utility in the buffers
							// for temporarily storing the sequence
							itemBuffer[itemBufferLength] = item;
							utilityBuffer[itemBufferLength] = itemUtility;
							itemBufferLength++;
							
							// We also put this item in the buffer for all items of this sequence
							itemsSequenceBuffer[itemsLength++] = item;
						}else {
							// if the item is not promising, we subtract its utility 
							// from the sequence utility, and we do not add it to the buffers
							// because this item will not be part of a high utility sequential pattern.
							sequenceUtility -= itemUtility;
						}
					}
				}
				
				// If the sequence utility is now zero, which means that the sequence
				// is empty after removing unpromising items, we don't keep it
				if(sequenceUtility == 0) {
					continue;
				}

				// If we are in debug mode,  
				if(DEBUG) {
					// We will show the original sequence
					System.out.println("SEQUENCE BEFORE REMOVING UNPROMISING ITEMS:\n");
					System.out.println(" " + thisLine);
					// We will show the sequence after removing unpromising items
					System.out.print("SEQUENCE AFTER REMOVING UNPROMISING ITEMS:\n ");
					for(int i=0; i< itemBufferLength; i++) {
						System.out.print(itemBuffer[i] + "[" + utilityBuffer[i] + "] ");
					}
					// And we will thow the sequence utility after removing the unpromising items.
					System.out.println("NEW SEQUENCE UTILITY " + sequenceUtility);
				}

				// Now, we sort the buffer for storing all items from the current sequence
				// in alphabetical order
				Arrays.sort(itemsSequenceBuffer,0, itemsLength);
				// but an item may appear multiple times in that buffer so we will
				// loop over the buffer to remove duplicates
				// This variable remember the last insertion position in the buffer:
				int newItemsPos = 0;
				// This variable remember the last item read in that buffer
				int lastItemSeen = -999;
				// for each position in that buffer
				for(int i=0; i< itemsLength; i++) {
					// get the item
					int item = itemsSequenceBuffer[i];
					// if the item was not seen previously
					if(item != lastItemSeen) {
						// we copy it at the current insertion position
						itemsSequenceBuffer[newItemsPos++] = item;
						// we remember this item as the last seen item
						lastItemSeen = item;
					}
				}
				
				// If we are in debugging mode
				if(DEBUG) {
					// We will print the list of promising items from the sequence,
					// sorted in alphabetical order:
					System.out.print("LIST OF PROMISING ITEMS IN THAT SEQUENCE:\n ");
					for(int i=0; i< newItemsPos; i++) {
						System.out.print(itemsSequenceBuffer[i] + " ");
					}
					System.out.println();
				}
				
				// New we count the number of items in that sequence
				int nbItems = newItemsPos;
				
				// And we will create the Qmatrix for that sequence
				QMatrix matrix = new QMatrix(nbItems, nbItemsets, itemsSequenceBuffer, newItemsPos, sequenceUtility);
				// We add the QMatrix to the initial sequence database.
				database.add(matrix);

				// Next we will fill the matrix column by column
				// This variable will represent the position in the sequence
				int posBuffer = 0;
				// for each itemset (column)
				for(int itemset=0; itemset < nbItemsets; itemset++) {
					// This variable represent the position in the list of items in the QMatrix
					int posNames = 0;
					// While we did not reach the end of the sequence
					while(posBuffer < itemBufferLength) {
						// Get the item at the current position in the sequence
						int item = itemBuffer[posBuffer];
						// if it is an itemset separator, we move to next position in the sequence
						if(item == -1) {
							posBuffer++;
							break;
						}
						// else if it is the item that correspond to the next row in the matrix
						else if(item == matrix.itemNames[posNames]) {
							// calculate the utility for this item/itemset cell in the matrix
							int utility = utilityBuffer[posBuffer];
							// We update the reamining utility by subtracting the utility of the
							// current item/itemset
							sequenceUtility -= utility;
							// update the cell in the matrix
							matrix.registerItem(posNames, itemset, utility, sequenceUtility);
							// move to the next item in the matrix and in the sequence
							posNames++;
							posBuffer++;
						}else if(item > matrix.itemNames[posNames]) {
							// if the next item in the sequence is larger than the current row in the matrix
							// it means that the item do not appear in that itemset, so we put a utility of 0
							// for that item and move to the next row in the matrix.
							matrix.registerItem(posNames, itemset, 0, sequenceUtility);
							posNames++;
						}else { 
							// Otherwise, we put a utility of 0 for the current row in the matrix and move
							// to the next item in the sequence
							matrix.registerItem(posNames, itemset, 0, sequenceUtility);
							posBuffer++;
						}
					}
				}
				
				// if in debug mode, we print the q-matrix that we have just built
				if(DEBUG) {
					System.out.println(matrix.toString());
					System.out.println();
				}
			}
		} catch (Exception e) {
			// catches exception if error while reading the input file
			e.printStackTrace();
		}finally {
			if(myInput != null){
				// close the input file
				myInput.close();
			}
	    }
		
		// check the memory usage
		MemoryLogger.getInstance().checkMemory();

		// Mine the database recursively using the USpan procedure
		// This procedure is the USPan procedure optimized for the first recursion
		uspanFirstTime(patternBuffer, 0, database);
		
		// check the memory usage again and close the file.
		MemoryLogger.getInstance().checkMemory();
		// close output file
		writer.close();
		// record end time
		endTimestamp = System.currentTimeMillis();
	}

	
	/**
	 * This is the initial call to the USpan procedure to find all High utility sequential patterns
	 * of length 1. It is optimized for finding patterns of length 1. 
	 * To find larger patterns the "uspan" method is then used recursively.
	 * @param prefix  This is the buffer for storing the current prefix. Initially, it is empty.
	 * @param prefixLength The current prefix length. Initially, it is zero.
	 * @param database This is the original sequence database (as a set of QMatrix)
	 * @throws IOException If an error occurs while reading/writting to file.
	 */
	private void uspanFirstTime(int[] prefix, int prefixLength, List<QMatrix> database) throws IOException {
		
		// For the first call to USpan, we only need to check I-CONCATENATIONS
		// =======================  I-CONCATENATIONS  ===========================/
		// scan the projected database to
		// calculate the SWU of each item
		Map<Integer,Integer> mapItemSWU = new HashMap<Integer,Integer>();
		for(QMatrix  qmatrix : database) {
			// for each row (item) we will update the swu of the corresponding item
			for(int item : qmatrix.itemNames) {
				// get its swu
				Integer currentSWU = mapItemSWU.get(item);
				// update its swu
				if(currentSWU == null) {
					mapItemSWU.put(item, qmatrix.swu);
				}else {
					mapItemSWU.put(item, currentSWU + qmatrix.swu);
				}
			}
		}

		// For each item 
		for(Entry<Integer,Integer> entry: mapItemSWU.entrySet()) {
			Integer itemSWU = entry.getValue();
			// if the item is promising
			if(itemSWU >= minUtility) {
				// We get the item
				int item = entry.getKey();
				// We initialize two variables for calculating the total utility and remaining utility
				// of that item
				int totalUtility = 0;
				int totalRemainingUtility = 0;

				// We also initialize a variable to remember the projected qmatrixes of sequences
				// where this item appears. This will be used for call to the recursive
				// "uspan" method later.
				List<QMatrixProjection> matrixProjections = new ArrayList<QMatrixProjection>();
				// For each sequence
				for(QMatrix qmatrix : database){
					
					// if the item appear in that sequence (in that qmatrix)
					int row = Arrays.binarySearch(qmatrix.itemNames, item);
					if(row >=0) {

						// create a list to store the positions (itemsets) where this item 
						// appear in that sequence
						List<MatrixPosition> positions = new ArrayList<MatrixPosition>();
						
						// find the max utility of this item in that sequence
						// and the max remaining utility
						int maxUtility = 0;
						int maxRemainingUtility = 0;
						// for each itemset in that sequence
						for(int itemset=0; itemset < qmatrix.matrixItemRemainingUtility[row].length; itemset++) {
							// get the utility of the item in that itemset
							int utility = qmatrix.matrixItemUtility[row][itemset];
							// if the utility is higher than 0
							if(utility >0) {
								// record this position
								positions.add(new MatrixPosition(row,itemset, utility));
								
								// if it is the maximum utility until now
								if(utility > maxUtility) {
									// record it as the maximum utility until now
									maxUtility = utility;
									
									// get the remaining utility at that position
									int remaining = qmatrix.matrixItemRemainingUtility[row][itemset];
									// If it is the first occurrence of this item
									// we remember the remaining utility as the max remaining utility
									if(remaining > 0 && maxRemainingUtility == 0) {
										maxRemainingUtility = remaining;
									}
								}
							}
						}
						
						// update the total utility and total remaining utility for all sequences 
						// until now by adding the utility and remaining utility of the current
						// sequence
						totalUtility += maxUtility;
						totalRemainingUtility += maxRemainingUtility;
						
						// create the projected matrix for the current sequence
						QMatrixProjection projection = new QMatrixProjection(qmatrix, positions);
						// add the projected matrix to the current database projection with this item
						matrixProjections.add(projection);
					}
				}
				
				// create the pattern consisting of this item
				// by appending the item to the prefix in the buffer, which is empty
				prefix[0] = item;
				// if the pattern is high utility, then output it
				if(totalUtility >= minUtility) {
					writeOut(prefix,1, totalUtility);
				}
//				//Tin checks:
//				if(itemSWU < totalUtility + totalRemainingUtility) {
//					System.out.println("swu(" + item + ") = " + itemSWU + " < SPU(" + item + ") = " + (totalUtility + totalRemainingUtility));
//					System.in.read();
//				}

				// if this item passes the depth pruning (remaining utility + totality >= minutil)
				if(totalUtility + totalRemainingUtility >= minUtility) {

					//Then, we recursively call the procedure uspan for growing this pattern and
					// try to find larger high utility sequential patterns
					if(1 < maxPatternLength) {
						uspan(prefix, 1, matrixProjections, 1);
					}
				}
			}
		}
		// we check the memory usage.
		MemoryLogger.getInstance().checkMemory();
	}


	/**
	 * This inner class is used to store the SWU of an item and the last sequence where it was seen.
	 * It is used in the uspan() method.
	 * 
	 * @author Philippe Fournier-Viger, 2015
	 *
	 */
	private class Pair{
		/** the total SWU of an item */
		int swu;
		/** the last sequence where the item was seen  (represented by a projected QMatrix)*/
		QMatrixProjection lastSID;
	}
	
	/**
	 * This is the general USpan procedure to find all High utility sequential patterns of length
	 * greater than 1. 
	 * @param prefix  This is the buffer for storing the current prefix.
	 * @param prefixLength The current prefix length. 
	 * @param database This is a projected sequence database (a set of projected QMatrixes)
	 * @param itemCount the number of items in the prefix
	 * @throws IOException If an error occurs while reading/writting to file.
	 */
	private void uspan(int[] prefix, int prefixLength, List<QMatrixProjection> projectedDatabase, int itemCount) throws IOException {
		if(DEBUG){
			// Print the current prefix
			for(int i=0; i< prefixLength; i++){
				System.out.print(prefix[i] + " ");
			}
			System.out.println();
			System.out.println();
		}
		
		// =======================  I-CONCATENATIONS  ===========================/
		// We first try to perform I-Concatenations to grow the pattern larger.
		// We scan the projected database to calculated the SWU of each item that could
		// be concatenated to the prefix.
		// The following map will store for each item, their SWU (key: item  value: swu)
		Map<Integer,Pair> mapItemSWU = new HashMap<Integer,Pair>();
		// For each sequence in the projected database
		for(QMatrixProjection  qmatrix : projectedDatabase) {
			
			// For each position in that Q-matrix
			for(MatrixPosition position : qmatrix.positions) {
				// Because we are looking for i-concatenation, we will search for items
				// occurring in the same column (itemset) as the current position 
				// but from the next row
				int row = position.row+1;
				int column = position.column;
				
				// The sequence utility for updating the SWU
				// will be the remaining utility at the current position
				int localSequenceUtility = qmatrix.getLocalSequenceUtility(position);
				
				// for each row we will update the local SWU of the corresponding item
				for(; row < qmatrix.getItemNames().length; row++) {
					// get the item for this row
					int item = qmatrix.getItemNames()[row];
					
					// if the item appears in that column
					if(qmatrix.getItemUtility(row, column) > 0) {
						// get its swu until now
						Pair currentSWU = mapItemSWU.get(item);
						// if it is the first time that we see this item
						if(currentSWU == null) {
							// We use a Pair object to store the SWU of the item and the
							// last sequence where the item appears
							Pair pair = new Pair();
							pair.lastSID = qmatrix;
							pair.swu = position.utility + localSequenceUtility;
							// we put this Pair object in the map for storing the SWU of all items
							mapItemSWU.put(item, pair);
						}else if (currentSWU.lastSID != qmatrix){
							// otherwise if it is not the first time that we see this item
							// but it is the first time that we see it in this sequence:
							// We remember this sequence as the last sequence where we have seen this item
							currentSWU.lastSID = qmatrix;
							// We add the local sequence utility to the SWU of the item
							currentSWU.swu += position.utility + localSequenceUtility;
						}else{
							// BUGFIX 2017: if it is the second occurrence
							// of the same item in that sequence and the SWU
							// is higher, we need to update it.
							// Before, we were not doing it, so the utility
							// could be underestimated, and some patterns were missing
							// in some cases.
							int tempSWU = position.utility + localSequenceUtility;
							
							if(tempSWU > currentSWU.swu){
								currentSWU.swu = tempSWU;
							}
							//  END OF BUGFIX
						}
					}
				}
			}
		}
		//  Now that we have calculated the local SWU of each item,
		// We perform a loop on each item and for each promising item we will create
		// the i-concatenation and calculate the utility of the resulting pattern.
		
		// For each item
		for(Entry<Integer,Pair> entry: mapItemSWU.entrySet()) {
			// Get the Pair object that store the calculated SWU for that item
			Pair itemSWU = entry.getValue();
			// if the item is promising (SWU >= minutil)
			if(itemSWU.swu >= minUtility) {
				// get the item
				int item = entry.getKey();
				
				// This variable will be used to calculate this item's utility for the whole database
				int totalUtility = 0; 
				// This variable will be used to calculate this item's remaining utility for the whole database
				int totalRemainingUtility = 0;
				
				// Initialize a variable to store the projected QMatrixes for the i-concatenation
				// of this item to the prefix
				List<QMatrixProjection> matrixProjections = new ArrayList<QMatrixProjection>();
				
				// for each sequence in the projected database
				for(QMatrixProjection qmatrix : projectedDatabase){
					
					// if the item appear in that sequence
					int rowItem = Arrays.binarySearch(qmatrix.getItemNames(), item);
					if(rowItem >=0) {
						
						// We initialize two variables that will be used to calculate the maximum
						// utility and remaining utility for the i-concatenation with this item 
						// in that sequence
						int maxUtility = 0;
						int maxRemainingUtility = 0;
						
						// create a list to store the matrix positions of i-concatenations with
						// this item in that sequence
						List<MatrixPosition> positions = new ArrayList<MatrixPosition>();
						
						// for each position of the prefix
						for(MatrixPosition position : qmatrix.positions) {
							// We will look for this item in the same column (in the same itemset)
							// because we look for a i-concatenation
							int column = position.column;
							
							// we will check if the new item appears in the same itemset
							int newItemUtility = qmatrix.getItemUtility(rowItem, column);
							// if the item appears in that itemset
							if(newItemUtility >0) {
								// calculate the utility of the i-concatenation at this position
								// in that sequence
								int newPrefixUtility = position.utility + newItemUtility;
								// Add this new position and its utility in the list of position
								// for this pattern
								positions.add(new MatrixPosition(rowItem, column, newPrefixUtility));
								
								// If the utility of this new i-concatenation is higher than
								// previous occurrences of that same pattern
								if(newPrefixUtility > maxUtility) {
									// record this utility as the maximum utility until now for that pattern
									maxUtility = newPrefixUtility;
									
									// Get the remaining utility at that position
									int remaining = qmatrix.getRemainingUtility(rowItem, column);
									
									// If it is the first position where this i-concatenation occurs
									// we record its remaining utility as the largest remaining utility
									// for this i-concatenation
									if(remaining > 0 && maxRemainingUtility == 0) {
										maxRemainingUtility = remaining;
									}
								}
							}
						
						}
						
						// update the total utility and total remaining utility for that i-concatenation
						// for all sequences by adding the utility and remaining utility for the
						// current sequence
						totalUtility += maxUtility;
						totalRemainingUtility += maxRemainingUtility;
						
						// create the projected matrix for the current sequence
						QMatrixProjection projection = new QMatrixProjection(qmatrix, positions);
						// Add it to the projected database for that i-concatenation.
						matrixProjections.add(projection);
					}
				}
				
				// create the i-concatenation by appending the item to the prefix in the buffer
				prefix[prefixLength] = item;
				// if the i-concatenation is high utility, then output it
				if(totalUtility >= minUtility) {
					writeOut(prefix,prefixLength+1, totalUtility);
				}
				
//				//Tin checks:
//				if(itemSWU.swu < totalUtility + totalRemainingUtility) {
//					System.out.println("swu(i-ext " + ToString(prefix, prefixLength+1) + "-1 -2) <= swu(" + item + ") = " + itemSWU.swu + " < SPU(i-ext " + ToString(prefix, prefixLength+1) + " -1 -2) = " + (totalUtility + totalRemainingUtility));
//				System.in.read();
//				}
				
				// if his i-concatenation passes the depth pruning (remaining utility + totality)
				if(totalUtility + totalRemainingUtility >= minUtility) {

					// Finally, we recursively call the procedure uspan for growing this pattern
					// to try to find larger patterns
					if(itemCount+1 < maxPatternLength) {
						uspan(prefix, prefixLength+1, matrixProjections, itemCount+1);
					}
					
				}
			}
		}
		
		// =======================  S-CONCATENATIONS  ===========================/
		// We will next look for for S-CONCATENATIONS.
		// We first clear the map for calculating the SWU of items to reuse it instead
		// of creating a new one
		mapItemSWU.clear();
		// Now, we will loop over sequences of the projected database to calculate the local SWU
		// of each item.
		//For each sequence in the projected database
		for(QMatrixProjection  qmatrix : projectedDatabase) {
			
			// For each position where the current prefix appear in the current sequence
			for(MatrixPosition position : qmatrix.positions) {

				// The local sequence utility is the remaining utility at the current position
				int localSequenceUtility = qmatrix.getLocalSequenceUtility(position);
				
				// For each item
				for(int row = 0; row < qmatrix.getItemNames().length; row++) {
					// get the item
					int item = qmatrix.getItemNames()[row];
					
					// We will look for s-concatenations starting from the next itemset (column)
					// For each column
					for(int column = position.column+1; 
							column < qmatrix.originalMatrix.matrixItemUtility[row].length; column++) {
						// if the item appears in that column
						if(qmatrix.getItemUtility(row, column) > 0) {
							// get its swu until now
							Pair currentSWU = mapItemSWU.get(item);
							// if it is 0
							if(currentSWU == null) {
								// We create a Pair object to store the SWU until now of that item
								Pair pair = new Pair();
								// We remember that this sequence is the last seen sequence containing
								// that item
								pair.lastSID = qmatrix;
								// We update the SWU of that item by adding this local sequence utility
								pair.swu = position.utility + localSequenceUtility;
								// We put the pair in the map
								mapItemSWU.put(item, pair);
							}else if (currentSWU.lastSID != qmatrix){
								// Otherwise, if it is not the first time that we see this item
								// but it is the first time that we see it in this sequence:
								// We remember this sequence as the last one that was seen containing
								// this item
								currentSWU.lastSID = qmatrix;
								// We update the SWU of that item by adding this local sequence utility
								currentSWU.swu += position.utility + localSequenceUtility;
							}else{
								// BUGFIX 2017: if it is the second occurrence
								// of the same item in that sequence and the SWU
								// is higher, we need to update it.
								// Before, we were not doing it, so the utility
								// could be underestimated, and some patterns were missing
								// in some cases.
								int tempSWU = position.utility + localSequenceUtility;
								
								if(tempSWU > currentSWU.swu){
									currentSWU.swu = tempSWU;
								}
								//  END OF BUGFIX
							}
							// we don't need to check the other column if we found one column where this item
							// appears after the previous item.
							break;
						}
					}
				}
			}
		}
		
		// Next we will calculate the utility of each s-concatenation for promising 
		// items that can be appended by s-concatenation
		for(Entry<Integer,Pair> entry: mapItemSWU.entrySet()) {
//			System.out.println(entry.getKey() + "  swu: " + entry.getValue().swu);
			// Get the item and its SWU
			Pair itemSWU = entry.getValue();
			// if the item is promising (SWU >= minutil
			if(itemSWU.swu >= minUtility) {

				// get the item
				int item = entry.getKey();

				// This variable is used to store the utility of this s-concatenation in the whole database
				int totalUtility = 0; 

				// This variable is used to store the remaining utility of this 
				// s-concatenation in the whole database
				int totalRemainingUtility = 0;
				
				// Initialize a variable to store position of the last item of this s-concatenation
				List<QMatrixProjection> matrixProjections = new ArrayList<QMatrixProjection>();
				
				// For each sequence of the projected database,
				// we check if the item appears and calculate the maximum utility
				for(QMatrixProjection qmatrix : projectedDatabase){
					
					// if the item appear in that qMatrix
					int rowItem = Arrays.binarySearch(qmatrix.getItemNames(), item);
					if(rowItem >=0) {
						
						// We will calculate the max utility and maximum remaining utility of that
						// s-concatenation with the item
						// This variable will store the maximum utility
						int maxUtility = 0;
						// This variable will store the maximum remaining utility
						int maxRemainingUtility = 0;
						
						// create a list to store the matrix positions of this item in that sequence
						List<MatrixPosition> positions = new ArrayList<MatrixPosition>();
						
						// for each position of the last item of the prefix
						for(MatrixPosition position : qmatrix.positions) {
							
							// We will start searching for the item starting at the next itemset (column)
							// because we are looking for s-concatenations					
							// For each column (itemset) after the last item of the prefix
							for(int column = position.column+1; 
									column < qmatrix.originalMatrix.matrixItemUtility[rowItem].length; column++) {
								
								// we will check if the new item appears in the same itemset
								int newItemUtility = qmatrix.getItemUtility(rowItem, column);
								// if the item appears
								if(newItemUtility >0) {
									// We calculate the utility of the s-concatenation with this item
									int newPrefixUtility = position.utility + newItemUtility;
									// We record the current position as the end of this
									// s-concatenation
									positions.add(new MatrixPosition(rowItem, column, newPrefixUtility));
									
									// if this s-concatenation utility is higher than 
									// what we seen until now for that sequence
									if(newPrefixUtility > maxUtility) {
										// We record the utiltiy of this s concatenation
										maxUtility = newPrefixUtility;
										
										// we get the remaining utility of this s-concatenation
										int remaining = qmatrix.getRemainingUtility(rowItem, column);
										// if it is the first s-concatenation that we have seen
										// we remember the remaining utility
										if(remaining > 0 && maxRemainingUtility == 0) {
											maxRemainingUtility = remaining;
										}
									}
								}	
							}
						}
						
						// Add the  utility and  remaining utility of this s-concatenation in 
						// that sequence to the utility and remaining utility of this s-concatenation
						// for the whole database.
						totalUtility += maxUtility;
						totalRemainingUtility += maxRemainingUtility;
						
						// create the projected matrix for this s-concatenation
						QMatrixProjection projection = new QMatrixProjection(qmatrix, positions);
						// add it to the projected database
						matrixProjections.add(projection);
					}
				}
				
				// create ths s-concatenation by appending an itemset separator to 
				// start a new itemset
				prefix[prefixLength] = -1;
				// then we append the new item
				prefix[prefixLength+1] = item;
				// if this s-concatenation is high utility, then we output it
				if(totalUtility >= minUtility) {
					writeOut(prefix,prefixLength+2, totalUtility);
				}
				
//				//Tin checks:
//				if(itemSWU.swu < totalUtility + totalRemainingUtility) {
//					System.out.println("swu(s-ext " + ToString(prefix, prefixLength+2) + "-1 -2) <= swu(" + item + ") = " + itemSWU.swu + " < SPU(s-ext " + ToString(prefix, prefixLength+2) + "-1 -2) = " + (totalUtility + totalRemainingUtility));
//				System.in.read();
//				}

				// if this s-concatenation passes the depth pruning 
				// (remaining utility + totality >= minutil)
				if(totalUtility + totalRemainingUtility >= minUtility) {

					// Finally, we recursively call the procedure uspan() for growing this pattern
					// to try to find larger high utilit sequential patterns
					if(itemCount+1 < maxPatternLength) {
						uspan(prefix, prefixLength+2, matrixProjections, itemCount+1);
					}
				}
			}
		}
		// We check the memory usage
		MemoryLogger.getInstance().checkMemory();
	}
	
////Tin added for checking above:	
//	public String ToString(int[] prefix, int length) {
//		StringBuilder sb = new StringBuilder();
//		for (int i=0; i < length; i++)
//			sb.append("" + prefix[i] + " ");
//		return sb.toString();
//	}
	
	/**
	 * Set the maximum pattern length
	 * @param maxPatternLength the maximum pattern length
	 */
	public void setMaxPatternLength(int maxPatternLength) {
		this.maxPatternLength = maxPatternLength;
	}
	
	/**
	 * Method to write a high utility itemset to the output file.
	 * @param the prefix to be written o the output file
	 * @param utility the utility of the prefix concatenated with the item
	 * @param prefixLength the prefix length
	 */
	private void writeOut(int[] prefix, int prefixLength,  int utility) throws IOException {
		// increase the number of high utility itemsets found
		patternCount++; 
		
		StringBuilder buffer = new StringBuilder();
		
		// If the user wants to save in SPMF format
		if(SAVE_RESULT_EASIER_TO_READ_FORMAT == false) {
			// append each item of the pattern
			for (int i = 0; i < prefixLength; i++) {
				buffer.append(prefix[i]);
				buffer.append(' ');
			}
			
			// append the end of itemset symbol (-1) and end of sequence symbol (-2)
			buffer.append("-1 #UTIL: ");
			// append the utility of the pattern
			buffer.append(utility);
		}
		else {
			// Otherwise, if the user wants to save in a format that is easier to read for debugging.

			// Append each item of the pattern
			buffer.append('<');
			buffer.append('(');
			for (int i = 0; i < prefixLength; i++) {
				if(prefix[i] == -1) {
					buffer.append(")(");
				}else {
					buffer.append(prefix[i]);
				}
			}
			buffer.append(")>:");
			buffer.append(utility);
		}
		
		// write the pattern to the output file
		writer.write(buffer.toString());
		writer.newLine();
		
		// if in debugging mode, then also print the pattern to the console
		if(DEBUG) {
			System.out.println(" SAVING : " + buffer.toString());
			System.out.println();
			
			// check if the calculated utility is correct by reading the file
			// for debugging purpose
			checkIfUtilityOfPatternIsCorrect(prefix, prefixLength, utility);
		}
	}
	
	/**
	 * This method check if the utility of a pattern has been correctly calculated for
	 * debugging purposes. It is not designed to be efficient since it is just used for
	 * debugging.
	 * @param prefix a pattern stored in a buffer
	 * @param prefixLength the pattern length
	 * @param utility the utility of the pattern
	 * @throws IOException if error while writting to file
	 */
	private void checkIfUtilityOfPatternIsCorrect(int[] prefix, int prefixLength, int utility) throws IOException {
		int calculatedUtility = 0;
		
		BufferedReader myInput = new BufferedReader(new InputStreamReader( new FileInputStream(new File(input))));
		// we will read the database
		try {
			// prepare the object for reading the file
			
			String thisLine;
			// for each line (transaction) until the end of file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is  a comment, is  empty or is a kind of metadata
				if (thisLine.isEmpty() == true || thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%' || thisLine.charAt(0) == '@') {
					continue;
				}
				
				// split the sequence according to the " " separator
				String tokens[] = thisLine.split(" "); 

				int tokensLength = tokens.length -3;
				
				int[] sequence = new int[tokensLength];
				int[] sequenceUtility = new int[tokensLength];
				
				// Copy the current sequence in the sequence buffer.
				// For each token on the line except the last three tokens
				// (the -1 -2 and sequence utility).
				for(int i=0; i< tokensLength; i++) {
					String currentToken = tokens[i];
					
					// if empty, continue to next token
					if(currentToken.length() == 0) {
						continue;
					}
					
					// read the current item
					int item;
					int itemUtility;
					
					// if the current token is -1
					if(currentToken.equals("-1")) {
						item = -1;
						itemUtility = 0;
					}else {
						// if  the current token is an item
						//  We will extract the item from the string:
						int positionLeftBracketString = currentToken.indexOf('[');
						int positionRightBracketString = currentToken.indexOf(']');
						String itemString = currentToken.substring(0, positionLeftBracketString);
						item = Integer.parseInt(itemString);
						
						// We also extract the utility from the string:
						String utilityString = currentToken.substring(positionLeftBracketString+1, positionRightBracketString);
						itemUtility = Integer.parseInt(utilityString);
					}
					sequence[i] = item;
					sequenceUtility[i] = itemUtility;
				}
				
 				// For each position of the sequence
				int util = tryToMatch(sequence,sequenceUtility, prefix, prefixLength, 0, 0, 0);
				calculatedUtility += util;
			}
		} catch (Exception e) {
			// catches exception if error while reading the input file
			e.printStackTrace();
		}finally {
			if(myInput != null){
				// close the input file
				myInput.close();
			}
	    }
		
		if(calculatedUtility != utility) {
			System.out.print(" ERROR, WRONG UTILITY FOR PATTERN : ");
			for(int i=0; i<prefixLength; i++) {
				System.out.print(prefix[i]);
			}
			System.out.println(" utility is: " + utility + " but should be: " + calculatedUtility);
System.in.read();
		}
	}

	/**
	 * This is some code for verifying that the utility of a pattern is correctly calculated
	 * for debugging only. It is not efficient. But it is a mean to verify that 
	 * the result is correct.
	 * @param sequence a sequence (the items and -1)
	 * @param sequenceUtility a sequence (the utility values and -1)
	 * @param prefix the current pattern stored in a buffer
	 * @param prefixLength the current pattern length
	 * @param prefixPos the position in the current pattern that we will try to match with the sequence
	 * @param seqPos the position in the sequence that we will try to match with the pattenr
	 * @param utility the calculated utility until now
	 * @return the utility of the pattern
	 */
	private int tryToMatch(int[] sequence, int[] sequenceUtility, int[] prefix,	int prefixLength, 
			int prefixPos, int seqPos, int utility) {
		
		// Note: I do not put much comment in this method because it is just
		// used for debugging.

		List<Integer> otherUtilityValues = new ArrayList<Integer>();
		
		// try to match the current itemset of prefix
		int posP = prefixPos;
		int posS = seqPos;
		
		int previousPrefixPos = prefixPos;
		int itemsetUtility = 0;
		while(posP < prefixLength & posS < sequence.length) {
			if(prefix[posP] == -1 && sequence[posS] == -1) {
				posS++;
				
				// try to skip the itemset in prefix
				int otherUtility = tryToMatch(sequence, sequenceUtility, prefix, prefixLength, previousPrefixPos, posS, utility);
				otherUtilityValues.add(otherUtility);

				posP++;
				utility += itemsetUtility;
				itemsetUtility = 0;
				previousPrefixPos = posP;
			}else if(prefix[posP] == -1) {
				// move to next itemset of sequence
				while(posS < sequence.length && sequence[posS] != -1){
					posS++;
				}
				
				// try to skip the itemset in prefix
				int otherUtility = tryToMatch(sequence, sequenceUtility, prefix, prefixLength, previousPrefixPos, posS, utility);
				otherUtilityValues.add(otherUtility);
				
				utility += itemsetUtility;
				itemsetUtility = 0;
				previousPrefixPos = posP;

			}else if(sequence[posS] == -1) {
				posP = previousPrefixPos;
				itemsetUtility = 0;
				posS++;
			}else if(prefix[posP] == sequence[posS]) {
				posP++;
				itemsetUtility += sequenceUtility[posS];
				posS++;
				if(posP == prefixLength) {
					
					// try to skip the itemset in prefix
					// move to next itemset of sequence
					while(posS < sequence.length && sequence[posS] != -1){
						posS++;
					}
					int otherUtility = tryToMatch(sequence, sequenceUtility, prefix, prefixLength, previousPrefixPos, posS, utility);
					otherUtilityValues.add(otherUtility);
					

					utility += itemsetUtility;
				}
			}else if(prefix[posP] != sequence[posS]) {
				posS++;
			}
		}
		
		int max = 0;
		if(posP == prefixLength) {
			max = utility;
		}
		for(int utilValue : otherUtilityValues) {
			if(utilValue > utility) {
				max = utilValue;
			}
		}
		return max;
	}

	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStatistics() {
		System.out.println("=============  USPAN ALGORITHM v2.14 - STATS ==========");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println(" High-utility sequential pattern count : " + patternCount); 
		System.out.println("========================================================");
	}
}