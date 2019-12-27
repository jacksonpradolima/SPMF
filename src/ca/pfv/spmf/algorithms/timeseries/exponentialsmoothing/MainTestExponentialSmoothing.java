package ca.pfv.spmf.algorithms.timeseries.exponentialsmoothing;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.timeseries.TimeSeries;

/**
 * Example of how to calculate the exponential smoothing of a time series, using
 * the source code of SPMF
 * 
 * @author Philippe Fournier-Viger, 2016.
 */
public class MainTestExponentialSmoothing {

	public static void main(String [] arg) throws IOException{

		// the smoothing constant (a double representing a percentage between 0 and 1)
		double alpha = 1;
		
		// Create a time series
		double [] dataPoints = new double[]{1.0, 4.5, 6.0, 4.0, 3.0, 4.0, 5.0, 4.0, 3.0, 2.0};;
		TimeSeries timeSeries = new TimeSeries(dataPoints, "SERIES1");
		
		// Applying the  algorithm
		AlgoExponentialSmoothing algorithm = new AlgoExponentialSmoothing();
		TimeSeries aSeries = algorithm.runAlgorithm(timeSeries, alpha);
		algorithm.printStats();
				
		// Print the moving average
		System.out.println(" Exponential smoothing: ");
		System.out.println(aSeries.toString());

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestExponentialSmoothing.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
