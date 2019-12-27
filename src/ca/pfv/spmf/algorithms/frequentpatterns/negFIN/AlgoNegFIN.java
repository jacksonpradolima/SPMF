package ca.pfv.spmf.algorithms.frequentpatterns.negFIN;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import ca.pfv.spmf.tools.MemoryLogger;

/*
 ** The implementation of the "negFIN algorithm", the algorithm presented in:
 * "Nader Aryabarzan, Behrouz Minaei-Bidgoli, and Mohammad Teshnehlab. (2018). negFIN: An efficient algorithm for fast mining frequent itemsets. Expert System with Applications, 105, 129–143"
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
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * This implementation was obtained by converting the C++ code of the negFIN algorithm to Java.
 * The C++ code of this algorithm was provided by Nader Aryabarzan, available on GitHub via https://github.com/aryabarzan/negFIN/.
 * <p>
 * Both the C++/Java code of the negFIN algorithms are respectively based on the C++/Java code of the "FIN algorithm", the algorithm which is presented in:
 * "Z. H. Deng and S. L. Lv. (2014). Fast mining frequent itemsets using Nodesets. Expert System with Applications, 41, 4505–4512"
 *
 * @author Nader Aryabarzan (Copyright 2018)
 * @Email aryabarzan@aut.ac.ir or aryabarzan@gmail.com
 */

public class AlgoNegFIN {

    // the start time and end time of the last algorithm execution
    long startTimestamp;
    long endTimestamp;

    // Tree stuff
    public BMCTreeNode bmcTreeRoot;//The root of BMC_tree
    public SetEnumerationTreeNode nlRoot;//The root of set enumeration tree.


    private int numOfTrans; //// Number of transactions
    public int numOfFItem; // Number of items
    int outputCount = 0;// number of itemsets found
    public int minSupport; // minimum count
    public Item[] item; // list of items sorted by count
    public int[] itemset; // the current itemset
    public int itemsetLen = 0; // the size of the current itemset

    public int[] sameItems;

    public Map<Integer, ArrayList<BMCTreeNode>> mapItemNodeset; //nodessets of 1-itemsets

    BufferedWriter writer = null;// object to write the output file


    /**
     * Comparator to sort items by decreasing order of frequency
     */
    static Comparator<Item> comp = new Comparator<Item>() {
        public int compare(Item a, Item b) {
            return ((Item) b).num - ((Item) a).num;
        }
    };


    /**
     * Read the input file to find the frequent items
     *
     * @param filename   input file name
     * @param minSupport
     * @throws IOException
     */
    void scanDB(String filename, double minSupport) throws IOException {
        numOfTrans = 0;

        // (1) Scan the database and count the count of each item.
        // The count of items is stored in map where
        // key = item value = count count
        Map<Integer, Integer> mapItemCount = new HashMap<Integer, Integer>();
        // scan the database
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        // for each line (transaction) until the end of the file
        while (((line = reader.readLine()) != null)) {
            // if the line is a comment, is empty or is a
            // kind of metadata
            if (line.isEmpty() == true || line.charAt(0) == '#'
                    || line.charAt(0) == '%' || line.charAt(0) == '@') {
                continue;
            }

            numOfTrans++;

            // split the line into items
            String[] lineSplited = line.split(" ");
            // for each item in the transaction
            for (String itemString : lineSplited) {
                // increase the count count of the item by 1
                Integer item = Integer.parseInt(itemString);
                Integer count = mapItemCount.get(item);
                if (count == null) {
                    mapItemCount.put(item, 1);
                } else {
                    mapItemCount.put(item, ++count);
                }
            }

        }
        // close the input file
        reader.close();

        this.minSupport = (int) Math.ceil(minSupport * numOfTrans);

        numOfFItem = mapItemCount.size();

        Item[] tempItems = new Item[numOfFItem];
        int i = 0;
        for (Entry<Integer, Integer> entry : mapItemCount.entrySet()) {
            if (entry.getValue() >= this.minSupport) {
                tempItems[i] = new Item();
                tempItems[i].index = entry.getKey();
                tempItems[i].num = entry.getValue();
                i++;
            }
        }

        item = new Item[i];
        System.arraycopy(tempItems, 0, item, 0, i);

        numOfFItem = item.length;

        Arrays.sort(item, comp);
    }


