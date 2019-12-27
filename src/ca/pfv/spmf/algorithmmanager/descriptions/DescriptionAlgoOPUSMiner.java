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
import ca.pfv.spmf.algorithms.frequentpatterns.opusminer.AlgoOpusMiner;

/**
 * This class describes the OPUS-Miner algorithm parameters. 
 * It is designed to be used by the graphical and command line interface.
 * 
 * @see OPUS-Miner
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoOPUSMiner extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoOPUSMiner(){
	}

	@Override
	public String getName() {
		return "OPUS-Miner";
	}

	@Override
	public String getAlgorithmCategory() {
		return "FREQUENT ITEMSET MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/OPUSMINER.php";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {
		
		// Set k to the integer value <i>.  By default it is 100.
		int k = getParamAsInteger(parameters[0]);
		
		// Filter out itemsets that are not independently productive.
		boolean filter = getParamAsBoolean(parameters[1]);
		
		// Set the measure of interest to lift.  By default it is leverage.
		boolean searchByLift = getParamAsBoolean(parameters[2]);
		
		// Remove redundant itemsets.
		boolean redundancyTests = getParamAsBoolean(parameters[3]);
		
		boolean correctionForMultiCompare = getParamAsBoolean(parameters[4]);
		
		//Each output itemset is followed by its closure.  
		boolean printClosure = getParamAsBoolean(parameters[5]);
		
		//------------ End of parameters ----------------------//

		// Applying the  algorithm
		AlgoOpusMiner algorithm = new AlgoOpusMiner();
		algorithm.runAlgorithm(inputFile, outputFile, printClosure, filter, 
				k ,searchByLift, 
				correctionForMultiCompare, redundancyTests, false);
		algorithm.printStats();
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[6];
		parameters[0] = new DescriptionOfParameter("k", "(e.g. 3)", Integer.class, false);
		parameters[1] = new DescriptionOfParameter("Check independency?", "(e.g. true)", Boolean.class, false);
		parameters[2] = new DescriptionOfParameter("Search by lift?", "(e.g. true)", Boolean.class, false);
		parameters[3] = new DescriptionOfParameter("Check redundancy?", "(e.g. true)", Boolean.class, false);
		parameters[4] = new DescriptionOfParameter("Correction for multicompare?", "(e.g. true)", Boolean.class, false);
		parameters[5] = new DescriptionOfParameter("Print closure?", "(e.g. false)", Boolean.class, false);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Java conversion by Xiang Li and Philippe Fournier-Viger";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Database of instances","Transaction database", "Simple transaction database"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Patterns", "Frequent patterns", "Frequent itemsets", "Self-Sufficient Itemsets"};
	}
	
}
