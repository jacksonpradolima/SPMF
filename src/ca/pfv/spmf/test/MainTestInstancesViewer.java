package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;

import ca.pfv.spmf.algorithms.clustering.instancereader.AlgoInstanceFileReader;
import ca.pfv.spmf.gui.instanceviewer.InstanceViewer;
import ca.pfv.spmf.patterns.cluster.DoubleArray;

/**
 * Example of how to view clusters from the source code of SPMF.
 * @author Philippe Fournier-Viger, 2016.
 */
public class MainTestInstancesViewer {

	public static void main(String [] arg) throws IOException{
		
		// the input file
//		String input = fileToPath("inputDBScan2.txt"); 
		String input = fileToPath("configKMeans.txt");   

		// Parameters of the algorithm
		String separator = " ";
		
		// Applying the  algorithm
		AlgoInstanceFileReader algorithm = new AlgoInstanceFileReader();
		List<DoubleArray> instances = algorithm.runAlgorithm(input, separator);
		List<String> attributeNames = algorithm.getAttributeNames();
//		algorithm.printStats();
		
//		System.out.println("INSTANCES");
//		for(DoubleArray instance : instances){
//			System.out.println(" "  + instance);
//		}
		InstanceViewer viewer = new InstanceViewer(instances, attributeNames);
		viewer.setVisible(true);
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestInstancesViewer.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
