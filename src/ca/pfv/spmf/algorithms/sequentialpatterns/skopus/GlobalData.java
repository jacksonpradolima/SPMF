package ca.pfv.spmf.algorithms.sequentialpatterns.skopus;
///*******************************************************************************
// * Copyright (C) 2015 Tao Li
// * 
// * This file is part of Skopus.
// * 
// * Skopus is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, version 3 of the License.
// * 
// * Skopus is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// * 
// * You should have received a copy of the GNU General Public License
// * along with Skopus.  If not, see <http://www.gnu.org/licenses/>.
// ******************************************************************************/
import java.util.ArrayList;

public class GlobalData {
	
	public static void Init()
	{
		nNumOfSequence = 0;
		nNumOfItems = 0;
		alItemName.clear();
		alSids.clear();
	}
	
	public static int nNumOfSequence = 0;
	public static int nNumOfItems = 0;
	public static ArrayList<String> alItemName = new ArrayList<String>();
	public static ArrayList<SidSet> alSids = new ArrayList<SidSet>();
	
	public static boolean bSmoothedValue = false; // true使用Smoothed value作为support或的计算方法，即分子分母均加1�?
	public static int nInterestingnessMeasure = 1; // the flag of interestingness measure. 1: support(default), 2: leverage
	public static int nK = 100; //the K number of top K, the default value is 100�?
	public static int nMaxResultPatternLength = 0; // the maximum sequential pattern length. if the value <=0, there is no limit; otherwise, the  result pattern length is less than nMaxLength.
	
	public static double dSmoothCoefficient = 0.5;
	
	public static int nSampleMaxLength = 0; //the max length of all the sequence in dataset
	public static double dSampleAverageLength = 0.0; // the average length of sequence in dataset 
	
	public static boolean bDebugInformation = false; //if true, output debug information; otherwise, do not output debug information
	
	public static ArrayList<Integer> alSequenceLengthList = new ArrayList<Integer>(); //store all the leghth of the sequences
}
