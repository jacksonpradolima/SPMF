package ca.pfv.spmf.algorithmmanager.descriptions;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.algorithms.frequentpatterns.MRCPPS.AlgoMRCPPS;

/**
 * This class describes the MRCPPS algorithm parameters. It is designed to be used by the graphical and command line interface.
 * 
 * @see AlgoMRCPPS
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoMRCPPS extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoMRCPPS(){
	}

	@Override
	public String getName() {
		return "MRCPPS";
	}

	@Override
	public String getAlgorithmCategory() {
		return "PERIODIC PATTERN MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/MRCPPS.php";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws Exception {

		int maxSup = getParamAsInteger(parameters[0]);

		// Max standard deviation
		double maxStd = getParamAsDouble(parameters[1]);

		// Minimum support
		double minBond = getParamAsDouble(parameters[2]);
		
		// Min RA
		double minRa = getParamAsDouble(parameters[3]);

		// Run the algorithm
		AlgoMRCPPS algorithm = new AlgoMRCPPS();
		algorithm.runAlgorithm(inputFile, outputFile, maxSup, maxStd, 
				minBond, minRa, true, false, 0);
		algorithm.printStats();
	}
	

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[4];
		parameters[0] = new DescriptionOfParameter("MaxSup ", "(e.g. 2)", Integer.class, false);
		parameters[1] = new DescriptionOfParameter("MaxStd ", "(default: 1)", Double.class, false);
		parameters[2] = new DescriptionOfParameter("MinBond ", "(e.g. 0.5)", Double.class, false);
		parameters[3] = new DescriptionOfParameter("MinRa ", "(e.g. 0.5)", Double.class, false);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Yang Peng, Zhitian Li, Philippe Fournier-Viger";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Database of instances","Sequence database", "Simple sequence database"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Patterns", "Periodic patterns", "Periodic rare patterns", "Rare correlated itemsets common to multiple sequences"};
	}
	
}
