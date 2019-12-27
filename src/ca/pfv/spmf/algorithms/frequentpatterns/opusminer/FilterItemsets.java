package ca.pfv.spmf.algorithms.frequentpatterns.opusminer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/**
 * The filterItemsets class from the Opus-Miner algorithm proposed in : </br></br>
 * 
 * Webb, G.I. & Vreeken, J. (2014) Efficient Discovery of the Most Interesting Associations.
  ACM Transactions on Knowledge Discovery from Data. 8(3), Art. no. 15.
 *
 *  The code was translated from C++ to Java.  The modifications to the original C++ code to obtain Java
 *  code and improvements are copyright by Xiang Li and Philippe Fournier-Viger, 
 *  while the original C++ code is copyright by Geoff Web.
 *  
 *  The code is under the GPL 3 license.
 */
//=========================================
// This is the header of the original C++ code:
//  
//check whether itemsets can be explained by their supersets
/* filter_itemsets.cpp - a module of OPUS Miner providing filter_itemsets, a function to filter itemsets that are not indpendently productive.
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
 ** along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class FilterItemsets {

	public static void filter_itemsets(ArrayList<itemsetRec> is)
	{
	  if (!is.isEmpty())
	  {
		// Sort the itemsets so that the largest are first.
		// This way we only need to look at the itemsets before one that we are processing to find superset.
		// Also, we will determine whether a superset is self sufficient before trying to determine whether its subsets are

		Collections.sort(is, new Comparator<itemsetRec>(){
			@Override
			public int compare(itemsetRec o1, itemsetRec o2) {
				return o2.size() - o1.size();                 /// IMPORTANT: MAKE SURE BY LARGEST TO SMALLEST!!!
			}});
		
//		public static boolean sizegt(itemset i1, itemset i2) {
//			return i1.size() > i2.size();
//		}

		Iterator<itemsetRec> subset_it = is.iterator();
	
		itemset supitems = new itemset(); // the additional items n the supersets of the current itemset
		tidset supsettids = new tidset(); // the tids covered by the supitems
		tidset thissupsettids = new tidset(); // the tids covered by the supitems
		if(subset_it.hasNext())
		{
			subset_it.next();
		}
		int i = 0;
		int j = 0;
		while (subset_it.hasNext()) 
		{
			
			supsettids.clear();
			
			
			itemset itemsetSubsetIt =  subset_it.next();
			i++;
			itemsetRec currentSubset = (itemsetRec) itemsetSubsetIt;
			
		
			if(currentSubset.size() == 2 && currentSubset.contains(2) && currentSubset.contains(63)){
				System.out.println("");
			}
			
			
			// get the TIDs that are covered by the current itemset's supersets
			Iterator<itemsetRec> supset_it = is.iterator();
			//int qqq = 0;
			j = 0;
			while (j<i)
			{
				j++;
				itemsetRec currentSupset = (itemsetRec) supset_it.next();
				
				if (currentSupset.self_sufficient)
				{
					supitems.clear();
					
					
					
					if (Utils.subset(currentSubset,currentSupset ))
					{
						Iterator<Integer> it = currentSupset.iterator();
						
						while (it.hasNext()) {
							Integer item = (Integer) it.next();
							if(currentSubset.contains(item) == false)
							{
								supitems.add(item);
							}
						}


						if (!supitems.isEmpty())
						{
							 
							 thissupsettids = Utils.gettids(supitems, thissupsettids);
							 
							 tidset x = new tidset();
							 x.addAll(thissupsettids);

				              if (supsettids.isEmpty()) {
				                supsettids = (tidset) x.clone();
				              }
				              else {
				            	  supsettids = tidset.dunion(supsettids, x);
				              }

						}
					}
				}
			}

			// !checkSS(*subset_it, supsettids)
			
		//	System.out.print(supsettids.size());
			//System.out.print("  ");
			//System.out.println(qqq);
			
			if (!supsettids.isEmpty() && !checkSS(currentSubset, supsettids))
			{
				// only call chechSS if one or more supersets were found (and hence TIDs retrieved
				itemsetSubsetIt.self_sufficient = false;
				currentSubset.self_sufficient = false;
			}
		}
	  }
	
	}

	// check all combinations of intersections of partitions of tidsavail moved
	// to either tidsleft or tidsright
	public static boolean checkSS2(ArrayList<tidset> uniqueTids, int no,
			tidset tidsleft, tidset tidsright, int availabletids, int count,
			double alpha) 
	   {
		if (no == 0)
		{
			if (FisherTest.fisherTest(availabletids - tidsleft.size()
					- tidsright.size() + count, tidsleft.size() - count,
					tidsright.size() - count, count) > alpha) 
			{
				return false;
			} else 
			{
				return true;
			}
		}

		// first try with the tidset committed to the left then try with it
		// committed to the right

		tidset newtids = new tidset();
		newtids.intersection(newtids, uniqueTids.get(no - 1), tidsleft);

		if (!checkSS2(uniqueTids, no - 1, newtids, tidsright, availabletids,
				count, alpha)) {
			return false;
		}

		newtids.intersection(newtids, uniqueTids.get(no - 1), tidsright);

		if (!checkSS2(uniqueTids, no - 1, tidsleft, newtids, availabletids,
				count, alpha)) {
			return false;
		}

		return true;
	}
	

	// check whether itemset is is self sufficient given that it has supersets
	// that cover the TIDs in supsettids
	public static boolean checkSS(itemset is, tidset supsettids)
	{
		
		
		
	
	  ArrayList<Integer> isList = new ArrayList<Integer>(is);
		
	  boolean result = true;

	  // find for each item in "is" the TIDs that it covers that are not in supsettids
	  ArrayList<tidset> uniqueTids = new ArrayList<tidset>(isList.size());
	  
//	  uniqueTids.resize(is.size());
//
//
//	  int i;
//	  itemset::const_iterator it;
//
//	  // for each item
//	  for (it = is.begin(), i = 0; it != is.end(); it++, i++) {
//	    uniqueTids[i].resize(tids[*it].size());
//		// calculate the difference between the tidset of the item and the tidset of the superset
//	    TID *ut_end = std::set_difference(tids[*it].begin(), tids[*it].end(), supsettids.begin(), supsettids.end(), &uniqueTids[i][0]);
	  
	  while(uniqueTids.size() < isList.size()){
		  uniqueTids.add(new tidset());
	  }
	  
	  // For each item
	  for (int i = 0; i < isList.size(); i++) 
	  {
		Integer current_it = isList.get(i);

		// Calculate set difference
		// the elements that are present in the first set, but not in the second one. 
		setDifference(Global.tids.get(current_it), supsettids, uniqueTids.get(i));
		
		if (uniqueTids.get(i).size() == 0)
		{
		  // there cannot be a significant association from adding this tidset
		  result = false;
		  break;
		}
	  }

	  if (result)
	  {
		// set up a process that will check whether uniqueCov.size() is significantly greater than can be predicted by assuming independence between any partition of is
		
		// this is the TIDs covered by is that are not in supsettids
		//tidset uniqueCov = new tidset(); 

		tidset uniqueCov = new tidset(); // this is the TIDs covered by is that are not in supsettids
		uniqueCov.addAll(uniqueTids.get(0));
		
		// calculate uniqueCov
		for (int i = 1; i < is.size(); i++)
		{
		  tidset.dintersection(uniqueCov, uniqueTids.get(i));
		}

		// this is the cover of the items committed to the right - initialise it to the last unique TID
		tidset tidsright = new tidset(); 
		tidsright = uniqueTids.get(uniqueTids.size() - 1);
		
		// start with the last item committed to the right, then successively commit each item first to the left 
		// then to the right
		for (int i = uniqueTids.size() - 2; i >= 0; i--)
		{
		  result = checkSS2(uniqueTids, i, uniqueTids.get(i), tidsright, Global.noOfTransactions - supsettids.size(), uniqueCov.size(), Global.getAlpha(is.size()));

		  if (result == false)
		  {
			  return false;
		  }

		  if (i > 0)
		  {
			tidsright.dintersection(tidsright, uniqueTids.get(i));
		  }
		}
	  }

	  return result;
	}
	
	/**
	 * Calculate set difference, that is the elements that 
	 *  are present in the first set, but not in the second one. 
	 * @author Philippe Fournier-Viger
	 * @param set1
	 * @param set2
	 * @param result 
	 * @return the set difference
	 */
	private static tidset setDifference(tidset set1, tidset set2, tidset result) {
//		tidset result = new tidset();
		int i = 0;
		int j = 0;
		
		while(i < set1.size() && j < set2.size())
		{
			Integer val1 = set1.get(i);
			Integer val2 = set2.get(j);
			if(val1.equals(val2))
			{
				i++;
				j++;
			}
			else if (val1 > val2)
			{
				j++;
			}else if (val1 < val2)
			{
				result.add(val1);
				i++;
			}
		
		}
		while(i < set1.size())
		{
			Integer val1 = set1.get(i);
			result.add(val1);
			i++;
		}
		return null;
	}

//	/**
//	 * @author Tangible
//	 * @param list
//	 * @param newSize
//	 */
//	public static <T> void resize(ArrayList<T> list, int newSize)
//	{
//		T value = null;
//
//		if (list.size() > newSize)
//		{
//			for (int i = list.size() - 1; i >= newSize; i--)
//			{
//				list.remove(i);
//			}
//		}
//		else if (list.size() < newSize)
//		{
//			for (int i = list.size(); i < newSize; i++)
//			{
//				list.add(value);
//			}
//		}
//	}
}