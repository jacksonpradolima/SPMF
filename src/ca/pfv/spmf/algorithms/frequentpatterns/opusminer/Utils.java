package ca.pfv.spmf.algorithms.frequentpatterns.opusminer;

import java.util.Iterator;

/**
 * This is the Utils class used by the Opus-Miner algorithm proposed in : </br></br>
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

public class Utils {


	// true iff s1 is a subset of s2
	// assumes that sets are stored in ascending order
	public static <Type> boolean subset(itemsetRec s1, itemsetRec s2)
	{
		Iterator<Integer> it1 = s1.iterator();
		Iterator<Integer> it2 = s2.iterator();
		
		Integer val1 = it1.next();
		Integer val2 = it2.next();
	  int i = s1.size();
	  int j = s2.size();
	  while (i>0)
	  {
		
		if (val1 < val2)
		{
			return false;
		}
		if (val1.equals(val2))
		{
			if(i == 1)
			{
				break;
			}
			val1 = it1.next();
			i--;
		}
		if(j == 1)
		{
			return false;
		}
		val2 = it2.next();
		j--;
		
	  }
	  return true;
	}

	// get the tidset for an itemset
	public static tidset gettids(itemset is, tidset t)
	{
	
	  assert is.size() > 0;
	  tidset res = new tidset();
	  Iterator<Integer> it = is.iterator();

	  if (is.size() == 1)
	  {
		t = Global.tids.get(it.next());
		return t;
	  }
	  else
	  {
		final Integer item1 = it.next();
		final Integer item2 = it.next();
		
		tidset s1 = new tidset();
		tidset s2 = new tidset();
		
		s1 = Global.tids.get(item1);
		s2 = Global.tids.get(item2);
		
		tidset.intersection(res,s1, s2);

		while (it.hasNext())
		{
			tidset s = new tidset();
			s = Global.tids.get(it.next());
			tidset.dintersection(res, s);
		}
	  }
	  
	  return res;
	  
	}

	public static float countToSup(int count) {
		return count / (float) Global.noOfTransactions;
	}

	public static int getNum(String string)
	{
	  int result = 0;
//	  while (*str >= (byte)'0' && *str <= (byte)'9')
//	  {
//		result = result * 10 + *str ++ - '0';
//	  }

	  return string.hashCode();
	}

	public static float itemSup(Integer item) {
		return countToSup(Global.tids.get(item).size());
	}

	// return the result of a fFisher exact test for an itemset i with support
	// count count relative to support counts count1 and count2 for two subsets
	// s1 and s2 that form a partition of i
	public static double fisher(int count, int count1, int count2) {
		return FisherTest.fisherTest(Global.noOfTransactions - count1 - count2 + count, count1 - count, count2 - count, count);
	}

}