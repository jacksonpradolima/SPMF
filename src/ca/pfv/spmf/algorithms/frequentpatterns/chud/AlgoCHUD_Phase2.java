package ca.pfv.spmf.algorithms.frequentpatterns.chud;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import ca.pfv.spmf.tools.MemoryLogger;

/* This file is copyright (c) 2011 Cheng-Wei-Wu, Philippe Fournier-Viger
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
 * This class implements the Phase 2 of the CHUD algorithm for closed high utility itemset mining.
 * The CHUD algorithm was published in the following paper: <br/><br/>
 * 
 * Wu, C.-W., Fournier-Viger, P., Yu., P. S., Tseng, V. S. (2011). Efficient Mining of a 
 * Concise and Lossless Representation of High Utility Itemsets. Proceedings of the 11th 
 * IEEE Intern. Conference on Data Mining (ICDM 2011). IEEE CS Press, pp.824-833.
 * 
 * @see AlgoCHUD
 */
class AlgoCHUD_Phase2 {
	
	/** the time when the algorithm was started */
	private long startTimestamp = 0;
	
	/** total execution time */
	protected long totaltime =0;
	
	/** number of HUIs found */
	protected int huiCount =0;
	
	/** Maximum amount of memory */
	protected double maxMemory = 0;
	
	
	/** number of transactions of a database to be used by CHUD */
	private int maximumNumberOfTransactions = Integer.MAX_VALUE;

	/**
	 * Constructor
	 */
	public AlgoCHUD_Phase2() {}

	/**
	 * Run the phase 2 of the CHUD algorithm
	 * @param path
	 * @param filePathPhase1
	 * @param filePathOutput  the path of the final output file for the results
	 * @param minUtility the minimum utility thrsehold
	 * @param phase2SaveHUIsInOneFile  if true all results should 
	 *    be saved in a single file
	 * @throws IOException if some error occurs while reading/writing results to a file
	 */
	public void runAlgorithm(String path, String filePathPhase1, String filePathOutput,  int minUtility, boolean phase2SaveHUIsInOneFile) throws IOException {
		startTimestamp  = System.currentTimeMillis();
		MemoryLogger.getInstance().reset();
		
		// PFV 2014 ============================================================
		UtilityTransactionDatabaseTP db = new UtilityTransactionDatabaseTP();
		db.loadFile(path);
		
		// ========================  PHASE 2: Calculate exact utility of each candidate =============
		BufferedReader reader = new BufferedReader(new FileReader(filePathPhase1));
		int level =1;
		BufferedWriter writer = new BufferedWriter(new FileWriter(filePathOutput)); 
		String line = reader.readLine();
		int tidcount = 0;
		while( line != null && line.length() > 2){
			
			String[] lineSplited = line.split(":");
			String[] itemsetStr = lineSplited[0].split(" ");
			int[] itemset = new int[itemsetStr.length];
			for(int i=0; i< itemsetStr.length; i++) {
				itemset[i] = Integer.parseInt(itemsetStr[i]);
			}			
			
			String[] tidsetStr = lineSplited[1].split(" ");
			int utility = 0;
			
			Arrays.sort(itemset);
			
			for(int i=0; i< tidsetStr.length; i++){
				int tid = Integer.parseInt(tidsetStr[i]);
				TransactionTP transaction = db.getTransactions().get(tid);
				
				int pos = 0;
				for(int j=0; j < itemset.length; j++) {
					while(itemset[j] != transaction.getItems().get(pos)) {
						pos++;
					}
					utility += transaction.getItemsUtilities().get(pos);
					pos++;
				}
			}
			
			// read next line
			line = reader.readLine();
			
			// Save to file
			if(utility >= minUtility){
				huiCount++;
				StringBuilder buffer = new StringBuilder();
//				buffer.append(lineSplited[0]);
				for (int i = 0; i < itemset.length; i++) {
					buffer.append(itemset[i]);
					if (i != itemset.length -1) {
						buffer.append(' ');
					}
				}
//				buffer.append(':');
//				buffer.append(tidsetStr.length);
				buffer.append(" #UTIL: ");
				buffer.append(utility);

				writer.write(buffer.toString());
				
				if(line != null){
					writer.newLine();
				}
			}
			MemoryLogger.getInstance().checkMemory();			
			tidcount++;
			if(tidcount == maximumNumberOfTransactions) {
				break;
			}
			
		}
		reader.close();
		writer.close();
		
		totaltime = System.currentTimeMillis() - startTimestamp;
		
		maxMemory = MemoryLogger.getInstance().getMaxMemory();	
	}

	
	/**
	 * Set the number of transactions of a database to be used by CHUD
	 * @param maximumNumberOfTransactions the number of transaction (a positive integer)
	 */
	public void setMaxNumberOfTransactions(int maximumNumberOfTransactions) {
		this.maximumNumberOfTransactions = maximumNumberOfTransactions;
	}

}


