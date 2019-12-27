package ca.pfv.spmf.algorithms.frequentpatterns.uphist;
/* This file is copyright (c) 2018+  by Siddharth Dawar et al.
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A histogram as used by the UPHist algorithm.
 * @author S. Dawar et al.
 *@see AlgoUPHist
 */
public class Hist{
	// Histogram key represents internal Utility, value represents number of transactions
	HashMap<Integer, Integer> H = null;
	
	/**
	 * addElement function adds another transaction inter utility to the histogram
	 * @param interU 
	 * interU is the internal utility of the transaction to be added
	 */
	public void addElement(Integer interU){
		if(!H.containsKey(interU)){
			H.put(interU, 1 );
		}else{
			H.put(interU, H.get(interU)+1);
		}
	}
	
	/**
	 * getMinSupportInterU is a function to get minimum internal Utility for
	 * support number of transactions
	 * @param supp
	 * supp specifies number of transactions
	 * @return
	 * miniumum internal utility
	 */
	
	public int getMinSupportInterU(int supp){
		int sum=0;
		//get the keys of H in an array
		Integer[] L = H.keySet().toArray(new Integer[H.size()]);
		//sort array
		Arrays.sort(L);
		int freq=0;
		// Process list in ascending order
		for(int i=0; i< L.length; i++){
			freq = freq + H.get(L[i]);
			if(freq >= supp){
				sum = sum + (supp -(freq-H.get(L[i]))) * L[i];
				
				break;
			}else{
				sum = sum + L[i]* H.get(L[i]);
			}
		}
		return sum;
	}
	
				
	
	/**
	 * getMaxSupportInterU is a function to get maximum internal Utility for
	 * support number of transactions
	 * @param supp
	 * supp specifies number of transactions
	 * @return
	 * maximum internal utility
	 */
	
	public int getMaxSupportInterU(int supp){
		int sum=0;
		Integer[] L = H.keySet().toArray(new Integer[H.size()]);
		//sort array
		Arrays.sort(L);
		// compute value
		int freq=0;
		// Process list in descending order
		for(int i=L.length; i > 0; i--){
			freq = freq + H.get(L[i-1]);
			if(freq >= supp){
				sum = sum + (supp -(freq-H.get(L[i-1]))) * L[i-1];
				break;
			}else{
				sum = sum + L[i-1]* H.get(L[i-1]);
			}
		}
		return sum;
	}
	
	// To be used by getPartialList Method
	/**
	 * addPair adds an entry into histogram. Used internall for 
	 * other function which splits histogram
	 * @param l
	 * internal utility
	 * @param value
	 * number of transactions
	 */
	private void addPair(Integer l, Integer value){
		H.put(l, value);
	}
	
	public HashMap<Integer, Integer> getHistogram(){
		return H;
	}
	
	public void updateHist(Hist h){
		HashMap<Integer, Integer> histogram = h.getHistogram();
		Iterator<Integer> iterInterU = histogram.keySet().iterator();
		while(iterInterU.hasNext()){
			Integer interU = iterInterU.next();
			if(!H.containsKey(interU)){
				H.put( interU, histogram.get(interU) );
				
			}else{
				H.put( interU, H.get(interU)+ histogram.get(interU));
			}
		}
	}
	public void updateHist(int quantity, int support)
	{
		if(!this.H.containsKey(quantity))
		{
			this.H.put( quantity,1);
			
		}
		else
		{
			this.H.put( quantity, H.get(quantity)+1);
			
		}
	}
	
	public Hist(){
		H= new HashMap<Integer,Integer>();
	}
	
	public Hist(int interU){
		H = new HashMap<Integer, Integer>();
		H.put(interU, 1);
	}
	public Hist(Hist h){
		if(H == null)
			H= new HashMap<Integer,Integer>();
		H.putAll(h.getHistogram());
	}

	
		
}