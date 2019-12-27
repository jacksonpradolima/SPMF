package ca.pfv.spmf.algorithms.sequentialpatterns.skopus;

///*******************************************************************************
// * Copyright (C) 2015 Tao Li
// * 
// * This file is part of Skopus.
// * 
// * Skopus is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, version 3 of the License.
// * 
// * Skopus is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// * 
// * You should have received a copy of the GNU General Public License
// * along with Skopus.  If not, see <http://www.gnu.org/licenses/>.
// ******************************************************************************/
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.function.BinaryOperator;

import ca.pfv.spmf.tools.MemoryLogger;

public class ItemsetFinder {

	PriorityQueue<ItemsetRec> pqMItemsetTopk; // top K result sequential patterns
	private double dMMinValueGlobal; // min value of current results sequential pattern

	private ArrayList<Double> alMOuterBaseSupport; // the base support in all the partitions

	private HashMap<ItemsetRec, Integer> hmMCheckedSequenceCount;

	private BinPartitionTemplate[] aryMBinPartitionTemplate;
	private CombinTemplate[][] aryMCombineTemplate;
	
	private final int nTemplateLength = 10;	//Ĭ��Ϊ16
	
	public String strDebugFile = "";

	// Ĭ�Ϲ��캯��
	public ItemsetFinder() {
		pqMItemsetTopk = new PriorityQueue<ItemsetRec>(GlobalData.nK, new ItemsetAscSortByValue());

		dMMinValueGlobal = 0.0;

		alMOuterBaseSupport = new ArrayList<Double>(); // the base support in all the partitions

		hmMCheckedSequenceCount = new HashMap<ItemsetRec, Integer>();

//		aryMBinPartitionTemplate = new BinPartitionTemplate[GlobalData.nSampleMaxLength + 1];
//		aryMCombineTemplate = new CombinTemplate[GlobalData.nSampleMaxLength + 1][(int) (GlobalData.nSampleMaxLength / 2) + 2];
		
		aryMBinPartitionTemplate = new BinPartitionTemplate[nTemplateLength + 1]; //����11�ڵĲ�������ģ�壬����10�ģ�ʵʱ����
		aryMCombineTemplate = new CombinTemplate[nTemplateLength + 1][(int) (nTemplateLength / 2) + 1];		//����11�ڵĲ�������ģ�壬����10�ģ�ʵʱ����
		
	}

	
	
