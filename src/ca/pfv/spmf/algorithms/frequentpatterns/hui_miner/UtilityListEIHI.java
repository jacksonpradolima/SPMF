package ca.pfv.spmf.algorithms.frequentpatterns.hui_miner;

/* This file is copyright (c) 2008-2013 Philippe Fournier-Viger
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


import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a UtilityList as used by the HUI-Miner algorithm.
 *
 * @see AlgoHUIMiner
 * @see Element
 * @author Philippe Fournier-Viger
 */
public class UtilityListEIHI {

	// the last item of the itemset represented by this utility list
	public Integer item;
	// the sum of iutil values of D
	public int sumIutilsD = 0;
	// the sum of rutil values  of D
	public int sumRutilsD = 0;
	 
	// the sum of iutil values of D'
	public int sumIutilsDP = 0;
	// the sum of rutil values of D'
	public int sumRutilsDP = 0;
	
	// the list of elements in this utility list
	public List<Element> elementsD = new ArrayList<Element>();
	public List<Element> elementsDP = new ArrayList<Element>();

	/**
	 * Constructor
	 * @param item the last item of the itemset represented by this utility list
	 */
	public UtilityListEIHI(Integer item) {
		super();
		this.item = item;
	}

	/**
	 * Method to add an element to this utility list and update the sums at the same time.
	 * @param element the element to be added
	 * @param firstTIDOfDP the tid of the first transaction of DP
	 */
	public void addElementD(Element element/*, int firstTIDOfDP*/){
		sumIutilsD += element.iutils;
		sumRutilsD += element.rutils;
		elementsD.add(element);
	}
	
	/**
	 * Method to add an element to this utility list and update the sums at the same time.
	 * @param element the element to be added
	 * @param firstTIDOfDP the tid of the first transaction of DP
	 */
	public void addElementDP(Element element/*, int firstTIDOfDP*/){
			sumIutilsDP += element.iutils;
			sumRutilsDP += element.rutils;
			elementsDP.add(element);
	}

	public void switchDPtoD() {
		sumIutilsD += sumIutilsDP;
		sumIutilsDP = 0;
		sumRutilsD += sumRutilsDP;
		sumRutilsDP = 0;
		elementsD.addAll(elementsDP);
		elementsDP.clear();
	}
}
