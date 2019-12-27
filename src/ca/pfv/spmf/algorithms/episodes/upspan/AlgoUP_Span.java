package ca.pfv.spmf.algorithms.episodes.upspan;

/* This file is copyright (c) Wu et al. 2013
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
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ca.pfv.spmf.tools.MemoryLogger;

/**
 * Implementation of the UP-SPAN algorithm by Wu et al. Obtained from the
 * UP-Miner software under the GPL 3 license.
 * 
 * @author Wu et al. ౮p V01.2013.01.29
 * 
 *         Code cleaned and adapted by Philippe Fournier-Viger (2018) for the
 *         integration in SPMF.
 *
 */
public class AlgoUP_Span {
	/** the time the algorithm started */
	private long startTimestamp = 0;

	/** the time the algorithm terminated */
	private long endTimestamp = 0;

	/** If true the single events will be output */
	boolean outputSingleEvents = false;

	/////// �t�ΰѼ�
	int allCalculateCount = 0;
	
	/** �ɶ��I */
	long timePoint = 1112949;
	
	/** Event type�Ӽ� */
	int eventType = 50000;
	
	/** Min. Utility���Ӽ� */
	double minUtility = 0.9;

	/** Input file path */
	String inputFile;
	
	/** Output file path */
	String outputFile;

	/** Maximum time duration */
	int maximumTimeDuration = 4;

	/** List of single events */
	ArrayList<Integer> freF1 = new ArrayList<Integer>();
	
	/** �bMax. time duration���d�򤺤�Total Utility */
	ArrayList<Integer> totalUtilityByTimeAndDuration = new ArrayList<Integer>();
	
	/** �s�C�Ӯɶ��I��Total Utility */
	ArrayList<Integer> totalUtilityByTime = new ArrayList<Integer>();
	
	/** Total utility in the database */
	long totalUtilityinAllSequence = 0;
	
	/** �s�C�Ӯɶ��I��Event Utility  ArrayList���ɶ����ǡAEvent����Utility�h�ϥ�map�Ӧs*/
	ArrayList<HashMap<Integer, Integer>> eventUtilityByTime = new ArrayList<HashMap<Integer, Integer>>();

	/** �b�p��F1���̤jUtility(�����)  <F1,Total utility>*/
	HashMap<Integer, Integer> F1TotalUtilitybackward = new HashMap<Integer, Integer>(); 
	/** �b�p��F1���̤jUtility(����(�e�ᤤ)����)  <F1,Total utility>*/
	HashMap<Integer, Integer> F1TotalUtility = new HashMap<Integer, Integer>();
	
	/** �b�p��F1���̤jUtility(����(�e�ᤤ)����)  <F1,Total  utility>*/
	HashMap<Integer, HashSet<Integer>> F1TotalUtilityTime = new HashMap<Integer, HashSet<Integer>>(); 
	
    /** event �X�{������ */
	int[] EventCount;
	
	/** event   �{�b���X��time  point */
	HashMap<Integer, ArrayList<Integer>> eventTID = new HashMap<Integer, ArrayList<Integer>>();
	
	/** �o��time  point������event */
	HashMap<Integer, ArrayList<Integer>> freDB = new HashMap<Integer, ArrayList<Integer>>();

	////// output���F��/////
	ArrayList<String> FreEP = new ArrayList<String>();
	ArrayList<Integer> EPCount = new ArrayList<Integer>();

	long Num_FreEP = 0;

	/** Number of candidates found */
	long numberOfCandidates = 0;

	/** Number of episodes found */
	int numberOfEpisodes = 0;
	
	/** Number of single events found */
	int numberOfSingleEvents = 0;

	/**
	 * Constructor
	 */
	public AlgoUP_Span() {

	}

