package ca.pfv.spmf.algorithms.frequentpatterns.MRCPPS;

import java.util.ArrayList;
import java.util.List;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

/*
 * This file is part of the SPMF DATA MINING SOFTWARE *
 * (http://www.philippe-fournier-viger.com/spmf).
 *
 * SPMF is free software: you can redistribute it and/or modify it under the *
 * terms of the GNU General Public License as published by the Free Software *
 * Foundation, either version 3 of the License, or (at your option) any later *
 * version. SPMF is distributed in the hope that it will be useful, but WITHOUT
 * ANY * WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright Peng Yang, Philippe Fournier-Viger, 2019
 */
/**
 * An RCPPS list
 * 
 * @see AlgoMRCPPS
 * @author Peng Yang
 */
public class RCPPSlist {

	/** sequence id list */
	private List<Integer> sidList;

	/** Conjunctive tid-list */
	private List<List<Integer>> listConTIDlist;

	/** Disconjunctive tid-list */
	private List<List<Integer>> listDisTIDlist;

	/**
	 * Constructor
	 */
	RCPPSlist() {
		this.listConTIDlist = new ArrayList<>();
		this.sidList = new ArrayList<>();
		this.listDisTIDlist = new ArrayList<>();
	}

	/**
	 * Get the size of the SID list
	 * 
	 * @return the size
	 */
	public int getSizeOfSIDlist() {
		return this.sidList.size();
	}

	/**
	 * Add a SID to the list
	 * 
	 * @param sid the SID
	 */
	public void addSID(int sid) {
		// if the sid has been used, we skip it
		if (sidList.size() > 0 && sidList.get(sidList.size() - 1) == sid) {
			return;
		}
		sidList.add(sid);
		listConTIDlist.add(new ArrayList<Integer>());
		listDisTIDlist.add(new ArrayList<Integer>());
	}

	/**
	 * Add a transaction ID
	 * 
	 * @param tid the transaction ID
	 */
	public void addTID(int tid) {
		// if the tid is same, we skip it.
		int lastIndex = this.listConTIDlist.get(this.getSizeOfSIDlist() - 1).size() - 1;
		if (lastIndex > 0 && this.listConTIDlist.get(this.getSizeOfSIDlist() - 1).get(lastIndex) == tid) {
			return;
		}
		listConTIDlist.get(this.getSizeOfSIDlist() - 1).add(tid);
		listDisTIDlist.get(this.getSizeOfSIDlist() - 1).add(tid);
	}

	/**
	 * Get the RCPPSlist of some candidate
	 * 
	 * @param alpha   the RCPPSlist of alpha
	 * @param minBond the minbond
	 * @return the RCPPSlist
	 */
	public RCPPSlist genRCPPSlistOfCandidate(RCPPSlist alpha, double minBond) {
		RCPPSlist candidate = new RCPPSlist();
		// i to index of this.SIDlist, j to index of alpha of SIDlist
		int i = 0;
		int j = 0;
		while (i < this.sidList.size() && j < alpha.sidList.size()) {
			if (this.sidList.get(i) < alpha.sidList.get(j)) {
				// if the sequence id of this less than the sequence id of alpha
				i++;
			} else if (this.sidList.get(i) > alpha.sidList.get(j)) {
				// if the sequence id of this greater than the sequence id of alpha
				j++;
			} else {
				// equal
				int currentSID = this.sidList.get(i);

				List<Integer> iThOfconTIDlist = this.listDisTIDlist.get(i);
				List<Integer> jThOfconTIDlist = alpha.getListConTIDlist().get(j);
				List<Integer> conTIDlistOfCandidate = getConjunctiveList(iThOfconTIDlist, jThOfconTIDlist);
				if (conTIDlistOfCandidate != null && conTIDlistOfCandidate.size() > 0) {
					// if they have intersection
					List<Integer> iThOfdisTIDlist = this.listDisTIDlist.get(i);
					List<Integer> jThOfdisTIDlist = alpha.getListDisTIDlist().get(j);
					List<Integer> disTIDlistOfCandidate = getDisconjunctiveList(iThOfdisTIDlist, jThOfdisTIDlist);

					double bond = (double) conTIDlistOfCandidate.size() / (double) disTIDlistOfCandidate.size();

					if (bond >= minBond) {
						// if bond(X,SID) >=maxBond, then X is candidate in this SID
						candidate.getSIDlist().add(currentSID);
						candidate.getListConTIDlist().add(conTIDlistOfCandidate);
						candidate.getListDisTIDlist().add(disTIDlistOfCandidate);
					}

				}

				i++;
				j++;
			}
		}
		return candidate;
	}

