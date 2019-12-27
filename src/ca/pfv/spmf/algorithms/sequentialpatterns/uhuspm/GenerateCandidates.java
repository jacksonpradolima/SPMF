package ca.pfv.spmf.algorithms.sequentialpatterns.uhuspm;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
*
* SPMF is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* SPMF is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with SPMF.  If not, see <http://www.gnu.org/licenses/>.
* 
* @Copyright Ting Li et al., 2018
*/
/**
 * This is an implementation of candidate generation, as used by the UHUSPM algorithm.
 * 
 * @see AlgoUHUSPM
 * @author Ting Li
 */
public class GenerateCandidates {
	
	/**
	 * private constructor
	 */
	private GenerateCandidates() { 
	}
	
	/** Set of...	 */
	static Set<List<List<Integer>>> PSs = new HashSet<List<List<Integer>>>();
	
	/** Map of...	 */
	static Map<List<List<Integer>>, Integer> USs = new HashMap<List<List<Integer>>, Integer>();
	
	/**
	 * Generate candidate
	 * @param generation list of patterns
	 * @param minUtility minimum utility threshold
	 * @param minProbability minimum probability threshold
	 * @param writer writer to write patterns to the output file
	 * @return a list of new patterns
	 * @throws IOException if error while writing to file
	 */
	static List<SequenceList> generate2GenerationCandidates(
			List<SequenceList> generation, int minUtility,
			float minProbability, BufferedWriter writer) throws IOException {
		List<SequenceList> candidates = new ArrayList<SequenceList>();
		/*output high utility sequential pattern*/
		
		for(SequenceList list: generation){
			if(list.sumProbability >= minProbability){
				PSs.add(list.itemsets);
			}

			if(list.sumUtility >= minUtility && list.sumProbability >= minProbability){
				AlgoUHUSPM.numberOfHUSP++;
				writeOut(list, writer);
			}
		}
		
		/*generate candidates*/
		for(int i = 0; i < generation.size(); i++){
			SequenceList pattern1 = generation.get(i);
			
			for(int j=i; j < generation.size();j++){
				
				SequenceList pattern2 = generation.get(j);
				
				//itemset-based
				SequenceList combiner = new SequenceList();
				combiner.itemsets.addAll(pattern1.itemsets);
				
				int item= pattern2.itemsets.get(0).get(0);
				combiner.addItemset(pattern2.itemsets.get(pattern2.itemsets.size()-1));
				combiner.itemsetBasedExtend(pattern1, item, AlgoUHUSPM.sequenceDatabase);
				
				AlgoUHUSPM.numberOfCandidates++;

				combiner.calculate();
				/**********************/
				USs.put(combiner.itemsets, combiner.sumSWU);
				if(combiner.sumProbability >= minProbability){
					PSs.add(combiner.itemsets);
				}
				/**********************/
				if(combiner.sumSWU >= minUtility &&combiner.sumProbability >= minProbability)
					candidates.add(combiner);
				//item-based
				if(pattern1 != pattern2){
						//itemset-based extension
						SequenceList combiner2 = new SequenceList();
						List<Integer> itemset = new ArrayList<Integer>();
						itemset.addAll(pattern1.itemsets.get(0));
						itemset.add(item);
						
						Collections.sort(itemset,new Comparator<Integer>(){
				            public int compare(Integer mc1,Integer mc2){
				            	return mc1-mc2;
				            }
						});
						
						combiner2.itemsets.add(itemset);
						
						combiner2.itemBasedExtend(pattern1, item, AlgoUHUSPM.sequenceDatabase);
						
						AlgoUHUSPM.numberOfCandidates++;
						combiner2.calculate();
						/**************/
						USs.put(combiner2.itemsets, combiner2.sumSWU);
						if(combiner2.sumProbability >= minProbability){
							PSs.add(combiner2.itemsets);
						}
						/**************/
						if(combiner2.sumSWU >= minUtility && combiner2.sumProbability >= minProbability)
							candidates.add(combiner2);
					}
				}
		}
		
		/*generate candidates*/
		for(int i = generation.size()-1; i>=0; i--){
			SequenceList pattern1 = generation.get(i);
			
			for(int j=i-1; j >=0; j--){
				
				SequenceList pattern2 = generation.get(j);

				SequenceList combiner = new SequenceList();
				
				int item= pattern2.itemsets.get(0).get(0);
				/*generate itemsets*/
				combiner.itemsets.addAll(pattern1.itemsets);
				combiner.addItemset(pattern2.itemsets.get(pattern2.itemsets.size()-1));
				
				combiner.itemsetBasedExtend(pattern1, item, AlgoUHUSPM.sequenceDatabase);
				
				AlgoUHUSPM.numberOfCandidates++;
				/*calculate utility, probability*/
				combiner.calculate();
				/**********************/
				USs.put(combiner.itemsets, combiner.sumSWU);
				if(combiner.sumProbability >= minProbability){
					PSs.add(combiner.itemsets);
				}
				/**********************/
				if(combiner.sumSWU >= minUtility &&combiner.sumProbability >= minProbability)
					candidates.add(combiner);
			}
		}

		return candidates;
	}
	
