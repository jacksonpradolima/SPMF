package ca.pfv.spmf.algorithms.timeseries.autocorrelation;

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
 * An algorithm to calculate the autocorrelation time series of a time series 
 * The resulting time series contains values in the [-1,1] interval.
 * Calculating the auto-correlation plot is very useful in time series analysis to analyze
 * time series where the time interval between each observation is always the same. 
 * It can be used to check if a time series is random or if it is stationary,
 * to detect some periodic patterns, etc.
 * 
 * @author Philippe Fournier-Viger, 2018
 */
public class AlgoLagAutoCorrelation {
 
	/** the time the algorithm started */
	long startTimestamp = 0; 
	/** the time the algorithm terminated */
	long endTimestamp = 0;  
	
	/** This program will execute in DEBUG MODE if this variable is true */
	boolean DEBUG_MODE = false;
		
	/**
	 * Default constructor
	 */
	public AlgoLagAutoCorrelation() {
	}

	/**
	 * Generate the autocorrelation time series of a time series for a lag 1 to a lag "maxlag"
	 * @param timeSeries a time series
	 * @param maxlag a maximum lag
	 * @return the resulting autocorrelation time series
	 * @throws IOException exception if error while writing the file
	 */
	public TimeSeries runAlgorithm(TimeSeries timeSeries,  int maxlag) throws IOException {
		// check some error for parameters
		if( maxlag < 1 || maxlag > timeSeries.size()){
			throw new IllegalArgumentException(" The maxlag parameter must be set as follows:  1 <= maxlag <= timeSeries.length");
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
		double[] resultingTimeSeriesArray = calculateAutocorrelationTimeSeries(timeSeries.data, maxlag);
		TimeSeries resultingTimeSeries = new TimeSeries(resultingTimeSeriesArray,  timeSeries.getName() + "_AUTOCOR");
		
		// check the memory usage again and close the file.
		MemoryLogger.getInstance().checkMemory();
		// record end time
		endTimestamp = System.currentTimeMillis();

		return resultingTimeSeries;
	}
	
	/**
	 * Generate the autocorrelation time series of a time series from 1 to a  "maxlag"
	 * @param timeSeries a time series
	 * @param maxlag a maximum lag
	 * @return the transformed time series
	 */
	private double[] calculateAutocorrelationTimeSeries(double[] timeSeries, int maxlag) {
		// Create an array to store the result
		// it will store the autocorelation values for lag = 1,2,... k
		double[] autocorrelationResult = new double[maxlag+1]; 
		// By definition if there is no lag, the autocorrelation is 1 because
		// the time series is autocorrelated with itself!
		autocorrelationResult[0] = 1;
		
		// calculate the mean by using the whole time series
		double mean = 0d;
		for(int i = 0; i < timeSeries.length; i++){
			mean += timeSeries[i];
		}
		mean /= timeSeries.length;
		
		
		// calculate the denominator
		double denominator = 0d;
		for(int i = 0; i < timeSeries.length; i++){
			denominator += Math.pow(timeSeries[i] - mean, 2.0);
		}
		
		// calculate the numerator for each lag value
		for(int k=1; k <= maxlag; k++){
			
			double numerator = 0d;
			for(int i = 0; i < timeSeries.length - k; i++){
				numerator += (timeSeries[i] - mean) * (timeSeries[i+k] - mean);
			}
			autocorrelationResult[k] = numerator / denominator;
		}

		
		if(DEBUG_MODE){
			System.out.println(" Number of data points = " + timeSeries.length);
			System.out.println(" Minlag = " + maxlag);
			System.out.println(" Exponential smoothing transformation: " + Arrays.toString(autocorrelationResult));
		}	
		
		return autocorrelationResult;
	}


	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  Transform to lag k autocorrelation time series v2.21- STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("===================================================");
	}

}