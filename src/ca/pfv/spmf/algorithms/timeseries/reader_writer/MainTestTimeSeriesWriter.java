package ca.pfv.spmf.algorithms.timeseries.reader_writer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;

import ca.pfv.spmf.algorithms.timeseries.TimeSeries;

/**
 * Example of how to read time series from a file and write it to a file
 * @author Philippe Fournier-Viger, 2016.
 */
public class MainTestTimeSeriesWriter {

	public static void main(String [] arg) throws IOException{
		
		// the input file
		String input = fileToPath("contextSAX.txt");  
		String output = "./output.txt";

		// Parameters of the algorithm
		String separator = ",";
		
		// Applying the  algorithm
		AlgoTimeSeriesReader algorithm = new AlgoTimeSeriesReader();
		List<TimeSeries> timeSeries = algorithm.runAlgorithm(input, separator);
		algorithm.printStats();
		
		// print the time series
		System.out.println("TIME-SERIES");
		for(TimeSeries series : timeSeries){
			System.out.println(" "  + series);
		}
		
		// write the time series to a file
		AlgoTimeSeriesWriter algorithm2 = new AlgoTimeSeriesWriter();
		algorithm2.runAlgorithm(output, timeSeries, separator);
		algorithm2.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestTimeSeriesWriter.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
