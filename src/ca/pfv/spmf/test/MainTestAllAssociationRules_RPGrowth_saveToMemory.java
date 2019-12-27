package ca.pfv.spmf.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AlgoAgrawalFaster94;
import ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AssocRules;
import ca.pfv.spmf.algorithms.frequentpatterns.rpgrowth.AlgoRPGrowth;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;

/**
 * Example of how to mine all rare association rules with RPGROWTH,
 * from the source code.
 * 
 * @author Philippe Fournier-Viger (Copyright 2008), Ryan Benton, Blake Johns
 */
public class MainTestAllAssociationRules_RPGrowth_saveToMemory {
	public static void main(String [] arg) throws FileNotFoundException, IOException{
		// Loading the binary context
		String input = fileToPath("contextRP.txt");
		
		// By changing the following lines to some other values
		// it is possible to restrict the number of items in the antecedent and
		// consequent of rules
		int maxConsequentLength = 100;
		int maxAntecedentLength = 100;
		
		// STEP 1: Applying the RP-GROWTH algorithm (An implementation of the FP-Growth algorithm) to find rare item sets     
		double minsup = .8;
		double minraresup = 0.2;
		AlgoRPGrowth rpgrowth = new AlgoRPGrowth();
		rpgrowth.setMaximumPatternLength(maxAntecedentLength + maxConsequentLength);
		Itemsets patterns = rpgrowth.runAlgorithm(input, null, minsup, minraresup);
		int databaseSize = rpgrowth.getDatabaseSize();
		patterns.printItemsets(databaseSize);
		//rpgrowth.printStats();
		
		//STEP 2: Generating all rules from the set of rare item sets (based on Agrawal & Srikant, 94)
		double  minconf = 0.60;
		AlgoAgrawalFaster94 algoAgrawal = new AlgoAgrawalFaster94();
		// 
		algoAgrawal.setMaxConsequentLength(maxConsequentLength);
		algoAgrawal.setMaxAntecedentLength(maxAntecedentLength);
		
		// the next line run the algorithm.
		// Note: we pass null as output file path, because we don't want
		// to save the result to a file, but keep it into memory.
		AssocRules rules = algoAgrawal.runAlgorithm(patterns, null, databaseSize, minconf);
		rules.printRules(databaseSize);
		System.out.println("DATABASE SIZE " + databaseSize);

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestAllAssociationRules_FPGrowth_saveToMemory.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}