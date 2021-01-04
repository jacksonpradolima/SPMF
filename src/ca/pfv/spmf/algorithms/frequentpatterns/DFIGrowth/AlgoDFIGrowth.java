package ca.pfv.spmf.algorithms.frequentpatterns.DFIGrowth;

/* This is an implementation of the DFI-Growth algorithm. 
* 
* 
* This file is part of the SPMF DATA MINING SOFTWARE 
* (http://www.philippe-fournier-viger.com/spmf). 
* 
* SPMF is free software: you can redistribute it and/or modify it under the 
* terms of the GNU General Public License as published by the Free Software 
* Foundation, either version 3 of the License, or (at your option) any later version. 
*
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY 
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR 
* A PARTICULAR PURPOSE. See the GNU General Public License for more details. 
* 
* You should have received a copy of the GNU General Public License along with 
* SPMF. If not, see <http://www.gnu.org/licenses/>. 
* 
* @copyright Cheng-Wei Wu et al.
*/

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class AlgoDFIGrowth {

	// parameter
	static ArrayList<ArrayList<String>> data;  
	static ArrayList<ArrayList<Integer>> Intdata;
	static ArrayList<ArrayList<String>> frequence;  
	static ArrayList<LinkNode> subheaderTable; 
	static boolean change_treenode = true ;
	
	// for statistics
	static long startTimestamp;  		// start time of the latest execution
	static long endTime;  				// end time of the latest execution
	static double current_memory=0; 	// current memory usage
	static double MaxMemory=0; 			// the maxnum of memory usage
	static int transactionCount; 		// transaction count in the database
	static int itemsetCount; 			// number of frequent itemsets found
	
	/* calculate the maxnum memory usage */
	static void MemoryUsage() {
		current_memory = ((double)((double)(Runtime.getRuntime().totalMemory()/1024)/1024))-
				((double)((double)(Runtime.getRuntime().freeMemory()/1024)/1024));
		if(current_memory>MaxMemory) {
			MaxMemory = current_memory;
		}
	}
	
	/* structure of treenode */
	public static class TreeNode{
		String name = "";
		int count = 0;    
		ArrayList<TreeNode> child; 	     
		TreeNode friend = null;          
		TreeNode parent = null;           
		
		public TreeNode() {
			child = new ArrayList<TreeNode>();
		}
				
		public TreeNode(String n,int c) { 
			name = n;
			count = c;
			child = new ArrayList<TreeNode>();
		}
	}
	
	/* structure of linknode */
	public static class LinkNode{
		String hname; 
		TreeNode friend = null;
		
		public LinkNode(String n) { 
			hname = n;
		}
	}
	
	
	/**
	 * Method to run the DFIGrowth algorithm.
	 * @param input the path to an input file containing Frequent Closed Itemsets.
	 * @param output the output file path for saving the result (if null, the result 
	 *        will be returned by the method instead of being saved).
	 * @return the result if no output file path is provided.
	 * @throws IOException exception if error reading or writing files
	 */
	public  void runAlgorithm(String input) throws IOException {
		MemoryUsage(); 
		startTimestamp = System.currentTimeMillis(); 
		readDB(input); 
		frequence = filter(frequence); 
		data = changeDatabase(data,frequence);
		subheaderTable = createHT(frequence); 
		frequence = null;
		createFPT(subheaderTable,data); 
		DFIgrowthReady(subheaderTable);   
	}
	
	
	
	/**
	 * Scan the frequent closed itemset database
	 * and count the number of occurrence of each individual item.
	 */
	public static void readDB(String input) throws IOException {
		transactionCount = 0;
		String path = input; 
		String line = ""; 
		String token[],tokens[];
		
		boolean is_firsttime = true , is_same = false; 
		data = new ArrayList<ArrayList<String>>();
		frequence = new ArrayList<ArrayList<String>>();
		ArrayList<String> subdata;
		ArrayList<String> subfrequence;
		
		FileReader fl = new FileReader(path);
		BufferedReader br = new BufferedReader(fl);
		while( (line=br.readLine())!=null ) {
			transactionCount++;
			subdata = new ArrayList<String>(); 
			token = line.split(" #SUP: ");
			tokens = token[0].split(" "); 
			for(int i=0;i<tokens.length;i++) {
				subdata.add(tokens[i]);
				if(is_firsttime==true) { 
					subfrequence = new ArrayList<String>();
					subfrequence.add(tokens[i]);
					subfrequence.add(token[1]);
					frequence.add(subfrequence);
				}
				else { 
					is_same = false;
					String s = tokens[i];
					for(int z=0;z<frequence.size();z++) {
						if(frequence.get(z).get(0).equals(s)) {
							int num = Integer.valueOf( frequence.get(z).get(1) ) + Integer.valueOf( token[1] );
							frequence.get(z).set(1, String.valueOf(num));
							is_same = true;
							break;
						}
					}
					if(is_same==false) { 
						subfrequence = new ArrayList<String>();
						subfrequence.add(tokens[i]);
						subfrequence.add(token[1]);
						frequence.add(subfrequence);
					}
				}
			}
			subdata.add(token[1]);
			is_firsttime = false;
			data.add(subdata);
		}
		MemoryUsage(); 
		br.close();
		fl.close();
	}

	
	
	/**
	 * Sort the number of occurrence of each item from small to large.
	 */
	public static ArrayList<ArrayList<String>> filter(ArrayList<ArrayList<String>> freq){
		Collections.sort(freq,new Comparator<ArrayList<String>>() {

			@Override
			public int compare(ArrayList<String> o1, ArrayList<String> o2) {
				if(Integer.parseInt( o1.get(1)) > Integer.parseInt( o2.get(1) )) {
					return -1;   
				}
				else if(Integer.parseInt( o1.get(1)) == Integer.parseInt( o2.get(1) )) {
					if(Integer.parseInt( o1.get(0)) > Integer.parseInt( o2.get(0) )) {
						return -1;   
					}
				}
				return 1; 
			}
			
		});
		MemoryUsage(); 
		return freq;
	}
	
	
	
	/**
	 * Scan the database again and adjust the order of the database 
	 * according to the order of the number of occurrence of each item.
	 */
	public static ArrayList<ArrayList<String>> changeDatabase(ArrayList<ArrayList<String>> predata,ArrayList<ArrayList<String>> frequent) throws IOException{
		ArrayList<ArrayList<String>> newdata = new ArrayList<ArrayList<String>>();
		ArrayList<String> subnewdata;
		for(int i=0;i<predata.size();i++) {
			subnewdata = new ArrayList<String>();
			for(int j=0;j<frequent.size();j++) {
				if(predata.get(i).contains(frequent.get(j).get(0)) 
						&& predata.get(i).indexOf(frequent.get(j).get(0)) != predata.get(i).size()-1 ) {
					subnewdata.add(frequent.get(j).get(0));
				}
			}
			subnewdata.add(predata.get(i).get(predata.get(i).size()-1));
			newdata.add(subnewdata);
		}
		MemoryUsage(); 
		return newdata;
	}
	
	
	
	/**
	 * Scan the number of occurrence of each item and create HeaderTable.
	 */
	public static ArrayList<LinkNode> createHT(ArrayList<ArrayList<String>> frequent) {
		ArrayList<LinkNode> newheaderTable = new ArrayList<LinkNode>(); 
		for(int i=0;i<frequent.size();i++) {
			LinkNode headerNode = new LinkNode( frequent.get(i).get(0) );
			newheaderTable.add(headerNode);
		}
		return newheaderTable;
	}
	
	/**
	 * Scan the database and create FP-Tree. 
	 */
	public static void createFPT(ArrayList<LinkNode> newheaderTable,ArrayList<ArrayList<String>> datainfo) throws IOException {
		TreeNode root = new TreeNode(); 
		TreeNode Ttmp = null;
		for(int i=0;i<datainfo.size();i++) {
			Ttmp = root;
			for(int j=0;j<datainfo.get(i).size()-1;j++) {
				change_treenode = true;
				TreeNode tnode = new TreeNode(datainfo.get(i).get(j),Integer.valueOf(datainfo.get(i).get(datainfo.get(i).size()-1)));
				Ttmp = createTNode(Ttmp,tnode);
				if(change_treenode==true) {
					for(int z=0;z<newheaderTable.size();z++) {
						if(newheaderTable.get(z).hname.equals(Ttmp.name)) {
							TreeNode tmp = Ttmp;
							tmp.friend = newheaderTable.get(z).friend;
							newheaderTable.get(z).friend = tmp;
							break;
						}
					}
				}
			}
		}
		
	}
	
	
	
	/**
	 * Create every TreeNode of the FP-Tree.
	 */
	public static TreeNode createTNode(TreeNode begin,TreeNode tnode) {
		boolean is_havenode = false;
		if(begin.child.size()==0) {
			tnode.parent = begin;
			begin.child.add(tnode);
			return tnode;
		}
		else if(begin.child.size()!=0){
			for(int i=0;i<begin.child.size();i++) {
				if(begin.child.get(i).name.equals(tnode.name)) {		
					if(tnode.count > begin.child.get(i).count) {
						begin.child.get(i).count = tnode.count;
					}
					change_treenode = false;
					is_havenode = true;
					return begin.child.get(i);
				}
			}
			if(is_havenode==false) {
				tnode.parent = begin;
				begin.child.add(tnode);
				return tnode;
			}
		}
		return null;
	}
	
	
	
	/**
	 * Visit the HeaderTable from the last to the front to get ready for DFIgrowth 
	 */
	public static void DFIgrowthReady(ArrayList<LinkNode> linknode) throws IOException {
		data = null; 
		Intdata = new ArrayList<ArrayList<Integer>>();
		boolean is_itself = true; 
		boolean is_first = true;  
		int repect_num = 0;
		int maxcount = 0;
		for(int i=linknode.size()-1;i>=0;i--) {
			maxcount = 0;
			TreeNode Hnode = linknode.get(i).friend; 
			TreeNode Vnode = linknode.get(i).friend; 
			ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>(); 
			ArrayList<String> sublist;
			while(Hnode!=null) {
				if(Hnode.count>maxcount) {
					maxcount = Hnode.count;
				}
				sublist = new ArrayList<String>();
				is_itself = true;
				is_first = true;
				repect_num = 0;
				while(Vnode.parent!=null) {
					if(is_itself==true) {
						repect_num = Vnode.count;
					}
					if(is_itself==false) {
						if(is_first == true) {
							sublist.add(Vnode.name);
							is_first = false;
						}
						else {
							sublist.add(0, Vnode.name);
						}
					}
					if(Vnode.parent==null) {break;}
					Vnode = Vnode.parent;
					is_itself = false;
				}
				if(sublist.size()!=0) {
					sublist.add(String.valueOf(repect_num));
					list.add(sublist);
				}
				Hnode = Hnode.friend;
				Vnode = Hnode;
			}
			DFIgrowth(linknode.get(i).hname,maxcount,list); 
		}
		MemoryUsage(); 
		endTime = System.currentTimeMillis(); 
	}
	
	
	
	/**
	 * Use recursion and Depth-first search to create frequent itemsets.
	 */
	public static void DFIgrowth(String strname,int hcount,ArrayList<ArrayList<String>> list) throws IOException {
		ArrayList<ArrayList<String>> frequ = new ArrayList<ArrayList<String>>();
		ArrayList<String> subfrequ;
		ArrayList<String> allitem; 
		boolean is_firsttime = true ,is_same = false; 
		if(list.size()==0) { 
			sortoutputS(strname,String.valueOf(hcount));
			return;
		}
		else {
			if(list.size()==1) {
				ArrayList<ArrayList<String>> newlist = new ArrayList<ArrayList<String>>(list);
				newlist.get(0).remove(newlist.get(0).size()-1);
				GenSubset(strname,hcount,newlist.get(0));
				return;
			}
			else {
				allitem = new ArrayList<String>();
				for(int i=0;i<list.size();i++) {
					int countnum = Integer.parseInt(list.get(i).get(list.get(i).size()-1));
					for(int j=0;j<list.get(i).size()-1;j++) {
						if(is_firsttime==true) { 
							subfrequ = new ArrayList<String>();
							subfrequ.add(list.get(i).get(j));
							allitem.add(list.get(i).get(j));
							subfrequ.add(String.valueOf(countnum));
							frequ.add(subfrequ);
						}
						else { 
							is_same = false;
							String s = list.get(i).get(j);
							for(int z=0;z<frequ.size();z++) {
								if(frequ.get(z).get(0).equals(s)) {
									int num = Integer.valueOf( frequ.get(z).get(1) );
									num=num+countnum;
									frequ.get(z).set(1, String.valueOf(num));
									is_same = true;
									break;
								}
							}
							if(is_same==false) { 
								subfrequ = new ArrayList<String>();
								subfrequ.add(list.get(i).get(j));
								allitem.add(list.get(i).get(j));
								subfrequ.add(String.valueOf(countnum));
								frequ.add(subfrequ);
							}
						}
					}
					is_firsttime = false;
				}
				
				list = changeDatabase(list,frequ);
				ArrayList<LinkNode> newheaderTable = new ArrayList<LinkNode>();
				newheaderTable = createHT(frequ);  
				
				createFPT(newheaderTable,list);
				boolean is_itself = true; 
				boolean is_first = true;  
				int repect_num = 0;
				int maxcount = 0;
				String loopstr = "";
			
				for(int i=newheaderTable.size()-1;i>=0;i--) {
					TreeNode Hnode = newheaderTable.get(i).friend; 
					TreeNode Vnode = newheaderTable.get(i).friend; 
					ArrayList<ArrayList<String>> newlist = new ArrayList<ArrayList<String>>(); 
					ArrayList<String> sublist;
					while(Hnode!=null) {
						if(Hnode.count>maxcount) {
							maxcount = Hnode.count;
						}
						sublist = new ArrayList<String>();
						is_itself = true;
						is_first = true;
						repect_num = 0;
						while(Vnode.parent!=null) {
							if(is_itself==true) {
								repect_num = Vnode.count;
							}
							if(is_itself==false) {
								if(is_first == true) {
									sublist.add(Vnode.name);
									is_first = false;
								}
								else {
									sublist.add(0, Vnode.name);
								}
							}
							if(Vnode.parent==null) {break;}
							Vnode = Vnode.parent;
							is_itself = false;
						}
						if(sublist.size()!=0) {
							sublist.add(String.valueOf(repect_num));
							newlist.add(sublist);
						}
						Hnode = Hnode.friend;
						Vnode = Hnode;
					}
					loopstr = strname+" "+newheaderTable.get(i).hname;
					DFIgrowth(loopstr,maxcount,newlist);
				}
				MemoryUsage(); 
				sortoutputS(strname,String.valueOf(hcount));
			}
		}
		
	}
	
	
	
	/**
	 * If there is only a single path in the FP-Tree 
	 * then we will get frequent itemset by generating the subset.   
	 */
	static void GenSubset(String name,int count,ArrayList<String> list){
		sortoutputS(name,String.valueOf(count));
		for(int i=0;i<list.size();i++) {
			String Name = name + " " +list.get(list.size()-1);
			list.remove(list.size()-1); 
			i--;
			ArrayList<String> nlist = new ArrayList<String>(list);
			GenSubset(Name,count,nlist);
		}
	}
	
	
	
	/**
	 * Sort the frequent itemset from small to large.
	 */
	static void sortoutputS(String str,String num){
		String[] token = str.split(" ");
		ArrayList<Integer> tmparr = new ArrayList<Integer>();
		itemsetCount++;
		for(int i=0;i<token.length;i++) {
			tmparr.add(Integer.parseInt( token[i]) );
		}
		Collections.sort(tmparr);
		MemoryUsage();
		
		tmparr.add(Integer.parseInt(num));
		Intdata.add(tmparr);
	}
	
	
	
	/**
	 * Print statistics about the algorithm execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  DFI-GROWTH v.2.34 - STATS =============");
		long temps = endTime - startTimestamp;
		System.out.println(" Transactions count from database : " + transactionCount);
		System.out.print(" Max memory usage: " + MaxMemory + " mb \n");
		System.out.println(" Frequent itemsets count : " + itemsetCount);
		System.out.println(" Total time ~ " + temps + " ms");
		System.out.println("===================================================");
	}
	
	
	/**
	 *  Write the frequent itemsets to the output.txt.
	 * @throws IOException 
	 */
	public void writeOutPut(String output) throws IOException {
		FileWriter fw = new FileWriter(output);
		BufferedWriter bfw = new BufferedWriter(fw);
		for(int i=0;i<Intdata.size();i++) {
			for(int j=0;j<Intdata.get(i).size();j++) {
				if(j<Intdata.get(i).size()-1) {
					bfw.write(String.valueOf(Intdata.get(i).get(j)+" "));
				}
				else {
					bfw.write("#SUP: ");
					bfw.write(String.valueOf(Intdata.get(i).get(j)));
				}
			}
			bfw.newLine();
		}		
		bfw.flush();
		bfw.close();
		fw.close();
		Intdata = null;
	}
	
}
