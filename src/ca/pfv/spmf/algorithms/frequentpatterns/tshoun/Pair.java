package ca.pfv.spmf.algorithms.frequentpatterns.tshoun;



class Pair{
	Integer[] estimatedUtility;
	int exactUtility = 0;
	
	Pair(int periodCount){
		estimatedUtility = new Integer[periodCount];
	}
}