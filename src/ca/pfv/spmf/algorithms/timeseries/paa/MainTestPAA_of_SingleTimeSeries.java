package ca.pfv.spmf.algorithms.timeseries.paa;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Arrays;

import ca.pfv.spmf.algorithms.timeseries.TimeSeries;

/**
 * Example of how to calculate the Piecewise Aggregate Approximation of a time series, using
 * the source code of SPMF
 * 
 * @author Philippe Fournier-Viger, 2016.
 */
public class MainTestPAA_of_SingleTimeSeries {

	public static void main(String [] arg) throws IOException{

		// the number of data points that we want as output
		int numberOfSegments = 3;
		
		// Create a time series
		double [] dataPoints = new double[]{1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};
		TimeSeries timeSeries = new TimeSeries(dataPoints, "SERIES1");
		
		// Applying the  algorithm
		AlgoPiecewiseAggregateApproximation algorithm = new AlgoPiecewiseAggregateApproximation();
		TimeSeries paaTimeSeries = algorithm.runAlgorithm(timeSeries, numberOfSegments);
		algorithm.printStats();
				
		// Print the PAA 
		System.out.println(" Piecewise Aggregation Approximation: ");
		System.out.println(paaTimeSeries.toString());

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestPAA_of_SingleTimeSeries.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
