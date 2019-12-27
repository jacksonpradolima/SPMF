package ca.pfv.spmf.algorithms.frequentpatterns.chud;

import java.io.File;
import java.io.IOException;

/* This file is copyright (c) 2018 Philippe Fournier-Viger
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
 * This class runs the CHUD algorithm. It wraps the code for converting an horizontal database
 * to a vertical database and the code for performing Phase 1 and Phase 2 of the CHUD algorithm. <br/><br/>
 * 
 * Wu, C.-W., Fournier-Viger, P., Yu., P. S., Tseng, V. S. (2011). Efficient Mining of a 
 * Concise and Lossless Representation of High Utility Itemsets. Proceedings of the 11th 
 * IEEE Intern. Conference on Data Mining (ICDM 2011). IEEE CS Press, pp.824-833.
 * 
 * @author Philippe Fournier-Viger
 * @see AlgoCHUD
 */

public class AlgoCHUD {
	
	/** number of transactions of a database to be used by CHUD */
	private int maximumNumberOfTransactions = Integer.MAX_VALUE;
	
	/** Total execution time **/
	private double totalTime = 0;
	
	/** Total pattern count **/
	private double patternCount = 0;
	
	/** Total pattern count **/
	private double totalMemory = 0;
	
	/** Debug mode : if true some details will be shown in the console when running CHUD**/
	private boolean DEBUG = false;
	
	/**
	 * Run the algorithm
	 * @param dataset
	 * @param output
	 * @param output2
	 * @param minUtility
	 * @throws IOException
	 */

	public void runAlgorithm(String dataset, String output, int minUtility) throws IOException {
		String vertical = dataset + "_vertical.txt";
		String vertical2 = dataset + "_vertical2.txt";
		String vertical3 = dataset + "_vertical3.txt";
		
		// ======== IF NECESSARY, CONVERT THE DATABASE TO A VERTICAL DATABASE
		File file = new File(vertical);	
		if(file.exists() == false) {
			AlgoConvertToVerticalDatabase converter = new AlgoConvertToVerticalDatabase();
			converter.setMaxNumberOfTransactions(maximumNumberOfTransactions);
			converter.run(dataset, vertical, vertical2, vertical3);
			if(DEBUG){
				System.out.println("FINISHED CONVERTING DATABASE TO VERTICAL FORMAT");
				System.out.println("Time conversion: " + converter.totaltime/1000 + "s   (" +  converter.totaltime + " ms)");
			}
		}
		
		// ======================  APPLY PHASE 1 ==========================
		// DELETE files from previous executions
		String outputPhase1 = output + "_phase1.txt";
		File out1 = new File(outputPhase1);
		out1.delete();
		
		long startTime = System.currentTimeMillis();
		//	PHASE 1
		if(DEBUG){
			System.out.println("PHASE 1 of CHUD");
		}
		AlgoCHUD_Phase1 phase1 = new AlgoCHUD_Phase1();
		phase1.runAlgorithm(minUtility, vertical, vertical2, vertical3, outputPhase1);
		if(DEBUG){
			System.out.println("Number of transactions : " + maximumNumberOfTransactions);
			System.out.println("Time phase1: " + phase1.totaltime/1000 + "s   (" +  phase1.totaltime + " ms)");
			System.out.println("Closed candidates : " + phase1.closedCount); 
			System.out.println("Max memory : " + phase1.maxMemory);
			System.out.println("-------------------------");
		}

		// ======================  APPLY PHASE 2 ==========================
		if(DEBUG){
			System.out.println("PHASE 2 of CHUD");
		}
		// DELETE files from previous executions
		for(int i=1; i<100; i++){
			File out2 = new File("L" + i + ".txt");
			if(out2.exists() == false){
				break;
			}
			out2.delete();
		}
		
//		// Apply phase 2 of the Two-Phase algorithm
		AlgoCHUD_Phase2 phase2 = new AlgoCHUD_Phase2();
		phase2.setMaxNumberOfTransactions(maximumNumberOfTransactions);
		phase2.runAlgorithm(dataset, outputPhase1, output, minUtility, true);

		if(DEBUG){
			System.out.println("Time phase2: " + phase2.totaltime /1000 + "s   (" +  phase2.totaltime + " ms)");
			System.out.println("Closed HUI: " + phase2.huiCount); 
			System.out.println("Max memory : " + phase2.maxMemory);
			System.out.println("-------------------------");
			System.out.println("=========== CHUD RESULTS========");
		}
		
		// Save the results
		totalMemory = (phase1.maxMemory > phase2.maxMemory)? phase1.maxMemory : phase2.maxMemory;
		totalTime = (System.currentTimeMillis() - startTime);
		patternCount = phase2.huiCount;
		
		// Delete all the temporary files for the vertical database
		file = new File(vertical);
		file.delete();
		file = new File(vertical2);
		file.delete();
		file = new File(vertical3);
		file.delete();
		file = new File(outputPhase1);
		file.delete();
	}

	/**
	 * Set the number of transactions of a database to be used by CHUD
	 * @param maximumNumberOfTransactions the number of transaction (a positive integer)
	 */
	public void setMaxNumberOfTransactions(int maximumNumberOfTransactions) {
		this.maximumNumberOfTransactions = maximumNumberOfTransactions;
		
	}

	/**
	 * Print statistics about the last execution of the algorithm
	 */
	public void printStats() {
		System.out.println("=============  CHUD v.2.26 - STATS =============");
		
		System.out.println("Total execution time : " + totalTime);
		System.out.println("Max memory usage: " + totalMemory + " MB");
		System.out.println("Closed high utility itemset count: " + patternCount );

		System.out.println("===================================================");
	}
}
