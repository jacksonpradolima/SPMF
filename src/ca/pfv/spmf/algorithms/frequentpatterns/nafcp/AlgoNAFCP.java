package ca.pfv.spmf.algorithms.frequentpatterns.nafcp;

import ca.pfv.spmf.tools.MemoryLogger;

/*
 ** The implementation of the "NAFCP algorithm", the algorithm presented in:
 * "Le, Tuong; Vo, Bay. An N-list-based algorithm for mining frequent closed patterns. Expert Systems with Applications, 2015, 42.19: 6648-6657.‏"
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
 * This implementation was obtained by converting the C# code of the NAFCP algorithm to Java.
 * The C# code of this algorithm was provided by Dr. "Le Tuong", the first author of the above paper.
 * <p>
 *
 * @author Nader Aryabarzan (Copyright 2019)
 * @Email aryabarzan@aut.ac.ir or aryabarzan@gmail.com
 */

import java.io.*;
import java.util.*;

/*
 ** The implementation of the "nafcp" was done by Nader Aryabarzan et al.
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
 * This implementation was done by Nader Aryabarzan by converting the original
 * C# code of the negFIN algorithm to Java. The NAFCP algorithm is described in
 * this paper <br/>
 * Tuong Le, Bay Vo: An N-list-based algorithm for mining frequent closed
 * patterns. Expert Syst. Appl. 42(19): 6648-6657 (2015)
 * 
 * @author Nader Aryabarzan (Copyright 2019)
 * @Email aryabarzan@aut.ac.ir or aryabarzan@gmail.com
 */

public class AlgoNAFCP {
	int pre;
	int post;

	/** Minimum support threshold */
	int minSupport;

	/** List of frequent closed items */
	List<FCI> fcis_1;

	/** List of frequent closed itemsets */
	List<FCI> fcis;

	/** Number of transactions */
	int numOfTrans;

	/** Total number of frequent closed itemsets */
	int outputCount;

	Map<Integer, Integer> hashI1;
	Map<Integer, List<Integer>> hashFCIs;

	/** start time of the last algorithm execution */
	long startTimestamp;

	/** end time of the last algorithm execution */
	long endTimestamp;

	/** object to write the output file **/
	BufferedWriter writer = null;

	/** Constructor */
	public AlgoNAFCP() {

	}

	/** Read the input file */
	ProductDb readFile(String filename) throws IOException {
		ProductDb pDb = new ProductDb();

		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line;

		int i = 0;
		while (((line = reader.readLine()) != null)) {
			// if the line is a comment, is empty or is a
			// kind of metadata
			if (line.isEmpty() == true || line.charAt(0) == '#' || line.charAt(0) == '%' || line.charAt(0) == '@') {
				continue;
			}

			Product p = new Product();
			p.pID = ++i;
			// split the line into items
			String[] lineSplited = line.split(" ");

			// for each item in the transaction
			for (String itemString : lineSplited) {
				Item item = new Item();
				item.name = Integer.parseInt(itemString);
				p.items.add(item);
			}
			pDb.products.add(p);
		}
		// close the input file
		reader.close();

		return pDb;
	}

	/**
	 * Insert a product in the tree
	 * 
	 * @param p    product
	 * @param root the tree root
	 */
	void insertTree(Product p, WPPC_Node root) {
		while (p.items.size() > 0) {
			Item i = p.items.get(0);
			p.items.remove(0);

			boolean flag = false;

			WPPC_Node N = new WPPC_Node();

			for (int j = 0; j < root.childNodes.size(); j++) {
				if (root.childNodes.get(j).item.name == i.name) {
					root.childNodes.get(j).item.frequency++;
					N = root.childNodes.get(j);
					flag = true;
					break;
				}
			}
			if (flag == false) {
				N.item = i;
				N.item.frequency = 1;
				root.childNodes.add(N);
			}
			insertTree(p, N);
		}
	}

