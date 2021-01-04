package ca.pfv.spmf.algorithms.sequentialpatterns.qcsp;

import java.util.ArrayList;
import java.util.List;

public class SequentialPattern{
	public List<Integer> pattern;
	public SequentialPattern() {
		this.pattern = new ArrayList<Integer>();
	}
	public SequentialPattern(SequentialPattern prefix, Integer item){
		this.pattern = new ArrayList<Integer>(prefix.length() + 1);
		for(int i=0; i<prefix.length();i++) {
			this.pattern.add(prefix.pattern.get(i));
		}
		this.pattern.add(item);
	}
	final public int length() {
		return pattern.size();
	}
}
