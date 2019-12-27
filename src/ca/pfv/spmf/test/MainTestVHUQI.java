package ca.pfv.spmf.test;
/* This file is copyright (c) Philippe Fournier-Viger 2018
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.vhuqi.AlgoVHUQI;
import ca.pfv.spmf.algorithms.frequentpatterns.vhuqi.enumVHUQIMethod;

/**
 * This is an example of how to run the VHUQI algorithm and save the result to
 * a file
 * 
 * @author Philippe Fournier-Viger 2018
 *
 */
public class MainTestVHUQI {

	public static void main(String[] args) throws IOException {
		
		//Input path for database :
		String inputFileDBPath = fileToPath("HUQI_DB.txt");
		
		// Input path for profit table
		String inputFileProfitPath = fileToPath("HUQI_DB_Profit.txt");
		
		// Output path for discovered patterns 
		String outputPath = "output.txt";
		
		// Minimum utility threshold 
		float minUtility = 0.1f;
		
		//Related quantitative coefficient
		int relativeCoefficient = 7;
		
		// Combination method: ALLC MINC OR MAXC
		enumVHUQIMethod method = enumVHUQIMethod.MAXC;
		
		AlgoVHUQI algo = new AlgoVHUQI();
		algo.runAlgorithm(inputFileDBPath, inputFileProfitPath, outputPath, minUtility, relativeCoefficient, method);
		
		algo.printStatistics();
	}

	

	public static String fileToPath(String filename) throws UnsupportedEncodingException {
		URL url = MainTestVHUQI.class.getResource(filename);
		return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
	}
}