	/**
	 * Generate candidate patterns
	 * @param generation a list of candidate patterns
	 * @param minUtility a minimum utility threshold
	 * @param minProbability a minimum probability threshold
	 * @param writer a buffered writer to write patterns to the output file
	 * @return a list of new patterns
	 * @throws IOException if error while writing patterns to the output file
	 */
	static  List<SequenceList> generateNextGenerationCandidates(List<SequenceList> generation, int minUtility, float minProbability, BufferedWriter writer) throws IOException{	
		
		List<SequenceList> candidates = new ArrayList<SequenceList>();
		/*output high utility sequential pattern*/
		for(SequenceList list: generation){
			
			if(list.sumUtility >= minUtility && list.sumProbability >= minProbability){
				AlgoUHUSPM.numberOfHUSP++;
				writeOut(list, writer);
			}
		}
		
		/*generate candidates*/
		for(int i = 0; i < generation.size(); i++){
			for(int j=i; j < generation.size();j++){
				SequenceList candidate = combine(generation.get(i), generation.get(j), minUtility);
				
				if(candidate != null){
					AlgoUHUSPM.numberOfCandidates++;
					/*calculate utility, probability*/
					candidate.calculate();
					/**********************/
					if(candidate.sumProbability >= minProbability){
						PSs.add(candidate.itemsets);
					}
					/**********************/
					/*put candidate into next generation*/
					if(candidate.sumSWU >= minUtility && candidate.sumProbability >= minProbability)
						candidates.add(candidate);
					
				}
			}
		}
		
		for(int i = generation.size()-1; i >=0 ; i--){
			for(int j=i-1; j >=0; j--){
				SequenceList candidate = combine(generation.get(i), generation.get(j), minUtility);
				
				if(candidate != null){
					
					AlgoUHUSPM.numberOfCandidates++;
					/*calculate utility, probability*/
					candidate.calculate();
					/**********************/
					if(candidate.sumProbability >= minProbability){
						PSs.add(candidate.itemsets);
					}
					/**********************/
					/*put candidate into next generation*/
					if(candidate.sumSWU >= minUtility && candidate.sumProbability >= minProbability)
						candidates.add(candidate);
				}
			}
		}

		return candidates;
	}

	/**
	 * Combine two patterns to generate another pattern
	 * @param pattern1 a pattern
	 * @param pattern2 another pattern
	 * @param minUtility the minimum utility threshold
	 * @return a new pattern
	 * @throws IOException
	 */
	private static SequenceList combine(SequenceList pattern1, SequenceList pattern2, int minUtility){

		List<Integer> S1 = new ArrayList<Integer>();
		List<Integer> S2 = new ArrayList<Integer>();
		int i=0;
		int lastItem=0;
		while(i < pattern1.itemsets.size()){
			S1.addAll(pattern1.itemsets.get(i));
			i++;
		}
		
		S1.remove(0);
		
		int j=0;
		while(j < pattern2.itemsets.size()){
			S2.addAll(pattern2.itemsets.get(j));
			j++;
		}
		lastItem=S2.get(S2.size()-1);
		S2.remove(S2.size()-1);
		
		if(!S1.equals(S2)){
			return null;
		}else{
			SequenceList combiner = new SequenceList();
			combiner.itemsets.addAll(pattern1.itemsets);
			
			if(pattern2.itemsets.get(pattern2.itemsets.size()-1).size()>=2){
				//item-based
				List<Integer> itemset = new ArrayList<Integer>();
				itemset.addAll(combiner.itemsets.get(combiner.itemsets.size()-1));
				itemset.add(lastItem);
				
				Collections.sort(itemset,new Comparator<Integer>(){
		            public int compare(Integer mc1,Integer mc2){
		            	return mc1-mc2;
		            }
				});
				
				combiner.itemsets.remove(combiner.itemsets.size()-1);
				combiner.itemsets.add(itemset);
				
				if( EUCPProperty(combiner.itemsets, minUtility) == false )
					return null;
				
				combiner.itemBasedExtend(pattern1, lastItem, AlgoUHUSPM.sequenceDatabase);
			}else {
				//itemset-based
				combiner.itemsets.add(pattern2.itemsets.get(pattern2.itemsets.size()-1));
				
				if( EUCPProperty(combiner.itemsets, minUtility) == false )
					return null;
				
				combiner.itemsetBasedExtend(pattern1, lastItem, AlgoUHUSPM.sequenceDatabase);
			}
			return combiner;
		}
	}

