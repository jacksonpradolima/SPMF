package ca.pfv.spmf.algorithms.frequentpatterns.sppgrowth;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ca.pfv.spmf.tools.MemoryLogger;

/**
 * Copyright (c) 2019 Peng Yang, Philippe Fournier-Viger et al.

 * This file is part of the SPMF DATA MINING SOFTWARE *
 * (http://www.philippe-fournier-viger.com/spmf).
 * 
 * SPMF is free software: you can redistribute it and/or modify it under the *
 * terms of the GNU General Public License as published by the Free Software *
 * Foundation, either version 3 of the License, or (at your option) any later *
 * version. SPMF is distributed in the hope that it will be useful, but WITHOUT
 * ANY * WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * This is an implementation of the SPP-growth algorithm based on FP-growth
 * It is fast and memory efficient because it uses a compact tree datastrcture.
 * The SPP-growth algorithm finds all periodic time-intervals of patterns.
 * SPP-Growth is presented in this paper:
 * <br/><br/>
 * Fournier-Viger, P., Yang, P., Lin, J. C.-W., Kiran, U. (2019). Discovering Stable Periodic-Frequent Patterns 
 * in Transactional Data. Proc. 32nd Intern. Conf. on Industrial, Engineering and Other Applications of Applied 
 * Intelligent Systems (IEA AIE 2019), Springer LNAI, pp. 230-244
 * 
 * @see SPPTree
 * @see SPPNode
 * @see Support_maxla
 */

public class AlgoSPPgrowth {
    /**  start time of the latest execution */
    private long startTimestamp; 
    /** end time of the latest execution */
    private long endTime; 

    /** largest TID in the database */
    private int lastTID = -1; 
    
    /** number of freq. itemsets found */
    private int itemsetCount; 

    /** object to write the output file */
    BufferedWriter writer = null; 

    /** The  patterns that are found (if the user want to keep them into memory) */
    protected Itemsets patterns = null;

    /** This variable is used to determine the size of buffers to store itemsets.
    // A value of 50 is enough because it allows up to 2^50 patterns! */
    final int BUFFERS_SIZE = 2000;

    /** buffer for storing the current itemset that is mined when performing mining
    // the idea is to always reuse the same buffer to reduce memory usage. */
    private int[] itemsetBuffer = null;

    /** This buffer is used to store an itemset that will be written to file
    // so that the algorithm can sort the itemset before it is output to file
    // (when the user choose to output result to file). */
    private int[] itemsetOutputBuffer = null;

    /** maximum pattern length */
    private int maxPatternLength = 1000;

    /** whether the timestamps need self increment as step of 1 for each transcation
     * or timestamps is provided in the input file
    // default as true */
    private boolean self_increment;

    /** the minimum duration threshold. */
    private int minSup;

    /** the maximum periodicity threshold. */
    private int maxPer;

    /** the maxLa */
    private int maxLa;


    /**
     * Constructor
     */
    public AlgoSPPgrowth() {
    	
    }

