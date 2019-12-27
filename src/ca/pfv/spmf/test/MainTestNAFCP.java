package ca.pfv.spmf.test;

import ca.pfv.spmf.algorithms.frequentpatterns.nafcp.AlgoNAFCP;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

/**
 * Example of how to use the NAFCP algorithm from the source code.

 * @author Nader Aryabarzan (Copyright 2019)
 * @Email aryabarzan@aut.ac.ir or aryabarzan@gmail.com
 */

public class MainTestNAFCP {

    public static void main(String [] arg) throws IOException {

		String input = fileToPath("contextPasquier99.txt");
        String output = "output.txt";  // the path for saving the frequent itemsets found

        double minsup = 0.4; // means a minsup of 2 transaction (we used a relative count)

        // Applying the algorithm
        AlgoNAFCP algorithm = new AlgoNAFCP();

        algorithm.runAlgorithm(input, minsup, output);
        algorithm.printStats();
    }

    public static String fileToPath(String filename) throws UnsupportedEncodingException {
        URL url = MainTestNAFCP.class.getResource(filename);
        return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
    }
}

