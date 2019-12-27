package ca.pfv.spmf.algorithms.frequentpatterns.chud;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/* This file is copyright (c) 2011 Cheng-Wei-Wu, Philippe Fournier-Viger
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
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/

/**
 * This class converts an horizontal database to a vertical database for applying
 * the CHUD algorithm, which was proposed in <br/><br/>
 * 
 * Wu, C.-W., Fournier-Viger, P., Yu., P. S., Tseng, V. S. (2011). Efficient Mining of a 
 * Concise and Lossless Representation of High Utility Itemsets. Proceedings of the 11th 
 * IEEE Intern. Conference on Data Mining (ICDM 2011). IEEE CS Press, pp.824-833.
 * 
 * @see AlgoCHUD 
 * */
class AlgoConvertToVerticalDatabase {
	protected long totaltime =0;
	
	/** number of transactions of a database to be used by CHUD */
	private int maximumNumberOfTransactions = Integer.MAX_VALUE;
	
	/**
	 * Constructor
	 */
	public AlgoConvertToVerticalDatabase(){
		
	}
	
	/**
	 * Run the algorithm
	 * @param input an input database in horizontal format
	 * @param vertical the first part of the vertical database
	 * @param vertical2 the second part of the vertical database
	 * @param vertical3 the third part of the vertical database
	 * @throws IOException exception if an error occur while reading/writing the output files.
	 */
	public void run(String input, String vertical, String vertical2, String vertical3) throws IOException {
		totaltime = System.currentTimeMillis();
		
		int maxItem = -1;
		
		// READ HORIZONTAL DATABASE
		BufferedReader reader = new BufferedReader(new FileReader(input));
		Map<Integer, ItemStructure> mapStructures = new HashMap<Integer, ItemStructure>();
		Map<Integer, Integer>  mapTidTU = new HashMap<Integer, Integer>();
		
		String line;
		int tidcount=0;
		while( ((line = reader.readLine())!= null)){
			String[] lineSplited = line.split(":");
			
			int transactionUtility = Integer.parseInt(lineSplited[1]);  			
			mapTidTU.put(tidcount, transactionUtility);
			
			String[] transactionItems = lineSplited[0].split(" ");
			String[] transactionItemsUtility = lineSplited[2].split(" ");
			
			for(int i=0; i< transactionItems.length; i++){
				int itemValue = Integer.parseInt(transactionItems[i]);
				
				// Add tid to tidset of item
				ItemStructure structure = mapStructures.get(itemValue);
				if(structure == null){
					structure = new ItemStructure();
					structure.item = itemValue;
					if(itemValue > maxItem) {
						maxItem = itemValue;
					}
					mapStructures.put(itemValue, structure);
				}
				structure.tidset.add(tidcount);
				structure.utilitiesForEachTid.add(Integer.parseInt(transactionItemsUtility[i]));
			}
			tidcount++;
			if(tidcount == maximumNumberOfTransactions) {
				break;
			}
		}
		reader.close();
		
		// sort list of items in lexical order
		List<Integer> listItems = new ArrayList<Integer>(mapStructures.keySet());
		Collections.sort(listItems);

		// WRITE VERTICAL DATABASE FILE 1
		BufferedWriter writer = new BufferedWriter(new FileWriter(vertical)); 
		for(Integer item : listItems){
			ItemStructure structure = mapStructures.get(item);
			StringBuilder buffer = new StringBuilder();
			buffer.append(structure.item);
			buffer.append(":");
			// (1) SAVE TIDS
			Iterator<Integer> iterTIDS = structure.tidset.iterator();
			while(iterTIDS.hasNext()){
				buffer.append(iterTIDS.next());
				if(iterTIDS.hasNext()){
					buffer.append(' ');
				}else{
					break;
				}
			}
			buffer.append(":");
			
			// (2) SAVE UTILITY FOR EACH TID
			Iterator<Integer> iterTIDUtility = structure.utilitiesForEachTid.iterator();
			while(iterTIDUtility.hasNext()){
				buffer.append(iterTIDUtility.next());
				if(iterTIDUtility.hasNext()){
					buffer.append(' ');
				}else{
					break;
				}
			}
			writer.write(buffer.toString());
			writer.newLine();
		}
		
		writer.flush();
		writer.close();
		
		// WRITE VERTICAL DATABASE FILE 2
		BufferedWriter writer2 = new BufferedWriter(new FileWriter(vertical2)); 
		
		Iterator<Entry<Integer, Integer>> iterEntries = mapTidTU.entrySet().iterator();
		while(iterEntries.hasNext()){
			Entry<Integer, Integer> entry = iterEntries.next();
			if(iterEntries.hasNext()){
				writer2.write(entry.getKey() + ":" + entry.getValue());
				writer2.newLine();
			}else{
				writer2.write(entry.getKey() + ":" + entry.getValue());
				break;
			}
		}
		writer2.close();
		
		// WRITE VERTICAL DATABASE FILE 3
		BufferedWriter writer3 = new BufferedWriter(new FileWriter(vertical3)); 
		writer3.write(tidcount + "\n");
		writer3.write(maxItem + "\n");
		writer3.write(mapStructures.keySet().size() + "\n"); 
		writer3.close();
		totaltime = System.currentTimeMillis() - totaltime;
	}

	/**
	 * This is an inner class that is used for converting the database
	 * It represents an item, the list of identifiers (tids) of transactions 
	 * containing that item, and the utility of this item in each of these transactions.
	 */
	static class ItemStructure {
		/** an item */
		public int item = 0;
		/** The list of identifiers of transactions containing the item */
		public List<Integer> tidset = new ArrayList<Integer>();
		/** The list of utility values for transactions containing the item */
		public List<Integer> utilitiesForEachTid = new ArrayList<Integer>();
		
		/** Check if an "ItemStructure objects is equal (have the same item)
		 * @param another "ItemStructure" object
		 * @return true if the same. otherwise, returns false.
		 */
		public boolean equals(Object obj) {
			if(obj == this){
				return true;
			}
			ItemStructure itemS2 = (ItemStructure) obj;
			if(itemS2.item == item){
				return true;
			}
			return super.equals(obj);
		}
		
		/**
		 * Get the hashcode of this object
		 * @return a hash code
		 */
		public int hashCode() {
			String hash = "" + item; 
			return hash.hashCode();
		}
	}

	/**
	 * Set the number of transactions of a database to be used by CHUD
	 * @param maximumNumberOfTransactions the number of transaction (a positive integer)
	 */
	public void setMaxNumberOfTransactions(int maximumNumberOfTransactions) {
		this.maximumNumberOfTransactions = maximumNumberOfTransactions;
	}
}
