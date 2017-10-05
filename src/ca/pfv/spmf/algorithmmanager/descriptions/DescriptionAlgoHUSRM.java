package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.IOException;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.algorithms.sequential_rules.husrm.AlgoHUSRM;
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
 * This class describes the HUSRM algorithm parameters. It is designed to be used by the graphical and command line interface.
 * 
 * @see AlgoHUSRM
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoHUSRM extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoHUSRM(){
	}

	@Override
	public String getName() {
		return "HUSRM";
	}

	@Override
	public String getAlgorithmCategory() {
		return "HIGH-UTILITY PATTERN MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#husrm";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {
		int minutil = getParamAsInteger(parameters[0]);
		double minconf = getParamAsDouble(parameters[1]);
		int maxAntecedentSize = Integer.MAX_VALUE;
		if (parameters.length >=3 && "".equals(parameters[2]) == false) {
			maxAntecedentSize = getParamAsInteger(parameters[2]);
		}
		int maxConsequentSize = Integer.MAX_VALUE;
		if (parameters.length >=4 && "".equals(parameters[3]) == false) {
			maxConsequentSize = getParamAsInteger(parameters[3]);
		}		
		
		int maximumSequenceCount = Integer.MAX_VALUE;
		
		// Applying the algorithm
		AlgoHUSRM algo = new AlgoHUSRM();
		algo.runAlgorithm(inputFile, outputFile, minconf, minutil, maxAntecedentSize, maxConsequentSize, maximumSequenceCount);
		algo.printStats();
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[4];
		parameters[0] = new DescriptionOfParameter("Minimum utility", "(e.g. 40)", Integer.class, false);
		parameters[1] = new DescriptionOfParameter("Minimum confidence (%)", "(e.g. 0.7 or 70%)", Double.class, false);
		parameters[2] = new DescriptionOfParameter("Min antecedent size", "(e.g. 4)", Integer.class, true);
		parameters[3] = new DescriptionOfParameter("Max consequent size", "(e.g. 4)", Integer.class, true);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Souleymane Zida, Philippe Fournier-Viger";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Database of instances","Sequence database", "Sequence Database with utility values"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Patterns", "High-utility patterns","Sequential rules", "High-utility rules", "High-utility sequential rules"};
	}
//
//	@Override
//	String[] getSpecialInputFileTypes() {
//		return null; //new String[]{"ARFF"};
//	}
	
}
