package ca.pfv.spmf.algorithms.timeseries.normalization;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.timeseries.TimeSeries;

/**
 * Example of how to calculate the min max normalization of a time series, using
 * the source code of SPMF
 * 
 * @author Philippe Fournier-Viger, 2017.
 */
public class MainTestMinMaxNormalization {

	public static void main(String [] arg) throws IOException{
		
		// Create a time series
		double [] dataPoints = new double[]{1.0, 4.5, 6.0, 4.0, 3.0, 4.0, 5.0, 4.0, 3.0, 2.0};;
		TimeSeries timeSeries = new TimeSeries(dataPoints, "SERIES1");
		
		// Applying the  algorithm
		AlgoMinMaxNormalization algorithm = new AlgoMinMaxNormalization();
		TimeSeries aSeries = algorithm.runAlgorithm(timeSeries);
		algorithm.printStats();
				
		// Print the moving average
		System.out.println(" Min Max Normalization: ");
		System.out.println(aSeries.toString());

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestMinMaxNormalization.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
