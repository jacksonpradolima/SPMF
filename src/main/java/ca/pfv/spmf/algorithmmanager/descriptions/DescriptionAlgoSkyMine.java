package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.File;
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
import ca.pfv.spmf.algorithms.frequentpatterns.skymine.AlgoSkyMine;

/**
 * This class describes the SkyMine algorithm parameters. 
 * It is designed to be used by the graphical and command line interface.
 * 
 * @see AlgoSkyMine
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoSkyMine extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoSkyMine(){
	}

	@Override
	public String getName() {
		return "SkyMine";
	}

	@Override
	public String getAlgorithmCategory() {
		return "HIGH-UTILITY PATTERN MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/documentation.php#skymine";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {
		// file for sensitive
		String itemUtilities = parameters[0];
		
		File file = new File(inputFile);
		String itemUtilitiesPath;
		if (file.getParent() == null) {
			itemUtilitiesPath = itemUtilities;
		} else {
			itemUtilitiesPath = file.getParent() + File.separator + itemUtilities;
		}
		
		// Create an instance of the algorithm
		AlgoSkyMine up = new AlgoSkyMine();
		up.runAlgorithm(inputFile, itemUtilitiesPath, outputFile, true, true);
		// print statistics about the algorithm execution
		up.printStats();
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[1];
		parameters[0] = new DescriptionOfParameter("Utility file name", "(e.g. SkyMineItemUtilities.txt)", String.class, false);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Vikram Goyal, Ashish Sureka, Dhaval Patel, Siddharth Dawar";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Database of instances","Transaction database", "Transaction database with utility values skymine format"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Patterns",  "Skyline patterns", "High-utility patterns", "Skyline High-utility itemsets"};
	}
	
}
