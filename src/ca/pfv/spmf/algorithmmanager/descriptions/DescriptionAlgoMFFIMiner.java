package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.IOException;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.algorithms.frequentpatterns.ffi_miner.AlgoFFIMiner;
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
import ca.pfv.spmf.algorithms.frequentpatterns.mffi_miner.AlgoMFFIMiner;

/**
 * This class describes the MFFI-Miner algorithm parameters. 
 * It is designed to be used by the graphical and command line interface.
 * 
 * @see AlgoFFIMiner
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoMFFIMiner extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoMFFIMiner(){
	}

	@Override
	public String getName() {
		return "MFFI-Miner";
	}

	@Override
	public String getAlgorithmCategory() {
		return "FREQUENT ITEMSET MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/documentation.php#mffiminer";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {
		float minSupport = getParamAsFloat(parameters[0]);
		
		// Applying the MFFI-Miner algorithm
		AlgoMFFIMiner MFFIminer = new AlgoMFFIMiner();
		MFFIminer.runAlgorithm(inputFile, outputFile, minSupport);
		MFFIminer.printStats();
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[1];
		parameters[0] = new DescriptionOfParameter("Minimum support", "(e.g. 2)", Float.class, false);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Ting Li";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Database of instances","Transaction database", "Transaction database with utility values"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Patterns",  "Frequent patterns", "Frequent itemsets", "Multiple Frequent fuzzy itemsets"};
	}
	
}