	/**
	 * Generate order
	 * 
	 * @param root the root of a tree
	 */
	void generateOrder(WPPC_Node root) {
		root.preOrder = pre++;
		for (int i = 0; i < root.childNodes.size(); i++)
			generateOrder(root.childNodes.get(i));
		root.postOrder = post++;
	}

	/**
	 * Generate NC sets
	 * 
	 * @param root the root of a tree
	 */
	void generateNCSets(WPPC_Node root) {
		if (root.item.name != -1) {
			// thuat toan cua Deng dung 2 vòng for
			int stt = hashI1.get(root.item.name);
			NC nc = new NC();
			nc.postOrder = root.postOrder;
			nc.preOrder = root.preOrder;
			nc.frequency = root.item.frequency;

			fcis_1.get(stt).nCs.add(nc);
		}

		for (int i = 0; i < root.childNodes.size(); i++)
			generateNCSets(root.childNodes.get(i));
	}

	/**
	 * Check the N list
	 * 
	 * @param a
	 * @param b
	 * @return true or false
	 */
	boolean N_list_check(List<NC> a, List<NC> b) {
		int i = 0;
		int j = 0;
		while (j < b.size() && i < a.size()) {
			// tất cả phần tử trong b đều là con của 1 phần tử của a thì a nằm trong subsume
			// của b
			NC aI = a.get(i);
			NC bJ = b.get(j);
			if (aI.preOrder < bJ.preOrder && aI.postOrder > bJ.postOrder)
				j++;
			else
				i++;
		}
		if (j == b.size())
			return true;
		return false;
	}

	/**
	 * Perform the union of two list of items
	 * 
	 * @param a a list
	 * @param b another list
	 * @return the union
	 */
	List<Integer> itemUnion(List<Integer> a, List<Integer> b) {
		List<Integer> result = new ArrayList<>();

		int i = 0;
		int j = 0;
		while (i < a.size() && j < b.size()) {
			int aI = a.get(i);
			int bJ = b.get(j);
			if (aI > bJ) {
				result.add(aI);
				i++;
			} else if (aI == bJ) {
				result.add(aI);
				i++;
				j++;
			} else {
				result.add(bJ);
				j++;
			}
		}

		while (i < a.size()) {
			result.add(a.get(i));
			i++;
		}
		while (j < b.size()) {
			result.add(b.get(j));
			j++;
		}
		return result;
	}

	/**
	 * Perform combinations
	 * 
	 * @param a
	 * @param b
	 * @param totalFrequency
	 * @param g
	 * @return
	 */
	// C#: ref int g
	List<NC> ncCombination(List<NC> a, List<NC> b, int totalFrequency, IntegerByRef g) {
		List<NC> result = new ArrayList<>();

		// thuat toan co do phuc tap n+m

		int i = 0;
		int j = 0;
		int subFrequency = totalFrequency;

		while (i < a.size() && j < b.size()) {
			NC aI = a.get(i);
			NC bJ = b.get(j);
			if (aI.preOrder < bJ.preOrder) {
				if (aI.postOrder > bJ.postOrder) {
					if (result.size() > 0 && result.get(result.size() - 1).preOrder == aI.preOrder)
						result.get(result.size() - 1).frequency += bJ.frequency;
					else {
						NC temp = new NC();
						temp.postOrder = aI.postOrder;
						temp.preOrder = aI.preOrder;
						temp.frequency = bJ.frequency;
						result.add(temp);
					}
					g.value = g.value + bJ.frequency;
					j++;
				} else {
					subFrequency -= aI.frequency;
					i++;
				}
			} else {
				subFrequency -= bJ.frequency;
				j++;
			}
			if (subFrequency < minSupport)
				return null;
		}

		return result;
	}

