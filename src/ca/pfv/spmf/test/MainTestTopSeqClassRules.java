package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.sequential_rules.topseqrules_and_tns.AlgoTopSeqClassRules;
import ca.pfv.spmf.datastructures.redblacktree.RedBlackTree;
import ca.pfv.spmf.input.sequence_database_array_integers.SequenceDatabase;

/**
 *  * Example of how to use the TopSeqClasRules algorithm in source code.
 * @author Philippe Fournier-Viger (Copyright 2018)
 */
public class MainTestTopSeqClassRules {

	public static void main(String [] arg) throws IOException{
		// load database
		SequenceDatabase sequenceDatabase = new SequenceDatabase(); 
		try {
			sequenceDatabase.loadFile(fileToPath("contextPrefixSpan.txt"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		sequenceDatabase.printDatabaseStats();
		 
		int k = 70;
		double minconf = 0.5;
		
		// the item to be used as consequent for generating rules
		int[] itemToBeUsedAsConsequent = new int[]{1,2};
//
		AlgoTopSeqClassRules algo = new AlgoTopSeqClassRules();
		
//		// This optional parameter allows to specify the maximum number of items in the 
//		// left side (antecedent) of rules found:
//		algo.setMaxAntecedentSize(1);  // optional
		
//		// This optional parameter allows to specify the maximum number of items in the 
//		// right side (consequent) of rules found:
//		algo.setMaxConsequentSize(1);  // optional
		
		RedBlackTree<ca.pfv.spmf.algorithms.sequential_rules.topseqrules_and_tns.ClassRule> rules 
		 = algo.runAlgorithm(k, sequenceDatabase, minconf, itemToBeUsedAsConsequent);
		algo.printStats();
		algo.writeResultTofile(".//output.txt");   // to save results to file
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestTopSeqClassRules.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
