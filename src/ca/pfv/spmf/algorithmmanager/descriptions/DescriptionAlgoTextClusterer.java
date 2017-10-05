package ca.pfv.spmf.algorithmmanager.descriptions;

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
import ca.pfv.spmf.algorithms.clustering.text_clusterer.TextClusterAlgo;

/**
 * This class describes the TextClusterer algorithm parameters. 
 * It is designed to be used by the graphical and command line interface.
 * 
 * @see TextClusterAlgo
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoTextClusterer extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoTextClusterer(){
	}

	@Override
	public String getName() {
		return "TextClusterer";
	}

	@Override
	public String getAlgorithmCategory() {
		return "CLUSTERING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#TextClusterer";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {
		boolean performStemming = getParamAsBoolean(parameters[0]);
		boolean removeStopWords = getParamAsBoolean(parameters[0]);
		// Apply the algorithm
		TextClusterAlgo algo = new TextClusterAlgo();
		algo.runAlgorithm(inputFile, outputFile, performStemming, removeStopWords);
		algo.printStatistics();
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[2];
		parameters[0] = new DescriptionOfParameter("Perform stemming", "(e.g. true)", Boolean.class, false);
		parameters[1] = new DescriptionOfParameter("Remove stop words", "(e.g. true)", Boolean.class, false);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Sabarish Raghu";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Database of instances", "Database of double vectors"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Text clusters"};
	}
	
}