    /**
     * Method to run the FPGRowth algorithm.
     * @param input the path to an input file containing a transaction database.
     * @param output the output file path for saving the result (if null, the result
     *        will be returned by the method instead of being saved).
     * @param maxPer the maximum periodicity threshold
     * @param minSup the minimum support threshold
     * @param maxLa  the max lability threshold
     * @return the result if no output file path is provided.
     * @throws IOException exception if error reading or writing files
     */
    public Itemsets runAlgorithm(String input, String output, int maxPer, int minSup, int maxLa, boolean self_increment) throws FileNotFoundException, IOException {
        // record start time
        startTimestamp = System.currentTimeMillis();

        // number of itemsets found
        itemsetCount = 0;
        
        // save the parameters
        this.minSup = minSup;
        this.maxPer = maxPer;
        this.self_increment = self_increment;
        this.maxLa = maxLa;

        //initialize tool to record memory usage
        MemoryLogger.getInstance().reset();
        MemoryLogger.getInstance().checkMemory();

        // if the user want to keep the result into memory
        if (output == null) {
            writer = null;
            patterns = new Itemsets("Periodic Frequent Time-Interval of Itemsets");
        } else { // if the user want to save the result to a file
            patterns = null;
            writer = new BufferedWriter(new FileWriter(output));
            itemsetOutputBuffer = new int[BUFFERS_SIZE];
        }

        // (1) PREPROCESSING: Initial database scan to determine the maxla of each item
        // The TID is stored in a map:
        //    key: item   value: maxla
        final Map<Integer, Support_maxla> mapSPP_list = scanDatabaseToDeterminSPPlistOfSingleItems(input);


        // (2) Scan the database again to build the initial SPP-Tree
        // Before inserting a transaction in the SPPTree, we sort the items
        // by descending order of item's support.
        SPPTree tree = new SPPTree();

        buildTreeByScanDataAgain(tree, input,mapSPP_list);
        System.out.println("# of node : "+tree.numOfNode);

        // (3) We start to mine the SPP-Tree by calling the recursive method.
        // Initially, the prefix alpha is empty.
        // if at least an item has periodic frequent time-interval
        if(tree.headerList.size() > 0) {
            // initialize the buffer for storing the current itemset
            itemsetBuffer = new int[BUFFERS_SIZE];

            // recursively generate the itemsets that have periodic frequent time-interval  using the SPP-tree
            // Note: we assume that the initial SPP-Tree has more than one path
            // which should generally be the case.
            SPPGrowth(tree, itemsetBuffer, 0, mapSPP_list);
        }


//        // test
//        writer.write(testRes.toString());

        // close the output file if the result was saved to a file
        if(writer != null){
            writer.close();
        }
        // record the execution end time
        endTime= System.currentTimeMillis();

        // check the memory usage
        MemoryLogger.getInstance().checkMemory();

        // return the result (if saved to memory)

        return patterns;


    }

    private void SPPGrowth(SPPTree tree, int[] prefix, int prefixLength, Map<Integer, Support_maxla> mapSPP_list) throws IOException {
        if(prefixLength == maxPatternLength){
            return;
        }

        // For each  item in the header table list of the tree in reverse order.
        while(tree.headerList.size()>0){
            // get the tail item
            Integer item = tree.headerList.get(tree.headerList.size()-1);

            // Create Beta by concatening prefix Alpha by adding the current item to alpha
            prefix[prefixLength] = item;


            // save beta to the output file
            saveItemset(prefix, prefixLength+1, mapSPP_list.get(item).getSupport(),mapSPP_list.get(item).getMaxla());

            if(prefixLength+1 < maxPatternLength){

                // === (A) Construct beta's prefix tree ===
                // It is a subdatabase which consists of the set of prefix paths
                // in the SPP-tree co-occuring with the prefix pattern.
                List<List<SPPNode>> prefixPaths = new ArrayList<List<SPPNode>>();

                SPPNode path = tree.mapItemNodes.get(item);

                // Map to count the TIDs of items in the conditional prefix tree
                // Key: item   Value: TIDs
                Map<Integer, List<Integer>> mapBetaTIDs = new HashMap<Integer, List<Integer>>();

                while(path != null) {
                    // if the path is not just the root node
                    if (path.parent.itemID != -1) {
                        // create the prefixpath
                        List<SPPNode> prefixPath = new ArrayList<SPPNode>();
                        // add this node.
                        prefixPath.add(path);   // NOTE: we add it just to keep its TID,
                        // actually it should not be part of the prefixPath

                        List<Integer> pathTIDs = path.TIDs;

                        //Recursively add all the parents of this node.
                        SPPNode parent = path.parent;

                        while (parent.itemID != -1) {
                            prefixPath.add(parent);

                            // FOR EACH PATTERN WE ALSO UPDATE THE ITEM TIMESTAMPS AT THE SAME TIME
                            // if the first time we see that node id
                            if (mapBetaTIDs.get(parent.itemID) == null) {
                                // just add the path timestamps
                                mapBetaTIDs.put(parent.itemID, new ArrayList<Integer>(){{addAll(pathTIDs);}});
                            } else {
                                // otherwise, add all of timestamps to map
                                mapBetaTIDs.get(parent.itemID).addAll(pathTIDs);
                            }
                            parent = parent.parent;
                        }
                        // add the path to the list of prefixpaths
                        prefixPaths.add(prefixPath);
                    }
                    // We will look for the next prefixpath
                    path = path.nodeLink;
                }

                // convert beta's timestamps to support and maxla
                Map<Integer, Support_maxla> mapBetaSPPlist = getMapBetaSPPlist(mapBetaTIDs);

                // header table has SPP
                if(mapBetaSPPlist.size()>0) {
                    // (B) Construct beta's conditional SPPTree
                    // Create the tree.
                    SPPTree treeBeta = new SPPTree();
                    // Add each prefixpath in the SPPTree.
                    for (List<SPPNode> prefixPath : prefixPaths) {
                        treeBeta.addPrefixPath(prefixPath, mapBetaSPPlist);
                    }

                    // Mine recursively the Beta tree if the root has child(s)
                    if (treeBeta.root.childs.size() > 0) {

                        // Create the header list.
                        treeBeta.createHeaderList(tree.headerList,mapBetaSPPlist);
                        // recursive call
                        SPPGrowth(treeBeta, prefix, prefixLength + 1, mapBetaSPPlist);
                    }
                }
                // refreshing SPP-Tree by removing the tail item
                // the timestamps of tail item should be moved to its parent.
                tree.removeTailItem();
            }
        }

        // check the memory usage
        MemoryLogger.getInstance().checkMemory();
    }

