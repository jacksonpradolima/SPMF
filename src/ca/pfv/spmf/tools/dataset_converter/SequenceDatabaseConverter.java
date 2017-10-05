package ca.pfv.spmf.tools.dataset_converter;

/* This file is copyright (c) 2008-2012 Philippe Fournier-Viger
* 
* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
* 
* SPMF is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
* This class is for converting sequence databases from various formats
* to the SPMF format.
* 
* @see Formats
* @author Philippe Fournier-Viger
*/
public class SequenceDatabaseConverter {
	
	String input;  // the path of the input file
	String output; // the path of the file to be written to disk in SPMF format
	int lineCount =0; // the number of sequences in the input file
	BufferedWriter writer; // to write the output file

	/**
	 * This method converts a sequence database from a given format to the SPMF format.
	 * @param input  the path of the input file
	 * @param charset the charset (encoding) of the input file if it is a text file (e.g "UTF-8")
	 *   or null if you want to use the default text encoding or if it is not a text file.
	 * @param output the path of the file to be written to disk in SPMF format
	 * @param inputFileformat  the format of the input file
	 * @param lineCount  the number of lines from the input file that should be converted
	 * @throws IOException  an exception is thrown if there is an error reading/writing files
	 */
	public void convert(String input, String output, Formats inputFileformat, int lineCount, Charset charset) throws IOException {
		
		// we save the parameter in the class fields
		this.input = input;
		this.output = output;
		this.lineCount = lineCount;
		
		// Use the default charset if it is null
		if(charset == null){
			charset = Charset.defaultCharset();
		}
		
		// we create an object fro writing the output file
		writer = new BufferedWriter(new FileWriter(output)); 
		
		// we call the appropriate method for converting a database
		// according to the format of the input file
		if(inputFileformat.equals(Formats.IBMGenerator)){
			convertIBMGenerator();
		}
		else if(inputFileformat.equals(Formats.Kosarak)){
			convertKosarak(charset);
		}else if(inputFileformat.equals(Formats.CSV_INTEGER)){
			convertCSV(charset);
		}else if(inputFileformat.equals(Formats.BMS)){
			convertBMS(charset);
		}else if(inputFileformat.equals(Formats.Snake)){
			convertSnake(charset);
		}else if(inputFileformat.equals(Formats.SPMF_TRANSACTION_DB)){
			convertTransactionDB(charset);
		}else if(inputFileformat.equals(Formats.TEXT)){
			convertTEXT(false, charset);
		}
		
		// we close the output file
		writer.close();
	}
	
