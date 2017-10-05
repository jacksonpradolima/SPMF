package ca.pfv.spmf.algorithms.sequenceprediction.ipredict.predictor.LZ78;

import java.util.HashSet;
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
public class LZNode {

	/**
	 * Label of the node
	 */
	public int value;
	
	/**
	 * List of children nodes
	 */
	public HashSet<Integer> children;
	
	/**
	 * Support of the node
	 */
	private int support;
	
	/**
	 * Sum of its child's support
	 */
	private int childSumSupport;
	
	public LZNode(int value) {
		this.value = value;
		children = new HashSet<Integer>();
		support = 1;
		childSumSupport = 0;
	}
	
	/**
	 * Add child to the node
	 */
	public void addChild(Integer child) {
		children.add(child);
		incChildSupport();
	}
	
	public void incChildSupport() {
		childSumSupport++;
	}
	
	/**
	 * Increment the support of this node
	 */
	public void inc() {
		support++;
	}
	
	/**
	 * Returns the support of this node
	 */
	public int getSup() {
		return support;
	}
	
	/**
	 * Returns the sum of its child's support
	 */
	public int getChildSup() {
		return childSumSupport;
	}
	
	
}
