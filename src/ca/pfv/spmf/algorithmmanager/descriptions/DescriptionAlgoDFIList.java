package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.IOException;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.algorithms.frequentpatterns.DFIGrowth.AlgoDFIGrowth;
/* This is an implementation of the DFI-Growth algorithm. 
* 
* Copyright (c) 2018  Wu Cheng-Wei, Huang Jian-Tao
* 
* This file is part of the SPMF DATA MINING SOFTWARE 
* (http://www.philippe-fournier-viger.com/spmf). 
* 
* SPMF is free software: you can redistribute it and/or modify it under the 
* terms of the GNU General Public License as published by the Free Software 
* Foundation, either version 3 of the License, or (at your option) any later version. 
*
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY 
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR 
* A PARTICULAR PURPOSE. See the GNU General Public License for more details. 
* 
* You should have received a copy of the GNU General Public License along with 
* SPMF. If not, see <http://www.gnu.org/licenses/>. 
* 
* @author Wu Cheng-Wei, Huang Jian-Tao, 2018
*/
import ca.pfv.spmf.algorithms.frequentpatterns.DFIList.AlgoDFIList;

/**
 * This class describes the DFI-Growth algorithm parameters. 
 * It is designed to be used by the graphical and command line interface.
 * 
 * @see DFI-Growth
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoDFIList extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoDFIList(){
	}

	@Override
	public String getName() {
		return "DFI-List";
	}

	@Override
	public String getAlgorithmCategory() {
		return "FREQUENT ITEMSET MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/DFI-List.php";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {
		
		AlgoDFIList algo = new AlgoDFIList();
		algo.runAlgorithm(inputFile, outputFile);
		algo.printStats();
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[0];
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Wu et al.";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Frequent closed itemsets"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Patterns", "Frequent patterns", "Frequent itemsets"};
	}
	
}
