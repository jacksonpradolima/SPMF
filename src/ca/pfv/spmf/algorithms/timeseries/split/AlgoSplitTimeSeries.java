package ca.pfv.spmf.algorithms.timeseries.split;

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

import ca.pfv.spmf.algorithms.timeseries.TimeSeries;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * An algorithm to split a time series in several time series.
 * The algorithm takes as input a time series and the number of data points
 * to be used for each resulting time series.
 * 
 * @author Philippe Fournier-Viger, 2016
 */
public class AlgoSplitTimeSeries {
 
	/** the time the algorithm started */
	long startTimestamp = 0; 
	/** the time the algorithm terminated */
	long endTimestamp = 0;  
	
	/** This program will execute in DEBUG MODE if this variable is true */
	boolean DEBUG_MODE = false;
	
	/** The number of time-series created during the last execution of this algorithm */
	private int  numberOfSeries;
		
	/**
	 * Default constructor
	 */
	public AlgoSplitTimeSeries() {
	}

	/**
	 * Split the time series
	 * @param timeSeries a time series represented by a double array
	 * @param sizeOfSegment the number of data points that we want per time series
	 * @return  an array of time series (double array), thus a two-dimensional double array
	 */
	public TimeSeries[] runAlgorithm(TimeSeries timeSeries, int sizeOfSegment) {

		// check some error for parameters
		if(sizeOfSegment < 1){
			throw new IllegalArgumentException(" The size of segment should be > 1");
		}

		// reset memory logger
		MemoryLogger.getInstance().reset();
		
		// record the start time of the algorithm
		startTimestamp = System.currentTimeMillis();

		//======== SPLIT THE TIME SERIES ==/
		numberOfSeries =  (int) Math.ceil((double)timeSeries.data.length / sizeOfSegment);
		
		// IF in debug mode
		if(DEBUG_MODE){
			// Print the time series
			System.out.println(" The time series: " + timeSeries);
			System.out.println(" The length of the time series is: " + timeSeries.data.length);
			System.out.println(" The size of segment is : " + sizeOfSegment);
			System.out.println(" It will be splitted into " + numberOfSeries + " time series.");
		}
		
		TimeSeries[] splittedTimeSeries = splitTimeSeries(timeSeries, sizeOfSegment);

		// check the memory usage again and close the file.
		MemoryLogger.getInstance().checkMemory();
		// record end time
		endTimestamp = System.currentTimeMillis();

		return splittedTimeSeries;
	}
	
	/**
	 * Split a time series in a given number of segments
	 * @param number the number of segments
	 * @param timeSeries the time series to be splitted
	 * @return an array of time series (double array), thus a two-dimensional double array
	 */
	public TimeSeries[] runAlgorithm(int number, TimeSeries timeSeries){
		
		// check some error for parameters
		if(number < 1){
			throw new IllegalArgumentException(" The number of segments should be > 1");
		}

		// reset memory logger
		MemoryLogger.getInstance().reset();
		
		// record the start time of the algorithm
		startTimestamp = System.currentTimeMillis();
		
		// Calculate the size of segment
		int sizeOfSegment = (int) Math.ceil((double)timeSeries.size() / number);
		
		// save the number of time series for statistics
		numberOfSeries = number;
		
		// IF in debug mode
		if(DEBUG_MODE){
			// Print the time series
			System.out.println(" The time series: " + timeSeries);
			System.out.println(" The length of the time series is: " + timeSeries.data.length);
			System.out.println(" The size of segment is : " + sizeOfSegment);
			System.out.println(" It will be splitted into " + numberOfSeries + " time series.");
		}
		
		TimeSeries[] splittedTimeSeries = splitTimeSeries(timeSeries, sizeOfSegment);

		// check the memory usage again and close the file.
		MemoryLogger.getInstance().checkMemory();
		// record end time
		endTimestamp = System.currentTimeMillis();
	
		return splittedTimeSeries;
	}
	
	

	/**
	 * Split a time series according to the number of segments
	 * @param timeSeries the time series
	 * @param sizeOfSegment the number of segments
	 * @return an array of time series (double array), thus a two-dimensional double array
	 */
	private TimeSeries[] splitTimeSeries(TimeSeries timeSeries, int sizeOfSegment) {
		// Create the array for the splitted time series
		TimeSeries[] splittedTimeSeries = new TimeSeries[(int) Math.ceil(numberOfSeries)];
		
		// the current data point in the original time series
		int currentDataPoint = 0;
		
		// for each new time seires
		for(int i = 0; i < numberOfSeries; i++){
			// Calculate the number of data point for this time series
			int numberOfPoints = timeSeries.data.length - currentDataPoint;
			
			// If the number of points left is too much, we reduce it to the maximum
			if(numberOfPoints >= sizeOfSegment){
				numberOfPoints  = sizeOfSegment;
			}
			
			// create the array
			double[] dataPoints = new double[numberOfPoints];
			splittedTimeSeries[i] = 
					new TimeSeries(dataPoints, timeSeries.getName()+ "_PART" + i);
			
			// add each point
			for(int j=0; j < numberOfPoints; j++){
				double dataPoint = timeSeries.data[currentDataPoint++];
				splittedTimeSeries[i].data[j] = dataPoint;
			}
		}
		return splittedTimeSeries;
	}

	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  Split Time Series  ALGORITHM v2.06 - STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println(" The result is " + numberOfSeries + " time series.");
		System.out.println("===================================================");
	}



}