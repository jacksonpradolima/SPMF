package ca.pfv.spmf.algorithms.sequentialpatterns.clofast;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.stream.Collectors;

import ca.pfv.spmf.algorithms.sequentialpatterns.clofast.model.SparseIdList;


/* This file is copyright (c) Fabiana Lanotte, Fabio Fumarola, M. Ceci,  D. Malerba et al.
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
 * This is the representation of a dataset for the Fast and CloFast algorithms
 * 
 * @author Fabiana Lanotte, Fabio Fumarola, M. Ceci, D. Malerba et al.
 * @see AlgoFast 
 * @see AlgoCloFast
 */
public class FastDataset {

	/** the itemset separator in a file in SPMF format */
    public static final String ITEMSET_SEPARATOR = "-1";
    
    /** the sequence separator in a file in SPMF format */
    public static final String SEQUENCE_SEPARATOR = "-2";

    /**
     * Matp to associates to each frequent itemset its SparseIdList
     */
    private Map<String, SparseIdList> itemSILMap;
    
    /** Number of rows (sequences) */
    private final long numRows;
    
    /** Minimum support */
    private final float minSup;
    
    /** Maximum support */
    private final float maxSup;
    
    /** Absolute minimum support (optional parameter)*/
    private int absMinSup;
    
    /** Absolute maximum support */
    private int absMaxSup;

	/**
     * @param numRows
     * @param minSup
     */
    private FastDataset(long numRows, float minSup) {
        this.itemSILMap = new HashMap<>();
        this.numRows = numRows;
        this.minSup = minSup;
		this.maxSup = 1f;
        absMinSup = absoluteSupport(minSup, numRows);
        if (absMinSup == 0)
            absMinSup = 1;
		absMaxSup = (int) numRows;
    }
	
    /**
     * @param numRows
     * @param minSup
	 * @param maxSup
     */
    private FastDataset(long numRows, float minSup, float maxSup) {
        this.itemSILMap = new HashMap<>();
        this.numRows = numRows;
        this.minSup = minSup;
		this.maxSup = maxSup;
        absMinSup = absoluteSupport(minSup, numRows);
        if (absMinSup == 0)
            absMinSup = 1;
		absMaxSup = absoluteSupport(maxSup, numRows);
        if (absMaxSup == 0)
            absMaxSup = 1;
    }

    /**
     * Finds all frequent 1 items
     */
    private void computeFrequentItems() {
        final Map<String, SparseIdList> newMap = new TreeMap<>();
        itemSILMap.forEach((item, sparseIdList) -> {
            if (sparseIdList.getAbsoluteSupport() >= absMinSup && sparseIdList.getAbsoluteSupport() <= absMaxSup)
                newMap.put(item, sparseIdList);
        });
        itemSILMap = newMap;
    }

    /**
     * Get the frequent itemsets
     * @return a map of frequent itemsets
     */
    public Map<String, SparseIdList> getFrequentItemsets() {
        return itemSILMap;
    }

    /**
     * Get the SparseIdList for a particular item
     *
     * @param item
     * @return a SparseIdList, return null if that SparseIdList doesn't exist in
     * dataset
     */
    public SparseIdList getSparseIdList(String item) {
        return itemSILMap.get(item);
    }


    /**
     * Get the number of rows in the database
     * @return the number of rows 
     */
    public long getNumRows() {
        return numRows;
    }

    /**
     * Get the absolute minimum support
     * @return the absolute minimum support
     */
    public int getAbsMinSup() {
        return absMinSup;
    }
    
	/**
     * Get the absolute maximum support
     * @return the absolute maximum support
     */
    public int getAbsMaxSup() {
        return absMaxSup;
    }
	