	/**
	 * Check if an itemset "a" is a subset of another itemset "b"
	 * 
	 * @param a an itemset
	 * @param b an itemset
	 * @return true if it is a subset. Otherwise false
	 */
	boolean subsetCheck(List<Integer> a, List<Integer> b) {
		if (a.size() > b.size())
			return false;

		int i = 0;
		int j = 0;
		while (i < a.size() && j < b.size()) {
			int aI = a.get(i);
			int bJ = b.get(j);
			if (aI > bJ)
				return false;
			else if (aI == bJ) {
				i++;
				j++;
			} else
				j++;
		}
		if (i < a.size())
			return false;
		else
			return true;
	}

	/**
	 * Check if an itemset f is subsumed by a closed itemset
	 * 
	 * @param f the itemset
	 * @return true if subsumed.
	 */
	boolean subsumptionCheck(FCI f) {
		List<Integer> arr = (List<Integer>) hashFCIs.get(f.frequency);
		if (arr != null) {

			for (int i = 0; i < arr.size(); i++) {
				if (subsetCheck(f.items, fcis.get(arr.get(i)).items) == true)
					return true;
			}
		}
		return false;
	}

	/**
	 * The main recursive method to find the frequent closed itemsets
	 * 
	 * @param Is    a list of FCIs
	 * @param level the current level
	 * @throws IOException if error writing or reading files
	 */
	void findFCIs(List<FCI> Is, int level) throws IOException {
		for (int i = Is.size() - 1; i >= 0; i--) {
			FCI IsI = Is.get(i);
			List<FCI> FCIs_Next = new ArrayList<>();
			for (int j = i - 1; j >= 0; j--) {

				FCI IsJ = Is.get(j);
				if (N_list_check(IsJ.nCs, IsI.nCs) == true) {
					if (IsI.frequency == IsJ.frequency) {
						// bằng
						IsI.items = itemUnion(IsI.items, IsJ.items);
						Is.remove(j);
						i--;
					} else {
						IsI.items = itemUnion(IsI.items, IsJ.items);
						for (int k = 0; k < FCIs_Next.size(); k++)
							FCIs_Next.get(k).items = itemUnion(FCIs_Next.get(k).items, IsJ.items);
					}
					// update Inext

					continue;
				}

				FCI f = new FCI();
				f.items = itemUnion(IsI.items, IsJ.items);
				IntegerByRef g = new IntegerByRef(0);
				f.nCs = ncCombination(IsJ.nCs, IsI.nCs, IsJ.frequency + IsI.frequency, g);

				if (g.value >= minSupport) {

					f.frequency = g.value;
					FCIs_Next.add(0, f);
				}
			} // j

			if (subsumptionCheck(IsI) == false) {
				fcis.add(IsI);
				writer.write(IsI.toString());
				writer.write("\n");

				if (hashFCIs.get(IsI.frequency) == null) {
					List<Integer> ar = new ArrayList<>();
					ar.add(fcis.size() - 1);
					hashFCIs.put(IsI.frequency, ar);
				} else {
					List<Integer> ar = (List<Integer>) hashFCIs.get(IsI.frequency);
					ar.add(fcis.size() - 1);
					hashFCIs.put(IsI.frequency, ar);
				}
			}

			findFCIs(FCIs_Next, level + 1);

		}
	}

