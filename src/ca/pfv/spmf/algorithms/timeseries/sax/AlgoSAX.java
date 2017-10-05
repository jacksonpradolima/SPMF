package ca.pfv.spmf.algorithms.timeseries.sax;

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
import java.util.List;

import ca.pfv.spmf.algorithms.timeseries.TimeSeries;
import ca.pfv.spmf.algorithms.timeseries.paa.AlgoPiecewiseAggregateApproximation;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * An implementation of the SAX algorithm to convert a time series to a sequence of symbols.<br/><br/>
 * 
 * It is based on the description in:<br/><br/>
 * 
 * Lin, Jessica, et al. "Experiencing SAX: a novel symbolic representation of time series. <br/>
 * Data Mining and knowledge discovery 15.2 (2007): 107-144.<br/>
 * 
 * @author Philippe Fournier-Viger, 2016
 * @see SAXSymbol
 * @see AlgoPiecewiseAggregateApproximation
 */
public class AlgoSAX {
 
	/** the time the algorithm started */
	long startTimestamp = 0; 
	/** the time the algorithm terminated */
	long endTimestamp = 0;  
	
	/** This program will execute in DEBUG MODE if this variable is true */
	boolean DEBUG_MODE = false;
	
	/** The symbols created for the last converted time series */
	SAXSymbol[] symbols;
	
	/** the last symbol that was used (to make sure that we don't give the same symbol to several time series **/
	int lastSymbol = 0;
	
	/** the number of time series that was processed during the last execution of the algorithm **/
	int timeSeriesCount = 0;
	
	/** the minimum number of symbols allowed by this sax implementation*/
	public static final int MIN_NUMBER_OF_SYMBOLS = 2;
	
	/** the maximum number of symbols allowed by this sax implementation*/
	public static final int MAX_NUMBER_OF_SYMBOLS = 30;
		
	/**
	 * Default constructor
	 */
	public AlgoSAX() {
	}

	/**
	 * Run the algorithm SAX algorithm on a single time series
	 * @param timeSeries a time series 
	 * @param numberOfSegments the number of segments ( > 1)
	 * @param numberOfSymbols  the number of symbols 
	 * @param deactivatePAA set this parameter to true to deactivate PAA before applying SAX
	 * @return the sax representation of the time series (an array of Symbol objects)
	 * @throws IOException exception if error while writing the file
	 */
	public SAXSymbol[] runAlgorithm(TimeSeries timeSeries, int numberOfSegments, int numberOfSymbols, boolean deactivatePAA) throws IOException {
		// check some error for parameters
		if(timeSeries.data.length < numberOfSegments){
			throw new IllegalArgumentException(" The number of segments should be less than or equal to the number of data points in the time series");
		}
		
		// check some error for parameters
		if(numberOfSegments < 2){
			throw new IllegalArgumentException(" This implementation of SAX only support a number of segments > 1");
		}
		
		if(numberOfSymbols < MIN_NUMBER_OF_SYMBOLS || numberOfSymbols > MAX_NUMBER_OF_SYMBOLS){
			throw new IllegalArgumentException(" This implementation of SAX only support a number of symbols between"+ MIN_NUMBER_OF_SYMBOLS + " to " + MAX_NUMBER_OF_SYMBOLS);
		}
		
		// reset memory logger
		MemoryLogger.getInstance().reset();
		
		// record the start time of the algorithm
		startTimestamp = System.currentTimeMillis();
		
		// reset the lastSymbol 
		lastSymbol = 0;

		// IF in debug mode
		if(DEBUG_MODE){
			// Print the time series
			System.out.println(" Time series: " + timeSeries);
		}
		
		//========================  CALCULATE THE MINIMUM, MAXIMUM, AVERAGE, VARIANCE AND STDDEV OF THIS TIME SERIES =======================
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		double average =  0;
		double variance = 0;
		double stdev = 0;
		
		// Calculate the minimum maximum and average
		for(double number : timeSeries.data){
			if(number > max){
				max = number;
			}
			if(number < min){
				min = number;
			}
			average += number;
		}
		
		average /= timeSeries.data.length;
		
		// Calculate variance
		for(double number : timeSeries.data){
			variance += Math.pow(average - number, 2);
		}
		variance = variance / timeSeries.data.length;
		
		// Calculate the standard deviation
		stdev = Math.sqrt(variance);				
		
		// IF in debug mode
		if(DEBUG_MODE){
			System.out.println(" --- Calculating statistics ---");
			System.out.println(" Min = " + min + " Max = " + max + " Average = " + average + 
					System.lineSeparator() + " Standard deviation = " + stdev + " Variance = " + variance);
		}
		
		//======================== CREATE THE PAA (Piecewise aggregate approximation) of this time series============
		// Create an array to store the P.A.A.
		AlgoPiecewiseAggregateApproximation paaAlgo = new AlgoPiecewiseAggregateApproximation();
		TimeSeries piecewiseTransformedData;
		if(deactivatePAA){
			piecewiseTransformedData = timeSeries;
		}else{
			piecewiseTransformedData = paaAlgo.runAlgorithm(timeSeries, numberOfSegments);
		}
		

		//======================  CREATE THE SYMBOLS ===============================
		// create the symbols
		symbols = createSAXSymbols(numberOfSymbols, average, stdev);
		
		//======================  TRANSFORM P.A.A  to the SAW REPRESENTATION ===============================
		SAXSymbol[] saxSequence = transformPAAtoSAXRepresentation(piecewiseTransformedData.data, symbols);
				
		// There is only one time series
		timeSeriesCount = 1;
		
		// check the memory usage again and close the file.
		MemoryLogger.getInstance().checkMemory();
		// record end time
		endTimestamp = System.currentTimeMillis();

		return saxSequence;
	}
	

