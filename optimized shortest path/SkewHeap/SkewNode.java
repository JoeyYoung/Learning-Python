package SkewHeap;

public class SkewNode {
    /* distance */
    public int dst;
    public int id;
    /* children and parent */
    SkewNode left = null;
    SkewNode right = null;
    SkewNode parent = null;

    public SkewNode(int dst, int id){
        this.dst = dst;
        this.id = id;
    }
}
