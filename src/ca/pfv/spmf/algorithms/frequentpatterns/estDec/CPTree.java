package ca.pfv.spmf.algorithms.frequentpatterns.estDec;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

/**
 * This is an implementation of a estDecPlusTree. <br/>
 * <br/>
 * 
 * This implementation was made by Azadeh Soltani <br/>
 * <br/>
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf). <br/>
 * <br/>
 * 
 * SPMF is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version. <br/>
 * <br/>
 * 
 * SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details. <br/>
 * <br/>
 * 
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @see Algo_estDecPlus
 * @see CPTreeNode
 * @author Azadeh Soltani
 */
public class CPTree {

	private double N; // |Dk|
	private double d; // decay rate
	private double delta; // delta threshold

	// number of frequent itemsets found by mining the CPTree
	int patternCount = 0;

	// Hashtable for storing frequent itemsets into memory
	// (used if result is saved to memory)
	Hashtable<int[], Double> patterns;

	// writer used if result is saved to file
	private BufferedWriter writer;

	// the minsup parameter
	private double minsup;
	// the minsig parameter
	private double minsig;
	// the minmerge parameter
	private double minmerg;

	// the tree root
	CPTreeNode root; 
	
	int[] itemsetBuffer = new int[500];

	/**
	 * Constructor
	 */
	CPTree(double decay, double mins,  double minSigValue, double deltaValue, double minMergeValue) {

		N = 0;
		minsup = mins;
		minsig = minSigValue;
		minmerg = minMergeValue;
		d = decay;
		delta = deltaValue;

		root = new CPTreeNode(); // null node
	}
	
	/**
	 * Set the decay rate
	 * @param b  decay base 
	 * @param h decay-base life
	 */
	void setDecayRate(double b, double h) {
		d = Math.pow(b, -1.0 / h);
	}


	/**
	 * Method for updating parameters (Phase 1: parameter updating phase)
	 */
	void updateParams() {
		// |Dk| = |Dk| + 1
		N = N + 1;
	}


	/********************************************************************
	 * Method for inserting new possible frequent itemsets to the tree based on
	 * the new transaction (Phase 3 : Insertion Phase).
	 * 
	 * @param transaction
	 *            the new transaction m
	 *********************************/

	void insertItemset(int[] transaction) {
		// create a new transaction
		List<Integer> transaction2 = new ArrayList<Integer>();
		
		// add each item from the given transaction that has enough support to
		// the new transaction
		for (int item : transaction) {
			CPTreeNode child = root.getChildWithID(item, -1);
			if (child == null)
				// if i does not belong to the tree, insert a node m
				// representing i into the tree
				root.children.add(new CPTreeNode(item, root, (short) -1, 1));
			else if (child.counter1 / (N) >= minsig) {
				transaction2.add(item);
			}
		}
		// insert the new transaction
		for (int ind = 0; ind < transaction2.size(); ++ind) {
			Integer item = transaction2.get(ind);
			CPTreeNode child = root.getChildWithID(item, -1);
			if (child != null) {
				itemsetBuffer[0]=item;
				insert_n_itemsets(child, (short) 0, transaction2, ind + 1, itemsetBuffer,1);
			}
		}
	}

	
	/********************************************************************
	 * Method for obtaining the count of an itemset while ignoring an item at a
	 * given position.
	 * 
	 * @param itemset
	 *            the itemset
	 * @param pos
	 *            the index of the item to be ignored in the itemset
	 ********************************************************************/
	double getCountOfItemset(int[] itemset) {
    
		CPTreeNode currentNode = root.getChildWithID(itemset[0], -1);
		int ind = 1;
		short parentInd = 0;
		int l = 1;
		int oldPInd;
		CPTreeNode parentNode = currentNode;
		while (true) {
			if (ind >= itemset.length)
				break;
			oldPInd = parentInd;
			parentInd = currentNode.getInnerIndexWithID(itemset[ind],
					parentNode, parentInd);
			if (parentInd != -1) {
				ind++;
				l++;
				continue;
			} else {
				currentNode = currentNode.getChildWithID(itemset[ind],
						oldPInd);
				if (currentNode != null) {
					parentNode = currentNode;
					parentInd = 0;
					l = 1;
					ind++;
				} else
					return 0;
			}
		}// while
		return currentNode.estimateMergeCount(l, currentNode.getLongestLevel());
	}

