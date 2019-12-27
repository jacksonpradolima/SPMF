package ca.pfv.spmf.algorithms.timeseries.autocorrelation;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ca.pfv.spmf.algorithms.timeseries.TimeSeries;
import ca.pfv.spmf.algorithms.timeseries.reader_writer.AlgoTimeSeriesReader;
import ca.pfv.spmf.algorithms.timeseries.reader_writer.AlgoTimeSeriesWriter;

/**
 * Example of how to calculate the autocorrelation function of a time series, using
 * the source code of SPMF, by reading a time series file and writing a time series file as output
 * 
 * @author Philippe Fournier-Viger, 2018.
 */
public class MainTestAutocorrelationFileToFile {

	public static void main(String [] arg) throws IOException{
		
		// the input file
		String input = fileToPath("contextAutocorrelation.txt");  
		// the output file
		String output = "./output.txt";
		// The separator to be used for reading/writting the input/output file
		String separator = ",";
		
		// the maximum lag to be used to generate the k-lag autocorrelation plot of a time series. 
		int maxlag = 16;

		// (1) Read the time series
		AlgoTimeSeriesReader reader = new AlgoTimeSeriesReader();
		List<TimeSeries> multipleTimeSeries = reader.runAlgorithm(input, separator);
		
		
		// (2) Calculate the exponential smoothing of each time series
		List<TimeSeries> resultMultipleTimeSeries = new ArrayList<TimeSeries>();
		for(TimeSeries timeSeries : multipleTimeSeries){
			AlgoLagAutoCorrelation algorithm = new AlgoLagAutoCorrelation();
			TimeSeries aSeries = algorithm.runAlgorithm(timeSeries, maxlag);
			resultMultipleTimeSeries.add(aSeries);
			algorithm.printStats();
		}
				
		// (3) write the time series to a file
		AlgoTimeSeriesWriter algorithm2 = new AlgoTimeSeriesWriter();
		algorithm2.runAlgorithm(output, resultMultipleTimeSeries, separator);
		algorithm2.printStats();

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestAutocorrelationFileToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
