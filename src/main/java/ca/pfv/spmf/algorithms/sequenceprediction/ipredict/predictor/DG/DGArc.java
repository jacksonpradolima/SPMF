package ca.pfv.spmf.algorithms.sequenceprediction.ipredict.predictor.DG;
/*
 * This file is copyright (c) Ted Gueniche 
 * <ted.gueniche@gmail.com>
 *
 * This file is part of the IPredict project
 * (https://github.com/tedgueniche/IPredict).
 *
 * IPredict is distributed under The MIT License (MIT).
 * You may obtain a copy of the License at
 * https://opensource.org/licenses/MIT 
 */
/**
 * Represents an arc in a DG
 */
public class DGArc {

	public int dest; //Destination of this arc
	public int support; //Support for this arc

	/**
	 * Initialize a new arc to the destination with a support of 1
	 */
	public DGArc(int destination) {
		dest = destination;
		support = 1;
	}
	
}
