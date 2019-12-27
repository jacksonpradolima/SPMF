package ca.pfv.spmf.algorithms.timeseries.mediansmoothing;

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

import ca.pfv.spmf.algorithms.sort.Select;
import ca.pfv.spmf.algorithms.timeseries.TimeSeries;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * An algorithm to calculate the median smoothing of a time series. 
 * It takes as parameter the window size (a number of data points).
 * 
 * @author Philippe Fournier-Viger, 2018
 */
public class AlgoMedianSmoothing {
 
	/** the time the algorithm started */
	long startTimestamp = 0; 
	/** the time the algorithm terminated */
	long endTimestamp = 0;  
	
	/** This program will execute in DEBUG MODE if this variable is true */
	boolean DEBUG_MODE = false;
		
	/**
	 * Default constructor
	 */
	public AlgoMedianSmoothing() {
	}

	/**
	 * Generate the median smoothing of a time series
	 * @param timeSeries a time series 
	 * @param windowSize the number of data point inside the Window ( > 1)
	 * @return the median smoothing of the time series (an array of double objects)
	 * @throws IOException exception if error while writing the file
	 */
	public TimeSeries runAlgorithm(TimeSeries timeSeries, int windowSize) throws IOException {
		// check some error for parameters
		if(windowSize >= timeSeries.data.length  ||  windowSize < 2 ){
			throw new IllegalArgumentException(" The window size must be greater than 1, and no larger than the number of points in the time series");
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

		// Create an array to store the median smoothing
		
		// If the window size contains an odd number of points
		double[] movingMedianData;
		if((windowSize % 2) == 1){
			movingMedianData = calculateMedianSmoothingOdd(timeSeries.data, windowSize);
		}else{
			movingMedianData = calculateMedianSmoothingEven(timeSeries.data, windowSize);
		}
		
		TimeSeries medianSmoothing = new TimeSeries(movingMedianData,  timeSeries.getName() + "_CEMEDSMT");
		
		// check the memory usage again and close the file.
		MemoryLogger.getInstance().checkMemory();
		// record end time
		endTimestamp = System.currentTimeMillis();

		return medianSmoothing;
	}
	
	/**
	 * Generate the median smoothing of a time series for a window size that
	 * is an odd number.
	 * @param timeSeries a time series represented by a double array
	 * @param windowSize the number of data point inside the Window
	 * @return the median smoothing of the time series
	 */
	private double[] calculateMedianSmoothingOdd(double[] timeSeries, int windowSize) {
		// Calculate the number of points on each side of the central point
		// We call this "alpha"
		int alpha = (windowSize - 1) / 2;
		
		// Create an array to store the median smoothing
		double[] medianSmoothing = new double[timeSeries.length - (windowSize -1)];
		
		// Create an array to store the current window of points when
		// sliting the window
		double[] window = new double[windowSize];
		
		// For each window center
		for(int i = alpha; i < timeSeries.length - alpha; i++){
			
			// calculate the position in the time series
			int smoothingPosition = i - alpha;
			
			// Copy the values of the window centered at the i-th position
			// to the "window" array
			System.arraycopy(timeSeries, smoothingPosition, window, 0, windowSize);
	
			// Apply a selection algorithm to select the median
			// without having to sort the array.
			// This should be more efficient than soring.
			 medianSmoothing[smoothingPosition] = Select.randomizedSelect(window, alpha);
		}

		
		if(DEBUG_MODE){
			System.out.println(" Number of data points = " + timeSeries.length);
			System.out.println(" Window size = " + windowSize);
			System.out.println(" Median smoothing transformation: " + Arrays.toString(medianSmoothing));
		}	
		
		return medianSmoothing;
	}
	
	/**
	 * Generate the median smoothing of a time series for a window size that
	 * is an even number.
	 * @param timeSeries a time series represented by a double array
	 * @param windowSize the number of data point inside the Window
	 * @return the median smoothing of the time series
	 */
	private double[] calculateMedianSmoothingEven(double[] timeSeries, int windowSize) {
		// Calculate the number of points on each side of the central points
		// We call this "alpha"
		int alpha = (windowSize - 2) / 2;
		
		// Create an array to store the median smoothing
		double[] medianSmoothing = new double[timeSeries.length - (windowSize - 2) - 1];
		
		// Create an array to store the current window of points when
		// sliding the window
		double[] window = new double[windowSize];
		
		// For each window center (left)
		for(int i = alpha; i < timeSeries.length - alpha-1; i++){
			
			// calculate the position in the time series
			int smoothingPosition = i - alpha;
			
			// Copy the values of the window centered at the i-th position
			// to the "window" array
			System.arraycopy(timeSeries, smoothingPosition, window, 0, windowSize);
	
			// Apply a selection algorithm to select the median
			// without having to sort the array.
			// This should be more efficient than soring.
			double leftMiddle = Select.randomizedSelect(window, alpha);
			double rightMiddle = Select.randomizedSelect(window, alpha+1);
			
			 medianSmoothing[smoothingPosition] = (leftMiddle + rightMiddle) / 2.0d;
		}

		
		if(DEBUG_MODE){
			System.out.println(" Number of data points = " + timeSeries.length);
			System.out.println(" Window size = " + windowSize);
			System.out.println(" Median smoothing transformation: " + Arrays.toString(medianSmoothing));
		}	
		
		return medianSmoothing;
	}


	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  Transform to Median Smoothing v2.25 - STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("===================================================");
	}

}