	/**
	 * Run this algorithm
	 * 
	 * @param inputFile
	 *            an input file path
	 * @param outputFile
	 *            an output file path
	 * @param minimumUtility
	 *            a minimum utility threshold (percentage as double value)
	 * @param maximumTimeDuration
	 *            a minimum time duration
	 * @param outputSingleEvents
	 *            if true, the single events will be added to the output file.
	 */
	public void runAlgorithm(String inputFile, String outputFile, double minimumUtility, int maximumTimeDuration,
			boolean outputSingleEvents) {

		// Reset logger for memory usage
		MemoryLogger.getInstance().reset();

		startTimestamp = System.currentTimeMillis();

		// Calculate statistics about the database, required by the algorithm
		CalculateDatabaseInfo cal = new CalculateDatabaseInfo(inputFile);
		cal.runCalculate();

		// Database size
		timePoint = cal.getDBSize();

		// Number of Event IDs
		eventType = cal.getMaxID();

		// Minimum utility threshold
		minUtility = minimumUtility;

		// input file path
		this.inputFile = inputFile;

		// Maximum time duration
		this.maximumTimeDuration = maximumTimeDuration;

		// Parameter to determine if single events should be output as well
		this.outputSingleEvents = outputSingleEvents;

		// Output file path
		this.outputFile = outputFile;

		// Search for patterns
		MiningProcess();

		// Check memory usage
		MemoryLogger.getInstance().checkMemory();

		// Record the time that the algorithm finished its job
		endTimestamp = System.currentTimeMillis();

	}