	/**
	 * Run the SAX algorithm on multiple time series
	 * @param multipleTimeSeries a set of time series represented by a two-dimensional double array
	 * @param numberOfSegments the number of segments ( > 1)
	 * @param numberOfSymbols  the number of symbols 
	 * @param deactivatePAA Set this variable to true to deactivate the PAA part of the SAX algorithm.
	 * @return the sax representation of the time series (an array of Symbol objects)
	 * @throws IOException exception if error while writing the file
	 */
	public SAXSymbol[][] runAlgorithm(List<TimeSeries> multipleTimeSeries, int numberOfSegments,
			int numberOfSymbols, boolean deactivatePAA) throws IOException {
			
			// check some error for parameters
			if(numberOfSegments < 2){
				throw new IllegalArgumentException(" This implementation of SAX only support a number of segments > 1");
			}
			
			if(numberOfSymbols < 2 || numberOfSymbols >20){
				throw new IllegalArgumentException(" This implementation of SAX only support a number of symbols between 2 to 20");
			}
			
			// reset memory logger
			MemoryLogger.getInstance().reset();
			
			// record the start time of the algorithm
			startTimestamp = System.currentTimeMillis();
			
			// reset the lastSymbol 
			lastSymbol = 0;

			//========================  CALCULATE THE MINIMUM, MAXIMUM, AVERAGE, VARIANCE AND STDDEV OF THIS TIME SERIES =======================
			double min = Double.MAX_VALUE;
			double max = -Double.MAX_VALUE;
			double average =  0;
			double variance = 0;
			double stdev = 0;
			
			double dataPointCount = 0;
			
			// Calculate the minimdum maximum and average
			for(TimeSeries timeSeries : multipleTimeSeries){
				for(double number : timeSeries.data){
					if(number > max){
						max = number;
					}
					if(number < min){
						min = number;
					}
					
					average += number;
					
					dataPointCount++;
				}
			}
			
			average /= dataPointCount;
			
			// Calculate variance
			for(TimeSeries timeSeries : multipleTimeSeries){
				for(double dataPoint : timeSeries.data){
					variance += Math.pow(average - dataPoint, 2);
				}
			}
			
			variance = variance / dataPointCount;
			
			// Calculate the standard deviation
			stdev = Math.sqrt(variance);				
			
			// IF in debug mode
			if(DEBUG_MODE){
				System.out.println(" --- Calculating statistics ---");
				System.out.println(" Min = " + min + " Max = " + max + " Average = " + average + 
						System.lineSeparator() + " Standard deviation = " + stdev + " Variance = " + variance);
			}
			
			

			//======================  CREATE THE SAX SYMBOLS ===============================
			// create the symbols
			symbols = createSAXSymbols(numberOfSymbols, average, stdev);
			
			
			//======================== CREATE THE PAA (Piecewise aggregate representation) of this time series============

			// Remember the number of time series
			timeSeriesCount = multipleTimeSeries.size();				
			
			SAXSymbol[][] saxSequences = new SAXSymbol[timeSeriesCount][];
			
			for(int i = 0; i < multipleTimeSeries.size(); i++){
				TimeSeries timeSeries  = multipleTimeSeries.get(i);
				
				// IF in debug mode
				if(DEBUG_MODE){
					// Print the time series
					System.out.println(" ---------------------------------");
					System.out.println(" --- Processing Time series " + i + " ---- " + System.lineSeparator() + " Time series: " + timeSeries);
				}
								
				// Create an array to store the P.A.A.
				TimeSeries piecewiseTransformedData;
				if(deactivatePAA == false){
					AlgoPiecewiseAggregateApproximation paaAlgo = new AlgoPiecewiseAggregateApproximation();
					piecewiseTransformedData = paaAlgo.runAlgorithm(timeSeries, numberOfSegments);
				}else{
					piecewiseTransformedData = timeSeries;
				}
				//======================  TRANSFORM P.A.A  to the SAW REPRESENTATION ===============================
				saxSequences[i] = transformPAAtoSAXRepresentation(piecewiseTransformedData.data, symbols);

			}
			
			// check the memory usage again and close the file.
			MemoryLogger.getInstance().checkMemory();
			// record end time
			endTimestamp = System.currentTimeMillis();

			return saxSequences;
	}
	

