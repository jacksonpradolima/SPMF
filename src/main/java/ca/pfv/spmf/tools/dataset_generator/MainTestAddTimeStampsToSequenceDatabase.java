package ca.pfv.spmf.tools.dataset_generator;
/* This file is copyright (c) 2008-2012 Philippe Fournier-Viger
* 
* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
* 
* SPMF is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

/**
 * Example of how to add timestamps automatically to a sequence database
 * in SPMF format
 */
public class MainTestAddTimeStampsToSequenceDatabase {
	
	public static void main(String [] arg) throws IOException{
		
		String inputFile = fileToPath("contextPrefixspan.txt");
		String outputFile = ".//output.txt";
		
		AddTimeStampsToSequenceDatabase converter = new AddTimeStampsToSequenceDatabase();
		converter.convert(inputFile, outputFile);
	}

	

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestAddTimeStampsToSequenceDatabase.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
