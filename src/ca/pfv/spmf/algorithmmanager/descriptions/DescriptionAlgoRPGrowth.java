package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.IOException;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.algorithms.frequentpatterns.rpgrowth.AlgoRPGrowth;
/*This file is copyright (c) 2018 Ryan Benton and Blake Johns
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
 * 
 */

/**
 * This class describes the RPGrowth algorithm parameters.
 * It is designed to be used by the graphical and command line interface
 * 
 * @see AlgoRPGrowth
 * @author Ryan Benton and Blake Johns
 */
public class DescriptionAlgoRPGrowth extends DescriptionOfAlgorithm {
	
	//default constructor
	
	public DescriptionAlgoRPGrowth() {
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Ryan Benton and Blake Johns";
	}

	@Override
	public String getName() {
		return "RPGrowth_itemsets";
	}

	@Override
	public String getAlgorithmCategory() {
		return "FREQUENT ITEMSET MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/rpgrowthalgo.php";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {
		double minsup = getParamAsDouble(parameters[0]);
		double  minraresup = getParamAsDouble(parameters[1]);
		AlgoRPGrowth algorithm = new AlgoRPGrowth();
		
		if (parameters.length >= 3 && "".equals(parameters[2]) == false) {
			algorithm.setMaximumPatternLength(getParamAsInteger(parameters[2]));			
		}
		algorithm.runAlgorithm(inputFile,  outputFile, minsup, minraresup);
		algorithm.printStats();		
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[3];
		parameters[0] = new DescriptionOfParameter("Minsup (%)", "(e.g. 0.6 or 60%)", Double.class, false);
		parameters[1] = new DescriptionOfParameter("Minraresup (%)", "(e.g. 0.1 or 10%)", Double.class, false);
		parameters[2] = new DescriptionOfParameter("Max pattern length", "(e.g. 2 items)", Integer.class, true);
		return parameters;
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Database of instances","Transaction database", "Simple transaction database"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Patterns", "Frequent patterns", "Frequent itemsets"};

	}
	
	
}
