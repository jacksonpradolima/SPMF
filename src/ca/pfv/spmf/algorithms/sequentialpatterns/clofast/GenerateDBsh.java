package ca.pfv.spmf.algorithms.sequentialpatterns.clofast;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
/* This file is copyright (c) Fabiana Lanotte et al.
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
 * Create a file IBMgenDatasets.sh for the generation of datasets using the IBM data generator
 * 
 * @author fabiana
 * 
 */
public class GenerateDBsh {

	// containsItemset the possible values for the parameter -ncust, that is
	// number_of_customers_in_000s (default: 100)
	double[] d;
	// containsItemset the possible values for the parameter -slen
	// avg_trans_per_customer (default: 10)
	double[] c;
	// containsItemset the possible values for the parameter -tlen
	// avg_items_per_transaction (default: 2.5)
	double[] t;
	// containsItemset the possible values for the parameter -nitems
	// number_of_different_items_in_000s (default: 10)
	double[] n;

	/**
	 * 
	 * @param d containsItemset the possible values for the parameter -ncust, that is number_of_customers_in_000s (default: 100)
	 * @param c containsItemset the possible values for the parameter -slen avg_trans_per_customer (default: 10
	 * @param t containsItemset the possible values for the parameter -tlen avg_items_per_transaction (default: 2.5)
	 * @param n containsItemset the possible values for the parameter -nitems number_of_different_items_in_000s (default: 10)
	 * @throws IOException
	 */
	public GenerateDBsh(double[] d, double[] c, double[] t, double[] n) throws IOException {
		this.d = d;
		this.t = t;
		this.c = c;
		this.n = n;
		BufferedWriter out = new BufferedWriter(new FileWriter("IBMgenDatasets.sh"));
		int s=6;
		int i=4;
		for(int di=0; di<d.length; di++){
			for(int ti=0; ti<t.length; ti++){
				for(int ci=0; ci<c.length; ci++){
					for(int ni=0; ni<n.length; ni++){
						String datasetName="D"+d[di]+"C"+c[ci]+"T"+t[ti]+"N"+n[ni]+"S"+s+"I"+i;
						out.write("~/data_generator/bin/seq_data_generator seq -ncust "+d[di]+" -slen "+c[ci]+" -tlen "+t[ti]+" -nitems "+n[ni]+" -seq.npats 2000 -lit.npats 5000 -seq.patlen "+s+" -lit.patlen "+i+" -fname "+datasetName+"\n");
					}
				}
			}
		}
		out.close();
		System.out.println("Generation sh end");
	}
	public static void main(String[] args) {
		double[] d={20};
		double[] t={20};
		double[] c={30};
		double[] n={0.1,0.5, 2.5};
		try {
			GenerateDBsh generator= new GenerateDBsh(d, c, t, n);
		} catch (IOException e) {

			e.printStackTrace();
		}
		
	}
}
