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
 * This class represents a sequence node
 * @author 
 *
 */
public class SequenceNode {

	/**
	 * represents the position of the treenode in the parent children list
	 */
	private VerticalIdList verticalIdList;
	private List<SequenceNode> children = new LinkedList<>();
	private SequenceNode parent;
	private Sequence sequence;
	private int absSupport;

	/**
	 *
	 * @param verticalIdList
	 * @param sequence
	 * @param parent
	 * @param absSupport
	 */
	 SequenceNode(VerticalIdList verticalIdList, Sequence sequence, SequenceNode parent, int absSupport) {
		this.verticalIdList = verticalIdList;
		this.parent = parent;
		this.absSupport = absSupport;
		this.sequence = sequence;
	}


	public int getAbsSupport(){
		return absSupport;
	}

	public List<SequenceNode> getChildren() {
		return children;
	}

	public SequenceNode getParent() {
		return parent;
	}

	public VerticalIdList getVerticalIdList() {
		return verticalIdList;
	}

	public Sequence getSequence() {
		return sequence;
	}

	@Override
	public String toString() {
		return sequence.toString() + " #SUP: " + this.getAbsSupport();
	}
}