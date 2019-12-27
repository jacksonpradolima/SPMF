package ca.pfv.spmf.algorithms.frequentpatterns.mpfps;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ca.pfv.spmf.tools.MemoryLogger;

/**
 * @see SeqTidList
 * @author Zhitian Li
 */
public class AlgoMPFPS_DFS {
	
	/** ThreStanDe is the standard deviation threshold of periodicities for each sequence. 
	//This parameter is to ensure that mined patterns outcome as periodically. */
	double maxStandardDeviation  = 200.0;
	
	/** minRa is the threshold of indicating how many sequences in a database a frequent pattern 
	// appears in.*/
	double minRA = 0.01;
	
	/** This parameter indicates the maximum periodicity interval of a frequent pattern in a 
	//sequence, to avoid the situation that a pattern appears multiple times at a single time while
	//the other time don't have the pattern.*/
	int maxPeriodicity = 100;
	
	/** In each sequence, if an itemset is frequent, it's support should be larger than a threshold.*/
	int minimumSupport = 2;
	
	/** Calculate the number of sequences in each database, so that we can calculate the ratio of a frequent 
	//pattern. */
	int numOfSequences = 0;
	
	/** Store the lengths of sequences, so that we can compute the last period of a frequent pattern in these 
	sequences. */
	List<Integer> sequenceLengths = new ArrayList<Integer>();

	/** This map is used to store the contrast periodic frequent patterns and the corresponding BC parameter.
	Show this map to users. */
	Map<int [], Double> result = new HashMap<int [], Double>();

	/** Time spent during the last execution of this algorithm  */
	long totalTime;
	
	/** Number of patterns  */
	int patternCount;
	
	/** Constructor */
	public AlgoMPFPS_DFS() {

	}
	
	/**
	 * Run the algorithm
	 * @param maxStandardDeviation maximum standard deviation 
	 * @param minRA minimum RA
	 * @param maxPeriodicity maximum periodicity
	 * @param minimumSupport minimum support
	 * @param outputFile output file path
	 * @param inputFile  input file path
	 * @throws Exception if error reading or writing to file
	 */
	public void runAlgorithm(double maxStandardDeviation, double minRA, int maxPeriodicity, int minimumSupport, String inputFile, String outputFile) throws Exception{
		MemoryLogger.getInstance().reset();
		long startTime = System.currentTimeMillis();
		
		// save parameters
		this.maxStandardDeviation = maxStandardDeviation;
		this.maxPeriodicity = maxPeriodicity;
		this.minRA = minRA;
		this.minimumSupport = minimumSupport;
		
		// initialize data structures
		result = new HashMap<int [], Double>();
		sequenceLengths = new ArrayList<Integer>();
		numOfSequences = 0;
		
		// Run the algorithm
		List<SeqTidList> periodicFrequentPatterns = getFreqPeriodicPattern(inputFile);

		PrintWriter out = new PrintWriter(new BufferedWriter
				(new FileWriter(outputFile)));
		
		for(SeqTidList periodicPat: periodicFrequentPatterns){
			out.println(periodicPat);
		}
		out.println();
		out.close();
		
		patternCount = periodicFrequentPatterns.size();

		MemoryLogger.getInstance().checkMemory();
		totalTime = System.currentTimeMillis() - startTime;
	}
	
	/**
	 * Print statistics about the last algorithm execution.
	 */
	public void printStats() {
		System.out.println("=============  MPFPS_DFS v.2.40 - STATS =============");
		System.out.println("Pattern count: " + patternCount);
		System.out.println("Memory : " + MemoryLogger.getInstance().getMaxMemory() + " mb");
		System.out.println("Total time : " + totalTime + " ms");
		System.out.println("===================================================");
	}

