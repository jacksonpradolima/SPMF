package ca.pfv.spmf.algorithms.frequentpatterns.vhuqi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/* This file is copyright (c) Cheng-Wei Wu et al. and obtained under GPL license from the UP-Miner software.
 *  The modification made for integration in SPMF are (c) Philippe Fournier-Viger
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 * 
 * SPMF is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * This is an implementation of the VHUQI algorithm. The code was obtained
 * from the UP-Miner library under the GPL 3 license. The VHUQI algorithm is described in this research paper:<br/><br/>
 * 
 * C. H. Li, C. Wu and V. S. Tseng, “Efficient Vertical Mining of High Utility Quantitative Itemsets,
 * ”Proc. of Int’l Conf. on Granular Computing,pp. 155-160, 2014.<br/><br/>
 * 
 * Modification to the original implementation were made by Philippe Fournier-Viger to integrate it in SPMF.
 * 
 * @author Cheng Wei wu et al.
 */
public class AlgoVHUQI {

	/** start time of last execution */
	long startTime;

	/** end time of last execution */
	long endTime;

	/** decimal format for writing numbers */
	DecimalFormat decimalFormat = new DecimalFormat("#.##");

	/** the muv */
	int muv;

	/** the profit table */
	String profitTable;

	/** the relative coefficient */
	int relatedCoefficient;

	/** the total utility */
	int totalUtility;

	/** the type of combination method */
	enumVHUQIMethod methodType;

	/** Map of string to profit */
	HashMap<String, Integer> profitHashMap;

	/** Map of String to utility list */
	HashMap<String, UtilityList> qItemsUtilityList;

	/** TWU values */
	ArrayList<Integer> arrayTWU;

	long maxMemory;
	int huqiCount;
	int countUL;
	int countWeak;

	/**
	 * Run the algorithm
	 * 
	 * @param inputFileDBPath
	 *            a path to an input database
	 * @param inputFileProfitPath
	 *            a path to a profit table
	 * @param outputPath
	 *            a path to save the patterns found
	 * @param minUtility
	 *            a minimum utility threshold as a percentage
	 * @param relativeCoefficient
	 *            a relative coefficient
	 * @param method
	 *            the method to be used (either MINC, MAXC or ALLC)
	 * @throws IOException
	 *             if error writing/reading to file
	 */
	public void runAlgorithm(String inputFileDBPath,
			String inputFileProfitPath, String outputPath, float minUtility,
			int relativeCoefficient, enumVHUQIMethod method) throws IOException {

		startTime = System.currentTimeMillis();

		countUL = 0;
		countWeak = 0;
		huqiCount = 0;
		maxMemory = 0;
		profitHashMap = new HashMap<String, Integer>();
		qItemsUtilityList = new HashMap<String, UtilityList>();
		arrayTWU = new ArrayList<Integer>();

		ArrayList<String> qItemNameList = new ArrayList<String>();
		ArrayList<String> nextNameList = new ArrayList<String>();
		ArrayList<String> hwQUI = new ArrayList<String>();

		// output file path, add at 2015/07/10
		String huqiOutputPath;

		String inputDatabase = inputFileDBPath;
		profitTable = inputFileProfitPath;
		relatedCoefficient = relativeCoefficient;
		methodType = method;

		huqiOutputPath = outputPath;
		String newDatabase = "temp_newDatabase.txt";

		BufferedWriter writerHQUI = new BufferedWriter(new FileWriter(
				huqiOutputPath));

		readFileFromTwoDatabase(inputDatabase, newDatabase);

		muv = (int) (totalUtility * minUtility);

		buildUtilityList(newDatabase, qItemNameList);

		findHQUA(writerHQUI, qItemNameList, hwQUI);

		miner(qItemsUtilityList, null, qItemNameList, nextNameList, writerHQUI,
				hwQUI);

		// Delete temporary file
		File file = new File(newDatabase);
		file.delete();

		endTime = System.currentTimeMillis();

		writerHQUI.close();
	}

