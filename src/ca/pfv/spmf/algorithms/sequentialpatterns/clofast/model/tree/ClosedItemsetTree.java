package ca.pfv.spmf.algorithms.sequentialpatterns.clofast.model.tree;

import java.util.HashMap;
import java.util.List;

import ca.pfv.spmf.algorithms.sequentialpatterns.clofast.AlgoCloFast;
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
* A closed itemset tree (as used by CloFast)
* 
* @see AlgoCloFast 
*/
public class ClosedItemsetTree {

	private ClosedItemsetNode root;

	private HashMap<Integer, List<Itemset>> closedTable;

	public ClosedItemsetTree() {
		root = new ClosedItemsetNode();
		closedTable = new HashMap<Integer, List<Itemset>>();
	}

	/**
	 *
	 * @param parent
	 * @param itemset
	 * @param position
	 * @param sil
	 * @return
	 */
	public ClosedItemsetNode addChild(ClosedItemsetNode parent,Itemset itemset,SparseIdList sil, int position) {
		ClosedItemsetNode newNode = new ClosedItemsetNode(parent,itemset, sil, position);
		parent.getChildren().add(newNode);
		return newNode;
	}


	public ClosedItemsetNode getRoot() {
		return root;
	}
}
