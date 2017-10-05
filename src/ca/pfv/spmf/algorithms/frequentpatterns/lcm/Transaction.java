package ca.pfv.spmf.algorithms.frequentpatterns.lcm;

import java.util.List;


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
 * This class has an abstraction of a dataset transaction.
 *
 * @author Alan Souza <apsouza@inf.ufrgs.br>
 */
public class Transaction {
	public static Integer[] temp = new Integer[500];

	Transaction originalTransaction;
	int offset;
	
    private Integer[] items;

    public Transaction(Integer[] items) {
    	originalTransaction = this;
    	this.items = items;
    	this.offset = 0;
    }
    
    public Transaction(Transaction transaction, int offset) {
    	this.originalTransaction = transaction.originalTransaction; 	
    	
    	this.items = transaction.getItems();
    	this.offset = offset;
    }

    public Integer[] getItems() {
        return items;
    }

    /**
     * Check if an item appears in this transaction
     * @param item  the item
     * @return true if it appears. Otherwise, false.
     */
	public int containsByBinarySearch(Integer item) {
//		if(item > items[items.length -1]) {
//			return -1;
//		}
		int low = offset;
		int high = items.length - 1;

		while (high >= low) {
			int middle = ( low + high ) >>> 1; // divide by 2
			if (items[middle].equals(item)) {
				return middle;
			}
			if (items[middle]< item) {
				low = middle + 1;
			}
			if (items[middle] > item) {
				high = middle - 1;
			}
		}
		return -1;
	}
	
	public boolean containsByBinarySearchOriginalTransaction(Integer item) {
		Integer[] originalItems = originalTransaction.getItems();
		int low = 0;
		int high = originalItems.length - 1;

		while (high >= low) {
			int middle = ( low + high ) >>> 1; // divide by 2
			if (originalItems[middle].equals(item)) {
				return true;
			}
			if (originalItems[middle]< item) {
				low = middle + 1;
			}
			if (originalItems[middle] > item) {
				high = middle - 1;
			}
		}
		return false;
	}

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();

        for(Integer item : items) {
        	string.append(item.intValue());
        	string.append(" ");
        }
        return string.toString();
    }

	public void removeInfrequentItems(List<Transaction>[] buckets, int minsupRelative) {
    	
    	// copy only the frequent itemsets after the offset in the temporary buffer
    	int i = 0;
    	for(Integer item : items) {
    		if(buckets[item].size() >= minsupRelative) {
    			temp[i++] = item;
    		}
    	}
    	// copy the buffer back into the original array
    	this.items = new Integer[i];
    	System.arraycopy(temp, 0, this.items, 0, i);
	}


}