	private void readFileFromTwoDatabase(String inputDatabase,
			String newDatabase) throws IOException {
		BufferedReader readerProfitTable = new BufferedReader(new FileReader(
				profitTable));
		BufferedReader readerInputDB = new BufferedReader(new FileReader(
				inputDatabase));
		BufferedWriter writer = new BufferedWriter(new FileWriter(newDatabase));
		profitHashMap.clear();
		qItemsUtilityList.clear();
		int line = 0;
		totalUtility = 0;
		String str;

		while ((str = readerProfitTable.readLine()) != null) {

			String[] itemInfo = str.split(", ");
			if (itemInfo.length >= 2) {
				int profit = Integer.parseInt(itemInfo[1]);
				if (profit == 0)
					profit = 1;

				int item = Integer.parseInt(itemInfo[0]);
				profitHashMap.put(String.valueOf(item), profit);
			}

		}// end while

		readerProfitTable.close();

		while ((str = readerInputDB.readLine()) != null) {

			line++;

			String[] itemInfo = str.split(" ");// (A,2) (B, 5)

			ArrayList<String> qItemset = new ArrayList<String>();
			int strUtility = 0;
			for (int i = 0; i < itemInfo.length; i++) {

				try {
					itemInfo[i] = itemInfo[i].substring(1,
							itemInfo[i].length() - 1);// B,2
				} catch (Exception e) {
					System.out
							.println("info1=" + itemInfo[i] + "@line=" + line);
					itemInfo[i] = itemInfo[i].substring(1,
							itemInfo[i].length() - 1);
				}
				String[] itemInfoSplit = itemInfo[i].split(",");

				int profit = 0;
				try {
					profit = profitHashMap.get(itemInfoSplit[0]);// itemInfoSplit[0]
																	// item
				} catch (Exception e) {
					System.out.println("!" + str + "!");
					System.out.println(itemInfoSplit[0]);
				}
				int quantity = Integer.parseInt(itemInfoSplit[1]);// itemInfoSplit[1]
																	// quantity

				strUtility += profit * quantity;

				qItemset.add(itemInfo[i]);
				if (!qItemsUtilityList.containsKey(itemInfo[i])) {
					UtilityList ul = new UtilityList("", itemInfo[i], 0);
					qItemsUtilityList.put(itemInfo[i], ul);
				}

			}// end for

			arrayTWU.add(strUtility);
			totalUtility += strUtility;

			sortUtility(qItemset);
			// write file to newDatabase.txt
			for (String qItem : qItemset) {
				writer.write("(" + qItem + ") ");
			}
			writer.newLine();

		}// end while
		long usedMem = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage()
				.getUsed();
		if (maxMemory < usedMem)
			maxMemory = usedMem;
		readerInputDB.close();
		writer.close();

	}

	private void buildUtilityList(String newDatabase,
			ArrayList<String> qItemNameList) throws IOException{
		BufferedReader readerInputDB = new BufferedReader(new FileReader(
				newDatabase));
		String str = "";
		int tid = 0;
		while ((str = readerInputDB.readLine()) != null) {
			tid++;
			String[] itemInfo = str.split(" ");// (A,2) (B, 5)
			int ru = arrayTWU.get(tid - 1);
			for (int i = 0; i < itemInfo.length; i++) {
				try {
					itemInfo[i] = itemInfo[i].substring(1,
							itemInfo[i].length() - 1);// B,2
					String[] itemInfoSplit = itemInfo[i].split(",");
					int profit = profitHashMap.get(itemInfoSplit[0]);// B
					int quantity = Integer.parseInt(itemInfoSplit[1]);// 2
					int utility = profit * quantity;
					ru -= utility;
					UtilityList ul = qItemsUtilityList.get(itemInfo[i]);
					QItemTrans qTid = new QItemTrans(tid, utility, ru);
					ul.addTrans(qTid, arrayTWU.get(tid - 1));
					qItemsUtilityList.put(itemInfo[i], ul);

					if (!qItemNameList.contains(itemInfo[i])) {
						qItemNameList.add(itemInfo[i]);
					}
				} catch (Exception e) {
					System.out.println(itemInfo[i] + "$");
				}
			}
		}

		sortUtility(qItemNameList);
		countUL += qItemsUtilityList.size();

		long usedMem = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage()
				.getUsed();
		if (maxMemory < usedMem)
			maxMemory = usedMem;
	}

	private void sortUtility(ArrayList<String> qItemNameList) {
		Collections.sort(qItemNameList, new Comparator<String>() {
			public int compare(String qItem1, String qItem2) {
				String[] info1 = qItem1.split(",");
				String[] info2 = qItem2.split(",");
				int util1;
				int util2;
				util1 = profitHashMap.get(info1[0])
						* Integer.parseInt(info1[1]);
				util2 = profitHashMap.get(info2[0])
						* Integer.parseInt(info2[1]);

				if (util2 == util1) {
					return info1[0].compareToIgnoreCase(info2[0]);
				} else {
					return util2 - util1;
				}

			}

		});// end Collections.sort
	}// end sortUtility

	private void findHQUA(BufferedWriter writerHUQI,
			ArrayList<String> qItemNameList, ArrayList<String> hwQUI)
			throws IOException {
		ArrayList<String> candidateList = new ArrayList<String>();

		for (int i = 0; i < qItemNameList.size(); i++) {
			int us = qItemsUtilityList.get(qItemNameList.get(i)).getSumEU();
			if (us >= muv) {
				writerHUQI.write("(" + qItemNameList.get(i) + ") #UTIL: " + us
						+ "\r\n");
				hwQUI.add(qItemNameList.get(i));

				huqiCount++;
			} else {
				if ((methodType != enumVHUQIMethod.MAXC && us >= Math.floor(muv
						/ (double) relatedCoefficient))
						|| (methodType == enumVHUQIMethod.MAXC && us >= Math
								.floor(muv / 2d))) {

					candidateList.add(qItemNameList.get(i));
				}
				float temp = (float) muv
						/ (qItemsUtilityList.get(qItemNameList.get(i))
								.getMaxEURU());
				int kSupportBound = (int) Math.ceil(temp);// �V�W�����
				if (qItemsUtilityList.get(qItemNameList.get(i)).getQItemTrans()
						.size() >= kSupportBound) {
					countWeak++;
					hwQUI.add(qItemNameList.get(i));

				}
			}

		}
		long usedMem;
		if (candidateList.size() > 0) {
			usedMem = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage()
					.getUsed();
			if (maxMemory < usedMem) {
				maxMemory = usedMem;
			}
			combineMethod(candidateList, writerHUQI, qItemNameList, hwQUI,
					null, null);
		}
		usedMem = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage()
				.getUsed();
		if (maxMemory < usedMem) {
			maxMemory = usedMem;
		}

	}// end findHQUA()

