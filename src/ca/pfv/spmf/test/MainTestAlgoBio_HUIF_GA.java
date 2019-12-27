package ca.pfv.spmf.test;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.biohuif.AlgoBio_HUIF_GA;

/**
 * Example of how to use HUIF-GA algorithm from the source code.
 */
public class MainTestAlgoBio_HUIF_GA {
	public static void main(String [] arg) throws IOException{
		
		String input = fileToPath("contextHUIM.txt");
		
		String output = "output.txt";
		int min_utility = 40;  // 
		
		AlgoBio_HUIF_GA algorithm = new AlgoBio_HUIF_GA();
		algorithm.runAlgorithm(input, output, min_utility);
		algorithm.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestAlgoBio_HUIF_GA.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}


}