    /**
     * Build the tree
     *
     * @param filename the input filename
     * @throws IOException if an exception while reading/writting to file
     */
    void construct_BMC_tree(String filename) throws IOException {

        int bmcTreeNodeCount = 0;
        bmcTreeRoot.label = -1;
        bmcTreeRoot.bitmapCode = new MyBitVector(numOfFItem);

        // READ THE FILE
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;

        // we will use a buffer to store each transaction that is read.
        Item[] transaction = new Item[numOfFItem];

        // for each line (transaction) until the end of the file
        while (((line = reader.readLine()) != null)) {
            // if the line is a comment, is empty or is a
            // kind of metadata
            if (line.isEmpty() == true || line.charAt(0) == '#'
                    || line.charAt(0) == '%' || line.charAt(0) == '@') {
                continue;
            }

            // split the line into items
            String[] lineSplited = line.split(" ");

            // for each item in the transaction
            int tLen = 0; // tLen
            for (String itemString : lineSplited) {
                // get the item
                int itemX = Integer.parseInt(itemString);

                // add each item from the transaction except infrequent item
                for (int j = 0; j < numOfFItem; j++) {
                    // if the item appears in the list of frequent items, we add
                    // it
                    if (itemX == item[j].index) {
                        transaction[tLen] = new Item();
                        transaction[tLen].index = itemX; // the item
                        transaction[tLen].num = 0 - j;
                        tLen++;
                        break;
                    }
                }
            }

            // sort the transaction
            Arrays.sort(transaction, 0, tLen, comp);

            int curPos = 0;
            BMCTreeNode curRoot = (bmcTreeRoot);
            BMCTreeNode rightSibling = null;
            while (curPos != tLen) {
                BMCTreeNode child = curRoot.firstChild;
                while (child != null) {
                    if (child.label == 0 - transaction[curPos].num) {
                        curPos++;
                        child.count++;
                        curRoot = child;
                        break;
                    }
                    if (child.rightSibling == null) {
                        rightSibling = child;
                        child = null;
                        break;
                    }
                    child = child.rightSibling;
                }
                if (child == null)
                    break;
            }
            for (int j = curPos; j < tLen; j++) {
                BMCTreeNode bmcTreeNode = new BMCTreeNode();
                bmcTreeNode.label = 0 - transaction[j].num;
                if (rightSibling != null) {
                    rightSibling.rightSibling = bmcTreeNode;
                    rightSibling = null;
                } else {
                    curRoot.firstChild = bmcTreeNode;
                }
                bmcTreeNode.rightSibling = null;
                bmcTreeNode.firstChild = null;
                bmcTreeNode.father = curRoot;
                bmcTreeNode.count = 1;
                curRoot = bmcTreeNode;
                bmcTreeNodeCount++;
            }
        }
        // close the input file
        reader.close();


        BMCTreeNode root = bmcTreeRoot.firstChild;
        mapItemNodeset = new HashMap<>();
        while (root != null) {
            root.bitmapCode = (MyBitVector) root.father.bitmapCode.clone();
            root.bitmapCode.set(root.label);//bitIndex=numOfFItem - 1 - root.label
            ArrayList<BMCTreeNode> nodeset = mapItemNodeset.get(root.label);
            if (nodeset == null) {
                nodeset = new ArrayList<>();
                mapItemNodeset.put(root.label, nodeset);
            }
            nodeset.add(root);

            if (root.firstChild != null) {
                root = root.firstChild;
            } else {
                if (root.rightSibling != null) {
                    root = root.rightSibling;
                } else {
                    root = root.father;
                    while (root != null) {
                        if (root.rightSibling != null) {
                            root = root.rightSibling;
                            break;
                        }
                        root = root.father;
                    }
                }
            }
        }

    }

    /**
     * Initialize the tree
     */
    void initializeSetEnumerationTree() {

        SetEnumerationTreeNode lastChild = null;
        for (int t = numOfFItem - 1; t >= 0; t--) {
            SetEnumerationTreeNode nlNode = new SetEnumerationTreeNode();
            nlNode.label = t;
            nlNode.count = 0;
            nlNode.nodeset = mapItemNodeset.get(t);
            nlNode.firstChild = null;
            nlNode.next = null;
            nlNode.count = item[t].num;
            if (nlRoot.firstChild == null) {
                nlRoot.firstChild = nlNode;
                lastChild = nlNode;
            } else {
                lastChild.next = nlNode;
                lastChild = nlNode;
            }
        }
    }


