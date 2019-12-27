package ca.pfv.spmf.algorithms.sequentialpatterns.clofast.model.tree;

import java.util.ArrayList;
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
* A closed itemset node (as used by CloFast)
* 
* @see AlgoCloFast 
*/
public class ClosedItemsetNode implements Comparable<ClosedItemsetNode> {

	/**
	 * represents the position of the treenode in the parent children list
	 */
	private int position = -1;
	private List<ClosedItemsetNode> children = new ArrayList<>();
	private ClosedItemsetNode parent;
	private Itemset itemset;
	private ItemsetNodeType type = ItemsetNodeType.toCheck;
	private SparseIdList sil;

	ClosedItemsetNode(){

	}

	/**
	 * @param parent
	 * @param itemset
	 * @param position
	 * @param sil
	 * by default it has toCheck as type
	 */
	ClosedItemsetNode(ClosedItemsetNode parent,Itemset itemset, SparseIdList sil, int position) {
		this.parent = parent; 
		this.position=position;
		this.itemset = itemset;
		this.sil = sil;
	}
	
	
	public List<ClosedItemsetNode> getChildren() {
		return children;
	}

	public ClosedItemsetNode getParent() {
		return parent;
	}

	public int getPosition() {
		return position;
	}

	public int getAbsoluteSupport() {
		return sil.getAbsoluteSupport();
	}
	
	public Itemset getItemset() {
		return itemset;
	}

	@Override
	public String toString() {
		return itemset.toString();
	}
	
	public ItemsetNodeType getType() {
		return type;
	}
	
	public void setType(ItemsetNodeType type) {
		this.type = type;
	}
	
	public SparseIdList getIdList() {
		return sil;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ClosedItemsetNode)) return false;

		ClosedItemsetNode that = (ClosedItemsetNode) o;

		if (itemset != null ? !itemset.equals(that.itemset) : that.itemset != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return itemset != null ? itemset.hashCode() : 0;
	}

	@Override
	public int compareTo(ClosedItemsetNode o) {
		return this.itemset.compareTo(o.itemset);
	}
}
