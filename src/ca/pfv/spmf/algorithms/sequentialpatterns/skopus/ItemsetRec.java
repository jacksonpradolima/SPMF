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

//��ǰ�������ģʽ
public class ItemsetRec extends ArrayList<Integer>{
	private static final long serialVersionUID = -8974421858134023255L;
	public int count; // ��������		(�������ڼ���support)
	public double value; // ��Ȥ�ȶ���ֵ
	public boolean self_sufficient; // filter itemsets�����У�����ϡ������㡱�����ı��?
	

	public ItemsetRec() {
		this.clear();
		count = 0;
		value = 0.0;
		self_sufficient = false;
	}
	
	public ItemsetRec(int nLength){
		super(nLength);
		this.clear();
		count = 0;
		value = 0.0;
		self_sufficient = false;
	}

	/**
	 * copy constructor
	 * */
	public ItemsetRec(ItemsetRec ir){
		super(ir.size());
		this.clear();
		this.addAll(ir);

		this.count = ir.count;
		this.value = ir.value;
		this.self_sufficient = ir.self_sufficient;
		
	}
	
	//ʹ���ir��ָ��λ�õ�Ԫ�س�ʼ��һ���µ����������ʼλ�õ�Ԫ��
	public ItemsetRec(ItemsetRec ir, int nBeginIX, int nEndIX){
		super(ir.size());
		this.clear();
		if ((nBeginIX > nEndIX) || (nBeginIX < 0) ||(nEndIX >= ir.size())){
			return;
		}
		for(int ix = nBeginIX; ix <= nEndIX; ix++){
			this.add(ir.get(ix));
		}
		
	}

	
	/**
	//���������������Ŀ��һ��������true
	 */
	public boolean isAllSame(){
		if(this.size() <=1 )
			return true;
		for(int i = 1; i < this.size(); i++){
			if(!this.get(i).equals(this.get(i-1)))
				return false;
		}
		return true;
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof ItemsetRec))return false;
		ItemsetRec list = (ItemsetRec) o;
		if(this.size()!=list.size())return false;
		for (int i = 0; i < this.size(); i++) {
			if(!this.get(i).equals(list.get(i)))return false;
		}
		return true;
	}
	
	
	public String toString() {

		String strResult = new String();
		if (this.size() > 0) {
			for (int i = 0; i < this.size() - 1; i++) {
				strResult += GlobalData.alItemName.get(this.get(i)) + " -1 ";
			}
			strResult += GlobalData.alItemName.get(this.get(this
					.size() - 1)) + " -1 ";
		}

		strResult += "#SUP: "+count;
		
		if(GlobalData.nInterestingnessMeasure == 2){
			strResult += " #LEVERAGE: " +  value;
		}
		
		return strResult;
	}
	
}
