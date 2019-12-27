package ca.pfv.spmf.test;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.sppgrowth.AlgoSPPgrowth;

/**
 * This example shows how to use the SPP-Growth algorithm
 * @author Philippe Fournier-Viger 2019
 *
 */
public class MainTestSPPGrowth {

    public static void main(String[] args) throws IOException {

        // the Input and output files
        String inputFile = fileToPath("contextSPPGrowth.txt");
        String outputFile = "output.txt";

        // The algorithm parameters:
        int maxPer = 2;
        int minSup = 3;
        int maxLa = 2;
        
        // If the input file does not contain timestamps, then set this variable to true
        // to automatically assign timestamps as 1,2,3...
        boolean selfIncrement = false;

        // self-growth = flase only for online_minute.txt , others are true
        AlgoSPPgrowth algo = new AlgoSPPgrowth();
        algo.runAlgorithm(inputFile, outputFile,maxPer,minSup,maxLa,selfIncrement);
        algo.printStats();
    }
    
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestSPPGrowth.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
