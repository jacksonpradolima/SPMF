package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.sequentialpatterns.phuspm.AlgoPHUSPM;

public class MainTestPHUSPM {

	public static void main(String[] args) throws ClassNotFoundException, IOException {

		// The input and output file path
		String input = fileToPath("contextPHUSPM.txt");
		String output = "output.txt";
		
		// The parameters
		int minUtility = 20;
		float minProbability = (float) 1.4;
		
		// Run the algorithm
		AlgoPHUSPM algorithm = new AlgoPHUSPM();
		algorithm.runAlgorithm(input, output, minUtility, minProbability);
		algorithm.printStats();

	}
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestPHUSPM.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
