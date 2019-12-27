package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.graph_mining.tkg.AlgoGSPAN;

/**
 * Example of how to use the GSPAN algorithm 
 * from the source code and output the result to a file.
 * @author Chao Cheng & Philippe Fournier-Viger 2019
 */
public class MainTestGSPAN {

	public static void main(String [] arg) throws IOException, ClassNotFoundException{

		// set the input and output file path
		String input = fileToPath("contextTKG.txt");
		String output = ".//output.txt";

		// set the minimum support threshold
		double minSupport = 0.9;
		
		// The maximum number of edges for frequent subgraph patterns
		int maxNumberOfEdges = Integer.MAX_VALUE;
		
		// If true, single frequent vertices will be output
		boolean outputSingleFrequentVertices = true;
		
		// If true, a dot file will be output for visualization using GraphViz
		boolean outputDotFile = false;
		
		// Output the ids of graph containing each frequent subgraph
		boolean outputGraphIds = true;
		
		// Apply the algorithm 
		AlgoGSPAN algo = new AlgoGSPAN();
		algo.runAlgorithm(input, output, minSupport, outputSingleFrequentVertices, 
				outputDotFile, maxNumberOfEdges, outputGraphIds);
		
		// Print statistics about the algorithm execution
		algo.printStats();
	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestGSPAN.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
