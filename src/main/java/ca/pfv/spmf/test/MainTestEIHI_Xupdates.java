package ca.pfv.spmf.test;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoEIHI;

/**
 * Example of how to use the EIHI algorithm from the source code by processing a single file
 * in several parts (updates).
 * 
 * @author Philippe Fournier-Viger, 2014
 */
public class MainTestEIHI_Xupdates {

	public static void main(String[] arg) throws IOException {

		String input = fileToPath("DB_Utility.txt");  // retails_utilityPFV
		int min_utility = 30;
		
		// the number of updates to be performed
		int numberOfUpdates = 2;
		
		// scan the database to count the number of lines
		// for our test purpose
		int linecount = countLines(input);
		
		double addedratio = 1d / ((double) numberOfUpdates);
		int linesForeEachUpdate = (int)(addedratio * linecount);
		
		
		// Apply the algorithm several times
		AlgoEIHI algo = new AlgoEIHI();
		int firstLine = 0;		
		for(int i = 0; i < numberOfUpdates; i++){
			int lastLine = firstLine + linesForeEachUpdate;
			//

			// Applying the algorithm
			// If this is the last update, we make sure to run until the last line
			if(i == numberOfUpdates -1) {
				System.out.println("" + i + ") Run the algorithm using line " + firstLine + " to before line " + linecount + " of the input database.");
				algo.runAlgorithm(input, min_utility, firstLine, linecount);
			}else {
				// If this is not the last update
				System.out.println("" + i + ") Run the algorithm using line " + firstLine + " to before line " + lastLine + " of the input database.");
				algo.runAlgorithm(input, min_utility, firstLine, lastLine);
			}
			algo.printStats();
			
			firstLine = lastLine;
		}
		
		// Print the number of HUIs found until now to the console
		int realHUICount =  algo.getRealHUICount();
		System.out.println("NUMBER OF HUI FOUND: " + realHUICount);
		
		// PRINT THE HUIs FOUND
		algo.printHUIs();
		
		// PRINT THE TRIE FOR DEBUGGING
//		algo.printTrie();
		
		// WE CAN ALSO WRITE ALL THE HUIs found until now to a file at any time with
		// the following code
		String output = ".//output.txt";
		algo.writeHUIsToFile(output);
	}

	/**
	 * This methods counts the number of lines in a text file.
	 * @param filepath the path to the file
	 * @return the number of lines as an int
	 * @throws IOException Exception if error reading/writting file
	 */
	public static int countLines(String filepath) throws IOException {
		LineNumberReader reader = new LineNumberReader(new FileReader(filepath));
		while(reader.readLine() != null) {}
		int count = reader.getLineNumber();
		reader.close();
		return count;
	}

	public static String fileToPath(String filename)
			throws UnsupportedEncodingException {
		URL url = MainTestEIHI_Xupdates.class.getResource(filename);
		return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
	}
}