    /**
     * Recursively constructing_frequent_itemset_tree the tree to find frequent itemsets
     *
     * @param curNode
     * @param level
     * @param sameCount
     * @throws IOException if error while writing itemsets to file
     */

    public void constructing_frequent_itemset_tree(SetEnumerationTreeNode curNode, int level, int sameCount) throws IOException {

        MemoryLogger.getInstance().checkMemory();

        SetEnumerationTreeNode sibling = curNode.next;
        SetEnumerationTreeNode lastChild = null;
        while (sibling != null) {
            SetEnumerationTreeNode child = new SetEnumerationTreeNode();

            child.nodeset = new ArrayList<>();
            int countNegNodeset = 0;
            if (level == 1) {
                for (int i = 0; i < curNode.nodeset.size(); i++) {
                    BMCTreeNode ni = curNode.nodeset.get(i);
                    if (!ni.bitmapCode.isSet(sibling.label)) {
                        child.nodeset.add(ni);
                        countNegNodeset += ni.count;
                    }
                }
            } else {
                for (int j = 0; j < sibling.nodeset.size(); j++) {
                    BMCTreeNode nj = sibling.nodeset.get(j);
                    if (nj.bitmapCode.isSet(curNode.label)) {
                        child.nodeset.add(nj);
                        countNegNodeset += nj.count;
                    }
                }
            }
            child.count = curNode.count - countNegNodeset;

            if (child.count >= minSupport) {
                if (curNode.count == child.count) {
                    sameItems[sameCount++] = sibling.label;
                } else {
                    child.label = sibling.label;
                    child.firstChild = null;
                    child.next = null;
                    if (curNode.firstChild == null) {
                        curNode.firstChild = lastChild = child;
                    } else {
                        lastChild.next = child;
                        lastChild = child;
                    }
                }
            } else {
                child.nodeset = null;
            }

            sibling = sibling.next;
        }
//        resultCount += Math.pow(2.0, sameCount);
//        nlLenSum += Math.pow(2.0, sameCount) * curNode.nodeset.size();

        itemset[itemsetLen++] = curNode.label;

        // ============= Write itemset(s) to file ===========
        writeItemsetsToFile(curNode, sameCount);

        // ======== end of write to file

        SetEnumerationTreeNode child = curNode.firstChild;
        curNode.firstChild = null;

        SetEnumerationTreeNode next = null;
        while (child != null) {
            next = child.next;
            constructing_frequent_itemset_tree(child, level + 1, sameCount);
            child.next = null;
            child = next;
        }
        itemsetLen--;
    }

    /**
     * This method write an itemset to file + all itemsets that can be made
     * using its node list.
     *
     * @param curNode   the current node
     * @param sameCount the same count
     * @throws IOException exception if error reading/writting to file
     */
    private void writeItemsetsToFile(SetEnumerationTreeNode curNode, int sameCount)
            throws IOException {

        // create a stringuffer
        StringBuilder buffer = new StringBuilder();

        outputCount++;
        // append items from the itemset to the StringBuilder
        for (int i = 0; i < itemsetLen; i++) {
            buffer.append(item[itemset[i]].index);
            buffer.append(' ');
        }
        // append the count of the itemset
        buffer.append("#SUP: ");
        buffer.append(curNode.count);
        buffer.append("\n");


        // === Write all combination that can be made using the node list of
        // this itemset
        if (sameCount > 0) {
            // generate all subsets of the node list except the empty set
            for (long i = 1, max = 1 << sameCount; i < max; i++) {
                for (int k = 0; k < itemsetLen; k++) {
                    buffer.append(item[itemset[k]].index);
                    buffer.append(' ');
                }

                // we create a new subset
                for (int j = 0; j < sameCount; j++) {
                    // check if the j bit is set to 1
                    int isSet = (int) i & (1 << j);
                    if (isSet > 0) {
                        // if yes, add it to the set
                        buffer.append(item[sameItems[j]].index);
                        buffer.append(' ');
                        // newSet.add(item[sameItems[j]].index);
                    }
                }
                buffer.append("#SUP: ");
                buffer.append(curNode.count);
                buffer.append("\n");
                outputCount++;
            }
        }
        // write the strinbuffer to file and create a new line
        // so that we are ready for writing the next itemset.
        writer.write(buffer.toString());
    }


