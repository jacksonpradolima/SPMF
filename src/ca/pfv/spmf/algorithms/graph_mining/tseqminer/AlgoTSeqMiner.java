package ca.pfv.spmf.algorithms.graph_mining.tseqminer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.pfv.spmf.tools.MemoryLogger;

/* This file is copyright (c) 2018 by Chao Cheng
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
 * The SeqMiner algorithm as described in this paper: <br/>
 * <br/>
 * Fournier-Viger, P., Cheng, C., Cheng, Z., Lin, J. C.-W., Selmaoui-Folcher, N.
 * (2019). Mining Significant Trend Sequences in Dynamic Attributed Graphs.
 * Knowledge-Based Systems (KBS) <br/>
 * <br/>
 * 
 * @author Chao Cheng 2019
 */
public class AlgoTSeqMiner {

	/**
	 * item dynamic attributed graph acquired from preprocessing of original dynamic
	 * attributed graph
	 */
	private Map<Integer, ItemAttributedGraph> timestampMapItemAG;
	/** total number of vertex in this timestampMapItemAG */
	private int totalNumVertex;

	private Map<Integer, String> eventTypeMapName;

	/** associate itemset with its supporting points */
	private Map<Itemset, STPSet> itemsetMapSTPSet;
	/** associate itemset with its support in whole space */
	private Map<Itemset, Double> itemsetMapSup;
	/** total number of frequent itemset */
	private int totalItemsetNum;

//    private static Itemset startItemset;

	/** partitioning itemsets according to size */
	private static Map<Integer, Set<Itemset>> levelMapItemsets;
	/** store direct superset for each itemset */
	private static Map<Itemset, List<Itemset>> itemsetMapSuperset;
	/** minimal support among all supersets */
	private static Map<Itemset, Double> itemsetMapMinSup;

	/** path of file storing mined patterns */
//    private static String PATTERN_PATH = ParametersSetting.PATTERN_PATH;
	private static BufferedWriter bw;

	/** user specified minimal significance */
//    private static double SIGNIFICANCE = ParametersSetting.MIN_SIGNIFICANCE;
	/**
	 * user specified minimal support of tail itemset allowing to be extended
	 * further
	 */
//    private static int MIN_TAIL_SUP = ParametersSetting.MIN_TAIL_SUP;

	/**
	 * indicate whether overlapping of neighboring space is allowed, generally
	 * speaking, allowing overlapping will overestimate significance
	 */
//    private static boolean ALLOW_OVERLAPPING = ParametersSetting.ALLOW_OVERLAPPING;

	/** indicate whether to interpret significant sequence */
	private boolean INTERPRET_RESULT = true;
//    private static boolean EXHIBIT_SUPPORTING_POINTS = ParametersSetting.EXHIBIT_SUPPORTING_POINTS;

	/** mapping from vertex index to real entity name */
	private Map<Integer, String> vertexMapName;
//    private static String VERTEX_MAP_NAME_PATH = ParametersSetting.VERTEX_MAP_NAME_PATH;

	/**
	 * parameters for testing efficiency of pruning and different traversal methods
	 */
//    public static boolean ADOPT_PRUNING = ParametersSetting.ADOPTING_PRUNING;

	/** associate itemset with its supersets that are dominated by it */
	private static Map<Itemset, Set<Itemset>> itemsetMapDominantsSuperset;

	/**
	 * this flag specify traversal behavior case 1: complete DFS case 2: process
	 * outer and inner separately, outer DFS, inner BFS case 3: process outer and
	 * inner separately, outer DFS, inner DFS
	 */
//    private static int TRAVERSAL_FLAG = ParametersSetting.TRAVERSAL_FLAG;

	private long totalMiningTime = 0;

	private int patternCount = 0;

	private static NumberFormat nf = NumberFormat.getInstance();

	/*
	 * public static void main(String[] args) throws IOException { //get all
	 * frequent itemset and their support points readFrequentItemsetFromEclat();
	 * //get item dynamic attributed graph getItemDyAGFromPreprocessing();
	 * 
	 * // for (int vId : vertexMapName.keySet()) { // System.out.println(vId + " " +
	 * vertexMapName.get(vId)); // }
	 * 
	 * //compute getItemsetMapDominantsSuperset(); }
	 */