	/**
	 * Calculate the disjunctive list of two list of integers
	 * 
	 * @param a a list
	 * @param b another list
	 * @return the result (intersection of a and b)
	 */
	public List<Integer> getDisconjunctiveList(List<Integer> a, List<Integer> b) {
		List<Integer> res = new ArrayList<>();
		if (a == null && b == null)
			return res;
		if (a == null) {
			res.addAll(b);
			return res;
		}
		if (b == null) {
			res.addAll(a);
			return res;
		}

		int ai = 0;
		int bi = 0;
		while (ai < a.size() && bi < b.size()) {
			if (a.get(ai) < b.get(bi)) {
				res.add(a.get(ai));
				ai++;
			} else if (a.get(ai) > b.get(bi)) {
				res.add(b.get(bi));
				bi++;
			} else {
				res.add(a.get(ai));
				ai++;
				bi++;
			}
		}
		if (ai < a.size()) {
			for (; ai < a.size(); ai++) {
				res.add(a.get(ai));
			}
		}
		if (bi < a.size()) {
			for (; bi < b.size(); bi++) {
				res.add(b.get(bi));
			}
		}
		return res;
	}

	/**
	 * Calculate the conjunctive list of two list of integers
	 * 
	 * @param a a list
	 * @param b another list
	 * @return the result (conjunction of a and b)
	 */
	public List<Integer> getConjunctiveList(List<Integer> a, List<Integer> b) {
		List<Integer> res = new ArrayList<>();
		if (a == null || b == null || a.size() <= 0 || b.size() <= 0)
			return res;

		int ai = 0;
		int bi = 0;
		while (ai < a.size() && bi < b.size()) {
			if (a.get(ai) < b.get(bi)) {
				ai++;
			} else if (a.get(ai) > b.get(bi)) {
				bi++;
			} else {
				res.add(a.get(ai));
				ai++;
				bi++;
			}
		}
		return res;
	}

	/**
	 * Get the number of sequences where the pattern respect the parameters minsup,
	 * maxStd etc.
	 * 
	 * @param maxSup       the maximum support
	 * @param maxStd       the maximum standard deviation
	 * @param lenOfseqList contains the length of each sequence (index is the
	 *                     sequence id from zero to )
	 * @param useLemma2    if true, the lemma 2 is used. Otherwise not.
	 * @return the number of sequences
	 */
	public int getNumSeq(double maxSup, double maxStd, List<Integer> lenOfseqList, boolean useLemma2) {
		// to check whether the candidate is both rare and periodic
		// and from 'genRCPPSlistOfCandidate', we know that the candidate is correlated
		// in each sequence
		int numSeq = 0;
		for (int i = 0; i < this.sidList.size(); i++) {
			int sid = sidList.get(i);
			List<Integer> conTIDlist = this.listConTIDlist.get(i);

			int lengthOfCurrentSequence = lenOfseqList.get(sid);
			double stanDev = getStanDevFromTIDlist(conTIDlist, lengthOfCurrentSequence, useLemma2);
			int sup = conTIDlist.size();
			if (sup <= maxSup && stanDev <= maxStd) {
				numSeq++;
			}
		}
		return numSeq;
	}

	/**
	 * Get the number of candidates
	 * 
	 * @return the number of candidates
	 */
	public int getNumCand() {
		// because we only save the correlated items in each sequence in the process of
		// 'genRCPPSlistOfCandidate'.
		return this.sidList.size();
	}

