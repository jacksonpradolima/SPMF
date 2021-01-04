package ca.pfv.spmf.algorithms.graph_mining.tseqminer;
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
 * This class is a spatio temporal point
*
 * @see AlgoTSeqMiner
 */
public class STPoint {
    /** represent identifier of item attributed graph */
    private int timestamp;
    /** represent identifier of item vertex */
    private int vId;

    /**
     * This method construct spatio-temporal point using identifiers of attributed graph and vertex
     * @param timestamp identitier of attributed graph
     * @param vId identifier of vertex
     */
    public STPoint(int timestamp, int vId) {
        this.timestamp = timestamp;
        this.vId = vId;
    }

    /**
     * This method get timestamp ot this spatio-temporal point
     * @return timestamp of the the ST point
     */
    public int getTimestamp() {
        return timestamp;
    }

    /**
     *  This method get vertex identifier of the ST point
     * @return vertex identifier of the ST point
     */
    public int getvId() {
        return vId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(timestamp).append(",").append(vId).append(")");
        return sb.toString();
    }
}
