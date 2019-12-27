package ca.pfv.spmf.algorithms.timeseries.normalization;

/* This file is copyright (c) 2008-2017 Philippe Fournier-Viger
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


import java.io.IOException;
import java.util.Arrays;

import ca.pfv.spmf.algorithms.timeseries.TimeSeries;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * An algorithm to calculate the min max normalization of a time series.
 * The purpose of using this algorithm is to transform a time series such that all values
 * are in the [0,1] interval.  Each value y is replaced by: y' = (y - min) / (max - min).
 * 
 * @author Philippe Fournier-Viger, 2017
 */
public class AlgoMinMaxNormalization {
 
	/** the time the algorithm started */
	long startTimestamp = 0; 
	/** the time the algorithm terminated */
	long endTimestamp = 0;  
	
	/** This program will execute in DEBUG MODE if this variable is true */
	boolean DEBUG_MODE = false;
		
	/**
	 * Default constructor
	 */
	public AlgoMinMaxNormalization() {
	}

	/**
	 * Generate the min max normalization of a time series containing at least 1 data point.
	 * @param timeSeries a time series 
	 * @return the min max normalization of the time series (a time series)
	 * @throws IOException exception if error while writing the file
	 */
	public TimeSeries runAlgorithm(TimeSeries timeSeries) throws IOException {
		// Validate parameters	
		if( timeSeries.size() < 1){
			throw new IllegalArgumentException(" The time series should contain at least 1 point.");
		}

		// reset memory logger
		MemoryLogger.getInstance().reset();
		
		// record the start time of the algorithm
		startTimestamp = System.currentTimeMillis();

		// IF in debug mode
		if(DEBUG_MODE){
			// Print the time series
			System.out.println(" Time series: " + Arrays.toString(timeSeries.data));
		}

		// Create an array to store the result
		double[] resultTimeSeriesArray = calculateMinMaxNormalization(timeSeries.data);
		TimeSeries transformedTimeSeries = new TimeSeries(resultTimeSeriesArray,  timeSeries.getName() + "_MMAX");
		
		// check the memory usage again and close the file.
		MemoryLogger.getInstance().checkMemory();
		// record end time
		endTimestamp = System.currentTimeMillis();

		return transformedTimeSeries;
	}
	
	/**
	 * Generate the min max normalization of a time series
	 * @param timeSeries a time series represented by a double array
	 * @return the transformed time series
	 */
	private double[] calculateMinMaxNormalization(double[] timeSeries) {
		double[] result = new double[timeSeries.length]; 
		
		// find the minimum and maximum values
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		
		// For each data point
		for(int i =0; i < timeSeries.length; i++){
			if(timeSeries[i] < min){
				min = timeSeries[i];
			}
			if(timeSeries[i] > max){
				max = timeSeries[i];
			}
		}
		
		double MaxMinusMin = max - min;
		
		
		// For each data point, calculate the new value
		for(int i = 0; i < timeSeries.length; i++){
			// y' = (y - min) / (max - min).
			result[i] = (timeSeries[i] - min) / MaxMinusMin;
		}
		
		if(DEBUG_MODE){
			System.out.println(" Number of data points = " + timeSeries.length);
			System.out.println(" Min max normalization: " + Arrays.toString(result));
		}	
		
		return result;
	}


	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  Min Max Normalization transformation v2.21- STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("===================================================");
	}

}