package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.lhui.AlgoLHUIMiner;
import ca.pfv.spmf.algorithms.frequentpatterns.lhui.AlgoPHUIMiner;

/**
 * Example of how to use the PHUI-Miner algorithm from the source code
 * @author Yimin Zhang, Philippe Fournier-Viger
 * @see AlgoLHUIMiner
 */
public class MainTestPHUIMiner {

	public static void main(String[] args) throws IOException {
		// local minimum utility threshold
		long lminutil = 40;
		
		// lambda
		int lamda = 2;
		
		// window size
		int windowSize = 3;
		
		// Input file 
		String inputFile = fileToPath("DB_LHUI.txt");
		
		// Output file 
		String outputFile = "output.txt";
		
		AlgoPHUIMiner phuiminer = new AlgoPHUIMiner();
		phuiminer.runAlgorithm(inputFile,
				outputFile, lminutil, windowSize, lamda);
		phuiminer.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestLHUIMiner.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}

}
