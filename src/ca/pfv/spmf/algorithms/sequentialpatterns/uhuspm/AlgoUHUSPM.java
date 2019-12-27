package ca.pfv.spmf.algorithms.sequentialpatterns.uhuspm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.pfv.spmf.tools.MemoryLogger;

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
 * This is an implementation of the UHUSPM algorithm as presented in this paper: <br/><br/>
 * 
 *    Zhang, B., Lin, J. C.-W., Li, T., Gan, W., Fournier-Viger, P. (2017). Mining High Utility-Probability Sequential 
 *    Patterns in Uncertain Databases. PLoS One, Public Library of Science, to appear.
 * 
 * @author Ting Li
 */
public class AlgoUHUSPM {
	/** the maximum memory usage */
	int  maxMemory = 0;     
	
	/** the time the algorithm started */
	long startTimestamp = 0; 
	
	/**  the time the algorithm terminated */
	long endTimestamp = 0;   
	
	/** the number of HUSPs */
	public static int numberOfHUSP = 0;
	
	/** the number of candidates */
	public static int numberOfCandidates = 0;

	/** the sequence datatabase */
	public static List<List<Itemset>> sequenceDatabase;
	
	/** map of item to SWU */
	Map<Integer, Integer> SWU;
	
	/** map of item to SWP */
	Map<Integer, Float> SWP;
	
	/** map of order SWU */
	Map<Integer, Integer> orderSWU;
	
	/** map of order SWP */
	Map<Integer, Float> orderSWP;
	
	/** writer to write the output file **/
	BufferedWriter writer = null;  
	
