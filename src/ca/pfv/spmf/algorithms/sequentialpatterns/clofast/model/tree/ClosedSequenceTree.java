package ca.pfv.spmf.algorithms.sequentialpatterns.clofast.model.tree;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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
* A closed sequence tree (as used by CloFast)
* 
* @see AlgoCloFast 
*/
public class ClosedSequenceTree {

    private ClosedSequenceNode root;

    public ClosedSequenceTree(int sizePosList) {
        root = new ClosedSequenceNode(sizePosList);

    }

    public ClosedSequenceNode addChild(ClosedSequenceNode parent, Sequence sequence,
                                       VerticalIdList vil, int support) {
        ClosedSequenceNode newNode = new ClosedSequenceNode(parent, sequence, vil, support);
        parent.getChildren().add(newNode);
        return newNode;
    }

    public ClosedSequenceNode getRoot() {
        return root;
    }


    public static List<ClosedSequenceNode> visit(ClosedSequenceTree closedTree) {
        Queue<ClosedSequenceNode> queue = new LinkedList<>();
        List<ClosedSequenceNode> res = new ArrayList<>();
        queue.addAll(closedTree.getRoot().getChildren());

        ClosedSequenceNode currentNode;
        while (!queue.isEmpty()) {
            currentNode = queue.remove();
            res.add(currentNode);
            queue.addAll(currentNode.getChildren());
        }
        return res;
    }
}