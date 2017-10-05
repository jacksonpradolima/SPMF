package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.IOException;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.tools.dataset_converter.Formats;
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
import ca.pfv.spmf.tools.dataset_converter.TransactionDatabaseConverter;

/**
 * This class describes the algorithm to convert a transaction database to SPMF format. It is designed to be used by the graphical and command line interface.
 * 
 * @see TransactionDatabaseConverter
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoConvertTransactionDBToSPMF extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoConvertTransactionDBToSPMF(){
	}

	@Override
	public String getName() {
		return "Convert_a_transaction_database_to_SPMF_format";
	}

	@Override
	public String getAlgorithmCategory() {
		return "DATASET TOOLS";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#convtdb";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {
		String format = getParamAsString(parameters[0]);
		int transactionCount = getParamAsInteger(parameters[1]);

		long startTime = System.currentTimeMillis();
		TransactionDatabaseConverter converter = new TransactionDatabaseConverter();
		converter.convert(inputFile, outputFile, Formats.valueOf(format),
				transactionCount);
		long endTIme = System.currentTimeMillis();
		System.out.println("Transaction database converted.  Time spent for conversion = " + (endTIme - startTime) + " ms.");
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[2];
		parameters[0] = new DescriptionOfParameter("Input format", "(e.g. CSV_INTEGER)", String.class, false);
		parameters[1] = new DescriptionOfParameter("Transaction count", "(e.g. 5)", Integer.class, false);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Philippe Fournier-Viger";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Transaction database in non SPMF format"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Database of instances","Transaction database", "Simple transaction database"};
	}
//
//	@Override
//	String[] getSpecialInputFileTypes() {
//		return null; //new String[]{"ARFF"};
//	}
	
}
