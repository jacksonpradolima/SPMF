package ca.pfv.spmf.algorithms.frequentpatterns.opusminer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * This is the Tidset class used by the Opus-Miner algorithm proposed in : </br></br>
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
/* TidSet.h - header file for the TidSet.cpp module of OPUS Miner.
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

// store tids as an ordered vector of ints

public class tidset extends ArrayList<Integer> {

	/** Serial UID */
	private static final long serialVersionUID = 1L;

	public tidset() {

	}

	public static int countIntersection(tidset s1, tidset s2) {
		// count the size of the intersection
		// relies on the sets both being stored in ascending order

		if (s1.size() == 0 || s2.size() == 0) {
			return 0;
		}

		Iterator<Integer> it1 = s1.iterator();
		Integer v1 = it1.next();
		Iterator<Integer> it2 = s2.iterator();
		Integer v2 = it2.next();

		int count = 0;

		while (true) {
			if (v1.intValue() == v2.intValue()) {
				count++;
				if (it1.hasNext()  == false) {
					break;
				}
				v1 = it1.next();

				if (it2.hasNext()  == false) {
					break;
				}
				v2 = it2.next();
			} else if (v1.intValue()< v2.intValue()) {
				if (it1.hasNext()  == false) {
					break;
				}
				v1 = it1.next();
			} else {
				if (it2.hasNext()  == false) {
					break;
				}
				v2 = it2.next();
			}
		}

		return count;
	}

	// find the intersection of two TidSets
	// relies on the sets both being stored in ascending order
	public static void intersection(tidset result, tidset s1, tidset s2) {
				
		result.ensureCapacity(Math.min(s1.size(), s2.size()));
		result.clear();

		if (s1.size() == 0 || s2.size() == 0) {
			return;
		}

		Iterator<Integer> it1 = s1.iterator();
		Iterator<Integer> it2 = s2.iterator();
		
		
		Integer v1 = it1.next();	
		Integer v2 = it2.next();

		while (true) {
			if (v1.intValue() == v2.intValue() ) {
				result.add(v1);
				if (it1.hasNext()  == false) {
					break;
				}
				v1 = it1.next();

				if (it2.hasNext()  == false) {
					break;
				}
				v2 = it2.next();
			} else if (v1.intValue()  < v2.intValue() ) {
//				result.add(v1);
				if (it1.hasNext()  == false) {
					break;
				}
				v1 = it1.next();
			} else {

				if (it2.hasNext() == false) {
					break;
				}
				v2 = it2.next();
			}
		}
	}

	// destructively update s1 to its intersection with s2
	public static void dintersection(tidset s1, tidset s2)
	{
		if (s1.size() == 0) 
		{
			return;
		}

		if (s2.size() == 0) 
		{
			s1.clear();
			return;
		}

		int from = 0;
		int to = 0;
		
		Iterator<Integer> it1 = s1.iterator();
		Integer v1 = it1.next();
		Iterator<Integer> it2 = s2.iterator();
		Integer v2 = it2.next();


		while (true) {
			if (v1.intValue() == v2.intValue())
			{
				s1.set(to++, s1.get(from++));
				if (from  == s1.size())
				{
					break;
				}
				v1 = it1.next();
				
				if (it2.hasNext()  == false)
				{
					break;
				}
				v2 = it2.next();
			} else if (v1.intValue() < v2.intValue())
			{
				from++;
				if (from == s1.size())
				{
					break;
				}
				v1 = it1.next();
			} else {
				if (it2.hasNext()  == false) 
				{
					break;
				}
				v2 = it2.next();
			}
		}

		resize(s1, to);
	}
	
	public static <T> void resize(ArrayList<T> list, int newSize)
	{
		T value = null;

		if (list.size() > newSize)
		{
			for (int i = list.size() - 1; i >= newSize; i--)
			{
				list.remove(i);
			}
		}
		else if (list.size() < newSize)
		{
			for (int i = list.size(); i < newSize; i++)
			{
				list.add(value);
			}
		}
	}

	// destructively update s1 to its union with s2
	public static tidset dunion(tidset s1, tidset s2)
	{
		tidset result = new tidset();
		result.addAll(s1);
		for (Integer tid : s2) 
		{
			if (result.contains(tid) == false) 
			{
				result.add(tid);
			}
		}
		Collections.sort(result);
		return result;
	}

}
