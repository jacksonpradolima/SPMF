package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

import ca.pfv.spmf.algorithms.sequential_rules.rulegrowth.AlgoERMiner;
import ca.pfv.spmf.tools.dataset_converter.SequenceDatabaseConverter;
import ca.pfv.spmf.tools.resultConverter.ResultConverter;

/**
 * Example of how to use the ERMiner algorithm in source code with a text file.
 * 
 * @author Philippe Fournier-Viger
 */
public class MainTestERMiner_textFile {

	public static void main(String[] arg) throws IOException,
			InterruptedException {
		
		// If you want to specify a different encoding for the text file, you can replace this line:
		Charset charset = Charset.defaultCharset();
		// by this line :
//		 Charset charset = Charset.forName("UTF-8");
		// Or other encodings  "UTF-16" etc.

		
		SequenceDatabaseConverter converter = new SequenceDatabaseConverter();
		Map<Integer, String> mapping = converter.convertTEXTandReturnMap(fileToPath("example2.text"), "example2_converted.txt", Integer.MAX_VALUE, charset);

		//  Applying ERMiner algorithm with minsup = 3 sequences and minconf = 0.5

		AlgoERMiner algo = new AlgoERMiner();
		algo.runAlgorithm("example2_converted.txt", "output.txt", 3, 0.5);

		ResultConverter converter2 = new ResultConverter();
		converter2.convert(mapping, "output.txt", "final_output.txt", charset);
		
		// print statistics
		algo.printStats();

	}


	public static String fileToPath(String filename)
			throws UnsupportedEncodingException {
		URL url = MainTestERMiner_textFile.class.getResource(filename);
		return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
	}
}