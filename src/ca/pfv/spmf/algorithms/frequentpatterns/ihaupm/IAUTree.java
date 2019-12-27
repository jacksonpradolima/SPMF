package ca.pfv.spmf.algorithms.frequentpatterns.ihaupm;

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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This is an implementation of the "IHAUPM" algorithm for High-Average-Utility
 * Itemsets Mining as described in the conference paper : <br/>
 * <br/>
 *
 * Jerry Chun-Wei Lin, Shifeng Ren, and Philippe Fournier-Viger. Efficiently
 * Updating the Discovered High Average-Utility Itemsets with Transaction
 * Insertion. EAAI (unpublished, minor revision)
 *
 * @see algorithm.IHAUPM
 * @see tree.IAUNode
 * @see tree.TableNode
 * @see util.Item
 * @see util.Itemset
 * @see util.StackElement
 * @author Shi-Feng Ren
 */

public class IAUTree {
	// root node
	static IAUNode root = new IAUNode(-1, (IAUNode) null);
	// headTable
	static Map<String, TableNode> headTableMap = null;
	static List<TableNode> headTable = null;
	// threshold that all itemsets must be greater that it
	static double threshold = 0.0;

	static double minautil = 0;

	static Map<String, Long> mapItemToAuub = null;
	static final Map<String, Map<String, Long>> EUCS = new HashMap<>();

	BufferedReader in = null;
	// BufferedReader reIn = null;

	boolean isActiveEUCS = true;

	public Map<String, Integer> item2profits = null;

	public void clear() {
		mapItemToAuub.clear();
		if (isActiveEUCS)
			EUCS.clear();
		headTable.clear();
		headTableMap.clear();
		root = new IAUNode(-1, (IAUNode) null);
	}

	public void construct(String datafileName, String profitFileName,
			double threshold, long numOfTrancs, boolean isInsert)
			throws Exception {
		item2profits = readProfits(profitFileName);
		this.threshold = threshold;
		in = new BufferedReader(new FileReader(datafileName));
		String line = null;
		String[] items = null;
		mapItemToAuub = new HashMap<>();
		long totalUtility = 0;
		int ii = 0;
		while ((line = in.readLine()) != null && (ii++ < numOfTrancs)) {
			items = line.split(" ");
			Integer maxItemUtility = -1;
			for (int i = 0; i < items.length; i += 2) {
				// name of item quantity
				int utility = Integer.parseInt(items[i+1].trim())
						* item2profits.get(items[i].trim());
				totalUtility += utility;
				if (maxItemUtility < utility) {
					maxItemUtility = utility;
				}
			}
			for (int i = 0; i < items.length; i += 2) {
				// name of item quantity
				Long auub = mapItemToAuub.get(items[i]);
				auub = (auub == null) ? maxItemUtility : auub + maxItemUtility;
				mapItemToAuub.put(items[i], auub);
			}
		}
		in.close();

		this.minautil = (double) totalUtility * this.threshold;
		Iterator<Entry<String, Long>> iter = mapItemToAuub.entrySet()
				.iterator();
		while (iter.hasNext()) {
			Entry<String, Long> entry = iter.next();
			// remove itemsets whose average utility is less than minimal
			// utility count
			// That will guarantee the item in `mapItemToAuub` equal to
			// `headTable`
			if (entry.getValue() < this.minautil)
				iter.remove();

		}
		buildHeadTable(mapItemToAuub);

		in = new BufferedReader(new FileReader(datafileName));
		ii = 0;
		while ((line = in.readLine()) != null && (ii++ < numOfTrancs)) {
			items = line.split(" ");
			Itemset itemset = new Itemset();
			int maximalItemUtility = -1;
			for (int i = 0; i < items.length; i += 2) {
				Long auub = mapItemToAuub.get(items[i]);
				if (auub == null || auub < (this.minautil))
					continue;
				itemset.add(new Item(items[i], Integer.parseInt(items[i+1])));
				int itemUtility = Integer.parseInt(items[i+1])
						* item2profits.get(items[i]);
				maximalItemUtility = maximalItemUtility < itemUtility ? itemUtility
						: maximalItemUtility;
			}
			itemset.trimToSize();
			itemset.maxItemUtility = maximalItemUtility;
			Collections.sort(itemset, new Comparator<Item>() {
				public int compare(Item a, Item b) {
					int cmp = (int) (mapItemToAuub.get(b.name) - mapItemToAuub
							.get(a.name));
					return cmp == 0 ? a.name.compareTo(b.name) : cmp;
				}
			});
			// add EUCS strategy
			if (isActiveEUCS)
				for (int i = 0; i < itemset.size(); i++) {
					Map<String, Long> subEUCS = EUCS.get(itemset.get(i).name);
					if (subEUCS == null) {
						subEUCS = new HashMap<>();
						EUCS.put(itemset.get(i).name, subEUCS);
					}
					for (int j = i + 1; j < itemset.size(); j++) {
						Long val = subEUCS.get(itemset.get(j).name);
						val = (val == null) ? itemset.maxItemUtility : val
								+ itemset.maxItemUtility;
						subEUCS.put(itemset.get(j).name, val);
					}
				}

			insertOneTranc(itemset, maximalItemUtility);

		}
		if (!isInsert)
			in.close();

	}

