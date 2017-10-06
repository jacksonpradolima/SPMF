package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.IOException;
import java.nio.charset.Charset;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.gui.PreferencesManager;
import ca.pfv.spmf.tools.dataset_converter.Formats;
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
 * This class describes the algorithm to convert a text file to a sequence database. 
 * It is designed to be used by the graphical and command line interface.
 * 
 * @see SequenceDatabaseConverter
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoConvertTEXTFileToSequenceDB extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoConvertTEXTFileToSequenceDB(){
	}

	@Override
	public String getName() {
		return "Convert_TEXT_file_to_sequence_database";
	}

	@Override
	public String getAlgorithmCategory() {
		return "DATASET TOOLS";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#convtextfile";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {
		int sequenceCount = getParamAsInteger(parameters[0]);
		// Get the text encoding
		Charset charset = PreferencesManager.getInstance().getPreferedCharset();

		long startTime = System.currentTimeMillis();
		// Create a converter
		SequenceDatabaseConverter converter = new SequenceDatabaseConverter();
		// Call the method to convert the input file from TEXT to the SPMF format
		converter.convert(inputFile, outputFile, Formats.TEXT, sequenceCount, charset);
		long endTIme = System.currentTimeMillis();
		System.out
				.println("Sequence database converted.  Time spent for conversion = "
						+ (endTIme - startTime) + " ms.");
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[1];
		parameters[0] = new DescriptionOfParameter("Sequence count count", "(e.g. 5)", Integer.class, false);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Philippe Fournier-Viger";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Text file"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Database of instances","Sequence database", "Simple sequence database"};
	}
//
//	@Override
//	String[] getSpecialInputFileTypes() {
//		return null; //new String[]{"ARFF"};
//	}
	
}
