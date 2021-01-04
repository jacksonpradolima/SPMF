/*
* This is an implementation of the CEPB, corCEPB, CEPN algorithm.
*
* 
* This file is part of the SPMF DATA MINING SOFTWARE * (http://www.philippe-fournier-viger.com/spmf).
*
* SPMF is free software: you can redistribute it and/or modify it under the * terms of the GNU General Public License as published by the Free Software * Foundation, either version 3 of the License, or (at your option) any later * version. *

* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR * A PARTICULAR PURPOSE. See the GNU General Public License for more details. *
*
* You should have received a copy of the GNU General Public License along with SPMF. If not, see <http://www.gnu.org/licenses/>.
*
* Copyright (c) 2019 Jiaxuan Li
*/

package ca.pfv.spmf.algorithms.sequentialpatterns.cost;

import java.util.HashMap;
import java.util.Map.Entry;

/**
 * This class is an implementation for transforming the sequences of events
 * represented by the text(String) into the number (Integer). For example, event
 * "Study_Es_6_1" is transformed into the unique number 1.<br>
 * When output the results, found cost-effective patterns, the number
 * representing the unique event will be transformed back to the text. The
 * reason of the transformation is that it is more easier to process the data in
 * the program.
 * 
 * @author Jiaxuan Li
 * @see AlgoCEPM
 */
public class DataMapper {

	/**
	 * the one-to-one correspondence between the event's representation String and
	 * Integer
	 */
	private static HashMap<String, Integer> keyPair = new HashMap<>();

	/**
	 * Getting the unique label of the event
	 * 
	 * @param key event's name
	 * @return event's label in the program
	 */
	public static Integer mapKV(String key) {
		Integer value = 0;
		if (!keyPair.containsKey(key)) {
			value = (keyPair.size());
			keyPair.put(key, value);
		} else {
			value = keyPair.get(key);
		}
		return value;

	}

	/**
	 * Getting event's name
	 * 
	 * @param value the unique label representing the event
	 * @return event event's name
	 */
	public static String getKey(Integer value) {
		for (Entry<String, Integer> ent : keyPair.entrySet()) {
			if (ent.getValue().equals(value))
				return ent.getKey();
		}

		return "*-1*";
	}

}
