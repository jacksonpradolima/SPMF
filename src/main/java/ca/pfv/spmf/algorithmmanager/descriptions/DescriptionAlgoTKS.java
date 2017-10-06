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
import ca.pfv.spmf.algorithms.sequentialpatterns.spam.AlgoTKS;

/**
 * This class describes the TKS algorithm parameters. It is designed to be used by the graphical and command line interface.
 * 
 * @see AlgoTKS
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoTKS extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoTKS(){
	}

	@Override
	public String getName() {
		return "TKS";
	}

	@Override
	public String getAlgorithmCategory() {
		return "SEQUENTIAL PATTERN MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#tks";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {

		int k = getParamAsInteger(parameters[0]);

		AlgoTKS algo = new AlgoTKS();
		if (parameters.length >=2 && "".equals(parameters[1]) == false) {
			algo.setMinimumPatternLength(getParamAsInteger(parameters[1]));
		}
		if (parameters.length >=3 && "".equals(parameters[2]) == false) {
			algo.setMaximumPatternLength(getParamAsInteger(parameters[2]));
		}
		// get the required items if any (optional)
		if (parameters.length >=4 && parameters[3] != null && parameters[3].isEmpty() != true) {
			// if the author did not say any
			String[] itemsString = parameters[3].split(",");
			int[] requiredItems = new int[itemsString.length];
			for (int i = 0; i < itemsString.length; i++) {
				requiredItems[i] = Integer.parseInt(itemsString[i]);
			}
			algo.setMustAppearItems(requiredItems);
		}
		if (parameters.length >=5 && "".equals(parameters[4]) == false) {
			algo.setMaxGap(getParamAsInteger(parameters[4]));
		}
		
		if (parameters.length >=6 && "".equals(parameters[5]) == false) {
			boolean outputSeqIdentifiers = getParamAsBoolean(parameters[5]);
			algo.showSequenceIdentifiersInOutput(outputSeqIdentifiers);
		}
		
		// execute the algorithm
		algo.runAlgorithm(inputFile, outputFile, k);
		algo.writeResultTofile(outputFile); // to save results to file
		algo.printStatistics();
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[6];
		parameters[0] = new DescriptionOfParameter("k ", "(e.g. 5 patterns)", Double.class, false);
		parameters[1] = new DescriptionOfParameter("Min pattern length", "(e.g. 1 items)", Integer.class, true);
		parameters[2] = new DescriptionOfParameter("Max pattern length", "(e.g. 10 items)", Integer.class, true);
		parameters[3] = new DescriptionOfParameter("Required items", "(e.g. 1,2,3)", String.class, true);
		parameters[4] = new DescriptionOfParameter("Max gap", "(e.g. 1 item)", Integer.class, true);
		parameters[5] = new DescriptionOfParameter("Show sequence ids?", "(default: false)", Boolean.class, true);
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
		return new String[]{"Patterns", "Sequential patterns", "Top-k frequent sequential patterns"};
	}
	
}