    /**
     *    convert beta's timestamps to time-intervals
     * @param mapBetaTIDs
     * @return
     */
    private Map<Integer, Support_maxla> getMapBetaSPPlist(Map<Integer, List<Integer>> mapBetaTIDs){

        Map<Integer, Support_maxla> mapBetaSPPlist = new HashMap<>();


        for(Map.Entry<Integer,List<Integer>> entry:mapBetaTIDs.entrySet()) {

            Support_maxla sm = new Support_maxla();
            List<Integer> TIDs = entry.getValue();
            // 1,sort the timestamps
            Collections.sort(TIDs);

            // 2.scan the timestamps
            int pre_TID = 0;
            int pre_la = 0;
            for (int current_TID : TIDs) {

                int current_la = Math.max(0,pre_la+current_TID-pre_TID-maxPer);

                sm.setMaxla(current_la);
                sm.increaseSupport();

                pre_TID = current_TID;
                pre_la = current_la;
            }

            // 3. Deal with the last timestamp

            int current_la = Math.max(0,pre_la+lastTID-pre_TID-maxPer);

            sm.setMaxla(current_la);

            // 4. save time-interval
            if(sm.getSupport() >= minSup && sm.getMaxla() <= maxLa){
                mapBetaSPPlist.put(entry.getKey(),sm);
            }
        }
        // clear the memory of mapTimestampsBeta
        mapBetaTIDs.clear();

        return mapBetaSPPlist;
    }


    private void saveItemset(int[] itemset, int itemsetLength, int support, int maxla) throws IOException {

        // increase the number of itemsets found for statistics purpose
        itemsetCount++;

         // if the result should be saved to a file
        if(writer != null){
            // copy the itemset in the output buffer and sort items
            System.arraycopy(itemset, 0, itemsetOutputBuffer, 0, itemsetLength);
            Arrays.sort(itemsetOutputBuffer, 0, itemsetLength);

            // Create a string buffer
            StringBuilder buffer = new StringBuilder();
            // write the items of the itemset
            for(int i=0; i< itemsetLength; i++){
                buffer.append(itemsetOutputBuffer[i]);
                if(i != itemsetLength-1){
                    buffer.append(' ');
                }
            }
            // Then, write the support and maxla
            buffer.append(" #SUP: ");
            buffer.append(support);

            buffer.append(" #MAXLA: ");
            buffer.append(maxla);

            // write to file and create a new line
            writer.write(buffer.toString());
            writer.newLine();

        }// otherwise the result is kept into memory
        else{
            // create an object Itemset and add it to the set of patterns
            // found.
            int[] itemsetArray = new int[itemsetLength];
            System.arraycopy(itemset, 0, itemsetArray, 0, itemsetLength);

            // sort the itemset so that it is sorted according to lexical ordering before we show it to the user
            Arrays.sort(itemsetArray);

            Itemset itemsetObj = new Itemset(itemsetArray);
            patterns.addItemset(itemsetObj, itemsetLength);
        }
    }


