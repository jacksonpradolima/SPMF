package ca.pfv.spmf.algorithms.sequentialpatterns.skopus;
///*******************************************************************************
// * Copyright (C) 2015 Tao Li
// * 
// * This file is part of Skopus.
// * 
// * Skopus is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, version 3 of the License.
// * 
// * Skopus is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// * 
// * You should have received a copy of the GNU General Public License
// * along with Skopus.  If not, see <http://www.gnu.org/licenses/>.
// ******************************************************************************/

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class GlobalOper {
	private static ArrayList<Double> lf = new ArrayList<Double>(); 

	private static double logfact(final int n) {
		//static std::vector<double> lf;

		int i;

		for (i = lf.size(); i <= n; i++) 
		{
			if (i == 0)
				lf.add(.0);
			else
				lf.add(lf.get(i-1) + Math.log((double)(i)));
		}

		return lf.get(n);
	}

	// ����n��ѡk��������Ķ���?
	public static double log_combin(final int n, final int k) {
		return logfact(n) - logfact(k) - logfact(n - k);
	}
	
	public static double computeSupport(int nCoverCount){
		if(GlobalData.bSmoothedValue){
			return Double.valueOf(nCoverCount + GlobalData.dSmoothCoefficient) 
					/ Double.valueOf(GlobalData.nNumOfSequence + GlobalData.dSmoothCoefficient);
		}
		else{
			return Double.valueOf(nCoverCount) 
					/ Double.valueOf(GlobalData.nNumOfSequence);	
		}
	}
	
	public static double computeCoverCount(double nCoverCount){
		if(GlobalData.bSmoothedValue){
			return Double.valueOf(nCoverCount + GlobalData.dSmoothCoefficient);
		}
		else{
			return Double.valueOf(nCoverCount);	
		}
	}

	 /**
     * Append content string in the file
     *
     * @param fileName
     * @param content
     */
    public static void appendFileContent(String fileName, String content) {
        try {
            //��һ��д�ļ��������캯���еĵڶ�������true��ʾ��׷����ʽд�ļ�  
            FileWriter writer = new FileWriter(fileName, true);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
