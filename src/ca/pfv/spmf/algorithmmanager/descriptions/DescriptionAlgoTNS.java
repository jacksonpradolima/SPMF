package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.IOException;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.algorithms.sequential_rules.topseqrules_and_tns.AlgoTNS;
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
 * This class describes the TNS algorithm parameters. 
 * It is designed to be used by the graphical and command line interface.
 * 
 * @see AlgoTNS
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoTNS extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoTNS(){
	}

	@Override
	public String getName() {
		return "TNS";
	}

	@Override
	public String getAlgorithmCategory() {
		return "SEQUENTIAL RULE MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#tns";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {
		// Load database into memory
		ca.pfv.spmf.input.sequence_database_array_integers.SequenceDatabase database = new ca.pfv.spmf.input.sequence_database_array_integers.SequenceDatabase();
		database.loadFile(inputFile);

		int k = getParamAsInteger(parameters[0]);
		double minconf = getParamAsDouble(parameters[1]);
		int delta = getParamAsInteger(parameters[2]);

		AlgoTNS algo = new AlgoTNS();
		algo.runAlgorithm(k, database, minconf, delta);
		algo.printStats();
		algo.writeResultTofile(outputFile); // to save results to file
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[3];
		parameters[0] = new DescriptionOfParameter("k", "(e.g. 3 patterns)", Integer.class, false);
		parameters[1] = new DescriptionOfParameter("Minconf (%)", "(e.g. 0.8 or 80%)", Double.class, false);
		parameters[2] = new DescriptionOfParameter("Delta", "(e.g. 2 patterns)", Integer.class, false);
		return parameters;
	}


	@Override
	public String getImplementationAuthorNames() {
		return "Philippe Fournier-Viger";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Database of instances","Sequence database", "Simple sequence database"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Patterns", "Sequential rules", "Top-k frequent non-redundant sequential rules"};
	}
	
}
