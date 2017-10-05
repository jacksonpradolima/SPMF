package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.IOException;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.algorithms.sequential_rules.trulegrowth.AlgoTRuleGrowth;
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
import ca.pfv.spmf.algorithms.sequential_rules.trulegrowth_with_strings.AlgoTRuleGrowth_withStrings;

/**
 * This class describes the TRuleGrowth algorithm parameters with strings. 
 * It is designed to be used by the graphical and command line interface.
 * 
 * @see AlgoTRuleGrowth
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoTRuleGrowthWithStrings extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoTRuleGrowthWithStrings(){
	}

	@Override
	public String getName() {
		return "TRuleGrowth_with_strings";
	}

	@Override
	public String getAlgorithmCategory() {
		return "SEQUENTIAL RULE MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#trulegrowth";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {
		double minsup = getParamAsDouble(parameters[0]);
		double minconf = getParamAsDouble(parameters[1]);
		int window = getParamAsInteger(parameters[2]);

		AlgoTRuleGrowth_withStrings algo = new AlgoTRuleGrowth_withStrings();
		algo.runAlgorithm(minsup, minconf, inputFile, outputFile, window);
		algo.printStats();
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[5];
		parameters[0] = new DescriptionOfParameter("Minsup (%)", "(e.g. 0.7 or 70%)", Double.class, false);
		parameters[1] = new DescriptionOfParameter("Minconf (%)", "(e.g. 0.8 or 80%)", Double.class, false);
		parameters[2] = new DescriptionOfParameter("Window size", "(e.g. 3)", Integer.class, false);
		parameters[3] = new DescriptionOfParameter("Max antecedent size", "(e.g. 1 items)", Integer.class, true);
		parameters[4] = new DescriptionOfParameter("Max consequent size", "(e.g. 2 items)", Integer.class, true);
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
		return new String[]{"Patterns", "Sequential rules", "Frequent sequential rules with strings"};
	}
	
}
