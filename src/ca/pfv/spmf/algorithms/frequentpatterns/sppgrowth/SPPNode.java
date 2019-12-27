package ca.pfv.spmf.algorithms.frequentpatterns.sppgrowth;
import java.util.ArrayList;
import java.util.List;

public class SPPNode {
    int itemID = -1;  // item id
    List<Integer> TIDs = new ArrayList<Integer>();

    // the parent node of that node or null if it is the root
    SPPNode parent = null;

    // the child nodes of that node
    List<SPPNode> childs = new ArrayList<SPPNode>();

    // link to next node with the same item id (for the header table).
    SPPNode nodeLink = null;

    SPPNode(){

    }

    /**
     * Return the immediate child of this node having a given ID.
     * If there is no such child, return null;
     */
    public SPPNode getChildByID(int id) {
        // for each child node
        for(SPPNode child : childs){
            // if the id is the one that we are looking for
            if(child.itemID == id){
                // return that node
                return child;
            }
        }
        // if not found, return null
        return null;
    }

    public void removeChildByID(int id){
        //for each child node
        for(int i = 0;i<childs.size();i++){
            if(childs.get(i).itemID == id){
                childs.remove(i);
                return;
            }
        }
    }


    public String toString() {
        return ""+itemID;
    }
}
