package ca.pfv.spmf.algorithms.sequentialpatterns.qcsp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;

import ca.pfv.spmf.algorithms.sequentialpatterns.qcsp.util.CountMap;
import ca.pfv.spmf.algorithms.sequentialpatterns.qcsp.util.Pair;
import ca.pfv.spmf.algorithms.sequentialpatterns.qcsp.util.Timer;
import ca.pfv.spmf.algorithms.sequentialpatterns.qcsp.util.Triple;
import ca.pfv.spmf.tools.MemoryLogger;
/**
 * This is an implementation of the QCSP algorithm.
 * For more information please refer the paper Mining Top-K Quantile-based Cohesive Sequential Patterns 
 * by Len Feremans, Boris Cule and Bart Goethals, published in 2018 SIAM International Conference on Data Mining (SDM18).<br/>
 *
 * Copyright (c) 2020 Len Feremans (Universiteit Antwerpen)
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
 * You should have received a copy of the GNU General Public License along wit
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Len Feremans
 */
public class AlgoQCSP {
	
	//parameters
	private double alpha;
	private int minsup;
	private int maxsize; 
	private int topK;
	private String patternOutputFile;
	
	//additional parameter for experimental purpose: turns pruning of
	private boolean pruningOf = false; //TODO make getters/setters
	private boolean debug = false; //TODO make getters/setters
	private String labelsFile = null; 
	public static long DEBUG_ITER = 1000000;
	
	//state:
	private List<Window> init = makeList(new Window(-1,-1,-1));
	private QCSPData data;
	private double mincoh = 0.0;
	
	//stats:
	private long elapsedTime = -1;
	private long iterations = 0;
	private long leafs = 0;
	private long patternCount = 0;
	
	//state for computing minimal windows: 
	private List<Window> shorterWindowsCache = new ArrayList<>();
	private List<Pair<List<Integer>,Window>> stack = new ArrayList<>();
	private List<List<Integer>> occurrences = new ArrayList<>();
	private Map<Integer,Integer> itemAtT = new HashMap<Integer,Integer>();

	public AlgoQCSP() {}
	
	/**
	 * Method to run the algorithm
	 * 
	 * @param singleSequenceFile to an input file (single sequence) or input file consisting of several sequences separated by -2
	 * @param patternOutputFile to an output file
	 * @param minsup for filtering infrequent items, e.g. if minsup=2, this will remove item occurring less than 2 times in the entire sequence
	 * @param alpha size of window, relative to pattern. e.g. alpha=2 will only count occurrences of (a,b,c) within a window of size 3*2=6
	 * @param maxsize, maximum length of sequential pattern
	 * @param topK,  Number of most quantile-based cohesive sequential patterns to return
	 * @param outputFilePath path for writing the output file
	 * @throws IOException
	 */
	public List<Pair<SequentialPattern,Double>> runAlgorithm(String singleSequenceFile, String patternOutputFile, 
			int minsup, double alpha, int maxsize, int topK) throws IOException {
		//parameters
		this.minsup = minsup;
		this.alpha = alpha;
		this.maxsize = maxsize;
		this.topK = topK;
		this.patternOutputFile = patternOutputFile;
		//read data
		this.data = new QCSPData();
		data.loadData(new File(singleSequenceFile), labelsFile!=null?new File(labelsFile):null, minsup, alpha, maxsize, debug);
		//run
		return run(debug);
	}
	
