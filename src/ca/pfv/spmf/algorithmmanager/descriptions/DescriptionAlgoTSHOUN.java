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
import ca.pfv.spmf.algorithms.frequentpatterns.tshoun.AlgoTSHoun;
import ca.pfv.spmf.algorithms.frequentpatterns.tshoun.DatabaseWithPeriods;

/**
 * This class describes the TS-HOUN algorithm parameters. 
 * It is designed to be used by the graphical and command line interface.
 * 
 * @see AlgoTSHoun
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoTSHOUN extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoTSHOUN(){
	}

	@Override
	public String getName() {
		return "TS-HOUN";
	}

	@Override
	public String getAlgorithmCategory() {
		return "HIGH-UTILITY PATTERN MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/documentation.php#tshoun";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {
		double min_utility_ratio = getParamAsDouble(parameters[0]);
		int periodCount = getParamAsInteger(parameters[1]);
		// Loading the database into memory
		DatabaseWithPeriods database = new DatabaseWithPeriods(periodCount);
		database.loadFile(inputFile);
		
		// Applying the algorithm
		AlgoTSHoun algo = new AlgoTSHoun();
		algo.runAlgorithm(database, min_utility_ratio, outputFile, periodCount);
		algo.printStats();
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[2];
		parameters[0] = new DescriptionOfParameter("Minimum utility ratio", "(e.g. 0.8)", Double.class, false);
		parameters[1] = new DescriptionOfParameter("Period count", "(e.g. 3)", Double.class, false);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Philippe Fournier-Viger";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Database of instances","Transaction database", "Transaction database with shelf-time periods and positive/negative utility values"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Patterns",  "High-utility patterns","On-shelf high-utility itemsets"};
	}
	
}
