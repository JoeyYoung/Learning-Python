package MinHeap;

import java.util.HashMap;

public class MinHeap {

    private class HeapNode{
        /* represent the unique node in Graph */
        public int id;
        /* distance from source of every node */
        public int dst;

        HeapNode(int id, int dst){
            this.id = id;
            this.dst = dst;
        }
    }

    /* maxsize based on the test cases */
    private Integer maxSize = 30000000;

    /* store the nodes
     * map: id-distance
     * idx represents the position in the heap
     */
    private HeapNode[] heapArray = new HeapNode[maxSize];

    /* add the map from id to position
     * valid position <= size
     */
    private HashMap<Integer, Integer> id2pos;

    /* size of the heap */
    private int size;

    /* top [0] */
    private final int NINF = -1;

    /* sink the top node down
     * exchange the min child node up
     * size not changed
     * O(logV)
     */
    private void sinkDown(int position){
        int cur = position;
        int smallChild;
        /* while not leaf */
        while(cur <= size/2){
            /* determine the smaller one */
            smallChild = cur*2;
            if((smallChild+1) <= size
                    && heapArray[smallChild+1].dst < heapArray[smallChild].dst)
                smallChild = smallChild+1;
            if(heapArray[cur].dst <= heapArray[smallChild].dst)
                return;
            swapNode(cur, smallChild);
            cur = smallChild;
        }
    }

    /* popup the last item after insert a new node
     * parent = cur/2
     * left = cur*2, right = cur*2 + 1
     * leaf: > size/2 && <= size
     * size not changed
     * O(logV)
     */
    private void popUp(int position){
        int cur = position;
        while(heapArray[cur].dst < heapArray[cur/2].dst){
            swapNode(cur, cur/2);
            cur = cur/2;
        }
    }

    /* carry function
     * change the position of two nodes
     */
    private void swapNode(int pos1, int pos2){
        int tmpID = heapArray[pos2].id;
        int tmpDst = heapArray[pos2].dst;
        heapArray[pos2].id = heapArray[pos1].id;
        heapArray[pos2].dst = heapArray[pos1].dst;
        heapArray[pos1].id = tmpID;
        heapArray[pos1].dst = tmpDst;

        /* update the map from id 2 position */
        id2pos.put(heapArray[pos1].id, pos1);
        id2pos.put(heapArray[pos2].id, pos2);
    }


    /* input the node id Graph and current dst */
    /* O(logV) */
    public void insertNode(int id, int dst){
        heapArray[++size] = new HeapNode(id, dst);
        /* init the node space in map */
        id2pos.put(id, size);
        popUp(size);
    }

    /* get the min node when been visited */
    public int getMin(){
        swapNode(1, size);
        size--;
        if(size != 0)
            sinkDown(1);
        return heapArray[size+1].id;
    }

    /* get size */
    public int getSize(){
        return this.size;
    }

    /* get the dst of one certain node */
    public int getDst(int id){
        return heapArray[id2pos.get(id)].dst;
    }

    /* update the dst and adjust the minheap */
    public void setDst(int id, int newDst){
        int position = id2pos.get(id);
        heapArray[position].dst = newDst;
        popUp(position);
        position = id2pos.get(id);
        sinkDown(position);
    }

    /* init size & position 0 */
    public MinHeap(){
        id2pos = new HashMap<>();
        size = 0;
        heapArray[0] = new HeapNode(0, NINF);
    }

}