	/**
	 * Reads the file and returns a list of SeqTidList objects which itemSet[] is single, and are frequent and 
	 * periodic in the database. Moreover, the frequent patterns must be satisfied with the MIN_RA threshold.  
	 * @param fileName the name of the file to be read.
	 * @return singleItemTidList: an arrayList of SeqTidList objects
	 * @throws IOException if IO error.
	 */
	public List<SeqTidList> getSingleItemTidList(String fileName, int minSup) throws IOException{
		File file = new File(fileName);
		BufferedReader reader= null;
		String tempReader = null;
		String[] tempSplitted;
		List<SeqTidList> singleItemTidList = new ArrayList<SeqTidList>();
		int currentLine = 0;
		reader = new BufferedReader(new FileReader(file));
		while((tempReader = reader.readLine()) != null){
			// if the line is  a comment, is  empty or is a
			// kind of metadata
			if (tempReader.isEmpty() == true ||
					tempReader.charAt(0) == '#' || tempReader.charAt(0) == '%'
							|| tempReader.charAt(0) == '@') {
				continue;
			}
			
			tempSplitted = tempReader.split(" ");
			int currentTran = 0;
			//The number of the transaction currently processing.
			
			//While reading a new line, reset the flag of each TidList of single item to true.
			for(SeqTidList tempSeqTid: singleItemTidList)
				tempSeqTid.newLine = true;
			
			//When the read number is a positive integer, judge if this is the first appearance of a new line. If 
			//true, then create a new list of Integer to store the tidSet. If false, add this tid to current list. 
			for(int i=0; i<tempSplitted.length-1; i++){
				boolean found = false;
				int  temp = Integer.parseInt(tempSplitted[i]);
				//temp is the item currently processing.
     			int tempOfNext = Integer.parseInt(tempSplitted[i+1]);
				if(temp != -1 && temp != -2){
					for(SeqTidList current: singleItemTidList){
						int lengthOfSequences = current.sequenceNum.size();
						if(temp == current.itemSet[0]){
							if(current.sequenceNum.get(lengthOfSequences-1) != currentLine){
								List<Integer> newTidSet = new ArrayList<Integer>();
								newTidSet.add(currentTran);
								current.seqTidSet.add(newTidSet);
								current.sequenceNum.add(currentLine);
								current.seqSupport.add(1);
								current.newLine = false;
							}

							else{
								int length = current.seqTidSet.size();
								current.seqTidSet.get(length-1).add(currentTran);
								int tempSup = current.seqSupport.get(length-1);
								current.seqSupport.set(length-1, tempSup+1);
							}
							found = true;
							break;
						}
					}
					
					if(!found){
						SeqTidList current = new SeqTidList(1);
						current.itemSet[0] = temp;
						current.seqTidSet.add(new ArrayList<Integer>());
						current.seqTidSet.get(0).add(currentTran);
						current.seqSupport.add(1);
						current.sequenceNum.add(currentLine);
						singleItemTidList.add(current);
					}
				}
				else if(temp == -1 && tempOfNext != -2){
					currentTran++;
					
				}
				else if(temp == -1 && tempOfNext == -2){
					sequenceLengths.add(currentTran);
					break;
				}
			}
			currentLine++;
		}
		numOfSequences = currentLine;
		reader.close();
		
		List<SeqTidList> periodicSingleItemTidList = new ArrayList<SeqTidList>();
		//Check if the SeqTidList objects are  periodic or not.
		for(SeqTidList temp: singleItemTidList){
			int num = 0;
		    Iterator<List<Integer>> iterator = temp.seqTidSet.iterator();
			while (iterator.hasNext()) {
				List<Integer> currentTidSet = (List<Integer>) iterator.next();
				if( !checkPeriodicity(currentTidSet, sequenceLengths.get(temp.sequenceNum.get(num))) ){
					iterator.remove();
					temp.sequenceNum.remove(num);
					temp.seqSupport.remove(num);
					}
				else{
					num++;
				}
			}
			if(!temp.seqTidSet.isEmpty())
				periodicSingleItemTidList.add(temp);
		}
	
		List<SeqTidList> tempTidlist = new ArrayList<SeqTidList>();
		for(SeqTidList temp: periodicSingleItemTidList){
			int num = 0;
			SeqTidList tempSeqTidList = new SeqTidList();
			for(int sup: temp.seqSupport){
				if(sup >= minSup){
					tempSeqTidList.seqTidSet.add(temp.seqTidSet.get(num));
					tempSeqTidList.seqSupport.add(temp.seqSupport.get(num));
					tempSeqTidList.sequenceNum.add(temp.sequenceNum.get(num));
				}
				num++;
			}
			tempSeqTidList.itemSet = temp.itemSet;
			double seqRatio  = (double)tempSeqTidList.seqSupport.size()/numOfSequences;
			if(tempSeqTidList.seqSupport.size()>0 && seqRatio >= minRA){
				tempSeqTidList.ra = seqRatio;
				tempTidlist.add(tempSeqTidList);
			}
		}
		return tempTidlist;
	}
	
	/**
	 * Get all the frequent patterns that are periodic in this database.
	 * @param fileName the file to be read.
	 * @return periodicFrequent: all the frequent patterns which are periodic in this database.
	 * @throws Exception
	 */
	public  List<SeqTidList> getFreqPeriodicPattern(String fileName) 
			throws IOException{
		List<SeqTidList> freSingleItemTidlist = getSingleItemTidList(fileName, minimumSupport);
		List<SeqTidList> periodicFrequent = new ArrayList<SeqTidList>(); 
		periodicFrequent = periodicFrequent(freSingleItemTidlist, minimumSupport, periodicFrequent);
		 return periodicFrequent;
	}
	
