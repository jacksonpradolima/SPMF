package ca.pfv.spmf.algorithms.timeseries;

/* This file is copyright (c) 2008-2016 Philippe Fournier-Viger
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
import ca.pfv.spmf.patterns.cluster.DoubleArrayInstance;

/**
 * This class represent a time-series.
 * It simply extends the DoubleArrayInstance class.
 * In the future it could contain other information, specifically
 *  relevant for a time series.
 * 
 * @author Philippe Fournier-Viger, 2016 */
public class TimeSeries extends DoubleArrayInstance{


	/**
	 * Constructor
	 * @param dataPoints the data points of the time series
	 */
	public TimeSeries(double[] dataPoints, String name){
		super(dataPoints,name);
	}

}
