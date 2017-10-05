package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.IOException;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
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
import ca.pfv.spmf.algorithms.sequentialpatterns.uspan.AlgoUSpan;

/**
 * This class describes the USpan algorithm parameters. It is designed to be used by the graphical and command line interface.
 * 
 * @see AlgoUSpan
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoUSpan extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoUSpan(){
	}

	@Override
	public String getName() {
		return "USpan";
	}

	@Override
	public String getAlgorithmCategory() {
		return "HIGH-UTILITY PATTERN MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#uspan";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {
		AlgoUSpan algo = new AlgoUSpan();
		if (parameters.length >=2 && "".equals(parameters[1]) == false) {
			algo.setMaxPatternLength(getParamAsInteger(parameters[1]));
		}

		// execute the algorithm 
		algo.runAlgorithm(inputFile, outputFile,
				getParamAsInteger(parameters[0])); // 
		algo.printStatistics();
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[2];
		parameters[0] = new DescriptionOfParameter("Minimum utility", "(e.g. 35)", Integer.class, false);
		parameters[1] = new DescriptionOfParameter("Maximum length", "(e.g. 4)", Integer.class, true);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Philippe Fournier-Viger";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Database of instances","Sequence database", "Sequence Database with utility values"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Patterns", "High-utility patterns","Sequential patterns", "High-utility patterns", "High-utility sequential patterns"};
	}
//
//	@Override
//	String[] getSpecialInputFileTypes() {
//		return null; //new String[]{"ARFF"};
//	}
	
}
