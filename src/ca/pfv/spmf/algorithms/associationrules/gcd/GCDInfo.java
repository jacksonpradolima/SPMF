/**
 * Warranty disclaimer: This software is provided 'as-is', without any express or implied warranty.    
 * In no event will the author(s) be held liable for any damages arising from the use of this software.  
 * 
 * Copyrights (c) 2015 to Ahmed El-Serafy (a.elserafy@ieee.org) and Hazem El-Raffiee (hazem.farouk.elraffiee@gmail.com)  
 * 
 * All of the files that are part of the GCDs Association Rules algorithm are licensed under either GPL v.3 or dual-licensed under the following terms.  
 * 
 * 1- Any use of the provided source code must be preceded by a written authorization from one of the author(s).  
 * 2- The license text must be kept in source files headers.   
 * 3- The use of the provided source code must be acknowledged in the project documentation and any consequent presentations or documents.   
 * This is achieved by referring to the original repository (https://bitbucket.org/aelserafy/gcd-association-rules)  
 * 4- Any enhancements introduced to the provided algorithm must be shared with the original author(s) along with its source code and changes log.   
 * This is if you are building directly or indirectly upon the algorithm provided by the original author(s).  
 * 5- The public availability of the new source code is provided upon agreement with the original author(s).  
 * 6- For commercial distribution and use, a license agreement must be obtained from one of the author(s).  
*/

package ca.pfv.spmf.algorithms.associationrules.gcd;

public class GCDInfo {
	private MyBigInteger gcd;
	private int frequency;

	public GCDInfo(MyBigInteger gcd) {
		this.gcd = gcd;
		frequency = 0;
	}

	public MyBigInteger getGCD() {
		return gcd;
	}

	public Integer getFrequency() {
		return frequency;
	}

	public void incrementFrequency(int count) {
		frequency += count;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof GCDInfo)
			return gcd.equals(((GCDInfo) obj).gcd);
		else if (obj instanceof MyBigInteger)
			return gcd.equals(obj);
		else
			return false;
	}

	@Override
	public int hashCode() {
		return gcd.hashCode();
	}

	@Override
	public String toString() {
		return gcd.toString();
	}
}
