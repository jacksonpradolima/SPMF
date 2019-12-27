package ca.pfv.spmf.algorithms.frequentpatterns.ihaupm;

/* This file is copyright (c) 2008-2015 Philippe Fournier-Viger
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
*
*/

import java.util.ArrayList;

/**
 * This is an implementation of the "IHAUPM" algorithm for High-Average-Utility Itemsets Mining
 * as described in the conference paper : <br/><br/>
 *
 * Jerry Chun-Wei Lin, Shifeng Ren, and Philippe Fournier-Viger. Efficiently Updating the Discovered High Average-Utility Itemsets with Transaction Insertion. EAAI (unpublished, minor revision)
 *
 * @see algorithm.IHAUPM
 * @see tree.IAUTree
 * @see tree.IAUNode
 * @see tree.TableNode
 * @see util.Item
 * @see util.StackElement
 * @author Shi-Feng Ren
 */
public class Itemset extends ArrayList<Item>{

	private static final long serialVersionUID = 4907417249949024565L;
	
	public int maxItemUtility=-1;

	public Itemset(int capacity){
		super(capacity);
	}
	public Itemset(){
	}

	@Override
	public String toString(){
		String tmp="[";
		int i=0;
		for (i=0; i<this.size()-1; i++){
			tmp+=(get(i).getName()+",");
		}
		tmp+=(get(i).getName()+"]");
		return tmp;
	}

}
