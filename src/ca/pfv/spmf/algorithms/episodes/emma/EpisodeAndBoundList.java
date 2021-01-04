package ca.pfv.spmf.algorithms.episodes.emma;

import java.util.List;

// NEW FOR TKE
public class EpisodeAndBoundList implements Comparable<EpisodeAndBoundList> {
	
	Episode episode;
	List<int[]> boundlist;
	
	public EpisodeAndBoundList(Episode episode, List<int[]> boundlist) {
		this.episode = episode;
		this.boundlist = boundlist;
	}
	
	   /**
     * Compare this pattern with another pattern
     * @param o another pattern
     * @return 0 if equal, -1 if smaller, 1 if larger (in terms of support).
     */
    public int compareTo(EpisodeAndBoundList o) {
		if(o == this){
			return 0;
		}
		long compare =  episode.support - o.episode.support;
		if(compare > 0){
			return 1;
		}
		if(compare < 0){
			return -1;
		}
		return 0;
	}

}