	private String preprocessCombine(ArrayList<String> candidate) {
		String name = "";

		for (int i = 0; i < candidate.size(); i++) {// A,1=>A,1,1
			String qName = candidate.get(i);
			String[] qNameSpilt = qName.split(",");
			if (qNameSpilt.length < 3) {

				qName = qNameSpilt[0] + "," + qNameSpilt[1] + ","
						+ qNameSpilt[1];// A,1,1
				candidate.set(i, qName);
			}

		}
		Collections.sort(candidate);

		for (String qname : candidate)
			name += "(" + qname + ") ";

		name = name.substring(0, name.length() - 1);
		candidate.clear();

		return name;
	}

	private void buildUtilityListofCombine(String qName,
			ArrayList<String> qItemNameList){
		String[] qNameSplit = qName.split(",");
		int lowerBound = Integer.parseInt(qNameSplit[1]);
		int upperBound = Integer.parseInt(qNameSplit[2]);
		UtilityList ul = new UtilityList("", qName, 0);

		for (int i = lowerBound; i <= upperBound; i++) {
			ul.addNoPrefixUtilityList(qItemsUtilityList.get(qNameSplit[0] + ","
					+ i));
		}
		qItemsUtilityList.put(qName, ul);
		countUL++;

		// ��Jcombine���e�@�Ӧ�m
		if (qItemNameList.contains(qNameSplit[0] + "," + upperBound)) {
			int site = qItemNameList.indexOf(qNameSplit[0] + "," + upperBound);
			qItemNameList.add(site, qName);

		}

	}// end buildUtilityListofCombine()

	private int utilityCal(String qItem, int preUS)// qItem (A,2,3) or
													// (A,2)
	{
		qItem = (String) qItem.subSequence(1, qItem.length() - 1);// A,2,3 or
																	// A,2
		String[] qItemSplit = qItem.split(",");

		// int profit = profitHashMap.get(qItemSplit[0]);// qItemSplit[0]
		// = A
		int quantity = 0;

		int utility = 0;

		if (qItemSplit.length > 2) {
			int lower = Integer.parseInt(qItemSplit[1]);
			int upper = Integer.parseInt(qItemSplit[2]);
			for (int i = lower; i <= upper; i++) {
				utility += preUS
						+ qItemsUtilityList.get(qItemSplit[0] + "," + i)
								.getSumEU();
			}

		} else {
			quantity = Integer.parseInt(qItemSplit[1]);
			utility += preUS
					+ qItemsUtilityList.get(qItemSplit[0] + "," + quantity)
							.getSumEU();
		}
		return utility;
	}// end utilityCal()

	private UtilityList construct(UtilityList ul1, UtilityList ul2,
			UtilityList preUL) {
		UtilityList conUl;

		String[] name1split = ul1.getName().split(",");
		String[] name2split = ul2.getName().split(",");

		if (name1split[0].equals(name2split[0])) {
			return null;
		} else if (preUL == null)// length 1
		{
			if (ul1.getQItemTrans().size() == 0) {
				int lowerBound = Integer.parseInt(ul1.getName().split(",")[1]);
				int upperBound = Integer.parseInt(ul1.getName().split(",")[2]);

				for (int i = lowerBound; i <= upperBound; i++) {
					ul1.addNoPrefixUtilityListQItemTrans(qItemsUtilityList
							.get(ul1.getName().split(",")[0] + "," + i));
				}
			}

			if (ul2.getQItemTrans().size() == 0) {
				int lowerBound = Integer.parseInt(ul2.getName().split(",")[1]);
				int upperBound = Integer.parseInt(ul2.getName().split(",")[2]);

				for (int i = lowerBound; i <= upperBound; i++) {
					ul2.addNoPrefixUtilityListQItemTrans(qItemsUtilityList
							.get(ul2.getName().split(",")[0] + "," + i));
				}
			}

			conUl = new UtilityList("(" + ul1.getName() + ")", ul2.getName(), 0);

			if (constructLengthOne(conUl, ul1.getQItemTrans(),
					ul2.getQItemTrans())) {
				long usedMem = ManagementFactory.getMemoryMXBean()
						.getHeapMemoryUsage().getUsed();
				if (maxMemory < usedMem) {
					maxMemory = usedMem;
				}

				return conUl; // �i��n�վ�
			}
		} else {
			conUl = new UtilityList(ul1.getPrefix() + " (" + ul1.getName()
					+ ")", ul2.getName(), 0);

			if (constructLengthOneMore(conUl, ul1.getQItemTrans(),
					ul2.getQItemTrans(), preUL.getQItemTrans())) {

				long usedMem = ManagementFactory.getMemoryMXBean()
						.getHeapMemoryUsage().getUsed();
				if (maxMemory < usedMem) {
					maxMemory = usedMem;
				}

				return conUl;
			}
		}
		return null;

	}// end construct()

