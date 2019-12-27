package ca.pfv.spmf.algorithms.frequentpatterns.sppgrowth;
public class Support_maxla {
    int support = 0;
    int maxla = 0;

    Support_maxla(){}

    public void increaseSupport(){
        this.support++;
    }

    public int getSupport(){
        return this.support;
    }

    public void setMaxla(int current_la){
        this.maxla = Math.max(this.maxla,current_la);
    }


    public int getMaxla(){
        return this.maxla;
    }

}
