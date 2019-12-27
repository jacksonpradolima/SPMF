package ca.pfv.spmf.algorithms.frequentpatterns.ihaupm;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/* This file is copyright (c) 2008-2019 Shi-Feng Ren
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
 *
 */

/**
 * The IHAUPM algorithm for incremental average utility mining
 */
public class IHAUPM {
	// reference to HAUP-tree
	IAUTree tree = null;
	// profit for each item in db
	Map<String, Integer> item2profits;
	// keep auub value of each pair of co-occurrence items in db
	Map<String, Map<String, Long>> EUCS = null;
	public int numOfNodes = 0;

	public IHAUPM(IAUTree DBTree) {
		tree = DBTree;
		item2profits = DBTree.item2profits;
		EUCS = DBTree.EUCS;
	}

	/**
	 * Discover all itemsets from HAUP-tree
	 * 
	 * @param outfile
	 * @return
	 * @throws IOException
	 */
	public int[] mine(String outfile) throws IOException {
		BufferedWriter out = null;
		if (outfile != null) {
			out = new BufferedWriter(new FileWriter(outfile));
		}
		int candidateNum = 0;
		int minedItemsetNum = 0;
		// discovered itemsets
		Map<String, Double> result = new HashMap<>();
		Iterator<Entry<String, Double>> it = null;
		List<TableNode> headTableList = tree.headTable;
		// BufferedWriter writeCand = new BufferedWriter(new
		// FileWriter("./candidates.txt"));
		for (int i = headTableList.size() - 1; i >= 0; i--) {
			TableNode p = headTableList.get(i);
			IAUNode s = p.hlink;
			result.clear();

			// Merge the same itemsets (Maybe they have didfference utilities)
			List<List<IAUNode.IAUPair>> arys = collectIntoList(s);
			combine(arys); // Optimiztion

			for (List<IAUNode.IAUPair> quanAry : arys) {
				List<IAUNode.IAUPair> sublist = quanAry.subList(0,
						quanAry.size() - 1);
				getCombination(sublist, result, quanAry.get(quanAry.size() - 1));
			}

			// The conanical form!
			while (s != null) {
				List<IAUNode.IAUPair> quanAry = s.getQuanAry();
				List<IAUNode.IAUPair> sublist = quanAry.subList(0,
						quanAry.size() - 1);
				getCombination(sublist, result, quanAry.get(quanAry.size() - 1));
				s = s.getRight();
			}

			it = result.entrySet().iterator();
			candidateNum += result.size();
			while (it.hasNext()) {
				Entry<String, Double> entry = it.next();
				String line = entry.getKey();
				// System.out.println(line+":"+entry.getValue());
				// writeCand.write(line);
				double averageUtility = entry.getValue()
						/ line.split(",").length;
				line += " #AUTIL: " + averageUtility;
				if (averageUtility >= tree.minautil) {
					if (out != null)
						out.write(line + "\n");
					++minedItemsetNum;
				}
			}
		}
		// writeCand.close();
		if (out != null)
			out.close();
		return new int[] { candidateNum, minedItemsetNum };

	}

	/**
	 * Generate new itemsets which are kept in `stack` structure The type of its
	 * elements is StackElement
	 * 
	 * @param quanAry
	 *            , quanity array of node
	 * @param result
	 *            , discovered itemsets
	 * @param curNode
	 *            , current processing node
	 */
	private void getCombination(List<IAUNode.IAUPair> quanAry,
			Map<String, Double> result, IAUNode.IAUPair curNode) {
		List<StackElement> stack = new ArrayList<>();
		StackElement element = new StackElement();
		element.name = curNode.name;
		element.utility = curNode.quan * item2profits.get(curNode.name);
		element.location = quanAry.size();
		stack.add(element);
		while (stack.size() != 0) {
			StackElement ele = stack.remove(stack.size() - 1);
			if (!result.containsKey(ele.name)) {
				result.put(ele.name, ele.utility);
			} else {
				double u = result.get(ele.name);
				u += ele.utility;
				result.put(ele.name, u);
			}
			String[] names = ele.name.split(",");
			Loop: for (int k = ele.location - 1; k >= 0; k--) {
				IAUNode.IAUPair pair = quanAry.get(k);
				// Use EUCS strcture to avoid unnecessary combination.
				for (int i = 0; i < names.length; i++) {
					String name1 = pair.name;
					String name2 = names[i];
					if (!isCombine(name1, name2))
						continue Loop;
				}
				StackElement tmp = new StackElement();
				tmp.name = ele.name + "," + pair.name;
				tmp.utility = ele.utility + pair.quan
						* item2profits.get(pair.name);
				tmp.location = k;
				stack.add(tmp);
			}
		}
	}

