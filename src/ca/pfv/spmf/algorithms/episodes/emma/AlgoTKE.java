package ca.pfv.spmf.algorithms.episodes.emma;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
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
 * This is a implementation of the TKE algorithm for top-k frequent episode
 * mining. The TKE algorithm was published in this paper: <br/>
 * <br/>
 * <br/>
 * 
 * Fournier-Viger, P., Wang, Y., Yang, P., Lin, J. C.-W., Yun, U. (2020). TKE:
 * Mining Top-K Frequent Episodes. Proc. 33rd Intern. Conf. on Industrial,
 * Engineering and Other Applications of Applied Intelligent Systems (IEA AIE
 * 2020), Springer LNAI, 12 pages
 * 
 * @author Peng yang, Philippe Fournier-Viger, Yanjun Yang et al.
 */

public class AlgoTKE {
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

	/** the top k itemsets found until now  (FIMA SEARCH)*/
	PriorityQueue<Itemset> kItemsets;

	/** the FIMA candidates for expansion (FIMA SEARCH)*/
	PriorityQueue<Itemset> candidatesFIMA;
	
	/** the top k patterns found until now */
	PriorityQueue<Episode> kEpisodes;

	/** the candidates for expansion (SERIAL SEARCH)*/
	PriorityQueue<EpisodeAndBoundList> candidates;


	/**
	 * a sequence database to store all events int[] : 0-> item, 1-> tid
	 */
	private List<int[]> indexDB;

	/** the frequent itemsets */
	private List<Itemset> frequentItemsets = null;

	/** the Encoding table */
	// ==== REDEFINED FOR TKE ===============f
	private List<EpisodeAndBoundList> encodingTable = null;
	// ==== END OF REDEFINED FOR TKE ===============

	/** the minimum support threshold */
	private int minSupport;

	/** the maximum window threshold */
	private int maxWindow;

	/** the k parameter */
	private int k;

	/** STRATEGY 1 1-ITEM INCREASE */
	private boolean ONE_ITEM_INCREASE = true;

	/** STRATEGY 2 FIMA INCREASE */
	private boolean FIMA_INCREASE = true;

	/** STRATEGY 3 DYNAMIC SEARCH_FIMA **/
	private boolean DYNAMIC_SEARCH_FIMA = true;
	
	/** STRATEGY 3 DYNAMIC SEARCH_SERIAL **/
	private boolean DYNAMIC_SEARCH_SERIAL = true;

	/** DEBUG MODE */
	private boolean DEBUG_MODE = false;

	// ================== new for TKE
	PriorityQueue<Integer> itemSupportQueue = null;

	// ================== end new for TKE
	
//	/** the minimum time duration (an optional parameter)*/
//	private int minTime = 0;

//	/** If true, show the intervals
//	private boolean showPatternInterval;
	/**
	 * Construct
	 */
	public AlgoTKE() {
		// empty
	}

