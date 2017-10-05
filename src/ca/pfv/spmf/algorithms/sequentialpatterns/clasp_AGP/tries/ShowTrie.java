package ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.tries;

import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;


/**
 * This class is used to display a trie visually using Swing if necessary.
 * 
 * Copyright Antonio Gomariz Peñalver 2013
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
 *
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author agomariz
 */
public class ShowTrie {

	static int j=0;

    /**
     * Method to show the tree in a graphical way
     */
    public static void showTree(Trie frequentAtomsTrie) {
    	DefaultMutableTreeNode root = new DefaultMutableTreeNode("Tree");
        DefaultTreeModel model = new DefaultTreeModel(root);
        JTree tree = new JTree(model);

        display(frequentAtomsTrie, model, root);

        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }

        // Construction a visualization of the window
        JFrame v = new JFrame();
        JScrollPane scroll = new JScrollPane(tree);
        v.getContentPane().add(scroll);
        v.pack();
        v.setVisible(true);
        v.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
    

    /**
     * Method to display graphically the Trie by means of a TreeModel
     * @param model TreeModel when we want to insert the Trie nodes
     * @param p TreeNode for the TreeModel
     */
    private static void display(Trie frequentAtomsTrie, DefaultTreeModel model, MutableTreeNode p) {
    	List<TrieNode> nodes = frequentAtomsTrie.nodes;
        if (nodes != null) {
            //For each node
            for (int i = 0; i < nodes.size(); i++) {
                TrieNode node = nodes.get(i);
                Trie child = node.getChild();

                //We create a new TreeNode composed of the pair and the list of appearances
                DefaultMutableTreeNode currentNode = new DefaultMutableTreeNode(node.getPair().toString()+" ("+child.getSupport()+")");
                //And we insert it in the TreeModel
                model.insertNodeInto(currentNode, p, i);
                //And we go on doing the same process with the child
                j++;
                if(j <3) {
                	display(child, model, currentNode);
                }
                j--;
            }
        }
    }
}
