package ca.pfv.spmf.algorithms.frequentpatterns.UFH;
/**
 * Node list structure used by the UFH algorithm.
 * @author Siddharth Dawar
 * @see AlgoUFH
 */
public class NodeList_SPMF {
	int item;
	NodeList_SPMF next;

	public NodeList_SPMF(int itemName) {
		this.item = itemName;
		this.next = null;
	}

	public int getItemName() {
		return this.item;
	}

	public NodeList_SPMF getNextNode() {
		return this.next;
	}

	public NodeList_SPMF addNode(NodeList_SPMF node) {
		this.next = node;
		return this;
	}

}