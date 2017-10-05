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
import ca.pfv.spmf.tools.dataset_stats.SequenceStatsGenerator;
import ca.pfv.spmf.tools.other_dataset_tools.ResizeDatabaseTool;

/**
 * This class describes the algorithm to resize a database. It is designed to be used by the graphical and command line interface.
 * 
 * @see SequenceStatsGenerator
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoResizeADatabase extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoResizeADatabase(){
	}

	@Override
	public String getName() {
		return "Resize_a_database";
	}

	@Override
	public String getAlgorithmCategory() {
		return "DATASET TOOLS";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#resizedatabase";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {
		double percentage = getParamAsDouble(parameters[0]);
		ResizeDatabaseTool tool = new ResizeDatabaseTool();
		tool.convert(inputFile, outputFile, percentage);
		System.out.println("Finished resizing the database.");
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[1];
		parameters[0] = new DescriptionOfParameter("Percentage (%)", "(e.g. 0.7 or 70%)", Double.class, false);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Philippe Fournier-Viger";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Database of instances"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Database of instances"};
	}
//
//	@Override
//	String[] getSpecialInputFileTypes() {
//		return null; //new String[]{"ARFF"};
//	}
	
}
