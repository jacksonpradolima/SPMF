package ca.pfv.spmf.algorithms.graph_mining.tseqminer;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

/**
 * Example of how to use the TSeqMiner algorithm 
 * from the source code and output the result to a file.
 * @author Chao Cheng & Philippe Fournier-Viger 2019
 */
public class MainTestTSeqMiner2 {

	public static void main(String [] arg) throws IOException, ClassNotFoundException{

		// /D:/Workspace/SPMF_2019/bin/ca/pfv/spmf/algorithms/graph_mining/tseqminer/DB_TSEQMINER

		// The input directory containing a dynamic attributed graph
		String inputDirectory = fileToPath("USFLIGHT_TSEQMINER") + File.separator;

		// The output file path
		String outputPath = "output.txt";

		//PARAMETER 1: discretization threshold (float)
		// If the value is 2, it means when (next_value - cur_value) >= 2,
		// the trend is '+' and when (next_value - cur_value) <= -2, the trend is '-'. Otherwise the trend is '0'.
		float discretizationThreshold = 0.1f;

		// PARAMETER 2: A frequent sequence should satisfy that the frequency
		// of the first item in sequence >= minInitSup.
		float minInitSup = 0.0016f;

		// PARAMETER 3: A frequent sequence should satisfy that the number of
		// tail point of the sequence >= minTailSup.
		int minTailSup = 100;

		// PARAMETER 4: A significant sequence should satisfy that the significance
		// between any two items in sequence is >= minSig.
		float minSig = 10.0f;

		// PARAMETER 5: the number of considered attributes  (int)
		int attributeCount = 8;

		// Note that there are more parameters that can be used for TSeqMiner in the source code,
		// in the class ParametersSetting.java.
		// They can be used for very specific cases.


		// Apply the algorithm
		AlgoTSeqMiner algo = new AlgoTSeqMiner();
		algo.runAlgorithm(inputDirectory, outputPath, discretizationThreshold,
				minInitSup, minTailSup, minSig, attributeCount);

		// Print statistics about the algorithm execution
		algo.printStats();
	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestTSeqMiner2.class.getResource(filename);
		return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
