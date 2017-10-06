package ca.pfv.spmf.test;

import java.io.File;

import ca.pfv.spmf.algorithms.classifiers.naive_bayes_text_classifier.AlgoNaiveBayesClassifier;

/**
 * Example of how to use the Text Classifier from the source code
 * @author Sabarish Raghu (Copyright 2015)
 */
public class MainTestTextClassifier {

	public static void main(String [] arg) throws Exception{

		String trainingSetDirectory = getCurrentPath() + ("/text_classification_set/Train/"); 
		System.out.println(trainingSetDirectory);
		String testSetDirectory = getCurrentPath() + ("/text_classification_set/Test/"); 
		String outputDirectory = "./";
		boolean memoryFlag = true; // if set to true, the data will be loaded in memory rather than repeatedly using file IO

		AlgoNaiveBayesClassifier nb=new AlgoNaiveBayesClassifier();
		nb.runAlgorithm(trainingSetDirectory, testSetDirectory, outputDirectory, memoryFlag);
	}

	public static String getCurrentPath() {
		 return new File(MainTestTextClassifier.class.getResource("MainTestTextClassifier.class").getPath()).getParent();
	}
}
