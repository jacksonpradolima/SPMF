package ca.pfv.spmf.algorithms.classifiers.naive_bayes_text_classifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import ca.pfv.spmf.tools.MemoryLogger;
import ca.pfv.spmf.tools.textprocessing.PorterStemmer;
import ca.pfv.spmf.tools.textprocessing.StopWordAnalyzer;

/** * * * This is an implementation of the Naive Bayes Document Classifier algorithm. 
* 
* Copyright (c) 2014 Sabarish Raghu
* 
* This file is part of the SPMF DATA MINING SOFTWARE * (http://www.philippe-fournier-viger.com/spmf). 
* 
* 
* SPMF is free software: you can redistribute it and/or modify it under the * terms of the GNU General Public License as published by the Free Software * Foundation, either version 3 of the License, or (at your option) any later * version. * 

* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR * A PARTICULAR PURPOSE. See the GNU General Public License for more details. * 
* 
* You should have received a copy of the GNU General Public License along with * SPMF. If not, see . 
* 
* @author SabarishRaghu
*/
/**
 * 
 * Input can be of any format. 
 * For the training data, Please place the input files under corresponding ClassNames as FolderNames.
 * Output has the following format outputfileName \t ClassName 
 */
public class AlgoNaiveBayesClassifier {
	private String mTestDataDirectory="";
	private String mTrainingDataDirectory="";
	private boolean mInMemoryFlag=false; //By Default operations are executed by File IO rather than in memory objects.
	private HashMap<String,List<File>> mFileLists=new HashMap<String,List<File>>(); 
	private ArrayList<String> mClassNames;
	private StopWordAnalyzer mAnalyzer;
	private PorterStemmer mStemmer;
	private String mOutputDirectory="";
	private ArrayList<MemoryFile> mMemFiles=new ArrayList<MemoryFile>();
	long mStartTimestamp = 0; // last execution start time
	long mEndTimeStamp = 0; // last execution end time
	HashMap<String,Integer> classProb;
	/**
	 * 
	 * @param trainingDirectory
	 * @param testDirectory
	 * @param outputDirectory
	 * @param memoryFlag
	 * @throws Exception
	 */
	public void runAlgorithm(String trainingDirectory,String testDirectory,String outputDirectory,boolean memoryFlag) throws Exception
	{
		this.mTrainingDataDirectory=trainingDirectory;
		this.mTestDataDirectory=testDirectory;
		this.mOutputDirectory=outputDirectory;
		this.mInMemoryFlag=memoryFlag;
		this.runAlgorithm();
		Runtime.getRuntime().freeMemory(); //Mark the objects for GC's Mark & Sweep.
	}

	
	private void runAlgorithm() throws Exception
	{
		this.mStartTimestamp=System.currentTimeMillis();
		mAnalyzer=new StopWordAnalyzer();
		mStemmer=new PorterStemmer();
		classProb=new HashMap<String,Integer>();
		BufferedWriter writer=new BufferedWriter(new FileWriter(new File(mOutputDirectory+"/output.tsv")));
		ArrayList<OccurrenceProbabilties> op=new ArrayList<OccurrenceProbabilties>(); //A cache to hold probability for already calculated words in each class
		File[] listOfTestFiles=new File(mTestDataDirectory).listFiles();
		File[] listOfTrainingFiles=new File(mTrainingDataDirectory).listFiles();
		mClassNames=new ArrayList<String>();
		int totalTrainingFiles=0;
		for(File f: listOfTrainingFiles)
		{
			mClassNames.add(f.getName());
			OccurrenceProbabilties oc=new OccurrenceProbabilties();
			oc.setClassName(f.getName());
			oc.setOccuranceMap(new HashMap<String,Double>());
			op.add(oc);
			File classTraining[]=new File(mTrainingDataDirectory+"/"+f.getName()).listFiles();
			mFileLists.put(f.getName(), (List<File>) Arrays.asList(classTraining));
			classProb.put(f.getName(), classTraining.length);
			totalTrainingFiles++;
		}
		
		if(mInMemoryFlag==true)
		{
			System.out.println("Loading Data in to memory.... May take a while depending upon the size of the data");
			loadIntoMemory();
		}
		
		for(File f:listOfTestFiles)
		{
			TreeMap<String,BigDecimal> probabilities=new TreeMap<String,BigDecimal>();
			System.out.println("---------------Computing for Test File:"+f.getName()+"-----------");
			for(String currentClass:mClassNames)
			{
				TestRecord testRecord=readOneTestFile(f);
				BigDecimal prob=new BigDecimal(""+1.0);
				for(String word:testRecord.getWords())
				{
					double termProbInClass=0.0;
					if(getFromExistingProbability(word,op,currentClass)!=0.0)
					{
						termProbInClass=getFromExistingProbability(word,op,currentClass);
					}
					else
					{
						if(mInMemoryFlag==true)
							termProbInClass=calculateProbabilityInMemory(word,op,currentClass);
						else
							termProbInClass=calculateProbability(word,op,currentClass);
						for(OccurrenceProbabilties oc:op)
						{
							if(oc.getClassName().equalsIgnoreCase(currentClass))
							{
								oc.getOccuranceMap().put(word, termProbInClass);
								break;
							}
						}
					}
				prob=prob.multiply(new BigDecimal(""+termProbInClass));
				}
				//P(Class/Doc)=P(Doc/class)*P(class)
				prob=prob.multiply(new BigDecimal(""+((double)classProb.get(currentClass)/(double)totalTrainingFiles)));
				probabilities.put(currentClass, prob);
			}
			Entry<String,BigDecimal> maxEntry = null;

			for(Entry<String,BigDecimal> entry : probabilities.entrySet()) {
			    if (maxEntry == null || entry.getValue().compareTo( maxEntry.getValue())>0) {
			        maxEntry = entry;
			    }
			}	
		System.out.println(f.getName()+"\t"+maxEntry.getKey());
		writer.write(f.getName()+"\t"+maxEntry.getKey()+"\n");
		}
		writer.close();
		this.mEndTimeStamp=System.currentTimeMillis();
	}
	
