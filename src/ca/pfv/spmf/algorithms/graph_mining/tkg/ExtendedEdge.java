package ca.pfv.spmf.algorithms.graph_mining.tkg;
import java.io.Serializable;

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
 * This is an implementation of an extended edge, used by the GSPAN algorithm
 *  <br/><br/>
 *  
 * The GSPAN algorithm is described  in :
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
public class ExtendedEdge implements Serializable{
	
	/** serial UID */
	private static final long serialVersionUID = -8338195030333292537L;

	/** the first vertex id */
    int v1;
    
    /** the second vertex id */
    int v2;
    
    /** the label of the first vertex */
    int vLabel1;
    
    /** the label of the second vertex */
    int vLabel2;
    
    /** the edge label */
    int edgeLabel;
    
    /** the hash code */
    int hashcode;

    /**
     * Constructor
     * @param v1  a vertex id
     * @param v2  another vertex id
     * @param vLabel1 the first vertex label
     * @param vLabel2 the second vertex label
     * @param edgeLabel the edge label
     */
    public ExtendedEdge(int v1, int v2, int vLabel1, int vLabel2, int edgeLabel) {
        this.v1 = v1;
        this.v2 = v2;
        this.vLabel1 = vLabel1;
        this.vLabel2 = vLabel2;
        this.edgeLabel = edgeLabel;
        this.hashcode = (1 + v1)*100 + (1 + v2) * 50 + (1 + vLabel1) * 30 + (1 + vLabel2)  * 20 + (1 + edgeLabel);
    }

	public boolean smallerThan(ExtendedEdge that) {
		if (that == null)
			return true;
		int x1 = this.getV1();
		int x2 = this.getV2();
		int y1 = that.getV1();
		int y2 = that.getV2();
		if (pairSmallerThan(x1, x2, y1, y2))
			return true;
		else if (x1 == y1 && x2 == y2) {
			return this.getvLabel1() < that.getvLabel1()
					|| (this.getvLabel1() == that.getvLabel1() && this
							.getvLabel2() < that.getvLabel2())
					|| (this.getvLabel1() == that.getvLabel1()
							&& this.getvLabel2() == that.getvLabel2() && this
							.getEdgeLabel() < that.getEdgeLabel());
		} else
			return false;
    }

    private boolean pairSmallerThan(int x1, int x2, int y1, int y2) {
        boolean xForward = x1 < x2 ;
        boolean yForward = y1 < y2 ;
        if (xForward && yForward)
            return x2 < y2 || (x2 == y2 && x1 > y1);
        else if ((! xForward) && (! yForward))
            return x1 < y1 || (x1 == y1 && x2 < y2);
        else if (xForward)
            return x2 <= y1;
        else
            return x1 < y2;
    }

    /**
     * Get the first vertex id
     * @return the first vertex id
     */
    public int getV1() {
        return v1;
    }

    /**
     * Get the second vertex id
     * @return the second vertex id
     */
    public int getV2() {
        return v2;
    }

    /** 
     * get the first vertex label
     * @return the first vertex label
     */
    public int getvLabel1() {
        return vLabel1;
    }
    
    /** 
     * get the second vertex label
     * @return the second vertex label
     */
    public int getvLabel2() {
        return vLabel2;
    }

    /** 
     * Get the edge label
     * @return the edge label
     */
    public int getEdgeLabel() {
        return edgeLabel;
    }
    @Override
    
    /**
     * Get the hash code of this extended edge
     * @return a hash code
     */
    public int hashCode() {
        return hashcode;
    }

    @Override
    /**
     * Check if this extended edge is equal to another one
     * @param another extended edge or Object
     * @return true if equal
     */
    public boolean equals(Object another) {
        if (another == null) return false;
        if (this == another) return true;
        if (!(another instanceof ExtendedEdge)) return false;
        ExtendedEdge that = (ExtendedEdge) another;
        if (this.v1 == that.getV1() && this.v2 == that.getV2() && this.vLabel1 == that.getvLabel1()
                && this.vLabel2 == that.getvLabel2() && this.edgeLabel == that.getEdgeLabel())
            return true;
        else
            return false;
    }

    @Override
    /**
     * Obtain a String representation of this extended edge
     * @return a String
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(v1).append(",").append(v2).append(",").append(vLabel1).append(",").append(vLabel2)
                .append(",").append(edgeLabel).append(">");
        return sb.toString();
    }

}