	//additional optional parameters:
	public void setPruningOf(boolean pruningOf) {
		this.pruningOf = pruningOf;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setLabelsFile(String labelsFile) {
		this.labelsFile = labelsFile;
	}

	
	/**
	 * @param debug if true shows progress (use setDebug())
	 * @throws IOException 
	 */
	public List<Pair<SequentialPattern,Double>> run(boolean debug) throws IOException {
		Timer timer = new Timer("QSCP.run()");
		System.out.format("Parameters: alpha=%f, maxsize=%d, top-k=%d, pruningOf=%s\n", this.alpha, this.maxsize, this.topK, this.pruningOf);
		PriorityQueue<Pair<SequentialPattern,Double>> heapPatterns = new PriorityQueue<>(topK, heapComparator);
		mincoh = 0.0;
		ArrayList<Triple<SequentialPattern, List<Window>, List<Integer>>> stack = new ArrayList<>();
		List<Integer> allItems = this.data.getItemsSortedOnAscendingSupport();
		stack.add(new Triple<>(new SequentialPattern(),init,allItems));
		iterations = 0;
		leafs = 0;
		while(!stack.isEmpty()) {
			iterations++;
			Triple<SequentialPattern, List<Window>, List<Integer>> top = stack.remove(stack.size()-1);
			SequentialPattern X = top.getFirst();
			List<Window> P = top.getSecond();
			List<Integer> Y = top.getThirth();
			if(debug && iterations % DEBUG_ITER == 0)
			{
				int currentIndex = allItems.indexOf(X.pattern.size() > 0?X.pattern.get(0):0);
				timer.progress(currentIndex, allItems.size());
				System.out.format("Iterations:%-10d, #Patterns: %d, worst: %s, min_coh:%.3f, \n",  
							iterations/DEBUG_ITER, heapPatterns.size(), 
							toString(heapPatterns.size()>0?heapPatterns.peek().getFirst().pattern:Collections.EMPTY_LIST), 
							mincoh);
			}
			if(Y.size() == 0) { //leaf
				if(X.length() > 1) {
					leafs++;
					double qcoh = quantileCohesionComputedOnProjection(X,P);
					if(heapPatterns.size() < topK) {
						heapPatterns.add(new Pair<SequentialPattern,Double>(X,qcoh));
						if(heapPatterns.size() == topK) {
							mincoh = heapPatterns.peek().getSecond();
						}
					}
					else{
						if(qcoh> mincoh) {
							heapPatterns.poll();
							heapPatterns.add(new Pair<SequentialPattern,Double>(X,qcoh));
							mincoh = heapPatterns.peek().getSecond();
						}
					}
				}
			}
			else { //branch-and-bound
				if(!pruningOf && prune(X,P,Y,mincoh)) {
					continue;
				}
				stack.add(new Triple<>(X,P,new ArrayList<Integer>(Y.subList(1, Y.size()))));
				if(X.length() != maxsize) {
					int nextItem = Y.get(0);
					SequentialPattern Z = new SequentialPattern(X,nextItem); 
					List<Window> projectionZ = project(Z,P);
					List<Integer> itemsZ = projectCandidates(Z,projectionZ);
					stack.add(new Triple<>(Z,projectionZ,itemsZ));
				}
			}
		}
		//save stats
		elapsedTime = timer.end();
		patternCount = heapPatterns.size();
		//save patterns
		List<Pair<SequentialPattern,Double>> sorted = new ArrayList<>();
		while(!heapPatterns.isEmpty()) {
			Pair<SequentialPattern,Double> pattern = heapPatterns.poll();
			sorted.add(pattern);
		}
		//sort on support AND cohesion
		Collections.sort(sorted, new Comparator<Pair<SequentialPattern,Double>>(){

			@Override
			public int compare(Pair<SequentialPattern, Double> o1, Pair<SequentialPattern, Double> o2) {
				return (int)(10000 * o2.getSecond() - 10000 * o1.getSecond() 
						+ data.support(o2.getFirst().pattern) - data.support(o1.getFirst().pattern));
			}});

		savePatterns(sorted);
		return sorted;
	}

	/*
	 * Save
	1 2 -1 #SUP: 2
	1 -1 2 -1 6 -1 #SUP: 2
	*/
	private void savePatterns(List<Pair<SequentialPattern, Double>> sortedPatterns) throws IOException {
		//print normal output + save to file
		File output;
		BufferedWriter writer =null;
		if(patternOutputFile != null) {
			output = new File(this.patternOutputFile);
			if(!output.getParentFile().exists())
				output.getParentFile().mkdirs();
			writer = new BufferedWriter(new FileWriter(output));
		}
		System.out.println("============================");
		System.out.println("QC Patterns:");
		for(int i=0; i<sortedPatterns.size(); i++) {
			Pair<SequentialPattern, Double> pattern =  sortedPatterns.get(i);
			String patternToString = null;
			if(data.hasLabels()){
				patternToString = String.format("%s   #SUP: %d   #QCOH: %.3f",  //pattern, quantile-based cohesion, support
					toString(pattern.getFirst().pattern), 
					data.support(pattern.getFirst().pattern),
					pattern.getSecond());
			}
			else {
				patternToString = String.format("%s   #SUP: %d   #QCOH: %.3f",  //pattern, quantile-based cohesion, support
						toStringSPMF(pattern.getFirst().pattern), 
						data.support(pattern.getFirst().pattern),
						pattern.getSecond());
			}
			System.out.println(patternToString);
			if(patternOutputFile != null) {
				writer.write(patternToString);
				writer.write("\n");
			}
		}
		if(patternOutputFile != null) {
			writer.close();
		}
		System.out.println("end QC Patterns:");
		System.out.println("============================");
	}
	
	

	/**
	 * Print the statistics of the algorithm execution to System.out.
	 */
	public void printStatistics() {
		System.out.println("=============  QCSP algorithm v1.00 - STATS =======");
		System.out.println(" Pattern count: " +  patternCount);
		System.out.println(" Min cohesion: " +  mincoh);
		System.out.println(" Total time ~ " + (elapsedTime) + " ms");
		System.out.println(" Number of iterations: " + iterations);
		System.out.println(" Number of candidates: " + leafs);
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println(" Parameters");
		System.out.println("  Maxsize: " + maxsize);
		System.out.println("  Pruning enabled: " + !pruningOf);
		System.out.println("  Alpha: " + alpha);
		System.out.println("  Top-k: " + topK);
		System.out.println(" Input file information");
		System.out.println("  number of symbols: " + data.getItemsSortedOnAscendingSupport().size());
		System.out.println("  sequence length: " + data.getSequenceSize());
		System.out.println("  label file enabled: " + data.hasLabels());
		System.out.println("===========================================================");
	}

	
	/**
	 * Carefull for special cases: 
	 * 1) Overlapping windows: e.g. X=(a,b) and S=(a,a,b,b)" -> count both b's only once
	 * 2) A has multiple 'roles': X=(a,b,a) and S=(a,b,a,_,_,a,b,a) -> last a has minwin=3, not infinity 
	 * @param X
	 * @param projectionX
	 * @return
	 */
	public double quantileCohesionComputedOnProjection(SequentialPattern X, List<Window> projectionX) {
		int maxwin = (int)Math.floor(this.alpha * X.length());
		Map<Integer, Integer> minWinAtT = computeMinimalWindowsBasedOnProjection1(X, projectionX, maxwin);
		//Step 4: count number of minwindows
		int count = minWinAtT.keySet().size(); 
		//Step 5: compute qcohesion
		int supportX = data.support(X.pattern);
		double qcoh = count / (double)supportX;
		return qcoh;
	}

	private Map<Integer, Integer> computeMinimalWindowsBasedOnProjection1(SequentialPattern X, List<Window> projectionX, int maxwin) {
		//TODO: Copy/paste with next
		//Step 1: shorten windows
		List<Window> shorterWindows = new ArrayList<Window>();
		for(Window window: projectionX) {
			if(window.a - window.t > maxwin)
				continue;
			int end = Math.min(window.b, window.t + maxwin);
			shorterWindows.add(new Window(window.t, end, end));
		}
		//Step 2: count all variations, level-wise -> do small DFS search
		//e.g. a0 -> b1, b2
		//			a0 -> b1 ->c1
		//			a0 -> b2 -> c1
		//     a1 -> b2, b2
		// 		     ...
		List<Integer> sequence = data.getSequence();
		List<Pair<List<Integer>,Window>> stack = new ArrayList<>();
		for(Window window: shorterWindows) {
			List<Integer> X_poslist = makeList(window.t);
			stack.add(new Pair<>(X_poslist, new Window(window.t+1, window.b, window.b)));
		}
		List<List<Integer>> occurrences = new ArrayList<>();
		while(!stack.isEmpty()) {
			Pair<List<Integer>,Window> top = stack.remove(stack.size()-1);
			List<Integer> poslist = top.getFirst();
			Window window = top.getSecond();
			if(poslist.size() == X.length()) {
				occurrences.add(poslist);
			}
			else {
				int currentItem = X.pattern.get(poslist.size());
				for(int i=window.t; i<window.b; i++) {
					Integer item = sequence.get(i);
					if(item == null)
						continue;
					if(item  == currentItem) {
						List<Integer> newPoslist = new ArrayList<Integer>(poslist);
						newPoslist.add(i);
						stack.add(new Pair<>(newPoslist,new Window(i+1,window.b,window.b)));
					}
				}
			}
		}
		//Step 3: compute minimum window at t for each occurrence
		Map<Integer,Integer> minWinAtT = new HashMap<Integer,Integer>();
		for(List<Integer> occurrence: occurrences) {
			Integer mwin = occurrence.get(occurrence.size()-1) - occurrence.get(0);
			for(Integer pos: occurrence) {
				minWinAtT.put(pos, minWindow(mwin, minWinAtT.get(pos)));
			}
		}
		return minWinAtT;
	}

	//Compute minimal windows. Less detailed in paper.
	private int computeNumberOfMinimalWindowsBasedOnProjection(SequentialPattern X, Set<Integer> XNoneOverlapping, List<Window> projectionX, int lengthZMax) {
		int maxwin = (int)Math.floor(alpha * lengthZMax);
		List<Integer> sequence = data.getSequence();
		//Step 1: shorten windows
		List<Window> shorterWindows = projectionX;
		if(lengthZMax < maxsize){ 
			shorterWindows = shorterWindowsCache;
			shorterWindowsCache.clear();
			for(Window window: projectionX) {
				if(window.a - window.t > maxwin)
					continue;
				int end = Math.min(window.b, window.t + maxwin);
				shorterWindows.add(new Window(window.t, end, end));
			}
		}
		//Step 2: count all variations, level-wise -> do small DFS search
		stack.clear();
		for(Window window: shorterWindows) {
			List<Integer> X_poslist = new ArrayList<Integer>(X.length());
			X_poslist.add(window.t);
			stack.add(new Pair<>(X_poslist, new Window(window.t+1, window.b, window.b)));
		}
		occurrences.clear();
		while(!stack.isEmpty()) {
			Pair<List<Integer>,Window> top = stack.remove(stack.size()-1);
			List<Integer> poslist = top.getFirst();
			Window window = top.getSecond();
			if(poslist.size() == X.length()) {
				occurrences.add(poslist);
			}
			else {
				int currentItem = X.pattern.get(poslist.size());
				for(int i=window.t; i<window.b; i++) {
					Integer item = sequence.get(i);
					if(item == null)
						continue;
					if(item  == currentItem) {
						List<Integer> newPoslist = new ArrayList<Integer>(poslist);
						newPoslist.add(i);
						stack.add(new Pair<>(newPoslist,new Window(i+1,window.b,window.b)));
					}
				}
			}
		}
		//compute for each item, number of minimal windows
		itemAtT.clear();
		for(List<Integer> occurrence: occurrences) {
			for(int i=0; i<occurrence.size(); i++) {
				itemAtT.put(occurrence.get(i), X.pattern.get(i));
			}
		}
		int countSmallWindowsNonOverlapping = 0;
		for(Entry<Integer,Integer> entry: itemAtT.entrySet()) {
			Integer item = entry.getValue();
			if(XNoneOverlapping.contains(item)) 
				countSmallWindowsNonOverlapping++;
		}
		return countSmallWindowsNonOverlapping;
	}
	
	private int minWindow(Integer a, Integer bOrNull) {
		if(bOrNull == null) 
			return a;
		else 
			return Math.min(a, bOrNull);
	}

	private List<Window> project(SequentialPattern Z, List<Window> projectionX) {
		List<Integer> sequence = data.getSequence();
		if(Z.length() == 1) {
			List<Integer> positions = data.getPositions(Z.pattern.get(0));
			List<Window> windows = new ArrayList<Window>(positions.size());
			int maxwin = (int)Math.floor(this.alpha * this.maxsize);
			for(int pos: positions) {
				windows.add(new Window(pos, pos+1, Math.min(pos+maxwin,data.getSequenceSize())));
			}
			return windows;
		}
		else {
			List<Window> windows = new ArrayList<Window>(projectionX.size());
			int lastItem = Z.pattern.get(Z.length()-1);
			for(Window window: projectionX) {
				int found = -1;
				for(int i=window.a; i < window.b; i++) {
					Integer item = sequence.get(i);
					if(item == null)
						continue;
					if(item == lastItem) {
						found = i;
						break;
					}
				}
				if(found != -1) {
					windows.add(new Window(window.t, found+1, window.b));
				}
			}
			return windows;
		}
	}

	//compute Y
	private List<Integer> projectCandidates(SequentialPattern z, List<Window> projectionZ) {
		//union of all items in suffixes
		List<Integer> sequence = data.getSequence();
		CountMap<Integer> supportInP = new CountMap<Integer>();
		for(Window window: projectionZ) {
			for(int i=window.a; i < window.b; i++) {
				Integer item = sequence.get(i);
				if(item != null)
					supportInP.add(item);
			}
		}
		//sort on descending support in P
		return data.getItemsSorted(supportInP, false);
	}

	private boolean prune(SequentialPattern X, List<Window> P, List<Integer> Y, double mincoh) {
		final int xLen = X.length();
		if(xLen < 2) {
			return false;
		}
		//mingap pruning
		boolean overlap = false;
		for(Integer item: X.pattern) {
			if(Y.contains(item)) {
				overlap = true;
				break;
			}
		}
		if(!overlap) {
			int lengthZMax = Math.min(computeLengthZMax(X,P),maxsize); //change len 2018-1-17
			int mingap = computeMinGap(X,P);
			if(mingap + lengthZMax> alpha * (lengthZMax + xLen)) {
				return true;
			}
		}
		//c_maxquan pruning
		//step 1: compute supportZMax
		Set<Integer> XNoneOverlapping = new HashSet<Integer>();
		XNoneOverlapping.addAll(X.pattern);
		XNoneOverlapping.removeAll(Y);
		if(XNoneOverlapping.size() == 0) {
			return false;
		}
		int supportXNoneOverlapping  = data.support(XNoneOverlapping);
		int supportZMax = supportXNoneOverlapping + data.support(Y);
		//step 2: compute |Zmax| 
		int maxsizeYPlus = computeYPlus(X,P,Y);
		int lengthZMaxCorrect = Math.min(xLen + maxsizeYPlus, maxsize); //MIN!
		//step 3: compute count windows
		int countSmallWindowsNonOverlapping = computeNumberOfMinimalWindowsBasedOnProjection(X, XNoneOverlapping, P, lengthZMaxCorrect);
		int countLargeWindows = supportXNoneOverlapping - countSmallWindowsNonOverlapping;
		double maxQuantileCohesion = 1.0 - (countLargeWindows / (double)supportZMax);
		if(maxQuantileCohesion <= mincoh) {
			return true;
		}
		return false;
	}

	private int computeYPlus(SequentialPattern X, List<Window> projectionX, List<Integer> Y){
		List<Integer> sequence = data.getSequence();
		int[] multisetCount = new int[Y.size()];
		int[] windowCounts = new int[Y.size()];
		for(Window window: projectionX) {
			for(int i=window.a; i < window.b; i++) {
				Integer item = sequence.get(i);
				if(item != null) {
					int idx = Y.indexOf(item);
					if(idx != -1) //could be the case, e.g. item is filtered out of Y in previous DFS loop, but still in projection
						windowCounts[idx]+=1;
				}
			}
			for(int i=0; i<Y.size(); i++) {
				multisetCount[i] = Math.max(windowCounts[i],  multisetCount[i]);
			}
		}
		//Important: NOT return multiSetJoin.getMap().size();
		int maxlen = 0;
		for(int count: multisetCount) {
			maxlen += count;
		}
		return maxlen;
	}

	private int computeMinGap(SequentialPattern X, List<Window> P) {
		int mingap = Integer.MAX_VALUE;
		for(Window window: P) {
			mingap = Math.min(window.a - window.t,mingap);
		}
		return mingap;
	}

	private int computeLengthZMax(SequentialPattern X, List<Window> P){
		List<Integer> sequence = data.getSequence();
		int maxlen = 0;
		for(Window window: P) {
			//trim null elements
			int start = window.a;
			for(int i=window.a; i<window.b; i++) {
				if(sequence.get(i) == null){
					start +=1;
				}
				else {
					break;
				}
			}
			int end = window.b;
			for(int i=window.b-1; i>=window.a; i--) {
				if(sequence.get(i) == null){
					end -=1;
				}
				else {
					break;
				}
			}
			maxlen = Math.max(maxlen, end-start);
		}
		return X.length() + maxlen;
	}

	private <T> ArrayList<T> makeList(T first){
		ArrayList<T> lst = new ArrayList<T>();
		lst.add(first);
		return lst;
	}

	private static Comparator<Pair<SequentialPattern,Double>> heapComparator = new Comparator<Pair<SequentialPattern,Double>>() {

		@Override
		public int compare(Pair<SequentialPattern, Double> o1, Pair<SequentialPattern, Double> o2) {
			return Double.compare(o1.getSecond(),o2.getSecond()); //sort on descending qcohesion
		}
	};
	
	private String toString(List<Integer> pattern) {
		if(data.hasLabels()){
			return data.patternToString(pattern);
		}
		else {
			StringBuffer buff = new StringBuffer();
			buff.append("(");
			for(int i=0; i<pattern.size()-1; i++) {
				buff.append(Integer.valueOf(pattern.get(i)));
				buff.append(",");
			}
			if(pattern.size() > 0)
				buff.append(Integer.valueOf(pattern.get(pattern.size()-1)));
			buff.append(")");
			return buff.toString();
		}
	}
	
	//1 -1 2 -1 6 -1 #SUP: 2
	private String toStringSPMF(List<Integer> pattern) {
		StringBuffer buff = new StringBuffer();
		for(int i=0; i<pattern.size(); i++) {
			buff.append(Integer.valueOf(pattern.get(i)));
			buff.append(" -1 ");
		}
		return buff.toString();
	}
	
	public int support(List<Integer> pattern) {
		return data.support(pattern);
	}

}