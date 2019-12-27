package ca.pfv.spmf.algorithms.frequentpatterns.tku;

/* This file is copyright (c) Cheng-Wei Wu et al.
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
 * This class is the Phase 2 of the TKU algorithm.
 * 
 * The code of TKU was obtained from the UP-Miner project, 
 * which is distributed under the GPL license and slightly modified
 * by Philippe Fournier-Viger to integrate TKU into SPMF.
 *
 *(#)Phase2_1209.java
 *
 * @author 
 * @version 1.00 2009/12/9
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import ca.pfv.spmf.datastructures.redblacktree.RedBlackTree;

/**
 * This is an implementation of the Phase 2 of the TKU algorithm
 * 
 * @see AlgoTKU
 */
class AlgoPhase2OfTKU {

	/** the minimum utility threshold */
	private int minUtility;
	private int theCurrentK;

	/** Number of transactions */
	private int numberOfTransactions;

	/** #CIs checked by Phase II **/
	private String inputFilePath;
	
	/** The path to a file of sorted candidates */
	private String sortedCandidatePath;

	private String temporaryFilePathWHUIs = "HUI.txt";

	/** the output file path */
	private String outputTopKHUIsFilePath;

	/** delimiter used in some files  by TKU */
	private final String delimiter = ":";

	/** The number of top-k HUIs found */
	private int numTopKHUI;

	void runAlgorithm(int minUtil, int transactionCount,
			int currentK, String inputPath, String sortedCandidateFile,
			String outputFile) throws IOException {

		minUtility = minUtil;
		numberOfTransactions = transactionCount;
		theCurrentK = currentK;
		inputFilePath = inputPath;
		sortedCandidatePath = sortedCandidateFile;
		outputTopKHUIsFilePath = outputFile;

		FileWriter fw = new FileWriter(temporaryFilePathWHUIs);
		BufferedWriter bfw = new BufferedWriter(fw);

		ArrayList<Integer> HDB[] = new ArrayList[numberOfTransactions];
		ArrayList<Integer> BNF[] = new ArrayList[numberOfTransactions];

		// Initialization
		initialization(HDB, BNF, HDB.length);

		// Read Database into memory
		readDatabase(HDB, BNF, HDB.length, inputFilePath);

		// System.out.println("Check file:"+CUI);
		// System.out.println("Read DB Complete !!");

		// Read Candidate from disk
		readCandidateItemsets(HDB, BNF, HDB.length, sortedCandidatePath, bfw);


		// System.out.println("");
		// System.out.println("Border_Minutil:"+gMin_Util);

		// ******************************
		FileReader bf1 = new FileReader(temporaryFilePathWHUIs);
		BufferedReader bfr1 = new BufferedReader(bf1);

		FileWriter fw1 = new FileWriter(outputTopKHUIsFilePath);
		BufferedWriter bfw1 = new BufferedWriter(fw1);

		String record = "";
		setNumberOfTopKHUIs(0);
		while ((record = bfr1.readLine()) != null) {
			String temp[] = record.split(":");

			if (Integer.parseInt(temp[1]) >= minUtility) {
				/************ ADDED BY PHILIPPE ******************/
				// THIS IS TO MAKE SURE THAT THE OUTPUT IS IN SPMF FORMAT..
				// IT IS NOT EFFICIENT BUT SINCE THE CODE IS NOT VERY
				// EFFICIENT ANYWAY, IT WILL NOT MAKE MUCH OF A DIFFERENCE.
				record = record.replace(":", " #UTIL: ");
				/***********************************/
				bfw1.write(record);
				bfw1.newLine();
				setNumberOfTopKHUIs(getNumberOfTopKHUIs() + 1);
			}
		}

		bfw1.flush();
		fw1.close();
		bfw1.close();

		bf1.close();
		bfr1.close();

		// System.out.println("");
		// System.out.println(" [Phase2] Exection time : " + ExeTime + " sec");
		// System.out.println(" Number of top-k high utility patterns : "
		// + getNumTopKHUI());
		// System.out.println(" Final border minimum utility threshold : "
		// + gMin_Util);
		// System.out.println("Total_CI      :"+num_CI);
		// System.out.println("Check_CIs     :"+Check_CIs);

		fw.close();
		bfw.close();
		
		/****************************************************/
		/**  Delete the temporary files (added by Philippe) */
		File fileToDelete = new File(temporaryFilePathWHUIs);
		fileToDelete.delete();
		fileToDelete = new File(sortedCandidateFile);
		fileToDelete.delete();
		/****************************************************/

	}// End main()


