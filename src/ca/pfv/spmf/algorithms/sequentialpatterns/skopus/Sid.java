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

public class Sid extends ArrayList<Integer>{
	private int nSidNumber;

	public Sid() {
		super(20);
		//this.clear();
	}

	public Sid(int n) {
		super(20);
		nSidNumber = n;
	}
	
	//current Sid, and current position value
	public Sid(int nSid, int val){
		super(1);
		nSidNumber = nSid;
		this.add(val);
	}

	public Sid(Sid copy){
		super(copy.size());
		this.nSidNumber = copy.nSidNumber;
		this.addAll(copy);
		return;
	}

	public int getSidNumber(){
		return this.nSidNumber;
	}
	
	public void addPosition(int sno, int pos) {
		if (sno == nSidNumber) {
			if (this.isEmpty()) {
				this.add(pos);
			} else {
				if (this.indexOf(pos) < 0) {
					// û�ҵ�
					this.add(pos);
					// sort
					Collections.sort(this);
				} else {
					// �ҵ���
				}
			}
		}// if (sno == nSidNumber)

	}

	// �ҵ���һ��λ�ã�������λ���±ꣻδ�ҵ����򷵻�-1
	public int getNextPosition(Sid s) {
		int pos = -1;
		if (this.nSidNumber != s.nSidNumber) {
			return -1;
		}
		if (this.size() < 1) {
			return -1;
		}
		for (int i = 0; i < s.size(); i++) {
			if (this.get(0) < s.get(i)) {
				pos = s.get(i);
				break;
			}
		}
		return pos;
	}

	public boolean lessThan(final Sid s) {
		return nSidNumber < s.nSidNumber;
	}

	public boolean isEqual(final Sid s) {
		return nSidNumber == s.nSidNumber;
	}

	// destructively update Position
//	public void updatePosition(int pos) {
//		this.clear();
//		this.add(pos);
//	}

	
}