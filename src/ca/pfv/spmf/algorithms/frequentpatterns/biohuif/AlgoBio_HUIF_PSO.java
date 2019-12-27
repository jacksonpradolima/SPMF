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
public class AlgoBio_HUIF_PSO {
	double maxMemory = 0; // the maximum memory usage
	long startTimestamp = 0; // the time the algorithm started
	long endTimestamp = 0; // the time the algorithm terminated
	final int pop_size = 100;//the size of population
	final int max_iter = 2000;// maximum iterations
	int transactionCount=0;//the total num of transactions
	
	Map<Integer, Integer> mapItemToTWU;
	Map<Integer, Integer> mapItemToTWU0;
	List<Integer> twuPattern;// the items which has twu value more than minUtil
	
	BufferedWriter writer = null; // writer to write the output file
	
	// this class represent an item and its utility in a transaction
	class Pair {
		int item = 0;
		int utility = 0;
	}
	// this class represent the particles
	class Particle {
		BitSet X;// the particle
		int fitness;// fitness value of particle

		 Particle() {
			X = new BitSet(twuPattern.size());
			fitness = 0;
		}

		 Particle(int length) {
			X = new BitSet(length);
			fitness = 0;
		}
		
		 void copyParticle(Particle particle1){
			this.X=(BitSet)particle1.X.clone();
			this.fitness = particle1.fitness;
		}
		
