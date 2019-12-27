package ca.pfv.spmf.algorithms.frequentpatterns.opusminer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * The FisherTest class from the Opus-Miner algorithm proposed in : </br></br>
 * 
 * Webb, G.I. & Vreeken, J. (2014) Efficient Discovery of the Most Interesting Associations.
  ACM Transactions on Knowledge Discovery from Data. 8(3), Art. no. 15.
 *
 *  The code was translated from C++ to Java.  The modifications to the original C++ code to obtain Java
 *  code and improvements are copyright by Xiang Li and Philippe Fournier-Viger, 
 *  while the original C++ code is copyright by Geoff Web.
 *  
 *  The code is under the GPL license.
 */

public class itemQClass extends ArrayList<ItemQElement> implements
		java.io.Closeable {
	public itemQClass() {
	}

	public final void close() {
	}

	public final void insert(float ubVal, Integer item) {
		int initialSize = size();

		if (initialSize == 0) {
			ItemQElement element = new ItemQElement();
			element.ubVal = ubVal;
			element.item = item;
			this.add(element);
		} else {

			
			int first = 0;
			int last = initialSize - 1;

			// binary search
			while (first < last) {
				final int mid = first + (last - first) / 2;
				if (ubVal <= this.get(mid).ubVal) {
					first = mid + 1;
				} else {
					last = mid;
				}
			}

			if (this.get(first).ubVal >= ubVal) {
				// this should only happen if all items in the queue have lower
				// value than the new item
				first++;
			}
			
			// RESIZE  BY CREATING A NEW ELEMENT WHICH WILL BE SET WITH VALUES
			// BELOW
			ItemQElement element = new ItemQElement();
			element.ubVal = -9999;
			element.item = -9999;
			this.add(element);
			/////////////////

			for (last = initialSize; last > first; last--) {
				this.get(last).item = this.get(last -1).item;
				this.get(last).ubVal = this.get(last -1).ubVal;
			}

			this.get(first).ubVal = ubVal;
			this.get(first).item = item;
		}
	}

	public final void append(float ubVal, Integer item) {
		
		ItemQElement element = new ItemQElement();
		element.ubVal = ubVal;
		element.item = item;
		this.add(element);
	}

	public final void sort() {
		Collections.sort(this, new Comparator<ItemQElement>() {

			@Override
			public int compare(ItemQElement iqe1, ItemQElement iqe2) {
				float val = (iqe2.ubVal - iqe1.ubVal);   /// IS IT CORRECT?
				if(val > 0){
					return 1;
				}
				if(val < 0){
					return -1;
				}
				return 0;            // CHECK IF OK...
			}
		});
		// std::sort(begin(), end(), GlobalMembers.iqeGreater);

		// public static boolean iqeGreater(ItemQElement iqe1, ItemQElement
		// iqe2) {
		// return ;
		// }

	}

}