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

public class ItemSummary {
	int itemName;
	int minUtility;
	int maxUtility;
	int TWU;
	int totalUtility;
	int support;

	ItemSummary(){
		
	}
	public ItemSummary(int item) {
		itemName = item;
		minUtility = 0;
		maxUtility = 0;
		TWU = 0;
		totalUtility = 0;
		this.support = 0;
	}
	public ItemSummary(int item, int minF, int maxF, int twu, int totalF, int supp) {
		itemName = item;
		minUtility = minF;
		maxUtility = maxF;
		TWU =twu;
		totalUtility = totalF;
		this.support = supp;
	}

	public void updateMinFrequency(int minF) {
		//if(this.minFrequency > minF)
		this.minUtility = minF;
	}

	public void updateMaxFrequency(int maxF) {
		this.maxUtility = maxF;
	}

	public void updateTWU(int twu) {
		this.TWU = this.TWU + twu;
	}

	public void updateTotalFrequency(int freq) {
		//float key=((HashMap<Short,Float>)utilityMap.get(key1)).get(freq);
		this.totalUtility = this.totalUtility + freq;
	}
	
	
	public void incrementSupp(){
		this.support ++;
	}

	public int getItemName() {
		return itemName;
	}

	public int getMinFreq() {
		return minUtility;
	}

	public int getMaxFreq() {
		return maxUtility;
	}

	public int getTWU() {
		return TWU;
	}

	public int getTotalFreq() {
		return totalUtility;
	}
	
	public int getSupport(){
		return support;
	}
	
	public void output(){
		System.out.println("ItemName : "+ this.itemName);
		System.out.println("MinFrequency : "+ this.minUtility);
		System.out.println("MaxFrequency : "+ this.maxUtility);
		System.out.println("TWU : "+ this.TWU);
		System.out.println("TotalUtility : "+ this.totalUtility);
		System.out.println("Support : " + this.support);
	}
	
	public String toString(){
		String line = "";
		line = line + this.itemName + " " + this.minUtility + " "
				+ this.maxUtility + " " + this.TWU + " "
				+ this.totalUtility + " " + this.support;
		return line;
	}

}