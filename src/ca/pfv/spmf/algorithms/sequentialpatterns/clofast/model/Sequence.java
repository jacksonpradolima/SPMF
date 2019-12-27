package ca.pfv.spmf.algorithms.sequentialpatterns.clofast.model;

import java.util.Iterator;
import java.util.LinkedList;

/* 
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

/**
 * @author Eliana Salvemini
 */
public class Sequence implements Iterable<Itemset>, Cloneable {

    // an array list which containsItemset in each position an itemset of the sequence
    private LinkedList<Itemset> elements = new LinkedList<Itemset>();

    /**
     * @param itemset
     */
    public Sequence(Itemset... itemset) {
        for (Itemset i : itemset) {
            elements.add(i);
        }
    }

    /**
     * Append a new itemset to the end of the sequence This is used for S-Step
     * extension
     * <p/>
     * <pre>
     * eg. ({a,b},{a,c,d},{a}) + {d} = ({a,b},{a,c,d},{a}, {d})
     * </pre>
     *
     * @param element new itemset
     */
    public void add(Itemset element) {
        elements.add(element);
    }

    /**
     * Return the last item in the last itemset
     *
     * @return the last itemset in the sequence
     */
    public String getLastItem() {
        return getLastItemset().getLast();
    }

    public String toString() {
        StringBuffer buff = new StringBuffer();
        for (Itemset itemset : elements) {
            buff.append(itemset.concatenate());
            buff.append(" -1 ");
        }
        return buff.toString() + "-2";
    }


    /**
     * @return the last itemset of a list
     */
    public Itemset getLastItemset() {
        return elements.getLast();
    }

    /**
     * Get the length of the sequence. It is defined as the number of itemsets
     * in the sequence.
     *
     * @return the length of the sequence
     */
    public int length() {
        return elements.size();
    }

    /**
     * Produce a deep copy of the Sequence
     *
     * @return deep copy of the Sequence
     */
    public Sequence clone() {
        Sequence other = new Sequence();
        for (Itemset itemset : elements) {
            other.add(itemset.clone());
        }
        return other;
    }

    @Override
    public Iterator<Itemset> iterator() {
        return elements.iterator();
    }

    public LinkedList<Itemset> getElements() {
        return elements;
    }

    public boolean containsItemset(Itemset itemset) {
        return elements.stream().anyMatch(i -> i.contains(itemset));
    }

    /**
     * @param other
     * @return true if the current sequence contains the other sequence
     */
    public boolean contains(Sequence other) {

        if (elements.size() < other.elements.size())
            return false;

        int matchIndex = 0;

        //checks for sequence containment
        for (Itemset itemset : other) {

            int nextIndex = -1;

            for (int i = matchIndex; i < elements.size(); i++) {
                if (elements.get(i).contains(itemset)) {
                    nextIndex = i;
                    break;
                }
            }

            //nextIndex == -1 if it is not found in this.getElements
            if (nextIndex == -1)
                return false;
            else matchIndex = nextIndex + 1;

        }

        return true;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sequence)) return false;

        Sequence sequence = (Sequence) o;

        if (elements != null ? !elements.equals(sequence.elements) : sequence.elements != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return elements != null ? elements.hashCode() : 0;
    }
}
