package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.timeseries.TimeSeries;
import ca.pfv.spmf.algorithms.timeseries.standardization.AlgoStandardization;

/**
 * Example of how to calculate the standardization of a time series, using
 * the source code of SPMF
 * 
 * @author Philippe Fournier-Viger, 2018.
 */
public class MainTestStandardization {

	public static void main(String [] arg) throws IOException{
		
		// Create a time series
		double [] dataPoints = new double[]{3.0,2.0,8.0,9.0,8.0,9.0,8.0,7.0,6.0,7.0,5.0,4.0,2.0,7.0,9.0,8.0,5.0};
		TimeSeries timeSeries = new TimeSeries(dataPoints, "SERIES1");
		
		// Applying the  algorithm
		AlgoStandardization algorithm = new AlgoStandardization();
		TimeSeries aSeries = algorithm.runAlgorithm(timeSeries);
		algorithm.printStats();
				
		// Print the moving average
		System.out.println(" Min Max Normalization: ");
		System.out.println(aSeries.toString());

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestStandardization.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
