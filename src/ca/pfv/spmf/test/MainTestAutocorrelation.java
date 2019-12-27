package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.timeseries.TimeSeries;
import ca.pfv.spmf.algorithms.timeseries.autocorrelation.AlgoLagAutoCorrelation;

/**
 * Example of how to calculate the  autocorrelation function of a time series, using
 * the source code of SPMF
 * 
 * @author Philippe Fournier-Viger, 2018.
 */
public class MainTestAutocorrelation {

	public static void main(String [] arg) throws IOException{

		// the maximum lag to be used to generate the k-lag autocorrelation plot of a time series. 
		int maxlag = 16;
		
		// Create a time series
		double [] dataPoints = new double[]{3.0,2.0,8.0,9.0,8.0,9.0,8.0,7.0,6.0,7.0,5.0,4.0,2.0,7.0,9.0,8.0,5.0};
		TimeSeries timeSeries = new TimeSeries(dataPoints, "SERIES1");
		
		// Applying the  algorithm
		AlgoLagAutoCorrelation algorithm = new AlgoLagAutoCorrelation();
		TimeSeries aSeries = algorithm.runAlgorithm(timeSeries,maxlag);
		algorithm.printStats();
				
		// Print the autocorrelation time series
		System.out.println(" Auto-correlation for lag: 1 to : " + maxlag);
		System.out.println(aSeries.toString());

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestAutocorrelation.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
