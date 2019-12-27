package ca.pfv.spmf.algorithms.sequentialpatterns.clofast;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import ca.pfv.spmf.algorithms.sequentialpatterns.clofast.model.Itemset;
import ca.pfv.spmf.algorithms.sequentialpatterns.clofast.model.ListNode;
import ca.pfv.spmf.algorithms.sequentialpatterns.clofast.model.Sequence;
import ca.pfv.spmf.algorithms.sequentialpatterns.clofast.model.SparseIdList;
import ca.pfv.spmf.algorithms.sequentialpatterns.clofast.model.VerticalIdList;
import ca.pfv.spmf.algorithms.sequentialpatterns.clofast.model.tree.ItemsetNode;
import ca.pfv.spmf.algorithms.sequentialpatterns.clofast.model.tree.ItemsetTree;
import ca.pfv.spmf.algorithms.sequentialpatterns.clofast.model.tree.SequenceNode;
import ca.pfv.spmf.algorithms.sequentialpatterns.clofast.model.tree.SequenceTree;
import ca.pfv.spmf.tools.MemoryLogger;


/* This file is copyright (c)  E. Salvemini et al.
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
 * This is an implementation of the Fast algorithm. Fast was proposed in the following paper
 *<br/><br/>
 *
 *Salvemini E, Fumarola F, Malerba D, Han J (2011) FAST sequence mining based 
 *on sparse id-lists. In: Kryszkiewicz M, Rybinski H, Skowron A, Ras ZW (eds) 
 *ISMIS, vol 6804 of Lecture Notes in Computer Science, Springer, Berlin, pp 316–325<br/><br/>
 * 
 * @author E. Salvemini et al.
 */
public class AlgoFast {

	/** The dataset */
    private FastDataset ds;

    /** The sequence tree */
    private SequenceTree sequenceTree;
    
	/** the time the algorithm started */
	long startTimestamp = 0; 
	
	/** the time the algorithm terminated */
	long endTimestamp = 0;  
	
	/** the number of patterns generated */
	int patternCount = 0;  
	


    /**
     * Constructor
     */
    public AlgoFast() {
    	
    }

    /**
     * Run the algorithm
     * 1. itemset extension
     * 2. sequence extension
     */
    private void run() {
        itemsetExtension();

        MemoryLogger.getInstance().checkMemory();
        
        sequenceTree = sequenceExtension();
    }

    
    public List<SequenceNode> getFrequentSequences() {
        return SequenceTree.visit(sequenceTree);
    }

    private void itemsetExtension() {
        final ItemsetTree tree = new ItemsetTree();
        final ItemsetNode root = tree.getRoot();

        final Queue<ItemsetNode> queue = new LinkedList<>();
        int pos = 0;
        ItemsetNode node;
        for (Map.Entry<String, SparseIdList> entry : ds.getFrequentItemsets().entrySet()) {
            node = tree.addChild(root, new Itemset(entry.getKey()), entry.getValue(), pos++);
            queue.add(node);
        }

        // explore the other levels, expand the tree
        while (!queue.isEmpty()) {
            node = queue.remove();
            itemsetExtension(tree, node);
            queue.addAll(node.getChildren());
        }
    }

    /**
     * @param tree
     * @param n    do the itemset extension on a node. It extends the tree by
     *             adding its children which contain a frequent itemset
     */
    private void itemsetExtension(ItemsetTree tree, ItemsetNode n) {

        //SparseIdList newSparseIdList = new SparseIdList(ds.getNumRows());
        //float support = 0;
        int pos = 0;
        // get the children list of the parent node, which are the brothers of
        // the current node
        List<ItemsetNode> children = n.getParent().getChildren();

        for (int i = n.getPosition() + 1; i < children.size(); i++) {
            ItemsetNode rightBrother = children.get(i);

            SparseIdList sil = SparseIdList.IStep(n.getSil(), rightBrother.getSil());

            if (sil.getAbsoluteSupport() >= ds.getAbsMinSup()) {
                // create the new sequence as replica
                Itemset newItemset = n.getItemset().clone();
                newItemset.addItem(rightBrother.getItemset().getLast());

                ds.getFrequentItemsets().put(newItemset.concatenate(), sil);
                tree.addChild(n, newItemset, sil, pos);
                pos++;
            }
        }

    }

