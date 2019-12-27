package ca.pfv.spmf.algorithms.sequentialpatterns.skopus;

import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This is the Skopus algorithm.
 * Skopus was propsoed in this paper: </br></br>
 *  François Petitjean, Tao Li, Nikolaj Tatti, Geoffrey I. Webb:
 *   Skopus: Mining top-k sequential patterns under leverage. Machine Learning, Volume 30, Number 5, 1086-1111 </br></br>
 *   
 *   The original Java mplementation was written by Tao Li et al. in the Skopus software.
 *   The modification of the code to integrate it in the SPMF software was done by Philippe Fournier-Viger (2017)
 *
 * @author Tao Li et al. 
 */
public class AlgoSkopus {
	
	/** The execution time of the last execution of this algorithm */
	private double executionTime = 0;
	
	/** Pattern count */
	int patternCount = 0;
	
	/**
	 * Run the algorithm
	 * @param strMInputFileName  the input file
	 * @param strMOutputPathName the output file
	 * @param useLeverageMeasureInsteadOfSupport  if true, the patterns will be found until the leverage interestingness measure instead of the support
	 * @param showDebugInformation if true, the algorithm will display debugging information
	 * @param useSmoothedValues if true, smoothed values will be used
	 * @param smoothingCoefficient if smoothing is used, this is the smoothing coefficient (e.g. 0.5)
	 * @param maximumSequentialPatternLength  this is the maximum sequential pattern length
	 * @param k
	 * @throws Exception
	 */
	public void runAlgorithm(String strMInputFileName, 
									String strMOutputPathName,
									boolean useLeverageMeasureInsteadOfSupport,
									boolean showDebugInformation,
									boolean useSmoothedValues,
									double smoothingCoefficient,
									int maximumSequentialPatternLength,
									int k) throws Exception {
		
		// record the start time
		double startTime = System.currentTimeMillis();
		// reset the amount of memory for statistics purpose
		MemoryLogger.getInstance().reset();
		
		if(useLeverageMeasureInsteadOfSupport){
			GlobalData.nInterestingnessMeasure = 2;  // leverage
		}else{
			GlobalData.nInterestingnessMeasure = 1;  // support
		}

		if(showDebugInformation){
			GlobalData.bDebugInformation = true;
		}
		
		if(maximumSequentialPatternLength < 0){
			maximumSequentialPatternLength = 0;
		}
		GlobalData.nMaxResultPatternLength = maximumSequentialPatternLength;
		
		if(useSmoothedValues){
			GlobalData.dSmoothCoefficient = smoothingCoefficient;
		}else{
			GlobalData.dSmoothCoefficient = 0.5;
		}

		if(k <0){
			k = 0;
		}
		GlobalData.nK = k;
		
		GlobalData.Init();
		
		FileLoader fl = new FileLoader();
		fl.loadData(strMInputFileName);

		ItemsetFinder isF = new ItemsetFinder();

		
		isF.strDebugFile = strMOutputPathName +"-debug.txt";
		java.io.File fileFileName = new java.io.File(isF.strDebugFile);
		if (fileFileName.exists()) {
			fileFileName.delete();
		}
				
		// Find the frequent patterns
		isF.generateResultItemsets();
		
		// save pattern count 
		patternCount = isF.pqMItemsetTopk.size();

		isF.outputResult(strMOutputPathName);

		double endTime = System.currentTimeMillis();
		executionTime = endTime - startTime;
	}
	
	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  Skopus algorithm v2.34 - STATS =======");
		System.out.println(" Pattern count: " +  patternCount);
		System.out.println(" Total time ~ " + (executionTime) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println(" Input file information");
		System.out.println("  number of symbols: " + GlobalData.nNumOfItems );
		System.out.println("  number of sequences: " + GlobalData.nNumOfSequence );
		System.out.println("  average sequence length: " + GlobalData.dSampleAverageLength);
		System.out.println("  maximum sequence length: " + GlobalData.nSampleMaxLength);
		System.out.println("===========================================================");
	}

	

}
