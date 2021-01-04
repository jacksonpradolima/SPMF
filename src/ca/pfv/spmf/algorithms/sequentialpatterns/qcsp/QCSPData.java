package ca.pfv.spmf.algorithms.sequentialpatterns.qcsp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import ca.pfv.spmf.algorithms.sequentialpatterns.qcsp.util.CountMap;
import ca.pfv.spmf.algorithms.sequentialpatterns.qcsp.util.FileStream;
import ca.pfv.spmf.algorithms.sequentialpatterns.qcsp.util.ListMap;
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
public class QCSPData {
	//data
	private List<Integer> sequenceList; //sequence of items
	private List<String> labelsList; //sequence of strings, e.g. item with id '0' is mapped to labelsList[0]
	private CountMap<Integer> support; //maps item id to support in S (e.g. support.get(0) = 200)
	private List<Integer> itemsSortedOnAscendingSupport; //items sorted (asscending support), e.g. erps-kwerps,....,moby
	private ListMap<Integer,Integer> itemPositions; //projection on first level, e.g. projection1.get(0) returns list of positions, that is [0, 102, 1003, 2003, 2010, 191919] etc


	public QCSPData() {}

	private int NULL_ITEM = 0; //for gaps
	private int SEPERATOR_ITEM = -1;
	private int END_ITEM = -2;

	public void loadData(File sequenceFile, File labelsFile, int minsup, double alpha, int maxsize, boolean debug) throws IOException {
		//validate
		if(!sequenceFile.canRead()) {
			throw new IOException("QCSP could not read sequence file " + sequenceFile.getPath());
		}
		if(labelsFile != null && !labelsFile.canRead()) {
			throw new IOException("QCSP could not read labels file " + labelsFile.getPath());
		}
		try {
			//Step 1: Load labels
			this.labelsList = new ArrayList<String>();
			if(labelsFile != null) {
				FileStream fs2 = new FileStream(labelsFile,' ','\n');
				String label = fs2.nextToken();
				while(label != null){
					labelsList.add(label);
					label = fs2.nextToken();
				}
			}
			//Step 2: Load sequence
			FileStream fs = new FileStream(sequenceFile,' ','\n');
			String token = fs.nextToken();
			int sizeSequence = 0;
			while(token !=null){
				token = fs.nextToken();
				sizeSequence++;
			}
			if(debug) {
				System.out.println("Sequence size: " + sizeSequence);
			}
			this.sequenceList = new ArrayList<Integer>(sizeSequence);
			fs = new FileStream(sequenceFile,' ','\n');
			token = fs.nextToken();
			while(token !=null){
				Integer item = Integer.parseInt(token);
				if(item == NULL_ITEM) { //if item 0, add gap
					sequenceList.add(null);
					token = fs.nextToken();
				}
				else if(item == SEPERATOR_ITEM) { //if item -1, ignore
					token = fs.nextToken();
				}
				else if(item == END_ITEM) { //if item -2, add max_size * alpha gaps, to avoid that quantile-based cohesive pattern occurrence spans multiple sequences
					for(int i=0; i < alpha * maxsize; i++) {
						sequenceList.add(null);
					}
					token = fs.nextToken();
				}
				else {
					sequenceList.add(item); 
					token = fs.nextToken();
				}	
			}

			//Step 3: Pre-compute support
			this.support = new CountMap<Integer>(); 
			for(Integer item: sequenceList) {
				if(item != null) {
					support.add(item);
				}
			}
			
			//Step 4: Remove infrequent items;
			Set<Integer> infrequent = new HashSet<>();
			for(Entry<Integer,Integer> itemWithSupport: support.getMap().entrySet()) {
				if(itemWithSupport.getValue() < minsup) {
					infrequent.add(itemWithSupport.getKey());
				}
			}
			if(debug) {
				System.out.print("Removing infrequent items:");
			}
			for(Integer item: infrequent) {
				support.remove(item);
				if(debug) {
					System.out.format("%d (%s), ",item, labelsList.size()>0?labelsList.get(item-1):null);
				}
			}
			if(debug) System.out.println();
			for(int i=0; i<sequenceList.size();i++) {
				Integer next = sequenceList.get(i);
				if(infrequent.contains(next)) {
					sequenceList.set(i, null);
				}
			}
			
			//Step 5: sort on ascending support
			this.itemsSortedOnAscendingSupport= getItemsSorted(support,true);

			//Step 6: Pre-compute poslist for level 1 projection
			this.itemPositions = new ListMap<>();
			for(int idx=0; idx < sequenceList.size(); idx++) {
				Integer item = sequenceList.get(idx);
				if(item != null)
					itemPositions.put(item, idx);
			}
		}catch(Exception e) {
			throw new RuntimeException("QCSP error loading data", e);
		}
	}
	
	public List<Integer> getItemsSortedOnAscendingSupport() {
		return itemsSortedOnAscendingSupport;
	}
	
	public List<Integer> getSequence(){
		return sequenceList;
	}
	
	public int getSequenceSize() {
		return sequenceList.size();
	}

	public List<Integer> getPositions(Integer item){
		return this.itemPositions.get(item);
	}
	
	public int support(Collection<Integer> items) {
		int support = 0;
		for(int item: items) {
			support += this.support.get(item);
		}
		return support;
	}

	public boolean hasLabels() {
		return !this.labelsList.isEmpty();
	}
	
	
	public List<Integer> getItemsSorted(CountMap<Integer> support, boolean ascending){
		List<Entry<Integer,Integer>> lst = new ArrayList<>(support.getMap().entrySet());
		final int sign = ascending? 1:-1;
		Collections.sort(lst, new Comparator<Entry<Integer,Integer>>() {

			@Override
			public int compare(Entry<Integer, Integer> o1, Entry<Integer, Integer> o2) {
				return sign * (o1.getValue() - o2.getValue());
			}
		});
		List<Integer> keys = new ArrayList<Integer>();
		for(Entry<Integer,Integer> entry: lst)
			keys.add(entry.getKey());
		return keys;
	}

	public String patternToString(List<Integer> X) {
		if(this.labelsList.isEmpty()) {
			throw new RuntimeException("No labels provided");
		}
		StringBuffer buff = new StringBuffer();
		buff.append("(");
		for(int i=0; i<X.size()-1; i++) {
			buff.append(this.labelsList.get(X.get(i)-1));
			buff.append(",");
		}
		if(X.size() > 0) {
			Integer last = X.get(X.size()-1);
			buff.append(this.labelsList.get(last-1));
		}
		buff.append(")");
		return buff.toString();
	}


}
