package ca.pfv.spmf.algorithms.frequentpatterns.MSAprioriSrinivas;
/*
 *  Copyright (c) 2008-2012 Philippe Fournier-Viger, Srinivas Paturu
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

 /**
 * This class is used to generate combinations from a list of items
 * up to a specified size.
 * 
 */

import java.util.Arrays;

public class CombinationGenerator {
	
	private int k;
	
	/**
	 * this function would return a list of all combinations up to size len from L
	 * @param L an input list of items 
	 * @param len the maximum size of combinations to be drawn from L
	 * @return 
	 */
	public int[][] generateCombinations(int[] L, int len)
	{
		int n = L.length, size=binomialCoeff(n,len);
		int[][] ans = new int[size][len];
		int[] res = new int[len];
		this.doCombine(L,res, 0, 0, len, ans);
		return ans;
	}
	
	/**
	 * this function would return a list of all combinations up to size len from L
	 * @param L an input list of items 
	 * @param len the maximum size of combinations to be drawn from L
	 * @return 
	 */
	public Integer[][] generateCombinations(Integer[] L, int len)
	{
		int n = L.length, size=binomialCoeff(n,len);
		Integer[][] ans = new Integer[size][len];
		Integer[] res = new Integer[len];
		this.doCombine(L,res, 0, 0, len, ans);
		return ans;
	}

	public CombinationGenerator()
	{
		this.k = 0;
	}
	
    private int binomialCoeff(int n, int k)
    {
        int res = 1;
        
        if (k>n - k)
            k = n - k;
     
        for (int i = 0; i < k; ++i)
        {
	        res *= (n - i);
	        res /= (i + 1);
        }
        return res;
    }

	private void doCombine(int[] arr, int[] res, int currIndex, int level, int r,
			                      int[][] ans)
	{
		if(level == r)
		{
			int[] copy = Arrays.copyOf(res, res.length);
			ans[k++] = copy;
			return;
		}
		for(int i=currIndex; i<arr.length; i++)
		{
			res[level] = arr[i];
			doCombine(arr, res, i+1, level+1, r, ans);
		}
	}

	private void doCombine(Integer[] arr, Integer[] res, int currIndex, int level, int r, Integer[][] ans) 
	{
		if (level == r) {
			Integer[] copy = Arrays.copyOf(res, res.length);
			ans[k++] = copy;
			return;
		}
		for (int i = currIndex; i < arr.length; i++) {
			res[level] = arr[i];
			doCombine(arr, res, i + 1, level + 1, r, ans);
		}
	}
}
