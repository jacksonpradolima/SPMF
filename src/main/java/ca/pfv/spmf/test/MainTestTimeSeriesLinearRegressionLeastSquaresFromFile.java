package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ca.pfv.spmf.algorithms.timeseries.TimeSeries;
import ca.pfv.spmf.algorithms.timeseries.reader_writer.AlgoTimeSeriesReader;
import ca.pfv.spmf.algorithms.timeseries.reader_writer.AlgoTimeSeriesWriter;
import ca.pfv.spmf.algorithms.timeseries.simplelinearregression.AlgoTimeSeriesLinearRegressionLeastSquare;

/**
 * Example of how to calculate the simple linear regression of one or more time series using
 * the source code of SPMF, by reading a time series file and writing the regression line 
 * (a time series) as output in a file
 * 
 * @author Philippe Fournier-Viger, 2016.
 */
public class MainTestTimeSeriesLinearRegressionLeastSquaresFromFile {

	public static void main(String [] arg) throws IOException{
		
		// the input file
		String input = fileToPath("contextSAX.txt");  
		// the output file
		String output = "./output.txt";
		
		// The separator to be used for reading/writting the input/output file
		String separator = ",";

		// (1) Read the time series
		AlgoTimeSeriesReader reader = new AlgoTimeSeriesReader();
		List<TimeSeries> multipleTimeSeries = reader.runAlgorithm(input, separator);
		
		// (2) Calculate the moving average of each time series
		List<TimeSeries> regressionLines = new ArrayList<TimeSeries>();
		for(TimeSeries timeSeries : multipleTimeSeries){
			AlgoTimeSeriesLinearRegressionLeastSquare algorithm = new AlgoTimeSeriesLinearRegressionLeastSquare();
			algorithm.trainModel(timeSeries);
			TimeSeries regressionLine = algorithm.calculateRegressionLine(timeSeries);
			regressionLines.add(regressionLine);
			algorithm.printStats();
		}
				
		// (3) write the time series to a file
		AlgoTimeSeriesWriter algorithm2 = new AlgoTimeSeriesWriter();
		algorithm2.runAlgorithm(output, regressionLines, separator);
		algorithm2.printStats();

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestTimeSeriesLinearRegressionLeastSquaresFromFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
