package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.IOException;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.algorithms.frequentpatterns.fhmds.ds.AlgoFHM_DS;
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
 * This class describes the FHMDS algorithm parameters. 
 * It is designed to be used by the graphical and command line interface.
 * 
 * @see AlgoFHM_DS
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoFHMDS extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoFHMDS(){
	}

	@Override
	public String getName() {
		return "FHMDS";
	}

	@Override
	public String getAlgorithmCategory() {
		return "HIGH-UTILITY PATTERN MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/FHMDS.php";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {

		int k =  getParamAsInteger(parameters[0]);
		
		// Win size is the number of batches in a window
		int win_size = getParamAsInteger(parameters[1]);
		
		// number_of_transactions_batch is the number of transactions in a batch
		int number_of_transactions_batch = getParamAsInteger(parameters[2]);

		// Run the algorithm
		AlgoFHM_DS algorithm = new AlgoFHM_DS();
		algorithm.runAlgorithm(
				inputFile,
				k,
				win_size, 
				number_of_transactions_batch, outputFile);
		
		algorithm.printStats();
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[3];
		parameters[0] = new DescriptionOfParameter("k", "(e.g. 5)", Integer.class, false);
		parameters[1] = new DescriptionOfParameter("Window size", "(e.g. 2)", Integer.class, false);
		parameters[2] = new DescriptionOfParameter("Transactions / batch", "(e.g. 2)", Integer.class, false);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Siddhart Dawar et al.";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Database of instances","Transaction database", "Transaction database with utility values"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Patterns",  "High-utility patterns","High-utility itemsets", "Top-k High-utility itemsets"};
	}
	
}
