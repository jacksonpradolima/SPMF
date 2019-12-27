package ca.pfv.spmf.algorithms.sequenceprediction.ipredict.predictor.TDAG;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.Item;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.Sequence;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.predictor.Predictor;
/*
 * This file is copyright (c) Ted Gueniche 
 * <ted.gueniche@gmail.com>
 *
 * This file is part of the IPredict project
 * (https://github.com/tedgueniche/IPredict).
 *
 * IPredict is distributed under The MIT License (MIT).
 * You may obtain a copy of the License at
 * https://opensource.org/licenses/MIT 
 */
/**
 * TDAG predictor is based on Markov Trees
 * Its fast but it has drawbacks in terms 
 * It takes a lot of space!
 * This implementation has a few of the optimization presented in the original paper below.
 * 
 * Optimization#1: max tree height as described in original papel -> reduce spacial size without noticeable effect on accuracy and coverage (to be double checked)
 * 
 * Source:  P. Laird and R. Saul, "Discrete sequence prediction and its applications"  Mach. Learning, vol. 15, pp. 43-68, 1994. 
 */
public class TDAGPredictor extends Predictor {

	/**
	 * FIFO used during training to remember the last Node inserted in the tree
	 */
	private List<TDAGNode> state;
	
	/**
	 * Root of the tree
	 */
	private TDAGNode root;
	
	/**
	 * Number of nodes in the tree
	 */
	private Integer size;
	
	/**
	 * Max tree height, forbid creating branch with a length higher
	 * than this parameter
	 */
	private final Integer maxTreeHeight = 6;
	
	/**
	 * Map a list of symbol to a specific node in the tree
	 * It is used to lookup specific nodes in the prediction method.
	 */
	private HashMap<List<Integer>, TDAGNode> mDictionary;
	
	
	public TDAGPredictor() {
		TAG = "TDAG";
	}

	public TDAGPredictor(String tag) {
		TAG = tag;
	}

	@Override
	public Boolean Train(List<Sequence> trainingSequences) {
		
		//reset
		root = new TDAGNode(0, new ArrayList<Integer>());
		size = 1;
		state = new ArrayList<TDAGNode>();
		mDictionary = new HashMap<List<Integer>, TDAGNode>();
		
		
		//for each training sequence
		for(Sequence seq : trainingSequences) {
			
			//resetting the states
			state.clear();
			state.add(root);
			
			//For each item in the current sequence
			for(Item item : seq.getItems()) {
				
				//Initiating the newState
				List<TDAGNode> newState = new ArrayList<TDAGNode>();
				newState.add(root);
				
				//Adding a child with this item to each of the nodes in State
				for(TDAGNode node : state) {
					
					//if the node has not the maximal allowed height
					if(node.pathFromRoot.size() <= maxTreeHeight) {
						
						/** START OF BUGFIX 2018-01-24
						 * Removed redundant size
						 * increments.
						 *
						 * size was incremented although
						 * the child node might have
						 * already existed. Now the
						 * absence of the child node is
						 * checked before the size is
						 * incremented.
						 */
						
						//Only increase the size if a new node will be added
						if (!node.hasChild(item.val)) {
							size++;
						}
//						size++;
						/**  END OF BUGFIX **/

						//Create and insert the node
						TDAGNode child = node.addChild(item.val);
						mDictionary.put(child.pathFromRoot, child);

						//Pushing the new child in the next state
						newState.add(child);
					}
				}
				
				//Overwriting State with the newState
				state = newState;
			}
		}
		
		//Free memory since this is only used in the training process
		state.clear();
		
		return true;
	}

