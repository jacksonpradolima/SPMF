package ca.pfv.spmf.algorithms.graph_mining.tkg;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
 * This is an implementation of a graph, used by the GSPAN algorithm
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
public class Graph {

	/** the graph ID */
    private int id;
    
    /** Map each vertex id to its corresponding Vertex object */
    public Map<Integer, Vertex> vMap;
    
    /** Map each vertex id to the ids of its neighbors */
    private Map<Integer, Vertex[]> neighborCache;

    /** The list of vertices */
    Vertex[] vertices;
    
    /** Map each vertex label to the list of vertex ids having this label */
    Map<Integer, int[]> mapLabelToVertexIDs;

    /** Number of edges */
	private int edgeCount = 0;
    
	/** empty vertex list */
	private static final Vertex[] EMPTY_VERTEX_LIST = new Vertex[0];
	
	/** empty integer array */
	private static final int[] EMPTY_INTEGER_ARRAY = new int[0];
	
	/**
	 * Remove infrequent label from this graph
	 * @param label the label
	 */
	public void removeInfrequentLabel(int label) {
		
		Iterator<Entry<Integer, Vertex>> iter = vMap.entrySet().iterator();
		while (iter.hasNext()) {
			Vertex vertex = (Vertex) iter.next().getValue();
			if(vertex.getLabel() == label){
				iter.remove();
			}
		}
		
		for(Vertex vertex: vMap.values()){
			Iterator<Edge> it = vertex.getEdgeList().iterator();
			while (it.hasNext()) {
				Edge edge = (Edge) it.next();
				if(vMap.get(edge.v1) == null || vMap.get(edge.v2) == null){
					it.remove();
				}
			}
		}
	}

    /**
     * Constructor
     * @param id  a graph id
     * @param vMap a map of vertex id to corresponding Vertex object
     * @param edgeCount 
     */
    public Graph(int id, Map<Integer, Vertex> vMap) {
        this.id = id;
        this.vMap= vMap;
    }

	/**
     * Constructor
     * @param c  a dfs code
     */
    public Graph(DFSCode c) {
        this.vMap = new HashMap<>();
        for (ExtendedEdge ee : c.getEeL()) {
            int v1 = ee.getV1();
            int v2 = ee.getV2();
            int v1L = ee.getvLabel1();
            int v2L = ee.getvLabel2();
            int eL = ee.getEdgeLabel();
            
            Edge e = new Edge(v1, v2, eL);
            if (vMap.get(v1) == null)
                vMap.put(v1, new Vertex(v1, v1L));
            if (vMap.get(v2) == null)
                vMap.put(v2, new Vertex(v2, v2L));
            vMap.get(v1).addEdge(e);
            vMap.get(v2).addEdge(e);
        }
        this.id = -1;

    	precalculateVertexList();
        precalculateVertexNeighbors();
        precalculateLabelsToVertices();
    }

    

    /**
     * For optimization purposes, precalculate the list of neighbors of each vertex.
     */
	public void precalculateVertexNeighbors() {
		
        neighborCache = new HashMap<>();

        List<Vertex> neighbors = new ArrayList<Vertex>();
        
        // For each vertex
        for(Entry<Integer, Vertex> entry: vMap.entrySet()){
        	int vertexID = entry.getKey();
        	Vertex vertex = entry.getValue();
        	List<Edge> vertexEdgeList = vertex.getEdgeList();

        	// For each edge
            for (Edge e : vertexEdgeList) {
            	Vertex vertexNeighboor = vMap.get(e.another(vertexID));
                neighbors.add(vertexNeighboor);
            }
            
            // Convert to array
            Vertex [] arrayNeighbors = new Vertex[neighbors.size()];
            for(int i =0; i< neighbors.size(); i++){
            	arrayNeighbors[i] = neighbors.get(i);
            }
            
            // Sort the array
            Arrays.sort(arrayNeighbors);
            
            neighborCache.put(vertexID, arrayNeighbors);
            edgeCount += neighbors.size();
            neighbors.clear();
        }
        edgeCount = edgeCount / 2;
	}
	
    /**
     * For optimization purposes, precalculate the list of vertices in this graph.
     */
	public void precalculateVertexList() {
		
		vertices = new Vertex[vMap.size()];

        // For each vertex
        int j = 0;
        for(Entry<Integer, Vertex> entry: vMap.entrySet()){
        	Vertex vertex = entry.getValue();
            
            // Add the vertex to the precalculated array of vertices
            vertices[j] = vertex;
            j++;
        }
	}
	
