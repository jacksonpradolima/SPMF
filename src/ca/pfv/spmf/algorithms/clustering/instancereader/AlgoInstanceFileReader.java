package ca.pfv.spmf.algorithms.clustering.instancereader;

/* This file is copyright (c) 2008-2016 Philippe Fournier-Viger
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
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import ca.pfv.spmf.algorithms.ArraysAlgos;
import ca.pfv.spmf.algorithms.timeseries.TimeSeries;
import ca.pfv.spmf.patterns.cluster.DoubleArray;
import ca.pfv.spmf.patterns.cluster.DoubleArrayInstance;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This class reads an instance file in memory. This is the
 * type of files used by clustering algorithms, for example.
 * 
 * @author Philippe Fournier-Viger, 2016
 * @see DoubleArray
 * @see DoubleArrayInstance
 */

public class AlgoInstanceFileReader {
 
	/** the time the algorithm started */
	long startTimestamp = 0; 
	
	/** the time the algorithm terminated */
	long endTimestamp = 0;  
	
	/** This program will execute in DEBUG MODE if this variable is true */
	boolean DEBUG_MODE = false;
	
	/** The number of time series in the last file that was read */
	int instanceCount = 0;

	/** The list of instances read by this algorithm **/
	private List<DoubleArray> instances;
	
	/** The names of the attributes **/
	private List<String> attributeNames = null;
	
		
	/**
	 * Default constructor
	 */
	public AlgoInstanceFileReader() {
	}

	/**
	 * Run the algorithm
	 * @param input the input file path
	 * @param separator the character or string that is used to separate values
	 * @return a list of DoubleArray (vectors of double values), each representing an instance.
	 * @throws IOException exception if error while writing the file
	 */
	public List<DoubleArray> runAlgorithm(String input, String separator) throws IOException {
		
		// reset memory logger
		MemoryLogger.getInstance().reset();
		
		// record the start time of the algorithm
		startTimestamp = System.currentTimeMillis();

		instances = new ArrayList<DoubleArray>();
		
		attributeNames = new ArrayList<String>();

		// Prepare to read the file...
		BufferedReader myInput = null;
		String thisLine;
		
		// This variable will count the number of instances
		instanceCount = 0;
		
		// If debug mode, we show some debugging information
		if(DEBUG_MODE){
			System.out.println(System.lineSeparator() + "======= READING THE INPUT FILE =======");
		}
		
		// Variable to store the name of the current time series
		String currentInstanceName = null;
		
		// prepare the object for reading the file
		myInput = new BufferedReader(new InputStreamReader( new FileInputStream(new File(input))));
		
		// for each line  until the end of file
		while ((thisLine = myInput.readLine()) != null) {

			// if the line is  a comment, is  empty or is a
			// kind of metadata
			if (thisLine.isEmpty() == true ||
					thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'){
				continue;
			}
			
			if(thisLine.charAt(0) == '@'){
				// if it is the name of an instance
				if(thisLine.startsWith("@NAME=")){
					currentInstanceName = thisLine.substring(6, thisLine.length());
				}
				// if it is the name of an attribute   // @ATTRIBUTEDEF=Y
				if(thisLine.startsWith("@ATTRIBUTEDEF=")){
					String attributeName = thisLine.substring(14, thisLine.length());
					attributeNames.add(attributeName);
				}
				continue;
			}

			// IF in debug mode
			if(DEBUG_MODE){
				// print the time series number
				System.out.println(System.lineSeparator() + "--- Reading instance #" + (instanceCount + 1) + " ---");
			}
			
			//========================== CONVERT THE LINE TO DOUBLE VALUES ===============================
			// We will split the lines into string values
			String tokens[] = thisLine.split(separator); 
			// Then, we convert to a double array containing each data point of the time series
			double[] dataPoints = ArraysAlgos.convertStringArrayToDoubleArray(tokens);
			String nameToUse = currentInstanceName == null ?  "Series " + instanceCount : currentInstanceName;
			currentInstanceName = null;
			instances.add(new DoubleArrayInstance(dataPoints, nameToUse));
			
			instanceCount++;
		}
		
		// If the file did not contain attribute names, we will generate some
		if(attributeNames.size() == 0 && instances.size() > 0){
			int dimensionCount = instances.get(0).data.length;
			for(int i = 0; i < dimensionCount; i++){
				attributeNames.add("Attribute"+i);
			}
		}
		
		// remember the number of instances
		instanceCount = instances.size();
		
		// closed input file
		myInput.close();
				
		// check the memory usage again and close the file.
		MemoryLogger.getInstance().checkMemory();
		
		// record end time
		endTimestamp = System.currentTimeMillis();
		
		// return the instance
		return instances;
	}

	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats() {
		System.out.println("======= READ INSTANCES TO MEMORY v2.09 - STATS =======");
		System.out.println(" Number of instances processed: " + instanceCount);
		System.out.println(" Number of attributes: " + attributeNames.size());
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("=====================================================================");
	}

	/**
	 * Obtain the list of attribute names from the last file that has been read
	 * @return the list of attribute names (a list of String objects)
	 */
	public List<String> getAttributeNames() {
		return attributeNames;
	}
}