	/********************************************************************
	 * Method for estimating the count of n-itemset from its n-1 subsets
	 * 
	 * @param currentNode
	 *            , transaction, index
	 ********************************************************************/
	double estimateCount(int[] itemset,int length) {
		double min = Double.MAX_VALUE;
		// We will consider each subset of length n-1 of the itemset "itemset" to
		// find the minimum count of its n-1 subsets.
		// This is done by removing each item from "itemset" one at a time and checking
		// the count of the resulting itemset
		
		// We create a temporary array for storing any n-1 itemset
		int[] itemset2 = new int[length-1];
		for (int i = 0; i < length; ++i) {
			// copy all items except the i-th item to the temporary array
			System.arraycopy(itemset, 0, itemset2, 0, i);
			System.arraycopy(itemset, i+1, itemset2, i, length - i - 1);
			// get the count of the itemset without the i-th item
		    
			double c = getCountOfItemset(itemset2);
			if (c==0) return 0;
			if (c < min)
				min = c;
		}
		return min;
	}

	/********************************************************************
	 * Recursive method for inserting all itemsets corresponding to the a
	 * transaction
	 * 
	 * @param currentNode
	 *            a tree node
	 * @param PI
	 *            the inner parent index   
	 *            
	 * @param transaction
	 *            the transaction
	 * @param ind
	 *           the depth of the current node with respect to the root of the tree
	 * @param itemset
	 * @param length
	 *           the length of the itemset
	 * 
	 ********************************************************************/

	public void insert_n_itemsets(CPTreeNode currentNode, short PI,
			List<Integer> transaction, int ind, int[] itemset, int length) {
		// stop recursion
		if (ind >= transaction.size())
			return;

		int item = transaction.get(ind);
		itemsetBuffer[length] = item;
		// look if there is a node already in the est-Tree		
		short PI2;
		PI2 = currentNode.getInnerIndexWithID(item, currentNode, PI);

		if (PI2 != -1) {
			insert_n_itemsets(currentNode, PI2, transaction, ind + 1, itemset,length+1);
		} else {
			CPTreeNode child = currentNode.getChildWithID(item, PI);
			if (child != null)
				insert_n_itemsets(child, (short) 0, transaction, ind + 1, itemset,length+1);
			else {
				if (currentNode.counter1 / N >= minsig) {
					double c = estimateCount(itemsetBuffer,length+1);
					if (c / N >= minsig) {
						child = new CPTreeNode(item, currentNode, PI, c);
						currentNode.children.add(child);
						if (((currentNode.counter1 - child.counter2) / N) < delta
								&& (child.counter2 / N) > minmerg)
							merge(currentNode, child);
					}
				}
			}
		}

		insert_n_itemsets(currentNode, PI, transaction, ind + 1, itemset,length);
	}

	/********************************************************************
	 * Method for force pruning
	 * 
	 * @param root
	 *            t
	 ********************************************************************/
	void forcePruning(CPTreeNode currentNode) {
		for (int i = 0; i < currentNode.children.size(); ++i) {
			CPTreeNode node = currentNode.children.get(i);
			if (node.counter1 / N < minsig && currentNode.itemIDList != null) {
				currentNode.children.remove(i--);
			}else {
				forcePruning(node);
			}
		}
	}

