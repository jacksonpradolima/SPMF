package ca.pfv.spmf.algorithms.clustering.text_clusterer;

import java.util.ArrayList;
import java.util.HashSet;
/* This file is copyright (c) 2014-2015 Sabarish Raghu
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
/**
 * This class consists of individual list of clusters.
 * @author Sabarish Raghu
 *
 */
public class TextCluster {
	public ArrayList<Integer> cluster; //consists of indices of records in a cluster

	public ArrayList<Integer> getCluster() {
		return cluster;
	}

	public void setCluster(ArrayList<Integer> cluster) {
		this.cluster = cluster;
	}

	/**
	 * equals method is over ridden to merge clusters transitively.
	 */
	@Override
	public boolean equals(Object o1)
	{
		ArrayList<Integer> cluster1List=this.getCluster();
		TextCluster cluster2=(TextCluster)o1;
		ArrayList<Integer> cluster2List=cluster2.getCluster();
		HashSet<Integer> cluster=new HashSet<Integer>();
		int flag=0;
		for(int x:cluster1List)
		{
		for(int y:cluster2List)	
		{	if(cluster2List.contains(x)||cluster1List.contains(y)||x==y)
			{
				
				flag=1;
			}
		}
		}
		if(flag==0)
		{
		return false;
		}
		else
		{
			cluster.addAll(cluster1List);
			cluster.addAll(cluster2List);
			ArrayList<Integer> clusterList=new ArrayList<Integer>(cluster);
			cluster2.setCluster(clusterList);
	return true;
		}
	}
	public int hashCode()
	{
		return 31;
	}


}
