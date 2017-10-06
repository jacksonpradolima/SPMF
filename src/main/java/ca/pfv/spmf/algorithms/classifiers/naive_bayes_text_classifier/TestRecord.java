package ca.pfv.spmf.algorithms.classifiers.naive_bayes_text_classifier;
import java.util.ArrayList;

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

public class TestRecord {
private int recordId;
private String fullRecord;

private ArrayList<String> words;
public int getRecordId() {
	return recordId;
}
public ArrayList<String> getWords() {
	return words;
}	
public void setWords(ArrayList<String> words) {
	this.words = words;
}
public void setRecordId(int recordId) {
	this.recordId = recordId;
}
public String getFullRecord() {
	return fullRecord;
}
public void setFullRecord(String fullRecord) {
	this.fullRecord = fullRecord;
}
}
