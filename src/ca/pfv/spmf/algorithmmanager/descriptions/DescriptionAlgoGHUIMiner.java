package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoCHUIMiner;
import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoGHUIMINER;
import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.Itemset;
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
 * This class describes the GHUI-Miner algorithm parameters. 
 * It is designed to be used by the graphical and command line interface.
 * 
 * @see AlgoGHUIMINER
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoGHUIMiner extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoGHUIMiner(){
	}

	@Override
	public String getName() {
		return "GHUI-Miner";
	}

	@Override
	public String getAlgorithmCategory() {
		return "HIGH-UTILITY PATTERN MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/documentation.php#ghuiminer";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {
		int minutil = getParamAsInteger(parameters[0]);
		// (1) Applying the CHUI-Miner algorithm to find 
		// closed high utility itemsets (CHUIs)
		System.out.println("Step 1: Mining CHUIs...");
		AlgoCHUIMiner chuiMineClosed = new AlgoCHUIMiner(true);
		List<List<Itemset>> closedItemsets = chuiMineClosed.runAlgorithm(inputFile, minutil, null);
		chuiMineClosed.printStats();
		Set<Integer> itemsInClosedItemsets = chuiMineClosed.setOfItemsInClosedItemsets;

		System.out.println("Step 2: Mining GHUIs...");
		// (2) Apply the GHUI-Miner algorithm to find
		// the set of generators of high utility itemsets (GHUIs)
		AlgoGHUIMINER ghuiMiner = new AlgoGHUIMINER();
		ghuiMiner.runAlgorithm(inputFile, outputFile, minutil, closedItemsets, itemsInClosedItemsets);
		ghuiMiner.printStats();
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[1];
		parameters[0] = new DescriptionOfParameter("Minimum utility", "(e.g. 30)", Integer.class, false);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Philippe Fournier-Viger";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Database of instances","Transaction database", "Transaction database with utility values"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Patterns",  "Generator patterns","High-utility patterns","Generator itemsets","High-utility itemsets","Generators of high-utility itemsets"};
	}
	
}
