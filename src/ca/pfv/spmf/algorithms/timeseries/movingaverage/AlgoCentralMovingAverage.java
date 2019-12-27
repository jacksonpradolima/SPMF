package ca.pfv.spmf.algorithms.timeseries.movingaverage;

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
 * An algorithm to calculate the central moving average of a time series. 
 * It takes as parameter the window size (a number of data points).
 * 
 * @author Philippe Fournier-Viger, 2018
 */
public class AlgoCentralMovingAverage {
 
	/** the time the algorithm started */
	long startTimestamp = 0; 
	/** the time the algorithm terminated */
	long endTimestamp = 0;  
	
	/** This program will execute in DEBUG MODE if this variable is true */
	boolean DEBUG_MODE = false;
		
	/**
	 * Default constructor
	 */
	public AlgoCentralMovingAverage() {
	}

	/**
	 * Generate the central moving average of a time series
	 * @param timeSeries a time series 
	 * @param windowSize the number of data point inside the Window ( > 1)
	 * @return the moving average of the time series (an array of double objects)
	 * @throws IOException exception if error while writing the file
	 */
	public TimeSeries runAlgorithm(TimeSeries timeSeries, int windowSize) throws IOException {
		// check some error for parameters
		if(windowSize >= timeSeries.data.length  ||  windowSize < 3 || (windowSize % 2) != 1){
			throw new IllegalArgumentException(" The window size must be odd, greater than 1, and no larger than the number of points in the time series");
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
		double[] movingAverageData = calculateCentralMovingAverage(timeSeries.data, windowSize);
		TimeSeries movingAverage = new TimeSeries(movingAverageData,  timeSeries.getName() + "_CEMAVG");
		
		// check the memory usage again and close the file.
		MemoryLogger.getInstance().checkMemory();
		// record end time
		endTimestamp = System.currentTimeMillis();

		return movingAverage;
	}
	
	/**
	 * Generate the central moving average of a time series
	 * @param timeSeries a time series represented by a double array
	 * @param windowSize the number of data point inside the Window
	 * @return the moving average ( a time series)
	 */
	private double[] calculateCentralMovingAverage(double[] timeSeries, int windowSize) {
		// Create an array to store the moving average
		double[] movingAverage = new double[timeSeries.length];
		
		// Calculate the number of points on each side of the central point
		// We call this "alpha"
		int alpha = (windowSize-1) / 2;
		
		// Initialize the sum using the central point + the following alpha points
		double sum = 0d;
		
		// FIRST POINT
		// Add all the points
		for(int i = 0; i <= alpha; i++){
			sum += timeSeries[i];
		}
		// Calculate the average
		movingAverage[0] = sum / (alpha+1);
		
		// NEXT POINT UNTIL A FULL WINDOW
		// Then we do a loop for the next points until we have a full window
		for(int j = 1; j <= alpha; j++){
			// add the point on the right but do not remove anything on the left
			sum += timeSeries[j+alpha];
			movingAverage[j] = sum / (alpha+1+j);
		}
		
		// NEXT POINTS UNTIL WE REACH THE LAST FULL WINDOW
		// We do a loop for the next points until the end of last full window
		for(int j = alpha+1; j < (timeSeries.length-alpha); j++){
			// add the point on the right 
			sum+= timeSeries[j+alpha];
			// remove the point on the left
			sum-= timeSeries[j-alpha-1];
					
			movingAverage[j] = sum / windowSize;
		}
		
		// NEXT POINTS UNTIL END OF TIME SERIES
		int pointsRemoved = 0;
		for(int j = (timeSeries.length-alpha); j < timeSeries.length; j++){
			// remove the previous point on the left
			sum-= timeSeries[j-alpha-1];
			
			pointsRemoved++;
			movingAverage[j] = sum / (windowSize-pointsRemoved);
		}
		
		
		if(DEBUG_MODE){
			System.out.println(" Number of data points = " + timeSeries.length);
			System.out.println(" Window size = " + windowSize);
			System.out.println(" Central Moving average transformation: " + Arrays.toString(movingAverage));
		}	
		
		return movingAverage;
	}


	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  Transform to Central Moving Average v2.21 - STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("===================================================");
	}

}