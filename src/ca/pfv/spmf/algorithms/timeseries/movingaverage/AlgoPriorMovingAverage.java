package ca.pfv.spmf.algorithms.timeseries.movingaverage;

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


import java.io.IOException;
import java.util.Arrays;

import ca.pfv.spmf.algorithms.timeseries.TimeSeries;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * An algorithm to calculate the prior moving average of a time series. 
 * The prior moving average for a point is computed as the average of the "windowSize" prior points
 * (it excludes the current point from the average).
 * It takes as parameter the window size (a number of data points).
 * 
 * @author Philippe Fournier-Viger, 2018
 */
public class AlgoPriorMovingAverage {
 
	/** the time the algorithm started */
	long startTimestamp = 0; 
	/** the time the algorithm terminated */
	long endTimestamp = 0;  
	
	/** This program will execute in DEBUG MODE if this variable is true */
	boolean DEBUG_MODE = false;
		
	/**
	 * Default constructor
	 */
	public AlgoPriorMovingAverage() {
	}

	/**
	 * Generate the prior moving average of a time series
	 * @param timeSeries a time series 
	 * @param windowSize the number of data point inside the Window ( > 1)
	 * @return the moving average of the time series (an array of double objects)
	 * @throws IOException exception if error while writing the file
	 */
	public TimeSeries runAlgorithm(TimeSeries timeSeries, int windowSize) throws IOException {
		// check some error for parameters
		if(timeSeries.data.length < windowSize){
			throw new IllegalArgumentException(" The window size should be greater or equal to 1");
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
		double[] movingAverageData = calculatePriorMovingAverage(timeSeries.data, windowSize);
		TimeSeries movingAverage = new TimeSeries(movingAverageData,  timeSeries.getName() + "_PMAVG");
		
		// check the memory usage again and close the file.
		MemoryLogger.getInstance().checkMemory();
		// record end time
		endTimestamp = System.currentTimeMillis();

		return movingAverage;
	}
	
	/**
	 * Generate the moving average of a time series
	 * @param timeSeries a time series represented by a double array
	 * @param windowSize the number of data point inside the Window ( > 1)
	 * @return the moving average ( a time series)
	 */
	private double[] calculatePriorMovingAverage(double[] timeSeries, int windowSize) {
		// Create an array to store the moving average
		double[] movingAverage = new double[timeSeries.length];
		
		// This variable will contain the moving average
		double sum = 0d;
		// This variable will be used to calculate the moving average of the points 
		// inside [0, windowSize -1]
		double firstSum = 0d;
		
		// For each data point
		for(int i = 0; i < timeSeries.length; i++){
			
			if(i == 0){
				// we don't save because there is no prior value
				
				// we just keep the value for the next point to be calculated
				firstSum += timeSeries[0];
				sum+= timeSeries[0] / windowSize;
			}else if(i < windowSize){
				movingAverage[i-1] = firstSum / i; //  JUST FILL THE SAME VALUE
				
				firstSum += timeSeries[i];
				sum+= timeSeries[i] / windowSize;
			}else{
				movingAverage[i-1] = sum;

				sum+= timeSeries[i] / windowSize;
				sum-= timeSeries[i-windowSize] / windowSize;
			}
		}
		
		// Then add the last point
		movingAverage[timeSeries.length-1] = sum;
		
		if(DEBUG_MODE){
			System.out.println(" Number of data points = " + timeSeries.length);
			System.out.println(" Window size = " + windowSize);
			System.out.println(" Prior Moving average transformation: " + Arrays.toString(movingAverage));
		}	
		
		return movingAverage;
	}


	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  Transform to Prior Moving Average v2.21 - STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("===================================================");
	}

}