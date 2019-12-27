package ca.pfv.spmf.algorithms.sequentialpatterns.skopus;
///*******************************************************************************
// * Copyright (C) 2015 Tao Li
// * 
// * This file is part of Skopus.
// * 
// * Skopus is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, version 3 of the License.
// * 
// * Skopus is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// * 
// * You should have received a copy of the GNU General Public License
// * along with Skopus.  If not, see <http://www.gnu.org/licenses/>.
// ******************************************************************************/
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

//item��event���������?
//ע�⣺�������Ԫ�غ�ʹ��Ԫ��֮ǰ����Ҫ����sort()��������֤��������
public class ItemQClass extends ArrayList<ItemQElem> {
	//���캯��
	public ItemQClass()
	{
		this.clear();
	}

	public void insert(ItemQElem elem){

		assert(elem.item < GlobalData.nNumOfItems);
		
		int i = indexOf(elem);
		if (i < 0) {
			this.add(elem);
		}
	}
	
	public void insert(int nIxItem){
		if (nIxItem >= GlobalData.nNumOfItems)
		{
			return;
		}
		int i = indexOf(nIxItem);
		if (i < 0){
			ItemQElem elem = new ItemQElem();
			elem.item = nIxItem;
			elem.ubvalue = GlobalData.alSids.get(nIxItem).size();
			this.add(elem);
			//Collections.sort(this, new ItemQElemSortByNumber());
		}
	}
	
	public void insert(int nIxItem, double dValue){
		if (nIxItem >= GlobalData.nNumOfItems)
		{
			return;
		}
		int i = indexOf(nIxItem);
		if (i < 0){
			ItemQElem elem = new ItemQElem();
			elem.item = nIxItem;
			elem.ubvalue = dValue;
			this.add(elem);
			//Collections.sort(this, new ItemQElemSortByNumber());
		}
	}
	
	public void sort()
	{
		Collections.sort(this, new ItemQElemSortByNumber());
		return;
	}
	
	public int indexOf(final int ixItem) {
		int nResult = -1;
		for (int i = 0; i < this.size(); i++) {
			if(this.get(i).item == ixItem){
				nResult = i;
				break;
			}
		}
		return nResult;
	}
	
	public int indexOf(final ItemQElem elem) {
		int nResult = -1;
		for (int i = 0; i < this.size(); i++) {
			if(elem.equals(this.get(i))){
				nResult = i;
				break;
			}
		}
		return nResult;
	}
	
	
	public String toString() {
		String strResult = new String();
		strResult += "{";
		if (this.size() > 0) {
			for (int i = 0; i < this.size() - 1; i++) {
				strResult += GlobalData.alItemName.get(this.get(i).item) + "("
						+ this.get(i).ubvalue + "), ";
			}
			strResult += GlobalData.alItemName
					.get(this.get(this.size() - 1).item)
					+ "("
					+ this.get(this.size() - 1).ubvalue + ")";
		}
		strResult += "}";

		return strResult;
	}
	
}

class ItemQElemSortByNumber implements Comparator<Object> {
	public int compare(Object o1, Object o2) {
		ItemQElem s1 = (ItemQElem) o1;
		ItemQElem s2 = (ItemQElem) o2;
		if (s1.ubvalue < s2.ubvalue) {
			return 1;
		} else if (s1.ubvalue == s2.ubvalue) {
			return 0;
		} else {
			return -1;
		}
	}
}