	/**
	 * Read the input file to find the frequent items
	 *
	 * @param filename   input file name
	 * @param minSupport minimum support
	 * @throws IOException if error writing or reading from files
	 */
	void getData(String filename, double minSupport) throws IOException {
		numOfTrans = 0;

		// (1) Scan the database and count the count of each item.
		// The count of items is stored in map where
		// key = item value = count count
		Map<Integer, Integer> mapItemCount = new HashMap<Integer, Integer>();
		// scan the database
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line;
		// for each line (transaction) until the end of the file
		while (((line = reader.readLine()) != null)) {
			// if the line is a comment, is empty or is a
			// kind of metadata
			if (line.isEmpty() == true || line.charAt(0) == '#' || line.charAt(0) == '%' || line.charAt(0) == '@') {
				continue;
			}

			numOfTrans++;

			// split the line into items
			String[] lineSplited = line.split(" ");
			// for each item in the transaction
			for (String itemString : lineSplited) {
				// increase the count count of the item by 1
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

		this.minSupport = (int) Math.ceil(minSupport * numOfTrans);

		int numOfItems = mapItemCount.size();

		Item[] tempItems = new Item[numOfItems];
		int i = 0;
		for (Map.Entry<Integer, Integer> entry : mapItemCount.entrySet()) {
			if (entry.getValue() >= this.minSupport) {
				tempItems[i] = new Item();
				tempItems[i].name = entry.getKey();
				tempItems[i].frequency = entry.getValue();
				i++;
			}
		}

		Item[] item = new Item[i];
		System.arraycopy(tempItems, 0, item, 0, i);

	}

	/**
	 * Run the algorithm
	 *
	 * @param filename   the input file path
	 * @param minSupport the minsup minSupport
	 * @param output     the output file path
	 * @throws IOException if error while reading/writting to file
	 */
	public void runAlgorithm(String filename, double minSupport, String output) throws IOException {

		MemoryLogger.getInstance().reset();

		// create object for writing the output file
		writer = new BufferedWriter(new FileWriter(output));

		// record the start time
		startTimestamp = System.currentTimeMillis();

		// Initialize some variables
		fcis_1 = new ArrayList<>();
		fcis = new ArrayList<>();
		hashI1 = new HashMap<>();
		hashFCIs = new HashMap<>();
		pre = 0;
		post = 0;

		// ==========================
		ProductDb pDB = readFile(filename);
		numOfTrans = pDB.products.size();
		// caculate the minSupport
		this.minSupport = (int) Math.ceil(pDB.products.size() * minSupport);

		// for (int i = 0; i < pDB.products.size(); i++)
//        {
//            Product pi = pDB.products.get(i);
//            for (int j = pi.items.size() - 1; j >= 0; j--)
//            {
//                boolean flag = false;
//                for (int k = 0; k < fcis_1.size(); k++)
//                {
//                    if (pi.items.get(j).name == fcis_1.get(k).items.get(0)){
//                        flag = true;
//                        fcis_1.get(k).frequency++;
//                        break;
//                    }
//                }
//                if (flag == false)
//                {
//                    FCIs f = new FCIs();
//                    f.items.add(pi.items.get(j).name);
//                    f.frequency = 1;
//                    fcis_1.add(f);
//                }
//            }
//        }

		// devide the list of 1-Item: erasable and inerasable
//        for (int i = fcis_1.size() - 1; i >= 0; i--)
//        {
//            if (fcis_1.get(i).frequency < this.minSupport)
//            {
//                fcis_1.remove(i);
//            }
//        }

		// scan database (1) to caculate frequency of each Item
		// The count of items is stored in map where
		// key = item value = count count
		Map<Integer, Integer> mapItemCount = new HashMap<Integer, Integer>();
		for (int i = 0; i < pDB.products.size(); i++) {
			Product pi = pDB.products.get(i);
			for (int j = pi.items.size() - 1; j >= 0; j--) {
				// increase the count count of the item by 1
				Integer item = pi.items.get(j).name;
				Integer count = mapItemCount.get(item);
				if (count == null) {
					mapItemCount.put(item, 1);
				} else {
					mapItemCount.put(item, ++count);
				}
			}
		}

		int i = 0;
		for (Map.Entry<Integer, Integer> entry : mapItemCount.entrySet()) {
			if (entry.getValue() >= this.minSupport) {
				FCI f = new FCI();
				f.items.add(entry.getKey());
				f.frequency = entry.getValue();
				fcis_1.add(f);
				i++;
			}
		}

		// Sort 1-fcis list on Frequency
		Collections.sort(fcis_1, FCI.fc);

		// add the order 1-FCIs to Hashtable
		for (i = 0; i < fcis_1.size(); i++)
			hashI1.put(fcis_1.get(i).items.get(0), i);

		WPPC_Node root = new WPPC_Node();
		root.item.name = -1;

		// scan database (2): delete infrequent items
		for (i = 0; i < pDB.products.size(); i++) {
			Product pDBi = pDB.products.get(i);
			for (int l = pDBi.items.size() - 1; l >= 0; l--) {
				Item il = pDBi.items.get(l);
				if (hashI1.get(il.name) == null)
					pDBi.items.remove(l);
				else
					il.frequency = fcis_1.get(hashI1.get(il.name)).frequency;
			}
			// sort the items of product i on frequency
			pDBi.Sort();
			insertTree(pDBi, root);
		}
		pDB = null;

		// scan tree (1): generate preOrder and postOrder
		generateOrder(root);

		// scan tree (2) to generate N-list of 1-FCIs
		generateNCSets(root);

		findFCIs(fcis_1, 1);

		outputCount = fcis.size();
		// ==========================
		writer.close();

		MemoryLogger.getInstance().checkMemory();

		// record the end time
		endTimestamp = System.currentTimeMillis();
	}

	/**
	 * Print statistics about the latest execution of the algorithm to System.out.
	 */
	public void printStats() {
		System.out.println("========== NAFCP - STATS ============");
		System.out.println(" Minsup : " + this.minSupport);
		System.out.println(" Number of transactions: " + numOfTrans);
		System.out.println(" Number of frequent 1-items  : " + fcis_1.size());
		System.out.println(" Number of closed  itemsets: " + outputCount);
		System.out.println(" Total time ~: " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max memory:" + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("=====================================");
	}
}

//============================
/**
 * Class IntegerByRef to pass an integer by reference as in C#
 */
class IntegerByRef {
	int value;

	IntegerByRef(int _value) {
		value = _value;
	}
}

//============================
/** Class representing an item
 */
class Item {
	/** the item name */
	int name;
	/** the frequency */
	int frequency;

	static Comparator<Item> itemComparator = new Comparator<Item>() {
		public int compare(Item x, Item y) {
			{
				if (x.frequency > y.frequency)
					return -1;
				else if (x.frequency < y.frequency)
					return 1;
				else
					return x.name - y.name;
			}
		}
	};
}

//============================
/** Class representing a product */
class Product {
	int pID;
	List<Item> items;

	void Sort() {
		Collections.sort(items, Item.itemComparator);
	}

	Product() {
		pID = 0;
		items = new ArrayList<>();
	}

}

//============================
/** Class representing a product database */
class ProductDb {
	List<Product> products;

	ProductDb() {
		products = new ArrayList<>();
	}
}

//============================
/** Class NC */
class NC {
	int postOrder;
	int preOrder;
	int frequency;
}

//============================
/** Class representing a WPPC node */
class WPPC_Node {
	Item item;
	List<WPPC_Node> childNodes;
	int preOrder;
	int postOrder;

	public WPPC_Node() {
		item = new Item();
		childNodes = new ArrayList<>();
		preOrder = 0;
		postOrder = 0;
	}
}

//============================
/** Class representing a frequent closed itemset */
class FCI {
	List<Integer> items;
	int frequency;
	List<NC> nCs;

	public FCI() {
		items = new ArrayList<>();
		nCs = new ArrayList<>();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Integer item : items) {
			sb.append(item);
			sb.append(' ');
		}
		sb.append("#SUP: ");
		sb.append(this.frequency);
		return sb.toString();
	}

	static Comparator<FCI> fc = new Comparator<FCI>() {
		@Override
		public int compare(FCI x, FCI y) {
			{
				if (x.frequency > y.frequency)
					return -1;
				else if (x.frequency < y.frequency)
					return 1;
				else
					return x.items.get(0).compareTo(y.items.get(0));
			}
		}
	};
}

//============================

//============================