	/**
	 * Check the downward closure property
	 * @param itemset a sequence
	 * @return true if the pattern passes the downward closure property. Otherwise false
	 * @throws ClassNotFoundException if cast exception
	 * @throws IOException  if error while writing to buffer
	 */
	private static  boolean DCProperty(List<List<Integer>> itemset) throws ClassNotFoundException, IOException{
		@SuppressWarnings("unchecked")
		List<List<Integer>> itemsetCopy = deepcopy(itemset);
		
		int size = itemsetCopy.size();
		for(int i = 0;i <itemsetCopy.size(); i++){
			for(int j = 0; j<itemsetCopy.get(i).size(); j++){
				int item = itemsetCopy.get(i).get(j);
				
				itemsetCopy.get(i).remove(j);
				if(itemsetCopy.get(i).size() == 0)
					itemsetCopy.remove(i);
				if(!PSs.contains(itemsetCopy)){
					return false;
				}
				if(itemsetCopy.size() == size)
					itemsetCopy.get(i).add(j, item);
				else{
					List<Integer> one = new ArrayList<Integer>();
					one.add(item);
					itemsetCopy.add(i, one);
				}
			}
		}
		return true;
	}
	
	/**
	 * Check if a pattern satistfies the EUCP property
	 * @param itemset a pattern
	 * @param minUtility the minimum utility threshold
	 * @return true if the EUCP property is passed. Otherwise false.
	 */
	private static  boolean EUCPProperty(List<List<Integer>> itemset, int minUtility){
		int l = itemset.size()-1;
		int r = itemset.get(l).size()-1;
		int lItem = itemset.get(l).get(r);
		List<List<Integer>> oneTwo = new ArrayList<List<Integer>>();
		List<Integer> one = new ArrayList<Integer>();
		List<Integer> two = new ArrayList<Integer>();
		two.add(lItem);
		oneTwo.add(one);
		oneTwo.add(two);
		for(int i = 0; i<itemset.size()-1; i++){
			for(int j = 0; j < itemset.get(i).size(); j++){
				oneTwo.get(0).add(itemset.get(i).get(j));
				if(USs.get(oneTwo) < minUtility)
					return false;
				oneTwo.get(0).remove(0);
			}
		}
		
		oneTwo.remove(0);
		for(int j = 0; j < r; j++){
			int item = itemset.get(l).get(j);
			oneTwo.get(0).add(0, item);
			if(USs.get(oneTwo) < minUtility)
				return false;
			oneTwo.get(0).remove(0);
		}
		
		return true;
	}
	
	/**
	 * write a pattern to file
	 * @param sequence the pattern
	 * @param writer a buffered writer to write to the file
	 * @throws IOException 
	 */
	private static void writeOut(SequenceList sequence, BufferedWriter writer) throws IOException{
		for (List<Integer> items : sequence.itemsets) {
			for (int item : items) {
				writer.write(item + " ");
			}
			writer.write("-1 ");
		}
		writer.write("#UITL: ");
		writer.write(Integer.toString(sequence.sumUtility));
		writer.write(" #SP: ");
		writer.write(Float.toString(sequence.sumProbability));
		writer.newLine();
	}
	
	/**
	 * Perform a deep copy of a list of objects
	 * @param src the List to be copied
	 * @return a new list
	 * @throws IOException if error while copying
	 * @throws ClassNotFoundException if error while casting to a type
	 */
	public static  List deepcopy(List src) throws IOException,  
    ClassNotFoundException {  
		ByteArrayOutputStream byteout = new ByteArrayOutputStream();  
		ObjectOutputStream out = new ObjectOutputStream(byteout);  
		out.writeObject(src);  
		ByteArrayInputStream bytein = new ByteArrayInputStream(byteout.toByteArray());  
		ObjectInputStream in = new ObjectInputStream(bytein);  
		List dest = (List) in.readObject();  
		return dest;  
	} 
}
