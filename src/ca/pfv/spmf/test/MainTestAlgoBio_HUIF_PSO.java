package ca.pfv.spmf.test;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.biohuif.AlgoBio_HUIF_PSO;

/**
 * Example of how to use HUIF-PSO algorithm from the source code.
 */
public class MainTestAlgoBio_HUIF_PSO {
	public static void main(String [] arg) throws IOException{
		
		String input = fileToPath("contextHUIM.txt");
		
		String output = "output.txt";
		int min_utility = 40;  // 
		
		// apply the algorithm
		AlgoBio_HUIF_PSO algorithm = new AlgoBio_HUIF_PSO();
		algorithm.runAlgorithm(input, output, min_utility);
		algorithm.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestAlgoBio_HUIF_PSO.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
