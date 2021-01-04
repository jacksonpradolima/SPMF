package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.MRCPPS.AlgoMRCPPS;

/*
 * This file is part of the SPMF DATA MINING SOFTWARE *
 * (http://www.philippe-fournier-viger.com/spmf).
 *
 * SPMF is free software: you can redistribute it and/or modify it under the *
 * terms of the GNU General Public License as published by the Free Software *
 * Foundation, either version 3 of the License, or (at your option) any later *
 * version. SPMF is distributed in the hope that it will be useful, but WITHOUT
 * ANY * WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright Peng Yang, Philippe Fournier-Viger, 2019
 */
public class MainTestMRCPPS {
    public static void main(String[] args) throws IOException {
        // the Input and output files
        String inputFile = fileToPath("contextPrefixSpan.txt");
        String outputFile = "output.txt";

        // The algorithm parameters:
        int maxSup = 2;
        double maxStd = 1;
        double minBond = 0.5;
        double minRa = 0.5;
        
        // Activate some optimization called Lemma 2 from the paper
        // (it is recommended to leave this parameter to true unless for doing some experiments)
        boolean useLemma2 = true;

        // whether convert the transaction database to a sequential database or not
        boolean needGroup = false;

        // if needGroup = true, how many transactions can be grouped to make a sequence
        int groupNum = 0;

        AlgoMRCPPS algo = new AlgoMRCPPS();
        algo.runAlgorithm(inputFile,outputFile,maxSup, maxStd, minBond, minRa, useLemma2, needGroup, groupNum);
        algo.printStats();
    }

    public static String fileToPath(String filename) throws UnsupportedEncodingException {
        URL url = MainTestMRCPPS.class.getResource(filename);
        return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
    }
}
