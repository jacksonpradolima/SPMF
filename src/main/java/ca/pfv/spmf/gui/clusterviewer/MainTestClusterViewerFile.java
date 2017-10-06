package ca.pfv.spmf.gui.clusterviewer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;

import ca.pfv.spmf.algorithms.clustering.clusterreader.AlgoClusterReader;
import ca.pfv.spmf.algorithms.timeseries.TimeSeries;
import ca.pfv.spmf.algorithms.timeseries.reader_writer.AlgoTimeSeriesReader;
import ca.pfv.spmf.patterns.cluster.Cluster;

/**
 * Example of how to view clusters from the source code of SPMF.
 * @author Philippe Fournier-Viger, 2016.
 */
public class MainTestClusterViewerFile {

	public static void main(String [] arg) throws IOException{
		
		// the input file
		String input = fileToPath("clustersDBScan.txt");  
		
		// Applying the  algorithm
		AlgoClusterReader algorithm = new AlgoClusterReader();
		List<Cluster> clusters = algorithm.runAlgorithm(input);
		List<String> attributeNames = algorithm.getAttributeNames();
		algorithm.printStats();

		ClusterViewer viewer = new ClusterViewer(clusters, attributeNames);
		viewer.setVisible(true);
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestClusterViewerFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