	/**
	 * Method to run the EMMA algorithm
	 * 
	 * @param input         a sequence
	 * @param output        the path of the output file to save the result or null
	 *                      if you want the result to be saved into memory
	 * @param k             the k parameter
	 * @param maxWindow     the maximum window size
	 * @param selfIncrement if true consecutive timestamps will be assigned to
	 *                      transactions. otherwise, timestamps from the input file
	 *                      will be used.
	 * @return the frequent episodes
	 * @throws IOException if error while reading or writing to file
	 */
	public PriorityQueue<Episode> runAlgorithm(String input, String output, int k, int maxWindow, boolean selfIncrement)
			throws IOException {

		// reset maximum
		MemoryLogger.getInstance().reset();

		this.minSupport = 1;
		this.k = k;
		this.maxWindow = maxWindow;
		this.selfIncrement = selfIncrement;

		/** the top k patterns found until now */
		this.kEpisodes = new PriorityQueue<Episode>();

		// ================== new for TKE
		if (FIMA_INCREASE || ONE_ITEM_INCREASE) {
			itemSupportQueue = new PriorityQueue<Integer>();
		}
		// ================== end new for TKE

		/** the candidates for expansion */
		this.candidates = new PriorityQueue<EpisodeAndBoundList>(Comparator.reverseOrder());
		
		this.kItemsets = new PriorityQueue<Itemset>();
		
		if(DYNAMIC_SEARCH_FIMA) {
			this.candidatesFIMA = new PriorityQueue<Itemset>(Comparator.reverseOrder());
		}
		frequentItemsets = new ArrayList<>();

		startTimestamp = System.currentTimeMillis();

		this.indexDB = new ArrayList<>();


		// scan the file to the menory (sequence) and determine the frequent 1-items in
		// the level1
		Set<Integer> frequentItemsName = scanDatabaseToDetermineFrequentItems(input);

		int frequentItemsCount = frequentItemsName.size();

		// transfrom the TDB into the indexDB and maintain the locations of all frequent
		// 1-items in the index database
		scanDatabaseAgainToDetermineIndexDB(input, frequentItemsName);

		frequentItemsName = null;
		
		if (DEBUG_MODE) {
//			System.out.println("=== BEFORE FIMA JOIN ====");
//			System.out.println("itemSuportQueue.size() : " + itemSupportQueue.size());
//			System.out.println("minup = " + minSupport);
		}

		if (DYNAMIC_SEARCH_FIMA) {
			for (Itemset itemset : frequentItemsets) {
				// NEW FOR TKE
				if (itemset.getSupport() >= minSupport){
					// END NEW FOR TKE
					registerAsFIMACandidate(itemset);
					save(itemset);
				}
			}

			// Now we have finished checking all the patterns containing 1 item
			// in the left side and 1 in the right side,
			// the next step is to recursively expand patterns in the set
			// "candidates" to find more patterns.
			while (candidatesFIMA.size() > 0) {
				// We take the pattern that has the highest support first
				Itemset pattern = candidatesFIMA.poll();
				// if there is no more candidates with enough support, then we stop
				if (pattern.getSupport() < minSupport) {
					break;
				}
				// Otherwise, we try to expand the pattern
				fimajoinDynamic(pattern);
			}

			if (DEBUG_MODE) {
				System.out.println("=== AFTER FIMA JOIN ====");
				System.out.println("itemSuportQueue.size() : " + itemSupportQueue.size());
				System.out.println("minup = " + minSupport);
			}

			// Encode the database construction
			this.encodingTable = new ArrayList<EpisodeAndBoundList>();
			
			Collection<Itemset> sourceOfItemsets;
//			if(minTime == 0) {
			// If we dont use the min time duration constraint, 
			// we use the top k itemsets for creating the encoding table
			sourceOfItemsets = kItemsets;
//			}else {
//				// Otherwise we use all frequent itemsets.
//				sourceOfItemsets = frequentItemsets;
//			}

			for (Itemset itemset : sourceOfItemsets) {

				if (itemset.getSupport() >= minSupport) {
					List<int[]> events = new ArrayList<>();
					events.add(itemset.getName());
					Episode episode = new Episode(events, itemset.getSupport());
//					if(minTime ==0) {
					save(episode);
//					}
					candidateCount++;

					List<int[]> boundlist = new ArrayList<>();
					for (int location : itemset.getLocationList()) {
						int[] bound = new int[] { indexDB.get(location)[1], indexDB.get(location)[1] };
						boundlist.add(bound);
					}
					EpisodeAndBoundList episodeAndBound = new EpisodeAndBoundList(episode, boundlist);
					encodingTable.add(episodeAndBound);
				}
			}
			
			candidatesFIMA = null;
			kItemsets = null;
		} else {
			// Obtain all frequent itemsets without candidates
			for (int i = 0; i < frequentItemsCount; i++) {
				Itemset itemset = frequentItemsets.get(i);
				// NEW FOR TKE
				if (itemset.getSupport() >= minSupport) {
					// END NEW FOR TKE
					fimajoin(itemset);
				}
			}

			if (DEBUG_MODE) {
				System.out.println("=== AFTER FIMA JOIN ====");
//				System.out.println("itemSuportQueue.size() : " + itemSupportQueue.size());
				System.out.println("minup = " + minSupport);
			}

			// Encode the database construction
			this.encodingTable = new ArrayList<EpisodeAndBoundList>();

			for (Itemset itemset : frequentItemsets) {

				if (itemset.getSupport() >= minSupport) {
					List<int[]> events = new ArrayList<>();
					events.add(itemset.getName());
					Episode episode = new Episode(events, itemset.getSupport());
//					if(minTime ==0) {
					save(episode);
//					}
					candidateCount++;

					List<int[]> boundlist = new ArrayList<>();
					for (int location : itemset.getLocationList()) {
						int[] bound = new int[] { indexDB.get(location)[1], indexDB.get(location)[1] };
						boundlist.add(bound);
					}
					EpisodeAndBoundList episodeAndBound = new EpisodeAndBoundList(episode, boundlist);
					encodingTable.add(episodeAndBound);
				}
			}
		}
		this.indexDB = null;
		this.frequentItemsets = null;

		if (DYNAMIC_SEARCH_SERIAL) {
			for (int i = 0; i < encodingTable.size(); i++) {
				// only do s-Extension
				serialJoins(encodingTable.get(i));
			}

			// **********************************************************************************************
			// Now we have finished checking all the patterns containing 1 item
			// in the left side and 1 in the right side,
			// the next step is to recursively expand patterns in the set
			// "candidates" to find more patterns.
			while (candidates.size() > 0) {
				// We take the pattern that has the highest support first
				EpisodeAndBoundList pattern = candidates.poll();
				// if there is no more candidates with enough support, then we stop
				if (pattern.episode.support < minSupport) {
					// candidates.remove(pattern);
					break;
				}
				// Otherwise, we try to expand the pattern
				serialJoins(pattern);
				// candidates.remove(pattern);
			}
			// ************************************************************************************************
		} else {
			for (int i = 0; i < encodingTable.size(); i++) {
				// only do s-Extension
				serialJoinsNonDynamicSearch(encodingTable.get(i));
			}
		}

		this.encodingTable = null;

		// record end time
		endTimestamp = System.currentTimeMillis();
		// check the memory usage
		MemoryLogger.getInstance().checkMemory();

		if (output != null) {
			writeResultTofile(output);
//			this.freEpisodes.saveToFile(output);
		}

		if (DEBUG_MODE) {
			System.out.println("=== AFTER END ====");
			System.out.println(" minsup: " + minSupport);
		}

		return kEpisodes;
	}

