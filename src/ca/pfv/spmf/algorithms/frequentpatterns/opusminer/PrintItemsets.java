package ca.pfv.spmf.algorithms.frequentpatterns.opusminer;


import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/**
 * This is the PrintItemsets class used by the Opus-Miner algorithm proposed in : </br></br>
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
/*
 * print_itemsets.cpp - a module of OPUS Miner providing print_itemsets, a
 * procedure to print the top-k itemsets to a file. Copyright (C) 2012
 * Geoffrey I Webb
 **
 ** This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 ** 
 ** This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 ** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 ** 
 ** You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */
public class PrintItemsets {
	


	public static boolean valgt(itemsetRec i1, itemsetRec i2) {
		return i1.value > i2.value;
	}

	public static void print_itemset(BufferedWriter writer, itemset is, boolean isCSVInputFile) throws IOException
	{
		Iterator<Integer> item_it = is.iterator();

		while (item_it.hasNext()) {
			Integer integer = (Integer) item_it.next();
			
			if(isCSVInputFile){
				String name = Global.itemNames.get(integer);
				writer.write(name);
			}else{
				writer.write(integer.toString());
			}
			
			if (item_it.hasNext()) {
				writer.write(' ');
			}
		}

	}

	public static void print_itemsetRec(BufferedWriter f, itemsetRec is, boolean isCSVInputFile, boolean searchByLift) throws IOException {
		print_itemset(f, is, isCSVInputFile);

		String measure = searchByLift ? " #LIFT: " : " #LEVERAGE: ";
		f.write(" #SUP: " + is.count + measure +  is.value);
		f.write(" #PVALUE: " + is.p);

		if (Global.printClosures) {
			itemset closure = new itemset();

			FindClosure.find_closure(is, closure);

			if (closure.size() > is.size()) {
				f.write(" #CLOSURE: ");
				print_itemset(f, closure, isCSVInputFile);
			}
		}

		f.newLine();
	}

	public static void print_itemsets(BufferedWriter f, ArrayList<itemsetRec> is, boolean isCSVInputFile, boolean searchByLift) throws IOException {
		int i;



//		f.write("\nSELF-SUFFICIENT ITEMSETS:\n");


		// produces a boolean value, while the Java Comparator parameter
		// produces a tri-state result:
		// ORIGINAL LINE: std::sort(is.begin(), is.end(), valgt);
		Collections.sort(is, new Comparator<itemsetRec>() {

			@Override
			public int compare(itemsetRec i1, itemsetRec i2) {
				float val = (i2.value - i1.value);
				if(val > 0){
					return 1;
				}
				if(val < 0){
					return -1;
				}
				return 0;            // CHECK IF OK...
			}
		});

		Iterator<itemsetRec> it = is.iterator();

		int failed_count = 0;
		
		while (it.hasNext()) {
			itemsetRec itemsetRec = (itemsetRec) it.next();
			if (!itemsetRec.self_sufficient) {
				failed_count++;
			} else {
				print_itemsetRec(f,itemsetRec,isCSVInputFile, searchByLift);
			}
		}


		if (failed_count != 0) {
			f.write("\n" + failed_count + " itemsets failed test for self sufficiency\n");
			it = is.iterator();
			while (it.hasNext()) {
				itemsetRec itemsetRec = (itemsetRec) it.next();
				if (!itemsetRec.self_sufficient) {
					print_itemsetRec(f, itemsetRec,isCSVInputFile, searchByLift);
				}
			}
		}
	}
}