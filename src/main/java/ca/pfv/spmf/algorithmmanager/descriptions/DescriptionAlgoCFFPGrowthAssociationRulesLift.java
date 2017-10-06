package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.File;
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
import ca.pfv.spmf.algorithms.frequentpatterns.cfpgrowth.AlgoCFPGrowth;

/**
 * This class describes parameters of the algorithm for generating association rules 
 * with the CFP-Growth algorithm with the lift measure. 
 * It is designed to be used by the graphical and command line interface.
 * 
 * @see AlgoCFPGrowth, AlgoAgrawalFaster94
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoCFFPGrowthAssociationRulesLift extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoCFFPGrowthAssociationRulesLift(){
	}

	@Override
	public String getName() {
		return "CFPGrowth++_association_rules_with_lift";
	}

	@Override
	public String getAlgorithmCategory() {
		return "ASSOCIATION RULE MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#allassociationrules";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {
		String misFile = parameters[0];
		double minconf = getParamAsDouble(parameters[1]);
		double minlift = getParamAsDouble(parameters[2]);

		File file = new File(inputFile);
		String misFileFullPath;
		if (file.getParent() == null) {
			misFileFullPath = misFile;
		} else {
			misFileFullPath = file.getParent() + File.separator + misFile;
		}

		AlgoCFPGrowth cfpgrowth = new AlgoCFPGrowth();
		ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets patterns = cfpgrowth
				.runAlgorithm(inputFile, null, misFileFullPath);
		cfpgrowth.printStats();
		int databaseSize = cfpgrowth.getDatabaseSize();

		// STEP 2: Generating all rules from the set of frequent itemsets
		// (based on Agrawal & Srikant, 94)
		ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AlgoAgrawalFaster94 algoAgrawal = new ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AlgoAgrawalFaster94();
		algoAgrawal.runAlgorithm(patterns, outputFile, databaseSize,
				minconf, minlift);
		algoAgrawal.printStats();
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[3];
		parameters[0] = new DescriptionOfParameter("MIS file name", "(e.g. MIS.txt)", String.class, false);
		parameters[1] = new DescriptionOfParameter("Minimum confidence (%)", "(e.g. 0.6 or 60%)", Double.class, false);
		parameters[2] = new DescriptionOfParameter("Minimum lift ", "(e.g. 0.2)", Double.class, false);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Azadeh Soltani, Philippe Fournier-Viger";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Database of instances","Transaction database", "Simple transaction database"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Patterns", "Association rules", "Association rules with lift and multiple support thresholds"};
	}
	
}
