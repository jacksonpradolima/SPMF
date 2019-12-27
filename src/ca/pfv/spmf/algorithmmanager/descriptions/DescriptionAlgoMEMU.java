package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.File;
import java.io.IOException;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.algorithms.frequentpatterns.memu.AlgoMEMU;
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
 * This class describes the HAUI-Miner algorithm parameters. 
 * It is designed to be used by the graphical and command line interface.
 * 
 * @see AlgoMEMU
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoMEMU extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoMEMU(){
	}

	@Override
	public String getName() {
		return "MEMU";
	}

	@Override
	public String getAlgorithmCategory() {
		return "HIGH-UTILITY PATTERN MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/MEMU.php";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {
		int beta = getParamAsInteger(parameters[0]);
		int glmau = getParamAsInteger(parameters[1]);
		String profitFile = getParamAsString(parameters[2]);
		
		File file = new File(inputFile);
		if (file.getParent() != null) {
			profitFile = file.getParent() + File.separator + profitFile;
		}
		
		//Applying the HAUIMMAU algorithm
		AlgoMEMU algorithm = new AlgoMEMU();
		algorithm.runAlgorithm(profitFile,inputFile, outputFile, beta, glmau);
		algorithm.printStats();
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[3];
		parameters[0] = new DescriptionOfParameter("BETA", "(e.g. 2)", Integer.class, false);
		parameters[1] = new DescriptionOfParameter("GLMAU", "(e.g. 25)", Integer.class, false);
		parameters[2] = new DescriptionOfParameter("Profit file", "(e.g. UtilityDB_profit.txt)", String.class, false);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Shifeng Ren";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Database of instances","Transaction database", "Transaction database with utility values (MEMU)"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Patterns",  "High-utility patterns","High average-utility itemsets"};
	}
	
}
