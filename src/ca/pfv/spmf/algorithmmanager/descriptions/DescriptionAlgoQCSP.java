package ca.pfv.spmf.algorithmmanager.descriptions;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.algorithms.sequentialpatterns.qcsp.AlgoQCSP;
/**
 * This is an implementation of the QCSP algorithm.
 * For more information please refer the paper Mining Top-K Quantile-based Cohesive Sequential Patterns 
 * by Len Feremans, Boris Cule and Bart Goethals, published in 2018 SIAM International Conference on Data Mining (SDM18).<br/>
 *
 * Copyright (c) 2020 Len Feremans (Universiteit Antwerpen)
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
 *
 * You should have received a copy of the GNU General Public License along wit
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Len Feremans
 */
import ca.pfv.spmf.algorithms.sequentialpatterns.spam.AlgoTKS;

/**
 * This class describes the QCSP algorithm parameters. 
 * It is designed to be used by the graphical and command line interface.
 * 
 * @see AlgoTKS
 * @author Len Feremans
 */
public class DescriptionAlgoQCSP extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoQCSP(){
	}

	@Override
	public String getName() {
		return "QCSP";
	}

	@Override
	public String getAlgorithmCategory() {
		return "SEQUENTIAL PATTERN MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "https://bitbucket.org/len_feremans/qcsp_public";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws Exception {

		int minsup = getParamAsInteger(parameters[0]);
		double alpha = getParamAsDouble(parameters[1]);
		int maxsize = getParamAsInteger(parameters[2]);
		int k = getParamAsInteger(parameters[3]);
		String labelFile = null;
		if(parameters.length > 4) {
			labelFile = parameters[4];
			if(labelFile.trim().equals("")) {
				labelFile = null;
			}
		}
		
		//--------------- Applying the  algorithm  ---------//
		AlgoQCSP algorithm = new AlgoQCSP();
		algorithm.setLabelsFile(labelFile);
		algorithm.runAlgorithm(inputFile, outputFile, minsup, alpha, maxsize, k);
		// Print statistics
		algorithm.printStatistics();
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[5];
		parameters[0] = new DescriptionOfParameter("Minsup ", "Frequency threshold on single item", Integer.class, false);
		parameters[1] = new DescriptionOfParameter("Alpha ", "Cohesion threshold, e.g. 2 times pattern size", Double.class, false);
		parameters[2] = new DescriptionOfParameter("Max pattern length", "", Integer.class, false);
		parameters[3] = new DescriptionOfParameter("Top-k patterns", "", Integer.class, false);
		parameters[4] = new DescriptionOfParameter("Label file name ", "(e.g. test_goKrimp.lab)", String.class, true);//optional	
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Feremans et al.";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Database of instances","Sequence database", "Simple sequence database", "Single sequence"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Patterns", "Sequential patterns", "Top-k sequential patterns with quantile-based cohesion"};
	}
	
}