	void initialDatabase(String input, int minUtility, float minProbability) throws IOException{

				
		BufferedReader myInput = null;

		try {
			// prepare the object for reading the file
			// for each line (transaction) until the end of file
			myInput =  new BufferedReader(new InputStreamReader( new FileInputStream(new File(input))));
			String thisLine = null;
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is  a comment, is  empty or is a
				// kind of metadata
					Set<Integer> set = new HashSet<Integer>();
					String arry[] = thisLine.split(" -1 ");
					
					int lineUtility = Integer.parseInt(arry[arry.length-2]);
					float lineProbability = Float.parseFloat(arry[arry.length-1]);
					
					for(int i=0; i<arry.length-2; i ++){
						String itemset[] = arry[i].split(" , ");
						
						for(int j=0; j<itemset.length; j++){
							String itemProperty[] = itemset[j].split(" ");
							int item=Integer.parseInt(itemProperty[0]);
							set.add(item);
						}
				}
				
			   Iterator<Integer> it=set.iterator();
		       while(it.hasNext()){
		           int item= it.next();
		           
		           if(!SWU.containsKey(item)){
						SWU.put(item, lineUtility);
					}else {
						SWU.put(item, SWU.get(item)+lineUtility);
					}
					
					if(!SWP.containsKey(item)){
						SWP.put(item, lineProbability);
					}else {
						SWP.put(item, SWP.get(item)+lineProbability);
					}
		       }
			}
		} catch (Exception e) {
			// catches exception if error while reading the input file
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
	    }
		
		
		try {
			// for each line (transaction) until the end of file
			myInput =  new BufferedReader(new InputStreamReader( new FileInputStream(new File(input))));
			String thisLine = null;
			int order = 0;
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is  a comment, is  empty or is a
				// kind of metadata
				    List<Itemset> sequnce = new ArrayList<Itemset>();
					String arry[] = thisLine.split(" -1 ");
					int lineSWU = Integer.parseInt(arry[arry.length-2]);
					float lineSWP = Float.parseFloat(arry[arry.length-1]);
					for(int i=0; i<arry.length-2; i ++){
						Itemset sitemset = new Itemset();
						
						String itemset[] = arry[i].split(" , ");
						
						for(int j=0; j<itemset.length; j++){
							
							String itemProperty[] = itemset[j].split(" ");
							int item=Integer.parseInt(itemProperty[0]);
							if(SWU.get(item) >= minUtility && SWP.get(item) >= minProbability){
								int utility = Integer.parseInt(itemProperty[1]);
								
								Item sitem = new Item();
								sitem.item = item;
								sitem.utility = utility;
								sitemset.Itemset.add(sitem);
							}
						}
						
						if(!sitemset.Itemset.isEmpty()){
							sequnce.add(sitemset);
						}
					}
					
					if(!sequnce.isEmpty()){
						sequenceDatabase.add(sequnce);
						orderSWU.put(order, lineSWU);
						orderSWP.put(order, lineSWP);
						order++;
					}
		       }
		} catch (Exception e) {
			// catches exception if error while reading the input file
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
	    }
	}

	List<SequenceList> initialization(String input, int minUtility, float minProbability) throws IOException{
		
		Map<Integer, SequenceList> projectSequnce = new HashMap<Integer, SequenceList>();
		
		initialDatabase(input, minUtility, minProbability);
		
		for(int i = 0; i < sequenceDatabase.size(); i++){
			int lineSWU = orderSWU.get(i);
			float lineProbability = orderSWP.get(i);
			
			for(int j = 0; j <sequenceDatabase.get(i).size(); j++){
				for(int k= 0; k < sequenceDatabase.get(i).get(j).Itemset.size(); k++){
					int item = sequenceDatabase.get(i).get(j).Itemset.get(k).item;
					int utility = sequenceDatabase.get(i).get(j).Itemset.get(k).utility;
					
					
					if( !projectSequnce.containsKey(item)){
						SequenceList list = new SequenceList();
						List<Integer> itemset = new ArrayList<Integer>();
						itemset.add(item);
						list.addItemset(itemset);
						list.addElement(i, j, utility, lineProbability, lineSWU);
						projectSequnce.put(item, list);
					}else projectSequnce.get(item).addElement(i, j, utility, lineProbability, lineSWU);
				}
			}
		}
		
		List<SequenceList> projectOneItemset = new ArrayList<SequenceList>(projectSequnce.values());
		
		/*calculate sumUtility and sumProbability of each sequnceList in projectOneItemset*/
		for(int i = 0; i < projectOneItemset.size(); i++){
			projectOneItemset.get(i).calculate();
			if(projectOneItemset.get(i).sumSWU < minUtility || projectOneItemset.get(i).sumProbability < minProbability){
				projectOneItemset.remove(i);
				i--;
			}
		}
		
		Collections.sort(projectOneItemset,new Comparator<SequenceList>(){
            public int compare(SequenceList mc1,SequenceList mc2){
            	return mc1.itemsets.get(0).get(0)-mc2.itemsets.get(0).get(0);
            }
		});
		
		return projectOneItemset;
	}

	/**
	 * Run the algorithm
	 * @param input an input file
	 * @param output an output file path
	 * @param minUtility a minimum utility threshold
	 * @param minProbability a minimum probability threshold
	 * @throws IOException if exception while reading or writing a file
	 * @throws ClassNotFoundException class not found exception
	 */
	public void runAlgorithm(String input, String output, int minUtility, float minProbability) throws IOException, ClassNotFoundException{
		maxMemory =0;
		startTimestamp = System.currentTimeMillis();
		
		numberOfCandidates = 0;
		numberOfHUSP = 0;
		maxMemory = 0;
		sequenceDatabase = new ArrayList<List<Itemset>>();
		SWU = new HashMap<Integer, Integer>();
		SWP = new HashMap<Integer, Float>();
		orderSWU = new HashMap<Integer, Integer>();
		orderSWP = new HashMap<Integer, Float>();
		
		// create a writer object to write results to file
		writer = new BufferedWriter(new FileWriter(output));
		
		List<SequenceList> nextGeneration = initialization( input, minUtility, minProbability);
		/* check the memory usage*/
        MemoryLogger.getInstance().checkMemory();
		
		if(!nextGeneration.isEmpty()){
			/* check the memory usage*/
	        MemoryLogger.getInstance().checkMemory();
	        nextGeneration = GenerateCandidates.generate2GenerationCandidates(nextGeneration, minUtility, minProbability, writer);
		}
		 
		while(!nextGeneration.isEmpty()){
			/* check the memory usage*/
	        MemoryLogger.getInstance().checkMemory();
	        nextGeneration = GenerateCandidates.generateNextGenerationCandidates(nextGeneration, minUtility, minProbability, writer);
		}

        MemoryLogger.getInstance().checkMemory();

		writer.close();
		
		endTimestamp = System.currentTimeMillis();
	}
	
	/**
	 * Print statistics about the algorithm execution
	 * @throws IOException
	 */
	public void printStats() throws IOException {
		System.out.println("=======  THE RESULT OF THE ALGORITHM - STATS ============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp)/1000 + " s");
		System.out.println(" Candidates count : " + numberOfCandidates); 
		System.out.println(" HUSP count: " + numberOfHUSP); 
		System.out.println(" Max memory: "+MemoryLogger.getInstance().getMaxMemory()+"  MB");
		System.out.println("======================================================");
	}
}
