package ca.pfv.spmf.algorithms.sequentialpatterns.clofast.model.tree;

import java.util.ArrayList;
import java.util.List;

import ca.pfv.spmf.algorithms.sequentialpatterns.clofast.model.Itemset;
import ca.pfv.spmf.algorithms.sequentialpatterns.clofast.model.SparseIdList;
/* 
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
* An itemset node 
* 
* @see AlgoFast 
*/
public class ItemsetNode {

    /**
     * represents the position of the treenode in the parent children list
     */
    private int position;
    private List<ItemsetNode> children = new ArrayList<>();
    private ItemsetNode parent;
    private Itemset itemset;
    private SparseIdList sil;

    /**
     * generates new itemesetNode root
     */
    ItemsetNode() {
        position = -1;
    }

    ItemsetNode(Itemset itemset, ItemsetNode parent, SparseIdList sil, int position) {
        this.parent = parent;
        this.sil = sil;
        this.position = position;
        this.itemset = itemset;
    }


    public List<ItemsetNode> getChildren() {
        return children;
    }


    public ItemsetNode getParent() {
        return parent;
    }

    public int getPosition() {
        return position;
    }

    public Itemset getItemset() {
        return itemset;
    }

    public SparseIdList getSil() {
        return sil;
    }

    @Override
    public String toString() {
        return itemset.toString();
    }


}
