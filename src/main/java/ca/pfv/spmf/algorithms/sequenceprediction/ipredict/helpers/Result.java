package ca.pfv.spmf.algorithms.sequenceprediction.ipredict.helpers;

import java.util.HashMap;
/*
 * This file is copyright (c) Ted Gueniche 
 * <ted.gueniche@gmail.com>
 *
 * This file is part of the IPredict project
 * (https://github.com/tedgueniche/IPredict).
 *
 * IPredict is distributed under The MIT License (MIT).
 * You may obtain a copy of the License at
 * https://opensource.org/licenses/MIT 
 */
//Represent a list of statistics and their values
public class Result {
	private HashMap<String, Double> data;
	
	public Result() {
		data = new HashMap<String, Double>();
	}
	
	public Double get(String stat) {
		
		if(data.get(stat) == null)
			data.put(stat, 0.0);
		
		return data.get(stat);
	}
	
	public void set(String stat, Double value) {
		data.put(stat, value);
	}
	
}
