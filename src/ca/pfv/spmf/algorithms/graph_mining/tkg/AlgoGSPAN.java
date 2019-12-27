package ca.pfv.spmf.algorithms.graph_mining.tkg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ca.pfv.spmf.tools.MemoryLogger;

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
 * This is an implementation of the GSPAN algorithm <br/>
 * <br/>
 * 
 * The gspan algorithm is described in : <br/>
 * <br/>
 * 
 * gSpan: Graph-Based Substructure Pattern Mining, by X. Yan and J. Han. Proc.
 * 2002 of Int. Conf. on Data Mining (ICDM'02)
 * 
 * <br/>
 * <br/>
 * 
 * The Gspan algorithm finds all the frequents subgraphs and their support in a
 * graph provided by the user. <br/>
 * <br/>
 * 
 * This implementation saves the result to a file
 * 
 * @author Chao Cheng
 */
public class AlgoGSPAN {

	/**
	 * the minimum support represented as a count (number of subgraph occurrences)
	 */
	private int minSup;

	/** The list of frequent subgraphs found by the last execution */
	private List<FrequentSubgraph> frequentSubgraphs;

	/** runtime of the most recent execution */
	private long runtime = 0;

	/** runtime of the most recent execution */
	private double maxmemory = 0;

	/** pattern count of the most recent execution */
	private int patternCount = 0;

	/** number of graph in the input database */
	private int graphCount = 0;

	/** frequent vertex labels */
	List<Integer> frequentVertexLabels;

	/** if true, debug mode is activated */
	private static final boolean DEBUG_MODE = false;

	/** eliminate infrequent labels from graphs */
	private static final boolean ELIMINATE_INFREQUENT_VERTICES = true;  // strategy in Gspan paper

	/** eliminate infrequent vertex pairs from graphs */
	private static final boolean ELIMINATE_INFREQUENT_VERTEX_PAIRS = true;

	/** eliminate infrequent labels from graphs */
	private static final boolean ELIMINATE_INFREQUENT_EDGE_LABELS = true;  // strategy in Gspan paper

	/** apply edge count pruning strategy */
	private static final boolean EDGE_COUNT_PRUNING = true;

	/** skip strategy */
	private static final boolean SKIP_STRATEGY = true;

	/** infrequent edges removed */
	int infrequentVertexPairsRemoved;

	/** infrequent edges removed */
	int infrequentVerticesRemovedCount;

	/** remove infrequent edge labels */
	int edgeRemovedByLabel;

	/** remove infrequent edge labels */
	int eliminatedWithMaxSize;

	/** empty graph removed count */
	int emptyGraphsRemoved;

	/** empty graph removed by edge count pruning */
	int pruneByEdgeCountCount;

	/** skip strategy count */
	int skipStrategyCount;

	/** maximum number of edges in each frequent subgraph */
	int maxNumberOfEdges = Integer.MAX_VALUE;

	/** Output the ids of graph containing each frequent subgraph */
	boolean outputGraphIds = true;

	/**
	 * Run the GSpan algorithm
	 * 
	 * @param inPath               the input file
	 * @param outPath              the output file
	 * @param minSupport           a minimum support value (a percentage represented
	 *                             by a value between 0 and 1)
	 * @param outputSingleVertices if true, frequent subgraphs containing a single
	 *                             vertex will be output
	 * @param outputDotFile        if true, a graphviz DOT file will be generated to
	 *                             visualize the patterns
	 * @param maxNumberOfEdges     an integer indicating a maximum number of edges
	 *                             for each frequent subgraph
	 * @param outputGraphIds       Output the ids of graph containing each frequent
	 *                             subgraph
	 * @throws IOException            if error while writing to file
	 * @throws ClassNotFoundException
	 */
	public void runAlgorithm(String inPath, String outPath, double minSupport, boolean outputSingleVertices,
			boolean outputDotFile, int maxNumberOfEdges, boolean outputGraphIds)
			throws IOException, ClassNotFoundException {

		// if maximum size is 0
		if (maxNumberOfEdges <= 0) {
			return;
		}

		/**
		 * the minimum support threshold as a precentage represented by a value between
		 * 0 and 1
		 */
		double minFrequency = minSupport;

		// Save the maximum number of edges
		this.maxNumberOfEdges = maxNumberOfEdges;

		// Save parameter
		this.outputGraphIds = outputGraphIds;

		// initialize variables for statistics
		infrequentVertexPairsRemoved = 0;
		infrequentVerticesRemovedCount = 0;
		edgeRemovedByLabel = 0;
		eliminatedWithMaxSize = 0;
		emptyGraphsRemoved = 0;
		pruneByEdgeCountCount = 0;

		// initialize structure to store results
		frequentSubgraphs = new ArrayList<FrequentSubgraph>();

		// Initialize the tool to check memory usage
		MemoryLogger.getInstance().reset();

		// reset the number of patterns found
		patternCount = 0;

		// Record the start time
		Long t1 = System.currentTimeMillis();

		// read graphs
		List<Graph> graphDB = readGraphs(inPath);

		// Calculate the minimum support as a number of graphs
		minSup = (int) Math.ceil(minFrequency * graphDB.size());

		// mining
		gSpan(graphDB, outputSingleVertices);

		// check the memory usage
		MemoryLogger.getInstance().checkMemory();

		// output
		writeResultToFile(outPath);

		Long t2 = System.currentTimeMillis();

		runtime = (t2 - t1) / 1000;

		maxmemory = MemoryLogger.getInstance().getMaxMemory();

		patternCount = frequentSubgraphs.size();

		if (outputDotFile) {
			outputDotFile(outPath);
		}
	}

	/**
	 * Output the DOT files to a given file path
	 * 
	 * @param outputPath the output file path
	 * @throws IOException if some exception when reading/writing the files
	 */
	private static void outputDotFile(String outputPath) throws IOException {
		String dirName = outputPath + "_dotfile";
		File dir = new File(dirName);
		if (!dir.exists())
			dir.mkdir();
		VizGraph.visulizeFromFile(outputPath, dirName);
	}

	/**
	 * Write the result to an output file
	 * 
	 * @param outputPath an output file path
	 **/
	private void writeResultToFile(String outputPath) throws IOException {
		// Create the output file
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputPath)));

		// For each frequent subgraph
		int i = 0;
		for (FrequentSubgraph subgraph : frequentSubgraphs) {
			StringBuilder sb = new StringBuilder();

			DFSCode dfsCode = subgraph.dfsCode;
			sb.append("t # ").append(i).append(" * ").append(subgraph.support).append(System.lineSeparator());
			if (dfsCode.size() == 1) {
				ExtendedEdge ee = dfsCode.getEeL().get(0);
				if (ee.getEdgeLabel() == -1) {
					sb.append("v 0 ").append(ee.getvLabel1()).append(System.lineSeparator());
				} else {
					sb.append("v 0 ").append(ee.getvLabel1()).append(System.lineSeparator());
					sb.append("v 1 ").append(ee.getvLabel2()).append(System.lineSeparator());
					sb.append("e 0 1 ").append(ee.getEdgeLabel()).append(System.lineSeparator());
				}
			} else {
				List<Integer> vLabels = dfsCode.getAllVLabels();
				for (int j = 0; j < vLabels.size(); j++) {
					sb.append("v ").append(j).append(" ").append(vLabels.get(j)).append(System.lineSeparator());
				}
				for (ExtendedEdge ee : dfsCode.getEeL()) {
					int startV = ee.getV1();
					int endV = ee.getV2();
					int eL = ee.edgeLabel;
					sb.append("e ").append(startV).append(" ").append(endV).append(" ").append(eL)
							.append(System.lineSeparator());
				}
			}
			// If the user choose to output the graph ids where the frequent subgraph
			// appears
			// We output it
			if (outputGraphIds) {
				sb.append("x");
				for (int id : subgraph.setOfGraphsIDs) {
					sb.append(" ").append(id);
				}
			}
			sb.append(System.lineSeparator()).append(System.lineSeparator());

			bw.write(sb.toString());

			i++;
		}
		bw.close();
	}

	/**
	 * Read graph from the input file
	 * 
	 * @param path the input file
	 * @return a list of input graph from the input graph database
	 * @throws IOException if error reading or writing to file
	 */
	private List<Graph> readGraphs(String path) throws IOException {
		if (DEBUG_MODE) {
			System.out.println("start reading graphs...");
		}
		BufferedReader br = new BufferedReader(new FileReader(new File(path)));
		List<Graph> graphDatabase = new ArrayList<>();

		String line = br.readLine();
		Boolean hasNextGraph = (line != null) && line.startsWith("t");

		// For each graph of the graph database
		while (hasNextGraph) {
			hasNextGraph = false;
			int gId = Integer.parseInt(line.split(" ")[2]);
			Map<Integer, Vertex> vMap = new HashMap<>();
			while ((line = br.readLine()) != null && !line.startsWith("t")) {

				String[] items = line.split(" ");

				if (line.startsWith("v")) {
					// If it is a vertex
					int vId = Integer.parseInt(items[1]);
					int vLabel = Integer.parseInt(items[2]);
					vMap.put(vId, new Vertex(vId, vLabel));
				} else if (line.startsWith("e")) {
					// If it is an edge
					int v1 = Integer.parseInt(items[1]);
					int v2 = Integer.parseInt(items[2]);
					int eLabel = Integer.parseInt(items[3]);
					Edge e = new Edge(v1, v2, eLabel);
//                    System.out.println(v1 + " " + v2 + " " + vMap.get(v1).id + " " + vMap.get(v2).id);
					vMap.get(v1).addEdge(e);
					vMap.get(v2).addEdge(e);
				}
			}
			graphDatabase.add(new Graph(gId, vMap));
			if (line != null) {
				hasNextGraph = true;
			}
		}

		br.close();

		if (DEBUG_MODE) {
			System.out.println("read successfully, totally " + graphDatabase.size() + " graphs");
		}
		graphCount = graphDatabase.size();
		return graphDatabase;
	}

	/**
	 * Find all isomorphisms between graph described by c and graph g each
	 * isomorphism is represented by a map
	 * 
	 * @param c a dfs code representing a subgraph
	 * @param g a graph
	 * @return the list of all isomorphisms
	 */
	private List<Map<Integer, Integer>> subgraphIsomorphisms(DFSCode c, Graph g) {

		List<Map<Integer, Integer>> isoms = new ArrayList<>();

		// initial isomorphisms by finding all vertices with same label as vertex 0 in C
		int startLabel = c.getEeL().get(0).getvLabel1(); // only non-empty DFSCode will be real parameter
		for (int vID : g.findAllWithLabel(startLabel)) {
			Map<Integer, Integer> map = new HashMap<>();
			map.put(0, vID);
			isoms.add(map);
		}

		// each extended edge will update partial isomorphisms
		// for forward edge, each isomorphism will be either extended or discarded
		// for backward edge, each isomorphism will be either unchanged or discarded
		for (ExtendedEdge ee : c.getEeL()) {
			int v1 = ee.getV1();
			int v2 = ee.getV2();
			int v2Label = ee.getvLabel2();
			int eLabel = ee.getEdgeLabel();

			List<Map<Integer, Integer>> updateIsoms = new ArrayList<>();
			// For each isomorphism
			for (Map<Integer, Integer> iso : isoms) {

				// Get the vertex corresponding to v1 in the current edge
				int mappedV1 = iso.get(v1);

				// If it is a forward edge extension
				if (v1 < v2) {
					Collection<Integer> mappedVertices = iso.values();

					// For each neighbor of the vertex corresponding to V1
					for (Vertex mappedV2 : g.getAllNeighbors(mappedV1)) {

						// If the neighbor has the same label as V2 and is not already mapped and the
						// edge label is
						// the same as that between v1 and v2.
						if (v2Label == mappedV2.getLabel() && (!mappedVertices.contains(mappedV2.getId()))
								&& eLabel == g.getEdgeLabel(mappedV1, mappedV2.getId())) {

							// TODO: PHILIPPE: getEdgeLabel() in the above line could be precalculated in
							// Graph.java ...

							// because there may exist multiple extensions, need to copy original partial
							// isomorphism
							HashMap<Integer, Integer> tempM = new HashMap<>(iso.size() + 1);
							tempM.putAll(iso);
							tempM.put(v2, mappedV2.getId());

							updateIsoms.add(tempM);
						}
					}
				} else {
					// If it is a backward edge extension
					// v2 has been visited, only require mappedV1 and mappedV2 are connected in g
					int mappedV2 = iso.get(v2);
					if (g.isNeighboring(mappedV1, mappedV2) && eLabel == g.getEdgeLabel(mappedV1, mappedV2)) {
						updateIsoms.add(iso);
					}
				}
			}
			isoms = updateIsoms;
		}

		// Return the isomorphisms
		return isoms;
	}

	private Map<ExtendedEdge, Set<Integer>> rightMostPathExtensionsFromSingle(DFSCode c, Graph g) {
		int gid = g.getId();

		// Map of extended edges to graph ids
		Map<ExtendedEdge, Set<Integer>> extensions = new HashMap<>();

		if (c.isEmpty()) {
			// IF WE HAVE AN EMPTY SUBGRAPH THAT WE WANT TO EXTEND

			// find all distinct label tuples
			for (Vertex vertex : g.vertices) {
				for (Edge e : vertex.getEdgeList()) {
					int v1L = g.getVLabel(e.v1);
					int v2L = g.getVLabel(e.v2);
					ExtendedEdge ee1;
					if (v1L < v2L) {
						ee1 = new ExtendedEdge(0, 1, v1L, v2L, e.getEdgeLabel());
					} else {
						ee1 = new ExtendedEdge(0, 1, v2L, v1L, e.getEdgeLabel());
					}

					// Update the set of graph ids for this pattern
					Set<Integer> setOfGraphIDs = extensions.get(ee1);
					if (setOfGraphIDs == null) {
						setOfGraphIDs = new HashSet<>();
						extensions.put(ee1, setOfGraphIDs);
					}
					setOfGraphIDs.add(gid);
				}
			}
		} else {
			// IF WE WANT TO EXTEND A SUBGRAPH
			int rightMost = c.getRightMost();

			// Find all isomorphisms of the DFS code "c" in graph "g"
			List<Map<Integer, Integer>> isoms = subgraphIsomorphisms(c, g);

			// For each isomorphism
			for (Map<Integer, Integer> isom : isoms) {

				// backward extensions from rightmost child
				Map<Integer, Integer> invertedISOM = new HashMap<>();
				for (Entry<Integer, Integer> entry : isom.entrySet()) {
					invertedISOM.put(entry.getValue(), entry.getKey());
				}
				int mappedRM = isom.get(rightMost);
				int mappedRMlabel = g.getVLabel(mappedRM);
				for (Vertex x : g.getAllNeighbors(mappedRM)) {
					Integer invertedX = invertedISOM.get(x.getId());
					if (invertedX != null && c.onRightMostPath(invertedX) && c.notPreOfRM(invertedX)
							&& !c.containEdge(rightMost, invertedX)) {
						// rightmost and invertedX both have correspondings in g, so label of vertices
						// and edge all
						// can be found by correspondings
						ExtendedEdge ee = new ExtendedEdge(rightMost, invertedX, mappedRMlabel, x.getLabel(),
								g.getEdgeLabel(mappedRM, x.getId()));
						if (extensions.get(ee) == null)
							extensions.put(ee, new HashSet<>());
						extensions.get(ee).add(g.getId());
					}
				}
				// forward extensions from nodes on rightmost path
				Collection<Integer> mappedVertices = isom.values();
				for (int v : c.getRightMostPath()) {
					int mappedV = isom.get(v);
					int mappedVlabel = g.getVLabel(mappedV);
					for (Vertex x : g.getAllNeighbors(mappedV)) {
						if (!mappedVertices.contains(x.getId())) {
							ExtendedEdge ee = new ExtendedEdge(v, rightMost + 1, mappedVlabel, x.getLabel(),
									g.getEdgeLabel(mappedV, x.getId()));
							if (extensions.get(ee) == null)
								extensions.put(ee, new HashSet<>());
							extensions.get(ee).add(g.getId());
						}
					}
				}
			}
		}
		return extensions;
	}

	private Map<ExtendedEdge, Set<Integer>> rightMostPathExtensions(DFSCode c, List<Graph> graphDatabase,
			Set<Integer> graphIds) {

		Map<ExtendedEdge, Set<Integer>> extensions = new HashMap<>();

		// if the DFS code is empty (WE START FROM AN EMPTY GRAPH)
		if (c.isEmpty()) {

			// For each graph
//            int highestSupport = 0;
//        	int remaininggraphCount = graphIds.size();
			for (Integer graphId : graphIds) {
				Graph g = graphDatabase.get(graphId);

				if (EDGE_COUNT_PRUNING && c.size() >= g.getEdgeCount()) {
					pruneByEdgeCountCount++;
					continue;
				}

				// find all distinct label tuples
				for (Vertex vertex : g.vertices) {
					for (Edge e : vertex.getEdgeList()) {
						int v1L = g.getVLabel(e.v1);
						int v2L = g.getVLabel(e.v2);
						ExtendedEdge ee1;
						if (v1L < v2L) {
							ee1 = new ExtendedEdge(0, 1, v1L, v2L, e.getEdgeLabel());
						} else {
							ee1 = new ExtendedEdge(0, 1, v2L, v1L, e.getEdgeLabel());
						}

						// Update the set of graph ids for this pattern
						Set<Integer> setOfGraphIDs = extensions.get(ee1);
						if (setOfGraphIDs == null) {
							setOfGraphIDs = new HashSet<>();
							extensions.put(ee1, setOfGraphIDs);
						}
						setOfGraphIDs.add(graphId);
//                        if(setOfGraphIDs.size() > highestSupport){
//                        	highestSupport =  setOfGraphIDs.size();
//                        }
					}
				}
//            	remaininggraphCount--;
//            	if(SKIP_STRATEGY && (highestSupport + remaininggraphCount  < minSup)){
////            		System.out.println("BREAK");
//            		skipStrategyCount++;
//            		break;
//            	}
			}
		} else {
			// IF THE DFS CODE IS NOT EMPTY (WE WANT TO EXTEND SOME EXISTING GRAPH)
			int remaininggraphCount = graphIds.size();
			int highestSupport = 0;
			int rightMost = c.getRightMost();
			// For each graph
			for (Integer graphId : graphIds) {
				Graph g = graphDatabase.get(graphId);

				if (EDGE_COUNT_PRUNING && c.size() >= g.getEdgeCount()) {
					pruneByEdgeCountCount++;
					continue;
				}

				List<Map<Integer, Integer>> isoms = subgraphIsomorphisms(c, g);
				for (Map<Integer, Integer> isom : isoms) {

					// backward extensions from rightmost child
					Map<Integer, Integer> invertedISOM = new HashMap<>();
					for (Entry<Integer, Integer> entry : isom.entrySet()) {
						invertedISOM.put(entry.getValue(), entry.getKey());
					}
					int mappedRM = isom.get(rightMost);
					int mappedRMlabel = g.getVLabel(mappedRM);
					for (Vertex x : g.getAllNeighbors(mappedRM)) {
						Integer invertedX = invertedISOM.get(x.getId());
						if (invertedX != null && c.onRightMostPath(invertedX) && c.notPreOfRM(invertedX)
								&& !c.containEdge(rightMost, invertedX)) {
							// rightmost and invertedX both have correspondings in g, so label of vertices
							// and edge all
							// can be found by correspondings
							ExtendedEdge ee = new ExtendedEdge(rightMost, invertedX, mappedRMlabel, x.getLabel(),
									g.getEdgeLabel(mappedRM, x.getId()));
							if (extensions.get(ee) == null)
								extensions.put(ee, new HashSet<>());
							extensions.get(ee).add(g.getId());
						}
					}
					// forward extensions from nodes on rightmost path
					Collection<Integer> mappedVertices = isom.values();
					for (int v : c.getRightMostPath()) {
						int mappedV = isom.get(v);
						int mappedVlabel = g.getVLabel(mappedV);
						for (Vertex x : g.getAllNeighbors(mappedV)) {
							if (!mappedVertices.contains(x.getId())) {
								ExtendedEdge ee = new ExtendedEdge(v, rightMost + 1, mappedVlabel, x.getLabel(),
										g.getEdgeLabel(mappedV, x.getId()));
								if (extensions.get(ee) == null)
									extensions.put(ee, new HashSet<>());
								Set<Integer> setOfGraphIDs = extensions.get(ee);

								setOfGraphIDs.add(g.getId());

								if (setOfGraphIDs.size() > highestSupport) {
									highestSupport = setOfGraphIDs.size();
								}
							}
						}
					}
				}

				if (SKIP_STRATEGY && (highestSupport + remaininggraphCount < minSup)) {
//            		System.out.println("BREAK2");
					skipStrategyCount++;
					extensions = null;
					break;
				}
				remaininggraphCount--;
			}
		}
		return extensions;
	}

	/**
	 * Initial call of the depth-first search
	 * 
	 * @param c                      the initial DFS code
	 * @param graphDB                a graph database
	 * @param outputFrequentVertices if true, include frequent subgraph with a
	 *                               single vertex in the output
	 * @throws IOException            exception if error writing/reading to file
	 * @throws ClassNotFoundException if error casting a class
	 */
	private void gSpan(List<Graph> graphDB, boolean outputFrequentVertices) throws IOException, ClassNotFoundException {

		// If the user wants single vertex graph, we will output them
		if (outputFrequentVertices || ELIMINATE_INFREQUENT_VERTICES) {
			findAllOnlyOneVertex(graphDB, outputFrequentVertices);
		}

		for (Graph g : graphDB) {
			g.precalculateVertexList();
		}

		if (ELIMINATE_INFREQUENT_VERTEX_PAIRS || ELIMINATE_INFREQUENT_EDGE_LABELS) {
			removeInfrequentVertexPairs(graphDB);
		}

		if (DEBUG_MODE) {
			System.out.println("Precalculating information...");
		}

		// Create a set with all the graph ids
		Set<Integer> graphIds = new HashSet<Integer>();
		for (int i = 0; i < graphDB.size(); i++) {
			Graph g = graphDB.get(i);

			if (g.vertices == null || g.vertices.length != 0) {
				// If we deleted some vertices, we recalculate again the vertex list
				if (infrequentVerticesRemovedCount > 0) {
					g.precalculateVertexList();
				}

				graphIds.add(i);

				// Precalculate the list of neighbors of each vertex
				g.precalculateVertexNeighbors();

				// Precalculate the list of vertices having each label
				g.precalculateLabelsToVertices();
			} else {
				if (DEBUG_MODE) {
					System.out.println("EMPTY GRAPHS REMOVED");
				}
				emptyGraphsRemoved++;
			}
		}

		if (frequentVertexLabels.size() != 0) {
			if (DEBUG_MODE) {
				System.out.println("Starting depth-first search...");
			}

			// Start the depth-first search
			gSpanDFS(new DFSCode(), graphDB, graphIds);
		}
	}

	/**
	 * Pair
	 */
	class Pair {
		/** a value */
		int x;
		/** another value */
		int y;

		Pair(int x, int y) {
			if (x < y) {
				this.x = x;
				this.y = y;
			} else {
				this.x = y;
				this.y = x;
			}
		}

		@Override
		public boolean equals(Object obj) {
			Pair other = (Pair) obj;
			return other.x == this.x && other.y == this.y;
		}

		@Override
		public int hashCode() {
			return x + 100 * y;
		}
	}

	/**
	 * Create the pruning matrix
	 */
	private void removeInfrequentVertexPairs(List<Graph> graphDB) {

		Set<Pair> alreadySeenPair;
		SparseTriangularMatrix matrix;
		if (ELIMINATE_INFREQUENT_EDGE_LABELS) {
			if (DEBUG_MODE) {
				System.out.println("Calculating the pruning matrix...");
			}
			matrix = new SparseTriangularMatrix();
			alreadySeenPair = new HashSet<Pair>();
		}

		Set<Integer> alreadySeenEdgeLabel;
		Map<Integer, Integer> mapEdgeLabelToSupport;
		if (ELIMINATE_INFREQUENT_EDGE_LABELS) {
			mapEdgeLabelToSupport = new HashMap<Integer, Integer>();
			alreadySeenEdgeLabel = new HashSet<Integer>();
		}

		// CALCULATE THE SUPPORT OF EACH ENTRY
		for (Graph g : graphDB) {
			Vertex[] vertices = g.getAllVertices();

			for (int i = 0; i < vertices.length; i++) {
				Vertex v1 = vertices[i];
				int labelV1 = v1.getLabel();

				for (Edge edge : v1.getEdgeList()) {
					int v2 = edge.another(v1.getId());
					int labelV2 = g.getVLabel(v2);

					if (ELIMINATE_INFREQUENT_EDGE_LABELS) {
						// Update vertex pair count
						Pair pair = new Pair(labelV1, labelV2);
						boolean seen = alreadySeenPair.contains(pair);
						if (!seen) {
							matrix.incrementCount(labelV1, labelV2);
							alreadySeenPair.add(pair);
						}
					}

					if (ELIMINATE_INFREQUENT_EDGE_LABELS) {
						// Update edge label count
						int edgeLabel = edge.getEdgeLabel();
						if (!alreadySeenEdgeLabel.contains(edgeLabel)) {
							alreadySeenEdgeLabel.add(edgeLabel);

							Integer edgeSupport = mapEdgeLabelToSupport.get(edgeLabel);
							if (edgeSupport == null) {
								mapEdgeLabelToSupport.put(edgeLabel, 1);
							} else {
								mapEdgeLabelToSupport.put(edgeLabel, edgeSupport + 1);
							}
						}
					}
				}
			}
			if (ELIMINATE_INFREQUENT_VERTEX_PAIRS) {
				alreadySeenPair.clear();
			}
			if (ELIMINATE_INFREQUENT_EDGE_LABELS) {
				alreadySeenEdgeLabel.clear();
			}
		}

		alreadySeenPair = null;

		// REMOVE INFREQUENT ENTRIES FROM THE MATRIX
		if (ELIMINATE_INFREQUENT_VERTEX_PAIRS) {
			if (DEBUG_MODE) {
				System.out.println("Removing infrequent pairs...  minsup = " + minSup);
			}
			matrix.removeInfrequentEntriesFromMatrix(minSup);
		}

		// REMOVE INFREQUENT EDGES
		if (ELIMINATE_INFREQUENT_VERTEX_PAIRS || ELIMINATE_INFREQUENT_EDGE_LABELS) {
			// CALCULATE THE SUPPORT OF EACH ENTRY
			for (Graph g : graphDB) {
				Vertex[] vertices = g.getAllVertices();

				for (int i = 0; i < vertices.length; i++) {
					Vertex v1 = vertices[i];
					int labelV1 = v1.getLabel();

					Iterator<Edge> iter = v1.getEdgeList().iterator();
					while (iter.hasNext()) {
						Edge edge = (Edge) iter.next();
						int v2 = edge.another(v1.getId());
						int labelV2 = g.getVLabel(v2);

						int count = matrix.getSupportForItems(labelV1, labelV2);
						if (ELIMINATE_INFREQUENT_VERTEX_PAIRS && count < minSup) {
							iter.remove();

							infrequentVertexPairsRemoved++;
						} else if (ELIMINATE_INFREQUENT_EDGE_LABELS
								&& mapEdgeLabelToSupport.get(edge.getEdgeLabel()) < minSup) {
							iter.remove();
							edgeRemovedByLabel++;
						}
					}

				}
			}
		}
	}

	/**
	 * Recursive method to perform the depth-first search
	 * 
	 * @param c        the current DFS code
	 * @param graphDB  the graph database
	 * @param graphIds the ids of graph where the graph "c" appears
	 * @throws IOException            exception if error writing/reading to file
	 * @throws ClassNotFoundException if error casting a class
	 */
	private void gSpanDFS(DFSCode c, List<Graph> graphDB, Set<Integer> graphIds)
			throws IOException, ClassNotFoundException {
		// If we have reached the maximum size, we do not need to extend this graph
		if (c.size() == maxNumberOfEdges - 1) {
			return;
		}

		// Find all the extensions of this graph, with their support values
		// They are stored in a map where the key is an extended edge, and the value is
		// the
		// is the list of graph ids where this edge extends the current subgraph c.
		Map<ExtendedEdge, Set<Integer>> extensions = rightMostPathExtensions(c, graphDB, graphIds);

		// For each extension
		if (extensions != null) {
			for (Map.Entry<ExtendedEdge, Set<Integer>> entry : extensions.entrySet()) {

				// Get the support
				Set<Integer> newGraphIDs = entry.getValue();
				int sup = newGraphIDs.size();

				// if the support is enough
				if (sup >= minSup) {

					// Create the new DFS code of this graph
					DFSCode newC = c.copy();
					ExtendedEdge extension = entry.getKey();
					newC.add(extension);

					// if the resulting graph is canonical (it means that the graph is non
					// redundant)
					if (isCanonical(newC)) {

						// Save the graph
						FrequentSubgraph subgraph = new FrequentSubgraph(newC, newGraphIDs, sup);
						frequentSubgraphs.add(subgraph);

						// Try to extend this graph to generate larger frequent subgraphs
						gSpanDFS(newC, graphDB, newGraphIDs);
					}
				}
			}
		}
		// check the memory usage
		MemoryLogger.getInstance().checkMemory();
	}

	/**
	 * Check if a DFS code is canonical
	 * 
	 * @param c a DFS code
	 * @return true if it is canonical, and otherwise, false.
	 */
	private boolean isCanonical(DFSCode c) {
		DFSCode canC = new DFSCode();
		for (int i = 0; i < c.size(); i++) {
			Map<ExtendedEdge, Set<Integer>> extensions = rightMostPathExtensionsFromSingle(canC, new Graph(c));
			ExtendedEdge minEE = null;
			for (ExtendedEdge ee : extensions.keySet()) {
				if (ee.smallerThan(minEE))
					minEE = ee;
			}

			if (minEE.smallerThan(c.getAt(i)))
				return false;
			canC.add(minEE);
		}
		return true;
	}

	/**
	 * This method finds all frequent vertex labels from a graph database.
	 * 
	 * @param graphDB                a graph database
	 * @param outputFrequentVertices if true, the frequent vertices will be output
	 */
	private void findAllOnlyOneVertex(List<Graph> graphDB, boolean outputFrequentVertices) {

		frequentVertexLabels = new ArrayList<Integer>();

		// Create a map (key = vertex label, value = graph ids)
		// to count the support of each vertex
		Map<Integer, Set<Integer>> labelM = new HashMap<>();

		// For each graph
		for (Graph g : graphDB) {
			// For each vertex
			for (Vertex v : g.getNonPrecalculatedAllVertices()) {

				// if it has some edges
				if (!v.getEdgeList().isEmpty()) {

					// Get the vertex label
					Integer vLabel = v.getLabel();

					// Store the graph id in the map entry for this label
					// if it is not there already
					Set<Integer> set = labelM.get(vLabel);
					if (set == null) {
						set = new HashSet<>();
						labelM.put(vLabel, set);
					}
					set.add(g.getId());
				}
			}
		}

		// For each vertex label
		for (Entry<Integer, Set<Integer>> entry : labelM.entrySet()) {
			int label = entry.getKey();

			// if it is a frequent vertex, then record that as a frequent subgraph
			Set<Integer> tempSupG = entry.getValue();
			int sup = tempSupG.size();
			if (sup >= minSup) {
				frequentVertexLabels.add(label);

				// if the user wants to output one vertex frequent subgraph
				if (outputFrequentVertices) {
					DFSCode tempD = new DFSCode();
					tempD.add(new ExtendedEdge(0, 0, label, label, -1));

					frequentSubgraphs.add(new FrequentSubgraph(tempD, tempSupG, sup));
				}
			} else if (ELIMINATE_INFREQUENT_VERTICES) {
				// for each graph
				for (Integer graphid : tempSupG) {
					Graph g = graphDB.get(graphid);

					g.removeInfrequentLabel(label);
					infrequentVerticesRemovedCount++;
				}
			}
		}
	}

	/**
	 * Print statistics about the algorithm execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  GSPAN v2.40 - STATS =============");
		System.out.println(" Number of graph in the input database: " + graphCount);
		System.out.println(" Frequent subgraph count : " + patternCount);
		System.out.println(" Total time ~ " + runtime + " s");
		System.out.println(" Minsup : " + minSup + " graphs");
		System.out.println(" Maximum memory usage : " + maxmemory + " mb");

		if(DEBUG_MODE) {
			if (ELIMINATE_INFREQUENT_VERTEX_PAIRS || ELIMINATE_INFREQUENT_VERTICES) {
				System.out.println("  -------------------");
			}
			if (ELIMINATE_INFREQUENT_VERTICES) {
				System.out.println("  Number of infrequent vertices pruned : " + infrequentVerticesRemovedCount);
				System.out.println("  Empty graphs removed : " + emptyGraphsRemoved);
			}
			if (ELIMINATE_INFREQUENT_VERTEX_PAIRS) {
				System.out.println("  Number of infrequent vertex pairs pruned : " + infrequentVertexPairsRemoved);
			}
			if (ELIMINATE_INFREQUENT_EDGE_LABELS) {
				System.out.println("  Number of infrequent edge labels pruned : " + edgeRemovedByLabel);
			}
			if (EDGE_COUNT_PRUNING) {
				System.out.println("  Extensions skipped (edge count pruning) : " + pruneByEdgeCountCount);
			}
			if (SKIP_STRATEGY) {
				System.out.println("  Skip strategy count : " + skipStrategyCount);
			}
		}
		System.out.println("===================================================");
	}
}
