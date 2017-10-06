package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.IOException;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.algorithms.frequentpatterns.apriori.AlgoApriori;
import ca.pfv.spmf.algorithms.frequentpatterns.haui_miner.AlgoHAUIMiner;
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
import ca.pfv.spmf.algorithms.frequentpatterns.haui_mmau.AlgoHAUIMMAU;
import ca.pfv.spmf.algorithms.frequentpatterns.haui_mmau.ItemsetsTP;
import ca.pfv.spmf.algorithms.frequentpatterns.haui_mmau.UtilityTransactionDatabaseTP;

/**
 * This class describes the HAUI-Miner algorithm parameters. 
 * It is designed to be used by the graphical and command line interface.
 * 
 * @see AlgoApriori
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoHAUIMMAU extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoHAUIMMAU(){
	}

	@Override
	public String getName() {
		return "HAUI-MMAU";
	}

	@Override
	public String getAlgorithmCategory() {
		return "HIGH-UTILITY PATTERN MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#hauimmau";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {
		int GLMAU = getParamAsInteger(parameters[0]);
		String minutilityPath = getParamAsString(parameters[1]);
		
		// Loading the database into memory
		UtilityTransactionDatabaseTP database = new UtilityTransactionDatabaseTP();
		database.loadFile(inputFile,minutilityPath);
		//database.printDatabase();
		
		//Applying the HAUIMMAU algorithm
		AlgoHAUIMMAU HAUIMMAU = new AlgoHAUIMMAU();
		ItemsetsTP highAUtilityItemsets = HAUIMMAU.runAlgorithm(database, database.mutipleMinUtilities, GLMAU);
		highAUtilityItemsets.saveResultsToFile(outputFile, database.size(),  GLMAU);
		HAUIMMAU.printStats();
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[2];
		parameters[0] = new DescriptionOfParameter("GLMAU", "(e.g. 0)", Integer.class, false);
		parameters[1] = new DescriptionOfParameter("MAU file", "(e.g. MAU_Utility.txt)", String.class, false);
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
		return new String[]{"Patterns",  "High-utility patterns","High average-utility itemsets"};
	}
	
}