	public boolean constructLengthOne(UtilityList conUl, List<QItemTrans> qT1,
			List<QItemTrans> qT2) {

		int i = 0;
		int j = 0;
		while (i < qT1.size() && j < qT2.size()) {
			int tid1 = qT1.get(i).getTid();
			int tid2 = qT2.get(j).getTid();
			if (tid1 == tid2) {

				int eu1 = qT1.get(i).getEu();

				int eu2 = qT2.get(j).getEu();


				if (qT1.get(i).getRu() >= qT2.get(j).getRu()) {
					QItemTrans temp = new QItemTrans(tid1, eu1 + eu2, qT2
							.get(j).getRu());
					conUl.addTrans(temp, arrayTWU.get(tid1 - 1));

				}
				i++;
				j++;
			} else if (tid1 > tid2) {
				j++;
			} else {
				i++;
			}


		}

		return (conUl.getqItemTransLength() > 0);
	}

	private boolean constructLengthOneMore(UtilityList conUl,
			ArrayList<QItemTrans> qT1, ArrayList<QItemTrans> qT2,
			ArrayList<QItemTrans> preQT) {

		int i = 0;
		int j = 0;
		int k = 0;
		while (i < qT1.size() && j < qT2.size()) {
			int tid1 = qT1.get(i).getTid();
			int tid2 = qT2.get(j).getTid();

			if (tid1 == tid2) {

				int eu1 = qT1.get(i).getEu();

				int eu2 = qT2.get(j).getEu();
				while (preQT.get(k).getTid() != tid1) {
					k++;
				}
				int preEU = preQT.get(k).getEu();

				if (qT1.get(i).getRu() >= qT2.get(j).getRu()) {
					QItemTrans temp = new QItemTrans(tid1, eu1 + eu2 - preEU,
							qT2.get(j).getRu());

					conUl.addTrans(temp, arrayTWU.get(tid1 - 1));
				}

				i++;
				j++;
			} else if (tid1 > tid2) {
				j++;
			} else {
				i++;
			}
		}
		return (conUl.getqItemTransLength() > 0);
	}

	private void miner(HashMap<String, UtilityList> nowUL,
			UtilityList beforeUL, ArrayList<String> qItemNameList,
			ArrayList<String> nextNameList, BufferedWriter writerHUQI,
			ArrayList<String> hwQUI) throws IOException {

		for (int i = 0; i < qItemNameList.size(); i++) {
			nextNameList.clear();

			HashMap<String, UtilityList> nextHUL = new HashMap<String, UtilityList>();
			HashMap<String, UtilityList> candidateHUL = new HashMap<String, UtilityList>();
			ArrayList<String> childNextNameList = new ArrayList<String>();
			ArrayList<String> nextHWQUI = new ArrayList<String>();

			if (!hwQUI.contains(qItemNameList.get(i)))
				continue;
			ArrayList<String> candidateList = new ArrayList<String>();

			for (int j = i + 1; j < qItemNameList.size(); j++) {
				if (qItemNameList.get(j).split(",").length == 3) {
					continue;
				}

				UtilityList afterUL = construct(
						nowUL.get(qItemNameList.get(i)),
						nowUL.get(qItemNameList.get(j)), beforeUL);

				if (afterUL != null) {
					nextNameList.add(afterUL.getName());
					if (afterUL.getSumEU() >= muv) {
						writerHUQI.write(afterUL.getPrefix() + " ("
								+ afterUL.getName() + ") #UTIL: " + afterUL.getSumEU()
								+ "\r\n");
						huqiCount++;
						nextHWQUI.add(afterUL.getName());
					} else {
						if ((methodType != enumVHUQIMethod.MAXC && afterUL
								.getSumEU() >= Math.floor(muv
								/ (double) relatedCoefficient))
								|| (methodType == enumVHUQIMethod.MAXC && afterUL
										.getSumEU() >= Math.floor(muv / 2d))) {// �L����˥h
							if (!candidateList.contains(afterUL.getName())) {

								candidateList.add(afterUL.getName());
								candidateHUL.put(afterUL.getName(), afterUL);

							}

						}

						float temp = (float) muv / afterUL.getMaxEURU();
						int kSupportBound = (int) Math.ceil(temp);// �L������
						if (afterUL.getQItemTrans().size() >= kSupportBound) {
							countWeak++;
							nextHWQUI.add(afterUL.getName());
						}
					}

					nextHUL.put(afterUL.getName(), afterUL);
					countUL++;
					//
				}

			}// inner for

			if (candidateList.size() > 0) {

				combineMethod(candidateList, writerHUQI, nextNameList,
						nextHWQUI, candidateHUL, nextHUL);

				candidateHUL.clear();
				candidateList.clear();
			}
			long usedMem = ManagementFactory.getMemoryMXBean()
					.getHeapMemoryUsage().getUsed();
			if (maxMemory < usedMem) {
				maxMemory = usedMem;
			}

			if (nextNameList.size() >= 1 && nextHUL.size() > 0) {
				miner(nextHUL, nowUL.get(qItemNameList.get(i)), nextNameList,
						childNextNameList, writerHUQI, nextHWQUI);
			}
			nextHUL.clear();
			childNextNameList.clear();
			nextHWQUI.clear();
			nowUL.remove(qItemNameList.get(i));
		}// outer for
	}// end Miner()

