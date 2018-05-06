package Fibonacci;

import java.util.ArrayList;
import java.util.List;

public class FibonacciHeap {
    public FibonacciHeap(){}

    /* the min entry in the entry list
     * the only entry for Fibonacci
     */
    public HeapEntry min = null;

    /* merge two entries into the Fib list
     * the return obj will represent the smaller node of the two
     */
    private HeapEntry mergeEntry(HeapEntry h1, HeapEntry h2){
        /* if any one of the two entries is empty,
         * just return null or the not empty one
         */
        if(h1 == null && h2 == null)
            return null;
        else if(h1 == null)
            return h2;
        else if(h2 == null)
            return h1;

        /* both not null */
        /* link the two entry list */
        HeapEntry tmp = h1.next;
        h1.next = h2.next;
        h1.next.pre = h1;
        h2.next = tmp;
        h2.next.pre = h2;

        /* min entry is the only entry for Fibonacci, so return the smaller one */
        if(h1.dst < h2.dst)
            return h1;
        else
            return h2;
    }

    /* insert one node with certain value for user */
    public HeapEntry insertNode(int id, int dst){
        HeapEntry newEntry = new HeapEntry(id, dst);

        /* merge it with the entry list */
        min = mergeEntry(min, newEntry);

        return newEntry;
    }

    /* pop up the min element of the whole Fib List */
    public HeapEntry popMin(){
        if(min == null){
            System.out.println("Error: Unable to Find Min");
            return null;
        }

        /* get the min entry of the Fib List (back up) */
        HeapEntry minEntry = min;

        /* if min is the only node in the heap set to be null, */
        if(min.next == min)
            min = null;
        /* else, remove the min link, and re-assignment min */
        else{
            min.pre.next = min.next;
            min.next.pre = min.pre;
            min = min.next;
        }

        /* if the old min node has its children
         * clear the min's children's parent fields by traversal
         * because the whole list is a circle
         */
        HeapEntry now;
        if(minEntry.child != null)
            for(now = minEntry.child; now.next != minEntry.child; now = now.next)
                now.parent = null;


        /* merge the children */
        min = mergeEntry(min, minEntry.child);

        /* break out */
        if(min == null)
            return minEntry;

        /* Every degree should only occur one time
         * position index means the degree
         */
        List<HeapEntry> degTable = new ArrayList<>();

        /* visit the element stored in order */
        List<HeapEntry> visit = new ArrayList<>();

        /* add all the item until item visited twice,
         * the start point visit[] can be null
         */
        HeapEntry cur;
        for(cur = min; visit.isEmpty() || visit.get(0) != cur; cur = cur.next){
            visit.add(cur);
        }

        /* visit items in order and combine if necessary */
        for(HeapEntry i : visit){
            while(true){

                while(i.degree >= degTable.size())
                    degTable.add(null);

                /* done, the degree can be matched */
                if(degTable.get(i.degree) == null){
                    degTable.set(i.degree, i);
                    break;
                }

                /* already been occupied, clear to null and do the merge */
                HeapEntry occupied = degTable.get(i.degree);
                degTable.set(i.degree, null);

                /* get the smaller min root */
                HeapEntry tmin;
                HeapEntry tmax;
                if(occupied.dst < i.dst){
                    tmin = occupied;
                    tmax = i;
                }
                else{
                    tmin = i;
                    tmax = occupied;
                }

                /* remove the max and relink */
                tmax.next.pre = tmax.pre;
                tmax.pre.next = tmax.next;

                /* self link the max as entry */
                tmax.pre = tmax;
                tmax.next = tmax;
                tmin.child = mergeEntry(tmin.child, tmax);

                tmax.parent = tmin;

                tmax.marked = false;

                tmin.degree++;

                i = tmin;
            }
            /* chose the min one */
            if(i.dst <= min.dst)
                min = i;
        }
        /* the return value should be the old min */
        return minEntry;
    }

    /* update the value with a smaller distance
     * used in the process of relax
     */
    public void decreaseDst(HeapEntry entry, int nDst){
        entry.dst = nDst;
        if(entry.parent != null && entry.dst <= entry.parent.dst)
            cascadingCut(entry);
        /* choose the smaller as the entry of Fibonacci */
        if(entry.dst <= min.dst)
            min = entry;
    }

    /* cut certain node from its parents */
    private void cascadingCut(HeapEntry entry){
        entry.marked = false;
        /* in the top layer list */
        if(entry.parent == null)
            return;
        /* break link between its list */
        if(entry.next != entry){
            entry.next.pre = entry.pre;
            entry.pre.next = entry.next;
        }

        /* Entry is child entry of parent, relink children */
        if(entry.parent.child == entry){
            if(entry.next != entry)
                entry.parent.child = entry.next;
            else
                entry.parent.child = null;
        }
        /* child cut, reduce the degree */
        entry.parent.degree--;

        entry.pre = entry;
        entry.next = entry;
        min = mergeEntry(min, entry);

        /* if parent has already lost child,
         * it is marked, and we will recursively cut it from its parent
         */
        if(entry.parent.marked)
            cascadingCut(entry.parent);
        /* or set is as marked */
        else
            entry.parent.marked = true;
        /* clear parent field */
        entry.parent = null;
    }
}
