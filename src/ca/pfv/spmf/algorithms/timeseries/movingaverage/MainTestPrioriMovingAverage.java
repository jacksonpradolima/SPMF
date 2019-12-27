package ca.pfv.spmf.algorithms.timeseries.movingaverage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.timeseries.TimeSeries;

/**
 * Example of how to calculate the moving average of a time series, using
 * the source code of SPMF
 * 
 * @author Philippe Fournier-Viger, 2016.
 */
public class MainTestPrioriMovingAverage {

	public static void main(String [] arg) throws IOException{

		// the number of data points that we want as output
		int windowSize = 3;
		
		// Create a time series
		double [] dataPoints = new double[]{3.0,2.0,8.0,9.0,8.0,9.0,8.0,7.0,6.0,7.0,5.0,4.0,2.0,7.0,9.0,8.0,5.0};
		TimeSeries timeSeries = new TimeSeries(dataPoints, "SERIES1");
		
		// Applying the  algorithm
		AlgoPriorMovingAverage algorithm = new AlgoPriorMovingAverage();
		TimeSeries movingAverageSeries = algorithm.runAlgorithm(timeSeries, windowSize);
		algorithm.printStats();
				
		// Print the moving average
		System.out.println(" Prior Moving average: ");
		System.out.println(movingAverageSeries.toString());

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestPrioriMovingAverage.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
