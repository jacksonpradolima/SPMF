package ca.pfv.spmf.algorithms.frequentpatterns.levelwise;

/* This is an implementation of the LevelWise algorithm. 
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
*/

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

public class AlgoLevelWise 
{
	//parameter
	public static ArrayList allFiles = new ArrayList();
	public static ArrayList<String[]> temp = new ArrayList<String[]>(); 
	public static ArrayList<String[]> temp1 = new ArrayList<String[]>(); 
	public static Hashtable<String, Integer> DB = new Hashtable<String, Integer>(); 
	public static Hashtable<String, Integer> FI = new Hashtable<String, Integer>(); 
	public static HashSet<Integer> record_length = new HashSet<Integer>(); 
	public static int Max; 
	
	public static long startTimestamp;      // start time of the latest execution
	public static long endTime;             // end time of the latest execution
	public static double current_memory=0;  // current memory usage
	public static double MaxMemory=0;       // the maxnum of memory usage
	public static int itemsetCount = 0;     // number of frequent itemsets found 
	public static int transactioncount = 0; // number of transaction itemsets 
	
	public static String Temp = "mu";
	public static String min_sup = "60p";
	public static String Input_Path;
	public static String Output_Path;
	
	/* calculate the maxnum memory usage. */
	public static void MemoryUsage() 
	{
		current_memory = ((double)((double)(Runtime.getRuntime().totalMemory()/1024)/1024))-
				((double)((double)(Runtime.getRuntime().freeMemory()/1024)/1024));
		if(current_memory>MaxMemory) 
		{
			MaxMemory = current_memory;
		}
	}
	
	/* deal the initial file of closed itemsets. */
 	public static void Data_Base() throws IOException  
	{
			FileReader file = new FileReader(Input_Path);
			BufferedReader Br = new BufferedReader(file);
			String line,tempString;
			String del = "#SUP";
			while( (line = Br.readLine()) !=null)
			{
				MemoryUsage();
				tempString = line;
				transactioncount++;
				tempString = tempString.replace(del,"");
				temp.add(tempString.split(":"));
			}
			Br.close();
	}				
	
 	/* put the data of closed itemsets into a Hashtable. */
	public static void gen_ItemSpace(Hashtable<String,Integer> H, ArrayList<String[]> T) 
	{
		String S="";
		for(int i=0; i<T.size(); i++)
		{
			MemoryUsage();
			for(int j=0; j<T.get(i).length; j++)
			{
				MemoryUsage();
				if( j%2==0 ) 
				{ 
					if(j!=T.get(i).length-1) 
					{
						S += T.get(i)[j]; S = S.trim();
					}
				}
				else if( j%2==1 ) 
				{ 
						int V = Integer.parseInt(T.get(i)[j].replaceAll(" ",""));
						H.put(S,V);
				}
			}
			S = "";
		}
	}
	
	/* build files by decreasing length of frequent closed itemsets. */
	public static void Write_Closed_txt() throws IOException 
	{
		for(int i=1; i<=Max; i++)
		{ 
		   MemoryUsage();
		   int R = i;
		   String S = Temp+String.valueOf(R);
		   File newTxt = new File(Output_Path+S+".txt");
		   readAllFiles(Output_Path+S+".txt");
		   if(!newTxt.exists())
		   {
			   newTxt.createNewFile();
		   }
			
		   FileWriter dataFile = new FileWriter(Output_Path+S+".txt");
		   BufferedWriter input = new BufferedWriter(dataFile);
			
		   for ( Map.Entry <String,Integer>entry:DB.entrySet())
		   {
			   MemoryUsage();
			   String k = entry.getKey();
			   String[] c = k.split(" ");
			   int v = entry.getValue();
			   if(c.length==R)
			   {
				   input.write(k);
				   input.write(":");
				   input.write(v + "\n");
			   }
		   }
		   input.close();
		   
		}
	}
	
	/* put the data into Hashtable.*/
	public static void Write_DB(String S, int V, Hashtable<String,Integer> H) 
	{
		if( H.containsKey(S)==false )
		{
			H.put(S,V);
		}
		else if(H.containsKey(S)==true)
		{
			if(H.get(S).intValue() < V)
			{
				H.put(S,V);
			}
		}
	}
	
	/* get the length of every itemsets. */
	public static void get_length() 
	{
		ArrayList<String[]> glength = new ArrayList<String[]>();
		for(Map.Entry<String, Integer>entry:DB.entrySet())
		{
			MemoryUsage();
			String k = entry.getKey();
			glength.add(k.split(" "));
			for(int i=0; i<glength.size(); i++)
			{
				MemoryUsage();
				int L = glength.get(i).length;
				record_length.add(L);
			}
		}
	}
	
	/* count the longest combination of frequent closed itemsets. */
	public static void get_Max()
	{
		Iterator iterator = record_length.iterator();
		Max = (int)iterator.next();
		while(iterator.hasNext())
		{ 
			MemoryUsage();
			int N = (int) iterator.next();
			if(N > Max)
			{
				Max = N;
			}
		}
	}
	