	/**
	 * Read profits from file
	 */
	private Map<String, Integer> readProfits(String fileName)
			throws IOException {
		Map<String, Integer> item2profits = new HashMap<>();

		BufferedReader in = new BufferedReader(new FileReader(fileName));
		String line = null;
		String[] pair = null;
		while ((line = in.readLine()) != null) {
			pair = line.split(", ");
			item2profits.put(pair[0].trim(), Integer.parseInt(pair[1].trim()));
		}
		in.close();

		return item2profits;
	}

	// insert one transaction into tree
	// elements in itemset has been sorted according to AUUB value
	private void insertOneTranc(Itemset itemset, Set<String> rescanItems) {
		IAUNode tmpRoot = root;
		for (int j = 0; j < itemset.size(); j++) {
			Item item = itemset.get(j);
			IAUNode child = tmpRoot.getChild(item.getName());
			// the current node has child named with <p>itme.getname()</p>
			if (child != null) {
				if (rescanItems != null) {
					if (rescanItems.contains(item.getName())) {
						child.plusAUUB(itemset.maxItemUtility);
						child.updateQuanBefor(j, itemset.subList(0, j + 1));
					}
				} else {
					child.plusAUUB(itemset.maxItemUtility);
					child.updateQuanBefor(j, itemset.subList(0, j + 1));
				}
			} else {
				if (rescanItems != null) {
					if (rescanItems.contains(item.getName())) {
						child = insertUnity(itemset, tmpRoot, item, j);
					} else
						continue; // Add at 2016/6/27
				} else {
					child = insertUnity(itemset, tmpRoot, item, j);
				}
			}
			if (child == null)
				System.out.println("item that leading to Null exception��"
						+ item.name);
			tmpRoot = child;
		}
	}

	private void insertOneTranc(Itemset itemset, int maximalItemUtility) {
		IAUNode tmpRoot = root;
		for (int j = 0; j < itemset.size(); j++) {
			Item item = itemset.get(j);
			IAUNode child = tmpRoot.getChild(item.getName());
			// the current node has child named with <p>itme.getname()</p>
			if (child != null) {
				child.plusAUUB(maximalItemUtility);
				child.updateQuanBefor(j, itemset.subList(0, j + 1));
			} else {
				child = insertUnity(itemset, tmpRoot, item, j);
			}
			tmpRoot = child;
		}
	}

	private IAUNode insertUnity(Itemset itemset, IAUNode tmpRoot, Item item,
			int j) {
		// Create new child and set its parent as tmpRoot
		// and initate its auub value.
		IAUNode child = new IAUNode(itemset.maxItemUtility, tmpRoot);
		// add child to the tmpRoot
		tmpRoot.putChild(item.getName(), child);
		// update new_child's quantities.
		child.addQuansBefor(j, itemset.subList(0, j + 1));
		// Link the new child to those IAUNodes having the same name
		TableNode p = headTableMap.get(item.getName());
		// If p is null, that mean the new child is the first in the tree
		child.setRight(p.hlink);
		if (p.hlink != null)
			p.hlink.setLeft(child);
		p.hlink = child;
		child.setLeft(null);

		child.setParent(tmpRoot); // add 2016/6/27
		return child;
	}

