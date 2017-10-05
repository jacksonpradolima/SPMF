package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.IOException;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.multidimensionalpatterns.AlgoDim;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.multidimensionalsequentialpatterns.AlgoSeqDim;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.multidimensionalsequentialpatterns.MDSequenceDatabase;
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
 * This class describes the SEQDIM algorithm parameters when applied with BIDE and Charm. 
 * It is designed to be used by the graphical and command line interface.
 * 
 * @see AlgoSeqDim
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoSeqDIMBideCharm extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoSeqDIMBideCharm(){
	}

	@Override
	public String getName() {
		return "SeqDim_(BIDE+Charm)";
	}

	@Override
	public String getAlgorithmCategory() {
		return "SEQUENTIAL PATTERN MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#exampleMDSPM1";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {

		double minsup = getParamAsDouble(parameters[0]); 

		MDSequenceDatabase contextMDDatabase = new MDSequenceDatabase(); //
		contextMDDatabase.loadFile(inputFile);
		// contextMDDatabase.printContext();

		AlgoDim algoDim = new AlgoDim(false, true);

		AlgoSeqDim algoSeqDim = new AlgoSeqDim();

		// Apply algorithm
		ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.AlgoBIDEPlus bideplus = new ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.AlgoBIDEPlus(
				minsup);
		algoSeqDim.runAlgorithm(contextMDDatabase, bideplus, algoDim, true,
				outputFile);

		// Print results
		algoSeqDim.printStatistics(contextMDDatabase.size());
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[1];
		parameters[0] = new DescriptionOfParameter("Minsup (%)", "(e.g. 0.4 or 40%)", Double.class, false);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Philippe Fournier-Viger";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Database of instances","Sequence database", "Multi-dimensional sequence database"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Patterns", "Sequential patterns", "Frequent sequential patterns", "Multi-dimensional frequent closed sequential patterns"};
	}
//
//	@Override
//	String[] getSpecialInputFileTypes() {
//		return null; //new String[]{"ARFF"};
//	}
	
}
