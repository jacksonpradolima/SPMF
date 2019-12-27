package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.IOException;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.algorithms.frequentpatterns.mpfps.AlgoMPFPS_BFS;
import ca.pfv.spmf.algorithms.frequentpatterns.mpfps.AlgoMPFPS_DFS;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.AlgoCloSpan;
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
 * This class describes the MPFPS-BFS algorithm parameters. It is designed to be used by the graphical and command line interface.
 * 
 * @see AlgoMPFPS_BFS
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoMPFPSDFS extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoMPFPSDFS(){
	}

	@Override
	public String getName() {
		return "MPFPS-DFS";
	}

	@Override
	public String getAlgorithmCategory() {
		return "PERIODIC PATTERN MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/MPFPS.php";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws Exception {
		// Max standard deviation
		double maxStandardDeviation = getParamAsDouble(parameters[0]);

		// Min RA
		double minRA = getParamAsDouble(parameters[1]);

		// Max periodicity
		int maxPeriodicity = getParamAsInteger(parameters[2]);

		// Minimum support
		int minimumSupport = getParamAsInteger(parameters[3]);

		// Run the algorithm
		AlgoMPFPS_DFS algorithm = new AlgoMPFPS_DFS();
		algorithm.runAlgorithm(maxStandardDeviation, minRA, maxPeriodicity,
				minimumSupport, inputFile, outputFile);
		algorithm.printStats();
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[4];
		parameters[0] = new DescriptionOfParameter("MaxStd (%)", "(e.g. 10)", Double.class, false);
		parameters[1] = new DescriptionOfParameter("MinRA (%)", "(default: 0.25)", Double.class, true);
		parameters[2] = new DescriptionOfParameter("MaxPer", "(e.g. 10)", Integer.class, false);
		parameters[3] = new DescriptionOfParameter("Minsup", "(e.g. 2)", Integer.class, false);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Zhitian Li, Philippe Fournier-Viger";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Database of instances","Sequence database", "Simple sequence database"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Patterns", "Periodic patterns", "Periodic frequent patterns", "Periodic frequent itemsets common to multiple sequences"};
	}
	
}