	/**
	 * Register a given pattern in the set of candidates for future expansions
	 * 
	 * @param pattern the given pattern
	 */
	private void registerAsCandidate(EpisodeAndBoundList pattern) {
		// add the pattern to candidates
		candidates.add(pattern);

		// check the memory usage
		MemoryLogger.getInstance().checkMemory();
	}

	/**
	 * Register a given pattern in the set of candidates for future expansions
	 * 
	 * @param pattern the given pattern
	 */
	private void registerAsFIMACandidate(Itemset pattern) {
		// add the pattern to candidates
		candidatesFIMA.add(pattern);
	}

	/**
	 * Save a pattern to the current set of top-k pattern.
	 * 
	 * @param pattern the pattern to be saved
	 * @param support the support of the pattern
	 */
	private void save(Episode pattern) {
		// We add the pattern to the set of top-k patterns
		kEpisodes.add(pattern);
		// if the size becomes larger than k
		if (kEpisodes.size() > k) {
			// if the support of the pattern that we haved added is higher than
			// the minimum support, we will need to take out at least one pattern
			if (pattern.support > this.minSupport) {
				// we recursively remove the pattern having the lowest support,
				// until only k patterns are left
				do {
					kEpisodes.poll();
				} while (kEpisodes.size() > k);
			}
			// we raise the minimum support to the lowest support in the
			// set of top-k patterns
			this.minSupport = kEpisodes.peek().support;
		}
	}
	
	/**
	 * Save a pattern to the current set of top-k pattern.
	 * 
	 * @param pattern the pattern to be saved
	 * @param support the support of the pattern
	 */
	private void save(Itemset pattern) {
		// We add the pattern to the set of top-k patterns
		kItemsets.add(pattern);
		// if the size becomes larger than k
		if (kItemsets.size() > k) {
			// if the support of the pattern that we haved added is higher than
			// the minimum support, we will need to take out at least one pattern
			if (pattern.getSupport() > this.minSupport) {
				// we recursively remove the pattern having the lowest support,
				// until only k patterns are left
				do {
					kItemsets.poll();
				} while (kItemsets.size() > k);
			}
			// we raise the minimum support to the lowest support in the
			// set of top-k patterns
			this.minSupport = kItemsets.peek().getSupport();
		}
	}

