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

public class BinPartitionSet extends HashSet<BinPartition>{
	public BinPartitionSet(int n){
		super((int)Math.ceil(n * 1.5));
		return;
	}
	
	public void createAllPartition(ItemsetRec irParaParent,
			BinPartitionTemplate bpt){
		assert(irParaParent.size() == bpt.getLength());
		if(irParaParent.size() != bpt.getLength()){
			this.clear();
			return;
		}

		for(int ixTemplate = 0; ixTemplate < bpt.size(); ixTemplate++){
			ItemsetRec irLeft = new ItemsetRec(irParaParent.size());
			ItemsetRec irRigh = new ItemsetRec(irParaParent.size());
			Byte[] naTemp = bpt.get(ixTemplate);
			for(int i = 0; i<naTemp.length; i++){
				if(naTemp[i] == 0){
					irLeft.add(irParaParent.get(i));
				}
				else{
					irRigh.add(irParaParent.get(i));
				}
			}//for(int i = 0; i<naTemp.length; i++)
			BinPartition bp = new BinPartition(irLeft, irRigh);
			this.add(bp);
		}
		
		
		return;
	}

}
