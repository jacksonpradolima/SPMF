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
public class Triple<T1,T2,T3> {
	private T1 first;
	private T2 second;
	private T3 thirth;
	
	public Triple(){}
	
	public Triple(T1 first, T2 second, T3 thirth) {
		super();
		this.first = first;
		this.second = second;
		this.thirth = thirth;
	}
	public T1 getFirst() {
		return first;
	}
	public void setFirst(T1 first) {
		this.first = first;
	}
	public T2 getSecond() {
		return second;
	}
	public void setSecond(T2 second) {
		this.second = second;
	}
	public T3 getThirth() {
		return thirth;
	}
	public void setThirth(T3 thirth) {
		this.thirth = thirth;
	}
	
	
	
}
