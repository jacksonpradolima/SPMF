package ca.pfv.spmf.gui;

/*
 * Copyright (c) 2008-2015 Philippe Fournier-Viger
 *
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 *
 * SPMF is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import com.sun.org.apache.xalan.internal.xsltc.runtime.Parameter;

import ca.pfv.spmf.algorithmmanager.AlgorithmManager;
import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.algorithms.associationrules.IGB.AlgoIGB;
import ca.pfv.spmf.algorithms.associationrules.Indirect.AlgoINDIRECT;
import ca.pfv.spmf.algorithms.associationrules.MNRRules.AlgoMNRRules;
import ca.pfv.spmf.algorithms.associationrules.TopKRules_and_TNR.Database;
import ca.pfv.spmf.algorithms.associationrules.closedrules.AlgoClosedRules;
import ca.pfv.spmf.algorithms.associationrules.closedrules.AlgoClosedRules_UsingFPClose;
import ca.pfv.spmf.algorithms.associationrules.fhsar.AlgoFHSAR;
import ca.pfv.spmf.algorithms.associationrules.gcd.GCDAssociationRules;
import ca.pfv.spmf.algorithms.clustering.dbscan.AlgoDBSCAN;
import ca.pfv.spmf.algorithms.clustering.distanceFunctions.DistanceFunction;
import ca.pfv.spmf.algorithms.clustering.hierarchical_clustering.AlgoHierarchicalClustering;
import ca.pfv.spmf.algorithms.clustering.kmeans.AlgoBisectingKMeans;
import ca.pfv.spmf.algorithms.clustering.kmeans.AlgoKMeans;
import ca.pfv.spmf.algorithms.clustering.optics.AlgoOPTICS;
import ca.pfv.spmf.algorithms.clustering.text_clusterer.TextClusterAlgo;
import ca.pfv.spmf.algorithms.frequentpatterns.MSApriori.AlgoMSApriori;
import ca.pfv.spmf.algorithms.frequentpatterns.apriori.AlgoApriori;
import ca.pfv.spmf.algorithms.frequentpatterns.aprioriTID.AlgoAprioriTID;
import ca.pfv.spmf.algorithms.frequentpatterns.aprioriTIDClose.AlgoAprioriTIDClose;
import ca.pfv.spmf.algorithms.frequentpatterns.apriori_HT.AlgoAprioriHT;
import ca.pfv.spmf.algorithms.frequentpatterns.apriori_close.AlgoAprioriClose;
import ca.pfv.spmf.algorithms.frequentpatterns.apriori_inverse.AlgoAprioriInverse;
import ca.pfv.spmf.algorithms.frequentpatterns.apriori_rare.AlgoAprioriRare;
import ca.pfv.spmf.algorithms.frequentpatterns.cfpgrowth.AlgoCFPGrowth;
import ca.pfv.spmf.algorithms.frequentpatterns.charm.AlgoCharmMFI;
import ca.pfv.spmf.algorithms.frequentpatterns.charm.AlgoCharm_Bitset;
import ca.pfv.spmf.algorithms.frequentpatterns.charm.AlgoDCharm_Bitset;
import ca.pfv.spmf.algorithms.frequentpatterns.cori.AlgoCORI;
import ca.pfv.spmf.algorithms.frequentpatterns.d2hup.AlgoD2HUP;
import ca.pfv.spmf.algorithms.frequentpatterns.dci_closed_optimized.AlgoDCI_Closed_Optimized;
import ca.pfv.spmf.algorithms.frequentpatterns.defme.AlgoDefMe;
import ca.pfv.spmf.algorithms.frequentpatterns.eclat.AlgoDEclat;
import ca.pfv.spmf.algorithms.frequentpatterns.eclat.AlgoDEclat_Bitset;
import ca.pfv.spmf.algorithms.frequentpatterns.eclat.AlgoEclat;
import ca.pfv.spmf.algorithms.frequentpatterns.eclat.AlgoEclat_Bitset;
import ca.pfv.spmf.algorithms.frequentpatterns.efim.AlgoEFIM;
import ca.pfv.spmf.algorithms.frequentpatterns.efim_closed.AlgoEFIMClosed;
import ca.pfv.spmf.algorithms.frequentpatterns.fin_prepost.FIN;
import ca.pfv.spmf.algorithms.frequentpatterns.fin_prepost.PrePost;
import ca.pfv.spmf.algorithms.frequentpatterns.foshu.AlgoFOSHU;
import ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth.AlgoFPClose;
import ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth.AlgoFPGrowth;
import ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth.AlgoFPMax;
import ca.pfv.spmf.algorithms.frequentpatterns.hmine.AlgoHMine;
import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoCHUIMiner;
import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoFCHM;
import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoFHM;
import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoFHMPlus;
import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoFHM_Freq;
import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoFHN;
import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoGHUIMINER;
import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoHUGMiner;
import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoHUIMiner;
import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoHUPMiner;
import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoMinFHM;
import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoPHM;
import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.Itemset;
import ca.pfv.spmf.algorithms.frequentpatterns.lcm.AlgoLCM;
import ca.pfv.spmf.algorithms.frequentpatterns.lcm.AlgoLCMFreq;
import ca.pfv.spmf.algorithms.frequentpatterns.lcm.AlgoLCMMax;
import ca.pfv.spmf.algorithms.frequentpatterns.lcm.Dataset;
import ca.pfv.spmf.algorithms.frequentpatterns.pascal.AlgoPASCAL;
import ca.pfv.spmf.algorithms.frequentpatterns.pfpm.AlgoPFPM;
import ca.pfv.spmf.algorithms.frequentpatterns.relim.AlgoRelim;
import ca.pfv.spmf.algorithms.frequentpatterns.skymine.AlgoSkyMine;
import ca.pfv.spmf.algorithms.frequentpatterns.tshoun.AlgoTSHoun;
import ca.pfv.spmf.algorithms.frequentpatterns.tshoun.DatabaseWithPeriods;
import ca.pfv.spmf.algorithms.frequentpatterns.two_phase.AlgoHUINIVMine;
import ca.pfv.spmf.algorithms.frequentpatterns.two_phase.AlgoTwoPhase;
import ca.pfv.spmf.algorithms.frequentpatterns.uapriori.AlgoUApriori;
import ca.pfv.spmf.algorithms.frequentpatterns.upgrowth_ihup.AlgoIHUP;
import ca.pfv.spmf.algorithms.frequentpatterns.upgrowth_ihup.AlgoUPGrowth;
import ca.pfv.spmf.algorithms.frequentpatterns.upgrowth_ihup.AlgoUPGrowthPlus;
import ca.pfv.spmf.algorithms.frequentpatterns.vme.AlgoVME;
import ca.pfv.spmf.algorithms.frequentpatterns.zart.AlgoZart;
import ca.pfv.spmf.algorithms.frequentpatterns.zart.TZTableClosed;
import ca.pfv.spmf.algorithms.sequential_rules.cmdeogun.AlgoCMDeogun;
import ca.pfv.spmf.algorithms.sequential_rules.cmrules.AlgoCMRules;
import ca.pfv.spmf.algorithms.sequential_rules.husrm.AlgoHUSRM;
import ca.pfv.spmf.algorithms.sequential_rules.rulegen.AlgoRuleGen;
import ca.pfv.spmf.algorithms.sequential_rules.rulegrowth.AlgoERMiner;
import ca.pfv.spmf.algorithms.sequential_rules.rulegrowth.AlgoRULEGROWTH;
import ca.pfv.spmf.algorithms.sequential_rules.topseqrules_and_tns.AlgoTNS;
import ca.pfv.spmf.algorithms.sequential_rules.topseqrules_and_tns.AlgoTopSeqRules;
import ca.pfv.spmf.algorithms.sequential_rules.trulegrowth.AlgoTRuleGrowth;
import ca.pfv.spmf.algorithms.sequential_rules.trulegrowth_with_strings.AlgoTRuleGrowth_withStrings;
import ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan.AlgoFEAT;
import ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan.AlgoFSGP;
import ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan.AlgoMaxSP;
import ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan.AlgoTSP_nonClosed;
import ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan_with_strings.AlgoPrefixSpan_with_Strings;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.AlgoCM_ClaSP;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.AlgoClaSP;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.AlgoCloSpan;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.AlgoFournierViger08;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.AlgoPrefixSpanMDSPM;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.multidimensionalpatterns.AlgoDim;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.multidimensionalsequentialpatterns.AlgoSeqDim;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.multidimensionalsequentialpatterns.MDSequenceDatabase;
import ca.pfv.spmf.algorithms.sequentialpatterns.goKrimp.AlgoGoKrimp;
import ca.pfv.spmf.algorithms.sequentialpatterns.goKrimp.DataReader;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.AlgoGSP;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.creators.AbstractionCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.creators.AbstractionCreator_Qualitative;
import ca.pfv.spmf.algorithms.sequentialpatterns.lapin.AlgoLAPIN_LCI;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.AlgoPrefixSpan_AGP;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixspan.AlgoBIDEPlus;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixspan.AlgoPrefixSpan;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.AlgoCMSPADE;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.AlgoSPADE;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.candidatePatternsGeneration.CandidateGenerator;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.candidatePatternsGeneration.CandidateGenerator_Qualitative;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.idLists.creators.IdListCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.idLists.creators.IdListCreator_FatBitmap;
import ca.pfv.spmf.algorithms.sequentialpatterns.spam.AlgoCMSPAM;
import ca.pfv.spmf.algorithms.sequentialpatterns.spam.AlgoSPAM;
import ca.pfv.spmf.algorithms.sequentialpatterns.spam.AlgoTKS;
import ca.pfv.spmf.algorithms.sequentialpatterns.spam.AlgoVGEN;
import ca.pfv.spmf.algorithms.sequentialpatterns.spam.AlgoVMSP;
import ca.pfv.spmf.algorithms.sequentialpatterns.uspan.AlgoUSpan;
import ca.pfv.spmf.gui.patternvizualizer.PatternVizualizer;
import ca.pfv.spmf.input.sequence_database_list_strings.SequenceDatabase;
import ca.pfv.spmf.input.transaction_database_list_integers.TransactionDatabase;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;
import ca.pfv.spmf.tools.dataset_converter.Formats;
import ca.pfv.spmf.tools.dataset_converter.SequenceDatabaseConverter;
import ca.pfv.spmf.tools.dataset_converter.TransactionDatabaseConverter;
import ca.pfv.spmf.tools.dataset_generator.AddTimeStampsToSequenceDatabase;
import ca.pfv.spmf.tools.dataset_generator.SequenceDatabaseGenerator;
import ca.pfv.spmf.tools.dataset_generator.TransactionDatabaseGenerator;
import ca.pfv.spmf.tools.dataset_generator.TransactionDatasetUtilityGenerator;
import ca.pfv.spmf.tools.dataset_stats.SequenceStatsGenerator;
import ca.pfv.spmf.tools.dataset_stats.TransactionStatsGenerator;
import ca.pfv.spmf.tools.other_dataset_tools.FixTransactionDatabaseTool;
import ca.pfv.spmf.tools.other_dataset_tools.ResizeDatabaseTool;
import ca.pfv.spmf.tools.other_dataset_tools.TransactionUtilityRemover;
import ca.pfv.spmf.tools.resultConverter.ResultConverter;

/**
 * This class executes commands from the command line interface or
 * graphical interface to run the algorithms.
 * 
 * @author Philippe Fournier-Viger
 */
