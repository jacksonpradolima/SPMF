package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.IOException;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.algorithms.graph_mining.tkg.AlgoTKG;

/**
 * This class describes the TKG algorithm parameters. It is designed to be used by the graphical and command line interface.
 * 
 * @see AlgoTKG
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoTKG extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoTKG(){
	}

	@Override
	public String getName() {
		return "TKG";
	}

	@Override
	public String getAlgorithmCategory() {
		return "GRAPH PATTERN MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/TKG.php";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException, ClassNotFoundException {

		// set the minimum support threshold
		int k = getParamAsInteger(parameters[0]);
		
		int maxNumberOfEdges = Integer.MAX_VALUE;
		
		// The maximum number of edges for frequent subgraph patterns
		if (parameters.length >=2 && "".equals(parameters[1]) == false) {
			maxNumberOfEdges = getParamAsInteger(parameters[1]);
		}
		
		// If true, single frequent vertices will be output
		boolean outputSingleFrequentVertices = true;
		
		if (parameters.length >=3 && "".equals(parameters[2]) == false) {
			outputSingleFrequentVertices = getParamAsBoolean(parameters[2]);
		}
		
		// If true, a dot file will be output for visualization using GraphViz
		boolean outputDotFile = false;
		
		if (parameters.length >=4 && "".equals(parameters[3]) == false) {
			outputDotFile = getParamAsBoolean(parameters[3]);
		}
		
		// Output the ids of graph containing each frequent subgraph
		boolean outputGraphIds = true;
		
		if (parameters.length >=5 && "".equals(parameters[4]) == false) {
			outputGraphIds = getParamAsBoolean(parameters[4]);
		}
		
		// Apply the algorithm 
		AlgoTKG algo = new AlgoTKG();
		algo.runAlgorithm(inputFile, outputFile, k, outputSingleFrequentVertices, 
				outputDotFile, maxNumberOfEdges, outputGraphIds);
		
		// Print statistics about the algorithm execution
		algo.printStats();
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[5];
		parameters[0] = new DescriptionOfParameter("k", "(e.g. 3)", Double.class, false);
		parameters[1] = new DescriptionOfParameter("Max Number of Edges", "(e.g. 2)", Boolean.class, true);
		parameters[2] = new DescriptionOfParameter("Output Single Vertices", "(e.g. true)", Boolean.class, true);
		parameters[3] = new DescriptionOfParameter("Output DOT file", "(e.g. false)", Boolean.class, true);
		parameters[4] = new DescriptionOfParameter("Output Graph IDs", "(e.g. true)", Boolean.class, true);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Philippe Fournier-Viger and Chao Cheng";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Database of instances","Graph database", "Labeled graph database"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Patterns", "Subgraphs", "Top-k Frequent subgraphs"};
	}
	
}