	/**
	 * Run the algorithm
	 * 
	 * @param inputDirectory          the input directory
	 * @param outputPath              the path of the output file
	 * @param discretizationThreshold the discretization threshold
	 * @param minInitSup              the minInitSup parameter
	 * @param minTailSup              the minTailSup parameter
	 * @param minSig                  the minSig parameter
	 * @param attributeCount          the number of attributes
	 * @throws IOException if an error reading or writing files
	 */
	public void runAlgorithm(String inputDirectory, String outputPath, float discretizationThreshold, float minInitSup,
			int minTailSup, float minSig, int attributeCount) throws IOException {

		// Save parameters
		ParametersSetting.INCRE_THRESHOLD = discretizationThreshold;
		ParametersSetting.MINSUP = minInitSup;
		ParametersSetting.MIN_TAIL_SUP = minTailSup;
		ParametersSetting.MIN_SIGNIFICANCE = minSig;
		ParametersSetting.TOTAL_NUM_ATTR = attributeCount;

		ParametersSetting.EDGE_FILE_PATH = inputDirectory + "graph.txt";
		ParametersSetting.ATTRI_MAPPING_PATH = inputDirectory + "attributes_mapping.txt";
//		System.out.println(ParametersSetting.ATTRI_MAPPING_PATH);
		ParametersSetting.ATTR_FILE_PATH = inputDirectory + "attributes.txt";
		ParametersSetting.VERTEX_MAP_NAME_PATH = inputDirectory + "vertices_mapping.txt";

		File resDir = new File(inputDirectory + "result");
		File preprocessDir = new File(inputDirectory + "preprocess");
		if (!resDir.exists())
			resDir.mkdir();
		if (!preprocessDir.exists())
			preprocessDir.mkdir();

		ParametersSetting.PATTERN_PATH = resDir + File.separator + outputPath;
//		System.out.println(ParametersSetting.PATTERN_PATH);
		ParametersSetting.TRANSACTION_PATH = inputDirectory + "preprocess" + File.separator + "transactions.txt";
		ParametersSetting.EVENTTYPE_MAPPING_PATH = inputDirectory + "preprocess" + File.separator
				+ "event_type_mapping.txt";
		ParametersSetting.FRE_ITEMSET_PATH = "frequent_itemset.txt";

//        System.out.println("\nstart to run sequence miner algorithm...\n");
		MemoryLogger.getInstance().reset();
		MemoryLogger.getInstance().checkMemory();
//        System.out.println("memory usage after preprocessing: " + MemoryLogger.getInstance().getMaxMemory());

		long t1 = System.currentTimeMillis();
		// get all frequent itemset and their support points

		readFrequentItemsetFromEclat();
		// get item dynamic attributed graph

		getItemDyAGFromPreprocessing();
		// compute

		getBasicMappings();
		// construct search structure for DFS
		getItemsetMapSuperMinAndMapDominants();

		long t2 = System.currentTimeMillis();
//        System.out.println("*************************** time for preparation: " + (t2 - t1) + "ms");
		ParametersSetting.PREPARE = t2 - t1;

		nf.setMaximumFractionDigits(1);

		// create a object for writing to file
		File patternPathFile = new File(ParametersSetting.PATTERN_PATH);
		if (!patternPathFile.exists())
			patternPathFile.createNewFile();
		System.out.println(ParametersSetting.PATTERN_PATH);
		bw = new BufferedWriter(new FileWriter(ParametersSetting.PATTERN_PATH));
		String head = "sequence     support of tail itemset     significance\n";
		bw.write(head);

		MemoryLogger.getInstance().checkMemory();
//        System.out.println("memory usage after all preparation: " + MemoryLogger.getInstance().getMaxMemory());

		// use flag to choose traversal methods
		switch (ParametersSetting.TRAVERSAL_FLAG) {
		case 0: {
			seqMinerDFS();
			break;
		}
		case 1: {
			seqMinerTwoPhase(true);
			break;
		}
		case 2: {
			seqMinerTwoPhase(false);
			break;
		}
		}

		bw.close();

		totalMiningTime = System.currentTimeMillis() - t2;

//        System.out.println("total " + patternCount + " patterns are found");
//        patternCount = 0;
	}

	/**
	 * This method apply DFS completely to mine sequential patterns. Main part here
	 * is to prepare initial objects for recursive method seqMinerDFSHelper.
	 * 
	 * @throws IOException
	 */
	private void seqMinerDFS() throws IOException {
		// this list record a sequence of itemset that is based to form sequential
		// pattern
		// the list can be reused by removing last itemset after returning from deeper
		// step
		// because we only append an itemset when going to deeper step
		List<Itemset> prefix = new LinkedList<>();
		List<STPSet> prefixSTPSet = new LinkedList<>();
		List<Double> prefixSigList = new LinkedList<>();
		// this part can be written in a more consistent way at cost of little loss of
		// efficiency
		// for each frequent itemset
//        System.out.println(itemsetMapSTPSet.size());
		for (Map.Entry<Itemset, STPSet> entry : itemsetMapSTPSet.entrySet()) {
			// find its supporting points
			Itemset itemset = entry.getKey();
			STPSet stpSet = entry.getValue();
			// compute neighboring space
			List<STPSet> neighboringPointsSet = acquireNeighboringPointsSet(stpSet);
			prefix.add(itemset);
			prefixSTPSet.add(itemsetMapSTPSet.get(itemset));
			prefixSigList.add(999.0);
			// for each itemset of size 1
			for (Itemset nextItemset : levelMapItemsets.get(1)) {
				// modify prefix and go deeper
				List<Double> significanceList = new LinkedList<>();
				significanceList.add(9999.0);
				seqMinerDFSHelper(prefix, prefixSTPSet, prefixSigList, nextItemset, neighboringPointsSet);
			}
			prefix.remove(prefix.size() - 1);
			prefixSTPSet.remove(prefixSTPSet.size() - 1);
			prefixSigList.remove(prefixSigList.size() - 1);
		}
	}

