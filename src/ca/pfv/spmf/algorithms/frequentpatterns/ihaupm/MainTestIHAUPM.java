package ca.pfv.spmf.algorithms.frequentpatterns.ihaupm;

/* This file is copyright (c) 2008-2015 Philippe Fournier-Viger
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
 *
 */

import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.test.MainTestMEMU;

/**
 * This is an implementation of the "IHAUPM" algorithm for High-Average-Utility
 * Itemsets Mining as described in the conference paper : <br/>
 * <br/>
 *
 * Jerry Chun-Wei Lin, Shifeng Ren, and Philippe Fournier-Viger. Efficiently
 * Updating the Discovered High Average-Utility Itemsets with Transaction
 * Insertion. EAAI (unpublished, minor revision)
 *
 * @see IHAUPM.HAUPGrowth
 * @see tree.IAUTree
 * @see tree.IAUNode
 * @see tree.TableNode
 * @see util.Item
 * @see util.Itemset
 * @see util.StackElement
 * @author Shi-Feng Ren
 */

public class MainTestIHAUPM {


	public static void main(String[] args) throws Exception {

		String increResultFolder = "\\increResult\\";
		String batchResultFolder = "\\batchResult\\";

		String dataFileName = fileToPath("UtilityDB.txt"); // specify dataset
															// name
		String profitsFile = fileToPath("UtilityDB_profit.txt");

		String outputFile = "output.txt";

		// the number of transactions of the specified dataset
		int numOfTrancsInDB = 4;

		// specify threshold (%)
		double threshold = 0.2;

		// true: insert mode; false: batch mode
		boolean isInsertMode = true;

		// specify the number of inserted transactions
		int numOfInsertedTransactions = 2;

		// the number of insertion in an experiment
		int numOfInsert = 2;

		AlgoIHAUPM algo = new AlgoIHAUPM();
		
		algo.runAlgorithm(profitsFile, dataFileName, numOfTrancsInDB, threshold,
				isInsertMode, numOfInsertedTransactions, numOfInsert,
				increResultFolder, batchResultFolder, outputFile);
	}


	public static String fileToPath(String filename)
			throws UnsupportedEncodingException {
		URL url = MainTestMEMU.class.getResource(filename);
		return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
	}

}
