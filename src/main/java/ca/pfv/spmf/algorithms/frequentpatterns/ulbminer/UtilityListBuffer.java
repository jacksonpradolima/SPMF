package ca.pfv.spmf.algorithms.frequentpatterns.ulbminer;


import java.util.ArrayList;

/**
 * This is an implementation of the utility-buffer as used by the "ULB-Miner" algorithm 
 * for High-Utility Itemsets Mining: <br/><br/>
 * 
 * Duong, Q.H., Fournier-Viger, P., Ramampiaro, H., Norvag, K. Dam, T.-L. (2017). 
 * Effcient High Utility Itemset Mining using Buffered Utility-Lists. Applied Intelligence, 
 * Springer (to appear)
 * 
 * @see UtilityList
 * @see Element
 * @see AlgoULBMiner
 * @author Philippe Fournier-Viger & Q. H. Duong
 */
public class UtilityListBuffer {
	
	/** The array of tids of elements stored in utility-lists */
	ArrayList<Element> elements = new ArrayList<Element>();
	
	/** The array of utility-lists  ( a buffer)*/
	ArrayList<Summary> summaries = new ArrayList<Summary>();
	
	/** IMPORTANT: the current utility-list that the program is using */
	public Summary currentSummary = null;
	
	/** This is an inner-class providing information about a utility-list */
	 class Summary{
		Integer item;
		int startPos;
		int endPos;
		int sumIUtils;
		int sumRUtils;
	}
	 
	 int allocatedElementCountForLastUtilityList = 0;
	 
	//=========== METHOD FOR ACCESSING INFORMATIONS ABOUT UTILITY-LISTS =========
	
	public UtilityListBuffer(int sumSupport, int itemCount) {
		elements = new ArrayList<Element>(sumSupport * 2);
		summaries = new ArrayList<Summary>(itemCount * 2);
	}
	
	public UtilityListBuffer() {
		elements = new ArrayList<Element>();
		summaries = new ArrayList<Summary>();
	}


	/**
	 * This method is to inform this class that we will work the i-th utility list
	 * in the buffer.
	 * @param utilityListIndex  the position of the utility list in the buffer (i)
	 */
	public void selectCurrentUtilityList(int utilityListIndex){
		currentSummary = summaries.get(utilityListIndex);
	}
	
	public int getSumIUtilCurrentUtilityList(){
		return currentSummary.sumIUtils;
	}
	
	public int getSumRUtilCurrentUtilityList(){
		return currentSummary.sumRUtils;
	}
	
	public int getItemCurrentUtilityList(){
		return currentSummary.item;
	}
	
	public int getElementCountCurrentUtilityList(){
		return currentSummary.endPos - currentSummary.startPos;
	}
	
	public Element getIthElementInCurrentUtilityList(int elementNumber){
		return elements.get(currentSummary.startPos + elementNumber);
	}
	
	//=========  METHODS FOR CREATING A NEW UTILITY-LIST  =====================//
	
	public void createANewUtilityList(int item, int utilityListIndex){
		// If we cannot reuse the memory
		// we create a new utility-list
		if(utilityListIndex >= summaries.size()){
			currentSummary = new Summary();
			summaries.add(currentSummary);
		}else{
			currentSummary =  summaries.get(utilityListIndex);
		}
		
		// we reset the information
		currentSummary.item = item;
		currentSummary.sumIUtils = 0;
		currentSummary.sumRUtils = 0;
		
		// if it is the first utility list
		if(utilityListIndex == 0){
			// we will store elements from position 0
			currentSummary.startPos = 0;
			currentSummary.endPos = 0;
		}else{
			// else
			// we will store elements after the previous utility list
			Summary previousUtilityList = summaries.get(utilityListIndex -1);
			currentSummary.startPos = previousUtilityList.endPos + allocatedElementCountForLastUtilityList;
			currentSummary.endPos = currentSummary.startPos;
		}
		
		// Reset allocated element count
		allocatedElementCountForLastUtilityList = 0;
	}
	
	public void addElementToCurrentUtilityList(int tid, int iutil, int rutil){
		int insertionPosition = currentSummary.endPos;
		
		// If we cannot reuse the memory
		// we create a new element
		if(insertionPosition >= elements.size()){
			elements.add(new Element(tid,iutil,rutil));
		}else{
			// otherwise we reuse the memory instead of creating new objects
			Element element = elements.get(insertionPosition);
			element.tid = tid;
			element.iutils = iutil;
			element.rutils = rutil;
		}
		
		// increase the sums
		currentSummary.sumIUtils += iutil;
		currentSummary.sumRUtils += rutil;
		
		currentSummary.endPos++;

	}

	// reserve some space for the elements in the buffer (for single items, since we know their support)
	public void allocateSpaceForElements(int support) {
		for(int i= 0; i < support; i++){
			elements.add(new Element());
		}
		allocatedElementCountForLastUtilityList = support;
		
	}
	
	//========================= BINARY SEARCH ==============
	/**
	 * Do a binary search to find the element with a given tid in a utility list
	 * @param ulist the utility list position in the buffer
	 * @return  the position or null if none has the tid.
	 */
	public Element findElementWithTIDCurrenUtilityList(int tid){
		
		// perform a binary search to check if  the subset appears in  level k-1.
        int first = currentSummary.startPos;
        int last = currentSummary.endPos  - 1;
       
        // the binary search
        while( first <= last )
        {
        	int middle = ( first + last ) >>> 1; // divide by 2

            if(elements.get(middle).tid < tid){
            	first = middle + 1;  //  the itemset compared is larger than the subset according to the lexical order
            }
            else if(elements.get(middle).tid > tid){
            	last = middle - 1; //  the itemset compared is smaller than the subset  is smaller according to the lexical order
            }
            else{
            	return elements.get(middle);  // IMPORTANT!!!!!!
            }
        }
		return null;
	}


	public void finishBuildingSingleItemsUtilityLists() {
		// reset that!
		allocatedElementCountForLastUtilityList = 0;
	}

	public void printToString() {
		System.out.println(" ====== ELEMENTS ======");
		for(int i=0; i< elements.size(); i++){
			System.out.println("tid = " + elements.get(i).tid +  " iutil " + elements.get(i).iutils +  " rutil " + elements.get(i).rutils);
		}
		System.out.println(" ====== UTILITY-lISTS ======");
		for(int i=0; i< summaries.size(); i++){
			Summary summary = summaries.get(i);
			System.out.println("item = " + summary.item   
					 + " start " + summary.startPos 
					 + " end " + summary.endPos 
					+  " sumI " + summary.sumIUtils
					+  " sumR " + summary.sumRUtils
					);
		}
	}
}