    /**
     * Print statistics about the latest execution of the algorithm to
     * System.out.
     */
    public void printStats() {
        System.out.println("========== negFIN - STATS ============");
        System.out.println(" Minsup = " + minSupport
                + "\n Number of transactions: " + numOfTrans);
        System.out.println(" Number of frequent  itemsets: " + outputCount);
        System.out.println(" Total time ~: " + (endTimestamp - startTimestamp)
                + " ms");
        System.out.println(" Max memory:"
                + MemoryLogger.getInstance().getMaxMemory() + " MB");
        System.out.println("=====================================");
    }

    /**
     * Run the algorithm
     *
     * @param filename the input file path
     * @param minsup   the minsup threshold
     * @param output   the output file path
     * @throws IOException if error while reading/writting to file
     */
    public void runAlgorithm(String filename, double minsup, String output)
            throws IOException {

        bmcTreeRoot = new BMCTreeNode();
        nlRoot = new SetEnumerationTreeNode();

        MemoryLogger.getInstance().reset();

        // create object for writing the output file
        writer = new BufferedWriter(new FileWriter(output));

        // record the start time
        startTimestamp = System.currentTimeMillis();

        // ==========================
        // Read Dataset
        scanDB(filename, minsup);

        itemsetLen = 0;
        itemset = new int[numOfFItem];

        // Build BMC-tree
        construct_BMC_tree(filename);//Lines 2 to 6 of algorithm 3 in the paper

        nlRoot.label = numOfFItem;
        nlRoot.firstChild = null;
        nlRoot.next = null;

        //Lines 12 to 19 of algorithm 3 in the paper
        // Initialize tree
        initializeSetEnumerationTree();
        sameItems = new int[numOfFItem];

        // Recursively constructing_frequent_itemset_tree the tree
        SetEnumerationTreeNode curNode = nlRoot.firstChild;
        nlRoot.firstChild = null;
        SetEnumerationTreeNode next = null;
        while (curNode != null) {
            next = curNode.next;
            // call the recursive "constructing_frequent_itemset_tree" method
            constructing_frequent_itemset_tree(curNode, 1, 0);
            curNode.next = null;
            curNode = next;
        }
        writer.close();

        MemoryLogger.getInstance().checkMemory();

        // record the end time
        endTimestamp = System.currentTimeMillis();
    }

    class Item {
        public int index;
        public int num;
    }

    class SetEnumerationTreeNode {
        public int label;
        public SetEnumerationTreeNode firstChild;
        public SetEnumerationTreeNode next;
        public int count;
        List<BMCTreeNode> nodeset;
    }

    class BMCTreeNode {
        public int label;
        public BMCTreeNode firstChild;
        public BMCTreeNode rightSibling;
        public BMCTreeNode father;
        public int count;
        MyBitVector bitmapCode;
    }
}


//This class is more efficient than the built in class BitSet
class MyBitVector {
    static long[] TWO_POWER;

    static {
        TWO_POWER = new long[64];
        for (int i = 0; i < TWO_POWER.length; i++) {
            TWO_POWER[i] = (long) Math.pow(2, i);
        }
    }

    long[] bits;
    private int cardinality;

    public MyBitVector(int numOfBits) {
        bits = new long[((numOfBits - 1) / 64) + 1];
        cardinality = 0;
    }

    public MyBitVector(int[] itemset, int last) {
        int length = itemset[0];
        bits = new long[(length / 64) + 1];
        cardinality = last;
        int item;
        for (int i = 0; i < last; i++) {
            item = itemset[i];
            bits[item / 64] |= MyBitVector.TWO_POWER[item % 64];
        }
    }

    public Object clone() {
        MyBitVector result = new MyBitVector(this.bits.length * 64);
        result.cardinality=this.cardinality;
        System.arraycopy(this.bits,0,result.bits,0,result.bits.length);
        return result;
    }

    public void set(int bitIndex) {
        bits[bitIndex / 64] |= MyBitVector.TWO_POWER[bitIndex % 64];
    }

    public boolean isSet(int bitIndex) {
        return (bits[bitIndex / 64] & MyBitVector.TWO_POWER[bitIndex % 64]) != 0;
    }

    public boolean isSubSet(MyBitVector q) {
        if (cardinality >= q.cardinality) {
            return false;
        }
        for (int i = 0; i < bits.length; i++) {
            if ((bits[i] & (~q.bits[i])) != 0) {
                return false;
            }
        }
        return true;
    }
}

