package ca.pfv.spmf.algorithms.frequentpatterns.sppgrowth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SPPTree {
    // List of items in the header table
    List<Integer> headerList = null;

    // List of pairs (item, SPPNode) of the header table
    Map<Integer, SPPNode> mapItemNodes = new HashMap<Integer, SPPNode>();

    // Map that indicates the last node for each item using the node links
    // key: item   value: an SPPNode
    Map<Integer, SPPNode> mapItemLastNode = new HashMap<>();

    // root of the tree
    SPPNode root = new SPPNode(); // null node

    int numOfNode = 0;

    /**
     * Constructor
     */
    public SPPTree(){
    }


    /**
     * Method for adding a transaction and its timestamp to the SPP-tree
     * (for the initial construction of the SPP-Tree).
     * @param transaction
     * @param TID
     */
    public void addTransaction(List<Integer> transaction,int TID) {
        SPPNode currentNode = root;

        // For each item in the transaction
        for(Integer item : transaction){
            // look if there is a node already in the FP-Tree
            SPPNode child = currentNode.getChildByID(item);
            if(child == null){
                numOfNode++;
                // there is no node, we create a new one
                SPPNode newNode = new SPPNode();
                newNode.itemID = item;
                newNode.parent = currentNode;
                // we link the new node to its parrent
                currentNode.childs.add(newNode);

                // we take this node as the current node for the next for loop iteration
                currentNode = newNode;

                // We update the header table.
                // We check if there is already a node with this id in the header table
                fixNodeLinks(item, newNode);
            }else{
                // there is a node already, we skip it
                currentNode = child;
            }
        }
        // add TIDs to tail node(currentNode)
        currentNode.TIDs.add(TID);
    }

    /**
     * Method to fix the node link for an item after inserting a new node.
     * @param item  the item of the new node
     * @param newNode the new node thas has been inserted.
     */
    private void fixNodeLinks(Integer item, SPPNode newNode) {
        // get the latest node in the tree with this item
        SPPNode lastNode = mapItemLastNode.get(item);
        if(lastNode != null) {
            // if not null, then we add the new node to the node link of the last node
            lastNode.nodeLink = newNode;
        }
        // Finally, we set the new node as the last node
        mapItemLastNode.put(item, newNode);

        SPPNode headernode = mapItemNodes.get(item);
        if(headernode == null){  // there is not
            mapItemNodes.put(item, newNode);
        }
    }

    /**
     * Method for creating the list of items in the header table,
     *  in descending order of total duration.
     * @param mapSPP_list the frequencies of each item (key: item  value: support + maxla)
     */
    void createHeaderList(List<Integer> lastHeaderList,final Map<Integer, Support_maxla> mapSPP_list) {

        if(lastHeaderList == null) {
            // this is headerList for total tree

            // create an array to store the header list with
            // all the items stored in the map received as parameter
            headerList = new ArrayList<Integer>(mapItemNodes.keySet());

            // sort the header table by decreasing order of total duration
            Collections.sort(headerList, new Comparator<Integer>() {
                public int compare(Integer item1, Integer item2) {

                    // if the same total duration, we check the lexical ordering!

                    int compare = mapSPP_list.get(item2).getSupport() - mapSPP_list.get(item1).getSupport();
                    if(compare == 0){
                        return (item1 - item2);
                    }
                    // otherwise, just use the total duration
                    return compare;
//                return item1 - item2;
                }
            });
        }else{
            headerList = new ArrayList<>();
            for(int item:lastHeaderList){
                if(mapSPP_list.containsKey(item)){
                    headerList.add(item);
                }
            }
        }
    }

    /**
     * Method for adding a prefixpath to a SPPTree
     * @param prefixPath            The prefix path
     * @param mapBetaSPPlist        The TIDs of items in the prefixpaths
     */
    public void addPrefixPath(List<SPPNode> prefixPath, Map<Integer,Support_maxla> mapBetaSPPlist) {
        // the first element of the prefix path contains the path TIDs
        List<Integer> pathTIDs = prefixPath.get(0).TIDs;

        SPPNode currentNode = root;

        // For each item in the transaction  (in backward order)
        // (and we ignore the first element  of the prefix path)
        for(int i = prefixPath.size() -1; i >=1; i--){
            SPPNode pathItem = prefixPath.get(i);
            // if the item has periodic frequent time-interval
            if(mapBetaSPPlist.containsKey(pathItem.itemID) ){

                // look if there is a node already in the FP-Tree
                SPPNode child = currentNode.getChildByID(pathItem.itemID);
                if(child == null){
                    // there is no node, we create a new one
                    SPPNode newNode = new SPPNode();
                    newNode.itemID = pathItem.itemID;
                    newNode.parent = currentNode;
                    currentNode.childs.add(newNode);
                    currentNode = newNode;
                    // We update the header table.
                    // and the node links
                    fixNodeLinks(pathItem.itemID, newNode);
                }else{
                    // there is a node already, we update it
                    currentNode = child;
                }
            }
        }
        // for the second element should accept the TIDs from first Node(pathTIDs)
        // tail Node move its TIDs to its parent
        if(currentNode.itemID != -1) {
            currentNode.TIDs.addAll(pathTIDs);
        }
    }


    public void removeTailItem() {
        // get the index of tail
        int tail = headerList.size()-1;

        // get the tail path
        SPPNode tailNode = mapItemNodes.get(headerList.get(tail));

        //delete the tail path from mapItemNodes
        mapItemNodes.remove(headerList.get(tail));

        // delete the tail from headerList
        headerList.remove(tail);

        // move each tail node's timestamps to its parent
        while(tailNode !=null){
            SPPNode parent = tailNode.parent;
            parent.removeChildByID(tailNode.itemID);
            if(parent.itemID!= -1) {
                List<Integer> timestamps = tailNode.TIDs;

                parent.TIDs.addAll(new ArrayList<Integer>() {{
                    addAll(timestamps);
                }});

            }
            tailNode = tailNode.nodeLink;
        }

    }
}