	/**
	 * This recursive method process one itemset at a time given prefix of itemset
	 * and projected space This function is called when we want to go deeper(next
	 * step). This case we need to append itemset to current prefix. This function
	 * is called also when we want to go broader. This case we do not modify prefix.
	 * 
	 * @param prefix       a sequence of itemset
	 * @param itemset      itemset to be investigated in this part
	 * @param neighborings range of space determined by tail itemset of prefix and
	 *                     within it we investigate current itemset
	 * @throws IOException
	 */
	private void seqMinerDFSHelper(List<Itemset> prefix, List<STPSet> prefixSTPSet, List<Double> prefixSigList,
			Itemset itemset, List<STPSet> neighborings) throws IOException {
		// Check memory usage
		MemoryLogger.getInstance().checkMemory();
//        System.out.println(MemoryLogger.getInstance().getMaxMemory());
		// Get support and minimal support among superset in original whole space
		double originSup = itemsetMapSup.get(itemset);
		// This list is just used to pass STPSet object so it always contains only one
		// item
		List<STPSet> candidateList = new LinkedList<>();
		// Find local support ratio and supporting points in given projected space
		double localSupRatio = computeAvgLocalSupRatio(neighborings, itemset, candidateList);
		STPSet candidateSTP = candidateList.get(0);
		double itSignificance = localSupRatio / originSup;
		// If significance of itemset in projected space is larger than user specified
		// threshold
		if (itSignificance >= ParametersSetting.MIN_SIGNIFICANCE) {
			// If besides large significance, there also exist enough many supporting points
			// in projected space
			if (candidateSTP.getSize() >= ParametersSetting.MIN_TAIL_SUP) {
				// This itemset together with prefix forms a qualified pattern
				if (ParametersSetting.OUTPUT_PATTERNS) {
					saveSequencePattern(prefix, prefixSTPSet, prefixSigList, itemset, candidateSTP, itSignificance);
					patternCount++;
				}

				// Then we modify prefix and go deeper
				prefix.add(itemset);
				prefixSTPSet.add(candidateSTP);
				prefixSigList.add(itSignificance);
				List<STPSet> nextNeighbors = acquireNeighboringPointsSet(candidateSTP);
				// All itemset of size 1 is enough for us to find all itemsets in next step
				for (Itemset nextItemset : levelMapItemsets.get(1)) {
					// This function call will go to next step
					seqMinerDFSHelper(prefix, prefixSTPSet, prefixSigList, nextItemset, nextNeighbors);
				}
				prefix.remove(prefix.size() - 1);
				prefixSigList.remove(prefixSigList.size() - 1);
				prefixSTPSet.remove(prefixSTPSet.size() - 1);
			}
			for (Itemset nextItemsetSameStep : itemsetMapDominantsSuperset.get(itemset)) {
				// This function call will not go to next step, it just investigate dominated
				// itemsets at this step
				// Here we see function call that go deeper is before that go broader
				// That we prefer to go deeper illustrate it is a DFS
				seqMinerDFSHelper(prefix, prefixSTPSet, prefixSigList, nextItemsetSameStep, neighborings);
			}
		} else if (ParametersSetting.ADOPTING_PRUNING) {
			double supMinSup = itemsetMapMinSup.get(itemset);
			// Compute upper bound of significance
			// Note that it take only one operation to acquire upper bound, so the time cost
			// is low
			double slackSignificance = localSupRatio / supMinSup;
			// If even this upper bound do not satisfy significance threshold
			if (slackSignificance < ParametersSetting.MIN_SIGNIFICANCE) {
				// We do not need to explore more itemset in this step
				return;
			} else if (ParametersSetting.MINI_GRAINED_PRUNING) {
				// Activate mini-grained pruning
				for (Itemset nextItemsetSameStep : itemsetMapDominantsSuperset.get(itemset)) {
					supMinSup = itemsetMapMinSup.get(nextItemsetSameStep);
					slackSignificance = localSupRatio / supMinSup;
					if (slackSignificance >= ParametersSetting.MIN_SIGNIFICANCE) {
						seqMinerDFSHelper(prefix, prefixSTPSet, prefixSigList, nextItemsetSameStep, neighborings);
					}
				}
			} else {
				for (Itemset nextItemsetSameStep : itemsetMapDominantsSuperset.get(itemset)) {
					seqMinerDFSHelper(prefix, prefixSTPSet, prefixSigList, nextItemsetSameStep, neighborings);
				}
			}
		}
		// Check memory
		MemoryLogger.getInstance().checkMemory();
	}

	/**
	 * This method mine sequential patterns by considering outer and inner
	 * arrangement separately. At outer level, it always use DFS. At inner level, it
	 * can use either DFS or BFS.
	 * 
	 * @throws IOException
	 */
	private void seqMinerTwoPhase(boolean innerDFS) throws IOException {
		List<Itemset> itemsetList = new LinkedList<>();
		List<STPSet> stpSetList = new LinkedList<>();
		List<Double> sigList = new LinkedList<>();
		for (Map.Entry<Itemset, STPSet> entry : itemsetMapSTPSet.entrySet()) {
			itemsetList.add(entry.getKey());
			stpSetList.add(entry.getValue());
			sigList.add(999.0);
		}
		// Set an enough significance for first iteration
		List<Double> prefixSigList = new LinkedList<>();

		dfsOuter(new LinkedList<>(), new LinkedList<>(), prefixSigList, itemsetList, stpSetList, sigList, innerDFS);
	}

	/**
	 * This method use DFS to generate sequence of itemset
	 * 
	 * @param prefix                      current sequence of itemset
	 * @param newItemsetList              a set of itemset that will be added to
	 *                                    current sequence separately
	 * @param newTailSupportingPointsList sets of tail points of itemset to be added
	 * @throws IOException
	 */
	private void dfsOuter(List<Itemset> prefix, List<STPSet> prefixSTPSETList, List<Double> prefixSigList,
			List<Itemset> newItemsetList, List<STPSet> newTailSupportingPointsList, List<Double> newSigList,
			boolean innerDFS) throws IOException {

		for (int i = 0; i < newItemsetList.size(); i++) {
			// The itemset to be added into sequence(prefix)
			Itemset newItemset = newItemsetList.get(i);
			// The tail point set associated with that itemset
			STPSet newStpSet = newTailSupportingPointsList.get(i);
			// The significance of the new added itemset
			double newSig = newSigList.get(i);
			// If size of the new sequence to be created is larger than 1
			// Save it to file
			if (prefix.size() != 0) {
				if (ParametersSetting.OUTPUT_PATTERNS) {
					saveSequencePattern(prefix, prefixSTPSETList, prefixSigList, newItemset, newStpSet, newSig);
					patternCount++;
				}

			}

			// Find neighboring space
			List<STPSet> neighboringSTPSetList = acquireNeighboringPointsSet(newStpSet);

			List<Itemset> addedItemsetList = new LinkedList<>();
			List<STPSet> addedTailPointsList = new LinkedList<>();
			List<Double> addedSigList = new LinkedList<>();
			// Use BFS to find all itemset that can be appended to current list of itemset
			// with their supporting points
			// i.e. all significant extension to current sequence in this neighboring space
			if (innerDFS)
				// Use DFS
				dfsInner(neighboringSTPSetList, addedItemsetList, addedTailPointsList, addedSigList);
			else
				// Use BFS
				bfsInner(neighboringSTPSetList, addedItemsetList, addedTailPointsList, addedSigList);

			// If there exist itemset to append
			if (addedItemsetList.size() != 0) {
				// Modify prefix and go deeper
				prefix.add(newItemset);
				prefixSTPSETList.add(newStpSet);
				prefixSigList.add(newSig);
				dfsOuter(prefix, prefixSTPSETList, prefixSigList, addedItemsetList, addedTailPointsList, addedSigList,
						innerDFS);
				prefixSTPSETList.remove(prefixSTPSETList.size() - 1);
				prefix.remove(prefix.size() - 1);
				prefixSigList.remove(prefixSigList.size() - 1);
			}
			// Check memory
			MemoryLogger.getInstance().checkMemory();
		}
	}

