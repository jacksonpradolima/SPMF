package ca.pfv.spmf.algorithms.episodes.emma;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.pfv.spmf.tools.MemoryLogger;

/*
 * This file is part of the SPMF DATA MINING SOFTWARE *
 * (http://www.philippe-fournier-viger.com/spmf).
 *
 * SPMF is free software: you can redistribute it and/or modify it under the *
 * terms of the GNU General Public License as published by the Free Software *
 * Foundation, either version 3 of the License, or (at your option) any later *
 * version. SPMF is distributed in the hope that it will be useful, but WITHOUT
 * ANY * WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright Peng Yang, Philippe Fournier-Viger, 2019
 */

/**
 * This is a implementation of the EMMA algorithm. EMMA was proposed by Huang et
 * al. 2008 in this paper:
 * 
 * Kuo-Yu Huang,Chia-Hui Chang (2008):Efficient mining of frequent episodes from
 * complex sequences. Inf. Syst.33(1):96-114
 * 
 * @author Peng yang
 */

public class AlgoEMMA {
	/** start time of the latest execution */
	private long startTimestamp;

	/** end time of the latest execution */
	private long endTimestamp;

	/** the number of candidates */
	private int candidateCount = 0;

	/**
	 * whether the timestamps need self increment as step of 1 for each transaction
	 */
	private boolean selfIncrement;

	/**
	 * The patterns that are found (if the user want to keep them into memory)
	 */
	private FrequentEpisodes freEpisodes = null;

	/**
	 * a sequence database to store all events int[] : 0-> item, 1-> tid
	 */
	private List<int[]> indexDB;

	/** the frequent itemsets */
	private List<Itemset> frequentItemsets = null;

	/** the Encoding table */
	private EncodingTable encodingTable = null;

	/** the minimum support threshold */
	private int minSupport;

	/** the maximum window threshold */
	private int maxWindow;

	/**
	 * Construct
	 */
	public AlgoEMMA() {
		// empty
	}

	/**
	 * Method to run the EMMA algorithm
	 * 
	 * @param input         a sequence
	 * @param output        the path of the output file to save the result or null
	 *                      if you want the result to be saved into memory
	 * @param minSupport    the minimum support threshold
	 * @param maxWindow     the maximum window size
	 * @param selfIncrement if true consecutive timestamps will be assigned to
	 *                      transactions. otherwise, timestamps from the input file
	 *                      will be used.
	 * @return the frequent episodes
	 * @throws IOException if error while reading or writing to file
	 */
	public FrequentEpisodes runAlgorithm(String input, String output, int minSupport, int maxWindow,
			boolean selfIncrement) throws IOException {
		// reset maximum
		MemoryLogger.getInstance().reset();

		this.minSupport = minSupport;
		this.maxWindow = maxWindow;
		this.selfIncrement = selfIncrement;

		startTimestamp = System.currentTimeMillis();

		this.indexDB = new ArrayList<>();

		// init the freItemset to contain the frequent itemset
		frequentItemsets = new ArrayList<>();

		// scan the file to the menory (sequence) and determine the frequent 1-items in
		// the level1
		Set<Integer> frequentItemsName = scanDatabaseToDetermineFrequentItems(input);

		int frequentItemsCount = frequentItemsName.size();

		// transfrom the TDB into the indexDB and maintain the locations of all frequent
		// 1-items in the index database
		scanDatabaseAgainToDetermineIndexDB(input, frequentItemsName);

		frequentItemsName = null;

		// obatin all frequent itemsets without candidates
		for (int i = 0; i < frequentItemsCount; i++) {

			fimajoin(frequentItemsets.get(i), 1);
		}

		// Encode the database construction

		this.encodingTable = new EncodingTable();
		freEpisodes = new FrequentEpisodes();

		for (Itemset itemset : frequentItemsets) {
			List<int[]> events = new ArrayList<>();
			events.add(itemset.getName());
			Episode episode = new Episode(events, itemset.getSupport());
			freEpisodes.addFrequentEpisode(episode, 1);
			candidateCount++;

			List<int[]> boundlist = new ArrayList<>();
			for (int location : itemset.getLocationList()) {
				int[] bound = new int[] { indexDB.get(location)[1], indexDB.get(location)[1] };
				boundlist.add(bound);
			}
			encodingTable.addEpisodeAndBoundlist(episode, boundlist);

		}
		this.indexDB = null;
		this.frequentItemsets = null;

		for (int i = 0; i < encodingTable.getTableLength(); i++) {
			// only do s-Extension
			serialJoins(encodingTable.getEpisodebyID(i), encodingTable.getBoundlistByID(i), 1);
		}

		this.encodingTable = null;

		// record end time
		endTimestamp = System.currentTimeMillis();
		// check the memory usage
		MemoryLogger.getInstance().checkMemory();

		if (output != null) {
			this.freEpisodes.saveToFile(output);
		}

		return freEpisodes;
	}