	//20150121��ʹ��Geoff����ȷ˼·��д
	// �ӱ�ѡitemsets�У���������Ȥ�ļ���
	public void generateResultItemsets() {
		// 1 ��ʼ��sequence pattern set��single item����available item sets
		ItemQClass iqAvailSymbols = new ItemQClass();
		for (int i = 0; i < GlobalData.nNumOfItems; i++) {
			iqAvailSymbols.insert(i);
		}
		
		//2 bootstrap
		iqAvailSymbols.sort();
		if (GlobalData.nInterestingnessMeasure != 1) {
			if ((((iqAvailSymbols.size()) * (iqAvailSymbols.size() - 1) * 1.0) / (GlobalData.nK * 1.0)) > 100.0 || GlobalData.nMaxResultPatternLength<3  ) {
				bootstrap2Symbols(iqAvailSymbols);
			} else {
				System.out.println("bootstrap 3");
				bootstrap3Symbols(iqAvailSymbols);
			}
		}
//		System.out.println("Bootstrap done");
		
		// 3 ���ɺ�ѡ���кͺ�ѡ�
		//iqAvailSymbols.sort();
		for (int i = 0; i < iqAvailSymbols.size(); i++) {
			//2.1���ɱ�ѡ�
			ItemsetRec r = new ItemsetRec();
			r.add(iqAvailSymbols.get(i).item);
			//2.2�ݹ����opus
			skopus(r, iqAvailSymbols);
		}
		MemoryLogger.getInstance().checkMemory();
	}
	
	
	//20150121��ʹ��Geoff����ȷ˼·��д
	private void skopus(final ItemsetRec irCandidateSequence,
			final ItemQClass iqAvailItems) {
		// 0 �������?
		if ((irCandidateSequence.size() < 1) || (iqAvailItems.size() < 1)) {
			return;
		}
		
		// 20150211-Ϊ�˼ӿ�ʵ���ٶȣ����г����޶���GlobalData.nMaxResultPatternLength����
		if ((GlobalData.nMaxResultPatternLength > 0)
				&&(irCandidateSequence.size() >= GlobalData.nMaxResultPatternLength)){
			return;
		}
		
		if (GlobalData.bDebugInformation) {
			GlobalOper.appendFileContent(strDebugFile, "\npreifx: "
					+ irCandidateSequence.toString() + "\n");
			GlobalOper.appendFileContent(strDebugFile, "symbols: "
					+ iqAvailItems.toString() + "\n\n");
		}
		// 1 ��ʼ��
		ItemQClass iqNewAvialItems = new ItemQClass();
		
		// 2 ѭ�����Available Item set�� update new available item set and candidate
		for (int ixSymbol = 0; ixSymbol < iqAvailItems.size(); ixSymbol++) {
			// 2.1 construct new ItemsetRec
			ItemsetRec irNewSequence = new ItemsetRec(irCandidateSequence);
			irNewSequence.add(iqAvailItems.get(ixSymbol).item);
			
			// 2.2 check the sequence
			evaluateSequencePattern(irNewSequence);
			
			//2.3����upper bound��������item set
			double dUBVal = getUpperBound(irNewSequence);
			if (dUBVal >= this.dMMinValueGlobal) {
				iqNewAvialItems.insert(iqAvailItems.get(ixSymbol).item, dUBVal);
			}//if (checkUpperBound(irNewSequence)) 
			
		}//for (int ixIitem = 0; ixIitem < iqAvailItems.size(); ixIitem++) 
		
		//3
		iqNewAvialItems.sort();
		for(int ix = 0; ix < iqNewAvialItems.size(); ix++){
			ItemsetRec irNewSequence = new ItemsetRec(irCandidateSequence);
			irNewSequence.add(iqNewAvialItems.get(ix).item);
			irNewSequence.value = iqNewAvialItems.get(ix).ubvalue;
			
			skopus(irNewSequence, iqNewAvialItems);
		}
		MemoryLogger.getInstance().checkMemory();
		return;
	}
	