	/********************************************************************
	 * Recursive method for finding frequent patterns.
	 * 
	 * @param root   root of the current subtree
	 * @param pattern    current pattern
	 * @throws IOException
	 ********************************************************************/
	void patternMining(CPTreeNode currentNode, int[] pattern) throws IOException {
		if (currentNode.itemIDList != null && currentNode.itemIDList.size() > 0) {
			
			// list of itemset of patterns corresponding to each itemIdList item
			List<int[]> itemsetList = new ArrayList<int[]>();
			
			// Create a new pattern by concatening
			// itemset "pattern" with the first item of itemIDList
			int[] concatenation = new int[pattern.length+1];
			System.arraycopy(pattern, 0, concatenation, 0, pattern.length);
			concatenation[pattern.length] = currentNode.itemIDList.get(0);
			itemsetList.add(concatenation);
			
			// Compute support of that pattern
			double s = currentNode.computeSupport(N, 1);
			
			// if the support is enough, save that pattern
			if (s >= minsup) {
				patternCount++;
				// if store into file
				if (patterns == null) {
					writeItemset(concatenation, s);
				} else {
					// else, store into memory
					patterns.put(concatenation, s);
				}
			}
			for (int i = 1; i < currentNode.itemIDList.size(); ++i) {
				
				int	PIn = currentNode.parents.get(i).pInd;
			    
				// create another pattern by concatenating the pattern
				// at position PIn with the i-th item of "itemIDList"
				
				int[] patternPIn = itemsetList.get(PIn);
				int[] concatenation2 = new int[patternPIn.length+1];
				System.arraycopy(patternPIn, 0, concatenation2, 0, patternPIn.length);
				concatenation2[patternPIn.length] = currentNode.itemIDList.get(i);
				itemsetList.add(concatenation2);
				
				// Compute support of that pattern
				s = currentNode.computeSupport(N, currentNode.getLevel(i));
				// If the support is enough, save that pattern
				if (s >= minsup) {
					patternCount++;
					// if store into file
					if (patterns == null) {
						writeItemset(concatenation2, s);
					} else {
						// else, store into memory
						patterns.put(concatenation2, s);
					}
				}// if s
			}// for i
			
			// Recursively mine using the child nodes of the current node
			for (CPTreeNode node : currentNode.children) {
				patternMining(node, itemsetList.get(node.parents.get(0).pInd));
			}
		}
	}

	/********************************************************************
	 * Method for finding frequent patterns and save them into memory
	 * 
	 * @param root
	 *            root of the current subtree
	 ********************************************************************/
	Hashtable<int[], Double> patternMining_saveToMemory()	throws IOException {
		// Initialize hashtable for storing frequent patterns into memory
		patterns = new Hashtable<int[], Double>();
		patternCount = 0;

		// recursive method for pattern mining
		for (CPTreeNode node : root.children)
			patternMining(node, new int[0]);

		return patterns; // return patterns found
	}

	/********************************************************************
	 * Method for finding frequent patterns and save them into file
	 * 
	 * @param root
	 *            the root of the curent subtree
	 * @param outputPath
	 *            the output file path
	 * @throws IOException
	 ********************************************************************/
	void patternMining_saveToFile(String outputPath) throws IOException {
		patterns = null; // because we will not save into memory
		writer = new BufferedWriter(new FileWriter(outputPath));
		patternCount = 0;

		// recursive method for pattern mining
		for (CPTreeNode node : root.children)
			patternMining(node, new int[0]);

		writer.close();
	}

	/********************************************************************
	 * Method for writing frequent patterns in output file
	 * 
	 * @param itemset
	 *            the pattern to be saved
	 * @param support
	 *            a double value
	 ********************************************************************/
	void writeItemset(int[] itemset, double support) throws IOException {
		StringBuilder buffer = new StringBuilder();

		// for each item
		for (Integer item : itemset) {
			// write the item
			buffer.append(item);
			buffer.append(" ");
		}
		// write the support
		buffer.append("#SUP: ");
		buffer.append(support);
		writer.write(buffer.toString());
		writer.newLine();
	}

	/**
	 * merge two parameter nodes
	 * 
	 * @param mp
	 *            the parent node
	 * @param m
	 *            the child node
	 */
	public void merge(CPTreeNode mp, CPTreeNode m) {

//		System.out.println("MERGE");
		int l = mp.itemIDList.size();
		mp.itemIDList.addAll(m.itemIDList);
		mp.parents.add(m.parents.get(0));
		
		for (int j = 1; j < m.parents.size(); ++j) {
			mp.parents.add(new ParentNode(mp, (short) (l + m.parents.get(j).pInd)));
		}
		for (CPTreeNode mc : m.children) {
			ParentNode p = mc.parents.get(0);
			p.pNode = mp;
			p.pInd = (short) (l + p.pInd);
			mc.parents.set(0, p);
			mp.children.add(mc);
		}
		if (mp.counter2>m.counter2)
			  mp.counter2=m.counter2;
		mp.children.remove(m);
		
	}

