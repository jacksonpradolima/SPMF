package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.IOException;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixspan.AlgoPrefixSpan;
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
import ca.pfv.spmf.algorithms.sequentialpatterns.prosecco.AlgoProsecco;

/**
 * This class describes the PrefixSpan algorithm parameters. It is designed to be used by the graphical and command line interface.
 * 
 * @see AlgoPrefixSpan
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoProSecCo extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoProSecCo(){
	}

	@Override
	public String getName() {
		return "ProSecCo";
	}

	@Override
	public String getAlgorithmCategory() {
		return "SEQUENTIAL PATTERN MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/ProSecCo.php";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {


		// Get the parameter "minsup"
		int blockSize = getParamAsInteger(parameters[0]); // number of transactions to process in each block
		int dbSize = getParamAsInteger(parameters[1]); // number of transactions in the dataset
		double errorTolerance = getParamAsDouble(parameters[2]); // failure probability
		double minsupRelative = getParamAsDouble(parameters[3]); // 50%
		
		// create an instance of the algorithm with minsup = 50 %
		AlgoProsecco algo = new AlgoProsecco(); 
		
		// execute the algorithm
		algo.runAlgorithm(inputFile, outputFile, blockSize, dbSize, errorTolerance, minsupRelative);    
		algo.printStatistics();
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[4];
		parameters[0] = new DescriptionOfParameter("Block size ", "(e.g. 1)", Integer.class, false);
		parameters[1] = new DescriptionOfParameter("Database size", "(e.g. 4)", Integer.class, true);
		parameters[2] = new DescriptionOfParameter("Error tolerance (%)", "(e.g. 0.05)", Double.class, true);
		parameters[3] = new DescriptionOfParameter("Minimum support (%)", "(e.g. 50%)", Double.class, true);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Sacha Servan-Schreiber";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Database of instances","Sequence database", "Simple Sequence Database"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Patterns", "Sequential patterns", "Progressive Frequent Sequential patterns"};
	}
	
}
