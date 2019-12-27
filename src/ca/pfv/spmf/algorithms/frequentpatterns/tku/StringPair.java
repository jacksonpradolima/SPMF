package ca.pfv.spmf.algorithms.frequentpatterns.tku;

/* This file is copyright (c) Wu et al.
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
 * This class is used by the TKU algorithm.
 * 
 * The code of TKU was obtained from the UP-Miner project, 
 * which is distributed under the GPL license and slightly modified
 * by Philippe Fournier-Viger to integrate TKU into SPMF.
 * @see AlgoTKU
 */

class StringPair implements Comparable<StringPair>{

	public String x;
	public int y;
    public StringPair(String x, int y) {
    	this.x = x;
    	this.y = y;
    }
    
    public int compareTo(StringPair o){
    	return y - o.y;
    }
}