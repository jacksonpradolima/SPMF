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
import ca.pfv.spmf.algorithms.sequentialpatterns.goKrimp.AlgoGoKrimp;
import ca.pfv.spmf.algorithms.sequentialpatterns.goKrimp.DataReader;

/**
 * This class describes the GoKrimp algorithm. 
 * It is designed to be used by the graphical and command line interface.
 * 
 * @see AlgoGoKrimp
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoGoKrimp extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoGoKrimp(){
	}

	@Override
	public String getName() {
		return "GoKrimp";
	}

	@Override
	public String getAlgorithmCategory() {
		return "SEQUENTIAL PATTERN MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#gokrimp";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {

		// file for sensitive
		String labelFilePath = parameters[0];
		if (labelFilePath == null) {
			labelFilePath = "";
		} else {
			File file = new File(inputFile);
			if (file.getParent() == null) {
				labelFilePath = parameters[0];
			} else {
				labelFilePath = file.getParent() + File.separator
						+ parameters[0];
			}
		}

		DataReader d = new DataReader();
		AlgoGoKrimp g = d.readData_SPMF(inputFile, labelFilePath);
		g.setOutputFilePath(outputFile); // if not set, then result will be
											// printed to console
		g.gokrimp();
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[1];
		parameters[0] = new DescriptionOfParameter("Label file name ", "(e.g. test_goKrimp.lab)", String.class, true);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Hoang Thanh Lam";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Database of instances","Sequence database", "Simple sequence database"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Patterns", "Sequential patterns", "Frequent Sequential patterns", "Compressing sequential patterns"};
	}
	
}
