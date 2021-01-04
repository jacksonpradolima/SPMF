package ca.pfv.spmf.algorithms.frequentpatterns.DFIList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

/* This is an implementation of the DFI-List algorithm. 
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
*/

/**
 * This is an implementation of the DFI-List algorithm (to appear)
 * 
 * @author 2020
 */
public class AlgoDFIList {

	private static final String SUPCONSTANT = "#SUP: ";

	/** File for storing temporary results **/
	String storageAfterFCI = "SortFCI.txt";

	/** maximum item */
	int itemMax = -1;

	/** Line */
	int line = 0;

	/** Frequent itemset count */
	int frequentItemsetCount = 0;

	/** start time of the latest execution */
	long startTimestampSortFCI;

	/** end time of the latest execution */
	long endTimeSortFCI;

	/** start time of the latest execution */
	long startTimestampBuildCidList;

	/** end time of the latest execution */
	long endTimeBuildCidList;

	/** start time of the latest execution */
	long startTimestampDerive; //

	/** end time of the latest execution */
	long endTimeDerive; //

	/** current memory usage */
	double currentMemory = 0; //

	/** the maximum of memory usage */
	double maxMemory = 0; //

	/** max */
	int max = 0;

	/**
	 * Method to run the DFIGrowth algorithm.
	 * 
	 * @param input  the path to an input file containing Frequent Closed Itemsets.
	 * @param output the output file path for saving the result (if null, the result
	 *               will be returned by the method instead of being saved).
	 * @return the result if no output file path is provided.
	 * @throws IOException exception if error reading or writing files
	 */
	public void runAlgorithm(String input, String output) throws IOException {

		startTimestampSortFCI = System.currentTimeMillis();
		
		// Read the database to find the maximum item name
		findMax(input);
		itemMax += 1;
		
		// Read the frequent closed itemset
		String[] closedItemsets = readFCI(input);
		
		// Sort frequent closed itemsets
		sortFCI(closedItemsets);
		endTimeSortFCI = System.currentTimeMillis();
		
		// Extract the support counts of closed itemsets
		int[] s = supportCount(closedItemsets);
		closedItemsets = null;

		startTimestampBuildCidList = System.currentTimeMillis();
		// Build the CID lists
		List[] h = buildCIDList();
		endTimeBuildCidList = System.currentTimeMillis();

		// Delete temporary file
		Files.deleteIfExists(new File(storageAfterFCI).toPath());

		List<ArrayList<Integer>> newH = (ArrayList<ArrayList<Integer>>) h[1];
		h[1] = null;

		// Create the output file
		FileWriter fileWriter = new FileWriter(output);

		for (int i = 0; i < h[0].size(); i++) {
			String z = String.valueOf(h[0].get(i));

			// Check the memory usage
			checkMemoryUsage();

			fileWriter.write("" + z + " #SUP: " + s[newH.get(i).get(0)] + "\n");
		}

		// Derive the frequent itemsets
		String p = "";
		deriveFI(p, h[0], newH, s, fileWriter);
		fileWriter.close();
		endTimeDerive = System.currentTimeMillis();
	}

	/**
	 * Derive the frequent itemset
	 * @param p
	 * @param l
	 * @param h
	 * @param s
	 * @param fileWriter
	 * @throws IOException
	 */
	private void deriveFI(String p, List<Integer> l, List<ArrayList<Integer>> h, int[] s, FileWriter fileWriter)
			throws IOException { // modify to write FI and SUP into the file

		for (int i = 0; i < h.size(); i++) {
			checkMemoryUsage();
			String newP;
			if (p.equals("")) {
				newP = p + l.get(i);
			} else {
				newP = p + " " + l.get(i);
			}
			ArrayList<Integer> newL = new ArrayList<Integer>();
			ArrayList<ArrayList<Integer>> newH = new ArrayList<ArrayList<Integer>>();
			for (int j = i + 1; j < h.size(); j++) {
				String z = newP + " " + l.get(j);
				ArrayList<Integer> tempValue  = intersection(h.get(i), h.get(j));
				if (!tempValue.contains(-1)) {
					newH.add(tempValue);
					newL.add(l.get(j));
					fileWriter.write("" + z + " #SUP: " + s[tempValue.get(0)] + System.lineSeparator()); // new implement
					frequentItemsetCount++;
				}
				checkMemoryUsage();
			}
			h.remove(0);
			i--;
			l.remove(0);
			checkMemoryUsage();
			deriveFI(newP, newL, newH, s, fileWriter);
		}
	}

