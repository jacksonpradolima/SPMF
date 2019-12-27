package ca.pfv.spmf.algorithms.sequentialpatterns.phuspm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ca.pfv.spmf.tools.MemoryLogger;

/* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
*
* SPMF is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* SPMF is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with SPMF.  If not, see <http://www.gnu.org/licenses/>.
* 
* @Copyright Ting Li et al., 2018
*/
/**
 * This is an implementation of the PHUSPM algorithm as presented in this paper: <br/><br/>
 * 
 *    Zhang, B., Lin, J. C.-W., Li, T., Gan, W., Fournier-Viger, P. (2017). Mining High Utility-Probability Sequential 
 *    Patterns in Uncertain Databases. PLoS One, Public Library of Science, to appear.
 * 
 * @author Ting Li
 */
public class AlgoPHUSPM {
	
	/** the maximum memory usage */
	int maxMemory = 0;
	
	/**  the time the algorithm started */
	long startTimestamp = 0;
	
	/**  the time the algorithm terminated */
	long endTimestamp = 0;

	/** the number of HUSP  **/
	public static int numberOfHUSP = 0;
	
	/** the number of candidates */
	public static int numberOfCandidates = 0;
	
	/** writer to write the output file **/
	BufferedWriter writer = null;  

	/**
	 * Constructor
	 */
	public AlgoPHUSPM() {

	}

