package ca.pfv.spmf.algorithms.sequentialpatterns.qcsp.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

public class ListMap<K,V> {

	private Map<K,List<V>> map = new HashMap<K,List<V>>();
	
	public void put(K key, V value){
		List<V> lst = map.get(key);
		if(lst == null){
			lst = new ArrayList<V>();
			map.put(key, lst);
		}
		lst.add(value);
	}
	
	public void putAll(K key, List<V> values){
		List<V> lst = map.get(key);
		if(lst == null){
			lst = values;
			map.put(key, lst);
		}
		lst.addAll(values);
	}
	
	public void putList(K key, List<V> lst){
		map.put(key, lst);
	}
	
	
	public void remove(K key){
		map.remove(key);
	}
	
	public List<V> get(K key){
		return map.get(key);
	}
	
	public Set<K> keySet(){
		return map.keySet();
	}

	public Set<Entry<K,List<V>>> entrySet() {
		return map.entrySet();
	}
	
	public String toString(){
		return map.toString();
	}
}