	private void evaluateSequencePattern(ItemsetRec isCurrentSequence) {
		// 2.2 check the support
		int nCoverCount = SetsOper.getCoverAndCount(isCurrentSequence,
				new SidSet());

		// 2.3 check the interestingness
		switch (GlobalData.nInterestingnessMeasure) {
		case 1: // Support
			isCurrentSequence.value = calcInterestingness(nCoverCount, 0);

			if (GlobalData.bDebugInformation) {
				GlobalOper.appendFileContent(strDebugFile,
						isCurrentSequence.toString() + "\n");
			}// if (GlobalData.bDebugInformation)

			if (isCurrentSequence.value > dMMinValueGlobal) {
				insertOneResultSequence(isCurrentSequence);

				if (GlobalData.bDebugInformation) {
					// -----------------------------------------------------------------------------
					// a1.output debug information
					ArrayList<ItemsetRec> alItemsetsResult = new ArrayList<ItemsetRec>();
					Iterator<ItemsetRec> itItemsetsResult = pqMItemsetTopk
							.iterator();
					while (itItemsetsResult.hasNext()) {
						alItemsetsResult.add(new ItemsetRec(
								(ItemsetRec) itItemsetsResult.next()));
					}
					Collections.sort(alItemsetsResult,
							new ItemsetDecSortByValue());
					assert (pqMItemsetTopk.size() == alItemsetsResult.size());
					GlobalOper
							.appendFileContent(strDebugFile, "\t\t==topK==\t");
					GlobalOper.appendFileContent(
							strDebugFile,
							isCurrentSequence.toString()
									+ "\t"
									+ alItemsetsResult.get(alItemsetsResult
											.size() - 1).value + "\t"
									+ pqMItemsetTopk.size() + "\t"
									+ dMMinValueGlobal + "\n");
					// ---------------------------------------------------------------------------
				}// if (GlobalData.bDebugInformation)
			} // if (isCurrentSequence.value > dMMinValueGlobal)
			break;
		case 2: // Leverage
			checkSequenceLeverage(isCurrentSequence);
			break;
		default: // other
			break;
		}
		return;
	}
	
	
	/**
	 * general algorithm to generate the sequence pattern with any size
	 * */
	private void checkSequenceLeverage(ItemsetRec isTheOneSequence) {
		// 0. �������?
		assert (isTheOneSequence.size() >= 2);
		//assert (!isTheOneSequence.isAllSame());
		
		//GlobalOper.appendFileContent(strDebugFile, isTheOneSequence.toString() + "\n");
		//System.out.println(isTheOneSequence.toString());
		
		// 1.��ʼ��
		alMOuterBaseSupport.clear();

		int nCoverCount = isTheOneSequence.count;
		assert (nCoverCount>0);
		if(nCoverCount<=1){
			isTheOneSequence.value = 0.0;
			return;
		}
		hmMCheckedSequenceCount.clear();
		hmMCheckedSequenceCount.put(isTheOneSequence, nCoverCount);

		// 2.����ȫ�����ܵ�binary partition
		BinPartitionTemplate bpt = getPartitionTemplate(isTheOneSequence.size());
		BinPartitionSet bpset = new BinPartitionSet(bpt.size());
		bpset.createAllPartition(isTheOneSequence, bpt);	

		for (Iterator<BinPartition> iter = bpset.iterator(); iter.hasNext();) {
			// 3.1ȡ�õ�ǰbinary partition
			BinPartition bp = iter.next();
			if (bp == null)
				continue;

			checkOnePartition(bp, isTheOneSequence);
		}// for (Iterator<BinPartition> iter = bpset.iterator(); iter.hasNext();)

		// 4.��min support����max���õ�base support
		double nBaseCount = getMaxDouble(alMOuterBaseSupport);
		
		// 5.����Interestness value
		isTheOneSequence.value = calcInterestingness(nCoverCount, nBaseCount);

		
		if (GlobalData.bDebugInformation) {
			GlobalOper.appendFileContent(strDebugFile, "\n" + isTheOneSequence.toString() + "\n");
		}
		if ( isTheOneSequence.value > dMMinValueGlobal) {
			insertOneResultSequence(isTheOneSequence);
			
			
			if (GlobalData.bDebugInformation) {
				// -----------------------------------------------------------------------------
				// a1.output debug information
				ArrayList<ItemsetRec> alItemsetsResult = new ArrayList<ItemsetRec>();
				Iterator<ItemsetRec> itItemsetsResult = pqMItemsetTopk
						.iterator();
				while (itItemsetsResult.hasNext()) {
					alItemsetsResult.add(new ItemsetRec(
							(ItemsetRec) itItemsetsResult.next()));
				}
				Collections.sort(alItemsetsResult, new ItemsetDecSortByValue());
				assert (pqMItemsetTopk.size() == alItemsetsResult.size());
				GlobalOper.appendFileContent(strDebugFile, "\t\t==topK==\t");
				GlobalOper.appendFileContent(strDebugFile,
								isTheOneSequence.toString()+ "\t"
										+ alItemsetsResult.get(alItemsetsResult	.size() - 1).value + "\t"
										+ pqMItemsetTopk.size() + "\t"
										+ dMMinValueGlobal + "\n");
				// ---------------------------------------------------------------------------
			}// if (GlobalData.bDebugInformation)
		}
		return;
	}