	/**
	 * Test if it is necessary to combine two itemset for generating new
	 * itemsets
	 * 
	 * @param name1
	 *            , one itemset
	 * @param name2
	 *            , another itemset
	 * @return Ture if then can combine to generate new itemsets, Or False.
	 */
	boolean isCombine(String name1, String name2) {
		Map<String, Long> subEUCS = EUCS.get(name1);
		if (subEUCS != null) {
			Long val = subEUCS.get(name2);
			if (val == null) {
				subEUCS = EUCS.get(name2);
				if (subEUCS == null)
					return false;
				else {
					Long oval = subEUCS.get(name1);
					if (oval == null || oval < tree.minautil) {
						return false;
					}
				}
			} else if (val < tree.minautil) {
				return false;
			}
		} else {
			subEUCS = EUCS.get(name2);
			if (subEUCS != null) {
				Long val = subEUCS.get(name1);
				if (val == null) {
					subEUCS = EUCS.get(name1);
					if (subEUCS == null)
						return false;
					else {
						Long oval = subEUCS.get(name2);
						if (oval == null || oval < tree.minautil) {
							return false;
						}
					}
				} else if (val < tree.minautil) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Merge quantity array
	 * 
	 * @param arys
	 *            , quantity arrays in the current hlink
	 */
	void combine(List<List<IAUNode.IAUPair>> arys) {
		Collections.sort(arys, new Comparator<List<IAUNode.IAUPair>>() {
			@Override
			public int compare(List<IAUNode.IAUPair> a, List<IAUNode.IAUPair> b) {
				int cmp = a.size() - b.size();
				if (cmp == 0) {
					for (int i = 0; i < a.size(); i++) {
						IAUNode.IAUPair aval = a.get(i);
						IAUNode.IAUPair bval = b.get(i);
						int namecmp = aval.name.compareTo(bval.name);
						if (namecmp != 0)
							return namecmp;
					}
					return 0;
				} else
					return cmp;
			}
		});
		for (int i = 1; i < arys.size(); i++) {
			List<IAUNode.IAUPair> a = arys.get(i - 1);
			List<IAUNode.IAUPair> b = arys.get(i);
			if (isEquals(a, b)) {
				for (int j = 0; j < a.size(); j++) {
					IAUNode.IAUPair aval = a.get(j);
					aval.quan += b.get(j).quan;
				}
				arys.remove(i);
				i--;
			}
		}
	}

	/**
	 * Test if two quantity array pointed by two node is equal.
	 * 
	 * @param a
	 *            , quantity array of node
	 * @param b
	 *            , quantity array of another node
	 * @return True if they are equals, or False
	 */
	boolean isEquals(List<IAUNode.IAUPair> a, List<IAUNode.IAUPair> b) {
		if (a.size() != b.size())
			return false;
		for (int i = 0; i < a.size(); i++) {
			IAUNode.IAUPair aval = a.get(i);
			IAUNode.IAUPair bval = b.get(i);
			if (!aval.name.equals(bval.name)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Kepp the quantity array in hlink into ArrayList
	 * 
	 * @param s
	 *            , a node
	 * @return ArrayList instance which keep the quantity array in hlink pointed
	 *         by `s`
	 */
	List<List<IAUNode.IAUPair>> collectIntoList(IAUNode s) {
		List<List<IAUNode.IAUPair>> arys = new ArrayList<>();
		while (s != null) {
			List<IAUNode.IAUPair> quanAry = s.getQuanAry();
			List<IAUNode.IAUPair> sublist = quanAry.subList(0, quanAry.size());
			arys.add(sublist);
			s = s.getRight();
			numOfNodes++;
		}
		return arys;
	}

}