	/* The first time to deal the frequent closed itemsets.
	 * read the file of longest combination of frequent closed itemsets and put them into a ArrayList.
	 * read the file which combination is the longest combination minus 1 and put them into a ArrayList. */
	public static void Initial_Read_level() throws IOException
	{
		if(Max>1)
		{
			String S = "";
			String T = "";
			int V = Max;
			int W = Max-1;
			S = Temp+String.valueOf(V);
			T = Temp+String.valueOf(W);
		
			FileReader file = new FileReader(Output_Path+S+".txt");
			BufferedReader Br = new BufferedReader(file);
			
			FileReader file2 = new FileReader(Output_Path+T+".txt");
			BufferedReader Br2 = new BufferedReader(file2);
		
			
			String line,tempString;
			String line2,tempString2;
			
			while( (line = Br.readLine()) !=null )
			{
				MemoryUsage();
				tempString = line;
				temp.add(tempString.split(":"));
			}
			gen_ItemSpace(DB,temp);
			
			while( (line2 = Br2.readLine()) !=null )
			{
				MemoryUsage();
				tempString2 = line2;
				temp1.add(tempString2.split(":"));
			}
			gen_ItemSpace(FI,temp1);
			Max = Max - 1;
			Br.close();
			Br2.close();
		}
	}
	 
	/* After the first time to deal the frequent closed itemsets.
	 * read the file which combination is the recently longest combination minus 1 and put them into a ArrayList. */
	public static void Read_txt_level() throws IOException
	{
		
		String S = "";
		int V = Max-1;
		S = Temp+String.valueOf(V);
		
		FileReader file = new FileReader(Output_Path+S+".txt");
		BufferedReader Br = new BufferedReader(file);
		
		String line,tempString;
		
		if(Max>1)
		{
			while( (line = Br.readLine()) !=null )
			{
				MemoryUsage();
				tempString = line;
				temp.add(tempString.split(":"));
			}
			gen_ItemSpace(FI,temp);
			Max--;
		}
		Br.close();
	}

	/* clear the hashtable of longest combination frequent closed itemsets
	 * fill it with the other hashtable which was generated former
	 * clear the other hashtable which was generated former
	 * clear the ArrayList which was used to store the frequent closed itemsets former
	 * clear the ArrayList which was used to store the other frequent closed itemsets former  */
	public static void initial() throws IOException
	{
		DB.clear();
		DB.putAll(FI);
		FI.clear();
		temp.clear();
		temp1.clear();
	}
	 
	/* Generating the subsets and store them into the hashtable */
	public static void gen_subsets()
	{
		String S = "";
		{
			for ( Map.Entry <String,Integer>entry:DB.entrySet())
			{
				MemoryUsage();
				String k = entry.getKey();
				String[] T = k.split(" ");
				int v = entry.getValue();
				for(int i=0; i<T.length; i++)
				{
					MemoryUsage();
					for(int j=0; j<T.length; j++)
					{
						MemoryUsage();
						if(T.length==1)
						{
							S = T[j]; S = S.trim();
						}
						else
						{
							if(j!=i)
							{
								S += T[j]+" "; 
							}
						}
					}
					S = S.trim();
					Write_DB(S, v, FI);
					S="";
				}
			}
		}
	}
	
	/* write the frequent itemsets into file */
	public static void Write() throws IOException
	{
			FileWriter dataFile = new FileWriter(Output_Path,true);
			BufferedWriter input = new BufferedWriter(dataFile);
		
			for ( Map.Entry <String,Integer>entry:DB.entrySet())
			{
			   MemoryUsage();
			   String k = entry.getKey();
			   int v = entry.getValue();
			   
			   input.write(k);
			   input.write(" #SUP: ");
			   input.write(v + "\n"); 
		   }
		   input.close();
	}
	
	/* the first step of deriving task */
	public static void First_process() throws IOException
	{
		MemoryUsage();
		Data_Base();
		gen_ItemSpace(DB,temp);
		get_length();
		get_Max();
	    Write_Closed_txt();
	    DB.clear();
	    temp.clear();
	}
	
	/* deriving task */
	public static void Subsets_process() throws IOException
	{
		MemoryUsage();
		Initial_Read_level();
		Write();
		gen_subsets();
		initial();
		Write();
		
		while(Max>1)
		{
			Read_txt_level();
			gen_subsets();
			initial();
			Write();
		}
	}
	
	/* count the number of frequent itemsets finally */
	public static void Count() throws IOException
	{
		FileReader file = new FileReader(Output_Path);
		BufferedReader Br = new BufferedReader(file);
		
		String line;
		while( (line = Br.readLine()) !=null)
		{
			itemsetCount++;
		}
		Br.close();
	}
	
	/* record the path of subsets */
	public static void readAllFiles(String filePath)
	{
		allFiles.add(filePath);
	}
	
	/*Delete all subsets that be generated in the process*/
	public static void Delete()
	{
		for(int i=0; i<allFiles.size(); i++)
		{
			File f = new File((String)allFiles.get(i));
			f.deleteOnExit();
		}
	}

	/**
	 * Method to run the LevelWise algorithm.
	 * @param input the path to an input file containing Frequent Closed Itemsets.
	 * @param output the output file path for saving the result (if null, the result 
	 *        will be returned by the method instead of being saved).
	 * @return the result if no output file path is provided.
	 * @throws IOException exception if error reading or writing files
	 */
	public void runAlgorithm(String input,String output) throws IOException
	{
		Input_Path = input;
		Output_Path = output;
		
		//--- Modified by Philippe
		//Delete output file
		File file = new File(output);
		file.delete();
		//---
		
		MemoryUsage();
		startTimestamp = System.currentTimeMillis();
		First_process();
		
		MemoryUsage();
		Subsets_process();
		
		endTime = System.currentTimeMillis();
		MemoryUsage();
		Count();
		Delete();
	}
	

	/**
	 * Print statistics about the algorithm execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  LevelWise - V.2.34 STATS =============");
		long temps = endTime - startTimestamp;
		System.out.println(" Transactions count from database : " + transactioncount);
		System.out.println(" Frequent itemsets count : " + itemsetCount);
		System.out.print(" Max memory usage: " + MaxMemory + " mb \n");
		System.out.println(" Total time ~ " + temps + " ms");
		System.out.println("===================================================");
	}
}

