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
import java.util.HashSet;

public class CombinSet extends HashSet<ItemsetRec> {
	private static final long serialVersionUID = 1L;

	public CombinSet(int n){
		super((int)Math.ceil(n * 1.5));
	}
	
	public void createAllCombin(ItemsetRec irLeft,
			ItemsetRec irRight,
			CombinTemplate ctTemp){
		assert ((irLeft.size() == ctTemp.getLeftLength()) 
				&& (irRight.size() == ctTemp.getRightLength()));
		
		if(((irLeft.size() != ctTemp.getLeftLength()) 
				|| (irRight.size() != ctTemp.getRightLength()))){
			this.clear();
			return;
		}
		
		for(int ixTemplate = 0; ixTemplate < ctTemp.size(); ixTemplate++){
			ItemsetRec irResult = new ItemsetRec(irLeft.size() + irRight.size());
			int ixLeft = 0;
			int ixRight = 0;
			Byte[] naTemp = ctTemp.get(ixTemplate);
			for(int i = 0; i< naTemp.length; i++){
				if(naTemp[i]== 0){
					irResult.add(irLeft.get(ixLeft));
					ixLeft++;
				}
				else{
					irResult.add(irRight.get(ixRight));
					ixRight++;
				}
			}//for(int i = 0; i<naTemp.length; i++)
			if(irResult.size() == (irLeft.size() + irRight.size())){
				this.add(irResult);
			}

		}//for(int ixTemplate = 0; ixTemplate < ctTemp.size(); ixTemplate++
		
	}
}