	/**
	 * This method is used to prepare for inner DFS
	 * 
	 * @param neiboringSTPSetList range of space that we will consider(if we don't
	 *                            allow overlapping, it contains only one item)
	 * @param addedItemsetList    list for holding itemsets to be added
	 * @param addedTailPointsList list for holding space points corresponding to
	 *                            added itemset
	 * @throws IOException
	 */
	private void dfsInner(List<STPSet> neiboringSTPSetList, List<Itemset> addedItemsetList,
			List<STPSet> addedTailPointsList, List<Double> significanceList) throws IOException {
		for (Itemset itemset : levelMapItemsets.get(1)) {
			dfsInnerHelp(itemset, neiboringSTPSetList, addedItemsetList, addedTailPointsList, significanceList);
		}
	}

	/**
	 * This recursive method is used to investigate itemset in given projected space
	 * 
	 * @param itemset             itmeset to be investigated
	 * @param neiboringSTPSetList a list of STPSet denoting projected space
	 * @param addedItemsetList    a list for holding itemsets to be added
	 * @param addedTailPointsList list for holding space points corresponding to
	 *                            added itemset
	 * @throws IOException
	 */
	private void dfsInnerHelp(Itemset itemset, List<STPSet> neiboringSTPSetList, List<Itemset> addedItemsetList,
			List<STPSet> addedTailPointsList, List<Double> significanceList) throws IOException {
		// Get support ratio ,superMin support ratio and supporting points of the
		// considered itemset
		double originalSupRatio = itemsetMapSup.get(itemset);
		// The list is used for storing set of supporting points of the itemset in given
		// neighboring space
		List<STPSet> candidateSTPSetList = new LinkedList<>();
		// This function call will return local support ratio and the set of supporting
		// points in neighboring space is
		// stored in the passed list
		double localSupRatio = computeAvgLocalSupRatio(neiboringSTPSetList, itemset, candidateSTPSetList);
		// Real significance
		double itSignificance = localSupRatio / originalSupRatio;
		STPSet candidateSTPSet = candidateSTPSetList.get(0);
		// If significance meet requirement
		if (itSignificance >= ParametersSetting.MIN_SIGNIFICANCE) {
			// If size of the set of tail supporting points meet requirement
			if (candidateSTPSet.getSize() >= ParametersSetting.MIN_TAIL_SUP) {
				// Add itemset into prefix list and add corresponding set of supporting points,
				// significance
				addedItemsetList.add(itemset);
				addedTailPointsList.add(candidateSTPSet);
				significanceList.add(itSignificance);
			}
			// Continue to check next itemset according to DFS search structure
			for (Itemset nextItemset : itemsetMapDominantsSuperset.get(itemset)) {
				dfsInnerHelp(nextItemset, neiboringSTPSetList, addedItemsetList, addedTailPointsList, significanceList);
			}
		}
		// If significance don't meet requirement and we plan to adopt pruning
		else if (ParametersSetting.ADOPTING_PRUNING) {
			double superMinSup = itemsetMapMinSup.get(itemset);
			// Then compute upper bound of significance
			double slackSignificance = localSupRatio / superMinSup;
			// If even the upper bound of significance don't meet requirement
			if (slackSignificance < ParametersSetting.MIN_SIGNIFICANCE)
				// Do not need to explore the dominated supersets
				return;
			// If mini-grained pruning is activated
			else if (ParametersSetting.MINI_GRAINED_PRUNING) {
				for (Itemset nextItemset : itemsetMapDominantsSuperset.get(itemset)) {
					// In the smaller range, only if the upper bound of significance meet
					// requirement, we check next
					// itemset according to DFS search structure.
					superMinSup = itemsetMapMinSup.get(nextItemset);
					slackSignificance = localSupRatio / superMinSup;
					if (slackSignificance >= ParametersSetting.MIN_SIGNIFICANCE) {
						dfsInnerHelp(nextItemset, neiboringSTPSetList, addedItemsetList, addedTailPointsList,
								significanceList);
					}
				}
			}
			// If mini-grained pruning is not activated
			else {
				// Check next itemset according to DFS search structure
				for (Itemset nextItemset : itemsetMapDominantsSuperset.get(itemset)) {
					dfsInnerHelp(nextItemset, neiboringSTPSetList, addedItemsetList, addedTailPointsList,
							significanceList);
				}
			}
		}
		// Check memory after processing each itemset
		MemoryLogger.getInstance().checkMemory();
//        // if allowing pruning technique
//        if (ParametersSetting.ADOPTING_PRUNING) {
//            if (itSignificance < ParametersSetting.MIN_SIGNIFICANCE) {
//                double superMinSup = itemsetMapMinSup.get(itemset);
//                //compute upper bound of significance
//                double slackSignificance = localSupRatio/superMinSup;
//                //if even upper bound of significance is not large enough
//                if (slackSignificance < ParametersSetting.MIN_SIGNIFICANCE)
//                    //do not need to explore the dominated supersets
//                    return;
//            }
//        }
//        //in common case, (no pruning technique or pruning requirement is not satisfied)
//        //we will explore supersets that dominated by current itemset
//        for (Itemset nextItemset : itemsetMapDominantsSuperset.get(itemset)) {
//            DFSInnerHelper(nextItemset, neiboringSTPSetList, addedItemsetList, addedTailPointsList, significanceList);
//        }
	}