	/**
	 * Get the list of all symbols used for converting the time series.
	 * @return the list of symbols or null if no time series has been converted yet.
	 */
	public SAXSymbol[] getSymbols() {
		return symbols;
	}

	/**
	 * Transform a time-series in PAA representation to its SAX representation
	 * @param piecewiseTransformedData the PAA representation (a double array)
	 * @param symbols the list of symbols to be used
	 * @return the SAX representation (a double array)
	 */
	private SAXSymbol[] transformPAAtoSAXRepresentation(
			double[] piecewiseTransformedData, SAXSymbol[] symbols) {
		SAXSymbol [] saxRepresentation = new SAXSymbol[piecewiseTransformedData.length];
		
		// for each point of the piecewise agregation time series
		for(int i=0; i< piecewiseTransformedData.length; i++){
			// for each symbol
			for(SAXSymbol symbol: symbols){
				// if it is this symbol
				if(piecewiseTransformedData[i] >= symbol.lowerBound 
						&& piecewiseTransformedData[i] < symbol.upperBound){
					// then we replace this point by its symbol
					saxRepresentation[i] = symbol;
				}
			}
		}
		
		// IF in debug mode
		if(DEBUG_MODE){
			System.out.println(" SAX representation : " + Arrays.toString(saxRepresentation));
			System.out.println();
		}
		return saxRepresentation;
	}

