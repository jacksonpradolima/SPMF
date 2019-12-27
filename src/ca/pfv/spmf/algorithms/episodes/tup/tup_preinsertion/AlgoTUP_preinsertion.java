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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * An implementation of the TUP(Preinsertion) algorithm for mining the top-k high utility episodes. 
 *  This algorithm is described in this paper: <br/><br/>
 *  
 *   Rathore, S.,  Dawar, S., Goyal, V., Patel, D. (2016) "Top-K High Utility Episode Mining from a Complex Event Sequence"<br/><br/>
 *   
 *   This is the original implementation
 */
public class AlgoTUP_preinsertion {
	/** the time the algorithm started */
	private long startTimestamp = 0; 
	
	/** the time the algorithm terminated */
	private long endTimestamp = 0; 
	
	private static double minUtility = 0;
	public static List<Episode_preinsertion> allEpi;
	private Database_preinsertion db;

	private static int maxTimeDuration;

	/**
	 * Constructor of class
	 * 
	 * @param maximumTimeDuration
	 *            maximum time duration
	 * @param inputfile
	 *            name of the transactions file
	 * @return a queue of episodes Queue<Episode_preinsertion>
	 */
	public Queue<Episode_preinsertion> runAlgorithm(String inputfile,  int maximumTimeDuration, int k) {
		startTimestamp = System.currentTimeMillis();
		
		minUtility = 0;
		
		TUPPGlobalVariables.k = k;
		TUPPGlobalVariables.idComparator = new CustomComparator_preinsertion();
		TUPPGlobalVariables.topKBuffer = new PriorityQueue<Episode_preinsertion>(k, TUPPGlobalVariables.idComparator);
		
		setMaxTimeDuration(maximumTimeDuration + 1);   // MODIFIED BY PHILIPPE  :  ADD + 1 to be consistent with US-SPAN
		
		//============================
		// READ THE DATABASE
		///===========================
		readData(inputfile);
		
//		System.out.println("Utility after insertion" + TUPPGlobalVariables.topKBuffer.peek()+TUPPGlobalVariables.topKBuffer.peek().getUtility());
		
		//System.out.println("Utility after insertion" + Main_preinsertion.topKBuffer.peek()+Main_preinsertion.topKBuffer.peek().getUtility());
		TUPPGlobalVariables.topKBuffer.clear();
		// accessing all 1-length episodes and calling MiningHUE
		allEpi = new ArrayList<Episode_preinsertion>(Episode_preinsertion.allEpisodes());

		for (int i = 0; i < allEpi.size(); i++) {
			Episode_preinsertion episode = allEpi.get(i);
			episode.oneLengthEwu();
			// System.out.println(allEpi.get(i)+"ewu is "+allEpi.get(i).getEwu()+" min utility is "+getUtility());
		}
		// System.out.println(ewuQueue);
		for (int j = 0; j < allEpi.size(); j++) {
			Episode_preinsertion e=allEpi.get(j);
			
			if (e.getEwu() >= getUtility()){
				SupportOperations_preinsertion.callSimultHUE(e);
				SupportOperations_preinsertion.callSerialHUE(e);
			}
			
		}
		
		endTimestamp = System.currentTimeMillis();

		return TUPPGlobalVariables.topKBuffer;
	}
	
	/**
	 * Print statistics about the algorithm execution
	 */
	public void printStats() {
		System.out.println("=============  TUP(Preinsertion) v2.23- STATS =============");
		System.out.println(" k = " + TUPPGlobalVariables.k);
		System.out.println(" Number of episodes found = " + TUPPGlobalVariables.topKBuffer.size());
//		System.out.println(" Total number of considered episodes : " + Episode_preinsertion.allEpisodes().size());
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println("===================================================");
	}

	/**
	 * Read sequence and profit of each item from input data files
	 * 
	 * @param inputfile   name of transactions file
	 */
	private void readData(String inputfile) {
		String line;
		try {

			HashMap<String, Double> externalUtill = new HashMap<String, Double>();

//			=============  TUP(Preinsertion) v2.23- STATS =============
//					 k = 3
//					 Total number of episodes : 13
//					 Total time ~ 29 ms
//					===================================================
//					episode 0: [5 3, 3] utility: 24.0 EWU: 24.0
//					episode 1: [5 4, 2] utility: 29.0 EWU: 29.0
//					episode 2: [5, 2] utility: 26.0 EWU: 26.0

			FileReader fReader = new FileReader(inputfile);
			BufferedReader reader = new BufferedReader(fReader);
			int seqCount = 0;
			db = new Database_preinsertion();
			while ((line = reader.readLine()) != null) {
				// if the line is  a comment, is  empty or is a
				// kind of metadata
				if (line.isEmpty() == true ||
						line.charAt(0) == '#' || line.charAt(0) == '%'
								|| line.charAt(0) == '@') {
					continue;
				}
				
				// split the line according to the separator
				String split[] = line.split(":");
				// get the list of items
				String items[] = split[0].split(" ");
				// get the list of utility values corresponding to each item
				// for that transaction
				String utilityValues[] = split[2].split(" ");
				
				seqCount++;
				// System.out.println("sequence number is"+seqCount);
				Sequence_preinsertion seq = new Sequence_preinsertion();
				seq.addID(seqCount);
				seq.fromString(items, utilityValues);
				db.addSequence(seqCount, seq);
			}

			reader.close();
//			extUtil.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * method to get minimum utility threshold
	 */
	public static double getUtility() {
		return minUtility;
	}

	/**
	 * method to set minimum utility threshold
	 * 
	 * @minUtility threshold given by user
	 */
	public static void setUtility(double minUtil) {
		// System.out.println("min utill is "+minUtility);
		minUtility = minUtil;
		// System.out.println("min utill is "+minUtility);
	}

	/**
	 * method to get external utility/profit of an episode
	 * 
	 * @param name
	 *            of the episode
	 * @param externalUtill 
	 */
	public static double getExternalUtility(String name, HashMap<String, Double> externalUtill) {
		double temp = 0;
		try {
			temp = externalUtill.get(name);
		} catch (Exception e) {

			System.out.println("Ex: " + name);
			throw e;
		}
		return temp;
	}

	/**
	 * method to set Maximum Time Duration
	 * 
	 * @param m
	 *            MTD set by user
	 */
	public void setMaxTimeDuration(int m) {
		maxTimeDuration = m;
	}

	/**
	 * method to get Maximum Time Duration
	 */
	public static int getMaxTimeDuration() {
		return maxTimeDuration;
	}

	/**
	 * Write the result by the last execution of the method "runAlgorithm" to an output file using the SPMF format
	 * @param path the output file path
	 * @throws IOException exception if an error occur when writing the file.
	 */
	public void writeResultTofile(String path) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(path)); 
		
		for (Episode_preinsertion episode : TUPPGlobalVariables.topKBuffer) {
			
			// Convert the name of the episode to the SPMF format:
			// example :   [5 3, 3]   -->  5 3 -1 3
			String episodeName = episode.toString();
			episodeName = episodeName.substring(1, episodeName.length()-1);
			episodeName = episodeName.replaceAll(",", " -1");
			
			// Create the string that will be written to file
			StringBuilder buffer = new StringBuilder();
			buffer.append(episodeName);
			buffer.append(" -1 #UTIL: ");
			buffer.append(episode.getUtility());
			
			// Write to file
			writer.write(buffer.toString());
			writer.newLine();
		}
		writer.close();
	}

}