	/**
	 * This method implement inner breadth first search. It find all itemset that
	 * can be added to previous sequence according to tail points as well new tail
	 * points
	 * 
	 * @param neiboringSTPSetList the tail points of the itemset to be added to
	 *                            previous sequence
	 * @param addedItemsetList    the itemset that will be added to previous
	 *                            sequence
	 * @param addedTailPointsList the tail points of the newly found itemset
	 */
	private void bfsInner(List<STPSet> neiboringSTPSetList, List<Itemset> addedItemsetList,
			List<STPSet> addedTailPointsList, List<Double> significanceList) throws IOException {
		if (neiboringSTPSetList.isEmpty()) {
			return;
		}
		// Record itemset of next level
		Set<Itemset> nextLevelItemsets = new HashSet<>();
		nextLevelItemsets.addAll(levelMapItemsets.get(1));
		// Record itemset of current level
		Set<Itemset> currentLevelItemsets = null;
		// For each level(size of itemset)
		for (int level = 1; level <= levelMapItemsets.keySet().size(); level++) {
//        for (int level = 1; nextLevelItemsets.size() != 0; level++) {
			currentLevelItemsets = nextLevelItemsets;
			// Constructing a set to hold itemsets of next level
			nextLevelItemsets = null;
			if (levelMapItemsets.containsKey(level + 1)) {
				nextLevelItemsets = new HashSet<>();
				nextLevelItemsets.addAll(levelMapItemsets.get(level + 1));
			}
			// For each itemset at current level
			for (Itemset itemset : currentLevelItemsets) {
				// Get its support ratio ,superMin support ratio and supporting points
				double originalSupRatio = itemsetMapSup.get(itemset);

				List<STPSet> candidateSTPSetList = new LinkedList<>();
				// Compute local support ratio given neighboring space and itemset
				double avgLocalSupRatio = computeAvgLocalSupRatio(neiboringSTPSetList, itemset, candidateSTPSetList);
				// Compute significance of itemset in neighboring space with respect to whole
				// space
				double itSignificance = avgLocalSupRatio / originalSupRatio;

				if (itSignificance >= ParametersSetting.MIN_SIGNIFICANCE) {
					// If significance is larger than the user specified threshold
					STPSet supSTP = candidateSTPSetList.get(0);
					if (supSTP.getSize() >= ParametersSetting.MIN_TAIL_SUP) {
						// And there exist enough points support this itemset in current neighboring
						// space
						// Add this itemset to prepare next extension
						addedItemsetList.add(itemset);
						addedTailPointsList.add(supSTP);
						significanceList.add(itSignificance);
					}
				}
				if (ParametersSetting.ADOPTING_PRUNING && nextLevelItemsets != null) {
					if (itSignificance < ParametersSetting.MIN_SIGNIFICANCE) {
						double superMinSup = itemsetMapMinSup.get(itemset);
						double itSlackSignificance = avgLocalSupRatio / superMinSup;
						if (itSlackSignificance < ParametersSetting.MIN_SIGNIFICANCE) {
							// If slack significance is smaller than the user specified threshold
							// Remove all super set of this itemset
							for (Itemset superSet : itemsetMapSuperset.get(itemset)) {
								if (nextLevelItemsets.contains(superSet)) {
									nextLevelItemsets.remove(superSet);
								}
							}
						} else if (ParametersSetting.MINI_GRAINED_PRUNING) {
							for (Itemset superset : itemsetMapSuperset.get(itemset)) {
								double superSup = itemsetMapSup.get(superset);
								itSlackSignificance = avgLocalSupRatio / superSup;
								if (itSlackSignificance < ParametersSetting.MIN_SIGNIFICANCE
										&& nextLevelItemsets.contains(superset)) {
									nextLevelItemsets.remove(superset);
								}
							}
						}
					}

//                        if (! ParametersSetting.MINI_GRAINED_PRUNING) {
//                            //if we choose to prune search space by upper bound of significance
//                            //compute slack significance by modifying support ratio in whole space to supMin support ratio in whole space
//                            double superMinSup = itemsetMapMinSup.get(itemset);
//                            double itSlackSignificance = avgLocalSupRatio/superMinSup;
//                            if (itSlackSignificance < ParametersSetting.MIN_SIGNIFICANCE) {
//                                //if slack significance is smaller than the user specified threshold
//                                //remove all super set of this itemset
//                                for (Itemset superSet: itemsetMapSuperset.get(itemset)) {
//                                    if (nextLevelItemsets.contains(superSet)) {
//                                        nextLevelItemsets.remove(superSet);
//                                    }
//                                }
//                            }
//                        }
//                        else {
//                            for (Itemset superset : itemsetMapSuperset.get(itemset)) {
//                                double superSup = itemsetMapSup.get(superset);
//                                double itSlackSignificance = avgLocalSupRatio/superSup;
//                                if (itSlackSignificance < ParametersSetting.MIN_SIGNIFICANCE && nextLevelItemsets.contains(superset)) {
//                                    nextLevelItemsets.remove(superset);
//                                }
//                            }
//                        }
				}
				// Check memory after processing each itemset
				MemoryLogger.getInstance().checkMemory();
			}
		}
	}

