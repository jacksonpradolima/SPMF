package ca.pfv.spmf.algorithms.frequentpatterns.fhmds.naive;


/* This file is copyright (c) 2018+  by Siddharth Dawar et al.
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
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 */
import java.util.ArrayList;
import java.util.List;

public class Batch {

	int bid;
	float sum_batch_iutils=0;
	float sum_batch_rutils=0;
	List<Element> elements = new ArrayList<Element>();
	/**
	 * Constructor.
	 * @param bid  the batch id
	 * @param iutils  the itemset utility
	 * @param rutils  the remaining utility
	 */
	public Batch(int bid, float iutils, float rutils){
		this.bid = bid;
		this.sum_batch_iutils = iutils;
		this.sum_batch_rutils = rutils;
	}
}
