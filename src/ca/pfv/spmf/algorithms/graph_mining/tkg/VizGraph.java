package ca.pfv.spmf.algorithms.graph_mining.tkg;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/* This file is copyright (c) 2018 by Chao Cheng
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
 * This is a class to convert a result file to a Graphviz file.
 * @author Chao Cheng
 */
public class VizGraph {

	/**
	 * Convert a result file of gSpan to a DOT file for GraphViz visualization
	 * @param gPath the path to a result file
	 * @param outDir the resulting DOT files
	 * @throws IOException exception if error while reading/writing to file
	 */
    public static void visulizeFromFile(String gPath, String outDir) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(gPath));
        String line = br.readLine();
        while (line != null) {
            if (line.startsWith("t")) {
                int gId = Integer.parseInt(line.split(" ")[2]);
                StringBuilder sb = new StringBuilder();
                sb.append("graph G {").append(System.lineSeparator());
                while ((line = br.readLine()) != null && !line.startsWith("t")) {
                    if (line.startsWith("v")) {
                        String[] items = line.split(" ");
                        int v = Integer.parseInt(items[1]);
                        int vLabel = Integer.parseInt(items[2]);
                        sb.append(v).append("[label=").append("\"").append(v).append(":").append(vLabel).append("\"]");
                        sb.append(System.lineSeparator());
                    }
                    else if (line.startsWith("e")) {
                        String[] items = line.split(" ");
                        int v1 = Integer.parseInt(items[1]);
                        int v2 = Integer.parseInt(items[2]);
                        int eLabel = Integer.parseInt(items[3]);
                        sb.append(v1).append("--").append(v2).append("[label=\"").append(eLabel).append("\"]");
                        sb.append(System.lineSeparator());
                    }
                }
                sb.append("}").append(System.lineSeparator());
                String outPath = outDir + "/g" + gId + ".dot";
                BufferedWriter bw = new BufferedWriter(new FileWriter(outPath));
                bw.write(sb.toString());
                bw.close();
            }
        }
        br.close();
    }
}
