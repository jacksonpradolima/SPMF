package ca.pfv.spmf.algorithms.frequentpatterns.fin_prepost;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import ca.pfv.spmf.tools.MemoryLogger;

/*
 * Copyright (c) 2008-2014 ZHIHONG DENG
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
 *
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * Java implementation of the FIN algorithm.
 * 
 * This implementation was obtained by converting the original C++ code of FIN
 * provided by ZHIHONG DENG, to Java.
 * 
 * The code is copyright by Zhihong Deng.
 * 
 * @author Philippe Fournier-Viger
 */
public class FIN {

	// the start time and end time of the last algorithm execution
	long startTimestamp;
	long endTimestamp;

	// number of itemsets found
	int outputCount = 0;

	// object to write the output file
	BufferedWriter writer = null;

	public int[][] bf;
	public int bf_cursor;
	public int bf_size;
	public int bf_col;
	public int bf_currentSize;

	public int numOfFItem; // Number of items
	public int minSupport; // minimum support
	public Item[] item; // list of items sorted by support

	// public FILE out;
	public int[] result; // the current itemset
	public int resultLen = 0; // the size of the current itemset
	public int resultCount = 0;
	public int nlLenSum = 0; // node list length of the current itemset

	// Tree stuff
	public PPCTreeNode ppcRoot;
	public NodeListTreeNode nlRoot;
	public int[] itemsetCount;

	public int[] nlistBegin;
	public int nlistCol;
	public int[] nlistLen;
	public int firstNlistBegin;
	public int PPCNodeCount;
	public int[] SupportDict;

	public int[] sameItems;
	public int nlNodeCount;

	/**
	 * Comparator to sort items by decreasing order of frequency
	 */
	static Comparator<Item> comp = new Comparator<Item>() {
		public int compare(Item a, Item b) {
			return ((Item) b).num - ((Item) a).num;
		}
	};

	private int numOfTrans;

	/**
	 * Run the algorithm
	 * 
	 * @param filename
	 *            the input file path
	 * @param minsup
	 *            the minsup threshold
	 * @param output
	 *            the output file path
	 * @throws IOException
	 *             if error while reading/writting to file
	 */
	public void runAlgorithm(String filename, double minsup, String output)
			throws IOException {

		ppcRoot = new PPCTreeNode();
		nlRoot = new NodeListTreeNode();
		nlNodeCount = 0;

		MemoryLogger.getInstance().reset();

		// create object for writing the output file
		writer = new BufferedWriter(new FileWriter(output));

		// record the start time
		startTimestamp = System.currentTimeMillis();

		bf_size = 1000000;
		bf = new int[100000][];
		bf_currentSize = bf_size * 10;
		bf[0] = new int[bf_currentSize];

		bf_cursor = 0;
		bf_col = 0;

		// ==========================
		// Read Dataset
		getData(filename, minsup);

		resultLen = 0;
		result = new int[numOfFItem];

		// Build tree
		buildTree(filename);

		nlRoot.label = numOfFItem;
		nlRoot.firstChild = null;
		nlRoot.next = null;

		// Initialize tree
		initializeTree();
		sameItems = new int[numOfFItem];

		int from_cursor = bf_cursor;
		int from_col = bf_col;
		int from_size = bf_currentSize;

		// Recursively traverse the tree
		NodeListTreeNode curNode = nlRoot.firstChild;
		NodeListTreeNode next = null;
		while (curNode != null) {
			next = curNode.next;
			// call the recursive "traverse" method
			traverse(curNode, nlRoot, 1, 0);
			for (int c = bf_col; c > from_col; c--) {
				bf[c] = null;
			}
			bf_col = from_col;
			bf_cursor = from_cursor;
			bf_currentSize = from_size;
			curNode = next;
		}
		writer.close();

		MemoryLogger.getInstance().checkMemory();

		// record the end time
		endTimestamp = System.currentTimeMillis();
	}