	/**
	 * Calculate the standard deviation from the TID list
	 * 
	 * @param conTIDlist              a conjunctive TID list
	 * @param lengthOfCurrentSequence the length of the sequence
	 * @param useLemma2               if true, the Lemma 2 is applied. Otherwise
	 *                                not.
	 * @return the standard deviation
	 */
	public double getStanDevFromTIDlist(List<Integer> conTIDlist, int lengthOfCurrentSequence, boolean useLemma2) {

		double stanDev = 0;
		int preTID = 0; // note that the smallest of TID is 1
		if (useLemma2) {
			for (int i = 0; i < conTIDlist.size(); i++) {
				int perI = conTIDlist.get(i) - preTID;
				stanDev = stanDev + Math.pow(perI, 2);
				preTID = conTIDlist.get(i);
			}
			// for per_(k+1)^2
			stanDev = stanDev + Math.pow(lengthOfCurrentSequence - preTID, 2);

			stanDev = stanDev / (double) (conTIDlist.size() + 1);
			stanDev = stanDev - Math.pow((double) lengthOfCurrentSequence / (double) (conTIDlist.size() + 1), 2);
			stanDev = Math.sqrt(stanDev);
		} else {
			double avgPer = 0;
			for (int i = 0; i < conTIDlist.size(); i++) {
				int perI = conTIDlist.get(i) - preTID;
				avgPer = avgPer + perI;
				preTID = conTIDlist.get(i);
			}
			// for per_{k+1}
			avgPer = avgPer + lengthOfCurrentSequence - preTID;

			avgPer = avgPer / (double) (conTIDlist.size() + 1);

			preTID = 0;
			for (int i = 0; i < conTIDlist.size(); i++) {
				int perI = conTIDlist.get(i) - preTID;
				stanDev = stanDev + Math.pow(perI - avgPer, 2);
				preTID = conTIDlist.get(i);
			}
			// for per_{k+1}
			stanDev = stanDev + Math.pow(lengthOfCurrentSequence - preTID - avgPer, 2);
			stanDev = stanDev / (double) (conTIDlist.size() + 1);
			stanDev = Math.sqrt(stanDev);
		}
		return stanDev;
	}

	/**
	 * Get the Conjunctive tid-list
	 * 
	 * @return the Conjunctive tid-list
	 */
	public List<List<Integer>> getListConTIDlist() {
		return listConTIDlist;
	}

	/**
	 * Get the Conjunctive tid-list
	 * 
	 * @return the Conjunctive tid-list
	 */
	public List<List<Integer>> getListDisTIDlist() {
		return listDisTIDlist;
	}

	/**
	 * Get the list of SIDs
	 * 
	 * @return the list of SID
	 */
	public List<Integer> getSIDlist() {
		return sidList;
	}

	/**
	 * Get details about the list of sequences
	 * 
	 * @param lenOfseqList the list of sequences lengths.
	 * @return the details as a string
	 */
	public String getDetails(List<Integer> lenOfseqList, double minBond, double maxSup, double maxStd) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(" #SIDOCC: ");
//		buffer.append(" #<SID,sup,bond,stanDev>:");
		for (int z = 0; z < this.sidList.size(); z++) {
			int sid = sidList.get(z);
			int lenOfs = lenOfseqList.get(sid);
			List<Integer> conTIDlist = this.listConTIDlist.get(z);
			List<Integer> disTIDlist = this.listDisTIDlist.get(z);

			int sup = conTIDlist.size();
			double bond = (double) sup / (double) disTIDlist.size();

			double stanDev = 0;
			int preTID = 0;
			for (int i = 0; i < conTIDlist.size(); i++) {
				int perI = conTIDlist.get(i) - preTID;
				stanDev = stanDev + Math.pow(perI, 2);
				preTID = conTIDlist.get(i);
			}
			// for per_(k+1)^2
			stanDev = stanDev + Math.pow(lenOfs - preTID, 2);

			stanDev = stanDev / (double) (conTIDlist.size() + 1);
			stanDev = stanDev - Math.pow((double) lenOfs / (double) (conTIDlist.size() + 1), 2);
			stanDev = Math.sqrt(stanDev);

//			buffer.append(" #BOND:" + bond + " #SUP: " + sup + " #STDEV: " + stanDev);
			//buffer.append(" < " + sid + " , " + sup + " , " + bond + " , " + stanDev + " > ");
			// print sids
			if(bond >= minBond && sup <= maxSup && stanDev <= maxStd) {
				buffer.append(sid);
				for(Integer tid : conTIDlist){
					buffer.append('[');
					buffer.append(tid-1);
					buffer.append("] ");
				}
			}
		}
		return buffer.toString();
	}
}
