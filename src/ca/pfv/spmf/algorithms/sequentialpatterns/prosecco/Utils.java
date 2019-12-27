package ca.pfv.spmf.algorithms.sequentialpatterns.prosecco;

import ca.pfv.spmf.algorithms.sequentialpatterns.prefixspan.*;
import ca.pfv.spmf.patterns.itemset_list_integers_without_support.Itemset;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains a set of useful static functions for subsequence matching, used in ProSecCo. 
 * 
 * Copyright (c) 2008-2019 Sacha Servan-Schreiber
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE
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
 */

public class Utils {


	private static boolean isSubsetOf(List<Integer> itemsetA, List<Integer> itemsetB) {

		boolean found = false;

		for(int tokenA : itemsetA){
			for(int tokenB : itemsetB){

				// if itemsetB contains tokenA
				if(tokenA == tokenB) {
					found = true;
					break;
				} 
			}

			if (!found) {
				return false;
			}

			found = false;
		}

		return false;
	}

	public static boolean isSubsequenceOf(int[] sequenceA, int[] sequenceB) {

		List<Integer> itemsetA = new ArrayList<Integer>();
		List<Integer> itemsetB = new ArrayList<Integer>();

		int idxItemsetB = 0;

		for(int tokenA : sequenceA){

			boolean isSubset = false;

			// if it is an item
			if (tokenA >= 0) {
				itemsetA.add(tokenA);

			} else if(tokenA == -1){

				int i;

				// if it is an itemset separator
				for(i = idxItemsetB; i < sequenceB.length; i++){

					if(sequenceB[i] == -1) {

						if (isSubsetOf(itemsetA, itemsetB)) {
							idxItemsetB = i+1;
							isSubset = true;
							break;
						} 

						itemsetB.clear();
					} 
					else 
						itemsetB.add(sequenceB[i]);
				}

				if (!isSubset)
					return false;

				itemsetA.clear();

			} else if(tokenA == -2){
				break;
			}
		}

		return true;
	}

	public static boolean isSubsequenceOf(SequentialPattern sequenceA, int[] sequenceB, boolean multiItem) {

		if (!multiItem) {
			return isSubsequenceOfSingleItems(sequenceA, sequenceB);
		}

		int idxItemsetB = 0;
		boolean subset = false;

		for (Itemset itA : sequenceA.getItemsets()) {	
			subset = false;
			Itemset itB = new Itemset();

			// if it is an itemset separator
			for(int i = idxItemsetB; i < sequenceB.length; i++){

				if(sequenceB[i] == -1) {
					if (itB.containsAll(itA)) {
						subset = true;
						idxItemsetB = i;						
					} else {
						itB = new Itemset();
					}

				} else {
					itB.addItem(sequenceB[i]);
				}


				if (sequenceB[i+1] == -2)
					break;


			}

			if (!subset) {	
				return false;
			}
		}

		return true;
	}

	public static boolean isSubsequenceOfSingleItems(SequentialPattern sequenceA, int[] sequenceB) {


		int idxItemsetB = 0;
		boolean subset = false;


		for (Itemset itA : sequenceA.getItemsets()) {	
			subset = false;

			// if it is an itemset separator
			for(int i = idxItemsetB; i < sequenceB.length; i++){

				if(sequenceB[i] == -1) {
					continue;
				}

				if (itA.getItems().get(0) == sequenceB[i]) {
					subset = true;
					idxItemsetB = i+1;
					break;

				} 

				if (sequenceB[idxItemsetB] == -2)
					break;

			}

			if (!subset) {	
				return false;
			}
		}

		return true;
	}

}
