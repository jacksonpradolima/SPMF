package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.IOException;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan.AlgoFEAT;
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
 * This class describes the PrefixSpan algorithm parameters. It is designed to be used by the graphical and command line interface.
 * 
 * @see AlgoFEAT
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoFEAT extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoFEAT(){
	}

	@Override
	public String getName() {
		return "FEAT";
	}

	@Override
	public String getAlgorithmCategory() {
		return "SEQUENTIAL PATTERN MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#feat";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {

		AlgoFEAT algo = new AlgoFEAT();
		if (parameters.length >=2 && "".equals(parameters[1]) == false) {
			algo.setMaximumPatternLength(getParamAsInteger(parameters[1]));
		}
		ca.pfv.spmf.input.sequence_database_list_integers.SequenceDatabase sequenceDatabase = new ca.pfv.spmf.input.sequence_database_list_integers.SequenceDatabase();
		sequenceDatabase.loadFile(inputFile);
		int minsup = (int) (getParamAsDouble(parameters[0]) * sequenceDatabase
				.size()); // we use a minimum support of 2 sequences.
		
		boolean outputSeqIdentifiers = false;
		if (parameters.length >=2 && "".equals(parameters[1]) == false) {
			outputSeqIdentifiers = getParamAsBoolean(parameters[1]);
		}

		algo.setShowSequenceIdentifiers(outputSeqIdentifiers);
		algo.runAlgorithm(sequenceDatabase,  minsup);
		algo.writeResultTofile(outputFile);   
		algo.printStatistics(sequenceDatabase.size());
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[3];
		parameters[0] = new DescriptionOfParameter("Minsup (%)", "(e.g. 0.4 or 40%)", Double.class, false);
		parameters[1] = new DescriptionOfParameter("Max pattern length", "", Integer.class, true);
		parameters[2] = new DescriptionOfParameter("Show sequence ids?", "(default: false)", Boolean.class, true);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Philippe Fournier-Viger";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Database of instances","Sequence database", "Simple sequence database"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Patterns", "Sequential patterns", "Frequent sequential patterns", "Frequent sequential generators"};
	}
//
//	@Override
//	String[] getSpecialInputFileTypes() {
//		return null; //new String[]{"ARFF"};
//	}
	
}
