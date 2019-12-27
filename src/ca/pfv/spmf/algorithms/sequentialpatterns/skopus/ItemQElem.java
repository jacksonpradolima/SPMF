package ca.pfv.spmf.algorithms.sequentialpatterns.skopus;

//�����͵�item��event�Ļ���ԭʼ
public class ItemQElem {
	public int item;
	public double ubvalue = 0;
	
	public boolean equals(final ItemQElem e){
		if(this.item != e.item)
			return false;

		return true;
	}
	
	public String toString() {
		String strResult = new String();
		strResult += "<";
		strResult += GlobalData.alItemName.get(item) ;
		strResult += ">";

		return strResult;
	}
}

