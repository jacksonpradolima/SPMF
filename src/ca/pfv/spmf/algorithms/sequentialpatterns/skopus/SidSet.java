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

public class SidSet extends ArrayList<Sid>{

	public SidSet() {
		super(20);
	}

	public SidSet(int nSize){
		super(nSize);
	}
	
	public SidSet(SidSet copy){
		super(copy.size());
		//this.clear();
		this.addAll(copy);
		return;
	}
	
	public void copyFrom(SidSet copy){
		this.clear();
		this.ensureCapacity(copy.size());
		this.addAll(copy);
		return;
	}
	
	@SuppressWarnings("unchecked")
 	public void addItem(int sidNum, int p) {
		if (this.isEmpty()) // if there is no sid, insert this one
		{
			Sid c = new Sid(sidNum);
			c.addPosition(sidNum, p);
			this.add(c);
		} else // if there are any sid, find it and insert this one
		{
			int ix = getIndex(sidNum);

			if (ix < 0) // not found
			{
				Sid c = new Sid(sidNum);
				c.addPosition(sidNum, p);
				this.add(c);
			} else // found
			{
				this.get(ix).addPosition(sidNum, p);
			}
			Collections.sort(this, new SidSortByNumber());
		}

	}

	//����sid�ı�ţ��ҵ�����sidset�е��±�λ��
	public int getIndex(int sidNum){
		int ix = -1;
		for(int i = 0; i <this.size(); i++){
			if(this.get(i).getSidNumber() == sidNum)
			{
				ix = i;
				break;
			}
		}
		return ix;
		
	}

}
