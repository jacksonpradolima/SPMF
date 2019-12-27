package ca.pfv.spmf.algorithms.frequentpatterns.biohuif;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is an implementation of the "Bio_HUIF_GA Algorithm" for High-Utility Itemsets Mining
 * as described in the paper : <br/><br/>
 * 
 * Wei Song, Chaomin Huang. Mining High Utility Itemsets Using Bio-Inspired Algorithms: 
 * A Diverse Optimal Value Framework. 
 * IEEE Access, 2018, 6(1): 19568-19582. 
 *  
 *
 * @author Wei Song, Chaomin Huang.
 */
public class AlgoBio_HUIF_BA {
	/** the maximum memory usage */
	double maxMemory = 0; 
	
	/** the time the algorithm started */
	long startTimestamp = 0; 
	
	/** the time the algorithm terminated */
	long endTimestamp = 0; //
	
	/** the size of population */
	final int pop_size = 100;
	
	/** maximum iterations */
	final int max_iter = 2000;
	
	/** the total num of transactions */
	int transactionCount=0;
	
	final double fmin = 0, fmax = 1;
	final double Amin = 0, Amax = 2;
	final double alpha = 0.8;
	final double gamma = 0.9;
	
	int gen;	
	
	Map<Integer,Integer> mapItemToUtil;
	
	Map<Integer,Integer> mapItemToSup;
	
	Map<Integer,Integer> mapItemToTWU;
	Map<Integer, Integer> mapItemToTWU0;
	
	List<Integer> twuPattern;
	
	BufferedWriter writer = null;
	class Pair{
		int item = 0;
		int utility = 0;
	}
	
	
	class BAIndividual{
			
		BitSet chrom;
		int velocity;
		double fitness;
		double freq;
		double loudness;
		double initEmissionrate;
		double emissionrate;
			
		public BAIndividual(){
			chrom = new BitSet(twuPattern.size());
			velocity = 2;
		}			
	
		/**
		 * ���
		 * @param tempBAIndividual
		 */
		public void deepcopy(BAIndividual tempBAIndividual){
			
			chrom = (BitSet)tempBAIndividual.chrom.clone();
			velocity = tempBAIndividual.velocity;
			fitness = tempBAIndividual.fitness;
			freq = tempBAIndividual.freq;
			loudness = tempBAIndividual.loudness;
			initEmissionrate = tempBAIndividual.initEmissionrate;
			emissionrate = tempBAIndividual.emissionrate;
		}
		
		
		public void calculateFitness(int k,List<Integer> templist) {
			if (k == 0)
				return;
			int i, p, q, temp,m;

			int sum, fitness = 0;
			for (m = 0; m < templist.size(); m++) { 
				p=templist.get(m).intValue();								
				i = 0;
				q = 0;
				temp = 0;
				sum = 0;
				
				while (q < database.get(p).size()
						&& i < this.chrom.length()){
					if(this.chrom.get(i)){
						if (database.get(p).get(q).item == twuPattern.get(i)){
							sum = sum + database.get(p).get(q).utility;
							++i;
							++q;
							++temp;
						}else{
							++q;
						}
					} else{
						++i;
					}
				}
				if (temp == k){
					fitness = fitness + sum;
				}
			}
			this.fitness = fitness;
		}
	}

	class HUI{
		
		String itemset;
		double fitness;
		
		public HUI(String itemset,double fitness){
			this.fitness = fitness;
			this.itemset = itemset;	
		}
	}

	class Item{
		int item;
		BitSet TIDS;
			
		public Item(){
			TIDS =  new BitSet(transactionCount);
		}
			
		public Item(int item){
			TIDS=new BitSet(transactionCount);
			this.item=item;	
		}
	}

	class ItemCompareBasedTWU implements Comparator{

		@Override
		public int compare(Object item1, Object item2) {
			// TODO Auto-generated method stub
			if(item1 instanceof Integer){
				return -(mapItemToTWU.get((Integer)item1) - mapItemToTWU.get((Integer)item2));
			}else{
				return -(mapItemToTWU.get(((Pair)item1).item) - mapItemToTWU.get(((Pair)item2).item));
			}
		}
		
	}
	
	class ItemCompareBasedUtil implements Comparator{

		@Override
		public int compare(Object item1, Object item2) {
			// TODO Auto-generated method stub
			if(item1 instanceof Integer){
				return (mapItemToUtil.get((Integer)item1) - mapItemToUtil.get((Integer)item2));
			}else {
				return (mapItemToUtil.get(((Pair)item1).item) - mapItemToUtil.get(((Pair)item2).item));
			}
		}
		
	}
	