	private void buildUtilityListofcombine2(String qName,
			HashMap<String, UtilityList> nowUL){

		String[] qNameSplit = qName.split(",");
		int lowerBound = Integer.parseInt(qNameSplit[1]);
		int upperBound = Integer.parseInt(qNameSplit[2]);

		String prefix = nowUL.get(qNameSplit[0] + "," + lowerBound).getPrefix();
		UtilityList ul = new UtilityList(prefix, qName, 0);
		for (int i = lowerBound; i <= upperBound; i++) {
			ul.addPrefixUtilityList(nowUL.get(qNameSplit[0] + "," + i));
		}
		nowUL.put(qName, ul);
		countUL++;
	}// end buildUtilityListofcombine2()
		// public static void buildUtilityListofcombine(String qName,
		// UtilityList now,UtilityList next)
		// combine(mayHuqiSpilt[0], candidate, br_writer_hqui, nextNameList,

	private void combineMethod(ArrayList<String> candidateList,
			BufferedWriter writerHUQI, ArrayList<String> qItemNameList,
			ArrayList<String> hwQUI,
			HashMap<String, UtilityList> candidateHUL,
			HashMap<String, UtilityList> nextHUL) throws IOException {

		String info = preprocessCombine(candidateList);

		if (methodType.equals(enumVHUQIMethod.MINC)) {
			combineMin(info, writerHUQI, qItemNameList, hwQUI, candidateHUL,
					nextHUL);
		} else if (methodType.equals(enumVHUQIMethod.MAXC)) {
			combineMax(info, writerHUQI, qItemNameList, hwQUI, candidateHUL,
					nextHUL);
		} else {
			combineAll(info, writerHUQI, qItemNameList, hwQUI, candidateHUL,
					nextHUL);
		}

	}

	private void combineAll(String info, BufferedWriter writerHQUI,
			ArrayList<String> qItemNameList, ArrayList<String> hwQUI,
			HashMap<String, UtilityList> candidateHUL,
			HashMap<String, UtilityList> nextHUL) throws IOException {
		long usedMem = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage()
				.getUsed();
		if (maxMemory < usedMem) {
			maxMemory = usedMem;
		}

		while (info.length() != 0) {
			String[] mayHuqiSpilt = info.split(" ");

			if (info.length() >= mayHuqiSpilt[0].length() + 1) {
				info = info.substring(mayHuqiSpilt[0].length() + 1);
				combineAllProcess(mayHuqiSpilt[0], info, candidateHUL,
						qItemNameList, writerHQUI, hwQUI, nextHUL);
			} else {
				break;
			}
		}

	}

	private void combineAllProcess(String now, String nextList,
			HashMap<String, UtilityList> candidateHUL,
			ArrayList<String> qItemNameList, BufferedWriter writerHQUI,
			ArrayList<String> hwQUI, HashMap<String, UtilityList> nextHUL)
			throws IOException {

		now = (String) now.subSequence(1, now.length() - 1);
		String[] nowSplit = now.split(",");
		if (candidateHUL == null) {
			int nowUB = Integer.parseInt(nowSplit[2]);
			int count = 1;
			String[] nextListSplit = nextList.split(" ");

			for (String nextName : nextListSplit) {

				// (B,5,5)=> B,5,5
				try {
					if (count < relatedCoefficient) {
						nextName = (String) nextName.subSequence(1,
								nextName.length() - 1);
						String[] nextNameSplit = nextName.split(",");

						// if same item .(A,1,1) compare (A,2,2)
						if (nextNameSplit.length >= 2
								&& nowSplit[0].compareTo(nextNameSplit[0]) == 0
								&& nowUB + 1 == Integer
										.valueOf(nextNameSplit[1])) {

							// �u�n�i�H�� �B�Lmus �N�[�J
							count++;
							nowUB++;
							String qItemName = "(" + nowSplit[0] + ","
									+ nowSplit[1] + "," + nowUB + ")";
							int us = utilityCal(qItemName, 0);

							if (us >= muv) {
								writerHQUI.write("("
										+ qItemName.substring(1,
												qItemName.length() - 1) + ") #UTIL: "
										+ us + "\r\n");
								huqiCount++;
								hwQUI.add(qItemName.substring(1,
										qItemName.length() - 1));
								buildUtilityListofCombine(
										qItemName.substring(1,
												qItemName.length() - 1),
										qItemNameList);
							}
						}// end if same item

					}// end if count
				} catch (Exception e) {
					System.out.println("!" + nextName + "@");
				}
			}// end for
		} else {
			int nowLB = Integer.parseInt(nowSplit[1]);
			int nowUB = Integer.parseInt(nowSplit[2]);
			int count = 1;
			int us = 0;

			UtilityList nowl;
			if (nowLB == nowUB)// A,2,2
			{
				// A,2
				nowl = candidateHUL.get(nowSplit[0] + "," + nowSplit[1]);
			} else {
				nowl = candidateHUL.get(now);
			}
			us = nowl.getSumEU();

			String[] nextListSplit = nextList.split(" ");
			for (String nextName : nextListSplit) {
				if (count < relatedCoefficient) {
					// (B,5,5)=> B,5,5
					nextName = (String) nextName.subSequence(1,
							nextName.length() - 1);

					String[] nextNameSplit = nextName.split(",");
					int nextLB = Integer.parseInt(nextNameSplit[1]);
					int nextUB = Integer.parseInt(nextNameSplit[2]);

					UtilityList nextl;
					if (nextLB == nextUB)// A,2,2
						nextl = candidateHUL.get(nextNameSplit[0] + ","
								+ nextNameSplit[1]);
					else
						// A,2,3
						nextl = candidateHUL.get(nextName);

					// if same item .(A,1,1) compare (A,2,2)
					if (nowSplit[0].compareTo(nextNameSplit[0]) == 0) {
						if (nowUB + 1 == nextLB) {
							us += nextl.getSumEU();
							count++;
							nowUB++;

							String qItemName = "(" + nowSplit[0] + ","
									+ nowSplit[1] + "," + nowUB + ")";

							if (us >= muv) // �p��utilty���L�j��mus
											// ���N�[�J�Կﶵ
							{
								writerHQUI.write(nowl.getPrefix() + " "
										+ qItemName + " #UTIL: " + us + "\r\n");
								huqiCount++;
								qItemName = qItemName.substring(1,
										qItemName.length() - 1);
								hwQUI.add(qItemName);

								String[] temp = qItemName.split(",");
								if (qItemNameList.contains(temp[0] + ","
										+ temp[2])) {
									int site = qItemNameList.indexOf(temp[0]
											+ "," + temp[2]);
									qItemNameList.add(site, qItemName);

								}
								buildUtilityListofcombine2(qItemName, nextHUL);
							}

						}
					}// end if same item
				}// end if count
			}// end for

		}
		long usedMem = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage()
				.getUsed();
		if (maxMemory < usedMem) {
			maxMemory = usedMem;
		}
	}

