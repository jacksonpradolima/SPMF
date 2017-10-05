package ca.pfv.spmf.algorithms.frequentpatterns.HUIM_BPSO;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * * * * This is an implementation of the high utility itemset mining algorithm
 * based on Binary Particle Swarm Optimization Algorithm.
 * 
 * Copyright (c) 2016 Jerry Chun-Wei Lin, Lu Yang, Philippe Fournier-Viger
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE *
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
 * 
 * @author Jerry Chun-Wei Lin, Lu Yang, Philippe Fournier-Viger
 */

public class AlgoHUIM_BPSO {
	// variable for statistics
	double maxMemory = 0; // the maximum memory usage
	long startTimestamp = 0; // the time the algorithm started
	long endTimestamp = 0; // the time the algorithm terminated
	final int pop_size = 5;// the size of populations
	final int iterations = 10;// the iterations of algorithms
	final int c1 = 2, c2 = 2;// the parameter used in BPSO algorithm
	final double w = 0.9;// the parameter used in BPSO algorithm

	Map<Integer, Integer> mapItemToTWU;
	List<Integer> twuPattern;// the items which has twu value more than minUtil

	BufferedWriter writer = null; // writer to write the output file

	// this class represent an item and its utility in a transaction
	class Pair {
		int item = 0;
		int utility = 0;
	}

	// this class represent the particles
	class Particle {
		List<Integer> X;// the particle
		int fitness;// fitness value of particle

		public Particle() {
			X = new ArrayList<Integer>();
		}