	/**
	 * Check all possible combination of one Partition
	 * */
	private void checkOnePartition(BinPartition bpOnePartition,
			ItemsetRec irOriginalOne) {
		if (irOriginalOne.size() < 1)
			return;
		// 1. ��ʼ��
		// the inner base support in one partition (all the possible combinations)
		ArrayList<Integer> alMInnerBaseSupport = new ArrayList<Integer>();

		alMInnerBaseSupport.clear();

		// 2. �����������?
		ItemsetRec isLeft = bpOnePartition.getLeft();
		ItemsetRec isRight = bpOnePartition.getRight();
		
		///*
		CombinTemplate ct = getCombineTemplate(isLeft.size(), isRight.size());
		CombinSet cs = new CombinSet(ct.size());
		cs.createAllCombin(isLeft, isRight, ct); 	// when the sequence length is big, this step is time-consuming

		for (Iterator<ItemsetRec> iter = cs.iterator(); iter.hasNext();) {
			// 3.1 ��ϳ�һ���µ�sequence
			ItemsetRec isNew = iter.next();

			// 3.2 �鿴�µ�sequence��Ӧ���ǵ�ǰ����
			if (isNew.equals(irOriginalOne)) {
				alMInnerBaseSupport.add(irOriginalOne.count); // the original sequence coverage count
				continue;
			}

			// 3.3 check the new sequence is processed or not
			int nCoverCount = Integer.MAX_VALUE;

			Integer alreadyComputedSupport = hmMCheckedSequenceCount.get(isNew);
			if ((alreadyComputedSupport == null)) {
				nCoverCount = SetsOper.getCoverAndCount(isNew, new SidSet());
				
				hmMCheckedSequenceCount.put(isNew, nCoverCount);
			} else {
				isNew.count = hmMCheckedSequenceCount.get(isNew);
				nCoverCount = isNew.count;
				
			}

			alMInnerBaseSupport.add(nCoverCount);
			

		}// for (Iterator<ItemsetRec> iter = cs.iterator(); iter.hasNext();)
		alMOuterBaseSupport.add(getMean(alMInnerBaseSupport));
		
		return;
	}

	public String toString() {
		String strResult = new String();
//
//		strResult = "Total K Itemset Number: \t" + pqMItemsetTopk.size()
//				+ "\n";
		ArrayList<ItemsetRec> alItemsetsResult = new ArrayList<ItemsetRec>();
		Iterator<ItemsetRec> itItemsetsResult = pqMItemsetTopk.iterator();
		while (itItemsetsResult.hasNext()) {
			alItemsetsResult.add(new ItemsetRec((ItemsetRec) itItemsetsResult.next()));
		}
		Collections.sort(alItemsetsResult, new ItemsetDecSortByValue());
		for (int i = 0; i < alItemsetsResult.size(); i++) {
			strResult += alItemsetsResult.get(i).toString() + "\n";
		}

		return strResult;
	}

	public void outputResult(String strFilename) {
		java.io.File fileFileName = new java.io.File(strFilename);
		if (fileFileName.exists()) {
			fileFileName.delete();
		}

		// Print the patterns found
		GlobalOper.appendFileContent(strFilename, this.toString() );
	}

	private static final double getMean(ArrayList<Integer> al) {
//		System.out.println("list to compute the mean of: "+al.toString());
		Optional<Integer> res = al.stream().reduce(new BinaryOperator<Integer>() {
			@Override
			public Integer apply(Integer t, Integer u) {
				return t+u;
			}
		});
		if(res.isPresent()){
			return 1.0*res.get()/al.size();
		}else{
			return Double.MAX_VALUE;
		}
	}

	private static final double getMaxDouble(ArrayList<Double> al) {
		Optional<Double> res = al.stream().max(new Comparator<Double>() {
			@Override
			public int compare(Double o1, Double o2) {
				return Double.compare(o1, o2);
			}
		});
		if(res.isPresent()){
			return res.get();
		}else{
			return 0.0;
		}
	}

	private double calcInterestingness(int nCoverCount, double nBaseCoverCount) {
		double dResult = 0.0;
		switch (GlobalData.nInterestingnessMeasure) {
		case 1: // Support
			dResult = nCoverCount;
			break;
		case 2: // Leverage
			dResult = nCoverCount - nBaseCoverCount;
			break;
		default: // other
			dResult = 0.0;
			break;
		}

		return dResult;
	}

