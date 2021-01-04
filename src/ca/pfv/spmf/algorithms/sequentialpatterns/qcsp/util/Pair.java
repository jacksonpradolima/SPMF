package ca.pfv.spmf.algorithms.sequentialpatterns.qcsp.util;

/**
 * This is an implementation of the QCSP algorithm.
 * For more information please refer the paper Mining Top-K Quantile-based Cohesive Sequential Patterns 
 * by Len Feremans, Boris Cule and Bart Goethals, published in 2018 SIAM International Conference on Data Mining (SDM18).<br/>
 *
 * Copyright (c) 2020 Len Feremans (Universiteit Antwerpen)
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
 *
 * You should have received a copy of the GNU General Public License along wit
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Len Feremans
 */

@SuppressWarnings("rawtypes")
public final class Pair<F, S>{
    private F first; 
    private S second;

    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }
    
    public final void setFirst(F first) {
        this.first = first;
    }

    public final void setSecond(S second) {
        this.second = second;
    }

    public final F getFirst() {
        return first;
    }

    public final S getSecond() {
        return second;
    }
    
    @Override
    public final String toString()
    {
    	return String.format("%s:%s", first, second);
    }

	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		return result;
	}

	@Override
	public final boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pair other = (Pair) obj;
		if (first == null) {
			if (other.first != null)
				return false;
		} else if (!first.equals(other.first))
			return false;
		if (second == null) {
			if (other.second != null)
				return false;
		} else if (!second.equals(other.second))
			return false;
		return true;
	}

	
    
    /*
    @SuppressWarnings("unchecked")
	@Override
    public int compareTo(Pair<F,S> otherPair)
    {
    	return first.compareTo(otherPair.first);
    }
	*/
}