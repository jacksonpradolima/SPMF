package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.sequentialpatterns.clofast.AlgoFast;

/**
 * Example of how to use the Fast algorithm in source code.
 */
public class MainTestFast {

	public static void main(String [] arg) throws IOException{   
		
		
		// input file
		String inputFile = fileToPath("contextPrefixSpan.txt");
		
		// output file path
		String outputPath = ".//output.txt";
		
        // Create the algoritm
        AlgoFast algorithm = new AlgoFast();

		// The minsup parameter will be set to 50 %
		float minsup = 0.4f;
		
		// An optional parameter is to specify a maximum support
		// Uncomment the two following lines to use it:
//		float maxsup = 0.8f;
//      algorithm.setMaximumSupport(maxsup);
		
		//=== Run the algorithm - this is the first way
		// - It will read the dataset and run the algorithm.
        algorithm.runAlgorithm(inputFile, outputPath, minsup);
        algorithm.printStatistics();
        
		//=== THIS IS THE SECOND WAY OF RUNNING CLOFAST =====
        // It should be used if you want to run CloFast several times
        // on the same dataset. In that case, the total execution time
        // does not include the time for reading the database
        
//		FastDataset dataset = FastDataset.fromPrefixspanSource(inputFile, minsup);
//        CloFast algorithm = new CloFast();
//        algorithm.runAlgorithm(dataset, outputPath, minsup);
//        
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestFast.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}