	/**
	 * Check the memory usage of this algorithm.
	 */
	void checkMemoryUsage() {
		currentMemory = ((double) ((double) (Runtime.getRuntime().totalMemory() / 1024) / 1024))
				- ((double) ((double) (Runtime.getRuntime().freeMemory() / 1024) / 1024));
		if (currentMemory > maxMemory) {
			maxMemory = currentMemory;
		}
	}

	/**
	 * Read the frequent closed itemset
	 * @param input input file path
	 * @return A list of strings
	 * @throws IOException if error reading the input file
	 */
	String[] readFCI(String input) throws IOException {
		String[] closedItemset = new String[line];

		FileReader fr = new FileReader(input);
		BufferedReader br = new BufferedReader(fr);

		String record = "";
		int i = 0;
		while ((record = br.readLine()) != null) {
			closedItemset[i] = record;
			i++;
		}
		br.close();
		fr.close();

		return closedItemset;
	}

	/**
	 * sor the frequent closed itemsets
	 * @param closedItemsets the closed itemsets
	 * @throws IOException if error writing a temporary file
	 */
	void sortFCI(String[] closedItemsets) throws IOException {
		Comparator<String> comparator = new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				String[] temp1 = o1.split(SUPCONSTANT);
				String[] temp2 = o2.split(SUPCONSTANT);

				int ntemp1 = Integer.parseInt(temp1[1]);
				int ntemp2 = Integer.parseInt(temp2[1]);

				if (ntemp1 < ntemp2) {
					return 1;
				} else if (ntemp1 > ntemp2) {
					return -1;
				} else {
					return 0;
				}
			}
		};
		Arrays.sort(closedItemsets, comparator);
		FileWriter fw = null;
		fw = new FileWriter(storageAfterFCI);

		for (int i = 0; i < closedItemsets.length; i++) {
			fw.write(closedItemsets[i] + System.lineSeparator());
		}
		fw.close();
	}

	/**
	 * Get the support counts
	 * @param closedItemsets the closed itemsets
	 * @return support counts of these closed itemsets
	 */
	int[] supportCount(String[] closedItemsets) {
		int[] s = new int[closedItemsets.length];
		for (int i = 0; i < closedItemsets.length; i++) {
			checkMemoryUsage();
			s[i] = Integer.parseInt(closedItemsets[i].split(SUPCONSTANT)[1]);
		}
		return s;
	}

	/**
	 * Build CID List
	 * @return a list of CID list
	 * @throws FileNotFoundException if error reading the temporary file
	 */
	List[] buildCIDList() throws FileNotFoundException {
		ArrayList<Integer> l = new ArrayList<Integer>();

		ArrayList<ArrayList<Integer>> cidList = new ArrayList<ArrayList<Integer>>();

		Scanner sc = new Scanner(new File(storageAfterFCI));
		int i = 0;
		while (sc.hasNextLine()) {
			String temp = sc.nextLine().split(SUPCONSTANT)[0];
			String[] splitTemp = temp.split(" ");
			for (int j = 0; j < splitTemp.length; j++) {
				checkMemoryUsage();
				int index = Integer.parseInt(splitTemp[j]);

				if (l.contains(index)) {
					cidList.get(l.indexOf(index)).add(i);
				} else {
					l.add(index);
					int size = cidList.size();
					cidList.add(new ArrayList<Integer>());
					cidList.get(size).add(i);
					frequentItemsetCount++;
				}
			}
			i++;
		}
		int[] specificSize = new int[cidList.size()];
		for (int j = 0; j < cidList.size(); j++) {
			specificSize[j] = cidList.get(j).size();
		}
		bubbleSort(specificSize, l, cidList);

		sc.close();

		return new List[] { l, cidList };
	}

	/**
	 * Implementatio of a bubble sort
	 * @param specific
	 * @param l
	 * @param cidList
	 */
	private void bubbleSort(int[] specific, ArrayList<Integer> l, ArrayList<ArrayList<Integer>> cidList) {
		for (int i = 0; i < specific.length - 1; i++) {
			for (int j = i + 1; j < specific.length; j++) {
				int temp;
				if (specific[i] > specific[j]) {
					Integer tempLValue;
					temp = specific[j];
					ArrayList<Integer> tempCidListValue = (ArrayList<Integer>) cidList.get(j).clone();
					tempLValue = l.get(j);

					specific[j] = specific[i];
					cidList.get(j).clear();
					cidList.get(j).addAll(cidList.get(i));
					l.set(j, l.get(i));

					specific[i] = temp;
					cidList.get(i).clear();
					cidList.get(i).addAll((ArrayList<Integer>) tempCidListValue.clone());
					l.set(i, tempLValue);
					tempCidListValue.clear();
				}
			}
		}

	}

	/**
	 * Calculate the intersetion of two list
	 * @param cidseq1 the first list
	 * @param cidseq2 the second list
	 * @return the intersection
	 */
	static ArrayList<Integer> intersection(ArrayList<Integer> cidseq1, ArrayList<Integer> cidseq2) {
		int countSizeofCidSeq1 = 0;
		int countSizeofCidSeq2 = 0;

		ArrayList<Integer> intersectionResults = new ArrayList<Integer>();
		int count = 0;
		while (countSizeofCidSeq1 < cidseq1.size() && countSizeofCidSeq2 < cidseq2.size()) {
			if (cidseq1.get(countSizeofCidSeq1) > cidseq2.get(countSizeofCidSeq2)) {
				countSizeofCidSeq2++;
			} else if (cidseq1.get(countSizeofCidSeq1) < cidseq2.get(countSizeofCidSeq2)) {
				countSizeofCidSeq1++;
			} else {
				count++;
				intersectionResults.add(cidseq1.get(countSizeofCidSeq1));
				countSizeofCidSeq1++;
				countSizeofCidSeq2++;
			}
		}
		if (count != 0) {
			return intersectionResults;
		} else {
			intersectionResults.add(-1);
			return intersectionResults;
		}
	}

	/**
	 * Read file to find the maximum item name
	 * 
	 * @param input the input file path
	 * @throws IOException if error while reading the input file
	 */
	void findMax(String input) throws IOException {
		FileReader fr = new FileReader(input);
		BufferedReader br = new BufferedReader(fr);

		String record = "";
		// For each line
		while ((record = br.readLine()) != null) {
			String[] temp = record.split(SUPCONSTANT)[0].split(" ");
			for (int i = 0; i < temp.length; i++) {
				checkMemoryUsage();
				int t = Integer.parseInt(temp[i]);
				// if the item name is larger, then remember it
				if (t > itemMax) {
					itemMax = t;
				}
			}
			line++;
		}
		br.close();
		fr.close();
	}

	/**
	 * Print statistics about the last execution of this algorithm
	 */
	public void printStats() {

		long sortFCI = endTimeSortFCI - startTimestampSortFCI;
		long buildCidList = endTimeBuildCidList - startTimestampBuildCidList;
		long deriving = endTimeDerive - endTimeBuildCidList;
		long totaltime = endTimeDerive - startTimestampSortFCI;

		System.out.println("===================  DFI - STATS ==================");
		System.out.println(" Frequent itemsets count : " + frequentItemsetCount);
		System.out.println(" Max memory usage: " + maxMemory + " MB");
		System.out.println(" SortFCI time ~ " + sortFCI + " ms");
		System.out.println(" BuildCidList time ~ " + buildCidList + " ms");
		System.out.println(" Deriving time ~ " + deriving + " ms");
		System.out.println(" Total time ~ " + totaltime + " ms");
		System.out.println("===================================================");
	}
}
