package ca.pfv.spmf.algorithms.frequentpatterns.memu;

/* This file is copyright (c) 2008-2019 Shi-Feng Ren
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
*
*/

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ca.pfv.spmf.tools.MemoryLogger;


/**
 * This is an implementation of the "MEMU" algorithm for High-Average-Utility Itemsets Mining
 * as described in the conference paper : <br/><br/>
 *
 * Jerry Chun-Wei Lin, Shifeng Ren, and Philippe Fournier-Viger. MEMU: More Efficient Algorithm to Mine High Average-Utility Patterns with Multiple Minimum Average-Utility Thresholds, IEEE Access (unpublished)
 *
 * @see CAUList
 * @see CAUEntry
 * @author Shi-Feng Ren
 */
public class AlgoMEMU {
	
	/** The time at which the algorithm started */
	private long startTimestamp;

	/**  The time at which the algorithm ended */
	private long endTimestamp;

	/** The number of generated high-average-utility itemsets  */
	private int hauiCount;

	/** The number of candidate high-utility itemsets */
	private long candidateCount;

	/** Map item to MAU */
	private Map<Integer, Integer> item2mau = null;

	/** The eaucs structure:  key: item   key: another item   value: auub */
	private Map<Integer, Map<Integer, Long>> mapEUCS =null;

	/** Buffer for current itemset */
	private int BUFFERS_SIZE = 200;
	private int[] itemsetBuffer = null;

    /** Write HAUIs in a specified file */
	private BufferedWriter writer = null;

	/** least MAU */
	private int leastMAU;

	/** This class represent an item and its utility in a transaction */
	private class Pair{
		int item = 0;
		int utility = 0;
	}

	/**
	 * Constructor
	 */
	public AlgoMEMU() {
		// Nothing to do
	}
	
	/**
	 *  read profit of each item in database
	 * @param profitPath
	 * @return
	 * @throws IOException 
	 * @throws Exception
	 */
	private Map<Integer, Integer> readProfits(String profitPath) throws IOException {
		System.out.println(profitPath);
		Map<Integer, Integer> item2profits = new HashMap<>();
		BufferedReader in = new BufferedReader(new FileReader(profitPath));
		String line = null;
		String[] pair = null;
		while ( (line = in.readLine())!=null){
			pair = line.split(", ");
			item2profits.put(Integer.parseInt(pair[0].trim()),
					Integer.parseInt(pair[1].trim()));
		}
		in.close();
		return item2profits;
	}