	private void combineMin(String info, BufferedWriter writerHUQI,
			ArrayList<String> qItemNameList, ArrayList<String> hwQUI,
			HashMap<String, UtilityList> candidateHUL,
			HashMap<String, UtilityList> nextHUL) throws IOException {
		ArrayList<String> huqiCandidate = new ArrayList<String>();
		ArrayList<String> huqiCandidateOutputInfo = new ArrayList<String>();
		while (info.length() != 0) {
			String[] mayHuqiSpilt = info.split(" ");

			if (info.length() >= mayHuqiSpilt[0].length() + 1) {
				info = info.substring(mayHuqiSpilt[0].length() + 1);
				combineMinProcess(mayHuqiSpilt[0], info, candidateHUL,
						huqiCandidate, huqiCandidateOutputInfo);
			} else {
				break;
			}
		}

		minChecking(huqiCandidate, huqiCandidateOutputInfo);
		writeHUCQI(candidateHUL, nextHUL, huqiCandidate,
				huqiCandidateOutputInfo, writerHUQI, qItemNameList, hwQUI);

		huqiCandidate.clear();
		huqiCandidateOutputInfo.clear();

	}

	private void combineMinProcess(String now, String nextList,
			HashMap<String, UtilityList> candidateHUL,
			ArrayList<String> huqiCandidate,
			ArrayList<String> huqiCandidateOutputInfo) {

		// (B,4,4)=> B,4,4
		now = (String) now.subSequence(1, now.length() - 1);
		String[] nowSplit = now.split(",");
		if (candidateHUL == null) {
			int nowUB = Integer.parseInt(nowSplit[2]);
			int count = 1;
			String[] nextListSplit = nextList.split(" ");
			for (String nextName : nextListSplit) {
				// (B,5,5)=> B,5,5
				try {
					if (count < relatedCoefficient) {
						nextName = (String) nextName.subSequence(1,
								nextName.length() - 1);

						String[] nextNameSplit = nextName.split(",");

						// if same item .(A,1,1) compare (A,2,2)
						if (nowSplit[0].compareTo(nextNameSplit[0]) == 0
								&& nextNameSplit.length >= 2) {

							int nextLB = Integer.parseInt(nextNameSplit[1]);
							// �u�n�i�H�� �B�Lmus �N�[�J
							if (nowUB + 1 == nextLB) {
								count++;
								nowUB++;
								String qItemName = "(" + nowSplit[0] + ","
										+ nowSplit[1] + "," + nowUB + ")";

								int us = utilityCal(qItemName, 0);

								if (us >= muv) {


									huqiCandidateOutputInfo.add("("
											+ qItemName.substring(1,
													qItemName.length() - 1)
											+ ") #UTIL: " + us);
									huqiCandidate.add(qItemName.substring(1,
											qItemName.length() - 1));
									break;
								}

							}
						}// end if same item
					}// end if count
				} catch (Exception e) {
					System.out.println("!" + nextName + "@");
				}
			}// end for

		} else {
			int nowLB = Integer.parseInt(nowSplit[1]);
			int nowUB = Integer.parseInt(nowSplit[2]);
			int count = 1;
			int us = 0;

			UtilityList nowl;
			if (nowLB == nowUB)// A,2,2
			{
				// A,2
				nowl = candidateHUL.get(nowSplit[0] + "," + nowSplit[1]);
			} else {
				nowl = candidateHUL.get(now);
			}
			us = nowl.getSumEU();

			String[] nextListSplit = nextList.split(" ");
			for (String nextName : nextListSplit) {
				if (count < relatedCoefficient) {
					// (B,5,5)=> B,5,5
					nextName = (String) nextName.subSequence(1,
							nextName.length() - 1);

					String[] nextNameSplit = nextName.split(",");
					int nextLB = Integer.parseInt(nextNameSplit[1]);
					int nextUB = Integer.parseInt(nextNameSplit[2]);

					UtilityList nextl;
					if (nextLB == nextUB)// A,2,2
						nextl = candidateHUL.get(nextNameSplit[0] + ","
								+ nextNameSplit[1]);
					else
						// A,2,3
						nextl = candidateHUL.get(nextName);

					// if same item .(A,1,1) compare (A,2,2)
					if (nowSplit[0].compareTo(nextNameSplit[0]) == 0) {
						if (nowUB + 1 == nextLB) {
							us += nextl.getSumEU();
							count++;
							nowUB++;

							String qItemName = "(" + nowSplit[0] + ","
									+ nowSplit[1] + "," + nowUB + ")";

							if (us >= muv) // �p��utilty���L�j��mus
											// ���N�[�J�Կﶵ
							{


								huqiCandidateOutputInfo.add(nowl.getPrefix()
										+ " ("
										+ qItemName.substring(1,
												qItemName.length() - 1) + ") #UTIL: "
										+ us);
								huqiCandidate.add(qItemName.substring(1,
										qItemName.length() - 1));
								break;
							}
						}
					}// end if same item
				}// end if count
			}// end for

		}

	}// end combine()

