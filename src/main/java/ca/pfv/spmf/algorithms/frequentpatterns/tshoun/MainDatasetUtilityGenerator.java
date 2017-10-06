package ca.pfv.spmf.algorithms.frequentpatterns.tshoun;




import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Convert a transaction database to a transaction database with utility values and on-shelf time periods
 * from the source code.
 * @author Philippe Fournier-Viger, 2010
 */
public class MainDatasetUtilityGenerator {

	public static void main(String [] arg) throws IOException, InterruptedException{
		
		boolean includeItemsHavingNegativeProfit = true;
		int numberOfPeriods = 5;  // for on-shelf utility mining, set this value > 1, otherwise set to 1
		
		String dataset = "retail";
		String input = "C:\\Datasets\\" + dataset + ".txt";
		String output;
		if(includeItemsHavingNegativeProfit) {
			output = "C:\\Datasets\\periods\\" + dataset + "_p" + numberOfPeriods + "_negative.txt";
		}else {
			output = "C:\\Datasets\\periods\\" + dataset + "_p" + numberOfPeriods + ".txt";
		}
		
		generateDataset(includeItemsHavingNegativeProfit, numberOfPeriods, input, output);
	}


	private static void generateDataset(
			boolean includeItemsHavingNegativeProfit, int numberOfPeriods, String input,
			String output) throws IOException, FileNotFoundException {
		// for stats
		Set<Integer> items = new HashSet<Integer>();
		long avglength =0;
		long tidcount = 0;
		
		Random randomGenerator = new Random(System.currentTimeMillis());
		
		
		Map<Integer, Integer> externalUtilities = new HashMap<Integer, Integer>();

		BufferedWriter writer = new BufferedWriter(new FileWriter(output)); 
		BufferedReader myInput = new BufferedReader(new InputStreamReader( new FileInputStream(new File(input))));
		// for each line (transaction) until the end of file
		String thisLine;
		while ((thisLine = myInput.readLine()) != null) {
			// if the line is  a comment, is  empty or is a
			// kind of metadata
			if (thisLine.isEmpty() == true ||
					thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
							|| thisLine.charAt(0) == '@') {
				continue;
			}
			
			
			// split the transaction according to the : separator
			String split[] = thisLine.split(" "); 
			

			tidcount++;
			avglength += split.length;

			for(int i=0; i <split.length; i++){
				// convert item to integer
				Integer item = Integer.parseInt(split[i]);
				
				items.add(item);
				
				if(externalUtilities.containsKey(item) == false) {
					double rand = Math.abs(randomGenerator.nextGaussian()) / 3d * 10d;
					int extUtility = (int) (rand) + 1;
					if(includeItemsHavingNegativeProfit && randomGenerator.nextBoolean()) {
						extUtility = 0 - extUtility;
					}
					externalUtilities.put(item, extUtility);
				}
			}
		}
		myInput.close();
		
		myInput = new BufferedReader(new InputStreamReader( new FileInputStream(new File(input))));
		// for each line (transaction) until the end of file
		while ((thisLine = myInput.readLine()) != null) {
			// if the line is  a comment, is  empty or is a
			// kind of metadata
			if (thisLine.isEmpty() == true ||
					thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
							|| thisLine.charAt(0) == '@') {
				continue;
			}
			
			// split the transaction according to the : separator
			String split[] = thisLine.split(" "); 
			
			List<Integer> quantities = new ArrayList<Integer>();
			int TU = 0;
			
			// split the transaction according to the : separator
			for(int i=0; i <split.length; i++){
				// convert item to integer
				Integer item = Integer.parseInt(split[i]);
				int quantity = randomGenerator.nextInt(10) +1;
				quantities.add(quantity);
				int extutility = externalUtilities.get(item);
				if(extutility > 0) {
					TU += extutility * quantity;
				}
			}
			
			
			// write the items
			for(int i=0; i <split.length; i++){
				// convert item to integer
				Integer item = Integer.parseInt(split[i]);
				writer.write(""+ item);
				if(i != split.length -1) {
					writer.write(" ");
				}
			}
			// write the transaction utility
			writer.write(":");
			writer.write(""+ TU);
			
			// write the items quantity x utility
			writer.write(":");
			for(int i=0; i <split.length; i++){
				// convert item to integer
				Integer item = Integer.parseInt(split[i]);
				Integer q = quantities.get(i);
				int extutility = externalUtilities.get(item);
				writer.write(""+ q * extutility);
				if(i != split.length -1) {
					writer.write(" ");
				}
			}
			
			// write the time period
			int period = randomGenerator.nextInt(numberOfPeriods);
			writer.write(":" + period);
			writer.newLine();
		}
		writer.close();
		
		
		System.out.println("item count " + items.size());
		System.out.println("transacion count " + tidcount);
		System.out.println("transacion avg length " + (avglength / (double) tidcount));
	}


	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainDatasetUtilityGenerator.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}

	