package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.IOException;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.algorithms.frequentpatterns.lhui.AlgoPHUIMiner;
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
 * This class describes the PHUI-Miner algorithm parameters. 
 * It is designed to be used by the graphical and command line interface.
 * 
 * @see AlgoPHUIMiner
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoPHUIMiner extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoPHUIMiner(){
		
	}

	@Override
	public String getName() {
		return "PHUI-Miner";
	}

	@Override
	public String getAlgorithmCategory() {
		return "HIGH-UTILITY PATTERN MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/PHUIMiner.php";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {
		int lminutil = getParamAsInteger(parameters[0]);
		
		// Window size
		int windowSize = getParamAsInteger(parameters[1]);
		
		// Window size
		double lambda = getParamAsInteger(parameters[2]);
		
		AlgoPHUIMiner algorithm = new AlgoPHUIMiner();
		algorithm.runAlgorithm(inputFile, outputFile, lminutil, windowSize, lambda);
		algorithm.printStats();
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[3];
		parameters[0] = new DescriptionOfParameter("Minimum utility", "(e.g. 40)", Long.class, false);
		parameters[1] = new DescriptionOfParameter("Window size", "(e.g. 3)", Integer.class, false);
		parameters[2] = new DescriptionOfParameter("Lambda", "(e.g. 2)", Double.class, false);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Yimin Zhang, Philippe Fournier-Viger";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Database of instances","Transaction database", "Transaction database with utility values and time"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Patterns",  "High-utility patterns","Peak high-utility itemsets"};
	}
	
}