	private void minChecking(ArrayList<String> huqiCandidate,
			ArrayList<String> huqiCandidateOutputInfo){
		// �u�n�p�϶�
		for (int i = 0; i < huqiCandidate.size(); i++) {
			String[] frontSplit = huqiCandidate.get(i).split(",");
			int frontLB = Integer.parseInt(frontSplit[1]);
			int frontUB = Integer.parseInt(frontSplit[2]);
			for (int j = 0; j < huqiCandidate.size(); j++) {
				if (j == i) {
					continue;
				}
				String[] tailSplit = huqiCandidate.get(j).split(",");

				if (frontSplit[0].compareTo(tailSplit[0]) == 0) {
					int tailLB = Integer.parseInt(tailSplit[1]);
					int tailUB = Integer.parseInt(tailSplit[2]);
					if (frontLB <= tailLB && frontUB >= tailUB) {
						huqiCandidate.remove(i);
						huqiCandidateOutputInfo.remove(i);
						i--;
						break;
					}
				}
			}
		}
	}

	private void combineMax(String info, BufferedWriter writerHUQI,
			ArrayList<String> qItemNameList, ArrayList<String> hwQUI,
			HashMap<String, UtilityList> candidateHUL,
			HashMap<String, UtilityList> nextHUL) throws IOException {
		ArrayList<String> huqiCandidate = new ArrayList<String>();
		ArrayList<String> huqiCandidateOutputInfo = new ArrayList<String>();
		String[] combineCandidate = info.split(" ");

		if (candidateHUL == null)// �S��prefix ���p
		{
			for (int i = 0; i < combineCandidate.length; i++) {
				combineCandidate[i] = (String) combineCandidate[i].subSequence(
						1, combineCandidate[i].length() - 1);// �h�����k�A��
				String[] frontQIemInfo = combineCandidate[i].split(",");

				int hqItemLB = Integer.parseInt(frontQIemInfo[1]);
				int hqItemUB = Integer.parseInt(frontQIemInfo[2]);
				int count = 1;
				int us = 0;

				for (int j = i + 1; j < combineCandidate.length; j++) {
					if (count < relatedCoefficient) {

						String nextQItem = (String) combineCandidate[j]
								.subSequence(1,
										combineCandidate[j].length() - 1);// �h�����k�A��
						String[] nextQIemInfo = nextQItem.split(",");
						if (frontQIemInfo[0].compareTo(nextQIemInfo[0]) == 0) {// Item�ۦP
							int nextUB = Integer.parseInt(nextQIemInfo[2]);
							if (hqItemUB + 1 == nextUB) {
								hqItemUB++;
								count++;
							}
						} else {
							break;
						}
					}
				}
				if (count > 1) {
					String qItemName = "(" + frontQIemInfo[0] + "," + hqItemLB
							+ "," + hqItemUB + ")";
					us = utilityCal(qItemName, 0);
					huqiCandidateOutputInfo.add("("
							+ qItemName.substring(1, qItemName.length() - 1)
							+ ") #UTIL: " + us);
					huqiCandidate.add(qItemName.substring(1,
							qItemName.length() - 1));

				}

			}
		} else {
			for (int i = 0; i < combineCandidate.length; i++) {
				combineCandidate[i] = (String) combineCandidate[i].subSequence(
						1, combineCandidate[i].length() - 1);// �h�����k�A��
				String[] frontQIemInfo = combineCandidate[i].split(",");
				int hqItemLB = Integer.parseInt(frontQIemInfo[1]);
				int hqItemUB = Integer.parseInt(frontQIemInfo[2]);
				int count = 1;
				int us = 0;
				UtilityList nowl;
				if (hqItemLB == hqItemUB)// A,2,2
				{
					// A,2
					nowl = candidateHUL.get(frontQIemInfo[0] + ","
							+ frontQIemInfo[1]);
				} else {
					nowl = candidateHUL.get(combineCandidate[i]);
				}
				us = nowl.getSumEU();

				for (int j = i + 1; j < combineCandidate.length; j++) {
					if (count < relatedCoefficient) {
						String nextQItem = (String) combineCandidate[j]
								.subSequence(1,
										combineCandidate[j].length() - 1);// �h�����k�A��
						String[] nextQIemInfo = nextQItem.split(",");
						if (frontQIemInfo[0].compareTo(nextQIemInfo[0]) == 0) {// Item�ۦP
							int nextLB = Integer.parseInt(nextQIemInfo[1]);
							int nextUB = Integer.parseInt(nextQIemInfo[2]);
							UtilityList nextl;
							if (nextLB == nextUB)// A,2,2
								nextl = candidateHUL.get(nextQIemInfo[0] + ","
										+ nextLB);
							else
								// A,2,3
								nextl = candidateHUL.get(combineCandidate[j]);
							if (hqItemUB + 1 == nextUB) {
								hqItemUB++;
								count++;
								us += nextl.getSumEU();
							}
						} else {
							break;
						}
					}
				}
				if (count > 1) {
					String qItemName = "(" + frontQIemInfo[0] + "," + hqItemLB
							+ "," + hqItemUB + ")";

					huqiCandidateOutputInfo.add(nowl.getPrefix() + " ("
							+ qItemName.substring(1, qItemName.length() - 1)
							+ ") #UTIL: " + us);
					huqiCandidate.add(qItemName.substring(1,
							qItemName.length() - 1));
				}
			}
		}

		maxChecking(huqiCandidate, huqiCandidateOutputInfo);

		writeHUCQI(candidateHUL, nextHUL, huqiCandidate,
				huqiCandidateOutputInfo, writerHUQI, qItemNameList, hwQUI);

		huqiCandidate.clear();
		huqiCandidateOutputInfo.clear();
	}