    private void buildTreeByScanDataAgain(SPPTree tree, String input, Map<Integer, Support_maxla> mapSPP_list) throws IOException {
        // read file
        BufferedReader reader = new BufferedReader(new FileReader(input));

        String line;

        if(self_increment) { // the timestamp is self-increment

            int current_TID = 1;
            while (((line = reader.readLine()) != null)) {
                // if the line is  a comment, is  empty or is a
                // kind of metadata
                if (line.isEmpty() || line.charAt(0) == '#' || line.charAt(0) == '%'
                        || line.charAt(0) == '@') {
                    continue;
                }
                String[] lineSplited = line.split(" ");

                List<Integer> transaction = new ArrayList<Integer>();

                for (String itemString : lineSplited) {
                    Integer item_name = Integer.parseInt(itemString);

                    // only the item is SPP
                    // and the current timestamp in its time-interval
                    // then this item can be added to the transaction (tree) .
                    if(mapSPP_list.containsKey(item_name) && !transaction.contains(item_name)){
                        transaction.add(item_name);
                    }
                }
                // sort item in the transaction by descending order of total duration
                Collections.sort(transaction, new Comparator<Integer>(){
                    public int compare(Integer item1, Integer item2){
                         //compare the support
                        int compare = mapSPP_list.get(item2).getSupport() -  mapSPP_list.get(item1).getSupport();
                        // if the same support, we check the lexical ordering!
                        if(compare == 0){
                            return (item1 - item2);
                        }
                        // otherwise, just use the total duration
                        return compare;
//                        return item1- item2;
                    }
                });

                // add the sorted transaction and current timestamp into tree.
                if(transaction.size()>0){
                    tree.addTransaction(transaction,current_TID);
                }
                // self increment
                current_TID++;
            }

        }else {  //// the timestamp exist in file

            int current_TID = 1;

            while (((line = reader.readLine()) != null)) {
                if (line.isEmpty() || line.charAt(0) == '#' || line.charAt(0) == '%' || line.charAt(0) == '@') {
                    continue;
                }

                String[] lineSplited = line.trim().split("\\|");
                String[] lineItems = lineSplited[0].trim().split(" ");

                current_TID = Integer.parseInt(lineSplited[1]);

                List<Integer> transaction = new ArrayList<Integer>();

                for (String itemString : lineItems) {
                    Integer item_name = Integer.parseInt(itemString);

                    // only the item has periodic frequent time-interval
                    // and the current timestamp in its time-interval
                    // then this item can be added to the transaction (tree) .
                    if(mapSPP_list.containsKey(item_name) && !transaction.contains(item_name)){
                        transaction.add(item_name);
                    }

                }
                // sort item in the transaction by descending order of total duration
                Collections.sort(transaction, new Comparator<Integer>(){
                    public int compare(Integer item1, Integer item2){

                        // compare the support
                        int compare = mapSPP_list.get(item2).getSupport() - mapSPP_list.get(item1).getSupport();
                        // if the same support, we check the lexical ordering!
                        if(compare == 0){
                            return (item1 - item2);
                        }
                        // otherwise, just use the total duration
                        return compare;
//                        return item1 -item2;
                    }
                });
                // add the sorted transaction and current timestamp into tree.
                if(transaction.size()>0){
                    tree.addTransaction(transaction,current_TID);
                }
            }
        }

        // close the input file
        reader.close();

        // We create the header table for the tree using the calculated support of single items
        tree.createHeaderList(null,mapSPP_list);

    }



