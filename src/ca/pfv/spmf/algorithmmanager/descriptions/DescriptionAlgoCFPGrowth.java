package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.File;
import java.io.IOException;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.algorithms.frequentpatterns.cfpgrowth.AlgoCFPGrowth;
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
 * This class describes the AprioriClose algorithm parameters. 
 * It is designed to be used by the graphical and command line interface.
 * 
 * @see AprioriClose
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoCFPGrowth extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoCFPGrowth(){
	}

	@Override
	public String getName() {
		return "CFPGrowth++";
	}

	@Override
	public String getAlgorithmCategory() {
		return "FREQUENT ITEMSET MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#cfpgrowth";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {
		String misFile = parameters[0];

		File file = new File(inputFile);
		String misFileFullPath;
		if (file.getParent() == null) {
			misFileFullPath = misFile;
		} else {
			misFileFullPath = file.getParent() + File.separator + misFile;
		}

		// Applying the algorithm
		AlgoCFPGrowth algo = new AlgoCFPGrowth();
		algo.runAlgorithm(inputFile, outputFile, misFileFullPath);
		algo.printStats();
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[1];
		parameters[0] = new DescriptionOfParameter("MIS file name", "(e.g. MIS.txt)", String.class, false);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Azadeh Soltani";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Database of instances","Transaction database", "Simple transaction database"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Patterns", "Frequent patterns", "Frequent itemsets", "Frequent itemsets with multiple thresholds"};
	}
	
}