	/**
	 * calculate the intersect of tidLists of  two different TidLists
	 * @param list1
	 * @param list2
	 * @return
	 */
	public List<Integer> intersectTids(List<Integer> list1, List<Integer> list2){
		List<Integer> common = new ArrayList <Integer>();
		for(int i=0; i<list1.size(); i++)
			if(Collections.binarySearch(list2, list1.get(i)) >= 0)
					common.add(list1.get(i));
		return common;
	}
	
	
	/**
	 * get the union itemsets of two integer arrays.
	 * @param list1
	 * @param list2
	 * @return unionItemSet: an integer array.
	 */
	public int[] unionItemsets(int list1[], int list2[]){
		int len = list1.length;
		int unionItemSet [] = Arrays.copyOf(list1, len+1);
		unionItemSet[len] = list2[len-1];
		return unionItemSet;
	}
	
	/**
	 * check if the frequent pattern is periodic. A periodic pattern must satisfies the MAX_PR threshold and
	 * the MAX_STAN_DEV threshold.
	 * @param tidSet
	 * @param lengthOfSequence: the length of currently processing sequence, to calculate the last period of
	 * this frequent pattern.
	 * @return periodic : boolean result.
	 */
	public boolean checkBoundRa(List<Integer> tidSet , int lengthOfSequence){
		boolean periodic = true;
		int length = tidSet.size();
		int periods[] = new int[length+1];
		int temp = 0;
		double avgPr = 0;
		int sum = 0;
		double sumDevi = 0;
		double stanDevi = 0;
		Collections.sort(tidSet);
		int firstPeriod = tidSet.get(0) - 0;
		if(firstPeriod<0 || firstPeriod>maxPeriodicity)
			periodic = false;
		else
			periods[0] = firstPeriod;
		//The first period should be first transaction number minus 0.
		
		for(int i=0; i<length-1; i++){
			temp= tidSet.get(i+1) - tidSet.get(i);
			if (temp <0 || temp> maxPeriodicity){
				periodic = false;
				break;
			}
			else{
				periods[i+1] = temp;
				sum+=temp;
			}
		}
		
		int lastPeriod = lengthOfSequence-tidSet.get(length-1);
		if(lastPeriod<0 || lastPeriod>maxPeriodicity)
			periodic = false;
		else
			periods[length] = lastPeriod;
		//The last period should be the number of transactions of this sequence minus the last position this 
		//frequent pattern appears.
		 
		if(periodic){
			avgPr = sum/length;
			for(int j=0; j<periods.length; j++){
				sumDevi += ((periods[j]-avgPr)*(periods[j]-avgPr));
			}
			stanDevi = Math.sqrt(sumDevi/length);
			if(stanDevi > maxStandardDeviation)
				periodic = false;
		}
		return periodic;
	}

	public boolean checkPeriodicity(List<Integer> tidSet , int lengthOfSequence){
		boolean periodic = true;
		int length = tidSet.size();
		int periods[] = new int[length+1];
		int temp = 0;
		double avgPr = 0;
		int sum = 0;
		double sumDevi = 0;
		double stanDevi = 0;
		Collections.sort(tidSet);
		
		int firstPeriod = tidSet.get(0) - 0;
		if(firstPeriod<0 || firstPeriod>maxPeriodicity)
			periodic = false;
		else
			periods[0] = firstPeriod;
		//The first period should be first transaction number minus 0.
		
		for(int i=0; i<length-1; i++){
			temp= tidSet.get(i+1) - tidSet.get(i);
			if (temp <0 || temp> maxPeriodicity){
				periodic = false;
				break;
			}
			else{
				periods[i+1] = temp;
				sum+=temp;
			}
		}
		
		int lastPeriod = lengthOfSequence-tidSet.get(length-1);
		if(lastPeriod<0 || lastPeriod>maxPeriodicity)
			periodic = false;
		else
			periods[length] = lastPeriod;
		//The last period should be the number of transactions of this sequence minus the last position this 
		//frequent pattern appears.
		 
		
		if(periodic){
			avgPr = sum/length;
			for(int j=0; j<periods.length; j++){
				sumDevi += ((periods[j]-avgPr)*(periods[j]-avgPr));
			}
			stanDevi = Math.sqrt(sumDevi/length);
			if(stanDevi > maxStandardDeviation)
				periodic = false;
		}
		return periodic;
	}
	
