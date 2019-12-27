package ca.pfv.spmf.algorithms.sequentialpatterns.clofast.model;

import java.util.LinkedList;

import ca.pfv.spmf.algorithms.sequentialpatterns.clofast.model.tree.ClosedSequenceNode;

/* This file is copyright (c) Fabiana Lanotte et al.
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
 * Basic node stored in a linked list
 * 
 * @author eliana
 * @see AlgoFast 
 * @see AlgoCloFast 
 */
public class ListNode {

    private ListNode next;
    //private int row;
    private int column;


    public ListNode(int c) {
        this.column = c;
    }

    public ListNode(int c, ListNode next) {
        this.column = c;
        this.next = next;
    }

    public int getColumn() {
        return column;
    }

    public void setNext(ListNode node) {
        this.next = node;
    }

    public ListNode next() {
        return next;
    }

    public ListNode before(ListNode succ) {
        while (succ != null) {
            if (this.column < succ.column)
                return succ;
            succ = succ.next;
        }
        return null;
    }


    /**
     * @param succsNodes
     * @param i
     * @return
     */
    public ListNode before(LinkedList<ClosedSequenceNode> succsNodes, Integer i) {

        ListNode curr = this;

        for (ClosedSequenceNode node : succsNodes) {
            curr = curr.before(node.getVerticalIdList().getElements()[i]);
            if (curr == null)
                break;
        }

        return curr;
    }

//	public boolean equal(LinkedList<ListNode[]> vilSuccList, int i) {
//		ListNode curr = vilSuccList.getFirst()[i];
//		return column == curr.column;
//	}


    /**
     * check equal on the current node
     *
     * @param succ
     * @return
     */
    public ListNode equal(ListNode succ) {

        while (succ != null) {
            if (this.column == succ.column)
                return succ;
            else
                succ = succ.next;
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("[" + " : " + column + "]");
        return buf.toString();
    }

}
