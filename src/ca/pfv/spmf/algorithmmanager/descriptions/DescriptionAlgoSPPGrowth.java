package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.IOException;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.algorithms.frequentpatterns.sppgrowth.AlgoSPPgrowth;

/**
 * This class describes the SPPGrowth algorithm parameters. 
 * It is designed to be used by the graphical and command line interface.
 * 
 * @see AlgoSPPgrowth
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoSPPGrowth extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoSPPGrowth(){
	}

	@Override
	public String getName() {
		return "SPPGrowth";
	}

	@Override
	public String getAlgorithmCategory() {
		return "PERIODIC PATTERN MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/SPPGrowth.php";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {
		// Read the parameters
		int maxPer = getParamAsInteger(parameters[0]);  
		int minSup = getParamAsInteger(parameters[1]); 
		int maxLA = getParamAsInteger(parameters[2]);  
		boolean noTimestamps = getParamAsBoolean(parameters[3]);  

		// Apply the algorithm
		AlgoSPPgrowth algo = new AlgoSPPgrowth();
		
		algo.runAlgorithm(inputFile, outputFile, maxPer,minSup,maxLA,noTimestamps);
		algo.printStats();
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[4];
		parameters[0] = new DescriptionOfParameter("Maximum periodicity", "(e.g. 2 transactions)", Integer.class, false);
		parameters[1] = new DescriptionOfParameter("Minimum support", "(e.g. 3 transactions)", Integer.class, false);
		parameters[2] = new DescriptionOfParameter("Maximum lability", "(e.g. 2)", Integer.class, false);
		parameters[3] = new DescriptionOfParameter("Has no timestamps?", "(e.g. true)", Boolean.class, false);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Peng Yang and Philippe Fournier-Viger";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Database of instances","Transaction database", "Simple transaction database"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Patterns", "Frequent patterns", "Periodic patterns", "Periodic frequent patterns", "Stable Periodic frequent itemsets"};
	}
	
}
