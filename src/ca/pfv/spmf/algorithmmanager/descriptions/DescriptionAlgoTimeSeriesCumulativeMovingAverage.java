package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.algorithms.timeseries.TimeSeries;
import ca.pfv.spmf.algorithms.timeseries.movingaverage.AlgoCumulativeMovingAverage;
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

/**
 * This class describes the algorithm to calculate the moving average of a time series
 * 
 * @see AlgoCumulativeMovingAverage
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoTimeSeriesCumulativeMovingAverage extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoTimeSeriesCumulativeMovingAverage(){
	}

	@Override
	public String getName() {
		return "Calculate_cumulative_moving_average_of_time_series";
	}

	@Override
	public String getAlgorithmCategory() {
		return "TIME SERIES MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/TimeSeriesCumulativeMovingAverage.php";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {

		String separator;
		if (parameters.length > 0 && "".equals(parameters[0]) == false) {
			separator = getParamAsString(parameters[0]);
		}else{
			separator = " ";
		}

		// (1) Read the time series
		AlgoTimeSeriesReader reader = new AlgoTimeSeriesReader();
		List<TimeSeries> multipleTimeSeries = reader.runAlgorithm(inputFile, separator);
		
		
		// (2) Calculate the cumulative moving average of each time series
		List<TimeSeries> movingAveragemultipleTimeSeries = new ArrayList<TimeSeries>();
		for(TimeSeries timeSeries : multipleTimeSeries){
			AlgoCumulativeMovingAverage algorithm = new AlgoCumulativeMovingAverage();
			TimeSeries movingAverageSeries = algorithm.runAlgorithm(timeSeries);
			movingAveragemultipleTimeSeries.add(movingAverageSeries);
			algorithm.printStats();
		}
				
		// (3) write the time series to a file
		AlgoTimeSeriesWriter algorithm2 = new AlgoTimeSeriesWriter();
		algorithm2.runAlgorithm(outputFile, movingAveragemultipleTimeSeries, separator);
		algorithm2.printStats();
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[1];
		parameters[0] = new DescriptionOfParameter("separator", "(e.g. ',' , default: ' ')", String.class, true);

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