	// Build head_table
	private void buildHeadTable(Map<String, Long> mapItemToAuub) {
		headTable = new ArrayList<>();
		headTableMap = new HashMap<>();
		for (Entry<String, Long> entry : mapItemToAuub.entrySet()) {
			TableNode tn = new TableNode();
			headTable.add(tn);
			tn.name = entry.getKey();
			headTableMap.put(entry.getKey(), tn);
		}
		Collections.sort(headTable, new Comparator<TableNode>() {
			public int compare(TableNode a, TableNode b) {
				int cmp = (int) (mapItemToAuub.get(b.name) - mapItemToAuub
						.get(a.name));
				return cmp == 0 ? a.name.compareTo(b.name) : cmp;
			}
		});
	}

	/***************************** insertion ****************************************/
	// insert transactions
	// rawDb : inserted_transaction
	public void insertNewDB(int numOfTrancEachInsert, boolean isCloseFid,
			String originalFile, int originalLinesNum) throws Exception {

		Map<String, Long> newMapItemToAuub = new HashMap<>();
		// calculate inserted db's total utility And update threshold
		long newDBTotalUtility = 0;
		String line = null;
		String lineItems[] = null;
		List<Itemset> insertDB = new LinkedList<>();
		int tid = 0;
		while ((line = in.readLine()) != null && (tid++ < numOfTrancEachInsert)) {
			lineItems = line.split(", ");
			Integer maxItemUtility = -1;
			Itemset itemset = new Itemset(lineItems.length / 2);
			for (int i = 0; i < lineItems.length; i += 2) {
				// name of item quantity
				int utility = Integer.parseInt(lineItems[i].trim())
						* item2profits.get(lineItems[i + 1].trim());
				newDBTotalUtility += utility;
				if (maxItemUtility < utility) {
					maxItemUtility = utility;
				}
				itemset.add(new Item(lineItems[i], Integer
						.parseInt(lineItems[i+1].trim())));
			}
			itemset.maxItemUtility = maxItemUtility;
			insertDB.add(itemset);

			for (int i = 0; i < lineItems.length; i += 2) {
				// name of item quantity
				Long auub = newMapItemToAuub.get(lineItems[i]);
				auub = (auub == null) ? maxItemUtility : auub + maxItemUtility;
				newMapItemToAuub.put(lineItems[i], auub);
			}
		}

		if (isCloseFid)
			in.close();

		double localMinAutil = newDBTotalUtility * this.threshold;
		this.minautil += localMinAutil;
		// Handle Four cases.
		Set[] items = handleThreeCases(newMapItemToAuub, localMinAutil);

		// Set<String> insertItems = (Set<String>)items[0];
		Set<String> rescanItems = (Set<String>) items[1];
		// calculate original db if necessary.
		final Map<String, Integer> mapToPriorityOfTable = new HashMap<>();
		if (rescanItems.size() != 0) {
			// System.out.println("Rescan Orginal Database!");
			// long start_rescan = System.currentTimeMillis();
			BufferedReader in = new BufferedReader(new FileReader(originalFile));
			String thisLine = null;
			int ii = 0;
			Map<String, Long> rescanMapItemToAuub = new HashMap<>();
			while ((thisLine = in.readLine()) != null
					&& (ii++ < originalLinesNum)) {
				lineItems = thisLine.split(", ");
				int maxItemUtility = -1;
				for (int i = 0; i < lineItems.length; i += 2) {
					int utility = Integer.parseInt(lineItems[i+1])
							* item2profits.get(lineItems[i]);
					if (maxItemUtility < utility)
						maxItemUtility = utility;
				}
				for (int i = 0; i < lineItems.length; i += 2) {
					// Optimizition
					if (!rescanItems.contains(lineItems[i]))
						continue;

					Long auub = rescanMapItemToAuub.get(lineItems[i]);
					auub = (auub == null) ? maxItemUtility : auub
							+ maxItemUtility;
					rescanMapItemToAuub.put(lineItems[i], auub);
				}
			}
			in.close();

			List<Itemset> rescan_transactions = new LinkedList<>();
			in = new BufferedReader(new FileReader(originalFile));
			ii = 0;
			while ((thisLine = in.readLine()) != null
					&& (ii++ < originalLinesNum)) {
				lineItems = thisLine.split(", ");
				boolean isIncluded = false;
				Itemset tranc = new Itemset(lineItems.length / 2);
				int maxItemUtility = -1;
				for (int i = 0; i < lineItems.length; i += 2) {
					if (rescanItems.contains(lineItems[i])) {
						Long itemAuub = rescanMapItemToAuub
								.get(lineItems[i]);
						Long ItemAuubInInsertDB = newMapItemToAuub
								.get(lineItems[i]);
						if (itemAuub + ItemAuubInInsertDB >= this.minautil) {
							isIncluded = true;
							mapItemToAuub.put(lineItems[i], itemAuub
									+ ItemAuubInInsertDB);
							// System.out.println(lineItems[i+1]+":"+(itemAuub +
							// ItemAuubInInsertDB));
						} else {
							rescanItems.remove(lineItems[i]);
							// insertItems.remove(lineItems[i+1]);
							newMapItemToAuub.remove(lineItems[i]);
						}
					}
					int quantity = Integer.parseInt(lineItems[i+1]);
					int utility = quantity * item2profits.get(lineItems[i]);
					if (maxItemUtility < utility)
						maxItemUtility = utility;
					Item item = new Item(lineItems[i], quantity);
					tranc.add(item);
				}
				tranc.maxItemUtility = maxItemUtility;
				if (isIncluded)
					rescan_transactions.add(tranc);
			}
			in.close();
			rescanMapItemToAuub = null;
			// Iterate items that appearing in inserted transactions rather than
			// in original DB
			Iterator<String> iter = rescanItems.iterator();
			while (iter.hasNext()) {
				String name = iter.next();
				if (mapItemToAuub.get(name) == null) {
					Long localAubbVal = newMapItemToAuub.get(name);
					if (localAubbVal < this.minautil)
						iter.remove();
					else
						mapItemToAuub.put(name, localAubbVal);
				}
			}

			// Remove items whose auuu is less than minimal utility and that
			// item is not in inserted DB but in original DB
			Iterator<Entry<String, Long>> it = mapItemToAuub.entrySet()
					.iterator();
			while (it.hasNext()) {
				Entry<String, Long> entry = it.next();
				if (entry.getValue() < this.minautil) {
					it.remove();
					TableNode r = headTableMap.get(entry.getKey());
					removeItemFromTree(r, entry.getKey());
					r = headTableMap.remove(entry.getKey());
					headTable.remove(r);
				}
			}

			newMapItemToAuub = null;
			updateHeadTable(rescanItems);

			// System.out.println(check());
			int tableLen = headTable.size();
			for (int i = 0; i < tableLen; i++) {
				mapToPriorityOfTable.put(headTable.get(i).name, tableLen - i);
			}

			// Delete item that should not be inserted into HAUP-tree from
			// inserted transactions
			for (Itemset itemset : rescan_transactions) {

				for (int i = 0; i < itemset.size(); i++) {
					if (mapItemToAuub.get(itemset.get(i).name) == null) {
						itemset.remove(i);
						i--;
					}

				}
				// Add 2016/6/27
				boolean isInsert = false;
				for (Item item : itemset) {
					if (rescanItems.contains(item.name)) {
						// System.out.print("����");
						isInsert = true;
						break;
					}
				}
				// System.out.println();
				if (!isInsert)
					continue;

				Collections.sort(itemset, new Comparator<Item>() {
					@Override
					public int compare(Item a, Item b) {
						int cmp = (mapToPriorityOfTable.get(b.name) - mapToPriorityOfTable
								.get(a.name));
						return (cmp == 0) ? a.name.compareTo(b.name) : cmp;
					}
				});

				// // System.out.println(itemset);
				// for(Item item : itemset){
				// if(!rescanItems.contains(item.name)){
				// System.out.print(item.name +"***");
				// }
				// }System.out.println();

				insertOneTranc(itemset, rescanItems);
			}
			// System.out.println("rescan_time="+(System.currentTimeMillis()-start_rescan));
		}
		if (mapToPriorityOfTable.size() == 0) {
			int tableLen = headTable.size();
			for (int i = 0; i < tableLen; i++) {
				mapToPriorityOfTable.put(headTable.get(i).name, tableLen - i);
			}
		}
		// System.out.println(check(mapToPriorityOfTable));
		for (Itemset itemset : insertDB) {
			for (int i = 0; i < itemset.size(); i++) {
				if (mapItemToAuub.get(itemset.get(i).name) == null) {
					itemset.remove(i);
					i--;
				}
			}
			if (itemset.size() == 0)
				continue;
			Collections.sort(itemset, new Comparator<Item>() {
				@Override
				public int compare(Item a, Item b) {
					int cmp = (mapToPriorityOfTable.get(b.name) - mapToPriorityOfTable
							.get(a.name));
					return (cmp == 0) ? a.name.compareTo(b.name) : cmp;
				}
			});

			// update EUCS structure
			if (isActiveEUCS)
				for (int i = 0; i < itemset.size(); i++) {
					Map<String, Long> subEUCS = EUCS.get(itemset.get(i).name);
					if (subEUCS == null) {
						subEUCS = new HashMap<>();
						EUCS.put(itemset.get(i).name, subEUCS);
					}
					for (int j = i + 1; j < itemset.size(); j++) {
						Long val = subEUCS.get(itemset.get(j).name);
						val = (val == null) ? itemset.maxItemUtility : val
								+ itemset.maxItemUtility;
						subEUCS.put(itemset.get(j).name, val);
					}
				}
			insertOneTranc(itemset, itemset.maxItemUtility);
		}
		//
	}

