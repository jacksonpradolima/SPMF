package ca.pfv.spmf.algorithms.timeseries.exponentialsmoothing;

/* This file is copyright (c) 2008-2018 Philippe Fournier-Viger
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
 * An algorithm to calculate the exponential smoothing of a time series.
 * The purpose of using this algorithm is to reduce noise in a time series by smoothing the time series. 
 * It takes as parameter called alpha, which is a percentage representing the influence of a data points as a percentage
 * compared to the influence of the preceding point).  It expects that a time series contains at least 2 points. The output
 * For a time series containing "n" points, the output is a time series containing n points.
 * 
 * Exponential smoothing was proposed in this paper: <br/><br/>
 * 
 * Brown, Robert Goodell (1963). Smoothing Forecasting and Prediction of Discrete Time Series. Englewood Cliffs, NJ: Prentice-Hall.
 * 
 * @author Philippe Fournier-Viger, 2017
 */
public class AlgoExponentialSmoothing {
 
	/** the time the algorithm started */
	long startTimestamp = 0; 
	/** the time the algorithm terminated */
	long endTimestamp = 0;  
	
	/** This program will execute in DEBUG MODE if this variable is true */
	boolean DEBUG_MODE = false;
		
	/**
	 * Default constructor
	 */
	public AlgoExponentialSmoothing() {
	}

	/**
	 * Generate the exponential smoothing of a time series
	 * @param timeSeries a time series 
	 * @param alpha the smoothing constant (a double representing a percentage between 0 and 1)
	 * @return the transformed time series (an array of double objects)
	 * @throws IOException exception if error while writing the file
	 */
	public TimeSeries runAlgorithm(TimeSeries timeSeries, double alpha) throws IOException {
		// check some error for parameters
		if( alpha < 0 || alpha > 1){
			throw new IllegalArgumentException(" The alpha constant should be a value in the [0,1] interval");
		}
		
		if( timeSeries.size() <= 1){
			throw new IllegalArgumentException(" The time series should contain at least 2 points to apply exponential smoothing.");
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

		// Create an array to store the transformed time series
		double[] transformedTimeSeriesArray = calculateExponentialSmoothing(timeSeries.data, alpha);
		TimeSeries transformedTimeSeries = new TimeSeries(transformedTimeSeriesArray,  timeSeries.getName() + "_EXPSTHG");
		
		// check the memory usage again and close the file.
		MemoryLogger.getInstance().checkMemory();
		// record end time
		endTimestamp = System.currentTimeMillis();

		return transformedTimeSeries;
	}
	
	/**
	 * Generate the exponential smoothing of a time series
	 * @param timeSeries a time series represented by a double array
	 * @param alpha the smoothing constant (a double value in the [0,1] interval, representing the influence of the current point
	 * as a percentage compared to the preceding point)
	 * @return the transformed time series
	 */
	private double[] calculateExponentialSmoothing(double[] timeSeries, double alpha) {
		// Create an array to store the exponential smoothing
		//*********************************************************************** //
		double[] exponentialSmoothing = new double[timeSeries.length]; 
		
		// set the first data point as the first point of the time series
		exponentialSmoothing[0] = timeSeries[0];
		
		// For each data point
		for(int i =1; i < timeSeries.length; i++){
			exponentialSmoothing[i] = (timeSeries[i] * alpha) + ( exponentialSmoothing[i-1] * (1.0 - alpha)) ;
		}
		
		if(DEBUG_MODE){
			System.out.println(" Number of data points = " + timeSeries.length);
			System.out.println(" Alpha = " + alpha);
			System.out.println(" Exponential smoothing transformation: " + Arrays.toString(exponentialSmoothing));
		}	
		
		return exponentialSmoothing;
	}


	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  Transform to Exponential Smoothing v2.21- STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("===================================================");
	}

}