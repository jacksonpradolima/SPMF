package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.timeseries.TimeSeries;
import ca.pfv.spmf.algorithms.timeseries.simplelinearregression.AlgoTimeSeriesLinearRegressionLeastSquare;

/**
 * Example of how to calculate the linear regression of a time series using
 * the least square method, from the source code of SPMF
 * 
 * @author Philippe Fournier-Viger, 2017.
 */
public class MainTestTimeSeriesLinearRegressionLeastSquare {

	public static void main(String [] arg) throws IOException{
		
		// Create a time series
		double [] dataPoints = new double[]{-1, -2.8, -3, -3, -3, -3.2, -2};
		TimeSeries timeSeries = new TimeSeries(dataPoints, "SERIES1");
		
		// Print the input data
		System.out.println("The input data is: ");
		System.out.println(" " + timeSeries.toString());
		System.out.println();
		
		// Train the regression model
		AlgoTimeSeriesLinearRegressionLeastSquare algorithm = new AlgoTimeSeriesLinearRegressionLeastSquare();
		algorithm.trainModel(timeSeries);
		 
		// Print statistics about model training
		algorithm.printStats();
		System.out.println();

		// Print the regression equation of the form : y(x) = bias + coefficient * x
		System.out.println("The following regression model is obtained: ");
		double bias = algorithm.getBias();
		double coefficient = algorithm.getCoefficient();
		System.out.println("  Y(x) = " + bias + " + " + coefficient + " * x \n");
		
		// Print the regression line
		TimeSeries regressionLine = algorithm.calculateRegressionLine(timeSeries);
		System.out.println("The regression line for the input data is: ");
		System.out.println(" " + regressionLine.toString());
		System.out.println();
		
		// Perform a prediction using the regression model
		System.out.println("We can use the model to make a prediction for a new value of x. \nFor example:");
		double prediction = algorithm.performPrediction(11d);
		System.out.println(" The prediction for x = 11 is  y = " + prediction);

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestTimeSeriesLinearRegressionLeastSquare.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
