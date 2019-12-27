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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This is an implementation of the "IHAUPM" algorithm for High-Average-Utility Itemsets Mining
 * as described in the conference paper : <br/><br/>
 *
 * Jerry Chun-Wei Lin, Shifeng Ren, and Philippe Fournier-Viger. Efficiently Updating the Discovered High Average-Utility Itemsets with Transaction Insertion. EAAI (unpublished, minor revision)
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

public class AlgoIHAUPM {

	public AlgoIHAUPM(){
		
	}
	
	/**
	 * Run the algorithm
	 * @param profitsFile
	 * @param dataFileName
	 * @param numOfTrancsInDB
	 * @param threshold
	 * @param isInsertMode
	 * @param numOfInsertedTransactions
	 * @param numOfInsert
	 * @param increResultFolder
	 * @param batchResultFolder
	 * @param writeMinedInfoFile
	 * @throws Exception
	 */
	public void runAlgorithm(final String profitsFile, final String dataFileName,
			int numOfTrancsInDB, double threshold, boolean isInsertMode,
			int numOfInsertedTransactions, int numOfInsert, 
			String increResultFolder, String batchResultFolder, String writeMinedInfoFile) throws Exception {
		
		MemoryLogger.getInstance().reset();

		File increFolder = new File(increResultFolder);
		File batchFolder = new File(batchResultFolder);
		if(!increFolder.exists())
		    increFolder.mkdir();
		if(!batchFolder.exists())
		    batchFolder.mkdir();
		
		long start = System.currentTimeMillis();
		
		

		IAUTree tree = new IAUTree();
		tree.construct(dataFileName,
                profitsFile, threshold, numOfTrancsInDB, isInsertMode);

		IHAUPM m = null;
		String itemsetFile=null ;
		BufferedWriter writeMinedInfo = null;
		if(writeMinedInfoFile!=null)
			writeMinedInfo = new BufferedWriter(new FileWriter(writeMinedInfoFile));


		int[] minedInfo = new int[]{-1,-1};
		if(isInsertMode) { // activate incremental mode
			int numOfTrancsInEachStep = numOfInsertedTransactions / numOfInsert;
			int numOfTrancsInLastStep = numOfInsertedTransactions;
			int numOfHasInsertTrancs = 0;
			for(int i=0; i<numOfInsert-1; i++) {
				// insert new transactions into HAUP-tree
				tree.insertNewDB(numOfTrancsInEachStep,false,
						dataFileName, numOfTrancsInDB+numOfHasInsertTrancs);
				m = new IHAUPM(tree);

                minedInfo = m.mine(itemsetFile);

				// write running status of the algorithm to the specified file.
				write(writeMinedInfo, minedInfo, i+1, start);
				numOfTrancsInLastStep -= numOfTrancsInEachStep;
				numOfHasInsertTrancs += numOfTrancsInEachStep;
			}
			tree.insertNewDB(numOfTrancsInLastStep, true,
					dataFileName, numOfTrancsInDB+numOfHasInsertTrancs);

            minedInfo = m.mine(itemsetFile);
			// write running status info to the specified file.
			write(writeMinedInfo, minedInfo, numOfInsert, start);
		} else {
			tree.clear();
			int numOfTrancsInEachStep = numOfInsertedTransactions / numOfInsert;
			int acc = numOfTrancsInEachStep;
			for(int i=0; i<numOfInsert-1; i++) {
				tree.construct(dataFileName, profitsFile,
						threshold, numOfTrancsInDB+acc, false);
				m = new IHAUPM(tree);

                minedInfo = m.mine(itemsetFile);
				// write mined info to the specified file.
				write(writeMinedInfo, minedInfo, i+1, start);
				tree.clear();
				acc += numOfTrancsInEachStep;
			}
			tree.construct(dataFileName, profitsFile,
					threshold, numOfTrancsInDB + numOfInsertedTransactions, false);

            minedInfo = m.mine(itemsetFile);
			// write mined info to the specified file.
			write(writeMinedInfo, minedInfo, numOfInsert, start);
		}
		if(writeMinedInfo!=null) writeMinedInfo.close();
		long end = System.currentTimeMillis();

		// output running information.
		System.out.println("candidateNum="+ minedInfo[0]);
		System.out.println("HAUIs=" +minedInfo[1]);
		System.out.println("Time(s)=" +(end-start) / (double)1000);
		System.out.println("Memory(M)=" + MemoryLogger.getInstance().getMaxMemory());
	}

	private static void write(BufferedWriter out, int[] minedInfo, int j, long startTime) throws IOException{
		if(out==null) return;
		out.write("***********The running status of "+ j +"-th insertion***************\n");
		StringBuilder wrtStr = new StringBuilder();
		int i;
		for(i=0; i<minedInfo.length; i++){
			wrtStr.append(minedInfo[i] +" ");
		}
		wrtStr.append((System.currentTimeMillis()-startTime)+" ");
		wrtStr.append(MemoryLogger.getInstance().getMaxMemory()+"\n");
		//wrtStr.append(minedInfo[i]+"");
		out.write(wrtStr.toString());
	}


}

