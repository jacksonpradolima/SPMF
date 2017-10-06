package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.Item;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.Sequence;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.SequenceDatabase;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.SequenceStatsGenerator;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.predictor.CPT.CPT.CPTPredictor;

/**
 * Example of how to use the CPT+ sequence prediction model in the source code.
 * Copyright 2015.
 */
public class MainTestCPT {

	public static void main(String [] arg) throws IOException{
		
		// Load the set of training sequences
		String inputPath = fileToPath("contextCPT.txt");  
		SequenceDatabase trainingSet = new SequenceDatabase();
		trainingSet.loadFileSPMFFormat(inputPath, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
		
		// Print the training sequences to the console
		System.out.println("--- Training sequences ---");
		for(Sequence sequence : trainingSet.getSequences()) {
			System.out.println(sequence.toString());
		}
		System.out.println();
		
		// Print statistics about the training sequences
		SequenceStatsGenerator.prinStats(trainingSet, " training sequences ");
		
		// The following line is to set optional parameters for the prediction model. 
		// We can activate the recursive divider strategy to obtain more noise
		// tolerant predictions (see paper). We can also use a splitting method
		// to reduce the model size (see explanation below).
		String optionalParameters = "splitLength:6 splitMethod:0 recursiveDividerMin:1 recursiveDividerMax:5";
		
		// An explanation about "splitMethod":
		// - If we set splitMethod to 0, then each sequence will be completely used
		//   for training. 
		// - If we set splitMethod to 1, then only the last k (here k = 6) symbols of
		// each sequence will be used for training. This will result in a smaller model
		// and faster prediction, but may decrease accuracy.
		// - If we set splitMethod to 2, then each sequence will be divided in several
		//   subsequences of length k or less to be used for training. 
		
		// Train the prediction model
		CPTPredictor predictionModel = new CPTPredictor("CPT", optionalParameters);
		predictionModel.Train(trainingSet.getSequences());
		
		// Now we will use the prediction model that we have trained to make a prediction.
		// We want to predict what would occur after the sequence <1, 4>.
		// We first create the sequence
		Sequence sequence = new Sequence(0);
		sequence.addItem(new Item(1));
		sequence.addItem(new Item(4));
		
		// Then we perform the prediction
		Sequence thePrediction = predictionModel.Predict(sequence);
		System.out.println("For the sequence <(1),(4)>, the prediction for the next symbol is: +" + thePrediction);
		
		// If we want to see why that prediction was made, we can also 
		// ask to see the count table of the prediction algorithm. The
		// count table is a structure that stores the score for each symbols
		// for the last prediction that was made.  The symbol with the highest
		// score was the prediction.
		System.out.println();
		System.out.println("To make the prediction, the scores were calculated as follows:");
		 Map<Integer, Float> countTable = predictionModel.getCountTable();
		 for(Entry<Integer,Float> entry : countTable.entrySet()){
			 System.out.println("symbol"  + entry.getKey() + "\t score: " + entry.getValue());
		 }

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestCPT.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
