package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.File;
import java.io.IOException;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.algorithms.sequentialpatterns.occur.AlgoOccur;
import ca.pfv.spmf.algorithms.sequentialpatterns.spam.AlgoCMSPAM;
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
 * This class describes the OCCUR algorithm parameters. It is designed to be used by the graphical and command line interface.
 * 
 * @see AlgoCMSPAM
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoOccur extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoOccur(){
	}

	@Override
	public String getName() {
		return "OCCUR";
	}

	@Override
	public String getAlgorithmCategory() {
		return "SEQUENTIAL PATTERN MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/OCCUR.php";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {

		// input file - patterns
		String patternFile = getParamAsString(parameters[0]);
		
		File file = new File(inputFile);
		if (file.getParent() == null) {
			patternFile = parameters[0];
		} else {
			patternFile = file.getParent() + File.separator+ parameters[0];
		}

		// Create an instance of the algorithm with minsup = 50 %
		AlgoOccur algo = new AlgoOccur(); 
		
		// execute the algorithm
		algo.runAlgorithm(inputFile, patternFile, outputFile);    
		algo.printStatistics();
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[1];
		parameters[0] = new DescriptionOfParameter("Pattern file", "(e.g. spmPatterns.txt)", String.class, false);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Philippe Fournier-Viger";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Sequential patterns"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Patterns", "Sequential patterns", "Frequent sequential patterns with occurrences"};
	}
	
}
