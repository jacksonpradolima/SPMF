package ca.pfv.spmf.test;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.episodes.minepiplus.AlgoMINEPIPlus;

/**
 * This file shows how to run the MINEPI_PLUS algorithm on an input file.
 * @author Peng yang
 */
public class MainTestMINEPI_PLUS {
    public static void main(String[] args) throws IOException {

        // the Input and output files
        String inputFile = fileToPath("contextEMMA.txt");
        String outputFile = "output.txt";

        // The algorithm parameters:
        int minSup = 2;
        int maxWindow = 2;

        // If the input file does not contain timestamps, then set this variable to true
        // to automatically assign timestamps as 1,2,3...
        boolean selfIncrement = false;

        // self-growth = flase only for online_minute.txt , others are true
        AlgoMINEPIPlus algo = new AlgoMINEPIPlus();
        algo.runAlgorithm(inputFile, outputFile,minSup,maxWindow,selfIncrement);
        algo.printStats();
    }

    public static String fileToPath(String filename) throws UnsupportedEncodingException {
        URL url = MainTestMINEPI_PLUS.class.getResource(filename);
        return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
    }
}
