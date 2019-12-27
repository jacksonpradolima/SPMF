package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AlgoAgrawalFaster94;
import ca.pfv.spmf.algorithms.frequentpatterns.rpgrowth.AlgoRPGrowth;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;

public class MainTestAllAssociationRules_RPGrowth_saveToFile {

	public static void main(String[] args) throws IOException{
		String input = fileToPath("contextRP.txt");
		String output = ".//output.txt";
		
		// By changing the following lines to some other values
		// it is possible to restrict the number of items in the antecedent and
		// consequent of rules
		
		int maxConsequentLength = 3;
		int maxAntecedentLength = 2;
		
		// STEP 1: Applying the RP-GROWTH algorithm to find rare itemsets
		double minsup = 0.8;
		double minraresup = 0.1;		
		AlgoRPGrowth rpgrowth = new AlgoRPGrowth();
		rpgrowth.setMaximumPatternLength(maxAntecedentLength + maxConsequentLength);
		Itemsets patterns = rpgrowth.runAlgorithm(input, null, minsup, minraresup);
		rpgrowth.printStats();
		int databaseSize = rpgrowth.getDatabaseSize();
		
		// STEP 2: Generating all rules from the set of frequent itemsets (based on Agrawal & Srikant, 94)
		double minconf = 0.60;
		AlgoAgrawalFaster94 algoAgrawal = new AlgoAgrawalFaster94();
		algoAgrawal.setMaxAntecedentLength(maxAntecedentLength);
		algoAgrawal.setMaxConsequentLength(maxConsequentLength);
		algoAgrawal.runAlgorithm(patterns, output, databaseSize, minconf);
		algoAgrawal.printStats();
	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestAllAssociationRules_FPGrowth_saveToMemory.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