	/**
	 * Save a pattern to the current set of top-k pattern.
	 * 
	 * @param pattern the pattern to be saved
	 * @param support the support of the pattern
	 */
	private void saveToItemSupportQueue(Integer value) {
		// We add the pattern to the set of top-k patterns
		itemSupportQueue.add(value);
		// if the size becomes larger than k
		if (itemSupportQueue.size() > k) {
			// if the support of the pattern that we haved added is higher than
			// the minimum support, we will need to take out at least one pattern
			if (value > this.minSupport) {
				// we recursively remove the pattern having the lowest support,
				// until only k patterns are left
				do {
					itemSupportQueue.poll();
				} while (itemSupportQueue.size() > k);
			}
			// we raise the minimum support to the lowest support in the
			// set of top-k patterns
			this.minSupport = itemSupportQueue.peek();
		}
	}

	/**
	 * Serial join (only do S-extention) because we first find all frequent itemsets
	 * 
	 * @param alpha          an episode
	 * @param alphaBoundlist the bound list of the episode
	 * @param levelNum       the level num
	 */
	private void serialJoins(EpisodeAndBoundList alphaWithList) {
		Episode alpha = alphaWithList.episode;
		List<int[]> alphaBoundlist = alphaWithList.boundlist;

		for (int j = 0; j < encodingTable.size(); j++) {
			if(encodingTable.get(j).boundlist.size() < minSupport) {
				continue;
			}
			
			List<int[]> tempBoundlist = temporalJoin(alphaBoundlist, encodingTable.get(j).boundlist);
			if (tempBoundlist.size() >= minSupport) {
				Episode beta = alpha.sExtension(encodingTable.get(j).episode.events.get(0), tempBoundlist.size());
				save(beta);
				if(DEBUG_MODE) {
					System.out.println("=====");
					System.out.println(beta);
					for (int[] array : tempBoundlist) {
						System.out.println(Arrays.toString(array));
					}
					System.out.println("=====");
				}
				EpisodeAndBoundList episodeWithList = new EpisodeAndBoundList(beta, tempBoundlist);
				registerAsCandidate(episodeWithList);
//				serialJoins(episodeWithList);
			}
		}
	}

