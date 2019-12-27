package ca.pfv.spmf.algorithms.sequentialpatterns.uhuspm;

/* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
*
* SPMF is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* SPMF is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with SPMF.  If not, see <http://www.gnu.org/licenses/>.
* 
* @Copyright Ting Li et al., 2018
*/
/**
 * This is an implementation of an element, as used by the UHUSPM algorithm.
 * 
 * @see AlgoUHUSPM
 * @author Ting Li
 */
public class Element {
		/** sequence identifier */
		int SID;
		
		/** location in the sequence */
		int location;
		
		/** utility */
		int utility;
		
		/** probability */
		float probability;
		
		/** SWU value */
		int SWU;
		
		/**
		 * Constructor
		 * @param SID sequence identifier
		 * @param location location in the sequence
		 * @param utility utility
		 * @param probability probability
		 * @param SWU SWU value
		 */
		public Element(int SID, int location, int utility, float probability, int SWU) {
			// TODO Auto-generated constructor stub
				this.SID = SID;
				this.location = location;
				this.utility = utility;
				this.probability = probability;
				this.SWU = SWU;
		}
}
