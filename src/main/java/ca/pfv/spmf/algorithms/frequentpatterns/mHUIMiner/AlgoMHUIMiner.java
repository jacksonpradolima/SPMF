package ca.pfv.spmf.algorithms.frequentpatterns.mHUIMiner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.pfv.spmf.tools.MemoryLogger;


/**
 * This is an implementation of the mHUIMiner algorithm. The algorithm takes transaction 
 * data in SPMF format and a user specified minUtility, and outputs all the high utility itemsets.
 * <\br><\br>
 * 
 * The algorithm is a combination of the IHUP algorithm and the HUI-Miner algorithm 
 * The details of these two algorithms are described in the following two papers: <\br><\br>
 * 
 * Chowdhury Farhan Ahmed, Syed Khairuzzaman Tanbeer, Byeong-Soo Jeong, 
 * Young-Koo Lee: Efficient Tree Structures for High Utility Pattern Mining 
 * in Incremental Databases. IEEE Trans. Knowl. Data Eng. 21(12): 1708-1721 (2009)
 * 
 * Liu, M., Qu, J. (2012). Mining High Utility Itemsets without Candidate Generation. 
 * Proc. of CIKM 2012. pp.55-64.<\br><\br>
 * 
 * This is the original implementation obtained from the GitHub repository. It has been
 * slightly adapted for integration in the SPMF software. 
 * 
 * 
 * @see IHUPTreeMod
 * @see UtilityTuple
 * @see UtilityList
 * @See Node
 * @see Item
 * @author Yuxuan(Alex) Peng et al.
 */

public class AlgoMHUIMiner {
	/**  the time the algorithm started */
	private long startTimestamp = 0; 
	
	/** the time the algorithm terminated */
	private long endTimestamp = 0; 
	
	/** the number of HUIs generated */
	private int huiCount = 0;
	
	/** sum of all transaction utilities */
	private long totalUtility = 0; 
	
	/** the minimum utility threshold */
	private int minUtility = 0; 
	
	/** number of times the construct method is called */
	private int joinCount = 0;
	
	/**  Map each item to its TWU */
	private Map<Integer, Integer> mapItemToTWU;
	
	/** Map each item to its utility list */
	private Map<Integer, UtilityList> mapItemToUtilityList;

	/** Object for writing the output to a file */
	private BufferedWriter writer = null; 
	
	/** If true the debug mode is activated */
	private final boolean DEBUG = false;

