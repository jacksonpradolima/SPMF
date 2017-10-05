package ca.pfv.spmf.algorithms.frequentpatterns.HUIM_BPSO_tree;
/**
 * This class represents the OR/NOR-tree node.
 * 
 * @author Jerry Chun-Wei Lin, Lu Yang, Philippe Fournier-Viger
 */
public class treeNode {
	int item;//item of each node
	treeNode OR;
	treeNode NOR;
	
	public treeNode() {
		OR=null;
		NOR=null;
	}

	public treeNode(int item) {
		super();
		this.item = item;
		this.OR=null;
		this.NOR=null;
	}
	
}