		public Particle(int length) {
			X = new ArrayList<Integer>();
			for (int i = 0; i < length; i++) {
				X.add(i, 0);
			}
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

	Particle gBest = new Particle();// the gBest particle in populations
	List<Particle> pBest = new ArrayList<Particle>();// each pBest particle in
														// populations,
	List<Particle> population = new ArrayList<Particle>();// populations
	List<HUI> huiSets = new ArrayList<HUI>();// the set of HUIs
	List<List<Double>> V = new ArrayList<List<Double>>();// the velocity of each
															// particle
	List<Double> percentage = new ArrayList<Double>();// the portation of twu
														// value of each
														// 1-HTWUIs in sum of
														// twu value

	// Create a list to store database
	List<List<Pair>> database = new ArrayList<List<Pair>>();

	/**
	 * Default constructor
	 */
	public AlgoHUIM_BPSO() {
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
					// add the utility of the item in the current transaction to
					// its twu
					twu = (twu == null) ? transactionUtility : twu
							+ transactionUtility;
					mapItemToTWU.put(item, twu);
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

		twuPattern = new ArrayList<Integer>(mapItemToTWU.keySet());
		Collections.sort(twuPattern);

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
		// check the memory usage
		checkMemory();
		// Mine the database recursively
		if (twuPattern.size() > 0) {
			// initial population
			generatePop(minUtility);

			for (int i = 0; i < iterations; i++) {
				// update population and HUIset
				update(minUtility);
//				System.out.println(i + "-update end. HUIs No. is "
//						+ huiSets.size());
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
	private void generatePop(int minUtility)//
	{
		int i, j, k, temp;
		// initial percentage according to the twu value of 1-HTWUIs
		percentage = roulettePercent();

		for (i = 0; i < pop_size; i++) {
			// initial particles
			Particle tempParticle = new Particle(twuPattern.size());
			j = 0;
			// k is the count of 1 in particle
			k = (int) (Math.random() * twuPattern.size());

			while (j < k) {
				// roulette select the position of 1 in population
				temp = rouletteSelect(percentage);
				if (tempParticle.X.get(temp) == 0) {
					j++;
					tempParticle.X.set(temp, 1);
				}

			}
			// calculate the fitness of each particle
			tempParticle.fitness = fitCalculate(tempParticle.X, k);
			// insert particle into population
			population.add(i, tempParticle);
			// initial pBest
			pBest.add(i, population.get(i));
			// update huiSets
			if (population.get(i).fitness >= minUtility) {
				insert(population.get(i));
			}
			// update gBest
			if (i == 0) {
				gBest = pBest.get(i);
			} else {
				if (pBest.get(i).fitness > gBest.fitness) {
					gBest = pBest.get(i);
				}
			}
			// update velocity
			List<Double> tempV = new ArrayList<Double>();
			for (j = 0; j < twuPattern.size(); j++) {
				tempV.add(j, Math.random());
			}
			V.add(i, tempV);
		}
	}

	/**
	 * Methos to update particle, velocity, pBest and gBest
	 * 
	 * @param minUtility
	 */
	private void update(int minUtility) {
		int i, j, k;
		double r1, r2, temp1, temp2;

		for (i = 0; i < pop_size; i++) {
			k = 0;// record the count of 1 in particle
			r1 = Math.random();
			r2 = Math.random();
			// update velocity
			for (j = 0; j < twuPattern.size(); j++) {
				double temp = V.get(i).get(j) + r1
						* (pBest.get(i).X.get(j) - population.get(i).X.get(j))
						+ r2 * (gBest.X.get(j) - population.get(i).X.get(j));
				V.get(i).set(j, temp);
				if (V.get(i).get(j) < -2.0)
					V.get(i).set(j, -2.0);
				else if (V.get(i).get(j) > 2.0)
					V.get(i).set(j, 2.0);
			}
			// update particle
			for (j = 0; j < twuPattern.size(); j++) {
				temp1 = Math.random();
				temp2 = 1 / (1.0 + Math.exp(-V.get(i).get(j)));
				if (temp1 < temp2) {
					population.get(i).X.set(j, 1);
					k++;
				} else {
					population.get(i).X.set(j, 0);
				}
			}
			// calculate fitness
			population.get(i).fitness = fitCalculate(population.get(i).X, k);
			// update pBest & gBest
			if (population.get(i).fitness > pBest.get(i).fitness) {
				pBest.set(i, population.get(i));
				if (pBest.get(i).fitness > gBest.fitness) {
					gBest = pBest.get(i);
				}
			}
			// update huiSets
			if (population.get(i).fitness >= minUtility) {
				insert(population.get(i));
			}
		}
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
			if (tempParticle.X.get(i) == 1) {
				temp.append(twuPattern.get(i));
				temp.append(' ');
			}
		}
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
		int i, sum = 0, tempSum = 0;
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
	 * Method to calculate the fitness of each particle
	 * 
	 * @param tempParticle
	 * @param k
	 *            the number of 1 in particle
	 * @return fitness
	 */
	private int fitCalculate(List<Integer> tempParticle, int k) {
		if (k == 0)
			return 0;
		int i, j, p, q, temp;

		int sum, fitness = 0;
		for (p = 0; p < database.size(); p++) {// p scan the transactions in
												// database
			i = 0;
			j = 0;
			q = 0;
			temp = 0;
			sum = 0;
			// j scan the 1 in particle, q scan each transaction, i scan each
			// particle
			while (j < k && q < database.get(p).size()
					&& i < tempParticle.size()) {
				if (tempParticle.get(i) == 1) {
					if (database.get(p).get(q).item < twuPattern.get(i))
						q++;
					else if (database.get(p).get(q).item == twuPattern.get(i)) {
						sum = sum + database.get(p).get(q).utility;
						j++;
						q++;
						temp++;
						i++;
					} else if (database.get(p).get(q).item > twuPattern.get(i)) {
						j++;
						i++;
					}
				} else
					i++;
			}
			if (temp == k) {
				fitness = fitness + sum;
			}
		}
		return fitness;
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
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats() {
		System.out
				.println("=============  HUIM-BPSO ALGORITHM v.2.11 - STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp)
				+ " ms");
		System.out.println(" Memory ~ " + maxMemory + " MB");
		System.out.println(" High-utility itemsets count : " + huiSets.size());
		System.out
				.println("===================================================");
	}
}
