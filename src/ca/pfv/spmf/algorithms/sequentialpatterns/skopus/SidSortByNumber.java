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
import java.util.Comparator;

@SuppressWarnings("rawtypes")
public class SidSortByNumber implements Comparator {

	public int compare(Object o1, Object o2) {
		Sid s1 = (Sid) o1;
		Sid s2 = (Sid) o2;
		if (s1.getSidNumber() > s2.getSidNumber()) {
			return 1;
		} else if (s1.getSidNumber() == s2.getSidNumber()) {
			return 0;
		} else {
			return -1;
		}
	}
}
