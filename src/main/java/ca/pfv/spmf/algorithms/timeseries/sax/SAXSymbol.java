package ca.pfv.spmf.algorithms.timeseries.sax;
/* This file is copyright (c) 2008-2016 Philippe Fournier-Viger
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
 * This class represent a symbol used in a SAX time-series
 * 
 * @author Philippe Fournier-Viger, 2016 */
public class SAXSymbol {
	
	/** The symbol (as a short number) **/
	int symbol;
	
	/** The lower bound of the interval for this symbol */
	double lowerBound;
	
	/** The upper bound of the interval for this symbol */
	double upperBound;

	/**
	 * Constructor
	 */
	public SAXSymbol(int symbol, double lowerBound, double upperBound){
		this.symbol = symbol;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}
		
	/**
	 * Obtain a string representation of this symbol
	 * @return a String representation
	 */
	public String toString() {
		return "(" + symbol + " [" + lowerBound + "," + upperBound + "])";
	}
}