    private Map<Integer, Support_maxla> scanDatabaseToDeterminSPPlistOfSingleItems(String input) throws IOException {
        // read file
        BufferedReader reader = new BufferedReader(new FileReader(input));
        String line;

        // The maxla is stored in a map:
        //    key: item   value: maxla
        Map<Integer, Support_maxla> mapSPP_list = new HashMap<>();

        // this save the previous timestamp of item
        //    key:   item ,     value: previous TID
        Map<Integer,Integer> preTID = new HashMap<>();

        // this save the current lability of a item
        //   key:   item ,     value: i-th lability
        Map<Integer,Integer> prela = new HashMap<>();

        if(self_increment) { // the timestamp is self-increment
            int current_TID = 1;
            while (((line = reader.readLine()) != null)) {
                // if the line is  a comment, is  empty or is a
                // kind of metadata
                if (line.isEmpty() || line.charAt(0) == '#' || line.charAt(0) == '%'
                        || line.charAt(0) == '@') {
                    continue;
                }
                String[] lineSplited = line.split(" ");

                for (String itemString : lineSplited) {
                    Integer item_name = Integer.parseInt(itemString);


                    int pre_TID = preTID.getOrDefault(item_name, 0);
                    // calculate the periodicity
                    int per = current_TID - pre_TID;
                    // if a transaction has same item
                    if (per == 0) continue;

                    
                    int current_la = Math.max(0, prela.getOrDefault(item_name, 0) + per - maxPer);
                    if(!mapSPP_list.containsKey(item_name)) mapSPP_list.put(item_name,new Support_maxla());
                    mapSPP_list.get(item_name).setMaxla(current_la);
                    
                    prela.put(item_name,current_la);
                    preTID.put(item_name,current_TID);
                    mapSPP_list.get(item_name).increaseSupport();
                }
                current_TID++;
            }
            lastTID = current_TID - 1;

        }else {  //// the timestamp exist in file
            int current_TID=1;
            while( ((line = reader.readLine())!= null)) {
                if (line.isEmpty() || line.charAt(0) == '#' || line.charAt(0) == '%' || line.charAt(0) == '@') {
                    continue;
                }

                String[] lineSplited = line.split("\\|");
                String[] lineItems = lineSplited[0].split(" ");
                current_TID = Integer.parseInt(lineSplited[1]);
                for (String itemString : lineItems) {
                    Integer item_name = Integer.parseInt(itemString);


                    int pre_TID = preTID.getOrDefault(item_name, 0);
                    // calculate the periodicity
                    int per = current_TID - pre_TID;
                    // if a transaction has same item
                    if (per == 0) continue;



                    int current_la = Math.max(0, prela.getOrDefault(item_name, 0) + per - maxPer);
                    if(!mapSPP_list.containsKey(item_name)) mapSPP_list.put(item_name,new Support_maxla());
                    mapSPP_list.get(item_name).setMaxla(current_la);

                    prela.put(item_name,current_la);
                    preTID.put(item_name,current_TID);
                    mapSPP_list.get(item_name).increaseSupport();
                }
            }
            lastTID = current_TID;
        }
        // close the input file
        reader.close();

        // Deal with the last TID

        Iterator<Map.Entry<Integer, Support_maxla>> it = mapSPP_list.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Support_maxla> entry = it.next();
            int item_name = entry.getKey();

//            // for test
//            if (item_name == 449)
//                testRes.add(lastTID-preTID.get(item_name));

            entry.getValue().setMaxla(prela.get(item_name)+lastTID - preTID.get(item_name) - maxPer);

            //  the item has not periodic frequent time-interval
            if(entry.getValue().getSupport() < minSup || entry.getValue().getMaxla() > maxLa){
                // remove it.
                it.remove();
            }
        }
        prela.clear();
        preTID.clear();

        return mapSPP_list;
    }


    /**
     * Print statistics about the algorithm execution to System.out.
     */
    public void printStats() {
        System.out.println("=============  SPP-growth  - STATS ===============");
        long temps = endTime - startTimestamp;
        System.out.print(" Max memory usage: " + MemoryLogger.getInstance().getMaxMemory() + " mb \n");
        System.out.println(" Itemset counts : " + this.itemsetCount);
        System.out.println(" Total time ~ " + temps + " ms");
        System.out.println("===================================================");
    }

    /**
     * Set the maximum pattern length
     * @param length the maximum length
     */
    public void setMaximumPatternLength(int length) {
        maxPatternLength = length;
    }

    public void cancelSelfIncrement(){
        this.self_increment = false;
    }


}