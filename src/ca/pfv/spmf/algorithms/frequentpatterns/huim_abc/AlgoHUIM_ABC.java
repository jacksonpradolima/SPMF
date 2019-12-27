package ca.pfv.spmf.algorithms.frequentpatterns.huim_abc;

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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * * * * 
 * Copyright (c) 2016 Wei Song, Chaomin Huang
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE 
 * (http://www.philippe-fournier-viger.com/spmf).
 * 
 * 
 * SPMF is free software: you can redistribute it and/or modify it under the *
 * terms of the GNU General Public License as published by the Free Software *
 * Foundation, either version 3 of the License, or (at your option) any later *
 * version. *
 * 
 * SPMF is distributed in the hope that it will be useful, but WITHOUT ANY *
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * *
 * 
 * You should have received a copy of the GNU General Public License along with
 * * SPMF. If not, see .
 */


/**
 * This is an implementation of the "HUIM-ABC Algorithm" for High-Utility Itemsets Mining
 * as described in the conference paper :
 * 
 * Discovering High Utility Itemsets Based on the Artificial Bee Colony Algorithm[C]
 * Pacific-Asia Conference on Knowledge Discovery and Data Mining. Springer, Cham, 2018.
 *
 * @author Wei Song,Chaomin Huang
 */
public class AlgoHUIM_ABC {
	// variable for statistics
	double maxMemory = 0; // the maximum memory usage
	long startTimestamp = 0; // the time the algorithm started
	long endTimestamp = 0; // the time the algorithm terminated
	int transactionCount=0;// total number of transactions in the database
	
	//final int NP=40;// employed bees plus onlooker bees
	final int pop_size =10;//The amount of nectar source is the same as the number of employed bees and onlooker bees
	final int limit = 5;//beyond this limit has not been updated, employed bee become Scout Bee
						 //abandon current nectar source and choose another
	final int iterations = 2000;// the iterations of algorithms
	int changeBitNO = 2 ;// Indicates how much each bee searches in the range of the reference point, the bit to be changed
	//final int minUtility = 625431; // the minimum utility threshold
	int times=5;// Limit the number of high-utility judgments to prevent an infinite loop
	final int prunetimes=50;// Limit the number of pruning decisions to prevent an infinite loop
	final int estiTransCount=10000;
	int m=0;
	int bucketNum= 120;//change  the parameter based on the number of items in database
	
	//int no=0;//nectar source incremental number of experiments

	int[] ScoutBeesBucket=new int[bucketNum];//Utility information for storing the number of initialization bits of the scout bee
	double[] RScoutBeesiniBit=new double[bucketNum];//Roulette information for storing the number of initialization bits of the scout bee
	
	int iniBitNO=0;//The number of 1 that the scout bee be initialized
	
	Map<Integer, Integer> mapItemToTWU;//create a map to store the TWU of each item
	Map<Integer, Integer> mapItemToTWU0;//Used to remove items whose TWU is less than minUtil
	
	List<Integer> twuPattern;//the items which has twu value more than minUtil

	BufferedWriter writer = null; // writer to write the output file
	
	// this class represent an item and its utility in a transaction
	class Pair {
		int item = 0;
		int utility = 0;
		int rutil = 0;
	}
	
	/**
	 *
	 * this class represent the particles
	 * @author Wei Song,Chaomin Huang
	 *
	 */
	class BeeGroup {
		List<Integer> X;//nectar source location
		int fitness;//the fitness(nectar)
		int rutil;//The utility of the X suffix extension
		int trail;//It represents the number of experiments
		double rfitness;//Relative fitness values, may use different roulettes in the calculation

		public void addtrail(int k){
			trail = trail + k;
		}
		
		public BeeGroup() {
			X = new ArrayList<Integer>();
		}
		
		public BeeGroup(int length) {
			X = new ArrayList<Integer>();
			for (int i = 0; i < length; i++) {
				X.add(0);			
			}
		}
	}
	
	/**
	 *
	 * store HUI
	 *
	 */
	class HUI {
		String itemset;
		int fitness;

		public HUI(String itemset, int fitness) {
			super();
			this.itemset = itemset;
			this.fitness = fitness;
		}

	}
	
	//this class used to record the set of transactions that contain the item
	class Item{
		int item;
		BitSet TIDS;
		public Item(){
			TIDS=new BitSet(estiTransCount);
		}
		public Item(int item){
			TIDS=new BitSet(estiTransCount);
			this.item=item;
		}
	}
	
	List<Item> Items;//The number of elements contained = twnPattern.size()
	List<BeeGroup> NectarSource = new ArrayList<BeeGroup>();//Nectar Source,Note that all modifications are for nectar
	List<BeeGroup> EmployedBee = new ArrayList<BeeGroup>();;//employed bees
	List<BeeGroup> OnLooker = new ArrayList<BeeGroup>();//onlooker bees
	
	long sumTwu=0;// total TU values of the database
	
	List<HUI> huiSets = new ArrayList<HUI>();//Collection of HUIS
	
	Set<List<Integer>>  huiBeeGroup = new HashSet<List<Integer>>();//Create a set to save the hui-itemset that has been calculated
												//Prevent duplicate calculations						
	// reorganized database
	List<List<Pair>> database = new ArrayList<List<Pair>>();
	
	List<List<Integer>> databaseTran = new ArrayList<List<Integer>>();//Data set without utility
	
	List<Double> percentage = new ArrayList<Double>();// Roulette wheel for roulette
	//Pruning node(stores suffix extensions) for test
	//prunelistFPTree pruneNodelist = new prunelistFPTree();	
	
	/**
	 * Default constructor
	 */
	
	public AlgoHUIM_ABC() {
	}

	/**
	 * Run the algorithm
	 * @param input
	 * 				the input file path
	 * @param output 
	 * 				the output file path
	 * @param minUtility
	 * 				the minimum utility threshold
	 * @throws IOException
	 * 				exception if error while writing the file
	 */
	public void runAlgorithm(String input, String output, int minUtility) throws IOException {
		// reset maximum
		maxMemory = 0;
		startTimestamp = System.currentTimeMillis();
		writer = new BufferedWriter(new FileWriter(output));

		// We create a map to store the TWU of each item
		mapItemToTWU = new HashMap<Integer, Integer>();
		mapItemToTWU0 = new HashMap<Integer, Integer>();
		
		//We scan the database first time to calculate the TWU of each item
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
				// the second part is the transaction utility
				int transactionUtility = Integer.parseInt(split[1]);
				sumTwu =sumTwu+transactionUtility;
				// for each item, we add the transaction utility to its TWU
				for (int i = 0; i < items.length; i++) {
					// convert item to integer
					Integer item = Integer.parseInt(items[i]);
					// get the current TWU of that item
					Integer twu = mapItemToTWU.get(item);
					Integer twu0 = mapItemToTWU0.get(item);
					// add the utility of the item in the current transaction to its twu
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

		//SECOND DATABASE PASS TO CONSTRUCT THE DATABASE
		//OF 1-ITEMSETS HAVING TWU >= minutil (promising items)
		try {
			// prepare object for reading the file
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

				// split the line according to the separator
				String split[] = thisLine.split(":");
				// get the list of items
				String items[] = split[0].split(" ");
				// get the list of utility values corresponding to each item
				// for that transaction
				String utilityValues[] = split[2].split(" ");

				//Create a list to store items and its utility
				List<Pair> revisedTransaction = new ArrayList<Pair>();
				
				List<Integer> pattern=new ArrayList<Integer>();//Create a list to store items
				
				int remainingUtility = 0;

				// for each item
				for (int i = 0; i < items.length; i++) {
					Pair pair = new Pair();
					pair.item = Integer.parseInt(items[i]);
					pair.utility = Integer.parseInt(utilityValues[i]);
					if (mapItemToTWU.get(pair.item) >= minUtility) {
						revisedTransaction.add(pair);
						pattern.add(pair.item);						
						remainingUtility +=pair.utility;	
					}else{
						mapItemToTWU0.remove(pair.item);
					}
				}
				
				//Assign a value to the pair.rutil in the revisedTransaction
				for(Pair pair : revisedTransaction){
					remainingUtility = remainingUtility - pair.utility;
					pair.rutil = remainingUtility;
				}
				
				//revisedtransaction add to reorganized database
				database.add(revisedTransaction);
				databaseTran.add(pattern);
				// variable to count the number of transaction
				++transactionCount;
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
		Collections.sort(twuPattern);//Ascending by dictionary
		
		m=(int)(twuPattern.size()/bucketNum);

		Items=new ArrayList<Item>();
		
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
		//Put the initial 5 points in each bucket, the scout bee will be used
		for(int i=0;i<ScoutBeesBucket.length;++i){
			ScoutBeesBucket[i]=1;
		}
			
		checkMemory();
		
		//m=twuPattern.size()/10;
		
		if(twuPattern.size()>0){
			Initialization(minUtility);
			for(int gen =0; gen < iterations; ++gen){
				iniBitNO=32+1;
				
				Employed_bees(minUtility);
				
				calculateRfitness();
				
				OnLooker_bees(minUtility);
				//System.out.println("bbbbbbbb");

				calScoutBees();
				
				Scout_bees(iniBitNO,minUtility);
				//System.out.println("bbbbbbbb");
//				if(gen%200==0) {
//					System.out.printf("��%d�ε�����HUIs��С��%d" ,gen, huiSets.size());
//					System.out.println();
//				}
				
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
/*
	public int getMaxFitness(){
		int temp=0;
		for(int i=0;i<NectarSource.size();++i){
			if(temp<NectarSource.get(i).fitness){
				temp=NectarSource.get(i).fitness;
			}
		}
		//System.out.println(x);
		return temp;
	}
	
*/
	/**
	 * Is it a promising BeeGroup
	 * @param tempBeeGroup
	 * @return
	 */
		public boolean isRBeeGroup(BeeGroup tempBeeGroup,List<Integer> list){
			List<Integer> templist=new ArrayList<Integer>();//Storage location is not equal to 0

			for(int i=0;i<tempBeeGroup.X.size();++i){
				if(tempBeeGroup.X.get(i)!=0){
					templist.add(i);
				}	
			}
			if(templist.size()==0){
				return false;
			}
			BitSet tempBitSet=new BitSet(estiTransCount);
			tempBitSet=(BitSet)Items.get(templist.get(0).intValue()).TIDS.clone();
			
			for(int i=1;i<templist.size();++i){
				if(tempBitSet.cardinality()==0){
					break;
				}
				tempBitSet.and(Items.get(templist.get(i).intValue()).TIDS);
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
	 * Initial population, nectar source and other information
	 * @param minUtility
	 */
	private void Initialization(int minUtility){
		int i = 0,k=0;
		//Initialize the percentage based on the TWU value of 1-HTWUIs
		percentage = roulettePercent();

		while (i < pop_size) {
			//Initialization nectar source
			BeeGroup tempNode;
			BeeGroup besttempNode=new BeeGroup(twuPattern.size());
			List<Integer> templist;

			//Produce an itemset that meets the requirements, ie High utility itemset
			int j=0;
			do{
				do{
					templist=new ArrayList<Integer>();
					//The number of 1 in BeeGroup is k
					do{
						k = (int) (Math.random() * twuPattern.size());
					}while(k==0);
					//all nectar sources are initialized
					tempNode = new BeeGroup(twuPattern.size());
					
					iniBeeGroup(tempNode,k);
				}while(!isRBeeGroup(tempNode,templist)||huiBeeGroup.contains(tempNode.X));
				
				fitCalculate(tempNode, k,templist);
				
				/*if(tempNode.rutil <minUtility ){
					pruneNodelist.addprunelist(tempNode.X);
				}*/
				
				if(tempNode.fitness>=besttempNode.fitness){
					copyBeeGroup(besttempNode,tempNode);
				}
				++j;//j is used to identify the number of experiments in order to generate an itemset that meets the requirements
			}while(besttempNode.fitness<minUtility&&j<times);

			besttempNode.trail = 0;//The number of trials for each nectar sources is initialized to 0
			
			OnLooker.add(new BeeGroup(twuPattern.size()));//initialization OnLooker bees
			EmployedBee.add(new BeeGroup(twuPattern.size()));//initialization Employed Bees
			
			NectarSource.add(besttempNode);
			
			if (besttempNode.fitness >= minUtility) {
				if(!huiBeeGroup.contains(besttempNode.X)){
					updateScoutBeesBucket(Collections.frequency(besttempNode.X, 1));
				}
				addlist(huiBeeGroup,besttempNode.X);
				insert(besttempNode);
			}
			i++;
		}
		
		//employed bees mapped to the nectar source
		copylistBeeGroup(EmployedBee, NectarSource);
	}
	
	
	
	
	/**
	 * employed bees mapped to the nectar source
	 * @param list1BeeGroup
	 * @param list2BeeGroup
	 */
	public void copylistBeeGroup(List<BeeGroup> list1BeeGroup,List<BeeGroup> list2BeeGroup){
		for(int i=0;i<list1BeeGroup.size();++i){
			copyBeeGroup(list1BeeGroup.get(i),list2BeeGroup.get(i));
		}
	}

	public void copyBeeGroup(BeeGroup beeG1,BeeGroup beeG2){
		copyList(beeG1.X,beeG2.X);
		beeG1.fitness =beeG2.fitness;
		beeG1.rfitness = beeG2.rfitness;
		beeG1.rutil =beeG2.rutil;
		beeG1.trail =beeG2.trail;
	}
	
	public void  copyList(List<Integer> list1,List<Integer> list2){
		for(int i=1;i<list1.size();++i){
			list1.set(i, list2.get(i).intValue());
		}
	}
	
	
	/**
	 * Re-copy the list and press it into the huiBeeGroup
	 * @param huiBeeGroup
	 * @param list
	 */
	public void addlist(Set<List<Integer>> huiBeeGroup,List<Integer> list){
		List<Integer> templist= new ArrayList<Integer>();
		for(int i=0;i<list.size();++i){
			templist.add(list.get(i).intValue());
		}
		huiBeeGroup.add(templist);
	}
	/**
	 * Simulation of the process of employed bees for exploration
	 */
	public void Employed_bees(int minUtility){
		int i = 0;
		//employed bee is mapped to the nectar source
		copylistBeeGroup(EmployedBee, NectarSource);
		BeeGroup temp;
		
		for(i = 0; i < pop_size; ++i){
			//PBV
			temp=meetReqBeeGroup(EmployedBee.get(i),minUtility,"sendEmployedBees");
			EmployedBee.set(i, temp);
			//Greedy selection strategy
			if(EmployedBee.get(i).fitness > NectarSource.get(i).fitness){
				//Replace the existing Nectar Source with a better source of nectar detected by the employed bee
				copyBeeGroup(NectarSource.get(i),EmployedBee.get(i));
			}else{
				NectarSource.get(i).addtrail(1);// increment
			}
		}
		
	}
	
	/**
	 * employed bees and onlooker bees exchange information, onlooker bees change information
	 */
	public void OnLooker_bees(int minUtility){
		for(int i=0;i<pop_size;++i){
			BeeGroup tempBeeGroup;
			int temp=selectNectarSource();
			//onlooker bee choose a nectar source
			copyBeeGroup(OnLooker.get(i),NectarSource.get(temp));
	
			//onlooker bee to explore a qualified nectar source

			tempBeeGroup=meetReqBeeGroup(OnLooker.get(i),minUtility,"sendOnLookerBees");
			
			OnLooker.set(i, tempBeeGroup);
			
			
			//Greedy selection strategy
			if(OnLooker.get(i).fitness > NectarSource.get(temp).fitness){
				
				copyBeeGroup(NectarSource.get(temp),OnLooker.get(i));
				
			}else{
				NectarSource.get(temp).addtrail(1);//increment
			}
			
		}
	}
	
	
	
	/**
	 * Determine whether the scout bees appear, there is generate nectar source
	 */
	public void Scout_bees(int iniBitNO,int minUtility){
		
		for(int i=0;i<pop_size;++i){
			if(NectarSource.get(i).trail>limit){
				//Initialization nectar source
				BeeGroup tempNode;
				BeeGroup besttempNode=new BeeGroup(twuPattern.size());
		
				//The number of 1 in BeeGroup is k
				int k =0;
				
				List<Integer> templist;
				//Produce an itemset that meets the requirements, ie High utility itemset
				int j=0;
				int times=5;
				do{
					do{
						templist=new ArrayList<Integer>();
						do{
							k=selectScoutIniBit()*m+(int)(Math.random()*m);
							//System.out.println("bbbbbbbb");

						}while(k==0);
						
						tempNode = new BeeGroup(twuPattern.size());
						iniBeeGroup(tempNode,k);
					}while(!isRBeeGroup(tempNode,templist)||huiBeeGroup.contains(tempNode.X));
					
					fitCalculate(tempNode, k,templist);
					/*if(tempNode.rutil <minUtility ){
						pruneNodelist.addprunelist(tempNode.X);
					}*/
					if(tempNode.fitness>=besttempNode.fitness){
						copyBeeGroup(besttempNode,tempNode);
					}
					++j;
					//System.out.println("dddddddddddd");
				}while(besttempNode.fitness<minUtility&&j<times);
				
				
				besttempNode.trail = 0;
				
				NectarSource.set(i,besttempNode);//Replace the original nectar source
				
				if (besttempNode.fitness >= minUtility) {
					if(!huiBeeGroup.contains(besttempNode.X)){
						updateScoutBeesBucket(Collections.frequency(besttempNode.X, 1));
					}
					addlist(huiBeeGroup,besttempNode.X);
					insert(besttempNode);
				}
				
			}
		}
		
		
	}

	private int selectScoutIniBit(){
		int i, temp = 0;
		double randNum;
		randNum = Math.random();
		for (i = 0; i < RScoutBeesiniBit.length; i++) {
			if (i == 0) {
				if ((randNum >= 0) && (randNum <= RScoutBeesiniBit[0])) {
					temp = 0;
					break;
				}
			} else if ((randNum > RScoutBeesiniBit[i-1])
					&& (randNum <= RScoutBeesiniBit[i])) {
				temp = i;
				break;
			}
		}
		return temp;
	}
	
	public void updateScoutBeesBucket(int k){
		int temp=k/bucketNum;
		if(k>=50){
			ScoutBeesBucket[bucketNum-1]+=1;
			return;
		}
		ScoutBeesBucket[temp]+=1;
	}
/**
 * Calculate the roulette that the scout bee decides to initialize the number of bits
 */
	public void calScoutBees(){
		int sum=0;
		int tempSum=0;
		//calculate fitness
		for(int i=0; i< ScoutBeesBucket.length;++i){
			sum =sum+ScoutBeesBucket[i];
		}
		
		for(int i=0; i< ScoutBeesBucket.length;++i){
			tempSum =tempSum+ScoutBeesBucket[i];
			RScoutBeesiniBit[i]= tempSum/(sum+0.0);
		}
	}
	
	/**
	 * onlooker bees select nectar source
	 */
	private int selectNectarSource() {
		int i, temp = 0;
		double randNum;
		randNum = Math.random();
		for (i = 0; i < NectarSource.size(); i++) {
			if (i == 0) {
				if ((randNum >= 0) && (randNum <= NectarSource.get(0).rfitness)) {
					temp = 0;
					break;
				}
			} else if ((randNum > NectarSource.get(i - 1).rfitness)
					&& (randNum <= NectarSource.get(i).rfitness)) {
				temp = i;
				break;
			}
		}
		return temp;
	}
	/**
	 * Calculate the rfitness of nectar source information
	 */
	public void calculateRfitness(){
		int sum=0;
		int temp=0;
		//Total fitness
		for(int i=0; i< NectarSource.size();++i){
			sum =sum+NectarSource.get(i).fitness;
		}
		//calculate roulette wheel
		for(int i=0; i< NectarSource.size();++i){
			temp =temp+NectarSource.get(i).fitness;
			NectarSource.get(i).rfitness= temp/(sum+0.0);
		}
	}
	

	/**
	 * Change the bit of the specified number
	 * and record the value of the fitness of tempGroup before using this function
	 * @param tempGroup
	 */
	public void changeKBit(BeeGroup tempGroup){
		List<Integer> templist=new ArrayList<Integer>();
		
		for(int i = 0; i <changeBitNO;++i){
			int k=0;
			do{
				k = (int) (Math.random() * twuPattern.size());
				
			}while(templist.contains(k));
			
			templist.add(k);
			
			if(tempGroup.X.get(k)==1){
				tempGroup.X.set(k,0);
			}else{
				tempGroup.X.set(k,1);
			}
		}

	}
	
	
	/**
	 * Generate a BeeGroup that meets the requirements and change the specified bit number
	 * and update huiBeeGroup,huiSets,pruneNodelist
	 * @param tempGroup
	 * @param minUtility
	 */
	public BeeGroup meetReqBeeGroup(BeeGroup tempGroup,int minUtility,String flag){
		int j=0;
		int k=0;
		//no=0;
		//int temp =(int)((Math.abs(tempGroup.fitness-minUtility)/minUtility)*10);
		changeBitNO=1;
		times=5;
		
		List<Integer> templist;
		
		BeeGroup besttempNode=new BeeGroup(twuPattern.size());
		
		copyBeeGroup(besttempNode,tempGroup);//besttempNode Preserve the original information nectar
		do{
			
			do{
				templist=new ArrayList<Integer>();
				changeKBit(tempGroup);
				
			}while(!isRBeeGroup(tempGroup,templist)||huiBeeGroup.contains(tempGroup.X));

			k=Collections.frequency(tempGroup.X, 1);
			
			fitCalculate(tempGroup,k ,templist);
			//add to pruneNodelist
			/*if(tempGroup.rutil <minUtility ){
				pruneNodelist.addprunelist(tempGroup.X);
			}*/
			
			if(tempGroup.fitness>besttempNode.fitness){
				copyBeeGroup(besttempNode,tempGroup);
			}else{
				copyBeeGroup(tempGroup,besttempNode);
			}
			++j;
			//System.out.println(j);
		}while(besttempNode.fitness<minUtility && j<times);
		
		if (besttempNode.fitness >= minUtility) {
			if(!huiBeeGroup.contains(besttempNode.X)){
				updateScoutBeesBucket(Collections.frequency(besttempNode.X, 1));
			}
			addlist(huiBeeGroup,besttempNode.X);
			insert(besttempNode);
		}
		//System.out.println(flag);
		return besttempNode;	
	}
	
	
	/**
	 * Eliminate the 0 at the end of the list,
	 * the returned list is independent of the parameters passed in
	 * 
	 * @param list
	 */
	public List<Integer> delete0(List<Integer> list){
		int i=0;
		int temp=0;
		if(list.size()>0&&Collections.frequency(list, 1)>0){
			i=list.size()-1;
	
			while(i>=0&&list.get(i)==0){
				--i;
			}
		
			List<Integer> templist= new ArrayList<Integer>(); 
	
			int j=0;
			while(j<=i){
				temp=list.get(j).intValue();
				templist.add(temp);
				++j;
			}
			return templist;
		}else{
			return null;
		}
	}
	
	
	/**
	 * initialization BeeGroup.X
	 * @param tempNode
	 * @param k
	 */
	public void iniBeeGroup(BeeGroup tempNode,int k){
		int j = 0;
		int temp;

		while (j < k) {
			//Determine the position of 1 in BeeGroup by roulette
			temp = select(percentage);
			if (tempNode.X.get(temp) == 0){
				++j;
				tempNode.X.set(temp, 1);
			}
			
		}
		
	}
		
	/**
	 * initialization percentage
	 * @return percentage
	 */
	private List<Double> roulettePercent() {
		int i, sum = 0, tempSum = 0;
		double tempPercent;

		//calculate the sum of 1-HTWUIs'TWU
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
	 * Use roulette to select the position of 1
	 * 
	 * @param percentage
	 *            the portation of twu value of each 1-HTWUIs in sum of twu
	 *            value
	 * @return the position of 1
	 */
	private int select(List<Double> percentage) {
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
	 * Calculate the fitness value and suffix utility of BeeGroup
	 * @param tempChroNode
	 * @param k
	 * @return
	 */
	private void fitCalculate(BeeGroup tempGroup, int k,List<Integer> templist) {
		if (k == 0)
			return;
		int i, j, p, q, temp,m;
		int sum, fitness = 0;
		int rutil = 0;
		for (m = 0; m < templist.size(); m++) {
			p=templist.get(m).intValue();									
			i = 0;
			j = 0;
			q = 0;
			temp = 0;
			sum = 0;
			
			while (j < k && q < database.get(p).size()
					&& i < tempGroup.X.size()) {
				if (tempGroup.X.get(i) == 1) {
					if (database.get(p).get(q).item < twuPattern.get(i))
						q++;
					else if (database.get(p).get(q).item == twuPattern.get(i)) {
						sum = sum + database.get(p).get(q).utility;
						j++;
						q++;
						temp++;
						i++;
					} else if (database.get(p).get(q).item > twuPattern.get(i)) {
						break;
					}
				} else{
					i++;
				}
			}
			if (temp == k) {
				rutil = rutil + database.get(p).get(q-1).rutil;
				fitness = fitness + sum;
			}
		}
		tempGroup.rutil = rutil + fitness;
		tempGroup.fitness = fitness;
		
	}
	
	/**
	 * Method to inseret tempChroNode to huiSets
	 * 
	 * @param tempChroNode
	 *            the chromosome to be inserted
	 */
	private void insert(BeeGroup tempBeeGroup) {
		int i;
		StringBuilder temp = new StringBuilder();
		for (i = 0; i < twuPattern.size(); i++) {
			if (tempBeeGroup.X.get(i) == 1) {
				temp.append(twuPattern.get(i));
				temp.append(' ');
			}
		}
		// huiSets is null
		if (huiSets.size() == 0) {
			huiSets.add(new HUI(temp.toString(), tempBeeGroup.fitness));
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
			if (i == huiSets.size())
				huiSets.add(new HUI(temp.toString(), tempBeeGroup.fitness));
		}
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
			buffer.append(System.lineSeparator());
		}
		// write to file
		writer.write(buffer.toString());
		writer.newLine();
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
	 * Get the bucket number (should be adjusted depending on number of items in the database)
	 * @return the bucket count
	 */
	public int getBucketNum() {
		return bucketNum;
	}
	
	/**
	 * Set the bucket number (should be adjusted depending on number of items in the database)
	 * @param bucketNum the bucket count
	 */
	public void setBucketNum(int bucketNum) {
		this.bucketNum = bucketNum;

		ScoutBeesBucket = new int[bucketNum];// Utility information for storing
												// the number of initialization
												// bits of the scout bee
		RScoutBeesiniBit = new double[bucketNum];// Roulette information for
													// storing the number of
													// initialization bits of
													// the scout bee

	}
	
	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats() {
		System.out
				.println("=============  HUIM-ABC ALGORITHM v.2.40 - STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp)
				+ " ms");
		System.out.println(" Memory ~ " + maxMemory + " MB");
		System.out.println(" High-utility itemsets count : " + huiSets.size());
		System.out
				.println("===================================================");
	}
	

}
