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

public class BinPartitionTemplate extends ArrayList<Byte[]>{
	private int nMLength = 0;
	
	public BinPartitionTemplate(int nLength) {
		if (nLength < 2) {
			this.clear();
			return;
		}
		nMLength = nLength;

		int nTotalSize = 0;
		for (int i = 1; i <= nMLength / 2; i++) {
			nTotalSize += (int) Math.ceil(Math.exp(GlobalOper.log_combin(
					nMLength, i)));
		}
		this.ensureCapacity(nTotalSize + 2);

		createAllPartition();
		return;
	}

	private void createAllPartition(){
		this.clear();
		for (int ix = 1; ix <= nMLength / 2; ix++) {
			Combination c = new Combination(nMLength, ix);
			while (c.hasNext()) {
				int[] a = c.next();
				// System.out.println(Arrays.toString(a));
				//Integer[] naTemplate = new Integer[nMLength];
				Byte[] naTemplate = new Byte[nMLength];
				for (int i = 0; i < naTemplate.length; i++) {
					naTemplate[i] = 1;
				}
				for (int i = 0; i < a.length; i++) {
					naTemplate[a[i]] = 0;
				}
				this.add(naTemplate);
			}// while (c.hasNext())
		}//for (int ix = 0; ix <= nMLength / 2; ix++) 
		return;
	}
	
	public int getLength(){
		return nMLength;
	}

}