	/**
	 * This method is used to compute support ratio of given itemset in neighboring
	 * space
	 * 
	 * @param neiboringSTPSetList donote neighboring points. if not allow
	 *                            overlapping, it is just a STPSet object rather
	 *                            than a list.
	 * @param itemset             the itemset to be compute support ratio
	 * @param candidateSTPSetList the list supporting points set
	 * @return average local support ratio
	 */
	private double computeAvgLocalSupRatio(List<STPSet> neiboringSTPSetList, Itemset itemset,
			List<STPSet> candidateSTPSetList) {
		STPSet stpSet = itemsetMapSTPSet.get(itemset);

		List<STPSet> tempSTPSetList = new LinkedList<>();
		// Sum up all support ratio
		double totalLocalSupRaio = 0;
		// For each set of neighboring points(neighboring space)
		for (STPSet neibor : neiboringSTPSetList) {
			// Size of single neighboring size
			int neiboringSize = neibor.getSize();
			// Compute intersection of this neighboring space and the itemset's set of
			// supporting points
			STPSet tempSTPSet = neibor.intersect(stpSet);
			// Record this possible tail points
			tempSTPSetList.add(tempSTPSet);
			// Compute support ratio in this neighboring space
			int localSup = tempSTPSet.getSize();
			if (neiboringSize != 0) {
				double localSupRatio = (double) localSup / neiboringSize;
//                        System.out.println(localSup + "!!!!!!!!!!!!!" + neiboringSize + "!!!!!!!!!" + localSupRatio);
				totalLocalSupRaio += localSupRatio;
			}
		}
		candidateSTPSetList.add(STPSet.mergeSTPSets(tempSTPSetList));
		// compute average support ratio in all neighboring space
//		double avgLocalSupRaio = ;
		return totalLocalSupRaio / neiboringSTPSetList.size();
	}

	/**
	 * This method is used to acquire neighboring points given a point set
	 * 
	 * @param stpSet based point set
	 * @return all neighboring point set
	 */
	private List<STPSet> acquireNeighboringPointsSet(STPSet stpSet) {
		// Constructing a set of neighboring space for this new itemset according to its
		// tail points
		List<STPSet> neighboringSTPSetList = new LinkedList<>();

		// For each spatio-temporal point, find a set of neighboring points
		for (int timestamp : stpSet.getTimestamps()) {
			for (int vId : stpSet.getVIdSet4Timestamp(timestamp)) {
				STPSet neighborSTPSet = null;
				if (ParametersSetting.NEIGHBOR_FLAG == 0) {
					neighborSTPSet = findNeighbors0(timestamp, vId);
				} else if (ParametersSetting.NEIGHBOR_FLAG == 1) {
					neighborSTPSet = findNeighbors1(timestamp, vId);
				}
				neighboringSTPSetList.add(neighborSTPSet);
			}
		}
		if (!ParametersSetting.ALLOW_OVERLAPPING) {
			// if we do not allow repeated appearance of neighboring points,
			// a merge process must be performed.
			List<STPSet> tempSTPSet = new LinkedList<>();
			tempSTPSet.add(STPSet.mergeSTPSets(neighboringSTPSetList));
			neighboringSTPSetList = tempSTPSet;
		}
		return neighboringSTPSetList;
	}

	/**
	 * This method define what neighborhood is and find neighbors for a vertex with
	 * specified timestamp and id
	 * 
	 * @param timestamp used to located itemAG in timestampMapItemAG
	 * @param vId       used to located vertex in an itemAG
	 * @return a set of neighboring points
	 */
	private STPSet findNeighbors0(int timestamp, int vId) {
		List<Integer> nextTimestamps = new LinkedList<>();
		List<Set<Integer>> vIdSets = new LinkedList<>();
		ItemAttributedGraph itemAG = timestampMapItemAG.get(timestamp);
		nextTimestamps.add(timestamp + 1);
//        nextTimestamps.add(timestamp + 2);
		Set<Integer> neighbors = itemAG.findAllNeighbors4V(vId);

		vIdSets.add(neighbors);
		return new STPSet(nextTimestamps, vIdSets);
	}

	private STPSet findNeighbors1(int timestamp, int vId) {
		List<Integer> nextTimestamps = new LinkedList<>();
		List<Set<Integer>> vIdSets = new LinkedList<>();
		nextTimestamps.add(timestamp + 1);
		Set<Integer> neighbors = new HashSet<>();
		neighbors.add(vId);
		vIdSets.add(neighbors);
		return new STPSet(nextTimestamps, vIdSets);
	}

	/**
	 * This method find mapping that map itemset to superset that are larger than
	 * it.
	 */
	private static void getItemsetMapDominantsSuperset() {
		itemsetMapDominantsSuperset = new HashMap<>();
		for (int i = 1; i <= levelMapItemsets.keySet().size(); i++) {
			for (Itemset itemset : levelMapItemsets.get(i)) {
				Set<Itemset> dominantSupersets = new HashSet<>();
				itemsetMapDominantsSuperset.put(itemset, dominantSupersets);
				for (Itemset superset : itemsetMapSuperset.get(itemset)) {
					if (superset.compareTo(itemset) > 0) {
						dominantSupersets.add(superset);
					}
				}
			}
		}
//        wholeSpace = EclatAlgo.wholeSpace;
//        Set<Itemset> startDominants = new HashSet<>();
//        startItemset = new Itemset(Integer.MAX_VALUE);
//        startDominants.addAll(levelMapItemsets.get(1));
//        itemsetMapDominantsSuperset.put(startItemset, startDominants);
	}

