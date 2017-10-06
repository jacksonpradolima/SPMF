package ca.pfv.spmf.algorithmmanager.descriptions;

import java.util.Calendar;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.algorithms.associationrules.gcd.GCDAssociationRules;

/**
 * This class describes parameters of the algorithm for generating association rules 
 * with the GCD algorithm. 
 * It is designed to be used by the graphical and command line interface.
 * 
 * @see GCDAssociationRules
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoGCDAssociationRules extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoGCDAssociationRules(){
	}

	@Override
	public String getName() {
		return "GCD_association_rules";
	}

	@Override
	public String getAlgorithmCategory() {
		return "ASSOCIATION RULE MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://www.philippe-fournier-viger.com/spmf/documentation.php#gcd";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile)throws Exception {
		double minsup = getParamAsDouble(parameters[0]);
		double minconf = getParamAsDouble(parameters[1]);
		int maxcomb = getParamAsInteger(parameters[2]);

		GCDAssociationRules gcdRunner = new GCDAssociationRules(inputFile, outputFile, 
				minsup,				minconf, maxcomb);

		long start = Calendar.getInstance().getTimeInMillis();
		gcdRunner.runAlgorithm();
		long end = Calendar.getInstance().getTimeInMillis();
		long ms = end - start;
		System.out.println("-------");
		System.out.println("Number of rules found: " + gcdRunner.getPatternCount());
		System.out.println("Total in milliseconds: " + ms + " ms");
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[3];
		parameters[0] = new DescriptionOfParameter("Minsup (%)", "(e.g. 0.5 or 50%)", Double.class, false);
		parameters[1] = new DescriptionOfParameter("Minconf (%)", "(e.g. 0.6 or 60%)", Double.class, false);
		parameters[2] = new DescriptionOfParameter("Maxcomb (%)", "(e.g. 3)", Integer.class, false);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Ahmed El-Serafy, Hazem El-Raffiee";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Database of instances","Transaction database", "Simple transaction database"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Patterns", "Association rules"};
	}
	
}