	/**
	 * Print statistics about the algorithm execution
	 */
	public void printStats() {
		System.out.println("=============  UP-SPAN v2.23- STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Number of high utility episodes = " + this.numberOfEpisodes);
		System.out.println(" Maximum memory : " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		if(outputSingleEvents){
			System.out.println(" Number of high utility single events = " + this.numberOfSingleEvents);
		}
		System.out.println(" Number of candidates = " + this.numberOfCandidates);
		System.out.println("===================================================");
	}

	/**
	 * Search for patterns
	 */
	private void MiningProcess() {
		Thread t1 = new Thread() {
			public void run() {

				EventCount = new int[eventType + 1];// event �X�{������

				ReadFileToGetF1(inputFile);
				
				// System.out.println("Number of FreF1: "+FreF1.size());
				// outputF1();
				PruneF1InDB();

				Mining();

				saveResultToFile();
			}
		};

		t1.start();

		MemoryLogger.getInstance().checkMemory();

		try {
			t1.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Read file to calculate various information about single events
	 * @param filename input file path
	 */
	private void ReadFileToGetF1(String filename) {
		/** �ŧiŪ�ɻݭn���ܼ� */
		File file = new File(filename);
		FileInputStream fis = null;
		DataInputStream dis = null;
		BufferedReader br = null;

		/** Ū�ɱo��Ҧ�event��support count */
		try {
			fis = new FileInputStream(file);
			dis = new DataInputStream(fis);
			br = new BufferedReader(new InputStreamReader(dis));

			String line;// ��J�ɪ��@���r
			int LineNumber = 0;// �ĴX��
			totalUtilityByTime.add(0, 0);
			eventUtilityByTime.add(0, null);
			totalUtilityByTimeAndDuration.add(0);
			while ((line = br.readLine()) != null) {
				LineNumber++;
				// System.out.println(LineNumber);
				/**
				 * ��Ƥ���(Event/�Ů���}):(Total Utility/�u����@��):(Event Utility/�Ů���})
				 */
				String[] target = line.split(":"); /** �N��Ʃ�T�� */
				String[] element = target[0].split(" "); /** �Ĥ@���OEvent */
				String TotalUtility = target[1]; /** �ĤG���OTotal Utility */
				String[] elementUtitle = target[2]
						.split(" "); /** �ĤG���O����Event��Utility */

				HashMap<Integer, Integer> EventUtility = new HashMap<Integer, Integer>();

				freDB.put(LineNumber, new ArrayList<Integer>());

				if (element.length > 0) // �N���ɶ����o��event
				{
					for (int i = 0; i < element.length; i++) {
						int event = Integer.valueOf(element[i]);
						int utility = Integer.valueOf(elementUtitle[i]);
						EventUtility.put(event, utility);
						EventCount[event] += utility;// �p��F1��Utility

						// �Nevent���X�{�b���Ǯɶ��I�s�J��EventTid & FreDB
						if (eventTID.get(event) == null) {
							eventTID.put(event, new ArrayList<Integer>());
						}
						eventTID.get(event).add(LineNumber);
						freDB.get(LineNumber).add(event);
					}

					// �O���C�@�檺total utility
					totalUtilityByTime.add(LineNumber, Integer.valueOf(TotalUtility));
					// �֥[�C�@�檺total utility
					totalUtilityinAllSequence += Integer.valueOf(TotalUtility);
					// �O���C�@��U�Oevent��utility
					eventUtilityByTime.add(LineNumber, EventUtility);

					// �p��bMax. time duration���d�򤺤�Total Utility
					totalUtilityByTimeAndDuration.add(Integer.valueOf(TotalUtility));
					if (LineNumber <= maximumTimeDuration) {
						for (int R = 1; R <= maximumTimeDuration; R++) {
							if ((LineNumber - R) < 1)
								break;
							else
								totalUtilityByTimeAndDuration.set((LineNumber - R),
										totalUtilityByTimeAndDuration.get(LineNumber - R)
												+ Integer.valueOf(TotalUtility));
							// TotalUtilityByTimeAndDuration[(LineNumber-R)] +=
							// Integer.valueOf(TotalUtility);
						}
					} else {
						for (int R = 1; R <= maximumTimeDuration; R++) {
							totalUtilityByTimeAndDuration.set((LineNumber - R),
									totalUtilityByTimeAndDuration.get(LineNumber - R) + Integer.valueOf(TotalUtility));
							// TotalUtilityByTimeAndDuration[(LineNumber-R)] +=
							// Integer.valueOf(TotalUtility);
						}
					}
				}
			}
			fis.close();
			dis.close();
			br.close();

			// �p��Ҧ�F1��total backward utility
			for (int L = 1; L < eventUtilityByTime.size(); L++) {

				Set<Integer> AllEventAtTime = eventUtilityByTime.get(L).keySet();
				for (Integer Event : AllEventAtTime) {
					// ��������ݪ��ɶ�
					if (!F1TotalUtilitybackward.containsKey(Event))
						F1TotalUtilitybackward.put(Event, totalUtilityByTimeAndDuration.get(L));// TotalUtilityByTimeAndDuration[L]
					else
						F1TotalUtilitybackward.put(Event,
								F1TotalUtilitybackward.get(Event) + totalUtilityByTimeAndDuration.get(L));// TotalUtilityByTimeAndDuration[L]

					// ��o�{event�Ҳ[�\�쪺Windows���ɶ����O���_��
					int StartRange = L - maximumTimeDuration;
					if (StartRange < 1)
						StartRange = 1;
					int EndRange = L + maximumTimeDuration;
					if (EndRange >= eventUtilityByTime.size())
						EndRange = eventUtilityByTime.size() - 1;

					for (int range = StartRange; range <= EndRange; range++) {
						if (!F1TotalUtilityTime.containsKey(Event)) {
							HashSet<Integer> Windows = new HashSet<Integer>();
							Windows.add(range);
							F1TotalUtilityTime.put(Event, Windows);
						} else {
							F1TotalUtilityTime.get(Event).add(range);
						}
					}

				}

			}

			Set<Integer> AllEventAtTime = F1TotalUtilityTime.keySet();
			for (Integer Event : AllEventAtTime) {
				int Total = 0;
				for (int i = 0; i < F1TotalUtilityTime.get(Event).size(); i++)
					Total += totalUtilityByTimeAndDuration.get(i);// TotalUtilityByTimeAndDuration[i]

				F1TotalUtility.put(Event, Total);
			}

		} catch (FileNotFoundException e) {
			System.out.println("�䤣���ɮ�:" + e);
		} catch (IOException e) {
			System.out.println("Ū�ɵo�Ϳ��~:" + e);
		}
		// endŪ��

		//// �o��F1
		for (int i = 0; i < eventType + 1; i++) {
			// if(EventCount[i]>=min_utility)
			if (EventCount[i] > 0) {
				// System.out.println("Event:"+i+" Utility:"+EventCount[i]);
				freF1.add(i);
			}
		}
		F1TotalUtilityTime.clear();
		// System.out.println(TotalUtilityinAllSequence);
		// System.out.println(F1TotalUtilityTime);
		// end�o��F1
	}

	/**
	 * Prune some single events that are unpromising from the database
	 */
	private void PruneF1InDB() {
		/** �s����Min_Utility��F1������T */
		ArrayList<Integer> WPurneF1 = new ArrayList<Integer>();

		// System.out.println("�C�X�Ĥ@���q�Qprune��event");
		for (int j = (freF1.size() - 1); j >= 0; j--) {
			if (F1TotalUtility.get(freF1.get(j)) < (minUtility * totalUtilityinAllSequence)) {
				WPurneF1.add(freF1.get(j));
				// System.out.print(FreF1.get(j)+",");
				eventTID.remove(freF1.get(j));
				freF1.remove(j);
			}
		}

		if (WPurneF1.size() > 0) {
			// �@��@��Ū�X��
			for (int LineNumber = 1; LineNumber < totalUtilityByTime.size(); LineNumber++) {
				// ��F1��prune
				for (int j = 0; j < WPurneF1.size(); j++) {
					if (eventUtilityByTime.get(LineNumber).containsKey(WPurneF1.get(j))) {
						totalUtilityByTime.set(LineNumber, totalUtilityByTime.get(LineNumber)
								- eventUtilityByTime.get(LineNumber).get(WPurneF1.get(j)));
						eventUtilityByTime.get(LineNumber).remove(WPurneF1.get(j));
						freDB.get(LineNumber).remove(WPurneF1.get(j));
					}
				}
				int TotalUtility = totalUtilityByTime.get(LineNumber);
				// TotalUtilityByTimeAndDuration[LineNumber] = TotalUtility;
				totalUtilityByTimeAndDuration.set(LineNumber, TotalUtility);
				if (LineNumber <= maximumTimeDuration) {
					for (int R = 1; R <= maximumTimeDuration; R++) {
						if ((LineNumber - R) < 1)
							break;
						else
							totalUtilityByTimeAndDuration.set((LineNumber - R),
									totalUtilityByTimeAndDuration.get((LineNumber - R)) + TotalUtility);
					}
				} else {
					for (int R = 1; R <= maximumTimeDuration; R++) {
						// TotalUtilityByTimeAndDuration[(LineNumber-R)] +=
						// TotalUtility;
						totalUtilityByTimeAndDuration.set((LineNumber - R),
								totalUtilityByTimeAndDuration.get((LineNumber - R)) + TotalUtility);
					}
				}

			}
		}
		F1TotalUtility.clear();
		totalUtilityByTime.clear();
	}
	//

//	/** 
//	 * Print event tids
//	 **/
//	private void PrintEventTid() {
//		System.out.println("////////////////Print Event Tid/////////////////////");
//		for (int i = 0; i < freF1.size(); i++) {
//			System.out.println("Frequent Event: " + freF1.get(i));
//			System.out.println("Tid:" + eventTID.get(freF1.get(i)));
//		}
//		System.out.println("////////////////End Print Event Tid/////////////////////");
//	}
//
//	/** �L�X
//	 *  Print events in the database (?)
//	 * */
//	private void PrintFreDB() {
//		System.out.println("////////////////Print Fre DB/////////////////////");
//		for (int i = 1; i < timePoint + 1; i++) {
//			System.out.println("Time Point_" + i + ": " + freDB.get(i));
//		}
//		System.out.println("////////////////End Print Fre DB/////////////////////");
//	}

	/**
	 * Mine episodes
	 */
	private void Mining() {
		// System.out.println("�C�XF1����:");
		for (int i = 0; i < freF1.size(); i++) {
			// Num_FreEP++;
			// System.out.println(FreF1.get(i)+","+FreF1.size());
			if (F1TotalUtilitybackward.get(freF1.get(i)) >= (minUtility * totalUtilityinAllSequence)) {
				allCalculateCount++;

				FreEP.add(String.valueOf(freF1.get(i)));
				EPCount.add(EventCount[freF1.get(i)]);
				MiningEP(String.valueOf(freF1.get(i)), EventCount[freF1.get(i)], eventTID.get(freF1.get(i)),
						eventTID.get(freF1.get(i)), eventTID.get(freF1.get(i)), eventTID.get(freF1.get(i)));
			}
		}
	}

	/**
	 * Helper function to mine episodes
	 * @param Pepisode
	 * @param EpisodeExactUtility
	 * @param Pos
	 * @param Poe
	 * @param Pmos
	 * @param Pmoe
	 */
	public void MiningEP(String Pepisode, int EpisodeExactUtility, ArrayList<Integer> Pos, ArrayList<Integer> Poe,
			ArrayList<Integer> Pmos, ArrayList<Integer> Pmoe) {
		numberOfCandidates++;
		// System.out.println(Pepisode+"=="+Pos);
		MiningSimult(Pepisode, EpisodeExactUtility, Pos, Poe, Pmos, Pmoe);
		MiningSerial(Pepisode, EpisodeExactUtility, Pos, Poe, Pmos, Pmoe);
	}

	/**
	 * Mine simultaneous episodes
	 * @param Pepisode
	 * @param EpisodeExactUtility
	 * @param Pos
	 * @param Poe
	 * @param Pmos
	 * @param Pmoe
	 */
	public void MiningSimult(String Pepisode, int EpisodeExactUtility, ArrayList<Integer> Pos, ArrayList<Integer> Poe,
			ArrayList<Integer> Pmos, ArrayList<Integer> Pmoe) {
		// System.out.println(Pepisode);
		int LocalCount[] = new int[eventType + 1];
		int EventExactUtility[] = new int[eventType + 1];
		Arrays.fill(LocalCount, 0);
		Arrays.fill(EventExactUtility, 0);

		Map<Integer, ArrayList<Integer>> OS = new HashMap<Integer, ArrayList<Integer>>();
		Map<Integer, ArrayList<Integer>> OE = new HashMap<Integer, ArrayList<Integer>>();

		Map<Integer, ArrayList<Integer>> MOS = new HashMap<Integer, ArrayList<Integer>>();
		Map<Integer, ArrayList<Integer>> MOE = new HashMap<Integer, ArrayList<Integer>>();

		for (int i = 1; i < eventType + 1; i++) {
			MOS.put(i, new ArrayList<Integer>());
			MOE.put(i, new ArrayList<Integer>());
		}

		String[] element1 = Pepisode.split(",");
		String[] element2 = element1[element1.length - 1].split(" ");

		int lastevent = Integer.valueOf(element2[element2.length - 1]);

		for (int i = 0; i < Pos.size(); i++) {
			int ocs = Pos.get(i);
			int timepoint = Poe.get(i);

			long TC = timePoint;
			if (ocs + maximumTimeDuration < TC) {
				TC = ocs + maximumTimeDuration;
			}

			ArrayList<Integer> EventList = freDB.get(timepoint);// �b�o�Ӯɶ��I�W���o�ͪ�SES

			if (EventList != null) {
				for (int k = 0; k < EventList.size(); k++) {
					if (EventList.get(k) > lastevent) {
						int oce = timepoint;
						///// �B�zOC
						if (OS.get(EventList.get(k)) == null) {
							OS.put(EventList.get(k), new ArrayList<Integer>());
							OE.put(EventList.get(k), new ArrayList<Integer>());
						}
						OS.get(EventList.get(k)).add(ocs);
						OE.get(EventList.get(k)).add(oce);

						///// �B�zMO
						int ismo = IsMo(MOS.get(EventList.get(k)), MOE.get(EventList.get(k)), ocs, oce);
						if (ismo == -1) {
							MOS.get(EventList.get(k)).add(ocs);
							MOE.get(EventList.get(k)).add(oce);
							// System.out.println("i="+i+","+EventList.get(k)+","+ocs+","+oce+","+EventUtilityByTime.get(ocs));
							// System.out.println(MOS.get(EventList.get(k))+","+MOE.get(EventList.get(k)));
							LocalCount[EventList.get(k)] += totalUtilityByTimeAndDuration.get(ocs);
							EventExactUtility[EventList.get(k)] += CalculateUtility(Pepisode + " " + EventList.get(k),
									ocs, oce, 1);
						} else if (ismo == -2) {

						} else {
							MOS.get(EventList.get(k)).remove(ismo);
							MOE.get(EventList.get(k)).remove(ismo);
							MOS.get(EventList.get(k)).add(ocs);
							MOE.get(EventList.get(k)).add(oce);
						}

					}
				}

			}
		}
		// System.out.println("Prefix:"+Pepisode);
		// System.out.println("LastEvent:"+lastevent);

		for (int i = 0; i < freF1.size(); i++) {
			if (LocalCount[freF1.get(i)] >= (minUtility * totalUtilityinAllSequence)) {
				Num_FreEP++;

				String Nepisode = Pepisode.concat(" " + String.valueOf(freF1.get(i)));
				FreEP.add(Nepisode);
				EPCount.add(EventExactUtility[freF1.get(i)]);
				// System.out.println("Episode:"+Nepisode+"
				// Support:"+LocalCount[FreF1.get(i)]+"
				// OS:"+OS.get(FreF1.get(i))+" OE:"+OE.get(FreF1.get(i))+"
				// MOS:"+MOS.get(FreF1.get(i))+" MOE:"+MOE.get(FreF1.get(i)));
				MiningEP(Nepisode, EventExactUtility[freF1.get(i)], OS.get(freF1.get(i)), OE.get(freF1.get(i)),
						MOS.get(freF1.get(i)), MOE.get(freF1.get(i)));
			}

		}
	}

	/**
	 * Mining serial episodes
	 * @param Pepisode
	 * @param EpisodeExactUtility
	 * @param Pos
	 * @param Poe
	 * @param Pmos
	 * @param Pmoe
	 */
	public void MiningSerial(String Pepisode, int EpisodeExactUtility, ArrayList<Integer> Pos, ArrayList<Integer> Poe,
			ArrayList<Integer> Pmos, ArrayList<Integer> Pmoe) {
		// System.out.println("episode="+Pepisode);
		int LocalCount[] = new int[eventType + 1];
		int EventExactUtility[] = new int[eventType + 1];
		Arrays.fill(LocalCount, 0);
		Arrays.fill(EventExactUtility, 0);

		Map<Integer, ArrayList<Integer>> OS = new HashMap<Integer, ArrayList<Integer>>();
		Map<Integer, ArrayList<Integer>> OE = new HashMap<Integer, ArrayList<Integer>>();

		Map<Integer, ArrayList<Integer>> MOS = new HashMap<Integer, ArrayList<Integer>>();
		Map<Integer, ArrayList<Integer>> MOE = new HashMap<Integer, ArrayList<Integer>>();

		for (int i = 0; i < eventType + 1; i++) {
			MOS.put(i, new ArrayList<Integer>());
			MOE.put(i, new ArrayList<Integer>());
		}

		///// �B�zOC
		for (int i = 0; i < Pos.size(); i++) {
			int ocs = Pos.get(i);
			int timepoint = Poe.get(i);

			long TC = timePoint;
			if (ocs + maximumTimeDuration < TC) {
				TC = ocs + maximumTimeDuration;
			}

			////// �q���ɶ��I��}�l���y ��X�H���ɶ��I���}�Y��mo
			for (int j = timepoint + 1; j < TC + 1; j++) {
				ArrayList<Integer> EventList = freDB.get(j);// �b�o�Ӯɶ��I�W���o�ͪ�SES
				if (EventList != null) {
					for (int k = 0; k < EventList.size(); k++) {
						int oce = j;

						if (OS.get(EventList.get(k)) == null) {
							OS.put(EventList.get(k), new ArrayList<Integer>());
							OE.put(EventList.get(k), new ArrayList<Integer>());
						}
						OS.get(EventList.get(k)).add(ocs);
						OE.get(EventList.get(k)).add(oce);
					}

				}
			}
		}

		/** find local item */
		for (int i = 0; i < Pmos.size(); i++) {
			int ocs = Pmos.get(i);
			int timepoint = Pmoe.get(i);

			long TC = timePoint;
			if (ocs + maximumTimeDuration < TC) {
				TC = ocs + maximumTimeDuration;
			}

			////// �q���ɶ��I��}�l���y ��X�H���ɶ��I���}�Y��mo
			for (int j = timepoint + 1; j < TC + 1; j++) {
				ArrayList<Integer> EventList = freDB.get(j);// �b�o�Ӯɶ��I�W���o�ͪ�SES
				if (EventList != null) {
					for (int k = 0; k < EventList.size(); k++) {

						int oce = j;

						int ismo = IsMo(MOS.get(EventList.get(k)), MOE.get(EventList.get(k)), ocs, oce);
						if (ismo == -1) {
							MOS.get(EventList.get(k)).add(ocs);
							MOE.get(EventList.get(k)).add(oce);
							LocalCount[EventList.get(k)] += totalUtilityByTimeAndDuration.get(ocs);
							EventExactUtility[EventList.get(k)] += CalculateUtility(Pepisode + "," + EventList.get(k),
									ocs, oce, 1);

						} else if (ismo == -2) {

						} else {
							MOS.get(EventList.get(k)).remove(ismo);
							MOE.get(EventList.get(k)).remove(ismo);
							MOS.get(EventList.get(k)).add(ocs);
							MOE.get(EventList.get(k)).add(oce);
						}
					}

				}
			}
		} ////// end find local item

		///// get frequent episode
		for (int i = 0; i < freF1.size(); i++) {
			// System.out.println(i+","+FreF1.get(i)+","+LocalCount[FreF1.get(i)]);
			// ���U��
			if (LocalCount[freF1.get(i)] >= (minUtility * totalUtilityinAllSequence)) {
				Num_FreEP++;
				String Nepisode = Pepisode.concat("," + String.valueOf(freF1.get(i)));
				FreEP.add(Nepisode);
				EPCount.add(EventExactUtility[freF1.get(i)]);
				// System.out.println("-->"+FreF1.get(i)+","+EventExactUtility[FreF1.get(i)]+","+LocalCount[FreF1.get(i)]);
				if (EpisodeExactUtility
						+ F1TotalUtilitybackward.get(freF1.get(i)) >= (minUtility * totalUtilityinAllSequence)) {
					allCalculateCount++;
					MiningEP(Nepisode, EventExactUtility[freF1.get(i)], OS.get(freF1.get(i)), OE.get(freF1.get(i)),
							MOS.get(freF1.get(i)), MOE.get(freF1.get(i)));
				}
			}
		}

	}

	/** 
	 * Calculate the utility
	 * �p��@��episode�b�S�w�ɶ��I��utility 
	 * */
	public int CalculateUtility(String Pepisode, int ocs, int oce, int type) {// type=1�O�p��real
																				// utility,type=2�O�p��Upper
																				// bound
																				// utility
		int utility = 0;
		String[] SubEpisode = Pepisode.split(",");

		// �p��real utility
		if (type == 1 && SubEpisode.length < 2) {
			String[] event = SubEpisode[(SubEpisode.length - 1)].split(" ");
			// System.out.print(Pepisode+",");
			for (int i = 0; i < event.length; i++) {
				// System.out.println("--"+event[i]+","+EventUtilityByTime.get(oce));
				if (eventUtilityByTime.get(oce).containsKey(event[i]))
					utility += eventUtilityByTime.get(oce).get(Integer.valueOf(event[i]));
			}

		} else if (type == 1 && SubEpisode.length >= 2) {
			int starttime = ocs;
			for (int j = 0; j < SubEpisode.length; j++) {
				String[] event = SubEpisode[j].split(" ");
				if (j == 0) {
					for (int i = 0; i < event.length; i++) {
						// System.out.println("--"+event[i]+","+EventUtilityByTime.get(oce));
						utility += eventUtilityByTime.get(starttime).get(Integer.valueOf(event[i]));
					}
					starttime++;
					// System.out.print("->"+starttime);
				} else {
					while (starttime <= oce) {
						int CheckEventInStarttime = 0;
						for (int i = 0; i < event.length; i++) {
							if (!eventUtilityByTime.get(starttime).containsKey(Integer.valueOf(event[i])))
								break;
							else
								CheckEventInStarttime++;
						}
						if (CheckEventInStarttime == event.length) // �p�G���׬ۦP�A��ܦ��X�{�b�Ӯɶ��I�̭�
						{
							for (int i = 0; i < event.length; i++)
								utility += eventUtilityByTime.get(starttime).get(Integer.valueOf(event[i]));
							break;// break while(starttime<=oce)
						} else {
							starttime++;
						}
					}
				}

			}

		}

		// �p��Upper bound utility
		if (type == 2 && SubEpisode.length < 2) {
			utility = totalUtilityByTimeAndDuration.get(ocs);
		} else if (type == 2 && SubEpisode.length >= 2) {
			int starttime = ocs;
			for (int j = 0; j < SubEpisode.length; j++) {
				String[] event = SubEpisode[j].split(" ");
				if (j == 0) {
					for (int i = 0; i < event.length; i++) {
						// System.out.println("--"+event[i]+","+EventUtilityByTime.get(oce));
						utility += eventUtilityByTime.get(starttime).get(Integer.valueOf(event[i]));
					}
					starttime++;
					// System.out.print("->"+starttime);
				} else {
					while (starttime <= oce) {
						int CheckEventInStarttime = 0;
						for (int i = 0; i < event.length; i++) {
							if (!eventUtilityByTime.get(starttime).containsKey(Integer.valueOf(event[i])))
								break;
							else
								CheckEventInStarttime++;
						}
						if (CheckEventInStarttime == event.length) // �p�G���׬ۦP�A��ܦ��X�{�b�Ӯɶ��I�̭�
						{
							if (starttime == oce) // �u�ݳ̫�@����Ƭ���
							{

							} else {
								for (int i = 0; i < event.length; i++) {
									utility += eventUtilityByTime.get(starttime).get(Integer.valueOf(event[i]));
								}
							}
							break;// break while(starttime<=oce)
						} else {
							starttime++;
						}

					}
				}

			}

		}

		// System.out.println("////"+Pepisode+"-->"+utility);
		return utility;
	}

	/** �P
	 * �_�O�_��MO 
	 * */
	public int IsMo(ArrayList<Integer> mos, ArrayList<Integer> moe, int ocs, int oce) {
		int ismo = -1;
		if (mos.size() == 0) {
			ismo = -1;
		} else {
			for (int i = 0; i < mos.size(); i++) {
				int Nmos = mos.get(i);
				int Nmoe = moe.get(i);

				if (ocs <= Nmos && oce >= Nmoe) {
					ismo = -2;
				} else if (ocs >= Nmos && oce <= Nmoe) {
					ismo = i;
				}
			}
		}
		return ismo;
	}

	/**
	 * Save the results to the file
	 */
	public void saveResultToFile() {
		try {
			FileWriter fstream = new FileWriter(outputFile);
			BufferedWriter out = new BufferedWriter(fstream);

			// OUTPUT EPISODE WITH A SINGLE EVENT
			if(outputSingleEvents){
				for (int i = 0; i < freF1.size(); i++) {
					int utility = EventCount[freF1.get(i)];
					if(utility >= (minUtility * totalUtilityinAllSequence)){
						out.write(freF1.get(i) + " -1 #UTIL: " + utility);
						out.newLine();
						numberOfSingleEvents++;
					}
				}
			}

			// OUTPUT OTHER EPISODES
			for (int i = 0; i < FreEP.size(); i++) {
				if (EPCount.get(i) >= (minUtility * totalUtilityinAllSequence)) //
				{
					// Convert the name of the episode to the SPMF format:
					// example : 5 3, 3 --> 5 3 -1 3 -1
					String episodeName = FreEP.get(i);
					// if
					episodeName = episodeName.replaceAll(",", " -1 ");

					// System.out.println(FreEP.get(i));
					// buffer.append(arg0)
					out.write(episodeName);
					out.write(" -1 #UTIL: " + EPCount.get(i));
					out.newLine();
					numberOfEpisodes++;
				}
			}
			out.close();
		} catch (IOException e) {
			System.out.println("�g�ɵo�Ϳ��~:" + e);
		}
	}

}