	/**
	 * Build the tree
	 * 
	 * @param filename
	 *            the input filename
	 * @throws IOException
	 *             if an exception while reading/writting to file
	 */
	void buildTree(String filename) throws IOException {

		PPCNodeCount = 0;
		ppcRoot.label = -1;

		// READ THE FILE
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line;

		// we will use a buffer to store each transaction that is read.
		Item[] transaction = new Item[1000];

		// for each line (transaction) until the end of the file
		while (((line = reader.readLine()) != null)) {
			// if the line is a comment, is empty or is a
			// kind of metadata
			if (line.isEmpty() == true || line.charAt(0) == '#'
					|| line.charAt(0) == '%' || line.charAt(0) == '@') {
				continue;
			}

			// split the line into items
			String[] lineSplited = line.split(" ");

			// for each item in the transaction
			int tLen = 0; // tLen
			for (String itemString : lineSplited) {
				// get the item
				int itemX = Integer.parseInt(itemString);

				// add each item from the transaction except infrequent item
				for (int j = 0; j < numOfFItem; j++) {
					// if the item appears in the list of frequent items, we add
					// it
					if (itemX == item[j].index) {
						transaction[tLen] = new Item();
						transaction[tLen].index = itemX; // the item
						transaction[tLen].num = 0 - j;
						tLen++;
						break;
					}
				}
			}

			// sort the transaction
			Arrays.sort(transaction, 0, tLen, comp);

			// Print the transaction
			// for(int j=0; j < tLen; j++){
			// System.out.print(" " + transaction[j].index + " ");
			// }
			// System.out.println();

			int curPos = 0;
			PPCTreeNode curRoot = (ppcRoot);
			PPCTreeNode rightSibling = null;
			while (curPos != tLen) {
				PPCTreeNode child = curRoot.firstChild;
				while (child != null) {
					if (child.label == 0 - transaction[curPos].num) {
						curPos++;
						child.count++;
						curRoot = child;
						break;
					}
					if (child.rightSibling == null) {
						rightSibling = child;
						child = null;
						break;
					}
					child = child.rightSibling;
				}
				if (child == null)
					break;
			}
			for (int j = curPos; j < tLen; j++) {
				PPCTreeNode ppcNode = new PPCTreeNode();
				ppcNode.label = 0 - transaction[j].num;
				if (rightSibling != null) {
					rightSibling.rightSibling = ppcNode;
					rightSibling = null;
				} else {
					curRoot.firstChild = ppcNode;
				}
				ppcNode.rightSibling = null;
				ppcNode.firstChild = null;
				ppcNode.father = curRoot;
				ppcNode.count = 1;
				curRoot = ppcNode;
				PPCNodeCount++;
			}
		}
		// close the input file
		reader.close();

		PPCTreeNode root = ppcRoot.firstChild;
		int pre = 0;
		itemsetCount = new int[(numOfFItem - 1) * numOfFItem / 2];
		nlistBegin = new int[(numOfFItem - 1) * numOfFItem / 2];
		nlistLen = new int[(numOfFItem - 1) * numOfFItem / 2];
		SupportDict = new int[PPCNodeCount + 1];
		while (root != null) {
			root.foreIndex = pre;
			SupportDict[pre] = root.count;
			pre++;
			PPCTreeNode temp = root.father;
			while (temp.label != -1) {
				itemsetCount[root.label * (root.label - 1) / 2 + temp.label] += root.count;
				nlistLen[root.label * (root.label - 1) / 2 + temp.label]++;
				temp = temp.father;
			}
			if (root.firstChild != null) {
				root = root.firstChild;
			} else {
				if (root.rightSibling != null) {
					root = root.rightSibling;
				} else {
					root = root.father;
					while (root != null) {
						if (root.rightSibling != null) {
							root = root.rightSibling;
							break;
						}
						root = root.father;
					}
				}
			}
		}

		// build 2-itemset nlist
		int sum = 0;
		for (int i = 0; i < (numOfFItem - 1) * numOfFItem / 2; i++) {
			if (itemsetCount[i] >= minSupport) {
				nlistBegin[i] = sum;
				sum += nlistLen[i];
			}
		}
		if (bf_cursor + sum > bf_currentSize * 0.85) {
			bf_col++;
			bf_cursor = 0;
			bf_currentSize = sum + 1000;
			bf[bf_col] = new int[bf_currentSize];
		}
		nlistCol = bf_col;
		firstNlistBegin = bf_cursor;
		root = ppcRoot.firstChild;
		bf_cursor += sum;
		while (root != null) {
			PPCTreeNode temp = root.father;
			while (temp.label != -1) {
				if (itemsetCount[root.label * (root.label - 1) / 2 + temp.label] >= minSupport) {
					int cursor = nlistBegin[root.label * (root.label - 1) / 2
							+ temp.label]
							+ firstNlistBegin;
					bf[nlistCol][cursor] = root.foreIndex;
					nlistBegin[root.label * (root.label - 1) / 2 + temp.label] += 1;
				}
				temp = temp.father;
			}
			if (root.firstChild != null) {
				root = root.firstChild;
			} else {
				if (root.rightSibling != null) {
					root = root.rightSibling;
				} else {
					root = root.father;
					while (root != null) {
						if (root.rightSibling != null) {
							root = root.rightSibling;
							break;
						}
						root = root.father;
					}
				}
			}
		}
		for (int i = 0; i < numOfFItem * (numOfFItem - 1) / 2; i++) {
			if (itemsetCount[i] >= minSupport) {
				nlistBegin[i] = nlistBegin[i] - nlistLen[i];
			}
		}

	}

