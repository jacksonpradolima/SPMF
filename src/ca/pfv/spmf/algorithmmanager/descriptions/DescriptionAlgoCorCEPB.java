package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.IOException;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.algorithms.sequentialpatterns.cost.AlgoCEPM;
/* This file is copyright (c) 2008-2020 Philippe Fournier-Viger
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
 * This class describes the CorCEPB algorithm parameters. 
 * It is designed to be used by the graphical and command line interface.
 * 
 * @see AlgoCEPM
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoCorCEPB extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoCorCEPB(){
	}

	@Override
	public String getName() {
		return "CORCEPB";
	}

	@Override
	public String getAlgorithmCategory() {
		return "SEQUENTIAL PATTERN MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/CORCEPB.php";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {

		// Get the parameter "minsup"
		int minsup = getParamAsInteger(parameters[0]);
		
		double maxcost = getParamAsDouble(parameters[1]);
		
		double minOccupancy = getParamAsDouble(parameters[2]);
		
		int maxPatternLength = 999;
		if (parameters.length >=4 && "".equals(parameters[3]) == false) {
			maxPatternLength = getParamAsInteger(parameters[3]);
		}
		
		// if true, patterns in the output file are sorted by utility
		boolean sortByCorrelation = false;
		
		if (parameters.length >=5 && "".equals(parameters[4]) == false) {
			sortByCorrelation = getParamAsBoolean(parameters[4]);
		}
		
		// Run the algorithm
		AlgoCEPM algo = new AlgoCEPM();
		algo.setMaximumPatternLength(maxPatternLength);
		algo.runAlgorithmCorCEPB(inputFile, outputFile, minsup, maxcost, minOccupancy, sortByCorrelation);
		algo.printStatistics();
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[5];
		parameters[0] = new DescriptionOfParameter("Minsup", "(e.g. 2)", Double.class, false);
		parameters[1] = new DescriptionOfParameter("Maxcost", "(e.g. 50)", Integer.class, false);
		parameters[2] = new DescriptionOfParameter("Minoccupancy", "(e.g. 0.1)", Double.class, false);
		parameters[3] = new DescriptionOfParameter("Max pattern length", "(e.g. 4)", Integer.class, true);
		parameters[4] = new DescriptionOfParameter("Sort by correlation?", "(default: false)", Boolean.class, true);
//		parameters[3] = new DescriptionOfParameter("Show sequence ids?", "(default: false)", Boolean.class, true);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Jiaxuan Li";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Database of instances","Sequence database", "Sequence Database with cost and binary utility"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Patterns", "Sequential patterns", "Cost-efficient Sequential patterns"};
	}
//
//	@Override
//	String[] getSpecialInputFileTypes() {
//		return null; //new String[]{"ARFF"};
//	}
	
}