	/**
	 * Serial join (only do S-extention) because we first find all frequent itemsets
	 * 
	 * @param alpha          an episode
	 * @param alphaBoundlist the bound list of the episode
	 * @param levelNum       the level num
	 */
	private void serialJoins(Episode alpha, List<int[]> alphaBoundlist, int levelNum) {
		for (int j = 0; j < encodingTable.getTableLength(); j++) {
			List<int[]> tempBoundlist = temporalJoin(alphaBoundlist, encodingTable.getBoundlistByID(j));
			if (tempBoundlist.size() >= minSupport) {
				Episode beta = alpha.sExtension(encodingTable.getEpisodeNameByID(j), tempBoundlist.size());
				this.freEpisodes.addFrequentEpisode(beta, levelNum + 1);

				serialJoins(beta, tempBoundlist, levelNum + 1);
			}
		}
	}

	/**
	 * Perform an S-extension (a temporal join)
	 * 
	 * @param alphaBoundlist the boundlist of an episode
	 * @param fjBoundlist    the boundlist of an item
	 * @return
	 */
	private List<int[]> temporalJoin(List<int[]> alphaBoundlist, List<int[]> fjBoundlist) {
		this.candidateCount++;

		List<int[]> tempBoundlist = new ArrayList<>();

		for (int i = 0, j = 0; i < alphaBoundlist.size() && j < fjBoundlist.size();) {

//            [ts_i,te_i] and te_j  -> [ts_i,te_j] where te_j - ts_i < maxWindow and te_j > te_i
			if (fjBoundlist.get(j)[1] <= alphaBoundlist.get(i)[1]) {
				// the te_j are small than current te_i
				j++;
			} else if (fjBoundlist.get(j)[1] - alphaBoundlist.get(i)[0] >= maxWindow) {
				// the te_j are large than current te_i, but te_j - ts_i >= maxWindow
				i++;
			} else {
				// the te_j are large than current ts_i and te_j -ts_i < maxWindow
				tempBoundlist.add(new int[] { alphaBoundlist.get(i)[0], fjBoundlist.get(j)[1] });
				// Each start point of alpha bound only can statisfy one within maxWindow
				// why not j++? because the j may combine with the next head of bound if they
				// statisfy the conditions
				i++;
			}
		}
		return tempBoundlist;
	}

	/**
	 * Do a fima join
	 * 
	 * @param itemset       an itemset
	 * @param itemsetLength the length of the itemset
	 */
	private void fimajoin(Itemset itemset, int itemsetLength) {
		Map<Integer, List<Integer>> mapCurrentItemsLocationList = new HashMap<>();
		List<Integer> lfi = generatePListAndObtainFrequentItems(itemset.getLocationList(), mapCurrentItemsLocationList);
		for (int lf_j : lfi) {
			int[] newFreItemset = new int[itemsetLength + 1];
			System.arraycopy(itemset.getName(), 0, newFreItemset, 0, itemsetLength);
			newFreItemset[itemsetLength] = lf_j;

			// save it to the freItemsets
			Itemset newItemset = new Itemset(newFreItemset, mapCurrentItemsLocationList.get(lf_j));
			frequentItemsets.add(newItemset);

			fimajoin(newItemset, itemsetLength + 1);
		}

	}

