package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.IOException;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.algorithms.episodes.upspan.AlgoUP_Span;
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
 * This class describes the UP-SPAN algorithm parameters. 
 * It is designed to be used by the graphical and command line interface.
 * 
 * @see AlgoUP_Span
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoUP_SPAN extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoUP_SPAN(){
	}

	@Override
	public String getName() {
		return "UP-SPAN";
	}

	@Override
	public String getAlgorithmCategory() {
		return "EPISODE MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/UP_SPAN.PHP";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {
		double minutil = getParamAsDouble(parameters[0]);
		int maxTimeDuration = getParamAsInteger(parameters[1]);  
		boolean outputSingleEvents  = getParamAsBoolean(parameters[2]);  
		
		// Applying the algorithm
		AlgoUP_Span algo = new AlgoUP_Span();

		algo.runAlgorithm(inputFile, outputFile, minutil, maxTimeDuration, outputSingleEvents);
		algo.printStats();
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[3];
		parameters[0] = new DescriptionOfParameter("Minimum utility", "(e.g. 56%)", Double.class, false);
		parameters[1] = new DescriptionOfParameter("Max. Time duration", "(e.g. 2)", Integer.class, false);
		parameters[2] = new DescriptionOfParameter("Output single events", "(e.g. false)", Boolean.class, false);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Cheng-Wei Wu et al.";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Database of instances","Transaction database", "Transaction database with utility values"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Patterns",  "Episodes", "High-utility patterns","High-Utility episodes"};
	}
	
}
