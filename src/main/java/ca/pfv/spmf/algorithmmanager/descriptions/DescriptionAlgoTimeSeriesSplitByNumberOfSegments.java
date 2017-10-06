package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.algorithms.timeseries.TimeSeries;
import ca.pfv.spmf.algorithms.timeseries.reader_writer.AlgoTimeSeriesReader;
import ca.pfv.spmf.algorithms.timeseries.reader_writer.AlgoTimeSeriesWriter;
/* This file is copyright (c) 2008-2016 Philippe Fournier-Viger
* 
* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
* 
* SPMF is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* 
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/
import ca.pfv.spmf.algorithms.timeseries.split.AlgoSplitTimeSeries;

/**
 * This class describes the algorithm to split time series in a given 
 * number of segments.
 * 
 * @see AlgoSplitTimeSeries
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoTimeSeriesSplitByNumberOfSegments extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoTimeSeriesSplitByNumberOfSegments(){
	}

	@Override
	public String getName() {
		return "Split_time_series_by_number_of_segments";
	}

	@Override
	public String getAlgorithmCategory() {
		return "TIME SERIES MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#splittimeseriesnumbersegments";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {
		int numberOfSeries = getParamAsInteger(parameters[0]);

		String separator;
		if (parameters.length > 1 && "".equals(parameters[1]) == false) {
			separator = getParamAsString(parameters[1]);
		}else{
			separator = " ";
		}
		
		// (1) Read the time series
		AlgoTimeSeriesReader reader = new AlgoTimeSeriesReader();
		List<TimeSeries> multipleTimeSeries = reader.runAlgorithm(inputFile, separator);
		
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
		algorithm2.runAlgorithm(outputFile, allSplittedTimeSeries, separator);
		algorithm2.printStats();
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[2];
		parameters[0] = new DescriptionOfParameter("Number of segments", "(e.g. 2)", Integer.class, false);
		parameters[1] = new DescriptionOfParameter("separator", "(e.g. ',' , default: ' ')", String.class, true);

		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Philippe Fournier-Viger";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Time series database"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Time series database"};
	}
	
}