	// Handle three cases when inserting
	// parameter newItem2auub store all items in inserted transactions
	private Set[] handleThreeCases(Map<String, Long> newItem2auub,
			double instThreshold) {
		// set of insert_items
		// Set<String> insertItems = new HashSet<>();
		// set of rescanItems
		Set<String> rescanItems = new HashSet<>();
		// iterate all items in inserted transactions
		Iterator<Entry<String, Long>> iter = newItem2auub.entrySet().iterator();
		// Iterator<Entry> iter = allItems.iterator();
		while (iter.hasNext()) {
			Entry<String, Long> entry = iter.next();
			String name = entry.getKey();
			Long auub = entry.getValue();
			// Owing to not storing the initial item2auub (we denote it as
			// oldItem2auub)
			// ,So we have to check the inserted items whether in headTable or
			// not.
			TableNode val = headTableMap.get(name);
			// case 1 case 2
			if (val != null) {
				Long valAuub = mapItemToAuub.get(val.name);
				if (valAuub + auub >= this.minautil) {
					// update the average utility upper bound value of Ij in
					// headTable
					mapItemToAuub.put(val.name, valAuub + auub);

					// insert item name in insert_items for updating tree.
					// insertItems.add(val.name);
				} else {
					// Remove this item from headTable
					// remove this item from the tree.
					TableNode r = headTableMap.get(name);
					removeItemFromTree(r, name);
					r = headTableMap.remove(name);
					headTable.remove(r);
					mapItemToAuub.remove(val.name);
					iter.remove();
				}
				// case 3, in this case
				// Rescaning original database is necessary
			} else if (val == null && auub >= instThreshold) {
				// insertItems.add(name);
				rescanItems.add(name);
				// case 4, the inserted item will never be higher than updated
				// threshold
			} else {
				iter.remove();
			}
		}
		return new Set[] { null, rescanItems };
	}

