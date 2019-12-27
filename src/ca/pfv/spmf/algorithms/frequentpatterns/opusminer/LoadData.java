package ca.pfv.spmf.algorithms.frequentpatterns.opusminer;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the ItemsetRec structure used by the Opus-Miner algorithm proposed in : </br></br>
 * 
 * Webb, G.I. & Vreeken, J. (2014) Efficient Discovery of the Most Interesting Associations.
  ACM Transactions on Knowledge Discovery from Data. 8(3), Art. no. 15.
 *
 *  The code was translated from C++ to Java.  The modifications to the original C++ code to obtain Java
 *  code and improvements are copyright by Xiang Li and Philippe Fournier-Viger, 
 *  while the original C++ code is copyright by Geoff Web.
 *  
 *  The code is under the GPL license.
 */
/*
 * load_data.h - header file for the load_data.cpp module of OPUS Miner.
 ** Copyright (C) 2012 Geoffrey I Webb
 **
 ** This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 ** 
 ** This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 ** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 ** 
 ** You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

public class LoadData {


	public static void load_data(String filename) throws IOException
	{
		// Reset the variables
		Global.noOfTransactions = 0;
		Global.tids.clear();
		Global.noOfItems = 0;
		
		String thisLine; // variable to read each line
		BufferedReader myInput = null; // object to read the file
		try {
			FileInputStream fin = new FileInputStream(new File(filename));
			myInput = new BufferedReader(new InputStreamReader(fin));
			// for each line
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is not a comment, is not empty or is not other
				// kind of metadata
				if (thisLine.isEmpty() == false &&
						thisLine.charAt(0) != '#' && thisLine.charAt(0) != '%'
						&& thisLine.charAt(0) != '@') {

					Global.noOfTransactions++;
					int tid = Global.noOfTransactions;
					
					// split the line according to spaces into items 
					String[] items = thisLine.split(" ");
					for(String itemString : items){
						Integer item = Integer.valueOf(itemString);
						
						
						while(Global.tids.size() < item + 1){
							Global.tids.add(new tidset());
						}	
						
						Global.tids.get(item).add(tid);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}
		
		Global.noOfItems = Global.tids.size() - 1;
	}
	
	/**
	 * Load a CSV file where each line are in this format:
	 *    oranges, lettuce, tomatoes, confectionery
	 * @param filename
	 * @throws IOException
	 */
	public static void loadCSVdata(String filename) throws IOException
	{
		// Reset the variables
		Global.noOfTransactions = 0;
		Global.tids.clear();
		Global.noOfItems = 0;
		Map<String, Integer> mapNameToItem = new HashMap<String, Integer>();
		Global.itemNames.clear();
		Global.itemNames.add(null);
		
		String thisLine; // variable to read each line
		BufferedReader myInput = null; // object to read the file
		try {
			FileInputStream fin = new FileInputStream(new File(filename));
			myInput = new BufferedReader(new InputStreamReader(fin));
			
			// for each line
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is not a comment, is not empty or is not other
				// kind of metadata
				if (thisLine.isEmpty() == false &&
						thisLine.charAt(0) != '#' && thisLine.charAt(0) != '%'
						&& thisLine.charAt(0) != '@') {

					Global.noOfTransactions++;
					int tid = Global.noOfTransactions;
					
					// split the line according to spaces into items 
					String[] items = thisLine.split(" ");
					for(String itemString : items){
						Integer item = mapNameToItem.get(itemString);
						if(item == null){
							item = mapNameToItem.size()+1;
							mapNameToItem.put(itemString, item);
							Global.itemNames.add(itemString);
						}
						
						if(Global.tids.size() < item + 1){
							for(int i = 0; i <= item+1 - Global.tids.size(); i++){
								Global.tids.add(new tidset());
							}
						}	
						
						Global.tids.get(item).add(tid);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}
		
		Global.noOfItems = Global.tids.size() - 1;
	}


}