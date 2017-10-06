package ca.pfv.spmf.algorithms.frequentpatterns.mffi_miner;

/* This is an implementation of the MFFI-Miner algorithm. 
* 
* Copyright (c) 2016 MFFI-Miner
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

class MFFIRegions {
		float low = 0;// low region
		float middle = 0;// middle region
		float high = 0;// high region
	
	MFFIRegions(int quanaity){
		//calculate the regions value
		if(quanaity >=0 &&quanaity <= 1){
			this.low = 1;
			this.middle=0;
			this.high = 0;
		}else if(quanaity==2){
			this.low = (float) 0.5;
			this.middle = (float) 0.6666667;
			this.high = 0; 
		}else if ( quanaity == 3){
			this.low = 0;
			this.middle = (float) 0.6666667 ;
			this.high = (float) 0.5;
		}else{
			this.low = 0;
			this.middle = 0;
			this.high = 1;
		}
	}
}
