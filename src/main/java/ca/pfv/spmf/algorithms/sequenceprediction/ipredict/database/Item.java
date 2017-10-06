package ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database;
/*
 * This file is copyright (c) Ted Gueniche 
 * <ted.gueniche@gmail.com>
 *
 * This file is part of the IPredict project
 * (https://github.com/tedgueniche/IPredict).
 *
 * IPredict is distributed under The MIT License (MIT).
 * You may obtain a copy of the License at
 * https://opensource.org/licenses/MIT 
 */
public class Item implements Comparable<Item> {

	public Integer val;
	
	public Item(Integer value) {
		val = value;
	}
	
	@Override
	public Item clone() {
		return new Item(val);
	}
	
	public Item() {
		val = -1;
	}
		
	public String toString() {
		return val.toString();
	}
	
	public int hashCode() {
		return val.hashCode();
	}
	

	public boolean equals(Item b) {
		return val.equals(b.val);
	}
	
	@Override
	public boolean equals(Object obj) {
		Item b = (Item) obj;
		return val.equals(b.val);
	};

	@Override
	public int compareTo(Item o) {
		return this.val.compareTo(o.val);
	}

}