	/**
	 * Initialize the tree
	 */
	void initializeTree() {

		NodeListTreeNode lastChild = null;
		for (int t = numOfFItem - 1; t >= 0; t--) {
			NodeListTreeNode nlNode = new NodeListTreeNode();
			nlNode.label = t;
			nlNode.support = 0;
			nlNode.NLStartinBf = bf_cursor;
			nlNode.NLLength = 0;
			nlNode.NLCol = bf_col;
			nlNode.firstChild = null;
			nlNode.next = null;
			nlNode.support = item[t].num;
			if (nlRoot.firstChild == null) {
				nlRoot.firstChild = nlNode;
				lastChild = nlNode;
			} else {
				lastChild.next = nlNode;
				lastChild = nlNode;
			}
		}
	}

	/**
	 * Read the input file to find the frequent items
	 * 
	 * @param filename
	 *            input file name
	 * @param minSupport
	 * @throws IOException
	 */
	void getData(String filename, double minSupport) throws IOException {
		numOfTrans = 0;

		// (1) Scan the database and count the support of each item.
		// The support of items is stored in map where
		// key = item value = support count
		Map<Integer, Integer> mapItemCount = new HashMap<Integer, Integer>();
		// scan the database
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line;
		// for each line (transaction) until the end of the file
		while (((line = reader.readLine()) != null)) {
			// if the line is a comment, is empty or is a
			// kind of metadata
			if (line.isEmpty() == true || line.charAt(0) == '#'
					|| line.charAt(0) == '%' || line.charAt(0) == '@') {
				continue;
			}

			numOfTrans++;

			// split the line into items
			String[] lineSplited = line.split(" ");
			// for each item in the transaction
			for (String itemString : lineSplited) {
				// increase the support count of the item by 1
				Integer item = Integer.parseInt(itemString);
				Integer count = mapItemCount.get(item);
				if (count == null) {
					mapItemCount.put(item, 1);
				} else {
					mapItemCount.put(item, ++count);
				}
			}

		}
		// close the input file
		reader.close();

		this.minSupport = (int)Math.ceil(minSupport * numOfTrans);

		numOfFItem = mapItemCount.size();

		Item[] tempItems = new Item[numOfFItem];
		int i = 0;
		for (Entry<Integer, Integer> entry : mapItemCount.entrySet()) {
			if (entry.getValue() >= minSupport) {
				tempItems[i] = new Item();
				tempItems[i].index = entry.getKey();
				tempItems[i].num = entry.getValue();
				i++;
			}
		}

		item = new Item[i];
		System.arraycopy(tempItems, 0, item, 0, i);

		numOfFItem = item.length;

		Arrays.sort(item, comp);
	}

