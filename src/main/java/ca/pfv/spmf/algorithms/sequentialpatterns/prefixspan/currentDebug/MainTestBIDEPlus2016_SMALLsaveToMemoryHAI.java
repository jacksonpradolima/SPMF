package ca.pfv.spmf.algorithms.sequentialpatterns.prefixspan.currentDebug;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;


/**
 * Example of how to use the BIDE+ algorithm, from the source code.
 * 
 * @author Philippe Fournier-Viger
 */
public class MainTestBIDEPlus2016_SMALLsaveToMemoryHAI {

	public static void main(String [] arg) throws IOException{    
		// input sequence database file path
		String input = fileToPath("example.txt");
//		String output = "./output.txt";
		
		// Create an instance of the algorithm
		AlgoBIDEPlus algo  = new AlgoBIDEPlus();
		
        // if you set the following parameter to true, the sequence ids of the sequences where
        // each pattern appears will be shown in the result
        boolean showSequenceIdentifiers = true; 
		
		// execute the algorithm
        algo.setShowSequenceIdentifiers(showSequenceIdentifiers);
		SequentialPatterns patterns = algo.runAlgorithm(input, null, 1);    
		System.out.println(" == PATTERNS FOUND ==");
		for(List<SequentialPattern> level : patterns.levels) {
			for(SequentialPattern pattern : level){
				System.out.println(pattern + " support : " + pattern.getAbsoluteSupport());
			}
		}
		// print statistics
		algo.printStatistics();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestBIDEPlus2016_SMALLsaveToMemoryHAI.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}