	// ���һ������Interestingness������sequence
	private void insertOneResultSequence(ItemsetRec ir) {
		if (ir.value <= dMMinValueGlobal || ir.size()<=1) {
			return;
		}
		if(pqMItemsetTopk.contains(ir)){
			return;
		}
		
		//now is going to insert the pattern
		ItemsetRec copy = new ItemsetRec(ir);
		while (pqMItemsetTopk.size() >= GlobalData.nK) {
			pqMItemsetTopk.poll();
		}
		pqMItemsetTopk.add(copy);
		if (pqMItemsetTopk.size() == GlobalData.nK) {
			double newMin = pqMItemsetTopk.peek().value;
			if (newMin > dMMinValueGlobal) {
				dMMinValueGlobal = newMin; // ����MPM��Сֵ��ȫ�ֱ��������ڴ˴����¡�
			}
		}
		if(pqMItemsetTopk.peek().value==0.0){
			System.err.println("was inserting "+copy+" when went to...");
		}
	}

	// ���Interestingness���Ͻ�
	private double getUpperBound(ItemsetRec ir) {
		int nCoverCount = ir.count;

		if (nCoverCount <= 1) {
			return -1.0;
		}

		double dResult = -1.0;
		switch (GlobalData.nInterestingnessMeasure) {
		case 1: // Support
			dResult = GlobalOper.computeCoverCount(nCoverCount);
			break;
		case 2: // Leverage
			dResult = GlobalOper.computeCoverCount(nCoverCount);
			break;
		default: // other
			dResult = -1.0;
			break;
		}

		return dResult;
	}
	
	private void bootstrap2Symbols(final ItemQClass iqAvailSymbols){
		for(int i = 0; i < iqAvailSymbols.size(); i++){
			for(int j = 0; j < iqAvailSymbols.size(); j++){
				// 2.1 construct new ItemsetRec
				ItemsetRec irNewSequence = new ItemsetRec();
				irNewSequence.add(iqAvailSymbols.get(i).item);
				irNewSequence.add(iqAvailSymbols.get(j).item);
				
				evaluateSequencePattern(irNewSequence);
//				if(irNewSequence.count>900){
//				String e1 = GlobalData.alItemName.get(iqAvailSymbols.get(i).item);
//				String e2 = GlobalData.alItemName.get(iqAvailSymbols.get(j).item);
//				System.out.println("<"+e1+","+e2+"> - support="+irNewSequence.count);
//				}
			}
		}
		System.out.println("Finished bootstrap depth 2");
	}
	
	private void bootstrap3Symbols(final ItemQClass iqAvailSymbols){
		
		bootstrap2Symbols(iqAvailSymbols);
		ItemsetRec pattern = new ItemsetRec();
		
		
		for (int i = 0; i < iqAvailSymbols.size(); i++) {
//			System.out.println(GlobalData.alItemName.get(iqAvailSymbols.get(i).item)+"\tworst in top-k = "+this.dMMinValueGlobal);
			pattern.add(iqAvailSymbols.get(i).item);
			
//			String e1 = GlobalData.alItemName.get(iqAvailSymbols.get(i).item);
//			if(!e1.equals("support"))continue;
			for (int j = 0; j < iqAvailSymbols.size(); j++) {
				// 2.1 construct new ItemsetRec
				pattern.add(iqAvailSymbols.get(j).item);
				// 2.2 evaluate
//				String e2 = GlobalData.alItemName.get(iqAvailSymbols.get(j).item);
				SetsOper.getCoverAndCount(pattern,new SidSet());
				double ub = getUpperBound(pattern);
				if(ub >= this.dMMinValueGlobal){
					for (int k = 0; k < iqAvailSymbols.size() && k<100; k++) {
//						String e3 = GlobalData.alItemName.get(iqAvailSymbols.get(k).item);
//						if(!e3.equals("machin"))continue;
						
						
						pattern.add(iqAvailSymbols.get(k).item);
						evaluateSequencePattern(pattern);
						
//						if(e1.equals("support")&&e2.equals("vector")){
//							System.out.println("\t"+GlobalData.alItemName.get(iqAvailSymbols.get(k).item));
//							System.out.println(pattern.value);
//						}
						pattern.remove(pattern.size()-1);
					}
				}
				pattern.remove(pattern.size()-1);
			}
			pattern.remove(pattern.size()-1);
		}
		System.out.println("finished bootstrap 3");
	}
	
