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
public class DescriptionAlgoSetPreferredCharset extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoSetPreferredCharset(){
	}

	@Override
	public String getName() {
		return "Set_prefered_charset";
	}

	@Override
	public String getAlgorithmCategory() {
		return "SPMF Options";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#textcharset";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {
		// Get the text encoding
		String encoding = getParamAsString(parameters[0]);
		// if the user wants to reset to the default encoding on this system
		// we will find it
		if(encoding.equals("default")){
			encoding = Charset.defaultCharset().name();
		}
		
		PreferencesManager.getInstance().setPreferedCharset(encoding);
		
		System.out.println("Charset preference saved");
		System.out.println("" + PreferencesManager.getInstance().getPreferedCharset().name());
		System.out.println("This preference indicates which charset to use to read text files.");
		System.out.println("Currently, this preference is not used by most algorithm implementations.");
		System.out.println("In the future, most implementations will be modified to use the user selected charset.");
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[1];
		parameters[0] = new DescriptionOfParameter("Prefered charset", "(e.g. default, UTF-8, UTF-16)", String.class, false);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Philippe Fournier-Viger";
	}

	@Override
	public String[] getInputFileTypes() {
		return null;
	}

	@Override
	public String[] getOutputFileTypes() {
		return null;
	}
//
//	@Override
//	String[] getSpecialInputFileTypes() {
//		return null; //new String[]{"ARFF"};
//	}
	
}
