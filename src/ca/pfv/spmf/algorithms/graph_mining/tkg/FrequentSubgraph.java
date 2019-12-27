package ca.pfv.spmf.algorithms.graph_mining.tkg;

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
 * This is a frequent subgraph. It stores the (1) DFS code of the subgraph,
 * (2) the support of the subgraph, and (3) the set of graphs where the 
 * subgraph appears.
 * 
 * @author Chao Cheng
 */
public class FrequentSubgraph implements Comparable<FrequentSubgraph>{
	
	/** dfs code */
    public DFSCode dfsCode;
    
    /** the ids of graphs where the subgraph appears */
    public Set<Integer> setOfGraphsIDs;
    
    /** the support of the subgraph */
    public int support;
    
    /**
     * Constructor
     * @param dfsCode a dfs code
     * @param setOfGraphsIDs the ids of graphs where the subgraph appears
     * @param support the support of the subgraph
     */
    public FrequentSubgraph(DFSCode dfsCode, Set<Integer> setOfGraphsIDs, int support){
    	this.dfsCode = dfsCode;
    	this.setOfGraphsIDs = setOfGraphsIDs;
    	this.support = support;
    }

    /**
     * Compare this subgraph with another subgraph
     * @param o another subgraph
     * @return 0 if equal, -1 if smaller, 1 if larger (in terms of support).
     */
    public int compareTo(FrequentSubgraph o) {
		if(o == this){
			return 0;
		}
		long compare =  this.support - o.support;
		if(compare > 0){
			return 1;
		}
		if(compare < 0){
			return -1;
		}
		return 0;
	}
}
