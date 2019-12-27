package ca.pfv.spmf.algorithms.timeseries.movingaverage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.timeseries.TimeSeries;

/**
 * Example of how to calculate the central moving average of a time series, using
 * the source code of SPMF
 * 
 * @author Philippe Fournier-Viger, 2016.
 */
public class MainTestCentralMovingAverage {

	public static void main(String [] arg) throws IOException{
		
		// Create a time series
		double [] dataPoints = new double[]{3.0,2.0,8.0,9.0,8.0,9.0,8.0,7.0,6.0,7.0,5.0,4.0,2.0,7.0,9.0,8.0,5.0};
		TimeSeries timeSeries = new TimeSeries(dataPoints, "SERIES1");
		
		// The window size (must be an odd number)
		int windowSize = 3;
		
		// Applying the  algorithm
		AlgoCentralMovingAverage algorithm = new AlgoCentralMovingAverage();
		TimeSeries movingAverageSeries = algorithm.runAlgorithm(timeSeries, windowSize);
		algorithm.printStats();
				
		// Print the moving average
		System.out.println(" Central Moving average: ");
		System.out.println(movingAverageSeries.toString());

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestCentralMovingAverage.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
