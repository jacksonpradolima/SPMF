package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.IOException;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;

/* This file is copyright (c) 2018 Ryan Benton and Blake Johns
* 
*  This file is part of the SPMF DATA MINING SOFTWARE
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
 * This class describes parameters of the algorithm for generating association rules
 * with the RPGrowth algorithm.
 * It is designed to be used bt the graphical and command line interface.
 * It is a modification of Philippe Fournier-Viger's
 *  "DescriptionAlgoFPGrowthAssociationRules" file.
 *  
 * @see AlgoRPGrowth, AlgoAgrawalFaster94
 * @author Ryan Benton and Blake Johns
 *
 */
public class DescriptionAlgoRPGrowthAssociationRules extends DescriptionOfAlgorithm {

	// default constructor
	
	public DescriptionAlgoRPGrowthAssociationRules() {
	}
	
	@Override
	public String getName() {
		return "RPGrowth_association_rules";
	}
	
	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/rpgrowthalgo.php";
	}
	
	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException{
		double minsup = getParamAsDouble(parameters[0]);
		double minraresup = getParamAsDouble(parameters[1]);
		double minconf = getParamAsDouble(parameters[2]);
		
		int maxAntecedentLength = 400;
		int maxConsequentLength = 400;
		if(parameters.length >= 4 && "".equals(parameters[3]) == false) {
			maxAntecedentLength = getParamAsInteger(parameters[3]);
		}
		if (parameters.length >= 5 && "".equals(parameters[4]) == false) {
			maxConsequentLength = getParamAsInteger(parameters[4]);
		}
		
		ca.pfv.spmf.algorithms.frequentpatterns.rpgrowth.AlgoRPGrowth rpgrowth = new ca.pfv.spmf.algorithms.frequentpatterns.rpgrowth.AlgoRPGrowth();
		rpgrowth.setMaximumPatternLength(maxAntecedentLength + maxConsequentLength);
		ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets patterns = rpgrowth.runAlgorithm(inputFile,  null, minsup, minraresup);
		rpgrowth.printStats();
		int databaseSize = rpgrowth.getDatabaseSize();
		
		//Step 2: generating all rules from the set of rare itemsets
		//(based on Agrawal & Srikant, 94)
		
		ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AlgoAgrawalFaster94 algoAgrawal = new ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AlgoAgrawalFaster94();
		algoAgrawal.setMaxAntecedentLength(maxAntecedentLength);
		algoAgrawal.setMaxConsequentLength(maxConsequentLength);
		algoAgrawal.runAlgorithm(patterns, outputFile, databaseSize, minconf);
		algoAgrawal.printStats();
	}
	

	@Override
	public String getImplementationAuthorNames() {
		return "Ryan Benton and Blake Johns";
	}

	@Override
	public String getAlgorithmCategory() {
		return "ASSOCIATION RULE MINING";
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[5];
		parameters[0] = new DescriptionOfParameter("Minsup (%)", "(e.g. 0.5 or 50%)", Double.class, false);
		parameters[1] = new DescriptionOfParameter("Minraresup (%)", "(e.g. 0.1 or 10%)", Double.class, false);
		parameters[2] = new DescriptionOfParameter("Minconf (%)", "(e.g. 0.6 or 60%)", Double.class, false);
		parameters[3] = new DescriptionOfParameter("Max antecedent length", "(e.g. 2 items)", Integer.class, true);
		parameters[4] = new DescriptionOfParameter("Max consequent length", "(e.g. 2 items)", Integer.class, true);
		return parameters;
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[] {"Database of instances", "Transaction database", "Simple transaction database"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[] {"Patterns", "Association rules"};
	}
}
