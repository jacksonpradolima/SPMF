package ca.pfv.spmf.algorithms.sequentialpatterns.qcsp;

public class Window {
	public int t;
	public int a; //note: a is start of SUFFIX
	public int b; //note: b is end of window and SUFFIX
	public Window(int t, int a, int b) {
		this.t= t; this.a=a; this.b=b;
	}
}