	/**
	 * Inspired by Eclat algorithm. At the same time, check if the patterns are periodic or not.
	 * @param tidlistOfTemp
	 * @param minSup
	 * @param result : result is a Map object to store all frequent patterns and their
	 * 	 corresponding supports
	 * @return
	 */
	public List<SeqTidList>   periodicFrequent(List<SeqTidList> tidlistOfTemp, 
			int minSup, List<SeqTidList> result){
			List<SeqTidList> tempTidList  = new ArrayList<SeqTidList>();	
			
			for(int i=0; i<tidlistOfTemp.size(); i++){
			
				SeqTidList currentTidListA = tidlistOfTemp.get(i);
				result.add(currentTidListA);
				int len = currentTidListA.itemSet.length;
				
				for(int j=i+1; j<tidlistOfTemp.size(); j++){
					
					SeqTidList currentTidListB = tidlistOfTemp.get(j);
					if(haveSamePrefix(currentTidListA, currentTidListB)){
						
						SeqTidList currentTidListAB=new SeqTidList(len+1);
						currentTidListAB.itemSet = 
								unionItemsets(currentTidListA.itemSet, currentTidListB.itemSet);
						List<Integer> interSequenceNum = 
								intersectTids(currentTidListA.sequenceNum,
								currentTidListB.sequenceNum);
						
						for(int serial: interSequenceNum){
							
							int serialNumA = 
									Collections.binarySearch(currentTidListA.sequenceNum, serial);
							int serialNumB = 
									Collections.binarySearch(currentTidListB.sequenceNum, serial);
		
							List<Integer> tidSetA = currentTidListA.seqTidSet.get(serialNumA);
							List<Integer> tidSetB = currentTidListB.seqTidSet.get(serialNumB);
							
							List<Integer> interTidsAB = intersectTids(tidSetA, tidSetB);
							
							if(interTidsAB.isEmpty())
								continue;
							
							if(checkPeriodicity(interTidsAB, interTidsAB.size())){
								currentTidListAB.seqTidSet.add(interTidsAB);
								currentTidListAB.sequenceNum.add(serial);
								currentTidListAB.seqSupport.add(interTidsAB.size());
							}
						}
							tempTidList.add(currentTidListAB);
						
					}
				}
			}
			
			//cutTidList is to store the frequent periodic patterns cut down.
			List<SeqTidList> cutTidList = new ArrayList<SeqTidList>();
			
			//cut down all of the infrequent or inperiodic patterns.
			for(SeqTidList temp: tempTidList){
				int num = 0;
				
				SeqTidList tempSeqTidList = new SeqTidList();
				
				for(int sup: temp.seqSupport){
					if(sup >= minSup){
						tempSeqTidList.seqTidSet.add(temp.seqTidSet.get(num));
						tempSeqTidList.seqSupport.add(temp.seqSupport.get(num));
						tempSeqTidList.sequenceNum.add(temp.sequenceNum.get(num));
					}
					num++;
				}
				tempSeqTidList.itemSet = temp.itemSet;
				double seqRatio = (double)tempSeqTidList.seqSupport.size()/numOfSequences;
				
				if(tempSeqTidList.seqSupport.size()>0 && seqRatio >= minRA){
					
					cutTidList.add(tempSeqTidList);
				}
			}
			
			tempTidList = cutTidList;
			
			//System.out.println(showRunning++);
			if(tempTidList.isEmpty())
				return result;
			else
				return periodicFrequent(tempTidList, minSup, result); 
		}

	/**
	 * Judge if the two TidList objects have the same prefix.
	 * @param list1
	 * @param list2
	 * @return
	 */
	public  boolean haveSamePrefix(SeqTidList list1, SeqTidList list2){
		
		int length = list1.itemSet.length;
		boolean havaSamePrefix=false;
		boolean temp=true;
		if(list1.itemSet.length == 1 && list2.itemSet.length == 1)
			havaSamePrefix = true;
			
		else{
			for(int i=0; i<length-1; i++)
				if(list1.itemSet[i]!=list2.itemSet[i])
					temp=false;
			if(temp)
				if(list1.itemSet[length-1]!=list2.itemSet[length-1])
					havaSamePrefix=true;
		}
		return havaSamePrefix;
	}
	
	public  int compare(int []first, int []second){
		for (int i = 0; i < first.length && i < second.length; i++) {

			if (first[i] < second[i]) {
				return -1;
			} else if (first[i] > second[i]) {
				return 1;
			}
		}

		return first.length - second.length;
	}
	
}