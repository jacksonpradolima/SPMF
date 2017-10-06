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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import ca.pfv.spmf.algorithms.ArraysAlgos;
import ca.pfv.spmf.algorithms.timeseries.TimeSeries;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This class write a list of time series to a file
 * 
 * @author Philippe Fournier-Viger, 2016
 * @see TimeSeries
 */

public class AlgoTimeSeriesWriter {
 
	/** the time the algorithm started */
	long startTimestamp = 0; 
	
	/** the time the algorithm terminated */
	long endTimestamp = 0;  
	
	/** This program will execute in DEBUG MODE if this variable is true */
	boolean DEBUG_MODE = false;
	
	/** The number of time series in the last file that was read */
	int timeSeriesCount = 0;
	
	/** writer to write the output file **/
	BufferedWriter writer = null;  
		
	/**
	 * Default constructor
	 */
	public AlgoTimeSeriesWriter() {
	}

	/**
	 * Run the algorithm
	 * @param input the output file path
	 * @param timeSeries a list of time  serie
	 * @param separator a separator (a string) to separate time series values (for exmaple the comma ',')
	 * @throws IOException exception if error while writing the file
	 */
	public void runAlgorithm(String output, List<TimeSeries> multipleTimeSeries, String separator) throws IOException {
		
		// reset memory logger
		MemoryLogger.getInstance().reset();
		
		// record the start time of the algorithm
		startTimestamp = System.currentTimeMillis();

		// write the file
		writer = new BufferedWriter(new FileWriter(output));
		
		// for each time series
		for(int j = 0; j < multipleTimeSeries.size(); j++){
			TimeSeries timeSeries  = multipleTimeSeries.get(j);

			// First, write the name of the time series
			writer.write("@NAME=" + timeSeries.getName() );
			writer.newLine();
			
			// for each value in this time series
			for(int i=0; i < timeSeries.data.length; i++){
				// write the value
				double value = timeSeries.data[i];
				writer.write(Double.toString(value));
				
				// if it is not the last value, then write the separator
				if(i != timeSeries.data.length  -1){
					writer.write(separator);
				}	
			}
			
			// if it is not the last time series, then change line
			if(j != multipleTimeSeries.size() -1){
				writer.newLine();
			}	
		}

		// close the writer
		writer.close();
		
		// remember the number of time series
		timeSeriesCount = multipleTimeSeries.size();
		
		// check the memory usage again and close the file.
		MemoryLogger.getInstance().checkMemory();
		// record end time
		endTimestamp = System.currentTimeMillis();
		
	}

	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats() {
		System.out.println("======= WRITE TIME SERIES TO FILE v2.06 - STATS =======");
		System.out.println(" Number of time series processed: " + timeSeriesCount);
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("=====================================================================");
	}
}