package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.algorithms.timeseries.TimeSeries;
import ca.pfv.spmf.algorithms.timeseries.reader_writer.AlgoTimeSeriesReader;
import ca.pfv.spmf.algorithms.timeseries.sax.AlgoConvertTimeSeriesFileToSequencesWithSAX;
import ca.pfv.spmf.gui.PreferencesManager;
import ca.pfv.spmf.tools.dataset_converter.SequenceDatabaseConverter;
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
 * This class describes the algorithm to convert a timeseries to a sequence database using the SAX algorithm
 * 
 * @see SequenceDatabaseConverter
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoConvertTimeSeriesToSequenceWithSAX extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoConvertTimeSeriesToSequenceWithSAX(){
	}

	@Override
	public String getName() {
		return "Convert_time_series_to_sequence_database_using_SAX";
	}

	@Override
	public String getAlgorithmCategory() {
		return "TIME SERIES MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#convertsax";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {
		int numberOfSegments = getParamAsInteger(parameters[0]);
		int numberOfSymbols = getParamAsInteger(parameters[1]);
		String separator = getParamAsString(parameters[2]);
		boolean deactivatePAA = false;
		if (parameters.length > 3 && "".equals(parameters[3]) == false) {
			deactivatePAA = getParamAsBoolean(parameters[3]);
		}
		
		// Get the text encoding
		Charset charset = PreferencesManager.getInstance().getPreferedCharset();

		// Applying the  algorithm
		AlgoTimeSeriesReader reader = new AlgoTimeSeriesReader();
		List<TimeSeries> timeSeries = reader.runAlgorithm(inputFile, separator);
		reader.printStats();
		
		// Applying the  algorithm
		AlgoConvertTimeSeriesFileToSequencesWithSAX algorithm = new AlgoConvertTimeSeriesFileToSequencesWithSAX();
		algorithm.runAlgorithm(timeSeries, outputFile, numberOfSegments, numberOfSymbols, deactivatePAA);
		algorithm.printStats();
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[4];
		parameters[0] = new DescriptionOfParameter("Number of segments", "(e.g. 3)", Integer.class, false);
		parameters[1] = new DescriptionOfParameter("Number of symbols", "(e.g. 3)", Integer.class, false);
		parameters[2] = new DescriptionOfParameter("Separator", "(e.g. , )", String.class, false);
		parameters[3] = new DescriptionOfParameter("Deactivate PAA", "(default: false)", String.class, true);
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
		return new String[]{"Database of instances","Sequence database", "Simple sequence database"};
	}
	
}
