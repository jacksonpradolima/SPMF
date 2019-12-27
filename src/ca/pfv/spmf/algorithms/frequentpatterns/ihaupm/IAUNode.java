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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is an implementation of the "IHAUPM" algorithm for High-Average-Utility Itemsets Mining
 * as described in the conference paper : <br/><br/>
 *
 * Jerry Chun-Wei Lin, Shifeng Ren, and Philippe Fournier-Viger. Efficiently Updating the Discovered High Average-Utility Itemsets with Transaction Insertion. EAAI (unpublished, minor revision)
 *
 * @see IHAUPM.AlgoIHAUPM
 * @see tree.IAUTree
 * @see tree.TableNode
 * @see util.Item
 * @see util.Itemset
 * @see util.StackElement
 * @author Shi-Feng Ren
 */

public class IAUNode {

	public static class IAUPair{
		public String name;
		public int quan;
		public IAUPair(String i, int quan){
			this.name = i;
			this.quan = quan;
		}
		public String toString(){
			return "*name:"+name+",quantity:"+quan+"*";
		}
	}
	@Override
	public String toString(){
		//String str="";
		//str+="AUUB:"+AUUB+",quanAry:"+quanAry+",children:"+children;
		return getName()+":"+this.AUUB;
		//return null;
	}
	/**
	 * just used by HeadTableList, record the index of element in headTableList
	 */
	private int index;
	/**
	 * save its children node referrence
	 */
	private Map<String, IAUNode> children=null;
	/**
	 * quantity of its prefix
	 */
	private ArrayList<IAUPair> quanAry=null;
	/**
	 * average utility upper bound of this item
	 */
	private long AUUB;

	private IAUNode parent = null;

	private IAUNode left = null;
	private IAUNode right = null;

	public void removeQuantityAt(int index){
		this.quanAry.remove(index);
	}
	public int quantityArySize(){
		return this.quanAry.size();
	}

	public void mergeQuanAry(IAUNode node){
		ArrayList<IAUPair> qa = node.getQuanAry();
		for (int i=0; i<qa.size(); i++){
			IAUPair p = qa.get(i);
			IAUPair ps = quanAry.get(i);
			ps.quan += p.quan;
		}
	}
	public void mergeAUUB(IAUNode node){
		this.AUUB += node.AUUB;
	}

	public IAUNode getPareent(){
		return this.parent;
	}
	public void setParent(IAUNode parent){
		this.parent = parent;
	}

	public IAUNode getLeft() {
		return left;
	}
	public void setLeft(IAUNode left) {
		this.left = left;
	}
	public IAUNode getRight() {
		return right;
	}
	public void setRight(IAUNode right) {
		this.right = right;
	}
	public Map<String, IAUNode> getChildren() {
		return children;
	}
	public IAUNode(){}

//	public IAUNode(int auub){
//		this(auub,null);
//	}
	public IAUNode(int auub, IAUNode parent){
		this.AUUB = auub;
		this.parent = parent;
		children = new HashMap<>();
		quanAry = new ArrayList<>();
	}
	// prepare for headtable
	public IAUNode(int auub, String name){
		AUUB = auub;
		quanAry = new ArrayList<>();
		quanAry.add(new IAUPair(name,0) );
	}

	public String getName(){
		if (quanAry.size() !=0)
			return quanAry.get(quanAry.size()-1).name;
		return null;
	}

	public void updateQuanBefor(int index, List<Item> quans){
		for (int i=0; i<=index; i++){
			IAUPair p = quanAry.get(i);
			p.quan += quans.get(i).getQuantity();
		}
	}
	public void addQuansBefor(int index, List<Item> quans){
		for(int i=0; i<=index; i++){
			Item item = quans.get(i);
			quanAry.add(new IAUPair(item.getName(), item.getQuantity()));
		}
	}

	/**
	 * add one child of this node
	 * @param child
	 */
	public void putChild(String I, IAUNode child){
		children.put(I, child);
	}

	public void removeChild(String name){
		children.remove(name);
	}

	public IAUNode getChild(String name) {
		return children.get(name);
	}
	public void setChildren(Map<String,IAUNode> children) {
		this.children = children;
	}
	public long getAUUB() {
		return AUUB;
	}
	public void setAUUB(int aUUB) {
		AUUB = aUUB;
	}
	public void plusAUUB(int utility){
		AUUB+=utility;
	}
	public ArrayList<IAUPair> getQuanAry() {
		return quanAry;
	}
	public void setQuanAry(ArrayList<IAUPair> quanAry) {
		this.quanAry = quanAry;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}


}