	class ItemCompareBasedSup implements Comparator{

		@Override
		public int compare(Object item1, Object item2) {
			// TODO Auto-generated method stub
			if(item1 instanceof Integer){
				return -(mapItemToSup.get((Integer)item1) - mapItemToSup.get((Integer)item2));
			}else{
				return -(mapItemToSup.get(((Pair)item1).item) - mapItemToSup.get(((Pair)item2).item));
			}
		}
		
	}
	
	List<Double> percentHUIBA;
	
	BAIndividual gBest;
	
	List<BAIndividual> huiBA = new ArrayList<BAIndividual>();
	
	List<BAIndividual> population = new ArrayList<BAIndividual>();
	List<HUI> huiSets = new ArrayList<HUI>();
	List<List<Double>> V = new ArrayList<List<Double>>();
	
	List<Double> percentage = new ArrayList<Double>();
	
	List<List<Pair>> database = new ArrayList<List<Pair>>();
	
	List<Item> Items;
	
	

	public AlgoBio_HUIF_BA(){
	}
	
	public void runAlgorithm(String input,String output,int minUtility)throws IOException{
		
		maxMemory = 0;
	
		startTimestamp = System.currentTimeMillis();

		writer = new BufferedWriter(new FileWriter(output));
		
		mapItemToUtil = new HashMap<Integer,Integer>();
		
		mapItemToSup = new HashMap<Integer,Integer>();
	
		mapItemToTWU = new HashMap<Integer, Integer>();
		mapItemToTWU0 = new HashMap<Integer, Integer>();
	
		BufferedReader myInput = null;
		String thisLine;
	
	
		try {
			// prepare the object for reading the file
			myInput = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(input))));
			// for each line (transaction) until the end of file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is a comment, is empty or is a
				// kind of metadata
				if (thisLine.isEmpty() == true || thisLine.charAt(0) == '#'
						|| thisLine.charAt(0) == '%'
						|| thisLine.charAt(0) == '@') {
					continue;
				}

				// split the transaction according to the : separator
				String split[] = thisLine.split(":");
				// the first part is the list of items 
				String items[] = split[0].split(" ");
				//���һ���ִ洢���Ǹ���item��Ӧ��Ч��ֵ
				String utilityValues[] = split[2].split(" ");
				// the second part is the transaction utility 
				int transactionUtility = Integer.parseInt(split[1]);
				// for each item, we add the transaction utility to its TWU
				for (int i = 0; i < items.length; i++) {
					// convert item to integer
					Integer item = Integer.parseInt(items[i]);
					//��Ч��ֵת��ΪInteger
					Integer utility = Integer.parseInt(utilityValues[i]);
					
					Integer util = mapItemToUtil.get(item);
					util = (util == null) ? utility : util + utility;
					mapItemToUtil.put(item, util);
					
					Integer sup = mapItemToSup.get(item);
					sup = (sup == null) ? 1 : sup + 1;
					mapItemToSup.put(item,sup);
					// get the current TWU of that item
					Integer twu = mapItemToTWU.get(item);
					Integer twu0 = mapItemToTWU0.get(item);
					
					// add the utility of the item in the current transaction to
					// its twu
					twu = (twu == null) ? transactionUtility : twu
							+ transactionUtility;
					twu0 = (twu0 == null) ? transactionUtility : twu0
							+ transactionUtility;
					mapItemToTWU.put(item, twu);
					mapItemToTWU0.put(item, twu0);
				}
				++transactionCount;
			}
		} catch (Exception e) {
			// catches exception if error while reading the input file
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}
	
	

		try {
			// prepare object for reading the file
			myInput = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(input))));
			// variable to count the number of transaction
			// for each line (transaction) until the end of file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is a comment, is empty or is a
				// kind of metadata
				if (thisLine.isEmpty() == true || thisLine.charAt(0) == '#'
						|| thisLine.charAt(0) == '%'
						|| thisLine.charAt(0) == '@') {
					continue;
				}
				
				// split the line according to the separator
				String split[] = thisLine.split(":");
				// get the list of items
				String items[] = split[0].split(" ");
				// get the list of utility values corresponding to each item
				// for that transaction
				String utilityValues[] = split[2].split(" ");

				// Create a list to store items and its utility
				List<Pair> revisedTransaction = new ArrayList<Pair>();
				// Create a list to store items
				List<Integer> pattern = new ArrayList<Integer>();
				// for each item
				for (int i = 0; i < items.length; i++) {
					// / convert values to integers
					Pair pair = new Pair();
					pair.item = Integer.parseInt(items[i]);
					pair.utility = Integer.parseInt(utilityValues[i]);
					// if the item has enough utility
					if (mapItemToTWU.get(pair.item) >= minUtility) {
						// add it
						revisedTransaction.add(pair);
						pattern.add(pair.item);
					}else{
						mapItemToTWU0.remove(pair.item);
					}
				}
				// Copy the transaction into database but
				// without items with TWU < minutility
				database.add(revisedTransaction);
			}
		} catch (Exception e) {
			// to catch error while reading the input file
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}
	
		
		twuPattern = new ArrayList<Integer>(mapItemToTWU0.keySet());
		Collections.sort(twuPattern);
	
		System.out.println("twuPattern:"+twuPattern.size());
		System.out.println(twuPattern);
	
		Items = new ArrayList<Item>();
		
	
		for(Integer tempitem:twuPattern){
			Items.add(new Item(tempitem.intValue()));
		}
	
		
		for(int i=0;i<database.size();++i){
			for(int j=0;j<Items.size();++j){
				for(int k=0;k<database.get(i).size();++k){
					if(Items.get(j).item==database.get(i).get(k).item){
						Items.get(j).TIDS.set(i);
					}
				}
			}
		}
		
		checkMemory();
		
		if(twuPattern.size()>0){
			gBest = new BAIndividual();
			pop_Init(minUtility);
		
			for( gen =0; gen < max_iter; ++gen){
				next_Gen_BA(minUtility);
				if(huiBA.size() != 0){
					percentHUIBA = roulettePercentHUIBA();
					int num = rouletteSelectHUIBA(percentHUIBA);
					gBest.deepcopy(huiBA.get(num));
				}
				if(gen%200==0){
					System.out.println("����������"+"	"+gen+"-update end. HUIs No. is "
							+ huiSets.size());
				}
			}
		}
		
		writeOut();
		// check the memory usage again and close the file.
		checkMemory();
		// close output file
		writer.close();
		// record end time
		endTimestamp = System.currentTimeMillis();	
	}
	
	private void pop_Init(double minUtility){
		int i,j,k,temp;
		
		List<Integer> transList;
		
		percentage = roulettePercent();
		System.out.println(percentage);
		
		for(i = 0;i<pop_size;++i){
			BAIndividual tempBAIndividual = new BAIndividual();
			
			j=0;
			
			k = (int)(Math.random()*twuPattern.size());
			
			
			while(j<k){
				temp = rouletteSelect(percentage);
				if(!tempBAIndividual.chrom.get(temp)){
					++j;
					tempBAIndividual.chrom.set(temp);
				}	
			}
			
			
			transList=new ArrayList<Integer>();
			pev_Check(tempBAIndividual,transList);
			tempBAIndividual.calculateFitness(k, transList);
			
			
			tempBAIndividual.freq = fmin + (fmax-fmin)*Math.random();
			tempBAIndividual.loudness = Amin + (Amax-Amin)*Math.random();
			tempBAIndividual.initEmissionrate = Math.random();
			tempBAIndividual.emissionrate = tempBAIndividual.initEmissionrate;
			
			
			population.add(tempBAIndividual);
			if(population.get(i).fitness>=minUtility&&population.get(i).chrom.cardinality()>0){
				
				insert(population.get(i));
				addHuiBA(population.get(i));
			}
			
			if(i==0){
				gBest.deepcopy(population.get(i));
			}else{
				if(population.get(i).fitness>=gBest.fitness){
					gBest.deepcopy(population.get(i));
				}
			}
		}
	}
	
	private void next_Gen_BA(int minUtility){
		
		double rnd,sum;
		int i,k,num,changeBit;
		
		List<Integer> transList;
		

		
		for( i = 0; i< pop_size;++i){
			
			population.get(i).freq = fmin +(fmax - fmin)*Math.random();
			List<Integer> disList = bitDiff(gBest,population.get(i));
			
			num = (int)(disList.size()*population.get(i).freq) + 1;
			if(disList.size()>0){
				for(int m = 0; m < num; ++m){
					changeBit = (int)(disList.size()*Math.random());
					
					if(population.get(i).chrom.get(disList.get(changeBit))){
						population.get(i).chrom.clear(disList.get(changeBit));
					}else{
						population.get(i).chrom.set(disList.get(changeBit));
					}
				}
			}
			for(int m = 0; m < 1;++m){
				changeBit = (int)(twuPattern.size()*Math.random());
				if(population.get(i).chrom.get(changeBit)){
					population.get(i).chrom.clear(changeBit);
				}else{
					population.get(i).chrom.set(changeBit);
				}
			}
		
			transList = new ArrayList<Integer>();
			pev_Check(population.get(i),transList);
			population.get(i).calculateFitness(population.get(i).chrom.cardinality(), transList);
			if(population.get(i).fitness >= minUtility&&population.get(i).chrom.cardinality()>0){
				insert(population.get(i));
				addHuiBA(population.get(i));
			}
			if(population.get(i).fitness > gBest.fitness){
				gBest.deepcopy(population.get(i));
			}
			
			rnd = Math.random();
			sum=0;
			for(int m=0;m<pop_size;++m){
				sum += population.get(m).loudness;
			}
			
			BAIndividual tmpBAIndividual = new BAIndividual();
			
			tmpBAIndividual.deepcopy(population.get(i));
			
			if(rnd > population.get(i).emissionrate){
				k = (int)(Math.random()*twuPattern.size());
				if(tmpBAIndividual.chrom.get(k)){
					tmpBAIndividual.chrom.clear(k);
				}else{
					tmpBAIndividual.chrom.set(k);
				}
			}
			transList = new ArrayList<Integer>();
			pev_Check(tmpBAIndividual,transList);
			tmpBAIndividual.calculateFitness(tmpBAIndividual.chrom.cardinality(), transList);
			
			if(tmpBAIndividual.fitness >= minUtility&&tmpBAIndividual.chrom.cardinality()>0){
				insert(tmpBAIndividual);
				addHuiBA(tmpBAIndividual);
			}
			
			if(tmpBAIndividual.fitness > gBest.fitness){
				gBest.deepcopy(tmpBAIndividual);
			}
			
			if(tmpBAIndividual.fitness < gBest.fitness && Math.random() < population.get(i).loudness){
				population.get(i).deepcopy(tmpBAIndividual);
				population.get(i).loudness *= alpha;
				population.get(i).emissionrate = population.get(i).initEmissionrate*(1 - Math.exp(-gamma*gen));
			}
		}
		
	}
	
	private List<Integer> bitDiff(BAIndividual gBest,BAIndividual tempBAIndividual){
		List<Integer> list = new ArrayList<Integer>();
		BitSet tmpBitSet = (BitSet)gBest.chrom.clone();
		tmpBitSet.xor(tempBAIndividual.chrom);
		for(int i = 0; i < tmpBitSet.length(); ++i){
			if(tmpBitSet.get(i)){
				list.add(i);
			}
		}
		return list;	
	}	
	//check itemset is promising or unpromising
	public boolean pev_Check(BAIndividual tempBAIndividual,List<Integer> list){
		List<Integer> templist=new ArrayList<Integer>();
		//int temp=0;
		for(int i=0;i<tempBAIndividual.chrom.length();++i){
			if(tempBAIndividual.chrom.get(i)){
				templist.add(i);
			}	
		}
		if(templist.size()==0){
			return false;
		}
		BitSet tempBitSet = new BitSet(transactionCount);
		BitSet midBitSet = new BitSet(transactionCount);
		tempBitSet = (BitSet)Items.get(templist.get(0).intValue()).TIDS.clone();
		midBitSet = (BitSet)tempBitSet.clone();
		
		for(int i=1;i<templist.size();++i){
			tempBitSet.and(Items.get(templist.get(i).intValue()).TIDS);
			if(tempBitSet.cardinality() != 0){
				midBitSet = (BitSet)tempBitSet.clone();
			}else{
				tempBitSet = (BitSet)midBitSet.clone();
				tempBAIndividual.chrom.clear(templist.get(i).intValue());
			}
		}
		
		if(tempBitSet.cardinality()==0){
			return false;
		}else{
			for(int m=0;m<transactionCount;++m){
				if(tempBitSet.get(m)){
					list.add(m);
				}	
			}
			return true;	
		}
	}
	/**
	 * 
	 * @return
	 */
	private List<Double> roulettePercentHUIBA() {
		double sum = 0;
		double tempsum = 0;
		double percent = 0.0;
		List<Double> percentHUIBA = new ArrayList<Double>();
		for(int i = 0; i < huiBA.size(); ++i){
			sum += huiBA.get(i).fitness;
		}
		for(int i = 0; i < huiBA.size();++i){
			tempsum += huiBA.get(i).fitness;
			percent = tempsum/sum;
			percentHUIBA.add(percent);
		}
		return percentHUIBA;
		
	}
	
	private int rouletteSelectHUIBA(List<Double> percentage) {
		int i,temp=0;
		double randNum;
		randNum = Math.random();
		for (i = 0; i < percentage.size(); i++) {
			if (i == 0) {
				if ((randNum >= 0) && (randNum <= percentage.get(0))) {
					temp = 0;
					break;
				}
			} else if ((randNum > percentage.get(i - 1))
					&& (randNum <= percentage.get(i))) {
				temp = i;
				break;
			}
		}
		return temp;
	}
	
	private void addHuiBA(BAIndividual tempBAIndividual){
		BAIndividual tmpBAIndividual = new BAIndividual();
		tmpBAIndividual.deepcopy(tempBAIndividual);
		BitSet tmpBitSet;
		if(huiBA.size() != 0){
			for(int i = 0; i < huiBA.size(); ++i){
				tmpBitSet = (BitSet)(tmpBAIndividual.chrom.clone());
				tmpBitSet.xor(huiBA.get(i).chrom);
				if(tmpBitSet.cardinality() == 0){
					return ;
				}
			}
		}	
		huiBA.add(tmpBAIndividual);
	}
	/**
	 * 
	 * @param tempBAIndividual
	 */
	private void insert(BAIndividual tempBAIndividual) {
		int i;
		StringBuilder temp = new StringBuilder();
		for (i = 0; i < twuPattern.size(); i++) {
			if (tempBAIndividual.chrom.get(i)) {
				temp.append(twuPattern.get(i));
				temp.append(' ');
			}
		}
		// huiSets is null
		if (huiSets.size() == 0) {
			huiSets.add(new HUI(temp.toString(), tempBAIndividual.fitness));
		} else {
			// huiSets is not null, judge whether exist an itemset in huiSets
			// same with tempChroNode
			for (i = 0; i < huiSets.size(); i++) {
				if (temp.toString().equals(huiSets.get(i).itemset)) {
					break;
				}
			}
			// if not exist same itemset in huiSets with tempChroNode,insert it
			// into huiSets
			if (i == huiSets.size()){
				huiSets.add(new HUI(temp.toString(), tempBAIndividual.fitness));
			}
		}
	}	
	
	private List<Double> roulettePercent() {
		int i;
		double sum = 0, tempSum = 0;
		double tempPercent;

		for (i = 0; i < twuPattern.size(); i++) {
			sum = sum + mapItemToTWU.get(twuPattern.get(i));
		}
		// calculate the portation of twu value of each item in sum
		for (i = 0; i < twuPattern.size(); i++) {
			tempSum = tempSum + mapItemToTWU.get(twuPattern.get(i));
			tempPercent = tempSum / (sum + 0.0);
			percentage.add(tempPercent);
		}
		return percentage;
	}
	/**
	 * Method to ensure the posotion of 1 in particle use roulette selection
	 * 
	 * @param percentage
	 *            the portation of twu value of each 1-HTWUIs in sum of twu
	 *            value
	 * @return the position of 1
	 */
	private int rouletteSelect(List<Double> percentage) {
		int i, temp = 0;
		double randNum;
		randNum = Math.random();
		for (i = 0; i < percentage.size(); i++) {
			if (i == 0) {
				if ((randNum >= 0) && (randNum <= percentage.get(0))) {
					temp = 0;
					break;
				}
			} else if ((randNum > percentage.get(i - 1))
					&& (randNum <= percentage.get(i))) {
				temp = i;
				break;
			}
		}
		return temp;
	}
	
	private void writeOut() throws IOException {
		// Create a string buffer
		StringBuilder buffer = new StringBuilder();
		// append the prefix
		for (int i = 0; i < huiSets.size(); i++) {
			buffer.append(huiSets.get(i).itemset);
			// append the utility value
			buffer.append("#UTIL: ");
			buffer.append(huiSets.get(i).fitness);
			if(i != huiSets.size() -1){
				buffer.append(System.lineSeparator());
			}
		}
		// write to file
		writer.write(buffer.toString());
	}
	
	private void checkMemory() {
		// get the current memory usage
		double currentMemory = (Runtime.getRuntime().totalMemory() - Runtime
				.getRuntime().freeMemory()) / 1024d / 1024d;
		// if higher than the maximum until now
		if (currentMemory > maxMemory) {
			// replace the maximum with the current memory usage
			maxMemory = currentMemory;
		}
	}
	public void printStats() {
		System.out
				.println("=============  HUIF-BA ALGORITHM v.2.36 - STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp)
				+ " ms");
		System.out.println(" Memory ~ " + maxMemory + " MB");
		System.out.println(" High-utility itemsets count : " + huiSets.size());
		System.out
				.println("===================================================");
	}

}
