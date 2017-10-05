package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.IOException;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixspan.AlgoPrefixSpan;
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
 * @see AlgoPrefixSpan
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoPrefixSpan extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoPrefixSpan(){
	}

	@Override
	public String getName() {
		return "PrefixSpan";
	}

	@Override
	public String getAlgorithmCategory() {
		return "SEQUENTIAL PATTERN MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#examplePrefixSpan";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {


		// Get the parameter "minsup"
		double minsup = getParamAsDouble(parameters[0]);
		
		AlgoPrefixSpan algo = new AlgoPrefixSpan();

		// Get the optional parameter to show sequence ids
		if (parameters.length >=2 && "".equals(parameters[1]) == false) {
			algo.setMaximumPatternLength(getParamAsInteger(parameters[1]));
		}
		
		if (parameters.length >=3 && "".equals(parameters[2]) == false) {
			algo.setShowSequenceIdentifiers(getParamAsBoolean(parameters[2]));
		}
		
		// Run the algorithm
		algo.runAlgorithm(inputFile, minsup, outputFile);
		algo.printStatistics();
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
		return new String[]{"Database of instances","Sequence database", "Simple Sequence Database"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Patterns", "Sequential patterns", "Frequent Sequential patterns"};
	}
//
//	@Override
//	String[] getSpecialInputFileTypes() {
//		return null; //new String[]{"ARFF"};
//	}
	
}
