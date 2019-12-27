package ca.pfv.spmf.algorithms.timeseries.differencing;

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
 * An algorithm to calculate the second order differencing of a time series.
 * The purpose of using this algorithm is to remove a trend in a time series. 
 * For a time series containing "n" points, the output is a time series containing n-1 points
 * where the i-th point of the transformed time series
 * is equal to the difference between the i-th point and  the(i+1)-th point in the original time series.
 * 
 * Differencing is a widely used technique in time series mining.
 * 
 * @author Philippe Fournier-Viger, 2017
 */
public class AlgoSecondOrderDifferencing {
 
	/** the time the algorithm started */
	long startTimestamp = 0; 
	/** the time the algorithm terminated */
	long endTimestamp = 0;  
	
	/** This program will execute in DEBUG MODE if this variable is true */
	boolean DEBUG_MODE = false;
		
	/**
	 * Default constructor
	 */
	public AlgoSecondOrderDifferencing() {
	}

	/**
	 * Generate the second order differencing of a time series containing at least three data points.
	 * @param timeSeries a time series 
	 * @return the second order differencing of the time series (a time series)
	 * @throws IOException exception if error while writing the file
	 */
	public TimeSeries runAlgorithm(TimeSeries timeSeries) throws IOException {
		// Validate parameters	
		if( timeSeries.size() < 3){
			throw new IllegalArgumentException(" The time series should contain at least 3 points to apply differencing.");
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

		// Create an array to store the moving average
		double[] resultTimeSeriesArray = calculateSecondOrderDifferencing(timeSeries.data);
		TimeSeries transformedTimeSeries = new TimeSeries(resultTimeSeriesArray,  timeSeries.getName() + "_SODIFF");
		
		// check the memory usage again and close the file.
		MemoryLogger.getInstance().checkMemory();
		// record end time
		endTimestamp = System.currentTimeMillis();

		return transformedTimeSeries;
	}
	
	/**
	 * Generate the second order differencing of a time series
	 * @param timeSeries a time series represented by a double array
	 * @return the transformed time series
	 */
	private double[] calculateSecondOrderDifferencing(double[] timeSeries) {
		// Create an array to store the resulting time series
		// ********************************************************************** //
		// NOTE : THIS ARRAY IS OF SIZE N - 2  //
		//*********************************************************************** //
		double[] result = new double[timeSeries.length - 2]; 
		
		
		// For each data point
		for(int i = 2; i < timeSeries.length; i++){
			result[i-2] = timeSeries[i] - (2* timeSeries[i-1]) + (timeSeries[i-2]);
		}
		
		if(DEBUG_MODE){
			System.out.println(" Number of data points = " + timeSeries.length);
			System.out.println(" Second order differencing: " + Arrays.toString(result));
		}	
		
		return result;
	}


	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  Second order differencing transformation v2.21- STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("===================================================");
	}

}