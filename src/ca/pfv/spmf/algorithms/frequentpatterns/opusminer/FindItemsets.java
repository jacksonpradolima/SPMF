package ca.pfv.spmf.algorithms.frequentpatterns.opusminer;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ca.pfv.spmf.tools.MemoryLogger;

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
/* find_itemsets.h - header file for the find_itemsets.cpp module of OPUS
 * Miner. Copyright (C) 2012 Geoffrey I Webb
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

public class FindItemsets {

	public static class RedundantAprioriFlags {
		boolean redundant = false;
		boolean apriori = false;
	}
	
	public static class ValP{
      float val;
      double p;
	}
	
	public static class SoFarRemaining{
		itemset sofar = null;
		itemset remaining = null;
	}

	public static void find_itemsets() {
		itemQClass q = new itemQClass(); // a queue of items, to be sorted on an upper bound on value
		int i;

		// initalise q - the queue of items ordered on an upper bound on value
		for (i = 1; i <= Global.noOfItems; i++) {
			final int c = Global.tids.get(i).size();

			final float sup = Utils.countToSup(c);
			final float ubVal = Global.searchByLift ? 1.0f / sup : sup - sup * sup;

			// make sure that the support is high enough for it to be possible
			// to create a significant itemset
			if (Utils.fisher(c, c, c) <= Global.getAlpha(2)) {
				q.append(ubVal, i); // it is faster to sort the q once it is 
				// full rather than doing an insertion sort
			}
		}

		itemQClass newq = new itemQClass(); // this is the queue of items that
											// will be available for the item
											// currently being explored

		if (q.size() > 0) {
			
			Collections.sort(q, new Comparator<ItemQElement>() {

				@Override
				public int compare(ItemQElement i1, ItemQElement i2) {
					float val = (i2.ubVal - i1.ubVal);
					if(val > 0){
						return 1;
					}
					if(val < 0){
						return -1;
					}
					return 0;            // CHECK IF OK...
				}
			});
			
			// the STL vector 'insert' method in Java:
			newq.insert(q.get(0).ubVal, q.get(0).item); // the first item will
														// have no previous
														// items with which to
														// be paired so is
														// simply added to the
														// queue of availabile
														// items
		}

		float prevMinVal = minValue; // remember the current minValue, and
										// output an update if it improves in
										// this iteration of the loop

		itemsetRec is = new itemsetRec();

		// we are stepping through all associations of i with j<i, so the first
		// value of i that will have effect is 1
		for (i = 1; i < q.size() && q.get(i).ubVal > minValue; i++) {
			final Integer item = q.get(i).item;

			is.clear();
			is.add(item);

			CoverIsQ coverIsQ = new CoverIsQ();
			coverIsQ.is =(itemsetRec) is.clone() ;
			coverIsQ.cover = (tidset) Global.tids.get(item).clone();
			coverIsQ.q = (itemQClass) newq.clone();
			opus(coverIsQ, Global.tids.get(item).size());

			newq.append(q.get(i).ubVal, item);

			if (prevMinVal < minValue) {
				System.out.printf("<%f>", minValue);
				prevMinVal = minValue;
			} else {
				System.out.print('.');
			}
		}

		System.out.print('\n');
	}

	// the minimum leverage of an itemset in the top-k so far
	// any itemset whose leverage does not exceed this value cannot enter the
	// top-k
	public static float minValue = -Float.MAX_VALUE;

	/** for each itemset explored for which supersets might be in the best k, keep the count */
	public static Map<itemset, Integer> TIDCount = new HashMap<itemset, Integer>();

	// / #endif

	/**
	 *  Get the support of an itemset stored in the map or null
	 *  if the support of the itemset was not memorized.
	 * @param is
	 * @param count the support or null if the itemset was not memorized
	 * @return the count
	 */
	public static Integer getTIDCount(itemset is)
	{
	  // if the itemset contains a single item
	  if (is.size() == 1)
	  {
		// the count is the tidset of that item
		return Global.tids.get(is.first()).size();
	  }
	  else
	  {
		return TIDCount.get(is);
	  }
	}





	public static void checkImmediateSubsets(itemset is, int isCnt, RedundantAprioriFlags flags)
	{
	  itemset subset = new itemset();
	  subset.addAll(is);  // MAKE SURE THIS IS CORRECT /////////////////
	  
	  Iterator<Integer> it = is.iterator();

	  flags.redundant = false;
	  flags.apriori = false;
	  
	  while (it.hasNext()) {
		Integer currentIt = (Integer) it.next();
		
		subset.remove(currentIt);

		Integer subsetCnt = getTIDCount(subset);
		if (subsetCnt == null)
		{
			flags.redundant = true;
			flags.apriori = true;
			return;
		}

		if (Global.redundancyTests && subsetCnt.equals(isCnt))
		{
			flags.redundant = true;
		}

		subset.add(currentIt);
	  }

	  return;
	}

	// calculates leverage, p, whether the itemset is is redundant and whether
	// it is possible to determine that all supersets of is will be redundant
	// return true iff is is not redundant, val > minValue and p <= alpha
	public static boolean checkSubsetsX(SoFarRemaining sfRemaining, Integer limit, int cnt, float new_sup, ValP valP, double alpha)
	{
	  Integer sofarCnt = getTIDCount(sfRemaining.sofar);
	  Integer remainingCnt = getTIDCount(sfRemaining.remaining);

	  if (sofarCnt == null || remainingCnt == null)
	  {
		return false;
	  }

	  // do test for sofar against remaining
	  final float this_val = Global.searchByLift ? new_sup / 
			  (Utils.countToSup(remainingCnt) * Utils.countToSup(sofarCnt)) 
			  : new_sup - Utils.countToSup(remainingCnt) * Utils.countToSup(sofarCnt);

	  if (this_val < valP.val)
	  {
		valP.val = this_val;
		if (this_val <= minValue)
		{
			return false;
		}
	  }

	  final double this_p = Utils.fisher(cnt, sofarCnt, remainingCnt);

	  if (this_p > valP.p)
	  {
		valP.p = this_p;
		if(valP.p > alpha)
		{
		  return false;
		}
	  }


	  if (sfRemaining.remaining.size() > 1)
	  {
		itemset new_remaining = new itemset();
		new_remaining.addAll(sfRemaining.remaining);

		Iterator<Integer> it =  sfRemaining.remaining.iterator();
		
//		for (it = remaining.iterator(); it != remaining.end() && *it < limit; it++)
				
		while(it.hasNext()){
//		for (Iterator iterator = new_remaining.iterator(); iterator.hasNext();) {
			Integer currentIt = (Integer) it.next();
			if(currentIt >= limit){
				break;
			}

			sfRemaining.sofar.add(currentIt);
			new_remaining.remove(currentIt);

			SoFarRemaining newSfRemaining = new SoFarRemaining();
			newSfRemaining.sofar = sfRemaining.sofar;
			newSfRemaining.remaining = new_remaining;
			
		  if (!checkSubsetsX(newSfRemaining, currentIt, cnt, new_sup, valP, alpha))
		  {
			return false;
		  }

		  sfRemaining.sofar.remove(currentIt);
		  new_remaining.add(currentIt);
		}
	  }

	  return valP.p <= alpha  && valP.val > minValue;
	}

	// calculates leverage, p, whether is is redundant and whether it is
	// possible to determine that all supersets of is will be redundant
	// return true iff is is not redundant, val > minValue and p <= alpha
	public static boolean checkSubsets(Integer item, itemset is, int cnt, float new_sup, 
			int parentCnt, float parentSup, ValP valP, double alpha)
	{
	  assert is.size() > 1;

	  // do test for new item against the rest
	  final int itemCnt = Global.tids.get(item).size();

	  valP.val = Global.searchByLift ? new_sup / (parentSup * Utils.itemSup(item)) 
			  : new_sup - parentSup * Utils.itemSup(item);

	  if (valP.val <= minValue)
	  {
		  return false;
	  }

	  valP.p = Utils.fisher(cnt, itemCnt, parentCnt);

	  if (valP.p > alpha)
	  {
		  return false;
	  }

	  if (is.size() > 2)
	  {
		SoFarRemaining sfRemaining = new SoFarRemaining();
		sfRemaining.remaining = new itemset();
		sfRemaining.remaining.addAll(is);
		sfRemaining.remaining.remove(item);

		sfRemaining.sofar = new itemset();
		sfRemaining.sofar.add(item);
		

		Iterator<Integer> it = is.iterator();
		
		while (it.hasNext()) {
		  Integer currentIt = (Integer) it.next();

		  if (currentIt.equals(item) == false)
		  {
			  sfRemaining.sofar.add(currentIt);
			  sfRemaining.remaining.remove(currentIt);

			if (!checkSubsetsX(sfRemaining, currentIt, cnt, new_sup, valP, alpha))
			{
			  return false;
			}

			sfRemaining.sofar.remove(currentIt);
			sfRemaining.remaining.add(currentIt);
		  }
		}
	  }

	  return valP.p <= alpha && valP.val > minValue;
	}

	// insert is into the collection of k best itemsets
	public static void insert_itemset(itemsetRec is) {
		if (AlgoOpusMiner.itemsets.size() >= Global.k) {
			AlgoOpusMiner.itemsets.poll();
		}
		itemsetRec iss = new itemsetRec();
		
		iss = (itemsetRec) is.clone();
		
		
		AlgoOpusMiner.itemsets.add(iss);
		if (AlgoOpusMiner.itemsets.size() == Global.k) {
			final float newMin = AlgoOpusMiner.itemsets.peek().value;  // MAKE SURE THIS IS CORRECT
			
//			while(OpusMiner.itemsets.size() > 0 ){
//				itemsetRec itemset = OpusMiner.itemsets.poll();
//				System.out.println(" "  + itemset + " " + itemset.value);
//			}
			
			if (newMin > minValue) {
				minValue = newMin;
			}
		}
	}

	// perform OPUS search for specialisations of is (which covers cover) using
	// the candidates in queue q
	// maxItemSup is the maximum of the supports of all individual items in is
	
	//  opus(is, newCover, newQ, newMaxItemCount);
    //opus(is, tids[item], newq, tids[item].size());
	//
	// void opus(itemsetRec &is, 
	//	         tidset &cover, 
	//			itemQClass &q, 
	// 			const int maxItemCount) 
	
	public static class CoverIsQ{
		itemsetRec is = null;
		tidset cover = null;
		itemQClass q = null;
	}
	 
	public static void opus(CoverIsQ coverIsQ, int maxItemCount)
	{
		
	  int i;
	  final float parentSup = Utils.countToSup(coverIsQ.cover.size());
	  final int depth = coverIsQ.is.size() + 1;

	  tidset newCover = new tidset();
	  itemQClass newQ = new itemQClass();

	  for (i = 0; i < coverIsQ.q.size(); i++)
	  {
		final Integer item = coverIsQ.q.get(i).item;
		int count;

		// determine the number of TIDs that the new itemset covers
		newCover.intersection(newCover, coverIsQ.cover, Global.tids.get(item));
		count = newCover.size();

		final int newMaxItemCount = Math.max(maxItemCount, (int)(Global.tids.get(item).size()));
		final float new_sup = Utils.countToSup(count);

		// this is a lower bound on the p value that may be obtained for this itemset or any superset
		final double lb_p = Utils.fisher(count, newMaxItemCount, count);

		// calculate an upper bound on the value that can be obtained by this itemset or any superset

		final float ubval = Global.searchByLift ? ((count == 0) ? 0.0f : (1.0f / Utils.countToSup(maxItemCount))) : new_sup - new_sup * Utils.countToSup(maxItemCount);

		// performing OPUS pruning - if this test fails, the item will not be included in any superset of is
		if (lb_p <= Global.getAlpha(depth) && ubval > minValue)  
		{
		  // only continue if there is any possibility of this itemset or its supersets entering the list of best itemsets

			coverIsQ.is.add(item);

		  RedundantAprioriFlags flags = new RedundantAprioriFlags();
		  checkImmediateSubsets(coverIsQ.is, count, flags);

		  if (!flags.apriori)
		  {
			  ValP valP = new ValP();
			  if (checkSubsets(item, coverIsQ.is, count, new_sup, coverIsQ.cover.size(), parentSup, valP, Global.getAlpha(depth)))
			  {
				  coverIsQ.is.count = count;
				  coverIsQ.is.value = valP.val;
				  coverIsQ.is.p = valP.p;
				  insert_itemset(coverIsQ.is);
			  }

			  // performing OPUS pruning - if this test fails, the item will not be included in any superset of is
			  if (!flags.redundant)
			  {
				  itemsetRec iss ;
				  iss = (itemsetRec) coverIsQ.is.clone();
				  TIDCount.put(iss, count); 

				  if (!newQ.isEmpty())
				  {
					  	// there are only more nodes to expand if there is a queue of items to consider expanding it with
						CoverIsQ newCoverIsQ = new CoverIsQ();
						newCoverIsQ.is = (itemsetRec) coverIsQ.is.clone();
						newCoverIsQ.cover = (tidset) newCover.clone();
						newCoverIsQ.q = (itemQClass) newQ.clone();
						opus(newCoverIsQ, newMaxItemCount);
				  }

				  newQ.insert(ubval, item);
			}
		  }

		  coverIsQ.is.remove(item);
		}
	  }
	  
	  MemoryLogger.getInstance().checkMemory();
	}
}