	NodeListTreeNode iskItemSetFreq(NodeListTreeNode ni, NodeListTreeNode nj,
			int level, NodeListTreeNode lastChild, IntegerByRef sameCountRef) {

		if (bf_cursor + ni.NLLength > bf_currentSize) {
			bf_col++;
			bf_cursor = 0;
			bf_currentSize = bf_size > ni.NLLength * 1000 ? bf_size
					: ni.NLLength * 1000;
			bf[bf_col] = new int[bf_currentSize];
		}

		NodeListTreeNode nlNode = new NodeListTreeNode();
		nlNode.support = 0;
		nlNode.NLStartinBf = bf_cursor;
		nlNode.NLCol = bf_col;
		nlNode.NLLength = 0;

		int cursor_i = ni.NLStartinBf;
		int cursor_j = nj.NLStartinBf;
		int col_i = ni.NLCol;
		int col_j = nj.NLCol;

		while (cursor_i < ni.NLStartinBf + ni.NLLength
				&& cursor_j < nj.NLStartinBf + nj.NLLength) {
			if (bf[col_i][cursor_i] == bf[col_j][cursor_j]) {
				bf[bf_col][bf_cursor++] = bf[col_j][cursor_j];
				nlNode.NLLength++;
				nlNode.support += SupportDict[bf[col_i][cursor_i]];
				cursor_i += 1;
				cursor_j += 1;
			} else if (bf[col_i][cursor_i] < bf[col_j][cursor_j]) {
				cursor_i += 1;
			} else {
				cursor_j += 1;
			}
		}
		if (nlNode.support >= minSupport) {
			if (ni.support == nlNode.support) {
				sameItems[sameCountRef.count++] = nj.label;
			} else {
				nlNode.label = nj.label;
				nlNode.firstChild = null;
				nlNode.next = null;
				if (ni.firstChild == null) {
					ni.firstChild = nlNode;
					lastChild = nlNode;
				} else {
					lastChild.next = nlNode;
					lastChild = nlNode;
				}
			}
			return lastChild;
		} else {
			bf_cursor = nlNode.NLStartinBf;
		}
		return lastChild;
	}

	/**
	 * Recursively traverse the tree to find frequent itemsets
	 * @param curNode
	 * @param curRoot
	 * @param level
	 * @param sameCount
	 * @throws IOException if error while writing itemsets to file
	 */
	public void traverse(NodeListTreeNode curNode, NodeListTreeNode curRoot,
			int level, int sameCount) throws IOException {

		MemoryLogger.getInstance().checkMemory();

//		System.out.println("==== traverse(): " + curNode.label + " " + level
//				+ " " + sameCount);
		NodeListTreeNode sibling = curNode.next;
		NodeListTreeNode lastChild = null;
		while (sibling != null) {
			if ((level == 1 && itemsetCount[(curNode.label - 1) * curNode.label
					/ 2 + sibling.label] >= minSupport)) {
				IntegerByRef sameCountTemp = new IntegerByRef();
				sameCountTemp.count = sameCount;
				lastChild = is2_itemSetValid(curNode, sibling, level,
						lastChild, sameCountTemp);
				sameCount = sameCountTemp.count;
			} else if (level > 1) {
				IntegerByRef sameCountTemp = new IntegerByRef();
				sameCountTemp.count = sameCount;
				lastChild = iskItemSetFreq(curNode, sibling, level, lastChild,
						sameCountTemp);
				sameCount = sameCountTemp.count;
			}
			sibling = sibling.next;
		}
		resultCount += Math.pow(2.0, sameCount);
		nlLenSum += Math.pow(2.0, sameCount) * curNode.NLLength;

		result[resultLen++] = curNode.label;

		// ============= Write itemset(s) to file ===========
		writeItemsetsToFile(curNode, sameCount);

		// ======== end of write to file

		nlNodeCount++;

		int from_cursor = bf_cursor;
		int from_col = bf_col;
		int from_size = bf_currentSize;
		NodeListTreeNode child = curNode.firstChild;
		NodeListTreeNode next = null;
		while (child != null) {
			next = child.next;
			traverse(child, curNode, level + 1, sameCount);
			for (int c = bf_col; c > from_col; c--) {
				bf[c] = null;
			}
			bf_col = from_col;
			bf_cursor = from_cursor;
			bf_currentSize = from_size;
			child = next;
		}
		resultLen--;
	}