    private SequenceTree sequenceExtension() {
        sequenceTree = new SequenceTree(ds.getNumRows());

        // create a queue to read the tree
        Queue<SequenceNode> queue = new LinkedList<>();

        Sequence s;
        SequenceNode node;
        for (Map.Entry<String, SparseIdList> entry : ds.getFrequentItemsets().entrySet()) {
            s = new Sequence(new Itemset(entry.getKey().split(" ")));
            VerticalIdList vil = entry.getValue().getStartingVIL();
            node = sequenceTree.addChild(sequenceTree.getRoot(), s, vil, entry.getValue().getAbsoluteSupport());
            queue.add(node);
        }

        // explore the other levels, expand the tree
        while (!queue.isEmpty()) {
            node = queue.remove();
            sequenceExtension(sequenceTree, node);
            queue.addAll(node.getChildren());
        }
        return sequenceTree;

    }

    private void sequenceExtension(SequenceTree tree, SequenceNode node) {
        int count = 0;
        ListNode[] newPosList;
        ListNode listNode, listNodeBrother;

        VerticalIdList vilNode = node.getVerticalIdList();
        VerticalIdList vilBrother;

        List<SequenceNode> brothers = node.getParent().getChildren();
        for (SequenceNode brotherNode : brothers) {

            newPosList = new ListNode[vilNode.getElements().length];
            vilBrother = brotherNode.getVerticalIdList();

            for (int i = 0; i < vilNode.getElements().length; i++) {

                listNode = vilNode.getElements()[i];
                listNodeBrother = vilBrother.getElements()[i];
                // when i found a null element I exit the for
                if ((listNode == null) || (listNodeBrother == null)) {
                    continue;
                }

                // case 1:
                if ((listNode.getColumn() < listNodeBrother.getColumn())) {
                    newPosList[i] = listNodeBrother;
                    count++;
                    // case 2:
                } else if ((listNode.getColumn() >= listNodeBrother.getColumn())) {
                    while ((listNodeBrother != null) && (listNode.getColumn() >= listNodeBrother.getColumn())) {
                        listNodeBrother = listNodeBrother.next();
                    }
                    if (listNodeBrother != null) {
                        newPosList[i] = listNodeBrother;
                        count++;
                    }
                }
            }
            //finally
            if (count >= ds.getAbsMinSup()) {
                Sequence sequence = node.getSequence().clone();
                sequence.add(brotherNode.getSequence().getLastItemset());
                tree.addChild(node, sequence, new VerticalIdList(newPosList, count), count);
            }
            count = 0;
        }
    }

    private void writePatterns(Path outputFile) throws IOException {

        final BufferedWriter out = Files.newBufferedWriter(outputFile);

        List<SequenceNode> nodes = getFrequentSequences();

        for (SequenceNode node : nodes) {
            out.write(node.toString() + System.lineSeparator());
        }
        out.flush();
        out.close();
        
        patternCount = nodes.size();
//        statistics.setNumFrequentSequences(nodes.size());
    }
    
    /**
     * Run the algorithm
     * @param inputFile  an input file path in SPMF format
     * @param outputPath  an output file path
     * @param minsup  the minimum suppor threshold
     * @throws IOException   if error reading or writing to file
     */
	public void runAlgorithm(String inputFile, String outputPath, float minsup) throws IOException {
		// read the dataset
		
		startTimestamp = System.currentTimeMillis();
		MemoryLogger.getInstance().reset();

        this.ds = FastDataset.fromPrefixspanSource(inputFile, minsup);
        
        // run the algoritm
        run();

        //save patterns to the output file
        writePatterns(Paths.get(outputPath));
        
        MemoryLogger.getInstance().checkMemory();
        endTimestamp = System.currentTimeMillis();
	}
	
    /**
     * Run the algorithm
     * @param FastDataset dataset  a dataset already loaded in memory
     * @param outputPath  an output file path
     * @param minsup  the minimum suppor threshold
     * @throws IOException   if error reading or writing to file
     */
	public void runAlgorithm(FastDataset dataset, String outputPath, float minsup) throws IOException {

		startTimestamp = System.currentTimeMillis();
		MemoryLogger.getInstance().reset();
		
		this.ds = dataset;
        
        // run the algoritm
        run();

        //save patterns to the output file
        writePatterns(Paths.get(outputPath));
        
        MemoryLogger.getInstance().checkMemory();
        endTimestamp = System.currentTimeMillis();
	}

	/**
	 * Print the statistics of the algorithm execution to System.out.
	 */
	public void printStatistics() {
		StringBuilder r = new StringBuilder(200);
		r.append("=============  Algorithm Fast v2.29 - STATISTICS =============\n");
		r.append("Pattern count : ");
		r.append(patternCount);
		r.append('\n');
		r.append("Total time: ");
		r.append((endTimestamp - startTimestamp) / 1000f );
		r.append(" s \n");
		r.append("Max memory (mb) : " );
		r.append( MemoryLogger.getInstance().getMaxMemory());
		r.append('\n');
		r.append("===================================================\n");
		System.out.println(r.toString());	
	}
    

}