package ca.pfv.spmf.algorithms.graph_mining.tkg;

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
 * This is an implementation of an edge, used by the GSPAN algorithm
 *  <br/><br/>
 *  
 * The gspan algorithm is described  in :
 * <br/><br/>
 *  
 * gSpan: Graph-Based Substructure Pattern Mining, by X. Yan and J. Han. 
 * Proc. 2002 of Int. Conf. on Data Mining (ICDM'02
 * 
 * <br/><br/>
 *  
 * The Gspan algorithm finds all the frequents subgraphs and their support in a
 * graph provided by the user.
 * <br/><br/>
 *  
 * This implementation saves the result to a file
 * 
 * @see AlgoGSPAN
 * @author Chao Cheng
 */
public class Edge {
	
	/** vertex id */
    public int v1;
    
	/** vertex id */
    public int v2;
    
    /** edge label */
    private int edgeLabel;
    
    /** the hashcode */
    private int hashcode;

    /**
     * Constructor
     * @param v1 vertex id
     * @param v2 vertex id
     * @param eLabel edge label
     */
    public Edge(int v1, int v2, int eLabel) {
        this.v1 = v1;
        this.v2 = v2;
        this.edgeLabel = eLabel;
        
        this.hashcode = (v1 + 1) * 100 + (v2 + 1) * 10 + edgeLabel;
    }

    /**
     * Given a vertex id in this edge, this method returns the id of the
     * other vertex connected by this edge.
     * 
     * @param v one of the two vertices appearing in this edge
     * @return the other vertex
     */
    public int another(int v) {
        return v == v1 ? v2 : v1;
    }

    /** Get the edge label */
    public int getEdgeLabel() {
        return edgeLabel;
    }

    @Override
    /**
     * Get the hashCode of this edge label
     * @return a hash code
     */
    public int hashCode() {
        return hashcode;
    }

    @Override
    /**
     * Check if this edge is equal to another edge
     * @param obj another edge or Object
     * @return true if equal
     */
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Edge)) return false;
        Edge that = (Edge) (obj);
        return this.hashcode == that.hashcode && this.v1 == that.v1 && this.v2 == that.v2 && this.edgeLabel == that.edgeLabel;
    }
}