	private void serialJoinsNonDynamicSearch(EpisodeAndBoundList alphaWithList) {
		Episode alpha = alphaWithList.episode;
		List<int[]> alphaBoundlist = alphaWithList.boundlist;

		for (int j = 0; j < encodingTable.size(); j++) {
			if(encodingTable.get(j).boundlist.size() < minSupport) {
				continue;
			}
			
			
			List<int[]> tempBoundlist = temporalJoin(alphaBoundlist, encodingTable.get(j).boundlist);
			if (tempBoundlist.size() >= minSupport) {
				Episode beta = alpha.sExtension(encodingTable.get(j).episode.events.get(0), tempBoundlist.size());
				save(beta);
				EpisodeAndBoundList episodeWithList = new EpisodeAndBoundList(beta, tempBoundlist);
//				registerAsCandidate(episodeWithList);
				serialJoinsNonDynamicSearch(episodeWithList);
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
	private void fimajoin(Itemset itemset) {
		Map<Integer, List<Integer>> mapCurrentItemsLocationList = new HashMap<>();
		List<Integer> lfi = generatePListAndObtainFrequentItems(itemset.getLocationList(), mapCurrentItemsLocationList);
		for (int lf_j : lfi) {
			// ============= new TKE
			List<Integer> locationList = mapCurrentItemsLocationList.get(lf_j);
			int support = locationList.size();

			if (support >= minSupport) {
				// ============= end new TKE
				int itemsetLength = itemset.getName().length;
				int[] newFreItemset = new int[itemsetLength + 1];
				System.arraycopy(itemset.getName(), 0, newFreItemset, 0, itemsetLength);
				newFreItemset[itemsetLength] = lf_j;

				// save it to the freItemsets
				Itemset newItemset = new Itemset(newFreItemset, locationList);
				frequentItemsets.add(newItemset);

				fimajoin(newItemset);
			}
		}
	}
	
	/**
	 * Do a fima join
	 * 
	 * @param itemset       an itemset
	 * @param itemsetLength the length of the itemset
	 */
	private void fimajoinDynamic(Itemset itemset) {
		Map<Integer, List<Integer>> mapCurrentItemsLocationList = new HashMap<>();
		List<Integer> lfi = generatePListAndObtainFrequentItems(itemset.getLocationList(), mapCurrentItemsLocationList);
		for (int lf_j : lfi) {
			// ============= new TKE
			List<Integer> locationList = mapCurrentItemsLocationList.get(lf_j);
			int support = locationList.size();

			if (support >= minSupport) {
				// ============= end new TKE
				int itemsetLength = itemset.getName().length;
				int[] newFreItemset = new int[itemsetLength + 1];
				System.arraycopy(itemset.getName(), 0, newFreItemset, 0, itemsetLength);
				newFreItemset[itemsetLength] = lf_j;

				// save it to the freItemsets
				Itemset newItemset = new Itemset(newFreItemset, locationList);
//				frequentItemsets.add(newItemset);

//				fimajoin(newItemset);
				registerAsFIMACandidate(newItemset);
//				if(minTime == 0) {
				save(newItemset);
//				}
			}
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
				candidateCount++;

				frequentItems.add(entry.getKey());
				// ================== new for TKE
				if (DYNAMIC_SEARCH_FIMA == false && FIMA_INCREASE) {
					saveToItemSupportQueue(entry.getValue());
				}
				// ==== END NEW FOR TKE ======================
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
					Integer item = Integer.parseInt(itemString);

					if (!frequentItemsName.contains(item)) {
						// if the item_name is not frequent item, skip it
						continue;
					}

					List<Integer> locationList = mapItemLocationList.get(item);
					if (locationList == null) {
						locationList = new ArrayList<>();
						locationList.add(index);
						mapItemLocationList.put(item, locationList);

						indexDB.add(new int[] { item, currentTID });
						index++;

					} else if (locationList.get(locationList.size() - 1) != index) {
						// maybe exist the same item in the one transaction
						locationList.add(index);
						mapItemLocationList.put(item, locationList);

						indexDB.add(new int[] { item, currentTID });
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
				// ================== new for TKE
				if (ONE_ITEM_INCREASE) {
					saveToItemSupportQueue(entry.getValue());
				}
				// ================== end new for TKE

				Itemset item = new Itemset(new int[] { entry.getKey() });
				frequentItemsets.add(item);

				frequentItemsName.add(entry.getKey());
			}
		}

		reader.close();

		return frequentItemsName;
	}

	/**
	 * Write the patterns found to an output file.
	 * 
	 * @param path the path to the output file
	 * @throws IOException exception if an error while writing the file
	 */
	public void writeResultTofile(String path) throws IOException {
		// Prepare the file
		BufferedWriter writer = new BufferedWriter(new FileWriter(path));

		if (kEpisodes.size() > 0) {
			// sort the patterns in sorted order before printing them
			// because the Iterator from Java on a priority queue do not
			// show the patterns in priority order unfortunately (even though
			// they are sorted in the priority queue.
			Object[] patterns = kEpisodes.toArray();
			Arrays.sort(patterns);

			// for each pattern
			for (Object patternObj : patterns) {
				Episode pattern = (Episode) patternObj;

				// Write the pattern
				StringBuilder buffer = new StringBuilder();
				buffer.append(pattern.toString());
//				// write separator
//				buffer.append(" #SUP: ");
//				// write support
//				buffer.append(pattern.support);
				writer.write(buffer.toString());
				writer.newLine();
			}
		}
		// close the file
		writer.close();
	}

	/**
	 * Print statistics about the algorithm execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  TKE - (head episode) - STATS =============");
		System.out.println(" Candidates count : " + candidateCount);
		System.out.println(" Top-k episode count : " + kEpisodes.size());
		System.out.println(" Maximum memory usage : " + MemoryLogger.getInstance().getMaxMemory() + " mb");
		System.out.println(" Total time ~ : " + (endTimestamp - startTimestamp) + " ms");
		System.out.println("===================================================");
	}

	/**
	 * Set whether dynamic search should be used or not
	 * @param useDynamicSearch if true, dynamic search will be used. Otherwise not.
	 */
	public void setUseDynamicSearch(boolean useDynamicSearch) {
		DYNAMIC_SEARCH_FIMA = useDynamicSearch;
		DYNAMIC_SEARCH_SERIAL = useDynamicSearch;
	}

//	/**
//	 * Set whether to show or not the time intervals of patterns found in the output file
//	 * @param value true or false
//	 */
//	public void setShowTimeIntervals(boolean value) {
//		// TODO Auto-generated method stub
//		this.showPatternInterval = value;
//	}

//	/**
//	 * Set a minimum time duration
//	 * @param minTime
//	 */
//	public void setMinimumTimeDuration(int minTime) {
//		this.minTime = minTime;
//		
//		/** if the minimum duration is used, we need to deactivate the one item increase optimization */
//		if(minTime > 0) {
//			ONE_ITEM_INCREASE = false;
//		}
//	}
}
