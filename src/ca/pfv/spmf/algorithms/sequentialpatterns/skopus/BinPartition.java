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


public class BinPartition {
	private ItemsetRec irLeft;
	private ItemsetRec irRight;

	public BinPartition(ItemsetRec isParaLeft, ItemsetRec isParaRight){
		if(lessThan(isParaLeft, isParaRight)){
			irLeft = new ItemsetRec(isParaLeft);
			irRight = new ItemsetRec(isParaRight);
		}
		else{
			irLeft = new ItemsetRec(isParaRight);
			irRight = new ItemsetRec(isParaLeft);
		}
		return;
	}
	
	public ItemsetRec getLeft(){
		return irLeft;
	}
	
	public ItemsetRec getRight(){
		return irRight;
	}
	
	
	public boolean equals(Object obj){
		if((irLeft.equals(((BinPartition)obj).getLeft()))
				&&((irRight.equals(((BinPartition)obj).getRight())))){
			return true;
		}
		else{
			return false;
		}
	}
	
	public int hashCode() {
		return (irLeft.hashCode() + "|" + irRight.hashCode()).hashCode();
	}
	
	private boolean lessThan(ItemsetRec isParaLeft, ItemsetRec isParaRight){
		assert((isParaLeft.size()>0)&&(isParaRight.size()>0));
		
		if(isParaLeft.size() < isParaRight.size()){
			return true;
		}
		else if(isParaLeft.size() == isParaRight.size()){
			for(int i = 0; i<isParaLeft.size(); i++){
				int nl = (int)isParaLeft.get(i);
				int nr = (int)isParaRight.get(i);
				if(nl < nr){
					return true;
				}
				else if(nl > nr){
					return false;
				}
			}
		}
		return true;
	}
}
