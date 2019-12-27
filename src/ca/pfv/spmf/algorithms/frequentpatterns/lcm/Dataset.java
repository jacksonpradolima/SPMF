package ca.pfv.spmf.algorithms.frequentpatterns.lcm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
/* This file is copyright (c) 2012-2014 Alan Souza
* 
* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
* 
* SPMF is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/
/**
 * This is the parser class for the dataset.
 * It has actions related to parse a txt based file to a Dataset class.
 *
 * @see AlgoLCM
 * @author Alan Souza <apsouza@inf.ufrgs.br>
 */
public class Dataset {

    private List<Transaction> transactions;
    private Integer[] transactionsItems;
    
    Set<Integer> uniqueItems = new HashSet<Integer>();

	private int maxItem = 0;

    public Dataset(String datasetPath) throws IOException {

        transactions = new ArrayList<Transaction>();
        
        BufferedReader br = new BufferedReader(new FileReader(datasetPath));
        String items;
        while((items = br.readLine()) != null) { // iterate over the lines to build the transaction
			// if the line is  a comment, is  empty or is  metadata
			if (items.isEmpty() == true || items.charAt(0) == '#' 
					|| items.charAt(0) == '%' || items.charAt(0) == '@') {
				continue;
			}
        	
            getTransactions().add(createTransaction(items));
        }
        br.close();
        
        /// sort transactions by increasing last item (optimization)
        Collections.sort(transactions, new Comparator<Transaction>() {
			public int compare(Transaction arg0, Transaction arg1) {
//				return arg0.getItems().length - arg1.getItems().length;
				return arg0.getItems()[arg0.getItems().length -1] - 
						arg1.getItems()[arg1.getItems().length -1]; 
			}});
        
        // create the list of items in the database and sort it
        transactionsItems = new Integer[uniqueItems.size()];
        int i=0;
        for(Integer item : uniqueItems) {
        	transactionsItems[i++] = item;
        }
        Arrays.sort(transactionsItems);
        
    }

    /**
     * Create a transaction object from a line from the input file
     * @param line a line from input file
     * @return a transaction
     */
    private Transaction createTransaction(String line) {

        //build the items
        Pattern splitPattern = Pattern.compile(" ");
        String[] items = splitPattern.split(line);

        Integer[] itemsSorted = new  Integer[items.length];

        for (int i = 0; i < items.length; i++) {
        	Integer item = Integer.valueOf(items[i]);
        	itemsSorted[i] = item;

            uniqueItems.add(item);
        }

        // update max item by checking the last item of the transaction
        int lastItem = itemsSorted[itemsSorted.length - 1];
        if(lastItem > maxItem) {
            maxItem = lastItem;
        }
        return new Transaction(itemsSorted);
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public Set<Integer> getUniqueItems() {
		return uniqueItems;
	}


//    public Integer[] getAllItems() {
//
//        return transactionsItems;
//    }

    public int getMaxItem() {
        return maxItem;
    }

    @Override
    public String toString() {
        StringBuilder datasetContent = new StringBuilder();

        for(Transaction transaction : transactions) {
            datasetContent.append(transaction);
            datasetContent.append("\n");
        }
        return datasetContent.toString();
    }

}
