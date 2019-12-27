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
 * This is an implementation of a utility list as used by LHUI-Miner.
 * 
 * @author Yimin Zhang, Philippe Fournier-Viger
 * @see AlgoLHUIMiner
 * @see AlgoPHUIMiner
 */
public class UtilityListLHUI extends UtilityList {

	/**
	 * Constructor
	 * 
	 * @param item
	 *            an item
	 */
	public UtilityListLHUI(Integer item) {
		super(item);
	}

	/**
	 * to store period that (iutitl)utility of the itemset is higher than
	 * threshold
	 */
	ArrayList<Period> iutilPeriod = new ArrayList<Period>();

	/** to store period that (iutil+rUtil) is higher */
	ArrayList<Period> utilPeriod = new ArrayList<Period>();

}
