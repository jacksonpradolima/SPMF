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
* A sequence tree
* 
* @see AlgoFast 
*/
public class SequenceTree {

    private SequenceNode root;

    public SequenceTree(long numSequences) {
        root = new SequenceNode(null,new Sequence(),null,(int) numSequences);
    }

    public SequenceNode addChild(SequenceNode parent, Sequence sequence, VerticalIdList vil, int absoluteSupport) {
        SequenceNode newNode = new SequenceNode(vil, sequence, parent, absoluteSupport);
        parent.getChildren().add(newNode);
        return newNode;
    }

    public SequenceNode getRoot() {
        return root;
    }

    /**
     *
     * @param tree
     * @return
     */
    public static List<SequenceNode> visit(SequenceTree tree) {

        Queue<SequenceNode> queue = new LinkedList<>();
        List<SequenceNode> result = new ArrayList<>();
        queue.addAll(tree.getRoot().getChildren());

        while (!queue.isEmpty()) {
            SequenceNode currentNode = queue.remove();
            result.add(currentNode);
            queue.addAll(currentNode.getChildren());
        }
        return result;
    }
}