package ca.pfv.spmf.algorithms.frequentpatterns.sppgrowth;

import java.util.List;
/**
 * Copyright (c) 2019 Peng Yang, Philippe Fournier-Viger et al.

 * This file is part of the SPMF DATA MINING SOFTWARE *
 * (http://www.philippe-fournier-viger.com/spmf).
 * 
 * SPMF is free software: you can redistribute it and/or modify it under the *
 * terms of the GNU General Public License as published by the Free Software *
 * Foundation, either version 3 of the License, or (at your option) any later *
 * version. SPMF is distributed in the hope that it will be useful, but WITHOUT
 * ANY * WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * This is an implementation of an itemset used by the SPP-Growth algorithm.
 * 
 * @see AlgoSPPgrowth
 */
public class Itemset {
    /** the array of items **/
    public int[] itemset;

    /**  the support of this itemset */
    public int support = 0;

    /** the maxla of this itemset **/
    public int maxla = 0;


    /**
     * Get the items as array
     * @return the items
     */
    public int[] getItems() {
        return itemset;
    }

    /**
     * Constructor
     */
    public Itemset(){
        itemset = new int[]{};
    }

    /**
     * Constructor
     * @param item an item that should be added to the new itemset
     */
    public Itemset(int item){
        itemset = new int[]{item};
    }

    /**
     * Constructor
     * @param items an array of items that should be added to the new itemset
     */
    public Itemset(int [] items){
        this.itemset = items;
    }

    /**
     * Constructor
     * @param itemset a list of Integer representing items in the itemset
     * @param support the support of the itemset
     */
    public Itemset(List<Integer> itemset, int support, int maxla){
        this.itemset = new int[itemset.size()];
        int i = 0;
        for (Integer item : itemset) {
            this.itemset[i++] = item.intValue();
        }
        this.support = support;
        this.maxla = maxla;
    }

    public Itemset(int[] itemset, int support, int maxla){
        this.itemset = itemset;
        this.support = support;
        this.maxla = maxla;
    }

    /**
     * Get the support of this itemset
     */
    public int getAbsoluteSupport(){
        return support;
    }

    /**
     * Increase the support of this itemset by 1
     */
    public void increaseTransactionCount() {
        this.support++;
    }
}
