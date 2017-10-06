package ca.pfv.spmf.algorithms.timeseries.sax;

/* This file is copyright (c) 2008-2016 Philippe Fournier-Viger
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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import ca.pfv.spmf.algorithms.timeseries.TimeSeries;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This class reads a time series file and convert it to a sequence of symbols using the SAX algorithm.
 * The format of the output is the SPMF format. The SAX algorithm is described in this paper:<br/><br/>
 * 
 * Lin, Jessica, et al. "Experiencing SAX: a novel symbolic representation of time series. <br/>
 * Data Mining and knowledge discovery 15.2 (2007): 107-144.<br/>
 * 
 * @author Philippe Fournier-Viger, 2016
 * @see AlgoSAX
 * @see SAXSymbol
 */

public class AlgoConvertTimeSeriesFileToSequencesWithSAX {
 
	/** the time the algorithm started */
	long startTimestamp = 0; 
	
	/** the time the algorithm terminated */
	long endTimestamp = 0;  
	
	/** writer to write the output file **/
	BufferedWriter writer = null;  
	
	/** This program will execute in DEBUG MODE if this variable is true */
	boolean DEBUG_MODE = false;
	
	/** The number of time series in the last file that was read */
	int timeSeriesCount = 0;
	
	/** The symbols created for the last converted time series */
	SAXSymbol[] symbols;
		
	/**
	 * Default constructor
	 */
	public AlgoConvertTimeSeriesFileToSequencesWithSAX() {
	}

	/**
	 * Run the algorithm
	 * @param output the output file path
	 * @param numberOfSegments the number of segments ( > 1)
	 * @param numberOfSymbols  the number of symbols 
	 * @param deactivatePAA  set this parameter to true to apply sax without converting the time series to PAA
	 * @throws IOException exception if error while writing the file
	 */
	public void runAlgorithm(List<TimeSeries> multipleTimeSeries, String output, int numberOfSegments, int numberOfSymbols, boolean deactivatePAA) throws IOException {
		
		// reset memory logger
		MemoryLogger.getInstance().reset();
		
		// record the start time of the algorithm
		startTimestamp = System.currentTimeMillis();
		
		// create a writer object to write results to file
		writer = new BufferedWriter(new FileWriter(output));
		writer.write("@CONVERTED_FROM_TIME_SERIES");

		// this variable will count the number of transactions
		timeSeriesCount = multipleTimeSeries.size();
		
		if(DEBUG_MODE){
			// print the time series number
			System.out.println(System.lineSeparator() + "======= READING THE INPUT FILE =======");
		}
		
		// IF in debug mode
		if(DEBUG_MODE){
			// prepare the object for reading the file
			for(TimeSeries timeSeries : multipleTimeSeries){
				// print the time series number
				System.out.println(System.lineSeparator() + "--- Reading time series #" + (timeSeriesCount + 1) + " ---");
			}
		}
		
		if(DEBUG_MODE){
			// print the time series number
			System.out.println(System.lineSeparator() + "======= APPLYING SAX  ========" + System.lineSeparator());
		}
		
		// Apply the SAX algorithm
		AlgoSAX algo = new AlgoSAX();
		SAXSymbol[][] saxSequences = algo.runAlgorithm(multipleTimeSeries, numberOfSegments, numberOfSymbols, deactivatePAA);
			
		if(DEBUG_MODE){
			// print the time series number
			System.out.println("======= WRITING THE OUTPUT FILE  ========" + System.lineSeparator());
		}
		
		// Write the output file
		symbols = algo.getSymbols();
		writeSAXSymbolsToOutputFile(symbols);
		for(int i=0; i< multipleTimeSeries.size(); i++){
			TimeSeries timeSeries = multipleTimeSeries.get(i);
			SAXSymbol[] saxSequence = saxSequences[i];
			writeSAXRepresentationToOutputFile(saxSequence, timeSeries.getName());
		}
		
		// remember the number of time series
		timeSeriesCount = multipleTimeSeries.size();
				
		// check the memory usage again and close the file.
		MemoryLogger.getInstance().checkMemory();
		// close output file
		writer.close();
		// record end time
		endTimestamp = System.currentTimeMillis();
	}

	/**
	 * Get the list of all symbols used for converting the time series.
	 * @return the list of symbols or null if no time series has been converted yet.
	 */
	public SAXSymbol[] getSymbols() {
		return symbols;
	}


	/**
	 * Write a sax representation of a time series to an output file
	 * @param saxRepresentation the sax representation of the time series
	 * @param name the name of the time series
	 * @throws IOException if an exception occurs when reading or writing to file.
	 */
	private void writeSAXRepresentationToOutputFile(SAXSymbol[] saxRepresentation, String name) throws IOException {
		// Print the time series
		writer.newLine();
		writer.write("@NAME=" + name);
		writer.newLine();
		for(SAXSymbol symbol : saxRepresentation){
			writer.write(symbol.symbol + " -1 ");
		}
		writer.write("-2");
	}
	
	/**
	 * Write a list of SAX symbols to the output file
	 * @param symbols the list of symbols
	 * @throws IOException if an exception occurs when reading or writing to file.
	 */
	private void writeSAXSymbolsToOutputFile(SAXSymbol[] symbols) throws IOException {
		// First print the symbols
		for(SAXSymbol symbol : symbols){
			writer.newLine();
			writer.append("@ITEM=" + symbol.symbol + "=[" + symbol.lowerBound + "," + symbol.upperBound + "]");
		}
	}

	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats() {
		System.out.println("======= CONVERT TIME SERIES TO SEQUENCES WITH SAX v2.05- STATS =======");
		System.out.println(" Number of time series processed: " + timeSeriesCount);
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("=====================================================================");
	}
}