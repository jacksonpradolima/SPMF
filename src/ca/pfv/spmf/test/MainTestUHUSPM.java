package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.sequentialpatterns.uhuspm.AlgoUHUSPM;

/**
 * 
 * @author Ting Li
 *
 */
public class MainTestUHUSPM {

	public static void main(String[] args) throws ClassNotFoundException, IOException {

		// the path of input and output
		String input = fileToPath("contextPHUSPM.txt");
		String output = "output.txt";
		
		// the parameters
		int minUtility = 20;
		float minProbability = (float) 1.4;
		
		// Applying the algorithm
		AlgoUHUSPM algorithm = new AlgoUHUSPM();
		algorithm.runAlgorithm(input, output, minUtility, minProbability);
		
		//output the results
		algorithm.printStats();

	}
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestUHUSPM.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