	/**
	 * Run the algorithm
	 * @param outputFilePath Specify the file used to keep the discovered HAUIs
	 * @param inputProfit the input file path of profit file
	 * @param inputDB the input file path of db
	 * @param beta the constant used to randomly generate threshold for each items in DB
	 * @param GLMAU the global minimum utility threshold
	 * @throws IOException exception if error while writing the file
	 */
	public void runAlgorithm(String inputProfit, String inputDB, String outputFilePath,  
			final int beta, final int GLMAU) throws IOException {
		
		// Initialize variables for statistics
		startTimestamp = 0;
		endTimestamp = 0;
		hauiCount = 0;
		candidateCount = 0;
		leastMAU = 0;

		startTimestamp = System.currentTimeMillis();
		
		MemoryLogger.getInstance().reset();

		// initialize the buffer for storing the current itemset
		itemsetBuffer = new int[BUFFERS_SIZE];
		mapEUCS =  new HashMap<>();

		Map<Integer, Long> item2auub  = new HashMap<Integer, Long>();

		item2mau = new HashMap<>();

		// Initialize writer
        if(outputFilePath!=null && !outputFilePath.equalsIgnoreCase("null")){
        	 writer = new BufferedWriter(new FileWriter(outputFilePath));
        }
           
		// Read items' profit
		Map<Integer, Integer> item2profits = readProfits(inputProfit);

		// Generate MAU for each item
		leastMAU = Integer.MAX_VALUE;
		for(Entry<Integer,Integer> entry : item2profits.entrySet()){
			int val = Math.max(entry.getValue() * beta, GLMAU);
			leastMAU = leastMAU>val ? val : leastMAU;
            item2mau.put(entry.getKey(), val);
		}


		// Scan the database a first time to calculate the auub of each item.
		BufferedReader dbReader = null;
		String curTran;

		dbReader = new BufferedReader(new InputStreamReader( new FileInputStream(new File(inputDB))));
		// for each transaction until the end of file
		int quantity;
		int itemName;
		while ((curTran = dbReader.readLine()) != null) {
			String[] items = curTran.split(" ");
			int maxItemUtility = -1;
			for(int i=0; i <items.length; i+=2){
				itemName = Integer.parseInt(items[i].trim());
				quantity = Integer.parseInt(items[i+1].trim());
				int tmputility = quantity * item2profits.get(itemName);
				if(maxItemUtility < tmputility){
					maxItemUtility = tmputility;
				}
			}
			for(int i=0; i <items.length; i+=2){
				itemName = Integer.parseInt(items[i].trim());
				Long auub = item2auub.get(itemName);
				// add the utility of the item in the current transaction to its AUUB
				auub = (auub == null)?
						maxItemUtility : auub + maxItemUtility;
				item2auub.put(itemName, auub);
			}
		}
		dbReader.close();

        // Container of CAUList of each item whose auub value >= leastMAU.
		List<CAUList> listOfCAULists = new ArrayList<CAUList>();

        // Key: item, Value:CAUList
		Map<Integer, CAUList> mapItemToUtilityList = new HashMap<Integer, CAUList>();


		for(Entry<Integer,Long> entry: item2auub.entrySet()){
			Integer item = entry.getKey();
			if(item2auub.get(item) >= leastMAU){
				CAUList uList = new CAUList(item);
				mapItemToUtilityList.put(item, uList);
				listOfCAULists.add(uList);
			}
		}

		//  Sort CAUList according to its mau-ascending order
		Collections.sort(listOfCAULists, new Comparator<CAUList>(){
			public int compare(CAUList o1, CAUList o2) {
				return compareItems(o1.item, o2.item);
			}
		} );


		// Scan DB again to construct CAUList of each item whose auub value >= minUtility.
		dbReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(inputDB))));
		int tid =0;
		while ((curTran = dbReader.readLine()) != null) {
			String[]  items= curTran.split(" ");
			List<Pair> revisedTransaction = new ArrayList<Pair>();
			int maxUtility =0;
			for(int i=0; i <items.length; i+=2){
				itemName = Integer.parseInt(items[i].trim());
				quantity = Integer.parseInt(items[i+1].trim());
				int tmputility = quantity * item2profits.get(itemName);

				Pair pair = new Pair();
				pair.item = itemName;
				pair.utility = tmputility;


				// if the item's auub value >= LeastMAU
				if(item2auub.get(pair.item) >= leastMAU){
					if(maxUtility < pair.utility){
						maxUtility = pair.utility;
					}
					revisedTransaction.add(pair);
				}
			}

			Collections.sort(revisedTransaction, new Comparator<Pair>(){
				public int compare(Pair o1, Pair o2) {
					return compareItems(o1.item, o2.item);
				}
			});

			int rmu=0;
			int remu=0;
			for(int i = revisedTransaction.size()-1; i>=0; --i){
				Pair pair =  revisedTransaction.get(i);
				rmu = pair.utility > rmu ? pair.utility : rmu;
				// get the utility list of this item
				CAUList cauListOfItem = mapItemToUtilityList.get(pair.item);
				// Add a new CAUEntry to the utility list of this item corresponding to this transaction
				CAUEntry cauEntry =null;

                cauEntry = new CAUEntry(tid, pair.utility, rmu, remu);

				cauListOfItem.addElement(cauEntry);

                remu = (remu<pair.utility) ? pair.utility : remu;
			}

			for(int i = 0; i<revisedTransaction.size(); ++i){
				Pair pair =  revisedTransaction.get(i);
					Map<Integer, Long> subEAUCS = mapEUCS.get(pair.item);
					if(subEAUCS == null) {
						subEAUCS = new HashMap<Integer, Long>();
						mapEUCS.put(pair.item, subEAUCS);
					}
					for(int j = i+1; j< revisedTransaction.size(); ++j){
						Pair pairAfter = revisedTransaction.get(j);
						Long twoAuub = subEAUCS.get(pairAfter.item);
						if(twoAuub == null) {
							subEAUCS.put(pairAfter.item, (long)(maxUtility));
						}else {
							subEAUCS.put(pairAfter.item, twoAuub + maxUtility);
						}
					}
			}
			tid++;
		}
		dbReader.close();

		// Mine the database recursively
		search(itemsetBuffer, 0, null, listOfCAULists, 0);

		if(writer!=null)
		    writer.close();
		
		MemoryLogger.getInstance().checkMemory();

		endTimestamp = System.currentTimeMillis();
	}


	/**
	 * A comparator for items
	 * @param item1 an item
	 * @param item2 another item
	 * @return a value <0 , == 0  or >0
	 */
	private int compareItems(int item1, int item2) {
		int compare = ( item2mau.get(item1) - item2mau.get(item2));
		return (compare == 0)? item1 - item2 :  compare;
	}
	/**
	 * @param prefix  This is the current prefix. Initially, it is empty.
	 * @param p This is the Utility List of the prefix. Initially, it is empty.
	 * @param cauListOfP The utility lists corresponding to each extension of the prefix.
	 * @param sumMAUOfPrefix The sum of mau of prefix.
	 * @param prefixLength The current prefix length
	 * @throws IOException
	 */
	private void search(int [] prefix, int prefixLength, CAUList p, List<CAUList> cauListOfP, int sumMAUOfPrefix)
			throws IOException {

		// For each extension X of prefix P
		for(int i=0; i< cauListOfP.size(); i++){
			CAUList x = cauListOfP.get(i);

			int sumMAUOfPx= (item2mau.get(x.item) + sumMAUOfPrefix);


			// we save the itemset:  pX
			if(x.sumUtility >= sumMAUOfPx) {
				hauiCount++;
				if(writer!=null)
				    writeOut(prefix, prefixLength, x.item, x.sumUtility / (double)(prefixLength+1), sumMAUOfPx / (double)(prefixLength+1));
			}

			// Check looser Upper bound
            if((x.sumUtility + x.sumOfRemu*(prefixLength+1) ) < sumMAUOfPx ) {
                continue;
            }


			// Check revised tighter upper bound
			if(x.sumOfRmu * (prefixLength+1) >= sumMAUOfPx) {
				// This list will contain the CAU lists of pX 1-extensions.
				List<CAUList> exULs = new ArrayList<>();
				// For each extension of p appearing
				// after X according to the ascending order
				for(int j=i+1; j < cauListOfP.size(); j++) {
					CAUList y = cauListOfP.get(j);

					// MAUCP strategy
                    Map<Integer, Long> auub1 = mapEUCS.get(x.item);
                    if(auub1 != null) {
                        Long auub2 = auub1.get(y.item);
                        long mauOfPrefix = 0;
                        if(prefixLength!=0) mauOfPrefix = sumMAUOfPrefix / prefixLength;
                        if(auub2 == null || auub2 < Math.max(leastMAU, mauOfPrefix)) {
                            continue;
                        }
                    }

					candidateCount++;

					// Construct  new itemset pXY and its CAU-List
					// and add it to the list of extensions of pX
					CAUList pxy = construct(prefixLength+1, p, x, y, sumMAUOfPx);
					if(pxy != null) {
						exULs.add(pxy);
					}
				}
                // Allocate new buffer when buffer size get small
                if(prefixLength==BUFFERS_SIZE) {
                    BUFFERS_SIZE = BUFFERS_SIZE + (BUFFERS_SIZE/4);
                    int[] tmp = new int[BUFFERS_SIZE];
                    System.arraycopy(itemsetBuffer,0, tmp, 0, prefixLength);
                    itemsetBuffer = tmp;
                }
				// Create new prefix pX
				itemsetBuffer[prefixLength] = x.item;
				// Recursive call to discover all itemsets with the prefix pX
				search(itemsetBuffer, prefixLength+1, x, exULs, sumMAUOfPx);
			}
		}
		
		MemoryLogger.getInstance().checkMemory();
	}

    private CAUList construct(int prefixLen, CAUList p, CAUList px, CAUList py, long sumMAUOfPx) {
        // create an empy utility list for Pxy
        CAUList pxyUL = new CAUList(py.item);

        long sumOfRmu = px.sumOfRmu;
        long sumOfRemu = (long)(px.sumUtility / (double)prefixLen + px.sumOfRemu);

        // For each element in the utility list of pX
        for(CAUEntry ex : px.cauEntries) {
            // Do a binary search to find element ey in py with tid = ex.tid
            CAUEntry ey = findElementWithTID(py, ex.tid);
            if(ey == null) {
                sumOfRmu -= ex.rmu;
                sumOfRemu -= (ex.utility /(double)prefixLen + ex.remu);
                if(Math.min(sumOfRemu, sumOfRmu) * prefixLen < sumMAUOfPx) {
                    return null;
                }
                continue;
            }

            // If the prefix p is null
            if(p == null){
                // Create the new element
                CAUEntry eXY = new CAUEntry(ex.tid, ex.utility + ey.utility, ex.rmu, ey.remu);

                // add the new element to the utility list of pXY
                pxyUL.addElement(eXY);

            } else {
                // find the element in the utility list of p wih the same tid
                CAUEntry e = findElementWithTID(p, ex.tid);
                if(e != null){
                    // Create new element
                    CAUEntry eXY = new CAUEntry(ex.tid, ex.utility + ey.utility - e.utility,
                            ex.rmu, ey.remu);
                    // add the new element to the utility list of pXY
                    pxyUL.addElement(eXY);
                }
            }
        }
        // return the utility list of pXY.
        return pxyUL;
    }


	/**
	 * Do a binary search to find the element with a given tid in a utility list
	 * @param ulist the utility list
	 * @param tid  the tid
	 * @return  the element or null if none has the tid.
	 */
	private CAUEntry findElementWithTID(CAUList ulist, int tid){
		List<CAUEntry> list = ulist.cauEntries;

		// perform a binary search to check if  the subset appears in  level k-1.
        int first = 0;
        int last = list.size() - 1;

        // the binary search
        while( first <= last )
        {
        	int middle = ( first + last ) >>> 1; // divide by 2

            if(list.get(middle).tid < tid){
            	first = middle + 1;  //  the itemset compared is larger than the subset according to the lexical order
            }
            else if(list.get(middle).tid > tid){
            	last = middle - 1; //  the itemset compared is smaller than the subset  is smaller according to the lexical order
            }
            else{
            	return list.get(middle);
            }
        }
		return null;
	}

    /**
     * Write an itemset to the output file
     * @param prefix the prefix of the itemset
     * @param prefixLength the length of the prefix
     * @param item an item to be appended to the prefix
     * @param averageUtility the average utility of the itemset
     * @param mau the mau of the itemset
     * @throws IOException if error writing or reading
     */
    private void writeOut(int[] prefix, int prefixLength, int item, double averageUtility, double mau) throws IOException {

        //Create a string buffer
        StringBuilder buffer = new StringBuilder();
        // append the prefix
        for (int i = 0; i < prefixLength; i++) {
            buffer.append(prefix[i]+" ");
        }
        // append the last item
        buffer.append(item);
        // append the utility value
        buffer.append(" #AUTIL: ");
        buffer.append(String.format("%.2f", averageUtility));
        buffer.append(" #mau: "+ String.format("%.2f", mau));
        // write to file
        writer.write(buffer.toString());
        writer.newLine();
    }
    
	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  MEMU ALGORITHM v.2.36 - STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println(" High-utility itemsets count : " + hauiCount);
		System.out.println("===================================================");
	}
}