	/**
	 * Method to obtain the frequent itemsets
	 * 
	 * @param locationList
	 * @param mapCurrentItemsLocationList
	 * @return
	 */
	private List<Integer> generatePListAndObtainFrequentItems(List<Integer> locationList,
			Map<Integer, List<Integer>> mapCurrentItemsLocationList) {
		List<Integer> frequentItems = new ArrayList<>();

		Map<Integer, Integer> mapItemCount = new HashMap<>();

		for (int i = 0; i < locationList.size(); i++) {
			int index = locationList.get(i);
			int currentTid = indexDB.get(index)[1];

			// find following items that having same TID with currentTID
			index++;
			while (index < indexDB.size() && indexDB.get(index)[1] == currentTid) {
				int itemName = indexDB.get(index)[0];
				Integer support = mapItemCount.get(itemName);
				List<Integer> currentItemLocationList = mapCurrentItemsLocationList.get(itemName);
				if (support == null) {
					mapItemCount.put(itemName, 1);

					currentItemLocationList = new ArrayList<>();
					currentItemLocationList.add(index);
					mapCurrentItemsLocationList.put(itemName, currentItemLocationList);

				} else {
					mapItemCount.put(itemName, support + 1);

					currentItemLocationList.add(index);
					mapCurrentItemsLocationList.put(itemName, currentItemLocationList);
				}
				index++;
			}
		}

		for (Map.Entry<Integer, Integer> entry : mapItemCount.entrySet()) {
			if (entry.getValue() >= minSupport) {
				frequentItems.add(entry.getKey());
			} else {
				// if the item is not frequent ,then delete its locationlist in the map
				mapCurrentItemsLocationList.remove(entry.getKey());
			}
		}

		return frequentItems;
	}

	/**
	 * Method to get the horizontal database
	 * 
	 * @param input
	 * @param frequentItemsName
	 * @throws IOException
	 */
	private void scanDatabaseAgainToDetermineIndexDB(String input, Set<Integer> frequentItemsName) throws IOException {
		// read file
		@SuppressWarnings("resource")
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;

		Map<Integer, List<Integer>> mapItemLocationList = new HashMap<>();

		int index = 0;
		if (selfIncrement) {
			int currentTID = 0;
			while (((line = reader.readLine()) != null)) {

				currentTID++;
				// if the line is a comment, is empty or is a
				// kind of metadata
				if (line.isEmpty() || line.charAt(0) == '#' || line.charAt(0) == '%' || line.charAt(0) == '@') {
					continue;
				}

				String[] lineSplited = line.split(" ");

				// 鎸夌�? ascll 鐮佹帓搴�?
				Arrays.sort(lineSplited);

				for (String itemString : lineSplited) {
					Integer itemName = Integer.parseInt(itemString);

					if (!frequentItemsName.contains(itemName)) {
						// if the item_name is not frequent item, skip it
						continue;
					}

					List<Integer> locationList = mapItemLocationList.get(itemName);
					if (locationList == null) {
						locationList = new ArrayList<>();
						locationList.add(index);
						mapItemLocationList.put(itemName, locationList);

						indexDB.add(new int[] { itemName, currentTID });
						index++;

					} else if (locationList.get(locationList.size() - 1) != index) {
						// maybe exist the same item in the one transaction
						locationList.add(index);
						mapItemLocationList.put(itemName, locationList);

						indexDB.add(new int[] { itemName, currentTID });
						index++;
					}
				}
			}
		} else {
			// the timestamp exist in file
			int currentTID = 1;

			while (((line = reader.readLine()) != null)) {
				if (line.isEmpty() || line.charAt(0) == '#' || line.charAt(0) == '%' || line.charAt(0) == '@') {
					continue;
				}

				String[] lineSplited = line.split("\\|");

				String[] lineItems = lineSplited[0].split(" ");
				currentTID = Integer.parseInt(lineSplited[1]);

				for (String itemString : lineItems) {
					Integer itemName = Integer.parseInt(itemString);

					if (!frequentItemsName.contains(itemName)) {
						// if the item_name is not frequent item, skip it
						continue;
					}

					List<Integer> locationList = mapItemLocationList.get(itemName);
					if (locationList == null) {
						locationList = new ArrayList<>();
						locationList.add(index);
						mapItemLocationList.put(itemName, locationList);

						indexDB.add(new int[] { itemName, currentTID });
						index++;

					} else if (locationList.get(locationList.size() - 1) != index) {
						// maybe exist the same item in the one transaction
						locationList.add(index);
						mapItemLocationList.put(itemName, locationList);

						indexDB.add(new int[] { itemName, currentTID });
						index++;
					}
				}
			}
		}

		// to add the locationList to corresponding frequent item
		for (int i = 0; i < frequentItemsets.size(); i++) {
			int itemName = frequentItemsets.get(i).getName()[0];
			frequentItemsets.get(i).setLocationList(mapItemLocationList.get(itemName));
		}
	}

