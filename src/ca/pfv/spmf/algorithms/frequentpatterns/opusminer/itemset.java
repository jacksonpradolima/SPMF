package ca.pfv.spmf.algorithms.frequentpatterns.opusminer;

import java.util.TreeSet;
/**
 * This is a queue of items that is sorted in descending order. Used by the Opus-Miner algorithm proposed in : </br></br>
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
// Original HEADER from the C++ code:
//
/* itemset.cpp - a module of OPUS Miner providing methods for the itemset class.
 ** Copyright (C) 2012 Geoffrey I Webb
 **
 ** This program is free software: you can redistribute it and/or modify
 ** it under the terms of the GNU General Public License as published by
 ** the Free Software Foundation, either version 3 of the License, or
 ** (at your option) any later version.
 ** 
 ** This program is distributed in the hope that it will be useful,
 ** but WITHOUT ANY WARRANTY; without even the implied warranty of
 ** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 ** GNU General Public License for more details.
 ** 
 ** You should have received a copy of the GNU General Public License
 ** along with this program.  If not, see <http: //www.gnu.org/licenses/>.
 */

/* itemset.h - header file for the itemset.cpp module of OPUS Miner.
 ** Copyright (C) 2012 Geoffrey I Webb
 **
 ** This program is free software: you can redistribute it and/or modify
 ** it under the terms of the GNU General Public License as published by
 ** the Free Software Foundation, either version 3 of the License, or
 ** (at your option) any later version.
 ** 
 ** This program is distributed in the hope that it will be useful,
 ** but WITHOUT ANY WARRANTY; without even the implied warranty of
 ** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 ** GNU General Public License for more details.
 ** 
 ** You should have received a copy of the GNU General Public License
 ** along with this program.  If not, see <http: //www.gnu.org/licenses/>.
 */

public class itemset extends TreeSet<Integer>  {

	public itemset() {
		// constructor
	}

	int count;
	float value;
	double p;
	boolean self_sufficient;
	
	/** hashcode -- phil **/
	int hashCode = 0;
	
	/**
	 * Check if this itemset is equal to another itemset
	 * @Override
	 * @author Philippe Fournier-Viger
	 */
	public boolean equals(Object o) {
		itemset o2 = (itemset)o;
		if(this.size() != o2.size()){
			return false;
		}
		
		for(Integer item : o2){
			if(this.contains(item) == false){
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Calculate the hash code of this itemset
	 * @Override
	 * @author Philippe Fournier-Viger
	 */
	public int hashCode() { 
//		hashCode = 0;
		// if the hashcode has no been computed yet, compute it
		if(hashCode == 0){
			for(Integer item : this){
				hashCode += item;
			}
		}
		return hashCode;
	}

}