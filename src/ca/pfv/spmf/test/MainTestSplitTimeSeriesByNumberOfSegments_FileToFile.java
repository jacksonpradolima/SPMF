package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ca.pfv.spmf.algorithms.timeseries.TimeSeries;
import ca.pfv.spmf.algorithms.timeseries.reader_writer.AlgoTimeSeriesReader;
import ca.pfv.spmf.algorithms.timeseries.reader_writer.AlgoTimeSeriesWriter;
import ca.pfv.spmf.algorithms.timeseries.split.AlgoSplitTimeSeries;

/**
 * Example of how to split time series by length, using
 * the source code of SPMF, by reading a time series file and writing a time series file as output
 * 
 * @author Philippe Fournier-Viger, 2016.
 */
public class MainTestSplitTimeSeriesByNumberOfSegments_FileToFile {

	public static void main(String [] arg) throws IOException{
		
		// the input file
		String input = fileToPath("contextSAX.txt");  
		// the output file
		String output = "./output.txt";
		
		// the number of time series that we want
		int numberOfSeries = 2;
		
		// The separator to be used for reading/writting the input/output file
		String separator = ",";

		// (1) Read the time series
		AlgoTimeSeriesReader reader = new AlgoTimeSeriesReader();
		List<TimeSeries> multipleTimeSeries = reader.runAlgorithm(input, separator);
		
		// (2) Calculate the moving average of each time series
		List<TimeSeries> allSplittedTimeSeries = new ArrayList<TimeSeries>(multipleTimeSeries.size()*numberOfSeries);
		for(TimeSeries timeSeries : multipleTimeSeries){
			AlgoSplitTimeSeries algorithm = new AlgoSplitTimeSeries();
			TimeSeries[] splittedTimeSeries =  algorithm.runAlgorithm(numberOfSeries, timeSeries);
			algorithm.printStats();
			
			// not very efficient to do that...
			for(TimeSeries series: splittedTimeSeries){
				allSplittedTimeSeries.add(series);
			}
		}
				
		// (3) write the time series to a file
		AlgoTimeSeriesWriter algorithm2 = new AlgoTimeSeriesWriter();
		algorithm2.runAlgorithm(output, allSplittedTimeSeries, separator);
		algorithm2.printStats();

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestSplitTimeSeriesByNumberOfSegments_FileToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
