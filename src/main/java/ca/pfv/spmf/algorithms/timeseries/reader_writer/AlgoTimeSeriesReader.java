package ca.pfv.spmf.algorithms.timeseries.reader_writer;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import ca.pfv.spmf.algorithms.ArraysAlgos;
import ca.pfv.spmf.algorithms.timeseries.TimeSeries;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This class reads a time series file in memory
 * 
 * @author Philippe Fournier-Viger, 2016
 * @see TimeSeries
 */

public class AlgoTimeSeriesReader {
 
	/** the time the algorithm started */
	long startTimestamp = 0; 
	
	/** the time the algorithm terminated */
	long endTimestamp = 0;  
	
	/** This program will execute in DEBUG MODE if this variable is true */
	boolean DEBUG_MODE = false;
	
	/** The number of time series in the last file that was read */
	int timeSeriesCount = 0;
		
	/**
	 * Default constructor
	 */
	public AlgoTimeSeriesReader() {
	}

	/**
	 * Run the algorithm
	 * @param input the input file path
	 * @param separator 
	 * @return 
	 * @throws IOException exception if error while writing the file
	 */
	public List<TimeSeries> runAlgorithm(String input, String separator) throws IOException {
		
		// reset memory logger
		MemoryLogger.getInstance().reset();
		
		// record the start time of the algorithm
		startTimestamp = System.currentTimeMillis();

		// create a variable to store the time series
		List<TimeSeries> multipleTimeSeries = new ArrayList<TimeSeries>();

		// We scan the database a first time to calculate the support of each item.
		BufferedReader myInput = null;
		String thisLine;
		// this variable will count the number of transactions
		timeSeriesCount = 0;
		
		if(DEBUG_MODE){
			// print the time series number
			System.out.println(System.lineSeparator() + "======= READING THE INPUT FILE =======");
		}
		
		// Variable to store the name of the current time series
		String currentTimeSeriesName = null;
		
		// prepare the object for reading the file
		myInput = new BufferedReader(new InputStreamReader( new FileInputStream(new File(input))));
		// for each line (transaction) until the end of file
		while ((thisLine = myInput.readLine()) != null) {

			// if the line is  a comment, is  empty or is a
			// kind of metadata
			if (thisLine.isEmpty() == true ||
					thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'){
				continue;
			}
			
			if(thisLine.charAt(0) == '@'){
				// if it is the name of a time series
				if(thisLine.startsWith("@NAME=")){
					currentTimeSeriesName = thisLine.substring(6, thisLine.length());
				}
				continue;
			}

			// IF in debug mode
			if(DEBUG_MODE){
				// print the time series number
				System.out.println(System.lineSeparator() + "--- Reading time series #" + (timeSeriesCount + 1) + " ---");
			}
			
			//========================== CONVERT THE LINE TO DOUBLE VALUES ===============================
			// We will split the lines into string values
			String tokens[] = thisLine.split(separator); 
			// Then, we convert to a double array containing each data point of the time series
			double[] dataPoints = ArraysAlgos.convertStringArrayToDoubleArray(tokens);
			String nameToUse = currentTimeSeriesName == null ?  "Series " + timeSeriesCount : currentTimeSeriesName;
			currentTimeSeriesName = null;
			multipleTimeSeries.add(new TimeSeries(dataPoints, nameToUse));
			
			timeSeriesCount++;
		}
		
		// remember the number of time series
		timeSeriesCount = multipleTimeSeries.size();
		
		// closed input file
		myInput.close();
				
		// check the memory usage again and close the file.
		MemoryLogger.getInstance().checkMemory();
		// record end time
		endTimestamp = System.currentTimeMillis();
		
		// return the time series
		return multipleTimeSeries;
	}

	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats() {
		System.out.println("======= READ TIME SERIES TO MEMORY v2.07- STATS =======");
		System.out.println(" Number of time series processed: " + timeSeriesCount);
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("=====================================================================");
	}
}