package ca.pfv.spmf.algorithms.frequentpatterns.uphist;
/* This file is copyright (c) 2018+  by Siddharth Dawar et al.
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
 * A node-list as used by the UPHist algorithm.
 * @author S. Dawar et al.
 *@see AlgoUPHist
 */
public class NodeList{
	int item;
	int max_quantity=0;
	int min_quantity=0;
	NodeList next;
	//UPNode UP=new UPNode();
	//HashMap<Short,Integer> H=null;
	Hist histogram=null;
	
	public NodeList(int itemName){
		this.item = itemName;
		this.next = null;
	}
	public NodeList(int itemName,int max_quantity){
		this.item = itemName;
		this.next = null;
		this.max_quantity=max_quantity;
	}
	public NodeList(int itemName,Hist hist )
	{
		this.item=itemName;
		this.next=null;
		this.histogram=new Hist();
		this.histogram.updateHist(hist);
		
	}
	public NodeList(int itemName,int max_quantity,int min_quantity){
		this.item = itemName;
		this.next = null;
		this.max_quantity=max_quantity;
		this.min_quantity=min_quantity;
	}
	public int getItemName(){
		return this.item;
	}
	public int getmaxquantity(){
		return this.max_quantity;
	}
	public int getminquantity(){
		return this.min_quantity;
	}
	public Hist getHistogram(){
		return this.histogram;
		
	}
	
	public NodeList getNextNode(){
		return this.next;
	}
	public NodeList addNode(NodeList node){
		this.next = node;
		return this;
	}
	

	
}