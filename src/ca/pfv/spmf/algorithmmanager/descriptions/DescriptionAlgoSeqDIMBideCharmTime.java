package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.IOException;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.AlgoFournierViger08;
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
 * This class describes the SEQDIM algorithm parameters when applied with BIDE, Charm, and time constraints. 
 * It is designed to be used by the graphical and command line interface.
 * 
 * @see AlgoSeqDim
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoSeqDIMBideCharmTime extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoSeqDIMBideCharmTime(){
	}

	@Override
	public String getName() {
		return "SeqDim_(BIDE+Charm)+time";
	}

	@Override
	public String getAlgorithmCategory() {
		return "SEQUENTIAL PATTERN MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#example14";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {

		double minsup = getParamAsDouble(parameters[0]); 
		double minInterval = getParamAsDouble(parameters[1]);
		double maxInterval = getParamAsDouble(parameters[2]);
		double minWholeInterval = getParamAsDouble(parameters[3]);
		double maxWholeInterval = getParamAsDouble(parameters[4]);

		MDSequenceDatabase contextMDDatabase = new MDSequenceDatabase(); //
		contextMDDatabase.loadFile(inputFile);
		// contextMDDatabase.printContext();

		AlgoDim algoDim = new AlgoDim(false, true); // <-- here

		AlgoSeqDim algoSeqDim2 = new AlgoSeqDim();

		// Apply algorithm
		AlgoFournierViger08 algoPrefixSpanHirateClustering = new AlgoFournierViger08(
				minsup, minInterval, maxInterval, minWholeInterval,
				maxWholeInterval, null, true, true);
		algoSeqDim2.runAlgorithm(contextMDDatabase,
				algoPrefixSpanHirateClustering, algoDim, true, outputFile);

		// Print results
		algoSeqDim2.printStatistics(contextMDDatabase.size());
		// NOTE : IF YOU DON'T WANT TO MINE *CLOSED* MD-SEQUENCES, JUST
		// CHANGE THE FOUR VALUES "true" for
		// "FALSE" in this example.
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[5];
		parameters[0] = new DescriptionOfParameter("Minsup (%)", "(e.g. 0.4 or 40%)", Double.class, false);
		parameters[1] = new DescriptionOfParameter("Min time interval", "(e.g. 1 itemsets)", Double.class, false);
		parameters[2] = new DescriptionOfParameter("Max time interval", "(e.g. 5 itemsets)", Double.class, false);
		parameters[3] = new DescriptionOfParameter("Min whole time interval", "(e.g. 1 itemsets)", Double.class, false);
		parameters[4] = new DescriptionOfParameter("Max whole time interval", "(e.g. 5 itemsets)", Double.class, false);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Philippe Fournier-Viger";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Database of instances", "Sequence database", "Sequence database with timestamps", "Multi-dimensional sequence database with timestamps"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Patterns", "Sequential patterns", "Frequent sequential patterns", "Multi-dimensional frequent closed sequential patterns with timestamps"};
	}
//
//	@Override
//	String[] getSpecialInputFileTypes() {
//		return null; //new String[]{"ARFF"};
//	}
	
}
