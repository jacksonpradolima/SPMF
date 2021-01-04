package ca.pfv.spmf.algorithms.sequentialpatterns.qcsp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

/**
 * This is an implementation of the QCSP algorithm.
 * For more information please refer the paper Mining Top-K Quantile-based Cohesive Sequential Patterns 
 * by Len Feremans, Boris Cule and Bart Goethals, published in 2018 SIAM International Conference on Data Mining (SDM18).<br/>
 *
 * Copyright (c) 2020 Len Feremans (Universiteit Antwerpen)
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
 * You should have received a copy of the GNU General Public License along wit
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Len Feremans
 */

public class Utils {
	

	public static void printHead(File file, int max) throws IOException{	
		System.out.println("====== HEAD " + file.getName() + "(#lines=" + countLines(file) + ") ========");
		List<String> lines = readFileUntil(file, max);
		for(String line: lines)
			System.out.println(line);
		long countL = countLines(file);
		if(countL > max){
			System.out.println("(" + (countL-max) + " more ...");
		}
	}
	
	public static String milisToStringReadable(long milis){
		if(milis < 1000){
			return String.format("%d ms", milis);
		}
		else if(milis >= 1000 && milis < 60000){
			return String.format("%.1f sec", milis/1000.0);
		}
		else if(milis >= 60 * 1000 && milis < 60 * 60 * 1000){
			return String.format("%.1f min", milis/(60 * 1000.0));
		}
		//if(milis >= 360000){
		return String.format("%.2f h", milis/(3600 * 1000.0));		
	}

	public static void printHead(File file) throws IOException{	
		System.out.println("====== HEAD " + file.getName() + "(#lines= " + countLines(file) + "0========");
		List<String> lines = readFileUntil(file, 10);
		for(String line: lines)
			System.out.println(line);
	}
	
	public static List<String> readFileUntil(File file, int lineNumber) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		List<String> lines = new ArrayList<String>();
		String current = reader.readLine();
		int line = 1;
		while(current != null)
		{
			lines.add(current);
			current = reader.readLine(); 
			line++;
			if(line >= lineNumber)
				break;
		}
		reader.close();
		return lines;
	}	

	public static String getFilenameNoExtension(File file) 
	{
		int idx = file.getName().lastIndexOf(".");
		if(idx == -1)
			return file.getName();
		else 
			return file.getName().substring(0, idx);
	}
	

	public static long countLines(File input) throws IOException {
		//see http://stackoverflow.com/questions/453018/number-of-lines-in-a-file-in-java
        LineNumberReader  lnr = new LineNumberReader(new FileReader(input));
        lnr.skip(Long.MAX_VALUE);
        long lines = lnr.getLineNumber();
        // Finally, the LineNumberReader object should be closed to prevent resource leak
        lnr.close();
        return lines;
	}
	
}
