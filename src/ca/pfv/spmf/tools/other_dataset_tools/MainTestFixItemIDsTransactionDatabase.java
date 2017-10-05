package ca.pfv.spmf.tools.other_dataset_tools;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

/**
 * Example of how to use the tool to increase all items ids by a given value in a transaction database
 * in SPMF format.
 */
public class MainTestFixItemIDsTransactionDatabase {

	public static void main(String [] arg) throws IOException{
		
		String inputFile = fileToPath("retail_negative.txt");
		String outputFile = "retail_negative2.txt";
		
		// This is a parameter that indicates that we want to increase the item ids by 1
		int increment = 1;

		FixItemIDsTransactionDatabaseTool tool = new FixItemIDsTransactionDatabaseTool();
		tool.convert(inputFile, outputFile, increment);
		

	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestFixItemIDsTransactionDatabase.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
