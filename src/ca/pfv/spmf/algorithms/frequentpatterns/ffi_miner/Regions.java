package ca.pfv.spmf.algorithms.frequentpatterns.ffi_miner;

/* This is an implementation of the FFI-Miner algorithm. 
* 
* Copyright (c) 2016 FFI-Miner
* 
* This file is part of the SPMF DATA MINING SOFTWARE * (http://www.philippe-fournier-viger.com/spmf). 
* 
* 
* SPMF is free software: you can redistribute it and/or modify it under the * terms of the GNU General Public License as published by the Free Software * Foundation, either version 3 of the License, or (at your option) any later * version. * 

* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR * A PARTICULAR PURPOSE. See the GNU General Public License for more details. * 
* 
* You should have received a copy of the GNU General Public License along with * SPMF. If not, see . 
* 
* @author Ting Li
*/

class Regions {
		float low = 0;// low region
		float middle = 0;// middle region
		float high = 0;// high region
	
	Regions(int quantity, int regionsNumber){
		//calculate the regions value
		if(regionsNumber==3){
			// if there are 3 regions
			if (quantity > 0 && quantity<=1){
				this.low = 1;
				this.high = 0;
				this.middle = 0;
			}else if (quantity >= 1 && quantity<6){
				this.low = (float) (-0.2 * quantity + 1.2);
				this.middle = (float) (0.2*quantity - 0.2);
				this.high = 0;
			}else if (quantity >= 6 && quantity<11){
				this.low = 0;
				this.middle = (float) (-0.2*quantity + 2.2);
				this.high = (float) (0.2*quantity - 1.2);
			}else{
				this.low = 0;
				this.middle = 0;
				this.high = 1;
			}
		}

		if(regionsNumber==2){
			// if there are 2 regions
			this.middle = 0;
			if(quantity > 0 && quantity<=1){
				this.low = 1;
				this.high = 0;
			}else if(quantity > 1 && quantity<=11){
				this.low = (float) (-0.1*quantity + 1.1);
				this.high = (float) (0.1*quantity  - 0.1);
			}else{
				this.low = 0;
				this.high = 1;
			}
		}
	}
}
