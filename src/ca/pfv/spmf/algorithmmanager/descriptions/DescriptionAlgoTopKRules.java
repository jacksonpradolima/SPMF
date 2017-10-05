package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.IOException;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.algorithms.associationrules.TopKRules_and_TNR.AlgoTopKRules;
import ca.pfv.spmf.algorithms.associationrules.TopKRules_and_TNR.Database;
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
 * This class describes parameters of the algorithm for generating Top-K association rules. 
 * It is designed to be used by the graphical and command line interface.
 * 
 * @see AlgoTopKRules
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoTopKRules extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoTopKRules(){
	}

	@Override
	public String getName() {
		return "TopKRules";
	}

	@Override
	public String getAlgorithmCategory() {
		return "ASSOCIATION RULE MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#topkrules";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {
		Database database = new Database();
		database.loadFile(inputFile);

		int k = getParamAsInteger(parameters[0]);
		double minconf = getParamAsDouble(parameters[1]);

		ca.pfv.spmf.algorithms.associationrules.TopKRules_and_TNR.AlgoTopKRules algo = new ca.pfv.spmf.algorithms.associationrules.TopKRules_and_TNR.AlgoTopKRules();
		algo.runAlgorithm(k, minconf, database);
		algo.printStats();
		algo.writeResultTofile(outputFile); // to save results to file
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[2];
		parameters[0] = new DescriptionOfParameter("k", "(e.g. 2)", Integer.class, false);
		parameters[1] = new DescriptionOfParameter("Minconf (%)", "(e.g. 0.8 or 80%)", Double.class, false);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Philippe Fournier-Viger";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Database of instances","Transaction database", "Simple transaction database"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Patterns", "Association rules", "Top-k association rules"};
	}
	
}
