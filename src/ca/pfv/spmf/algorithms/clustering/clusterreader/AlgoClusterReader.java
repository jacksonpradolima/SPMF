package ca.pfv.spmf.algorithms.clustering.clusterreader;

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
import java.util.Arrays;
import java.util.List;

import ca.pfv.spmf.algorithms.ArraysAlgos;
import ca.pfv.spmf.algorithms.timeseries.TimeSeries;
import ca.pfv.spmf.patterns.cluster.Cluster;
import ca.pfv.spmf.patterns.cluster.DoubleArray;
import ca.pfv.spmf.patterns.cluster.DoubleArrayInstance;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This class reads a cluster file in memory
 * 
 * @author Philippe Fournier-Viger, 2016
 * @see TimeSeries
 */

public class AlgoClusterReader {
 
	/** the time the algorithm started */
	long startTimestamp = 0; 
	
	/** the time the algorithm terminated */
	long endTimestamp = 0;  
	
	/** This program will execute in DEBUG MODE if this variable is true */
	boolean DEBUG_MODE = false;
	
	/** The number of time series in the last file that was read */
	int clusterCount = 0;
	
	/** The number of dimensions used to describe each instance */
	int dimensionCount = 0;
	
	/** The names of the attributes **/
	private List<String> attributeNames = null;
		
	/**
	 * Default constructor
	 */
	public AlgoClusterReader() {
	}