	@Override
	public Sequence Predict(Sequence target) {
		Sequence predicted = new Sequence(-1);
		
		//Converting the target sequence into a list of symbol
		List<Integer> symbols = new ArrayList<Integer>();
		symbols.add(0);
		for(Item item : target.getItems()) {
			symbols.add(item.val);
		}
		
		//Looking for a Node in the tree that contains the same symbols as a 
		//path from the root.
		TDAGNode context = mDictionary.get(symbols);
		
		/*** START BUG FIX 2018-01-25 
		 * Fixed TDAG Prediction Bug 1: Because symbols(0) is the root
 		 * node of the sequence, the symbol with the least relevance is
 		 * always at position 1. This bug is one reason why the TDAG
 		 * algorithm has high 'No Match' values.*/
		//while(context == null && symbols.size() > 0) {
		while(context == null && symbols.size() > 1) {

			//removing the less relevant symbol from the symbols
//			symbols.remove(0);
			//as symbols(0) is the rootNode, we have to remove element (1)
			symbols.remove(1);
			/** END OF BUG FIX */
			
			//Attempting to extract the right node
			context = mDictionary.get(symbols);
			
			/** START BUG FIX 2018-01-25 
			* Fixed TDAG Prediction Bug 2: When searching for a
			* suitable context in the tree, ignore all contexts
			* that do not have any children, as they do not provide
			* any information. Else the prediction will return an
			* empty Sequence, although there might be a context of
			* a lower order that can provide information.
			*/
			//Do not use a Context that does not have any children
			if (context != null && context.children.size() == 0) {
				context = null;
			}
			/** END OF BUG FIX */
		}
		
		
		if(context != null) {
			TDAGNode candidate1 = null; //Best candidate
			TDAGNode candidate2 = null; //Second best candidate
			
			//For each child of this context, we calculate the score (probability of appearance given the context)
			for(Entry<Integer, TDAGNode> entry : context.children.entrySet()) {
				
				double score = ((double) entry.getValue().inCount / context.outCount);
				entry.getValue().score = score;
				
				if(candidate1 == null || candidate1.score < score) {
					candidate2 = candidate1;
					candidate1 = entry.getValue();
				}
				else if(candidate2 == null || candidate2.score < score) {
					candidate2 = entry.getValue();
				}
			}
			
			
			//Generating a prediction with candidate1 only if
			//candidate1 has a higher score than candidate2 
			Double treshold = 0.0;
			/** START OF BUG FIX 2018-01-24 
			 * /**
			 * Fixed TDAG Prediction Bug 3: If a context has two
			 * children with the same score, the predictor returned
			 * an empty sequence instead of one of the two symbols.
			 *
			 * Instead of changing > to >=, the whole determination
			 * of candidate 2 could be skipped too.
			 */
			if(candidate1 != null && 
					//(candidate2 == null || candidate1.score - candidate2.score > treshold)) {
					(candidate2 == null || candidate1.score - candidate2.score >= treshold)) {
				/** END OF BUG FIX */
				predicted.addItem(new Item(candidate1.symbol));
			}
		}
		
		return predicted;
	}


	public long size() {
		return size;
	}

	/**
	 *  Each node has a list of children, the sum of the lists for all children is equals to the number of nodes.
+	 * Each nodes has also three integers one child ref (4 bytes) + 3 integers (12 bytes) = 16 bytes
	 */
	public float memoryUsage() {
		/** START OF BUG FIX 2018-01-24 */
//		return 2 * size * 12;
		return size * 16;
		/** END OF BUG FIX */
	}
	
	public static void main(String...args) {
		
		Sequence A = new Sequence(1);
		A.addItem(new Item(1));
		A.addItem(new Item(2));
		A.addItem(new Item(3));
		
		Sequence B = new Sequence(2);
		B.addItem(new Item(1));
		B.addItem(new Item(3));
		B.addItem(new Item(2));
		
		List<Sequence> trainingSet = new ArrayList<Sequence>();
		trainingSet.add(A);
		trainingSet.add(B);
		
		TDAGPredictor p = new TDAGPredictor();
		p.Train(trainingSet);
		
		Sequence X = new Sequence(3);
		X.addItem(new Item(4));
		
		Sequence predicted = p.Predict(X);
		System.out.println("Predicted "+ predicted);
	}

}
