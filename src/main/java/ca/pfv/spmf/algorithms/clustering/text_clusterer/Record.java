package ca.pfv.spmf.algorithms.clustering.text_clusterer;
/* This file is copyright (c) 2014-2015 Sabarish Raghu
* 
* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
* 
* SPMF is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* 
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/
/**
 * 
 * @author Sabarish Raghu
 *
 */
public class Record {
public int recordId; //record/Attribute Id in the input file
String attribute;	//the text record in the input file
public double[] tfVector;
public int getRecordId() {
	return recordId;
}
public void setRecordId(int recordId) {
	this.recordId = recordId;
}
public String getAttribute() {
	return attribute;
}
public void setAttribute(String attribute) {
	this.attribute = attribute;
}
public double[] getTfVector() {
	return tfVector;
}
public void setTfVector(double[] tfVector) {
	this.tfVector = tfVector;
}
}