	/**
	 * This method convert a transaction database in SPMF format to a sequence database in SPMF format
	 * @param charset the charset (encoding) of the input file  (e.g "UTF-8")
	 *   or null if you want to use the default text encoding.
	 */
	private void convertTransactionDB(Charset charset) {
		String thisLine; // variable to read a line
		BufferedReader myInput = null;  
		try {
			// Objects to read the file
			FileInputStream fin = new FileInputStream(new File(input));
			myInput = new BufferedReader(new InputStreamReader(fin,charset));
			
			int count =0;  // to count the number of line
			
			// we read the file line by line until the end of the file
			while ((thisLine = myInput.readLine()) != null) {
				// we split the line according to spaces
				String[] split = thisLine.split(" ");
				// for each item on this line
				for(String itemString : split) {
					int item = Integer.parseInt(itemString);
					// we write the item with an itemset separator
					writer.write(item + " -1 "); 
				}
				// we write the end of the line
				writer.write("-2");
				count++; // we increase the number of line that was read until now
				
				// if we have read enough lines, we stop.
				if(count == lineCount){
					break;
				}
				// start a new line
				writer.newLine();
			}
			myInput.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	/**
	 * This method convert a file from the SNAKE format to SPMF format
	 */
	private void convertSnake(Charset charset) {
		String thisLine; // variable to read a line
		BufferedReader myInput = null;  
		try {
			// Objects to read the file
			FileInputStream fin = new FileInputStream(new File(input));
			myInput = new BufferedReader(new InputStreamReader(fin, charset));
			
			int count =0;  // to count the number of line
			
			// we read the file line by line until the end of the file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line contains more than 11 elements
				// (we use this to filter smaller lines)
				if(thisLine.length() >= 11){   
					// for each integer on this line, we consider that it is an item
					for(int i=0; i< thisLine.length(); i++){
						// we subtract 65 to get the item number and
						// write the item to the file
						int character = thisLine.toCharArray()[i] - 65;
						// we write an itemset separator
						writer.write(character + " -1 ");   
					}
					// we write the end of the line
					writer.write("-2");
				}
				count++; // we increase the number of line that was read until now
				
				// if we have read enough lines, we stop.
				if(count == lineCount){
					break;
				}
				// start a new line
				writer.newLine();
			}
			myInput.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	/**
	 * This method convert a file from the BMS format to SPMF format
	 * @param charset the charset (encoding) of the input file  (e.g "UTF-8")
	 *   or null if you want to use the default text encoding.
	 */
	private void convertBMS(Charset charset) {
		String thisLine; // variable to read a line
		BufferedReader myInput = null;
		try {
			// Objects to read the file
			FileInputStream fin = new FileInputStream(new File(input));
			myInput = new BufferedReader(new InputStreamReader(fin, charset));
			
			// In the BMS format, the sequencs of webpage of a user
			// is separated on several lines.
			// We use this variable to remember the id of the current user 
			// that we are reading.
			int lastId = 0; 
			
			int count = 0; // to count the number of line
			
			// we read the file line by line until the end of the file
			while ((thisLine = myInput.readLine()) != null) {

				// we split the line according to spaces
				String[] split = thisLine.split(" ");
				
				// each line is a user id with a webpage
				int id = Integer.parseInt(split[0]); // id of the user on this line
				int val = Integer.parseInt(split[1]); // webpage viewed by this user
				
				// if the id of the current user is not the same as the previous line
				if(lastId != id){
					// and it is not the first line
					if(lastId!=0 ){ 
						count++; // increase sequence count
						
						// write the end of line
						writer.write("-2");
						writer.newLine();
					}
					lastId = id; // remember the current user id for this line  so that we know it for next line
				}
				// if we have read enough sequences, we stop.
				if(count == lineCount){
					break;
				}
				// after each line we write an itemset separator "-1"
				writer.write(val + " -1 ");   // WRITE
			}
			myInput.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	/**
	 * This method convert a file from the CSV format to SPMF format
	 * @param charset the charset (encoding) of the input file  (e.g "UTF-8")
	 *   or null if you want to use the default text encoding.
	 */
	private void convertCSV(Charset charset) throws IOException {
		String thisLine; // variable to read a line
		BufferedReader myInput = null;
		try {
			// Objects to read the file
			FileInputStream fin = new FileInputStream(new File(input));
			myInput = new BufferedReader(new InputStreamReader(fin, charset));
			
			int count = 0; // to count the number of line
			
			// we read the file line by line until the end of the file
			while ((thisLine = myInput.readLine()) != null) {
				
				// we split the line according to spaces
				String[] split = thisLine.split(",");
				// for each value
				for (String value : split) {
					// we convert to integer and write the item
					Integer item = Integer.parseInt(value);
					writer.write(item + " -1 ");   // write an itemset separator
				}
				writer.write("-2");    // write end of line
				
				count++; // increase the number of sequences
				// if we have read enough sequences, we stop.
				if(count == lineCount){
					break;
				}
				writer.newLine(); // create new line
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}
	}

	/**
	 * This method convert a file from the KOSARAK format to SPMF format
	 * @param charset the charset (encoding) of the input file if it is a text file (e.g "UTF-8")
	 *   or null if you want to use the default text encoding or if it is not a text file.
	 */
	private void convertKosarak(Charset charset) throws IOException {
		String thisLine; // variable to read a line
		BufferedReader myInput = null;
		try {
			// Objects to read the file
			FileInputStream fin = new FileInputStream(new File(input));
			myInput = new BufferedReader(new InputStreamReader(fin, charset));
			
			int count = 0; // to count the number of line
			
			// we read the file line by line until the end of the file
			while ((thisLine = myInput.readLine()) != null) {
				// we split the line according to spaces
				String[] split = thisLine.split(" ");
				// for each string on this line
				for (String value : split) {
					// we convert to integer and write it to file (it is an item)
					Integer item = Integer.parseInt(value);
					writer.write(item + " -1 ");   // write an itemset separator
				}
				writer.write("-2");  // write end of line
				
				count++;// increase the number of sequences
				// if we have read enough sequences, we stop.
				if(count == lineCount){
					break;
				}
				writer.newLine(); // create new line
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}
	}

	/**
	 * This method convert a file from the IBM GENERATOR format to SPMF format
	 * 
	 */
	private void convertIBMGenerator() {
		DataInputStream myInput = null;
		try {
			// Objects to read the input file in binary format
			FileInputStream fin = new FileInputStream(new File(input));
			myInput = new DataInputStream(fin);
			
			// Variable to remember if we have written -1 after a group of items or not
			// (because in the binary format, at the end of a line there is no -1 before the -2
			//  but in spmf format there is one).
			boolean lastMinus1 = false; 
			
			int count = 0; // to count the number of line
			
			// we read the file integer by integer until the end of the file
			while (myInput.available() != 0) {
				// we read the first 32 bits and convert to big indian
				int value = INT_little_endian_TO_big_endian(myInput.readInt());
				// if it is "-1", the end of an itemset
				if (value == -1) { 
					// we write the same thing as output
					writer.write("-1 "); 
					lastMinus1 = true; // to remember that we have written -1
				} 
				// if it is "-2", the end of a sequence
				else if (value == -2) { 
					// check if the last "-1" was not written
					if (lastMinus1 == false) {
						writer.write("-1 "); // write "-1"
					}
					writer.write("-2 "); // write end of line
					
					count++;// increase the number of sequences
					
					// if we have read enough sequences, we stop.
					if(count == lineCount){
						break;
					}
					writer.newLine(); // create new line
				}
				// else it is an item
				else {
					// we write the item
					writer.write(value + " ");  
					
					lastMinus1 = false; // to remember that we need to write a -1
				}
			}
			myInput.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 *  This method converts integer values from little indian to big endian
	 * @param i  an integer in little indian
	 * @return  the integer converted to big indian
	 */
	int INT_little_endian_TO_big_endian(int i) {
		return ((i & 0xff) << 24) + ((i & 0xff00) << 8) + ((i & 0xff0000) >> 8)
				+ ((i >> 24) & 0xff);
	}
	
	
	/**
	 * This method convert a text file to a sequence database in SPMF format and
	 * return a map of key = item id  value = corresponding attribute value. This 
	 * method is to be used by the GUI version of SPMF that need to keep the mapping
	 * between item IDs and attribute value in memory to avoid an extra database scan.
	 * @param inputFile the path of the file to be converted
	 * @param outputFile the path for saving the converted file
	 * @param lineCount the number of lines of the input file to be converted
	 * @param charset the charset (encoding) of the input file if it is a text file (e.g "UTF-8")
	 *   or null if you want to use the default text encoding or if it is not a text file.
	 * @return a map of entry (key : itemID, value: attribute-value) if the input format is TEXT
	 * @throws IOException  if an error while reading/writing files
	 */
	public Map<Integer, String> convertTEXTandReturnMap(String inputFile, String outputFile,
			int lineCount, Charset charset) throws IOException {
		// we save the parameter in the class fields
		this.input = inputFile;
		this.output = outputFile;
		this.lineCount = lineCount;
		return convertTEXT(true, charset);
	}
	
	
	/**
	 * This method convert a file from the TEXT format to the SPMF format.
	 * 
	 * @param returnMapItemIDWord
	 * @param charset the charset (encoding) of the input file(e.g "UTF-8")
	 *   or null if you want to use the default text encoding.
	 * @throws IOException  exception if error while reading/writing files.
	 * @return a map where an entry indicates for an item (key), the corresponding attribute value (value).
	 */
	private Map<Integer, String> convertTEXT(boolean returnMapItemIDWord, Charset charset) throws IOException {
		// This map will be used to store mapping from item id (key) to words (value).
		// It is used only if returnMapItemIDValue is set to true.  This is used by the GUI of SPMF
		// which need to keep this information in memory to avoid an extra database scan after an algorithm
		// is applied.
		Map<Integer, String> mapItemsIDToWords = null;
		if(returnMapItemIDWord){
			mapItemsIDToWords = new HashMap<Integer, String>();
		}
		
		// A map that store the corresponding Item ID for each word
		// An entry in the map is :
		//   key  =  a word
		//   value = Integer (item id)
		Map<String, Integer> mapWordsToItemIDs = new HashMap<String,Integer>();;
		
		// object for writing the output file
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output),charset));; 
		
		// This is the first line in the output file
		writer.write("@CONVERTED_FROM_TEXT");
		writer.newLine();

		// Now we will read the input file
		BufferedReader myInput = null;
		try {
			// Create some objects to read the file
			FileInputStream fin = new FileInputStream(new File(input));
			myInput = new BufferedReader(new InputStreamReader(fin, charset));

			
			// READ THE WHOLE CONTENT OF THE TEXT FILE LINE BY LINE
			// This variable is to remember if this   is the first word of a sentence.
			boolean isFirstWordOfSentence = true;
			// This variable is to remember if a sentence has not been ended yet.
			boolean someSentenceHasNotEndedYet = false;
			
			// Create a string builder to store the current sentence
			StringBuilder currentSentence = new StringBuilder();
			
			// Variable to be used to assign item ids (integers) to words
			int nextItemID = 1;
			
			// For each line in the input file
			String thisLine;
			while ((thisLine = myInput.readLine()) != null) {
//				System.out.println(thisLine);
				// if the line is too short (e.g emptylines), skip it
				if(thisLine.length() <1){
					continue;
				}
				
				// split the line into words
				String words[] = thisLine.split(" ");
			
//				// for each word
				for(String word : words){

					// We check if this token contains some punctuation that ends a sentence
					boolean isEndOfSentence = word.endsWith("?") || word.endsWith(".") || word.endsWith("!") 
							|| word.endsWith("\u002e")	|| word.endsWith("\u3002")  // <-- This is the Chinese end of sentence (to support chinese text)
					|| word.endsWith("\uFF01") // chinese "!"
					|| word.endsWith("\u003F"); // chinese "?"
					
					// If this word contains punctuation, we remove it (this could include ,()"' and other punctuation symbols.
					// We also transform the word to lowercase
					String cleanWord = word.replaceAll("[^\\p{L}\\p{N}]+", "").toLowerCase();
					
					if(cleanWord.length() !=0){

						// Convert the word to an item
						Integer item = mapWordsToItemIDs.get(cleanWord);
						if(item == null){
							// Give a new ID to this item
							item = nextItemID++;
							// Remember the ID 
							mapWordsToItemIDs.put(cleanWord, item);
							if(mapItemsIDToWords != null){
								mapItemsIDToWords.put(item, cleanWord);
							}
							// Write the ID to the file
							writer.write("@ITEM=" + item + "=" + cleanWord);
							writer.newLine();
						}
	
						// First we will save the word in the output file
						// If it is not the first word we will add a space.
						if(isFirstWordOfSentence){
							isFirstWordOfSentence = false;
						}else{
							currentSentence.append(" ");
						}
						currentSentence.append(item);
						// We need to write the itemset separator between each word
						currentSentence.append(" -1");
					}
					
					// If this is the last word of a sentence
					if(isFirstWordOfSentence == false && isEndOfSentence){
						// We write the end of sequence and create a new line
						currentSentence.append(" -2");
						// We reset the variables and increase the sequence count
						isFirstWordOfSentence = true;
						someSentenceHasNotEndedYet = false;
						
						
						// write the current sentence to the file
						writer.write(currentSentence.toString());
						writer.newLine();
						currentSentence.setLength(0);
					}else{
						// if it is not the end of a sentence, we remember that the current
						// sentence has not ended yet and we will need to make sure that it ends later.
						someSentenceHasNotEndedYet = true;
					}
				}
			}
			
			// This is a special case to be more robust.
			// If some sentence has not ended because some punctuation is missing for the last sentence of the file
			// We write the end of the last sequence.
			if(someSentenceHasNotEndedYet){
				// We write the end of sequence and create a new line
				currentSentence.append(" -2");
				// We reset the variables and increase the sequence count
				isFirstWordOfSentence = true;
				someSentenceHasNotEndedYet = false;
				
				
				// write the current sentence to the file
				writer.write(currentSentence.toString());
				currentSentence.setLength(0);
			}
			
			// close output file
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}
				
		return mapItemsIDToWords;	
	}
}