	// update headtable
	private void updateHeadTable(Set<String> rescanItems) {
		if (rescanItems.size() == 0)
			return;
		int len = headTable.size();
		for (String name : rescanItems) {
			TableNode tn = new TableNode();
			tn.name = name;
			headTableMap.put(tn.name, tn);
			headTable.add(tn);
		}
		List<TableNode> subList = headTable.subList(len, headTable.size());
		Collections.sort(subList, new Comparator<TableNode>() {
			@Override
			public int compare(TableNode a, TableNode b) {
				int cmp = (int) (mapItemToAuub.get(b.name) - mapItemToAuub
						.get(a.name));
				return (cmp == 0) ? a.name.compareTo(b.name) : cmp;
			}
		});
	}

	private void removeItemFromTree(TableNode p, String name) {
		IAUNode next = p.hlink;
		while (next != null) {
			IAUNode parent = next.getPareent();
			Map<String, IAUNode> delNodeChildren = next.getChildren();
			Iterator<Entry<String, IAUNode>> it = delNodeChildren.entrySet()
					.iterator();
			while (it.hasNext()) {
				Entry<String, IAUNode> entry = it.next();
				String key = entry.getKey();
				IAUNode val = entry.getValue();
				// Delete quantity array at specific location where the current
				// item appear.
				deleteQuanAryAt(val, 2);
				union(key, val, parent, it);
			}
			IAUNode tmp = next;
			next = next.getRight();
			p.hlink = next;
			if (next != null) {
				next.setLeft(null);
			}
			tmp.setRight(null);
			tmp.setLeft(null);
			// System.out.println(parent);
			parent.removeChild(name); // remove current node from parent.
			tmp.setParent(null);
		}
		p.hlink = null;
	}

