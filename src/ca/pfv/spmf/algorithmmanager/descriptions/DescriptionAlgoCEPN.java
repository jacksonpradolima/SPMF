package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.IOException;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.algorithms.sequentialpatterns.cost.AlgoCEPM;

/**
 * This class describes the CEPN algorithm parameters. 
 * It is designed to be used by the graphical and command line interface.
 * 
 * @see AlgoCEPM
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoCEPN extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoCEPN(){
	}

	@Override
	public String getName() {
		return "CEPN";
	}

	@Override
	public String getAlgorithmCategory() {
		return "SEQUENTIAL PATTERN MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/CEPNcost.php";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {

		// Get the parameter "minsup"
		int minsup = getParamAsInteger(parameters[0]);
		
		double maxcost = getParamAsDouble(parameters[1]);
		
		double minOccupancy = getParamAsDouble(parameters[2]);

		// if true, patterns in the output file are sorted by utility
		boolean sortByUtility = false;

		// if true, only patterns with lowest trade-off are output for each utility
		// value
		boolean outputLowestTradeOff = false;
		
		int maxPatternLength = 999;
		if (parameters.length >=4 && "".equals(parameters[3]) == false) {
			maxPatternLength = getParamAsInteger(parameters[3]);
		}
		
		if (parameters.length >=5 && "".equals(parameters[4]) == false) {
			sortByUtility = getParamAsBoolean(parameters[4]);
		}
		
		if (parameters.length >=6 && "".equals(parameters[5]) == false) {
			outputLowestTradeOff = getParamAsBoolean(parameters[5]);
		}
		
		// Run the algorithm
		AlgoCEPM algo = new AlgoCEPM();
		algo.setMaximumPatternLength(maxPatternLength);
		algo.runAlgorithmCEPN(inputFile, outputFile, minsup, maxcost, minOccupancy, sortByUtility, outputLowestTradeOff);
		algo.printStatistics();
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[6];
		parameters[0] = new DescriptionOfParameter("Minsup", "(e.g. 2)", Double.class, false);
		parameters[1] = new DescriptionOfParameter("Maxcost", "(e.g. 50)", Integer.class, false);
		parameters[2] = new DescriptionOfParameter("Minoccupancy", "(e.g. 0.1)", Double.class, false);
		parameters[3] = new DescriptionOfParameter("Max pattern length", "", Integer.class, true);
		parameters[4] = new DescriptionOfParameter("Sort by utility?", "(default: false)", Boolean.class, true);
		parameters[5] = new DescriptionOfParameter("Only lowest trade-off?", "(default: false)", Boolean.class, true);
//		parameters[3] = new DescriptionOfParameter("Show sequence ids?", "(default: false)", Boolean.class, true);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Jiaxuan Li";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Database of instances","Sequence database", "Sequence Database with cost and numeric utility"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Patterns", "Sequential patterns", "Cost-efficient Sequential patterns"};
	}
}