	/**
	 * Scan the database to find the frequent items
	 * 
	 * @param input the file path of a database
	 * @return a Set of frequent items
	 * @throws IOException if error occurs while reading file.
	 */
	private Set<Integer> scanDatabaseToDetermineFrequentItems(String input) throws IOException {
		// read file
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;

		// key: item, value: support
		Map<Integer, Integer> mapItemCount = new HashMap<>();

		if (selfIncrement) {
//            int current_TID = 0;
			while (((line = reader.readLine()) != null)) {

//                current_TID++;
				// if the line is a comment, is empty or is a
				// kind of metadata
				if (line.isEmpty() || line.charAt(0) == '#' || line.charAt(0) == '%' || line.charAt(0) == '@') {
					continue;
				}

				String[] lineSplited = line.split(" ");

				for (String itemString : lineSplited) {
					Integer itemName = Integer.parseInt(itemString);
					Integer itemSupport = mapItemCount.get(itemName);
					if (itemSupport == null) {
						mapItemCount.put(itemName, 1);
					} else {
						mapItemCount.put(itemName, itemSupport + 1);
					}
				}
			}
		} else {
			//// the timestamp exist in file
//            int current_TID = 1;

			while (((line = reader.readLine()) != null)) {
				if (line.isEmpty() || line.charAt(0) == '#' || line.charAt(0) == '%' || line.charAt(0) == '@') {
					continue;
				}

				String[] lineSplited = line.split("\\|");

				String[] lineItems = lineSplited[0].split(" ");
//                current_TID = Integer.parseInt(lineSplited[1]);

				for (String itemString : lineItems) {
					Integer itemName = Integer.parseInt(itemString);
					Integer itemSupport = mapItemCount.get(itemName);
					if (itemSupport == null) {
						mapItemCount.put(itemName, 1);
					} else {
						mapItemCount.put(itemName, itemSupport + 1);
					}
				}
			}
		}

		// record the frequent items' name , to filter non frequent items later.
		Set<Integer> frequentItemsName = new HashSet<>();

		// We obatin all frequent items;
		for (Map.Entry<Integer, Integer> entry : mapItemCount.entrySet()) {
			if (entry.getValue() >= minSupport) {
				Itemset item = new Itemset(new int[] { entry.getKey() });
				frequentItemsets.add(item);

				frequentItemsName.add(entry.getKey());
			}
		}

		reader.close();

		return frequentItemsName;
	}

	/**
	 * Print statistics about the algorithm execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  EMMA(head episode) - STATS =============");
		System.out.println(" Candidates count : " + candidateCount);
		System.out.println(" The algorithm stopped at size : " + freEpisodes.getTotalLevelNum());
		System.out.println(" Frequent itemsets count : " + this.freEpisodes.getFrequentEpisodesCount());
		System.out.println(" Maximum memory usage : " + MemoryLogger.getInstance().getMaxMemory() + " mb");
		System.out.println(" Total time ~ : " + (endTimestamp - startTimestamp) + " ms");
		System.out.println("===================================================");
	}
}