	/**
	 * Method to run the algorithm
	 * 
	 * @param input path to an input file
	 * @param output  path for writing the output file
	 * @param minimumUtility  the minimum utility threshold as a ratio
	 * @throws IOException  exception if error while reading or writing the file
	 */
	public void runAlgorithm(String input, String output, int minimumUtility) throws IOException {

		MemoryLogger.getInstance().reset();

		startTimestamp = System.currentTimeMillis();

		writer = new BufferedWriter(new FileWriter(output));

		// create a map to store the TWU of each item
		mapItemToTWU = new HashMap<Integer, Integer>();

		// ******************************************
		// First database scan to calculate the TWU of each item.
		BufferedReader myInput = null;
		String thisLine;
		try {
			myInput = new BufferedReader(new InputStreamReader(new FileInputStream(new File(input))));
			// for each line (transaction) until the end of file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is a comment, is empty or is a kind of metadata
				if (thisLine.isEmpty() == true || thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
						|| thisLine.charAt(0) == '@') {
					continue;
				}

				// split the transaction according to the : separator
				String split[] = thisLine.split(":");
				// the first part is the list of items
				String items[] = split[0].split(" ");
				// the second part is the transaction utility
				int transactionUtility = Integer.parseInt(split[1]);
				totalUtility += transactionUtility;

				// for each item, we add the transaction utility to its TWU
				for (int i = 0; i < items.length; i++) {
					Integer item = Integer.parseInt(items[i]);
					Integer twu = mapItemToTWU.get(item);
					twu = (twu == null) ? transactionUtility : twu + transactionUtility;
					mapItemToTWU.put(item, twu);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}
		MemoryLogger.getInstance().checkMemory();

		// ******************************************
		// second database scan generate revised transaction and global
		// IHUP-Tree
		// start mining once the IHUP-Tree is built
		try {

			// calculate minUtility threshold
			minUtility = minimumUtility;

			IHUPTreeMod tree = new IHUPTreeMod();

			// create the global hash table to store utilityList
			mapItemToUtilityList = new HashMap<Integer, UtilityList>();
			for (Integer itemID : mapItemToTWU.keySet()) {
				if (mapItemToTWU.get(itemID) >= minUtility) {
					UtilityList uList = new UtilityList(itemID);
					mapItemToUtilityList.put(itemID, uList);
				}
			}

			myInput = new BufferedReader(new InputStreamReader(new FileInputStream(new File(input))));

			// Transaction ID to track transactions
			int tid = 0;

			// for each line (transaction) until the end of file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is a comment, is empty or is a kind of metadata
				if (thisLine.isEmpty() == true || thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
						|| thisLine.charAt(0) == '@') {
					continue;
				}

				// split the line according to the separator
				String split[] = thisLine.split(":");
				// get the list of items
				String items[] = split[0].split(" ");
				// get the list of utility values corresponding to each item
				// for that transaction
				String utilityValues[] = split[2].split(" ");

				// Create revised transaction
				List<Item> revisedTransaction = new ArrayList<Item>();
				// for each item in the original transaction
				for (int i = 0; i < items.length; i++) {
					// convert values to integers
					int item = Integer.parseInt(items[i]);
					int utility = Integer.parseInt(utilityValues[i]);
					Item element = new Item(item, utility);

					// we remove unpromising items from the tree
					if (mapItemToTWU.get(item) >= minUtility) {
						revisedTransaction.add(element);
					}
				}

				tid++;// increment transaction ID

				// revised transaction in ascending order of TWU
				Collections.sort(revisedTransaction, new Comparator<Item>() {
					public int compare(Item o1, Item o2) {
						return compareItemsAsc(o1.getItemID(), o2.getItemID(), mapItemToTWU);
					}
				});

				// populate the hash table for utilityLists
				// The last item in the transaction has 0 remainingUtility
				int remainingUtility = 0;
				for (int i = revisedTransaction.size() - 1; i >= 0; i--) {
					Item item = revisedTransaction.get(i);
					UtilityTuple uTuple = new UtilityTuple(tid, item.getUtility(), remainingUtility);
					mapItemToUtilityList.get(item.getItemID()).addTuple(uTuple);
					remainingUtility += item.getUtility();
				}

				// add transaction to the global IHUP-Tree
				tree.addTransaction(revisedTransaction, tid);
			} // end while, finished building tree and utilityLists

			// We create the header table for the global IHUP-Tree
			tree.createHeaderList(mapItemToTWU);
			MemoryLogger.getInstance().checkMemory();

			// Next block only for debugging
			if (DEBUG) {
				System.out.println("GLOBAL TREE" + "\nmapITEM-TWU : " + mapItemToTWU + "\n" + tree.toString());
			}

			// Start Mining over the tree
			// For each item from the bottom of the header table list of the
			// tree
			for (int i = tree.headerList.size() - 1; i >= 0; i--) {
				Integer itemID = tree.headerList.get(i);

				// initial itemset contains only single item
				ArrayList<Integer> itemset = new ArrayList<Integer>();
				itemset.add(itemID);
				UtilityList ulist = mapItemToUtilityList.get(itemID);

				// if sumIutils >= minUtility, the itemset is a HUI
				// write this itemset in the output file
				if (ulist.sumIutils >= minUtility) {
					writeOut(itemset, ulist.sumIutils);
				}

				// if sumIutils + ulist.sumRutilsm>= minUtility,
				// we expand current itemset (build local tree)
				if ((ulist.sumIutils + ulist.sumRutils) >= minUtility) {
					// ===== CREATE THE LOCAL TREE =====
					IHUPTreeMod localTree = createLocalTree(tree, itemID);
					MemoryLogger.getInstance().checkMemory();

					// NEXT LINE IS FOR DEBUGING:
					if (DEBUG) {
						System.out.println("LOCAL TREE for projection by:"
								+ ((itemset == null) ? ""
										: Arrays.toString(itemset.toArray(new Integer[itemset.size()])) + ",")
								+ itemID + "\n" + localTree.toString());
					}

					// call the mining procedure to explore
					// itemsets that are extensions of the current itemset
					if (localTree.headerList.size() > 0) {
						mHUIMiner(localTree, minUtility, itemset, ulist.uLists);
						MemoryLogger.getInstance().checkMemory();
					}
				}
			} // end for
		} catch (Exception e) {
			// catches exception if error while reading the input file
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}
		MemoryLogger.getInstance().checkMemory();

		// record end time
		endTimestamp = System.currentTimeMillis();
		writer.close();
	}
	
