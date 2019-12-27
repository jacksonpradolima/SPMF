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


//���һ�����ģ��
public class CombinTemplate extends ArrayList<Byte[]>{
	private static final long serialVersionUID = -7394207193842094281L;
	private int nLeft = 0;
	private int nRight = 0;
	
	public CombinTemplate(int nLeftLength, int nRightLength){
		//assert(nLeftLength <= nRightLength);
		super((int)Math.ceil(
				Math.exp(
						GlobalOper.log_combin(
								nLeftLength+nRightLength, nLeftLength))));
		if(nLeftLength <= nRightLength){
			nLeft = nLeftLength;
			nRight = nRightLength;
		}
		else{
			nLeft = nRightLength;
			nRight = nLeftLength;
		}

		createAllTemplate();
	}
	
	private void createAllTemplate(){
		this.clear();
		Combination c = new Combination(nLeft + nRight, nLeft);
		while (c.hasNext()) {
			int[] a = c.next();
			//System.out.println(Arrays.toString(a));
			//Integer[] naTemplate = new Integer[nLeft + nRight];
			Byte[] naTemplate = new Byte[nLeft + nRight];
			for(int i = 0; i<naTemplate.length; i++){
				naTemplate[i] = 1;
			}
			for(int i = 0; i< a.length; i++){
				naTemplate[a[i]] = 0;
			}
			this.add(naTemplate);
		}
	}

	public int getLeftLength(){
		return nLeft;
	}
	
	public int getRightLength(){
		return nRight;
	}
}