	/**
	 * This method complete 3 tasks These 3 mappings will always be used whatever
	 * traversal method you use and whether you use optimization or not first is
	 * constructing mapping from size of itemset -> all itemsets with that size
	 * second is constructing mapping from itemset -> all its direct supersets third
	 * is finding for each itemset its superMinsupport
	 */
	private void getBasicMappings() {
		// partitioning itemsets according to size
		// so that we can quickly find itemsets of certain size
		levelMapItemsets = new HashMap<>();
		for (Itemset itemset : itemsetMapSTPSet.keySet()) {
			int size = itemset.getSize();
			Set<Itemset> itemsets = levelMapItemsets.get(size);
			if (itemsets == null) {
				itemsets = new HashSet<>();
				levelMapItemsets.put(size, itemsets);
			}
			itemsets.add(itemset);
		}

		// find for each itemset its direct superset
		itemsetMapSuperset = new HashMap<>();
		for (int i = 1; i <= levelMapItemsets.size(); i++) {
			for (Itemset itemset1 : levelMapItemsets.get(i)) {
				itemsetMapSuperset.put(itemset1, new LinkedList<>());
				Set<Itemset> itemset2List = levelMapItemsets.get(i + 1);
				if (itemset2List != null) {
					for (Itemset itemset2 : itemset2List) {
						if (itemset2.isSupersetOf(itemset1)) {
							itemsetMapSuperset.get(itemset1).add(itemset2);
						}
					}
				}
			}
		}

		// find for each itemset its itemsetMapMinSup
		itemsetMapSup = new HashMap<>();
//        itemsetMapMinSup = new HashMap<>();
		for (int i = levelMapItemsets.size(); i > 0; i--) {
			for (Itemset itemset : levelMapItemsets.get(i)) {
				int sup = itemsetMapSTPSet.get(itemset).getSize();
				itemsetMapSup.put(itemset, (double) sup / totalNumVertex);
//                for (Itemset superset : itemsetMapSuperset.get(itemset)) {
//                    int superSup = itemsetMapSTPSet.get(superset).getSize();
//                    if (superSup < sup)
//                        sup = superSup;
//                }
//                itemsetMapMinSup.put(itemset, (double)sup/totalNumVertex);
			}
		}

	}

	private void getItemsetMapSuperMinAndMapDominants() {
		// if we plan to use DFS, then a mapping of itemset -> dominating supersets is
		// needed
		if (ParametersSetting.TRAVERSAL_FLAG == 0 || ParametersSetting.TRAVERSAL_FLAG == 1) {
			getItemsetMapDominantsSuperset();
		}

		// if we plan to adopt pruning technique
		if (ParametersSetting.ADOPTING_PRUNING) {
			// we need a mapping of itemset -> superMinSup
			itemsetMapMinSup = new HashMap<>();
			// if we do not adopt a specific optimization
			if (ParametersSetting.LARGE_GRAINED_PRUNING) {
				// we just acquire a general upper bound that may not so efficient for specific
				// case
				getItemsetMapSuperMinSlack();
			}
			// compute a search strategy based upper bound
			else {
				// this is for case of DFS
				if (ParametersSetting.TRAVERSAL_FLAG == 0 || ParametersSetting.TRAVERSAL_FLAG == 1) {
					getItemsetMapSuperMinTight4DFS();
				}
				// this for BFS
				else {
					getItemsetMapSuperMinTight4BFS();
				}
			}
		}
	}

	private void getItemsetMapSuperMinTight4BFS() {
		for (int level = 1; level <= levelMapItemsets.size(); level++) {
			for (Itemset itemset : levelMapItemsets.get(level)) {
				double supRatioMin = itemsetMapSup.get(itemset);
				for (Itemset superset : itemsetMapSuperset.get(itemset)) {
					double superSup = itemsetMapSup.get(superset);
					if (superSup < supRatioMin) {
						supRatioMin = superSup;
					}
				}
				itemsetMapMinSup.put(itemset, supRatioMin);
			}
		}
	}

	private void getItemsetMapSuperMinTight4DFS() {
		int highestLevel = levelMapItemsets.size();
		for (Itemset itemset : levelMapItemsets.get(highestLevel)) {
			itemsetMapMinSup.put(itemset, itemsetMapSup.get(itemset));
		}
		for (int level = highestLevel - 1; level > 0; level--) {
			for (Itemset itemset : levelMapItemsets.get(level)) {
				double supRatioMin = itemsetMapSup.get(itemset);
				for (Itemset superset : itemsetMapDominantsSuperset.get(itemset)) {
					double superSup = itemsetMapMinSup.get(superset);
					if (superSup < supRatioMin) {
						supRatioMin = superSup;
					}
				}
				itemsetMapMinSup.put(itemset, supRatioMin);
			}
		}
	}

	private void getItemsetMapSuperMinSlack() {
		for (int i = levelMapItemsets.size(); i > 0; i--) {
			for (Itemset itemset : levelMapItemsets.get(i)) {
				double supRatioMin = itemsetMapSup.get(itemset);
				for (Itemset superset : itemsetMapSuperset.get(itemset)) {
					double superSup = itemsetMapMinSup.get(superset);
					if (superSup < supRatioMin)
						supRatioMin = superSup;
				}
				itemsetMapMinSup.put(itemset, supRatioMin);
			}
		}
	}

	/**
	 * This method just get timestampMapItemAG and event type mapping from
	 * preprocessing
	 * 
	 * @throws IOException
	 */
	private void getItemDyAGFromPreprocessing() throws IOException {
		timestampMapItemAG = Preprocess.itDyAG;

//        System.out.println("sequence miner input: timestamps-" + timestampMapItemAG.keySet().size() + "   vertex-" + timestampMapItemAG.get(0).getTotalSize());
		totalNumVertex = timestampMapItemAG.get(0).getTotalSize() * timestampMapItemAG.size();
		eventTypeMapName = Preprocess.eventTypeMapping;
		if (ParametersSetting.EXHIBIT_SUPPORTING_POINTS)
			getVerterMapName();
	}

	private  void getVerterMapName() throws IOException {
		vertexMapName = new HashMap<>();
//        System.out.println(ParametersSetting.VERTEX_MAP_NAME_PATH);
		File vertexMapNameFile = new File(ParametersSetting.VERTEX_MAP_NAME_PATH);
		if (!vertexMapNameFile.exists())
			vertexMapNameFile.createNewFile();
		BufferedReader br = new BufferedReader(new FileReader(ParametersSetting.VERTEX_MAP_NAME_PATH));
		String line = null;
		while ((line = br.readLine()) != null) {
			try {
				String[] items = line.split(",");
				int vId = Integer.parseInt(items[0]);
				String name = items[1];
				vertexMapName.put(vId, name);
			} catch (ArrayIndexOutOfBoundsException e) {
				System.out.println(line);
			}
		}
		br.close();
	}

