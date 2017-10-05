package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.IOException;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.tools.dataset_generator.SequenceDatabaseGenerator;
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
 * This class describes the algorithm to generate a sequence database with timestamps. It is designed to be used by the graphical and command line interface.
 * 
 * @see SequenceDatabaseGenerator
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoGenerateASequenceDBTimestamp extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoGenerateASequenceDBTimestamp(){
	}

	@Override
	public String getName() {
		return "Generate_a_sequence_database_with_timestamps";
	}

	@Override
	public String getAlgorithmCategory() {
		return "DATASET TOOLS";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#genseqt";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {
		int p1 = getParamAsInteger(parameters[0]);
		int p2 = getParamAsInteger(parameters[1]);
		int p3 = getParamAsInteger(parameters[2]);
		int p4 = getParamAsInteger(parameters[3]);

		SequenceDatabaseGenerator generator = new SequenceDatabaseGenerator();
		generator.generateDatabase(p1, p2, p3, p4, outputFile, true);
		System.out.println("Sequence database generated.  ");
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[4];
		parameters[0] = new DescriptionOfParameter("Transaction count", "(e.g. 100)", Integer.class, false);
		parameters[1] = new DescriptionOfParameter("Max distinct items", "(e.g. 1000)", Integer.class, false);
		parameters[2] = new DescriptionOfParameter("Max item count per transaction", "(e.g. 3)", Integer.class, false);
		parameters[3] = new DescriptionOfParameter("Max itemset count per sequence", "(e.g. 7)", Integer.class, false);
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
		return new String[]{"Database of instances","Sequence database", "Sequence Database with timestamps"};
	}
//
//	@Override
//	String[] getSpecialInputFileTypes() {
//		return null; //new String[]{"ARFF"};
//	}
	
}
