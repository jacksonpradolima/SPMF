package ca.pfv.spmf.algorithms.frequentpatterns.opusminer;

import java.util.ArrayList;

public class FisherTest {

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
	//=========================================
	// This is the header of the original C++ code:
	//  
	// return the p value for a one tailed fisher exact test for the probability
	// of obtaining d or more in a contingency table where the marginal
	// frequencies are invariant
	/*
	 * a module of OPUS Miner providing fisherTest, a function to
	 * calculate the Fisher exact test and log_combin a function to calculate
	 * the log of the number of combinations of k items selected from n.
	 ** Copyright (C) 2012 Geoffrey I Webb
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

	public static double fisherTest(int a, int b, int c, int d) {
		double p = 0; // cumulative value of p

		// will loop until b or c is 0 - as the values are interchangeable, make
		// c the lesser value and test only for when it reaches 0
		if (b < c) {
			final int t = b;
			b = c;
			c = t;
		}

		final double invariant = -logfact(a + b + c + d) + logfact(a + b) + logfact(c + d) + logfact(a + c)
				+ logfact(b + d);

		do {
			p += Math.exp(invariant - logfact(a) - logfact(b) - logfact(c) - logfact(d));
			a++;
			b--;
			c--;
			d++;
		} while (c >= 0);

		return p;
	}
	
	public static double log_combin(int n, int k) {
		return logfact(n) - logfact(k) - logfact(n - k);
	}

	private static ArrayList<Double> logfact_lf = new ArrayList<Double>();

	// return the log of the factorial of n
	public static double logfact(int n) {
		// C++ TO JAVA CONVERTER NOTE: This static local variable declaration
		// (not allowed in Java) has been moved just prior to the method:
		// static ArrayList<double> lf;

		int i;

		for (i = logfact_lf.size(); i <= n; i++) {
			if (i == 0) {
				logfact_lf.add(0d);
			} else {
				logfact_lf.add(logfact_lf.get(i - 1) + Math.log((double) i));
			}
		}

		return logfact_lf.get(n);
	}



}