	/**
	 * split the node mf
	 * 
	 * * @param m a node which should be split
	 */
	public void split(CPTreeNode m) {
		int longestLevel = m.getLongestLevel();
//		int l = m.itemIDList.size();
		for (int j = 1; j < m.itemIDList.size(); ++j)
			if (m.isLeafLevel(j)) {
				CPTreeNode m2 = new CPTreeNode();
				m2.itemIDList.add(m.itemIDList.get(j));
				m2.parents.add(m.parents.get(j));
				m.itemIDList.set(j, null);
				m2.counter1 = m.estimateMergeCount(m.getLevel(j), longestLevel);
				m2.counter2 = m2.counter1;
				for (int k = m.children.size()-1; k >= 0; --k) {
					CPTreeNode mc = m.children.get(k);
					if (mc.parents.get(0).pInd == j) {
						// make mc be a child node of m2
						mc.parents.set(0, new ParentNode(m2, (short) 0));
						m.children.remove(mc);
						m2.children.add(mc);
					}
				}
				// make m2 be a child node of m
				m.children.add(m2);
			}
		for (int k = m.itemIDList.size()-1; k >= 0 ; --k)
			
			if (m.itemIDList.get(k) == null) {
				
				m.itemIDList.remove(k);
			    m.parents.remove(k);
			      
				for (int y = 1; y < m.parents.size(); ++y) {
					ParentNode x = m.parents.get(y);
					if (x.pInd >k) {
						x.pInd--;
						m.parents.set(y, x);
					}
				}// for
				
				for (CPTreeNode mx : m.children) {
					ParentNode x = mx.parents.get(0);
					if (x.pInd > k) {
						x.pInd--;
						mx.parents.set(0, x);
					}
				}// for
				
			}// if
		int newLongestLevel = m.getLongestLevel();
		m.counter2 = m.estimateMergeCount(newLongestLevel, longestLevel);
	}

	/**
	 * 
	 */
	public void traverse(CPTreeNode m, CPTreeNode mp, int q, int[] transaction) {
		if (q != -1 && m.parents.get(0).pInd != q  &&  m.parents.get(0).pNode != mp)
			return;

		if (Arrays.binarySearch(transaction, m.itemIDList.get(0)) < 0)
			return;

		m.update(d);
		if (m.counter1 / N < minsig ) {
			mp.children.remove(m);
			return;
		} 
		else {
			
		List<Integer> leafCommonItemInds = new ArrayList<Integer>();
		List<Integer> levelParents = new ArrayList<Integer>();
	    int i = 1; // level
		if (m.isLeafLevel(0))
			leafCommonItemInds.add(0);
		else{
		    levelParents.add(0);
		    while (true) {
			    levelParents=FindLevelCommonItems(m, levelParents, 
					leafCommonItemInds, transaction);
			    if (levelParents.size() != 0)
			    	i++;
			    else
			    	break;
		    }//while
		}//else
		if (i==m.getLongestLevel())
			m.counter2 = m.counter2 * d + 1;
		if ((mp.counter1 - m.counter2) / N < delta
				&& m.counter2 / N >= minmerg) {
			if (mp != root )
				merge(mp, m);
		} else if ((m.counter1 - m.counter2) / N > delta
				&& m.counter2 / N >= minmerg && m.itemIDList.size() > 1)
			split(m);
		for (int j : leafCommonItemInds) {
		for (int f = 0; f < m.children.size(); ++f) {
				CPTreeNode mc = m.children.get(f);
				traverse(mc, m, j, transaction);
			}
		}
	}// else

	}

	/**
	 * 
	 */
	List<Integer> FindLevelCommonItems(CPTreeNode m,	List<Integer> levelParents,
			List<Integer> leafCommonItemInds, int[] transaction) {
        ArrayList<Integer> newParents = new ArrayList<Integer>();
		for (int k = levelParents.get(0)+1; k < m.itemIDList.size(); ++k) {
			if (Arrays.binarySearch(transaction, m.itemIDList.get(k)) >= 0) {
				int pInd=m.parents.get(k).pInd;
				if (levelParents.contains(pInd)){
					newParents.add(k);
					if (m.isLeafLevel(k))
						leafCommonItemInds.add(k);
				} else
					break;
			}
		}// for
		return newParents;
	}
	
	
	@Override
	/**
	 * Method for getting a string representation of the CP-tree 
	 * (to be used for debugging purposes).
	 * @return a string
	 */
	public String toString() {
		return root.toString("");
	}
	
	int nodeCount(CPTreeNode currentNode)	{
		int s=1;
		for (CPTreeNode child: currentNode.children)
			s+= nodeCount(child);
		return s;
	}
}// class