	private void maxChecking(ArrayList<String> huqiCandidate,
			ArrayList<String> huqiCandidateOutputInfo){

		for (int i = 0; i < huqiCandidate.size(); i++) {
			String[] frontSplit = huqiCandidate.get(i).split(",");
			int frontLB = Integer.parseInt(frontSplit[1]);
			int frontUB = Integer.parseInt(frontSplit[2]);
			for (int j = 0; j < huqiCandidate.size(); j++) {
				if (j == i)
					continue;

				String[] tailSplit = huqiCandidate.get(j).split(",");

				if (frontSplit[0].compareTo(tailSplit[0]) == 0) {
					int tailLB = Integer.parseInt(tailSplit[1]);
					int tailUB = Integer.parseInt(tailSplit[2]);

					if (frontLB >= tailLB && frontUB <= tailUB) {

						huqiCandidate.remove(i);
						huqiCandidateOutputInfo.remove(i);
						i--;

						break;
					}
				}
			}
		}

	}

	private void writeHUCQI(HashMap<String, UtilityList> candidateHUL,
			HashMap<String, UtilityList> nextHUL,
			ArrayList<String> huqiCandidate,
			ArrayList<String> huqiCandidateOutputInfo,
			BufferedWriter writerHUQI, ArrayList<String> qItemNameList,
			ArrayList<String> hwQUI) throws IOException {
		for (int i = 0; i < huqiCandidateOutputInfo.size(); i++) {
			writerHUQI.write(huqiCandidateOutputInfo.get(i) + "\r\n");
			huqiCount++;
			hwQUI.add(huqiCandidate.get(i));

			if (candidateHUL == null) {
				buildUtilityListofCombine(huqiCandidate.get(i), qItemNameList);
			} else {
				String[] temp = huqiCandidate.get(i).split(",");
				if (qItemNameList.contains(temp[0] + "," + temp[2])) {
					int site = qItemNameList.indexOf(temp[0] + "," + temp[2]);
					qItemNameList.add(site, huqiCandidate.get(i));

				}
				buildUtilityListofcombine2(huqiCandidate.get(i), nextHUL);
			}
		}

	}

	/**
	 * Print statistics about the algorithm execution
	 */
	public void printStatistics() {
		System.out.println("=============  VHUQI 2.37 - STATS =============");
		System.out.println(" Total exection time : "
				+ (double) (endTime - startTime) / 1000 + " s");
		System.out.println(" Maximum memory : "
				+ decimalFormat
						.format((long) ((1.0 * maxMemory) / 1024) / 1024)
				+ " MB");
		System.out.println(" Number of high utility quantitative patterns : "
				+ huqiCount);
		System.out.println("==========================================");
	}

}
