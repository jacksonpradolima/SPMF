package ca.pfv.spmf.algorithms.frequentpatterns.vhuqi;

import java.util.ArrayList;

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
 * This is an implementation of a Utility list
 * 
 * @see AlgoVHUQI
 * @author Cheng Wei wu et al.
 */

class UtilityList {
	/** a prefix */
	private String prefix;

	/** an item name */
	private String itemName;

	/** sum of estimated utility */
	private int sumEU;

	/** sum of remaining utility */
	private int sumRU;

	/** max of EU and RU utility */
	private int maxEURU;

	/** TWU */
	private int twu;

	/** List of QItems */
	private ArrayList<QItemTrans> qItemTrans = null;

	/**
	 * Constructor
	 * 
	 * @param prefix
	 *            prefix
	 * @param name
	 *            item name
	 * @param twu
	 *            the TWU
	 */
	UtilityList(String prefix, String name, int twu) {
		this.prefix = prefix;
		this.itemName = name;
		this.sumEU = 0;
		this.sumRU = 0;
		this.maxEURU = 0;
		this.twu = twu;
		qItemTrans = new ArrayList<QItemTrans>();
	}

	void addTWU(int twu) {
		this.twu += twu;
	}

	void setTWU() {
		this.twu = 0;
	}

	void addTrans(QItemTrans qTid, int twu) {
		this.sumEU += qTid.getEu();
		this.sumRU += qTid.getRu();
		qItemTrans.add(qTid);
		maxEURU(qTid.getEu(), qTid.getRu());
		this.twu += twu;
	}

	void addTrans(QItemTrans qTid) {
		this.sumEU += qTid.getEu();
		this.sumRU += qTid.getRu();
		qItemTrans.add(qTid);
		maxEURU(qTid.getEu(), qTid.getRu());
	}

	void maxEURU(int eu, int ru) {
		if (this.maxEURU < eu + ru)
			this.maxEURU = eu + ru;
	}

	int getMaxEURU() {
		return maxEURU;
	}

	int getSumEU() {
		return sumEU;
	}

	int getSumRU() {
		return sumRU;
	}

	int getTwu() {
		return twu;
	}

	String getName() {
		return itemName;
	}

	String getFullName() {
		return prefix + " " + itemName;
	}

	String getPrefix() {
		return prefix;
	}

	ArrayList<QItemTrans> getQItemTrans() {
		return qItemTrans;
	}

	// �u��combining ���p�~�|�Ψ�
	void addNoPrefixUtilityList(UtilityList next) {
		this.sumEU += next.getSumEU();
		this.sumRU += next.getSumRU();
		this.twu += next.getTwu();

		if (next.getMaxEURU() > this.maxEURU) {
			this.maxEURU = next.getMaxEURU();
		}
	}

	// �u��combining ���p�~�|�Ψ�
	void addNoPrefixUtilityListQItemTrans(UtilityList next) {
		ArrayList<QItemTrans> temp = next.getQItemTrans();
		ArrayList<QItemTrans> mainlist = new ArrayList<QItemTrans>();

		if (qItemTrans.size() == 0) {
			for (int k = 0; k < temp.size(); k++) {
				qItemTrans.add(temp.get(k));
			}
		} else {
			int i = 0, j = 0;
			// System.out.println("qItemTrans="+qItemTrans.size()+" temp="+temp.size());

			while (i < qItemTrans.size() && j < temp.size()) {
				int t1 = qItemTrans.get(i).getTid();
				int t2 = temp.get(j).getTid();
				if (t1 > t2) {
					mainlist.add(temp.get(j));
					j++;
				} else {
					mainlist.add(qItemTrans.get(i));
					i++;
				}

			}
			if (i == qItemTrans.size()) {
				while (j < temp.size()) {
					mainlist.add(temp.get(j++));
				}
			} else if (j == temp.size()) {
				while (i < qItemTrans.size()) {
					mainlist.add(qItemTrans.get(i++));
				}
			}
			qItemTrans.clear();
			qItemTrans = mainlist;

		}

		// System.out.println("!!�����Ƨ�="+mainlist+"\n");
		// System.out.println("!!�����Ƨ�="+qItemTrans);
	}

	// �u��combining ���p�~�|�Ψ�
	void addPrefixUtilityList(UtilityList next) {
		ArrayList<QItemTrans> temp = next.getQItemTrans();
		ArrayList<QItemTrans> mainlist = new ArrayList<QItemTrans>();

		this.sumEU += next.getSumEU();
		this.sumRU += next.getSumRU();
		this.twu += next.getTwu();

		if (next.getMaxEURU() > this.maxEURU) {
			this.maxEURU = next.getMaxEURU();
		}

		if (qItemTrans.size() == 0) {
			for (int k = 0; k < temp.size(); k++) {
				qItemTrans.add(temp.get(k));
			}
		} else {
			int i = 0, j = 0;
			// System.out.println("qItemTrans="+qItemTrans.size()+" temp="+temp.size());

			while (i < qItemTrans.size() && j < temp.size()) {
				int t1 = qItemTrans.get(i).getTid();
				int t2 = temp.get(j).getTid();
				if (t1 > t2) {
					mainlist.add(temp.get(j));
					j++;
				} else {
					mainlist.add(qItemTrans.get(i));
					i++;
				}

			}
			if (i == qItemTrans.size()) {
				while (j < temp.size()) {
					mainlist.add(temp.get(j++));
				}
			} else if (j == temp.size()) {
				while (i < qItemTrans.size()) {
					mainlist.add(qItemTrans.get(i++));
				}
			}
			qItemTrans.clear();
			qItemTrans = mainlist;

		}

		// System.out.println("!!�����Ƨ�="+mainlist+"\n");
		// System.out.println("!!�����Ƨ�="+qItemTrans);
	}

	public String toString() {
		String str = prefix + " " + itemName + "\r\n";
		str += "sumEU=" + sumEU + " sumRU=" + sumRU + " maxEURU=" + maxEURU
				+ " twu=" + twu + "\r\n";
		for (int i = 0; i < qItemTrans.size(); i++) {
			str += qItemTrans.get(i).toString() + "\r\n";
		}
		return str;
	}

	int getqItemTransLength() {
		if (qItemTrans == null)
			return 0;
		else
			return qItemTrans.size();
	}
}
