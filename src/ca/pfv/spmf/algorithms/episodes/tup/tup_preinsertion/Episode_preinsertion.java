package ca.pfv.spmf.algorithms.episodes.tup.tup_preinsertion;
/* This file is copyright (c) Rathore et al. 2018
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
*/

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Episode_preinsertion {

	ArrayList<String> name = new ArrayList<String>();
	public double utility = 0;
	public double ewu = 0;
	public static List<String> singleLengthEpi = new ArrayList<String>();

	private Map<Occurrence, Double> occUtilityMap = new HashMap<Occurrence, Double>();
	List<Occurrence> occ = new ArrayList<Occurrence>();
	List<Occurrence> minOcc = new ArrayList<Occurrence>();
	public static List<Episode_preinsertion> episodes = new ArrayList<Episode_preinsertion>();

	/**
	 * occurrence of episodes
	 * 
	 */
	public class Occurrence {
		public int startTime;
		public int endTime;

		public Occurrence(int startTime, int endTime) {
			this.startTime = startTime;
			this.endTime = endTime;
		}

		/**
		 * check if two occurrences are same
		 * 
		 */
		public boolean equals(Occurrence o) {
			if (this.startTime == o.startTime && this.endTime == o.endTime)
				return true;
			return false;
		}

		public String toString() {
			String str = this.startTime + " " + this.endTime;
			return str;
		}
	}

	/**
	 * constructor of episode
	 * 
	 */
	public Episode_preinsertion(ArrayList<String> name) {
		this.name = name;
	}

	public static void setEpisodeSet(List<Episode_preinsertion> allEpisode) {
		episodes = allEpisode;
	}

	public Map<Occurrence, Double> getMap() {
		return this.occUtilityMap;
	}

	public void addOccAndUtill(Occurrence minOcc, Double utill) {
		Map<Occurrence, Double> map = this.getMap();
		map.put(minOcc, utill);
	}

	public double getUtility(Occurrence minOcc) {
		return this.getMap().get(minOcc);
	}

	/**
	 * method to get episode utility
	 */
	public double getUtility() {
		return this.utility;
	}

	/**
	 * method to set episode utility
	 */
	public void setUtility(double utility) {
		this.utility = utility;
	}

	/**
	 * method to get event ewu
	 */
	public double getEwu() {
		return this.ewu;
	}

	/**
	 * method to set event ewu
	 */
	public void setEwu(double ewu) {
		this.ewu = ewu;
	}

	/**
	 * method to get particular episode
	 */
	public ArrayList<String> getName() {
		return name;
	}

	/**
	 * method to get particular episode Occurrence
	 */
	public List<Occurrence> getOcc() {
		if (null == this.occ) {
			this.occ = new ArrayList<Occurrence>();
		}
		return this.occ;
	}

	/**
	 * method to set particular episode Occurrence
	 */
	public void setOcc(List<Occurrence> occ) {
		this.occ = occ;
	}

	/**
	 * method to add an episode Occurrence
	 */
	public void addOccurrence(Occurrence occ) {
		this.getOcc().add(occ);
	}

	/**
	 * method to add an episode minimal Occurrence
	 */
	public void addMinOcc(Occurrence occ) {
		this.getMinOcc().add(occ);
	}

	/**
	 * method to get particular episode minimal Occurrence
	 */
	public List<Occurrence> getMinOcc() {
		if (null == this.minOcc) {
			this.minOcc = new ArrayList<Occurrence>();
		}
		return this.minOcc;
	}

	/**
	 * method to set particular episode's minimal Occurrence
	 */
	public List<Occurrence> setMinOccSerial(ArrayList<Occurrence> occList, Map<Occurrence, Double> occUtillMap) {
		// System.out.println(this+" old util map "+oldEpiOccUtillMap+" new
		// utill map "+occUtillMap);
		List<Occurrence> finalMinOccList = new ArrayList<Occurrence>();
		boolean flag = false;
		if (this.getName().size() == 1)
			this.minOcc = occList;
		else {

			for (int i = 0; i < occList.size(); i++) {
				int size = occList.size();
				flag = false;
				Occurrence minOcc = occList.get(i);
				int start = minOcc.startTime;
				int end = minOcc.endTime;
				if (start < end) {
					for (int j = 1; j < size; j++) {
						int inStart = occList.get((i + j) % size).startTime; // modulus
																				// to
																				// access
																				// every
																				// minocc
						int inEnd = occList.get((i + j) % size).endTime;
						if (start <= inStart && inEnd <= end) {
							occList.remove(i);
							i -= 1;
							flag = true;
							break;
						}
					}

					if (!flag) {
						this.addMinOcc(minOcc);
						double utility = occUtillMap.get(minOcc);
						this.addOccAndUtill(minOcc, utility);

						this.addUtility(utility);

					}
				}
			}
		}
		// System.out.println(" epi "+this+" min occ "+this.getMinOcc());
		return finalMinOccList;
	}

	/**
	 * method to get list of all episodes
	 */
	static public List<Episode_preinsertion> allEpisodes() {
		if (null == episodes) {
			episodes = new ArrayList<Episode_preinsertion>();
		}
		return episodes;
	}

	/**
	 * method to add episode to the list
	 */
	public static void addEpisode(Episode_preinsertion e) {
		allEpisodes().add(e);
	}

	/**
	 * method to get a particular episode
	 * 
	 * @param i
	 *            index of episode in list
	 */
	public static Episode_preinsertion getEpisode(int i) {
		return episodes.get(i);
	}

	public String toString() {
		String str = "" + this.name;
		return str;
	}

	/**
	 * method to set event utility
	 */
	public void addUtility(double utility) {
		this.utility += utility;
	}

	static public Episode_preinsertion fromString(String l) {
		int index = contains(l);
		if (index != -1)
			return allEpisodes().get(index);

		ArrayList<String> nameStr = new ArrayList<String>();
		nameStr.add(l);
		Episode_preinsertion oneLengthEpi = new Episode_preinsertion(nameStr);
		addEpisode(oneLengthEpi);
		return oneLengthEpi;
	}

	public void oneLengthEwu() {
		List<Occurrence> minOccList = this.getMinOcc();

		double ewu = 0;
		for (Occurrence minOcc : minOccList) {

			for (int i = minOcc.endTime; i <= minOcc.startTime + AlgoTUP_preinsertion.getMaxTimeDuration() - 1; i++) {

				Sequence_preinsertion s = Database_preinsertion.getSequence(i);
				if (s != null)
					ewu += s.getSeqUtility();

			}
		}

		this.setEwu(ewu);
	}

	/**
	 * method to calculate EWU of an episode
	 */
	public double calculateEwu(List<Occurrence> epiMinOcc) {
		double ewu = 0;
		ArrayList<String> name = this.getName();
		// System.out.println("epi name in ewu "+name+" min occ "+epiMinOcc);
		String lastEpi = name.get(name.size() - 1);
		String events[] = lastEpi.split(" ");
		String lastEvent = events[events.length - 1];
		// System.out.println("last event "+lastEvent);
		double finalEwu = 0;
		double firstPartEwu = this.getUtility();
		double leftFirstUtility = 0;
		// double thirdEwu=0;
		for (Occurrence occ : epiMinOcc) {
			double secondEwu = 0;
			// System.out.println("first ewu "+firstPartEwu);
			int start = occ.startTime;
			int end = occ.endTime;

			for (int j = end; j <= (start + AlgoTUP_preinsertion.getMaxTimeDuration() - 1); j++) {
				Sequence_preinsertion s = Database_preinsertion.getSequence(j);
				if (s != null)
					secondEwu += s.getSeqUtility();
			}
			// System.out.println("second ewu "+secondEwu);
			finalEwu += secondEwu;
			// System.out.println(" second ewu "+secondEwu);
			Sequence_preinsertion seq = Database_preinsertion.getSequence(end);

			// System.out.println("index of last event "+index);
			int lastEpiUtility = 0;
			for (int i = 0; i < events.length; i++) {
				int index = seq.contains(events[i]);
				lastEpiUtility += seq.getUtill(index);
			}

			leftFirstUtility += lastEpiUtility;
		}
		// System.out.println("epi "+this+" ewu before addition"+finalEwu);
		firstPartEwu -= leftFirstUtility;
		finalEwu += firstPartEwu;
		// System.out.println("epi "+this+" ewu after addition "+finalEwu);

		ewu = finalEwu;

		return ewu;
	}

	/**
	 * method to check if two episodes are equal
	 */
	public boolean equals(String epiName) {
		ArrayList<String> name = this.getName();
		for (int i = 0; i < name.size(); i++) {
			if (name.get(i).equals(epiName) || name.get(i).contains(epiName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * method to check if 1-length episode is present in the list
	 * 
	 * @return index of episode in list
	 */
	public static int contains(String epi) {
		// System.out.println("episode in list "+episodes);

		for (int i = 0; i < episodes.size(); i++) {
			Episode_preinsertion e = episodes.get(i);

			if (e.getName().get(0).equals(epi)) {
				// System.out.println("here");
				return i;
			}
		}
		return -1;
	}

	public boolean simultEpiPresent() {
		String epiName = this.getName().get(0);
		String[] tokens = epiName.split(" ");
		Arrays.sort(tokens);

		for (int i = 0; i < singleLengthEpi.size(); i++) {
			String epiPresent = singleLengthEpi.get(i);
			String[] tokensPresent = epiPresent.split(" ");

			if (tokens.length == tokensPresent.length) {
				Arrays.sort(tokensPresent);
				if (Arrays.equals(tokens, tokensPresent)) {
					return true;
				}
			}

		}

		return false;

	}
	
	/**
	 * Get the name of this episode in SPMF format
	 * @return the name
	 */
	public String getFormattedName()  {
		// Convert the name of the episode to the SPMF format:
		// example :   [5 3, 3]   -->  5 3 -1 3
		String episodeName = name.toString();
		episodeName = episodeName.substring(1, episodeName.length()-1);
		episodeName = episodeName.replaceAll(",", " -1");
		
		// Create the string that will be written to file
		StringBuilder buffer = new StringBuilder();
		buffer.append(episodeName);
		buffer.append(" -1");
		
		return buffer.toString();
	}
}