public class CommandProcessor {

	/**
	 * This method run an algorithm. It is called from the GUI interface or when
	 * the user run the jar file from the command line.
	 * 
	 * @param algorithmName
	 *            the name of the algorithm
	 * @param inputFile
	 *            the input file for the algorithm
	 * @param outputFile
	 *            the output file for the algorithm
	 * @param parameters
	 *            the parameters of the algorithm
	 * @throws Exception if sometimes bad occurs
	 */
	public static void runAlgorithm(String algorithmName,
			String inputFile, String outputFile, String[] parameters) throws Exception {

		// **** CHECK IF ARFF AS INPUT FILE *****
		// FIRST WE WILL CHECK IF IT IS AN ARFF FILE...
		// IF YES, WE WILL CONVERT IT TO SPMF FORMAT FIRST,
		// THEN WE WILL RUN THE ALGORITHM, AND FINALLY CONVERT THE RESULT SO
		// THAT IT CAN
		// BE SHOWED TO THE USER.
		// This map is to store the mapping from ItemID to Attribute value for
		// the conversion
		// from ARFF to SPMF.
		Map<Integer, String> mapItemToString = null;
		// This variable store the path of the original output file
		String originalOutputFile = null;
		// This variable store the path of the original input file
		String originalInputFile = null;
		
		// Get information about the chosen algorithm
		AlgorithmManager manager = AlgorithmManager.getInstance();
		DescriptionOfAlgorithm algorithm = manager.getDescriptionOfAlgorithm(algorithmName);
		
		// If the algorithm does not exist, throw an exception
		if(algorithm == null){
			throw new IllegalArgumentException("\n\n There is no algorithm with the name '" + algorithmName +"' in SPMF. ");
		}
		
		// If there is no imput file we remember it
		if(algorithm.getInputFileTypes() == null){
			inputFile = null;
		}
		// If there is no output file we remember it
		if(algorithm.getOutputFileTypes() == null){
			outputFile = null;
		}
		
		// Check that the parameters provided by the user are OK
		// in terms of data type (String, Integer...). 
		// Otherwise, we will throw an exception to the user
		
		// If we expect an input file
		if(algorithm.getInputFileTypes() != null){
			if(inputFile == null){
				throw new IllegalArgumentException(System.lineSeparator()+ System.lineSeparator() + "No input file has been provided.");
			}
			
			File input = new File(inputFile);
			if(input.exists() == false){
				throw new IllegalArgumentException(System.lineSeparator()+ System.lineSeparator() +" The input file '" + inputFile + "' does not exist.");
			}
		}
		
		// If we expect an output file
		if(algorithm.getOutputFileTypes() != null){
			if(outputFile == null){
				throw new IllegalArgumentException(System.lineSeparator()+ System.lineSeparator() + " No output file path has been provided.");
			}
		}
		
		int numberOfParameter = algorithm.getParametersDescription().length;
		
		for(int i = 0; i < numberOfParameter; i++){
			DescriptionOfParameter parameterI =  algorithm.getParametersDescription()[i];

			// Get the ordinal of this parameter
			String ordinal = ordinal(i+1);
			
			// If it is the command line interface and the user has not provided the parameter i
			if(i == parameters.length){
				// and if the parameter is not optional
				if(parameterI.isOptional == false){
					// we throw an exception
					throw new IllegalArgumentException(System.lineSeparator()+ System.lineSeparator() +" The " + ordinal + " parameter of this algorithm '" + parameterI.name + "' is mandatory. Please provide a value of type: " + parameterI.parameterType.getSimpleName() + ".");
				}
				break;
			}else{
				// Otherwise, the user has provided the parameter
				String valueI = parameters[i];
				
				// If the parameter is null but it is not optional, then we need to inform the user
				// that this parameter is not optional
				if("".equals(valueI)){
					if(parameterI.isOptional == false){
						throw new IllegalArgumentException(System.lineSeparator()+ System.lineSeparator() +" The " + ordinal + " parameter of this algorithm '" + parameterI.name + "' is mandatory. Please provide a value of type: " + parameterI.parameterType.getSimpleName() + ".");
					}
				}
				else{
					// Check if the parameter is of the correct type
					boolean isCorrectType = algorithm.isParameterOfCorrectType(parameters[i], i);
					
					// if not of the correct type, we throw an exception
					if(isCorrectType == false){		
						
						throw new IllegalArgumentException(System.lineSeparator()+ System.lineSeparator() + " The " + ordinal + " parameter value of this algorithm '" + parameterI.name + "' is of an incorrect type. The provided value is '" + parameters[i] + "' but it should be of type: " + parameterI.parameterType.getSimpleName() + ".");
					}
				}
			}
		}
		
		

		if(algorithmName.startsWith("Convert") == false && inputFile != null) {
			
			// ***********  PRE-PROCESSING IF THE FILE WAS A PREVIOUSLY CONVERTED FILE  **********
			// Check if the file was already converted.
			// Create some objects to read the file
			FileInputStream fin = new FileInputStream(new File(inputFile));
			BufferedReader myInput  = new BufferedReader(new InputStreamReader(fin));
			String firstLine = myInput.readLine();
			myInput.close();
			// if the file was previously converted from ARFF or text format or time series
			if(firstLine.startsWith("@CONVERTED_FROM_ARFF") 
					|| firstLine.startsWith("@CONVERTED_FROM_TEXT")
					|| firstLine.startsWith("@CONVERTED_FROM_TIME_SERIES")){
				// save the file paths selected by the user
				originalOutputFile = outputFile;
				originalInputFile = inputFile;
				outputFile = outputFile + ".tmp";
				

				// Create a new map to store the string representation of each item
				mapItemToString = new HashMap<Integer, String>();
				
				// Read the file to read the string representation of each item
				 fin = new FileInputStream(new File(inputFile));
				myInput  = new BufferedReader(new InputStreamReader(fin));
				String thisLine;
				// we read the file line by line until the end of the file
				while ((thisLine = myInput.readLine()) != null) {
					if(thisLine.startsWith("@ITEM")){
						// remove "@ITEM="
						thisLine = thisLine.substring(6);
						// get the position of the first = in the remaining string
						int index = thisLine.indexOf("=");
						int itemID = Integer.parseInt(thisLine.substring(0, index));
						String stringValue = thisLine.substring(index+1);
						mapItemToString.put(itemID, stringValue);
					}
				
				}
				myInput.close();

			}else{
				// ***********  PRE-PROCESSING IF THE FILE WAS AN ARFF FILE  **********
				// If the file is ARFF
				if ((inputFile.endsWith(".arff") || inputFile.endsWith(".ARFF"))) {
					// Convert it
					TransactionDatabaseConverter converter = new TransactionDatabaseConverter();
					System.out.println("Converting ARFF to SPMF format.");
					// save the file paths selected by the user
					originalOutputFile = outputFile;
					originalInputFile = inputFile;
					// change the ouptut file path to a temporary file
					inputFile = inputFile + ".tmp";
					outputFile = outputFile + ".tmp";
					mapItemToString = converter.convertARFFandReturnMap(
							originalInputFile, inputFile, Integer.MAX_VALUE);
					System.out.println("Conversion completed.");
				}
				// ***********  PRE-PROCESSING IF THE FILE WAS A TEXT FILE  **********
				// If the file is ARFF
				else if (inputFile.endsWith(".text") || inputFile.endsWith(".TEXT")) {
					
					// Get the prefered charset for this user
					Charset charset = PreferencesManager.getInstance().getPreferedCharset();
					
					// Convert it
					SequenceDatabaseConverter converter = new SequenceDatabaseConverter();
					System.out.println("Converting TEXT to SPMF format.");
					
					// save the file paths selected by the user
					originalOutputFile = outputFile;
					originalInputFile = inputFile;
					
					// change the ouptut file path to a temporary file
					inputFile = inputFile + ".tmp";
					outputFile = outputFile + ".tmp";
					mapItemToString = converter.convertTEXTandReturnMap(originalInputFile, inputFile, Integer.MAX_VALUE, charset);
					System.out.println("Conversion completed.");
				}
			}
		}

		// ******  WE  APPLY THE DESIRED ALGORITHM ******
		algorithm.runAlgorithm(parameters, inputFile, outputFile);
		
		
		// ***********  POST-PROCESSING IF THE FILE WAS AN ARFF or TEXT FILE  **********

		// IF THE FILE WAS AN ARFF OR TEXT FILE, WE NEED TO CONVERT BACK THE RESULT
		// SO THAT IT IS PRESENTED IN TERMS OF VALUES
		if (mapItemToString != null) {
			
			// Get the prefered charset for this user
			Charset charset = PreferencesManager.getInstance().getPreferedCharset();
			
			ResultConverter converter = new ResultConverter();
			System.out.println("Post-processing to show result in terms of string values.");
			converter.convert(mapItemToString, outputFile, originalOutputFile, charset);
			System.out.println("Post-processing completed.");
			// delete the temporary files
			// System.out.println("Delete : " + outputFile);
			File file = new File(outputFile);
			file.delete();
			// System.out.println("Delete : " + inputFile);
			if(inputFile.equals(originalInputFile) == false){
				File file2 = new File(inputFile);
				file2.delete();
			}
			// set the original outputFile and inputFile
			outputFile = originalOutputFile;
			inputFile = originalInputFile;
		}
	}
	
	/**
	 * Convert an integer number to its ordinal
	 * @param i the number
	 * @return its ordinal as a String
	 */
	public static String ordinal(int i) {
		// Code from : http://stackoverflow.com/questions/6810336/is-there-a-library-or-utility-in-java-to-convert-an-integer-to-its-ordinal
	    String[] sufixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
	    switch (i % 100) {
	    case 11:
	    case 12:
	    case 13:
	        return i + "th";
	    default:
	        return i + sufixes[i % 10];

	    }
	}


}
