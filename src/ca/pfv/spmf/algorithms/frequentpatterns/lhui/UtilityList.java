package ca.pfv.spmf.algorithms.frequentpatterns.lhui;

import java.util.ArrayList;

/* This file is copyright (c) 2018  Yimin Zhang, Philippe Fournier-Viger
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
/**
 * This is an implementation of a utility list as used by LHUI-MINER and
 * PHUI-Miner.
 * 
 * @author Yimin Zhang, Philippe Fournier-Viger
 * @see AlgoLHUIMiner
 * @see AlgoPHUIMiner
 */
public class UtilityList {

	/** an item */
	Integer item;

	/** sum of all iutil of all elements in utilityList */
	long sumIutils;

	/** sum of all rutil of all elements in utilityList */
	long sumRutils;

	/** elements stored in the list */
	ArrayList<Element> elements = new ArrayList<Element>();

	/**
	 * Constructor
	 * 
	 * @param item
	 *            an item
	 */
	public UtilityList(Integer item) {
		this.item = item;
	}

	/**
	 * add element to the UtilityList
	 * 
	 * @param e
	 *            element that is added to UtilityList
	 */
	public void addElement(Element e) {
		sumIutils += e.iutils;
		sumRutils += e.rutils;
		elements.add(e);
	}

}
