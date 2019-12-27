package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.graph_mining.tkg.AlgoTKG;

/**
 * Example of how to use the GSPAN algorithm 
 * from the source code and output the result to a file.
 * @author Chao Cheng & Philippe Fournier-Viger 2019
 */
public class MainTestTKG {

	public static void main(String [] arg) throws IOException, ClassNotFoundException{

		// set the input and output file path
		String input = fileToPath("contextTKG.txt");
		String output = ".//output.txt";

		// set the minimum support threshold
		int k = 3;
		
		// The maximum number of edges for frequent subgraph patterns
		int maxNumberOfEdges = Integer.MAX_VALUE;
		
		// If true, single frequent vertices will be output
		boolean outputSingleFrequentVertices = true;
		
		// If true, a dot file will be output for visualization using GraphViz
		boolean outputDotFile = false;
		
		// Output the ids of graph containing each frequent subgraph
		boolean outputGraphIds = true;
		
		// Apply the algorithm 
		AlgoTKG algo = new AlgoTKG();
		algo.runAlgorithm(input, output, k, outputSingleFrequentVertices, 
				outputDotFile, maxNumberOfEdges, outputGraphIds);
		
		// Print statistics about the algorithm execution
		algo.printStats();
	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestTKG.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
