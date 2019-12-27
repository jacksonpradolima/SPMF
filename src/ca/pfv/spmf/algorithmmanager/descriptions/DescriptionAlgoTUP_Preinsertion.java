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
import ca.pfv.spmf.algorithms.episodes.tup.tup_preinsertion.AlgoTUP_preinsertion;

/**
 * This class describes the TUP_Preinsertion algorithm parameters. 
 * It is designed to be used by the graphical and command line interface.
 * 
 * @see AlgoTUP_preinsertion
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoTUP_Preinsertion extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoTUP_Preinsertion(){
	}

	@Override
	public String getName() {
		return "TUP_Preinsertion";
	}

	@Override
	public String getAlgorithmCategory() {
		return "EPISODE MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/TUP.php";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {
		int maxTimeDuration = getParamAsInteger(parameters[0]);  
		int k  = getParamAsInteger(parameters[1]);  
		
		// Applying the algorithm
		AlgoTUP_preinsertion algo = new AlgoTUP_preinsertion();

		algo.runAlgorithm(inputFile, maxTimeDuration, k);
		algo.writeResultTofile(outputFile);
		algo.printStats();
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[2];
		parameters[0] = new DescriptionOfParameter("Max. Time duration", "(e.g. 2)", Integer.class, false);
		parameters[1] = new DescriptionOfParameter("k", "(e.g. 3)", Integer.class, false);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Rathore et al.";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Database of instances","Transaction database", "Transaction database with utility values"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Patterns",  "Episodes", "High-utility patterns","High-Utility episodes", "Top-k High-Utility episodes"};
	}
	
}
