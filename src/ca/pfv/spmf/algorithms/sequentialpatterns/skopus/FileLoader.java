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
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class FileLoader {

	public void loadData(String strParaFileName) throws UnsupportedEncodingException {
		Map<String, Integer> itemstrs = new HashMap<String, Integer>();

		try {
			BufferedReader br = new BufferedReader(
					new InputStreamReader(new FileInputStream(strParaFileName), "UTF-8"));

			try {
				// ���ж�ȡ�ļ���������
				String strTheLine = null;
				while ((strTheLine = br.readLine()) != null) {

					if (!strTheLine.trim().isEmpty()) {
						// if the line is  a comment, is  empty or is a
						// kind of metadata
						if (strTheLine.charAt(0) == '#' || strTheLine.charAt(0) == '%'
										|| strTheLine.charAt(0) == '@') {
							continue;
						}
						
						// System.out.println(strTheLine);
						GlobalData.nNumOfSequence++;
						parseOneLine(strTheLine.trim(), GlobalData.nNumOfSequence, itemstrs);
					} // if(!strTheLine.isEmpty())
				} // while ((strTheLine = br.readLine()) != null)

				GlobalData.dSampleAverageLength /= (GlobalData.nNumOfSequence * 1.0);
				assert(GlobalData.nNumOfItems == GlobalData.alItemName.size());
				assert(GlobalData.nNumOfItems == GlobalData.alSids.size());

			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	


	private void parseOneLine(String strParaLine, int nParaNumofTran, Map<String, Integer> itemstrs) {

		int nPosition = 0;
		
		// split the sequence according to spaces into tokens
		for(String token:  strParaLine.split(" ")){
			if(token.equals("-1") == false && token.equals("-2") == false){  
				//-------------------------------------------------
				// find its id
				int thisid;

				if (itemstrs.containsKey(token)) {
					thisid = itemstrs.get(token);
				} else {
					// if it doesn't have an id, assign one
					thisid = GlobalData.alItemName.size();
					itemstrs.put(token, thisid);
					GlobalData.alItemName.add(token);
					GlobalData.nNumOfItems = GlobalData.alItemName.size();
				}

				nPosition++;

				// Update sids[thisid].addItem(nParaNumofTran, nPosition);
				if (thisid >= GlobalData.alSids.size()) {
					SidSet ss = new SidSet();
					ss.addItem(nParaNumofTran, nPosition);
					GlobalData.alSids.add(ss);

				} else {
					GlobalData.alSids.get(thisid).addItem(nParaNumofTran, nPosition);
				}
				assert(GlobalData.alSids.size() == GlobalData.alItemName.size());
				//-------------------------------------------------
			}
		}

		GlobalData.alSequenceLengthList.add(nPosition);
		GlobalData.dSampleAverageLength += nPosition;

		if (nPosition > GlobalData.nSampleMaxLength) {
			GlobalData.nSampleMaxLength = nPosition;
		}

		return;
	}
}