	/**
	 * Run the algorithm
	 * 
	 * @param input the input file path
	 * @param output  the output file path
	 * @param minUtility  the minimum utility threshold count
	 * @param minProbability the minimum probability threshold count
	 * @throws IOException
	 */
	public void runAlgorithm(String input, String output, int minUtility, float minProbability)
			throws IOException {

		maxMemory = 0;
		startTimestamp = System.currentTimeMillis();
		numberOfHUSP = 0;
		numberOfCandidates = 0;
		
		// create a writer object to write results to file
		writer = new BufferedWriter(new FileWriter(output));

		BufferedReader myInput = null;
		Map<Integer, Integer> RSU = new HashMap<Integer, Integer>();
		Map<Integer, Float> Probability = new HashMap<Integer, Float>();
		Map<Integer, SequenceList> sequenceListMap = new HashMap<Integer, SequenceList>();
		// map of line's SU
		Map<Integer, Integer> orderSWU = new HashMap<Integer, Integer>();
		// map of line's probability
		Map<Integer, Float> orderSWP = new HashMap<Integer, Float>();
		// revised database
		List<List<Itemset>> revisedDatabase = new ArrayList<List<Itemset>>();

		// for each individual item(1-sequence), calculate its SWU and sequence
		// probability
		try {
			// prepare the object for reading the file
			myInput = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(input))));
			// for each line (transaction) until the end of file
			String thisLine = null;
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is a comment, is empty or is a
				// kind of metadata
				Set<Integer> set = new HashSet();
				String arry[] = thisLine.split(" -1 ");

				// sum of Line utility (SU)
				int lineUtility = Integer.parseInt(arry[arry.length - 2]);
				// Line's probability
				float lineProbability = Float.parseFloat(arry[arry.length - 1]);

				for (int i = 0; i < arry.length - 2; i++) {
					String itemset[] = arry[i].split(" , ");

					for (int j = 0; j < itemset.length; j++) {

						String itemProperty[] = itemset[j].split(" ");
						int item = Integer.parseInt(itemProperty[0]);
						set.add(item);
					}
				}

				Iterator<Integer> it = set.iterator();
				while (it.hasNext()) {
					int item = it.next();

					if (!RSU.containsKey(item)) {
						RSU.put(item, lineUtility);
					} else {
						RSU.put(item, RSU.get(item) + lineUtility);
					}

					if (!Probability.containsKey(item)) {
						Probability.put(item, lineProbability);
					} else {
						Probability.put(item, Probability.get(item)
								+ lineProbability);
					}
				}
			}
		} catch (Exception e) {
			// catches exception if error while reading the input file
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}

		try {
			// for each line (transaction) until the end of file
			myInput = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(input))));
			String thisLine = null;
			int order = 0;
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is a comment, is empty or is a
				// kind of metadata
				List<Itemset> sequnce = new ArrayList<Itemset>();
				String arry[] = thisLine.split(" -1 ");
				int lineSWU = Integer.parseInt(arry[arry.length - 2]);
				float lineSWP = Float.parseFloat(arry[arry.length - 1]);
				for (int i = 0; i < arry.length - 2; i++) {
					Itemset sitemset = new Itemset();

					String itemset[] = arry[i].split(" , ");

					for (int j = 0; j < itemset.length; j++) {

						String itemProperty[] = itemset[j].split(" ");
						int item = Integer.parseInt(itemProperty[0]);
						int utility = Integer.parseInt(itemProperty[1]);
						if (RSU.get(item) >= minUtility
								&& Probability.get(item) >= minProbability) {

							Item sitem = new Item();
							sitem.item = item;
							sitem.utility = utility;
							sitemset.Itemset.add(sitem);
						} else
							lineSWU -= utility;
					}

					if (!sitemset.Itemset.isEmpty()) {
						sequnce.add(sitemset);
					}
				}

				if (!sequnce.isEmpty()) {
					revisedDatabase.add(sequnce);
					orderSWU.put(order, lineSWU);
					orderSWP.put(order, lineSWP);
					order++;
				}
			}
		} catch (Exception e) {
			// catches exception if error while reading the input file
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}

		for (int i = 0; i < revisedDatabase.size(); i++) {
			int lineSWU = orderSWU.get(i);
			float lineProbability = orderSWP.get(i);
			int restUtility = lineSWU;

			for (int j = 0; j < revisedDatabase.get(i).size(); j++) {
				for (int k = 0; k < revisedDatabase.get(i).get(j).Itemset
						.size(); k++) {
					int item = revisedDatabase.get(i).get(j).Itemset.get(k).item;
					int utility = revisedDatabase.get(i).get(j).Itemset.get(k).utility;
					restUtility -= utility;
					Element element = new Element(i, j, utility,
							lineProbability, restUtility);

					if (sequenceListMap.containsKey(item)) {
						sequenceListMap.get(item).addElement(element);
					} else {
						SequenceList Seq = new SequenceList();
						List<Integer> itemSet = new ArrayList<Integer>();
						itemSet.add(item);
						Seq.addItemset(itemSet);
						Seq.addElement(element);
						sequenceListMap.put(item, Seq);
					}
				}
			}
		}

		// keep 1-sequnces in a Seq structure : sequenceList
		List<SequenceList> oneSequenceList = new ArrayList<SequenceList>(
				sequenceListMap.values());
		for (int i = 0; i < oneSequenceList.size(); i++) {
			oneSequenceList.get(i).calculate();
			if (oneSequenceList.get(i).sumSWU < minUtility
					|| oneSequenceList.get(i).sumProbability < minProbability) {
				oneSequenceList.remove(i);
				i--;
			}
		}

		Collections.sort(oneSequenceList, new Comparator<SequenceList>() {
			public int compare(SequenceList mc1, SequenceList mc2) {
				return mc1.itemsets.get(0).get(0) - mc2.itemsets.get(0).get(0);
			}
		});

		/* for each oneSequenceList prefix mining */
		for (SequenceList Seq : oneSequenceList) {
			AlgoPHUSPM(Seq, revisedDatabase, minUtility, minProbability);
		}
		numberOfCandidates += oneSequenceList.size();
		
		MemoryLogger.getInstance().checkMemory();
		writer.close();
			
		endTimestamp = System.currentTimeMillis();
	}

	/**
	 * mining processing, projection-based
	 * 
	 * @param Seq
	 *            , pre-sequence
	 * @param revisedDatabase
	 *            : revised database
	 * @param minUtility
	 *            : the minimum utility threshold count
	 * @param minProbability
	 *            : the minimum probability count
	 * @throws IOException 
	 */
	public void AlgoPHUSPM(SequenceList Seq,
			List<List<Itemset>> revisedDatabase, int minUtility,
			float minProbability) throws IOException {

		numberOfCandidates++;
		if (Seq.sumUtility >= minUtility
				&& Seq.sumProbability >= minProbability) {
			numberOfHUSP++;
			// output HUSP
			writeOut(Seq);
		}

		MemoryLogger.getInstance().checkMemory();
		List<SequenceList> nextGeneration = new ArrayList<SequenceList>();

		// keep sequence of combiner
		Map<Integer, SequenceList> itemExtend = new HashMap<Integer, SequenceList>();
		Map<Integer, SequenceList> itemsetExtend = new HashMap<Integer, SequenceList>();

		for (Element element : Seq.elements) {
			int SID = element.SID;
			int location = element.location;
			int preUtility = 0;

			int i = 0;
			for (i = 0; i < revisedDatabase.get(SID).get(location).Itemset
					.size(); i++) {
				int item = revisedDatabase.get(SID).get(location).Itemset
						.get(i).item;
				if (item == Seq.itemsets.get(Seq.itemsets.size() - 1).get(
						Seq.itemsets.get(Seq.itemsets.size() - 1).size() - 1)) {
					i++;
					break;
				}
			}
			// item extended
			for (; i < revisedDatabase.get(SID).get(location).Itemset.size(); i++) {
				int item = revisedDatabase.get(SID).get(location).Itemset
						.get(i).item;
				int utility = revisedDatabase.get(SID).get(location).Itemset
						.get(i).utility;
				preUtility += utility;

				Element newElement = new Element(SID, location, element.utility
						+ utility, element.probability, element.restUtility
						- preUtility);
				if (!itemExtend.containsKey(item)) {
					SequenceList newlist = new SequenceList();
					newlist.itemsets.addAll(Seq.itemsets);

					List<Integer> itemset = new ArrayList<Integer>();
					itemset.addAll(Seq.itemsets.get(Seq.itemsets.size() - 1));
					itemset.add(item);
					newlist.itemsets.remove(newlist.itemsets.size() - 1);
					newlist.itemsets.add(itemset);

					newlist.addElement(newElement);
					itemExtend.put(item, newlist);
				} else {
					itemExtend.get(item).addElement(newElement);
				}
			}

			/* itemset extended */
			for (int j = element.location + 1; j < revisedDatabase.get(SID)
					.size(); j++) {
				for (int k = 0; k < revisedDatabase.get(SID).get(j).Itemset
						.size(); k++) {
					int item = revisedDatabase.get(SID).get(j).Itemset.get(k).item;
					int utility = revisedDatabase.get(SID).get(j).Itemset
							.get(k).utility;
					preUtility += utility;
					Element newElement = new Element(SID, j, element.utility
							+ utility, element.probability, element.restUtility
							- preUtility);
					if (!itemsetExtend.containsKey(item)) {
						SequenceList newlist = new SequenceList();
						newlist.itemsets.addAll(Seq.itemsets);

						List<Integer> itemset = new ArrayList<Integer>();
						itemset.add(item);
						newlist.itemsets.add(itemset);

						newlist.addElement(newElement);
						itemsetExtend.put(item, newlist);
					} else {
						itemsetExtend.get(item).addElement(newElement);
					}
				}
			}
		}

		java.util.Iterator<Entry<Integer, SequenceList>> iter1 = itemExtend
				.entrySet().iterator();
		while (iter1.hasNext()) {// traverse mapOneItem, add frequent 1-itemsets
									// to frequentFuzzyOneItemset
			Map.Entry<Integer, SequenceList> entry = (Map.Entry<Integer, SequenceList>) iter1
					.next();
			entry.getValue().calculate();

			if (entry.getValue().sumSWU >= minUtility
					&& entry.getValue().sumProbability >= minProbability) {
				nextGeneration.add(entry.getValue());
			}
		}

		java.util.Iterator<Entry<Integer, SequenceList>> iter2 = itemsetExtend
				.entrySet().iterator();
		while (iter2.hasNext()) {// traverse mapOneItem, add frequent 1-itemsets
									// to frequentFuzzyOneItemset
			Map.Entry<Integer, SequenceList> entry = (Map.Entry<Integer, SequenceList>) iter2
					.next();
			entry.getValue().calculate();

			if (entry.getValue().sumSWU >= minUtility
					&& entry.getValue().sumProbability >= minProbability) {
				nextGeneration.add(entry.getValue());
			}
		}

		for (SequenceList nextlist : nextGeneration) {
			AlgoPHUSPM(nextlist, revisedDatabase, minUtility, minProbability);
		}
	}

	/**
	 * write a pattern to file
	 * @param sequence the pattern
	 * @param writer a buffered writer to write to the file
	 */
	private  void writeOut(SequenceList sequence) throws IOException {
		for (List<Integer> items : sequence.itemsets) {
			for (int item : items) {
				writer.write(item + " ");
			}
			writer.write("-1 ");
		}
		writer.write("#UITL: ");
		writer.write(Integer.toString(sequence.sumUtility));
		writer.write(" #SP: ");
		writer.write(Float.toString(sequence.sumProbability));
		writer.newLine();
	}

	/**
	 * Print statistics about the algorithm execution
	 */
	public void printStats() throws IOException {
		System.out
				.println("=======  THE RESULT OF THE ALGORITHM - STATS ============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp)
				/ 1000 + " s");
		System.out.println(" Candidates count: " + numberOfCandidates);
		System.out.println(" HUSPs count: " + numberOfHUSP);
		System.out.println(" Max memory: "
				+ MemoryLogger.getInstance().getMaxMemory() + "  MB");
		System.out.println("======================================================");
	}
}
