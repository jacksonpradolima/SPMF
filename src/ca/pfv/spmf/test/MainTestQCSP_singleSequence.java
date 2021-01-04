package ca.pfv.spmf.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import ca.pfv.spmf.algorithms.sequentialpatterns.qcsp.AlgoQCSP;
import ca.pfv.spmf.algorithms.sequentialpatterns.qcsp.SequentialPattern;
import ca.pfv.spmf.algorithms.sequentialpatterns.qcsp.util.Pair;

/**
 * Example of how to use the QCSP algorithm from the source code.
 * @author Len Feremans, 2020.
 */
public class MainTestQCSP_singleSequence {

	public static void main(String [] arg) throws Exception{

		//------------ Parameters ----------------------//
		//single sequence: 0 is used for gaps
		//motivating example from paper using fragment of moby dick
		//String inputFragment = fileToPath("moby_fragment.txt");  
		//transform 
		//List<String> sequenceAndLabelFilename = textFragmentToSequence(inputFragment);
		//String input = sequenceAndLabelFilename.get(0); //text fragment to sequence of integers 
		//String labelFile = sequenceAndLabelFilename.get(1); //labels for sequence, e.g. line=0 is first word
		String input = fileToPath("moby_fragment.seq");
		String labelFile = fileToPath("moby_fragment.lab");
		
		// output file path (for saving the patterns found 
		String output = "./output.txt";
		
		// frequency threshold on single item
		int minsup = 2; //each word must occur at least three times
				
				
		// threshold on window, relative to pattern length. alpha=1 -> no gaps, 
		// alpha=2  -> |X| * 2 gaps allowed for (quantile-based cohesive) sequential pattern occurrence
		double alpha = 2;

		//  this is the maximum sequential pattern length
		int maximumSequentialPatternLength = 10;

		// top-k sequential patterns, ranked on quantile-based cohesion, to return
		int topK = 20;

		//--------------- Applying the  algorithm  ---------//
		AlgoQCSP algorithm = new AlgoQCSP();
		algorithm.setDebug(true);
		algorithm.setLabelsFile(labelFile); //set label file
		List<Pair<SequentialPattern,Double>> patterns = algorithm.runAlgorithm(input, output, minsup, alpha, maximumSequentialPatternLength, topK);
		// Print statistics
		algorithm.printStatistics();
	}

	//convert Fragment of Moby Dick to sequence
	private static List<String> textFragmentToSequence(String tekstfragment) throws IOException{
		//read file content
		BufferedReader reader = new BufferedReader(new FileReader(new File(tekstfragment)));
		List<String> sentences = new ArrayList<>();
		String line = reader.readLine();
		while(line != null) {
			sentences.add(line);
			line = reader.readLine();
		}
		reader.close();
		//parse
		List<Integer> sequence = new ArrayList<>();
		Map<String,Integer> labelDict = new TreeMap<>();
		for(String sentence: sentences) {
			String[] words = convertLine(sentence);
			for(int i=0; i<words.length; i++) {
				Integer key = labelDict.get(words[i]);
				if(key == null) {
					key = labelDict.size()+1;
					labelDict.put(words[i], key);
				}
				sequence.add(key);
			}
		}
		//save 
		File outputSeq = new File("./moby_fragment.seq");
		File outputLabels = new File("./moby_fragment.lab");
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputSeq));
		for(int i: sequence) {
			writer.write(String.valueOf(i));
			writer.write(" ");
		}
		writer.close();
		writer = new BufferedWriter(new FileWriter(outputLabels));
		List<Entry<String,Integer>> entries = new ArrayList<>();
		for(Entry<String, Integer> entry: labelDict.entrySet()) {
			entries.add(entry);
		}
		entries.sort((a, b) -> a.getValue().compareTo(b.getValue()));
		for(Entry<String, Integer> entry: entries) {
			writer.write(entry.getKey());
			writer.write("\n");
		}
		writer.close();
		//return filenames
		return Arrays.asList(outputSeq.getAbsolutePath(), outputLabels.getAbsolutePath());
	}

	//convert line of text, to array of lower-case words without punctuation or numbers
	public static String[] convertLine(String line){
		//common seperators in text: 
		line = line.replaceAll("[\\.,\\?!\\-\\—\"\"'';\\|]+", " ");
		//remove non-alphanumerical token
		line = line.replaceAll("[^\\w+|\\d+|\\d+\\.\\d+|\\s+]"," ");
		//to lower case!
		line = line.toLowerCase().trim();
		//remove number
		line = line.replaceAll("\\d+|\\d+\\.\\d+"," ");
		//remove more-then-one-space
		line = line.replaceAll("\\s+", " ");
		//tokenize, see also nltk regex tokenizer package: http://www.nltk.org/_modules/nltk/tokenize/regexp.html
		String[] tokens = line.split("[^\\w+|\\d+|\\d\\.\\d+]");
		return tokens;
	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestQCSP_saveToMemory.class.getResource(filename);
		return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
