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

public class MemoryFile {
String classname;
String fileName;
ArrayList<String> content;
public String getClassname() {
	return classname;
}
public void setClassname(String classname) {
	this.classname = classname;
}
public String getFileName() {
	return fileName;
}
public void setFileName(String fileName) {
	this.fileName = fileName;
}
public ArrayList<String> getContent() {
	return content;
}
public void setContent(ArrayList<String> content) {
	this.content = content;
}
}
