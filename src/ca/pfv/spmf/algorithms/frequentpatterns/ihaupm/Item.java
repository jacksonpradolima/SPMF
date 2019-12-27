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
 * @see util.Itemset
 * @see util.StackElement
 * @author Shi-Feng Ren
 */
public class Item {
	public String name;
	public Integer quantity;

	public Item(String name, Integer quantity){
		this.name = name;
		this.quantity = quantity;
	}

	@Override
	public int hashCode(){
		return name.hashCode() + quantity.hashCode() +super.hashCode();
	}

	@Override
	public String toString(){
		return "< name:"+name+",quantity:"+quantity+">";
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
}