		 void calculateFitness(int k, List<Integer> templist) {
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
						&& i < this.X.length()){
					if(this.X.get(i)){
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
	
	class HUI {
		String itemset;
		int fitness;

		public HUI(String itemset, int fitness) {
			super();
			this.itemset = itemset;
			this.fitness = fitness;
		}

	}	
	
	//use Item to create bitmap
	class Item{
		int item;
		BitSet TIDS;
			
		public Item(){
			TIDS =  new BitSet(database.size());
		}
			
		public Item(int item){
			TIDS=new BitSet(database.size());
			this.item=item;	
		}
	}
	
	Particle gBest;// the gBest particle in populations
	List<Particle> pBest = new ArrayList<Particle>();// each pBest particle in populations
	List<Particle> population = new ArrayList<Particle>();// populations
	List<HUI> huiSets = new ArrayList<HUI>();// the set of HUIs
	List<Double> percentage = new ArrayList<Double>();// the portation of twu value of each
														// 1-HTWUIs in sum of twu value
	// Create a list to store database
	List<List<Pair>> database = new ArrayList<List<Pair>>();
	
	List<Item> Items;//bitmap database representation
	
	List<Particle> huiBA = new ArrayList<Particle>();//store hui Particles
	
	List<Double> percentHUIBA;
	/**
	 * Default constructor
	 */
	public AlgoBio_HUIF_PSO() {
	}
	/**
	 * Run the algorithm
	 * 
	 * @param input
	 *            the input file path
	 * @param output
	 *            the output file path
	 * @param minUtility
	 *            the minimum utility threshold
	 * @throws IOException
	 *             exception if error while writing the file
	 */
	public void runAlgorithm(String input, String output, int minUtility)
			throws IOException {
		// reset maximum
		maxMemory = 0;

		startTimestamp = System.currentTimeMillis();

		writer = new BufferedWriter(new FileWriter(output));

		// We create a map to store the TWU of each item
		mapItemToTWU = new HashMap<Integer, Integer>();
		mapItemToTWU0 = new HashMap<Integer, Integer>();

		// We scan the database a first time to calculate the TWU of each item.
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
				++transactionCount;//�����������ݿ���������

				// split the transaction according to the : separator
				String split[] = thisLine.split(":");
				// the first part is the list of items
				String items[] = split[0].split(" ");
				// the second part is the transaction utility
				int transactionUtility = Integer.parseInt(split[1]);
				// for each item, we add the transaction utility to its TWU
				for (int i = 0; i < items.length; i++) {
					// convert item to integer
					Integer item = Integer.parseInt(items[i]);
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
			}
		} catch (Exception e) {
			// catches exception if error while reading the input file
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}
		// SECOND DATABASE PASS TO CONSTRUCT THE DATABASE
		// OF 1-ITEMSETS HAVING TWU >= minutil (promising items)
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
		////scan database to create bitmap
		for(int i=0;i<database.size();++i){
			for(int j=0;j<Items.size();++j){
				for(int k=0;k<database.get(i).size();++k){
					if(Items.get(j).item==database.get(i).get(k).item){
						Items.get(j).TIDS.set(i);
					}
				}
			}
		}
		//init pBest
		for(int i=0;i<pop_size;++i){
			pBest.add(new Particle(twuPattern.size()));
		}
		//global Best
		gBest = new Particle(twuPattern.size());
		
		// check the memory usage
		checkMemory();
		// Mine the database recursively
		if (twuPattern.size() > 0) {
			// initial population
			pop_Init(minUtility);
			for (int i = 0; i < max_iter; i++) {
				
				// update population and HUIset
				next_Gen_PA(minUtility);
				if(huiBA.size() != 0){
					percentHUIBA = roulettePercentHUIBA();
					int num = rouletteSelectHUIBA(percentHUIBA);
					gBest.copyParticle(huiBA.get(num));
				}
				if(i%200==0){
					System.out.println(i + "-update end. HUIs No. is "
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
	/**
	 * This is the method to initial population
	 * 
	 * @param minUtility
	 *            minimum utility threshold
	 */
	private void pop_Init(int minUtility)//
	{
		int i, j, k, temp;
		
		List<Integer> transList;//��������itemset���ڵ�����ļ���
		// initial percentage according to the twu value of 1-HTWUIs
		percentage = roulettePercent();
		
		System.out.println(percentage);

		for (i = 0; i < pop_size; i++) {
			// initial particles
			Particle tempParticle = new Particle(twuPattern.size());
			j = 0;
			// k is the count of 1 in particle
			k = (int) (Math.random() * twuPattern.size());

			while (j < k) {
				// roulette select the position of 1 in population
				temp = rouletteSelect(percentage);
				if (!tempParticle.X.get(temp)) {
					j++;
					tempParticle.X.set(temp);
				}

			}
			//������Ӧ��
			transList=new ArrayList<Integer>();//�洢itemset���ڵ�����ļ���
			pev_Check(tempParticle,transList);
			tempParticle.calculateFitness(k, transList);
			
			// insert particle into population
			population.add(i, tempParticle);
			// initial pBest
			//pBest.add(i, population.get(i));
			pBest.get(i).copyParticle(tempParticle);
			// update huiSets
			if (population.get(i).fitness >= minUtility) {
				insert(population.get(i));
				addHuiBA(population.get(i));
			}
			// update gBest
			if (i == 0) {
				//gBest = pBest.get(i);
				gBest.copyParticle(pBest.get(i));
			} else {
				if (pBest.get(i).fitness > gBest.fitness) {
					gBest.copyParticle(pBest.get(i));
				}
			}
		}
	}
	/**
	 * Methos to update particle, pBest and gBest
	 * 
	 * @param minUtility
	 */
	private void next_Gen_PA(int minUtility) {
		int i, k,num,changeBit;
		
		List<Integer> disList;
		List<Integer> transList;

		for (i = 0; i < pop_size; i++) {
			//��i������������ʷ���Ž������������������λ���б��Ա������������Լ��õķ�������ƶ�
			disList = bitDiff(pBest.get(i),population.get(i));
			
			//ʹ�������������ӿ�£
			num = (int)(disList.size()*Math.random()) + 1;
			if(disList.size()>0){
				for(int m = 0; m < num; ++m){
					changeBit = (int)(disList.size()*Math.random());
					//System.out.println(changeBit);
					if(population.get(i).X.get(disList.get(changeBit))){
						population.get(i).X.clear(disList.get(changeBit));
					}else{
						population.get(i).X.set(disList.get(changeBit));
					}
				}
			}
			//��i��������ȫ�����Ž������������������λ���б��Ա���������ȫ�����Ž����ƶ�
			disList = bitDiff(gBest,population.get(i));
			
			//ʹ������������ʷ���ſ�£
			num = (int)(disList.size()*Math.random()) + 1;
			if(disList.size()>0){
				for(int m = 0; m < num; ++m){
					changeBit = (int)(disList.size()*Math.random());
					//System.out.println(changeBit);
					if(population.get(i).X.get(disList.get(changeBit))){
						population.get(i).X.clear(disList.get(changeBit));
					}else{
						population.get(i).X.set(disList.get(changeBit));
					}
				}
			}
			//�����еĲ��������ƶ��������ٶ�
			for(int m = 0; m < 1;++m){
				changeBit = (int)(twuPattern.size()*Math.random());
				if(population.get(i).X.get(changeBit)){
					population.get(i).X.clear(changeBit);
				}else{
					population.get(i).X.set(changeBit);
				}
			}

			k=population.get(i).X.cardinality();
			transList=new ArrayList<Integer>();
			pev_Check(population.get(i),transList);
			
			population.get(i).calculateFitness(k, transList);
		
			// update pBest & gBest
			if (population.get(i).fitness > pBest.get(i).fitness) {
				//pBest.set(i, population.get(i));
				pBest.get(i).copyParticle(population.get(i));
				if (pBest.get(i).fitness > gBest.fitness) {
					gBest.copyParticle(pBest.get(i));
				}
			}
			// update huiSets
			if (population.get(i).fitness >= minUtility) {
				insert(population.get(i));
				addHuiBA(population.get(i));
			}
		}
	}

	/**
	 *check itemset is promising or unpromising
	 * @param tempBAIndividual
	 * @param list
	 * @return
	 */
	 boolean pev_Check(Particle tempBAIndividual,List<Integer> list){
		List<Integer> templist=new ArrayList<Integer>();//�惦�����0��λ��
		//int temp=0;
		for(int i=0;i<tempBAIndividual.X.length();++i){
			if(tempBAIndividual.X.get(i)){
				templist.add(i);
			}	
		}
		if(templist.size()==0){
			return false;
		}
		BitSet tempBitSet = new BitSet(transactionCount);
		BitSet midBitSet = new BitSet(transactionCount);
		tempBitSet = (BitSet)Items.get(templist.get(0).intValue()).TIDS.clone();
		midBitSet = (BitSet)tempBitSet.clone();//��¼�м���
		
		//item��λͼ���������������ʹ��itemset�������item��������itemset��ȥ����item
		for(int i=1;i<templist.size();++i){
			tempBitSet.and(Items.get(templist.get(i).intValue()).TIDS);
			if(tempBitSet.cardinality() != 0){
				midBitSet = (BitSet)tempBitSet.clone();
			}else{
				tempBitSet = (BitSet)midBitSet.clone();
				tempBAIndividual.X.clear(templist.get(i).intValue());
			}
		}
		
		if(tempBitSet.cardinality()==0){
			return false;
		}else{
			for(int m=0;m<tempBitSet.length();++m){
				if(tempBitSet.get(m)){
					list.add(m);
				}	
			}
			return true;	
		}
	}
	/**
	 * xor(itemset1,itemset2)
	 * @param gBest
	 * @param tempBAIndividual
	 * @return
	 */
	private List<Integer> bitDiff(Particle gBest,Particle tempBAIndividual){
		List<Integer> list = new ArrayList<Integer>();
		BitSet tmpBitSet = (BitSet)gBest.X.clone();
		tmpBitSet.xor(tempBAIndividual.X);
		for(int i = 0; i < tmpBitSet.length(); ++i){
			if(tmpBitSet.get(i)){
				list.add(i);
			}
		}
		return list;	
	}
	/**
	 * add hui Particles to HuiBA
	 * @param tempBAIndividual
	 */
	private void addHuiBA(Particle tempBAIndividual){
		Particle tmpBAIndividual = new Particle();
		tmpBAIndividual.copyParticle(tempBAIndividual);
		BitSet tmpBitSet;
		if(huiBA.size() != 0){
			for(int i = 0; i < huiBA.size(); ++i){
				tmpBitSet = (BitSet)(tmpBAIndividual.X.clone());
				tmpBitSet.xor(huiBA.get(i).X);
				if(tmpBitSet.cardinality() == 0){
					return ;
				}
			}
		}	
		huiBA.add(tmpBAIndividual);
	}
	//Method to initial percentHUIBA
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
	//Method to roulette select Particles to replace Particle of population
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
	/**
	 * Method to inseret tempParticle to huiSets
	 * 
	 * @param tempParticle
	 *            the particle to be inserted
	 */
	private void insert(Particle tempParticle) {
		int i;
		StringBuilder temp = new StringBuilder();
		for (i = 0; i < twuPattern.size(); i++) {
			if (tempParticle.X.get(i)) {
				temp.append(twuPattern.get(i));
				temp.append(' ');
			}
		}
		// ========== ADDED BY PHILIPPE 2019-01 otherwise some empty itemsets may be output
		if(temp.length() == 0){
			return;
		}
		//========================================================================
		
		// huiSets is null
		if (huiSets.size() == 0) {
			huiSets.add(new HUI(temp.toString(), tempParticle.fitness));
		} else {
			// huiSets is not null, judge whether exist an itemset in huiSets
			// same with tempParticle
			for (i = 0; i < huiSets.size(); i++) {
				if (temp.toString().equals(huiSets.get(i).itemset)) {
					break;
				}
			}
			// if not exist same itemset in huiSets with tempParticle,insert it
			// into huiSets
			if (i == huiSets.size())
				huiSets.add(new HUI(temp.toString(), tempParticle.fitness));
		}
	}

	/**
	 * Method to initial percentage
	 * 
	 * @return percentage
	 */
	private List<Double> roulettePercent() {
		int i;
		double sum = 0, tempSum = 0;
		double tempPercent;

		// calculate the sum of twu value of each 1-HTWUIs
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

	/**
	 * Method to write a high utility itemset to the output file.
	 * 
	 * @throws IOException
	 */
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
	

	/**
	 * Method to check the memory usage and keep the maximum memory usage.
	 */
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

	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats() {
		System.out
				.println("=============  HUIF-PSO ALGORITHM v.2.11 - STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp)
				+ " ms");
		System.out.println(" Memory ~ " + maxMemory + " MB");
		System.out.println(" High-utility itemsets count : " + huiSets.size());
		System.out
				.println("===================================================");
	}
}
