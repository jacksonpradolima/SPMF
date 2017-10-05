package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.IOException;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.tools.dataset_stats.TransactionStatsGenerator;
import ca.pfv.spmf.tools.other_dataset_tools.FixItemIDsTransactionDatabaseTool;
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
import ca.pfv.spmf.tools.other_dataset_tools.FixTransactionDatabaseTool;

/**
 * This class describes the algorithm to fix a transaction database by incrementing item identifiers
 * by some increment (e.g. 1). 
 * It is designed to be used by the graphical and command line interfaces.
 * 
 * @see TransactionStatsGenerator
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoFixItemIDsTransactionDB extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoFixItemIDsTransactionDB(){
	}

	@Override
	public String getName() {
		return "Fix_item_ids_in_transaction_database";
	}

	@Override
	public String getAlgorithmCategory() {
		return "DATASET TOOLS";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#fixitemstdb";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {
		int increment = getParamAsInteger(parameters[0]);
		
		FixItemIDsTransactionDatabaseTool tool = new FixItemIDsTransactionDatabaseTool();
		tool.convert(inputFile, outputFile, increment);
		System.out.println("Finished fixing the transaction database.");
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[1];
		parameters[0] = new DescriptionOfParameter("Value to add to item ids", "(e.g. 1)", Integer.class, false);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Philippe Fournier-Viger";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Database of instances","Transaction database"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Database of instances","Transaction database"};
	}	
}
