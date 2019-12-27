package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.File;
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
import ca.pfv.spmf.algorithms.frequentpatterns.vhuqi.AlgoVHUQI;
import ca.pfv.spmf.algorithms.frequentpatterns.vhuqi.enumVHUQIMethod;

/**
 * This class describes the VHUQI algorithm parameters. 
 * It is designed to be used by the graphical and command line interface.
 * 
 * @see AlgoApriori
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoVHUQI extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoVHUQI(){
	}

	@Override
	public String getName() {
		return "VHUQI";
	}

	@Override
	public String getAlgorithmCategory() {
		return "HIGH-UTILITY PATTERN MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/VHUQI.php";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {

		String inputProfitFile = getParamAsString(parameters[0]);
		
		File file = new File(inputFile);
		if (file.getParent() != null) {
			inputProfitFile = file.getParent() + File.separator + inputProfitFile;
		}
		
		float minUtility = getParamAsFloat(parameters[1]);
		
		//Related quantitative coefficient
		int relativeCoefficient = getParamAsInteger(parameters[2]);
		
		// Combination method: ALLC MINC OR MAXC
		enumVHUQIMethod method = enumVHUQIMethod.valueOf(getParamAsString(parameters[3]));
		
		AlgoVHUQI algo = new AlgoVHUQI();
		algo.runAlgorithm(inputFile, inputProfitFile, outputFile, minUtility, relativeCoefficient, method);
		
		algo.printStatistics();
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[4];
		parameters[0] = new DescriptionOfParameter("Profit table", "(e.g. HUQI_DB_Profit.txt)", String.class, false);
		parameters[1] = new DescriptionOfParameter("Minimum utility", "(e.g. 0.1)", Float.class, false);
		parameters[2] = new DescriptionOfParameter("Relative coefficient", "(e.g. 7)", Integer.class, false);
		parameters[3] = new DescriptionOfParameter("Method", "(e.g. MINC, MAXC, ALLC)", String.class, false);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Cheng-Wei Wu et al.";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Database of instances","Transaction database", "Transaction database with utility values (HUQI)"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Patterns",  "High-utility patterns","Quantitative high utility itemsets"};
	}
	
}