	// Union child with same name
	private void union(String name, IAUNode val, IAUNode grandParent,
			Iterator<Entry<String, IAUNode>> iter) {
		IAUNode hasExist = grandParent.getChild(name);
		if (hasExist == null) {
			grandParent.putChild(name, val);
			val.setParent(grandParent);
		} else {
			// union quanAry
			hasExist.mergeQuanAry(val);
			// union auub
			hasExist.mergeAUUB(val);
			// update link
			if (val.getLeft() == null) { // hlink ָ����Ǹ�node
				TableNode tn = headTableMap.get(val.getName());
				// System.out.println(tn.hlink==val);
				tn.hlink = val.getRight();
			} else
				val.getLeft().setRight(val.getRight());

			if (val.getRight() != null) {
				val.getRight().setLeft(val.getLeft());
			}
			val.setRight(null);
			val.setLeft(null);
			val.setParent(null);
			iter.remove();
			// union children
			Map<String, IAUNode> children = val.getChildren();
			Iterator<Entry<String, IAUNode>> it = children.entrySet()
					.iterator();
			while (it.hasNext()) {
				Entry<String, IAUNode> entry = it.next();
				IAUNode child = entry.getValue();
				String key = entry.getKey();
				union(key, child, hasExist, it);

			}
		}
	}

	private void deleteQuanAryAt(IAUNode cur, int index) {
		cur.removeQuantityAt(cur.quantityArySize() - index);
		Map<String, IAUNode> children = cur.getChildren();
		Iterator<Entry<String, IAUNode>> it = children.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, IAUNode> entry = it.next();
			IAUNode child = entry.getValue();
			deleteQuanAryAt(child, index + 1);
		}
	}

	boolean check() {
		for (Entry<String, Long> entry : mapItemToAuub.entrySet()) {
			if (headTableMap.get(entry.getKey()) == null) {
				return false;
			}
		}
		return true;
	}

	boolean check(Map<String, Integer> mapPriority) {
		for (Entry<String, Long> entry : mapItemToAuub.entrySet()) {
			if (mapPriority.get(entry.getKey()) == null) {
				return false;
			}
		}
		return true;
	}

	boolean check(Set<String> rescanItems) {
		for (Entry<String, Long> entry : mapItemToAuub.entrySet()) {
			if (rescanItems.contains(entry.getKey())) {
				return false;
			}
		}
		return true;
	}
}
