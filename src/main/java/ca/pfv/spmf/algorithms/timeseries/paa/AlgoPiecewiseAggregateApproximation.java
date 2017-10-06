package ca.pfv.spmf.algorithms.timeseries.paa;

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
import ca.pfv.spmf.algorithms.timeseries.sax.AlgoSAX;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * An implementation of the algorithm to generate the Piecewise Aggregate Approximation of a time series
 * 
 * It is based on the description in:<br/><br/>
 * 
 * Lin, Jessica, et al. "Experiencing SAX: a novel symbolic representation of time series. <br/>
 * Data Mining and knowledge discovery 15.2 (2007): 107-144.<br/>
 * 
 * This algorithm is typically used as a first step before applying the SAX algorithm
 * 
 * @author Philippe Fournier-Viger, 2016
 * @see AlgoSAX
 */
public class AlgoPiecewiseAggregateApproximation {
 
	/** the time the algorithm started */
	long startTimestamp = 0; 
	/** the time the algorithm terminated */
	long endTimestamp = 0;  
	
	/** This program will execute in DEBUG MODE if this variable is true */
	boolean DEBUG_MODE = false;
		
	/**
	 * Default constructor
	 */
	public AlgoPiecewiseAggregateApproximation() {
	}

	/**
	 * Generate the Piecewise Aggregate Approximation of a time series
	 * @param timeSeries a time series 
	 * @param numberOfSegments the number of segments ( > 1)
	 * @return the PAA representation of the time series (an array of double)
	 * @throws IOException exception if error while writing the file
	 */
	public TimeSeries runAlgorithm(TimeSeries timeSeries, int numberOfSegments) throws IOException {
		// check some error for parameters
		if(timeSeries.data.length < numberOfSegments){
			throw new IllegalArgumentException(" The number of segments should be less than or equal to the number of data points in the time series");
		}
		
		// check some error for parameters
		if(numberOfSegments < 2){
			throw new IllegalArgumentException(" This implementation only support a number of segments > 1");
		}

		// reset memory logger
		MemoryLogger.getInstance().reset();
		
		// record the start time of the algorithm
		startTimestamp = System.currentTimeMillis();


		// IF in debug mode
		if(DEBUG_MODE){
			// Print the time series
			System.out.println(" Time series: " + timeSeries.toString());
		}

		//======================== CREATE THE PAA (Piecewise aggregate approximation) of this time series============
		// Create an array to store the P.A.A.
		double[] piecewiseTransformedData = transformTimeSeriesToPAARepresentation(timeSeries.data, numberOfSegments);
		TimeSeries paaSeries = new TimeSeries(piecewiseTransformedData, 
				timeSeries.getName() + "_PAA");
		
		
		// check the memory usage again and close the file.
		MemoryLogger.getInstance().checkMemory();
		// record end time
		endTimestamp = System.currentTimeMillis();

		return paaSeries;
	}
	
	/**
	 * This method calculates the Piecewise Agregation Approximation (PAA) representation
	 * of a time-series.
	 * @param dataPoints  the data points of the time series (an array of double)
	 * @param numberOfSegments  the number of segments to be used
	 * @return the PAA representation (an array of double)
	 */
	private double[] transformTimeSeriesToPAARepresentation(double[] dataPoints, int numberOfSegments) {
		double[] piecewiseTransformedData = new double[numberOfSegments];
		
		// Get  the size of each segment
		double segmentSize = dataPoints.length  / (double)numberOfSegments;
		double wholePartSegmentSize = Math.floor(segmentSize);
		
		boolean isExactlyDivisible = segmentSize == (int) segmentSize;
		
		// FOR THE FIRST SEGMENT
		double currentPoint = 0;
		
		// First we add all the full data points to the sum of the first segment
		for(; currentPoint < wholePartSegmentSize; currentPoint++){
			piecewiseTransformedData[0] += dataPoints[(int)currentPoint];
		}
		
		// Then if the last data point is partial
		if(isExactlyDivisible == false){
			// we calculate the weight of that point
			double weight = segmentSize - Math.floor(segmentSize);
		
			// then we multiply the data point by that weight and add it to the sum for that segment
			piecewiseTransformedData[0] += dataPoints[(int)currentPoint] * weight;
			
			currentPoint += weight;
		}
		
		// Then calculate the average for that segment
		piecewiseTransformedData[0]  /= segmentSize;
		
		// NOW WE DO THE SAME THING FOR THE FOLLOWING SEGMENTS

		
		for(int currentSegment = 1; currentSegment < numberOfSegments; currentSegment++){
			
			double remainingSegmentSize = segmentSize;

			boolean currenPointIsExactlyDivisible = currentPoint == (int) currentPoint;
			
			// If the first data point is not divisible
			if(currenPointIsExactlyDivisible == false){
				// we calculate the weight of that point
				double weight = Math.ceil(currentPoint) - currentPoint;
			
				// then we multiply the data point by that weight and add it to the sum for that segment
				piecewiseTransformedData[currentSegment] += dataPoints[(int)currentPoint] * weight;
				
				currentPoint += weight;
				
				remainingSegmentSize -= weight;
			}

			// Then we process the following data points

			for(; remainingSegmentSize>=1; remainingSegmentSize--, currentPoint++){
				piecewiseTransformedData[currentSegment] += dataPoints[(int)currentPoint];
			}

			// Then if the last data point is partial
			if(remainingSegmentSize > 0 && currentPoint < dataPoints.length){
				// we calculate the weight of that point
				double weight = remainingSegmentSize;
			
				// then we multiply the data point by that weight and add it to the sum for that segment
				piecewiseTransformedData[currentSegment] += dataPoints[(int)currentPoint] * weight;
				
				currentPoint += weight;
			}
			
			// Then calculate the average for that segment
			piecewiseTransformedData[currentSegment]  /= segmentSize;
		}

		if(DEBUG_MODE){
			System.out.println(System.lineSeparator() + " --- Dividing time series into segments ---");
			System.out.println(" Number of data points = " + dataPoints.length);
			System.out.println(" Number of segments = " + numberOfSegments);
			System.out.println(" Segment size = " + segmentSize);
			System.out.println(" Is exactly divisible into segment? " + isExactlyDivisible);
			System.out.println(" Piecewise-Aggregate-transformation: " + Arrays.toString(piecewiseTransformedData));
		}	
		
		return piecewiseTransformedData;
	}


	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  Transform To PAA  ALGORITHM v2.05- STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("===================================================");
	}

}