	private int compareItemsAsc(int item1, int item2, Map<Integer, Integer> mapItemEstimatedUtility) {
		int compare = mapItemEstimatedUtility.get(item1) - mapItemEstimatedUtility.get(item2);
		// if the same, use the lexical order otherwise use the TWU
		return (compare == 0) ? item1 - item2 : compare;
	}

	
	/**
	 * Mine UP Tree recursively
	 * 
	 * @param tree IHUPTree to mine
	 * @param minUtility minimum utility threshold
	 * @param itemset the prefix itemset
	 * @param pTuples a list of UtilityTuples of the current itemset
	 */
	private void mHUIMiner(IHUPTreeMod tree, int minUtility, ArrayList<Integer> itemset, List<UtilityTuple> pTuples)
			throws IOException {
		// from the bottom of the header list
		for (int i = tree.headerList.size() - 1; i >= 0; i--) {

			Integer itemID = tree.headerList.get(i);
			UtilityList xUL = mapItemToUtilityList.get(itemID);

			// extend current itemset p by item x
			itemset.add(itemID);

			// construct new utility list pxTuples
			UtilityList pxTuples = construct(pTuples, xUL.uLists);
			MemoryLogger.getInstance().checkMemory();
			joinCount++;

			// if sumIutils >= minUtility, the itemset is a HUI
			if (pxTuples.sumIutils >= minUtility) {
				writeOut(itemset, pxTuples.sumIutils);
			}

			// if sumIutils+sumRutils >= minUtility, we create new local prefix tree
			// and call mHUIMiner
			if (pxTuples.sumIutils+pxTuples.sumRutils >= minUtility) {
				// ===== CREATE THE LOCAL TREE =====
				IHUPTreeMod localTree = createLocalTree(tree, itemID);
				MemoryLogger.getInstance().checkMemory();
				// NEXT BLOCK IS FOR DEBUGING:
				if (DEBUG) {
					System.out.println("Local tree headlist size: " + localTree.headerList.size());
					System.out.println("LOCAL TREE for projection by:"
							+ ((itemset == null) ? ""
									: Arrays.toString(itemset.toArray(new Integer[itemset.size()])) + ",")
							+ itemID + "\n" + localTree.toString());
				}

				// recursively call the mHUIMiner procedure to
				// explore other itemsets that are extensions of the current one
				if (localTree.headerList.size() > 0) {
					mHUIMiner(localTree, minUtility, itemset, pxTuples.uLists);
					MemoryLogger.getInstance().checkMemory();
				}
			} // end if
			itemset.remove(itemset.size() - 1);
		} // end for
	}