	private void bootstrap4SymbolsFromTopK3(final ItemQClass iqAvailSymbols){
		bootstrap2Symbols(iqAvailSymbols);
		Iterator<ItemsetRec> itItemsetsResult = pqMItemsetTopk.iterator();
		ArrayList<ItemsetRec> copyTopK = new ArrayList<ItemsetRec>();
		while (itItemsetsResult.hasNext()) {
			ItemsetRec pattern = new ItemsetRec(itItemsetsResult.next());
			copyTopK.add(pattern);
		}
		
		for(ItemsetRec pattern:copyTopK){
			for (int i = 0; i < iqAvailSymbols.size(); i++) {
				for(int insertPosition=0;insertPosition<=pattern.size();insertPosition++){
					pattern.add(insertPosition,iqAvailSymbols.get(i).item);
					evaluateSequencePattern(pattern);
					pattern.remove(insertPosition);
				}
			}
		}
		
		itItemsetsResult = pqMItemsetTopk.iterator();
		copyTopK.clear();
		copyTopK = new ArrayList<ItemsetRec>();
		while (itItemsetsResult.hasNext()) {
			ItemsetRec pattern = new ItemsetRec(itItemsetsResult.next());
			copyTopK.add(pattern);
		}
		
		for(ItemsetRec pattern:copyTopK){
			for (int i = 0; i < iqAvailSymbols.size(); i++) {
				for(int insertPosition=0;insertPosition<=pattern.size();insertPosition++){
					pattern.add(insertPosition,iqAvailSymbols.get(i).item);
					evaluateSequencePattern(pattern);
					pattern.remove(insertPosition);
				}
			}
		}
		
	}
	
	private CombinTemplate getCombineTemplate(int nLeftLength, int nRightLength) {
		if ((nLeftLength < 1) || (nRightLength < 1)) {
			return null;
		}
		
		int nLeft;
		int nRight;
		if (nLeftLength <= nRightLength) {
			nLeft = nLeftLength;
			nRight = nRightLength;
		} else {
			nLeft = nRightLength;
			nRight = nLeftLength;
		}
		
		if ((nLeft + nRight) <= nTemplateLength) {
			if (this.aryMCombineTemplate[nLeft + nRight][nLeft] == null) {
				this.aryMCombineTemplate[nLeft + nRight][nLeft] = new CombinTemplate(nLeft, nRight);
			}
			return this.aryMCombineTemplate[nLeft + nRight][nLeft];
		} else {
			return new CombinTemplate(nLeft, nRight);
		}
	}

	private BinPartitionTemplate getPartitionTemplate(int nLength) {
		if (nLength < 2) {
			return null;
		}

		if (nLength <= nTemplateLength) {
			if (this.aryMBinPartitionTemplate[nLength] == null) {
				this.aryMBinPartitionTemplate[nLength] = new BinPartitionTemplate(nLength);
			}
			return this.aryMBinPartitionTemplate[nLength];
		} else {
			return new BinPartitionTemplate(nLength);
		}
	}
}

// ���ڽ���Ӵ�С���򣬸���itemsetRec����Ȥ��valueֵ
class ItemsetDecSortByValue implements Comparator<Object> {
	public int compare(Object o1, Object o2) {
		ItemsetRec s1 = (ItemsetRec) o1;
		ItemsetRec s2 = (ItemsetRec) o2;
		if (s1.value < s2.value) {
			return 1;
		} else if (s1.value == s2.value) {
			return 0;
		} else {
			return -1;
		}
	}
}

// ���ڽ��PriorityQueue����ıȽ��࣬С����Ȥ����ֵ�ڶ�ջ�������ڵ���?
class ItemsetAscSortByValue implements Comparator<Object> {
	public int compare(Object o1, Object o2) {

		ItemsetRec ir1 = (ItemsetRec) o1;
		ItemsetRec ir2 = (ItemsetRec) o2;
		if (ir1.value > ir2.value) {
			return 1;
		} else if (ir1.value == ir2.value) {
			return 0;
		} else {
			return -1;
		}

	}
}

