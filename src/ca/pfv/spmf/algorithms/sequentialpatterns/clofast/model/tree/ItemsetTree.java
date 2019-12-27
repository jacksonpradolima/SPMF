package ca.pfv.spmf.algorithms.sequentialpatterns.clofast.model.tree;

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
* An itemset tree 
* 
* @see AlgoFast 
*/
public class ItemsetTree {

    private ItemsetNode root = new ItemsetNode();

    /**
     * Adds new node into tree as child of parent
     *
     * @param parent   node parent of node to add
     * @param itemset
     * @param position position of new node into children list of parent node
     * @return
     */
    public ItemsetNode addChild(ItemsetNode parent, Itemset itemset, SparseIdList sil, int position) {
        ItemsetNode newNode = new ItemsetNode(itemset, parent, sil, position);
        parent.getChildren().add(newNode);
        return newNode;
    }

    public ItemsetNode getRoot() {
        return root;
    }

}
