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


public class Combination {
	private int n, r;
	private int[] index;
	private boolean hasNext = true;

	public Combination(int n, int r) {
		this.n = n;
		this.r = r;
		index = new int[r];
		for (int i = 0; i < r; i++)
			index[i] = i;
	}

	public boolean hasNext() {
		return hasNext;
	}

	private void moveIndex() {
		int i = rightmostIndexBelowMax();
		if (i >= 0) {
			index[i] = index[i] + 1;
			for (int j = i + 1; j < r; j++)
				index[j] = index[j - 1] + 1;
		} else
			hasNext = false;
	}

	public int[] next() {
		if (!hasNext)
			return null;
		int[] result = new int[r];
		for (int i = 0; i < r; i++)
			result[i] = index[i];
		moveIndex();
		return result;
	}

	private int rightmostIndexBelowMax() {
		for (int i = r - 1; i >= 0; i--)
			if (index[i] < n - r + i)
				return i;
		return -1;
	}
}