	/**
	 * Run the algorithm
	 * @param input the input file path
	 * @return  a list of clusters (Cluster) objects
	 * @throws IOException exception if error while writing the file
	 */
	public List<Cluster> runAlgorithm(String input) throws IOException {
		
		// reset memory logger
		MemoryLogger.getInstance().reset();
		
		// record the start time of the algorithm
		startTimestamp = System.currentTimeMillis();
		
		// initialize the list of attribute names
		attributeNames = new ArrayList<String>();

		// create a variable to store the time series
		List<Cluster> clusters = new ArrayList<Cluster>();

		BufferedReader myInput = null;
		String thisLine;
		
		// the instance have a name?
		boolean instanceHaveAName = false;
		
		if(DEBUG_MODE){
			System.out.println(System.lineSeparator() + "======= READING THE INPUT FILE =======");
		}

		// prepare the object for reading the file
		myInput = new BufferedReader(new InputStreamReader( new FileInputStream(new File(input))));
		// for each line (transaction) until the end of file
		while ((thisLine = myInput.readLine()) != null) {

			// if the line is  a comment, is  empty or is a
			// kind of metadata
			if (thisLine.isEmpty() == true ||
					thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'){
				continue;
			}
			
			if(thisLine.charAt(0) == '@'){
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
				System.out.println(System.lineSeparator() + "--- Reading cluster #" + (clusters.size()+1) + " ---");
			}
			
			//========================== CONVERT THE LINE TO A CLUSTER ===============================
			// We first process the line to remove all occurrences of the '[' character because we do not need them
			//[Instance2 1.0 0.0][Instance0 1.0 1.0][Instance1 0.0 1.0]
			thisLine = thisLine.substring(1,thisLine.length()).replace('[', ' ');
			
			// Then, we split the lines into string values 
			String tokens[] = thisLine.split(" "); 
			
			// Then, if it is the first instance of the first cluster, we will count how many dimensions there is in an instance.
			// We will also check if the instance has name. Having this information will make it easier
			// thereafter for reading all the instances in the file
			if(clusters.size() == 0){
				// For each token on this line
				for(String token : tokens){
					// Boolean to indicate if it is the last dimension of the first instance
					boolean lastDimension = false;
					// If it is the last dimension of the first instance
					if(token.charAt(token.length()-1)== ']'){
						// remove the ] character
						token = token.substring(0, token.length()-1);
						// remember that it is the last dimension of the first instance
						lastDimension = true;
					}

					// try to convert the current token to a double value
					double value = 0;
					boolean isNumber = true;
					try {
						value = Double.parseDouble(token);
					} catch (NumberFormatException nfe) {
						// if it is not a double value, we will remember it
						isNumber = false;
						// remember that the instance has a name
						instanceHaveAName = true;
					}
					
					// if it is a number,  increase the number of dimension for the first instance
					if(isNumber){
						dimensionCount++;
					}
					
					// if it is the last dimension of the first instance, we don't need to look at the other instance
					// because we have collected the information that we want, that is
					// the number of dimensions to describe each instance and wheter instances have names or not
					if(lastDimension){
						break;
					}
				}

				if(DEBUG_MODE){
					System.out.println("Number of dimensions: " + dimensionCount);
					System.out.println("Instances have names?: " + instanceHaveAName);
				}
			}
			
			// Now we will read all instances in the current line (cluster)
			// If instances have names
			if(instanceHaveAName) {
				
				// This will store the current instance being read
				DoubleArray instance = null;
				// This will store the values of the current instance
				double[] values = null;
				// This is the current cluster
				Cluster cluster = new Cluster();
				// This is the index of the current value in the current instance
				int indexValue = 0;
				
				// This value indicate that we are starting a new instance
				boolean newInstance = true;
				
				// For each token on this like
				for(int i=0; i < tokens.length; i++){
					// if this is a new instance
					if(newInstance){
						// We prepare the new instance
						String instanceName = tokens[i];
						values = new double[dimensionCount];
						instance =  new DoubleArrayInstance(values, instanceName);
						indexValue = 0;
						newInstance = false;
					}else{
						// We take the current token
						String token = tokens[i];
						// We check if the token contains ']'
						if(token.charAt(token.length()-1) == ']'){
							newInstance = true;
							token = token.substring(0, token.length()-1);
							cluster.addVector(instance);
						}
		
						// We convert the token to a double value
						double value = 0;
						try {
							value = Double.parseDouble(token);
						} catch (NumberFormatException nfe) {
							// if it is not a double value, we will remember it
							nfe.printStackTrace();
							throw new RuntimeException("Error in input file - parseDouble");
						}
						// We insert the value in the values of the current instance
						values[indexValue++] = value; 
					}
				}
				// We add the cluster to the list of clusters
				clusters.add(cluster);
			}else{
				// If the instances don't have names
				
				// This will store the current instance being read
				DoubleArray instance = null;
				// This will store the values of the current instance
				double[] values = null;
				// This is the current cluster
				Cluster cluster = new Cluster();
				// This is the index of the current value in the current instance
				int indexValue = 0;
				
				// This value indicate that we are starting a new instance
				boolean newInstance = true;
				
				// For each token on this like
				for(int i=0; i < tokens.length; i++){
					// if this is a new instance
					if(newInstance){
						// We prepare the new instance
						values = new double[dimensionCount];
						instance =  new DoubleArray(values);
						indexValue = 0;
						newInstance = false;
					} 
					
					// We take the current token
					String token = tokens[i];
					// We check if the token contains ']'
					if(token.charAt(token.length()-1) == ']'){
						newInstance = true;
						token = token.substring(0, token.length()-1);
						cluster.addVector(instance);
					}
	
					// We convert the token to a double value
					double value = 0;
					try {
						value = Double.parseDouble(token);
					} catch (NumberFormatException nfe) {
						// if it is not a double value, we will remember it
						nfe.printStackTrace();
						throw new RuntimeException("Error in input file - parseDouble");
					}
					// We insert the value in the values of the current instance
					values[indexValue++] = value; 
				}
				// We add the cluster to the list of clusters
				clusters.add(cluster);
			}
		}
		
		// If the file did not contain attribute names, we will generate some
		if(attributeNames.size() == 0 && clusters.size() > 0){
			int dimensionCount = clusters.get(0).getVectors().get(0).data.length;
			for(int i = 0; i < dimensionCount; i++){
				attributeNames.add("Attribute"+i);
			}
		}
		
		// remember the number of clusters
		clusterCount = clusters.size();
		
		// closed input file
		myInput.close();
				
		// check the memory usage again and close the file.
		MemoryLogger.getInstance().checkMemory();
		// record end time
		endTimestamp = System.currentTimeMillis();
		
		// return the clusters
		return clusters;
	}

	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats() {
		System.out.println("======= READ CLUSTERS TO MEMORY v2.09 - STATS =======");
		System.out.println(" Number of clusters processed: " + clusterCount);
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("=====================================================================");
	}

	/**
	 * Return the number of dimensions for describing each instance
	 * @return the number of dimensions
	 */
	public int getDimensionCount() {
		return dimensionCount;
	}

	/** Get the list of attributes
	 * @return a list of String objects
	 */
	public List<String> getAttributeNames() {
		return attributeNames;
	}
}