	/**
	 * This method get frequent itemset and their supporting points from static
	 * method of class EclatAlgo, not from file
	 * 
	 * @throws IOException
	 */
	private void readFrequentItemsetFromEclat() throws IOException {
		itemsetMapSTPSet = EclatAlgo.extendFreItems();
		totalItemsetNum = itemsetMapSTPSet.size();
//        System.out.println("load all frequent itemsets and their supporting points");
		System.out.println("total " + totalItemsetNum + " frequent itemsets");

//        print all itemsets and their support
//        System.out.println(itemsetMapSTPSet.size());
//        for (Itemset itemset: itemsetMapSTPSet.keySet()) {
//            System.out.println(itemset + "\n" +itemsetMapSTPSet.get(itemset));
//        }
	}

	/**
	 * This method save all frequent patterns to file
	 * 
	 * @param prefix        sequence of itemset which is used to extend
	 * @param prefixSTPSet  sequence of STPSet(support points) corresponding to
	 *                      prefix itemset
	 * @param prefixSigList sequence of significance of each step
	 * @param newItemset    new added itemset
	 * @param tailSTPSet    support of newItemset in neighboring space of prefix
	 * @param newSig        significance of new added itemset
	 * @throws IOException
	 */
	private void saveSequencePattern(List<Itemset> prefix, List<STPSet> prefixSTPSet, List<Double> prefixSigList,
			Itemset newItemset, STPSet tailSTPSet, double newSig) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		sb.append("{");
		if (INTERPRET_RESULT) {
			for (Itemset itemset : prefix) {
				sb.append(interpretItemset(itemset)).append(",");
			}
			sb.append(interpretItemset(newItemset));
		} else {
			for (Itemset itemset : prefix) {
				sb.append(itemset).append(",");
			}
			sb.append(newItemset);
		}
		sb.append("} {");
		for (STPSet stpSet : prefixSTPSet) {
			sb.append(stpSet.getSize()).append(",");
		}
		sb.append(tailSTPSet.getSize());
		sb.append("} {");
		for (int i = 1; i < prefixSigList.size(); i++) {
			sb.append(nf.format(prefixSigList.get(i))).append(",");
		}
		sb.append(nf.format(newSig));
		sb.append("}\n");
		if (ParametersSetting.EXHIBIT_SUPPORTING_POINTS) {
			for (STPSet stpSet : prefixSTPSet) {
				for (Integer timestamp : stpSet.getTimestamps()) {
					for (Integer vId : stpSet.getVIdSet4Timestamp(timestamp)) {
						sb.append("(").append(timestamp).append(",").append(vertexMapName.get(vId)).append(") ");
					}
				}
				sb.append("\n");
			}
			for (Integer timestamp : tailSTPSet.getTimestamps()) {
				for (Integer vId : tailSTPSet.getVIdSet4Timestamp(timestamp)) {
					sb.append("(").append(timestamp).append(",").append(vertexMapName.get(vId)).append(") ");
				}
			}
			sb.append("\n");
		}
		bw.write(sb.toString());
	}

	/**
	 * This method convert item denoted by Integer to String
	 * 
	 * @param itemset the itemset whose item will be decoded
	 * @return Itemset indicating real meaning
	 */
	private String interpretItemset(Itemset itemset) {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for (Integer item : itemset.getItems()) {
			sb.append(eventTypeMapName.get(item)).append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(")");
		return sb.toString();
	}

	/**
	 * This method read all frequent itemset and their supporting points into memory
	 * from intermediate file
	 * 
	 * @throws IOException
	 */
	private void readFrequentItemsetFromFile() throws IOException {
		EclatAlgo.extendFreItems();

		String freqItemsetPath = "dataset/DBLP/result/frequent_itemset.txt";
		BufferedReader br = new BufferedReader(new FileReader(freqItemsetPath));

		// use list to store frequent itemset, support and supporting points
		itemsetMapSTPSet = new LinkedHashMap<>();

		String line = br.readLine();
		while (line != null) {
			// when we find a line start with "#", we find a frequent itemset.
			// next is information about components, support, and supporting points
			if (line.startsWith("#")) {
				line = br.readLine();
				// get information about components and support from this line
				Itemset itemset = new Itemset(line);
				br.readLine();
				br.readLine();
				// get information about supporting points from next lines
				// use timestamp list and list of supporting point set to create a new STPSet
				// object
				List<Integer> timestampList = new LinkedList<>();
				List<Set<Integer>> vIdSetList = new LinkedList<>();
				while ((line = br.readLine()).startsWith("[")) {
					String[] splitItems = line.split(" ");
					timestampList.add(Integer.parseInt(splitItems[0].substring(1, splitItems[0].length() - 1)));
					Set<Integer> vIdSet = new HashSet<>();
					String idStr = splitItems[1].substring(1, splitItems[1].length() - 1);
					for (String str : idStr.split(",")) {
						vIdSet.add(Integer.parseInt(str));
					}
					vIdSetList.add(vIdSet);
				}
				itemsetMapSTPSet.put(itemset, new STPSet(timestampList, vIdSetList));
				totalItemsetNum++;
			} else {
				line = br.readLine();
			}
		}

//        System.out.println("load all frequent itemsets and their supporting points");
//        System.out.println("total " + totalItemsetNum + " frequent itemsets");
//        for (Itemset itemset: itemsetMapSTPSet.keySet()) {
//            System.out.println(itemset + "\n" + itemsetMapSTPSet.get(itemset));
//        }
	}

	public void printStats() {
		System.out.println("=============  TSeqMiner v2.40 - STATS =============");
		System.out.println(" Time to prepare the data: " + ParametersSetting.PREPARE + " ms");
		System.out.println(" Time to mine patterns from data: " + totalMiningTime + " ms");
		System.out.println(" Maximum memory usage : " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println(" Pattern count: " + patternCount);
		System.out.println("====================================================");
	}
}
