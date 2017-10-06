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
public class SimilarRecords {
int record1Pos; //index of the first record
int record2Pos; //index of the second record
double similarity; //similarity measure between the 2 records
public int getRecord1Pos() {
	return record1Pos;
}
public void setRecord1Pos(int record1Pos) {
	this.record1Pos = record1Pos;
}
public int getRecord2Pos() {
	return record2Pos;
}
public void setRecord2Pos(int record2Pos) {
	this.record2Pos = record2Pos;
}
public double getSimilarity() {
	return similarity;
}
public void setSimilarity(double similarity) {
	this.similarity = similarity;
}
}
