package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.tools.dataset_stats.TransactionDBUtilityStatsGenerator;

/**
 * Example of how to use the tool to calculate statistics about a
 * transaction database containing utility information
 * 
 * @author Philippe Fournier-Viger, 2010
 */
public class MainTestGenerateTransactionDBUtilityStats {


	public static void main(String [] arg) throws IOException{
		
		// input file path
		String inputFile = fileToPath("DB_Utility.txt");
		
		// Run the algorithm
		TransactionDBUtilityStatsGenerator transDBStats = new TransactionDBUtilityStatsGenerator(); 
		transDBStats.runAlgorithm(inputFile);
	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestGenerateTransactionDatabaseStats.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
