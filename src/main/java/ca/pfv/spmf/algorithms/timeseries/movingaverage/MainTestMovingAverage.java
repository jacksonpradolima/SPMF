package ca.pfv.spmf.algorithms.timeseries.movingaverage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Arrays;

import ca.pfv.spmf.algorithms.timeseries.TimeSeries;

/**
 * Example of how to calculate the moving average of a time series, using
 * the source code of SPMF
 * 
 * @author Philippe Fournier-Viger, 2016.
 */
public class MainTestMovingAverage {

	public static void main(String [] arg) throws IOException{

		// the number of data points that we want as output
		int windowSize = 4;
		
		// Create a time series
		double [] dataPoints = new double[]{1.0, 4.5, 6.0, 4.0, 3.0, 4.0, 5.0, 4.0, 3.0, 2.0};;
		TimeSeries timeSeries = new TimeSeries(dataPoints, "SERIES1");
		
		// Applying the  algorithm
		AlgoMovingAverage algorithm = new AlgoMovingAverage();
		TimeSeries movingAverageSeries = algorithm.runAlgorithm(timeSeries, windowSize);
		algorithm.printStats();
				
		// Print the moving average
		System.out.println(" Moving average: ");
		System.out.println(movingAverageSeries.toString());

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestMovingAverage.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
