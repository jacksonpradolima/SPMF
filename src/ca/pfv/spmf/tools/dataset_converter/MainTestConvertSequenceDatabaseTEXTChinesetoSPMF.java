package ca.pfv.spmf.tools.dataset_converter;

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
import java.nio.charset.Charset;

/**
 * Example of how to convert a text document to a sequence database in the
 * SPMF format.
 * <br/><br/>
 * 
 * A file name "example.text" is provided. It contains a text documents with a few sentences.<br/><br/>
 * 
 * "The SPMF software is a data mining platform that can be used to discover patterns in data. It has
been downloaded by many data mining researchers. SPMF can be used to discover patterns such as itemsets and sequential rules in data.
This file contains text data, and it is used to show how SPMF can be utilized to  find pattern in text documents.
The text document will be automatically transformed into the SPMF format, which can then be analyzed by SPMF to find interesting patterns"
 * <br/><br/>
 * 
 * The result of the conversion process is a sequence database in SPMF format, which is similar to this (here only the first lines are shown):
 * <br/><br/>
 * 
 * @CONVERTED_FROM_TEXT
 * @ITEM=1=the
 * @ITEM=2=spmf
 * @ITEM=3=software
 * @ITEM=4=is
 * @ITEM=5=a
 * @ITEM=6=data
 * @ITEM=7=mining
 * @ITEM=8=platform
 * @ITEM=9=that
 * @ITEM=10=can
 * @ITEM=11=be
 * @ITEM=12=used
 * @ITEM=13=to
 * @ITEM=14=discover
 * @ITEM=15=patterns
 * @ITEM=16=in
 * 1 -1  2 -1  3 -1  4 -1  5 -1  6 -1  7 -1  8 -1  9 -1  10 -1  11 -1  12 -1  13 -1  14 -1  15 -1  16 -1  6 -1  -2
 * @ITEM=17=it
 * @ITEM=18=has
 * @ITEM=19=been
 * @ITEM=20=downloaded
 * @ITEM=21=by
 * @ITEM=22=many
 * @ITEM=23=researchers
 * 17 -1  18 -1  19 -1  20 -1  21 -1  22 -1  6 -1  7 -1  23 -1  -2
 * 
 * The first line indicates that the file was converted from text. Then each following lines indicates the mapping between
 * items and words. For example, the second line here indicates that the item 1 represents the word "the". The following line
 * indicates that the item 2 represents the word "spmf".  After that, there are also some lines that represents sequences corresponding
 * to sentences. For example, the line: <br/>
 * 1 -1  2 -1  3 -1  4 -1  5 -1  6 -1  7 -1  8 -1  9 -1  10 -1  11 -1  12 -1  13 -1  14 -1  15 -1  16 -1  6 -1  -2<br/>
 * represents the first sentence of the original text. Each positive integer is a word. And each word is separated by -1. Moreover,
 * the end of the sentence is indicated by -2. This is the standard format for sequence databases in the SPMF library.
 */
class MainTestConvertSequenceDatabaseTEXTChinesetoSPMF {

	public static void main(String[] arg) throws IOException {

		//a file that is a text document
		String inputFile = fileToPath("chinese.text"); 
		
		//the resulting converted file in SPMF format
		String outputFile = ".//output.txt";  
		
		// the format of the input file (TEXT)
		Formats inputFileformat = Formats.TEXT;
		
		// the number of sequence from the input file to be converted
		int sequenceCount = Integer.MAX_VALUE;  
		
		// Create a converter
		SequenceDatabaseConverter converter = new SequenceDatabaseConverter();
		
		// Call the method to convert the input file from TEXT to the SPMF format
		converter.convert(inputFile, outputFile, inputFileformat, sequenceCount, Charset.defaultCharset());
	}

	public static String fileToPath(String filename)
			throws UnsupportedEncodingException {
		URL url = MainTestConvertSequenceDatabaseTEXTChinesetoSPMF.class
				.getResource(filename);
		return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
	}
}
