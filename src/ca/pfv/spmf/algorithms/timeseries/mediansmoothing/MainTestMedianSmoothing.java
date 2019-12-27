package ca.pfv.spmf.algorithms.timeseries.mediansmoothing;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.timeseries.TimeSeries;

/**
 * Example of how to calculate the median smoothing of a time series, using
 * the source code of SPMF
 * 
 * @author Philippe Fournier-Viger, 2016.
 */
public class MainTestMedianSmoothing {

	public static void main(String [] arg) throws IOException{
		
		// Create a time series
		double [] dataPoints = new double[]{3.0,2.0,8.0,9.0,8.0,9.0,8.0,7.0,6.0,7.0,5.0,4.0,2.0,7.0,9.0,8.0,5.0};
		TimeSeries timeSeries = new TimeSeries(dataPoints, "SERIES1");
		
		// The window size (must be an odd number)
		int windowSize = 3;
		
		// Applying the  algorithm
		AlgoMedianSmoothing algorithm = new AlgoMedianSmoothing();
		TimeSeries medianSmoothingSeries = algorithm.runAlgorithm(timeSeries, windowSize);
		algorithm.printStats();
				
		// Print the moving average
		System.out.println(" Median smoothing: ");
		System.out.println(medianSmoothingSeries.toString());

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestMedianSmoothing.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
