package ca.pfv.spmf.gui.patternvizualizer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.ParseException;

/*
 * Copyright (c) 2008-2015 Philippe Fournier-Viger
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
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * Class for testing the pattern vizualizer window using the "output.txt" file provided in this directory.
 * 
 * @author Philippe Fournier-Viger
 */
public class MainTestPatternVizualizer {

	public static void main(String[] args) throws ParseException, IOException {
		// the path of the file containing patterns for this test
		String patternFilePath = fileToPath("test.txt");
		
		// create the frame
		PatternVizualizer frame = new PatternVizualizer(patternFilePath);
		frame.setVisible(true);
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestPatternVizualizer.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
