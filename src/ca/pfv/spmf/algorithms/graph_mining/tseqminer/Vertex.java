package ca.pfv.spmf.algorithms.graph_mining.tseqminer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * This class is a vertex as used by the TSeqMiner algorithm.
*
 * @see AlgoTSeqMiner
 */
public class Vertex {
    /** mapping record attribute types and values */
    private Map<Integer, Double> attrDouMap;

    /**
     * construct vertex obejct by unique vertex id
     * @param id the unique id to identify different vertices
     */
    public Vertex(int id) {
        attrDouMap = new HashMap<>();
    }


    /**
     * This method set attribute types and values for vertex
     * @param attrL the attribute type list
     * @param valL the attribute value list
     */
    public void addAttrsValsForV(List<Integer> attrL, List<Double> valL) {
        for (int i = 0; i < attrL.size(); i++) {
            addAttrValForV(attrL.get(i), valL.get(i));
        }
    }

    /**
     * This method set single attribute type and value for vertex
     * @param attr single attribute type
     * @param val single attribute value
     */
    public void addAttrValForV(int attr, Double val) {
        attrDouMap.put(attr, val);
    }

    /**
     * This method get number of attribute type
     * @return number of attribute
     */
    public int getAttrNum() {
        return attrDouMap.size();
    }

    /**
     * This method get attribute map for the vertex
     * @return attribute map
     */
    public Map<Integer, Double> getAttrDouMap() {
        return attrDouMap;
    }
}
