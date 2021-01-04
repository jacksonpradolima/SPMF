package ca.pfv.spmf.algorithms.graph_mining.tseqminer;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 * An item in an attributed graph
 * @see AlgoTSeqMiner
 */
public class ItemAttributedGraph {
//    /** identifier of attributed graph */
//    private int id;
    /** associate vertex with its identifier */
    private Map<Integer, ItemVertex> vMap;
    /** represent edges by adjacent list */
    private Map<Integer, Set<Integer>> edgesMap;

    /**
     * Constructive function
     * @param id the identifier of attributed graph
     */
    public ItemAttributedGraph(int id, Map<Integer, ItemVertex> vMap, Map<Integer, Set<Integer>> edgesMap) {
//        this.id = id;
        this.vMap = vMap;
        this.edgesMap = edgesMap;
    }


    /**
     * This method add items for a vertex
     * @param vId vertex identifier
     * @param items a set of item
     */
    public void addItemsForV(int vId, List<Integer> items) {
        vMap.get(vId).addItems(items);
    }

    /**
     * This method get item vertex corresponding to the identifier
     * @param vId item vertex identifier
     * @return corresponding item vertex
     */
    public ItemVertex getItemV(int vId) {
        return vMap.get(vId);
    }

    /**
     * This method get id of vertex in way of iteration
     * @return id of vertex on at a time
     */
    public Iterable<Integer> getAllVId() {
        return vMap.keySet();
    }

    /** This method get item of a given id of vertex in way of iteration */
    public Iterable<Integer> getAllItems4V(int vId) {
        return vMap.get(vId).getAllItems();
    }

    public Set<Integer> findAllNeighbors4V(int vId) {
        return edgesMap.get(vId);
    }

    public int getTotalSize() {
        return vMap.size();
    }
}