	/**
     * Precalculate the list of vertices having each label
     */
	public void precalculateLabelsToVertices() {
		mapLabelToVertexIDs = new HashMap<Integer, int[]>();

		// create a temporary list to store vertex id
		List<Integer> sameIDs = new ArrayList<Integer>();

		// for each vertex
		for(int i = 0; i < vertices.length; i++){
			// get the label
			int label = vertices[i].getLabel();
			// if we did not already process that label
			if(!mapLabelToVertexIDs.containsKey(label)){
				// Find all other vertices having that label

				for(int j = i+1; j < vertices.length; j++){
					if(vertices[j].getLabel() == label){
						sameIDs.add(vertices[j].getId());
					}
				}

				// Create an array  to store the vertice IDs
				int[] verticeIDs = new int[sameIDs.size()+1];
				verticeIDs[0] = vertices[i].getId();
				for(int k = 0; k< sameIDs.size(); k++){
					verticeIDs[k+1] = sameIDs.get(k);
				}

				mapLabelToVertexIDs.put(label, verticeIDs);

				sameIDs.clear();
			}
		}
	}

	
    /**
     * Get all vertice IDs having a given label
     * @param targetLabel the label
     * @return the list of vertice IDs
     */
    public int[] findAllWithLabel(int targetLabel) {
    	int[] vertexIds = mapLabelToVertexIDs.get(targetLabel);
    	if(vertexIds == null){
    		return EMPTY_INTEGER_ARRAY;
    	}
        return vertexIds;
    }

	/**
     * Get all vertices of this graph
     * @return the set of all vertices
     */
    public Vertex[] getAllVertices() {
        return vertices;
    }
    
	/**
     * Get all vertices of this graph
     * @return the set of all vertices
     */
    public List<Vertex> getNonPrecalculatedAllVertices() {
       List<Vertex> vertices = new ArrayList<>(vMap.size());

        
        // For each vertex
        for(Entry<Integer, Vertex> entry: vMap.entrySet()){
        	Vertex vertex = entry.getValue();

            // Add the vertex to the precalculated array of vertices
        	vertices.add(vertex);
        }
        
        return vertices;
    }

    /**
     * Get all edges of this graph
     * @return the set of all edges
     */
    public Set<Edge> getAllEdges() {
        Set<Edge> edges = new HashSet<>();
        for (Vertex v : getAllVertices())
            edges.addAll(v.getEdgeList());
        return edges;
    }

    public int getVLabel(int v) {
        return vMap.get(v).getLabel();
    } 

    /**
     * Get the label of the edge between two vertex
     * @param v1 the id of a vertex
     * @param v2 the id of another vertex
     * @return the label if the edge exists, or otherwise -1.
     */
    public int getEdgeLabel(int v1, int v2) {
        for (Edge e : vMap.get(v1).getEdgeList()) {
        	if (e.v1 == v1 && e.v2 == v2){
                return e.getEdgeLabel();
        	}
        	if (e.v2 == v1 && e.v1 == v2){
                return e.getEdgeLabel();
        	}
        }
        return -1;
    }

    /**
     * Get the list of vertices connected to a given vertex
     * @param v the vertex
     * @return the list of vertices
     */
    public Vertex[] getAllNeighbors(int v) {
    	Vertex[] neighboors = neighborCache.get(v);
    	if(neighboors == null){
    		return EMPTY_VERTEX_LIST;
    	}
        return neighboors;
    }

    /**
     * Check if two vertices are neighbors
     * @param v1 the first vertex
     * @param v2 the second vertex
     * @return true if they are neighbors
     */
    public boolean isNeighboring(int v1, int v2) {
    	Vertex[] neighborsOfV1 = neighborCache.get(v1);
    	
//    	for(Vertex vertex: neighborsOfV1){
//    		if(vertex.getId() == v2){
//    			return true;
//    		}
//    	}

    	// ================ binary search ==========
		int low = 0;
		int high = neighborsOfV1.length - 1;

		while (high >= low) {
			int middle = (low + high) / 2;
			int val = neighborsOfV1[middle].getId();
			if (val == v2) {
				return true;
			}
			if (val < v2) {
				low = middle + 1;
			}
			if (val > v2) {
				high = middle - 1;
			}
		}
		return false;	
    }

    /**
     * Get the number of vertex
     * @return the number of vertex
     */
    public int getVertexCount() {
        return vertices.length;
    }
    
    /** 
     * Get the number of edges
     * @return the number of edges
     */
    public int getEdgeCount() {
//    	if(edgeCount == - 1){
//	        int num = 0;
//	        for (Vertex v : vertices) {
//	            num += v.getEdgeList().size();
//	        }
//	        edgeCount = num/2;
//    	}
        return edgeCount;
    }


    /**
     * Get the graph id
     * @return the id
     */
    public int getId() {
        return id;
    }




}