	private IHUPTreeMod createLocalTree(IHUPTreeMod tree, Integer itemID) {

		// It consists of the set of prefix paths
		List<List<Integer>> prefixPaths = new ArrayList<List<Integer>>();

		Node pathStart = tree.mapItemNodes.get(itemID);

		while (pathStart != null) {

			// if the path is not just the root node
			if (pathStart.parent.itemID != -1) {

				List<Integer> prefixPath = new ArrayList<Integer>();

				// add all the parents of this node to the current prefixPath
				Node parentnode = pathStart.parent;
				while (parentnode.itemID != -1) {
					prefixPath.add(parentnode.itemID);
					parentnode = parentnode.parent;
				}
				// add the prefixPath to the list of prefixPaths
				prefixPaths.add(prefixPath);
			}
			// We will look for the next prefixpath
			pathStart = pathStart.nodeLink;
		}

		// next block only for debugging
		if (DEBUG) {
			System.out.println("\n\n\nPREFIXPATHS:");
			for (List<Integer> prefixPath : prefixPaths) {
				for (Integer node : prefixPath) {
					System.out.println("    " + Integer.toString(node));
				}
				System.out.println("    --");
			}
		}

		// Create localTree
		IHUPTreeMod localTree = new IHUPTreeMod();

		// for each prefixpath ( partial transaction )
		for (List<Integer> prefixPath : prefixPaths) {
			// add partial transaction to local tree
			localTree.addLocalTransaction(prefixPath);
		}

		// create the local header table
		localTree.createHeaderList(mapItemToTWU);
		return localTree;
	}
	

	/**
	 * This method constructs the utility list of pX
	 * @param pUL :  the list of utilityTuples of prefix P.
	 * @param xUL : the list of utilityTuples of itemX
	 * @return the utility list of pxUL
	 */
	private UtilityList construct(List<UtilityTuple> pUL, List<UtilityTuple> xUL) {
		// create an empty utility list for pX
		UtilityList pxUL = new UtilityList();

		for (UtilityTuple ep : pUL) {
			// do a binary search to find element ex in xUL with ep.tid = ex.tid
			UtilityTuple ex = findElementWithTID(xUL, ep.getTid());
			if (ex == null) {
				continue;
			}
			UtilityTuple ePX = new UtilityTuple(ep.getTid(), ep.getIutils() + ex.getIutils(), ex.getRutils());
			// add the new UtilityTuple to the list pxUL
			pxUL.addTuple(ePX);
		}
		// return the utility list of pXY.
		return pxUL;
	}
	
	/**
	 * Do a binary search to find the UtilityTuple with a given tid in a list of utility tuples
	 * It assumes the list of tuples are ordered based on tid in ascending order
	 * @param ulist the list of utility tuples
	 * @param tid  the tid
	 * @return  the UtilityTuple or null if none has the tid.
	 */
	private UtilityTuple findElementWithTID(List<UtilityTuple> ulist, int tid) {
		int first = 0;
		int last = ulist.size() - 1;

		// the binary search
		while (first <= last) {
			int middle = (first + last) >>> 1; // divide by 2

			if (ulist.get(middle).getTid() < tid) {
				first = middle + 1;
			} else if (ulist.get(middle).getTid() > tid) {
				last = middle - 1;
			} else {
				return ulist.get(middle);
			}
		}
		return null;
	}

	
	/** 
	 * Write a HUI to the output file
	 * @param HUI
	 * @param utility
	 * @throws IOException
	 */
	private void writeOut(ArrayList<Integer> HUI, long utility) throws IOException {
		huiCount++; // increment the number of high utility itemsets found

		StringBuilder buffer = new StringBuilder();
		
		// Sor the HUI by ascending order (modification by Philippe)
		Collections.sort(HUI);

		for (int  i = 0; i < HUI.size(); i++) {
			buffer.append(HUI.get(i));
			buffer.append(' ');
		}
		buffer.append("#UTIL: ");
		buffer.append(utility);

		writer.write(buffer.toString());
		writer.newLine();
	}


	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  AlgoMHUIMiner v. 2.019 - STATS =============");
		System.out.println(" Total utility: " + totalUtility);
		System.out.println(" Minimum utility: " + minUtility);
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println(" Join count: "+ joinCount);
		System.out.println(" HUIs count : " + huiCount);
		System.out.println("===================================================");
	}
}