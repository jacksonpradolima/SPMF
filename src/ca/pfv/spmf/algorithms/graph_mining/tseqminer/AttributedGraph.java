package ca.pfv.spmf.algorithms.graph_mining.tseqminer;

import java.util.HashMap;
import java.util.HashSet;
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
 * An attributed graph
 * @see AlgoTSeqMiner
 */
public class AttributedGraph {
//    /** time identifier for this attributed graph */
//    private int stage;
    /** mapping associate integer with corresponding vertex */
    private Map<Integer, Vertex> vMap;
    /** represent edges by adjacent list, with a little redundancy */
    private Map<Integer, Set<Integer>> edgesMap;

    /**
     * This method construct a attributed graph by a time identifier and initialize mappings
     * @param stage time identifier
     */
    public AttributedGraph(int stage) {
//        this.stage = stage;
        this.vMap = new HashMap<>();
        this.edgesMap = new HashMap<>();
    }

    /**
     * This method add a vertex by unique identifier
     * @param id unique identifier of vertex
     */
    public void addVertex(int id) {
        vMap.put(id, new Vertex(id));
    }

    /**
     * This method find vertex object by unique identifier of vertex
     * @param id identifier of vertex
     * @return corresponding vertex
     */
    public Vertex getVertex(int id) {
        return vMap.get(id);
    }

    /**
     * This method add a set of attribute types and values for a vertex
     * @param vId identifier of vertex
     * @param attrList a set of attribute types
     * @param valList  a set of attribute values
     */
    public void addAttrValForV(int vId, List<Integer> attrList, List<Double> valList) {
        Vertex v = vMap.get(vId);
        v.addAttrsValsForV(attrList, valList);
    }

    /**
     * This method add edges for a set of vertex pairs
     * @param v1 the common vertex in the set of pairs
     * @param v2L a set of vertices associated with the common vertex
     */
    public void addEdges(int v1, List<Integer> v2L) {
        for (int v2 : v2L) {
            addEdge(v1, v2);
        }
    }

    /**
     * This method add edge for a pair of vertices
     * @param v1 one vertex identifier
     * @param v2 another identifier
     */
    public void addEdge(int v1, int v2) {
        Set<Integer> v1Neighbors = edgesMap.get(v1);
        Set<Integer> v2Neighbors = edgesMap.get(v2);
        if (v1Neighbors == null) {
            v1Neighbors = new HashSet<>();
            edgesMap.put(v1, v1Neighbors);
        }
        if (v2Neighbors == null) {
            v2Neighbors = new HashSet<>();
            edgesMap.put(v2, v2Neighbors);
        }
        v1Neighbors.add(v2);
        v2Neighbors.add(v1);
    }


    /**
     * This method get total number of vertices
     * @return total number
     */
    public int getVerNum() {
        return vMap.size();
    }

    /**
     * This method get all id of vertices
     */
    public Iterable<Integer> getAllVerticeId() {
        return vMap.keySet();
    }

    public Map<Integer, Set<Integer>> getEdgesMap() {
        return edgesMap;
    }

}
