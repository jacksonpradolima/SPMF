package ca.pfv.spmf.algorithms.frequentpatterns.fhmds.ds;


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

public class FMAP_TWU {

	//The variable stores the complete TWU of item in the window
	float sumTWU=0;
	//This map stores the batch wise TWU of an item
	HashMap<Integer,Float> fmap_twu=new HashMap<Integer,Float>();
	
	/**
	 * The constructor constructs the respective batch numbers
	 * and inserts it into fmap_twu map.
	 * @param winSize
	 * @param win_number
	 */
	public FMAP_TWU(int winSize,int win_number){
		
		for(int i=0;i<winSize;i++)
		{
			fmap_twu.put(win_number+i, 0F);
		}

	}
	
	/**
	 * This method removes the TWU of the oldest batch from the map
	 * as well as reduces it from the sumTWU variable
	 * @param winSize
	 * @param win_number
	 */
	void updateTWU(int winSize, int win_number)
	{
		try {
		
			fmap_twu.put(winSize+win_number-1, 0F);
			sumTWU=sumTWU-fmap_twu.get(winSize+win_number-1-winSize);
			fmap_twu.remove(winSize+win_number-1-winSize);
		}catch(Exception e)
		{
			System.out.println("updateTWU fmap");
		}
	}
	 
	/**
	 * This method removes the TWU of the oldest batch from the map
	 * as well as reduces it from the sumTWU variable
	 * @param winSize
	 * @param win_number
	 * @param batch_number
	 */
	 	void updateTWU(int winSize, int win_number,int batch_number)
		{
			try {
			
				fmap_twu.put(batch_number, 0F);
				sumTWU=sumTWU-fmap_twu.get(winSize+win_number-1-winSize);
				fmap_twu.remove(winSize+win_number-1-winSize);
			}catch(Exception e)
			{
				System.out.println("updateTWU fmap");
			}
		} 
	 	
	 	/**
	 	 * This method adds the twu of new batch
	 	 * @param batch_number
	 	 * @param twu
	 	 */
	 	void addTWU(int batch_number,float twu)
	 	{
	 		try{
				Float oldtwu=fmap_twu.get(batch_number);
				fmap_twu.put(batch_number, oldtwu+twu);
				sumTWU+=twu;
			}catch(Exception e)
			{
				System.out.println("Exception in addTWU fmap");
			}
	 		
	 	}
	 	
	 	/**
	 	 * This method adds the twu of new batch
	 	 * @param twu
	 	 * @param tid
	 	 * @param winSize
	 	 * @param number_transactions
	 	 */
	 void addTWU(Float twu,int tid,int winSize,int number_transactions)
	{
		int batch_number=0;
		
		if (tid%number_transactions==0)
			batch_number=tid/number_transactions;
		else
			batch_number=tid/number_transactions+1;
		try{
			Float oldtwu=fmap_twu.get(batch_number);
			fmap_twu.put(batch_number, oldtwu+twu);
			sumTWU+=twu;
		}catch(Exception e)
		{
			System.out.println("Exception in addTWU fmap");
		}
	}
}
