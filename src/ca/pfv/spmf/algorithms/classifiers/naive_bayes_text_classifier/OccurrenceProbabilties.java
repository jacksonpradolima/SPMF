package ca.pfv.spmf.algorithms.classifiers.naive_bayes_text_classifier;
import java.util.HashMap;

/** * * * This is an implementation of the Naive Bayes Document Classifier algorithm. 
* 
* Copyright (c) 2014 Sabarish Raghu
* 
* This file is part of the SPMF DATA MINING SOFTWARE * (http://www.philippe-fournier-viger.com/spmf). 
* 
* 
* SPMF is free software: you can redistribute it and/or modify it under the * terms of the GNU General Public License as published by the Free Software * Foundation, either version 3 of the License, or (at your option) any later * version. * 

* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR * A PARTICULAR PURPOSE. See the GNU General Public License for more details. * 
* 
* You should have received a copy of the GNU General Public License along with * SPMF. If not, see . 
* 
* @author SabarishRaghu
*/
public class OccurrenceProbabilties {
	private String className;
	private HashMap<String,Double> occuranceMap;
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public HashMap<String, Double> getOccuranceMap() {
		return occuranceMap;
	}
	public void setOccuranceMap(HashMap<String, Double> occuranceMap) {
		this.occuranceMap = occuranceMap;
	}
}
