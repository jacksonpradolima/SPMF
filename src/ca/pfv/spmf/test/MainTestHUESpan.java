package ca.pfv.spmf.test;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.episodes.huespan.AlgoHUESpan;

/**
 * This file shows how to run the HUE_Span algorithm on an input file.
 * @author Peng yang
 */
public class MainTestHUESpan {
    public static void main(String[] args) throws IOException {

        // the Input and output files
        String inputFile = fileToPath("contextHUE_Span.txt");
        String outputFile = "output.txt";

        /** The minimum utility ratio parameter*/
        double minUtilityRatio = 0.45;
        /** the maximum duration parameter */
        int maxDuration = 3;
        
        // ====== Other parameters below are optional ===/

        /** 
         * If true, episodes containing a single event will be output
         */
        boolean outputSingleEvents = true;  
        
        /** If true, the algorithm will use the maximum utility as proposed in the HUE-SPAN paper instead of the traditional
         * definition. The maximum utility can be argued to be a better definition.
         */
        boolean checkMaximumUtility = true;

        /** The two following variables allows to activate or deactivate the optimizations proposed in the research paper.
         * Normally, we leave these variables to true, unless we want to do some performance experiments.
         */
        boolean useTighterUpperBound = true;
        boolean useCoocMatrix = true;
        
        /** 
         *  if true, pruning episode by checking their prefix
         */
        boolean pruningPrefix = true;

        // ========================================================
        
        /** Run the algorithm */
        AlgoHUESpan algo = new AlgoHUESpan();
        algo.runAlgorithm(inputFile,outputFile,minUtilityRatio,maxDuration,checkMaximumUtility,useTighterUpperBound,outputSingleEvents,useCoocMatrix,pruningPrefix);
        algo.printStats();
    }

    public static String fileToPath(String filename) throws UnsupportedEncodingException{
        URL url = MainTestHUESpan.class.getResource(filename);
        return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
    }
}