    /**
     * Read an input file in SPMF format.
     * @param path the path of the file
     * @param relativeMinSupport the relative minimum support threshold
     * @param relativeMaxSupport the relative maximum support threshold
     * @return a memory representation of the databaset
     */
    public static FastDataset fromPrefixspanSource(String path, float relativeMinSupport, float relativeMaxSupport) throws IOException { 	
    	long numRows =0;
    	//========================== CODE CHANGED BY PHILIPPE =====================
    	// count the number of lines in the file
	    LineNumberReader lnr = new LineNumberReader(new FileReader(new File(path)));

        String line;
	    
		while ((line = lnr.readLine()) != null) {
			// if the line is a comment, is empty or is a
			// kind of metadata
			if (line.isEmpty() == true || line.startsWith("#")
					|| line.charAt(0) == '%' || line.charAt(0) == '@') {
				continue;
			}
			numRows++;
		}
		lnr.close();

        final FastDataset fastDataset = new FastDataset(numRows, relativeMinSupport, relativeMaxSupport);

        int lineNumber = 0;
        FileInputStream fin = new FileInputStream(new File(path));
		BufferedReader in = new BufferedReader(new InputStreamReader(fin));
    	//=============================================================
        while ((line = in.readLine()) != null) {
        	
			// if the line is  a comment, is  empty or is a
			// kind of metadata
			if (line.isEmpty() == true ||  line.startsWith("#") || line.charAt(0) == '%' 	|| line.charAt(0) == '@') {
				continue;
			}

            if (line.length() == 0)
                continue;

            int transID = 1;

            StringTokenizer tokenizer = new StringTokenizer(line);
            String token;
            while (tokenizer.hasMoreElements()) {
                token = tokenizer.nextToken();

                if (token.equals(ITEMSET_SEPARATOR)) {
                    transID++;
                    continue;
                }

                if (token.equals(SEQUENCE_SEPARATOR))
                    break;

                SparseIdList inserted = fastDataset.itemSILMap.putIfAbsent(token, new SparseIdList((int) numRows));
                fastDataset.itemSILMap.get(token).addElement(lineNumber, transID);
            }
            lineNumber++;
        }
        in.close();
        fastDataset.computeFrequentItems();
        return fastDataset;
    }

    /**
     * @param path
     * @param relativeMinSupport
     * @param relativeMaxSupport
     * @return
     */
    public static FastDataset fromPrefixspanSource(Path path, float relativeMinSupport, float relativeMaxSupport) throws IOException {
        long numRows = Files.lines(path).count();
        final FastDataset fastDataset = new FastDataset(numRows, relativeMinSupport, relativeMaxSupport);

        int lineNumber = 0;
        String line;
        BufferedReader in = Files.newBufferedReader(path);
        while ((line = in.readLine()) != null) {

            if (line.length() == 0)
                continue;

            int transID = 1;

            StringTokenizer tokenizer = new StringTokenizer(line);
            String token;
            while (tokenizer.hasMoreElements()) {
                token = tokenizer.nextToken();

                if (token.equals(ITEMSET_SEPARATOR)) {
                    transID++;
                    continue;
                }

                if (token.equals(SEQUENCE_SEPARATOR))
                    break;

                SparseIdList inserted = fastDataset.itemSILMap.putIfAbsent(token, new SparseIdList((int) numRows));
                fastDataset.itemSILMap.get(token).addElement(lineNumber, transID);
            }
            lineNumber++;
        }
        fastDataset.computeFrequentItems();
        return fastDataset;
    }

    /**
     * @param path
     * @return
     * @throws IOException
     */
    private static long countNumRowsSpamSource(Path path) throws IOException {
        Set<String> custIds = Files.lines(path).
                filter(l -> l.length() > 0).
                map(l -> l.split(" ")[0]).collect(Collectors.toSet());

        return custIds.size();

    }

    /**
     *
     * @param path
     * @param relativeMinSupport
     * @param relativeMaxSupport
     * @return
     * @throws IOException
     */
    public static FastDataset fromSpamSource(Path path, float relativeMinSupport, float relativeMaxSupport) throws IOException {

        long numRows = countNumRowsSpamSource(path);
        final FastDataset fastDataset = new FastDataset(numRows, relativeMinSupport, relativeMaxSupport);

        Files.lines(path).filter(l -> l.length() > 0).forEach(l -> {
            String[] split = l.split(" ");
            int custId = Integer.parseInt(split[0]);
            int transId = Integer.parseInt(split[1]);

            SparseIdList inserted = fastDataset.itemSILMap.putIfAbsent(split[2], new SparseIdList((int) numRows));
            inserted.addElement(custId,transId);
        });
        fastDataset.computeFrequentItems();
        return fastDataset;
    }
    
    /**
    *
    * @param relativeSupport
    * @param totalCount
    * @return the absolute support for the given relative support
    */
   int absoluteSupport(float relativeSupport, long totalCount){
       return (int) Math.ceil((relativeSupport * totalCount));
   }
}