	int readCandidateItemsets(ArrayList<Integer> HDB[], ArrayList<Integer> BNF[], int num_trans,
			String CIPath,  BufferedWriter Lbfw) throws IOException {

		RedBlackTree<StringPair> Heap = new RedBlackTree<StringPair>(true);

		FileReader bf = new FileReader(CIPath);
		BufferedReader bfr = new BufferedReader(bf);

		int num_HU = 0;

		String CIR = "";
		while ((CIR = bfr.readLine()) != null) {

			String CI[] = CIR.split(delimiter); // CIR[0] Candidate;
			// CIR[1] Estimate Utility;
			int Match_Count = 0;
			int EUtility = 0;

			String candidate[] = CI[0].split(" "); // candidate[0]:1
													// candidate[1]:3
													// candidate[2]:4
													// candidate[3]:5
			// Sort items in candidate

			// int candidate[] = sort_candidate(candidate1);

			// *********
			// System.out.println(CI[0]);

			if (Integer.parseInt(CI[1]) >= minUtility) {

				// For each Candidate CI[0], Scan DB
				for (int i = 0; i < num_trans; i++) {
					if (HDB[i].size() != 0) {
						// System.out.println(HDB[i]);

						Match_Count = 0;
						int PUtility = 0;

						for (int s = 0; s < candidate.length; s++) {
							if (HDB[i].contains(Integer.parseInt(candidate[s]))) {
								Match_Count++;

								int index = HDB[i].indexOf(Integer
										.parseInt(candidate[s]));
								ArrayList<Integer> B = BNF[i];

								int Ben = B.get(index);
								PUtility = PUtility + Ben;

								// System.out.println("Yes,utility="+Ben);
							} else {
								PUtility = 0;
								break;
							}

						}// end for-s

						// System.out.println("Match_Count:"+Match_Count);

						if (Match_Count == candidate.length) {
							EUtility += PUtility;

						}// End if

					}// End if(HDB[i].size()!=0)

				}// End for-i

				/*--*/// System.out.println("=====================HU:"+CI[0]+":"+EUtility);

				if (EUtility >= minUtility) {
					Lbfw.write(CI[0] + ":" + EUtility);
					Lbfw.newLine();
					// System.out.println("HU:"+CI[0]+":"+EUtility);

					updateHeap(Heap, CI[0], EUtility);

					num_HU++;
				}
			}// if(CIR[1] >= MSC)

		}// End while

		Lbfw.flush();

		// System.out.println("num_HU:"+num_HU);

		bf.close();
		bfr.close();

		return num_HU;

	}// End ReadCI

	static void readDatabase(ArrayList<Integer> HDB[], ArrayList<Integer> BNF[], int num_trans,
			String DBPath) throws IOException {
		FileReader bf = new FileReader(DBPath);
		BufferedReader bfr = new BufferedReader(bf);

		String record = "";
		int trans_count = 0;
		while ((record = bfr.readLine()) != null) {
			String data[] = record.split(":"); // data[0] Transaction;
												// data[1] TWU
												// data[2] Benefit

			String transaction[] = data[0].split(" ");
			String benefit[] = data[2].split(" ");

			for (int i = 0; i < transaction.length; i++) {
				HDB[trans_count].add(Integer.parseInt(transaction[i]));
				BNF[trans_count].add(Integer.parseInt(benefit[i]));

			}// End for-i
			trans_count++;

		}// End while

	}// End ReadDB

	void initialization(ArrayList HDB[], ArrayList BNF[], int num_trans) {
		for (int i = 0; i < num_trans; i++) {
			HDB[i] = new ArrayList<Integer>(0);
			BNF[i] = new ArrayList<Integer>(0);

		}// End for

	}// End Initialization

	void updateHeap(RedBlackTree<StringPair> NCH, String HUI, int Utility) {
		if (NCH.size() < theCurrentK) {
			NCH.add(new StringPair(HUI, Utility));
		} else if (NCH.size() >= theCurrentK) {
			if (Utility > minUtility) {
				NCH.add(new StringPair(HUI, Utility));
				NCH.popMinimum();

			}
		}// End if-else

		if ((NCH.minimum().y > minUtility) && (NCH.size() >= theCurrentK)) {
			minUtility = NCH.minimum().y;

			// System.out.println("raise ---->:"+gMin_Util);
		}

	}// End UpdateNodeCountHeap

	int getNumberOfTopKHUIs() {
		return numTopKHUI;
	}

	void setNumberOfTopKHUIs(int numTopKHUI) {
		this.numTopKHUI = numTopKHUI;
	}

}// End Phase2_1209