	/**
	 * Load the training data in to memory
	 */
	private void loadIntoMemory() throws IOException
	{
		for(String s:mClassNames)
		{
			List<File> classTraining=mFileLists.get(s);
			MemoryFile memfile=new MemoryFile();
			ArrayList<String> words=new ArrayList<String>();
			memfile.setClassname(s);
			for(File f:classTraining)
			{
				BufferedReader reader=new BufferedReader(new FileReader(f));
				String currentLine="";
				while((currentLine=reader.readLine())!=null)
				{
					currentLine=currentLine.replaceAll("\\P{L}", " ").toLowerCase().replaceAll("\n"," ");
					currentLine=currentLine.replaceAll("\\s+", " ");
					currentLine=mAnalyzer.removeStopWords(currentLine);
					for(String processedWord:currentLine.split("\\s+"))
					{
						processedWord=mStemmer.stem(processedWord);
						if(processedWord.length()>1)
						{
							words.add(processedWord);
						}
					
					}
				}
			reader.close();
			}
			memfile.setContent(words);
			mMemFiles.add(memfile);
		}
	}
	
	/**
	 * Get the probability value of a particular word in a particular class. Calculates them from in memory stored objects
	 * @param word
	 * @param op
	 * @param currentClass
	 * @return probability value of the word in the class
	 */
	private double calculateProbabilityInMemory(String word,ArrayList<OccurrenceProbabilties> op, String currentClass)
	{
		double prob=0.0;
		int count=0;
		int occurances=0;
		for(MemoryFile memFile:mMemFiles)
		{
			if(memFile.getClassname().equals(currentClass))
			{
				occurances+=Collections.frequency(memFile.getContent(), word)*50; //Giving more weightage to occurances rather than just incrementing by 1.
				count+=memFile.getContent().size();
			}
		}
		prob=(double)((double)occurances+50.0)/(double)((double)count+100.0); // Normalizing the value to avoid return of 0.0
		return prob;
	}
	
