package ca.pfv.spmf.test;

import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.tools.other_dataset_tools.TransactionUtilityRemover;

/**
 * Class file that shows how to remove the utility value from a transaction database having utility values
 * using the tool called "TransactionUtilityRemover".
 * @author Philippe Fournier-Viger
 *
 */
public class MainTestTransactionUtilityRemover {
	
	public static void main(String[] args) throws Exception{
		
		// input file path
		String input = fileToPath("DB_Utility.txt");
		// output file path
		String output = ".//output.txt";
		
		// Create the tool to remove utility values
		TransactionUtilityRemover remover = new TransactionUtilityRemover();
		// Run the algorithm
		remover.convert(input, output);
		// The result will be saved to the output file path
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestTransactionUtilityRemover.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
