package ca.pfv.spmf.algorithms.sequentialpatterns.clofast.model.tree;

import java.util.LinkedList;
import java.util.List;

import ca.pfv.spmf.algorithms.sequentialpatterns.clofast.model.Sequence;
import ca.pfv.spmf.algorithms.sequentialpatterns.clofast.model.VerticalIdList;
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
* A closed sequence node (as used by CloFast)
* 
* @see AlgoCloFast 
*/
public class ClosedSequenceNode {

	/**
	 * represents the position of the treenode in the parent children list
	 */
	private VerticalIdList vil;
	private List<ClosedSequenceNode> children = new LinkedList<>();
	private ClosedSequenceNode parent;
	private Sequence sequence;
	private NodeType type = NodeType.toCheck;
	private int absoluteSupport;

	/**
	 * For SequenceNode root
	 * 
	 * @param sizePositionList
	 */
	ClosedSequenceNode(int sizePositionList) {
		sequence = new Sequence();
		this.absoluteSupport = sizePositionList;
	}

	ClosedSequenceNode(ClosedSequenceNode parent, Sequence sequence, VerticalIdList vil, int absoluteSupport) {
		this.vil = vil;
		this.parent = parent;
		this.sequence = sequence;
		this.absoluteSupport = absoluteSupport;
	}

	public List<ClosedSequenceNode> getChildren() {
		return children;
	}

	public ClosedSequenceNode getParent() {
		return parent;
	}

	public VerticalIdList getVerticalIdList(){
		return vil;
	}

	public Sequence getSequence() {
		return sequence;
	}

	public NodeType getType() {
		return type;
	}

	public void setType(NodeType type) {
		this.type = type;
	}

	public int getAbsoluteSupport() {
		return absoluteSupport;
	}

	@Override
	public String toString() {
		return sequence.toString() + " #SUP: " + this.absoluteSupport;
	}




	public boolean containsLastItemset(ClosedSequenceNode n) {
		if (sequence.getLastItemset().equals(n.sequence.getLastItemset()))
			return false;
		
		return sequence.getLastItemset().contains(n.getSequence().getLastItemset());
	}
}