	/**
	 * This method generate the symbols to be used for the SAX representation
	 * @param numberOfSymbols  the number of symbols
	 * @param average  the average of the data points in the time series
	 * @param stdev    the standard deviation of the data points in the time series
	 * @return  an array of symbols
	 */
	private SAXSymbol[] createSAXSymbols(int numberOfSymbols, double average, 	double stdev) {
		// Create the array of Symbol
		SAXSymbol[] symbols = new SAXSymbol[numberOfSymbols];

		// We get the break points to be used for generating the symbols (see SAX paper)
		double[] breakpoints = getBreakpoints(numberOfSymbols);
		
		// For each symbol
		for(int i = 0; i < numberOfSymbols; i++){
			// We calculate the lower and upper bounds
			double lowerBound = average + (stdev* breakpoints[i]); 
			double upperBound = average + (stdev * breakpoints[i+1]);
			
			// Then we create the symbol
			int symbolName = ++lastSymbol;
			symbols[i] = new SAXSymbol(symbolName, lowerBound, upperBound);
		}
		
		// IF in debug mode
		if(DEBUG_MODE){
			System.out.println(System.lineSeparator() + " --- Creating the Symbols ---");
			System.out.println(" Symbols: " + Arrays.toString(symbols) + System.lineSeparator());
		}
		return symbols;
	}