	/**
	 * Get the probability value of a particular word in a particular class. Calculates them from File search. 
	 * @param word
	 * @param op
	 * @param currentClass
	 * @return probability value of the word in the class
	 * @throws Exception
	 */
	private double calculateProbability(String word,
			ArrayList<OccurrenceProbabilties> op, String currentClass) throws Exception {
		// TODO Auto-generated method stub
		double probability=0.0;
		List<File> classTraining=mFileLists.get(currentClass);
		ArrayList<String> words=new ArrayList<String>();
		double count=0.0;
		for(File f:classTraining)
		{	
			BufferedReader reader=new BufferedReader(new FileReader(f));
			String currentLine="";
			while((currentLine=reader.readLine())!=null)
			{
				currentLine=currentLine.replaceAll("\\P{L}", " ").toLowerCase().replaceAll("\n"," ");
				currentLine=currentLine.replaceAll("\\s+", " ");
				currentLine=mAnalyzer.removeStopWords(currentLine);
				for(String processedWord:currentLine.split("\\s+"))
				{
					processedWord=mStemmer.stem(processedWord);
				if(processedWord.length()>1)
				{
					words.add(processedWord);
				}
				if(processedWord.equalsIgnoreCase(word))
				{
					count+=20;
				}
				
				}
			}
			reader.close();
		}
		probability=(double)((double)count+50.0)/(double)((double)words.size()+100.0);
		return probability;
	}
	
	/**
	 * Get from cached probabilities.
	 * @param word
	 * @param probabilties
	 * @param className
	 * @return the cached probabilities
	 */
	public double getFromExistingProbability(String word, ArrayList<OccurrenceProbabilties>probabilties, String className)
	{
		double value=0.0;
		for(OccurrenceProbabilties op:probabilties)
		{
			if(op.getClassName().equals(className))
			{
				Set<String> myKeys=op.getOccuranceMap().keySet();
				for(String s:myKeys)
				{
					if(op.getOccuranceMap().get(s) != null&&s.equals(word))
					{
						value=op.getOccuranceMap().get(s);
					}
				}
			}
		}
		
		return value;
	}
	
	/**
	 * Reads one test file and stores it in Object.
	 * @param f
	 * @return
	 */
	public TestRecord readOneTestFile(File f) throws Exception
	{
		TestRecord record=new TestRecord();
		String currentLine;
		ArrayList<String> words=new ArrayList<String>();
		BufferedReader br=new BufferedReader(new FileReader(f));
		while((currentLine=br.readLine())!=null)
		{
			currentLine=currentLine.toLowerCase(); //convert everyword to lower case
			currentLine=currentLine.replaceAll("\\P{L}", " "); //only alphabets
			currentLine=currentLine.replaceAll("\n"," ");
			currentLine=currentLine.replaceAll("\\s+", " ").trim();
			currentLine=mAnalyzer.removeStopWords(currentLine);
			String lineWords[]=currentLine.split("\\s+");
			for(String eachWord:lineWords)
			{
				String processedWord=mStemmer.stem(eachWord);
				if(processedWord.length()>1)
				{
				words.add(processedWord);
				}
				
			}
		}
		record.setRecordId(Integer.parseInt(f.getName().replaceAll("\\D+","")));
		record.setWords(words);
		br.close();
		
		
		return record;
	}
	
	/**
	 * Print statistics of the latest execution to System.out.
	 */
	public void printStatistics() {
		System.out.println("========== Naive Bayes Classifier Stats ============");
		System.out.println(" Total time ~: " + (mEndTimeStamp - mStartTimestamp)
				+ " ms");
		System.out.println(" Max memory:" + MemoryLogger.getInstance().getMaxMemory() + " mb ");
		System.out.println("=====================================");
	}
}
