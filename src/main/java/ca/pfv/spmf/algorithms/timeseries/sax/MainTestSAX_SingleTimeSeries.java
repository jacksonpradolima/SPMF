package ca.pfv.spmf.algorithms.timeseries.sax;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Arrays;

import ca.pfv.spmf.algorithms.timeseries.TimeSeries;

/**
 * Example of how to use SAX algorithm for converting a time series to the SAX representation, in the source code
 * @author Philippe Fournier-Viger, 2016.
 */
public class MainTestSAX_SingleTimeSeries {

	public static void main(String [] arg) throws IOException{

		int numberOfSegments = 3;
		int numberOfSymbols = 3;
		
		// Create a time series
		double [] timeSeriesData = new double[]{1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};
		TimeSeries timeSeries = new TimeSeries(timeSeriesData, "SERIES1");
		
		// Set this variable to true to deactivate the PAA part of the SAX algorithm.
		boolean deactivatePAA = false;
		
		// Applying the  algorithm
		AlgoSAX algorithm = new AlgoSAX();
		SAXSymbol[] saxSequence = algorithm.runAlgorithm(timeSeries, numberOfSegments, numberOfSymbols, deactivatePAA);
		algorithm.printStats();
		
		// Print the list of SAX symbols
		SAXSymbol[] symbols = algorithm.getSymbols();
		System.out.println(" SAX SYMBOLS: ");
		System.out.println(" Symbols : " + Arrays.toString(symbols) + System.lineSeparator());
		
		// Print the sax sequences
		System.out.println(" SAX SEQUENCE : ");
		System.out.println(" Sequence : " + Arrays.toString(saxSequence));

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestSAX_SingleTimeSeries.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
