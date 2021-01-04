package ca.pfv.spmf.test;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;

import ca.pfv.spmf.algorithms.sequentialpatterns.qcsp.AlgoQCSP;
import ca.pfv.spmf.algorithms.sequentialpatterns.qcsp.SequentialPattern;
import ca.pfv.spmf.algorithms.sequentialpatterns.qcsp.util.Pair;

/**
 * Example of how to use the QCSP algorithm from the source code.
 * @author Len Feremans, 2020.
 */
public class MainTestQCSP_saveToMemory {

	public static void main(String [] arg) throws Exception{
		
		//------------ Parameters ----------------------//
		//single sequence: 0 is used for gaps
		//example from paper
		String input = fileToPath("contextQCSP.txt");  
	
		
		// if true, show debug information
		boolean showDebugInformation = true;
		
		// frequency threshold on single item
		int minsup = 1;
		
		// threshold on window, relative to pattern length. alpha=1 -> no gaps, 
		// alpha=2  -> |X| * 2 gaps allowed for (quantile-based cohesive) sequential pattern occurrence
		double alpha = 2;

		//  this is the maximum sequential pattern length
		int maximumSequentialPatternLength = 3;
		
		// top-k sequential patterns, ranked on quantile-based cohesion, to return
		int topK = 4;

		//--------------- Applying the  algorithm  ---------//
		AlgoQCSP algorithm = new AlgoQCSP();
		algorithm.setDebug(showDebugInformation);
		List<Pair<SequentialPattern,Double>> patterns = algorithm.runAlgorithm(input, null, minsup, alpha, maximumSequentialPatternLength, topK);
		for(Pair<SequentialPattern,Double> pattern: patterns) {
			List<Integer> sp = pattern.getFirst().pattern;
			Double cohesion  = pattern.getSecond();
			int support = algorithm.support(sp);
			String patternToString = String.format(">>>>%s\t#SUP: %d\t#QCOH: %.3f", sp, support, cohesion);
			System.out.println(patternToString);
		}
		
		// Print statistics
		algorithm.printStatistics();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestQCSP_saveToMemory.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
