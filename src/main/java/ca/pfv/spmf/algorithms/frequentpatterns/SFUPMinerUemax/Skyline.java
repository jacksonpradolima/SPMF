package ca.pfv.spmf.algorithms.frequentpatterns.SFUPMinerUemax;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a skyline point, which contains itemSet, frequency and utility
 * @author Jerry Chun-Wei Lin, Lu Yang, Philippe Fournier-Viger
 *
 */

public class Skyline {
	String itemSet ;  //the itemset	
	int frequent; 	//the frequency of itemset
	int utility; //the utility of itemset

}

class SkylineList{
	//skylinelist store different itemsets that have same frequency and same utility.
	List<Skyline> skylinelist= new ArrayList<Skyline>();

	public Skyline get(int index) {
		return skylinelist.get(index);
	}

	public void add(Skyline e) {
		skylinelist.add(e);
	}
	
	public void remove(int index) {
		skylinelist.remove(index);
	}
	
	public int size(){
		return skylinelist.size();
	}
}

