package ca.pfv.spmf.algorithms.sequentialpatterns.qcsp.util;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
/**
 * This is an implementation of the QCSP algorithm.
 * For more information please refer the paper Mining Top-K Quantile-based Cohesive Sequential Patterns 
 * by Len Feremans, Boris Cule and Bart Goethals, published in 2018 SIAM International Conference on Data Mining (SDM18).<br/>
 *
 * Copyright (c) 2020 Len Feremans (Universiteit Antwerpen)
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
 *
 * You should have received a copy of the GNU General Public License along wit
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Len Feremans
 */

/**
 * Some "low-level" code, to prevent reading a file at once.
 * Idea: read data buffered, and return tokens, one by one, update buffer when required. 
 * Seperator can be '\n' or ' ' or ... 
 * 
 * Note: Similar to BufferReader, but there the seperator is always '\n', hence if all data is on a on single line...
 * E.g.
 * FileStream stream = new FileStream(new File("very-big-file.txt"), '\n');
 * String token = s.nextToken();
 * while(token != null){
 *   ...
 *   token = s.nextToken();
 * }
 * 
 * 
 * @author lfereman
 *
 */
public class FileStream {

	private Reader reader;
	private char[] seperators = new char[]{' ','\n'};
	
	private char buff[] = new char[16*1024];
	private int len = 0;
	private int start =0;
	
	public FileStream(File input,  char... seperators) throws IOException{
		this.reader = new FileReader(input);
		this.seperators = seperators;
	}
	
	public void setBufferSize(int size){
		this.buff = new char[size];
	}
	
	/**
	 * returns null of EOF
	 * 
	 * @return
	 * @throws IOException
	 */
	public String nextToken() throws IOException{
		//check if EOF
		if(reader == null)//EOF
			return null;
		if(start == len){ //buffer empty
			if(!readFromBuff())
				return null;
		}
		//search for seperator in buffer
		int endIndex = indexOf(buff, seperators, start, len);
		if(endIndex != -1){
			char[] token = Arrays.copyOfRange(buff, start, endIndex);
			start = endIndex+1;
			//read ahead, and continue after first non-seperator character (e.g. if stuff like "abc \n \naba", continue on a, after abc)
			for(; start<len; start++){
				if(indexOf(seperators,buff[start]) == -1)
					break;
			}
			String s = new String(token);	
			return s;
		}
		//seperator not found, read more data
		else{
			//load more data into buffer
			char[] previous = Arrays.copyOfRange(buff, start, len);
			StringBuffer bufferString = new StringBuffer();
			bufferString.append(previous);
			//stop if EOF
			while(true){//repeat until seperator found (normally if buffer length is small)
				if(!readFromBuff())
					return bufferString.toString();
				//find seperator again: assuming len(token) << len(buffer) !?!
				endIndex = indexOf(buff, seperators, start, len);
				if(endIndex == -1){
					bufferString.append(buff);
					continue;
				}
				char[] token = Arrays.copyOfRange(buff, start, endIndex);
				start = endIndex+1;
				//read ahead, and continue after first non-seperator character (e.g. if stuff like "abc \n \naba", continue on a, after abc)
				for(; start<len; start++){
					if(indexOf(seperators,buff[start]) == -1)
						break;
				}
				bufferString.append(token);
				return bufferString.toString();
			}
		}	
	}

	private boolean readFromBuff() throws IOException {
		len = reader.read(buff);
		start = 0;
		if(len == -1){
			len = 0;
			reader.close();
			reader = null;
			return false;
		}
		return true;
	}
	
	private static final int indexOf(char[] data, char[] targets, int start, int end){
		for(int i=start; i<end; i++)
			if(indexOf(targets,data[i]) != -1)
				return i;
		return -1;
	}
	
	private static final int indexOf(char[] data, char target){
		for(int i=0; i<data.length; i++)
			if(data[i] == target)
				return i;
		return -1;
	}
}
