package ca.pfv.spmf.algorithms.graph_mining.tseqminer;
import java.util.LinkedList;
import java.util.List;
/* This file is copyright (c) 2018 by Chao Cheng
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
 * An item with vertex as used by TSeqMiner
 * @see AlgoTSeqMiner
 */
public class ItemVertex {
//    /** unique vertex id */
//    private int id;
    /** a set of item*/
    private List<Integer> items;
    /**
     * construct vertex obejct by unique vertex id
     * @param id the unique id to identify different vertices
     */
    public ItemVertex(int id) {
//        this.id = id;
        items = new LinkedList<>();
    }

    /**
     * This method add a set of items for vertex
     * @param items a set of item
     */
    public void addItems(List<Integer> items) {
        this.items.addAll(items);
    }

    /** This method get all items of the vertex
     * @return all items of the vertex
     */
    public Iterable<Integer> getAllItems() {
        return items;
    }
}