	NodeListTreeNode is2_itemSetValid(NodeListTreeNode ni, NodeListTreeNode nj,
			int level, NodeListTreeNode lastChild, IntegerByRef sameCount) {
		int i = ni.label;
		int j = nj.label;
		if (ni.support == itemsetCount[(i - 1) * i / 2 + j]) {
			sameItems[sameCount.count++] = nj.label;
		} else {
			NodeListTreeNode nlNode = new NodeListTreeNode();
			nlNode.label = j;
			nlNode.NLCol = nlistCol;
			nlNode.NLStartinBf = nlistBegin[(i - 1) * i / 2 + j];
			nlNode.NLLength = nlistLen[(i - 1) * i / 2 + j];
			nlNode.support = itemsetCount[(i - 1) * i / 2 + j];
			nlNode.firstChild = null;
			nlNode.next = null;
			if (ni.firstChild == null) {
				ni.firstChild = nlNode;
				lastChild = nlNode;
			} else {
				lastChild.next = nlNode;
				lastChild = nlNode;
			}
		}
		return lastChild;
	}

	/**
	 * This method write an itemset to file + all itemsets that can be made
	 * using its node list.
	 * 
	 * @param curNode
	 *            the current node
	 * @param sameCount
	 *            the same count
	 * @throws IOException
	 *             exception if error reading/writting to file
	 */
	private void writeItemsetsToFile(NodeListTreeNode curNode, int sameCount)
			throws IOException {

		// create a stringuffer
		StringBuilder buffer = new StringBuilder();
		if(curNode.support >= minSupport) {
			outputCount++;
			// append items from the itemset to the StringBuilder
			for (int i = 0; i < resultLen; i++) {
				buffer.append(item[result[i]].index);
				buffer.append(' ');
			}
			// append the support of the itemset
			buffer.append("#SUP: ");
			buffer.append(curNode.support);
			buffer.append("\n");
		}

		// === Write all combination that can be made using the node list of
		// this itemset
		if (sameCount > 0) {
			// generate all subsets of the node list except the empty set
			for (long i = 1, max = 1 << sameCount; i < max; i++) {
				for (int k = 0; k < resultLen; k++) {
					buffer.append(item[result[k]].index);
					buffer.append(' ');
				}

				// we create a new subset
				for (int j = 0; j < sameCount; j++) {
					// check if the j bit is set to 1
					int isSet = (int) i & (1 << j);
					if (isSet > 0) {
						// if yes, add it to the set
						buffer.append(item[sameItems[j]].index);
						buffer.append(' ');
						// newSet.add(item[sameItems[j]].index);
					}
				}
				buffer.append("#SUP: ");
				buffer.append(curNode.support);
				buffer.append("\n");
				outputCount++;
			}
		}
		// write the strinbuffer to file and create a new line
		// so that we are ready for writing the next itemset.
		writer.write(buffer.toString());
	}
	

	/**
	 * Print statistics about the latest execution of the algorithm to
	 * System.out.
	 */
	public void printStats() {
		System.out.println("========== FIN - STATS ============");
		System.out.println(" Minsup = " + minSupport
				+ "\n Number of transactions: " + numOfTrans);
		System.out.println(" Number of frequent  itemsets: " + outputCount);
		System.out.println(" Total time ~: " + (endTimestamp - startTimestamp)
				+ " ms");
		System.out.println(" Max memory:"
				+ MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("=====================================");
	}

	/** Class to pass an integer by reference as in C++
	 */
	class IntegerByRef {
		int count;
	}

	class Item {
		public int index;
		public int num;
	}

	class NodeListTreeNode {
		public int label;
		public NodeListTreeNode firstChild;
		public NodeListTreeNode next;
		public int support;
		public int NLStartinBf;
		public int NLLength;
		public int NLCol;
	}

	class PPCTreeNode {
		public int label;
		public PPCTreeNode firstChild;
		public PPCTreeNode rightSibling;
		public PPCTreeNode father;
		public int count;
		public int foreIndex;
	}
}