	/**
	 * This method returns the breakpoints (defined in the SAX paper) according to the number of symbols
	 * choosen by the user. The breakpoint are chosen so that the area under a normal curve are equal (see the sax paper for details)
	 * To calculate this quickly, we use a table of breakpoints.
	 * @param numberOfSymbols the number of symbols chosen by the user
	 * @return an array of breakpoints
	 */
	private double[] getBreakpoints(int numberOfSymbols) {
		if(numberOfSymbols < 2 || numberOfSymbols >30){
			throw new IllegalArgumentException(" This implementation of SAX only support a number of symbols between 2 to 30");
		}
		
		switch (numberOfSymbols) {
		case 2:
			return new double[]{Double.NEGATIVE_INFINITY, 0, Double.POSITIVE_INFINITY};
		case 3:
			return new double[]{Double.NEGATIVE_INFINITY, -0.43, 0.43, Double.POSITIVE_INFINITY};
		case 4:
			return new double[]{Double.NEGATIVE_INFINITY, -0.67, 0, 0.67, Double.POSITIVE_INFINITY};
		case 5:
			return new double[]{Double.NEGATIVE_INFINITY,  -0.84, -0.25, 0.25, 0.84, Double.POSITIVE_INFINITY};
		case 6:
			return new double[]{Double.NEGATIVE_INFINITY,  -0.97, -0.43, 0, 0.43, 0.97, Double.POSITIVE_INFINITY};
		case 7:
			return new double[]{Double.NEGATIVE_INFINITY,  -1.07, -0.57, -0.18, 0.18, 0.57, 1.07, Double.POSITIVE_INFINITY};
		case 8:
			return new double[]{Double.NEGATIVE_INFINITY,  -1.15, -0.67, -0.32, 0, 0.32, 0.67, 1.15,  Double.POSITIVE_INFINITY};
		case 9:
			return new double[]{Double.NEGATIVE_INFINITY,  -1.22, -0.76, -0.43, -0.14, 0.14, 0.43, 0.76, 1.22,  Double.POSITIVE_INFINITY};
		case 10:
			return new double[]{Double.NEGATIVE_INFINITY,  -1.28, -0.84, -0.52, -0.25, 0, 0.25, 0.52, 0.84, 1.28,  Double.POSITIVE_INFINITY};
		case 11:
			return new double[]{Double.NEGATIVE_INFINITY,  -1.34, -0.91, -0.6, -0.35, -0.11, 0.11, 0.35, 0.6, 0.91, 1.34,   Double.POSITIVE_INFINITY};
		case 12:
			return new double[]{Double.NEGATIVE_INFINITY,  -1.38, -0.97, -0.67, -0.43, -0.21, 0, 0.21, 0.43, 0.67, 0.97, 1.38,   Double.POSITIVE_INFINITY};
		case 13:
			return new double[]{Double.NEGATIVE_INFINITY,  -1.43, -1.02, -0.74, -0.5, -0.29, -0.1, 0.1, 0.29, 0.5, 0.74, 1.02, 1.43,   Double.POSITIVE_INFINITY};
		case 14:
			return new double[]{Double.NEGATIVE_INFINITY,  -1.47, -1.07, -0.79, -0.57, -0.37, -0.18, 0, 0.18, 0.37, 0.57, 0.79, 1.07, 1.47,  Double.POSITIVE_INFINITY};
		case 15:
			return new double[]{Double.NEGATIVE_INFINITY, -1.5, -1.11, -0.84, -0.62, -0.43, -0.25, -0.08, 0.08, 0.25, 0.43, 0.62, 0.84, 1.11, 1.5,   Double.POSITIVE_INFINITY};
		case 16:
			return new double[]{Double.NEGATIVE_INFINITY,  -1.53, -1.15, -0.89, -0.67, -0.49, -0.32, -0.16, 0, 0.16, 0.32, 0.49, 0.67, 0.89, 1.15, 1.53,  Double.POSITIVE_INFINITY};
		case 17:
			return new double[]{Double.NEGATIVE_INFINITY,   -1.56, -1.19, -0.93, -0.72, -0.54, -0.38, -0.22, -0.07, 0.07, 0.22, 0.38, 0.54, 0.72, 0.93, 1.19, 1.56,   Double.POSITIVE_INFINITY};
		case 18:
			return new double[]{Double.NEGATIVE_INFINITY,  -1.59, -1.22, -0.97, -0.76, -0.59, -0.43, -0.28, -0.14, 0, 0.14, 0.28, 0.43, 0.59, 0.76, 0.97, 1.22, 1.59,    Double.POSITIVE_INFINITY};
		case 19:
			return new double[]{Double.NEGATIVE_INFINITY, -1.62, -1.25, -1, -0.8, -0.63, -0.48, -0.34, -0.2, -0.07, 0.07, 0.2, 0.34, 0.48, 0.63, 0.8, 1, 1.25, 1.62,   Double.POSITIVE_INFINITY};
		case 20:
			return new double[]{Double.NEGATIVE_INFINITY, -1.64, -1.28, -1.04, -0.84, -0.67, -0.52, -0.39, -0.25, -0.13, 0, 0.13, 0.25, 0.39, 0.52, 0.67, 0.84, 1.04, 1.28, 1.64,  Double.POSITIVE_INFINITY};
		case 21:
			return new double[]{Double.NEGATIVE_INFINITY, -1.668391, -1.309172,  -1.067571,  -0.876143,  -0.712443,  -0.565949, -0.430727,  -0.302980,  -0.180012,  -0.059717,   0.059717,   0.180012,  0.302980,   0.430727,   0.565949,   0.712443,   0.876143,   1.067571, 1.309172,   1.668391,  Double.POSITIVE_INFINITY};
		case 22:
			return new double[]{Double.NEGATIVE_INFINITY, -1.69062,  -1.33518, -1.09680,  -0.90846,  -0.74786,  -0.60459,  -0.47279, -0.34876,  -0.22988,  -0.11419,   0.00000,   0.11419,   0.22988,  0.34876, 0.47279,  0.60459,   0.74786,   0.90846,   1.09680,   1.33518,   1.69062,  Double.POSITIVE_INFINITY};
		case 23:
			return new double[]{Double.NEGATIVE_INFINITY, -1.711675,  -1.359737,  -1.124338,  -0.938814,  -0.781034,  -0.640667,  -0.511936,  -0.391196,  -0.275921,  -0.164211,  -0.054519,   0.054519, 0.164211,   0.275921,   0.391196,   0.511936,   0.640667,   0.781034, 0.938814,   1.124338,   1.359737,   1.711675,   
 Double.POSITIVE_INFINITY};
		case 24:
			return new double[]{Double.NEGATIVE_INFINITY, -1.73166,  -1.38299,  -1.15035,  -0.96742,  -0.81222,  -0.67449,  -0.54852, -0.43073,  -0.31864,  -0.21043,  -0.10463,   0.00000,   0.10463,   0.21043, 0.31864,   0.43073,   0.54852,   0.67449,   0.81222,   0.96742,   1.15035, 1.38299,  1.73166, Double.POSITIVE_INFINITY};
		case 25:
			return new double[]{Double.NEGATIVE_INFINITY,  -1.750686,  -1.405072,  -1.174987,  -0.994458,  -0.841621,  -0.706303,   -0.582842,  -0.467699,  -0.358459,  -0.253347,  -0.150969,  -0.050154, 0.050154,   0.150969,   0.253347,   0.358459,   0.467699,   0.582842,  0.706303,   0.841621,   0.994458,   1.174987,   1.405072,   1.750686,
 Double.POSITIVE_INFINITY};
		case 26:
			return new double[]{Double.NEGATIVE_INFINITY, -1.76883,  -1.42608,  -1.19838,  -1.02008,  -0.86942,  -0.73632,  -0.61514, -0.50240,  -0.39573,  -0.29338,  -0.19403,  -0.09656,   0.00000,   0.09656, 0.19403,   0.29338,   0.39573,   0.50240,   0.61514,   0.73632,   0.86942, 1.02008,   1.19838,   1.42608,   1.76883,
 Double.POSITIVE_INFINITY};
		case 27:
			return new double[]{Double.NEGATIVE_INFINITY,  -1.786156,  -1.446104,  -1.220640,  -1.044409,  -0.895780,  -0.764710,   -0.645631,  -0.535083,  -0.430727,  -0.330873,  -0.234219,  -0.139710, -0.046436,   0.046436,   0.139710,   0.234219,   0.330873,   0.430727,   0.535083,   0.645631,   0.764710,   0.895780,   1.044409,   1.220640,   1.446104,   1.786156, Double.POSITIVE_INFINITY};
		case 28:
			return new double[]{Double.NEGATIVE_INFINITY, -1.80274,  -1.46523,  -1.24187,  -1.06757,  -0.92082,  -0.79164,  -0.67449, -0.56595,  -0.46371,  -0.36611,  -0.27188,  -0.18001,  -0.08964,   0.00000, 0.08964,   0.18001,   0.27188,   0.36611,   0.46371,   0.56595,   0.67449,0.79164,   0.92082,   1.06757,   1.24187,   1.46523,   1.80274,
Double.POSITIVE_INFINITY};	
		case 29:
			return new double[]{Double.NEGATIVE_INFINITY, -1.818646,  -1.483540,  -1.262145,  -1.089662,  -0.944670,  -0.817237,  -0.701873,  -0.595179,  -0.494873,  -0.399323,  -0.307293,  -0.217798,  -0.130019,  -0.043231,   0.043231,   0.130019,   0.217798,   0.307293,   0.399323,  0.494873,   0.595179,   0.701873,   0.817237,   0.944670,   1.089662,   1.262145,   1.483540,   1.818646,
Double.POSITIVE_INFINITY};
		case 30:
			return new double[]{Double.NEGATIVE_INFINITY, -1.83391,  -1.50109,  -1.28155,  -1.11077,  -0.96742,  -0.84162,  -0.72791,  -0.62293,  -0.52440,  -0.43073,  -0.34069,  -0.25335,  -0.16789,  -0.08365,   0.00000,   0.08365,   0.16789,   0.25335,   0.34069,   0.43073,   0.52440,   0.62293,   0.72791,   0.84162,   0.96742,   1.11077,   1.28155,   1.50109,    1.83391,
Double.POSITIVE_INFINITY};	       		          
		}
		
		//  ====================================================================================================
		// NOTE :  If more breakpoints are needed, they can be generated using the following Matlab/Octave code if needed:
		//  ====================================================================================================
		//startRange = 2;
		// stdc= 1;
		// endRange = 512;
		//
		// table = cell(endRange-startRange,1);
		//  for r=startRange:endRange
		//    table{r-startRange+1} = norminv((1:r-1)/r,0,stdc);
		//  end
		//	================  The above code is from the Eamonn Keoh website.

		
		// This should never happen
		return null;
	}

	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  SAX  ALGORITHM v2.10 - STATS =============");
		System.out.println(" Number of time series processed: " + timeSeriesCount);
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("===================================================");
	}

}