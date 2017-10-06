package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.tools.dataset_stats.TransactionStatsGenerator;

/**
 * Example of how to get statistics about a transaction database in SPMF format
 */
public class MainTestGenerateTransactionDatabaseStats {
	
	public static void main(String [] arg) throws IOException{
		
		String inputFile = fileToPath("contextPasquier99.txt");
		try{
			TransactionStatsGenerator transDBStats = new TransactionStatsGenerator(); 
			transDBStats.getStats(inputFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestGenerateTransactionDatabaseStats.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
