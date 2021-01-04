package ca.pfv.spmf.algorithms.sequentialpatterns.qcsp.util;

/**
 * This is an implementation of the QCSP algorithm.
 * For more information please refer the paper Mining Top-K Quantile-based Cohesive Sequential Patterns 
 * by Len Feremans, Boris Cule and Bart Goethals, published in 2018 SIAM International Conference on Data Mining (SDM18).<br/>
 *
 * Copyright (c) 2020 Len Feremans (Universiteit Antwerpen)
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
 *
 * You should have received a copy of the GNU General Public License along wit
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Len Feremans
 */
public class Timer {

	public static boolean VERBOSE = true;
	private long start;
	private long intermediateStart;
	private String process;
	
	public Timer(String process){
		this.start = System.currentTimeMillis();
		this.intermediateStart = this.start;
		this.process = process;
		if(process.length() > 20){
			this.process = process.substring(0, 20) + "...";
		}
		if(VERBOSE)
			System.out.format(">Started %s\n", process);
	}
	
	public void progress(long i, long total){
		progress(null, i, total);
	}
	
	public void progress(String message, long i, long total){
		long end = System.currentTimeMillis();
		long elapsed = (end - intermediateStart);
		long elapsedTotal = (end - start);
		if(VERBOSE){
			String estimate = "";
			if(total<i)
				total = i;
			if(total > 10){
				//rule of three:
				//   if processing k items takes s milis
				//   then processing 1 item takes s/k milis
				//   and estimated time is (total - k) X s/k milis
				long estimatedMilis =  Math.round((total - i) * (elapsedTotal/(double)i)); 
				estimate = " Expected " +  Utils.milisToStringReadable(estimatedMilis);
			}
			System.out.format(" Process %s %s: %.2f %% items. Elapsed %s. Total %s.%s\n", 
				process, message == null? "": message, i/(double)total * 100,
				Utils.milisToStringReadable(elapsed),
				Utils.milisToStringReadable(elapsedTotal),
				estimate);
		}
		this.intermediateStart = System.currentTimeMillis(); 
	}
	
	
	public long end(){
		long end = System.currentTimeMillis();
		long elapsed = (end - start);
		if(VERBOSE)
			System.out.format("<Finished %s. Took %s\n", process, 
					Utils.milisToStringReadable(elapsed));
		return elapsed;
				
	}
	

}
