package ca.pfv.spmf.algorithms.frequentpatterns.fhmds.naive;


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

import java.util.HashMap;

import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoHUIMiner;

/**
 * This class represents a UtilityList as used by the HUI-Miner algorithm.
 *
 * @see AlgoHUIMiner
 * @see Element
 * @author Philippe Fournier-Viger
 */
class UtilityList {
	int item;  // the item
	float sumIutils = 0;  // the sum of item utilities
	float sumRutils = 0;  // the sum of remaining utilities
	//List<Element> elements = new ArrayList<Element>();  // the elements
	HashMap<Integer,Batch> batches = new HashMap<Integer,Batch>();
	/**
	 * Constructor.
	 * @param item the item that is used for this utility list
	 */
	public UtilityList(int item){
		this.item = item;
		
	}
	public UtilityList(int item,int winSize){
		this.item = item;
		for(int i=1;i<=winSize;i++)
		{
			Batch b=new Batch(i,0,0);
			batches.put(i,b);
		}
	}
	
	public UtilityList(int item,int winSize,int win_number){
		this.item = item;
		for(int i=0;i<winSize;i++)
		{
			Batch b=new Batch(win_number+i,0,0);
			batches.put(win_number+i, b);
		}

	}
	
	/**
	 * Method to add an element to this utility list and update the sums at the same time.
	 */
	public void addElement(Element element,int winSize,int number_transactions){
		sumIutils += element.iutils;
		sumRutils += element.rutils;
		int batch_number=0;
		
		if (element.tid%number_transactions==0)
			batch_number=element.tid/number_transactions;
		else
			batch_number=element.tid/number_transactions+1;
		try {
			//if(batches.containsKey(batch_number))
				batches.get(batch_number).elements.add(element);
				batches.get(batch_number).sum_batch_iutils+=element.iutils;
				batches.get(batch_number).sum_batch_rutils+=element.rutils;
			/*else
			{
				Batch b=new Batch(batch_number,0,0);
				batches.put(batch_number, b);
				batches.get(batch_number).elements.add(element);
			}*/
			
		}catch(Exception e)
		{
			System.out.println("There");
			
		}
		
		
		
	}
}
