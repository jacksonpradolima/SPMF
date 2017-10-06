package ca.pfv.spmf.algorithms.timeseries.simplelinearregression;

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


import java.util.Arrays;

import ca.pfv.spmf.algorithms.timeseries.TimeSeries;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * An algorithm to calculate the simple linear regression of a time series
 * using the least squares method. 
 * This generates a model of the form y(x) = bias + coefficient * x.
 * 
 * @author Philippe Fournier-Viger, 2017
 */
public class AlgoTimeSeriesLinearRegressionLeastSquare {
 
	/** the time the algorithm started */
	long startTimestamp = 0; 
	/** the time the algorithm terminated */
	long endTimestamp = 0;  
	
	/** This program will execute in DEBUG MODE if this variable is true */
	boolean DEBUG_MODE = false;
	
	/** The bias of the equation y(x) = bias + coefficient * x    */
	double bias = 0d;
	
	/** The coefficient of the equation y(x) = bias + coefficient * x   */
	double coefficient = 0d;
		
	/**
	 * Default constructor
	 */
	public AlgoTimeSeriesLinearRegressionLeastSquare() {
		
	}

	/**
	 * Train a linear regression model for a given time series using the least squares method.
	 * @param timeSeries a time series 
	 */
	public void trainModel(TimeSeries timeSeries) {

		// reset memory logger
		MemoryLogger.getInstance().reset();
		
		// record the start time of the algorithm
		startTimestamp = System.currentTimeMillis();

		// IF in debug mode
		if(DEBUG_MODE){
			// Print the time series
			System.out.println(" Time series: " + Arrays.toString(timeSeries.data));
		} 

		// Train the model
		trainRegressionModel(timeSeries.data);

		// check the memory usage again and close the file.
		MemoryLogger.getInstance().checkMemory();
		
		// record end time
		endTimestamp = System.currentTimeMillis();
	}
	
	/**
	 * Calculate the regression line corresponding to a time series
	 * @param series  a time series
	 * @param the regression line corresponding to the time series (a TimeSeries object)
	 */
	public TimeSeries calculateRegressionLine(TimeSeries series){
		// Obtain the data
		double[] timeSeries = series.data;

		// Create an array to store the  regression line 
		double[] regressionLine = new double[timeSeries.length];

		// Calculate the regression line based on the input data
		for (int i = 0; i < timeSeries.length; i++) {
			regressionLine[i] = performPrediction(i);
		}
		
		if(DEBUG_MODE){
			System.out.println(" Time-series obtained by the regression: " + Arrays.toString(regressionLine));
		}	
		
		// Return the result as a TimeSeries object
		return  new TimeSeries(regressionLine,  series.getName() + "_LR");
	}
	
	/**
	 * Generate a simple regression of a time series (an equation of the form
	 *   y = bias + coefficient*x
	 * 
	 * @param timeSeries a time series represented by a double array
	 * @param windowSize the number of data point inside the Window ( > 1)
	 * @return the moving average ( a time series)
	 */
	private void trainRegressionModel(double[] timeSeries) {
		
		// (1) Calculate the average of X values
		double sumXvalues = 0d;
		for (int i = 0; i < timeSeries.length; i++) {
			sumXvalues += timeSeries[i]; // x value
		}
		double averageXvalues = sumXvalues / timeSeries.length;

		// (2) Calculate the average of Y values
		double sumYvalues = 0d;
		for (int i = 0; i < timeSeries.length; i++) {
			sumYvalues += i; // y value
		}
		double averageYvalues = sumYvalues / timeSeries.length;

		// (3) calculate the least squares to obtain the bias and coefficient
		double sumOfErrorsXwithX = 0d;
		double sumOfErrorsXwithY = 0d;
		
		// For each position in the time series
		for (int i = 0; i < timeSeries.length; i++) {
			// Calculate the difference between  xi and the average
			double xi = timeSeries[i];
			double difference = (xi - averageXvalues);
			
			// Calculate the square error for Xi
			sumOfErrorsXwithX += difference * difference;

			// Calculate the suqare error for Yi
			double yi = i;
			sumOfErrorsXwithY += difference * (yi - averageYvalues);
		}

		 coefficient = sumOfErrorsXwithY / sumOfErrorsXwithX;
		 bias = averageYvalues - (coefficient * averageXvalues);

		// print results
		if(DEBUG_MODE){
			System.out.println(" Number of data points = " + timeSeries.length);
			System.out.println(" Regression line is: ");
			System.out.println("  Y(x) = " + bias + " + " + coefficient + " * x");
		}	
	}
	
	/**
	 * Perform a prediction using the current regression model.
	 * This assumes that the model has  been trained using the 
	 * calculateRegression() method.
	 * @param x a double value
	 * @return a prediction calculating using the model  y(x) = bias + coefficient * x, or 0 if the model has not been trained.
	 */
	public double performPrediction(double x){
		return bias + x * coefficient;
	}
	
	/**
	 * Get the bias of the trained model.
	 * This assumes that the model has  been trained using the 
	 * calculateRegression() method.
	 * @return the bias of the equation  y(x) = bias + coefficient * x,  or 0 if the model has not been trained.
	 */
	public double getBias() {
		return bias;
	}
	
	/**
	 * Get the coefficient of the trained model.
	 * This assumes that the model has  been trained using the 
	 * calculateRegression() method.
	 * @return the coefficient of the equation  y(x) = bias + coefficient * x,  or 0 if the model has not been trained.
	 */
	public double getCoefficient() {
		return coefficient;
	}


	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  Linear regression (least squares) v2.19- STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("===================================================");
	}

}