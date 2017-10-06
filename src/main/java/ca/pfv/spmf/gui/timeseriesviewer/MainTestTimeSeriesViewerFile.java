package ca.pfv.spmf.gui.timeseriesviewer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;

import ca.pfv.spmf.algorithms.timeseries.TimeSeries;
import ca.pfv.spmf.algorithms.timeseries.reader_writer.AlgoTimeSeriesReader;

/**
 * Example of how to view a time series, from the source code of SPMF.
 * @author Philippe Fournier-Viger, 2016.
 */
public class MainTestTimeSeriesViewerFile {

	public static void main(String [] arg) throws IOException{
		
		// the input file
		String input = fileToPath("contextSAX.txt");    // contextSAX_SplitLength3  // 

		// Parameters of the algorithm
		String separator = ",";

		AlgoTimeSeriesReader reader = new AlgoTimeSeriesReader();
		List<TimeSeries> timeSeries = reader.runAlgorithm(input, separator);

		TimeSeriesViewer viewer = new TimeSeriesViewer(timeSeries);
		viewer.setVisible(true);
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestTimeSeriesViewerFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
