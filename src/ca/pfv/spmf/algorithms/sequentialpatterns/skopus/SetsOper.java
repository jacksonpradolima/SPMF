package ca.pfv.spmf.algorithms.sequentialpatterns.skopus;
///*******************************************************************************
// * Copyright (C) 2015 Tao Li
// * 
// * This file is part of Skopus.
// * 
// * Skopus is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, version 3 of the License.
// * 
// * Skopus is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// * 
// * You should have received a copy of the GNU General Public License
// * along with Skopus.  If not, see <http://www.gnu.org/licenses/>.
// ******************************************************************************/
import java.util.Collections;

public class SetsOper {
	
	
	// find the intersection of two sidsets: parameter s1 and s2
	// relies on the sets both being stored in ascending order
	// get the results where the position in s1 is less than s2 in the same sequence
	public static int intersection(SidSet result, SidSet s1, SidSet s2)
	{
		result.clear();
		result.ensureCapacity(s1.size() < s2.size() ? s1.size() : s2.size());

		if (s1.size() == 0 || s2.size() == 0) {
			return 0;
		}

		int it1 = 0;
		int it2 = 0;
		final int end1 = s1.size();
		final int end2 = s2.size();
		
		Sid v1 = s1.get(0);
		Sid v2 = s2.get(0);

		while (true) {
			if (v1.isEqual(v2)) {
				int pos = v1.getNextPosition(v2);
				if (pos >= 0)
				{
//					Sid v = new Sid(v1.getSidNumber());
//					v.add(pos);
					Sid v = new Sid(v1.getSidNumber(), pos);
					result.add(v);
				}

				it1++;
				if (it1 >= end1) break;
				v1 = s1.get(it1);
				it2++;
				if (it2 >= end2) break;
				v2 = s2.get(it2);
			}
			else if (v1.lessThan(v2)) {
				it1++;
				if (it1 >= end1) break;
				v1 = s1.get(it1);
			}
			else {
				it2++;
				if (it2 >= end2) break;
				v2 = s2.get(it2);
			}
		}//while (true) 

		return result.size();
	}

	// find the result sResult SID that is covers, return the size of sResult
	public static int getCoverAndCount(ItemsetRec is, SidSet sResult) 
	{
		int nResult = 0;
		sResult.clear();
		if (is.size() < 1)
		{
			nResult = 0;
			sResult.clear();
		}
		else if (is.size() == 1)
		{
			sResult = GlobalData.alSids.get(is.get(0));
			nResult = sResult.size();
		}
		else
		{
			SidSet r = new SidSet();
			r.copyFrom(GlobalData.alSids.get(is.get(0)));
			for (int i = 1; i < is.size(); i++)
			{
				SidSet temp = new SidSet();
				nResult = intersection(temp, r, GlobalData.alSids.get(is.get(i)));
				if (nResult < 1)
				{
					sResult.clear();
					break;
				}
				r.copyFrom(temp);
				sResult.copyFrom(temp);
			}//for (int i = 1; i < is.size(); i++)
		}//else

		is.count = nResult;
		return nResult;
	}

	// destructively update s1 to its intersection with s2
	public static void dintersection(SidSet s1, SidSet s2)
	{
		SidSet result = new SidSet(s1.size() < s2.size() ? s1.size() : s2.size());
		intersection(result, s1, s2);
		s1.copyFrom(result);
		return;
	}

	
	// get the result s3 of  s1 union with s2
	@SuppressWarnings("unchecked")
	public static SidSet getUnion(SidSet s1, SidSet s2)
	{
		SidSet s3 = new SidSet(s1.size() + s2.size());

		Collections.sort(s1, new SidSortByNumber());
		Collections.sort(s2, new SidSortByNumber());

		int it1 = 0;
		int it2 = 0;
		
		final int end1 = s1.size();
		final int end2 = s2.size();

		while (true) {
			if (it1 >= end1 -1) {
				while (it2 < end2) {
					s3.add(s2.get(it2));
					it2++;
				}
				break;
			}
			else if (it2 >= end2-1) {
				while (it1 <= end1-1) {
					s3.add(s1.get(it1));
					it1++;
				}
				break;
			}
			else if (s1.get(it1).isEqual(s2.get(it2))) {
				s3.add(s1.get(it1));
				it1++;
				it2++;
			}
			else if (s1.get(it1).lessThan(s2.get(it2))) {
				s3.add(s1.get(it1));
				it1++;
			}
			else {
				s3.add(s2.get(it2));
				it2++;
			}
		}
		return s3;

	}


	// destructively update s1 to its union with s2
	public static void dUnion(SidSet s1, SidSet s2)
	{
		SidSet result = getUnion(s1, s2);
		s1.copyFrom(result);
		return;
	}

	// get the result s3 of  s1 intersection with s2
	public static SidSet getIntersection(SidSet s1, SidSet s2)
	{
		SidSet s3 = new SidSet(s1.size() < s2.size() ? s1.size() : s2.size());

		int it1 = 0;
		int it2 = 0;
		
		final int end1 = s1.size();
		final int end2 = s2.size();
		
		while (true)
		{
			if (it1 >= end1) {
				break;
			}
			else if (it2 >= end2) {
				break;
			}
			else if (s1.get(it1).isEqual(s2.get(it2))){
				s3.add(s1.get(it1));
				it1++;
				it2++;
			}
			else if (s1.get(it1).lessThan(s2.get(it2))) {
				it1++;
			}
			else {
				it2++;
			}
		}

		return s3;
	}




}
